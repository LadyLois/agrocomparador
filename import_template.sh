#!/bin/bash
set -e

BACKUP_FILE="/tmp/BACKUP_FILENAME_PLACEHOLDER"
DB_USER="admin"
DB_NAME="agrocomparador"

echo "Importando BD en EC2..."

# Descomprimir si es .gz
if [[ "$BACKUP_FILE" == *.gz ]]; then
    gunzip -f "$BACKUP_FILE"
    BACKUP_FILE="${BACKUP_FILE%.gz}"
fi

# Crear BD
mysql -h localhost -u $DB_USER -p -e "CREATE DATABASE IF NOT EXISTS $DB_NAME CHARACTER SET utf8mb4;" 2>/dev/null || true

# Importar
mysql -h localhost -u $DB_USER -p $DB_NAME < "$BACKUP_FILE"

# Verificar
COUNT=$(mysql -h localhost -u $DB_USER -p -N -e "USE $DB_NAME; SELECT COUNT(*) FROM productos;" 2>/dev/null)

echo "BD Importada. Productos encontrados: $COUNT"
