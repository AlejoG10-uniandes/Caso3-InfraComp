package sockets;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class MainCliente {

	/**
	 * Metodo main del Cliente Llama al metodo start() de la clase Cliente
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		Cliente cliente = new Cliente();
		try {
			cliente.start();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
}
