import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
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
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class LFTServer {

    private String javaPath = "/home/raul/LFT_Certificados_RJC/"; // ruta a mis certificados

    private static final int __MAX_BUFFER = 1024;
    private static boolean modoSSL = false;
    private static int puerto;
    private static String carpetaServidor;
    private static int actualClients;
    private static int maximumClients;
    private static InputStream in;
    private static OutputStream out;
    private ServerSocket servSok;
    private static Socket clientSocket;

    public static void main(String[] args) throws IOException {
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
                    "Se han recogido los argumentos correctamente:\n<modoSSL=" + modoSSL + "> <puerto=" + puerto
                            + "> <carpeta_servidor=" + carpetaServidor + "> <max_clientes=" + maximumClients + ">");
            // * log los argumentos
            LFTServer _miServidor = new LFTServer(modoSSL, puerto);
            _miServidor.start();
        } catch (Exception e) {
            System.err.println(e.getMessage()); // Mensaje genérico que mostrará información de la excepción
            // ! ERROR + log <---- e.printStackTrace();
        }
    }

    public LFTServer(boolean modoSSL, int puerto) {
        if (modoSSL) {
            /* Modo SSL Activado */
            // TODO log para cada paso en ssl
            try {
                // 1. Acceso al almacén de claves
                KeyStore keyStore = KeyStore.getInstance("JKS");
                keyStore.load(new FileInputStream(javaPath + "serverKey.jks"), "servpass".toCharArray());

                // 2. Acceso a las claves del almacén
                KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                kmf.init(keyStore, "servpass".toCharArray());
                KeyManager[] keyManagers = kmf.getKeyManagers();

                // 3. ACCESO AL ALMACEN DE CLAVES "ServerTrustedStore.jks" con password servpass
                KeyStore trustedStore = KeyStore.getInstance("JKS");
                trustedStore.load(new FileInputStream(javaPath + "ServerTrustedStore.jks"), "servpass".toCharArray());

                TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(trustedStore);
                TrustManager[] trustManagers = tmf.getTrustManagers();

                // 4. Obtener un SSLSocketFactory y un socket cliente
                try {
                    SSLContext sc = SSLContext.getInstance("SSL");
                    // doble handshake con params en cliente y servidor
                    sc.init(keyManagers, trustManagers, null);
                    SSLServerSocketFactory ssf = sc.getServerSocketFactory();
                    servSok = (SSLServerSocket) ssf.createServerSocket(puerto);
                    // 5. Intercambio de certificados
                    ((SSLServerSocket) servSok).setNeedClientAuth(true);
                    System.out.println("Iniciando servidor SSL...");
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
            try {
                servSok = new ServerSocket(puerto);
                // * log servidor iniciado non-ssl
                /* El servidor nunca se cierra, siempre a la espera de peticiones de cliente */
                System.out.println("Iniciando servidor NON-SSL...");
            } catch (IOException ioe) {
                System.err.println(ioe.getMessage());
                // ! log: error en la entrada/salida + ioe.printStackTrace();
            }
        }
    }

    public void start() {
        try {
            while (true) {
                clientSocket = servSok.accept(); //! BLOQUEANTE
                System.out.println("Cliente abierto.");
                // admitimos tantas peticiones como clientes máximos especificados
                if (++actualClients <= maximumClients) {
                    System.out.println("Cliente de " + clientSocket.getPort() + " a " + clientSocket.getInetAddress()
                    + ":" + clientSocket.getLocalPort() + " aceptado.");
                    new Handler(clientSocket).start();

                    System.out.println("Termino aquí."); //?
                } else {
                    System.out.println("Cliente cerrado.");
                    clientSocket.close();
                    actualClients--;
                }
            }
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
            // ! log: error en la entrada/salida + ioe.printStackTrace();
        }
    }
    // Manejador de peticiones del servidor
    public static class Handler extends Thread {
        int bytesLeidos;
        byte[] buffer = new byte[__MAX_BUFFER];
        final Socket clienteSocket;

        /* Constructor del manejador o hilo */
        Handler(Socket cliente) {
            clienteSocket = cliente;
        }

        @Override
        public void run() {
            try {
                /* Onjetos de Entrada/Salida para comunicarse con el Cliente */
                in = clienteSocket.getInputStream();
                out = clienteSocket.getOutputStream();
                int bytesLeidos = in.read(buffer, 0, buffer.length);
                if (bytesLeidos != -1) {
                    String peticion_cli = new String(buffer);
                    if (peticion_cli != null) {
                        String[] argum_clients = peticion_cli.split(" ", 2);
                        System.out.println("Resultado de la petición:\nComando:[" + argum_clients[0]
                                + "], Parametro:<" + argum_clients[1] + ">");
                        sirve(argum_clients[0], argum_clients[1]);
                    }
                }
            } catch (IOException ioe) {
                System.err.println(ioe.getMessage());
            }
        }
    }

    public static void sirve(String comando, String parametro) {
        try {
            String enviar = "";
            switch (comando.trim()) {
                case "LIST":
                    File fichero = new File(carpetaServidor);
                    if (fichero.exists()) {// se comprueba si existe el el directorio
                        File[] arrayFicheros = fichero.listFiles();
                        for (int i = 0; i < arrayFicheros.length; i++) {
                            enviar += i + 1 + " : " + arrayFicheros[i].getName() + " ("
                                    + arrayFicheros[i].length() + ")" + "\n";
                        }
                    }
                    int bytesAlojar = enviar.length();
                    /* Calculamos cuánto debe alojar el cliente exactamente */
                    byte[] espacio = solicitudAlojamiento(bytesAlojar);
                    // ? System.out.println(enviar);
                    /* Servidor envia el espacio a utilizar */
                    out.write(espacio, 0, espacio.length);
                    out.flush();
                    /* Enviamos el listado de archivos como tal */
                    out.write(enviar.getBytes());
                    out.flush();
                    break;
                case "GET":
                    if (parametro.trim().equals("")) {
                        // Esto no es coherente
                    } else {
                        try {
                            /* Creamos la ruta absoluta del archivo solicitado, sin espacios en blanco */
                            String ruta = carpetaServidor + "/" + parametro.trim();
                            // ? System.out.println(ruta);
                            File peticion = new File(ruta);
                            if (peticion.exists()) {
                                /* Calculamos cuanto espacio necesita el cliente */
                                long tamanyo = peticion.length();
                                // ? System.out.println(tamanyo);
                                byte[] alojar = solicitudAlojamiento(tamanyo);
                                out.write(alojar);
                                out.flush();
                                /* Enviamos los bytes del archivo */
                                int bytesArchivoLeidos;
                                FileInputStream fins = new FileInputStream(peticion);
                                byte buffer2[] = new byte[__MAX_BUFFER];
                                while ((bytesArchivoLeidos = fins.read(buffer2)) != -1) {
                                    out.write(buffer2, 0, bytesArchivoLeidos);
                                    out.flush();
                                }
                                fins.close();
                            } else {
                                System.err.println("No se puede localizar el fichero");
                            }
                        } catch (ArrayIndexOutOfBoundsException aioobe) {
                            System.err.println(aioobe.getMessage());
                            // ! log: numero incorrecto de argumentos en este caso
                        } catch (FileNotFoundException fnfe) {
                            System.err.println(fnfe.getMessage());
                            // ! log: archivo no
                        }
                    }
                    break;
                case "PUT":
                    break;
                case "SALIR":
                    System.out.println(clientSocket.getPort()+" quiere salir");
                    String exit = clientSocket.getPort()+"/EXIT";
                    out.write(exit.getBytes());
                    out.flush();
                    actualClients--;
                    break;
            }
            System.out.println("Termino de servir");
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
            // ! log: error en la entrada/salida + ioe.printStackTrace();
        }
    }

    public static byte[] solicitudAlojamiento(long cifra) {
        String cifras = Long.toString(cifra);
        int length = cifras.length();
        byte[] alojamiento = new byte[length + 1];
        for (int i = 0; i < length; i++)
            // Cada byte es una cifra del tamaño en bytes del listado
            alojamiento[i] = Long.valueOf(cifras.charAt(i)).byteValue();
        // Para no tener que usar el buffer entero, agregamos un separador
        alojamiento[length] = (byte) '/';
        // TODO probar con trim
        return alojamiento;
    }
}