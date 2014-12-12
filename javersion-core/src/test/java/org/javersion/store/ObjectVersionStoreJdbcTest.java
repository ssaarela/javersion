package org.javersion.store;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.javersion.core.Version;
import org.javersion.core.VersionGraph;
import org.javersion.object.ObjectVersion;
import org.javersion.object.ObjectVersionManager;
import org.javersion.object.Versionable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.collect.ImmutableList;
import com.mysema.query.sql.H2Templates;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ObjectVersionStoreJdbcTest.Conf.class)
public class ObjectVersionStoreJdbcTest {

    @Configuration
    @EnableAutoConfiguration
    public static class Conf {
    }

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

    @Inject
    DataSource dataSource;

    private final ObjectVersionManager<Product, Void> versionManager = new ObjectVersionManager<Product, Void>(Product.class).init();

    private ObjectVersionStoreJdbc<Void> store;

    @Before
    public void init() {
        this.store = new ObjectVersionStoreJdbc<>(dataSource, new H2Templates());
    }

    @Test
    public void insert_and_load() {
        String docId = randomUUID().toString();

        assertThat(store.load(docId).isEmpty()).isTrue();

        Product product = new Product();
        product.id = 123l;
        product.name = "product";

        ObjectVersion<Void> versionOne = versionManager.versionBuilder(product).build();
        store.append(docId, versionOne);
        assertThat(store.load(docId).isEmpty()).isTrue();

        store.commit();
        VersionGraph versionGraph = store.load(docId);
        assertThat(versionGraph.isEmpty()).isFalse();
        assertThat(versionGraph.getTip().version).isEqualTo(versionOne);

        product.price = new Price(new BigDecimal(10));
        product.tags = ImmutableList.of("tag", "and", "another");
        product.vat = 22.5;

        store.append(docId, versionManager.versionBuilder(product).build());

        product.outOfStock = true;

        ObjectVersion<Void> lastVersion = versionManager.versionBuilder(product).build();
        store.append(docId, lastVersion);
        assertThat(store.load(docId).getTip().version).isEqualTo(versionOne);

        store.commit();
        versionGraph = store.load(docId);
        assertThat(versionGraph.getTip().version).isEqualTo(lastVersion);

        versionManager.init(versionGraph);
        Product persisted = versionManager.mergeObject(Version.DEFAULT_BRANCH).object;
        assertThat(persisted.id).isEqualTo(product.id);
        assertThat(persisted.name).isEqualTo(product.name);
        assertThat(persisted.outOfStock).isEqualTo(product.outOfStock);
        assertThat(persisted.price.amount).isEqualTo(product.price.amount);
        assertThat(persisted.tags).isEqualTo(product.tags);
        assertThat(persisted.vat).isEqualTo(product.vat);
    }

}
