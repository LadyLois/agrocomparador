DROP DATABASE IF EXISTS comparador;
CREATE DATABASE comparador CHARACTER SET utf8 COLLATE utf8_general_ci;
USE comparador;

CREATE TABLE productos (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    variedad VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) CHARACTER SET utf8 COLLATE utf8_general_ci;

CREATE TABLE fuentes (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) CHARACTER SET utf8 COLLATE utf8_general_ci;

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

INSERT INTO productos (nombre, variedad) VALUES
('Ajo', 'Blanco'),
('Berenjena', 'Larga'),
('Calabacin', 'Verde'),
('Cebolla', 'Blanca'),
('Cebolla', 'Roja'),
('Champiñon', 'Cultivado'),
('Lechuga', 'Romana'),
('Lechuga', 'Iceberg'),
('Maiz', 'Dulce'),
('Patata', 'Agria');

INSERT INTO fuentes (nombre) VALUES
('Mercado Central'),
('Distribuidor Local'),
('Cooperativa Agricola'),
('Supermercado Regional'),
('Tienda de Verduras'),
('Productor Directo');

INSERT INTO precios (producto_id, fuente_id, precio) VALUES
(1, 1, 2.50), (1, 2, 2.60),
(2, 1, 0.90), (2, 2, 0.95), (2, 3, 1.00),
(3, 1, 0.65), (3, 2, 0.70), (3, 4, 0.75),
(4, 1, 0.30), (4, 2, 0.35), (4, 3, 0.28),
(5, 1, 0.40), (5, 2, 0.45), (5, 3, 0.38),
(6, 1, 1.50), (6, 2, 1.60), (6, 3, 1.40),
(7, 1, 0.50), (7, 2, 0.55), (7, 3, 0.45),
(8, 1, 0.45), (8, 2, 0.50), (8, 3, 0.40),
(9, 1, 0.80), (9, 2, 0.85), (9, 3, 0.75),
(10, 1, 0.35), (10, 2, 0.40), (10, 3, 0.32),
(1, 3, 2.45), (1, 4, 2.70),
(2, 4, 1.05), (3, 3, 0.68),
(4, 4, 0.32), (5, 4, 0.48),
(6, 4, 1.55), (7, 4, 0.58),
(8, 4, 0.52), (9, 4, 0.88),
(10, 4, 0.38), (1, 5, 2.55),
(2, 5, 0.92), (3, 5, 0.72),
(4, 5, 0.33), (5, 5, 0.46),
(6, 5, 1.45), (7, 5, 0.52),
(8, 5, 0.48), (9, 5, 0.82),
(10, 5, 0.36), (1, 6, 2.40),
(2, 6, 0.88), (3, 6, 0.62),
(4, 6, 0.29), (5, 6, 0.42),
(6, 6, 1.35), (7, 6, 0.48),
(8, 6, 0.44), (9, 6, 0.78),
(10, 6, 0.34);

SELECT COUNT(*) as total_productos FROM productos;
SELECT COUNT(*) as total_fuentes FROM fuentes;
SELECT COUNT(*) as total_precios FROM precios;
