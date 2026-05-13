package agrocomparador.scraper;

import agrocomparador.data.DatabaseConnection;
import java.sql.*;
import java.util.*;

public class ScraperScheduler implements Runnable {

    private static ScraperScheduler instancia;
    private static final int INTERVALO_MINUTOS = 60;
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
            thread.interrupt();
        }
        System.out.println("🛑 Deteniendo scheduler de scraper...");
    }

    public void forzarActualizacionAsincrona() {
        Thread t = new Thread(() -> {
            try {
                actualizarDatos();
            } catch (Exception e) {
                System.err.println("❌ Error en carga forzada: " + e.getMessage());
            }
        }, "ScraperForzado");
        t.setDaemon(true);
        t.start();
        System.out.println("🚀 Carga de datos iniciada en segundo plano");
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
        System.out.println("\n📥 Iniciando actualización de datos desde las fuentes...");

        actualizarFuente("AgroPrecios.com", AgrePreciosScraperDAO.obtenerProductosDesdeScraper());
        actualizarFuente("AgroPizarra.com", AgroPizarraScraperDAO.obtenerProductosDesdeScraper());
    }

    private void actualizarFuente(String nombre, List<Map<String, String>> productos) {
        if (productos.isEmpty()) {
            System.out.println("⚠️ No hay datos de " + nombre);
            return;
        }
        guardarEnBaseDatos(productos);
        System.out.println("✓ " + nombre + ": " + productos.size() + " registros guardados");
    }

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
        if (productos.isEmpty()) return;

        String origen = productos.get(0).getOrDefault("origen", "SCRAPER");

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            try {
                if (tieneDatosDeHoy(conn, origen)) {
                    System.out.println("   ℹ️ Ya existen datos de " + origen + " para hoy, omitiendo inserción");
                    conn.setAutoCommit(true);
                    return;
                }

                String sql = "INSERT INTO precios_scraper " +
                            "(nombre, variedad, fuente, precio, origen, fecha_actualizacion) " +
                            "VALUES (?, ?, ?, ?, ?, NOW())";

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    int insertados = 0;
                    for (Map<String, String> p : productos) {
                        stmt.setString(1, p.getOrDefault("nombre", ""));
                        stmt.setString(2, p.getOrDefault("variedad", ""));
                        stmt.setString(3, p.getOrDefault("fuente", origen));
                        double precio;
                        try {
                            precio = Double.parseDouble(p.getOrDefault("precio", "0"));
                        } catch (NumberFormatException e) {
                            precio = 0.0;
                        }
                        stmt.setDouble(4, precio);
                        stmt.setString(5, origen);

                        stmt.addBatch();

                        if (++insertados % 100 == 0) {
                            stmt.executeBatch();
                            System.out.println("   → Insertados " + insertados + " registros...");
                        }
                    }
                    stmt.executeBatch();
                }

                conn.commit();
                System.out.println("   ✓ " + productos.size() + " registros de " + origen + " guardados en BD (" + java.time.LocalDate.now() + ")");

            } catch (SQLException e) {
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

    private boolean tieneDatosDeHoy(Connection conn, String origen) throws SQLException {
        String sql = "SELECT COUNT(*) FROM precios_scraper WHERE origen = ? AND DATE(fecha_actualizacion) = CURDATE()";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, origen);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
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
                    "INDEX idx_fecha (fecha_actualizacion), " +
                    "INDEX idx_origen (origen)" +
                    ")";

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("   ✓ Tabla precios_scraper lista");
        }
    }

}
