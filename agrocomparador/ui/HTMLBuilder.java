package agrocomparador.ui;

import agrocomparador.business.ProductoService;
import java.util.*;

public class HTMLBuilder {

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

        html.append(construirInfoScraper());
        html.append(construirFormularioBusqueda(filtroAplicado));

        if (error != null && !error.isEmpty()) {
            html.append("<div class='error'>\n");
            html.append("<p><strong>Error:</strong> ").append(error).append("</p>\n");
            html.append("</div>\n");
        }

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
               "  .info-scraper { background-color: #e3f2fd; border-left: 4px solid #1976d2; padding: 15px; border-radius: 4px; margin-bottom: 20px; color: #0d47a1; font-size: 14px; }\n" +
               "  .scraper-stats { display: grid; grid-template-columns: repeat(4, 1fr); gap: 10px; margin-top: 10px; }\n" +
               "  .stat-box { background: white; padding: 10px; border-radius: 4px; text-align: center; border-top: 3px solid #1976d2; }\n" +
               "  .stat-value { font-size: 18px; font-weight: bold; color: #1976d2; }\n" +
               "  .stat-label { font-size: 12px; color: #666; margin-top: 5px; }\n" +
               "  .origen { display: inline-block; padding: 3px 8px; border-radius: 3px; font-size: 12px; font-weight: bold; }\n" +
               "  .origen-bd { background-color: #c8e6c9; color: #1a5f2d; }\n" +
               "  .origen-scraper { background-color: #bbdefb; color: #0d47a1; }\n" +
               "  .no-data { background-color: #f5f5f5; padding: 20px; text-align: center; border-radius: 4px; color: #999; }\n" +
               "</style>\n";
    }

    private static String construirInfoScraper() {
        StringBuilder info = new StringBuilder();
        info.append("<div class='info-scraper'>");
        info.append("<strong>📥 Datos en Vivo:</strong> Los precios incluyen información en tiempo real de agroprecios.com\n");

        try {
            Map<String, String> stats = ProductoService.obtenerEstadisticasScraper();

            if (stats != null && !stats.isEmpty()) {
                info.append("<div class='scraper-stats'>");

                info.append("<div class='stat-box'>");
                info.append("<div class='stat-value'>").append(stats.getOrDefault("total_registros", "0")).append("</div>");
                info.append("<div class='stat-label'>Registros Totales</div>");
                info.append("</div>");

                info.append("<div class='stat-box'>");
                info.append("<div class='stat-value'>").append(stats.getOrDefault("productos_unicos", "0")).append("</div>");
                info.append("<div class='stat-label'>Productos Únicos</div>");
                info.append("</div>");

                info.append("<div class='stat-box'>");
                info.append("<div class='stat-value'>€").append(stats.getOrDefault("precio_promedio", "0")).append("</div>");
                info.append("<div class='stat-label'>Precio Promedio</div>");
                info.append("</div>");

                String ultima = stats.getOrDefault("ultima_actualizacion", "No disponible");
                info.append("<div class='stat-box'>");
                info.append("<div class='stat-label' style='font-size: 11px;'>Última Actualización</div>");
                info.append("<div style='font-size: 10px; color: #0d47a1; margin-top: 5px;'>").append(ultima.substring(0, Math.min(10, ultima.length()))).append("</div>");
                info.append("</div>");

                info.append("</div>");
            }
        } catch (Exception e) {
            info.append("⚠️ No hay datos de scraper disponibles aún.");
        }

        info.append("</div>\n");
        return info.toString();
    }

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

    private static String construirTablaProductos(List<Map<String, Object>> productos) {
        StringBuilder tabla = new StringBuilder();
        tabla.append("<table>\n");
        tabla.append("  <thead>\n");
        tabla.append("    <tr>\n");
        tabla.append("      <th>Producto</th>\n");
        tabla.append("      <th>Variedad</th>\n");
        tabla.append("      <th>Fuente</th>\n");
        tabla.append("      <th>Precio</th>\n");
        tabla.append("      <th>Origen</th>\n");
        tabla.append("    </tr>\n");
        tabla.append("  </thead>\n");
        tabla.append("  <tbody>\n");

        for (Map<String, Object> producto : productos) {
            tabla.append("    <tr>\n");
            tabla.append("      <td>").append(escapeHTML(producto.get("nombre").toString())).append("</td>\n");
            tabla.append("      <td>").append(escapeHTML(producto.get("variedad").toString())).append("</td>\n");
            tabla.append("      <td>").append(escapeHTML(producto.get("fuente").toString())).append("</td>\n");
            tabla.append("      <td class='precio'>€").append(String.format("%.2f", producto.get("precio"))).append("</td>\n");

            String origen = producto.getOrDefault("origen", "BD").toString();
            String claseOrigen = origen.equals("SCRAPER") ? "origen-scraper" : "origen-bd";
            String labelOrigen = origen.equals("SCRAPER") ? "🌐 Scraper" : "🗄️ BD";
            tabla.append("      <td><span class='origen ").append(claseOrigen).append("'>").append(labelOrigen).append("</span></td>\n");

            tabla.append("    </tr>\n");
        }

        tabla.append("  </tbody>\n");
        tabla.append("</table>\n");

        return tabla.toString();
    }

    private static String escapeHTML(String texto) {
        return texto
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
    }

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
