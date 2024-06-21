package TCP.Ejercicio7;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;

public class ServidorHTTP {
    private final static int _MAX_LENGTH = 1024;
    private final static String _err_400 = "<html><body><p>ERROR 400 - BAD REQUEST</p></body></html>";
    private final static String _err_404 = "<html><body><p>ERROR 404 - NOT FOUND</p></body></html>";
    private final static String _err_405 = "<html><body><p>ERROR 405 - METHOD NOT ALLOWED</p></body></html>";
    private static int puerto;
    private static String wwwroot; // Directorio raíz
    private static ServerSocket ss;

    /**
     * El método principal se encarga del tratamiento de la línea de órdenes.
     * Después, se crea un objeto servidor para poder arrancarlo.
     * 
     * @param args
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Uso de ServidorHTTP: <puerto servidor> <directorio raíz>");
        } else {
            try {
                puerto = Integer.parseInt(args[0]);
                wwwroot = args[1];
                ServidorHTTP servidor = new ServidorHTTP();
                servidor.start();

            } catch (NumberFormatException nfe) {
                System.err.println(nfe.getMessage());
            }
        }
    }

    /**
     * Se asigna el ServerSocket al puerto, arrancando el server, funcionando
     * infinitamente. Cada socket cliente será la aceptación del servidor, y a su
     * vez se crea un hilo, con parámetro el socket cliente, y arrancando,
     * ejecutándose concurrentemente.
     */
    public void start() {
        try {
            ss = new ServerSocket(puerto); // Servidor escuchando...
            while (true) {
                Socket cliente = ss.accept();
                Hilo thr = new Hilo(cliente); // Hilo para cada cliente aceptado
                thr.start();
            }
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }
    }

    /**
     * Un hilo posee un constructor, que es el socket del hilo "miSocket". Su método
     * ejecutable consiste en procesar la línea ejecutada en el cliente, para que
     * acepte únicamente la petición GET de HTTP1.1.
     */
    public class Hilo extends Thread {
        Socket miSocket; // Cliente aceptado

        Hilo(Socket cli) {
            miSocket = cli;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[_MAX_LENGTH];
            int bytesLeidos;
            String url;
            try {
                System.out.println("Cliente de " + miSocket.getPort() + " a " + miSocket.getInetAddress() + ":"
                        + miSocket.getLocalPort() + " aceptado.");
                InputStream is = miSocket.getInputStream();
                OutputStream os = miSocket.getOutputStream();
                bytesLeidos = is.read(buffer);
                String peticionHTML = new String(buffer);
                String[] params = peticionHTML.split("\n");
                /* Nos aseguramos de que se haya leído bien la petición */
                if (params.length > 0 && bytesLeidos != -1) {
                    String[] tokens = params[0].split(" "); // Separamos la línea en tokens. Deben ser 3...
                    if (tokens.length == 3) {
                        System.out.println(tokens[0] + " " + tokens[1]);
                        /* Si el primer token es el comando GET, lo servimos */
                        if (tokens[0].equals("GET")) {
                            url = tokens[1];
                            sirve(url, os);
                        } else {
                            os.write(_err_405.getBytes());
                        }
                    } else {
                        os.write(_err_400.getBytes());
                    }
                } else {
                    os.write(_err_400.getBytes());
                }

            } catch (IOException ioe) {
                System.err.println(ioe.getMessage());
                // ioe.printStackTrace();
            }
        }
    }

    /**
     * En primer lugar se analizará la url que solicitará el cliente. Distinguimos
     * de la barra (index) de todo lo demás. Una vez tenemos la dirección del
     * archivo, se escriben todos sus bytes (contenido) por el OutputStream.
     * 
     * @param url La dirección de cada archivo en la web de yoga
     * @param out Stream de escritura
     */
    public void sirve(String url, OutputStream out) {
        String Ok = "HTTP/1.0 200 OK\n";
        if (url.equals("/")) {
            url = wwwroot + "/index.html";
        } else {
            url = wwwroot + url;
        }

        try {
            File fichero = new File(url);
            if (fichero.exists()) {
                System.out.println("... Sirviendo " + url);
                byte[] bytes = Files.readAllBytes(fichero.toPath());
                out.write(Ok.getBytes());
                out.write(bytes);
                out.flush();
            } else {
                out.write(("HTTP/1.1 404 Not Found\n\n" + _err_404).getBytes());
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

    }
}
