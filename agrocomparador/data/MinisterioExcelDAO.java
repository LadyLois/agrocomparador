package agrocomparador.data;

import java.sql.*;
import java.util.*;
import java.util.zip.*;

public class MinisterioExcelDAO {

    private static final String EXCEL_PATH = "agrocomparador/excels/Indices y Precios Percibidos Agrarios (enero 2024-enero 2026).xlsx";
    private static final String[] MESES = {
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiem.", "Octubre", "Noviem.", "Diciem.", "Anual"
    };

    static {
        try {
            crearTabla();
            if (estaVacia()) {
                System.out.println("Importando datos del Ministerio desde Excel...");
                importarDatos(EXCEL_PATH);
                System.out.println("Importacion del Ministerio completada.");
            }
        } catch (Exception e) {
            System.err.println("Error inicializando MinisterioExcelDAO: " + e.getMessage());
        }
    }

    private static void crearTabla() throws Exception {
        String sql = "CREATE TABLE IF NOT EXISTS precios_ministerio (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "producto VARCHAR(500), anio INT, mes VARCHAR(50), precio DECIMAL(10,4), " +
            "INDEX idx_prod_min (producto(100)))";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    private static boolean estaVacia() throws Exception {
        String sql = "SELECT COUNT(*) FROM precios_ministerio";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() && rs.getInt(1) == 0;
        }
    }

    public static void leerExcel(String path) {
        try {
            importarDatos(path);
        } catch (Exception e) {
            System.err.println("Error leyendo Excel del ministerio: " + e.getMessage());
        }
    }

    private static void importarDatos(String path) throws Exception {
        try (ZipFile zip = new ZipFile(path)) {
            List<String> sharedStrings = XlsxReader.readSharedStrings(zip);
            // Sheets 2-6 correspond to PrePer1-PrePer6 (precios percibidos por producto)
            for (int s = 2; s <= 6; s++) {
                Map<Integer, Map<Integer, String>> sheet = XlsxReader.readSheet(zip, "xl/worksheets/sheet" + s + ".xml", sharedStrings);
                importarHoja(sheet);
            }
        }
    }

    private static void importarHoja(Map<Integer, Map<Integer, String>> sheet) throws Exception {
        String sql = "INSERT INTO precios_ministerio (producto, anio, mes, precio) VALUES (?, ?, ?, ?)";
        String lastProducto = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (Map.Entry<Integer, Map<Integer, String>> entry : sheet.entrySet()) {
                Map<Integer, String> row = entry.getValue();

                // Col A (0): product name, Col B (1): year, Cols C-O (2-14): monthly prices
                String producto = row.getOrDefault(0, "");
                String anioStr = row.getOrDefault(1, "");

                if (!producto.isEmpty() && !producto.matches("[0-9.,]+")) {
                    lastProducto = producto;
                }

                if (lastProducto == null || anioStr.isEmpty()) continue;

                try {
                    int anio = (int) Double.parseDouble(anioStr);
                    if (anio < 2000 || anio > 2030) continue;

                    for (int m = 0; m < MESES.length; m++) {
                        String precioStr = row.getOrDefault(m + 2, "");
                        if (precioStr.isEmpty()) continue;
                        try {
                            double precio = Double.parseDouble(precioStr.replace(",", "."));
                            if (precio <= 0) continue;
                            ps.setString(1, lastProducto.trim());
                            ps.setInt(2, anio);
                            ps.setString(3, MESES[m]);
                            ps.setDouble(4, precio);
                            ps.addBatch();
                        } catch (NumberFormatException ignored) {}
                    }
                } catch (NumberFormatException ignored) {}
            }
            ps.executeBatch();
        }
    }

    public static List<Map<String, Object>> obtenerHistoricoProducto(String producto) {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = "SELECT producto, anio, mes, precio FROM precios_ministerio " +
                     "WHERE producto LIKE ? " +
                     "ORDER BY anio, FIELD(mes,'Enero','Febrero','Marzo','Abril','Mayo','Junio'," +
                     "'Julio','Agosto','Septiem.','Octubre','Noviem.','Diciem.','Anual')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + producto + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("producto", rs.getString("producto"));
                row.put("anio", rs.getInt("anio"));
                row.put("mes", rs.getString("mes"));
                row.put("precio", rs.getDouble("precio"));
                result.add(row);
            }
        } catch (Exception e) {
            System.err.println("Error obteniendo historico ministerio: " + e.getMessage());
        }
        return result;
    }

    public static List<String> obtenerProductos() {
        List<String> result = new ArrayList<>();
        String sql = "SELECT DISTINCT producto FROM precios_ministerio ORDER BY producto";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) result.add(rs.getString("producto"));
        } catch (Exception e) {
            System.err.println("Error obteniendo productos ministerio: " + e.getMessage());
        }
        return result;
    }
}
