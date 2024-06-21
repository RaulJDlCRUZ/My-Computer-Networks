package TCP.Ejercicio6;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class ServidorMultiHilo {
    private final static int _MAX_LENGTH = 1024;
    private static int puerto;
    private static ServerSocket ss;

    /**
     * Tratamiento de la línea de órdenes y llamada al método de arranque del
     * servidor
     * 
     * @param args
     */
    public static void main(String[] args) {
        if (args.length != 1)
            System.err.println("Uso de ServidorMultiHilo: <puerto>");
        else
            try {
                /**
                 * Debido a la naturaleza de los hilos, será más facil declarar un objeto de
                 * esta clase, para llamar al start y utilizar las variables de clase.
                 */
                puerto = Integer.parseInt(args[0]);
                ServidorMultiHilo servidor = new ServidorMultiHilo();
                servidor.start();
            } catch (NumberFormatException nfe) {
                System.err.println(nfe.getMessage());
                nfe.printStackTrace();
            }
    }

    /**
     * Creación del socket servidor según el puerto y arranque del servidor. En todo
     * momento se crea un socket cliente que será la respuesta (aceptar) del
     * servidor. Después, se llama al método sirve (de peticiones), al que le
     * pasamos el socket del cliente
     */
    public void start() {
        try {
            ss = new ServerSocket(puerto);
            while (true) {
                Socket cliente = ss.accept();
                sirve(cliente);
            }
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
            ioe.printStackTrace();
        }
    }

    /**
     * El método sirve realmente se basa únicamente en la creación de un hilo, el
     * cual tiene como parámetro el socket cliente. Cuando se crea el objeto (a
     * través del constructor), se llama a su método start, que es proceder con el
     * inicio del contenido del hilo.
     * 
     * @param cliente el socket cliente
     */
    public void sirve(Socket cliente) {
        Hilo thr = new Hilo(cliente);
        thr.start();
    }

    /**
     * Sub-clase Hilo (extiende del objeto Thread de la API de Java). Posee método
     * constructor, donde se asigna al socket local el socket llegado como
     * parámetro. Después tendrá su método run(), donde contiene la serie de
     * instrucciones que ejecutará el hilo.
     */
    public class Hilo extends Thread {
        Socket mSocket; // referencia "miembro" al socket Cliente

        Hilo(Socket s) {
            mSocket = s;
        }

        @Override
        public void run() { // ! ES CONCURRENTE!
            // si llega aquí, ha entrado una conexión
            System.out.println("Entra conexión de " + mSocket.getRemoteSocketAddress());
            try {
                /* Espera aleatoria de hasta 2 segundos para SIMULAR procesado */
                Random r = new Random();
                int espera = r.nextInt(2000);
                Thread.sleep(espera);
            } catch (InterruptedException ie) {
                System.err.println(ie.getMessage());
                ie.printStackTrace();
            }

            try {
                /* Ejecución de la petición ECHO */
                InputStream is = mSocket.getInputStream();
                OutputStream os = mSocket.getOutputStream();
                byte[] buffer = new byte[_MAX_LENGTH];
                int bytesLeidos = is.read(buffer, 0, _MAX_LENGTH);
                if (bytesLeidos != -1) {
                    // hemos recibido un request de ECHO-> mandar el response
                    String cadenaLeida = new String(buffer);
                    os.write(cadenaLeida.getBytes()); // response
                }
                mSocket.close(); // Cerramos cliente y procesamos el siguiente REQUEST
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
