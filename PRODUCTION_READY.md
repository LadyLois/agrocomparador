# ✅ Checklist Pre-Producción y Git

## 🔐 Seguridad (100% ✅)

- ✅ **No hay contraseñas hardcodeadas en código**
  - DatabaseConnection.java requiere variables de entorno
  - Sin valores por defecto sensibles
  
- ✅ **.gitignore protege archivos sensibles**
  - .env, .env.local, .env.production ignorados
  - *.log, *.class ignorados
  
- ✅ **Scripts seguros**
  - setup-env.bat y setup-env.ps1 piden contraseña en tiempo de ejecución
  - run_aws.bat pide contraseña (no hardcodeada)
  
- ✅ **Documentación clara**
  - SECURITY.md - Guía de seguridad
  - AWS_READY.md - Configuración AWS
  - QUICK_START.md - Instrucciones para todos los SO

---

## 🗑️ Limpieza (100% ✅)

- ✅ Archivos .class eliminados
- ✅ Archivos de test eliminados (TestCredentials.java, TestUTF8.java, InsertarDatos.java)
- ✅ Logs temporales eliminados (output.log)
- ✅ Solo fuentes .java incluidos

---

## 📝 Archivos para Git (SAFE)

Estos archivos son SEGUROS para comitear:

```
✅ SIEMPRE comitear:
   - agrocomparador/*.java (código fuente)
   - *.md (documentación)
   - .gitignore (para proteger secretos)
   - .env.example (solo ejemplo, sin valores reales)
   - .env.production.example (solo ejemplo)
   - setup-env.bat, setup-env.ps1 (scripts públicos)
   - run_production.sh (script público)
   - mysql-connector-java-9.0.0.jar (si es usado como submodule o dependencia)
   - crear_base_datos.sql (datos públicos)

❌ NUNCA comitear:
   - .env
   - .env.local
   - .env.production (archivos CON valores reales)
   - *.class (archivos compilados)
   - *.log (logs)
   - Archivos de IDE (.vscode/settings.json con paths personales)
```

---

## 🚀 Para Producción (AWS)

### 1. Clonar el repositorio
```bash
git clone https://github.com/tu-user/agrocomparador.git
cd agrocomparador
```

### 2. Configurar variables en AWS
```bash
# Opción A: AWS Secrets Manager
aws secretsmanager create-secret --name agrocomparador/db \
  --secret-string '{"host":"rds-endpoint.aws.com","user":"admin","password":"TU_PASSWORD_SEGURA"}'

# Opción B: AWS Systems Manager Parameter Store
aws ssm put-parameter --name /agrocomparador/db-host --value "rds-endpoint.aws.com"
aws ssm put-parameter --name /agrocomparador/db-user --value "admin"
aws ssm put-parameter --name /agrocomparador/db-password --value "TU_PASSWORD_SEGURA" --type SecureString
```

### 3. En EC2, crear script de inicio
```bash
#!/bin/bash
# /opt/agrocomparador/start.sh

export DB_HOST=$(aws ssm get-parameter --name /agrocomparador/db-host --query 'Parameter.Value' --output text)
export DB_USER=$(aws ssm get-parameter --name /agrocomparador/db-user --query 'Parameter.Value' --output text)
export DB_PASSWORD=$(aws ssm get-parameter --name /agrocomparador/db-password --query 'Parameter.Value' --output text --with-decryption)
export PORT=8080

cd /opt/agrocomparador
java -cp ".:mysql-connector-java-9.0.0.jar" agrocomparador >> /var/log/agrocomparador.log 2>&1 &
```

### 4. Compilar
```bash
javac -encoding UTF-8 -cp ".:mysql-connector-java-9.0.0.jar" agrocomparador/**/*.java agrocomparador.java
```

### 5. Ejecutar
```bash
bash /opt/agrocomparador/start.sh
```

---

## 📋 Resumen Final

| Aspecto | Estado | Notas |
|---------|--------|-------|
| **Código sin secretos** | ✅ SEGURO | Requiere variables de entorno |
| **Git protegido** | ✅ SEGURO | .gitignore configurado |
| **Scripts seguros** | ✅ SEGURO | Piden contraseña en runtime |
| **Documentación** | ✅ COMPLETA | SECURITY.md, AWS_READY.md, QUICK_START.md |
| **Compilado** | ✅ LIMPIO | Sin archivos temporales |
| **Credenciales** | ✅ SEGURO | Solo variables de entorno |

---

## 🎯 Próximos Pasos

1. **Para Git:**
   ```bash
   git init
   git add .
   git commit -m "Initial commit: AgroComparador app"
   git push origin main
   ```

2. **Para AWS:**
   - Crear RDS MySQL
   - Crear EC2 instance
   - Configurar Security Groups
   - Configurar AWS Secrets Manager
   - Ejecutar inicialmente con setup

---

## ⚠️ IMPORTANTE

**NUNCA hacer esto:**
```bash
# ❌ MAL - Expone la contraseña en el historial
java -cp ".:mysql-connector-java-9.0.0.jar" agrocomparador &
export DB_PASSWORD=MyPassword123!

# ✅ BIEN - Pedir interactivamente
./setup-env.sh  # Pide la contraseña
```

---

## 📞 Auditoría Final

Para asegurar que no hay secretos antes de comitear:
```bash
# Buscar credenciales
git status
git diff

# Revisar archivos importantes
cat .gitignore | grep -E "\.env|secret|key|password"
```

**¡Ya está listo para producción! 🚀**
