# 🚀 Guía Rápida de Inicio

## Windows PowerShell

### Opción A: Script automático (Recomendado)
```powershell
.\setup-env.ps1
# Te pedirá la contraseña de forma segura
```

### Opción B: Variables manuales + Ejecutar
```powershell
# Establecer variables
$env:DB_HOST = "localhost"
$env:DB_USER = "admin"
$env:DB_PASSWORD = "AgroComparador2026!"
$env:PORT = 8080

# Ejecutar
java -cp ".;mysql-connector-java-9.0.0.jar" agrocomparador
```

---

## Windows CMD

### Opción A: Script automático (Recomendado)
```cmd
setup-env.bat
REM Te pedirá la contraseña
```

### Opción B: Variables manuales + Ejecutar
```cmd
set DB_HOST=localhost
set DB_USER=admin
set DB_PASSWORD=AgroComparador2026!
set PORT=8080
java -cp ".;mysql-connector-java-9.0.0.jar" agrocomparador
```

---

## Linux / Mac (Bash)

### Opción A: Script automático
```bash
chmod +x run_production.sh
./run_production.sh
```

### Opción B: Variables manuales
```bash
export DB_HOST=localhost
export DB_USER=admin
export DB_PASSWORD=AgroComparador2026!
export PORT=8080
java -cp ".:mysql-connector-java-9.0.0.jar" agrocomparador
```

---

## AWS RDS Producción

```powershell
# PowerShell
$secret = aws secretsmanager get-secret-value --secret-id agrocomparador/db --query 'SecretString' --output text | ConvertFrom-Json
$env:DB_HOST = $secret.host
$env:DB_USER = $secret.user
$env:DB_PASSWORD = $secret.password
java -cp ".;mysql-connector-java-9.0.0.jar" agrocomparador
```

```bash
# Bash
SECRET=$(aws secretsmanager get-secret-value --secret-id agrocomparador/db --query 'SecretString' --output text)
export DB_HOST=$(echo $SECRET | jq -r '.host')
export DB_USER=$(echo $SECRET | jq -r '.user')
export DB_PASSWORD=$(echo $SECRET | jq -r '.password')
java -cp ".:mysql-connector-java-9.0.0.jar" agrocomparador
```

---

## ✅ Verificación

Una vez iniciada la aplicación, deberías ver:
```
🚀 Servidor iniciado en puerto 8080
📍 Accede a: http://localhost:8080/
```

Abre tu navegador en http://localhost:8080/ y ¡listo!

---

## Troubleshooting

### Error: "VARIABLE DE ENTORNO REQUERIDA"
Significa que falta alguna variable. Asegúrate de:
- ✅ DB_HOST está configurado
- ✅ DB_USER está configurado
- ✅ DB_PASSWORD está configurado

### Error: "Access denied for user"
- Verifica que la contraseña es correcta
- Verifica que el usuario existe en MySQL

### Error: "Cannot connect to host"
- Verifica que MySQL está corriendo
- Verifica que DB_HOST es correcto

---

## Archivos auxiliares

| Archivo | Uso |
|---------|-----|
| `setup-env.ps1` | Script PowerShell para Windows |
| `setup-env.bat` | Script CMD para Windows |
| `run_production.sh` | Script Bash para Linux/Mac |
| `SECURITY.md` | Guía completa de seguridad |
| `DEPLOYMENT_AWS.md` | Guía de deployment en AWS |
