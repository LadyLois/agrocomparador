package agrocomparador.data;

import java.sql.*;
import java.util.*;

public class ProductoDAOScraper {

    // Datos scrapeados: todos los días acumulados, ordenados del más reciente al más antiguo
    public static List<Map<String, String>> obtenerProductosDelScraper() {
        List<Map<String, String>> productos = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            String sql = "SELECT p.nombre, p.variedad, f.nombre AS fuente, pr.precio, pr.origen, pr.fecha AS fecha_actualizacion " +
                        "FROM precios pr " +
                        "JOIN productos p ON pr.producto_id = p.id " +
                        "JOIN fuentes f ON pr.fuente_id = f.id " +
                        "WHERE pr.origen IN ('AGROPRECIOS','AGROPIZARRA') " +
                        "ORDER BY pr.fecha DESC, p.nombre, pr.precio " +
                        "LIMIT 5000";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
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
        } catch (SQLException e) {
            System.err.println("❌ Error obteniendo datos del scraper: " + e.getMessage());
        } finally {
            if (conn != null) DatabaseConnection.closeConnection(conn);
        }
        return productos;
    }

    public static List<String> obtenerFechasDisponibles() {
        List<String> fechas = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            String sql = "SELECT DISTINCT DATE(fecha) AS fecha FROM precios " +
                        "WHERE origen IN ('AGROPRECIOS','AGROPIZARRA') ORDER BY fecha DESC";
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
            String sql = "SELECT p.nombre, p.variedad, f.nombre AS fuente, pr.precio, pr.origen, pr.fecha AS fecha_actualizacion " +
                        "FROM precios pr " +
                        "JOIN productos p ON pr.producto_id = p.id " +
                        "JOIN fuentes f ON pr.fuente_id = f.id " +
                        "WHERE pr.origen IN ('AGROPRECIOS','AGROPIZARRA') AND DATE(pr.fecha) = ? " +
                        "ORDER BY p.nombre, pr.precio LIMIT 1000";
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

    public static List<Map<String, String>> obtenerProductosPorRangoFechas(String desde, String hasta) {
        List<Map<String, String>> productos = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            String condFecha = "";
            if (!desde.isEmpty() && !hasta.isEmpty())
                condFecha = "AND DATE(pr.fecha) BETWEEN ? AND ? ";
            else if (!desde.isEmpty())
                condFecha = "AND DATE(pr.fecha) >= ? ";
            else if (!hasta.isEmpty())
                condFecha = "AND DATE(pr.fecha) <= ? ";

            String sql = "SELECT p.nombre, p.variedad, f.nombre AS fuente, pr.precio, pr.origen, pr.fecha AS fecha_actualizacion " +
                        "FROM precios pr " +
                        "JOIN productos p ON pr.producto_id = p.id " +
                        "JOIN fuentes f ON pr.fuente_id = f.id " +
                        "WHERE pr.origen IN ('AGROPRECIOS','AGROPIZARRA') " + condFecha +
                        "ORDER BY pr.fecha DESC, p.nombre, pr.precio LIMIT 10000";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                int i = 1;
                if (!desde.isEmpty()) stmt.setString(i++, desde);
                if (!hasta.isEmpty()) stmt.setString(i,   hasta);
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
            System.err.println("❌ Error obteniendo productos por rango: " + e.getMessage());
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
                int eliminados = stmt.executeUpdate(
                    "DELETE FROM precios WHERE origen IN ('AGROPRECIOS','AGROPIZARRA')");
                System.out.println("🧹 Vaciados " + eliminados + " registros del scraper");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error al vaciar datos: " + e.getMessage());
        } finally {
            if (conn != null) DatabaseConnection.closeConnection(conn);
        }
    }

    public static void vaciarDatosPorFecha(String fecha) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            String sql = "DELETE FROM precios WHERE origen IN ('AGROPRECIOS','AGROPIZARRA') AND DATE(fecha) = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, fecha);
                int eliminados = stmt.executeUpdate();
                System.out.println("🧹 Vaciados " + eliminados + " registros del scraper para " + fecha);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error al vaciar datos por fecha: " + e.getMessage());
        } finally {
            if (conn != null) DatabaseConnection.closeConnection(conn);
        }
    }

    public static double obtenerPrecioMinimoDeScraper(String nombreProducto) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            String sql = "SELECT MIN(pr.precio) AS precio_min FROM precios pr " +
                        "JOIN productos p ON pr.producto_id = p.id " +
                        "WHERE LOWER(p.nombre) LIKE LOWER(?) AND pr.precio > 0 " +
                        "  AND pr.origen IN ('AGROPRECIOS','AGROPIZARRA')";
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
            if (conn != null) DatabaseConnection.closeConnection(conn);
        }
        return 0;
    }

    public static Map<String, String> obtenerEstadisticasScraper() {
        Map<String, String> stats = new HashMap<>();
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();

            String filtro = "WHERE origen IN ('AGROPRECIOS','AGROPIZARRA')";

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS total FROM precios " + filtro)) {
                if (rs.next()) stats.put("total_registros", String.valueOf(rs.getInt("total")));
            }

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                     "SELECT COUNT(DISTINCT producto_id) AS unicos FROM precios " + filtro)) {
                if (rs.next()) stats.put("productos_unicos", String.valueOf(rs.getInt("unicos")));
            }

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT MAX(fecha) AS ultima FROM precios " + filtro)) {
                if (rs.next()) stats.put("ultima_actualizacion", rs.getString("ultima"));
            }

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                     "SELECT AVG(precio) AS promedio FROM precios " + filtro + " AND precio > 0")) {
                if (rs.next()) stats.put("precio_promedio", String.format("%.2f", rs.getDouble("promedio")));
            }

        } catch (SQLException e) {
            System.err.println("❌ Error obteniendo estadísticas: " + e.getMessage());
        } finally {
            if (conn != null) DatabaseConnection.closeConnection(conn);
        }
        return stats;
    }

    private static String safe(String s) { return s != null ? s : ""; }
}
