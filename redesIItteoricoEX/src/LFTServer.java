
//import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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

import java.util.logging.*;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class LFTServer {

    private static String errorLogPath = "/home/raul/RC2-TT/TT_REDES2/redesIItteoricoEX/Logs/Errores.log";
    private static String accionLogPath = "/home/raul/RC2-TT/TT_REDES2/redesIItteoricoEX/Logs/Acciones.log";

    // Esta variable de ruta varía según la distribucion del Sistema Operativo
    // /home/pablozar12/LFT_Certificados_PBS/
    private String javaPath = "/home/raul/LFT_Certificados_RJC/"; // ruta a mis certificados
                                                                  // /home/raul/LFT_Certificados_RJC/

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

                        logWriter(errorLogPath, "ERROR La sintaxis empleada para la activación del SSL es incorrecta");
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

                    logWriter(errorLogPath, "ERROR: Argumentos introducidos incorrectamente");
                    System.exit(2);
                    break;
            }
            System.out.println(
                    "Se han recogido los argumentos correctamente:\n<modoSSL=" + modoSSL + "> <puerto=" + puerto
                            + "> <carpeta_servidor=" + carpetaServidor + "> <max_clientes=" + maximumClients + ">");

            logWriter(accionLogPath, "Argumentos introducidos correctamente");
            LFTServer _miServidor = new LFTServer(modoSSL, puerto);
            _miServidor.start();
        } catch (Exception e) {
            System.err.println(e.getMessage());

            logWriter(errorLogPath, "ERROR: " + e.getMessage());
        }
    }

    public LFTServer(boolean modoSSL, int puerto) {
        if (modoSSL) {
            /* Modo SSL Activado */

            try {
                // 1. Acceso al almacén de claves
                logWriter(accionLogPath, "Acceso al al almacén de claves");
                KeyStore keyStore = KeyStore.getInstance("JKS");
                keyStore.load(new FileInputStream(javaPath + "serverKey.jks"), "servpass".toCharArray());

                // 2. Acceso a las claves del almacén
                logWriter(accionLogPath, "Acceso a las claves del almacén");
                KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                kmf.init(keyStore, "servpass".toCharArray());
                KeyManager[] keyManagers = kmf.getKeyManagers();

                // 3. ACCESO AL ALMACEN DE CLAVES "ServerTrustedStore.jks" con password servpass
                logWriter(accionLogPath, "Acceso al almacén de claves 'cacerts' con password changeit");
                KeyStore trustedStore = KeyStore.getInstance("JKS");
                trustedStore.load(new FileInputStream(javaPath + "ServerTrustedStore.jks"), "servpass".toCharArray());

                TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(trustedStore);
                TrustManager[] trustManagers = tmf.getTrustManagers();

                // 4. Obtener un SSLSocketFactory y un Serversocket
                logWriter(accionLogPath, "Obtencion de un SSLSocketFactory y un socket cliente");
                try {
                    SSLContext sc = SSLContext.getInstance("SSL");
                    // doble handshake con params en cliente y servidor
                    sc.init(keyManagers, trustManagers, null);
                    SSLServerSocketFactory ssf = sc.getServerSocketFactory();
                    servSok = (SSLServerSocket) ssf.createServerSocket(puerto);

                    // 5. Intercambio de certificados
                    logWriter(accionLogPath, "Intercambio de certificados");
                    ((SSLServerSocket) servSok).setNeedClientAuth(true);
                    System.out.println("Iniciando servidor SSL...");
                } catch (KeyManagementException kme) {
                    System.err.println(kme.getMessage());

                    logWriter(errorLogPath, "ERROR El manejo de la clave impide definir la conexión SSL");
                }
            } catch (KeyStoreException kse) {
                System.err.println(kse.getMessage());

                logWriter(errorLogPath, "ERROR La key no se encuentra en el almacén de claves: " + kse.getMessage());
            } catch (NoSuchAlgorithmException nsae) {
                System.err.println(nsae.getMessage());

                logWriter(errorLogPath, "ERROR Algoritmo de encriptación no encontrado");
            } catch (IOException ioe) {
                System.err.println(ioe.getMessage());

                logWriter(errorLogPath, "ERROR E/S " + ioe.getMessage());
            } catch (CertificateException ce) {
                System.err.println(ce.getMessage());

                logWriter(errorLogPath, "ERROR El certificado no existe");
            } catch (UnrecoverableKeyException uke) {
                System.err.println(uke.getMessage());

                logWriter(errorLogPath, "ERROR La key no se puede recuperar");
            }
        } else {
            try {
                servSok = new ServerSocket(puerto);

                logWriter(accionLogPath, "Servidor iniciado en modo NON-SSL");
                /* El servidor nunca se cierra, siempre a la espera de peticiones de cliente */
                System.out.println("Iniciando servidor NON-SSL...");
            } catch (IOException ioe) {
                System.err.println(ioe.getMessage());

                logWriter(errorLogPath, "ERROR E/S " + ioe.getMessage());
            }
        }
    }

    public void start() {
        try {
            while (true) {
                clientSocket = servSok.accept();
                System.out.println("Cliente abierto.");
                // admitimos tantas peticiones como clientes máximos especificados
                if (++actualClients <= maximumClients) {
                    System.out.println("Cliente de " + clientSocket.getPort() + " a " + clientSocket.getInetAddress()
                            + ":" + clientSocket.getLocalPort() + " aceptado.");
                    new Handler(clientSocket).start();

                } else {
                    System.out.println("Cliente cerrado.");
                    clientSocket.close();
                    actualClients--;
                }
            }
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());

            logWriter(errorLogPath, "ERROR E/S: " + ioe.getMessage());
        }
    }

    // Manejador de peticiones del servidor
    public class Handler extends Thread {
        int bytesLeidos;
        final Socket clienteSocket;

        /* Constructor del manejador o hilo */
        Handler(Socket cliente) {
            clienteSocket = cliente;
        }

        @Override
        public void run() {
            try {
                in = clienteSocket.getInputStream();
                out = clienteSocket.getOutputStream();
                byte[] buffer = new byte[__MAX_BUFFER];
                /* Onjetos de Entrada/Salida para comunicarse con el Cliente */
                int bytesLeidos = in.read(buffer, 0, buffer.length);
                if (bytesLeidos != -1) {
                    String peticion_cli = new String(buffer);
                    if (peticion_cli != null) {
                        String[] argum_clients = peticion_cli.split(" ", 2);
                        System.out.println("Resultado de la petición:\nComando:[" + argum_clients[0]
                                + "], Parametro:<" + argum_clients[1] + ">");

                        /* Desde aquí ya puedo gestionar las peticiones */
                        bytesLeidos = 0;
                        int bytesEsperados, bytesLeidosTotales = 0;
                        String[] cadena;
                        String enviar = "";
                        switch (argum_clients[0].trim()) {
                            case "LIST":
                                logWriter(accionLogPath, "Recibida Petición LIST");
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

                                /* Servidor envia el espacio a utilizar */
                                out.write(espacio, 0, espacio.length);
                                out.flush();
                                /* Enviamos el listado de archivos como tal */
                                out.write(enviar.getBytes());
                                out.flush();

                                logWriter(accionLogPath, "Fin ejecución LIST en servidor");
                                break;
                            case "GET":
                                logWriter(accionLogPath, "Recibida Petición GET");

                                try {
                                    /*
                                     * Creamos la ruta absoluta del archivo solicitado, sin espacios en blanco
                                     */
                                    String ruta = carpetaServidor + "/" + argum_clients[1].trim();
                                    byte[] remitir = new byte[__MAX_BUFFER];
                                    File peticion = new File(ruta);
                                    if (peticion.exists()) {
                                        /* Calculamos cuanto espacio necesita el cliente */
                                        long tamanyo = peticion.length();
                                        remitir = solicitudAlojamiento(tamanyo);
                                        out.write(remitir);
                                        out.flush();
                                        /* Enviamos los bytes del archivo */
                                        int bytesArchivoLeidos;
                                        FileInputStream fins = new FileInputStream(peticion);
                                        byte leerget[] = new byte[__MAX_BUFFER];
                                        while ((bytesArchivoLeidos = fins.read(leerget)) != -1) {
                                            out.write(leerget, 0, bytesArchivoLeidos);
                                            out.flush();
                                        }
                                        fins.close();
                                    } else {
                                        System.err.println("No se puede localizar el fichero");
                                    }

                                    logWriter(accionLogPath, "Fin ejecución GET en servidor");
                                } catch (ArrayIndexOutOfBoundsException aioobe) {
                                    System.err.println(aioobe.getMessage());

                                    logWriter(errorLogPath, "ERROR Numero incorrecto de argumentos");
                                } catch (FileNotFoundException fnfe) {
                                    System.err.println(fnfe.getMessage());

                                    logWriter(errorLogPath, "ERROR Archivo no encontrado");
                                }
                                break;
                            case "PUT":
                                logWriter(accionLogPath, "Recibida Petición PUT");
                                byte[] tomar = new byte[__MAX_BUFFER];
                                // Recojemos el tamaño del archivo a alojar
                                in.read(tomar, 0, __MAX_BUFFER);
                                cadena = new String(tomar).split("/", 2);
                                bytesEsperados = Integer.parseInt(cadena[0]);

                                System.out.println(
                                        "Necesito en total " + bytesEsperados + " bytes para alojar el archivo.\n");

                                String ruta = carpetaServidor + "/" + argum_clients[1].trim();
                                System.out.println("Escribiendo " + ruta + "...");
                                // Temporalmente se usa un archivo generico
                                File nuevo_arch_serv = new File(carpetaServidor + "/" + "archivo");
                                File def = new File(ruta);
                                FileOutputStream fous = new FileOutputStream(nuevo_arch_serv);

                                /* Como cliente escribió tres veces, reciclamos lo que quedaba del Stream */
                                fous.write(cadena[1].trim().getBytes());
                                bytesLeidosTotales += cadena[1].trim().length();

                                byte[] leerput = new byte[__MAX_BUFFER];

                                while (bytesLeidosTotales < bytesEsperados && bytesLeidos != -1) {
                                    bytesLeidos = in.read(leerput, 0, Math.min(__MAX_BUFFER, bytesEsperados));
                                    if (bytesLeidos != -1) {
                                        fous.write(leerput, 0, bytesLeidos);
                                        bytesLeidosTotales += bytesLeidos;
                                        System.out.print("\r" + 100 * bytesLeidosTotales / bytesEsperados + "%");
                                    }
                                }
                                System.out.print("\n"); // Recolocamos cursor tras porcentaje de obtención
                                fous.close();
                                nuevo_arch_serv.renameTo(def); // Tras descargar el archivo en servidor, lo recombramos
                                String respuesta;
                                if (bytesLeidosTotales != bytesEsperados) {
                                    respuesta = "Comunicación rota. ";
                                } else {
                                    respuesta = "Recibido OK. ";
                                }
                                out.write((respuesta+bytesLeidosTotales).getBytes());
                                logWriter(accionLogPath, "Fin ejecución PUT en servidor");
                                break;
                            case "SALIR":

                                logWriter(accionLogPath, "Recibida Petición SALIR");
                                System.out.println(clientSocket.getPort() + " quiere salir");
                                String exit = clientSocket.getPort() + "/EXIT";
                                out.write(exit.getBytes());
                                out.flush();

                                logWriter(accionLogPath, "Fin ejecución SALIR en servidor");
                                break;
                        }
                        System.out.println(
                                "El cliente ha sido desconectado del Servidor para dejar paso a otros clientes");
                        actualClients--;
                        /* Cerramos los Streams para liberar recursos/restos del cliente finalizado */
                        out.flush();
                        in.close();
                        out.close();
                    }
                }
            } catch (IOException ioe) {
                System.err.println(ioe.getMessage());

                logWriter(errorLogPath, "ERROR E/S: " + ioe.getMessage());
            }
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

        return alojamiento;
    }

    public static void logWriter(String logPath, String logMessage) {
        Logger log = Logger.getLogger("Registro de Eventos");
        FileHandler fileH;

        try {
            fileH = new FileHandler(logPath, true);
            log.addHandler(fileH);

            SimpleFormatter format = new SimpleFormatter();
            fileH.setFormatter(format);

            if (logPath.equals(accionLogPath)) {
                log.info(logMessage);
            } else if (logPath.equals(errorLogPath)) {
                log.warning(logMessage);
            }

        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}