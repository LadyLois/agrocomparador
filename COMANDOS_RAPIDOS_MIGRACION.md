# ========================================
# REFERENCIA RÁPIDA: Migración BD Local -> AWS EC2
# ========================================

## 🚀 OPCIÓN 1: MIGRACIÓN AUTOMATIZADA (Recomendado)

# Paso 1: Exportar BD (Windows PowerShell)
.\1_EXPORTAR_BD.ps1

# Paso 2: Transferir a EC2 (Windows PowerShell)
.\2_TRANSFERIR_A_AWS.ps1

# Paso 3: Importar en EC2 (SSH -> Bash)
scp -i tu-key.pem 3_IMPORTAR_EN_EC2.sh ec2-user@tu-ec2-ip:/tmp/
ssh -i tu-key.pem ec2-user@tu-ec2-ip
chmod +x /tmp/3_IMPORTAR_EN_EC2.sh
/tmp/3_IMPORTAR_EN_EC2.sh


## ⚡ OPCIÓN 2: COMANDOS MANUALES DIRECTOS

### En tu PC (Windows PowerShell)

# Exportar
mysqldump -h localhost -u admin -p agrocomparador > C:\Java\agroagrocomparador\backups\backup.sql

# Comprimir (opcional)
tar -czf C:\Java\agroagrocomparador\backups\backup.sql.gz C:\Java\agroagrocomparador\backups\backup.sql

# Transferir
scp -i tu-key.pem C:\Java\agroagrocomparador\backups\backup.sql ec2-user@tu-ec2-dns:/tmp/

### En EC2 (SSH)

# Descomprimir si es .gz
gunzip /tmp/backup.sql.gz

# Crear BD
mysql -h localhost -u admin -p -e "CREATE DATABASE agrocomparador CHARACTER SET utf8mb4;"

# Importar
mysql -h localhost -u admin -p agrocomparador < /tmp/backup.sql

# Verificar
mysql -h localhost -u admin -p agrocomparador -e "SELECT COUNT(*) as total FROM productos;"


## 🔧 CONFIGURAR APLICACIÓN PARA USAR BD REMOTA

### Opción A: En EC2, antes de ejecutar Java

# Variables de entorno
export DB_HOST=localhost              # O tu RDS endpoint si usas RDS
export DB_PORT=3306
export DB_NAME=agroagrocomparador
export DB_USER=admin
export DB_PASSWORD=tu_password
export PORT=8080

# Ejecutar aplicación
java -cp ".;mysql-connector-java-9.0.0.jar" agroagrocomparador

### Opción B: Crear archivo .env en EC2

# /home/ec2-user/agroagrocomparador/.env
DB_HOST=localhost
DB_PORT=3306
DB_NAME=agroagrocomparador
DB_USER=admin
DB_PASSWORD=tu_password
PORT=8080

# Ejecutar con source
source .env && java -cp ".;mysql-connector-java-9.0.0.jar" agroagrocomparador


## 📋 FORMATO DE ARCHIVO BACKUP RECOMENDADO

# Usar mysqldump con estas opciones para máxima compatibilidad:
mysqldump -h localhost -u admin -p \
  --set-charset \
  --default-character-set=utf8mb4 \
  --single-transaction \
  --lock-tables=false \
  --quick \
  --add-locks=false \
  --no-tablespaces \
  agroagrocomparador > backup.sql


## ❓ USAR RDS AMAZON EN LUGAR DE MYSQL EN EC2

# Ventajas de RDS:
# - Respaldos automáticos
# - Replicación automática
# - Mantenimiento automático
# - Mejor para producción

# Crear RDS MySQL:
aws rds create-db-instance \
  --db-instance-identifier agroagrocomparador-mysql \
  --db-instance-class db.t3.micro \
  --engine mysql \
  --engine-version 8.0 \
  --master-username admin \
  --master-user-password TuPasswordSegura123! \
  --allocated-storage 20 \
  --publicly-accessible false \
  --region us-east-1

# Luego transferir datos igual
mysqldump -u admin -p agrocomparador | \
  mysql -h agroagrocomparador-mysql.xxxxx.us-east-1.rds.amazonaws.com -u admin -p agrocomparador

# Y en tu aplicación usar:
export DB_HOST=agroagrocomparador-mysql.xxxxx.us-east-1.rds.amazonaws.com
java -cp ".;mysql-connector-java-9.0.0.jar" agroagrocomparador


## 🆘 TROUBLESHOOTING

### "MySQL connection refused"
# En EC2:
sudo systemctl status mysql
sudo systemctl start mysql
sudo systemctl enable mysql  # Auto-iniciar

### "Access denied for user"
# Resetear contraseña en EC2:
sudo mysql -u root
> ALTER USER 'admin'@'localhost' IDENTIFIED BY 'nueva_password';
> FLUSH PRIVILEGES;

### "Lost connection to MySQL during query"
# Aumentar timeout y memory en EC2:
sudo nano /etc/mysql/mysql.conf.d/mysqld.cnf

# Agregar/modificar:
[mysqld]
max_allowed_packet = 256M
connect_timeout = 60
read_timeout = 300
write_timeout = 300

# Reiniciar:
sudo systemctl restart mysql

### "Character set issues"
# Verificar en EC2:
mysql -h localhost -u admin -p agroagrocomparador -e "SHOW CREATE TABLE productos;"

# Convertir si es necesario:
mysql -h localhost -u admin -p agroagrocomparador -e "ALTER DATABASE agroagrocomparador CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

### "Out of memory during import"
# Aumentar recursos en EC2:
# O dividir el backup en partes más pequeñas


## 📊 VALIDACIÓN POST-MIGRACIÓN

# En EC2, verificar:
mysql -h localhost -u admin -p agroagrocomparador << 'EOF'
SHOW TABLES;
SELECT COUNT(*) as total_productos FROM productos;
SELECT COUNT(*) as total_fuentes FROM fuentes;
SELECT COUNT(*) as total_precios FROM precios;
SELECT * FROM productos LIMIT 5;
EOF


## 🔐 CHECKLIST DE SEGURIDAD

- [ ] BD remota NO está públicamente accesible (si algo falló, solo desde EC2)
- [ ] EC2 Security Group permite MySQL solo desde EC2 (no desde internet)
- [ ] SSH key (.pem) guardado en lugar seguro
- [ ] Contraseña BD es fuerte (no "admin" si es producción)
- [ ] Variable DB_PASSWORD está en environment, no en código
- [ ] Backup local guardado en carpeta backups/
- [ ] Firewall permite tu IP en puerto 22 (SSH)


## 📞 ¿TODO LISTO?

Una vez la BD esté en EC2:
1. Sube tu app Java a EC2
2. Configura las variables de entorno correctas
3. ¡Ejecuta tu aplicación!

Ver: DEPLOYMENT_AWS.md para la guía completa
