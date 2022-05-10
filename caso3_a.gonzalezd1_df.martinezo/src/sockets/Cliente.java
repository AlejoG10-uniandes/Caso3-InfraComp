package sockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

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
	 * Scanner del Cliente (inputs)
	 */
	private Scanner scn;
	
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
		scn = new Scanner(System.in);
		pw = new PrintWriter(cs.getOutputStream());
		bf = new BufferedReader(new InputStreamReader(cs.getInputStream()));
	}
	
	/**
	 * Paso 1: El Cliente lee la llave publica del Servidor
	 */
	public void leerPkServidor() {
		// TODO
	}
	
	/**
	 * Paso 2: El Cliente pide iniciar sesion
	 * Recibe confirmacion por parte del Servidor
	 * @throws IOException 
	 */
	public void peticionIniciarSesion() throws IOException {
		String inicio = "";
		while(!inicio.equalsIgnoreCase("INICIO")) {
			System.out.println("[2] Escriba INICIO para iniciar sesion: \n");
			inicio = scn.nextLine();		
		}
	    
		pw.println(inicio);
		pw.flush();
		
		String str = bf.readLine();
		System.out.println("Servidor: " + str + "\n");
	}
	
	/**
	 * Paso 3: El Cliente envia el reto al Servidor 
	 * Por default el reto es un numero aleatorio de 24 digitos
	 * @param l, longitud del reto
	 */
	public void enviarReto(int l) {
		String reto = "<";
		int min = 0;
		int max = 9;
		for (int i=0; i < l; i++) {
			// primer digito: min ≠ 0
			min = i == 0 ? 1 : 0;
			
			reto += (int) (Math.random() * max) + min;
		}
		
		reto += ">";
		
		System.out.println("[3] Enviando el siguiente reto al Servidor: " + reto + "\n");
		
		pw.println(reto);
		pw.flush();
	}
	
	/**
	 * Paso 4: El Cliente valida el reto cifrado, enviado por el Servidor 
	 * Si la validacion pasa el Cliente continua con el protocolo
	 * Si la validacion no pasa el Cliente termina la conexion
	 */
	public void validarRetoCifrado() {
		// TODO
	}
	
	/**
	 * Paso 5: El Cliente genera la llave simetrica LS
	 * El Cliente cifra LS con la PK del Servidor
	 * El Cliente recibe un mensaje de confirmacion por parte del Servidor
	 */
	public void generarCifrarLS() {
		// TODO
	}
	
	/**
	 * Paso 6: El Cliente envia su nombre
	 * Si el nombre esta en la tabla el Cliente recibe un mensaje de confirmacion por parte del Servidor
	 * Si el nombre no esta en la tabla el Cliente recibe un error por parte del Servidor
	 */
	public void enviarNombre() {
		// TODO
	}
	
	/**
	 * Paso 7: El Cliente envia el id del paquete
	 * Espera confirmacion por parte del Servidor (nombre y id paquete)
	 */
	public void enviarIdPKT() {
		// TODO
	}
	
	/**
	 * Paso 8: El Cliente envia un mensaje de confirmacion (ACK)
	 */
	public void confirmarInfo() {
		System.out.println("[8] Escriba ACK para confirmar la informacion: \n");
		String ack = scn.nextLine();		
	    
		pw.println(ack);
		pw.flush();
	}
	
	/**
	 * Paso 9: El Cliente valida la integridad de la informacion
	 */
	public void validarIntegridadInfo() {
		// TODO
	}
	
	/**
	 * Ultimo paso: La conexion termina
	 * @throws IOException
	 */
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
		
		// paso 4
		validarRetoCifrado();
		
		// paso 5
		generarCifrarLS();
		
		// paso 6
		enviarNombre();
		
		// paso 7
		enviarIdPKT();
		
		// paso 8
		confirmarInfo();
		
		// paso 9
		validarIntegridadInfo();
		
		// TODO: DEMAS PASOS
		
		// terminar conexion
		terminarConexion();
    }
}
