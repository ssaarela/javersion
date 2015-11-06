package org.javersion.store.jdbc;

import static org.javersion.store.sql.QEntity.entity;
import static org.javersion.store.sql.QEntityVersion.entityVersion;
import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;
import static org.springframework.transaction.annotation.Propagation.MANDATORY;

import java.util.Collection;

import org.javersion.core.VersionNode;
import org.javersion.path.PropertyPath;
import org.springframework.transaction.annotation.Transactional;

import com.mysema.query.group.Group;

public class CustomEntityVersionStore extends EntityVersionStoreJdbc<String, String> {

    private static PropertyPath NAME = PropertyPath.ROOT.property("name");

    public CustomEntityVersionStore() {}

    public CustomEntityVersionStore(EntityStoreOptions<String> options) {
        super(options);
    }

    @Override
    @Transactional(readOnly = false, isolation = READ_COMMITTED, propagation = MANDATORY)
    public EntityUpdateBatch<String, String> updateBatch(String docId) {
        return new UpdateBatch(options, docId);
    }

    @Override
    @Transactional(readOnly = false, isolation = READ_COMMITTED, propagation = MANDATORY)
    public EntityUpdateBatch<String, String> updateBatch(Collection<String> docIds) {
        return new UpdateBatch(options, docIds);
    }

    @Override
    @Transactional(readOnly = false, isolation = READ_COMMITTED, propagation = MANDATORY)
    protected EntityUpdateBatch<String, String> optimizationUpdateBatch() {
        return new UpdateBatch(options);
    }

    public static class UpdateBatch extends EntityUpdateBatch<String, String> {

        public UpdateBatch(EntityStoreOptions<String> options) {
            super(options);
        }

        public UpdateBatch(EntityStoreOptions<String> options, String docId) {
            super(options, docId);
        }

        public UpdateBatch(EntityStoreOptions<String> options, Collection<String> docIds) {
            super(options, docIds);
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
