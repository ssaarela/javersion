package org.javersion.store.jdbc;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.javersion.store.jdbc.DocumentVersionStoreJdbcTest.mapOf;

import java.util.Map;

import javax.annotation.Resource;

import org.javersion.object.ObjectVersion;
import org.javersion.object.ObjectVersionGraph;
import org.javersion.path.PropertyPath;
import org.javersion.store.PersistenceTestConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.support.TransactionTemplate;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = PersistenceTestConfiguration.class)
public class EntityVersionStoreJdbcTest {

    @Resource
    EntityVersionStoreJdbc<String, Void> entityVersionStore;

    @Resource
    TransactionTemplate transactionTemplate;

    @Test
    public void should_return_empty_graph_if_not_found() {
        assertThat(entityVersionStore.load(randomUUID().toString()).isEmpty()).isTrue();
    }

    @Test
    public void save_read_and_update_one() {
        final String docId = randomUUID().toString();
        final Map<PropertyPath, Object> changeset = mapOf("id", docId, "name", "name of " + docId);

        transactionTemplate.execute(status -> {
            EntityUpdateBatch<String, Void> update = entityVersionStore.updateBatch(docId);
            assertThat(update.contains(docId)).isTrue();
            assertThat(update.contains(randomUUID().toString())).isFalse();
            assertThat(update.isCreate(docId)).isTrue();
            assertThat(update.isUpdate(docId)).isFalse();

            ObjectVersion<Void> v1 = ObjectVersion.<Void>builder().changeset(changeset).build();
            update.addVersion(docId, ObjectVersionGraph.init(v1).getTip());
            update.execute();
            return null;
        });

        ObjectVersionGraph<Void> graph = entityVersionStore.load(docId);
        assertThat(graph.getTip().getVersion().changeset).isEqualTo(changeset);
    }
}
