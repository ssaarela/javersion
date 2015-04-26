package org.javersion.store;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Resource;

import org.javersion.core.Version;
import org.javersion.core.VersionGraph;
import org.javersion.object.ObjectVersion;
import org.javersion.object.ObjectVersionBuilder;
import org.javersion.object.ObjectVersionGraph;
import org.javersion.object.ObjectVersionManager;
import org.javersion.object.Versionable;
import org.javersion.path.PropertyPath;
import org.javersion.store.jdbc.ObjectVersionStoreJdbc;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.collect.ImmutableList;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = PersistenceTestConfiguration.class)
public class ObjectVersionStoreJdbcTest {

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
    private ObjectVersionStoreJdbc<Void> versionStore;

    @Test
    public void insert_and_load() {
        String docId = randomUUID().toString();

        assertThat(versionStore.load(docId).isEmpty()).isTrue();

        Product product = new Product();
        product.id = 123l;
        product.name = "product";

        ObjectVersion<Void> versionOne = versionManager.versionBuilder(product).build();
        versionStore.append(docId, versionManager.getVersionNode(versionOne.revision));
        assertThat(versionStore.load(docId).isEmpty()).isTrue();

        versionStore.commit();
        VersionGraph versionGraph = versionStore.load(docId);
        assertThat(versionGraph.isEmpty()).isFalse();
        assertThat(versionGraph.getTip().getVersion()).isEqualTo(versionOne);

        product.price = new Price(new BigDecimal(10));
        product.tags = ImmutableList.of("tag", "and", "another");
        product.vat = 22.5;

        versionStore.append(docId, versionManager.versionBuilder(product).buildVersionNode());

        product.outOfStock = true;

        ObjectVersion<Void> lastVersion = versionManager.versionBuilder(product).build();
        versionStore.append(docId, versionManager.getVersionNode(lastVersion.revision));
        assertThat(versionStore.load(docId).getTip().getVersion()).isEqualTo(versionOne);

        versionStore.commit();
        versionGraph = versionStore.load(docId);
        assertThat(versionGraph.getTip().getVersion()).isEqualTo(lastVersion);

        versionManager.init(versionGraph);
        Product persisted = versionManager.mergeObject(Version.DEFAULT_BRANCH).object;
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
        ObjectVersion<Void> emptyVersion = new ObjectVersionBuilder<Void>().build();
        ObjectVersionGraph<Void> versionGraph = ObjectVersionGraph.init(emptyVersion);
        versionStore.append(docId, versionGraph.getTip());
        versionStore.commit();
        versionGraph = versionStore.load(docId);
        List<Version<PropertyPath, Object, Void>> versions = versionGraph.getVersions();
        assertThat(versions).hasSize(1);
        assertThat(versions.get(0)).isEqualTo(emptyVersion);
    }

}
