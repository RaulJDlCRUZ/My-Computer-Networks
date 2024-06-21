package TCP.Ejercicio5;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

public class ClienteCC {
    private static Socket s;

    /**
     * La realidad es que si el servidor devuelve exactamente lo mismo con las
     * mayúsculas/minúsculas invertidas, equivale a un echo
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Uso de ClienteCC: <servidor> <puerto>");
        } else {
            System.out.println("Argumentos recogidos: <" + args[0] + "> <" + args[1] + ">");
            try {
                int puerto = Integer.parseInt(args[1]);

                s = new Socket(args[0], puerto);

                InputStream is = s.getInputStream();
                OutputStream os = s.getOutputStream();

                Scanner teclado = new Scanner(System.in);
                String enviar = "";

                boolean fin = false;
                while (!fin) {
                    enviar = teclado.nextLine();

                    os.write(enviar.getBytes());
                    os.flush();

                    int bytesEsperados = enviar.length(), bytesLeidos = 0, bytesLeidosTotales = 0;
                    byte[] buffer = new byte[bytesEsperados];

                    while (bytesLeidosTotales < bytesEsperados && bytesEsperados != -1) {
                        bytesLeidos = is.read(buffer, 0, bytesEsperados);
                        if (bytesLeidos != -1) {
                            bytesLeidosTotales += bytesLeidos;
                        }
                    }

                    if (bytesLeidosTotales == bytesEsperados) {
                        String recibido = new String(buffer);
                        System.out.println("Recibido: " + recibido);
                        if (recibido.equals("FIN"))
                            fin = true;
                    } else {
                        System.err.println("Comunicación rota");
                    }
                    os.flush();
                }
                System.out.println("Cerrando...");
                teclado.close();
                is.close();
                os.close();
            } catch (IOException ioe) {
                System.err.println(ioe.getMessage());
                ioe.printStackTrace();
            } catch (NumberFormatException nfe) {
                System.err.println(nfe.getMessage());
                nfe.printStackTrace();
            }
        }
    }
}
