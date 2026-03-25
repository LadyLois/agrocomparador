# 🎨 GUÍA VISUAL DE LA ARQUITECTURA

## 📊 Diagrama de Componentes

```
┌─────────────────────────────────────────────────────────────────────┐
│                        NAVEGADOR WEB                                │
│                  http://localhost/?producto=Tomate                  │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
                               ↓
┌─────────────────────────────────────────────────────────────────────┐
│                   CAPA DE PRESENTACIÓN (UI)                          │
│                      agrocomparador/ui/                             │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌──────────────────────┐          ┌──────────────────────────┐   │
│  │   WebServer.java     │          │   HTMLBuilder.java       │   │
│  ├──────────────────────┤          ├──────────────────────────┤   │
│  │ • ServerSocket       │          │ • Tabla HTML             │   │
│  │ • Parsea URL         │─────────→│ • Estilos CSS            │   │
│  │ • Extrae params      │          │ • Formulario búsqueda    │   │
│  │ • Maneja threads     │          │ • Escape HTML (XSS)      │   │
│  │                      │          │                          │   │
│  │ GET /?producto=X     │          │ HTTP/1.1 200 OK          │   │
│  └────────┬─────────────┘          └──────────────┬───────────┘   │
│           │                                       │                │
└───────────┼───────────────────────────────────────┼────────────────┘
            │                                       │
            ↓                                       ↑
┌─────────────────────────────────────────────────────────────────────┐
│                   CAPA DE LÓGICA (NEGOCIO)                           │
│                   agrocomparador/business/                          │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │            ProductoService.java                             │  │
│  ├──────────────────────────────────────────────────────────────┤  │
│  │ • obtenerTodosLosProductos()                               │  │
│  │ • obtenerProductosPorNombre(String) ← Filtra aquí          │  │
│  │ • obtenerPrecioMinimo()                                    │  │
│  │ • obtenerFuenteBarata()                                    │  │
│  │                                                             │  │
│  │ Filtro: "Tomate" → retorna solo productos con "Tomate"    │  │
│  └────────────────────┬─────────────────────────────────────┘  │
│                       │                                        │
└───────────────────────┼────────────────────────────────────────┘
                        │
                        ↓
┌─────────────────────────────────────────────────────────────────────┐
│                   CAPA DE DATOS (DATA ACCESS)                       │
│                     agrocomparador/data/                           │
├─────────────────────────────────────────────────────────────────────┤
│                                                                    │
│  ┌───────────────────────┐    ┌──────────────────────────────┐   │
│  │ProductoDAO.java       │    │DatabaseConnection.java       │   │
│  ├───────────────────────┤    ├──────────────────────────────┤   │
│  │ • obtenerProductos()  │───→│ • getConnection()            │   │
│  │ • SELECT JOIN...      │    │ • closeConnection()          │   │
│  │ • Mapeo ResultSet     │    │ • Credenciales MySQL         │   │
│  │ • List<Map>           │    │ • localhost:3306             │   │
│  │                       │    │ • Usuario: admin             │   │
│  │                       │    │ • Pass: AgroComparador2026!  │   │
│  └───────────┬───────────┘    └──────────────┬───────────────┘   │
│              │                               │                  │
└──────────────┼───────────────────────────────┼──────────────────┘
               │                               │
               └───────────┬───────────────────┘
                           │
                           ↓
                ┌──────────────────────┐
                │  MYSQL DATABASE      │
                ├──────────────────────┤
                │ Database: comparador │
                │ • productos          │
                │ • fuentes            │
                │ • precios            │
                └──────────────────────┘
```

---

## 📋 Flujo de una Solicitud

### Escenario: Usuario busca "Tomate"

```
1. Usuario escribe en navegador:
   http://localhost/?producto=Tomate
   
   ↓
   
2. WebServer.manejarSolicitud() recibe:
   "GET /?producto=Tomate HTTP/1.1"
   
   ↓
   
3. Parsea y extrae:
   - ruta = "/?producto=Tomate"
   - filtroProducto = "Tomate"
   
   ↓
   
4. Llama a:
   ProductoService.obtenerProductosPorNombre("Tomate")
   
   ↓
   
5. ProductoService obtiene TODOS los produtos:
   ProductoDAO.obtenerProductos()
   → List<Map> con 50 productos
   
   ↓
   
6. ProductoDAO ejecuta:
   SELECT p.nombre, p.variedad, f.nombre, pr.precio
   FROM precios pr
   JOIN productos p ...
   JOIN fuentes f ...
   → ResultSet con datos de BD
   
   ↓
   
7. Convierte a List<Map>:
   [{nombre: Tomate, variedad: Cherry, fuente: Mercado, precio: 0.85},
    {nombre: Tomate, variedad: Pera, fuente: Local, precio: 0.90},
    ...]
   
   ↓
   
8. ProductoService FILTRA:
   Mantiene solo donde nombre contiene "Tomate"
   → List reducida a ~5 productos
   
   ↓
   
9. WebServer pasa a HTMLBuilder:
   construirRespuestaHTML(productosFiltrados, null, "Tomate")
   
   ↓
   
10. HTMLBuilder genera HTML:
    - Tabla con 5 filas
    - Formulario prerellenado: "Tomate"
    - Mensaje: "Se encontraron 5 registros"
    - Botón "Limpiar" visible
    
    ↓
    
11. HTMLBuilder envuelve en HTTP:
    HTTP/1.1 200 OK
    Content-Type: text/html; charset=UTF-8
    Content-Length: 8523
    Connection: close
    
    [Contenido HTML...]
    
    ↓
    
12. WebServer envía respuesta
    
    ↓
    
13. Navegador recibe HTTP 200 + HTML
    
    ↓
    
14. Navegador renderiza:
    ✅ Tabla visible con 5 productos
    ✅ Formulario con "Tomate"
    ✅ Botones "Buscar" y "Limpiar"
```

---

## 🔍 Detalles de Cada Capa

### 📀 CAPA DE DATOS

**DatabaseConnection.java** (60 líneas)
```
┌─────────────────────────────────────┐
│  DatabaseConnection                 │
├─────────────────────────────────────┤
│                                     │
│  URL = jdbc:mysql://localhost:3306  │
│  USER = admin                       │
│  PASSWORD = AgroComparador2026!     │
│                                     │
│  + getConnection()                  │
│    → Connection a MySQL             │
│                                     │
│  + closeConnection(Connection)      │
│    → Libera recurso                 │
│                                     │
└─────────────────────────────────────┘
```

**ProductoDAO.java** (70 líneas)
```
┌──────────────────────────────────────────┐
│  ProductoDAO                             │
├──────────────────────────────────────────┤
│                                          │
│  + obtenerProductos()                    │
│    └─ Ejecuta SELECT con JOINs           │
│       DatabaseConnection.getConnection() │
│       Statement.executeQuery(sql)        │
│       ResultSet → Loop → Map             │
│       List<Map<String, Object>>          │
│                                          │
│  Estructura del Map:                     │
│  {                                       │
│    "nombre": "Tomate",                   │
│    "variedad": "Cherry",                 │
│    "fuente": "Mercado Central",          │
│    "precio": 0.85                        │
│  }                                       │
│                                          │
└──────────────────────────────────────────┘
```

### ⚙️ CAPA DE LÓGICA

**ProductoService.java** (80 líneas)
```
┌──────────────────────────────────────────┐
│  ProductoService                         │
├──────────────────────────────────────────┤
│                                          │
│  + obtenerTodosLosProductos()            │
│    └─ return ProductoDAO.obtenerProductos()
│                                          │
│  + obtenerProductosPorNombre(String nom) │
│    └─ ProductoDAO.obtenerProductos()     │
│    └─ .stream().filter(p →               │
│       p.get("nombre").contains(nom))     │
│    └─ return List filtrada             │
│                                          │
│  + obtenerPrecioMinimo(...)              │
│    └─ Encuentra mínimo del stream        │
│                                          │
│  + obtenerFuenteBarata(...)              │
│    └─ Encuentra precio mínimo            │
│                                          │
│  Característicaws:                      │
│  • Case-insensitive (toLower)            │
│  • Búsqueda parcial (contains)           │
│  • Stream API (functional)               │
│                                          │
└──────────────────────────────────────────┘
```

### 🎨 CAPA DE PRESENTACIÓN

**WebServer.java** (90 líneas)
```
┌────────────────────────────────────────┐
│  WebServer                             │
├────────────────────────────────────────┤
│                                        │
│  PUERTO = 80                           │
│                                        │
│  + iniciar()                           │
│    └─ ServerSocket server = new(...) │
│    └─ loop {                           │
│         Socket cliente = accept()      │
│         new Thread(manejarSolicitud)   │
│       }                                │
│                                        │
│  + manejarSolicitud(Socket)            │
│    └─ Lee HTTP request                 │
│    └─ Parsea URL                       │
│    └─ Extrae params                    │
│    └─ Llama Service                    │
│    └─ Llama HTMLBuilder                │
│    └─ Envía respuesta HTTP             │
│                                        │
│  URL Parsing:                          │
│  GET /?producto=Tomate HTTP/1.1        │
│  ↓                                     │
│  producto=Tomate                       │
│  ↓                                     │
│  URLDecoder → "Tomate"                 │
│                                        │
└────────────────────────────────────────┘
```

**HTMLBuilder.java** (150 líneas)
```
┌──────────────────────────────────────────┐
│  HTMLBuilder                             │
├──────────────────────────────────────────┤
│                                          │
│  + construirRespuestaHTML(              │
│      productos,                         │
│      error,                             │
│      filtroAplicado)                    │
│    └─ <!DOCTYPE html>                   │
│    └─ <meta charset="UTF-8">            │
│    └─ CSS moderno                       │
│    └─ <h1>Comparador de Precios</h1>    │
│    └─ Formulario búsqueda               │
│    └─ <table>                           │
│       ├─ <thead>                        │
│       │  ├─ Producto                    │
│       │  ├─ Variedad                    │
│       │  ├─ Fuente                      │
│       │  └─ Precio                      │
│       └─ <tbody>                        │
│          └─ Loop productos              │
│             └─ <tr> con datos           │
│    └─ Escape HTML: &lt; &gt; &amp;      │
│    └─ Formato moneda: €0.85             │
│                                          │
│  + construirRespuestaHTTP(html)         │
│    └─ HTTP/1.1 200 OK                   │
│    └─ Content-Type: text/html;...       │
│    └─ Content-Length: 8523              │
│    └─ Connection: close                 │
│    └─ [Body]                            │
│                                          │
└──────────────────────────────────────────┘
```

---

## 📊 Tabla de Datos Retornada

```
FROM ProductoDAO.obtenerProductos():

[
  {
    "nombre": "Tomate",
    "variedad": "Cherry",
    "fuente": "Mercado Central",
    "precio": 0.85
  },
  {
    "nombre": "Tomate",
    "variedad": "Cherry",
    "fuente": "Distribuidor Local",
    "precio": 0.90
  },
  {
    "nombre": "Tomate",
    "variedad": "Pera",
    "fuente": "Cooperativa Agrícola",
    "precio": 1.20
  },
  {
    "nombre": "Lechuga",
    "variedad": "Romana",
    "fuente": "Mercado Central",
    "precio": 0.60
  },
  ...
]

DESPUÉS DE FILTRAR CON ProductoService.obtenerProductosPorNombre("Tomate"):

[
  {
    "nombre": "Tomate",
    "variedad": "Cherry",
    "fuente": "Mercado Central",
    "precio": 0.85
  },
  {
    "nombre": "Tomate",
    "variedad": "Cherry",
    "fuente": "Distribuidor Local",
    "precio": 0.90
  },
  {
    "nombre": "Tomate",
    "variedad": "Pera",
    "fuente": "Cooperativa Agrícola",
    "precio": 1.20
  }
]
```

---

## 🎯 Independencia de Capas

```
Cambio en DATOS (BD)           Cambio en LÓGICA (Filtros)
├─ Agregar columna             ├─ Agregar filtro por precio
├─ Cambiar SELECT             ├─ Agregar ordenamiento
└─ No afecta: UI, Lógica       └─ No afecta: Base de datos, UI
   
   Cambio en UI (HTML)
   ├─ Cambiar estilos CSS
   ├─ Agregar columnas nuevas
   └─ No afecta: Lógica, BD
```

**Cada capa se puede modificar sin tocar las otras** ← Ese es el poder de esta arquitectura.

---

## 🔄 Ciclo de Vida de una Request

```
┌─────────────────┐
│ Browser envía  │
│   solicitud    │
└────────┬────────┘
         │
         ↓
┌─────────────────────────┐
│ 1. WebServer recibe    │
│    Socket + HTTP      │
└────────┬────────────────┘
         │
         ↓
┌─────────────────────────┐
│ 2. Parsea y extrae    │
│    parámetros         │
└────────┬────────────────┘
         │
         ↓
┌─────────────────────────┐
│ 3. Llama ProductoService│
│    con filtro         │
└────────┬────────────────┘
         │
         ↓
┌─────────────────────────┐
│ 4. Service llama DAO  │
│    para datos         │
└────────┬────────────────┘
         │
         ↓
┌─────────────────────────┐
│ 5. DAO consulta BD    │
│    via Connection     │
└────────┬────────────────┘
         │
         ↓
┌─────────────────────────┐
│ 6. DAO retorna datos  │
│    filtradas          │
└────────┬────────────────┘
         │
         ↓
┌─────────────────────────┐
│ 7. HTMLBuilder genera │
│    tabla HTML         │
└────────┬────────────────┘
         │
         ↓
┌─────────────────────────┐
│ 8. WebServer envía    │
│    respuesta HTTP     │
└────────┬────────────────┘
         │
         ↓
┌─────────────────────────┐
│ Browser renderiza    │
│    página             │
└─────────────────────┘
```

---

**¡Todas las capas trabajando en armonía!** 🎼
