# 🚀 Migrar BD Local a AWS EC2 - GUÍA PASO A PASO

> **Tiempo estimado:** 20-30 minutos

---

## 🎯 Requisitos Previos

✅ **Local (tu PC):**
- MySQL instalado (`mysql` y `mysqldump` en PATH)
- BD `comparador` creada con datos
- PowerShell 5.1+ con acceso a herramientas (scp, ssh)

✅ **AWS EC2:**
- Instancia EC2 ejecutándose (Linux)
- MySQL instalado y corriendo
- SSH key (.pem) descargado
- Security Group permite:
  - Puerto 22 (SSH) desde tu IP
  - Puerto 3306 (MySQL) desde EC2 hacia localhost (interno)

---

## ⚡ OPCIÓN A: MIGRACIÓN RÁPIDA EN 1 COMANDO (Recomendado)

```powershell
# En tu PC, PowerShell
# Actualiza EC2_IP y PEM_FILE con tus valores
powershell -ExecutionPolicy Bypass -File MIGRACION_RAPIDA.ps1
```

**Eso es todo.** El script:
1. ✅ Exporta tu BD local
2. ✅ Transfiere el archivo a EC2
3. ✅ Importa automáticamente en EC2

---

## 📋 OPCIÓN B: MIGRACIÓN PASO A PASO (Manual)

### Paso 1️⃣: EXPORTAR BD LOCAL (tu PC)

**Windows PowerShell:**
```powershell
cd c:\Java\agrocomparador

# Exportar
mysqldump -h localhost -u admin -p `
  --set-charset `
  --default-character-set=utf8mb4 `
  --single-transaction `
  --lock-tables=false `
  comparador > backup_comparador.sql

# Cuando pida contraseña: AgroComparador2026! (o tu contraseña)
```

**Verificar que se creó:**
```powershell
dir backup_comparador.sql
```

✅ **Resultado:** Archivo `backup_comparador.sql` creado

---

### Paso 2️⃣: TRANSFERIR A EC2 (tu PC)

**Actualiza estas variables en PowerShell:**
```powershell
$EC2_IP = "ec2-54-123-45-67.compute-1.amazonaws.com"  # ← Tu EC2 DNS
$EC2_USER = "ec2-user"  # o "ubuntu" para Ubuntu
$PEM_FILE = "C:\Users\TuUsuario\Downloads\tu-key.pem"  # ← Tu archivo .pem
```

**Transferir el archivo:**
```powershell
scp -i $PEM_FILE backup_comparador.sql "${EC2_USER}@${EC2_IP}:/tmp/"
```

✅ **Resultado:** Archivo transferido a EC2

---

### Paso 3️⃣: CONECTAR A EC2 (tu PC)

**Abre SSH:**
```powershell
ssh -i $PEM_FILE ${EC2_USER}@${EC2_IP}
```

**Deberías ver el prompt de EC2:**
```
[ec2-user@ip-172-xx-xx-xx ~]$
```

✅ **Ahora estás dentro de tu EC2 Linux**

---

### Paso 4️⃣: IMPORTAR BD EN EC2 (dentro de SSH)

**Verificar que el archivo está allí:**
```bash
ls -lh /tmp/backup_comparador.sql
```

**Crear la BD:**
```bash
mysql -h localhost -u admin -p -e "CREATE DATABASE IF NOT EXISTS comparador CHARACTER SET utf8mb4;"
```
*Ingresa contraseña MySQL cuando pida*

**Importar los datos:**
```bash
mysql -h localhost -u admin -p comparador < /tmp/backup_comparador.sql
```
*De nuevo, ingresa contraseña MySQL*

✅ **Resultado:** BD importada completamente

---

### Paso 5️⃣: VERIFICAR LOS DATOS (en EC2)

```bash
# Conectar a MySQL y verificar
mysql -h localhost -u admin -p comparador
```

**Dentro de MySQL:**
```sql
SHOW TABLES;
SELECT COUNT(*) as total_productos FROM productos;
SELECT COUNT(*) as total_precios FROM precios;
EXIT;
```

✅ **Deberías ver datos importados correctamente**

---

## 🖧 CONFIGURAR APLICACIÓN JAVA PARA USAR BD REMOTA

### Opción 1: Variables de Entorno (Recomendado)

**En EC2, antes de ejecutar Java:**

```bash
# En el archivo ~/.bashrc o como variables globales
export DB_HOST=localhost              
export DB_PORT=3306
export DB_NAME=comparador
export DB_USER=admin
export DB_PASSWORD=tu_password_mysql
export PORT=8080

# Ejecutar aplicación
java -cp ".;mysql-connector-java-9.0.0.jar" agrocomparador
```

**O en una sola línea:**
```bash
DB_HOST=localhost DB_USER=admin DB_PASSWORD=tu_password java -cp ".;mysql-connector-java-9.0.0.jar" agrocomparador
```

### Opción 2: Archivo .env

**En EC2:**
```bash
# Crear archivo
nano /home/ec2-user/.env

# Agregar:
DB_HOST=localhost
DB_PORT=3306
DB_NAME=comparador
DB_USER=admin
DB_PASSWORD=tu_password
PORT=8080

# Ctrl+O, Enter, Ctrl+X para guardar

# Ejecutar:
source .env && java -cp ".;mysql-connector-java-9.0.0.jar" agrocomparador
```

### Opción 3: Si usas RDS (en lugar de MySQL en EC2)

**Variables para RDS:**
```bash
export DB_HOST="agrocomparador-prod.xxxxx.us-east-1.rds.amazonaws.com"
export DB_PORT="3306"
export DB_NAME="comparador"
export DB_USER="admin"
export DB_PASSWORD="tu_password_rds"
export PORT="8080"

java -cp ".;mysql-connector-java-9.0.0.jar" agrocomparador
```

---

## ❓ PREGUNTAS FRECUENTES

### ❓ ¿Qué pasa si se corta la transferencia?
```powershell
# Reintentar (scp tiene soporte para resumir)
scp -i $PEM_FILE -C backup_comparador.sql "${EC2_USER}@${EC2_IP}:/tmp/"
```

### ❓ ¿Por qué pide la contraseña MySQL 2 veces?
Es normal. Una vez para crear la BD y otra para importar.

### ❓ ¿Cómo resetear contraseña MySQL en EC2?
```bash
sudo mysql -u root
ALTER USER 'admin'@'localhost' IDENTIFIED BY 'nueva_password';
FLUSH PRIVILEGES;
EXIT;
```

### ❓ ¿Puedo usar RDS en lugar de MySQL en EC2?
Sí, es mejor para producción. Los pasos son iguales, solo cambias el HOST en las variables.

### ❓ ¿Cómo verifico que MySQL está corriendo en EC2?
```bash
sudo systemctl status mysql
sudo systemctl start mysql      # para iniciar
sudo systemctl enable mysql     # para auto-iniciar
```

### ❓ ¿Qué archivo SQL debería transferir?
- Si es < 10 MB: el `.sql` normal
- Si es > 10 MB: comprime con `tar -czf backup.sql.gz backup.sql`

---

## 🔐 CHECKLIST DE SEGURIDAD

- [ ] Contraseña MySQL es fuerte (no "admin")
- [ ] SSH key (.pem) está protegida (no compartir)
- [ ] EC2 Security Group NO permite MySQL públicamente (solo 22 SSH)
- [ ] Variables de entorno se usan (no hardcodear credenciales)
- [ ] Backup local guardado en lugar seguro
- [ ] BD remota NO está públicamente accesible

---

## 🚨 TROUBLESHOOTING

### Error: "mysql: command not found"
```bash
# En EC2, instalar MySQL client:
sudo yum install mysql         # Amazon Linux
sudo apt install mysql-client  # Ubuntu
```

### Error: "Connection refused" al importar
```bash
# Verificar que MySQL corre:
sudo systemctl status mysql

# Si no está corriendo:
sudo systemctl start mysql
```

### Error: "Access denied for user"
- Verifica que usas `admin` como usuario
- Contraseña correcta
- O resetea contraseña (ver preguntas frecuentes)

### Error: "Out of memory"
```bash
# En EC2, aumentar memoria de MySQL
sudo nano /etc/mysql/mysql.conf.d/mysqld.cnf

# Agregar bajo [mysqld]:
max_allowed_packet = 256M

# Reiniciar:
sudo systemctl restart mysql
```

### Error: "Character set 'utf8' is not valid"
```bash
# Asegúrate de usar utf8mb4:
mysqldump --default-character-set=utf8mb4 comparador > backup.sql
```

---

## 📞 RESUMEN RÁPIDO

| Paso | Comando | Dónde |
|------|---------|-------|
| Exportar | `mysqldump ... > backup.sql` | Tu PC |
| Transferir | `scp ... backup.sql ec2:` | Tu PC |
| Importar | `mysql ... < backup.sql` | EC2 SSH |
| Verificar | `mysql -e "SELECT COUNT(...)" comparador` | EC2 SSH |
| Ejecutar app | `java ... agrocomparador` | EC2 SSH |

---

## ✅ SIGUIENTES PASOS

Una vez la BD esté en EC2:

1. **Sube tu aplicación Java** a EC2
   ```bash
   scp -i tu-key.pem agrocomparador/ ec2-user@EC2_IP:~/
   ```

2. **Verifica la conexión** desde Java
   ```bash
   ssh ... ec2-user@EC2_IP
   java -cp ".:mysql-connector-java-9.0.0.jar" agrocomparador
   ```

3. **Configura auto-inicio** con systemd (ver DEPLOYMENT_AWS.md)

4. **Configura DNS/ALB** para acceso público

---

**¿Necesitas ayuda?**
- Ver `MIGRAR_A_AWS.md` para guía detallada
- Ver `COMANDOS_RAPIDOS_MIGRACION.md` para todos los comandos
- Ver `DEPLOYMENT_AWS.md` para deployment completo
