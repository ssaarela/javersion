package org.javersion.store;

import static java.util.UUID.randomUUID;

import java.math.BigDecimal;
import java.util.UUID;

import javax.inject.Inject;
import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.*;

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

import com.mysema.query.sql.H2Templates;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ObjectVersionStoreJdbcTest.Conf.class)
public class ObjectVersionStoreJdbcTest {

    @Configuration
    @EnableAutoConfiguration
    public static class Conf {
    }

    @Versionable
    public static class Product {
        public String name;
        public BigDecimal price;
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
        Product product = new Product();
        product.name = "first name";
        product.price = new BigDecimal(10);

        ObjectVersion<Void> versionOne = versionManager.versionBuilder(product).build();
        store.append(docId, versionOne);
        assertThat(store.load(docId).isEmpty()).isTrue();

        store.commit();
        VersionGraph versionGraph = store.load(docId);
        assertThat(versionGraph.isEmpty()).isFalse();
        assertThat(versionGraph.getTip().version).isEqualTo(versionOne);
    }

}
