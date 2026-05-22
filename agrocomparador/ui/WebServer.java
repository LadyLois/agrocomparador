package agrocomparador.ui;

import agrocomparador.business.ProductoService;
import agrocomparador.data.MinisterioExcelDAO;
import agrocomparador.data.InformeSemanalDAO;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Servidor Web HTTP que maneja solicitudes
 * Coordina entre la capa visual y la capa de lógica/datos
 * 
 * Soporta:
 * - GET / : Muestra todos los productos
 * - GET /?producto=Tomate : Muestra productos filtrados por nombre
 */
public class WebServer {
    // Puerto configurable desde variable de entorno (por defecto 8080)
    private static final int PUERTO = Integer.parseInt(
        System.getenv().getOrDefault("PORT", "8080")
    );
    
    /**
     * Inicia el servidor web
     * @throws Exception si hay error al iniciar
     */
    public static void iniciar() throws Exception {
        ServerSocket server = new ServerSocket(PUERTO);
        System.out.println("🚀 Servidor iniciado en puerto " + PUERTO);
        System.out.println("📍 Accede a: http://localhost:" + PUERTO + "/");
        
        while (true) {
            Socket cliente = server.accept();
            // Ejecutar en hilo separado para no bloquear
            new Thread(() -> manejarSolicitud(cliente)).start();
        }
    }
    
    /**
     * Maneja una solicitud HTTP individual
     * @param cliente Socket del cliente
     */
    private static void manejarSolicitud(Socket cliente) {
        try {
            // Leer la solicitud HTTP
            BufferedReader entrada = new BufferedReader(new InputStreamReader(cliente.getInputStream(), "UTF-8"));
            String primeraLinea = entrada.readLine();
            
            if (primeraLinea == null) {
                cliente.close();
                return;
            }
            
            // Parsear la solicitud (ej: "GET /?producto=Tomate HTTP/1.1")
            String[] partes = primeraLinea.split(" ");
            String ruta = partes.length > 1 ? partes[1] : "/";

            if (ruta.equals("/historico")) {

                String html =
                    HTMLBuilder
                    .construirPaginaHistorico();

                String respuesta =
                    HTMLBuilder
                    .construirRespuestaHTTP(html);

                OutputStream salida =
                    cliente.getOutputStream();

                salida.write(
                    respuesta.getBytes("UTF-8")
                );

                salida.flush();
                salida.close();
                cliente.close();

                return;
            }

            if (ruta.equals("/semanal")) {

                String html =
                    HTMLBuilder
                    .construirPaginaSemanal();

                String respuesta =
                    HTMLBuilder
                    .construirRespuestaHTTP(html);

                OutputStream salida =
                    cliente.getOutputStream();

                salida.write(
                    respuesta.getBytes("UTF-8")
                );

                salida.flush();
                salida.close();
                cliente.close();

                return;
            }

            if (ruta.equals("/comparativa")) {

                String html =
                    HTMLBuilder
                    .construirPaginaComparativa();

                String respuesta =
                    HTMLBuilder
                    .construirRespuestaHTTP(html);

                OutputStream salida =
                    cliente.getOutputStream();

                salida.write(
                    respuesta.getBytes("UTF-8")
                );

                salida.flush();
                salida.close();
                cliente.close();

                return;
            }
            
            // Extraer parámetro de búsqueda
            String filtroProducto = null;
            String historicoProducto = null;
            String semanalProducto = null;
            if (ruta.contains("?")) {
                String[] rutaParts = ruta.split("\\?");
                String queryString = rutaParts[1];
                
                if (queryString.startsWith("producto=")) {

                    filtroProducto = queryString.substring("producto=".length());
                    filtroProducto = URLDecoder.decode(filtroProducto, "UTF-8");

                } else if (queryString.startsWith("historico=")) {

                    historicoProducto = queryString.substring("historico=".length());
                    historicoProducto = URLDecoder.decode(historicoProducto, "UTF-8");
                }else if (queryString.startsWith("semanal=")) {

                    semanalProducto =
                        queryString.substring(
                            "semanal=".length()
                        );

                    semanalProducto =
                        URLDecoder.decode(
                            semanalProducto,
                            "UTF-8"
                        );
                }
            }
            
            // Obtener datos
            List<Map<String, Object>> productos = null;
            String error = null;
            
            try {
                if (filtroProducto != null && !filtroProducto.trim().isEmpty()) {
                    productos = ProductoService.obtenerProductosPorNombre(filtroProducto);
                } else {
                    productos = ProductoService.obtenerTodosLosProductos();
                }
            } catch (Exception e) {
                error = e.getMessage();
                System.err.println("❌ Error: " + error);
                e.printStackTrace();
            }
            
            // Construir respuesta HTML
            String htmlContent;

            if (historicoProducto != null) {

                List<Map<String, Object>> datosHistorico = MinisterioExcelDAO.obtenerHistoricoProducto(historicoProducto);

                htmlContent = HTMLBuilder.construirHistoricoHTML(datosHistorico);

            }else if (semanalProducto != null) {
                List<Map<String, Object>> datosSemanales = InformeSemanalDAO.obtenerHistoricoSemanal(semanalProducto);
                htmlContent =HTMLBuilder.construirSemanalHTML(datosSemanales);
            }else {
                htmlContent = HTMLBuilder.construirRespuestaHTML(productos, error, filtroProducto);
            }
            
            String respuestaHTTP = HTMLBuilder.construirRespuestaHTTP(htmlContent);
            
            // Enviar respuesta
            OutputStream salida = cliente.getOutputStream();
            salida.write(respuestaHTTP.getBytes("UTF-8"));
            salida.flush();
            salida.close();
            cliente.close();
            
        } catch (Exception e) {
            System.err.println("❌ Error al manejar solicitud: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
