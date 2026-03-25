package agrocomparador.business;

import agrocomparador.data.ProductoDAO;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio de lógica de negocio para productos y comparación de precios
 * Implementa la lógica de filtrado y procesamiento de datos
 */
public class ProductoService {
    
    /**
     * Obtiene todos los productos disponibles
     * @return Lista de productos
     */
    public static List<Map<String, Object>> obtenerTodosLosProductos() {
        return ProductoDAO.obtenerProductos();
    }
    
    /**
     * Obtiene productos filtrados por nombre
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
}
