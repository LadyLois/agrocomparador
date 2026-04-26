# ========================================
# SCRIPT 1: EXPORTAR BASE DE DATOS LOCAL
# Ejecutar en: tu PC Windows (PowerShell)
# ========================================

$ErrorActionPreference = "Stop"

Write-Host "🚀 Iniciando exporta de base de datos..." -ForegroundColor Green

# Variables
$BACKUP_DIR = "c:\Java\agrocomparador\backups"
$TIMESTAMP = Get-Date -Format "yyyy-MM-dd_HHmmss"
$BACKUP_FILE = "$BACKUP_DIR\backup_comparador_$TIMESTAMP.sql"
$BACKUP_COMPRESSED = "$BACKUP_FILE.gz"
$DB_HOST = "localhost"
$DB_USER = "admin"
$DB_NAME = "agrocomparador"

# Crear carpeta de backups si no existe
if (!(Test-Path $BACKUP_DIR)) {
    New-Item -ItemType Directory -Path $BACKUP_DIR | Out-Null
    Write-Host "✓ Carpeta de backups creada: $BACKUP_DIR" -ForegroundColor Green
}

# Verificar que mysqldump existe
$mysqldump = Get-Command mysqldump -ErrorAction SilentlyContinue
if (!$mysqldump) {
    Write-Host "❌ ERROR: mysqldump no encontrado en PATH" -ForegroundColor Red
    Write-Host "   Asegúrate de tener MySQL instalado y en tu PATH" -ForegroundColor Yellow
    Exit 1
}

# Exportar base de datos
Write-Host ""
Write-Host "📦 Exportando BD: $DB_NAME desde $DB_HOST..." -ForegroundColor Cyan
Write-Host "   Ingresa la contraseña cuando se pida" -ForegroundColor Yellow

try {
    mysqldump -h $DB_HOST -u $DB_USER -p `
        --set-charset `
        --default-character-set=utf8mb4 `
        --single-transaction `
        --lock-tables=false `
        $DB_NAME > $BACKUP_FILE 2>&1
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ Error al exportar BD" -ForegroundColor Red
        Exit 1
    }
    
    # Obtener tamaño del archivo
    $fileSize = (Get-Item $BACKUP_FILE).Length
    $fileSizeMB = [math]::Round($fileSize / 1MB, 2)
    
    Write-Host "✓ BD Exportada exitosamente" -ForegroundColor Green
    Write-Host "   Archivo: $BACKUP_FILE" -ForegroundColor Cyan
    Write-Host "   Tamaño: $fileSizeMB MB" -ForegroundColor Cyan
    
} catch {
    Write-Host "❌ Error: $_" -ForegroundColor Red
    Exit 1
}

# Comprimir si es mayor a 10MB
if ($fileSize -gt 10MB) {
    Write-Host ""
    Write-Host "🗜️  Comprimiendo archivo (> 10MB)..." -ForegroundColor Cyan
    
    try {
        # Usar tar nativo de PowerShell (Windows 10+)
        tar -czf $BACKUP_COMPRESSED $BACKUP_FILE
        
        if (Test-Path $BACKUP_COMPRESSED) {
            $compressedSize = (Get-Item $BACKUP_COMPRESSED).Length
            $compressedSizeMB = [math]::Round($compressedSize / 1MB, 2)
            Write-Host "✓ Archivo comprimido: $BACKUP_COMPRESSED" -ForegroundColor Green
            Write-Host "   Tamaño comprimido: $compressedSizeMB MB" -ForegroundColor Cyan
        }
    }
    catch {
        Write-Host "⚠️  Advertencia: No se pudo comprimir (tar no disponible)" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "✅ EXPORTACIÓN COMPLETADA" -ForegroundColor Green
Write-Host ""
Write-Host "📋 Próximos pasos:" -ForegroundColor Yellow
Write-Host "   1. Nex paso: Transferir archivo a EC2" -ForegroundColor White
Write-Host "      .\2_TRANSFERIR_A_AWS.ps1" -ForegroundColor Cyan
Write-Host ""
Write-Host "   2. O manualmente:" -ForegroundColor White
Write-Host "      scp -i tu-key.pem '$BACKUP_FILE' ec2-user@tu-ec2-ip:/tmp/" -ForegroundColor Cyan
Write-Host ""
