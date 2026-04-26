#!/bin/bash
# ========================================
# INSTALACIÓN DE DEPENDENCIAS EN EC2
# Ejecutar UNA VEZ en tu EC2 nueva
# ========================================

echo "🚀 Instalando dependencias en EC2..."
echo ""

# ===== ACTUALIZAR SISTEMA =====
echo "📦 Actualizando sistema..."
sudo yum update -y

# ===== INSTALAR JAVA =====
echo "☕ Instalando Java 11..."
sudo yum install -y java-11-openjdk java-11-openjdk-devel

# Verificar Java
echo "Verificando Java..."
java -version
echo ""

# ===== INSTALAR MYSQL =====
echo "🗄️ Instalando MySQL..."
sudo yum install -y mysql

# Verificar MySQL client
echo "Verificando MySQL client..."
mysql --version
echo ""

# ===== INICIAR MYSQL SERVER (si está en EC2) =====
echo "▶️  Iniciando MySQL..."
sudo systemctl start mysql

# Auto-iniciar en startup
sudo systemctl enable mysql

# Verificar estado
sudo systemctl status mysql
echo ""

# ===== VERIFICAR CONEXIÓN =====
echo "✔️ Verificando conexión a MySQL..."
mysql -h localhost -u root -e "SELECT 1;" 2>/dev/null && echo "✓ MySQL funcionando" || echo "⚠️ MySQL no responde aún"

echo ""
echo "✅ INSTALACIÓN COMPLETADA"
echo ""
echo "📋 Próximos pasos:"
echo "  1. Transferir tu archivo backup.sql"
echo "  2. Ejecutar: mysql -h localhost -u admin -p agrocomparador < backup.sql"
echo "  3. Transferir aplicación Java"
echo "  4. Ejecutar aplicación con variables de entorno"
echo ""
