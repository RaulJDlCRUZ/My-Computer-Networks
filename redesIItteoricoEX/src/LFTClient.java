import java.io.FileInputStream;
// import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import javax.net.ssl.*;

import java.net.Socket;
import java.net.UnknownHostException;

public class LFTClient {

    private static final int __MAX_BUFFER = 1024;

    private String javaPath = "/usr/lib/jvm/java-20-openjdk/lib/security/cacerts"; // ruta a trusted store -> cacerts
    private String javaPathKeyStore = "{donde sea}/clientKey.jks"; // ruta a keymanager del cliente

    private static boolean modoSSL = false;
    private static String host;
    private static int puerto;
    private static String carpetaCliente;

    private SSLSocket clienteSSL;
    private Socket clienteNoSSL;

    public static void main(String[] args) throws IOException {
        LFTClient _miCliente = new LFTClient();
        try {
            switch (args.length) {
                case 3:
                    host = args[0];
                    puerto = Integer.parseInt(args[1]);
                    carpetaCliente = args[2];
                    break;
                case 4:
                    if (args[0].equals("modo=SSL") == false) {
                        System.err.println(
                                "La sintaxis empleada para la activación del SSL es incorrecta. En <modo> emplear \"modo=SSL\" para proceder con su activación");
                        // ! ERROR + log
                        System.exit(1);
                    } else {
                        modoSSL = true;
                        host = args[1];
                        puerto = Integer.parseInt(args[2]);
                        carpetaCliente = args[3];
                    }
                    break;
                default:
                    System.err.println(
                            "Los argumentos introducidos es incorrecto. Uso: <modo> <host> <puerto> <carpeta_cliente>");
                    // ! ERROR + log
                    System.exit(2);
                    break;
            }
            System.out.println("Se han recogido los argumentos correctamente. <modoSSL=" + modoSSL + "> <host=" + host
                    + "> <puerto=" + puerto + "> <carpeta_cliente=" + carpetaCliente + ">");
            // * log los argumentos
            _miCliente.start(modoSSL, host, puerto, carpetaCliente);
        } catch (Exception e) {
            System.err.println(e.getMessage()); // Mensaje genérico que mostrará información de la excepción
            // ! ERROR + log <---- e.printStackTrace();
        }
    }

    public void start(boolean modoSSL, String ip_host, int puerto, String carpetaCliente) {
        /*
         * Aquí es donde se creará el Socket correspondiente al cliente. Para ello, se
         * discriminará si está activado el modo SSL o no.
         * En caso afirmativo, se creará un Socket del subtipo SSL Socket, en caso
         * negativo un Socket normal.
         */
        if (modoSSL) {

            /* Modo SSL Activado */

            try {
                // 1. Acceso al almacén de claves
                KeyStore keyStore = KeyStore.getInstance("JKS");
                keyStore.load(new FileInputStream(javaPathKeyStore), "clientpass".toCharArray());

                // 2. Acceso a las claves del almacén
                KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                kmf.init(keyStore, "clientpass".toCharArray());
                KeyManager[] keyManagers = kmf.getKeyManagers();

                // 3. ACCESO AL ALMACEN DE CLAVES "cacerts" con password changeit (Por defecto)
                KeyStore trustedStore = KeyStore.getInstance("JKS");
                trustedStore.load(new FileInputStream(javaPath), "changeit".toCharArray());

                TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(trustedStore);
                TrustManager[] trustManagers = tmf.getTrustManagers();

                // 4. Obtener un SSLSocketFactory y un socket cliente
                try {
                    SSLContext sc = SSLContext.getInstance("SSL");
                    sc.init(keyManagers, trustManagers, null);
                    SSLSocketFactory ssf = sc.getSocketFactory();
                    clienteSSL = (SSLSocket) ssf.createSocket(ip_host, puerto);

                    // 5. Imprime info sobre el certificado del servidor
                    clienteSSL.addHandshakeCompletedListener(new HandshakeCompletedListener() {
                        @Override
                        public void handshakeCompleted(HandshakeCompletedEvent hce) {
                            X509Certificate cert;
                            try {
                                cert = (X509Certificate) hce.getPeerCertificates()[0];
                                String certName = cert.getSubjectX500Principal().getName().substring(3,
                                        cert.getSubjectX500Principal().getName().indexOf(","));
                                System.out.println("conectado al servidor con nombre de certificado: " + certName);
                            } catch (SSLPeerUnverifiedException sslpue) {
                                System.err.println(sslpue.getMessage());
                                // ! log: conexión SSL no verificada. + sslpue.printStackTrace();
                            }
                        }
                    });
                    // 6. Handshake por parte del cliente
                    clienteSSL.startHandshake();
                    System.out.println("El cliente ha establecido la conexión a través de Secure Sockets Layer (SSL).");
                    // ! IR AL MENU
                    // * log de finalización del acuerdo cliente-servidor
                } catch (KeyManagementException kme) {
                    System.err.println(kme.getMessage());
                    // ! log: el manejo de la clave impide definir la conexión SSL
                }
            } catch (KeyStoreException kse) {
                System.err.println(kse.getMessage());
                // ! log: la key no se encuentra en el almacén de claves + printstack
            } catch (NoSuchAlgorithmException nsae) {
                System.err.println(nsae.getMessage());
                // ! log: algoritmo de encriptación no encontrado
            } catch (IOException ioe) { // También cubre la excepción del tipo FileNotFoundException
                System.err.println(ioe.getMessage());
                // ! log: error en la entrada/salida + ioe.printStackTrace();
            } catch (CertificateException ce) {
                System.err.println(ce.getMessage());
                // ! log: el certificado no existe
            } catch (UnrecoverableKeyException uke) {
                System.err.println(uke.getMessage());
                // ! log: la key no se puede recuperar
            }
        } else {
            /* No se usa SSL */
            try{
                clienteNoSSL = new Socket(ip_host, puerto);
                System.out.println("Conexión establecida a través de TCP sin protocolo TLS.");
                // * log de establecimiento conexión non-ssl
                // ! IR AL MENU
            } catch (UnknownHostException uhe) {
                System.err.println(uhe.getMessage());
                //! log: la dirección del host es desconocida o inexistente + uhe.printStackTrace();
            } catch (IOException ioe) {
                System.err.println(ioe.getMessage());
                // ! log: error en la entrada/salida + ioe.printStackTrace();
                
            }
            
        }
    }

    public void menu() {
        /* Listar las opciones de cliente, tanto ssl como no ssl */
        System.out.println("LIST: Listar los ficheros almacenados en la carpeta del servidor" +
                "\nGET <archivo>: El servidor transferirá al cliente el fichero especificado" +
                "\nPUT <archivo>: El cliente enviará al servidor el archivo introducido por teclado" +
                "\nSALIR");
    }
}

// TODO: Implementar los logs de acciones y errores
