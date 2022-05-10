package sockets;

import java.io.IOException;

public class MainCliente {
	
	/**
	 * Metodo main del Cliente
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		Cliente cliente = new Cliente();
		cliente.start();
	}
}
