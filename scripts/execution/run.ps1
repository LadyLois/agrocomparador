# ========================================
# SCRIPT PARA AUTOMATIZAR COMPILACIÓN Y EJECUCIÓN
# ========================================
# Uso: En PowerShell, ejecuta: .\run.ps1
# ========================================

# Cambiar al directorio raíz del proyecto (dos niveles arriba del script)
$ProjectRoot = (Resolve-Path "$PSScriptRoot\..\..")
Set-Location $ProjectRoot

# Colores para el output
$Green = [System.ConsoleColor]::Green
$Red = [System.ConsoleColor]::Red
$Yellow = [System.ConsoleColor]::Yellow

Write-Host "================================================" -ForegroundColor $Green
Write-Host "🚀 AGROCOMPARADOR - Compilar y Ejecutar" -ForegroundColor $Green
Write-Host "================================================" -ForegroundColor $Green
Write-Host ""

# Verificar si los JARs están presentes
$JarFiles = Get-ChildItem -Filter "mysql-connector-java-*.jar" -ErrorAction SilentlyContinue
if ($JarFiles.Count -eq 0) {
    Write-Host "❌ ERROR: No se encontró mysql-connector-java-*.jar" -ForegroundColor $Red
    Write-Host "   Por favor descarga el driver desde: https://dev.mysql.com/downloads/connector/j/" -ForegroundColor $Yellow
    exit 1
}

$JsoupFiles = Get-ChildItem -Filter "jsoup-*.jar" -ErrorAction SilentlyContinue
if ($JsoupFiles.Count -eq 0) {
    Write-Host "❌ ERROR: No se encontró jsoup-*.jar" -ForegroundColor $Red
    Write-Host "   Por favor descarga jsoup desde: https://jsoup.org/download" -ForegroundColor $Yellow
    exit 1
}

$DriverJar = $JarFiles[0].Name
$JsoupJar  = $JsoupFiles[0].Name
Write-Host "✅ Driver encontrado: $DriverJar" -ForegroundColor $Green
Write-Host "✅ Jsoup encontrado:  $JsoupJar"  -ForegroundColor $Green
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

# ── Variables de entorno de base de datos ──────────────────────────────────────
$EnvFile = ".env"
if (Test-Path $EnvFile) {
    Write-Host "📄 Cargando variables desde $EnvFile..." -ForegroundColor $Yellow
    Get-Content $EnvFile | Where-Object { $_ -match "^\s*[^#]" -and $_ -match "=" } | ForEach-Object {
        $parts = $_ -split "=", 2
        $key   = $parts[0].Trim()
        $value = $parts[1].Trim()
        [System.Environment]::SetEnvironmentVariable($key, $value, "Process")
    }
    Write-Host "✅ Variables cargadas desde .env" -ForegroundColor $Green
} else {
    Write-Host "⚙️  Configuración de base de datos (pulsa Enter para usar el valor por defecto):" -ForegroundColor $Yellow
    $dbHost = Read-Host "DB_HOST [localhost]"
    if ([string]::IsNullOrWhiteSpace($dbHost)) { $dbHost = "localhost" }

    $dbUser = Read-Host "DB_USER [root]"
    if ([string]::IsNullOrWhiteSpace($dbUser)) { $dbUser = "root" }

    $dbPass = Read-Host "DB_PASSWORD"

    $dbName = Read-Host "DB_NAME [agrocomparador]"
    if ([string]::IsNullOrWhiteSpace($dbName)) { $dbName = "agrocomparador" }

    $env:DB_HOST     = $dbHost
    $env:DB_USER     = $dbUser
    $env:DB_PASSWORD = $dbPass
    $env:DB_NAME     = $dbName

    $guardar = Read-Host "¿Guardar en .env para próximas ejecuciones? (s/N)"
    if ($guardar -eq "s" -or $guardar -eq "S") {
        @"
DB_HOST=$dbHost
DB_USER=$dbUser
DB_PASSWORD=$dbPass
DB_NAME=$dbName
DB_PORT=3306
PORT=$PUERTO
"@ | Set-Content $EnvFile -Encoding utf8
        Write-Host "✅ Variables guardadas en .env" -ForegroundColor $Green
    }
}
Write-Host ""

Write-Host "Compilando..." -ForegroundColor $Yellow

# Compilar
$sourceFiles = @("agrocomparador.java") + (Get-ChildItem `
    agrocomparador\data\*.java, `
    agrocomparador\business\*.java, `
    agrocomparador\ui\*.java, `
    agrocomparador\scraper\*.java | Select-Object -ExpandProperty FullName)
javac -cp "$JsoupJar;$DriverJar" $sourceFiles 2>&1

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
java -cp ".;$JsoupJar;$DriverJar" agrocomparador
