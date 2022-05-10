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
	
	public void start() throws IOException
    {
		// Mensajes iniciales
		System.out.println("Iniciando Servidor en el puerto: " + PORT + "\n");
		System.out.println("Esperando conexión...\n");
		
		// Accepta el socket del Cliente
		Socket cs = ss.accept();
		System.out.println("Cliente conectado\n");
		
		InputStreamReader in = new InputStreamReader(cs.getInputStream());
		BufferedReader bf = new BufferedReader(in);
		String str = bf.readLine();
		System.out.println("Cliente: " + str);
		
		// Servidor -> Cliente
		PrintWriter pw = new PrintWriter(cs.getOutputStream());
		pw.println("Hello");
		pw.flush();
		
		// Fin de la conexion
		ss.close();
		System.out.println("Fin de la conexión.\n");
    }
}
