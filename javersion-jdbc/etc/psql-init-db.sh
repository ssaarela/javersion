#!/bin/bash

# Usage examples
#
# - create local
# bash etc/psql-init-db.sh -h localhost -U username

function init() {
  export PGCLIENTENCODING="UTF8"
  : ${DB:="javersion"}
  : ${DB_SCHEMA:=$DB}
  : ${DB_USER:=$DB}
  : ${DB_PASS:=$DB}

  PSQL_OPTS=("-v" "ON_ERROR_STOP=1")
  for arg do
    case "$arg" in
      --schema-only) SCHEMA_ONLY=1 ;;
      *) PSQL_OPTS+=("$arg") ;;
    esac
  done
}

function create_db() {
  psql "${PSQL_OPTS[@]}" <<EOF
DROP DATABASE IF EXISTS $DB;
CREATE DATABASE $DB ENCODING 'UTF8' LC_COLLATE 'fi_FI.UTF-8' LC_CTYPE 'fi_FI.UTF-8' TEMPLATE template0;
\q
EOF
}

function create_user() {
  psql "${PSQL_OPTS[@]}" <<EOF
DROP USER IF EXISTS $DB_USER;
CREATE USER $DB_USER WITH ENCRYPTED PASSWORD '$DB_PASS';
GRANT CONNECT, TEMP ON DATABASE $DB TO $DB_USER;
EOF
}

function create_schema() {
  psql "${PSQL_OPTS[@]}" <<EOF
\connect $DB
DROP SCHEMA IF EXISTS $DB_SCHEMA;
CREATE SCHEMA $DB_SCHEMA;
GRANT ALL PRIVILEGES ON SCHEMA $DB_SCHEMA TO $DB_USER;
\q
EOF
}

function run() {
  if [ -z "$SCHEMA_ONLY" ] ; then
    create_db
    create_user
  fi
  create_schema
}

VERBOSE="true"

init "$@"

run "$@"
