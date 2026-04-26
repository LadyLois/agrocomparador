#!/bin/bash
# ========================================
# SCRIPT 3: IMPORTAR EN EC2
# Ejecutar en: tu EC2 (bash)
# ========================================

set -e

echo "🚀 Iniciando importación de base de datos en EC2..."
echo ""

# ===== CONFIGURACIÓN =====
DB_HOST="localhost"
DB_USER="admin"
DB_NAME="agrocomparador"
BACKUP_DIR="/tmp"

# ===== VARIABLES INTERNAS =====
GREEN='\033[0;32m'
CYAN='\033[0;36m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# ===== FUNCIONES =====
log_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

log_info() {
    echo -e "${CYAN}ℹ $1${NC}"
}

log_warn() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

log_error() {
    echo -e "${RED}❌ $1${NC}"
}

# ===== VALIDAR PROGRAMAS =====
log_info "Validando que MySQL está instalado..."

if ! command -v mysql &> /dev/null; then
    log_error "MySQL no está instalado"
    echo "Instala con: sudo yum install mysql (AWS Linux) o sudo apt-get install mysql-client (Ubuntu)"
    exit 1
fi

log_success "MySQL cliente encontrado"

# ===== VALIDAR MySQL SERVIDOR =====
log_info "Validando conexión a MySQL servidor..."

if ! mysql -h $DB_HOST -u $DB_USER -p -e "SELECT 1" &>/dev/null; then
    log_error "No se puede conectar a MySQL en $DB_HOST"
    echo "Verifica:"
    echo "  - MySQL servidor está corriendo: sudo systemctl status mysql"
    echo "  - Usuario/contraseña son correctos"
    exit 1
fi

log_success "Conexión a MySQL establecida"

# ===== LISTAR ARCHIVOS BACKUP =====
echo ""
log_info "Archivos disponibles en $BACKUP_DIR:"

backups=()
for file in "$BACKUP_DIR"/backup_comparador*.sql*; do
    if [ -f "$file" ]; then
        size=$(du -h "$file" | cut -f1)
        backups+=("$file")
        echo "  [${#backups[@]}] $(basename "$file") ($size)"
    fi
done

if [ ${#backups[@]} -eq 0 ]; then
    log_error "No hay archivos .sql en $BACKUP_DIR"
    exit 1
fi

# ===== SELECCIONAR ARCHIVO =====
echo ""
read -p "Selecciona el número del archivo a importar: " selection

# Validar selección
if ! [[ "$selection" =~ ^[0-9]+$ ]] || [ "$selection" -lt 1 ] || [ "$selection" -gt ${#backups[@]} ]; then
    log_error "Selección inválida"
    exit 1
fi

BACKUP_FILE="${backups[$((selection-1))]}"
BACKUP_NAME=$(basename "$BACKUP_FILE")

# ===== DETECTAR Y DESCOMPRIMIR SI ES NECESARIO =====
echo ""
if [[ "$BACKUP_NAME" == *".gz" ]]; then
    log_info "Detectado archivo .gz, descomprimiendo..."
    gunzip -f "$BACKUP_FILE"
    BACKUP_FILE="${BACKUP_FILE%.gz}"
    BACKUP_NAME="${BACKUP_NAME%.gz}"
    log_success "Archivo descomprimido"
fi

# ===== CREAR BASE DE DATOS =====
echo ""
log_info "Creando base de datos: $DB_NAME..."

mysql -h $DB_HOST -u $DB_USER -p -e "CREATE DATABASE IF NOT EXISTS $DB_NAME CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>/dev/null || {
    log_warn "BD ya existe o no se puede crear"
}

log_success "BD creada o ya existe"

# ===== IMPORTAR =====
echo ""
log_info "Importando datos desde: $BACKUP_NAME"
log_warn "Esto puede tomar varios minutos según el tamaño..."
echo ""

if mysql -h $DB_HOST -u $DB_USER -p $DB_NAME < "$BACKUP_FILE" 2>/dev/null; then
    log_success "Datos importados exitosamente"
else
    log_error "Error al importar datos"
    echo "Intenta nuevamente o verifica la integridad del archivo SQL"
    exit 1
fi

# ===== VERIFICAR =====
echo ""
log_info "Verificando datos importados..."
echo ""

# Contar registros
PRODUCTOS=$(mysql -h $DB_HOST -u $DB_USER -p -N -e "USE $DB_NAME; SELECT COUNT(*) FROM productos;" 2>/dev/null)
FUENTES=$(mysql -h $DB_HOST -u $DB_USER -p -N -e "USE $DB_NAME; SELECT COUNT(*) FROM fuentes;" 2>/dev/null)
PRECIOS=$(mysql -h $DB_HOST -u $DB_USER -p -N -e "USE $DB_NAME; SELECT COUNT(*) FROM precios;" 2>/dev/null)

echo "  📊 Productos: $PRODUCTOS"
echo "  📊 Fuentes:   $FUENTES"
echo "  📊 Precios:   $PRECIOS"

# Validar que hay datos
if [ "$PRODUCTOS" -gt 0 ] && [ "$PRECIOS" -gt 0 ]; then
    log_success "Datos verificados correctamente"
    echo ""
    log_success "IMPORTACIÓN COMPLETADA"
    echo ""
    echo "🎉 Base de datos $DB_NAME lista en AWS EC2"
    echo ""
    echo "Próximos pasos:"
    echo "  1. Transferir aplicación Java a EC2"
    echo "  2. Configurar variables de entorno"
    echo "  3. Ejecutar aplicación"
    exit 0
else
    log_warn "Veifica los datos importados, parecen estar incompletos"
fi

