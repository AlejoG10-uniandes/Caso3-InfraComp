package tests;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import sockets.Cliente;
import sockets.Servidor;

public class Tester {
	
	/**
	 * Atributos
	 */
	
	/**
	 * Referencia al Servidor
	 */
	private static Servidor s;
	
	/**
	 * Referencia al Cliente
	 */
	private static Cliente c;
	
	/**
	 * Realiza la tarea 1 (i) del caso
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	public static void tareaIterativa() throws IOException, NoSuchAlgorithmException {
		
		System.out.println("*** ITERATIVA ***\n");
		
		// inicializa atributos
		s = new Servidor();
		c = new Cliente();
		
		// variables
		String reto;
		long start;
		long end;
		double tiempoS;
		double tiempoA;
		
		for (int i = 0; i < 32; i++) {	
			
			// simetrico
			c.generateKey(256);
			s.setLlaveSimetrica(c.getLlaveSimetrica());
			
			// asimetrico
			s.generarLlavesPublicaPrivada();
			
			// genera el reto
			reto = c.generarReto(24);
			
			// calcula el tiempo para el cifrado simetrico
			start = System.nanoTime();
			s.cifrarConLlaveSimetrica(reto);
			end = System.nanoTime();
			tiempoS = (end - start);
			
			// calcula el tiempo para el cifrado asimetrico
			start = System.nanoTime();
			s.cifrarConLlavePrivada(reto);
			end = System.nanoTime();
			tiempoA = (end - start);
			
			// imprime resultados
			System.out.println("Simetrico: " + tiempoS);
			System.out.println("Asimetrico: " + tiempoA);
			System.out.println("");
		}
		
		s.getSS().close();
	}
	
	/**
	 * Realiza la tarea 1 (ii) del caso
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	public static void tareaConcurrente() throws IOException, NoSuchAlgorithmException {
		
		System.out.println("*** CONCURRENTE ***\n");
		
		// variables
		String reto;
		long start;
		long end;
		double tiempoS;
		double tiempoA;
		
		System.out.println("*** 4 DELEGADOS ***\n");
		// 4 delegados
		Servidor s = new Servidor();
		for (int i = 0; i < 4; i++) {
			Cliente c = new Cliente();
			
			// simetrico
			c.generateKey(256);
			s.setLlaveSimetrica(c.getLlaveSimetrica());
			
			// asimetrico
			s.generarLlavesPublicaPrivada();
			
			// genera el reto
			reto = c.generarReto(24);
			
			// calcula el tiempo para el cifrado simetrico
			start = System.nanoTime();
			s.cifrarConLlaveSimetrica(reto);
			end = System.nanoTime();
			tiempoS = (end - start);
			
			// calcula el tiempo para el cifrado asimetrico
			start = System.nanoTime();
			s.cifrarConLlavePrivada(reto);
			end = System.nanoTime();
			tiempoA = (end - start);
			
			// imprime resultados
			System.out.println("Simetrico: " + tiempoS);
			System.out.println("Asimetrico: " + tiempoA);
			System.out.println("");
			
			c.getCS().close();
		}
		
		System.out.println("*** 16 DELEGADOS ***\n");
		// 16 delegados
		for (int i = 0; i < 16; i++) {
			Cliente c = new Cliente();
			
			// simetrico
			c.generateKey(256);
			s.setLlaveSimetrica(c.getLlaveSimetrica());
			
			// asimetrico
			s.generarLlavesPublicaPrivada();
			
			// genera el reto
			reto = c.generarReto(24);
			
			// calcula el tiempo para el cifrado simetrico
			start = System.nanoTime();
			s.cifrarConLlaveSimetrica(reto);
			end = System.nanoTime();
			tiempoS = (end - start);
			
			// calcula el tiempo para el cifrado asimetrico
			start = System.nanoTime();
			s.cifrarConLlavePrivada(reto);
			end = System.nanoTime();
			tiempoA = (end - start);
			
			// imprime resultados
			System.out.println("Simetrico: " + tiempoS);
			System.out.println("Asimetrico: " + tiempoA);
			System.out.println("");
			
			c.getCS().close();
		}
		
		System.out.println("*** 32 DELEGADOS ***\n");
		// 32 delegados
		for (int i = 0; i < 32; i++) {
			Cliente c = new Cliente();
			
			// simetrico
			c.generateKey(256);
			s.setLlaveSimetrica(c.getLlaveSimetrica());
			
			// asimetrico
			s.generarLlavesPublicaPrivada();
			
			// genera el reto
			reto = c.generarReto(24);
			
			// calcula el tiempo para el cifrado simetrico
			start = System.nanoTime();
			s.cifrarConLlaveSimetrica(reto);
			end = System.nanoTime();
			tiempoS = (end - start);
			
			// calcula el tiempo para el cifrado asimetrico
			start = System.nanoTime();
			s.cifrarConLlavePrivada(reto);
			end = System.nanoTime();
			tiempoA = (end - start);
			
			// imprime resultados
			System.out.println("Simetrico: " + tiempoS);
			System.out.println("Asimetrico: " + tiempoA);
			System.out.println("");
			
			c.getCS().close();
		}
		
		s.getSS().close();
	}
	
	/**
	 * Main
	 * @param args
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	public static void main(String[] args) throws IOException, NoSuchAlgorithmException {		
	
		// tarea 1 (i)
		tareaIterativa();
		
		// tarea 2 (ii)
		tareaConcurrente();
		
	}
}
