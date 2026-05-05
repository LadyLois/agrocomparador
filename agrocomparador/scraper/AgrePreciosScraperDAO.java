package agrocomparador.scraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.*;
import java.io.*;
import java.text.SimpleDateFormat;

public class AgrePreciosScraperDAO {
    private static final String BASE_URL = "https://www.agroprecios.com/es/precios-producto/";
    private static final int TIMEOUT = 15000;
    private static final int MAX_REINTENTOS = 3;
    private static final int DELAY_ENTRE_REINTENTOS = 2000;
    private static final String CACHE_FILE = "scraper_cache.txt";
    // Fix #5: formato estándar de timestamp para MySQL
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static List<Map<String, String>> obtenerProductosDesdeScraper() {
        List<Map<String, String>> productos = new ArrayList<>();

        for (int intento = 1; intento <= MAX_REINTENTOS; intento++) {
            try {
                System.out.println("🔄 Intento " + intento + "/" + MAX_REINTENTOS + " de scraping desde agroprecios.com...");
                productos = scrapearAgroPrecios();

                if (!productos.isEmpty()) {
                    guardarCacheLocal(productos);
                    System.out.println("✓ Scrapeados " + productos.size() + " productos de AgroPrecios.com");
                    return productos;
                }

            } catch (Exception e) {
                System.err.println("❌ Intento " + intento + " falló: " + e.getMessage());

                if (intento < MAX_REINTENTOS) {
                    try {
                        System.out.println("⏳ Esperando " + (DELAY_ENTRE_REINTENTOS / 1000) + "s antes de reintentar...");
                        Thread.sleep(DELAY_ENTRE_REINTENTOS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        System.out.println("⚠️ Todos los reintentos fallaron. Intentando usar caché local...");
        productos = cargarCacheLocal();

        if (!productos.isEmpty()) {
            System.out.println("✓ Se cargaron " + productos.size() + " productos del caché local");
        } else {
            System.out.println("❌ No hay caché disponible. Retornando lista vacía.");
        }

        return productos;
    }

    private static List<Map<String, String>> scrapearAgroPrecios() throws Exception {
        List<Map<String, String>> productos = new ArrayList<>();

        // Fix #6: ignoreHttpErrors(false) lanza excepción en respuestas 4xx/5xx
        Document doc = Jsoup.connect(BASE_URL)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(TIMEOUT)
                .followRedirects(true)
                .ignoreHttpErrors(false)
                .get();

        Elements filas = doc.select("table tbody tr");

        if (filas.isEmpty()) {
            filas = doc.select("tr[class*='producto']");
        }

        if (filas.isEmpty()) {
            filas = doc.select("table tr");
        }

        // Fix #1: validar que se encontró contenido antes de procesar
        if (filas.isEmpty()) {
            System.out.println("⚠️ No se encontraron filas en la página. La estructura HTML puede haber cambiado.");
            return productos;
        }

        int contador = 0;
        String fechaHoy = DATE_FORMAT.format(new Date());

        for (Element fila : filas) {
            try {
                Elements celdas = fila.select("td");

                if (celdas.size() < 2) continue;

                String nombre    = celdas.get(0).text().trim();
                String precioRaw = celdas.get(1).text().trim();
                // Fix #4: columna 2 es la fuente/subasta, no la variedad
                String fuente    = celdas.size() > 2 ? celdas.get(2).text().trim() : "AgroPrecios.com";

                if (nombre.isEmpty() || precioRaw.isEmpty() || !esNumero(precioRaw)) continue;

                Map<String, String> producto = new HashMap<>();
                producto.put("nombre", nombre);
                producto.put("precio", limpiarPrecio(precioRaw));
                producto.put("fuente", fuente.isEmpty() ? "AgroPrecios.com" : fuente);
                producto.put("variedad", "");
                producto.put("origen", "SCRAPER");
                // Fix #5: usar formato yyyy-MM-dd HH:mm:ss en lugar de Date.toString()
                producto.put("fecha_actualizacion", fechaHoy);

                productos.add(producto);
                contador++;

            } catch (Exception e) {
                // continuar con la siguiente fila si esta falla
            }
        }

        System.out.println("   → Se extrajeron " + contador + " registros válidos");
        return productos;
    }

    // Fix #3: maneja formato europeo de miles "1.234,56" → "1234.56"
    private static String limpiarPrecio(String precio) {
        String limpio = precio.replaceAll("[^0-9.,]", "");

        if (limpio.contains(",") && limpio.contains(".")) {
            // "1.234,56": el punto es separador de miles, la coma es decimal
            limpio = limpio.replace(".", "").replace(",", ".");
        } else {
            limpio = limpio.replace(",", ".");
        }

        try {
            Double.parseDouble(limpio);
            return limpio;
        } catch (NumberFormatException e) {
            return "0.0";
        }
    }

    // Fix #3: consistente con limpiarPrecio para evitar falsos negativos
    private static boolean esNumero(String str) {
        String limpio = str.replaceAll("[^0-9.,]", "");
        if (limpio.isEmpty()) return false;

        if (limpio.contains(",") && limpio.contains(".")) {
            limpio = limpio.replace(".", "").replace(",", ".");
        } else {
            limpio = limpio.replace(",", ".");
        }

        try {
            Double.parseDouble(limpio);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Fix #7: separador tabulador en lugar de | para evitar corrupción si el nombre contiene |
    private static void guardarCacheLocal(List<Map<String, String>> productos) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(CACHE_FILE))) {
            writer.println("# Cache de Scraper - " + DATE_FORMAT.format(new Date()));
            for (Map<String, String> p : productos) {
                writer.println(p.get("nombre") + "\t" + p.get("precio") + "\t" +
                               p.get("fuente") + "\t" + p.get("variedad"));
            }
            System.out.println("💾 Caché guardado en: " + CACHE_FILE);
        } catch (IOException e) {
            System.err.println("❌ No se pudo guardar caché: " + e.getMessage());
        }
    }

    // Fix #7: split por tabulador consistente con guardarCacheLocal
    private static List<Map<String, String>> cargarCacheLocal() {
        List<Map<String, String>> productos = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(CACHE_FILE))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                if (linea.startsWith("#") || linea.trim().isEmpty()) continue;

                String[] partes = linea.split("\t", -1);
                if (partes.length >= 4) {
                    Map<String, String> producto = new HashMap<>();
                    producto.put("nombre", partes[0]);
                    producto.put("precio", partes[1]);
                    producto.put("fuente", partes[2]);
                    producto.put("variedad", partes[3]);
                    producto.put("origen", "CACHE");
                    productos.add(producto);
                }
            }
        } catch (IOException e) {
            System.err.println("ℹ️ No hay caché disponible: " + e.getMessage());
        }

        return productos;
    }
}
