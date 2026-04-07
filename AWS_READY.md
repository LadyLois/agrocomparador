# 🌾 Comparador de Precios Agrícolas - Guía AWS

## Cambios realizados para AWS Compatibility

### ✅ Configuración de Entorno
La aplicación ahora soporta **variables de entorno** para todos los parámetros críticos:

```bash
# Variables de Base de Datos
DB_HOST          # Host de MySQL (ej: localhost o RDS endpoint)
DB_PORT          # Puerto (defecto: 3306)
DB_NAME          # Nombre de BD (defecto: comparador)
DB_USER          # Usuario MySQL (defecto: admin)
DB_PASSWORD      # Contraseña MySQL (defecto: AgroComparador2026! - CAMBIAR EN PROD)

# Variables de Servidor
PORT             # Puerto del servidor web (defecto: 8080)
```

### 📝 Archivos de Configuración Nuevos

| Archivo | Uso |
|---------|-----|
| `.env.example` | Plantilla de configuración local |
| `.env.production.example` | Plantilla para AWS (NUNCA comitear) |
| `AWS_CONFIG.md` | Configuración detallada para AWS |
| `DEPLOYMENT_AWS.md` | Guía completa de deployment |
| `run_production.sh` | Script de inicio automático |

---

## 🚀 Opción A: Desarrollo Local (Sin cambios)

```bash
# Mantiene valores por defecto
java -cp ".;mysql-connector-java-9.0.0.jar" agrocomparador
```

---

## 🚀 Opción B: AWS RDS + EC2

### 1. Crear RDS MySQL
- Engine: MySQL 5.7+ o 8.0
- Instance Class: db.t3.micro (free tier)
- Copiar endpoint (ej: `agrocomparador-prod.xxxxx.us-east-1.rds.amazonaws.com`)

### 2. Inicializar BD en RDS
```bash
mysql -h [RDS_ENDPOINT] -u admin -p comparador < crear_base_datos.sql
java -cp ".;mysql-connector-java-9.0.0.jar" InsertarDatos
```

### 3. Configurar en EC2
```bash
# Crear .env.production
cat > .env.production << EOF
DB_HOST=agrocomparador-prod.xxxxx.us-east-1.rds.amazonaws.com
DB_PORT=3306
DB_NAME=comparador
DB_USER=admin
DB_PASSWORD=tu_password_segura
PORT=8080
EOF

# Ejecutar
bash run_production.sh
```

---

## 🔒 Seguridad en Producción

### ❌ NO hagas esto:
```bash
java -cp ".;mysql-connector-java-9.0.0.jar" agrocomparador  # Sin vars de entorno
```

### ✅ Hacer esto:
```bash
# Opción 1: Variables de entorno
export DB_PASSWORD=$(aws secretsmanager get-secret-value --secret-id agrocomparador/db-password --query SecretString --output text)
java -cp ".;mysql-connector-java-9.0.0.jar" agrocomparador

# Opción 2: AWS Systems Manager
export DB_PASSWORD=$(aws ssm get-parameter --name /agrocomparador/db-password --query 'Parameter.Value' --output text --with-decryption)
java -cp ".;mysql-connector-java-9.0.0.jar" agrocomparador
```

---

## 📊 Configuración en AWS Management Console

### EC2 - Variables de Entorno
```
Systems Manager > Parameter Store > Create Parameter
Name: /agrocomparador/db-host
Type: String
Value: [RDS_ENDPOINT]

Name: /agrocomparador/db-password
Type: SecureString
Value: [PASSWORD]
```

### Launch Configuration / Auto Scaling Group
```bash
#!/bin/bash
export DB_HOST=$(aws ssm get-parameter --name /agrocomparador/db-host --query 'Parameter.Value' --output text)
export DB_PASSWORD=$(aws ssm get-parameter --name /agrocomparador/db-password --query 'Parameter.Value' --output text --with-decryption)
export PORT=8080
cd /opt/agrocomparador
java -cp ".;mysql-connector-java-9.0.0.jar" agrocomparador >> /var/log/agrocomparador.log 2>&1 &
```

---

## 🐳 Docker (Opcional)

```dockerfile
FROM openjdk:11-jre-slim
WORKDIR /app
COPY mysql-connector-java-9.0.0.jar .
COPY agrocomparador/ ./agrocomparador/
COPY *.class .
EXPOSE 8080
CMD ["java", "-cp", ".:mysql-connector-java-9.0.0.jar", "agrocomparador"]
```

```bash
docker build -t agrocomparador:latest .
docker run -e DB_HOST=rds-endpoint -e DB_PASSWORD=xxxx -p 8080:8080 agrocomparador
```

---

## ⚙️ Cambios en el Código

### DatabaseConnection.java
- ✅ Ahora lee variables de entorno
- ✅ Tiene valores por defecto para desarrollo
- ✅ Muestra mensajes de error útiles si falla la conexión

### WebServer.java
- ✅ Puerto configurable desde variable `PORT`
- ✅ Muestra el puerto real en el mensaje de inicio

---

## 🧪 Testing

```bash
# Local (como antes)
java -cp ".;mysql-connector-java-9.0.0.jar" agrocomparador

# AWS (con variables)
export DB_HOST=agrocomparador-prod.xxxxx.aws.com
export DB_USER=admin
export DB_PASSWORD=MySecurePass123!
export PORT=8080
java -cp ".;mysql-connector-java-9.0.0.jar" agrocomparador
```

---

## 📋 Checklist para Producción

- [ ] RDS MySQL creada y configurada
- [ ] Security Groups permiten conexión EC2 → RDS (puerto 3306)
- [ ] Base de datos creada en RDS
- [ ] Variables de entorno en AWS Systems Manager o Secrets Manager
- [ ] EC2 tiene Java instalado
- [ ] Aplicación compilada en EC2
- [ ] Script run_production.sh ejecutable
- [ ] Load Balancer/ALB configurado (opcional)
- [ ] CloudWatch Logs configurado
- [ ] Backups automáticos de RDS habilitados

---

## 📞 Soporte

Para más detalles, ver:
- [DEPLOYMENT_AWS.md](DEPLOYMENT_AWS.md) - Guía técnica completa
- [AWS_CONFIG.md](AWS_CONFIG.md) - Configuración específica
