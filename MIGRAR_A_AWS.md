# 🚀 Guía Completa: Migrar BD Local a AWS EC2

## 📋 Requisitos Previos

✅ Tienes BD local MySQL en ejecución (`comparador`)
✅ AWS EC2 instancia creada con MySQL instalado
✅ Acceso SSH a tu EC2 (archivo .pem guardado)
✅ Security Group de EC2 permite:
   - Puerto 3306 (MySQL) desde tu EC2
   - Puerto 22 (SSH) desde tu IP
   - Puerto 8080 (Aplicación) público si es necesario

---

## 🔄 Método 1: MIGRACIÓN RÁPIDA (Recomendado - Archivo SQL)

### Paso 1️⃣: Exportar BD Local

**Windows (PowerShell):**
```powershell
# Navega a la carpeta del proyecto
cd c:\Java\agrocomparador

# Exportar toda la BD local a archivo SQL
mysql -h localhost -u admin -p comparador > backup_comparador.sql
# Te pedirá contraseña: AgroComparador2026!
```

**Alternativa (Windows CMD):**
```cmd
mysqldump -h localhost -u admin -p --databases comparador > backup_comparador.sql
```

**Verificar que el archivo se creó:**
```powershell
Get-Item backup_comparador.sql | Select-Object Length, LastWriteTime
```

---

### Paso 2️⃣: Copiar archivo SQL a EC2

**Desde tu Windows (PowerShell):**
```powershell
# Reemplaza con tu información AWS
$EC2_IP = "ec2-XX-XX-XX-XX.compute-1.amazonaws.com"
$PEM_FILE = "C:\path\a\tu-key.pem"
$AWS_USER = "ec2-user"  # o "ubuntu" si es Ubuntu

# Copiar archivo SQL a EC2
scp -i $PEM_FILE backup_comparador.sql ${AWS_USER}@${EC2_IP}:/tmp/
```

**O usar AWS Systems Manager Session Manager:**
```powershell
# Si tu EC2 tiene rol IAM con Systems Manager
aws ssm start-session --target i-xxxxxxxxxxxxx  # tu instance ID
```

---

### Paso 3️⃣: Importar BD en EC2

**Conéctate por SSH a tu EC2:**
```powershell
ssh -i $PEM_FILE ${AWS_USER}@${EC2_IP}
```

**Una vez en EC2, importar la BD:**
```bash
# Crear BD vacía primero
mysql -h localhost -u admin -p -e "CREATE DATABASE IF NOT EXISTS comparador CHARACTER SET utf8 COLLATE utf8_general_ci;"

# Importar datos desde el backup
mysql -h localhost -u admin -p comparador < /tmp/backup_comparador.sql

# Verificar que se importó correctamente
mysql -h localhost -u admin -p comparador -e "SELECT COUNT(*) as total_productos FROM productos; SELECT COUNT(*) as total_precios FROM precios;"
```

---

## 🔄 Método 2: MIGRACIÓN CON mysqldump (Más Seguro)

### Paso 1️⃣: Crear backup completo desde Windows

```powershell
cd c:\Java\agrocomparador

# Backup con carácter UTF-8 y compatible con AWS
mysqldump -h localhost -u admin -p `
  --set-charset `
  --default-character-set=utf8mb4 `
  --single-transaction `
  --lock-tables=false `
  comparador > backup_completo_$(Get-Date -Format 'yyyy-MM-dd_HHmmss').sql
```

### Paso 2️⃣: Comprimir para transferencia rápida

```powershell
# Instalar 7-Zip si no lo tienes (o usar cmd: tar)
tar -czf backup_comparador.sql.gz backup_comparador.sql
```

### Paso 3️⃣: Transferir a EC2 y descomprimir

```powershell
# Transferir
scp -i $PEM_FILE backup_comparador.sql.gz ${AWS_USER}@${EC2_IP}:/tmp/

# En EC2
ssh -i $PEM_FILE ${AWS_USER}@${EC2_IP}

# Dentro de EC2
gunzip /tmp/backup_comparador.sql.gz
mysql -h localhost -u admin -p comparador < /tmp/backup_comparador.sql
```

---

## ⚠️ Método 3: MIGRACIÓN DIRECTA (Sin archivo intermedio)

**Si tu EC2 puede acceder a tu BD local (firewall/VPN):**

```bash
# En tu EC2, directamente desde Windows BD
mysqldump -h YOUR_LOCAL_IP -u admin -p comparador | \
  mysql -h localhost -u admin -p comparador

# O en PowerShell Windows (si MySQL está en Path):
mysqldump -h localhost -u admin -p comparador | `
  mysql -h tu-ec2-private-ip.compute-1.amazonaws.com -u admin -p comparador
```

---

## ✅ Verificación Post-Migración

**En tu EC2, verifica:**

```bash
# 1️⃣ Conectar a BD
mysql -h localhost -u admin -p comparador

# 2️⃣ Dentro de MySQL, ejecuta:
SQL> SHOW TABLES;
SQL> SELECT COUNT(*) as registros FROM productos;
SQL> SELECT COUNT(*) as precios FROM precios;
SQL> SELECT COUNT(*) as fuentes FROM fuentes;
```

**Resultado esperado:**
```
registros: 8 (o más si agregaste datos)
precios: 16+ (o más si agregaste datos)
fuentes: 4+ (o más si agregaste datos)
```

---

## 🖧 Configurar tu Aplicación Java para AWS

### Opción A: Variables de Entorno en EC2

**Archivo `/etc/environment` en EC2:**
```bash
# Conectar a EC2 y editar:
sudo nano /etc/environment

# Agregar:
DB_HOST="localhost"
DB_PORT="3306"
DB_NAME="comparador"
DB_USER="admin"
DB_PASSWORD="tu_password_aqui"
PORT="8080"

# Guardar: Ctrl+O, Enter, Ctrl+X
```

**Luego recargar:**
```bash
source /etc/environment
```

### Opción B: Script de inicio con variables

**Crear `run_aws.sh` en EC2:**
```bash
#!/bin/bash
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=comparador
export DB_USER=admin
export DB_PASSWORD="tu_password"
export PORT=8080

java -cp ".:mysql-connector-java-9.0.0.jar" agrocomparador
```

**Hacer ejecutable:**
```bash
chmod +x run_aws.sh
./run_aws.sh
```

### Opción C: RDS en lugar de MySQL local en EC2

Si prefieres usar **AWS RDS MySQL** (en lugar de MySQL en EC2):

```bash
# Variables para RDS
export DB_HOST="agrocomparador-prod.xxxxx.us-east-1.rds.amazonaws.com"
export DB_PORT="3306"
export DB_NAME="comparador"
export DB_USER="admin"
export DB_PASSWORD="contraseña_rds"
export PORT="8080"

java -cp ".:mysql-connector-java-9.0.0.jar" agrocomparador
```

---

## 🔐 Seguridad - Checklist

- [ ] BD local tiene contraseña fuerte (no `admin`)
- [ ] SSH key (.pem) guardado en lugar seguro
- [ ] EC2 Security Group permite solo tu IP en puerto 22
- [ ] BD contraseña NO está en el código fuente
- [ ] Variables de entorno usadas (ver `DatabaseConnection.java`)
- [ ] Backup local guardado en carpeta segura
- [ ] RDS/MySQL en EC2 no está públicamente accesible (solo desde EC2)

---

## 🆘 Troubleshooting

### Error: "MySQL connection refused"
```bash
# En EC2, verifica que MySQL esté corriendo
sudo systemctl status mysql
sudo systemctl start mysql
```

### Error: "Access denied for user 'admin'@'localhost'"
```bash
# Resetear contraseña MySQL en EC2
sudo mysql -u root
> ALTER USER 'admin'@'localhost' IDENTIFIED BY 'nueva_password';
> FLUSH PRIVILEGES;
```

### Error: "Out of memory" en importación
```bash
# Aumentar memoria para mysqld
sudo nano /etc/mysql/mysql.conf.d/mysqld.cnf

# Cambiar:
max_connections = 1000
max_allowed_packet = 256M

# Reiniciar:
sudo systemctl restart mysql
```

### Error: "Character set 'utf8' is not valid"
```bash
# En el comando mysqldump, usar utf8mb4:
mysqldump --default-character-set=utf8mb4 comparador > backup.sql
```

---

## 📊 Próximos Pasos

1. ✅ Migrar BD (esta guía)
2. ⏭️ Compilar aplicación Java en EC2
3. ⏭️ Configurar variables de entorno
4. ⏭️ Ejecutar aplicación
5. ⏭️ Configurar auto-inicio con systemd
6. ⏭️ Configurar ALB/ELB para DNS público

---

## 🚀 Comando Rápido (Resumen)

```powershell
# 1. Exportar en Windows
mysqldump -h localhost -u admin -p comparador > backup.sql

# 2. Copiar a EC2
scp -i tu-key.pem backup.sql ec2-user@tu-ec2-ip:/tmp/

# 3. Importar en EC2
ssh -i tu-key.pem ec2-user@tu-ec2-ip
mysql -h localhost -u admin -p comparador < /tmp/backup.sql

# 4. Verificar en EC2
mysql -h localhost -u admin -p comparador -e "SELECT COUNT(*) FROM productos;"
```

