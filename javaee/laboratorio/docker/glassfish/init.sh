#!/usr/bin/env bash
set -euo pipefail

: "${DB_HOST:=banco}"
: "${DB_PORT:=5432}"
: "${DB_NAME:=estudos_javaee}"
: "${DB_USER:=estudos}"
: "${DB_PASSWORD:?DB_PASSWORD não configurada}"

# Official GlassFish image runs this script BEFORE normal server start.
# Start a temporary domain to create JDBC resources, then stop it.
asadmin --interactive=false start-domain domain1
asadmin --interactive=false create-jdbc-connection-pool \
  --datasourceclassname org.postgresql.ds.PGSimpleDataSource \
  --restype javax.sql.DataSource \
  --property "serverName=${DB_HOST}:portNumber=${DB_PORT}:databaseName=${DB_NAME}:user=${DB_USER}:password=${DB_PASSWORD}" \
  EstudosPool
asadmin --interactive=false create-jdbc-resource \
  --connectionpoolid EstudosPool jdbc/EstudosDS
asadmin --interactive=false ping-connection-pool EstudosPool
asadmin --interactive=false stop-domain domain1
