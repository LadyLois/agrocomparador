/**
 * ARCHIVO DE EJEMPLO Y PRUEBAS
 * 
 * Este archivo muestra cómo usar cada componente del proyecto
 * NO necesita compilarse/ejecutarse, es solo referencia
 */

// ============================================
// 1. CAPA DE DATOS - Ejemplos de uso
// ============================================

import agrocomparador.data.DatabaseConnection;
import agrocomparador.data.ProductoDAO;
import java.sql.Connection;

public class EjemplosUso {
    
    // Ejemplo 1: Obtener conexión a la BD
    public void ejemplo1_ConexionBaseDatos() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            System.out.println("✅ Conectado a la BD");
            DatabaseConnection.closeConnection(conn);
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
        }
    }
    
    // Ejemplo 2: Obtener todos los productos
    public void ejemplo2_ObtenerProductos() {
        java.util.List<java.util.Map<String, Object>> productos = 
            ProductoDAO.obtenerProductos();
        
        System.out.println("Total de registros: " + productos.size());
        
        for (java.util.Map<String, Object> producto : productos) {
            System.out.println(
                producto.get("nombre") + " - " + 
                producto.get("fuente") + " - €" + 
                producto.get("precio")
            );
        }
    }
    
    // Ejemplo 3: Filtrar productos por nombre
    public void ejemplo3_FiltrarProductos() {
        j java.util.List<java.util.Map<String, Object>> productos = 
            agrocomparador.business.ProductoService.obtenerProductosPorNombre("Tomate");
        
        System.out.println("Productos encontrados: " + productos.size());
    }
    
    // Ejemplo 4: Encontrar precio mínimo
    public void ejemplo4_PrecioMinimo() {
        java.util.List<java.util.Map<String, Object>> productos = 
            agrocomparador.business.ProductoService.obtenerTodosLosProductos();
        
        Double precioMin = agrocomparador.business.ProductoService
            .obtenerPrecioMinimo(productos, "Tomate");
        
        System.out.println("Precio mínimo de Tomate: €" + precioMin);
    }
    
    // Ejemplo 5: Encontrar fuente más barata
    public void ejemplo5_FuenteBarata() {
        java.util.List<java.util.Map<String, Object>> productos = 
            agrocomparador.business.ProductoService.obtenerTodosLosProductos();
        
        java.util.Map<String, Object> fuente = 
            agrocomparador.business.ProductoService
                .obtenerFuenteBarata(productos, "Lechuga");
        
        if (fuente != null) {
            System.out.println("Fuente más barata: " + fuente.get("fuente") + 
                             " - €" + fuente.get("precio"));
        }
    }
}

// ============================================
// 2. RUTAS HTTP DISPONIBLES
// ============================================

/**
 * RUTAS DEL SERVIDOR:
 * 
 * GET /
 * Descripción: Muestra todos los productos
 * Resultado: Tabla HTML con todos los registros
 * 
 * GET /?producto=Tomate
 * Descripción: Filtra productos que contengan "Tomate"
 * Parámetros:
 *   - producto (query string) = Nombre o parte del nombre a buscar
 * Resultado: Tabla HTML solo con productos filtrados
 * 
 * Ejemplos:
 * - http://localhost/
 * - http://localhost/?producto=Tomate
 * - http://localhost/?producto=Lechuga
 * - http://localhost/?producto=maiz (case-insensitive)
 * - http://localhost/?producto=Producto%20Especial (con espacios codificados)
 */

// ============================================
// 3. FLUJO COMPLETO: Solicitud HTTP
// ============================================

/**
 * EJEMPLO: Usuario accede a http://localhost/?producto=Tomate
 * 
 * 1. WebServer.manejarSolicitud() recibe la solicitud HTTP
 *    - GET /?producto=Tomate HTTP/1.1
 * 
 * 2. Parsea la URL y extrae parámetro:
 *    - ruta: "/?producto=Tomate"
 *    - filtroProducto: "Tomate"
 * 
 * 3. Llama a ProductoService:
 *    ProductoService.obtenerProductosPorNombre("Tomate")
 * 
 * 4. ProductoService llama a ProductoDAO:
 *    ProductoDAO.obtenerProductos()
 * 
 * 5. ProductoDAO necesita conexión:
 *    DatabaseConnection.getConnection()
 * 
 * 6. Se conecta a MySQL y ejecuta:
 *    SELECT p.nombre, p.variedad, f.nombre AS fuente, pr.precio
 *    FROM precios pr
 *    JOIN productos p ON pr.producto_id = p.id
 *    JOIN fuentes f ON pr.fuente_id = f.id
 *    ORDER BY p.nombre, pr.precio
 * 
 * 7. ProductoDAO retorna List<Map<String, Object>> con todos los productos
 * 
 * 8. ProductoService FILTRA la lista:
 *    - Mantiene solo filas donde nombre contiene "Tomate"
 *    - Retorna lista filtrada
 * 
 * 9. WebServer llama a HTMLBuilder:
 *    HTMLBuilder.construirRespuestaHTML(productosFiltrados, null, "Tomate")
 * 
 * 10. HTMLBuilder genera HTML:
 *     - Título: "🌾 Comparador de Precios Agrícolas"
 *     - Formulario de búsqueda con "Tomate" prerellenado
 *     - Tabla con columnas: Producto | Variedad | Fuente | Precio
 *     - Estilos CSS
 *     - Información: "Se encontraron X registros"
 * 
 * 11. HTMLBuilder envuelve en respuesta HTTP:
 *     HTTP/1.1 200 OK
 *     Content-Type: text/html; charset=UTF-8
 *     Content-Length: 12345
 *     Connection: close
 *     
 *     [HTML content aquí]
 * 
 * 12. WebServer envía respuesta HTTP al cliente
 * 
 * 13. Navegador recibe HTML y lo renderiza
 */

// ============================================
// 4. DATOS ESPERADOS EN LA TABLA
// ============================================

/**
 * Estructura de cada fila en List<Map<String, Object>>:
 * 
 * {
 *   "nombre": "Tomate",
 *   "variedad": "Cherry",
 *   "fuente": "Mercado Central",
 *   "precio": 0.85
 * }
 * 
 * Esta estructura viene directamente de ProductoDAO.obtenerProductos()
 * que traduce el ResultSet a Map
 */

// ============================================
// 5. MANEJO DE ERRORES
// ============================================

/**
 * Casos de error que maneja:
 * 
 * 1. Base de datos no disponible
 *    - DatabaseConnection.getConnection() lanza SQLException
 *    - ProductoDAO detecta el error
 *    - HTMLBuilder muestra: "Error: Connection refused"
 * 
 * 2. Tabla vacía en BD
 *    - ProductoDAO.obtenerProductos() retorna List vacía
 *    - HTMLBuilder muestra: "No hay productos disponibles"
 * 
 * 3. Sin resultados para el filtro
 *    - ProductoService.obtenerProductosPorNombre() retorna List vacía
 *    - HTMLBuilder muestra: "Se encontraron 0 registros" + "No hay productos disponibles"
 * 
 * 4. Parámetro con caracteres especiales
 *    - WebServer decodifica con URLDecoder.decode(param, "UTF-8")
 *    - Ejemplo: "Producto%20Especial" → "Producto Especial"
 */

// ============================================
// 6. LOGS Y DEBUG
// ============================================

/**
 * A. Consola del Servidor
 * 
 * Al iniciar:
 *   🚀 Servidor iniciado en puerto 80
 *   📍 Accede a: http://localhost/
 * 
 * Al recibir solicitud:
 *   (Sin log automático, pero puedes agregar en WebServer.manejarSolicitud)
 * 
 * En caso de error:
 *   ❌ Error: Connection refused
 *   ❌ Error al manejar solicitud: ...
 * 
 * B. Agregando debug
 * 
 * En WebServer.manejarSolicitud(), agregar:
 *   System.out.println("📍 Solicitud recibida: " + ruta);
 *   if (filtroProducto != null) {
 *       System.out.println("🔍 Filtro aplicado: " + filtroProducto);
 *   }
 *   System.out.println("📊 Productos encontrados: " + productos.size());
 */

// ============================================
// 7. PERSONALIZACIÓN
// ============================================

/**
 * Cambiar puerto:
 *   Edita: WebServer.PUERTO
 *   Accede: http://localhost:NUEVO_PUERTO/
 * 
 * Cambiar estilos CSS:
 *   Edita: HTMLBuilder.construirCSS()
 * 
 * Agregar nueva columna a tabla:
 *   1. ProductoDAO: SELECT ... nuevo_campo ...
 *   2. HTMLBuilder.construirTablaProductos(): Agregar <th> y <td>
 * 
 * Agregar nuevo filtro (por precio):
 *   1. ProductoService: public static List obtenerProductosPorPrecio(Double max)
 *   2. WebServer: Parsear parámetro ?precio=100
 *   3. HTMLBuilder: Mostrar filtro activo
 */
