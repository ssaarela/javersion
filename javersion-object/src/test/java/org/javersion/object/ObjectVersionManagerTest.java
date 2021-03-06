package org.javersion.object;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.singleton;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.javersion.core.Version.DEFAULT_BRANCH;
import static org.javersion.object.ObjectVersionManagerTest.ProductStatus.IN_STOCK;
import static org.javersion.object.ObjectVersionManagerTest.ProductStatus.PRE_ORDER;
import static org.javersion.object.ReflectionEquals.reflectionEquals;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.util.Set;

import org.javersion.core.Revision;
import org.javersion.core.Version;
import org.javersion.core.VersionNode;
import org.javersion.path.PropertyPath;
import org.joda.time.DateTime;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;

public class ObjectVersionManagerTest {

    public enum ProductStatus {
        PRE_ORDER,
        IN_STOCK
    }

    @Versionable
    public static class Product {
        String name;
        BigDecimal price;
        DateTime statusDate;
        ProductStatus status;
    }

    private final ObjectVersionManager<Product, Void> versionManager = new ObjectVersionManager<Product, Void>(Product.class).init();

    @Test
    public void Save_Null() {
        ObjectVersion<Void> version = versionManager.versionBuilder(null).build();
        assertThat(version.changeset.entrySet(), empty());

        defaultMergeNoConflicts(null);
    }

    @Test
    public void is_empty() {
        assertThat(versionManager.isEmpty(), equalTo(true));
    }

    @Test
    public void get_branches() {
        versionManager.versionBuilder(null).build();
        assertThat(versionManager.getBranches(), equalTo(set(DEFAULT_BRANCH)));
    }

    @Test
    public void commit_returns_new_VersionNode() {
        ObjectVersion<Void> version = versionManager.versionBuilder(null).build(false);
        VersionNode<PropertyPath, Object, Void> versionNode = versionManager.commit(version);
        assertThat(versionNode, sameInstance(versionManager.getVersionNode(version.revision)));
    }

    @Test
    public void get_heads() {
        assertThat(versionManager.getHeads(), nullValue());
        ObjectVersion<Void> version = versionManager.versionBuilder(null).build();
        assertThat(versionManager.getHeads(), equalTo(set(version.revision)));
    }

    @Test
    public void is_not_empty() {
        versionManager.versionBuilder(null).build();
        assertThat(versionManager.isEmpty(), equalTo(false));
    }

    @Test
    public void merge_branches_vs_revisions() {
        Revision rev1 = new Revision(),
                rev2 = new Revision();

        Product product = new Product();
        product.name = "product 1";
        versionManager.versionBuilder(product).revision(rev2).branch("b1").build();

        // Concurrent version with earlier revision in another branch
        product.name = "product 2";
        versionManager.versionBuilder(product).revision(rev1).branch("b2").parents(ImmutableSet.of()).build();

        MergeObject<Product, Void> mergeObject = versionManager.mergeBranches("b2", "b1");
        product = mergeObject.object;
        assertThat(product.name, equalTo("product 2"));

        mergeObject = versionManager.mergeRevisions(mergeObject.getMergeHeads());
        product = mergeObject.object;
        assertThat(product.name, equalTo("product 1"));
    }

    @Test
    public void Save_Empty() {
        Product expected = new Product();
        ObjectVersion<Void> version = versionManager.versionBuilder(expected).build();
        assertThat(version.changeset.size(), equalTo(1));

        defaultMergeNoConflicts(expected);
    }

    private MergeObject<Product, Void> defaultMergeNoConflicts(Product expected) {
        MergeObject<Product, Void> mergeObject = versionManager.mergeBranches(DEFAULT_BRANCH);
        Product actual = mergeObject.object;
        if (expected != null) {
            assertThat(actual, not(sameInstance(expected)));
        }
        assertThat(actual, reflectionEquals(expected));
        assertThat(mergeObject.merge.getConflicts().entries(), empty());
        return mergeObject;
    }

    @Test
    public void Linear_Versions() {
        Product product = new Product();
        product.name = "name";
        product.price = new BigDecimal("1.1");
        product.status = IN_STOCK;
        product.statusDate = new DateTime();

        ObjectVersion<Void> version = versionManager.versionBuilder(product).build();
        assertThat(version.changeset.size(), equalTo(5));
        defaultMergeNoConflicts(product);

        product.price = new BigDecimal("2.0");
        version = versionManager.versionBuilder(product).build();
        assertThat(version.changeset.size(), equalTo(1));
        defaultMergeNoConflicts(product);

        product.name = "nextgen";
        product.status = PRE_ORDER;
        product.statusDate = new DateTime();
        version = versionManager.versionBuilder(product).build();
        assertThat(version.changeset.size(), equalTo(3));
        defaultMergeNoConflicts(product);

        product.name = null;
        version = versionManager.versionBuilder(product).build();
        assertThat(version.changeset.size(), equalTo(1));
        defaultMergeNoConflicts(product);
    }

    @Test
    public void rebase() {
        Revision v1 = new Revision(),
                 v2 = new Revision();

        Product product = new Product();
        product.name = "name";
        versionManager.versionBuilder(product).revision(v1).build();

        product.name = "change";
        versionManager.versionBuilder(product).revision(v2).build();

        product.name = "rebased conflict";
        versionManager.versionBuilder(product)
                .parents(v1)
                .rebaseOn(singleton(v2))
                .build();

        MergeObject<Product, Void> merge = versionManager.mergeBranches();
        assertThat(merge.getConflicts().isEmpty(), equalTo(true));
        assertThat(merge.object.name, equalTo("rebased conflict"));
    }

    @Test
    public void Merge_Price_and_Name() {
        // First version
        Product product = new Product();
        product.name = "name";
        product.price = new BigDecimal("1.0");
        Revision r1 = versionManager.versionBuilder(product).build().revision;

        // Second version, new name
        product.name = "name2";
        Revision r2 = versionManager.versionBuilder(product).build().revision;

        // Concurrent version, new price
        product.price = new BigDecimal("2.0");
        Revision r3 = versionManager.versionBuilder(product).parents(r1).build().revision;

        // Merged
        MergeObject<Product, Void> mergeObject = versionManager.mergeBranches(Version.DEFAULT_BRANCH);
        assertThat(mergeObject.merge.getMergeHeads(), equalTo(set(r2, r3)));
        assertThat(mergeObject.merge.getConflicts().isEmpty(), equalTo(true));

        product = mergeObject.object;
        assertThat(product.name, equalTo("name2"));
        assertThat(product.price, equalTo(new BigDecimal("2.0")));
    }

    @SafeVarargs
    private static <T> Set<T> set(final T... ts) {
        return newHashSet(ts);
    }

}
