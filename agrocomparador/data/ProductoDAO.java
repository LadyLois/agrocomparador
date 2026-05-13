package agrocomparador.data;

import java.sql.*;
import java.util.*;

public class ProductoDAO {

    public static List<Map<String, Object>> obtenerProductos() {
        List<Map<String, Object>> productos = new ArrayList<>();
        Connection conn = null;

        try {
            conn = DatabaseConnection.getConnection();

            String sql = "SELECT p.nombre, p.variedad, f.nombre AS fuente, pr.precio, " +
                        "COALESCE(pr.origen, 'BD') AS origen, pr.fecha AS fecha_actualizacion " +
                        "FROM precios pr " +
                        "JOIN productos p ON pr.producto_id = p.id " +
                        "JOIN fuentes f ON pr.fuente_id = f.id " +
                        "WHERE COALESCE(pr.origen, 'BD') = 'BD' " +
                        "ORDER BY p.nombre, pr.precio";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Map<String, Object> producto = new HashMap<>();
                producto.put("nombre",  rs.getString("nombre"));
                producto.put("variedad", rs.getString("variedad"));
                producto.put("fuente",  rs.getString("fuente"));
                producto.put("precio",  rs.getDouble("precio"));
                producto.put("origen",  rs.getString("origen"));
                java.sql.Timestamp ts = rs.getTimestamp("fecha_actualizacion");
                producto.put("fecha_actualizacion",
                    ts != null ? ts.toString().substring(0, Math.min(19, ts.toString().length())) : "");
                productos.add(producto);
            }

            rs.close();
            stmt.close();

        } catch (SQLException e) {
            System.err.println("Error al obtener productos: " + e.getMessage());
        } finally {
            DatabaseConnection.closeConnection(conn);
        }

        return productos;
    }
}
