package SSLUDP.Ejercicio2;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class ClientEchoSSL {
    private String javaPath = "/home/raul/LFT_Certificados_RJC/cacerts";
    private SSLSocket cliente;

    public static void main(String[] args) throws Exception { // haciendo que el método haga el throw simplifica
        ClientEchoSSL miCliente = new ClientEchoSSL("localhost", 1721);
        miCliente.start();
    }

    public ClientEchoSSL(String servidor, int puerto) throws Exception {
        try {
            // Acceso al almacén de claves "cacerts" con password changeit
            KeyStore trustedStore = KeyStore.getInstance("JKS");
            trustedStore.load(new FileInputStream(javaPath), "changeit".toCharArray());

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustedStore);
            TrustManager[] trustManagers = tmf.getTrustManagers();

            // Obtener un SSLSocketFactory y un socket cliente
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustManagers, null);
            SSLSocketFactory ssf = sc.getSocketFactory();
            cliente = (SSLSocket) ssf.createSocket(servidor, puerto);

            // Imprime info sobre el certificado del servidor al que se conecta el cliente
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
        new Thread() {
            @Override
            public void run() {
                try {
                    InputStream input = cliente.getInputStream();
                    OutputStream output = cliente.getOutputStream();
                    byte[] data = new byte[1024];

                    output.write("Hola Mundo SSL!".getBytes());
                    output.flush();

                    input.read(data);
                    String recibido = new String(data).trim();
                    System.out.println("Recibido: " + recibido);

                    cliente.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }.start();

    }
}
