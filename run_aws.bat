@echo off
REM Script para ejecutar AgroComparador con variables de entorno (Windows)
REM Uso: run_aws.bat

echo.
echo ========================================
echo   Comparador de Precios Agricolas
echo   AWS Configuration Helper
echo ========================================
echo.

REM Valores por defecto (desarrollo local)
setlocal enabledelayedexpansion

set "DB_HOST=localhost"
set "DB_PORT=3306"
set "DB_NAME=comparador"
set "DB_USER=admin"
set "DB_PASSWORD="
set "PORT=8080"

REM Pedir contraseña si no está configurada
if "!DB_PASSWORD!"=="" (
    set /p DB_PASSWORD="Ingresa la contraseña de la base de datos: "
)
if exist .env.local (
    echo Cargando configuracion de .env.local...
    for /f "tokens=1,2 delims==" %%A in (.env.local) do (
        if not "%%A"=="" (
            set "%%A=%%B"
        )
    )
)

echo.
echo Configuracion actual:
echo   Base de datos: !DB_HOST!:!DB_PORT!/!DB_NAME!
echo   Usuario: !DB_USER!
echo   Puerto servidor: !PORT!
echo.

echo Iniciando AgroComparador...
echo.

setlocal
set DB_HOST=!DB_HOST!
set DB_PORT=!DB_PORT!
set DB_NAME=!DB_NAME!
set DB_USER=!DB_USER!
set DB_PASSWORD=!DB_PASSWORD!
set PORT=!PORT!

java -cp ".;mysql-connector-java-9.0.0.jar" agrocomparador

endlocal
