package sockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Clase que hace uso del servidor
 */
public class Servidor extends Conexion {
	
	/**
	 * ---------
	 * ATRIBUTOS
	 * ---------
	 */
	
	/**
	 * Server Sockes - Socket del Servidor
	 */
	private ServerSocket ss;
	
	/**
	 * Socket - Socket del Cliente
	 */
	private Socket cs;
	
	/**
	 * Llave privada (secret) del Servidor
	 */
	private String sk;
	
	/**
	 * Llave publica (public) del Servidor
	 */
	private String pk;
	
	/**
	 * Reto numerico enviado por el Cliente
	 */
	private String reto;
	
	/**
	 * PrintWriter del Servidor - output writer (comunicacion con el Cliente)
	 */
	private PrintWriter pw;
	
	/**
	 * BufferedReader del Servidor - input reader (lectura de mensajes del Cliente)
	 */
	private BufferedReader bf;
	
	/**
	 * -------
	 * METODOS
	 * -------
	 */
	
	/**
	 * Constructor de la clase Servidor
	 * @throws IOException
	 */
	public Servidor() throws IOException {
		ss = new ServerSocket(PORT);
	}
	
	/**
	 * Lee los mensajes enviados por el cliente
	 * @throws IOException 
	 */
	public void leerMensajesCliente() throws IOException {
		String str;
		while(!(str = bf.readLine()).equals("END")) {
			
			if (str == null)
				continue;
			
			// mensaje 1 - inicio de sesion
            if (str.equalsIgnoreCase("Peticion para iniciar sesion")) {
            	System.out.println("Cliente: " + str + "\n");
            	pw.println("Inicio de sesion completado exitosamente");
            }
            
            // mensaje 2 - reto
            if (str.startsWith("reto -> ")) {
            	System.out.println("Cliente: " + str + "\n");
            	reto = str.substring(8, str.length());
            }
            
            // TODO: DEMAS MENSAJES
            
            
            pw.flush();
        }
	}
	
	public void start() throws IOException
    {
		// Mensajes iniciales
		System.out.println("Iniciando Servidor en el puerto: " + PORT + "\n");
		System.out.println("Esperando conexión...\n");
		
		// Socket del Cliente
		cs = ss.accept();
		System.out.println("Cliente conectado\n");
		
		// Output writer
		pw = new PrintWriter(cs.getOutputStream());
		
		// Input reader
		InputStreamReader in = new InputStreamReader(cs.getInputStream());
		bf = new BufferedReader(in);
		
		leerMensajesCliente();
		
		// Fin de la conexion
		ss.close();
		System.out.println("Fin de la conexión.\n");
    }
}
