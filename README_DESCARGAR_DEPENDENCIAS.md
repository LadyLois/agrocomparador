# 📥 Descargar Dependencias para Web Scraping

Este script descarga automáticamente las librerías necesarias para compilar y ejecutar agrocomparador con soporte de Web Scraping.

## 📦 Dependencias descargadas

1. **Jsoup 1.15.3** - HTML parser para web scraping
2. **MySQL JDBC Driver** - Conexión a base de datos MySQL

## ▶️ Ejecutar

### Windows (PowerShell)
```powershell
cd c:\Java\agrocomparador
.\scripts\download-dependencies.ps1
```

### Linux/Mac (Bash)
```bash
cd ~/Java/agrocomparador
bash scripts/download-dependencies.sh
```

## ✅ Verificar Instalación

Después de ejecutar el script, verifica que los archivos existan:

```powershell
ls lib/jsoup-*.jar
ls lib/mysql-connector-*.jar
```

Deberían listar los JARs descargados.

## 🔧 Manual (Si prefieres descargar manualmente)

### Jsoup
1. Ve a https://jsoup.org/download
2. Descarga `jsoup-1.15.3.jar`
3. Coloca en `lib/jsoup-1.15.3.jar`

### MySQL JDBC
1. Ve a https://dev.mysql.com/downloads/connector/j/
2. Descarga `mysql-connector-java-8.0.33.jar`
3. Coloca en `lib/mysql-connector-java-8.0.33.jar`

## 📝 Próximos Pasos

```bash
# 1. Compilar
.\scripts\build\compile-with-scraper.bat

# 2. Crear tabla en BD
mysql -u root -p agrocomparador < backups/crear_tabla_scraper.sql

# 3. Ejecutar
java -cp "lib/jsoup-1.15.3.jar;lib/mysql-connector-java-8.0.33.jar;." agrocomparador
```
