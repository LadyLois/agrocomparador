package agrocomparador.scraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.*;
import java.io.*;
import java.text.SimpleDateFormat;

public class AgrePreciosScraperDAO {
    private static final String BASE_URL  = "https://www.agroprecios.com/es/precios-producto/";
    private static final String HOST      = "https://www.agroprecios.com";
    private static final String USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Safari/537.36";
    private static final int TIMEOUT               = 15000;
    private static final int MAX_REINTENTOS        = 3;
    private static final int DELAY_ENTRE_REINTENTOS = 2000;
    private static final int DELAY_ENTRE_PRODUCTOS  = 1200;
    private static final String CACHE_FILE = "scraper_cache.txt";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static List<Map<String, String>> obtenerProductosDesdeScraper() {
        return obtenerProductosDesdeScraper(null);
    }

    public static List<Map<String, String>> obtenerProductosDesdeScraper(String fecha) {
        String urlBase = buildUrl(BASE_URL, fecha);
        for (int intento = 1; intento <= MAX_REINTENTOS; intento++) {
            try {
                System.out.println("🔄 Intento " + intento + "/" + MAX_REINTENTOS + " – agroprecios.com");
                Document docPrincipal = conectar(urlBase);
                List<Map<String, String>> todos = scrapearTodosLosProductos(docPrincipal, fecha);

                if (!todos.isEmpty()) {
                    if (fecha == null || fecha.isEmpty()) guardarCacheLocal(todos);
                    System.out.println("✓ AgroPrecios.com: " + todos.size() + " registros en total");
                    return todos;
                }
            } catch (Exception e) {
                System.err.println("❌ Intento " + intento + " falló: " + e.getMessage());
                if (intento < MAX_REINTENTOS) pausa(DELAY_ENTRE_REINTENTOS);
            }
        }

        if (fecha == null || fecha.isEmpty()) {
            System.out.println("⚠️ Usando caché local...");
            List<Map<String, String>> cache = cargarCacheLocal();
            if (!cache.isEmpty()) System.out.println("✓ " + cache.size() + " registros del caché");
            else System.out.println("❌ Sin caché disponible.");
            return cache;
        }
        return new ArrayList<>();
    }

    private static String buildUrl(String base, String fecha) {
        if (fecha == null || fecha.isEmpty()) return base;
        String[] p = fecha.split("-");
        // Los sitios usan formato DD-MM-YYYY en el parámetro ?fecha=
        return base + (base.contains("?") ? "&" : "?") + "fecha=" + p[2] + "-" + p[1] + "-" + p[0];
    }

    // ─── Iteración de todos los productos ────────────────────────────────────

    private static List<Map<String, String>> scrapearTodosLosProductos(Document docPrincipal, String fecha) {
        List<Map<String, String>> todos = new ArrayList<>();
        List<Map<String, String>> urlsProductos = extraerOpcionesProducto(docPrincipal);

        if (urlsProductos.isEmpty()) {
            // Sin lista de productos: scrapeamos solo la página actual
            String nombre = extraerNombreProducto(docPrincipal, "");
            todos.addAll(scrapearPaginaPrecios(docPrincipal, nombre, ""));
            return todos;
        }

        System.out.println("   → " + urlsProductos.size() + " productos detectados en agroprecios.com");

        for (Map<String, String> opcion : urlsProductos) {
            String url    = buildUrl(opcion.get("url"), fecha);
            String nombre = opcion.get("nombre");
            try {
                pausa(DELAY_ENTRE_PRODUCTOS);
                Document docProducto = opcion.get("url").equals(BASE_URL) ? docPrincipal : conectar(url);
                String nombreReal = extraerNombreProducto(docProducto, nombre);
                String variedad   = extraerVariedad(docProducto, nombreReal);

                // Fallback: si la variedad sigue vacía y el nombre de la opción tiene varias
                // palabras (ej. "Pepino frances"), separar la primera como producto y el resto como variedad
                if (variedad.isEmpty() && nombre.contains(" ")) {
                    int idx = nombre.indexOf(" ");
                    nombreReal = nombre.substring(0, idx).trim();
                    variedad   = nombre.substring(idx + 1).trim();
                }

                List<Map<String, String>> precios = scrapearPaginaPrecios(docProducto, nombreReal, variedad);
                if (!precios.isEmpty()) {
                    todos.addAll(precios);
                    System.out.println("   ✓ " + nombreReal +
                        (variedad.isEmpty() ? "" : " [" + variedad + "]") +
                        ": " + precios.size() + " registros");
                }
            } catch (Exception e) {
                System.err.println("   ⚠️ Error en '" + nombre + "': " + e.getMessage());
            }
        }

        return todos;
    }

    // ─── Extracción de la lista de productos desde el <select> o nav ─────────

    private static List<Map<String, String>> extraerOpcionesProducto(Document doc) {
        List<Map<String, String>> opciones = new ArrayList<>();

        // Buscar en todos los <select> el que tenga más opciones (= selector de producto)
        Element mejorSelect = null;
        int maxOpciones = 1;
        for (Element select : doc.select("select")) {
            int n = select.select("option[value]").size();
            if (n > maxOpciones) { maxOpciones = n; mejorSelect = select; }
        }

        if (mejorSelect != null) {
            for (Element opt : mejorSelect.select("option[value]")) {
                String val    = opt.attr("value").trim();
                String nombre = opt.text().trim();
                if (val.isEmpty() || val.equals("0") || val.equals("#") || nombre.isEmpty()) continue;
                String url = resolverUrl(val);
                if (url == null) continue;
                Map<String, String> m = new HashMap<>();
                m.put("url", url);
                m.put("nombre", nombre);
                opciones.add(m);
            }
        }

        // Fallback: links de navegación con el patrón de URL del producto
        if (opciones.isEmpty()) {
            Set<String> vistas = new HashSet<>();
            for (Element a : doc.select("a[href*='precios-producto/']")) {
                String href   = a.absUrl("href");
                String nombre = a.text().trim();
                if (href.isEmpty() || nombre.isEmpty()) continue;
                if (href.equals(BASE_URL) || href.endsWith("/precios-producto/")) continue;
                if (!vistas.add(href)) continue;
                Map<String, String> m = new HashMap<>();
                m.put("url", href);
                m.put("nombre", nombre);
                opciones.add(m);
            }
        }

        return opciones;
    }

    private static String resolverUrl(String val) {
        if (val.startsWith("http"))  return val;
        if (val.startsWith("/"))     return HOST + val;
        if (val.matches("[a-zA-Z0-9áéíóúüñÁÉÍÓÚÜÑ%_-]+")) return BASE_URL + val + "/";
        return null;
    }

    // ─── Scraping de la tabla de precios en una página de producto ────────────

    private static List<Map<String, String>> scrapearPaginaPrecios(Document doc, String nombre, String variedad) {
        List<Map<String, String>> productos = new ArrayList<>();
        String fechaHoy = DATE_FORMAT.format(new Date());

        Elements filas = doc.select("table tbody tr");
        if (filas.isEmpty()) filas = doc.select("table tr");

        for (Element fila : filas) {
            try {
                Elements celdas = fila.select("td");
                if (celdas.size() < 2) continue;

                String subasta = celdas.get(0).text().trim();
                if (esEncabezado(subasta)) continue;

                double precio = extraerPrimerPrecioValido(celdas, 1);
                if (precio <= 0) continue;

                Map<String, String> p = new HashMap<>();
                p.put("nombre",              nombre);
                p.put("variedad",            variedad);
                p.put("fuente",              subasta);
                p.put("precio",              String.valueOf(precio));
                p.put("origen",              "AGROPRECIOS");
                p.put("fecha_actualizacion", fechaHoy);
                productos.add(p);
            } catch (Exception ignored) {}
        }

        return productos;
    }

    // ─── Extracción de nombre y variedad ─────────────────────────────────────

    private static String extraerNombreProducto(Document doc, String fallback) {
        // Intentar el texto de la opción seleccionada en el primer <select>
        Element selOpt = doc.selectFirst("select option[selected]");
        if (selOpt != null) {
            String t = selOpt.text().trim();
            if (!t.isEmpty() && t.length() < 60) return t;
        }
        // Encabezados h1, h2, h3
        for (String sel : new String[]{"h1", "h2", "h3", ".page-title", ".titulo"}) {
            Element el = doc.selectFirst(sel);
            if (el != null) {
                String t = el.text().trim();
                if (!t.isEmpty() && t.length() < 80
                        && !t.toLowerCase().contains("agroprecios")
                        && !t.toLowerCase().contains("agropizarra")) {
                    return t;
                }
            }
        }
        // Título de la pestaña del navegador
        String titulo = doc.title();
        if (!titulo.isEmpty()) return titulo.split("[|\\-–]")[0].trim();
        return fallback.isEmpty() ? "AgroPrecios" : fallback;
    }

    private static String extraerVariedad(Document doc, String nombreProducto) {
        // Si hay un segundo <select>, su opción seleccionada es la variedad
        List<Element> selects = doc.select("select");
        if (selects.size() >= 2) {
            Element selVariedad = selects.get(1);
            Element opt = selVariedad.selectFirst("option[selected]");
            if (opt != null) {
                String v = opt.text().trim();
                if (!v.isEmpty() && !v.equalsIgnoreCase(nombreProducto)) return v;
            }
        }
        // Si h1 contiene el nombre del producto seguido de más texto → esa parte es la variedad
        Element h1 = doc.selectFirst("h1");
        if (h1 != null) {
            String h1Text = h1.text().trim();
            String base   = nombreProducto.toLowerCase();
            String h1Low  = h1Text.toLowerCase();
            if (h1Low.startsWith(base) && h1Text.length() > nombreProducto.length()) {
                String resto = h1Text.substring(nombreProducto.length()).trim();
                if (!resto.isEmpty() && resto.length() < 60) return resto;
            }
        }
        return "";
    }

    // ─── Utilidades ──────────────────────────────────────────────────────────

    private static Document conectar(String url) throws Exception {
        return Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT)
                .followRedirects(true)
                .ignoreHttpErrors(false)
                .get();
    }

    private static double extraerPrimerPrecioValido(Elements celdas, int desde) {
        for (int i = desde; i < celdas.size(); i++) {
            String val = celdas.get(i).text().trim();
            if (val.isEmpty() || val.equals("-") || val.equals("—")) continue;
            // Descartar fechas con separadores (dd/mm/yyyy, dd-mm-yyyy, etc.)
            if (val.matches(".*\\d[/\\-.]\\d.*")) continue;
            String soloDigitos = val.replaceAll("[^0-9]", "");
            // Descartar si tiene más de 6 dígitos (fechas sin separadores: 11052026)
            if (soloDigitos.isEmpty() || soloDigitos.length() > 6) continue;
            try {
                int centimos = Integer.parseInt(soloDigitos);
                if (centimos > 0) return centimos / 100.0;
            } catch (NumberFormatException ignored) {}
        }
        return 0;
    }

    private static boolean esEncabezado(String texto) {
        if (texto == null || texto.isEmpty()) return true;
        if (texto.equalsIgnoreCase("SUBASTAS"))  return true;
        if (texto.matches("\\d+"))               return true;
        if (texto.length() > 80)                 return true;
        return false;
    }

    private static void pausa(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
    }

    // ─── Caché local ─────────────────────────────────────────────────────────

    private static void guardarCacheLocal(List<Map<String, String>> productos) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(CACHE_FILE))) {
            writer.println("# Cache AgroPrecios – " + DATE_FORMAT.format(new Date()));
            for (Map<String, String> p : productos) {
                writer.println(p.get("nombre") + "\t" + p.get("precio") + "\t" +
                               p.get("fuente") + "\t" + p.get("variedad"));
            }
            System.out.println("💾 Caché guardado: " + CACHE_FILE);
        } catch (IOException e) {
            System.err.println("❌ No se pudo guardar caché: " + e.getMessage());
        }
    }

    private static List<Map<String, String>> cargarCacheLocal() {
        List<Map<String, String>> productos = new ArrayList<>();
        String fechaHoy = DATE_FORMAT.format(new Date());
        try (BufferedReader reader = new BufferedReader(new FileReader(CACHE_FILE))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                if (linea.startsWith("#") || linea.trim().isEmpty()) continue;
                String[] partes = linea.split("\t", -1);
                if (partes.length >= 3) {
                    Map<String, String> p = new HashMap<>();
                    p.put("nombre",              partes[0]);
                    p.put("precio",              partes[1]);
                    p.put("fuente",              partes[2]);
                    p.put("variedad",            partes.length > 3 ? partes[3] : "");
                    p.put("origen",              "AGROPRECIOS");
                    p.put("fecha_actualizacion", fechaHoy);
                    productos.add(p);
                }
            }
        } catch (IOException e) {
            System.err.println("ℹ️ Sin caché disponible: " + e.getMessage());
        }
        return productos;
    }
}
