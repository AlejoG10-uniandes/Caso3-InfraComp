package sockets;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class MainCliente {

	/**
	 * Metodo main del Cliente Llama al metodo start() de la clase Cliente
	 * 
	 * @param args
	 * @throws IOException
	 * @throws NoSuchAlgorithmException 
	 */
	public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
		Cliente cliente = new Cliente();
		cliente.start();
	}
}
