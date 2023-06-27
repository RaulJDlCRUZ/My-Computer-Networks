import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Scanner;

import java.util.logging.*;

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

public class LFTClient {
    private static String errorLogPath = "/home/raul/RC2-TT/TT_REDES2/redesIItteoricoEX/Logs/Errores.log";
    private static String accionLogPath = "/home/raul/RC2-TT/TT_REDES2/redesIItteoricoEX/Logs/Acciones.log";
    private static final int __MAX_BUFFER = 1024;

    // Estas 2 variables de rutas varían según el computador o S.O.
    private String javaPath = "/home/raul/LFT_Certificados_RJC/cacerts"; // ruta a trusted store -> cacerts
    private String javaPathKeyStore = "/home/raul/LFT_Certificados_RJC/clientKey.jks"; // ruta a keymanager del cliente

    private static boolean modoSSL = false;
    private static String host;
    private static int puerto;
    private static String carpetaCliente;

    public static void main(String[] args) throws IOException {
        LFTClient _miCliente = new LFTClient();
        try {
            switch (args.length) {
                case 3: // No se usa modo SSL
                    host = args[0];
                    puerto = Integer.parseInt(args[1]);
                    carpetaCliente = args[2];
                    break;
                case 4: // Se usa modo SSL
                    if (args[0].equals("modo=SSL") == false) {
                        System.err.println(
                                "La sintaxis empleada para la activación del SSL es incorrecta. En <modo> emplear \"modo=SSL\" para proceder con su activación");

                        _miCliente.logWriter(errorLogPath,
                                "ERROR La sintaxis empleada para la activación del SSL es incorrecta");
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

                    _miCliente.logWriter(errorLogPath, "ERROR: Argumentos introducidos incorrectamente");
                    System.exit(2);
                    break;
            }
            System.out.println("Se han recogido los argumentos correctamente. <modoSSL=" + modoSSL + "> <host=" + host
                    + "> <puerto=" + puerto + "> <carpeta_cliente=" + carpetaCliente + ">");

            _miCliente.logWriter(accionLogPath, "Argumentos introducidos correctamente");

            _miCliente.start(modoSSL, host, puerto, carpetaCliente);
        } catch (Exception e) {
            System.err.println(e.getMessage());

            _miCliente.logWriter(errorLogPath, "ERROR: " + e.getMessage());
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
                logWriter(accionLogPath, "Acceso al al almacén de claves");
                KeyStore keyStore = KeyStore.getInstance("JKS");
                keyStore.load(new FileInputStream(javaPathKeyStore), "clientpass".toCharArray());

                // 2. Acceso a las claves del almacén
                logWriter(accionLogPath, "Acceso a las claves del almacén");
                KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                kmf.init(keyStore, "clientpass".toCharArray());
                KeyManager[] keyManagers = kmf.getKeyManagers();

                // 3. ACCESO AL ALMACEN DE CLAVES "cacerts" con password changeit (Por defecto)
                logWriter(accionLogPath, "Acceso al almade claves 'cacerts' con password changeit");
                KeyStore trustedStore = KeyStore.getInstance("JKS");
                trustedStore.load(new FileInputStream(javaPath), "changeit".toCharArray());
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(trustedStore);
                TrustManager[] trustManagers = tmf.getTrustManagers();

                // 4. Obtener un SSLSocketFactory y un socket cliente
                logWriter(accionLogPath, "Obtencion de un SSLSocketFactory y un socket cliente");
                try {
                    SSLContext sc = SSLContext.getInstance("SSL");
                    sc.init(keyManagers, trustManagers, null);
                    SSLSocketFactory ssf = sc.getSocketFactory();
                    SSLSocket clienteSSL = (SSLSocket) ssf.createSocket(ip_host, puerto);

                    // 5. Imprime info sobre el certificado del servidor
                    clienteSSL.addHandshakeCompletedListener(new HandshakeCompletedListener() {
                        @Override
                        public void handshakeCompleted(HandshakeCompletedEvent hce) {
                            X509Certificate cert;
                            try {
                                cert = (X509Certificate) hce.getPeerCertificates()[0];
                                String certName = cert.getSubjectX500Principal().getName().substring(3,
                                        cert.getSubjectX500Principal().getName().indexOf(","));
                                System.out.println(
                                        "\n[Conectado al servidor con nombre de certificado: " + certName + "]\n");
                                logWriter(accionLogPath, "Conectado al servidor SSL correctamente");
                            } catch (SSLPeerUnverifiedException sslpue) {
                                System.err.println(sslpue.getMessage());

                                logWriter(errorLogPath, "ERROR Conexión SSL no verificada: " + sslpue.getMessage());
                            }
                        }
                    });

                    // 6. Handshake por parte del cliente
                    logWriter(accionLogPath, "Inicio del Hansdshake por parte del cliente");
                    clienteSSL.startHandshake();
                    System.out.println("El cliente ha establecido la conexión a través de Secure Sockets Layer (SSL).");
                    HandlerClient(clienteSSL);

                    logWriter(accionLogPath, "Finalizacion de acuerdo cliente-servidor");
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
            /* No se usa SSL */
            try {
                Socket clienteNoSSL = new Socket(ip_host, puerto);
                System.out.println("Conexión establecida a través de TCP sin protocolo TLS.");

                logWriter(accionLogPath, "Establecimiento de conexión NON-SSL");
                HandlerClient(clienteNoSSL);

            } catch (UnknownHostException uhe) {
                System.err.println(uhe.getMessage());

                logWriter(errorLogPath,
                        "ERROR La direccion del Host es desconocida o inexistente: " + uhe.getMessage());

            } catch (IOException ioe) {
                System.err.println(ioe.getMessage());

                logWriter(errorLogPath, "ERROR E/S " + ioe.getMessage());
            }
        }
    }

    public void HandlerClient(Socket sk) {

        logWriter(accionLogPath, "Cliente arrancado correctamente");
        new Thread() {
            @Override
            public void run() {

                Scanner sn = new Scanner(System.in);
                try {
                    InputStream input = sk.getInputStream();
                    OutputStream output = sk.getOutputStream();
                    byte[] alojar = new byte[__MAX_BUFFER];
                    int bytesEsperados, bytesLeidos = 0, bytesLeidosTotales = 0;
                    String[] cadena;
                    menu();
                    String linea_teclado = sn.nextLine();
                    String[] paramsclissl = linea_teclado.split(" ", 2);
                    switch (paramsclissl[0]) {
                        case "LIST":

                            logWriter(accionLogPath, "Seleccionado LIST");
                            paramsclissl[0] += " ";
                            output.write(paramsclissl[0].getBytes());
                            output.flush();

                            // Primera parte: tamaño de llegada
                            input.read(alojar, 0, __MAX_BUFFER);
                            cadena = new String(alojar).split("/");

                            // Segunda parte: recibir el listado
                            bytesEsperados = Integer.parseInt(cadena[0]);
                            byte[] listado = new byte[bytesEsperados];

                            while (bytesLeidosTotales < bytesEsperados && bytesLeidos != -1) { //
                                bytesLeidos = input.read(listado, bytesLeidosTotales, bytesEsperados);
                                if (bytesLeidos != -1) { //
                                    bytesLeidosTotales += bytesLeidos;
                                }
                            }

                            // terminada la interacción: comprobamos: ¿Se recibió todo o se rompió el canal?
                            if (bytesLeidosTotales == bytesEsperados)
                                System.out.println(new String(listado));
                            else {
                                System.err.println("Comunicación rota!");
                                logWriter(errorLogPath, "ERROR Comunicacion rota durante el LIST");
                            }

                            logWriter(accionLogPath, "Ejecución de LIST finalizada correctamente");
                            logWriter(accionLogPath, "Cliente finalizando conexión con el servidor");
                            break;
                        case "GET":
                            if (paramsclissl[1].trim().equals("")) {
                                System.err.println("Uso de GET: GET <nombre_archivo>");
                                logWriter(errorLogPath, "ERROR GET solicitado incorrectamente");
                            } else {
                                logWriter(accionLogPath, "Seleccionado GET");

                                String obtener = paramsclissl[0] + " " + paramsclissl[1];

                                output.write(obtener.getBytes());
                                output.flush();

                                input.read(alojar, 0, __MAX_BUFFER);
                                cadena = new String(alojar).split("/");

                                bytesEsperados = Integer.parseInt(cadena[0]);
                                if (bytesEsperados == -1) { // Si el servidor envió -1 -> no existe el archivo
                                    System.err.println("No existe el fichero en el servidor.");
                                    logWriter(errorLogPath, "ERROR Archivo no encontrado en el servidor");
                                } else {
                                    System.out.println("Necesito en total " + bytesEsperados + " bytes para alojar el archivo.\n");
    
                                    String ruta = carpetaCliente + "/" + paramsclissl[1].trim();
                                    System.out.println("Escribiendo " + ruta + "...");
                                    File nuevo_arch_cli = new File(ruta);
                                    /* Herramienta para escribir en el archivo, hereda de la clase outputstream */
                                    FileOutputStream fous = new FileOutputStream(nuevo_arch_cli);
    
                                    byte[] buffer = new byte[__MAX_BUFFER];
                                    while (bytesLeidosTotales < bytesEsperados && bytesLeidos != -1) {
                                        bytesLeidos = input.read(buffer, 0, Math.min(__MAX_BUFFER, bytesEsperados));
                                        if (bytesLeidos != -1) {
                                            fous.write(buffer, 0, bytesLeidos);
                                            bytesLeidosTotales += bytesLeidos;
                                            System.out.print("\r" + 100 * bytesLeidosTotales / bytesEsperados + "%");
                                        }
                                    }
                                    // Recolocamos el cursor tras los print del porcentaje de obtención con \n
                                    System.out.print("\n");
                                    fous.close();
    
                                    if (bytesLeidosTotales != bytesEsperados) {
                                        System.err.println("Comunicación rota.");
                                        logWriter(errorLogPath, "ERROR Comunicacion rota durante el GET");
                                    }
                                }

                                logWriter(accionLogPath, "Ejecución GET finalizada");
                                logWriter(accionLogPath, "Cliente finalizando conexión con el servidor");
                            }

                            break;
                        case "PUT":
                            if (paramsclissl[1].trim().equals("")) {
                                System.err.println("Uso de PUT: PUT <nombre_archivo>");
                                logWriter(errorLogPath, "ERROR PUT solicitado incorrectamente");
                            } else {
                                try {

                                    logWriter(accionLogPath, "Seleccionado PUT");

                                    String rutaEnviar = carpetaCliente + "/" + paramsclissl[1].trim();
                                    File fileSend = new File(rutaEnviar);
                                    if (fileSend.exists()) {
                                        String enviar = paramsclissl[0].trim() + " " + paramsclissl[1].trim();
                                        // Notificamos al Servidor que hemos realizado una petición PUT
                                        output.write(enviar.getBytes());
                                        output.flush();
                                        // Cálculo del espacio que necesita el servidor
                                        long fileSize = fileSend.length();
                                        alojar = solicitudAlojamiento(fileSize);
                                        output.write(alojar);
                                        output.flush();
                                        // Enviamos los bytes del fichero de la petición
                                        int bytesFileRead;
                                        FileInputStream fins = new FileInputStream(fileSend);
                                        byte buffer2[] = new byte[__MAX_BUFFER];
                                        while ((bytesFileRead = fins.read(buffer2)) != -1) {
                                            output.write(buffer2, 0, bytesFileRead);
                                            output.flush();
                                        }
                                        fins.close();
                                        // !
                                        byte[] respuestaServidor = new byte[__MAX_BUFFER];
                                        input.read(respuestaServidor, 0, __MAX_BUFFER);
                                        System.out.println("Respuesta servidor: " + new String(respuestaServidor));
                                    } else {
                                        System.err.println("No se puede localizar el fichero");
                                        logWriter(errorLogPath, "ERROR Archivo no encontrado en el servidor");
                                    }

                                    logWriter(accionLogPath, "Ejecución PUT finalizada");
                                    logWriter(accionLogPath, "Cliente finalizando conexión con el servidor");

                                } catch (ArrayIndexOutOfBoundsException aioobe) {
                                    System.err.println(aioobe.getMessage());

                                    logWriter(errorLogPath, "ERROR Numero incorrecto de indice en array");
                                } catch (FileNotFoundException fnfe) {
                                    System.err.println(fnfe.getMessage());

                                    logWriter(errorLogPath, "ERROR Archivo no encontrado");
                                }
                            }

                            break;
                        case "SALIR":
                            logWriter(accionLogPath, "Seleccionado SALIR");
                            paramsclissl[0] += " ";
                            output.write(paramsclissl[0].getBytes());
                            output.flush(); // no dejamos ningún byte restante
                            input.read(alojar, 0, __MAX_BUFFER);
                            String[] salir = new String(alojar).split("/", 2);
                            if ((Integer.parseInt(salir[0].trim())) == sk.getLocalPort()
                                    && salir[1].trim().equals("EXIT")) {
                                // Desde el servidor se autoriza la finalización del cliente
                                logWriter(accionLogPath, "Cliente finalizando conexión con el servidor");
                                input.close();
                                output.close();
                                sk.close();
                                sn.close();
                                System.exit(0);
                            }
                            break;
                        default:
                            System.err.println("El comando introducido no es una petición válida");
                            logWriter(errorLogPath, "ERROR petición desconocida");
                            break;
                    }
                    input.close();
                    output.close();
                    sk.close();
                    sn.close();
                } catch (IOException ioe) {
                    System.err.println(ioe.getMessage());

                    logWriter(errorLogPath, "ERROR E/S " + ioe.getMessage());
                } catch (NumberFormatException nfe) {
                    System.err.println(nfe.getMessage());

                    logWriter(errorLogPath, "ERROR La petición introducida es incorrecta");
                } catch (IndexOutOfBoundsException ioobe) {
                    System.err.println(ioobe.getMessage());

                    logWriter(errorLogPath, "ERROR En el índice de un array durante la escritura");
                }
            }
        }.start(); // Objeto anónimo
    }

    public void menu() {
        /* Listar las opciones de cliente, tanto ssl como no ssl */
        System.out.println("----------------------------------------------------------------------------------" +
                "\nLIST: Listar los ficheros almacenados en la carpeta del servidor" +
                "\nGET <archivo>: El servidor transferirá al cliente el fichero especificado" +
                "\nPUT <archivo>: El cliente enviará al servidor el archivo introducido por teclado" +
                "\nSALIR\n----------------------------------------------------------------------------------");
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

    public void logWriter(String logPath, String logMessage) {
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