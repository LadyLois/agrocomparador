package agrocomparador.business;

import agrocomparador.data.ProductoDAO;
import agrocomparador.data.ProductoDAOScraper;
import agrocomparador.scraper.AgrePreciosScraperDAO;
import agrocomparador.scraper.ScraperScheduler;
import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio de lógica de negocio para productos y comparación de precios
 * Implementa la lógica de filtrado y procesamiento de datos
 * 
 * Integra 2 fuentes:
 * 1. Base de datos local (ProductoDAO)
 * 2. Web Scraping de agroprecios.com (ProductoDAOScraper)
 */
public class ProductoService {
    
    /**
     * Obtiene todos los productos disponibles (solo de BD)
     * @return Lista de productos
     */
    public static List<Map<String, Object>> obtenerTodosLosProductos() {
        return ProductoDAO.obtenerProductos();
    }
    
    /**
     * Obtiene productos combinando Base de Datos + Web Scraping
     * @return Lista de productos de ambas fuentes
     */
    public static List<Map<String, Object>> obtenerTodosLosProductosCombinados() {
        // Datos BD legacy + datos scrapeados, todos en la misma tabla precios
        List<Map<String, Object>> productos = new ArrayList<>(ProductoDAO.obtenerProductos());

        for (Map<String, String> p : ProductoDAOScraper.obtenerProductosDelScraper()) {
            Map<String, Object> producto = new HashMap<>();
            producto.put("nombre",              p.getOrDefault("nombre", ""));
            producto.put("variedad",            p.getOrDefault("variedad", ""));
            producto.put("fuente",              p.getOrDefault("fuente", ""));
            try { producto.put("precio", Double.parseDouble(p.getOrDefault("precio", "0"))); }
            catch (NumberFormatException e) { producto.put("precio", 0.0); }
            producto.put("origen",              p.getOrDefault("origen", "SCRAPER"));
            producto.put("fecha_actualizacion", p.getOrDefault("fecha_actualizacion", ""));
            productos.add(producto);
        }

        return productos;
    }
    
    public static List<Map<String, Object>> obtenerProductosPorNombre(String filtro) {
        List<Map<String, Object>> todos = ProductoDAO.obtenerProductos();
        if (filtro == null || filtro.trim().isEmpty()) return todos;
        return todos.stream().filter(p -> coincide(p, filtro)).collect(Collectors.toList());
    }

    public static List<Map<String, Object>> obtenerProductosPorNombreCombinados(String filtro) {
        List<Map<String, Object>> todos = obtenerTodosLosProductosCombinados();
        if (filtro == null || filtro.trim().isEmpty()) return todos;
        return todos.stream().filter(p -> coincide(p, filtro)).collect(Collectors.toList());
    }

    public static boolean coincidePublico(Map<String, Object> producto, String filtro) {
        return coincide(producto, filtro);
    }

    private static boolean coincide(Map<String, Object> producto, String filtro) {
        String f = normalizar(filtro);
        return normalizar(producto.getOrDefault("nombre",   "").toString()).contains(f)
            || normalizar(producto.getOrDefault("variedad", "").toString()).contains(f)
            || normalizar(producto.getOrDefault("fuente",   "").toString()).contains(f)
            || normalizar(producto.getOrDefault("origen",   "").toString()).contains(f);
    }

    private static String normalizar(String texto) {
        if (texto == null || texto.isEmpty()) return "";
        String sin_tildes = Normalizer.normalize(texto, Normalizer.Form.NFD)
                                      .replaceAll("\\p{InCombiningDiacriticalMarks}", "");
        return sin_tildes.toLowerCase();
    }
    
    /**
     * Encuentra el precio más bajo para un producto específico
     * @param productos Lista de productos
     * @param nombreProducto Nombre del producto a buscar
     * @return Precio más bajo o 0.0 si no encuentra
     */
    public static Double obtenerPrecioMinimo(List<Map<String, Object>> productos, String nombreProducto) {
        return productos.stream()
            .filter(p -> p.get("nombre").equals(nombreProducto))
            .mapToDouble(p -> ((Number) p.get("precio")).doubleValue())
            .min()
            .orElse(0.0);
    }
    
    /**
     * Encuentra la fuente más barata para un producto
     * @param productos Lista de productos
     * @param nombreProducto Nombre del producto
     * @return Información de la fuente más barata
     */
    public static Map<String, Object> obtenerFuenteBarata(List<Map<String, Object>> productos, String nombreProducto) {
        return productos.stream()
            .filter(p -> p.get("nombre").equals(nombreProducto))
            .min(Comparator.comparingDouble(p -> ((Number) p.get("precio")).doubleValue()))
            .orElse(null);
    }
    
    /**
     * Cuenta el total de productos únicos en la lista
     * @param productos Lista de productos
     * @return Total de productos diferentes
     */
    public static int obtenerTotalProductosUnicos(List<Map<String, Object>> productos) {
        return (int) productos.stream()
            .map(p -> p.get("nombre"))
            .distinct()
            .count();
    }
    
    public static List<String> obtenerFechasDisponibles() {
        return ProductoDAOScraper.obtenerFechasDisponibles();
    }

    public static List<Map<String, Object>> obtenerProductosPorFechaCombinados(String fecha) {
        List<Map<String, Object>> productos = new ArrayList<>(ProductoDAO.obtenerProductos());
        List<Map<String, String>> scraped = ProductoDAOScraper.obtenerProductosPorFecha(fecha);
        for (Map<String, String> p : scraped) {
            Map<String, Object> producto = new HashMap<>();
            producto.put("nombre",              p.getOrDefault("nombre", ""));
            producto.put("variedad",            p.getOrDefault("variedad", ""));
            producto.put("fuente",              p.getOrDefault("fuente", ""));
            try { producto.put("precio", Double.parseDouble(p.getOrDefault("precio", "0"))); }
            catch (NumberFormatException e) { producto.put("precio", 0.0); }
            producto.put("origen",              p.getOrDefault("origen", "SCRAPER"));
            producto.put("fecha_actualizacion", p.getOrDefault("fecha_actualizacion", ""));
            productos.add(producto);
        }
        return productos;
    }

    public static List<Map<String, Object>> obtenerProductosPorRangoCombinados(String desde, String hasta) {
        List<Map<String, Object>> productos = new ArrayList<>(ProductoDAO.obtenerProductos());
        for (Map<String, String> p : ProductoDAOScraper.obtenerProductosPorRangoFechas(desde, hasta)) {
            Map<String, Object> producto = new HashMap<>();
            producto.put("nombre",              p.getOrDefault("nombre", ""));
            producto.put("variedad",            p.getOrDefault("variedad", ""));
            producto.put("fuente",              p.getOrDefault("fuente", ""));
            try { producto.put("precio", Double.parseDouble(p.getOrDefault("precio", "0"))); }
            catch (NumberFormatException e) { producto.put("precio", 0.0); }
            producto.put("origen",              p.getOrDefault("origen", "SCRAPER"));
            producto.put("fecha_actualizacion", p.getOrDefault("fecha_actualizacion", ""));
            productos.add(producto);
        }
        return productos;
    }

    public static void vaciarDatosScraper() {
        ProductoDAOScraper.vaciarDatos();
    }

    public static void vaciarDatosScraperPorFecha(String fecha) {
        ProductoDAOScraper.vaciarDatosPorFecha(fecha);
    }

    public static void forzarCargaDatos() {
        forzarCargaDatos(null);
    }

    public static void forzarCargaDatos(String fecha) {
        ScraperScheduler.getInstance().forzarActualizacionAsincrona(fecha);
    }

    public static Set<String> obtenerFechasEnProceso() {
        return ScraperScheduler.getFechasEnProceso();
    }

    /**
     * Obtiene estadísticas del scraper
     * @return Mapa con estadísticas
     */
    public static Map<String, String> obtenerEstadisticasScraper() {
        return ProductoDAOScraper.obtenerEstadisticasScraper();
    }
    
    /**
     * Obtiene precio mínimo considerando ambas fuentes
     * @param nombreProducto Nombre del producto
     * @return Precio mínimo encontrado
     */
    public static double obtenerPrecioMinimoCombinado(String nombreProducto) {
        List<Map<String, Object>> todos = obtenerTodosLosProductosCombinados();
        return todos.stream()
            .filter(p -> p.get("nombre").toString().toLowerCase()
                    .contains(nombreProducto.toLowerCase()))
            .mapToDouble(p -> ((Number) p.get("precio")).doubleValue())
            .filter(p -> p > 0)
            .min()
            .orElse(0.0);
    }
}
