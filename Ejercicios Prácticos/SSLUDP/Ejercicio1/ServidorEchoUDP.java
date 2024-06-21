package SSLUDP.Ejercicio1;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ServidorEchoUDP {

    private final static int _MAX_LINE = 255;
    private static int puerto;

    public static void main(String[] args) {
        if (args.length != 1) {
            /* Sintaxis para lanzar in-situ una excepción de argumentos: */
            throw new IllegalArgumentException("\nUso de ServidorEchoUDP: <Puerto>\n");
        } else
            try {
                /* Asignamos el puerto */
                puerto = Integer.parseInt(args[0]);
                /* Socket Servidor UDP */
                DatagramSocket udpSocket = new DatagramSocket(puerto);
                /* Objeto "paquete" de longitud máxima = 255 */
                byte[] buffer = new byte[_MAX_LINE];
                DatagramPacket udpPacket = new DatagramPacket(buffer, _MAX_LINE);
                /* Servidor escucha por el puerto, esperamos paquetes infinitamente */
                while (true) {
                    /* Mi 'Datagram Socket' recibe paquete 'Datagram Packet' */
                    udpSocket.receive(udpPacket);
                    /* Gracias al paquete recibido, obtenemos la dirección (InetAddress a String) */
                    System.out.println("Conexión entrante de " + udpPacket.getAddress().getHostAddress());
                    /* Sacamos la información: obtenemos los datos del paquete, off=0 y longitud */
                    System.out.println("Recibido: " + new String(udpPacket.getData(), 0, udpPacket.getLength()));
                    /* Enviamos de vuelta el paquete, porque es un servidor ECHO */
                    udpSocket.send(udpPacket);
                    /* Reestablezco la cantidad máxima de datos a recibir */
                    udpPacket.setLength(_MAX_LINE);
                }
            } catch (NumberFormatException nfe) {
                System.err.println(nfe.getMessage());
            } catch (IOException ioe) { // Con esto controlo también SocketException
                System.err.println(ioe.getMessage());
            }
    }
}