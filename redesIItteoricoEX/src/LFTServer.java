import java.io.IOException;

public class LFTServer {
    private static final int __MAX_BUFFER = 1024;
    private static boolean modoSSL = false;
    private static int puerto;
    private static String carpetaServidor;
    private static int maximumClients;

    public static void main(String[] args) throws IOException {
        /* java LFTServer <modo> <puerto> <carpeta_servidor> <max_clientes> */
        try {
            switch (args.length) {
                case 3:
                    puerto = Integer.parseInt(args[1]);
                    carpetaServidor = args[2];
                    maximumClients = Integer.parseInt(args[3]);
                    break;
                case 4:
                    if (!args[1].equals("modo=SSL")) {
                        // ! ERROR + log
                        System.exit(-1);
                    } else {
                        modoSSL = true;
                        puerto = Integer.parseInt(args[2]);
                        carpetaServidor = args[3];
                        maximumClients = Integer.parseInt(args[4]);
                    }
                    break;
                default:
                    // ! ERROR + log
                    System.exit(-2);
                    break;
            }
        } catch (Exception e) {
            // ! ERROR + log

        }
        //* log los argumentos
        LFTServer _miServidor = new LFTServer();
        //_miServidor.start(puerto);
    }
}
