# 📋 Estructura del Proyecto - Comparador de Precios Agrícolas

## 🎯 Objetivo
Refactorizar una aplicación web Java en capas bien separadas siguiendo el patrón **MVC** (Model-View-Controller) de manera simple, sin frameworks complejos como Spring.

---

## 🏗️ Arquitectura en 3 Capas

### 1. 📀 CAPA DE DATOS (Data Access Layer)
**Ubicación:** `agrocomparador/data/`

**Responsabilidad:** Gestionar conexión y lectura desde la base de datos MySQL

#### DatabaseConnection.java
- Gestiona la conexión a MySQL
- Centraliza credenciales y configuración
- Proporciona métodos estáticos para obtener/cerrar conexiones
- **Métodos principales:**
  - `getConnection()` → Obtiene conexión a la BD
  - `closeConnection(conn)` → Cierra conexión seguramente

#### ProductoDAO.java (Data Access Object)
- Ejecuta consultas SQL específicas
- Transforma ResultSet en estructuras de datos Java (List<Map>)
- **Métodos principales:**
  - `obtenerProductos()` → Recupera todos los productos con precios

**Consulta SQL:**
```sql
SELECT p.nombre, p.variedad, f.nombre AS fuente, pr.precio 
FROM precios pr 
JOIN productos p ON pr.producto_id = p.id 
JOIN fuentes f ON pr.fuente_id = f.id 
ORDER BY p.nombre, pr.precio
```

---

### 2. ⚙️ CAPA DE LÓGICA (Business Logic Layer)
**Ubicación:** `agrocomparador/business/`

**Responsabilidad:** Implementar la lógica de negocio y procesamiento de datos

#### ProductoService.java
- Aplica filtros y lógica de comparación
- Puede reutilizarse en otros contextos (API REST, CLI, etc.)
- **Métodos principales:**
  - `obtenerTodosLosProductos()` → Obtiene todos sin filtros
  - `obtenerProductosPorNombre(String)` → **Filtra por nombre de producto**
  - `obtenerPrecioMinimo(productos, nombre)` → Encuentra precio más bajo
  - `obtenerFuenteBarata(productos, nombre)` → Encuentra fuente más barata
  - `obtenerTotalProductosUnicos(productos)` → Cuenta productos diferentes

---

### 3. 🎨 CAPA DE PRESENTACIÓN (UI/Frontend Layer)
**Ubicación:** `agrocomparador/ui/`

**Responsabilidad:** Generar HTML y manejar solicitudes HTTP

#### HTMLBuilder.java
- Construye respuestas HTML formateadas
- Genera tabla con columnas: **Producto | Variedad | Fuente | Precio**
- Incluye formulario de búsqueda
- Estilos CSS modernos
- **Métodos principales:**
  - `construirRespuestaHTML(productos, error, filtroAplicado)` → Genera HTML completo
  - `construirRespuestaHTTP(htmlContent)` → Envuelve en cabecera HTTP

#### WebServer.java
- Servidor HTTP en puerto 80 (ServerSocket)
- Parsea solicitudes HTTP y extrae parámetros
- Maneja hilos para múltiples clientes simultáneos
- **Rutas soportadas:**
  - `GET /` → Muestra todos los productos
  - `GET /?producto=Tomate` → Filtra por nombre

---

## 🔄 Flujo de Datos Completo

```
┌─────────────────────────────────────────────────────────────┐
│ 1. Cliente Web (navegador)                                  │
│    GET http://localhost/?producto=Tomate                   │
└──────────────────────┬──────────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────────┐
│ 2. WebServer (UI)                                           │
│    - Lee solicitud HTTP                                     │
│    - Extrae parámetro: producto=Tomate                      │
│    - Llama a ProductoService                               │
└──────────────────────┬──────────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────────┐
│ 3. ProductoService (Business Logic)                         │
│    - obtenerProductosPorNombre("Tomate")                    │
│    - Filtra lista de productos                             │
│    - Retorna List<Map> filtrada                            │
└──────────────────────┬──────────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────────┐
│ 4. ProductoDAO (Data Access)                                │
│    - obtenerProductos()                                     │
│    - Executa SELECT JOIN...                                │
│    - Transforma ResultSet en List<Map>                      │
└──────────────────────┬──────────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────────┐
│ 5. DatabaseConnection (Data Access)                         │
│    - getConnection() → Conecta a MySQL                      │
│    - closeConnection() → Cierra conexión                    │
└──────────────────────┬──────────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────────┐
│ 6. Base de Datos MySQL                                      │
│    - comparador.precios                                     │
│    - comparador.productos                                   │
│    - comparador.fuentes                                     │
│    - Retorna datos                                          │
└──────────────────────┬──────────────────────────────────────┘
                       │
                  [Retorno de datos...]
                       │
┌──────────────────────▼──────────────────────────────────────┐
│ 7. HTMLBuilder (UI)                                         │
│    - construirRespuestaHTML(productos, null, "Tomate")      │
│    - Genera tabla HTML con productos                        │
│    - Formatea respuesta HTTP con headers                    │
└──────────────────────┬──────────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────────┐
│ 8. Cliente Web (navegador)                                  │
│    - Recibe respuesta HTTP con tabla HTML                   │
│    - Renderiza página con estilos CSS                       │
└─────────────────────────────────────────────────────────────┘
```

---

## 📁 Estructura de Directorios

```
c:\Java\agrocomparador.worktrees\eaugustin\
│
├── agrocomparador.java                        ← Punto de entrada (main)
├── ESTRUCTURA.md                              ← Este archivo
│
└── agrocomparador/
    ├── data/                                  ← CAPA DE DATOS
    │   ├── DatabaseConnection.java
    │   └── ProductoDAO.java
    │
    ├── business/                              ← CAPA DE LÓGICA
    │   └── ProductoService.java
    │
    └── ui/                                    ← CAPA DE PRESENTACIÓN
        ├── HTMLBuilder.java
        └── WebServer.java
```

---

## 💻 Cómo Usar

### Compilar
```bash
javac -d . agrocomparador.java agrocomparador/data/*.java agrocomparador/business/*.java agrocomparador/ui/*.java
```

### Ejecutar
```bash
java agrocomparador
```

Accede a:
- 📍 **Todos los productos:** http://localhost/
- 🔍 **Filtrar por producto:** http://localhost/?producto=Tomate

---

## ✨ Características

✅ **Separación clara de responsabilidades**
- Cada capa tiene una función específica
- Cambios en BD no afectan HTML
- Cambios en HTML no afectan lógica

✅ **Búsqueda/Filtrado**
- Parámetro `?producto=XXX` en la URL
- Búsqueda case-insensitive
- Búsqueda parcial (contiene)

✅ **Tabla HTML moderna**
- Columnas: Producto | Variedad | Fuente | Precio
- Estilos CSS responsive
- Formatos de moneda (€)

✅ **Manejo de errores**
- Try-catch en cada capa
- Mensajes de error legibles
- Logs en consola

✅ **Escalabilidad**
- Fácil agregar nuevas consultas en DAO
- Fácil agregar nuevas rutas en WebServer
- Fácil agregar filtros adicionales en Service

✅ **Seguridad**
- Escape de caracteres HTML (XSS prevention)
- Decodificación de URL
- Manejo seguro de conexiones

---

## 🔧 Extensiones Futuras

### Agregar nueva funcionalidad a la BD
1. Editar `ProductoDAO.java` → Agregar nuevo método
2. Editar `ProductoService.java` → Exponer lógica
3. Editar `WebServer.java` → Agregar ruta

### Cambiar interfaz visual
1. Editar `HTMLBuilder.java` → Cambiar CSS/HTML
2. El resto del código no se ve afectado

### Cambiar servidor HTTP
1. Reemplazar `WebServer.java`
2. Las capas de negocio y datos funcionan igual

---

## 📊 Base de Datos

### Estructura esperada
```sql
CREATE DATABASE comparador;

CREATE TABLE productos (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100),
    variedad VARCHAR(100)
);

CREATE TABLE fuentes (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100)
);

CREATE TABLE precios (
    id INT PRIMARY KEY AUTO_INCREMENT,
    producto_id INT,
    fuente_id INT,
    precio DECIMAL(10, 2),
    fecha TIMESTAMP,
    FOREIGN KEY (producto_id) REFERENCES productos(id),
    FOREIGN KEY (fuente_id) REFERENCES fuentes(id)
);
```

### Credenciales (hardcodeadas en DatabaseConnection.java)
- **Host:** localhost
- **Puerto:** 3306
- **Base de datos:** comparador
- **Usuario:** admin
- **Contraseña:** AgroComparador2026!

> ⚠️ En producción, usar variables de entorno o archivo de configuración

---

## 🎓 Patrón Arquitectónico

Este proyecto implementa el patrón **DAO + Service + Presentation**:

```
┌─────────────────────────────────────┐
│   Presentation Layer                │
│   (HTMLBuilder, WebServer)          │
└──────────────┬──────────────────────┘
               │ Depende
┌──────────────▼──────────────────────┐
│   Service/Business Logic Layer      │
│   (ProductoService)                 │
└──────────────┬──────────────────────┘
               │ Depende
┌──────────────▼──────────────────────┐
│   Data Access Layer                 │
│   (ProductoDAO, DatabaseConnection) │
└──────────────┬──────────────────────┘
               │ Depende
┌──────────────▼──────────────────────┐
│   Database Layer                    │
│   (MySQL)                           │
└─────────────────────────────────────┘
```

Esta arquitectura facilita:
- **Testing** de cada capa independientemente
- **Mantenimiento** porque cambios están localizados
- **Reutilización** de componentes
- **Evolución** hacia patrones más complejos (Spring, etc.)

---

## 📝 Comentarios en el Código

Todos los archivos incluyen:
- Documentación JavaDoc
- Comentarios explicativos
- Descriptores claros de métodos y parámetros

---

## ✅ Verificación de Requisitos

- [x] Separación en 3 capas claras
- [x] DatabaseConnection.java → conexión MySQL
- [x] ProductoDAO.java → consultas SQL
- [x] ProductoService.java → lógica de negocio
- [x] agrocomparador.java → servidor HTTP limpio
- [x] Página "/" muestra tabla HTML
- [x] Columnas: Producto | Variedad | Fuente | Precio
- [x] Filtrado por producto (?producto=XXX)
- [x] JDBC sin frameworks
- [x] ServerSocket mantiene arquitectura
- [x] Código modular y comentado
- [x] HTMLBuilder separado para presentación
- [x] Manejo de errores completo

¡Proyecto completamente estructurado y listo para expandir!
