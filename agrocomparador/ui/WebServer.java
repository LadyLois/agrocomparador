package agrocomparador.ui;

import agrocomparador.business.ProductoService;
import java.io.*;
import java.net.*;
import java.util.*;

public class WebServer {
    private static final int PUERTO = Integer.parseInt(
        System.getenv().getOrDefault("PORT", "8080")
    );

    public static void iniciar() throws Exception {
        ServerSocket server = new ServerSocket(PUERTO);
        System.out.println("🚀 Servidor iniciado en puerto " + PUERTO);
        System.out.println("📍 Accede a: http://localhost:" + PUERTO + "/");

        while (true) {
            Socket cliente = server.accept();
            new Thread(() -> manejarSolicitud(cliente)).start();
        }
    }

    private static void manejarSolicitud(Socket cliente) {
        try {
            BufferedReader entrada = new BufferedReader(new InputStreamReader(cliente.getInputStream(), "UTF-8"));
            String primeraLinea = entrada.readLine();

            if (primeraLinea == null) { cliente.close(); return; }

            String[] partes = primeraLinea.split(" ");
            String ruta = partes.length > 1 ? partes[1] : "/";
            String rutaBase = ruta.contains("?") ? ruta.split("\\?")[0] : ruta;
            Map<String, String> params = parsearQueryString(ruta.contains("?") ? ruta.split("\\?", 2)[1] : "");

            OutputStream salida = cliente.getOutputStream();

            if (rutaBase.equals("/vaciar")) {
                String fechaVaciar = params.get("fecha");
                if (fechaVaciar != null && !fechaVaciar.trim().isEmpty()) {
                    fechaVaciar = URLDecoder.decode(fechaVaciar, "UTF-8").trim();
                    ProductoService.vaciarDatosScraperPorFecha(fechaVaciar);
                } else {
                    ProductoService.vaciarDatosScraper();
                }
                enviarRedireccion(salida, "/?accion=vaciado");
                cliente.close();
                return;
            }

            if (rutaBase.equals("/cargar")) {
                String fechaCarga = params.get("fecha");
                if (fechaCarga != null) fechaCarga = URLDecoder.decode(fechaCarga, "UTF-8").trim();
                ProductoService.forzarCargaDatos(fechaCarga != null && !fechaCarga.isEmpty() ? fechaCarga : null);
                enviarRedireccion(salida, "/?accion=cargando");
                cliente.close();
                return;
            }

            String filtroProducto = params.get("producto");
            if (filtroProducto != null) filtroProducto = URLDecoder.decode(filtroProducto, "UTF-8");
            String filtroFechaDesde = params.get("fechaDesde");
            if (filtroFechaDesde != null) filtroFechaDesde = URLDecoder.decode(filtroFechaDesde, "UTF-8");
            String filtroFechaHasta = params.get("fechaHasta");
            if (filtroFechaHasta != null) filtroFechaHasta = URLDecoder.decode(filtroFechaHasta, "UTF-8");
            String accion = params.get("accion");

            List<Map<String, Object>> productos = null;
            String error = null;

            try {
                String desde = filtroFechaDesde != null ? filtroFechaDesde.trim() : "";
                String hasta  = filtroFechaHasta != null ? filtroFechaHasta.trim() : "";
                List<Map<String, Object>> base;
                if (!desde.isEmpty() || !hasta.isEmpty()) {
                    base = ProductoService.obtenerProductosPorRangoCombinados(desde, hasta);
                } else {
                    base = ProductoService.obtenerTodosLosProductosCombinados();
                }
                if (filtroProducto != null && !filtroProducto.trim().isEmpty()) {
                    final String fp = filtroProducto;
                    productos = base.stream()
                        .filter(p -> ProductoService.coincidePublico(p, fp))
                        .collect(java.util.stream.Collectors.toList());
                } else {
                    productos = base;
                }
            } catch (Exception e) {
                error = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                System.err.println("❌ Error: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            }

            String htmlContent = HTMLBuilder.construirRespuestaHTML(productos, error, filtroProducto, accion, filtroFechaDesde, filtroFechaHasta);
            String respuestaHTTP = HTMLBuilder.construirRespuestaHTTP(htmlContent);

            salida.write(respuestaHTTP.getBytes("UTF-8"));
            salida.flush();
            salida.close();
            cliente.close();

        } catch (Exception e) {
            System.err.println("❌ Error al manejar solicitud: " + e.getMessage());
        }
    }

    private static Map<String, String> parsearQueryString(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isEmpty()) return params;
        for (String par : query.split("&")) {
            String[] kv = par.split("=", 2);
            if (kv.length == 2) params.put(kv[0], kv[1]);
            else if (kv.length == 1) params.put(kv[0], "");
        }
        return params;
    }

    private static void enviarRedireccion(OutputStream salida, String url) throws IOException {
        String respuesta = "HTTP/1.1 302 Found\r\nLocation: " + url + "\r\nConnection: close\r\n\r\n";
        salida.write(respuesta.getBytes("UTF-8"));
        salida.flush();
    }
}
























