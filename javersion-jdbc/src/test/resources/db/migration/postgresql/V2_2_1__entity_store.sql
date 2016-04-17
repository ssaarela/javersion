-- Find unpublished versions
create index ENTITY_UNPUBLISHED_IDX on ENTITY_VERSION (ORDINAL, DOC_ID, REVISION) where ORDINAL is null;
