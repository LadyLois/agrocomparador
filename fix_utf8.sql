USE comparador;
SET NAMES utf8;
SET CHARACTER SET utf8;
SET FOREIGN_KEY_CHECKS=0;
ALTER DATABASE comparador CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE productos CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE fuentes CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE precios CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
TRUNCATE TABLE precios;
TRUNCATE TABLE productos;
TRUNCATE TABLE fuentes;
SET FOREIGN_KEY_CHECKS=1;
INSERT INTO productos (nombre, variedad) VALUES
('Ajo', 'Blanco'),
('Berenjena', 'Larga'),
('Calabacín', 'Verde'),
('Cebolla', 'Blanca'),
('Cebolla', 'Roja'),
('Champiñón', 'Cultivado'),
('Lechuga', 'Romana'),
('Lechuga', 'Iceberg'),
('Maíz', 'Dulce'),
('Patata', 'Agria');
INSERT INTO fuentes (nombre) VALUES
('Mercado Central'),
('Distribuidor Local'),
('Cooperativa Agrícola'),
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
SELECT * FROM productos;
