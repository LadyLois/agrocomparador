# 🚀 Guía de Deployment en AWS

## Opción 1: EC2 + RDS (Recomendado)

### 1. Crear RDS MySQL
```bash
aws rds create-db-instance \
  --db-instance-identifier agrocomparador-prod \
  --db-instance-class db.t3.micro \
  --engine mysql \
  --master-username admin \
  --master-user-password [CONTRASEÑA_SEGURA] \
  --allocated-storage 20 \
  --publicly-accessible false
```

### 2. Crear EC2 Instance
- AMI: Amazon Linux 2 o Ubuntu 20.04
- Instancia mínima: t3.micro (eligible para free tier)
- Security Group: Allow inbound 8080 (SSH 22, HTTP 80)

### 3. Instalar Java en EC2
```bash
sudo yum install java-11-openjdk java-11-openjdk-devel
# o en Ubuntu:
sudo apt-get install openjdk-11-jdk
```

### 4. Configurar Variables de Entorno
```bash
# En EC2, crear archivo /etc/environment u .bashrc
export DB_HOST=agrocomparador-prod.xxxxx.us-east-1.rds.amazonaws.com
export DB_PORT=3306
export DB_NAME=comparador
export DB_USER=admin
export DB_PASSWORD=tu_contraseña_segura
export PORT=8080
```

### 5. Crear base de datos en RDS
```bash
mysql -h [RDS_ENDPOINT] -u admin -p < crear_base_datos.sql
java -cp ".;mysql-connector-java-9.0.0.jar" InsertarDatos
```

### 6. Ejecutar aplicación
```bash
java -cp ".;mysql-connector-java-9.0.0.jar" agrocomparador
```

---

## Opción 2: Docker + ECS (Más escalable)

### Crear Dockerfile
```dockerfile
FROM openjdk:11-jre-slim
WORKDIR /app
COPY mysql-connector-java-9.0.0.jar .
COPY agrocomparador/ ./agrocomparador/
COPY *.class .
EXPOSE 8080
CMD ["java", "-cp", ".:mysql-connector-java-9.0.0.jar", "agrocomparador"]
```

### Buildear imagen
```bash
docker build -t agrocomparador:latest .
docker tag agrocomparador:latest [ACCOUNT_ID].dkr.ecr.[REGION].amazonaws.com/agrocomparador:latest
docker push [ACCOUNT_ID].dkr.ecr.[REGION].amazonaws.com/agrocomparador:latest
```

---

## Opción 3: Usar AWS Elastic Beanstalk (Más simple)

```bash
eb init -p "Java Platform" agrocomparador
eb create agrocomparador-env \
  --database.engine mysql \
  --database.version 5.7 \
  --scale 1
eb setenv DB_HOST=[RDS_ENDPOINT] DB_USER=admin DB_PASSWORD=[PASS] PORT=8080
eb deploy
```

---

## Configuración de Seguridad

### 1. Usar AWS Secrets Manager
```bash
aws secretsmanager create-secret \
  --name agrocomparador/db-password \
  --secret-string "tu_contraseña_segura"
```

### 2. En aplicación Java (modificar DatabaseConnection.java)
```java
import software.amazon.awssdk.secretsmanager.*;
// Recuperar secreto en lugar de usar variable de entorno
```

### 3. Security Groups
- RDS: Solo permite conexiones de EC2 (puerto 3306)
- EC2: Permite SSH (22) solo de tu IP y puerto 8080 público

---

## Load Balancer (Producción)

Si esperas mucho tráfico:
```bash
# Crear ALB
aws elbv2 create-load-balancer \
  --name agrocomparador-alb \
  --type application
  
# Target Group
aws elbv2 create-target-group \
  --name agrocomparador-targets \
  --protocol HTTP \
  --port 8080
```

Luego redirigir tráfico de puerto 80/443 al 8080 de EC2.

---

## Monitoreo

```bash
# CloudWatch para logs
tail -f /var/log/agrocomparador.log

# Ver métricas de EC2
aws cloudwatch get-metric-statistics \
  --namespace AWS/EC2 \
  --metric-name CPUUtilization \
  --dimensions Name=InstanceId,Value=[INSTANCE_ID] \
  --start-time 2026-04-07T00:00:00Z \
  --end-time 2026-04-08T00:00:00Z \
  --period 300 \
  --statistics Average
```

---

## Troubleshooting

### Error: "Access denied for user 'admin'"
- Verificar que DB_HOST, DB_USER, DB_PASSWORD son correctos
- Verificar que RDS permite conexiones entrantes (Security Group)
- Verificar que la base de datos "comparador" existe

### Error: "Cannot find MySQL driver"
- Asegurar que mysql-connector-java-9.0.0.jar está en el path
- O incluirlo en un JAR fat jar

### Aplicación no responde
- Verificar puerto: `netstat -tlnp` o `lsof -i :8080`
- Verificar logs: `tail -f nohup.out`
- Aumentar memory: `java -Xmx512m -cp ...`
