# Script para configurar variables de entorno en PowerShell (Windows)
# Uso: .\setup-env.ps1

param(
    [string]$Host = "localhost",
    [string]$User = "admin",
    [string]$Password = "",
    [int]$Port = 8080
)

Write-Host "╔════════════════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "║  AgroComparador - Configuración de Entorno         ║" -ForegroundColor Green
Write-Host "║  Windows PowerShell                                ║" -ForegroundColor Green
Write-Host "╚════════════════════════════════════════════════════╝" -ForegroundColor Green
Write-Host ""

# Si no se proporciona contraseña, pedirla
if ([string]::IsNullOrEmpty($Password)) {
    Write-Host "⚠️  Necesito la contraseña de la base de datos" -ForegroundColor Yellow
    $SecurePassword = Read-Host "Ingresa la contraseña" -AsSecureString
    $Password = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto([System.Runtime.InteropServices.Marshal]::SecureStringToCoTaskMemUnicode($SecurePassword))
}

# Establecer variables de entorno
Write-Host "Estableciendo variables de entorno..." -ForegroundColor Cyan
$env:DB_HOST = $Host
$env:DB_USER = $User
$env:DB_PASSWORD = $Password
$env:DB_PORT = "3306"
$env:DB_NAME = "comparador"
$env:PORT = $Port

Write-Host ""
Write-Host "✅ Variables configuradas:" -ForegroundColor Green
Write-Host "   DB_HOST = $($env:DB_HOST)"
Write-Host "   DB_USER = $($env:DB_USER)"
Write-Host "   DB_PASSWORD = (oculto por seguridad)"
Write-Host "   DB_PORT = $($env:DB_PORT)"
Write-Host "   DB_NAME = $($env:DB_NAME)"
Write-Host "   PORT = $($env:PORT)"
Write-Host ""

Write-Host "Iniciando AgroComparador..." -ForegroundColor Cyan
Write-Host ""

java -cp ".;mysql-connector-java-9.0.0.jar" agrocomparador
