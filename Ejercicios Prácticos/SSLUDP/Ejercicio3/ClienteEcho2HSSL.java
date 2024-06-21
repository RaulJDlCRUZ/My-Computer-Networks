package SSLUDP.Ejercicio3;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Scanner;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class ClienteEcho2HSSL {
    private String javaPath = "/home/raul/LFT_Certificados_RJC/cacerts";
    private final String javaPathKeyStore = "/home/raul/LFT_Certificados_RJC/clientKey.jks";
    private SSLSocket cliente;

    public static void main(String[] args) throws Exception {
        /* "localhost" es un String equivalente a poner "127.0.0.1" */
        ClienteEcho2HSSL miCliente = new ClienteEcho2HSSL("localhost", 1721);
        miCliente.start();
    }

    public ClienteEcho2HSSL(String servidor, int puerto) throws Exception {
        try {
            // Acceso al almacén de claves con password clientpass
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream(javaPathKeyStore), "clientpass".toCharArray());

            // !Acceso a las claves del almacén
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, "clientpass".toCharArray());
            KeyManager[] keyManagers = kmf.getKeyManagers();

            // ACCESO AL ALMACEN DE CLAVES "cacerts" con password changeit
            KeyStore trustedStore = KeyStore.getInstance("JKS");
            trustedStore.load(new FileInputStream(javaPath), "changeit".toCharArray());

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustedStore);
            TrustManager[] trustManagers = tmf.getTrustManagers();

            // Obtener un SSLSocketFactory y un socket cliente
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(keyManagers, trustManagers, null);
            SSLSocketFactory ssf = sc.getSocketFactory();
            cliente = (SSLSocket) ssf.createSocket(servidor, puerto);

            // Imprime info sobre el certificado del servidor
            cliente.addHandshakeCompletedListener(new HandshakeCompletedListener() {
                @Override
                public void handshakeCompleted(HandshakeCompletedEvent hce) {
                    X509Certificate cert;
                    try {
                        cert = (X509Certificate) hce.getPeerCertificates()[0];
                        String certName = cert.getSubjectX500Principal().getName().substring(3,
                                cert.getSubjectX500Principal().getName().indexOf(","));
                        System.out.println("conectado al servidor con nombre de certificado: " + certName);
                    } catch (SSLPeerUnverifiedException e) {
                        e.printStackTrace();
                    }
                }
            });

            cliente.startHandshake();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start() {
        System.out.println("cliente arrancado!");
        new Thread() {
            @Override
            public void run() {
                try {
                    PrintWriter output = new PrintWriter(cliente.getOutputStream());
                    Scanner sc = new Scanner(cliente.getInputStream());

                    output.println("Hola mundo SSL!");
                    output.flush();

                    String recibido = sc.nextLine();
                    System.out.println("Recibido: " + recibido);

                    cliente.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }.start();
    }
}
