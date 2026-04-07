package agrocomparador.ui;

import agrocomparador.business.ProductoService;
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
            
            // Extraer parámetro de búsqueda
            String filtroProducto = null;
            if (ruta.contains("?")) {
                String[] rutaParts = ruta.split("\\?");
                String queryString = rutaParts[1];
                
                if (queryString.startsWith("producto=")) {
                    filtroProducto = queryString.substring("producto=".length());
                    // Decodificar URL
                    filtroProducto = URLDecoder.decode(filtroProducto, "UTF-8");
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
            String htmlContent = HTMLBuilder.construirRespuestaHTML(productos, error, filtroProducto);
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
