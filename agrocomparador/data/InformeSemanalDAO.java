package agrocomparador.data;

import java.sql.*;
import java.util.*;
import java.util.zip.*;

public class InformeSemanalDAO {

    private static final String EXCELS_DIR = "agrocomparador/excels/";
    private static final Set<String> SECTION_WORDS = new HashSet<>(Arrays.asList(
        "CEREALES", "ARROZ", "SEMILLAS", "TORTAS", "PROTEICOS", "VINOS", "ACEITES",
        "HORTALIZAS", "FRUTAS", "CÍTRICOS", "CITRICOS", "GANADO", "LECHE", "HUEVOS",
        "PATATAS", "PRODUCTOS", "FLORES", "LEGUMINOSAS", "TUBERCULOS", "AGRIOS",
        "PORCINO", "VACUNO", "OVINO", "CAPRINO", "AVES", "CONEJOS", "LANA",
        "ESPECIFICACIONES", "PRECIOS MEDIOS", "VARIACI"
    ));

    static {
        try {
            crearTabla();
            if (estaVacia()) {
                System.out.println("Importando informes semanales desde Excel...");
                importarTodosLosInformes();
                System.out.println("Importacion semanal completada.");
            }
        } catch (Exception e) {
            System.err.println("Error inicializando InformeSemanalDAO: " + e.getMessage());
        }
    }

    private static void crearTabla() throws Exception {
        String sql = "CREATE TABLE IF NOT EXISTS precios_semanales (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "producto VARCHAR(500), semana VARCHAR(20), " +
            "precio_anterior DECIMAL(10,4), precio_actual DECIMAL(10,4), " +
            "variacion_euros DECIMAL(10,4), variacion_pct DECIMAL(10,4), " +
            "INDEX idx_prod_sem (producto(100)), INDEX idx_semana (semana))";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    private static boolean estaVacia() throws Exception {
        String sql = "SELECT COUNT(*) FROM precios_semanales";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() && rs.getInt(1) == 0;
        }
    }

    public static void importarTodosLosInformes() {
        for (int i = 1; i <= 13; i++) {
            String semana = String.format("S-%02d", i);
            String path = EXCELS_DIR + "Informe Semanal de Coyuntura " + semana + ".xlsx";
            try {
                importarInforme(path, semana);
                System.out.println("  Importado: " + semana);
            } catch (Exception e) {
                System.err.println("  Error en " + semana + ": " + e.getMessage());
            }
        }
    }

    public static void importarInforme(String path, String semana) throws Exception {
        try (ZipFile zip = new ZipFile(path)) {
            List<String> sharedStrings = XlsxReader.readSharedStrings(zip);
            // Sheet 2 (Pág. 4): cereales, oleaginosas, vinos, aceites
            // Sheet 3 (Pág. 5): frutas y hortalizas (pimientos, tomates, berenjenas, etc.)
            List<Map<Integer, Map<Integer, String>>> sheets = new ArrayList<>();
            sheets.add(XlsxReader.readSheet(zip, "xl/worksheets/sheet2.xml", sharedStrings));
            sheets.add(XlsxReader.readSheet(zip, "xl/worksheets/sheet3.xml", sharedStrings));

            String sql = "INSERT INTO precios_semanales (producto, semana, precio_anterior, precio_actual, variacion_euros, variacion_pct) VALUES (?, ?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                for (Map<Integer, Map<Integer, String>> sheet : sheets) {
                    for (Map.Entry<Integer, Map<Integer, String>> entry : sheet.entrySet()) {
                        int rowNum = entry.getKey();
                        if (rowNum < 8) continue; // saltar cabeceras (sheet3 empieza en fila 8)

                        Map<Integer, String> row = entry.getValue();
                        String producto = row.getOrDefault(2, "").trim(); // col C

                        if (producto.isEmpty()) continue;
                        if (esEncabezado(producto)) continue;

                        try {
                            double precioAnterior = parseDouble(row.getOrDefault(3, "0")); // col D
                            double precioActual   = parseDouble(row.getOrDefault(4, "0")); // col E
                            double varEuros       = parseDouble(row.getOrDefault(5, "0")); // col F
                            double varPct         = parseDouble(row.getOrDefault(6, "0")); // col G

                            if (precioActual <= 0) continue;

                            ps.setString(1, producto);
                            ps.setString(2, semana);
                            ps.setDouble(3, precioAnterior);
                            ps.setDouble(4, precioActual);
                            ps.setDouble(5, varEuros);
                            ps.setDouble(6, varPct);
                            ps.addBatch();
                        } catch (NumberFormatException ignored) {}
                    }
                }
                ps.executeBatch();
            }
        }
    }

    private static boolean esEncabezado(String texto) {
        String upper = texto.toUpperCase();
        for (String word : SECTION_WORDS) {
            if (upper.contains(word)) return true;
        }
        // También son encabezados las líneas totalmente en mayúsculas sin paréntesis
        return upper.equals(texto) && !texto.contains("(") && !texto.contains("/");
    }

    private static double parseDouble(String s) {
        if (s == null || s.isEmpty()) return 0;
        return Double.parseDouble(s.replace(",", ".").trim());
    }

    public static List<Map<String, Object>> obtenerHistoricoSemanal(String producto) {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = "SELECT producto, semana, precio_actual AS precio " +
                     "FROM precios_semanales WHERE producto LIKE ? ORDER BY semana";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + producto + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("producto", rs.getString("producto"));
                row.put("semana", rs.getString("semana"));
                row.put("precio", rs.getDouble("precio"));
                result.add(row);
            }
        } catch (Exception e) {
            System.err.println("Error obteniendo historico semanal: " + e.getMessage());
        }
        return result;
    }

    public static List<Map<String, Object>> obtenerTendencias() {
        List<Map<String, Object>> result = new ArrayList<>();
        String maxSemana = "";
        String prevSemana = "";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT DISTINCT semana FROM precios_semanales ORDER BY semana DESC LIMIT 2")) {
            if (rs.next()) maxSemana = rs.getString("semana");
            if (rs.next()) prevSemana = rs.getString("semana");
        } catch (Exception e) {
            System.err.println("Error obteniendo semanas: " + e.getMessage());
            return result;
        }

        if (maxSemana.isEmpty()) return result;

        final String semActual = maxSemana;
        final String semAnterior = prevSemana.isEmpty() ? maxSemana : prevSemana;

        String sql = "SELECT producto, precio_actual AS actual, variacion_pct AS variacion " +
                     "FROM precios_semanales WHERE semana = ? ORDER BY producto LIMIT 25";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maxSemana);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("producto", rs.getString("producto"));
                row.put("actual", rs.getDouble("actual"));
                row.put("variacion", rs.getDouble("variacion"));
                row.put("semanaActual", semActual);
                row.put("semanaAnterior", semAnterior);
                result.add(row);
            }
        } catch (Exception e) {
            System.err.println("Error obteniendo tendencias: " + e.getMessage());
        }
        return result;
    }

    public static List<Map<String, Object>> obtenerComparativaProductos() {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = "SELECT producto, AVG(precio_actual) AS promedio " +
                     "FROM precios_semanales GROUP BY producto ORDER BY producto";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("producto", rs.getString("producto"));
                row.put("promedio", rs.getDouble("promedio"));
                result.add(row);
            }
        } catch (Exception e) {
            System.err.println("Error obteniendo comparativa: " + e.getMessage());
        }
        return result;
    }

    public static List<String> obtenerSemanas() {
        List<String> result = new ArrayList<>();
        String sql = "SELECT DISTINCT semana FROM precios_semanales ORDER BY semana";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) result.add(rs.getString("semana"));
        } catch (Exception e) {
            System.err.println("Error obteniendo semanas: " + e.getMessage());
        }
        return result;
    }

    public static Map<String, Double> obtenerPreciosPorSemana(String productoExacto) {
        Map<String, Double> result = new TreeMap<>();
        String sql = "SELECT semana, precio_actual FROM precios_semanales WHERE producto = ? ORDER BY semana";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productoExacto);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) result.put(rs.getString("semana"), rs.getDouble("precio_actual"));
        } catch (Exception e) {
            System.err.println("Error obteniendo evolución semanal: " + e.getMessage());
        }
        return result;
    }

    public static List<String> obtenerProductos() {
        List<String> result = new ArrayList<>();
        String sql = "SELECT DISTINCT producto FROM precios_semanales ORDER BY producto";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) result.add(rs.getString("producto"));
        } catch (Exception e) {
            System.err.println("Error obteniendo productos semanales: " + e.getMessage());
        }
        return result;
    }
}
