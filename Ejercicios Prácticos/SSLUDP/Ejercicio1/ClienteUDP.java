package SSLUDP.Ejercicio1;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class ClienteUDP {

    private final static int _MAX_INTENTOS = 5;
    private final static int _MAX_TIMEOUT = 3000; // 3s x 1000 milesimas
    private final static String _SALIR = "FIN";
    private static int puerto;
    private static String servidor;

    public static void main(String[] args) {
        if (args.length != 2) {
            throw new IllegalArgumentException("Uso incorrecto. Formato: <servidor> <puerto>");
        } else
            try {
                Scanner teclado = new Scanner(System.in);
                /* Asignación argumentos */
                servidor = args[0];
                puerto = Integer.parseInt(args[1]);
                /* Creo un Socket UDP, y el puerto lo asignará el sistema operativo */
                DatagramSocket cliUDP = new DatagramSocket();
                /* Establezco un tiempo límite de espera o TIMEOUT de 3 segundos */
                cliUDP.setSoTimeout(_MAX_TIMEOUT);
                String linea;
                while ((linea = teclado.nextLine()).equals(_SALIR) == false) {
                    int reintentos = 0;
                    boolean respuestaRecibida = false;
                    while (reintentos++ < _MAX_INTENTOS && !respuestaRecibida) {
                        /**
                         * Aquí es cuando se envía el paquete al servidor. A diferencia de TCP, no me
                         * tengo que conectar a un servidor, pero tengo que especificar la dirección: IP
                         * (llamada estática a InetAddress al método getByName) y puerto (dónde escucha
                         * exactamente). Entonces envío la cadena del escáner pasada a bytes, su
                         * longitud y el destino:
                         */
                        DatagramPacket enviar = new DatagramPacket(linea.getBytes(), linea.length(),
                                InetAddress.getByName(servidor), puerto);
                        cliUDP.send(enviar);
                        /* A partir de aquí esperamos la respuesta */
                        try {
                            byte[] bufferLlegada = new byte[linea.length()];
                            DatagramPacket recibir = new DatagramPacket(bufferLlegada, linea.length());
                            /* Comparamos la IP del servidor con la del paquete recibido */
                            // ¿El paquete viene del servidor ECHO?
                            InetAddress iprecibida = recibir.getAddress();
                            if (iprecibida.equals(InetAddress.getByName(servidor)) == false) {
                                // NO, Lanzamos excepción
                                throw new IOException("\nPaquete no esperado, recibido de " + recibir.getAddress());
                            } else {
                                // SÍ
                                respuestaRecibida = true;
                                /* Imprimimos lo que ha llegado */
                                System.out.println("Recibido " + new String(recibir.getData()));
                            }

                        } catch (IOException timeout) { // Situación anómala, se acaba el tiempo
                            System.err.println("TIMEOUT. Reintentando... " + timeout.getMessage());
                        }
                    }
                }
                teclado.close();
                cliUDP.close();
            } catch (IOException ioe) {
                System.err.println(ioe.getMessage());
            } catch (NumberFormatException nfe) {
                System.err.println(nfe.getMessage());
            }
    }
}
