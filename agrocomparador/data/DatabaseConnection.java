package agrocomparador.data;

import java.sql.*;

/**
 * Gestiona la conexión a la base de datos MySQL
 * IMPORTANTE: Las credenciales SE DEBEN pasar por variables de entorno
 * 
 * Variables requeridas:
 * - DB_HOST: Host de la base de datos (requerido)
 * - DB_PORT: Puerto (defecto: 3306)
 * - DB_NAME: Nombre de base de datos (defecto: comparador)
 * - DB_USER: Usuario de BD (requerido)
 * - DB_PASSWORD: Contraseña de BD (requerido - NUNCA poner en código)
 * 
 * Ejemplo:
 * export DB_HOST=localhost
 * export DB_USER=admin
 * export DB_PASSWORD=tu_password_segura
 * java -cp ".;mysql-connector-java-9.0.0.jar" agrocomparador
 */
public class DatabaseConnection {
    // Variables de entorno REQUERIDAS (sin valores por defecto por seguridad)
    private static final String DB_HOST = getRequiredEnv("DB_HOST");
    private static final String DB_USER = getRequiredEnv("DB_USER");
    private static final String DB_PASSWORD = getRequiredEnv("DB_PASSWORD");
    
    // Variables con valores por defecto (no sensibles)
    private static final String DB_NAME = getEnv("DB_NAME", "comparador");
    private static final String DB_PORT = getEnv("DB_PORT", "3306");
    
    private static final String URL = String.format(
        "jdbc:mysql://%s:%s/%s?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC",
        DB_HOST, DB_PORT, DB_NAME
    );
    
    /**
     * Obtiene variable de entorno REQUERIDA
     * Lanza excepción si no está definida
     */
    private static String getRequiredEnv(String key) {
        String value = System.getenv(key);
        if (value == null || value.trim().isEmpty()) {
            throw new RuntimeException(
                "❌ VARIABLE DE ENTORNO REQUERIDA NO ENCONTRADA: " + key + "\n" +
                "   Las siguientes variables son obligatorias:\n" +
                "   - DB_HOST (ej: localhost o agrocomparador.xxxxx.rds.amazonaws.com)\n" +
                "   - DB_USER (ej: admin)\n" +
                "   - DB_PASSWORD (NUNCA dejar vacío o en el código)\n" +
                "\n   Ejemplo:\n" +
                "   export DB_HOST=localhost\n" +
                "   export DB_USER=admin\n" +
                "   export DB_PASSWORD=tu_password_segura\n" +
                "   java -cp \".;mysql-connector-java-9.0.0.jar\" agrocomparador"
            );
        }
        return value;
    }
    
    /**
     * Obtiene variable de entorno con valor por defecto (para valores no sensibles)
     */
    private static String getEnv(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        return value;
    }
    
    /**
     * Obtiene una conexión a la base de datos
     * @return Connection a la base de datos
     * @throws SQLException si hay error de conexión
     */
    public static Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(URL, DB_USER, DB_PASSWORD);
        } catch (SQLException e) {
            System.err.println("❌ Error de conexión a BD: " + e.getMessage());
            System.err.println("   Verifica que las variables de entorno sean correctas:");
            System.err.println("   - DB_HOST: " + DB_HOST);
            System.err.println("   - DB_PORT: " + DB_PORT);
            System.err.println("   - DB_NAME: " + DB_NAME);
            System.err.println("   - DB_USER: " + DB_USER);
            System.err.println("   (DB_PASSWORD no se imprime por seguridad)");
            throw e;
        }
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
