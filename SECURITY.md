# 🔒 Guía de Seguridad - AgroComparador

## Máximas Prioridades

### 1. ❌ NUNCA Hardcodear Credenciales
```java
// ❌ MALO - Nunca hagas esto
private static final String PASSWORD = "AgroComparador2026!";

// ✅ BUENO - Solo variables de entorno
private static final String PASSWORD = getRequiredEnv("DB_PASSWORD");
```

### 2. ❌ NUNCA Comitear .env con contraseñas
```bash
# .gitignore protege estos archivos
.env
.env.local
.env.production
```

### 3. ✅ Usar Variables de Entorno
```bash
export DB_PASSWORD=$(aws secretsmanager get-secret-value --secret-id agrocomparador/db-password --query SecretString --output text)
java -cp ".;mysql-connector-java-9.0.0.jar" agrocomparador
```

---

## Flujo Seguro por Entorno

### Desarrollo Local
```bash
# Linux/Mac - Bash
export DB_HOST=localhost
export DB_USER=admin
export DB_PASSWORD=mi_contraseña_local
java -cp ".;mysql-connector-java-9.0.0.jar" agrocomparador

# Windows - PowerShell
$env:DB_HOST="localhost"
$env:DB_USER="admin"
$env:DB_PASSWORD="mi_contraseña_local"
java -cp ".;mysql-connector-java-9.0.0.jar" agrocomparador

# Windows - CMD
set DB_HOST=localhost
set DB_USER=admin
set DB_PASSWORD=mi_contraseña_local
java -cp ".;mysql-connector-java-9.0.0.jar" agrocomparador
```

### Producción en AWS

**Opción A: AWS Secrets Manager (Recomendado)**
```bash
#!/bin/bash
# Script de inicio seguro en EC2/ECS

SECRET=$(aws secretsmanager get-secret-value --secret-id agrocomparador/db --query 'SecretString' --output text)
export DB_HOST=$(echo $SECRET | jq -r '.host')
export DB_USER=$(echo $SECRET | jq -r '.user')
export DB_PASSWORD=$(echo $SECRET | jq -r '.password')

java -cp ".;mysql-connector-java-9.0.0.jar" agrocomparador
```

**Opción B: AWS Systems Manager Parameter Store**
```bash
#!/bin/bash
export DB_HOST=$(aws ssm get-parameter --name /agrocomparador/db-host --query 'Parameter.Value' --output text)
export DB_USER=$(aws ssm get-parameter --name /agrocomparador/db-user --query 'Parameter.Value' --output text)
export DB_PASSWORD=$(aws ssm get-parameter --name /agrocomparador/db-password --query 'Parameter.Value' --output text --with-decryption)

java -cp ".;mysql-connector-java-9.0.0.jar" agrocomparador
```

---

## Checklist de Seguridad

- [ ] **DB_PASSWORD es requerido** - No hay valor por defecto en DatabaseConnection.java
- [ ] **.gitignore protege .env files** - Ver `.gitignore` en la raíz
- [ ] **No hay credenciales en código** - `git log --all -p | grep -i password` regresa vacío
- [ ] **No hay credenciales en logs** - Los logs no imprime DB_PASSWORD
- [ ] **Variables de entorno en producción** - Pasar en tiempo de ejecución
- [ ] **AWS Secrets Manager configurado** - (Producción)
- [ ] **Security Groups restrictivos** - RDS solo acepta desde EC2
- [ ] **Cifrado en tránsito** - SSL/TLS habilitado DB_HOST

---

## Auditoría de Seguridad

### Verificar que no hay contraseñas en el repo
```bash
# Buscar la palabra "password" en el código
git log --all -p | grep -i "password"

# Verificar que no hay valores en ejemplos
grep -r "AgroComparador2026!" . --exclude-dir=.git

# Ver qué archivos tienen credenciales en memoria
ps aux | grep java
```

### Limpiar historial si algo se comitó por accidente
```bash
# Advertencia: Esto reescribe el historio de git
git filter-branch --force --index-filter 'git rm --cached --ignore-unmatch .env' HEAD

# O usar git-secrets
brew install git-secrets
git secrets --install
git secrets --register-aws
```

---

## Variables Requeridas en Producción

| Variable | Requerida | Valor Por Defecto | Ejemplo |
|----------|-----------|-------------------|---------|
| DB_HOST | ✅ SÍ | ❌ NO | `agrocomparador.xxxxx.rds.amazonaws.com` |
| DB_USER | ✅ SÍ | ❌ NO | `admin` |
| DB_PASSWORD | ✅ SÍ | ❌ NO | `(desde Secrets Manager)` |
| DB_PORT | ❌ NO | `3306` | `3306` |
| DB_NAME | ❌ NO | `comparador` | `comparador` |
| PORT | ❌ NO | `8080` | `8080` |

---

## Mensajes de Error (Seguro)

Si falta DB_PASSWORD, obtendrás:
```
❌ VARIABLE DE ENTORNO REQUERIDA NO ENCONTRADA: DB_PASSWORD
   Las siguientes variables son obligatorias:
   - DB_HOST (ej: localhost o agrocomparador.xxxxx.rds.amazonaws.com)
   - DB_USER (ej: admin)
   - DB_PASSWORD (NUNCA dejar vacío o en el código)

   Ejemplo:
   export DB_HOST=localhost
   export DB_USER=admin
   export DB_PASSWORD=tu_password_segura
   java -cp ".;mysql-connector-java-9.0.0.jar" agrocomparador
```

Nota: **Los logs NUNCA imprimen la contraseña real**, solo que falta la variable.

---

## Referencias Útiles

- [AWS Secrets Manager Best Practices](https://docs.aws.amazon.com/secretsmanager/)
- [OWASP - Secrets Management](https://cheatsheetseries.owasp.org/cheatsheets/Secrets_Management_Cheat_Sheet.html)
- [12 Factor App - Config](https://12factor.net/config)

---

## Test de Credenciales

java TestCredentials
→ ❌ VARIABLE DE ENTORNO REQUERIDA NO ENCONTRADA: DB_HOST
