#!/bin/bash
# deploy_production.sh — Copia y recompila en el servidor de producción (EC2/Linux)
# Uso local: bash scripts/execution/deploy_production.sh USER@HOST RUTA_PROYECTO
# Ejemplo:   bash scripts/execution/deploy_production.sh ubuntu@1.2.3.4 /home/ubuntu/agrocomparador

set -e

REMOTE="${1:?Uso: $0 USER@HOST RUTA_PROYECTO}"
REMOTE_DIR="${2:-/home/ubuntu/agrocomparador}"

echo "📦 Copiando fuentes Java a $REMOTE:$REMOTE_DIR ..."
scp agrocomparador.java "$REMOTE:$REMOTE_DIR/"
scp agrocomparador/business/ProductoService.java       "$REMOTE:$REMOTE_DIR/agrocomparador/business/"
scp agrocomparador/data/DatabaseConnection.java        "$REMOTE:$REMOTE_DIR/agrocomparador/data/"
scp agrocomparador/data/ProductoDAO.java               "$REMOTE:$REMOTE_DIR/agrocomparador/data/"
scp agrocomparador/data/ProductoDAOScraper.java        "$REMOTE:$REMOTE_DIR/agrocomparador/data/"
scp agrocomparador/scraper/AgrePreciosScraperDAO.java  "$REMOTE:$REMOTE_DIR/agrocomparador/scraper/"
scp agrocomparador/scraper/AgroPizarraScraperDAO.java  "$REMOTE:$REMOTE_DIR/agrocomparador/scraper/"
scp agrocomparador/scraper/ScraperScheduler.java       "$REMOTE:$REMOTE_DIR/agrocomparador/scraper/"
scp agrocomparador/ui/HTMLBuilder.java                 "$REMOTE:$REMOTE_DIR/agrocomparador/ui/"
scp agrocomparador/ui/WebServer.java                   "$REMOTE:$REMOTE_DIR/agrocomparador/ui/"
scp scripts/migration/migrate_schema.sql               "$REMOTE:$REMOTE_DIR/"

echo "⚙️  Compilando en remoto..."
ssh "$REMOTE" "cd $REMOTE_DIR && \
  javac -cp '.:mysql-connector-java-9.0.0.jar:jsoup-1.15.3.jar' \
    agrocomparador.java \
    agrocomparador/data/*.java \
    agrocomparador/business/*.java \
    agrocomparador/scraper/*.java \
    agrocomparador/ui/*.java && \
  echo '✅ Compilación correcta'"

echo ""
echo "⚠️  PENDIENTE — ejecuta en el servidor si es la primera vez:"
echo "   mysql -u \$DB_USER -p \$DB_NAME < $REMOTE_DIR/migrate_schema.sql"
echo ""
echo "🔄  Reinicia el servidor para aplicar los cambios:"
echo "   pkill -f 'java.*agrocomparador' ; cd $REMOTE_DIR && bash scripts/execution/run_production.sh"
