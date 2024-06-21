package TCP.Ejercicio5;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class CaseChangerMultihilo {
    private final int MAX_LENGHT=1024;
    boolean parar=false;
    public static void main(String args[]){
        CaseChangerMultihilo servidor=new CaseChangerMultihilo();
        servidor.iniciar();
    }

    public class Hilo extends Thread{
        Socket mSocket; //referencia "miembro" al socket Cliente
        boolean parar;
        Hilo(Socket s, boolean p){
            mSocket=s;
            parar = p;
        }

        @Override
        public void run(){
            // ES CONCURRENTE!
            //si llega aquí, ha entrado una conexión
            System.out.println("Entra conexión de "+mSocket.getRemoteSocketAddress()); 
            
            //Esperamos aleatoriamente un tiempo entre 0 y 2 segundos para simular procesamiento
            try {
                Random r=new Random();
                int espera=r.nextInt(2000);   
                Thread.sleep(espera);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try{
                InputStream is=mSocket.getInputStream();
                OutputStream os=mSocket.getOutputStream();
                byte[] buffer=new byte[MAX_LENGHT];
				
                int bytesLeidos=is.read(buffer,0,MAX_LENGHT);
                if(bytesLeidos!=-1){
                    //hemos recibido un request de ECHO-> mandar el response
                    String cadenaLeida=new String(buffer);
                    if(!cadenaLeida.equals("FIN")){
                        cadenaLeida=procasarCadena(cadenaLeida);
                    }
                    else
                    parar= true;
                    os.write(cadenaLeida.getBytes()); //response                   
                }
                mSocket.close(); //Cerramos cliente y procesamos el siguiente REQUEST
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    private String procasarCadena(String cadena){
        String resultado="";
        for (int i=0; i< cadena.length();i++) {
            if(cadena.charAt(i)<'a')
                resultado+=Character.toLowerCase(cadena.charAt(i));
            else
				resultado+=Character.toUpperCase(cadena.charAt(i));

        }
        return resultado;
    }

    public void iniciar(){
        //servidor eschuchando en 9999
        try {
            ServerSocket ss=new ServerSocket(9998);
            while(!parar){
                Socket s=ss.accept();
                procesar(s);
            }      
            ss.close();
            System.out.println("adios");      
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void procesar(Socket s){
        Hilo h=new Hilo(s,parar);
        h.start();
    }

    
}