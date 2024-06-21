package TCP.Ejercicio1;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class MiHostAdaptadores {
    public static void main(String[] args) {
        try {
            /*
             * Obtengo objeto localhost. Como es estático,
             * tiene que invocarse con el nombre de la clase, que es InetAddress
             */
            InetAddress local = InetAddress.getLocalHost();
            /* Obtengo nombre canónico del host */
            String canonical = local.getCanonicalHostName();
            /* Con dicho nombre saco una lista de IP en función de éste */
            InetAddress[] listaia = InetAddress.getAllByName(canonical);
            for (int i = 0; i < listaia.length; i++) {
                System.out.println(listaia[i]); // .. y lo saco por pantalla
            }
        } catch (UnknownHostException uhe) {
            /* Para intentar sacar el host, se debe controlar esta excepción */
            System.err.println(uhe.getMessage());
            uhe.printStackTrace();
        }
    }
}
