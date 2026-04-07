@echo off
REM Script para configurar variables de entorno (Windows CMD)
REM Uso: setup-env.bat [DB_HOST] [DB_USER]
REM Ejemplo: setup-env.bat localhost admin

setlocal enabledelayedexpansion

echo.
echo ========================================
echo   AgroComparador - Configuracion
echo   Windows CMD/Batch
echo ========================================
echo.

REM Valores por defecto
set "DB_HOST=localhost"
set "DB_USER=admin"
set "DB_PORT=3306"
set "DB_NAME=comparador"
set "PORT=8080"

REM Si se proporcionan argumentos, usarlos
if not "%1"=="" set "DB_HOST=%1"
if not "%2"=="" set "DB_USER=%2"

REM Pedir contraseña
echo.
echo Necesito la contraseña de la base de datos
set /p DB_PASSWORD="Ingresa la contraseña: "

echo.
echo Estableciendo variables de entorno...
set DB_HOST=%DB_HOST%
set DB_USER=%DB_USER%
set DB_PASSWORD=%DB_PASSWORD%
set DB_PORT=%DB_PORT%
set DB_NAME=%DB_NAME%
set PORT=%PORT%

echo.
echo ✓ Variables configuradas:
echo   DB_HOST = %DB_HOST%
echo   DB_USER = %DB_USER%
echo   DB_PASSWORD = (oculto por seguridad)
echo   DB_PORT = %DB_PORT%
echo   DB_NAME = %DB_NAME%
echo   PORT = %PORT%
echo.

echo Iniciando AgroComparador...
echo.

java -cp ".;mysql-connector-java-9.0.0.jar" agrocomparador

endlocal
