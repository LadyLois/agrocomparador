# 📥 Web Scraping Completamente Implementado - Resumen Técnico

## ✅ Lo que se ha construido

### 1. **AgrePreciosScraperDAO.java** (Capa de Datos)
```
agrocomparador/scraper/AgrePreciosScraperDAO.java
```
- Extrae datos de https://www.agroprecios.com/es/
- **3 reintentos automáticos** con delays
- Caché local en archivo (`scraper_cache.txt`)
- Limpieza de precios (€15,50 → 15.50)
- Timeout de 15 segundos

**Métodos principales:**
- `obtenerProductosDesdeScraper()` - Descarga y reintenta automáticamente
- `scrapearAgroPrecios()` - Realiza el scraping
- `limpiarPrecio()` - Normaliza valores monetarios

---

### 2. **ScraperScheduler.java** (Scheduler Automático)
```
agrocomparador/scraper/ScraperScheduler.java
```
- Ejecuta scraping cada **60 minutos** en background
- Thread daemon (no bloquea la aplicación)
- Patrón **Singleton**
- Guarda datos en tabla `precios_scraper` de MySQL
- Limpia datos > 7 días automáticamente

**Características:**
- `iniciar()` - Inicia el scheduler
- `detener()` - Para el scheduler
- Reintentos cada 5 min si hay error

---

### 3. **ProductoDAOScraper.java** (Capa de Datos - BD)
```
agrocomparador/data/ProductoDAOScraper.java
```
- Lee datos del scraper almacenados en BD
- Consultas optimizadas con índices
- Estadísticas en tiempo real

**Métodos:**
- `obtenerProductosDelScraper()` - Lee todos
- `obtenerPrecioMinimoDeScraper()` - Precio más bajo
- `obtenerFuenteBarataDeScraper()` - Fuente más barata
- `obtenerEstadisticasScraper()` - Stats para dashboard

---

### 4. **ProductoService.java** (Lógica de Negocio - Actualizado)
```
agrocomparador/business/ProductoService.java
```
**Métodos nuevos:**
- `obtenerTodosLosProductosCombinados()` - BD + Scraper
- `obtenerProductosPorNombreCombinados()` - Filtro en ambas fuentes
- `obtenerPrecioMinimoCombinado()` - Compara ambas fuentes
- `obtenerEstadisticasScraper()` - Obtiene stats

---

### 5. **HTMLBuilder.java** (UI - Actualizado)
```
agrocomparador/ui/HTMLBuilder.java
```
**Cambios:**
- Nueva sección info con estadísticas en vivo
- Dashboard de scraper (4 cajas de datos)
- Tabla con columna "Origen" (🗄️ BD vs 🌐 Scraper)
- Estilos CSS para diferenciar fuentes
- Última fecha de actualización

---

### 6. **agrocomparador.java** (Punto de entrada - Actualizado)
```
agrocomparador.java
```
- Inicia `ScraperScheduler` al arrancar
- Luego inicia el servidor web
- Todo ocurre en background automáticamente

---

### 7. **Tabla MySQL**
```sql
CREATE TABLE precios_scraper (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255),
    variedad VARCHAR(255),
    fuente VARCHAR(255),
    precio DECIMAL(10, 2),
    origen VARCHAR(50),
    fecha_actualizacion TIMESTAMP,
    ...índices
);
```

---

## 🎯 Flujo de Datos Completo

```
┌─────────────────────────────────────────────────┐
│ Aplicación Inicia                               │
│ agrocomparador.main()                           │
└────────────────┬────────────────────────────────┘
                 │
                 ├─→ ScraperScheduler.iniciar()
                 │   (Thread daemon en background)
                 │
                 └─→ WebServer.iniciar()
                     (Puerto 80)

┌─────────────────────────────────────────────────┐
│ Cada 60 minutos (background)                    │
├─────────────────────────────────────────────────┤
│ 1. Obtener HTML de agroprecios.com              │
│ 2. Extraer tabla con productos                  │
│ 3. Reintentar 3 veces si falla                  │
│ 4. Limpiar y validar precios                    │
│ 5. Guardar en tabla precios_scraper             │
│ 6. Limpiar datos > 7 días                       │
│ 7. Guardar caché local                          │
└─────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────┐
│ Usuario accede: http://localhost/               │
├─────────────────────────────────────────────────┤
│ WebServer recibe solicitud                      │
│   → ProductoService.obtenerTodosLosProductosCombinados()
│   → Lee de ProductoDAO (BD local)               │
│   → Lee de ProductoDAOScraper (BD scraper)      │
│   → Combina ambas listas                        │
│   → HTMLBuilder genera tabla con origen         │
│   → Respuesta HTTP con HTML                     │
└─────────────────────────────────────────────────┘
```

---

## 🚀 Cómo Usar

### 1. Descargar Jsoup
```powershell
cd c:\Java\agrocomparador

# Crear carpeta lib si no existe
mkdir lib -Force

# Descargar desde jsoup.org
Invoke-WebRequest -Uri "https://jsoup.org/dist/jsoup-1.15.3.jar" `
    -OutFile "lib/jsoup-1.15.3.jar"
```

### 2. Crear Tabla en BD
```bash
mysql -u root -p agrocomparador < backups/crear_tabla_scraper.sql
```

### 3. Compilar
```bash
cd c:\Java\agrocomparador
.\scripts\build\compile-with-scraper.bat

# O manualmente:
javac -cp "lib/jsoup-1.15.3.jar;lib/mysql-connector-java-8.0.33.jar" -d . `
    agrocomparador.java `
    agrocomparador/data/*.java `
    agrocomparador/business/*.java `
    agrocomparador/ui/*.java `
    agrocomparador/scraper/*.java
```

### 4. Ejecutar
```bash
java -cp "lib/jsoup-1.15.3.jar;lib/mysql-connector-java-8.0.33.jar;." agrocomparador
```

### 5. Ver en navegador
```
http://localhost/
```

---

## 📊 Salida en Consola

```
🕐 Scheduler de scraper iniciado (cada 60 minutos)
🚀 Servidor iniciado en puerto 80
📍 Accede a: http://localhost/

[Cada 60 minutos...]
📥 Iniciando actualización de datos desde AgroPrecios.com...
🔄 Intento 1/3 de scraping desde agroprecios.com...
✓ Scrapeados 45 productos de AgroPrecios.com
   → Se extrajeron 45 registros válidos
✓ 45 registros guardados en BD
✓ Actualización completada: 45 registros guardados

[Próxima actualización en 60 minutos]
```

---

## 🔄 Reintentos y Fallbacks

```
Intento 1: Scraping
  ✓ OK → Guarda en BD y caché
  ✗ Falla → Espera 2s, pasa a intento 2

Intento 2: Scraping
  ✓ OK → Guarda en BD y caché
  ✗ Falla → Espera 2s, pasa a intento 3

Intento 3: Scraping
  ✓ OK → Guarda en BD y caché
  ✗ Falla → Carga del caché local (scraper_cache.txt)
  
Si caché vacío → Retorna lista vacía (con advertencia)
```

---

## 📈 Estadísticas Mostradas

En la página web se muestran:
- **Total de Registros** - Cuántos precios hay en caché
- **Productos Únicos** - Cuántos productos diferentes
- **Precio Promedio** - €XX.XX
- **Última Actualización** - Fecha YYYY-MM-DD

---

## ⚙️ Configuración Personalizable

### Cambiar frecuencia de scraping (default: 60 min)
```java
// En ScraperScheduler.java
private static final int INTERVALO_MINUTOS = 60; // Cambiar aquí
```

### Cambiar URL de scraping
```java
// En AgrePreciosScraperDAO.java
private static final String BASE_URL = "https://www.agroprecios.com/es/precios-producto/";
```

### Cambiar selectores CSS (si cambia el HTML)
```java
// En AgrePreciosScraperDAO.java
Elements filas = doc.select("table tbody tr"); // Adaptar selectores
```

### Retención de datos (default: 7 días)
```java
// En ScraperScheduler.java
"WHERE fecha_actualizacion < DATE_SUB(NOW(), INTERVAL 7 DAY)"
// Cambiar INTERVAL 7 DAY
```

---

## 🛠️ Troubleshooting

### "package org.jsoup does not exist"
- Falta jsoup-1.15.3.jar en classpath
- Verifica: `lib/jsoup-1.15.3.jar` existe

### "No suitable driver found for jdbc:mysql"
- Falta mysql-connector-java-*.jar
- Descargalo desde MySQL website

### Scraper no extrae datos
- El HTML de agroprecios.com puede haber cambiado
- Inspecciona con F12 el sitio web
- Adapta selectores CSS en `AgrePreciosScraperDAO.java`

### Puerto 80 en uso
- Cambia puerto en `WebServer.java` (requiere recompilación)
- O executa con `java ... --port=8080` (si se implementa)

---

## 📋 Archivos Creados

| Archivo | Descripción |
|---------|-------------|
| `agrocomparador/scraper/AgrePreciosScraperDAO.java` | DAO del scraper |
| `agrocomparador/scraper/ScraperScheduler.java` | Scheduler automático |
| `agrocomparador/data/ProductoDAOScraper.java` | DAO para BD scraper |
| `backups/crear_tabla_scraper.sql` | Script SQL |
| `scripts/build/compile-with-scraper.bat` | Compilación Windows |
| `scripts/build/compile-with-scraper.sh` | Compilación Linux |
| `info/COMPILAR_CON_SCRAPER.md` | Guía completa |
| `info/WEB_SCRAPING_RESUMEN.md` | Este archivo |

---

## ✨ Características Totales

✅ Web scraping automático de agroprecios.com
✅ Reintentos automáticos (3 intentos)
✅ Caché local en archivo
✅ Almacenamiento en BD MySQL
✅ Actualización cada 60 minutos
✅ Combinación BD + Scraper
✅ Dashboard en vivo con estadísticas
✅ Indicador visual de origen (BD vs Scraper)
✅ Limpieza automática de datos antiguos
✅ Thread daemon (no bloquea la app)
✅ Patrón Singleton para scheduler
✅ Manejo de errores robusto
