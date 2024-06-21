package SSLUDP.Ejercicio2;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

public class ServerEchoSSL {
    private final String javapath = "/home/raul/LFT_Certificados_RJC/serverKey.jks";
    private SSLServerSocket ssSSL;

    public static void main(String[] args) {
        /* Nuevo servidor escuchando en 1721 */
        ServerEchoSSL serverEchoSSL = new ServerEchoSSL(1721);
        serverEchoSSL.start();
    }

    public ServerEchoSSL(int puerto) {
        try {
            // Acceso al almacén de claves
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream(javapath), "servpass".toCharArray());

            // Acceso a las claves del almacén
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, "servpass".toCharArray());
            KeyManager[] keyManagers = kmf.getKeyManagers();

            // Conseguir una factoría de sockets y un ServerSocket
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(keyManagers, null, null);
            SSLServerSocketFactory ssf = sc.getServerSocketFactory();
            ssSSL = (SSLServerSocket) ssf.createServerSocket(puerto);
            System.out.println("servidor arrancado...");
        } catch (Exception e) { // Todas las excepciones que se manejen las resumimos
            e.printStackTrace();
        }
    }

    public void start() {
        while (true) { // servir infinitamente
            try {
                // Cada cliente SSL es el resultado de que el servidor los 'escuche'
                SSLSocket cliente = (SSLSocket) ssSSL.accept();
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
                    // Leemos del socket
                    InputStream input = cliente.getInputStream();
                    OutputStream output = cliente.getOutputStream();
                    byte[] data = new byte[1024];

                    input.read(data, 0, data.length);
                    String recibido = new String(data).trim();
                    System.out.println("recibido: " + recibido);

                    output.write(recibido.getBytes());
                    output.flush();
                    cliente.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }.start(); // Objeto anónimo
    }
}
