# AgroComparador — Archivos necesarios para el despliegue

## Información del proyecto

| Campo | Valor |
|---|---|
| Aplicación | AgroComparador |
| Lenguaje | Java (sin framework, servidor HTTP propio) |
| Base de datos | MySQL |
| Servidor | AWS EC2 (Ubuntu) |
| Puerto | 8080 |
| Repositorio | https://github.com/LadyLois/agrocomparador (rama: eaugustin) |

---

## Archivos imprescindibles

### Código fuente Java

| Archivo | Descripción |
|---|---|
| `agrocomparador.java` | Punto de entrada de la aplicación |
| `agrocomparador/data/DatabaseConnection.java` | Gestión de conexión a MySQL |
| `agrocomparador/data/ProductoDAO.java` | Consultas de productos en BD local |
| `agrocomparador/data/ProductoDAOScraper.java` | Consultas de precios scrapeados |
| `agrocomparador/business/ProductoService.java` | Lógica de negocio y filtrado |
| `agrocomparador/scraper/AgrePreciosScraperDAO.java` | Scraper de agroprecios.com |
| `agrocomparador/scraper/AgroPizarraScraperDAO.java` | Scraper de agropizarra.com |
| `agrocomparador/scraper/ScraperScheduler.java` | Programador automático de scraping |
| `agrocomparador/ui/HTMLBuilder.java` | Generación de la interfaz web |
| `agrocomparador/ui/WebServer.java` | Servidor HTTP |

### Librerías (JARs)

| Archivo | Descripción |
|---|---|
| `jsoup-1.15.3.jar` | Librería de web scraping (Jsoup) |
| `mysql-connector-java-9.0.0.jar` | Driver JDBC para MySQL |

### Configuración y arranque

| Archivo | Descripción |
|---|---|
| `.env.production` | Variables de entorno (credenciales BD, puerto) |
| `scripts/execution/run_production.sh` | Script de arranque del servidor |
| `scripts/migration/migrate_schema.sql` | Creación de tablas en la base de datos |

---

## Variables de entorno requeridas

El archivo `.env.production` debe contener:

```
DB_HOST=localhost
DB_PORT=3306
DB_NAME=comparador
DB_USER=admin
DB_PASSWORD=tu_password
PORT=8080
```

---

## Esquema de base de datos

Tablas necesarias en MySQL (generadas por `migrate_schema.sql`):

| Tabla | Descripción |
|---|---|
| `productos` | Catálogo de productos (nombre, variedad) |
| `fuentes` | Fuentes de precios (nombre de la subasta/mercado) |
| `precios` | Registros de precios con fecha y origen (AGROPRECIOS / AGROPIZARRA) |

---

## Pasos para desplegar desde cero

### 1. Clonar el repositorio
```bash
git clone https://github.com/LadyLois/agrocomparador.git
cd agrocomparador
git checkout eaugustin
```

### 2. Crear la base de datos
```bash
mysql -u admin -p comparador < scripts/migration/migrate_schema.sql
```

### 3. Configurar variables de entorno
```bash
cp .env.production.example .env.production
nano .env.production   # rellenar con los valores reales
```

### 4. Compilar
```bash
javac -cp ".:mysql-connector-java-9.0.0.jar:jsoup-1.15.3.jar" \
  agrocomparador.java \
  agrocomparador/data/*.java \
  agrocomparador/business/*.java \
  agrocomparador/scraper/*.java \
  agrocomparador/ui/*.java
```

### 5. Arrancar
```bash
bash scripts/execution/run_production.sh
```

### 6. Verificar
```bash
tail -f agrocomparador.log
```

---

## Pasos para actualizar en producción

```bash
# En el servidor
git pull origin eaugustin

javac -cp ".:mysql-connector-java-9.0.0.jar:jsoup-1.15.3.jar" \
  agrocomparador.java \
  agrocomparador/data/*.java \
  agrocomparador/business/*.java \
  agrocomparador/scraper/*.java \
  agrocomparador/ui/*.java

pkill -f 'java.*agrocomparador'
bash scripts/execution/run_production.sh
```

---

## Archivos prescindibles (solo desarrollo/documentación)

- `info/` — documentación técnica interna
- `backups/` — volcados SQL históricos
- `scripts/build/` — scripts de compilación alternativos
- `scripts/execution/run.bat`, `run.ps1`, `run_aws.bat` — arranque en Windows local
- `scripts/migration/1_*.ps1`, `2_*.ps1` — scripts de migración a AWS
- `scraper_cache.txt` — caché temporal del scraper
- `HelloWeb.java` — archivo de prueba inicial
- `README*.md`, `INICIO_RAPIDO*.md` — documentación

---

*Generado el 2026-05-14*
