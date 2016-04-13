0.14 JDBC-Based Persistence Optimization
========================================

* Rename module `javersion-spring-jdbc` to `javersion-jdbc` 
* Make Spring optional dependency
** Configurable `Transactions` interface with `SpringTransactions` implementation instead of `@Transactional` 
* Configurable VersionGraph load optimization strategy
** Documents are automatically optimized in background when needed
* Configurable VersionGraphCache compaction strategy
* PostgreSQL optimizations
** db/migration/postgresql/*.sql