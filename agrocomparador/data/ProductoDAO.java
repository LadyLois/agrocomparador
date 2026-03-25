package agrocomparador.data;

import java.sql.*;
import java.util.*;

/**
 * Data Access Object para productos y precios
 * Recupera información de la base de datos
 */
public class ProductoDAO {
    
    /**
     * Obtiene todos los productos con sus precios de diferentes fuentes
     * @return Lista de mapas con información de productos
     */
    public static List<Map<String, Object>> obtenerProductos() {
        List<Map<String, Object>> productos = new ArrayList<>();
        Connection conn = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            
            String sql = "SELECT p.nombre, p.variedad, f.nombre AS fuente, pr.precio " +
                        "FROM precios pr " +
                        "JOIN productos p ON pr.producto_id = p.id " +
                        "JOIN fuentes f ON pr.fuente_id = f.id " +
                        "ORDER BY p.nombre, pr.precio";
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                Map<String, Object> producto = new HashMap<>();
                producto.put("nombre", rs.getString("nombre"));
                producto.put("variedad", rs.getString("variedad"));
                producto.put("fuente", rs.getString("fuente"));
                producto.put("precio", rs.getDouble("precio"));
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
