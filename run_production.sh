#!/bin/bash
# Script de inicio para producción (run_production.sh)
# Uso: ./run_production.sh

set -a  # Exportar todas las variables
source .env.production  # Cargar variables de entorno
set +a

# Validar variables requeridas
if [ -z "$DB_HOST" ] || [ -z "$DB_USER" ] || [ -z "$DB_PASSWORD" ]; then
    echo "❌ Error: Variables de entorno no configuradas"
    echo "   DB_HOST, DB_USER, DB_PASSWORD son obligatorios"
    exit 1
fi

echo "🔧 Iniciando AgroComparador..."
echo "   Base de datos: $DB_HOST:$DB_PORT/$DB_NAME"
echo "   Puerto servidor: $PORT"

# Crear archivo de log
LOG_FILE="agrocomparador.log"

# Ejecutar aplicación en background con redirección de logs
nohup java -Xmx256m -cp ".:mysql-connector-java-9.0.0.jar" agrocomparador >> "$LOG_FILE" 2>&1 &

PID=$!
echo "✅ Aplicación iniciada (PID: $PID)"
echo "📝 Logs en: $LOG_FILE"
echo ""
echo "Para detener: kill $PID"
echo "Ver logs: tail -f $LOG_FILE"
