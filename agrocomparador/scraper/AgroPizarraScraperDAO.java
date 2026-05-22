package agrocomparador.scraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.*;
import java.text.SimpleDateFormat;

public class AgroPizarraScraperDAO {
    private static final String BASE_URL  = "https://www.agropizarra.com/es/pizarra-producto/";
    private static final String HOST      = "https://www.agropizarra.com";
    private static final String USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Safari/537.36";
    private static final int TIMEOUT               = 15000;
    private static final int MAX_REINTENTOS        = 3;
    private static final int DELAY_ENTRE_REINTENTOS = 2000;
    private static final int DELAY_ENTRE_PRODUCTOS  = 1200;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static List<Map<String, String>> obtenerProductosDesdeScraper() {
        return obtenerProductosDesdeScraper(null);
    }

    public static List<Map<String, String>> obtenerProductosDesdeScraper(String fecha) {
        // Siempre cargar la lista de productos desde BASE_URL (sin fecha)
        // Para fechas históricas, cada producto usa el endpoint de API con base64
        for (int intento = 1; intento <= MAX_REINTENTOS; intento++) {
            try {
                System.out.println("🔄 Intento " + intento + "/" + MAX_REINTENTOS + " – agropizarra.com");
                Document docPrincipal = conectar(BASE_URL);
                List<Map<String, String>> todos = scrapearTodosLosProductos(docPrincipal, fecha);

                if (!todos.isEmpty()) {
                    System.out.println("✓ AgroPizarra.com: " + todos.size() + " registros en total");
                    return todos;
                }
            } catch (Exception e) {
                System.err.println("❌ Intento " + intento + " falló: " + e.getMessage());
                if (intento < MAX_REINTENTOS) pausa(DELAY_ENTRE_REINTENTOS);
            }
        }

        System.out.println("❌ No se pudieron obtener datos de AgroPizarra.com");
        return new ArrayList<>();
    }

    private static String buildUrl(String base, String fecha) {
        if (fecha == null || fecha.isEmpty()) return base;
        // Si la URL contiene un ID de producto (ej: /24-berenjena-larga/), usar el endpoint AJAX con base64
        // que es el mecanismo real que usa el sitio para fechas históricas
        String id = extractProductId(base);
        if (id != null) {
            String param = id + "|" + fecha + "|1|1|1";
            String encoded = java.util.Base64.getEncoder().encodeToString(param.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return HOST + "/pizarra-producto-ver.php?var=" + encoded;
        }
        // Fallback para URLs sin ID de producto (no debería llegar aquí en producción)
        String[] p = fecha.split("-");
        return base + (base.contains("?") ? "&" : "?") + "fecha=" + p[2] + "-" + p[1] + "-" + p[0];
    }

    // Extrae el ID numérico del producto desde la URL slug, ej: ".../24-berenjena-larga/" → "24"
    private static String extractProductId(String url) {
        int idx = url.indexOf("/pizarra-producto/");
        if (idx < 0) return null;
        String slug = url.substring(idx + "/pizarra-producto/".length());
        int dashIdx = slug.indexOf('-');
        if (dashIdx <= 0) return null;
        String id = slug.substring(0, dashIdx);
        return id.matches("\\d+") ? id : null;
    }

    // ─── Iteración de todos los productos ────────────────────────────────────

    private static List<Map<String, String>> scrapearTodosLosProductos(Document docPrincipal, String fecha) {
        List<Map<String, String>> todos = new ArrayList<>();
        List<Map<String, String>> urlsProductos = extraerOpcionesProducto(docPrincipal);

        if (urlsProductos.isEmpty()) {
            String nombre = extraerNombreProducto(docPrincipal, "");
            todos.addAll(scrapearPaginaPrecios(docPrincipal, nombre, ""));
            return todos;
        }

        System.out.println("   → " + urlsProductos.size() + " productos detectados en agropizarra.com");

        for (Map<String, String> opcion : urlsProductos) {
            String url    = buildUrl(opcion.get("url"), fecha);
            String nombre = opcion.get("nombre");
            try {
                pausa(DELAY_ENTRE_PRODUCTOS);
                // Para fechas históricas siempre conectar al endpoint específico;
                // para hoy, reutilizar docPrincipal si el producto coincide con la URL base
                boolean esUrlBase = opcion.get("url").equals(BASE_URL);
                boolean esFechaHistorica = (fecha != null && !fecha.isEmpty());
                Document docProducto = (esUrlBase && !esFechaHistorica) ? docPrincipal : conectar(url);
                String nombreReal = extraerNombreProducto(docProducto, nombre);
                String variedad   = extraerVariedad(docProducto, nombreReal);

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

        // El <select> con más opciones es el selector de producto
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

        // Fallback: links de navegación
        if (opciones.isEmpty()) {
            Set<String> vistas = new HashSet<>();
            for (Element a : doc.select("a[href*='pizarra-producto/']")) {
                String href   = a.absUrl("href");
                String nombre = a.text().trim();
                if (href.isEmpty() || nombre.isEmpty()) continue;
                if (href.equals(BASE_URL) || href.endsWith("/pizarra-producto/")) continue;
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

    // ─── Scraping de precios en una página de producto ────────────────────────

    private static List<Map<String, String>> scrapearPaginaPrecios(Document doc, String nombre, String variedad) {
        // AgroPizarra usa ul/li; como fallback, tabla estándar
        List<Map<String, String>> resultado = scrapearDesdeListaUL(doc, nombre, variedad);
        if (resultado.isEmpty()) resultado = scrapearDesdeTabla(doc, nombre, variedad);
        return resultado;
    }

    private static List<Map<String, String>> scrapearDesdeListaUL(Document doc, String nombre, String variedad) {
        List<Map<String, String>> productos = new ArrayList<>();
        String fechaHoy = DATE_FORMAT.format(new Date());

        String[] listSelectores = {
            "ul.lista-precios > li", "ul.pizarra > li", "ul.precios > li",
            "ul.tabla-pizarra > li", ".pizarra ul > li", ".precios-tabla ul > li"
        };
        Elements filas = new Elements();
        for (String sel : listSelectores) {
            filas = doc.select(sel);
            if (!filas.isEmpty()) { System.out.println("   → Selector ul: " + sel); break; }
        }
        if (filas.isEmpty()) return productos;

        for (Element item : filas) {
            try {
                Map<String, String> p = procesarItemLista(item, nombre, variedad, fechaHoy);
                if (p != null) productos.add(p);
            } catch (Exception ignored) {}
        }
        return productos;
    }

    private static Map<String, String> procesarItemLista(Element item, String nombre, String variedad, String fechaHoy) {
        Elements children = item.select("span, div, strong, em");
        if (children.size() >= 2) {
            // La primera celda puede ser un logo vacío; buscar el primero con texto no numérico
            int subastaIdx = 0;
            for (int i = 0; i < Math.min(children.size() - 1, 3); i++) {
                String t = children.get(i).text().trim();
                if (!t.isEmpty() && !t.matches("\\d+")) { subastaIdx = i; break; }
            }
            String subasta = children.get(subastaIdx).text().trim();
            if (esEncabezado(subasta)) return null;
            double precio = extraerPrimerEnteroValido(children, subastaIdx + 1);
            if (precio <= 0) return null;
            return crearRegistro(nombre, variedad, subasta, precio, fechaHoy);
        }

        // Texto plano: "NombreSubasta 480 510 ..."
        String texto = item.text().trim();
        if (texto.isEmpty()) return null;
        String[] partes = texto.split("\\s+");
        StringBuilder sb = new StringBuilder();
        int inicioPrecios = -1;
        for (int i = 0; i < partes.length; i++) {
            if (partes[i].matches("\\d+")) { inicioPrecios = i; break; }
            if (sb.length() > 0) sb.append(" ");
            sb.append(partes[i]);
        }
        String subasta = sb.toString().trim();
        if (esEncabezado(subasta) || inicioPrecios < 0) return null;
        try {
            double precio = Integer.parseInt(partes[inicioPrecios]) / 100.0;
            if (precio <= 0) return null;
            return crearRegistro(nombre, variedad, subasta, precio, fechaHoy);
        } catch (NumberFormatException ignored) { return null; }
    }

    private static List<Map<String, String>> scrapearDesdeTabla(Document doc, String nombre, String variedad) {
        List<Map<String, String>> productos = new ArrayList<>();
        String fechaHoy = DATE_FORMAT.format(new Date());

        Elements filas = doc.select("table tbody tr");
        if (filas.isEmpty()) filas = doc.select("table tr");

        for (Element fila : filas) {
            try {
                Elements celdas = fila.select("td");
                if (celdas.size() < 2) continue;
                // La primera celda puede ser un logo vacío; buscar la primera con texto no numérico
                int nombreIdx = 0;
                for (int i = 0; i < Math.min(celdas.size() - 1, 3); i++) {
                    String t = celdas.get(i).text().trim();
                    if (!t.isEmpty() && !t.matches("\\d+")) { nombreIdx = i; break; }
                }
                String subasta = celdas.get(nombreIdx).text().trim();
                if (esEncabezado(subasta)) continue;
                double precio = extraerPrimerEnteroValido(celdas, nombreIdx + 1);
                if (precio <= 0) continue;
                productos.add(crearRegistro(nombre, variedad, subasta, precio, fechaHoy));
            } catch (Exception ignored) {}
        }
        return productos;
    }

    // ─── Extracción de nombre y variedad ─────────────────────────────────────

    private static String extraerNombreProducto(Document doc, String fallback) {
        Element selOpt = doc.selectFirst("select option[selected]");
        if (selOpt != null) {
            String t = selOpt.text().trim();
            if (!t.isEmpty() && t.length() < 60) return t;
        }
        for (String sel : new String[]{"h1", "h2", "h3", ".page-title", ".titulo"}) {
            Element el = doc.selectFirst(sel);
            if (el != null) {
                String t = el.text().trim();
                if (!t.isEmpty() && t.length() < 80
                        && !t.toLowerCase().contains("agropizarra")
                        && !t.toLowerCase().contains("agroprecios")) {
                    return t;
                }
            }
        }
        String titulo = doc.title();
        if (!titulo.isEmpty()) return titulo.split("[|\\-–]")[0].trim();
        return fallback.isEmpty() ? "AgroPizarra" : fallback;
    }

    private static String extraerVariedad(Document doc, String nombreProducto) {
        List<Element> selects = doc.select("select");
        if (selects.size() >= 2) {
            Element selVariedad = selects.get(1);
            Element opt = selVariedad.selectFirst("option[selected]");
            if (opt != null) {
                String v = opt.text().trim();
                if (!v.isEmpty() && !v.equalsIgnoreCase(nombreProducto)) return v;
            }
        }
        Element h1 = doc.selectFirst("h1");
        if (h1 != null) {
            String h1Text = h1.text().trim();
            String h1Low  = h1Text.toLowerCase();
            String base   = nombreProducto.toLowerCase();
            if (h1Low.startsWith(base) && h1Text.length() > nombreProducto.length()) {
                String resto = h1Text.substring(nombreProducto.length()).trim();
                if (!resto.isEmpty() && resto.length() < 60) return resto;
            }
        }
        return "";
    }

    // ─── Utilidades ──────────────────────────────────────────────────────────

    private static Document conectar(String url) throws Exception {
        // El endpoint AJAX requiere cabeceras adicionales para no ser rechazado por el servidor
        if (url.contains("pizarra-producto-ver.php")) {
            return Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT)
                    .followRedirects(true)
                    .ignoreHttpErrors(false)
                    .referrer(HOST + "/es/pizarra-producto/")
                    .header("X-Requested-With", "XMLHttpRequest")
                    .header("Accept", "text/html, */*; q=0.01")
                    .get();
        }
        return Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT)
                .followRedirects(true)
                .ignoreHttpErrors(false)
                .get();
    }

    private static double extraerPrimerEnteroValido(Elements elementos, int desde) {
        for (int i = desde; i < elementos.size(); i++) {
            String val = elementos.get(i).text().trim();
            if (val.isEmpty() || val.equals("-") || val.equals("—")) continue;
            if (val.matches(".*\\d[/\\-.]\\d.*")) continue;
            String soloDigitos = val.replaceAll("[^0-9]", "");
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
        if (texto.equalsIgnoreCase("SUBASTAS")) return true;
        if (texto.matches("\\d+"))             return true;
        if (texto.length() > 80)               return true;
        return false;
    }

    private static Map<String, String> crearRegistro(String nombre, String variedad, String fuente, double precio, String fecha) {
        Map<String, String> p = new HashMap<>();
        p.put("nombre",              nombre);
        p.put("variedad",            variedad);
        p.put("fuente",              fuente);
        p.put("precio",              String.valueOf(precio));
        p.put("origen",              "AGROPIZARRA");
        p.put("fecha_actualizacion", fecha);
        return p;
    }

    private static void pausa(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
    }
}