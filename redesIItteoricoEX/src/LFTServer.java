import java.io.IOException;

public class LFTServer {
    private static final int __MAX_BUFFER = 1024;
    private static boolean modoSSL = false;
    private static int puerto;
    private static String carpetaServidor;
    private static int maximumClients;

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
            System.out.println("Se han recogido los argumentos correctamente. <modoSSL=" + modoSSL + "> <puerto=" + puerto
                            + "> <carpeta_servidor=" + carpetaServidor + "> <max_clientes=" + maximumClients);
            // * log los argumentos
            LFTServer _miServidor = new LFTServer();
            // _miServidor.start(puerto);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            //! ERROR + log <---- e.printStackTrace();
        }
    }
}
