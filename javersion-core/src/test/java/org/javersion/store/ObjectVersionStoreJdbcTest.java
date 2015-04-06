package org.javersion.store;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.sql.DataSource;

import org.javersion.core.Version;
import org.javersion.core.VersionGraph;
import org.javersion.object.ObjectVersion;
import org.javersion.object.ObjectVersionGraph;
import org.javersion.object.ObjectVersionManager;
import org.javersion.object.Versionable;
import org.javersion.path.PropertyPath;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.google.common.collect.ImmutableList;
import com.mysema.query.sql.H2Templates;
import com.mysema.query.sql.SQLQueryFactory;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ObjectVersionStoreJdbcTest.Conf.class)
public class ObjectVersionStoreJdbcTest {

    @Configuration
    @EnableAutoConfiguration
    @EnableTransactionManagement
    public static class Conf {

        @Bean
        public SQLQueryFactory queryFactory(final DataSource dataSource) {
            com.mysema.query.sql.Configuration configuration = new com.mysema.query.sql.Configuration(new H2Templates());
            ObjectVersionStoreJdbc.registerTypes(configuration);
            return new SQLQueryFactory(configuration, () -> DataSourceUtils.getConnection(dataSource));
        }

        @Bean
        public ObjectVersionStoreJdbc.Initializer storeInitializer(SQLQueryFactory queryFactory) {
            return new ObjectVersionStoreJdbc.Initializer(queryFactory);
        }

        @Bean
        public VersionStore<String,
                PropertyPath, Object, Void,
                ObjectVersionGraph<Void>,
                ObjectVersionGraph.Builder<Void>> versionStore(ObjectVersionStoreJdbc.Initializer initializer) {
            return new ObjectVersionStoreJdbc<>(initializer);
        }

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

    private final ObjectVersionManager<Product, Void> versionManager = new ObjectVersionManager<Product, Void>(Product.class).init();

    @Resource
    private VersionStore<String,
            PropertyPath, Object, Void,
            ObjectVersionGraph<Void>,
            ObjectVersionGraph.Builder<Void>> versionStore;

    @Test
    public void insert_and_load() {
        String docId = randomUUID().toString();

        assertThat(versionStore.load(docId).isEmpty()).isTrue();

        Product product = new Product();
        product.id = 123l;
        product.name = "product";

        ObjectVersion<Void> versionOne = versionManager.versionBuilder(product).build();
        versionStore.append(docId, versionOne);
        assertThat(versionStore.load(docId).isEmpty()).isTrue();

        versionStore.commit();
        VersionGraph versionGraph = versionStore.load(docId);
        assertThat(versionGraph.isEmpty()).isFalse();
        assertThat(versionGraph.getTip().version).isEqualTo(versionOne);

        product.price = new Price(new BigDecimal(10));
        product.tags = ImmutableList.of("tag", "and", "another");
        product.vat = 22.5;

        versionStore.append(docId, versionManager.versionBuilder(product).build());

        product.outOfStock = true;

        ObjectVersion<Void> lastVersion = versionManager.versionBuilder(product).build();
        versionStore.append(docId, lastVersion);
        assertThat(versionStore.load(docId).getTip().version).isEqualTo(versionOne);

        versionStore.commit();
        versionGraph = versionStore.load(docId);
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
