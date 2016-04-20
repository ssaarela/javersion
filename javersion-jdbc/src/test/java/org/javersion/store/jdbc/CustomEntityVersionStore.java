package org.javersion.store.jdbc;

import static java.util.Arrays.asList;
import static org.javersion.store.sql.QEntity.entity;
import static org.javersion.store.sql.QEntityVersion.entityVersion;

import java.util.Collection;

import org.javersion.core.VersionNode;
import org.javersion.path.PropertyPath;

import com.querydsl.core.dml.StoreClause;
import com.querydsl.core.group.Group;
import com.querydsl.sql.dml.SQLUpdateClause;

public class CustomEntityVersionStore extends EntityVersionStoreJdbc<String, String, JEntityVersion<String>> {

    private static PropertyPath NAME = PropertyPath.ROOT.property("name");

    @SuppressWarnings("unused")
    protected CustomEntityVersionStore() {}

    public CustomEntityVersionStore(EntityStoreOptions<String, String, JEntityVersion<String>> options) {
        super(options);
    }

    @Override
    public EntityUpdateBatch<String, String, JEntityVersion<String>> updateBatch(Collection<String> docIds) {
        return new UpdateBatch(this, docIds);
    }

    public static class UpdateBatch extends EntityUpdateBatch<String, String, JEntityVersion<String>> {

        protected final SQLUpdateClause entityUpdateBatch;

        public UpdateBatch(CustomEntityVersionStore store, String docId) {
            this(store, asList(docId));
        }

        public UpdateBatch(CustomEntityVersionStore store, Collection<String> docIds) {
            super(store, docIds);
            entityUpdateBatch = options.queryFactory.update(options.entity);
        }

        @Override
        public void execute() {
            super.execute();
            if (isNotEmpty(entityUpdateBatch)) {
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
        protected void setMeta(String meta, StoreClause versionBatch) {
            versionBatch.set(entityVersion.comment, meta);
        }
    }

    @Override
    protected String getMeta(Group versionAndParents) {
        return versionAndParents.getOne(entityVersion.comment);
    }
}
