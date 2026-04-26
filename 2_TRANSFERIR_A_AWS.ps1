# ========================================
# SCRIPT 2: TRANSFERIR A AWS EC2
# Ejecutar en: tu PC Windows (PowerShell)
# ========================================

$ErrorActionPreference = "Stop"

Write-Host "🚀 Iniciando transferencia a AWS EC2..." -ForegroundColor Green

# ===== CONFIGURACIÓN =====
# IMPORTANTE: Actualizar con tus valores
$EC2_IP = "ec2-XX-XX-XX-XX.compute-1.amazonaws.com"  # ⚠️ CAMBIAR
$EC2_USER = "ec2-user"  # o "ubuntu" si es Ubuntu
$PEM_FILE = "C:\path\a\tu-key.pem"  # ⚠️ CAMBIAR
$BACKUP_DIR = "c:\Java\agrocomparador\backups"

# ===== VALIDAR CONFIGURACIÓN =====
Write-Host ""
Write-Host "📋 Validando configuración..." -ForegroundColor Cyan

# Validar variables
if ($EC2_IP -like "*XX-XX*" -or $EC2_IP -eq "tu-ec2-ip") {
    Write-Host "❌ ERROR: Debes actualizar \$EC2_IP con tu IP real" -ForegroundColor Red
    Write-Host "   Ejemplo: ec2-54-123-45-67.compute-1.amazonaws.com" -ForegroundColor Yellow
    Exit 1
}

if ($PEM_FILE -like "*path\a\tu-key*" -or $PEM_FILE -eq "tu-key.pem") {
    Write-Host "❌ ERROR: Debes actualizar \$PEM_FILE con la ruta a tu llave SSH" -ForegroundColor Red
    Write-Host "   Ejemplo: C:\Users\Usuario\Downloads\agrocomparador-key.pem" -ForegroundColor Yellow
    Exit 1
}

# Validar PEM existe
if (!(Test-Path $PEM_FILE)) {
    Write-Host "❌ ERROR: Archivo PEM no encontrado: $PEM_FILE" -ForegroundColor Red
    Exit 1
}

# Validar que SSH está disponible
$scp = Get-Command scp -ErrorAction SilentlyContinue
$ssh = Get-Command ssh -ErrorAction SilentlyContinue
if (!$scp -or !$ssh) {
    Write-Host "❌ ERROR: SSH/SCP no encontrado en PATH" -ForegroundColor Red
    Write-Host "   En Windows 10+, debe estar incluido. Verifica:" -ForegroundColor Yellow
    Write-Host "   - Servicios -> OpenSSH Authentication Agent (debe estar habilitado)" -ForegroundColor Yellow
    Write-Host "   - O instala Git Bash" -ForegroundColor Yellow
    Exit 1
}

# ===== SELECCIONAR ARCHIVO =====
Write-Host ""
Write-Host "📁 Archivos disponibles en $BACKUP_DIR:" -ForegroundColor Cyan

$backups = @()
if (Test-Path $BACKUP_DIR) {
    $backups = Get-ChildItem -Path $BACKUP_DIR -Filter "*.sql*" | Sort-Object LastWriteTime -Descending
}

if ($backups.Count -eq 0) {
    Write-Host "❌ No hay archivos SQL en $BACKUP_DIR" -ForegroundColor Red
    Write-Host "   Primero ejecuta: .\1_EXPORTAR_BD.ps1" -ForegroundColor Yellow
    Exit 1
}

$backups | ForEach-Object -Begin {$i=0} -Process { Write-Host "   [$i] $($_.Name) ($([math]::Round($_.Length/1MB,2)) MB)" -ForegroundColor White; $i++ }

Write-Host ""
$selection = Read-Host "Selecciona el número del archivo a transferir (0-$($backups.Count-1))"

if ($selection -lt 0 -or $selection -ge $backups.Count) {
    Write-Host "❌ Selección inválida" -ForegroundColor Red
    Exit 1
}

$BACKUP_FILE = $backups[$selection].FullName
$BACKUP_BASENAME = $backups[$selection].Name

# ===== TRANSFERIR =====
Write-Host ""
Write-Host "🌐 Transferiendo archivo..." -ForegroundColor Cyan
Write-Host "   Origen:  $BACKUP_FILE" -ForegroundColor White
Write-Host "   Destino: ${EC2_USER}@${EC2_IP}:/tmp/$BACKUP_BASENAME" -ForegroundColor White
Write-Host "   Puede tomar unos minutos..." -ForegroundColor Yellow
Write-Host ""

try {
    # Transferir
    & scp -i $PEM_FILE -o StrictHostKeyChecking=no -o UserKnownHostsFile=NUL $BACKUP_FILE "${EC2_USER}@${EC2_IP}:/tmp/"
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ Error en la transferencia" -ForegroundColor Red
        Write-Host "   Verifica:" -ForegroundColor Yellow
        Write-Host "   - EC2 IP y credenciales son correctas" -ForegroundColor Yellow
        Write-Host "   - Security Group permite SSH (puerto 22)" -ForegroundColor Yellow
        Write-Host "   - Archivo PEM tiene permisos correctos" -ForegroundColor Yellow
        Exit 1
    }
    
    Write-Host "✓ Archivo transferido exitosamente" -ForegroundColor Green
    
} catch {
    Write-Host "❌ Error: $_" -ForegroundColor Red
    Exit 1
}

# ===== INSTRUCCIONES PARA EC2 =====
Write-Host ""
Write-Host "✅ TRANSFERENCIA COMPLETADA" -ForegroundColor Green
Write-Host ""
Write-Host "📋 Próximos pasos en tu EC2:" -ForegroundColor Yellow
Write-Host ""
Write-Host "1️⃣  Conecta a tu EC2 por SSH:" -ForegroundColor Cyan
Write-Host "    ssh -i tu-key.pem ${EC2_USER}@${EC2_IP}" -ForegroundColor White
Write-Host ""
Write-Host "2️⃣  Dentro de EC2, importa la BD:" -ForegroundColor Cyan
Write-Host "    mysql -h localhost -u admin -p comparador < /tmp/$BACKUP_BASENAME" -ForegroundColor White
Write-Host ""
Write-Host "3️⃣  (Si es .gz, descomprime primero):" -ForegroundColor Cyan
Write-Host "    gunzip /tmp/$BACKUP_BASENAME" -ForegroundColor White
Write-Host ""
Write-Host "4️⃣  Verifica los datos:" -ForegroundColor Cyan
Write-Host "    mysql -h localhost -u admin -p comparador -e 'SELECT COUNT(*) FROM productos;'" -ForegroundColor White
Write-Host ""
Write-Host "💡 Usa: .\3_IMPORTAR_EN_EC2.sh en EC2 para automatizar pasos 2-4" -ForegroundColor Yellow
Write-Host ""
