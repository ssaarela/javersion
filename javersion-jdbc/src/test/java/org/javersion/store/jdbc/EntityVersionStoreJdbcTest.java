package org.javersion.store.jdbc;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.javersion.store.jdbc.VersionStatus.ACTIVE;
import static org.javersion.store.jdbc.VersionStatus.REDUNDANT;
import static org.javersion.store.jdbc.VersionStatus.SQUASHED;
import static org.javersion.store.sql.QEntity.entity;
import static org.javersion.store.sql.QEntityVersion.entityVersion;
import static org.javersion.store.sql.QEntityVersionParent.entityVersionParent;
import static org.javersion.store.sql.QEntityVersionProperty.entityVersionProperty;

import java.util.List;

import javax.annotation.Resource;

import org.javersion.core.VersionNotFoundException;
import org.javersion.object.ObjectVersion;
import org.javersion.object.ObjectVersionGraph;
import org.javersion.store.PersistenceTestConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.querydsl.sql.SQLQueryFactory;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = PersistenceTestConfiguration.class)
public class EntityVersionStoreJdbcTest extends AbstractVersionStoreTest {

    private final String
            docId1 = randomId(),
            docId2 = randomId();

    @Resource
    CustomEntityVersionStore entityStore;

    @Resource
    SQLQueryFactory queryFactory;

    @Test
    public void should_return_empty_graph_if_not_found() {
        assertThat(entityStore.load(randomId()).isEmpty()).isTrue();
    }

    @Test(expected = IllegalStateException.class)
    public void must_lock_entity_before_update() {
        transactionTemplate.execute(status -> {
            EntityUpdateBatch<String, String, JEntityVersion<String>> update = entityStore.updateBatch(randomId());
            update.addVersion(randomId(), ObjectVersionGraph.init(ObjectVersion.<String>builder().build()).getTip());
            update.execute();
            return null;
        });
    }

    @Test(expected = VersionNotFoundException.class)
    public void throws_exception_if_since_revision_is_not_found() {
        entityStore.fetchUpdates(docId2, rev1);
    }

    @Test
    public void save_read_and_update_flow() {

        create_first_two_versions_of_doc1();

        cannot_bulk_load_before_publish();

        create_doc2_and_update_doc1();

        fetch_updates_of_doc1();

        fetch_updates_of_doc2();

        bulk_load_after_publish();

        prune_doc1();
    }

    @Test
    public void save_and_load_version_comment() {
        final String comment = "Comment metadata";
        final String docId = randomId();
        transactionTemplate.execute(status -> {
            EntityUpdateBatch<String, String, JEntityVersion<String>> update = entityStore.updateBatch(docId);
            ObjectVersion<String> version = ObjectVersion.<String>builder()
                    .meta(comment)
                    .build();
            update.addVersion(docId, ObjectVersionGraph.init(version).getTip());
            update.execute();

            return null;
        });
        ObjectVersionGraph<String> graph = entityStore.load(docId);
        assertThat(graph.getTip().getMeta()).isEqualTo(comment);
    }

    @Override
    protected void verifyRedundantRelations() {
        // Redundant parents of inactive versions are removed
        assertThat(queryFactory
                .from(entityVersion)
                .innerJoin(entityVersion._entityVersionParentParentRevisionFk, entityVersionParent)
                .where(entityVersion.status.eq(SQUASHED), entityVersionParent.status.eq(REDUNDANT))
                .fetchCount())
                .isEqualTo(0);
        // Verify that inverse is true: there exists redundant parents on ACTIVE versions
        assertThat(queryFactory
                .from(entityVersion)
                .innerJoin(entityVersion._entityVersionParentParentRevisionFk, entityVersionParent)
                .where(entityVersion.status.eq(ACTIVE), entityVersionParent.status.eq(REDUNDANT))
                .fetchCount())
                .isGreaterThan(0);

        // Redundant properties of inactive versions are removed
        assertThat(queryFactory
                .from(entityVersion)
                .innerJoin(entityVersion._entityVersionPropertyRevisionFk, entityVersionProperty)
                .where(entityVersion.status.eq(SQUASHED), entityVersionProperty.status.eq(REDUNDANT))
                .fetchCount())
                .isEqualTo(0);
        // Verify that inverse is true: there exists redundant properties on ACTIVE versions
        assertThat(queryFactory
                .from(entityVersion)
                .innerJoin(entityVersion._entityVersionPropertyRevisionFk, entityVersionProperty)
                .where(entityVersion.status.eq(ACTIVE), entityVersionProperty.status.eq(REDUNDANT))
                .fetchCount())
                .isGreaterThan(0);
    }

    @Override
    protected AbstractVersionStoreJdbc<String, String, ?, ?, ?> getStore() {
        return entityStore;
    }

    private void create_first_two_versions_of_doc1() {
        transactionTemplate.execute(status -> {
            EntityUpdateBatch<String, String, ?> update = entityStore.updateBatch(docId1);
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

        ObjectVersionGraph<String> graph = entityStore.load(docId1);
        assertThat(graph.getTip().getProperties()).isEqualTo(mapOf(
                "id", docId1,
                "name", "Fixed name"
        ));

        String persistedName = queryFactory
                .select(entity.name)
                .from(entity)
                .where(entity.id.eq(docId1))
                .fetchOne();
        assertThat(persistedName).isEqualTo("Fixed name");
    }

    private void cannot_bulk_load_before_publish() {
        GraphResults<String, String> graphs = entityStore.load(asList(docId1, docId2));
        assertThat(graphs.isEmpty()).isTrue();
        assertThat(graphs.size()).isEqualTo(0);
    }

    private void create_doc2_and_update_doc1() {
        transactionTemplate.execute(status -> {
            EntityUpdateBatch<String, String, JEntityVersion<String>> update = entityStore.updateBatch(asList(docId1, docId2));
            ObjectVersionGraph<String> graph = entityStore.load(docId1);
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
        List<ObjectVersion<String>> updates = entityStore.fetchUpdates(docId1, rev1);
        assertThat(updates).hasSize(2);
        assertThat(updates.get(0).revision).isEqualTo(rev2);
        assertThat(updates.get(1).revision).isEqualTo(rev4);
    }

    private void fetch_updates_of_doc2() {
        List<ObjectVersion<String>> updates = entityStore.fetchUpdates(docId2, rev3);
        assertThat(updates).isEmpty();
    }

    private void bulk_load_after_publish() {
        entityStore.publish();

        GraphResults<String, String> graphs = entityStore.load(asList(docId1, docId2));
        assertThat(graphs.containsKey(docId1)).isTrue();
        assertThat(graphs.containsKey(docId2)).isTrue();

        assertThat(graphs.getVersionGraph(docId1).getTip().getProperties()).isEqualTo(mapOf(
                "id", docId1,
                "name", "doc1"
        ));

        assertThat(graphs.getVersionGraph(docId2).getTip().getProperties()).isEqualTo(mapOf(
                "name", "doc2"
        ));

        String persistedName = queryFactory
                .select(entity.name)
                .from(entity)
                .where(entity.id.eq(docId1))
                .fetchOne();
        assertThat(persistedName).isEqualTo("doc1");

        persistedName = queryFactory
                .select(entity.name)
                .from(entity)
                .where(entity.id.eq(docId2))
                .fetchOne();
        assertThat(persistedName).isEqualTo("doc2");
    }

    private void prune_doc1() {
        entityStore.prune(docId1, graph -> v -> v.revision.equals(rev4));
        ObjectVersionGraph<String> graph = entityStore.load(docId1);
        assertThat(graph.versionNodes.size()).isEqualTo(1);
    }

    private String randomId() {
        return randomUUID().toString();
    }
}
