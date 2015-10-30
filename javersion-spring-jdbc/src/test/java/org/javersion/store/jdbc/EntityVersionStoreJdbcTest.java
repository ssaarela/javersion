package org.javersion.store.jdbc;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.javersion.store.jdbc.DocumentVersionStoreJdbcTest.mapOf;
import static org.javersion.store.sql.QEntity.entity;

import javax.annotation.Resource;

import org.javersion.core.Revision;
import org.javersion.object.ObjectVersion;
import org.javersion.object.ObjectVersionGraph;
import org.javersion.store.PersistenceTestConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.support.TransactionTemplate;

import com.mysema.query.sql.SQLQueryFactory;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = PersistenceTestConfiguration.class)
public class EntityVersionStoreJdbcTest {

    @Resource
    CustomEntityVersionStore entityVersionStore;

    @Resource
    TransactionTemplate transactionTemplate;

    @Resource
    SQLQueryFactory queryFactory;

    @Test
    public void should_return_empty_graph_if_not_found() {
        assertThat(entityVersionStore.load(randomId()).isEmpty()).isTrue();
    }

    @Test(expected = IllegalStateException.class)
    public void must_lock_entity_before_update() {
        transactionTemplate.execute(status -> {
            EntityUpdateBatch<String, Void> update = entityVersionStore.updateBatch(randomId());
            update.addVersion(randomId(), ObjectVersionGraph.init(ObjectVersion.<Void>builder().build()).getTip());
            update.execute();
            return null;
        });
    }

    @Test
    public void save_read_and_update_flow() {
        final String
                docId1 = randomId(),
                docId2 = randomId();
        final Revision
                rev1 = new Revision(),
                rev2 = new Revision(),
                rev3 = new Revision(),
                rev4 = new Revision();

        create_first_two_versions_of_doc1(docId1, rev1, rev2);

        ObjectVersionGraph<Void> graph = entityVersionStore.load(docId1);
        assertThat(graph.getTip().getProperties()).isEqualTo(mapOf(
                "id", docId1,
                "name", "Fixed name"
        ));

        String persistedName = queryFactory.from(entity).where(entity.id.eq(docId1)).singleResult(entity.name);
        assertThat(persistedName).isEqualTo("Fixed name");

        cannot_bulk_load_before_publish(docId1, docId2);

        create_doc2_and_update_doc1(docId1, docId2, rev2, rev3, rev4);

        entityVersionStore.publish();

        FetchResults<String, Void> graphs = entityVersionStore.load(asList(docId1, docId2));
        assertThat(graphs.containsKey(docId1)).isTrue();
        assertThat(graphs.containsKey(docId2)).isTrue();

        assertThat(graphs.getVersionGraph(docId1).getTip().getProperties()).isEqualTo(mapOf(
                "id", docId1,
                "name", "doc1"
        ));

        assertThat(graphs.getVersionGraph(docId2).getTip().getProperties()).isEqualTo(mapOf(
                "name", "doc2"
        ));

        persistedName = queryFactory.from(entity).where(entity.id.eq(docId1)).singleResult(entity.name);
        assertThat(persistedName).isEqualTo("doc1");

        persistedName = queryFactory.from(entity).where(entity.id.eq(docId2)).singleResult(entity.name);
        assertThat(persistedName).isEqualTo("doc2");
    }

    private String randomId() {
        return randomUUID().toString();
    }

    private void create_first_two_versions_of_doc1(String docId1, Revision rev1, Revision rev2) {
        transactionTemplate.execute(status -> {
            EntityUpdateBatch<String, Void> update = entityVersionStore.updateBatch(docId1);
            assertThat(update.contains(docId1)).isTrue();
            assertThat(update.contains(randomId())).isFalse();
            assertThat(update.isCreate(docId1)).isTrue();
            assertThat(update.isUpdate(docId1)).isFalse();

            ObjectVersion<Void> v1 = ObjectVersion.<Void>builder(rev1)
                    .changeset(mapOf(
                            "id", docId1,
                            "name", "name of " + docId1))
                    .build();
            ObjectVersionGraph<Void> graph = ObjectVersionGraph.init(v1);
            update.addVersion(docId1, graph.getTip());
            assertThat(update.isCreate(docId1)).isFalse();
            assertThat(update.isUpdate(docId1)).isTrue();

            ObjectVersion<Void> v2 = ObjectVersion.<Void>builder(rev2)
                    .parents(rev1)
                    .changeset(mapOf(
                            "name", "Fixed name"))
                    .build();
            graph = graph.commit(v2);
            update.addVersion(docId1, graph.getTip());
            update.execute();

            return null;
        });
    }

    private void cannot_bulk_load_before_publish(String docId1, String docId2) {
        FetchResults<String, Void> graphs = entityVersionStore.load(asList(docId1, docId2));
        assertThat(graphs.isEmpty()).isTrue();
        assertThat(graphs.size()).isEqualTo(0);
    }

    private void create_doc2_and_update_doc1(String docId1, String docId2, Revision rev2, Revision rev3, Revision rev4) {
        transactionTemplate.execute(status -> {
            EntityUpdateBatch<String, Void> update = entityVersionStore.updateBatch(asList(docId1, docId2));
            ObjectVersionGraph<Void> graph = entityVersionStore.load(docId1);
            assertThat(graph.isEmpty()).isFalse();

            // Create doc2
            ObjectVersion<Void> v3 = ObjectVersion.<Void>builder(rev3).changeset(mapOf("name", "doc2")).build();
            update.addVersion(docId2, ObjectVersionGraph.init(v3).getTip());

            // Update doc1
            ObjectVersion<Void> v4 = ObjectVersion.<Void>builder(rev4).parents(rev2).changeset(mapOf("name", "doc1")).build();

            update.addVersion(docId1, graph.commit(v4).getTip());

            update.execute();
            return null;
        });
    }
}
