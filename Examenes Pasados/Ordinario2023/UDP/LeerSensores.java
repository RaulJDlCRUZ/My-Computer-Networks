package Examen2023.UDP;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class LeerSensores {
    private static final int _PORT = 9999;
    private static final int _LECTURAS = 3;
    private static final String _SERV = "localhost";

    public static void main(String[] args) throws Exception {
        // Es un cliente, no requiere especificarse puerto
        DatagramSocket socket = new DatagramSocket();
        while (true) {
            // 3 bytes: X, Y, Z
            byte lecturas[] = new byte[_LECTURAS];
            for (int i = 0; i < _LECTURAS; i++) {
                // lecturas[i]=Sensor.leer();
            }
            DatagramPacket enviar = new DatagramPacket(lecturas, _LECTURAS, InetAddress.getByName(_SERV), _PORT);
            socket.send(enviar);
        }
    }
}

// public class UDPSensorReader {
//     private final int numeroLecturas = 3;
//     private final String servidor = "172.22.154.93";
//     private final int puerto = 9999;

//     public static void main(String[] args) {
//         UDPSensorReader udpSensorReader = new UDPSensorReader();
//         udpSensorReader.run();
//     }

//     public void run() {
//         try {
//             DatagramSocket socket = new DatagramSocket();
//             while (true) {
//                 byte lecturas[] = new byte[numeroLecturas];
//                 for (int i = 0; i < numeroLecturas; i++) {
//                     lecturas[i] = Sensor.leer();
//                 }

//                 DatagramPacket packet = new DatagramPacket(lecturas, lecturas.length,
//                         InetAddress.getByName(servidor), puerto);
//                 socket.send(packet);
//             }
//         } catch (SocketException e) {
//             e.printStackTrace();
//         } catch (UnknownHostException e) {
//             e.printStackTrace();
//         } catch (IOException e) {
//             e.printStackTrace();
//         }

//     }
// }