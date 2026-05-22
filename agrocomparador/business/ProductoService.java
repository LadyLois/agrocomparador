package agrocomparador.business;

import agrocomparador.data.ProductoDAO;
import agrocomparador.scraper.AgrePreciosScraperDAO;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio de lógica de negocio para productos y comparación de precios
 * Implementa la lógica de filtrado y procesamiento de datos
 * 
 * Integra 2 fuentes:
 * 1. Base de datos local (ProductoDAO)
 * 2. Web Scraping de agroprecios.com (AgrePreciosScraperDAO)
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
        List<Map<String, Object>> productos = new ArrayList<>(ProductoDAO.obtenerProductos());
        
        // Obtener datos del scraper almacenados en BD
        List<Map<String, String>> scrapedRaw = AgrePreciosScraperDAO.obtenerProductosDesdeScraper();
        
        // Convertir a formato compatible
        for (Map<String, String> p : scrapedRaw) {
            Map<String, Object> producto = new HashMap<>();
            producto.put("nombre", p.getOrDefault("nombre", ""));
            producto.put("variedad", p.getOrDefault("variedad", ""));
            producto.put("fuente", p.getOrDefault("fuente", "AgroPrecios"));
            
            try {
                producto.put("precio", Double.parseDouble(p.getOrDefault("precio", "0")));
            } catch (NumberFormatException e) {
                producto.put("precio", 0.0);
            }
            
            producto.put("origen", p.getOrDefault("origen", "SCRAPER"));
            productos.add(producto);
        }
        
        return productos;
    }
    
    /**
     * Obtiene productos filtrados por nombre (solo de BD)
     * @param nombreProducto Nombre del producto a buscar
     * @return Lista de productos que coinciden con el filtro
     */
    public static List<Map<String, Object>> obtenerProductosPorNombre(String nombreProducto) {
        List<Map<String, Object>> todos = ProductoDAO.obtenerProductos();
        
        if (nombreProducto == null || nombreProducto.trim().isEmpty()) {
            return todos;
        }
        
        return todos.stream()
            .filter(p -> p.get("nombre").toString().toLowerCase()
                    .contains(nombreProducto.toLowerCase()))
            .collect(Collectors.toList());
    }
    
    /**
     * Obtiene productos filtrados por nombre (BD + Scraper)
     * @param nombreProducto Nombre del producto a buscar
     * @return Lista de productos que coinciden
     */
    public static List<Map<String, Object>> obtenerProductosPorNombreCombinados(String nombreProducto) {
        List<Map<String, Object>> todos = obtenerTodosLosProductosCombinados();
        
        if (nombreProducto == null || nombreProducto.trim().isEmpty()) {
            return todos;
        }
        
        return todos.stream()
            .filter(p -> p.get("nombre").toString().toLowerCase()
                    .contains(nombreProducto.toLowerCase()))
            .collect(Collectors.toList());
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
    
    /**
     * Obtiene estadísticas del scraper
     * @return Mapa con estadísticas
     */
    public static Map<String, String> obtenerEstadisticasScraper() {
        return new HashMap<>();//cambio para iniciar servidor
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
