package TCP.Ejercicio2;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class MisAdaptadoresRed {

    public static void main(String[] args) {
        try {
            /* Obtengo el listado de interfaces */
            Enumeration<NetworkInterface> nilist = NetworkInterface.getNetworkInterfaces();
            while (nilist.hasMoreElements()) {
                /* Obtengo una interfaz siempre y cuando existan elementos en su listado */
                NetworkInterface nif = nilist.nextElement();
                if (nif.isUp()) {
                    /* En este momento tengo una interfaz en uso, imprimo nombres */
                    System.out.println(nif.getName() + " [" + nif.getDisplayName() + "]");
                    /* A partir de una, obtengo todas sus direcciones IP */
                    Enumeration<InetAddress> ialist = nif.getInetAddresses();
                    while (ialist.hasMoreElements()) {
                        /* Obtengo e imprimo una IP del listado, si es instancia de IPv4 */
                        InetAddress direccionip = ialist.nextElement();
                        System.out.println("\t" + direccionip.getHostAddress());
                    }
                }
            }
        } catch (SocketException soke) {
            /* Al obtener las interfaces de Red se ha de implementar esta excepción */
            System.err.println(soke.getMessage() + "\n");
            soke.printStackTrace();
        }
    }
}
