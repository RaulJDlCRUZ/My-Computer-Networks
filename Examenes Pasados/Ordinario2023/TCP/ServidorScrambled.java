package Examen2023.TCP;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class ServidorScrambled {
    private static final int __MAX_LENGTH = 199;
    private static final int _PORT = 1721;
    private static ServerSocket serversk;

    public static void main(String[] args) {
        /**
         * En este caso no proceso la línea de órdenes porque el único parámetro que
         * necesito ya lo tengo, el puerto por donde escucha el servidor
         */
        ServidorScrambled servidor = new ServidorScrambled(); // Instancia de servidor
        servidor.start();
    }

    public void start() {
        try {
            serversk = new ServerSocket(_PORT); // Arrancar servidor
            System.out.println("Servidor arrancado");
            while (true) {
                Socket cliente = serversk.accept();
                Hilo hilo = new Hilo(cliente); // al nuevo hilo le tengo que pasar el socket del cliente
                hilo.start();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public class Hilo extends Thread {
        Socket miSocket;

        /* Constructor */
        Hilo(Socket s) {
            miSocket = s;
        }

        @Override
        public void run() {
            try {
                // Streams y buffer de datos
                InputStream input = miSocket.getInputStream();
                OutputStream output = miSocket.getOutputStream();
                byte[] buffer = new byte[__MAX_LENGTH];
                boolean fin = false;

                while (!fin) {
                    // Leo lo que me ha escrito el cliente
                    input.read(buffer, 0, __MAX_LENGTH);
                    // Separo por palabras, con espacios
                    String[] tokens = new String(buffer).split(" ");

                    for (int i = 0; i < tokens.length; i++) {
                        String cadena = tokens[i].replace("\n", "");
                        if (cadena.equals("FIN")) {
                            fin = true;
                            output.write("NIF\n".getBytes());
                            miSocket.close();
                            return;
                        }
                        cadena = scramble(cadena);
                        output.write((cadena + " ").getBytes());
                    }
                    output.write("\n".getBytes());
                    output.flush();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    public static String scramble(String S) {
        if (S.length() <= 1) {
            return S;
        } else {
            Random rand = new Random();
            // Devolverá aleatorios entre 0 y la longitud de petición, -1
            int indiceAleatorio = rand.nextInt(S.length());
            // Dos subcadenas, S = X+Y
            String X = S.substring(0, indiceAleatorio);
            String Y = S.substring(indiceAleatorio, S.length());
            // System.out.println(X+ " - " + Y);
            return scramble(Y) + scramble(X);
        }
    }
}
