package TCP.Ejercicio3;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class ClienteMandar {
    private static Socket s;

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Uso de ClienteMandar: <servidor> <puerto>");
        } else
            try {
                int puerto = Integer.parseInt(args[1]);
                s = new Socket(args[0], puerto);
                OutputStream os = s.getOutputStream();
                os.write("Hola mundo!".getBytes());
                s.close();
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
