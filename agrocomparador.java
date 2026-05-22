import agrocomparador.ui.WebServer;
import agrocomparador.scraper.ScraperScheduler;

/**
 * Punto de entrada de la aplicación
 * Inicia el servidor web del Comparador de Precios Agrícolas
 * 
 * Características:
 * - Servidor HTTP en puerto 80
 * - Integración con web scraping de agroprecios.com
 * - Actualización automática de datos cada 60 minutos
 */
public class agrocomparador {
    public static void main(String[] args) throws Exception {
        // Iniciar scheduler de scraper en thread separado
        ScraperScheduler.getInstance().iniciar();
        
        // Iniciar servidor web
        WebServer.iniciar();
    }
}
