@echo off
cd /d "%~dp0..\..\"
setlocal enabledelayedexpansion
set "DB_HOST=localhost"
set "DB_PORT=3306"
set "DB_NAME=agrocomparador"
set "DB_USER=admin"
set "PORT=8080"
if "!DB_PASSWORD!"=="" (
    set /p DB_PASSWORD="Contraseña: "
)
echo Iniciando AgroComparador...
java -cp ".;mysql-connector-java-9.0.0.jar" agrocomparador
