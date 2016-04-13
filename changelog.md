0.14 JDBC-Based Persistence Optimization
========================================

Breaking Changes
----------------
* Rename module `javersion-spring-jdbc` to `javersion-jdbc` 
* Make Spring optional dependency
** Configurable `Transactions` interface with `SpringTransactions` implementation instead of `@Transactional` 
* Cannot use CGLIB proxy for VersionStore
** AbstractVersionStoreJdbc uses final public methods to enforce correct transaction handling
* Options `repository` and `repositoryName` are dropped from StoreOptions
** `REPOSITORY` table is no longer needed - version rows locked instead
** `VERSION_TYPE` table is no longer needed - use check constraint instead

New Features
------------
* Configurable VersionGraph load optimization strategy
** Documents are automatically optimized in background when needed
* Configurable VersionGraphCache compaction strategy
* PostgreSQL specific optimizations in `db/migration/postgresql/*.sql`
