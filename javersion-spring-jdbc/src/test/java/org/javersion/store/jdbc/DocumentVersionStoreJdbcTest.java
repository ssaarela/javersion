package org.javersion.store.jdbc;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableMap;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.javersion.path.PropertyPath.ROOT;
import static org.javersion.path.PropertyPath.parse;
import static org.javersion.store.sql.QDocumentVersion.documentVersion;
import static org.javersion.store.sql.QDocumentVersionProperty.documentVersionProperty;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javax.annotation.Resource;

import org.javersion.core.Persistent;
import org.javersion.core.Revision;
import org.javersion.core.Version;
import org.javersion.core.VersionGraph;
import org.javersion.core.VersionNode;
import org.javersion.object.ObjectVersion;
import org.javersion.object.ObjectVersionGraph;
import org.javersion.object.ObjectVersionManager;
import org.javersion.object.Versionable;
import org.javersion.path.PropertyPath;
import org.javersion.store.PersistenceTestConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.querydsl.core.group.GroupBy;
import com.querydsl.sql.SQLQueryFactory;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = PersistenceTestConfiguration.class)
public class DocumentVersionStoreJdbcTest {

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

    private final ObjectVersionManager<Product, Void> versionManager = new ObjectVersionManager<Product, Void>(Product.class).init();

    @Resource
    DocumentVersionStoreJdbc<String, Void, JDocumentVersion<String>> documentStore;

    @Resource
    DocumentVersionStoreJdbc<String, Void, JDocumentVersion<String>> mappedDocumentStore;

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

        ObjectVersion<Void> versionOne = versionManager.versionBuilder(product).build();
        documentStore.append(docId, versionManager.getVersionNode(versionOne.revision));
        assertThat(documentStore.load(docId).isEmpty()).isTrue();

        documentStore.publish();
        VersionGraph versionGraph = documentStore.load(docId);
        assertThat(versionGraph.isEmpty()).isFalse();
        assertThat(versionGraph.getTip().getVersion()).isEqualTo(versionOne);

        product.price = new Price(new BigDecimal(10));
        product.tags = ImmutableList.of("tag", "and", "another");
        product.vat = 22.5;

        documentStore.append(docId, versionManager.versionBuilder(product).buildVersionNode());

        product.outOfStock = true;

        ObjectVersion<Void> lastVersion = versionManager.versionBuilder(product).build();
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
        ObjectVersion<Void> emptyVersion = new ObjectVersion.Builder<Void>().build();
        ObjectVersionGraph<Void> versionGraph = ObjectVersionGraph.init(emptyVersion);
        documentStore.append(docId, versionGraph.getTip());
        documentStore.publish();
        versionGraph = documentStore.load(docId);
        List<Version<PropertyPath, Object, Void>> versions = newArrayList(versionGraph.getVersions());
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
                ObjectVersion<Void> version1 = ObjectVersion.<Void>builder(r1)
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
        ObjectVersion<Void> version2 = ObjectVersion.<Void>builder(r2)
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

        ObjectVersion<Void> v1 = ObjectVersion.<Void>builder()
                .changeset(mapOf("property", "value1"))
                .build();

        ObjectVersion<Void> v2 = ObjectVersion.<Void>builder()
                .changeset(mapOf("property", "value2"))
                .build();

        ObjectVersionGraph<Void> versionGraph = ObjectVersionGraph.init(v1, v2);
        documentStore.append(docId, versionGraph.getVersionNode(v1.revision));
        assertThat(documentStore.publish()).isEqualTo(ImmutableMultimap.of(docId, v1.revision)); // v1
        documentStore.append(docId, versionGraph.getVersionNode(v2.revision));

        List<ObjectVersion<Void>> updates = documentStore.fetchUpdates(docId, v1.revision);
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
        String docId = randomUUID().toString();

        ObjectVersion<Void> v1 = ObjectVersion.<Void>builder()
                .changeset(mapOf(
                        // This should ve moved to v3
                        "property1", "value1",
                        "property2", "value1"))
                .build();

        ObjectVersion<Void> v2 = ObjectVersion.<Void>builder()
                // Toombstones should be removed
                .changeset(mapOf("property2", null))
                .parents(v1.revision)
                .build();

        ObjectVersion<Void> v3 = ObjectVersion.<Void>builder()
                .parents(v2.revision)
                .build();

        // This intermediate version should be removed
        ObjectVersion<Void> v4 = ObjectVersion.<Void>builder()
                .changeset(mapOf(
                        // These should be left as is
                        "property1", "value2",
                        "property2", "value1"))
                .parents(v3.revision)
                .build();

        ObjectVersion<Void> v5 = ObjectVersion.<Void>builder()
                // This should be in conflict with v4
                .changeset(mapOf("property2", "value2"))
                .parents(v3.revision)
                .build();

        ObjectVersion<Void> v6 = ObjectVersion.<Void>builder()
                // This should be replaced with v3
                .parents(v4.revision)
                .build();

        ObjectVersionGraph<Void> versionGraph = ObjectVersionGraph.init(v1, v2, v3, v4, v5, v6);
        documentStore.append(docId, ImmutableList.copyOf(versionGraph.getVersionNodes()).reverse());
        documentStore.publish();

        assertThat(queryFactory.from(documentVersion).where(documentVersion.docId.eq(docId)).fetchCount()).isEqualTo(6);

        documentStore.prune(docId,
                versionNode -> versionNode.revision.equals(v5.revision) || versionNode.revision.equals(v6.revision));

        assertThat(queryFactory.from(documentVersion).where(documentVersion.docId.eq(docId)).fetchCount()).isEqualTo(3);

        versionGraph = documentStore.load(docId);

        VersionNode<PropertyPath, Object, Void> versionNode = versionGraph.getVersionNode(v3.revision);
        assertThat(versionNode.getParentRevisions()).isEmpty();
        // Toombstone is removed
        assertThat(versionNode.getChangeset()).isEqualTo(mapOf("property1", "value1"));
        assertThat(versionNode.getProperties()).doesNotContainKey(parse("property2"));

        versionNode = versionGraph.getVersionNode(v5.revision);
        assertThat(versionNode.getParentRevisions()).isEqualTo(ImmutableSet.of(v3.revision));
        assertThat(versionNode.getChangeset()).isEqualTo(mapOf("property2", "value2"));

        versionNode = versionGraph.getVersionNode(v6.revision);
        assertThat(versionNode.getParentRevisions()).isEqualTo(ImmutableSet.of(v3.revision));
        assertThat(versionNode.getChangeset()).isEqualTo(mapOf(
                "property1", "value2",
                "property2", "value1"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void prune_should_not_delete_all_versions() {
        String docId = randomUUID().toString();

        ObjectVersion<Void> v1 = ObjectVersion.<Void>builder()
                .changeset(mapOf("property1", "value1"))
                .build();

        ObjectVersionGraph<Void> versionGraph = ObjectVersionGraph.init(v1);
        documentStore.append(docId, ImmutableList.copyOf(versionGraph.getVersionNodes()).reverse());
        documentStore.publish();

        documentStore.prune(docId, v -> false);
    }

    @Test(expected = RuntimeException.class)
    public void unpublished_version_may_fail_pruning() {
        String docId = randomUUID().toString();

        ObjectVersion<Void> v1 = ObjectVersion.<Void>builder()
                .changeset(mapOf("property1", "value1"))
                .build();

        ObjectVersion<Void> v2 = ObjectVersion.<Void>builder()
                .changeset(mapOf("property1", "value2"))
                .parents(v1.revision)
                .build();

        ObjectVersion<Void> v3 = ObjectVersion.<Void>builder()
                .changeset(mapOf("property1", "value3"))
                // v1 is to be squashed...
                .parents(v1.revision)
                .build();

        ObjectVersionGraph<Void> versionGraph = ObjectVersionGraph.init(v1, v2);
        documentStore.append(docId, ImmutableList.copyOf(versionGraph.getVersionNodes()).reverse());
        documentStore.publish();

        versionGraph = versionGraph.commit(v3);
        documentStore.append(docId, versionGraph.getVersionNode(v3.revision));

        // v3 is not published yet!
        documentStore.prune(docId, v -> v.revision.equals(v2.revision));
    }

    @Test
    public void supported_value_types() {
        String docId = randomUUID().toString();

        Map<PropertyPath, Object> changeset = mapOf(
                "Object", Persistent.object("Object"),
                "Array", Persistent.array(),
                "String", "String",
                "Boolean", true,
                "Long", 123l,
                "Double", 123.456,
                "BigDecimal", BigDecimal.TEN,
                "Void", null);

        ObjectVersion<Void> version = ObjectVersion.<Void>builder().changeset(changeset).build();
        documentStore.append(docId, ObjectVersionGraph.init(version).getTip());
        documentStore.publish();
        assertThat(documentStore.load(docId).getTip().getVersion()).isEqualTo(version);
    }

    @Test
    public void load_multiple_documents() {
        String docId1 = randomUUID().toString();
        String docId2 = randomUUID().toString();

        Map<PropertyPath, Object> props1 = mapOf("id", docId1);
        Map<PropertyPath, Object> props2 = mapOf("id", docId2);

        ObjectVersion<Void> v1 = ObjectVersion.<Void>builder().changeset(props1).build();
        ObjectVersion<Void> v2 = ObjectVersion.<Void>builder().changeset(props2).build();

        documentStore.append(docId1, ObjectVersionGraph.init(v1).getTip());
        documentStore.append(docId2, ObjectVersionGraph.init(v2).getTip());
        documentStore.publish();

        FetchResults<String, Void> results = documentStore.load(asList(docId1, docId2));
        assertThat(results.getDocIds()).isEqualTo(ImmutableSet.of(docId1, docId2));
        assertThat(results.latestRevision).isEqualTo(v2.revision);
        assertThat(results.getVersions(docId1).get(0)).isEqualTo(v1);
        assertThat(results.getVersions(docId2).get(0)).isEqualTo(v2);
    }

    @Test
    public void id_and_name_mapped_to_version_table() {
        String docId = randomUUID().toString();

        ObjectVersion<Void> v1 = ObjectVersion.<Void>builder()
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

        ObjectVersion<Void> v2 = ObjectVersion.<Void>builder()
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


    public static Map<PropertyPath, Object> mapOf(Object... entries) {
        Map<PropertyPath, Object> map = Maps.newHashMap();
        for (int i=0; i+1 < entries.length; i+=2) {
            map.put(parse(entries[i].toString()), entries[i+1]);
        }
        return unmodifiableMap(map);
    }

}
