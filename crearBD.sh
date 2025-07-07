#!/usr/bin/env bash
set -euo pipefail

# Apunta a un solo nodo, el primero es suficiente.
# El clúster se encargará de la consistencia.
NODE="crdb-node1"

SQL='
CREATE DATABASE IF NOT EXISTS "db-notifications";
CREATE DATABASE IF NOT EXISTS "db-catalog";
CREATE DATABASE IF NOT EXISTS "db-publish";
'

# Ejecuta el comando SQL solo una vez en el nodo especificado.
echo "Initializing databases on node $NODE..."
docker exec -i "$NODE" ./cockroach sql --insecure -e "$SQL"
echo "Databases initialized successfully."