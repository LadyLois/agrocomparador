# 🗄️ Configurar MySQL para Agroagrocomparador

## 1️⃣ Descargar Driver JDBC MySQL

### Opción A: Descargar manualmente
1. Ve a: https://dev.mysql.com/downloads/connector/j/
2. Descarga: `mysql-connector-java-*.jar` (última versión 5.x o 8.x)
3. Guarda en: `c:\Java\agroagrocomparador\` (en la raíz del proyecto)

### Opción B: Usando Maven (si tienes Maven instalado)
Si tienes Maven, descarga automáticamente ejecutando en la carpeta del proyecto:
```bash
mvn dependency:copy-dependencies -DoutputDirectory=lib
```

---

## 2️⃣ Crear Base de Datos en MySQL

### Prerequisitos
- MySQL 5.7+ instalado y corriendo
- Acceso a MySQL con usuario `root` y contraseña `Agroagrocomparador2026!`

### Crear esquema
1. Abre MySQL Workbench o CMD mysql
2. Ejecuta estos comandos:

```sql
-- Crear base de datos
CREATE DATABASE agrocomparador;

-- Usar la base de datos
USE agrocomparador;

-- Tabla de productos
CREATE TABLE productos (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    variedad VARCHAR(100)
);

-- Tabla de fuentes (comerciantes/distribuidores)
CREATE TABLE fuentes (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL UNIQUE
);

-- Tabla de precios (relación muchos-a-muchos)
CREATE TABLE precios (
    id INT PRIMARY KEY AUTO_INCREMENT,
    producto_id INT NOT NULL,
    fuente_id INT NOT NULL,
    precio DECIMAL(10, 2) NOT NULL,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (producto_id) REFERENCES productos(id) ON DELETE CASCADE,
    FOREIGN KEY (fuente_id) REFERENCES fuentes(id) ON DELETE CASCADE
);
```

---

## 3️⃣ Insertar Datos de Ejemplo

```sql
-- Insertar productos
INSERT INTO productos (nombre, variedad) VALUES
('Tomate', 'Cherry'),
('Tomate', 'Pera'),
('Lechuga', 'Romana'),
('Lechuga', 'Iceberg'),
('Zanahoria', 'Nantes'),
('Maíz', 'Dulce'),
('Cebolla', 'Blanca'),
('Patata', 'Agria');

-- Insertar fuentes
INSERT INTO fuentes (nombre) VALUES
('Mercado Central'),
('Distribuidor Local'),
('Cooperativa Agrícola'),
('Supermercado Regional');

-- Insertar precios
INSERT INTO precios (producto_id, fuente_id, precio) VALUES
(1, 1, 0.85),  -- Tomate Cherry - Mercado Central
(1, 2, 0.90),  -- Tomate Cherry - Distribuidor Local
(1, 3, 0.80),  -- Tomate Cherry - Cooperativa
(2, 1, 1.20),  -- Tomate Pera - Mercado Central
(2, 2, 1.15),  -- Tomate Pera - Distribuidor
(3, 1, 0.60),  -- Lechuga Romana - Mercado Central
(3, 2, 0.65),  -- Lechuga Romana - Distribuidor
(3, 3, 0.55),  -- Lechuga Romana - Cooperativa
(4, 1, 0.50),  -- Lechuga Iceberg - Mercado
(4, 2, 0.55),  -- Lechuga Iceberg - Distribuidor
(5, 1, 0.45),  -- Zanahoria - Mercado
(5, 3, 0.40),  -- Zanahoria - Cooperativa
(6, 2, 1.50),  -- Maíz Dulce - Distribuidor
(6, 4, 1.45),  -- Maíz Dulce - Supermercado
(7, 1, 0.35),  -- Cebolla - Mercado
(7, 2, 0.40),  -- Cebolla - Distribuidor
(8, 1, 0.30),  -- Patata - Mercado
(8, 3, 0.28);  -- Patata - Cooperativa
```

---

## 4️⃣ Compilar con Driver JDBC

### En PowerShell (Windows)

```powershell
# Opción 1: Si el driver está en la raíz del proyecto
javac -cp "mysql-connector-java-*.jar" -d . agroagrocomparador.java agroagrocomparador/data/*.java agroagrocomparador/business/*.java agroagrocomparador/ui/*.java

# Opción 2: Si descargaste driver específico (ejemplo: versión 8.0.33)
javac -cp "mysql-connector-java-8.0.33.jar" -d . agroagrocomparador.java agroagrocomparador/data/*.java agroagrocomparador/business/*.java agroagrocomparador/ui/*.java

# Opción 3: En subdirectorio (si creaste carpeta lib/)
javac -cp "lib/*" -d . agroagrocomparador.java agroagrocomparador/data/*.java agroagrocomparador/business/*.java agroagrocomparador/ui/*.java
```

---

## 5️⃣ Ejecutar con Driver JDBC

### Opción 1: Usar puerto 80 (requiere admin)
```powershell
java -cp ".;mysql-connector-java-*.jar" agroagrocomparador
```

### Opción 2: Usar puerto 8080 (SIN permisos admin)
Primero edita [WebServer.java](agroagrocomparador/ui/WebServer.java):
- Línea 10: Cambia `private static final int PUERTO = 80;` 
- A: `private static final int PUERTO = 8080;`

Luego compila y ejecuta:
```powershell
javac -cp "mysql-connector-java-*.jar" -d . agroagrocomparador.java agroagrocomparador/data/*.java agroagrocomparador/business/*.java agroagrocomparador/ui/*.java

java -cp ".;mysql-connector-java-*.jar" agroagrocomparador
```

Abre: http://localhost:8080/

---

## 6️⃣ Abrir en Navegador

Después de ejecutar, abre:
- **http://localhost/** (si usas puerto 80)
- **http://localhost:8080/** (si usas puerto 8080)

Deberías ver:
- ✅ Una tabla con los productos
- ✅ Columnas: Producto | Variedad | Fuente | Precio
- ✅ Todos los datos de la base de datos
- ✅ Formulario de búsqueda funcionando

---

## 🔧 Cambiar Credenciales de Base de Datos

Si usas **usuario/contraseña diferente**, edita [DatabaseConnection.java](agroagrocomparador/data/DatabaseConnection.java):

```java
private static final String URL = "jdbc:mysql://localhost:3306/agrocomparador";
private static final String USER = "tu_usuario";        // ← Cambia esto
private static final String PASSWORD = "tu_contraseña"; // ← Y esto
```

Luego recompila.

---

## ⚠️ Errores Comunes y Soluciones

### ❌ "JDBC Driver not found" o "Class not found: com.mysql.jdbc.Driver"
**Problema:** El driver JDBC no está en el classpath  
**Solución:** 
1. Verifica que el `.jar` está en la carpeta del proyecto
2. Usa `-cp "mysql-connector-java-*.jar"` en la compilación

### ❌ "Connection refused" o "Cannot connect to server"
**Problema:** MySQL no está corriendo  
**Solución:**
1. Inicia MySQL: Busca "MySQL 8.0 Command Line Client" o usa servicios Windows
2. Verifica que el servidor está en: `localhost:3306`

### ❌ "Access denied for user 'admin'"
**Problema:** Usuario/contraseña incorrectos  
**Solución:** 
1. Verifica credenciales en MySQL
2. Edita [DatabaseConnection.java](agroagrocomparador/data/DatabaseConnection.java) con credenciales correctas
3. Recompila

### ❌ "Unknown database 'agrocomparador'"
**Problema:** Base de datos no existe  
**Solución:**
1. Ejecuta el script SQL de creación (sección 2️⃣)
2. Verifica con MySQL: `SHOW DATABASES;`

### ❌ Tabla vacía en navegador
**Problema:** No hay datos en la base de datos  
**Solución:** Ejecuta el script de datos de ejemplo (sección 3️⃣)

---

## 📁 Estructura de Archivos Esperada

```
c:\Java\agroagrocomparador\
├── mysql-connector-java-*.jar          ← Driver JDBC
├── agroagrocomparador.java                 ← Punto de entrada
├── agroagrocomparador/
│   ├── data/
│   │   ├── DatabaseConnection.java
│   │   └── ProductoDAO.java
│   ├── business/
│   │   └── ProductoService.java
│   └── ui/
│       ├── WebServer.java
│       └── HTMLBuilder.java
└── CONFIGURAR_MYSQL.md                 ← Este archivo
```

---

## ✅ Checklist de Configuración

- [ ] MySQL 5.7+ instalado y corriendo
- [ ] Driver JDBC MySQL descargado en la carpeta del proyecto
- [ ] Base de datos `agrocomparador` creada
- [ ] Tablas creadas (productos, fuentes, precios)
- [ ] Datos de ejemplo insertados
- [ ] Compilado con `-cp "mysql-connector-java-*.jar"`
- [ ] Ejecutado con `-cp ".;mysql-connector-java-*.jar"`
- [ ] Navegador abierto en http://localhost/ o http://localhost:8080/
- [ ] Tabla visible con datos desde MySQL ✅

---

## 🚀 Próximos Pasos

1. **Importar más datos:** Modifica el script SQL
2. **Agregar filtros:** Edita [ProductoService.java](agroagrocomparador/business/ProductoService.java)
3. **Cambiar estilos:** Modifica [HTMLBuilder.java](agroagrocomparador/ui/HTMLBuilder.java)
4. **Deploy:** Usa Tomcat o cualquier servidor Java
