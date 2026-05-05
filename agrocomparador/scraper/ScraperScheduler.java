package agrocomparador.scraper;

import agrocomparador.data.DatabaseConnection;
import java.sql.*;
import java.util.*;

// Fix #10: eliminado import muerto de ProductoDAO
public class ScraperScheduler implements Runnable {

    private static ScraperScheduler instancia;
    private static final int INTERVALO_MINUTOS = 60;
    // Fix #8: volatile garantiza visibilidad del flag entre threads
    private volatile boolean activo = true;
    private Thread thread;

    private ScraperScheduler() {}

    public static synchronized ScraperScheduler getInstance() {
        if (instancia == null) {
            instancia = new ScraperScheduler();
        }
        return instancia;
    }

    public void iniciar() {
        if (thread == null || !thread.isAlive()) {
            // Fix #9: crear tabla una sola vez al arrancar, no en cada ciclo
            inicializarTabla();
            thread = new Thread(this, "ScraperScheduler");
            thread.setDaemon(true);
            thread.start();
            System.out.println("🕐 Scheduler de scraper iniciado (cada " + INTERVALO_MINUTOS + " minutos)");
        }
    }

    public void detener() {
        activo = false;
        if (thread != null) {
            thread.interrupt(); // despierta el sleep para que pare inmediatamente
        }
        System.out.println("🛑 Deteniendo scheduler de scraper...");
    }

    @Override
    public void run() {
        while (activo) {
            try {
                actualizarDatos();

                long tiempoEspera = INTERVALO_MINUTOS * 60 * 1000L;
                System.out.println("⏳ Próxima actualización en " + INTERVALO_MINUTOS + " minutos");
                Thread.sleep(tiempoEspera);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("❌ Error en scheduler: " + e.getMessage());
                try {
                    Thread.sleep(5 * 60 * 1000L);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    private void actualizarDatos() {
        System.out.println("\n📥 Iniciando actualización de datos desde AgroPrecios.com...");

        try {
            List<Map<String, String>> productos = AgrePreciosScraperDAO.obtenerProductosDesdeScraper();

            if (productos.isEmpty()) {
                System.out.println("⚠️ No hay datos para actualizar");
                return;
            }

            guardarEnBaseDatos(productos);
            System.out.println("✓ Actualización completada: " + productos.size() + " registros guardados");

        } catch (Exception e) {
            System.err.println("❌ Error actualizando datos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Fix #9: llamado una sola vez desde iniciar(), separado de guardarEnBaseDatos()
    private void inicializarTabla() {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            crearTablaSiNoExiste(conn);
        } catch (SQLException e) {
            System.err.println("❌ Error al inicializar tabla del scraper: " + e.getMessage());
        } finally {
            if (conn != null) DatabaseConnection.closeConnection(conn);
        }
    }

    private void guardarEnBaseDatos(List<Map<String, String>> productos) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            // Fix #2: transacción para garantizar atomicidad de borrado + inserción
            conn.setAutoCommit(false);

            try {
                limpiarDatosAntiguos(conn);

                String sql = "INSERT INTO precios_scraper " +
                            "(nombre, variedad, fuente, precio, origen, fecha_actualizacion) " +
                            "VALUES (?, ?, ?, ?, ?, NOW())";

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    int insertados = 0;
                    for (Map<String, String> p : productos) {
                        stmt.setString(1, p.getOrDefault("nombre", ""));
                        stmt.setString(2, p.getOrDefault("variedad", ""));
                        stmt.setString(3, p.getOrDefault("fuente", "AgroPrecios"));
                        // Fix #11: setDouble en lugar de setString para columna DECIMAL
                        double precio;
                        try {
                            precio = Double.parseDouble(p.getOrDefault("precio", "0"));
                        } catch (NumberFormatException e) {
                            precio = 0.0;
                        }
                        stmt.setDouble(4, precio);
                        stmt.setString(5, "SCRAPER");

                        stmt.addBatch();

                        if (++insertados % 100 == 0) {
                            stmt.executeBatch();
                            System.out.println("   → Insertados " + insertados + " registros...");
                        }
                    }
                    stmt.executeBatch(); // flush del lote final
                }

                conn.commit();
                System.out.println("   ✓ " + productos.size() + " registros guardados en BD");

            } catch (SQLException e) {
                // Fix #2: rollback si falla el insert, los datos antiguos no se pierden
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error en base de datos: " + e.getMessage());
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
                DatabaseConnection.closeConnection(conn);
            }
        }
    }

    private void crearTablaSiNoExiste(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS precios_scraper (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "nombre VARCHAR(255) NOT NULL, " +
                    "variedad VARCHAR(255), " +
                    "fuente VARCHAR(255), " +
                    "precio DECIMAL(10, 2), " +
                    "origen VARCHAR(50), " +
                    "fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "INDEX idx_nombre (nombre), " +
                    "INDEX idx_fecha (fecha_actualizacion)" +
                    ")";

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("   ✓ Tabla precios_scraper lista");
        }
    }

    private void limpiarDatosAntiguos(Connection conn) throws SQLException {
        String sql = "DELETE FROM precios_scraper WHERE fecha_actualizacion < DATE_SUB(NOW(), INTERVAL 7 DAY)";

        try (Statement stmt = conn.createStatement()) {
            int eliminados = stmt.executeUpdate(sql);
            if (eliminados > 0) {
                System.out.println("   🧹 Eliminados " + eliminados + " registros antiguos");
            }
        }
    }
}
