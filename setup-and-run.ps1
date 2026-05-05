# Script para configurar y ejecutar agrocomparador con Web Scraping
# Ejecuta este script y todo se configura automáticamente

param(
    [string]$mysqlPassword = "",
    [switch]$skipCompile = $false
)

Clear-Host
Write-Host "==============================================================`n" -ForegroundColor Cyan
Write-Host "  Setup Agrocomparador con Web Scraping`n" -ForegroundColor Cyan

# 1. Agregar MySQL al PATH
Write-Host "1 - Configurando PATH para MySQL..." -ForegroundColor Yellow
$env:Path += ";C:\Program Files\MySQL\MySQL Server 8.4\bin"
Write-Host "   OK - MySQL agregado al PATH`n" -ForegroundColor Green

# 2. Crear tabla en BD
Write-Host "2 - Creando tabla en base de datos..." -ForegroundColor Yellow
$sqlFile = "C:\Java\agrocomparador\backups\crear_tabla_scraper.sql"

if (-Not (Test-Path $sqlFile)) {
    Write-Host "   ERROR - Archivo SQL no encontrado: $sqlFile" -ForegroundColor Red
    exit 1
}

$sqlContent = Get-Content -Path $sqlFile -Raw

try {
    if ($mysqlPassword) {
        $sqlContent | mysql -u root "-p$mysqlPassword" agrocomparador 2>&1 | Out-Null
    } else {
        $sqlContent | mysql -u root agrocomparador 2>&1 | Out-Null
    }
    
    Write-Host "   OK - Tabla creada exitosamente`n" -ForegroundColor Green
}
catch {
    Write-Host "   ERROR - No se pudo crear la tabla" -ForegroundColor Red
    Write-Host "   Verifica la contraseña de MySQL o intenta manualmente" -ForegroundColor Yellow
    Write-Host "   Presiona Enter para continuar..." -ForegroundColor Yellow
    Read-Host
}

# 3. Compilar
if (-not $skipCompile) {
    Write-Host "3 - Compilando aplicacion..." -ForegroundColor Yellow
    cd C:\Java\agrocomparador
    
    $libs = "lib\jsoup-1.15.3.jar;lib\mysql-connector-java-8.0.33.jar"
    
    javac -cp $libs -d . `
        agrocomparador.java `
        agrocomparador\data\*.java `
        agrocomparador\business\*.java `
        agrocomparador\ui\*.java `
        agrocomparador\scraper\*.java 2>&1 | Out-Null
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "   OK - Compilacion exitosa`n" -ForegroundColor Green
    } else {
        Write-Host "   ERROR - Compilacion fallo" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "3 - Compilacion omitida`n" -ForegroundColor Yellow
}

# 4. Ejecutar
Write-Host "4 - Iniciando aplicacion...`n" -ForegroundColor Yellow
Write-Host "======================================================" -ForegroundColor Green
Write-Host "  OK - APLICACION LISTA" -ForegroundColor Green
Write-Host "======================================================`n" -ForegroundColor Green
Write-Host "Accede a: http://localhost/`n" -ForegroundColor Cyan
Write-Host "Presiona Ctrl+C para detener la aplicacion`n" -ForegroundColor Yellow

$libs = "lib\jsoup-1.15.3.jar;lib\mysql-connector-java-8.0.33.jar"
java -cp "$libs;." agrocomparador
