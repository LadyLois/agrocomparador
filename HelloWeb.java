import java.io.*;
import java.net.*;

public class HelloWeb {
    public static void main(String[] args) throws Exception {
        ServerSocket server = new ServerSocket(80);
        System.out.println("Servidor en puerto 80...");

        while (true) {
            Socket client = server.accept();
            OutputStream out = client.getOutputStream();

            String response = "HTTP/1.1 200 OK\r\n\r\nHola mundo desde AWS 🚀";
            out.write(response.getBytes());
            out.flush();
            client.close();
        }
    }
}