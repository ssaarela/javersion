WIP: 0.14 JDBC-Based Persistence Optimization
=============================================

New Features
------------
* JDBC VersionStore 
  * VersionGraph load optimization
    * Configurable when and what (versions) to optimize - default is not to optimize anything
    * Documents are automatically optimized in background when needed
    * Configurable Executor for optimizations: ASYNC, SYNC, NONE or custom
  * Automatic publishing 
    * Configurable executor: ASYNC, SYNC, NONE or custom
  * Configurable cache
    * Guava Cache based implementation
      * Configurable compaction strategy (when and what)
  * PostgreSQL specific optimizations
    * [javersion-jdbc/src/test/resources/db/migration/postgresql](javersion-jdbc/src/test/resources/db/migration/postgresql)
    * Querydsl SQLTemplates: `org.javersion.store.jdbc.PostgreSQLTemplatesForNoKeyUpdate`

Breaking Changes
----------------
* Core
  * VersionGraph as an interface with simplified generic signature: <K, V, M> (key, value, meta)
  * Switch Revision(node, timeSeq) constructor parameters: Revision(timeSeq, node) that is more in sync with serialization
* JDBC
  * Interface for VersionStore with simplified generic signature: <Id, M> (id-type, meta)
  * Rename VersionStore load\* methods to get\*Graph
    * `getFullGraph(docId)` - load full (original/unoptimized) VersionGraph
    * `getOptimizedGraph(docId)` - load optimized VersionGraph
    * `getGraph(docId)` - load VersionGraph using the fastest method available (e.g. cache)
    * `getGraph(docId, revisions)` - as getGraph(Id) but fallback to getFullGraph if all revisions are not found
  * Interface for UpdateBatch with simplified generic signature: <Id, M>
  * Rename module `javersion-spring-jdbc` to `javersion-jdbc` 
  * Make Spring dependency optional
    * Configurable `Transactions` interface with `SpringTransactions` implementation instead of `@Transactional` 
  * Database schema changes
    * `REPOSITORY` table is no longer needed - version rows locked instead
    * `VERSION_TYPE` table is no longer needed - uses check constraint instead
    * Status column added to all tables
    * Changes to column types and indexes
  * Options `repository` and `repositoryName` are dropped from StoreOptions
