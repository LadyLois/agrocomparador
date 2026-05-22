package agrocomparador.ui;

import agrocomparador.business.ProductoService;
import agrocomparador.data.InformeSemanalDAO;
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

        html.append("""

        <div style='
            background:#1b4332;
            padding:18px 40px;
            margin-bottom:40px;
            display:flex;
            gap:30px;
            align-items:center;
        '>

            <a href='/'
            style='
                    color:white;
                    text-decoration:none;
                    font-weight:bold;
                    font-size:18px;'>

                🏠 Inicio

            </a>

            <a href='/historico'
            style='
                    color:white;
                    text-decoration:none;
                    font-weight:bold;
                    font-size:18px;'>

                📅 Evolución anual de precios

            </a>

            <a href='/semanal'
            style='
                    color:white;
                    text-decoration:none;
                    font-weight:bold;
                    font-size:18px;'>

                📈 Evolución semanal de precios

            </a>

            <a href='/comparativa'
            style='
                    color:white;
                    text-decoration:none;
                    font-weight:bold;
                    font-size:18px;'>

                📊 Comparativa precios/productos

            </a>

        </div>

        """);

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

    public static String construirHistoricoHTML(List<Map<String, Object>> datos) {

        if (datos == null || datos.isEmpty()) {
            return """
                <html>
                <body style='font-family:Arial;padding:40px;'>
                    <h1>No hay datos históricos disponibles</h1>
                    <a href='/'>⬅ Volver</a>
                </body>
                </html>
            """;
        }

        StringBuilder html = new StringBuilder();
        Map<Integer, List<Double>> preciosPorAnio = new HashMap<>();
        preciosPorAnio.put(2024, new ArrayList<>());
        preciosPorAnio.put(2025, new ArrayList<>());
        preciosPorAnio.put(2026, new ArrayList<>());

        String producto = datos.get(0).get("producto").toString().replace(" (100kg)", "");

        for (Map<String, Object> fila : datos) {

            int anio = (int) fila.get("anio");
            double precio = ((Number) fila.get("precio")).doubleValue();
            if (!fila.get("mes").equals("Anual") && precio > 0) {
                preciosPorAnio.get(anio).add(precio);
            }
        }

        double promedio2024 = preciosPorAnio.get(2024).stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double promedio2025 = preciosPorAnio.get(2025).stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double promedio2026 = preciosPorAnio.get(2026).stream().mapToDouble(Double::doubleValue).average().orElse(0);
        String precios2024 = preciosPorAnio.get(2024).toString();
        String precios2025 = preciosPorAnio.get(2025).toString();
        String precios2026 = preciosPorAnio.get(2026).toString();

        html.append("""
            <html>
            <head>

                <meta charset="UTF-8">

                <title>Histórico de precios</title>

                <style>

                    body {
                        font-family: Arial;
                        margin: 40px;
                        background-color: #f4f4f4;
                    }

                    table {
                        border-collapse: collapse;
                        width: 100%;
                        background: white;
                    }

                    th, td {
                        border: 1px solid #ccc;
                        padding: 10px;
                        text-align: center;
                    }

                    th {
                        background-color: #2e7d32;
                        color: white;
                    }

                    tr:nth-child(even) {
                        background-color: #f9f9f9;
                    }

                    canvas {
                        background: white;
                        padding: 20px;
                        border-radius: 10px;
                        margin-top: 20px;
                    }

                </style>

            </head>
            <body>
        """);

        html.append("<h1>Histórico de precios - " + producto + "</h1>");
        html.append("""
        <p style='
            margin-top:10px;
            color:#666;
            font-size:15px;
        '>

        Datos oficiales recopilados por el
        Ministerio de Agricultura de España.
        Precios medios nacionales orientados
        al seguimiento del mercado agrícola.

        </p>

        """);

        html.append("""
            <div style="
                display:flex;
                gap:20px;
                margin:30px 0;
                flex-wrap:wrap;
            ">
        """);

        // TARJETA 2024
        html.append("""
            <div style="
                background:white;
                padding:20px;
                border-radius:10px;
                min-width:180px;
                box-shadow:0 2px 8px rgba(0,0,0,0.1);
            ">
                <h3>2024</h3>
                <p style="font-size:28px; color:green;">
        """);

        html.append(String.format("%.2f €/kg", promedio2024));

        html.append("""
                </p>
                <small>Precio medio</small>
            </div>
        """);

        // TARJETA 2025
        html.append("""
            <div style="
                background:white;
                padding:20px;
                border-radius:10px;
                min-width:180px;
                box-shadow:0 2px 8px rgba(0,0,0,0.1);
            ">
                <h3>2025</h3>
                <p style="font-size:28px; color:blue;">
        """);

        html.append(String.format("%.2f €/kg", promedio2025 ));

        html.append("""
                </p>
                <small>Precio medio</small>
            </div>
        """);

        // TARJETA 2026
        html.append("""
            <div style="
                background:white;
                padding:20px;
                border-radius:10px;
                min-width:180px;
                box-shadow:0 2px 8px rgba(0,0,0,0.1);
            ">
                <h3>2026</h3>
                <p style="font-size:28px; color:red;">
        """);

        html.append(String.format("%.2f €/kg", promedio2026 ));

        html.append("""
                </p>
                <small>Precio medio</small>
            </div>
        """);

        html.append("</div>");

        // GRAFICA
        html.append("""
            <h2 style="
                margin-top:40px;
                margin-bottom:20px;
                color:#2e7d32;
            ">
                📈 Gráfica de precios
            </h2>

            <div style="
                width:100%;
                max-width:1100px;
                margin:auto;
            ">
                <canvas id="grafica"></canvas>
            </div>

            <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
        """);

        // BOTON TABLA
        html.append("""
            <button onclick="toggleTabla()"
                style="
                    padding:12px 20px;
                    background:#2e7d32;
                    color:white;
                    border:none;
                    border-radius:8px;
                    cursor:pointer;
                    margin:20px 0;
                ">
                Mostrar/Ocultar tabla detallada
            </button>
        """);

        // TABLA
        html.append("""
            <div style="margin-top:30px;">

                <a href="/"
                style="
                    display:inline-block;
                    margin-bottom:20px;
                    color:#2e7d32;
                    text-decoration:none;
                    font-weight:bold;
                ">
                    ⬅ Volver al inicio
                </a>

                <table id="tablaHistorico" style="display:none;">

                    <tr>
                        <th>Producto</th>
                        <th>Año</th>
                        <th>Mes</th>
                        <th>Precio €/kg</th>
                    </tr>
        """);

        for (Map<String, Object> fila : datos) {

            html.append("<tr>");

            html.append("<td>")
                .append(fila.get("producto").toString().replace(" (100kg)", ""))
                .append("</td>");

            html.append("<td>")
                .append(fila.get("anio"))
                .append("</td>");

            html.append("<td>")
                .append(fila.get("mes"))
                .append("</td>");

            html.append("<td>")
                .append(fila.get("precio"))
                .append("</td>");

            html.append("</tr>");
        }

        html.append("""
                </table>
            </div>
        """);

        // SCRIPT GRAFICA
        html.append("""
            <script>

            const ctx = document.getElementById('grafica');

            new Chart(ctx, {

                type: 'line',

                data: {

                    labels: [
                        'Enero','Febrero','Marzo','Abril',
                        'Mayo','Junio','Julio','Agosto',
                        'Septiembre','Octubre','Noviembre','Diciembre'
                    ],

                    datasets: [

                        {
                            label: '2024',
                            data:
        """);

        html.append(precios2024);

        html.append("""
                            ,
                            borderColor: 'green',
                            tension: 0.4,
                            fill: false
                        },

                        {
                            label: '2025',
                            data:
        """);

        html.append(precios2025);

        html.append("""
                            ,
                            borderColor: 'blue',
                            tension: 0.4,
                            fill: false
                        },

                        {
                            label: '2026',
                            data:
        """);

        html.append(precios2026);

        html.append("""
                            ,
                            borderColor: 'red',
                            tension: 0.4,
                            fill: false
                        }

                    ]
                }
            });

            function toggleTabla() {

                const tabla = document.getElementById("tablaHistorico");

                if (tabla.style.display === "none") {
                    tabla.style.display = "table";
                } else {
                    tabla.style.display = "none";
                }
            }

            </script>

            </body>
            </html>
        """);

        return html.toString();
    }

    public static String construirSemanalHTML(List<Map<String, Object>> datos) {

        if (datos == null || datos.isEmpty()) {

            return """
                <html>
                <body style='font-family:Arial;padding:40px;'>

                    <h1>
                        No hay datos semanales
                    </h1>

                    <a href='/'>
                        ⬅ Volver
                    </a>

                </body>
                </html>
            """;
        }

        String producto =
            datos.get(0)
            .get("producto")
            .toString();

        List<String> semanas =
            new ArrayList<>();

        List<Double> precios =
            new ArrayList<>();

        double suma = 0;
        double max = 0;
        double min = Double.MAX_VALUE;

        for (Map<String, Object> fila : datos) {

            String semana =
                fila.get("semana")
                .toString();

            double precio =
                ((Number) fila.get("precio"))
                .doubleValue();

            semanas.add("'" + semana + "'");
            precios.add(precio / 100);

            suma += precio;

            if (precio > max) {
                max = precio;
            }

            if (precio < min) {
                min = precio;
            }
        }

        double media =
            suma / precios.size();

        StringBuilder html =
            new StringBuilder();

        html.append("""

        <html>

        <head>

            <meta charset='UTF-8'>

            <title>
                Evolución semanal
            </title>

            <script src='https://cdn.jsdelivr.net/npm/chart.js'></script>

            <style>

                body {
                    font-family: Arial;
                    background:#f4f4f4;
                    margin:40px;
                }

                .cards {
                    display:flex;
                    gap:20px;
                    flex-wrap:wrap;
                    margin:30px 0;
                }

                .card {
                    background:white;
                    padding:20px;
                    border-radius:10px;
                    min-width:180px;
                    box-shadow:0 2px 8px rgba(0,0,0,0.1);
                }

                .valor {
                    font-size:28px;
                    font-weight:bold;
                    margin-top:10px;
                }

                canvas {
                    background:white;
                    padding:20px;
                    border-radius:10px;
                }

                table {
                    width:100%;
                    border-collapse:collapse;
                    background:white;
                    margin-top:30px;
                }

                th, td {
                    padding:12px;
                    border:1px solid #ddd;
                    text-align:center;
                }

                th {
                    background:#1565c0;
                    color:white;
                }

            </style>

        </head>

        <body>

        """);

        html.append(
            "<h1>📈 Evolución semanal - "
            + producto
            + "</h1>"
        );

        html.append("""

            <div class='cards'>

        """);

        html.append("""

            <div class='card'>
                <h3>Precio medio</h3>
                <div class='valor' style='color:green;'>

        """);

        html.append(
            String.format("%.2f €", media/100)
        );

        html.append("""

                </div>
            </div>

        """);

        html.append("""

            <div class='card'>
                <h3>Máximo</h3>
                <div class='valor' style='color:red;'>

        """);

        html.append(
            String.format("%.2f €", max / 100)
        );

        html.append("""

                </div>
            </div>

        """);

        html.append("""

            <div class='card'>
                <h3>Mínimo</h3>
                <div class='valor' style='color:blue;'>

        """);

        html.append(
            String.format("%.2f €", min / 100)
        );

        html.append("""

                </div>
            </div>

            </div>

        """);

        html.append("""

            <canvas id='grafica'></canvas>

        """);

        html.append("""

        <button onclick="toggleTabla()"
            style="
                padding:12px 20px;
                background:#1565c0;
                color:white;
                border:none;
                border-radius:8px;
                cursor:pointer;
                margin:20px 0;
            ">

            Mostrar/Ocultar tabla detallada

        </button>

        """);

        html.append("""

            <table id='tablaSemanal' style='display:none;'>

                <tr>
                    <th>Semana</th>
                    <th>Precio</th>
                </tr>

        """);

        for (Map<String, Object> fila : datos) {

            html.append("<tr>");

            html.append("<td>")
                .append(fila.get("semana"))
                .append("</td>");

            html.append("<td>")
                .append(String.format("%.2f €/kg",((Number) fila.get("precio")).doubleValue() / 100))
                .append("</td>");

            html.append("</tr>");
        }

        html.append("</table>");

        html.append("""

        <div style='margin-top:30px;'>

            <a href='/'
            style='color:#1565c0;
                    font-weight:bold;
                    text-decoration:none;'>

                ⬅ Volver

            </a>

        </div>

        """);

        html.append("""

        <script>

        new Chart(
            document.getElementById('grafica'),
            {

                type:'line',

                data: {

                    labels:
        """);

        html.append(semanas.toString());

        html.append("""

                    ,

                    datasets: [{

                        label:'Precio semanal',

                        data:
        """);

        html.append(precios.toString());
        

        html.append("""

                        ,

                        borderColor:'#1565c0',

                        tension:0.4,

                        fill:false
                    }]
                }
            }
        );
        function toggleTabla() {

            const tabla =
                document.getElementById(
                    "tablaSemanal"
                );

            if (tabla.style.display === "none") {

                tabla.style.display = "table";

            } else {

                tabla.style.display = "none";
            }
        }
                
        

        </script>

        </body>
        </html>

        """);

        return html.toString();
    }

    public static String construirPaginaHistorico() {

        StringBuilder html =
            new StringBuilder();

        html.append("""

        <html>

        <head>

            <meta charset='UTF-8'>

            <title>
                Histórico anual
            </title>

            <style>

                body {
                    font-family: Arial;
                    background:#f4f4f4;
                    margin:0;
                    padding:0;
                }

                .navbar {
                    background:#1b4332;
                    padding:18px 40px;
                    display:flex;
                    gap:30px;
                }

                .navbar a {
                    color:white;
                    text-decoration:none;
                    font-weight:bold;
                }

                .container {
                    max-width:1100px;
                    margin:auto;
                    padding:40px;
                }

                .productos {
                    display:grid;
                    grid-template-columns:
                        repeat(auto-fill,
                        minmax(220px,1fr));
                    gap:20px;
                    margin-top:40px;
                }

                .card {
                    background:white;
                    padding:25px;
                    border-radius:14px;
                    text-decoration:none;
                    color:#222;
                    box-shadow:
                        0 4px 12px
                        rgba(0,0,0,0.08);
                }

                .card:hover {
                    transform:translateY(-3px);
                }

            </style>

        </head>

        <body>

        <div class='navbar'>

            <a href='/'>🏠 Inicio</a>
            <a href='/historico'>📅 Evolución anual de precios</a>
            <a href='/semanal'>📈 Evolución semanal de precios</a>
            <a href='/comparativa'>📊 Comparativa precios/productos</a>

        </div>

        <div class='container'>

            <h1>
                📅 Evolución anual
            </h1>

            <p style='
                color:#666;
                margin-top:10px;
                line-height:1.6;'>

                Consulta cómo han evolucionado
                los precios agrícolas en España
                a lo largo de los últimos años.

                Datos oficiales del Ministerio
                de Agricultura de España.

            </p>

            <div class='productos'>

        """);

        String[][] productos = {

            {"🍅", "TOMATE"},
            {"🥒", "PEPINO"},
            {"🫑", "PIMIENTO"},
            {"🍆", "BERENJENA"},
            {"🥬", "CALABACIN"}

        };

        for (String[] p : productos) {

            html.append("""

            <a class='card'
            href='/?historico=
            """);

            html.append(p[1]);

            html.append("""

            '>

                <h2>

            """);

            html.append(p[0] + " " + p[1]);

            html.append("""

                </h2>

                <p style='margin-top:10px;color:#666;'>

                    Ver histórico anual

                </p>

            </a>

            """);
        }

        html.append("""

            </div>

        </div>

        </body>

        </html>

        """);

        return html.toString();
    }

    public static String construirPaginaSemanal() {

        StringBuilder html =
            new StringBuilder();

        List<Map<String, Object>> tendencias =
            InformeSemanalDAO
            .obtenerTendencias();

        html.append("""

        <html>

        <head>

            <meta charset='UTF-8'>

            <title>
                Evolución semanal
            </title>

            <style>

                body {
                    font-family: Arial;
                    background:#f4f4f4;
                    margin:0;
                    padding:0;
                }

                .navbar {
                    background:#1b4332;
                    padding:18px 40px;
                    display:flex;
                    gap:30px;
                }

                .navbar a {
                    color:white;
                    text-decoration:none;
                    font-weight:bold;
                }

                .container {
                    max-width:1100px;
                    margin:auto;
                    padding:40px;
                }

                .productos {
                    display:grid;
                    grid-template-columns:
                        repeat(auto-fill,
                        minmax(240px,1fr));
                    gap:20px;
                    margin-top:40px;
                }

                .card {
                    background:white;
                    padding:25px;
                    border-radius:14px;
                    text-decoration:none;
                    color:#222;
                    box-shadow:
                        0 4px 12px
                        rgba(0,0,0,0.08);

                    transition:0.2s;
                }

                .card:hover {
                    transform:translateY(-3px);
                }

                .emoji {
                    font-size:34px;
                    margin-bottom:15px;
                }

                .titulo {
                    font-size:22px;
                    font-weight:bold;
                    color:#1565c0;
                }

                .descripcion {
                    margin-top:10px;
                    color:#666;
                    line-height:1.5;
                }

            </style>

        </head>

        <body>

        <div class='navbar'>

            <a href='/'>🏠 Inicio</a>
            <a href='/historico'>📅 Evolución anual de precios</a>
            <a href='/semanal'>📈 Evolución semanal de precios</a>
            <a href='/comparativa'>📊 Comparativa precios/productos</a>

        </div>

        <div class='container'>

            <h1>
                📈 Evolución semanal
            </h1>

            <p style='
                color:#666;
                margin-top:10px;
                line-height:1.6;
                max-width:850px;'>

                Consulta la evolución semanal
                de precios agrícolas medios
                en €/kg para distintos productos.

                Datos oficiales del mercado
                agrícola español.

            </p>

            """);

            html.append("""
            <div class='productos'>
            """);

        String[][] productos = {

            {"🍅", "TOMATE Cereza"},
            {"🍅", "TOMATE Racimo"},
            {"🍅", "TOMATE Redondo"},
            {"🥒", "PEPINO"},
            {"🫑", "PIMIENTO"},
            {"🍆", "BERENJENA"},
            {"🥬", "CALABACÍN"}

        };

        for (String[] p : productos) {

            String emoji = p[0];
            String nombre = p[1];

            html.append("""

            <a class='card'
            href='/?semanal=
            """);

            html.append(nombre);

            html.append("""

            '>

                <div class='emoji'>

            """);

            html.append(emoji);

            html.append("""

                </div>

                <div class='titulo'>

            """);

            html.append(nombre);

            html.append("""

                </div>

                <div class='descripcion'>

                    Ver evolución semanal,
                    tendencias y variación
                    de precios agrícolas.

                </div>

            </a>

            """);
        }

        html.append("""

            </div>

        """);

        html.append("""

        <div style='
            display:grid;
            grid-template-columns:
                repeat(auto-fill,
                minmax(260px,1fr));
            gap:20px;
            margin-top:40px;
            margin-bottom:50px;
        '>

        """);

        for (Map<String, Object> fila : tendencias) {

            String producto =
                fila.get("producto")
                .toString();

            double precio =
                ((Number) fila.get("actual"))
                .doubleValue();

            double variacion =
                ((Number) fila.get("variacion"))
                .doubleValue();

            String semanaActual =
                fila.get("semanaActual")
                    .toString();

            String semanaAnterior =
                fila.get("semanaAnterior")
                    .toString();    
            
            boolean subida =
                variacion >= 0;

            String flecha =
                subida ? "↑" : "↓";

            String color =
                subida
                ? "#2e7d32"
                : "#c62828";

            html.append("""

            <div style='
                background:white;
                padding:25px;
                border-radius:16px;
                box-shadow:
                    0 4px 12px
                    rgba(0,0,0,0.08);
            '>

            """);

            html.append("""

                <div style='
                    font-size:22px;
                    font-weight:bold;
                    color:#1565c0;
                    margin-bottom:15px;'>

            """);

            html.append(producto);

            html.append("</div>");

            html.append("""

                <div style='
                    font-size:34px;
                    font-weight:bold;
                    margin-bottom:12px;'>

            """);

            html.append(
                String.format(
                    "%.2f €/kg",
                    precio
                )
            );

            html.append("</div>");

            html.append("""

                <div style='
                    font-size:22px;
                    font-weight:bold;
                    color:
            """);

            html.append(color);

            html.append(";'>");

            html.append(
                flecha + " " +
                String.format(
                    "%.1f%%",
                    Math.abs(variacion)
                )
            );

            html.append("</div>");

            html.append("""

                <div style='
                    margin-top:12px;
                    color:#666;
                    font-size:15px;'>

                    Comparativa:

                </div>

            """);

            html.append("""

                <div style='
                    margin-top:6px;
                    font-size:16px;
                    font-weight:bold;
                    color:#444;'>

            """);

            html.append(
                semanaActual
                + " vs "
                + semanaAnterior
            );

            html.append("</div>");

            html.append("""

                </div>

            """);

        }

        html.append("</div>");

        html.append("""

            </div>

        </div>

        </body>

        </html>

        """);

        return html.toString();
    }

    public static String construirPaginaComparativa() {

        List<Map<String, Object>> comparativa =
            InformeSemanalDAO
            .obtenerComparativaProductos();

        StringBuilder html =
            new StringBuilder();

        html.append("""

        <html>

        <head>

            <meta charset='UTF-8'>

            <title>
                Comparativa de precios
            </title>

            <style>

                body {
                    font-family: Arial;
                    background:#f4f4f4;
                    margin:0;
                    padding:0;
                }

                .navbar {
                    background:#1b4332;
                    padding:18px 40px;
                    display:flex;
                    gap:30px;
                }

                .navbar a {
                    color:white;
                    text-decoration:none;
                    font-weight:bold;
                }

                .container {
                    max-width:1100px;
                    margin:auto;
                    padding:40px;
                }

                .bloque {
                    background:white;
                    padding:35px;
                    border-radius:18px;
                    box-shadow:
                        0 4px 14px
                        rgba(0,0,0,0.06);
                    margin-top:35px;
                }

                .barra {
                    width:100%;
                    height:18px;
                    background:#e9ecef;
                    border-radius:999px;
                    overflow:hidden;
                    margin-top:10px;
                }

                .relleno {
                    height:100%;
                    border-radius:999px;
                    background:
                        linear-gradient(
                            90deg,
                            #2d6a4f,
                            #52b788
                        );
                }

            </style>

        </head>

        <body>

        <div class='navbar'>

            <a href='/'>🏠 Inicio</a>
            <a href='/historico'>
                📅 Evolución anual de precios
            </a>

            <a href='/semanal'>
                📈 Evolución semanal de precios
            </a>

            <a href='/comparativa'>
                📊 Comparativa precios/productos
            </a>

        </div>

        <div class='container'>

            <h1>
                📊 Comparativa de precios agrícolas
            </h1>

            <p style='
                color:#666;
                line-height:1.7;
                max-width:850px;'>

                Consulta y compara precios
                medios agrícolas en €/kg
                para distintos productos.

                Esta información puede ayudar
                al seguimiento del mercado
                y a la toma de decisiones
                agrícolas.

            </p>

            <div class='bloque'>

        """);

        double maxPrecio = 0;

        for (Map<String, Object> fila : comparativa) {

            double precio =
                ((Number) fila.get("promedio"))
                .doubleValue() / 100;

            if (precio > maxPrecio) {
                maxPrecio = precio;
            }
        }

        for (Map<String, Object> fila : comparativa) {

            String producto =
                fila.get("producto")
                .toString();

            double precio =
                ((Number) fila.get("promedio"))
                .doubleValue() / 100;

            double porcentaje =
                (precio / maxPrecio) * 100;

            html.append("""

            <div style='margin-bottom:35px;'>

            """);

            html.append("""

            <div style='
                display:flex;
                justify-content:space-between;
                font-size:20px;
                font-weight:bold;'>

            """);

            html.append("<span>");
            html.append(producto);
            html.append("</span>");

            html.append("<span style='color:#2d6a4f;'>");

            html.append(
                String.format(
                    "%.2f €/kg",
                    precio
                )
            );

            html.append("</span>");

            html.append("</div>");

            html.append("<div class='barra'>");

            html.append("""

            <div class='relleno'
            style='width:
            """);

            html.append(
                String.format("%.1f", porcentaje)
            );

            html.append("%'></div>");

            html.append("</div>");

            html.append("</div>");
        }

        html.append("""

            </div>

        </div>

        </body>

        </html>

        """);

        return html.toString();
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
