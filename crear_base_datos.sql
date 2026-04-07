-- ========================================
-- SCRIPT SQL: CREAR BASE DE DATOS COMPARADOR
-- ========================================
-- Ejecuta este script en MySQL Workbench o en la línea de comandos
-- ========================================

-- 1. CREAR BASE DE DATOS
DROP DATABASE IF EXISTS comparador;
CREATE DATABASE comparador CHARACTER SET utf8 COLLATE utf8_general_ci;
USE comparador;

-- 2. CREAR TABLAS
-- Tabla de productos
CREATE TABLE productos (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    variedad VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) CHARACTER SET utf8 COLLATE utf8_general_ci;

-- Tabla de fuentes (comerciantes, distribuidores, etc.)
CREATE TABLE fuentes (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) CHARACTER SET utf8 COLLATE utf8_general_ci;

-- Tabla de precios (relación muchos-a-muchos)
CREATE TABLE precios (
    id INT PRIMARY KEY AUTO_INCREMENT,
    producto_id INT NOT NULL,
    fuente_id INT NOT NULL,
    precio DECIMAL(10, 2) NOT NULL,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (producto_id) REFERENCES productos(id) ON DELETE CASCADE,
    FOREIGN KEY (fuente_id) REFERENCES fuentes(id) ON DELETE CASCADE,
    INDEX idx_producto (producto_id),
    INDEX idx_fuente (fuente_id),
    INDEX idx_fecha (fecha)
) CHARACTER SET utf8 COLLATE utf8_general_ci;

-- 3. INSERTAR DATOS DE EJEMPLO

-- Insertar productos
INSERT INTO productos (nombre, variedad) VALUES
('Tomate', 'Cherry'),
('Tomate', 'Pera'),
('Tomate', 'Rama'),
('Lechuga', 'Romana'),
('Lechuga', 'Iceberg'),
('Lechuga', 'Rizada'),
('Zanahoria', 'Nantes'),
('Zanahoria', 'Puntiaguda'),
('Maíz', 'Dulce'),
('Maíz', 'Blanco'),
('Cebolla', 'Blanca'),
('Cebolla', 'Roja'),
('Patata', 'Agria'),
('Patata', 'Nueva'),
('Pimiento', 'Rojo'),
('Pimiento', 'Verde'),
('Berenjena', 'Larga'),
('Calabacín', 'Verde'),
('Ajo', 'Blanco'),
('Champiñón', 'Cultivado');

-- Insertar fuentes
INSERT INTO fuentes (nombre) VALUES
('Mercado Central'),
('Distribuidor Local'),
('Cooperativa Agrícola'),
('Supermercado Regional'),
('Tienda de Verduras'),
('Productor Directo');

-- Insertar precios
INSERT INTO precios (producto_id, fuente_id, precio) VALUES
-- Tomate Cherry
(1, 1, 0.85),
(1, 2, 0.90),
(1, 3, 0.80),
(1, 4, 0.95),

-- Tomate Pera
(2, 1, 1.20),
(2, 2, 1.15),
(2, 3, 1.10),
(2, 4, 1.25),

-- Tomate Rama
(3, 1, 0.75),
(3, 2, 0.78),
(3, 3, 0.70),

-- Lechuga Romana
(4, 1, 0.60),
(4, 2, 0.65),
(4, 3, 0.55),
(4, 4, 0.68),

-- Lechuga Iceberg
(5, 1, 0.50),
(5, 2, 0.55),
(5, 3, 0.48),

-- Lechuga Rizada
(6, 1, 0.45),
(6, 2, 0.50),

-- Zanahoria Nantes
(7, 1, 0.45),
(7, 2, 0.48),
(7, 3, 0.40),
(7, 4, 0.50),

-- Zanahoria Puntiaguda
(8, 1, 0.50),
(8, 3, 0.45),

-- Maíz Dulce
(9, 2, 1.50),
(9, 4, 1.45),
(9, 5, 1.55),

-- Maíz Blanco
(10, 1, 1.20),
(10, 3, 1.15),

-- Cebolla Blanca
(11, 1, 0.35),
(11, 2, 0.40),
(11, 3, 0.30),

-- Cebolla Roja
(12, 1, 0.45),
(12, 2, 0.48),

-- Patata Agria
(13, 1, 0.30),
(13, 2, 0.35),
(13, 3, 0.28),
(13, 4, 0.38),

-- Patata Nueva
(14, 1, 0.45),
(14, 3, 0.42),

-- Pimiento Rojo
(15, 1, 1.80),
(15, 2, 1.85),
(15, 4, 1.90),

-- Pimiento Verde
(16, 1, 1.20),
(16, 2, 1.25),

-- Berenjena Larga
(17, 1, 0.95),
(17, 2, 1.00),
(17, 3, 0.90),

-- Calabacín Verde
(18, 1, 0.65),
(18, 2, 0.70),
(18, 4, 0.75),

-- Ajo Blanco
(19, 1, 2.50),
(19, 2, 2.60),

-- Champiñón Cultivado
(20, 1, 1.50),
(20, 2, 1.55),
(20, 4, 1.60);

-- 4. VERIFICAR DATOS
SELECT COUNT(*) as total_productos FROM productos;
SELECT COUNT(*) as total_fuentes FROM fuentes;
SELECT COUNT(*) as total_precios FROM precios;

-- 5. VER EJEMPLO DE DATOS
SELECT 
    p.nombre,
    p.variedad,
    f.nombre AS fuente,
    pr.precio
FROM precios pr
JOIN productos p ON pr.producto_id = p.id
JOIN fuentes f ON pr.fuente_id = f.id
ORDER BY p.nombre, pr.precio
LIMIT 10;
