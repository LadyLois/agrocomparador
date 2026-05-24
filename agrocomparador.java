import agrocomparador.ui.WebServer;

/**
 * Punto de entrada de la aplicación
 * Inicia el servidor web del Comparador de Precios Agrícolas
 *
 * La carga de datos se realiza manualmente desde la interfaz (botón Cargar datos).
 * Para proteger el botón Vaciar, define la variable de entorno ADMIN_PASSWORD.
 */
public class agrocomparador {
    public static void main(String[] args) throws Exception {
        WebServer.iniciar();
    }
}
