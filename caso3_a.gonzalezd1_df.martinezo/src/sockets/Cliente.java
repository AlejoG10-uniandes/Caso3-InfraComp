package sockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Cliente extends Conexion {
	
	/**
	 * ---------
	 * ATRIBUTOS
	 * ---------
	 */
	
	/**
	 * Client Sockes - Socket del Cliente
	 */
	private Socket cs;
	
	/**
	 * -------
	 * METODOS
	 * -------
	 */
	
	/**
	 * Constructor de la clase Cliente
	 * @throws IOException
	 */
	public Cliente() throws IOException {
		cs = new Socket(HOST, PORT);
	}
	
	/**
	 * Inicia el socket del Cliente
	 * @throws IOException
	 */
	public void start() throws IOException
    {
		// Cliente -> Servidor
		PrintWriter pw = new PrintWriter(cs.getOutputStream());
		pw.println("Hello");
		pw.flush();
		
		InputStreamReader in = new InputStreamReader(cs.getInputStream());
		BufferedReader bf = new BufferedReader(in);
		String str = bf.readLine();
		System.out.println("Servidor: " + str);
		
		cs.close();
    }
}
