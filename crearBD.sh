#!/usr/bin/env bash
set -euo pipefail

# Nombre del contenedor al que nos conectaremos.
NODE_TO_EXEC="crdb-node1"

# Lista de bases de datos a crear.
DATABASES=(
  "db_patient_data"
  "db_health_alerts"
  "db_notifications"
)

echo "Esperando a que el clúster de CockroachDB esté listo..."
# Damos unos segundos extra para que el clúster se estabilice después del healthcheck.
sleep 15

echo "Creando bases de datos..."

for db in "${DATABASES[@]}"; do
  echo "Creando base de datos: $db"
  docker exec -i "$NODE_TO_EXEC" ./cockroach sql --insecure -e "CREATE DATABASE IF NOT EXISTS \"$db\";"
done

echo "Bases de datos creadas exitosamente."