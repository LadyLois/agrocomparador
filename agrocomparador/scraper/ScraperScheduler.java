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
            thread = new Thread(this, "ScraperScheduler");
            thread.setDaemon(true);
            thread.start();
            System.out.println("🕐 Scheduler de scraper iniciado (cada " + INTERVALO_MINUTOS + " minutos)");
        }
    }

    public void detener() {
        activo = false;
        if (thread != null) thread.interrupt();
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
                try { Thread.sleep(5 * 60 * 1000L); }
                catch (InterruptedException ie) { Thread.currentThread().interrupt(); break; }
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
        System.out.println("✓ " + nombre + ": " + productos.size() + " registros procesados");
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

                int insertados = 0;
                for (Map<String, String> p : productos) {
                    String nombre   = p.getOrDefault("nombre", "").trim();
                    String variedad = p.getOrDefault("variedad", "").trim();
                    String fuente   = p.getOrDefault("fuente", origen).trim();
                    double precio;
                    try { precio = Double.parseDouble(p.getOrDefault("precio", "0")); }
                    catch (NumberFormatException e) { precio = 0.0; }

                    if (nombre.isEmpty() || precio <= 0) continue;

                    int productoId = obtenerOCrearProducto(conn, nombre, variedad);
                    int fuenteId   = obtenerOCrearFuente(conn, fuente);
                    insertarPrecio(conn, productoId, fuenteId, precio, origen);
                    insertados++;
                }

                conn.commit();
                System.out.println("   ✓ " + insertados + " registros de " + origen +
                    " guardados en BD (" + java.time.LocalDate.now() + ")");

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
        String sql = "SELECT COUNT(*) FROM precios WHERE origen = ? AND DATE(fecha) = CURDATE()";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, origen);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private int obtenerOCrearProducto(Connection conn, String nombre, String variedad) throws SQLException {
        String varBuscar = variedad.isEmpty() ? null : variedad;
        String sql = "SELECT id FROM productos WHERE nombre = ? AND " +
                    (varBuscar == null ? "variedad IS NULL" : "variedad = ?");
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nombre);
            if (varBuscar != null) stmt.setString(2, varBuscar);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        }
        String ins = "INSERT INTO productos (nombre, variedad, created_at) VALUES (?, ?, NOW())";
        try (PreparedStatement stmt = conn.prepareStatement(ins, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, nombre);
            stmt.setString(2, varBuscar);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("No se pudo crear el producto: " + nombre);
    }

    private int obtenerOCrearFuente(Connection conn, String nombre) throws SQLException {
        String sql = "SELECT id FROM fuentes WHERE nombre = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nombre);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        }
        // INSERT IGNORE por si hay condición de carrera entre hilos
        String ins = "INSERT IGNORE INTO fuentes (nombre, created_at) VALUES (?, NOW())";
        try (PreparedStatement stmt = conn.prepareStatement(ins)) {
            stmt.setString(1, nombre);
            stmt.executeUpdate();
        }
        // Volver a leer el id (puede que ya existiera por la condición de carrera)
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nombre);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        }
        throw new SQLException("No se pudo crear la fuente: " + nombre);
    }

    private void insertarPrecio(Connection conn, int productoId, int fuenteId,
                                double precio, String origen) throws SQLException {
        String sql = "INSERT INTO precios (producto_id, fuente_id, precio, fecha, origen) " +
                    "VALUES (?, ?, ?, NOW(), ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productoId);
            stmt.setInt(2, fuenteId);
            stmt.setDouble(3, precio);
            stmt.setString(4, origen);
            stmt.executeUpdate();
        }
    }
}
