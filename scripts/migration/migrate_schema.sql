-- Migración: tablas normalizadas para datos del scraper
-- Ejecutar en producción: mysql -u USER -p DBNAME < migrate_schema.sql
-- Es idempotente: no falla si las tablas ya existen

CREATE TABLE IF NOT EXISTS productos (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    nombre     VARCHAR(255) NOT NULL,
    variedad   VARCHAR(255) DEFAULT NULL,
    created_at DATETIME    DEFAULT NOW(),
    UNIQUE KEY uq_nombre_variedad (nombre, variedad)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS fuentes (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    nombre     VARCHAR(255) NOT NULL,
    created_at DATETIME    DEFAULT NOW(),
    UNIQUE KEY uq_nombre (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS precios (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    producto_id INT          NOT NULL,
    fuente_id   INT          NOT NULL,
    precio      DOUBLE       NOT NULL,
    fecha       DATETIME     DEFAULT NOW(),
    origen      VARCHAR(50)  NOT NULL DEFAULT 'SCRAPER',
    CONSTRAINT fk_precio_producto FOREIGN KEY (producto_id) REFERENCES productos(id),
    CONSTRAINT fk_precio_fuente   FOREIGN KEY (fuente_id)   REFERENCES fuentes(id),
    INDEX idx_origen_fecha (origen, fecha)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
