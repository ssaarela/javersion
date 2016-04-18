WIP: 0.14 JDBC-Based Persistence Optimization
=============================================

Breaking Changes
----------------
* Core
  * Interface for VersionGraph with simplified generic signature: <K, V, M> (key, value, meta)
* JDBC
  * Interface for VersionStore with simplified generic signature: <Id, M> (id-type, meta)
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

New Features
------------
* Configurable VersionGraph load optimization strategy
  * Documents are automatically optimized in background when needed
  * Configurable when and what (versions) to optimize - default is not to optimize anything
  * Configurable Executor for optimizations
* Configurable VersionGraphCache compaction strategy (when and what)
* PostgreSQL specific optimizations in [javersion-jdbc/src/test/resources/db/migration/postgresql](javersion-jdbc/src/test/resources/db/migration/postgresql)
