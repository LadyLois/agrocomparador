@echo off
REM ========================================
REM SCRIPT PARA EJECUTAR AGROCOMPARADOR
REM ========================================
REM Uso: Desde CMD, ejecuta: run.bat
REM ========================================

echo.
echo ================================================
echo. AgroComparador - Compilar y Ejecutar
echo ================================================
echo.

REM Buscar el driver JDBC
setlocal enabledelayedexpansion
set DRIVER_JAR=
for %%f in (mysql-connector-java-*.jar) do (
    if defined DRIVER_JAR (
        echo [ADVERTENCIA] Hay m's de un driver JAR encontrado
    )
    set "DRIVER_JAR=%%f"
)

if not defined DRIVER_JAR (
    echo [ERROR] No se encuentra mysql-connector-java-*.jar
    echo Descárgalo desde: https://dev.mysql.com/downloads/connector/j/
    echo Guárdalo en la carpeta del proyecto
    pause
    exit /b 1
)

echo [OK] Driver encontrado: !DRIVER_JAR!
echo.

REM Seleccionar puerto
echo ¿Qué puerto deseas usar?
echo 1 = Puerto 80 (requiere administrador)
echo 2 = Puerto 8080 (recomendado)
set /p PORT="Selecciona [1 o 2, default 2]: "

if "%PORT%"=="1" (
    echo Usando puerto 80...
) else (
    echo Usando puerto 8080...
    echo Configurando WebServer.java...
    REM Reemplazar puerto en WebServer.java (si es necesario)
)

echo.
echo Compilando...
echo.

REM Compilar
javac -cp "!DRIVER_JAR!" -d . ^
    agrocomparador.java ^
    agrocomparador\data\*.java ^
    agrocomparador\business\*.java ^
    agrocomparador\ui\*.java

if errorlevel 1 (
    echo [ERROR] Error en la compilacion
    pause
    exit /b 1
)

echo [OK] Compilacion exitosa
echo.
echo ================================================
echo. Iniciando servidor...
echo ================================================
echo.

REM Ejecutar con el driver en el classpath
java -cp ".;!DRIVER_JAR!" agrocomparador

pause
