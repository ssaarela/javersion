package org.javersion.store.jdbc;

import static java.util.Arrays.asList;
import static org.javersion.store.sql.QEntity.entity;
import static org.javersion.store.sql.QEntityVersion.entityVersion;
import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;
import static org.springframework.transaction.annotation.Propagation.MANDATORY;

import java.util.Collection;

import org.javersion.core.VersionNode;
import org.javersion.path.PropertyPath;
import org.springframework.transaction.annotation.Transactional;

import com.mysema.query.group.Group;
import com.mysema.query.sql.dml.SQLUpdateClause;

public class CustomEntityVersionStore extends EntityVersionStoreJdbc<String, String, JEntityVersion<String>> {

    private static PropertyPath NAME = PropertyPath.ROOT.property("name");

    public CustomEntityVersionStore() {}

    public CustomEntityVersionStore(EntityStoreOptions<String, JEntityVersion<String>> options) {
        super(options);
    }

    @Override
    @Transactional(readOnly = false, isolation = READ_COMMITTED, propagation = MANDATORY)
    public EntityUpdateBatch<String, String, JEntityVersion<String>> updateBatch(String docId) {
        return new UpdateBatch(options, docId);
    }

    @Override
    @Transactional(readOnly = false, isolation = READ_COMMITTED, propagation = MANDATORY)
    public EntityUpdateBatch<String, String, JEntityVersion<String>> updateBatch(Collection<String> docIds) {
        return new UpdateBatch(options, docIds);
    }

    @Override
    @Transactional(readOnly = false, isolation = READ_COMMITTED, propagation = MANDATORY)
    protected EntityUpdateBatch<String, String, JEntityVersion<String>> optimizationUpdateBatch() {
        return new UpdateBatch(options);
    }

    public static class UpdateBatch extends EntityUpdateBatch<String, String, JEntityVersion<String>> {

        protected final SQLUpdateClause entityUpdateBatch;

        public UpdateBatch(EntityStoreOptions<String, JEntityVersion<String>> options) {
            super(options);
            entityUpdateBatch = null;
        }

        public UpdateBatch(EntityStoreOptions<String, JEntityVersion<String>> options, String docId) {
            this(options, asList(docId));
        }

        public UpdateBatch(EntityStoreOptions<String, JEntityVersion<String>> options, Collection<String> docIds) {
            super(options, docIds);
            entityUpdateBatch = options.queryFactory.update(options.entity);
        }

        @Override
        public void execute() {
            super.execute();
            if (entityUpdateBatch != null && !entityUpdateBatch.isEmpty()) {
                entityUpdateBatch.execute();
            }
        }

        @Override
        protected void insertEntity(String docId, VersionNode<PropertyPath, Object, String> version) {
            entityCreateBatch
                    .set(options.entity.id, docId)
                    .set(entity.name, (String) version.getProperties().get(NAME))
                    .addBatch();
        }

        @Override
        protected void updateEntity(String docId, VersionNode<PropertyPath, Object, String> version) {
            entityUpdateBatch
                    .set(entity.name, (String) version.getProperties().get(NAME))
                    .where(entity.id.eq(docId))
                    .addBatch();
        }

        @Override
        protected void insertVersion(String docId, VersionNode<PropertyPath, Object, String> version) {
            versionBatch.set(entityVersion.comment, version.meta);
            super.insertVersion(docId, version);
        }
    }

    @Override
    protected String getMeta(Group versionAndParents) {
        return versionAndParents.getOne(entityVersion.comment);
    }
}
