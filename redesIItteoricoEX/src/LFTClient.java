import java.io.IOException;

public class LFTClient {
    private static final int __MAX_BUFFER = 1024;
    private static boolean modoSSL = false;
    private static String host;
    private static int puerto;
    private static String carpetaCliente;

    public static void main(String[] args) throws IOException {
        /* java LFTClient <modo> <host> <puerto> <carpeta_cliente> */
        try {
            switch (args.length) {
                case 3:
                    host = args[1];
                    puerto = Integer.parseInt(args[2]);
                    carpetaCliente = args[3];
                    break;
                case 4:
                    if (!args[1].equals("modo=SSL")) {
                        System.err.println("La sintaxis empleada para la activación del SSL es incorrecta. En <modo> emplear \"modo=SSL\" para proceder con su activación");
                        // ! ERROR + log
                        System.exit(1);
                    } else {
                        modoSSL = true;
                        host = args[2];
                        puerto = Integer.parseInt(args[3]);
                        carpetaCliente = args[4];
                    }
                    break;
                default:
                System.err.println("Los argumentos introducidos es incorrecto. Uso: <modo> <host> <puerto> <carpeta_cliente>");
                    // ! ERROR + log
                    System.exit(2);
                    break;
            }
        } catch (Exception e) {
            // ! ERROR + log
            
        }
        System.out.println("Se han recogido los argumentos correctamente. <modoSSL="+modoSSL+"> <host="+host+"> <puerto="+puerto+"> <carpeta_cliente="+carpetaCliente+">");
        // * log los argumentos
        LFTClient _miCliente = new LFTClient();
        // _miCliente.start(puerto);
    }
}
