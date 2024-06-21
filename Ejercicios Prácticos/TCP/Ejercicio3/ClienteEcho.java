package TCP.Ejercicio3;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ClienteEcho { // o SocketClienteEcho
    private static Socket s;

    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Uso de ClienteEcho: <servidor> <palabra> <puerto>");
        } else {
            try {
                System.out.println("Argumentos recogidos: <" + args[0] + "> <" + args[1] + "> <" + args[2] + ">");
                int puerto = Integer.parseInt(args[2]);
                /* 1. Abre el socket al puerto del servidor */
                s = new Socket(args[0], puerto);
                /* 2. Captura los streams de entrada y salida */
                InputStream is = s.getInputStream(); // Ya se puede leer
                OutputStream os = s.getOutputStream(); // Ya se puede escribir

                /**
                 * Protocolo de comunicaciones de nivel de aplicación aplicado a nuestro
                 * servidor echo: Envío una palabra, recibo la misma palabra
                 */

                /* 3. Envía la palabra a través del OutputStream */
                os.write(args[1].getBytes());
                /* 4. Recibe bytes de la palabra de vuelta o se cierra la conexión */
                int bytesEsperados = args[1].length(), bytesLeidos = 0, bytesLeidosTotales = 0;
                /* Sé cuántos bytes me van a llegar porque es un servidor echo */
                byte[] buffer = new byte[bytesEsperados]; // Buffer de lectura

                /**
                 * En todo momento el número de bytes leídos en total tiene que ser inferior al
                 * tamaño de la palabra enviada al servidor de echo, y que, por supuesto, que su
                 * longitud (al realizarse la lectura) no sea -1, ya que esto es el resultado de
                 * un error en la comunicación.
                 */

                while (bytesLeidosTotales < bytesEsperados && bytesEsperados != -1) {
                    // ! operación bloqueante, hasta que hay datos disponibles o cierra conexión
                    bytesLeidos = is.read(buffer, 0, bytesEsperados);
                    if (bytesLeidos != -1) {
                        bytesLeidosTotales += bytesLeidos;
                    }
                }

                /**
                 * Cuando termina el proceso de lectura/escritura, hacemos una comprobación
                 * (similar a una cheksum) para verificar que llegaron todos los bytes. En caso
                 * afirmativo, la interacción tuvo un resultado exitoso, en caso contrario el
                 * canal de la comunicación se rompió en algún punto.
                 */

                if (bytesLeidosTotales == bytesEsperados) {
                    System.out.println("Recibido: " + new String(buffer));
                } else {
                    System.err.println("Comunicación rota");
                }
                /* El servidor cerrará la conexión del cliente */

            } catch (IOException ioe) { // Ya maneja UnknownHostException
                System.err.println(ioe.getMessage());
                ioe.printStackTrace();
            } catch (NumberFormatException nfe) {
                // Un número no se ha escrito correctamente. Por ejemplo el puerto
                System.err.println(nfe.getMessage());
                nfe.printStackTrace();
            }
        }
    }
}
