package org.javersion.store.jdbc;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.javersion.path.PropertyPath.ROOT;
import static org.javersion.path.PropertyPath.parse;
import static org.javersion.store.jdbc.VersionStatus.ACTIVE;
import static org.javersion.store.jdbc.VersionStatus.REDUNDANT;
import static org.javersion.store.jdbc.VersionStatus.SQUASHED;
import static org.javersion.store.sql.QDocumentVersion.documentVersion;
import static org.javersion.store.sql.QDocumentVersionParent.documentVersionParent;
import static org.javersion.store.sql.QDocumentVersionProperty.documentVersionProperty;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

import javax.annotation.Resource;

import org.javersion.core.Persistent;
import org.javersion.core.Revision;
import org.javersion.core.Version;
import org.javersion.core.VersionNode;
import org.javersion.object.ObjectVersion;
import org.javersion.object.ObjectVersionGraph;
import org.javersion.object.ObjectVersionManager;
import org.javersion.object.Versionable;
import org.javersion.path.PropertyPath;
import org.junit.Test;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.querydsl.core.group.GroupBy;
import com.querydsl.sql.SQLQueryFactory;

public class DocumentVersionStoreJdbcTest extends AbstractVersionStoreTest {

    @Versionable
    public static class Price {
        public final BigDecimal amount;
        private Price() {
            this(null);
        }
        public Price(BigDecimal amount) {
            this.amount = amount;
        }
    }

    @Versionable
    public static class Product {
        public long id;
        public String name;
        public Price price;
        public List<String> tags;
        public double vat;
        public boolean outOfStock;
    }

    private final ObjectVersionManager<Product, String> versionManager = new ObjectVersionManager<Product, String>(Product.class).init();

    @Resource
    DocumentVersionStoreJdbc<String, String, JDocumentVersion<String>> documentStore;

    @Resource
    DocumentVersionStoreJdbc<String, String, JDocumentVersion<String>> mappedDocumentStore;

    @Resource
    TransactionTemplate transactionTemplate;

    @Resource
    SQLQueryFactory queryFactory;

    @Test
    public void insert_and_load() {
        String docId = randomUUID().toString();

        assertThat(documentStore.load(docId).isEmpty()).isTrue();

        Product product = new Product();
        product.id = 123l;
        product.name = "product";

        ObjectVersion<String> versionOne = versionManager.versionBuilder(product).build();
        documentStore.append(docId, versionManager.getVersionNode(versionOne.revision));
        assertThat(documentStore.load(docId).isEmpty()).isTrue();

        documentStore.publish();
        ObjectVersionGraph<String> versionGraph = documentStore.load(docId);
        assertThat(versionGraph.isEmpty()).isFalse();
        assertThat(versionGraph.getTip().getVersion()).isEqualTo(versionOne);

        product.price = new Price(new BigDecimal(10));
        product.tags = ImmutableList.of("tag", "and", "another");
        product.vat = 22.5;

        documentStore.append(docId, versionManager.versionBuilder(product).buildVersionNode());

        product.outOfStock = true;

        ObjectVersion<String> lastVersion = versionManager.versionBuilder(product).build();
        documentStore.append(docId, versionManager.getVersionNode(lastVersion.revision));
        assertThat(documentStore.load(docId).getTip().getVersion()).isEqualTo(versionOne);

        documentStore.publish();
        versionGraph = documentStore.load(docId);
        assertThat(versionGraph.getTip().getVersion()).isEqualTo(lastVersion);

        versionManager.init(versionGraph);
        Product persisted = versionManager.mergeBranches(Version.DEFAULT_BRANCH).object;
        assertThat(persisted.id).isEqualTo(product.id);
        assertThat(persisted.name).isEqualTo(product.name);
        assertThat(persisted.outOfStock).isEqualTo(product.outOfStock);
        assertThat(persisted.price.amount).isEqualTo(product.price.amount);
        assertThat(persisted.tags).isEqualTo(product.tags);
        assertThat(persisted.vat).isEqualTo(product.vat);
    }

    @Test
    public void load_version_with_empty_changeset() {
        String docId = randomUUID().toString();
        ObjectVersion<String> emptyVersion = new ObjectVersion.Builder<String>().build();
        ObjectVersionGraph<String> versionGraph = ObjectVersionGraph.init(emptyVersion);
        documentStore.append(docId, versionGraph.getTip());
        documentStore.publish();
        versionGraph = documentStore.load(docId);
        List<Version<PropertyPath, Object, String>> versions = newArrayList(versionGraph.getVersions());
        assertThat(versions).hasSize(1);
        assertThat(versions.get(0)).isEqualTo(emptyVersion);
    }

    @Test
    public void ordinal_is_assigned_by_publish() throws InterruptedException {
        final CountDownLatch firstInsertDone = new CountDownLatch(1);
        final CountDownLatch secondInsertDone = new CountDownLatch(1);
        final CountDownLatch firstInsertCommitted = new CountDownLatch(1);

        final String docId = randomUUID().toString();
        final Revision r1 = new Revision();
        final Revision r2 = new Revision();

        new Thread(() -> {
            transactionTemplate.execute(status -> {
                ObjectVersion<String> version1 = ObjectVersion.<String>builder(r1)
                        .changeset(mapOf(ROOT.property("concurrency"), " slow"))
                        .build();
                documentStore.append(docId, ObjectVersionGraph.init(version1).getTip());

                // First insert is done, but transaction is not committed yet
                firstInsertDone.countDown();
                try {
                    // Wait until second insert is committed before committing this
                    secondInsertDone.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return null;
            });
            firstInsertCommitted.countDown();
        }).start();

        // Wait until first insert is done, but not committed yet
        firstInsertDone.await();

        // Insert and commit another before first insert is committed
        ObjectVersion<String> version2 = ObjectVersion.<String>builder(r2)
                .changeset(mapOf("concurrency", "fast"))
                .build();
        documentStore.append(docId, ObjectVersionGraph.init(version2).getTip());
        documentStore.publish();

        // Verify that first insert is not yet visible
        long count = queryFactory.from(documentVersion)
                .where(documentVersion.docId.eq(docId))
                .fetchCount();
        assertThat(count).isEqualTo(1);

        // Let the first transaction commit
        secondInsertDone.countDown();

        firstInsertCommitted.await();

        // Verify that first insert is now visible (committed)
        count = queryFactory.from(documentVersion)
                .where(documentVersion.docId.eq(docId))
                .fetchCount();
        assertThat(count).isEqualTo(2);

        // Before documentStore.publish(), unpublished version should not have ordinal
        Map<Revision, Long> ordinals = findOrdinals(docId);
        assertThat(ordinals.get(r1)).isNull();
        assertThat(ordinals.get(r2)).isNotNull();

        // documentStore.publish() should assign ordinal
        documentStore.publish();
        ordinals = findOrdinals(docId);
        assertThat(ordinals.get(r1)).isGreaterThan(ordinals.get(r2));
    }

    private Map<Revision, Long> findOrdinals(String docId) {
        return queryFactory.from(documentVersion)
                .where(documentVersion.docId.eq(docId))
                .transform(GroupBy.groupBy(documentVersion.revision).as(documentVersion.ordinal));
    }

    @Test
    public void publish_nothing() {
        // Flush first if there's pending versions
        documentStore.publish();
        assertThat(documentStore.publish()).isEqualTo(ImmutableMultimap.of());

    }

    @Test
    public void load_updates() {
        String docId = randomUUID().toString();

        ObjectVersion<String> v1 = ObjectVersion.<String>builder()
                .changeset(mapOf("property", "value1"))
                .build();

        ObjectVersion<String> v2 = ObjectVersion.<String>builder()
                .changeset(mapOf("property", "value2"))
                .build();

        ObjectVersionGraph<String> versionGraph = ObjectVersionGraph.init(v1, v2);
        documentStore.append(docId, versionGraph.getVersionNode(v1.revision));
        assertThat(documentStore.publish()).isEqualTo(ImmutableMultimap.of(docId, v1.revision)); // v1
        documentStore.append(docId, versionGraph.getVersionNode(v2.revision));

        List<ObjectVersion<String>> updates = documentStore.fetchUpdates(docId, v1.revision);
        assertThat(updates).isEmpty();

        assertThat(documentStore.publish()).isEqualTo(ImmutableMultimap.of(docId, v2.revision)); // v2
        updates = documentStore.fetchUpdates(docId, v1.revision);
        assertThat(updates).hasSize(1);
        assertThat(updates.get(0)).isEqualTo(v2);
    }

    /**
     *   v1
     *   |
     *   v2
     *   |
     *   v3*
     *  /  \
     * v4  v5*
     * |
     * v6*
     */
    @Test
    public void prune() {
        final String docId = randomUUID().toString();
        ObjectVersionGraph<String> versionGraph = graphForOptimization();
        documentStore.append(docId, ImmutableList.copyOf(versionGraph.getVersionNodes()).reverse());
        documentStore.publish();

        assertThat(queryFactory.from(documentVersion).where(documentVersion.docId.eq(docId)).fetchCount()).isEqualTo(6);

        documentStore.prune(docId,
                graph -> versionNode -> versionNode.revision.equals(rev5) || versionNode.revision.equals(rev6));

        assertThat(queryFactory.from(documentVersion).where(documentVersion.docId.eq(docId)).fetchCount()).isEqualTo(3);

        versionGraph = documentStore.load(docId);

        VersionNode<PropertyPath, Object, String> versionNode = versionGraph.getVersionNode(rev3);
        assertThat(versionNode.getParentRevisions()).isEmpty();
        // Toombstone is removed
        assertThat(versionNode.getChangeset()).isEqualTo(mapOf("property1", "value1"));
        assertThat(versionNode.getProperties()).doesNotContainKey(parse("property2"));

        versionNode = versionGraph.getVersionNode(rev5);
        assertThat(versionNode.getParentRevisions()).isEqualTo(ImmutableSet.of(rev3));
        assertThat(versionNode.getChangeset()).isEqualTo(mapOf("property2", "value2"));

        versionNode = versionGraph.getVersionNode(rev6);
        assertThat(versionNode.getParentRevisions()).isEqualTo(ImmutableSet.of(rev3));
        assertThat(versionNode.getChangeset()).isEqualTo(mapOf(
                "property1", "value2",
                "property2", "value1"));
    }

    protected void verifyRedundantRelations() {
        // Redundant parents of inactive versions are removed
        assertThat(queryFactory
                .from(documentVersion)
                .innerJoin(documentVersion._documentVersionParentParentRevisionFk, documentVersionParent)
                .where(documentVersion.status.eq(SQUASHED), documentVersionParent.status.eq(REDUNDANT))
                .fetchCount())
                .isEqualTo(0);
        // Verify that inverse is true: there exists redundant parents on ACTIVE versions
        assertThat(queryFactory
                .from(documentVersion)
                .innerJoin(documentVersion._documentVersionParentParentRevisionFk, documentVersionParent)
                .where(documentVersion.status.eq(ACTIVE), documentVersionParent.status.eq(REDUNDANT))
                .fetchCount())
                .isGreaterThan(0);

        // Redundant properties of inactive versions are removed
        assertThat(queryFactory
                .from(documentVersion)
                .innerJoin(documentVersion._documentVersionPropertyRevisionFk, documentVersionProperty)
                .where(documentVersion.status.eq(SQUASHED), documentVersionProperty.status.eq(REDUNDANT))
                .fetchCount())
                .isEqualTo(0);
        // Verify that inverse is true: there exists redundant properties on ACTIVE versions
        assertThat(queryFactory
                .from(documentVersion)
                .innerJoin(documentVersion._documentVersionPropertyRevisionFk, documentVersionProperty)
                .where(documentVersion.status.eq(ACTIVE), documentVersionProperty.status.eq(REDUNDANT))
                .fetchCount())
                .isGreaterThan(0);
    }

    @Test(expected = RuntimeException.class)
    public void unpublished_version_may_fail_pruning() {
        String docId = randomUUID().toString();

        ObjectVersion<String> v1 = ObjectVersion.<String>builder()
                .changeset(mapOf("property1", "value1"))
                .build();

        ObjectVersion<String> v2 = ObjectVersion.<String>builder()
                .changeset(mapOf("property1", "value2"))
                .parents(v1.revision)
                .build();

        ObjectVersion<String> v3 = ObjectVersion.<String>builder()
                .changeset(mapOf("property1", "value3"))
                // v1 is to be squashed...
                .parents(v1.revision)
                .build();

        ObjectVersionGraph<String> versionGraph = ObjectVersionGraph.init(v1, v2);
        documentStore.append(docId, ImmutableList.copyOf(versionGraph.getVersionNodes()).reverse());
        documentStore.publish();

        versionGraph = versionGraph.commit(v3);
        documentStore.append(docId, versionGraph.getVersionNode(v3.revision));

        // v3 is not published yet!
        documentStore.prune(docId, graph -> v -> v.revision.equals(v2.revision));
    }

    @Test
    public void supported_value_types() {
        String docId = randomUUID().toString();

        Map<PropertyPath, Object> changeset = mapOf(
                "Object", Persistent.object("Object"),
                "Array", Persistent.array(),
                "String", "String",
                "Boolean", true,
                "Long", 123L,
                "Double", 123.456,
                "BigDecimal", BigDecimal.TEN,
                "Null", Persistent.NULL,
                "Void", null);

        ObjectVersion<String>
                v1 = ObjectVersion.<String>builder().changeset(mapOf("Void", "null")).build(),
                v2 = ObjectVersion.<String>builder().parents(v1.revision).changeset(changeset).build();

        ObjectVersionGraph<String> graph = ObjectVersionGraph.init(v1, v2);
        documentStore.append(docId, graph.getVersionNode(v1.revision));
        documentStore.append(docId, graph.getVersionNode(v2.revision));
        documentStore.publish();

        assertThat(documentStore.load(docId).getVersionNode(v2.revision).getVersion()).isEqualTo(v2);
    }

    @Test
    public void load_multiple_documents() {
        String docId1 = randomUUID().toString();
        String docId2 = randomUUID().toString();

        Map<PropertyPath, Object> props1 = mapOf("id", docId1);
        Map<PropertyPath, Object> props2 = mapOf("id", docId2);

        ObjectVersion<String> v1 = ObjectVersion.<String>builder().changeset(props1).build();
        ObjectVersion<String> v2 = ObjectVersion.<String>builder().changeset(props2).build();

        documentStore.append(docId1, ObjectVersionGraph.init(v1).getTip());
        documentStore.append(docId2, ObjectVersionGraph.init(v2).getTip());
        documentStore.publish();

        GraphResults<String, String> results = documentStore.load(asList(docId1, docId2));
        assertThat(results.getDocIds()).isEqualTo(ImmutableSet.of(docId1, docId2));
        assertThat(results.latestRevision).isEqualTo(v2.revision);
        assertThat(results.getVersionGraph(docId1).getTip().getVersion()).isEqualTo(v1);
        assertThat(results.getVersionGraph(docId2).getTip().getVersion()).isEqualTo(v2);
    }

    @Test
    public void id_and_name_mapped_to_version_table() {
        String docId = randomUUID().toString();

        ObjectVersion<String> v1 = ObjectVersion.<String>builder()
                .changeset(mapOf(
                        "name", "name",
                        "id", 5l))
                .build();

        mappedDocumentStore.append(docId, ObjectVersionGraph.init(v1).getTip());
        mappedDocumentStore.publish();
        assertThat(mappedDocumentStore.load(docId).getTip().getVersion()).isEqualTo(v1);

        long count = queryFactory.from(documentVersionProperty)
                .innerJoin(documentVersionProperty.documentVersionPropertyRevisionFk, documentVersion)
                .where(documentVersion.docId.eq(docId))
                .fetchCount();
        assertThat(count).isEqualTo(0);

        ObjectVersion<String> v2 = ObjectVersion.<String>builder()
                .parents(v1.revision)
                .build();

        mappedDocumentStore.append(docId, ObjectVersionGraph.init(v1, v2).getTip());
        mappedDocumentStore.publish();

        // Inherited values
        count = queryFactory.from(documentVersion)
                .where(documentVersion.docId.eq(docId),
                        documentVersion.revision.eq(v2.revision),
                        documentVersion.name.eq("name"),
                        documentVersion.id.eq(5l))
                .fetchCount();
        assertThat(count).isEqualTo(1);
        assertThat(mappedDocumentStore.load(docId).getTip().getVersion()).isEqualTo(v2);
    }

    @Override
    protected AbstractVersionStoreJdbc<String, String, ?, ?, ?> newStore(Executor executor, GraphOptions<String, String> graphOptions) {
        return new DocumentVersionStoreJdbc<>(documentStore.options.toBuilder()
                .optimizationExecutor(executor)
                .graphOptions(graphOptions).build());
    }

    @Override
    protected AbstractVersionStoreJdbc<String, String, ?, ?, ?> getStore() {
        return documentStore;
    }
}
