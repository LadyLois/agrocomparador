@echo off
REM Compilar agrocomparador con soporte de Web Scraping

setlocal enabledelayedexpansion

cd /d c:\Java\agrocomparador

echo.
echo ============================================
echo 🔨 Compilando agrocomparador con Scraper
echo ============================================
echo.

REM Verificar si los JARs existen
if not exist "lib\jsoup-1.15.3.jar" (\n    echo ❌ ERROR: jsoup-1.15.3.jar no encontrado\n    echo   Descargalo desde: https://jsoup.org/download\n    echo   Ubicacion requerida: lib\jsoup-1.15.3.jar\n    exit /b 1\n)\n\nif not exist "lib\mysql-connector-java-8.0.33.jar" (\n    echo ⚠️ ADVERTENCIA: mysql-connector-java no encontrado\n    echo   Puede que haya error de conexión a BD\n)\n\necho ✓ Configuración correcta\necho.\necho Compilando archivos Java...\necho.\n\nREM Compilar con classpath\njavac -cp "lib\jsoup-1.15.3.jar;lib\mysql-connector-java-8.0.33.jar" -d . ^^\n    agrocomparador.java ^^\n    agrocomparador\data\*.java ^^\n    agrocomparador\business\*.java ^^\n    agrocomparador\ui\*.java ^^\n    agrocomparador\scraper\*.java\n\nif %ERRORLEVEL% equ 0 (\n    echo.\n    echo ✓ Compilación exitosa!\n    echo.\n    echo Para ejecutar:\n    echo   java -cp "lib\jsoup-1.15.3.jar;lib\mysql-connector-java-8.0.33.jar;." agrocomparador\n    echo.\n    echo O usa run.bat para ejecutar directamente\n    echo.\n) else (\n    echo.\n    echo ❌ Error durante la compilación\n    echo.\n    exit /b 1\n)\n\nendlocal\n