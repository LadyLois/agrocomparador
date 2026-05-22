import agrocomparador.ui.WebServer;
import agrocomparador.scraper.ScraperScheduler;
import agrocomparador.data.MinisterioExcelDAO;
import agrocomparador.data.InformeSemanalDAO;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
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

        //MinisterioExcelDAO.leerExcel("/mnt/c/Users/PC/Desktop/InformeSemanales/Indices y Precios Percibidos Agrarios (enero 2024-enero 2026).xlsx");
        
        InformeSemanalDAO.importarTodosLosInformes();   

        // Iniciar scheduler de scraper en thread separado
        //ScraperScheduler.getInstance().iniciar();

        // Iniciar servidor web
        WebServer.iniciar();
    }
}
