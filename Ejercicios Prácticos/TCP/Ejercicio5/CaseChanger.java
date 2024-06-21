package TCP.Ejercicio5;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class CaseChanger {
    private final static int _MAX_LENGTH = 1024;
    private static int puertoServidor;
    private static ServerSocket serversocket;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Uso de CaseChanger: <puerto servidor>");
        } else {
            try {
                puertoServidor = Integer.parseInt(args[0]);
                CaseChanger.start();
            } catch (NumberFormatException nfe) {
                System.err.println(nfe.getMessage());
                nfe.printStackTrace();
            }
        }
    }

    public static void start() {
        try {
            serversocket = new ServerSocket(puertoServidor);
            while (true) {
                Socket cliente = serversocket.accept();
                System.out.println("Conexión entrate: " + cliente.getRemoteSocketAddress());
                InputStream is = cliente.getInputStream();
                OutputStream os = cliente.getOutputStream();
                byte[] buf = new byte[_MAX_LENGTH];
                int bytesLeidos = is.read(buf, 0, buf.length);
                if (bytesLeidos != -1) {
                    /* Obtengo la cadena del buffer, sin espacios residuales */
                    String convertir = new String(buf).trim();
                    if (convertir.trim().equals("FIN")) {
                        os.write("FIN".getBytes());
                    } else {
                        String resultado = procesar(convertir);
                        os.write(resultado.getBytes());
                        os.flush();
                    }
                }
            }
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
            ioe.printStackTrace();
        }

    }

    /**
     * La estrategia es la siguiente: para toda cadena (que no ponga fin) iremos
     * obteniendo caracter a caracter. Comprobaremos si es una letra, a través del
     * código ASCII, y con éste miramos si es una mayúscula o minúscula.
     * 
     * @param cadena la cadena a procesar
     * @return String la cadena procesada
     */
    public static String procesar(String cadena) {
        String resultado = "";
        for (int i = 0; i < cadena.length(); i++) {
            char ch = cadena.charAt(i);
            int ascii = ch;
            if (ascii > 64 && ascii < 91) { // MINUS?
                ascii += 32;
            } else if (ascii > 96 && ascii < 123) { // MAYUS?
                ascii -= 32;
            }
            char chr = (char) ascii;
            resultado += chr;
        }
        return resultado;
    }
}
