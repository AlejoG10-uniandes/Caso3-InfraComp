package sockets;

import java.io.IOException;

public class MainServidor {
	
	/**
	 * Metodo main del Servidor
	 * Llama al metodo start() de la clase Servidor
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		Servidor servidor = new Servidor();
		servidor.start();
	}
}
