package TCP.Ejercicio2;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class MisAdaptadoresB {

    /**
     * Básicamente es optimizar los bucles y asignaciones de
     * listas, en un único bucle for.
     * También se restringe a que sólo se muestren aquellas IPs de formato IPv4
     * (esto último es el ejercicio 2b)
     */

    public static void main(String[] args) {
        // Conseguir la enumeración con los interfaces de red
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface ni = en.nextElement();
                if (ni.isUp()) {
                    System.out.println(ni.getName() + " [" + ni.getDisplayName() + "]");
                    for (Enumeration<InetAddress> direcciones = ni.getInetAddresses(); direcciones.hasMoreElements();) {
                        InetAddress ip = direcciones.nextElement();
                        if (ip instanceof Inet4Address) { // Sólo muestro aquellas IPs de formato IPv4
                            System.out.println("\t" + ip.getHostAddress());
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
