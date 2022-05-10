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
	 * PrintWriter del Cliente (comunicacion con el Servidor)
	 */
	private PrintWriter pw;
	
	/**
	 * BufferedReader del Cliente - input reader (lectura de mensajes del Servidor)
	 */
	private BufferedReader bf;
	
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
		pw = new PrintWriter(cs.getOutputStream());
		bf = new BufferedReader(new InputStreamReader(cs.getInputStream()));
	}
	
	/**
	 * Paso 1: El Cliente lee la llave publica del Servidor
	 */
	public void leerPkServidor() {
		
	}
	
	/**
	 * Paso 2: El Cliente pide iniciar sesion y recibe confirmacion por el Servidor
	 * @throws IOException 
	 */
	public void peticionIniciarSesion() throws IOException {
		pw.println("Peticion para iniciar sesion");
		pw.flush();
		
		String str = bf.readLine();
		System.out.println("Servidor: " + str + "\n");
	}
	
	/**
	 * Paso 3: El Cliente envia el reto al Servidor (numero aleatorio de 24 digitos)
	 * @param l, longitud del reto
	 */
	public void enviarReto(int l) {
		String reto = "reto -> ";
		int min = 0;
		int max = 9;
		int rnd = 0;
		for (int i=0; i < l; i++) {
			// primer digito: min ≠ 0
			min = i == 0 ? 1 : 0;
			
			// (int) (Math.random() * max) + min
			rnd = (int) (Math.random() * max) + min;
			
			reto += rnd;
		}
		
		pw.println(reto);
		pw.flush();
	}
	
	public void terminarConexion() throws IOException {
		pw.println("END");
		pw.flush();
		cs.close();
		System.out.println("Fin de la conexión.\n");
	}
	
	/**
	 * Inicia el socket del Cliente
	 * @throws IOException
	 */
	public void start() throws IOException
    {	
		// paso 1
		leerPkServidor();
		
		// paso 2
		peticionIniciarSesion();
		
		// paso 3
		enviarReto(24);
		
		// TODO: DEMAS PASOS
		
		// terminar conexion
		terminarConexion();
    }
}
