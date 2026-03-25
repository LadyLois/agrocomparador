import java.io.*;
import java.net.*;
import java.sql.*;

public class HelloWeb {
    public static void main(String[] args) throws Exception {

        ServerSocket server = new ServerSocket(80);
        System.out.println("Servidor en puerto 80...");

        while (true) {
            Socket client = server.accept();
            OutputStream out = client.getOutputStream();

            String html = "<meta charset='UTF-8'><h1>Comparador de precios</h1>";

            try {
                Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/comparador",
                    "admin",
                    "AgroComparador2026!"
                );

                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT p.nombre, p.variedad, f.nombre AS fuente, pr.precio " +
    "FROM precios pr " +
    "JOIN productos p ON pr.producto_id = p.id " +
    "JOIN fuentes f ON pr.fuente_id = f.id"
);

                html += "<ul>";

                while (rs.next()) {
                    html += "<li>" 
                       + rs.getString("nombre") + " (" + rs.getString("variedad") + ") - "
        + rs.getString("fuente") + " → "
        + rs.getDouble("precio") + "€</li>"; 
                }

                html += "</ul>";
                conn.close();

            } catch (Exception e) {
                html += "<p>Error: " + e.getMessage() + "</p>";
            }

            String response = "HTTP/1.1 200 OK\r\nContent-Type: text/html; charset=UTF-8\r\n\r\n" + html;
            out.write(response.getBytes());
            out.flush();
            client.close();
        }
    }
}

