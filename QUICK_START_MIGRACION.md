# ⚡ QUICK START: Migración en 5 Minutos

> Si solo tienes 5 minutos, este es tu documento

---

## 🚀 LA FORMA MÁS RÁPIDA

**En tu PC (PowerShell):**

```powershell
# Reemplaza estos valores:
$EC2_IP = "ec2-XX-XX-XX-XX.compute-1.amazonaws.com"
$PEM_FILE = "C:\path\a\tu-key.pem"

# Paso 1: Exportar
mysqldump -h localhost -u admin -p comparador > backup.sql

# Paso 2: Transferir
scp -i $PEM_FILE backup.sql ec2-user@${EC2_IP}:/tmp/

# Paso 3: Conectar y importar
ssh -i $PEM_FILE ec2-user@${EC2_IP}

# ↓ YA ESTÁS EN EC2, ejecuta esto: ↓

mysql -h localhost -u admin -p -e "CREATE DATABASE comparador CHARACTER SET utf8mb4;"
mysql -h localhost -u admin -p comparador < /tmp/backup.sql

# Verificar
mysql -h localhost -u admin -p comparador -e "SELECT COUNT(*) FROM productos;"

# Listo ✅
```

---

## ✅ Eso es. La BD está migrada.

**Para ejecutar tu aplicación en EC2:**

```bash
export DB_HOST=localhost
export DB_USER=admin
export DB_PASSWORD=tu_password
export DB_NAME=comparador

java -cp ".:mysql-connector-java-9.0.0.jar" agrocomparador
```

---

## 🤔 ¿Problemas?

- **"Command not found"** → Instala: `sudo yum install mysql` en EC2
- **"Access denied"** → Verifica usuario/contraseña
- **"Connection refused"** → Iniciar MySQL: `sudo systemctl start mysql`

---

## 📚 Si necesitas más detalles:

- Guía completa: [MIGRACION_PASO_A_PASO.md](MIGRACION_PASO_A_PASO.md)
- Todos los comandos: [COMANDOS_RAPIDOS_MIGRACION.md](COMANDOS_RAPIDOS_MIGRACION.md)
- Documentación extensa: [MIGRAR_A_AWS.md](MIGRAR_A_AWS.md)

---

**¡Listo!** Tu BD está en AWS. 🎉
