package agrocomparador.data;

import java.sql.*;
import java.util.*;

/**
 * Obtiene datos de precios desde la tabla de caché del scraper
 * Similar a ProductoDAO pero accede a datos de scraping almacenados en BD
 */
public class ProductoDAOScraper {
    
    /**
     * Obtiene todos los productos scrapedos almacenados en la BD
     */
    public static List<Map<String, String>> obtenerProductosDelScraper() {
        List<Map<String, String>> productos = new ArrayList<>();
        Connection conn = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            
            // Muestra solo los datos del día más reciente disponible por origen
            String sql = "SELECT p.nombre, p.variedad, p.fuente, p.precio, p.origen, p.fecha_actualizacion " +
                        "FROM precios_scraper p " +
                        "WHERE DATE(p.fecha_actualizacion) = (" +
                        "  SELECT MAX(DATE(p2.fecha_actualizacion)) FROM precios_scraper p2 WHERE p2.origen = p.origen" +
                        ") " +
                        "ORDER BY p.nombre, p.precio " +
                        "LIMIT 1000";
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                while (rs.next()) {
                    Map<String, String> producto = new HashMap<>();
                    producto.put("nombre",             safe(rs.getString("nombre")));
                    producto.put("variedad",           safe(rs.getString("variedad")));
                    producto.put("fuente",             safe(rs.getString("fuente")));
                    producto.put("precio",             String.valueOf(rs.getDouble("precio")));
                    producto.put("origen",             safe(rs.getString("origen")));
                    producto.put("fecha_actualizacion", safe(rs.getString("fecha_actualizacion")));
                    
                    productos.add(producto);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error obteniendo datos del scraper: " + e.getMessage());
        } finally {
            if (conn != null) {
                DatabaseConnection.closeConnection(conn);
            }
        }
        
        return productos;
    }
    
    public static List<String> obtenerFechasDisponibles() {
        List<String> fechas = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            String sql = "SELECT DISTINCT DATE(fecha_actualizacion) AS fecha FROM precios_scraper ORDER BY fecha DESC";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    String f = rs.getString("fecha");
                    if (f != null) fechas.add(f);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error obteniendo fechas disponibles: " + e.getMessage());
        } finally {
            if (conn != null) DatabaseConnection.closeConnection(conn);
        }
        return fechas;
    }

    public static List<Map<String, String>> obtenerProductosPorFecha(String fecha) {
        List<Map<String, String>> productos = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            String sql = "SELECT p.nombre, p.variedad, p.fuente, p.precio, p.origen, p.fecha_actualizacion " +
                        "FROM precios_scraper p " +
                        "WHERE DATE(p.fecha_actualizacion) = ? " +
                        "ORDER BY p.nombre, p.precio " +
                        "LIMIT 1000";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, fecha);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Map<String, String> producto = new HashMap<>();
                        producto.put("nombre",              safe(rs.getString("nombre")));
                        producto.put("variedad",            safe(rs.getString("variedad")));
                        producto.put("fuente",              safe(rs.getString("fuente")));
                        producto.put("precio",              String.valueOf(rs.getDouble("precio")));
                        producto.put("origen",              safe(rs.getString("origen")));
                        producto.put("fecha_actualizacion", safe(rs.getString("fecha_actualizacion")));
                        productos.add(producto);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error obteniendo productos por fecha: " + e.getMessage());
        } finally {
            if (conn != null) DatabaseConnection.closeConnection(conn);
        }
        return productos;
    }

    public static void vaciarDatos() {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            try (Statement stmt = conn.createStatement()) {
                int eliminados = stmt.executeUpdate("DELETE FROM precios_scraper");
                System.out.println("🧹 Vaciados " + eliminados + " registros de precios_scraper");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error al vaciar datos: " + e.getMessage());
        } finally {
            if (conn != null) DatabaseConnection.closeConnection(conn);
        }
    }

    /**
     * Obtiene precio mínimo de un producto del scraper
     */
    public static double obtenerPrecioMinimoDeScraper(String nombreProducto) {
        Connection conn = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            
            String sql = "SELECT MIN(precio) as precio_min " +
                        "FROM precios_scraper " +
                        "WHERE LOWER(nombre) LIKE LOWER(?) AND precio > 0";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, "%" + nombreProducto + "%");
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        double precio = rs.getDouble("precio_min");
                        return precio > 0 ? precio : 0;
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error obteniendo precio mínimo del scraper: " + e.getMessage());
        } finally {
            if (conn != null) {
                DatabaseConnection.closeConnection(conn);
            }
        }
        
        return 0;
    }
    
    /**
     * Obtiene la fuente más barata para un producto del scraper
     */
    public static String obtenerFuenteBarataDeScraper(String nombreProducto) {
        Connection conn = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            
            String sql = "SELECT fuente " +
                        "FROM precios_scraper " +
                        "WHERE LOWER(nombre) LIKE LOWER(?) AND precio > 0 " +
                        "ORDER BY precio ASC LIMIT 1";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, "%" + nombreProducto + "%");
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("fuente");
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error obteniendo fuente barata del scraper: " + e.getMessage());
        } finally {
            if (conn != null) {
                DatabaseConnection.closeConnection(conn);
            }
        }
        
        return "No disponible";
    }
    
    /**
     * Obtiene estadísticas del scraper
     */
    private static String safe(String s) { return s != null ? s : ""; }

    public static Map<String, String> obtenerEstadisticasScraper() {
        Map<String, String> stats = new HashMap<>();
        Connection conn = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            
            // Contar registros totales
            String sql1 = "SELECT COUNT(*) as total FROM precios_scraper";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql1)) {
                if (rs.next()) {
                    stats.put("total_registros", String.valueOf(rs.getInt("total")));
                }
            }
            
            // Productos únicos
            String sql2 = "SELECT COUNT(DISTINCT nombre) as unicos FROM precios_scraper";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql2)) {
                if (rs.next()) {
                    stats.put("productos_unicos", String.valueOf(rs.getInt("unicos")));
                }
            }
            
            // Fecha última actualización
            String sql3 = "SELECT MAX(fecha_actualizacion) as ultima FROM precios_scraper";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql3)) {
                if (rs.next()) {
                    stats.put("ultima_actualizacion", rs.getString("ultima"));
                }
            }
            
            // Precio promedio
            String sql4 = "SELECT AVG(precio) as promedio FROM precios_scraper WHERE precio > 0";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql4)) {
                if (rs.next()) {
                    stats.put("precio_promedio", String.format("%.2f", rs.getDouble("promedio")));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error obteniendo estadísticas: " + e.getMessage());
        } finally {
            if (conn != null) {
                DatabaseConnection.closeConnection(conn);
            }
        }
        
        return stats;
    }
}
