package org.javersion.store.jdbc;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.javersion.store.jdbc.DocumentVersionStoreJdbcTest.mapOf;
import static org.javersion.store.sql.QEntity.entity;

import java.util.List;

import javax.annotation.Resource;

import org.javersion.core.Revision;
import org.javersion.core.VersionNotFoundException;
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

    private final String
            docId1 = randomId(),
            docId2 = randomId();
    private final Revision
            rev1 = new Revision(),
            rev2 = new Revision(),
            rev3 = new Revision(),
            rev4 = new Revision();

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
            EntityUpdateBatch<String, String> update = entityVersionStore.updateBatch(randomId());
            update.addVersion(randomId(), ObjectVersionGraph.init(ObjectVersion.<String>builder().build()).getTip());
            update.execute();
            return null;
        });
    }

    @Test(expected = VersionNotFoundException.class)
    public void throws_exception_if_since_revision_is_not_found() {
        entityVersionStore.fetchUpdates(docId2, rev1);
    }

    @Test
    public void save_read_and_update_flow() {

        create_first_two_versions_of_doc1();

        cannot_bulk_load_before_publish();

        create_doc2_and_update_doc1();

        fetch_updates_of_doc1();

        fetch_updates_of_doc2();

        bulk_load_after_publish();

        optimize_doc1();
    }

    @Test
    public void save_and_load_version_comment() {
        final String comment = "Comment metadata";
        final String docId = randomId();
        transactionTemplate.execute(status -> {
            EntityUpdateBatch<String, String> update = entityVersionStore.updateBatch(docId);
            ObjectVersion<String> version = ObjectVersion.<String>builder()
                    .meta(comment)
                    .build();
            update.addVersion(docId, ObjectVersionGraph.init(version).getTip());
            update.execute();

            return null;
        });
        ObjectVersionGraph<String> graph = entityVersionStore.load(docId);
        assertThat(graph.getTip().getMeta()).isEqualTo(comment);
    }

    private void create_first_two_versions_of_doc1() {
        transactionTemplate.execute(status -> {
            EntityUpdateBatch<String, String> update = entityVersionStore.updateBatch(docId1);
            assertThat(update.contains(docId1)).isTrue();
            assertThat(update.contains(randomId())).isFalse();
            assertThat(update.isCreate(docId1)).isTrue();
            assertThat(update.isUpdate(docId1)).isFalse();

            ObjectVersion<String> v1 = ObjectVersion.<String>builder(rev1)
                    .changeset(mapOf(
                            "id", docId1,
                            "name", "name of " + docId1))
                    .build();
            ObjectVersionGraph<String> graph = ObjectVersionGraph.init(v1);
            update.addVersion(docId1, graph.getTip());
            assertThat(update.isCreate(docId1)).isFalse();
            assertThat(update.isUpdate(docId1)).isTrue();

            ObjectVersion<String> v2 = ObjectVersion.<String>builder(rev2)
                    .parents(rev1)
                    .changeset(mapOf(
                            "name", "Fixed name"))
                    .build();
            graph = graph.commit(v2);
            update.addVersion(docId1, graph.getTip());
            update.execute();

            return null;
        });

        ObjectVersionGraph<String> graph = entityVersionStore.load(docId1);
        assertThat(graph.getTip().getProperties()).isEqualTo(mapOf(
                "id", docId1,
                "name", "Fixed name"
        ));

        String persistedName = queryFactory.from(entity).where(entity.id.eq(docId1)).singleResult(entity.name);
        assertThat(persistedName).isEqualTo("Fixed name");
    }

    private void cannot_bulk_load_before_publish() {
        FetchResults<String, String> graphs = entityVersionStore.load(asList(docId1, docId2));
        assertThat(graphs.isEmpty()).isTrue();
        assertThat(graphs.size()).isEqualTo(0);
    }

    private void create_doc2_and_update_doc1() {
        transactionTemplate.execute(status -> {
            EntityUpdateBatch<String, String> update = entityVersionStore.updateBatch(asList(docId1, docId2));
            ObjectVersionGraph<String> graph = entityVersionStore.load(docId1);
            assertThat(graph.isEmpty()).isFalse();

            // Create doc2
            ObjectVersion<String> v3 = ObjectVersion.<String>builder(rev3).changeset(mapOf("name", "doc2")).build();
            update.addVersion(docId2, ObjectVersionGraph.init(v3).getTip());

            // Update doc1
            ObjectVersion<String> v4 = ObjectVersion.<String>builder(rev4).parents(rev2).changeset(mapOf("name", "doc1")).build();

            update.addVersion(docId1, graph.commit(v4).getTip());

            update.execute();
            return null;
        });
    }

    private void fetch_updates_of_doc1() {
        List<ObjectVersion<String>> updates = entityVersionStore.fetchUpdates(docId1, rev1);
        assertThat(updates).hasSize(2);
        assertThat(updates.get(0).revision).isEqualTo(rev2);
        assertThat(updates.get(1).revision).isEqualTo(rev4);
    }

    private void fetch_updates_of_doc2() {
        List<ObjectVersion<String>> updates = entityVersionStore.fetchUpdates(docId2, rev3);
        assertThat(updates).isEmpty();
    }

    private void bulk_load_after_publish() {
        entityVersionStore.publish();

        FetchResults<String, String> graphs = entityVersionStore.load(asList(docId1, docId2));
        assertThat(graphs.containsKey(docId1)).isTrue();
        assertThat(graphs.containsKey(docId2)).isTrue();

        assertThat(graphs.getVersionGraph(docId1).getTip().getProperties()).isEqualTo(mapOf(
                "id", docId1,
                "name", "doc1"
        ));

        assertThat(graphs.getVersionGraph(docId2).getTip().getProperties()).isEqualTo(mapOf(
                "name", "doc2"
        ));

        String persistedName = queryFactory.from(entity).where(entity.id.eq(docId1)).singleResult(entity.name);
        assertThat(persistedName).isEqualTo("doc1");

        persistedName = queryFactory.from(entity).where(entity.id.eq(docId2)).singleResult(entity.name);
        assertThat(persistedName).isEqualTo("doc2");
    }

    private void optimize_doc1() {
        entityVersionStore.optimize(docId1, v -> v.revision.equals(rev4));
        ObjectVersionGraph<String> graph = entityVersionStore.load(docId1);
        assertThat(graph.versionNodes.size()).isEqualTo(1);
    }

    private String randomId() {
        return randomUUID().toString();
    }
}
