package agrocomparador.data;

import java.sql.*;

/**
 * Gestiona la conexión a la base de datos MySQL
 */
public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/comparador";
    private static final String USER = "admin";
    private static final String PASSWORD = "AgroComparador2026!";
    
    /**
     * Obtiene una conexión a la base de datos
     * @return Connection a la base de datos
     * @throws SQLException si hay error de conexión
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
    
    /**
     * Cierra una conexión
     * @param conn Conexión a cerrar
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar conexión: " + e.getMessage());
            }
        }
    }
}
