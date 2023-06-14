import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class LFTServer {

    private String javaPath = "/usr/lib/jvm/java-20-openjdk/lib/security/"; // ruta a security

    private static final int __MAX_BUFFER = 1024;
    private static boolean modoSSL = false;
    private static int puerto;
    private static String carpetaServidor;
    private static int actualClients;
    private static int maximumClients;

    private SSLServerSocket serverSocket;
	private Socket clientSocket;

    public static void main(String[] args) throws IOException {
        LFTServer _miServidor = new LFTServer();
        try {
            switch (args.length) {
                case 3:
                    puerto = Integer.parseInt(args[0]);
                    carpetaServidor = args[1];
                    maximumClients = Integer.parseInt(args[2]);
                    break;
                case 4:
                    if (args[0].equals("modo=SSL") == false) {
                        System.err.println(
                                "La sintaxis empleada para la activación del SSL es incorrecta. En <modo> emplear \"modo=SSL\" para proceder con su activación");
                        // ! ERROR + log
                        System.exit(1);
                    } else {
                        modoSSL = true;
                        puerto = Integer.parseInt(args[1]);
                        carpetaServidor = args[2];
                        maximumClients = Integer.parseInt(args[3]);
                    }
                    break;
                default:
                    System.err.println(
                            "Los argumentos introducidos es incorrecto. Uso: <modo> <puerto> <carpeta_servidor> <max_clientes>");
                    // ! ERROR + log
                    System.exit(2);
                    break;
            }
            System.out.println(
                    "Se han recogido los argumentos correctamente. <modoSSL=" + modoSSL + "> <puerto=" + puerto
                            + "> <carpeta_servidor=" + carpetaServidor + "> <max_clientes=" + maximumClients);
            // * log los argumentos
            _miServidor.start(modoSSL, puerto, carpetaServidor, maximumClients);
        } catch (Exception e) {
            System.err.println(e.getMessage()); // Mensaje genérico que mostrará información de la excepción
            // ! ERROR + log <---- e.printStackTrace();
        }
    }

    public void start(boolean modoSSL, int puerto, String carpetaServidor, int maximumClients) {
        if (modoSSL) {

            /* Modo SSL Activado */
            // TODO log para cada paso en ssl
            // TODO Probar que funcione el modo SSL
            try {
                // 1. Acceso al almacén de claves
                KeyStore keyStore = KeyStore.getInstance("JKS");
                keyStore.load(new FileInputStream(javaPath + "serverKey.jks"),"servpass".toCharArray());

                // 2. Acceso a las claves del almacén
                KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                kmf.init(keyStore, "servpass".toCharArray());
                KeyManager[] keyManagers = kmf.getKeyManagers();

                // 3. ACCESO AL ALMACEN DE CLAVES "ServerTrustedStore.jks" con password servpass (Por defecto)
                KeyStore trustedStore = KeyStore.getInstance("JKS");
                trustedStore.load(new FileInputStream(javaPath + "ServerTrustedStore.jks"), "servpass".toCharArray());

                TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(trustedStore);
                TrustManager[] trustManagers = tmf.getTrustManagers();

                // 4. Obtener un SSLSocketFactory y un socket cliente
                try {
                    SSLContext sc = SSLContext.getInstance("SSL");
                    sc.init(keyManagers, trustManagers, null); // con doble handshake los dos primeros parametro en cliente y servidor

                    SSLServerSocketFactory ssf = sc.getServerSocketFactory();
					serverSocket = (SSLServerSocket) ssf.createServerSocket(puerto);
                // 5. Intercambio de certificados
                    serverSocket.setNeedClientAuth(true);
                    System.out.println("servidor arrancado...");
					while (actualClients <= maximumClients) {
						serverSocket.accept();
                        //* log: peticion de cliente aceptada
						SSLSocket miSocketCliente = (SSLSocket) serverSocket.accept();
						sirve(miSocketCliente);
						actualClients++;
					}

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
        }
    }
}
