# ========================================
# SCRIPT PARA AUTOMATIZAR COMPILACIÓN Y EJECUCIÓN
# ========================================
# Uso: En PowerShell, ejecuta: .\run.ps1
# ========================================

# Colores para el output
$Green = [System.ConsoleColor]::Green
$Red = [System.ConsoleColor]::Red
$Yellow = [System.ConsoleColor]::Yellow

Write-Host "================================================" -ForegroundColor $Green
Write-Host "🚀 AGROCOMPARADOR - Compilar y Ejecutar" -ForegroundColor $Green
Write-Host "================================================" -ForegroundColor $Green
Write-Host ""

# Verificar si el driver está presente
$JarFiles = Get-ChildItem -Filter "mysql-connector-java-*.jar" -ErrorAction SilentlyContinue
if ($JarFiles.Count -eq 0) {
    Write-Host "❌ ERROR: No se encontró mysql-connector-java-*.jar" -ForegroundColor $Red
    Write-Host "   Por favor descarga el driver desde: https://dev.mysql.com/downloads/connector/j/" -ForegroundColor $Yellow
    Write-Host "   Y guárdalo en: $(Get-Location)" -ForegroundColor $Yellow
    exit 1
}

$DriverJar = $JarFiles[0].Name
Write-Host "✅ Driver encontrado: $DriverJar" -ForegroundColor $Green
Write-Host ""

# Obtener el puerto
Write-Host "¿Qué puerto deseas usar?" -ForegroundColor $Yellow
Write-Host "1 = Puerto 80 (requiere permisos de administrador)"
Write-Host "2 = Puerto 8080 (sin permisos especiales) - RECOMENDADO"
$Puerto = Read-Host "Selecciona (1 o 2, default 2)"

if ($Puerto -eq "1") {
    $PUERTO = 80
    Write-Host "Usando puerto 80..." -ForegroundColor $Green
} else {
    $PUERTO = 8080
    Write-Host "Usando puerto 8080..." -ForegroundColor $Green
    
    # Modificar WebServer.java para usar puerto 8080
    Write-Host "Configurando puerto 8080 en WebServer.java..." -ForegroundColor $Yellow
    $WebServerPath = "agrocomparador\ui\WebServer.java"
    if (Test-Path $WebServerPath) {
        $Content = Get-Content $WebServerPath -Raw
        $Content = $Content -replace 'private static final int PUERTO = 80;', 'private static final int PUERTO = 8080;'
        Set-Content $WebServerPath $Content
        Write-Host "✅ WebServer.java actualizado a puerto 8080" -ForegroundColor $Green
    }
}

Write-Host ""
Write-Host "Compilando..." -ForegroundColor $Yellow

# Compilar
javac -cp "$DriverJar" -d . `
    agrocomparador.java `
    "agrocomparador/data/*" `
    "agrocomparador/business/*" `
    "agrocomparador/ui/*" `
    2>&1

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Error en la compilación" -ForegroundColor $Red
    exit 1
}

Write-Host "✅ Compilación exitosa" -ForegroundColor $Green
Write-Host ""
Write-Host "================================================" -ForegroundColor $Green
Write-Host "🌍 Iniciando servidor..." -ForegroundColor $Green
Write-Host "================================================" -ForegroundColor $Green
Write-Host ""

# Ejecutar
java -cp ".;$DriverJar" agrocomparador
