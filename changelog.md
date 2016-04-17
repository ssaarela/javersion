WIP: 0.14 JDBC-Based Persistence Optimization
=============================================

Breaking Changes
----------------
* Rename module `javersion-spring-jdbc` to `javersion-jdbc` 
* Make Spring optional dependency
  * Configurable `Transactions` interface with `SpringTransactions` implementation instead of `@Transactional` 
* Cannot use CGLIB proxy for VersionStore
  * AbstractVersionStoreJdbc uses final public methods to enforce correct transaction handling
* Database schema changes
  * Options `repository` and `repositoryName` are dropped from StoreOptions
    * `REPOSITORY` table is no longer needed - version rows locked instead
    * `VERSION_TYPE` table is no longer needed - use check constraint instead
  * Status column added to all tables
  * Changes to column types and indexes

New Features
------------
* Configurable VersionGraph load optimization strategy
  * Documents are automatically optimized in background when needed
  * Configurable when and what (versions) to optimize - default is not to optimize anything
  * Configurable Executor for optimizations
* Configurable VersionGraphCache compaction strategy (when and what)
* PostgreSQL specific optimizations in [javersion-jdbc/src/test/resources/db/migration/postgresql](javersion-jdbc/src/test/resources/db/migration/postgresql)
