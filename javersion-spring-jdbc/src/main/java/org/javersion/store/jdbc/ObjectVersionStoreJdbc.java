/*
 * Copyright 2015 Samppa Saarela
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.javersion.store.jdbc;

import static com.mysema.query.group.GroupBy.groupBy;
import static java.lang.System.arraycopy;
import static java.util.Collections.singleton;
import static java.util.Map.Entry;
import static org.javersion.store.jdbc.RevisionType.REVISION_TYPE;
import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;
import static org.springframework.transaction.annotation.Propagation.REQUIRED;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.javersion.core.Persistent;
import org.javersion.core.Revision;
import org.javersion.core.VersionNode;
import org.javersion.core.VersionType;
import org.javersion.object.ObjectVersion;
import org.javersion.object.ObjectVersionBuilder;
import org.javersion.object.ObjectVersionGraph;
import org.javersion.path.PropertyPath;
import org.javersion.util.Check;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mysema.commons.lang.Pair;
import com.mysema.query.ResultTransformer;
import com.mysema.query.Tuple;
import com.mysema.query.group.Group;
import com.mysema.query.group.GroupBy;
import com.mysema.query.group.QPair;
import com.mysema.query.sql.Configuration;
import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.SQLQueryFactory;
import com.mysema.query.sql.dml.SQLInsertClause;
import com.mysema.query.sql.types.EnumByNameType;
import com.mysema.query.types.Expression;
import com.mysema.query.types.Path;
import com.mysema.query.types.QTuple;
import com.mysema.query.types.expr.SimpleExpression;

public class ObjectVersionStoreJdbc<Id, M> {

    public static void registerTypes(String tablePrefix, Configuration configuration) {
        configuration.register(tablePrefix + "VERSION", "TYPE", new EnumByNameType<>(VersionType.class));
        configuration.register(tablePrefix + "VERSION", "REVISION", REVISION_TYPE);

        configuration.register(tablePrefix + "VERSION_PARENT", "REVISION", REVISION_TYPE);
        configuration.register(tablePrefix + "VERSION_PARENT", "PARENT_REVISION", REVISION_TYPE);

        configuration.register(tablePrefix + "VERSION_PROPERTY", "REVISION", REVISION_TYPE);
    }

    protected final Expression<Long> nextOrdinal;

    protected final JVersion<Id> jVersion;

    protected final JVersionParent jParent;

    protected final JVersionProperty jProperty;

    protected final JRepository jRepository;

    protected final SQLQueryFactory queryFactory;

    protected final Expression<?>[] versionAndParents;

    protected final QPair<Revision, Id> revisionAndDocId;

    protected final ResultTransformer<Map<Revision, List<Tuple>>> properties;

    protected static final String REPOSITORY_ID = "repository";

    protected final Map<PropertyPath, Column> versionTableProperties;

    @SuppressWarnings("unused")
    protected ObjectVersionStoreJdbc() {
        nextOrdinal = null;
        jVersion = null;
        jParent = null;
        jProperty = null;
        jRepository = null;

        versionTableProperties = null;
        versionAndParents = null;
        revisionAndDocId = null;
        properties = null;
        queryFactory = null;
    }

    public <P extends SimpleExpression<Id> & Path<Id>> ObjectVersionStoreJdbc(
            JRepository jRepository,
            Expression<Long> nextOrdinal,
            JVersion<Id> jVersion,
            JVersionParent jParent,
            JVersionProperty jProperty,
            SQLQueryFactory queryFactory) {
        this(jRepository, nextOrdinal, jVersion, jParent, jProperty, queryFactory, ImmutableMap.of());
    }

    public <P extends SimpleExpression<Id> & Path<Id>> ObjectVersionStoreJdbc(
            JRepository jRepository,
            Expression<Long> nextOrdinal,
            JVersion<Id> jVersion,
            JVersionParent jParent,
            JVersionProperty jProperty,
            SQLQueryFactory queryFactory,
            Map<PropertyPath, Column> versionTableProperties) {
        this.nextOrdinal = nextOrdinal;
        this.jRepository = jRepository;
        this.jVersion = jVersion;
        this.jParent = jParent;
        this.jProperty = jProperty;
        this.queryFactory = queryFactory;

        this.versionTableProperties = ImmutableMap.copyOf(versionTableProperties);
        versionAndParents = concat(jVersion.all(), GroupBy.set(jParent.parentRevision));
        revisionAndDocId = new QPair<>(jVersion.revision, jVersion.docId.expr);
        properties = groupBy(jProperty.revision).as(GroupBy.list(new QTuple(jProperty.all())));
    }

    @Transactional(readOnly = false, isolation = READ_COMMITTED, propagation = REQUIRED)
    public void append(Id docId, VersionNode<PropertyPath, Object, M> version) {
        append(docId, singleton(version));
    }

    @Transactional(readOnly = false, isolation = READ_COMMITTED, propagation = REQUIRED)
    public void append(Id docId, Iterable<VersionNode<PropertyPath, Object, M>> versions) {
        SQLInsertClause versionBatch = queryFactory.insert(jVersion);
        SQLInsertClause parentBatch = queryFactory.insert(jParent);
        SQLInsertClause propertyBatch = queryFactory.insert(jProperty);

        for (VersionNode<PropertyPath, Object, M> version : versions) {
            addVersion(docId, version, versionBatch);
            addParents(version, parentBatch);
            addProperties(docId, version, propertyBatch);
        }

        if (!versionBatch.isEmpty()) {
            versionBatch.execute();
        }
        if (!parentBatch.isEmpty()) {
            parentBatch.execute();
        }
        if (!propertyBatch.isEmpty()) {
            propertyBatch.execute();
        }
    }

    @Transactional(readOnly = false, isolation = READ_COMMITTED, propagation = REQUIRES_NEW)
    public Set<Id> publish() {
        long lastOrdinal = getLastOrdinalForUpdate();
        List<Pair<Revision, Id>> uncommittedRevisions = findUncommittedRevisions();
        Set<Id> publishedDocs = Sets.newLinkedHashSetWithExpectedSize(uncommittedRevisions.size());

        for (Pair<Revision, Id> revisionAndDocId : uncommittedRevisions) {
            publishedDocs.add(revisionAndDocId.getSecond());
            queryFactory
                    .update(jVersion)
                    .where(jVersion.revision.eq(revisionAndDocId.getFirst()))
                    .set(jVersion.ordinal, ++lastOrdinal)
                    .setNull(jVersion.txOrdinal)
                    .execute();
        }

        queryFactory
                .update(jRepository)
                .where(jRepository.id.eq(REPOSITORY_ID))
                .set(jRepository.ordinal, lastOrdinal)
                .execute();

        return publishedDocs;
    }

    @Transactional(readOnly = true, isolation = READ_COMMITTED, propagation = REQUIRED)
    public ObjectVersionGraph<M> load(Id docId) {
        return ObjectVersionGraph.init(fetch(docId, null));
    }

    @Transactional(readOnly = true, isolation = READ_COMMITTED, propagation = REQUIRED)
    public List<ObjectVersion<M>> fetchUpdates(Id docId, @Nullable Revision since) {
        return fetch(docId, since);
    }

    @Transactional(readOnly = true, isolation = READ_COMMITTED, propagation = REQUIRED)
    // TODO: 1) Return also revision of max(ordinal)
    // TODO: 2) Add "Revision until" parameter to searches
    public List<Id> findDocuments(@Nullable Revision updatedSince) {
        Long sinceOrdinal = getOrdinal(updatedSince);
        SQLQuery qry = queryFactory
                .from(jVersion)
                .groupBy(jVersion.revision)
                .orderBy(jVersion.ordinal.max().asc());

        if (sinceOrdinal == null) {
            qry.where(jVersion.ordinal.isNotNull());
        } else {
            qry.where(jVersion.ordinal.gt(sinceOrdinal));
        }

        return qry.list(jVersion.docId.expr);
    }

    private List<ObjectVersion<M>> fetch(Id docId, @Nullable Revision sinceRevision) {
        Check.notNull(docId, "docId");

        Long sinceOrdinal = getOrdinal(sinceRevision);

        List<Group> versionsAndParents = getVersionsAndParents(docId, sinceOrdinal);
        if (versionsAndParents.isEmpty()) {
            return ImmutableList.of();
        }

        List<ObjectVersion<M>> versions = new ArrayList<>(versionsAndParents.size());
        Map<Revision, List<Tuple>> properties = getPropertiesByDocId(docId, sinceOrdinal);

        for (Group versionAndParents : versionsAndParents) {
            Revision rev = versionAndParents.getOne(jVersion.revision);
            Map<PropertyPath, Object> changeset = toChangeSet(properties.get(rev));

            versions.add(buildVersion(rev, versionAndParents, changeset));
        }
        return versions;
    }

    private Long getOrdinal(Revision sinceRevision) {
        if (sinceRevision == null) {
            return null;
        }
        return queryFactory
                .from(jVersion)
                .where(jVersion.revision.eq(sinceRevision))
                .singleResult(jVersion.ordinal);
    }

    private Long getLastOrdinalForUpdate() {
        return queryFactory
                .from(jRepository)
                .where(jRepository.id.eq(REPOSITORY_ID))
                .forUpdate()
                .singleResult(jRepository.ordinal);
    }

    private List<Pair<Revision, Id>> findUncommittedRevisions() {
        return queryFactory
                .from(jVersion)
                .where(jVersion.txOrdinal.isNotNull())
                .orderBy(jVersion.txOrdinal.asc())
                .list(revisionAndDocId);
    }

    protected void addProperties(Id docId, VersionNode<PropertyPath, Object, M> version, SQLInsertClause propertyBatch) {
        addProperties(docId, version.revision, version.getChangeset(), propertyBatch);
    }

    protected void addProperties(Id docId, Revision revision, Map<PropertyPath, Object> changeset, SQLInsertClause propertyBatch) {
        for (Entry<PropertyPath, Object> entry : changeset.entrySet()) {
            if (!versionTableProperties.containsKey(entry.getKey())) {
                propertyBatch
                        .set(jProperty.revision, revision)
                        .set(jProperty.path, entry.getKey().toString());
                setValue(entry.getKey(), entry.getValue(), propertyBatch);
                propertyBatch.addBatch();
            }
        }
    }

    protected void setValue(PropertyPath path, Object value, SQLInsertClause propertyBatch) {
        // type:
        // n=null, O=object, A=array, s=string,
        // b=boolean, l=long, d=double, D=bigdecimal
        char type;
        String str = null;
        Long nbr = null;
        switch (Persistent.Type.of(value)) {
            case NULL:
                type = 'n';
                break;
            case OBJECT:
                type = 'O';
                str = ((Persistent.Object) value).type;
                break;
            case ARRAY:
                type = 'A';
                break;
            case STRING:
                type = 's';
                str = (String) value;
                break;
            case BOOLEAN:
                type = 'b';
                nbr = ((Boolean) value) ? 1l : 0l;
                break;
            case LONG:
                type = 'l';
                nbr = (Long) value;
                break;
            case DOUBLE:
                type = 'd';
                nbr = Double.doubleToRawLongBits((Double) value);
                break;
            case BIG_DECIMAL:
                type = 'D';
                str = value.toString();
                break;
            default:
                throw new IllegalArgumentException("Unsupported type: " + value.getClass());
        }
        propertyBatch
                .set(jProperty.type, Character.toString(type))
                .set(jProperty.str, str)
                .set(jProperty.nbr, nbr);
    }

    protected void addParents(VersionNode<PropertyPath, Object, M> version, SQLInsertClause parentBatch) {
        for (Revision parentRevision : version.parentRevisions) {
            parentBatch
                    .set(jParent.revision, version.revision)
                    .set(jParent.parentRevision, parentRevision)
                    .addBatch();
        }
    }

    protected void addVersion(Id docId, VersionNode<PropertyPath, Object, M> version, SQLInsertClause versionBatch) {
        versionBatch
                .set(jVersion.docId.path, docId)
                .set(jVersion.revision, version.revision)
                .set(jVersion.txOrdinal, nextOrdinal)
                .set(jVersion.type, version.type)
                .set(jVersion.branch, version.branch);

        if (!versionTableProperties.isEmpty()) {
            Map<PropertyPath, Object> properties = version.getProperties();
            for (Entry<PropertyPath, Column> entry : versionTableProperties.entrySet()) {
                PropertyPath path = entry.getKey();
                @SuppressWarnings("unchecked")
                Column<Object> column = (Column<Object>) entry.getValue();
                versionBatch.set(column.path, properties.get(path));
            }
        }
        versionBatch.addBatch();
    }

    protected ObjectVersion<M> buildVersion(Revision rev, Group versionAndParents, Map<PropertyPath, Object> changeset) {
        if (!versionTableProperties.isEmpty()) {
            if (changeset == null) {
                changeset = new HashMap<>();
            }
            for (Entry<PropertyPath, Column> entry : versionTableProperties.entrySet()) {
                PropertyPath path = entry.getKey();
                @SuppressWarnings("unchecked")
                Column<Object> column = (Column<Object>) entry.getValue();
                // FIXME: This only works if path is in the latest version changeset
                if (!changeset.containsKey(path)) {
                    changeset.put(path, versionAndParents.getOne(column.expr));
                }
            }
        }
        return new ObjectVersionBuilder<M>(rev)
                .branch(versionAndParents.getOne(jVersion.branch))
                .type(versionAndParents.getOne(jVersion.type))
                .parents(versionAndParents.getSet(jParent.parentRevision))
                .changeset(changeset)
                .build();
    }

    protected Map<Revision, List<Tuple>> getPropertiesByDocId(Id docId, @Nullable Long sinceOrdinal) {
        SQLQuery qry = queryFactory
                .from(jProperty)
                .innerJoin(jVersion).on(jVersion.revision.eq(jProperty.revision))
                .where(jVersion.docId.expr.eq(docId));

        if (sinceOrdinal == null) {
            qry.where(jVersion.ordinal.isNotNull());
        } else {
            qry.where(jVersion.ordinal.gt(sinceOrdinal));
        }
        return qry.transform(properties);
    }

    protected List<Group> getVersionsAndParents(Id docId, @Nullable Long sinceOrdinal) {
        SQLQuery qry = queryFactory
                .from(jVersion)
                .leftJoin(jParent).on(jParent.revision.eq(jVersion.revision))
                .where(jVersion.docId.expr.eq(docId), jVersion.ordinal.isNotNull())
                .orderBy(jVersion.ordinal.asc());

        if (sinceOrdinal != null) {
            qry.where(jVersion.ordinal.gt(sinceOrdinal));
        }

        return qry.transform(groupBy(jVersion.revision).list(versionAndParents));
    }

    protected Map<PropertyPath, Object> toChangeSet(List<Tuple> properties) {
        if (properties == null) {
            return null;
        }
        Map<PropertyPath, Object> changeset = Maps.newHashMapWithExpectedSize(properties.size());
        for (Tuple tuple : properties) {
            PropertyPath path = PropertyPath.parse(tuple.get(jProperty.path));
            Object value = getPropertyValue(path, tuple);
            changeset.put(path, value);
        }
        return changeset;
    }

    protected Object getPropertyValue(PropertyPath path, Tuple tuple) {
        String type = tuple.get(jProperty.type);
        String str = tuple.get(jProperty.str);
        Long nbr = tuple.get(jProperty.nbr);

        switch (type.charAt(0)) {
            case 'O': return Persistent.object(str);
            case 'A': return Persistent.array();
            case 's': return str;
            case 'b': return nbr != 0;
            case 'l': return nbr;
            case 'd': return Double.longBitsToDouble(nbr);
            case 'D': return new BigDecimal(str);
            case 'n': return null;
            default:
                throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }

    private static Expression<?>[] concat(Expression<?>[] expr1, Expression<?>... expr2) {
        Expression<?>[] expressions = new Expression<?>[expr1.length + expr2.length];
        arraycopy(expr1, 0, expressions, 0, expr1.length);
        arraycopy(expr2, 0, expressions, expr1.length, expr2.length);
        return expressions;
    }

}
