package agrocomparador.ui;

import java.util.*;

/**
 * Constructor de respuestas HTML
 * Maneja la presentación visual de los datos
 * Genera tablas y formularios de búsqueda
 */
public class HTMLBuilder {
    
    /**
     * Construye la respuesta HTML completa con productos en tabla
     * @param productos Lista de productos a mostrar
     * @param error Mensaje de error (null si no hay error)
     * @param filtroAplicado Nombre del filtro aplicado (null si no hay filtro)
     * @return HTML formateado
     */
    public static String construirRespuestaHTML(List<Map<String, Object>> productos, String error, String filtroAplicado) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n<head>\n");
        html.append("<meta charset='UTF-8'>\n");
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>\n");
        html.append("<title>Comparador de Precios Agrícolas</title>\n");
        html.append(construirCSS());
        html.append("</head>\n<body>\n");
        
        html.append("<div class='container'>\n");
        html.append("<h1>🌾 Comparador de Precios Agrícolas</h1>\n");
        
        // Formulario de búsqueda
        html.append(construirFormularioBusqueda(filtroAplicado));
        
        // Mostrar error si existe
        if (error != null && !error.isEmpty()) {
            html.append("<div class='error'>\n");
            html.append("<p><strong>Error:</strong> ").append(error).append("</p>\n");
            html.append("</div>\n");
        }
        
        // Mostrar tabla de productos
        if (productos != null && !productos.isEmpty()) {
            html.append("<p class='info'>Se encontraron ").append(productos.size()).append(" registros</p>\n");
            html.append(construirTablaProductos(productos));
        } else {
            html.append("<p class='no-data'>No hay productos disponibles.</p>\n");
        }
        
        html.append("</div>\n");
        html.append("</body>\n</html>");
        
        return html.toString();
    }
    
    /**
     * Construye los estilos CSS
     * @return Contenido CSS
     */
    private static String construirCSS() {
        return "<style>\n" +
               "  * { margin: 0; padding: 0; box-sizing: border-box; }\n" +
               "  body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f0f2f5; color: #333; }\n" +
               "  .container { max-width: 1000px; margin: 0 auto; padding: 20px; }\n" +
               "  h1 { color: #1a5f2d; margin-bottom: 20px; text-align: center; }\n" +
               "  .form-busqueda { background: white; padding: 15px; border-radius: 8px; margin-bottom: 20px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }\n" +
               "  .form-busqueda form { display: flex; gap: 10px; }\n" +
               "  .form-busqueda input { flex: 1; padding: 10px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px; }\n" +
               "  .form-busqueda button { padding: 10px 20px; background-color: #1a5f2d; color: white; border: none; border-radius: 4px; cursor: pointer; font-weight: bold; }\n" +
               "  .form-busqueda button:hover { background-color: #0d3d1f; }\n" +
               "  .form-busqueda .limpiar { background-color: #999; }\n" +
               "  .form-busqueda .limpiar:hover { background-color: #666; }\n" +
               "  table { width: 100%; border-collapse: collapse; background: white; box-shadow: 0 2px 4px rgba(0,0,0,0.1); border-radius: 8px; overflow: hidden; }\n" +
               "  thead { background-color: #1a5f2d; color: white; }\n" +
               "  th { padding: 15px; text-align: left; font-weight: bold; }\n" +
               "  td { padding: 12px 15px; border-bottom: 1px solid #eee; }\n" +
               "  tbody tr:hover { background-color: #f5f5f5; }\n" +
               "  tbody tr:nth-child(even) { background-color: #fafafa; }\n" +
               "  .precio { color: #d32f2f; font-weight: bold; font-size: 16px; }\n" +
               "  .error { background-color: #ffebee; border-left: 4px solid #d32f2f; padding: 15px; border-radius: 4px; margin-bottom: 20px; color: #b71c1c; }\n" +
               "  .info { background-color: #e8f5e9; border-left: 4px solid #1a5f2d; padding: 10px; border-radius: 4px; margin-bottom: 20px; color: #1a5f2d; }\n" +
               "  .no-data { background-color: #f5f5f5; padding: 20px; text-align: center; border-radius: 4px; color: #999; }\n" +
               "</style>\n";
    }
    
    /**
     * Construye el formulario de búsqueda
     * @param filtroAplicado Nombre del filtro actual
     * @return HTML del formulario
     */
    private static String construirFormularioBusqueda(String filtroAplicado) {
        StringBuilder form = new StringBuilder();
        form.append("<div class='form-busqueda'>\n");
        form.append("  <form method='GET' action='/'>\n");
        form.append("    <input type='text' name='producto' placeholder='Buscar producto...' ");
        if (filtroAplicado != null && !filtroAplicado.isEmpty()) {
            form.append("value='").append(filtroAplicado).append("'");
        }
        form.append(" />\n");
        form.append("    <button type='submit'>🔍 Buscar</button>\n");
        if (filtroAplicado != null && !filtroAplicado.isEmpty()) {
            form.append("    <a href='/'><button type='button' class='limpiar'>Limpiar</button></a>\n");
        }
        form.append("  </form>\n");
        form.append("</div>\n");
        return form.toString();
    }
    
    /**
     * Construye la tabla HTML con productos
     * @param productos Lista de productos
     * @return HTML de la tabla
     */
    private static String construirTablaProductos(List<Map<String, Object>> productos) {
        StringBuilder tabla = new StringBuilder();
        tabla.append("<table>\n");
        tabla.append("  <thead>\n");
        tabla.append("    <tr>\n");
        tabla.append("      <th>Producto</th>\n");
        tabla.append("      <th>Variedad</th>\n");
        tabla.append("      <th>Fuente</th>\n");
        tabla.append("      <th>Precio</th>\n");
        tabla.append("    </tr>\n");
        tabla.append("  </thead>\n");
        tabla.append("  <tbody>\n");
        
        for (Map<String, Object> producto : productos) {
            tabla.append("    <tr>\n");
            tabla.append("      <td>").append(escapeHTML(producto.get("nombre").toString())).append("</td>\n");
            tabla.append("      <td>").append(escapeHTML(producto.get("variedad").toString())).append("</td>\n");
            tabla.append("      <td>").append(escapeHTML(producto.get("fuente").toString())).append("</td>\n");
            tabla.append("      <td class='precio'>€").append(String.format("%.2f", producto.get("precio"))).append("</td>\n");
            tabla.append("    </tr>\n");
        }
        
        tabla.append("  </tbody>\n");
        tabla.append("</table>\n");
        
        return tabla.toString();
    }
    
    /**
     * Escapa caracteres HTML especiales para seguridad
     * @param texto Texto a escapar
     * @return Texto escapado
     */
    private static String escapeHTML(String texto) {
        return texto
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
    }
    
    /**
     * Construye la respuesta HTTP completa
     * @param htmlContent Contenido HTML
     * @return Respuesta HTTP formateada
     */
    public static String construirRespuestaHTTP(String htmlContent) {
        try {
            byte[] bytes = htmlContent.getBytes("UTF-8");
            return "HTTP/1.1 200 OK\r\n" +
                   "Content-Type: text/html; charset=UTF-8\r\n" +
                   "Content-Length: " + bytes.length + "\r\n" +
                   "Connection: close\r\n" +
                   "\r\n" +
                   htmlContent;
        } catch (Exception e) {
            return "HTTP/1.1 500 Internal Server Error\r\n" +
                   "Content-Type: text/plain; charset=UTF-8\r\n" +
                   "\r\n" +
                   "Error: " + e.getMessage();
        }
    }
}
