# ========================================
# MIGRACIÓN RÁPIDA: 3 PASOS AUTOMÁTICOS
# Ejecutar en Windows PowerShell como Administrador
# ========================================

param(
    [string]$EC2_IP = $null,
    [string]$PEM_FILE = $null,
    [string]$EC2_USER = "ec2-user"
)

$ErrorActionPreference = "Stop"

# Colores
$info = @{ ForegroundColor = 'Cyan'; }
$success = @{ ForegroundColor = 'Green'; }
$warn = @{ ForegroundColor = 'Yellow'; }
$error = @{ ForegroundColor = 'Red'; }

Write-Host ""
Write-Host "╔════════════════════════════════════════════╗" @success
Write-Host "║  MIGRACION BD: Local -> AWS EC2            ║" @success
Write-Host "║  (Todos los pasos automaticos)             ║" @success
Write-Host "╚════════════════════════════════════════════╝" @success
Write-Host ""

# ===== INPUT DEL USUARIO =====
if (-not $EC2_IP -or $EC2_IP -like "*XX*") {
    Write-Host "Configuracion requerida:" @info
    Write-Host ""
    $EC2_IP = Read-Host "Ingresa tu EC2 DNS (ej: ec2-54-123-45-67.compute-1.amazonaws.com)"
}

if (-not $PEM_FILE -or $PEM_FILE -like "*tu-key*") {
    $PEM_FILE = Read-Host "Ingresa la ruta completa a tu archivo .pem (ej: C:\Downloads\key.pem)"
}

# Validar
if ($EC2_IP -like "*XX*" -or -not $EC2_IP) {
    Write-Host "ERROR: IP de EC2 invalida" @error
    exit 1
}

if (-not (Test-Path $PEM_FILE)) {
    Write-Host "ERROR: Archivo PEM no encontrado: $PEM_FILE" @error
    exit 1
}

Write-Host ""
Write-Host "OK - Configuracion validada:" @success
Write-Host "  EC2:  $EC2_IP" -ForegroundColor Gray
Write-Host "  User: $EC2_USER" -ForegroundColor Gray
Write-Host "  Key:  $PEM_FILE" -ForegroundColor Gray
Write-Host ""

# ===== PASO 1: EXPORTAR =====
Write-Host "═══════════════════════════════════════════" @success
Write-Host "PASO 1 : EXPORTAR BASE DE DATOS LOCAL" @success
Write-Host "═══════════════════════════════════════════" @success
Write-Host ""

$BACKUP_DIR = "c:\Java\agrocomparador\backups"
$TIMESTAMP = Get-Date -Format "yyyy-MM-dd_HHmmss"
$BACKUP_FILE = "$BACKUP_DIR\backup_comparador_$TIMESTAMP.sql"

if (!(Test-Path $BACKUP_DIR)) {
    New-Item -ItemType Directory -Path $BACKUP_DIR | Out-Null
}

Write-Host "Exportando BD 'comparador' desde localhost..." @info
Write-Host "Debes ingresar la contrasena MySQL cuando se pida" @warn

try {
    mysqldump -h localhost -u admin -p `
        --set-charset `
        --default-character-set=utf8mb4 `
        --single-transaction `
        --lock-tables=false `
        comparador > $BACKUP_FILE 2>&1
    
    if ($LASTEXITCODE -ne 0) {
        throw "Error en mysqldump"
    }
    
    $fileSizeMB = [math]::Round((Get-Item $BACKUP_FILE).Length / 1MB, 2)
    Write-Host "OK - BD Exportada: $BACKUP_FILE [$fileSizeMB MB]" -ForegroundColor Green
    
} catch {
    Write-Host "Error al exportar: $_" -ForegroundColor Red
    exit 1
}

Write-Host ""

# ===== PASO 2: TRANSFERIR =====
Write-Host "═══════════════════════════════════════════" @success
Write-Host "PASO 2 : TRANSFERIR A EC2" @success
Write-Host "═══════════════════════════════════════════" @success
Write-Host ""

$BACKUP_NAME = (Get-Item $BACKUP_FILE).Name

Write-Host "Transfiriendo $BACKUP_NAME a EC2..." @info
Write-Host "Esto puede tomar unos minutos..." @warn

try {
    & scp -i $PEM_FILE `
        -o StrictHostKeyChecking=no `
        -o UserKnownHostsFile=NUL `
        -q `
        $BACKUP_FILE "${EC2_USER}@${EC2_IP}:/tmp/"
    
    if ($LASTEXITCODE -ne 0) {
        throw "Error en SCP"
    }
    
    Write-Host "OK - Archivo transferido a EC2:/tmp/$BACKUP_NAME" @success
    
} catch {
    Write-Host "ADVERTENCIA: Error en transferencia" @warn
    Write-Host "Verifica:" @warn
    Write-Host "  - EC2 IP es correcta" -ForegroundColor Yellow
    Write-Host "  - Archivo .pem tiene permisos correctos" -ForegroundColor Yellow
    Write-Host "  - Security Group permite SSH (puerto 22)" -ForegroundColor Yellow
    exit 1
}

Write-Host ""

# ===== PASO 3: IMPORTAR EN EC2 =====
Write-Host "═══════════════════════════════════════════" @success
Write-Host "PASO 3 : IMPORTAR EN EC2" @success
Write-Host "═══════════════════════════════════════════" @success
Write-Host ""

# Crear script de importacion
$IMPORT_SCRIPT_TEMPLATE = Get-Content -Path "$PSScriptRoot\import_template.sh" -Raw
$IMPORT_SCRIPT = $IMPORT_SCRIPT_TEMPLATE -replace 'BACKUP_FILENAME_PLACEHOLDER', $BACKUP_NAME

# Guardar temporalmente
$TEMP_SCRIPT = "$BACKUP_DIR\import_temp.sh"
Set-Content -Path $TEMP_SCRIPT -Value $IMPORT_SCRIPT

Write-Host "Conectando a EC2 para importar datos..." @info
Write-Host "Debes ingresar la contrasena MySQL en EC2" @warn
Write-Host ""

try {
    # Transferir script de importacion
    & scp -i $PEM_FILE `
        -o StrictHostKeyChecking=no `
        $TEMP_SCRIPT "${EC2_USER}@${EC2_IP}:/tmp/import.sh" 2>$null
    
    # Ejecutar en EC2
    & ssh -i $PEM_FILE `
        -o StrictHostKeyChecking=no `
        "${EC2_USER}@${EC2_IP}" `
        "chmod +x /tmp/import.sh; /tmp/import.sh"
    
    Write-Host ""
    Write-Host "OK - BD Importada en EC2" @success
    
} catch {
    Write-Host "ADVERTENCIA: Error en importacion remota" @warn
    Write-Host "Conectate manualmente a EC2 y ejecuta:" @info
    Write-Host "   ssh -i $PEM_FILE ${EC2_USER}@${EC2_IP}" -ForegroundColor Gray
    Write-Host "   mysql -h localhost -u admin -p comparador REDIRECTION_OPERATOR /tmp/$BACKUP_NAME" -ForegroundColor Gray
}

# Limpiar
if (Test-Path $TEMP_SCRIPT) {
    Remove-Item $TEMP_SCRIPT
}

Write-Host ""

# ===== RESUMEN =====
Write-Host "═══════════════════════════════════════════" @success
Write-Host "MIGRACION COMPLETADA" @success
Write-Host "═══════════════════════════════════════════" @success

Write-Host ""
Write-Host "Proximos pasos:" -ForegroundColor Cyan
Write-Host ""
Write-Host "1 - Transferir aplicacion Java a EC2:" -ForegroundColor White
Write-Host "    scp -i PEMFILE agrocomparador*.class ECUSER@ECIP:~/" -ForegroundColor Gray
Write-Host ""
Write-Host "2 - Configurar variables de entorno en EC2:" -ForegroundColor White
Write-Host "    ssh -i PEMFILE ECUSER@ECIP" -ForegroundColor Gray
Write-Host "    export DB_HOST=localhost" -ForegroundColor Gray
Write-Host "    export DB_USER=admin" -ForegroundColor Gray
Write-Host "    export DB_PASSWORD=tu_password" -ForegroundColor Gray
Write-Host "    export DB_NAME=comparador" -ForegroundColor Gray
Write-Host ""
Write-Host "3 - Ejecutar la aplicacion:" -ForegroundColor White
Write-Host "    java -cp '.:mysql-connector-java-9.0.0.jar' agrocomparador" -ForegroundColor Gray
Write-Host ""
Write-Host "Documentacion completa - ver MIGRAR_A_AWS.md" -ForegroundColor Cyan
Write-Host ""
