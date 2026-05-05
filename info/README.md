# AgroComparador - agrocomparador de Precios Agrícolas

Una aplicación web Java simple que compara precios de productos agrícolas consultando una base de datos MySQL, con arquitectura de **3 capas bien separadas**.

## 🚀 Inicio Rápido

### Requisitos
- Java 8 o superior
- MySQL 5.7+ con base de datos `agrocomparador` configurada
- Driver JDBC MySQL (mysql-connector-java)

### Compilar
```bash
cd c:\Java\agrocomparador.worktrees\eaugustin

# Compilar todos los archivos
javac -d . agrocomparador.java agrocomparador/data/*.java agrocomparador/business/*.java agrocomparador/ui/*.java
```

### Ejecutar
```bash
java agrocomparador
```

Verás:
```
🚀 Servidor iniciado en puerto 80
📍 Accede a: http://localhost/
```

---

## 📍 Usar la Aplicación

### 🏠 Página Principal
```
http://localhost/
```
Muestra **todos los productos** con sus precios de diferentes fuentes en una tabla.

### 🔍 Filtrar por Producto
```
http://localhost/?producto=Tomate
```
Filtra productos que contengan "Tomate" en el nombre (búsqueda case-insensitive).

Otros ejemplos:
- `http://localhost/?producto=Lechuga`
- `http://localhost/?producto=maiz` (mayúsculas/minúsculas no importan)

### Limpiar Filtro
Usa el botón "Limpiar" en la página o vuelve a `http://localhost/`

---

## 📊 Tabla de Columnas

| Columna | Descripción |
|---------|------------|
| **Producto** | Nombre del producto agrícola |
| **Variedad** | Variedad específica del producto |
| **Fuente** | Comerciante o distribuidor |
| **Precio** | Precio en euros (€) |

---

## 🏗️ Arquitectura

La aplicación está dividida en **3 capas independientes**:

### 📀 Capa de Datos (`agrocomparador/data/`)
```
DatabaseConnection.java  → Gestiona conexión MySQL
ProductoDAO.java         → Consultas SQL y recuperación de datos
```

### ⚙️ Capa de Lógica (`agrocomparador/business/`)
```
ProductoService.java     → Lógica de negocio y filtrados
```

### 🎨 Capa de Presentación (`agrocomparador/ui/`)
```
WebServer.java           → Servidor HTTP y rutas
HTMLBuilder.java         → Generación de HTML y estilos
```

Consulta [ESTRUCTURA.md](ESTRUCTURA.md) para una documentación detallada.

---

## 🗄️ Base de Datos

### Crear Base de Datos
```sql
CREATE DATABASE agrocomparador;

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

### Insertar Datos de Ejemplo
```sql
INSERT INTO productos (nombre, variedad) VALUES
('Tomate', 'Cherry'),
('Tomate', 'Pera'),
('Lechuga', 'Romana'),
('Lechuga', 'Iceberg'),
('Zanaoria', 'Nantes'),
('Maiz', 'Dulce');

INSERT INTO fuentes (nombre) VALUES
('Mercado Central'),
('Distribuidor Local'),
('Cooperativa Agrícola'),
('Supermercado Regional');

INSERT INTO precios (producto_id, fuente_id, precio, fecha) VALUES
(1, 1, 0.85, NOW()),
(1, 2, 0.90, NOW()),
(1, 3, 0.80, NOW()),
(2, 1, 1.20, NOW()),
(2, 2, 1.15, NOW()),
(3, 1, 0.60, NOW()),
(3, 2, 0.65, NOW()),
(3, 3, 0.55, NOW());
```

### Configuración de Conexión
Edita `agrocomparador/data/DatabaseConnection.java`:
```java
private static final String URL = "jdbc:mysql://localhost:3306/agrocomparador";
private static final String USER = "admin";
private static final String PASSWORD = "AgroComparador2026!";
```

---

## 💻 Estructura de Directorios

```
agrocomparador.worktrees/eaugustin/
├── agrocomparador.java              ← Punto de entrada (main)
├── README.md                        ← Este archivo
├── ESTRUCTURA.md                    ← Documentación detallada
│
└── agrocomparador/
    ├── data/
    │   ├── DatabaseConnection.java
    │   └── ProductoDAO.java
    ├── business/
    │   └── ProductoService.java
    └── ui/
        ├── HTMLBuilder.java
        └── WebServer.java
```

---

## 🔧 Solución de Problemas

### Puerto 80 en uso
```
❌ Error: Address already in use
```
**Solución 1:** Cambiar puerto en `WebServer.java`
```java
private static final int PUERTO = 8080;
```

Luego accede a: `http://localhost:8080/`

**Solución 2:** Matar proceso en puerto 80 (Windows)
```bash
netstat -ano | findstr :80
taskkill /PID <PID> /F
```

### Error de conexión a BD
```
❌ Error: Connection refused
```
**Solución:**
1. Verificar que MySQL esté corriendo
2. Verificar credenciales en DatabaseConnection.java
3. Verificar que base de datos `agrocomparador` existe
4. Verificar que driver JDBC está en classpath

### Tabla vacía
**Solución:** Insertar datos de prueba (ver sección de BD)

---

## 🎨 Personalización

### Cambiar Estilos CSS
Edita `agrocomparador/ui/HTMLBuilder.java` → método `construirCSS()`

### Agregar Nuevos Filtros
1. Edita `ProductoService.java` → Agregar método
2. Edita `WebServer.java` → Parsear parámetro
3. Edita `HTMLBuilder.java` → Mostrar filtro activo

### Agregar Nuevas Columnas
1. Edita `ProductoDAO.java` → Agregar a SELECT
2. Edita `ProductoService.java` si necesita lógica
3. Edita `HTMLBuilder.java` → Agregar columna a tabla

---

## 📚 Documentación

- **[ESTRUCTURA.md](ESTRUCTURA.md)** - Explicación detallada de la arquitectura

---

## 🚀 Próximos Pasos

- [ ] Agregar paginación a la tabla
- [ ] Filtrar por rango de precios
- [ ] Agregar histórico de precios
- [ ] Exportar a CSV
- [ ] API REST
- [ ] Caché de consultas
- [ ] Autenticación de usuarios

---

## 📝 Licencia

Proyecto educativo sin licencia específica.

---

## 👨‍💻 Autor

Desarrollado como ejemplo de arquitectura en capas sin frameworks.

---

## 📞 Soporte

Para dudas sobre la estructura, consulta [ESTRUCTURA.md](ESTRUCTURA.md).

Para errores de compilación/ejecución:
1. Verifica Java 8+
2. Verifica MySQL corriendo
3. Verifica credenciales en DatabaseConnection.java
4. Verifica puerto 80 disponible (o cambia a otro)

¡Éxito! 🎉
