package TCP.Ejercicio4;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServidorEcho {
    /**
     * Variables privadas y estáticas del servidor:
     * el puerto (entero) y el socket servidor (ServerSocket)
     */
    private final static int _MAX_LENGTH = 1024;
    private static int puerto;
    private static ServerSocket serversocket;

    /**
     * El método main servirá únicamente para recoger el parámetro requerido, el
     * puerto por donde escuchará el servidor. Antes de llamar al método donde el
     * servidor arranca, comprobamos que el puerto sea un entero válido.
     * 
     * @param args
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Uso de ServidorEcho: <puerto servidor>");
        } else {
            try {
                puerto = Integer.parseInt(args[0]);
                ServidorEcho.start();
            } catch (NumberFormatException nfe) {
                System.err.println(nfe.getMessage());
                nfe.printStackTrace();
            }
        }
    }

    /**
     * El siguiente método es el propio funcionamiento del servidor, el cual serán
     * infinito, definido con un while true. Primero se define el serverSocket con
     * el puerto especificado, de esta manera arrancará el servidor, y se pone a la
     * escucha de manera indefinida a la espera de un cliente. De hecho se debe
     * crear un socket para el cliente aceptado. Entonces, a través de un buffer y
     * streams de entrada/salida, aceptamos palabras que serán la salida del
     * servidor, al tratarse de un servidor echo. Una vez se ha servido al cliente,
     * cerramos su socket (la conexión con el mismo).
     */
    public static void start() {
        try {
            /* 1. Abre el socket servidor */
            serversocket = new ServerSocket(puerto);
            /* Justamente aquí el servidor ha arrancado -> 2. Bucle infinito */
            while (true) {
                /* A partir de ahora, se bloqueará hasta encontrar y aceptar conexiones */
                Socket cliente = serversocket.accept();
                /* Cliente aceptado. Podemos mostrar por pantalla la dirección del cliente */
                System.out.println("Conexión entrante: " + cliente.getRemoteSocketAddress());
                /* Streams de Entrada/Salida */
                InputStream is = cliente.getInputStream();
                OutputStream os = cliente.getOutputStream();
                /* Buffer de lectura */
                byte[] buffer = new byte[_MAX_LENGTH];
                int bytesLeidos = is.read(buffer, 0, buffer.length);
                /* Comprobamos que la lectura no haya fallado */
                if (bytesLeidos != -1) {
                    /* hemos recibido un request de ECHO -> mandamos su response */
                    String cadenaLeida = new String(buffer);
                    System.out.println(cadenaLeida);
                    os.write(cadenaLeida.getBytes());
                }
                /* Cerramos conexión */
                cliente.close();
            }
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
            ioe.printStackTrace();
        }
    }
}
