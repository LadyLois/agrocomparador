# 🚀 Instrucciones de Compilación y Ejecución - Con Web Scraping

## 📋 Requisitos

1. **Java 8+**
2. **MySQL 5.7+** con base de datos `agrocomparador` configurada
3. **Jsoup 1.15.3** (librería para web scraping)
4. **MySQL JDBC Driver** (mysql-connector-java)

## 📦 Descargar Dependencias

### 1. Jsoup (Web Scraping)
```powershell
# Descargar Jsoup
Invoke-WebRequest -Uri "https://jsoup.org/download" -OutFile jsoup-1.15.3.jar

# O manualmente:
# Ve a https://jsoup.org/ y descarga el JAR
# Coloca el archivo en: c:\Java\agrocomparador\lib\jsoup-1.15.3.jar
```

### 2. MySQL JDBC (ya debería estar disponible)
```powershell
# Si no tienes el driver, descargalo desde:
# https://dev.mysql.com/downloads/connector/j/
```

## 🗄️ Preparar Base de Datos

### ⚡ Opción 1: Automático (Recomendado)

Ejecuta este script desde PowerShell (en `c:\Java\agrocomparador\`):

```powershell
Set-ExecutionPolicy -ExecutionPolicy Bypass -Scope Process
.\setup-and-run.ps1
```

**Esto hace TODO automáticamente:**
- ✅ Configura MySQL en PATH
- ✅ Crea la tabla en BD
- ✅ Compila la aplicación
- ✅ Inicia el servidor

### ⚙️ Opción 2: Manual

```powershell
# 1. Agregar MySQL al PATH
$env:Path += ";C:\Program Files\MySQL\MySQL Server 8.4\bin"

# 2. Crear tabla (sin contraseña)
mysql -u root agrocomparador < c:\Java\agrocomparador\backups\crear_tabla_scraper.sql

# 3. Verificar que se creó
mysql -u root agrocomparador -e "SHOW TABLES;"
```

Si pide contraseña:
```powershell
mysql -u root -p agrocomparador < c:\Java\agrocomparador\backups\crear_tabla_scraper.sql
# Ingresa tu contraseña de MySQL
```

## 🔨 Compilar

### ⚡ Opción 1: Automático (Incluido en setup-and-run.ps1)

Ya está incluido en el script anterior.

### ⚙️ Opción 2: Manual

```powershell
cd c:\Java\agrocomparador

# Agregar MySQL al PATH
$env:Path += ";C:\Program Files\MySQL\MySQL Server 8.4\bin"

# Compilar con Jsoup y MySQL JDBC
javac -cp "lib/jsoup-1.15.3.jar;lib/mysql-connector-java-8.0.33.jar" -d . `
    agrocomparador.java `
    agrocomparador/data/*.java `
    agrocomparador/business/*.java `
    agrocomparador/ui/*.java `
    agrocomparador/scraper/*.java
```

## ▶️ Ejecutar

### ⚡ Opción 1: Automático (LO MÁS FÁCIL)

```powershell
cd c:\Java\agrocomparador
Set-ExecutionPolicy -ExecutionPolicy Bypass -Scope Process
.\setup-and-run.ps1
```

### ⚙️ Opción 2: Manual

```powershell
cd c:\Java\agrocomparador

# Agregar MySQL al PATH (si no lo hiciste antes)
$env:Path += ";C:\Program Files\MySQL\MySQL Server 8.4\bin"

# Ejecutar
java -cp "lib/jsoup-1.15.3.jar;lib/mysql-connector-java-8.0.33.jar;." agrocomparador
```

### 📱 En background (opcional)

```powershell
Start-Process -NoNewWindow -FilePath java -ArgumentList @(
    "-cp", "lib/jsoup-1.15.3.jar;lib/mysql-connector-java-8.0.33.jar;.",
    "agrocomparador"
)
```

## 📊 Lo que verás

```
🕐 Scheduler de scraper iniciado (cada 60 minutos)
🚀 Servidor iniciado en puerto 80
📍 Accede a: http://localhost/

🔄 Intento 1/3 de scraping desde agroprecios.com...
✓ Scrapeados 45 productos de AgroPrecios.com
✓ 45 registros guardados en BD
```

## 🌐 Acceder a la Aplicación

```
http://localhost/
http://localhost/?producto=Tomate
http://localhost/?producto=Pepino
```

## 📝 Características Implementadas

| Característica | Descripción |
|---|---|
| **Web Scraping** | Extrae datos de agroprecios.com cada 60 minutos |
| **Reintentos** | 3 intentos automáticos si falla el scraping |
| **Caché Local** | Guarda datos en archivo `scraper_cache.txt` |
| **BD Caché** | Tabla `precios_scraper` en MySQL |
| **Scheduler** | Actualización automática en thread separado |
| **Datos Combinados** | Muestra precios de BD + Scraper |
| **Origen Visible** | Indica si el dato viene de BD o Scraper |
| **Estadísticas** | Muestra total de registros, productos únicos, precio promedio |

## 🔧 Configuración

Edita los valores en estos archivos para personalizar:

### `agrocomparador/scraper/AgrePreciosScraperDAO.java`
```java
private static final String BASE_URL = "https://www.agroprecios.com/es/precios-producto/";
private static final int TIMEOUT = 15000; // 15 segundos
private static final int MAX_REINTENTOS = 3;
private static final int DELAY_ENTRE_REINTENTOS = 2000; // 2 segundos
```

### `agrocomparador/scraper/ScraperScheduler.java`
```java
private static final int INTERVALO_MINUTOS = 60; // Cambiar frecuencia
```

## 🧹 Limpiar Datos Antiguos

```sql
-- Ejecutar en MySQL para limpiar datos mayores a 7 días
CALL limpiar_cache_scraper();

-- O manualmente:
DELETE FROM precios_scraper WHERE fecha_actualizacion < DATE_SUB(NOW(), INTERVAL 7 DAY);
```

## ⚠️ Troubleshooting

### Jsoup JAR no encontrado
```
Error: package org.jsoup does not exist
```
Solución: Verifica que jsoup-1.15.3.jar esté en el classpath

### Error de conexión a MySQL
```
Error: No suitable driver found
```
Solución: Asegúrate de tener mysql-connector-java-*.jar en el classpath

### Scraper no extrae datos
- Inspecciona https://www.agroprecios.com/es/ con F12
- Adapta los selectores CSS en `AgrePreciosScraperDAO.java`

### Puerto 80 en uso
```
Error: Permission denied / Address already in use
```
Solución: Cambia el puerto en `WebServer.java` a 8080 o superior
