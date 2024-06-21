package SSLUDP.Ejercicio3;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.KeyStore;
import java.util.Scanner;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import SSLUDP.Ejercicio2.ServerEchoSSL;

public class ServidorEcho2HSSL {
    private final String javapath = "/home/raul/LFT_Certificados_RJC/serverKey.jks";
    private final String javaPathTrustedStores = "/home/raul/LFT_Certificados_RJC/ServerTrustedStore.jks";
    private SSLServerSocket ssSSL;

    public static void main(String[] args) {
        /* Nuevo servidor escuchando en 1721 */
        ServerEchoSSL serverEchoSSL = new ServerEchoSSL(1721);
        serverEchoSSL.start();
    }

    public ServidorEcho2HSSL(int puerto) throws Exception {
        try {
            // Acceso al almacén de claves con password servpass
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream(javapath), "servpass".toCharArray());

            // Acceso a las claves del almacén
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, "servpass".toCharArray());
            KeyManager[] keyManagers = kmf.getKeyManagers();

            // ! Acceso a los trustManagers
            KeyStore trustedStore = KeyStore.getInstance("JKS");
            trustedStore.load(new FileInputStream(javaPathTrustedStores), "servpass".toCharArray());

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustedStore);
            TrustManager[] trustManagers = tmf.getTrustManagers();

            // Conseguir una factoría de sockets y un ServerSocket
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(keyManagers, trustManagers, null);
            SSLServerSocketFactory ssf = sc.getServerSocketFactory();
            ssSSL = (SSLServerSocket) ssf.createServerSocket(puerto);
            ssSSL.setNeedClientAuth(true); // !exige autentificación al cliente!
            System.out.println("servidor arrancado...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start() {
        while (true) { // servir infinitamente
            try {
                SSLSocket cliente = (SSLSocket) ssSSL.accept();
                // obtener certificado del cliente
                SSLSession s = cliente.getSession();
                System.out.println(s.getPeerHost() + ";" + s.getPeerPrincipal().toString());
                sirve(cliente);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sirve(SSLSocket socketcliente) {
        final SSLSocket cliente = socketcliente;
        new Thread() {
            @Override
            public void run() {
                // tratamos=servir cliente!
                try {
                    /**
                     * Para esta solución podemos cambiar los objetos para manejar los streams, por
                     * ejemplo con un escáner para leer la línea del cliente, y un printwriter para
                     * escribir la respuesta. En definitiva, es lo mismo.
                     */
                    Scanner sc = new Scanner(cliente.getInputStream());
                    PrintWriter output = new PrintWriter(cliente.getOutputStream());
                    // Leemos del socket
                    String recibido = sc.nextLine();
                    // respondemos!
                    output.println("Recibido: " + recibido);
                    output.flush();
                    cliente.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
