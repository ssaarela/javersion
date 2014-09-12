package org.javersion.object;

import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.javersion.core.Version.DEFAULT_BRANCH;
import static org.javersion.object.ObjectVersionManagerTest.ProductStatus.IN_STOCK;
import static org.javersion.object.ObjectVersionManagerTest.ProductStatus.PRE_ORDER;
import static org.javersion.util.ReflectionEquals.reflectionEquals;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.util.Set;

import org.javersion.core.Version;
import org.joda.time.DateTime;
import org.junit.Test;

public class ObjectVersionManagerTest {

    public static enum ProductStatus {
        PRE_ORDER,
        IN_STOCK,
        DISCONTINUED
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
        ObjectVersion<Void> version = versionManager.buildVersion(null).build();
        assertThat(version.changeset.entrySet(), empty());

        defaultMergeNoConflicts(null, 1);
    }

    @Test
    public void Save_Empty() {
        Product expected = new Product();
        ObjectVersion<Void> version = versionManager.buildVersion(expected).build();
        assertThat(version.changeset.size(), equalTo(1));

        defaultMergeNoConflicts(expected, 1);
    }

    private MergeObject<Product> defaultMergeNoConflicts(Product expected, long expectedRevision) {
        MergeObject<Product> mergeObject = versionManager.mergeObject(DEFAULT_BRANCH);
        Product actual = mergeObject.object;
        if (expected != null) {
            assertThat(actual, not(sameInstance(expected)));
        }
        assertThat(actual, reflectionEquals(expected));
        assertThat(mergeObject.merge.getConflicts().entries(), empty());
        assertThat(mergeObject.merge.getMergeHeads(), equalTo(set(expectedRevision)));
        return mergeObject;
    }

    @Test
    public void Linear_Versions() {
        Product product = new Product();
        product.name = "name";
        product.price = new BigDecimal("1.1");
        product.status = IN_STOCK;
        product.statusDate = new DateTime();

        ObjectVersion<Void> version = versionManager.buildVersion(product).build();
        assertThat(version.changeset.size(), equalTo(5));
        defaultMergeNoConflicts(product, 1);

        product.price = new BigDecimal("2.0");
        version = versionManager.buildVersion(product).build();
        assertThat(version.changeset.size(), equalTo(1));
        defaultMergeNoConflicts(product, 2);

        product.name = "nextgen";
        product.status = PRE_ORDER;
        product.statusDate = new DateTime();
        version = versionManager.buildVersion(product).build();
        assertThat(version.changeset.size(), equalTo(3));
        defaultMergeNoConflicts(product, 3);

        product.name = null;
        version = versionManager.buildVersion(product).build();
        assertThat(version.changeset.size(), equalTo(1));
        defaultMergeNoConflicts(product, 4);
    }

    @Test
    public void Merge_Price_and_Name() {
        // First version
        Product product = new Product();
        product.name = "name";
        product.price = new BigDecimal("1.0");
        versionManager.buildVersion(product).build();

        // Second version, new name
        product.name = "name2";
        versionManager.buildVersion(product).build();

        // Concurrent version, new price
        product.price = new BigDecimal("2.0");
        versionManager.buildVersion(product).parents(1l).build();

        // Merged
        MergeObject<Product> mergeObject = versionManager.mergeObject(Version.DEFAULT_BRANCH);
        assertThat(mergeObject.merge.getMergeHeads(), equalTo(set(2l, 3l)));
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
