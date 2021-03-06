package sockets;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.*;
import java.util.Base64;
import java.util.Scanner;
import javax.crypto.*;

public class Cliente extends Conexion {

	/**
	 * ATRIBUTOS
	 */

	/**
	 * Client Socket - Socket del Cliente
	 */
	private Socket cs;

	/**
	 * Nombre del Cliente
	 */
	private String nombre;

	/**
	 * ID del paquete del Cliente
	 */
	private String idPaquete;

	/**
	 * Estado del paquete del Cliente
	 */
	private String estadoPaquete;

	/**
	 * Llave publica del Servidor
	 */
	private PublicKey pkServidor;

	/**
	 * Llave simetrica
	 */
	private SecretKey llaveSimetrica;

	/**
	 * Reto numerico enviado al Servidor
	 */
	private String reto;

	/**
	 * Scanner del Cliente (input consola)
	 */
	private Scanner scn;

	/**
	 * PrintWriter del Cliente - output writer (comunicacion con el Servidor)
	 */
	private PrintWriter pw;

	/**
	 * BufferedReader del Cliente - input reader (lectura de mensajes del Servidor)
	 */
	private BufferedReader bf;

	/**
	 * Tiempo que toma el cifrado con metodo asimetrico
	 */
	private String tiempoAsimetrico;

	/**
	 * Tiempo que toma el cifrado con metodo simetrico
	 */
	private String tiempoSimetrico;

	/**
	 * METODOS
	 */

	/**
	 * Constructor de la clase Cliente
	 * 
	 * @throws IOException
	 */
	public Cliente() throws IOException {
		cs = new Socket(HOST, PORT);
		reto = "";
		nombre = "";
		idPaquete = "";
		estadoPaquete = "";
		scn = new Scanner(System.in);
		pw = new PrintWriter(cs.getOutputStream());
		bf = new BufferedReader(new InputStreamReader(cs.getInputStream()));
	}

	/**
	 * Protocolo 0: El Cliente lee la llave publica del Servidor
	 * 
	 * @throws IOException
	 */
	public void leerPkServidor() throws IOException {
		File publicKeyFile = new File("./keys/public.key");
		byte[] publicKeyBytes = Files.readAllBytes(publicKeyFile.toPath());

		KeyFactory keyFactory;

		try {
			keyFactory = KeyFactory.getInstance("RSA");
			EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
			pkServidor = keyFactory.generatePublic(publicKeySpec);
			System.out.println("[0] Se ha leido la llave publica del Servidor\n");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Protocolo 1, 2: El Cliente pide iniciar sesion y recibe confirmacion por
	 * parte del Servidor
	 * 
	 * @throws IOException
	 */
	public void inicioSesion() throws IOException {
		String inicio = "";
		while (!inicio.equalsIgnoreCase("INICIO")) {
			System.out.println("[1] Escriba INICIO para iniciar sesion: ");
			inicio = scn.nextLine();
		}

		System.out.println("");
		pw.println(inicio);
		pw.flush();

		// confirmacion del Servidor
		String str = bf.readLine();
		System.out.println("[2] Servidor: " + str + "\n");
	}
	
	/**
	 * Protocolo 3: El Cliente envia el reto al Servidor, por default el reto es un
	 * numero aleatorio de 24 digitos
	 * 
	 * @param l, longitud del reto
	 * @throws IOException
	 */
	public void enviarReto(int l) throws IOException {
		reto = generarReto(l);

		System.out.println("[3] Enviando el reto al Servidor: " + reto + "\n");

		pw.println("Reto <" + reto + ">");
		pw.flush();
	}

	/**
	 * Protocolo 4, 5: El Cliente valida si el reto encriptado es igual al generado,
	 * si es correcto se genera la llave sim??trica y se env??a cifrada con la llave
	 * p??blica al Servidor
	 * 
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	public void validarRetoCifrado() throws IOException, NoSuchAlgorithmException {
		// Reto cifrado por el Servidor
		String str = bf.readLine();
		System.out.println("[4] Servidor: Reto Cifrado <" + str + ">\n");

		String retoDescifrado = descifrarPublica(str);

		if (!reto.equals(retoDescifrado)) {
			terminarConexion();
		}

		// genera llave simetrica de 256 bits
		generateKey(256);

		Cipher encryptCipher;

		try {
			encryptCipher = Cipher.getInstance("RSA");
			encryptCipher.init(Cipher.ENCRYPT_MODE, pkServidor);

			byte[] encryptedKey = encryptCipher.doFinal(llaveSimetrica.getEncoded());
			String encodedKey = Base64.getEncoder().encodeToString(encryptedKey);

			// env??o llave simetrica encriptada RSA
			System.out.println("[5] Enviando la llave simetrica al Servidor: " + encodedKey + "\n");
			pw.println("LS <" + encodedKey + ">");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException
				| BadPaddingException e) {
			e.printStackTrace();
		}

		pw.flush();
	}

	/**
	 * Protocolo 6, 7, 8, 9: El Cliente envia su nombre. Si el nombre esta en la
	 * tabla el Cliente recibe un mensaje de confirmacion por parte del Servidor. Si
	 * el nombre no esta en la tabla el Cliente recibe un error por parte del
	 * Servidor
	 */
	public void enviarNombre() throws IOException {
		// confirmacion de LS
		String str = bf.readLine();
		System.out.println("[6] Servidor: " + str + "\n");

		if (str.equals("ACK")) {
			System.out.println("[7] Escriba su nombre: ");
			nombre = scn.nextLine();
			System.out.println("");

			String encodedName = cifrarPublica(nombre);

			// env??o nombre encriptado RSA
			pw.println("Nombre <" + encodedName + ">");
			System.out.println("[7] Enviando nombre cifrado al Servidor: " + encodedName + "\n");
			pw.flush();
		}

		// confirmacion del nombre
		str = bf.readLine();
		System.out.println("[8] Servidor: " + str + "\n");

		if (str.equals("ERROR")) {
			terminarConexion();
		} else {
			System.out.println("[9] Escriba el identificador de paquete: ");
			idPaquete = scn.nextLine();
			System.out.println("");

			String encodedPaquete = cifrarConLlaveSimetrica(idPaquete);

			pw.println("ID-PKT <" + encodedPaquete + ">");
			System.out.println("[9] Enviando id de paquete cifrado al Servidor: " + encodedPaquete + "\n");
		}

		pw.flush();
	}

	/**
	 * Protocolo 10, 11: Se recibe el estado de paquete y se confirma "ACK"
	 * 
	 * @throws IOException
	 */
	public void recibirEstado() throws IOException {
		String str = bf.readLine();
		if (str.equals("DESCONOCIDO")) {
			terminarConexion();
			return;
		}

		estadoPaquete = descifrarConLlaveSimetrica(str);
		System.out.println("[10] Servidor: Estado PKT <" + estadoPaquete + ">\n");

		System.out.println("[11] Escriba ACK para confirmar: ");
		String ack = scn.nextLine();
		System.out.println("");

		pw.println(ack);
		pw.flush();
	}

	/**
	 * Paso 12, 13: Se recibe el resumen y se verifica
	 * 
	 * @throws IOException
	 * @throws Exception
	 */
	public void recibirResumen() throws IOException {
		String str = bf.readLine();
		if (str.startsWith("TS <")) {
			tiempoSimetrico = str.substring(4, str.length() - 1);

		}

		str = bf.readLine();
		if (str.startsWith("TA <")) {
			tiempoAsimetrico = str.substring(4, str.length() - 1);
		}

		str = bf.readLine();
		if (str.startsWith("DIGEST <")) {
			str = str.substring(8, str.length() - 1);
		}

		try {
			String comparacion = codigoResumen(
					reto + nombre + idPaquete + estadoPaquete + tiempoSimetrico + tiempoAsimetrico);

			if (str.equals(comparacion)) {
				System.out.println("[12] Resumen");
				System.out.println("\t- El reto enviado fue: " + reto);
				System.out.println("\t- El nombre del Cliente que consulta es: " + nombre);
				System.out.println("\t- El ID del paquete consultado es: " + idPaquete);
				System.out.println("\t- El estado del paquete consultado es: " + estadoPaquete);
				System.out.println(
						"\t- El tiempo de cifrado simetrico del reto fue de: " + tiempoSimetrico + " nanosegundos");
				System.out.println(
						"\t- El tiempo de cifrado asimetrico del reto fue de: " + tiempoAsimetrico + " nanosegundos");
			} else {
				System.out.println("[12] ERROR: No se pudo verificar/ mostrar la informacion");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("");
		pw.println("TERMINAR");
		System.out.println("[13] Terminando conexion con el Servidor...\n");

	}

	/**
	 * Ultimo paso: Terminar conexion
	 * 
	 * @throws IOException
	 */
	public void terminarConexion() throws IOException {
		pw.println("TERMINAR");
		pw.flush();
		scn.close();
		cs.close();
		System.out.println("Fin de la conexion.\n");
	}
	
	/**
	 * Otros metodos auxiliares
	 */

	public String generarReto(int l) {
		String retoo = "";
		int min = 0;
		int max = 9;
		int rnd = 0;
		for (int i = 0; i < l; i++) {
			// primer digito: min ??? 0
			min = i == 0 ? 1 : 0;
			rnd = (int) (Math.random() * max) + min;
			retoo += rnd;
		}
		return retoo;
	}
	
	public String codigoResumen(String mensaje) throws Exception {
		Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
		sha256_HMAC.init(llaveSimetrica);
		return byteArrayToHex(sha256_HMAC.doFinal(mensaje.getBytes("UTF-8")));
	}

	public static String byteArrayToHex(byte[] a) {
		StringBuilder sb = new StringBuilder(a.length * 2);
		for (byte b : a)
			sb.append(String.format("%02x", b));
		return sb.toString();
	}

	public String descifrarPublica(String mensaje) {
		Cipher decryptCipher;
		String mensajeDescifrado = "";
		try {
			decryptCipher = Cipher.getInstance("RSA");
			decryptCipher.init(Cipher.DECRYPT_MODE, pkServidor);

			byte[] secretMessageBytes = Base64.getDecoder().decode(mensaje);

			byte[] decryptedMessageBytes = decryptCipher.doFinal(secretMessageBytes);
			mensajeDescifrado = new String(decryptedMessageBytes, StandardCharsets.UTF_8);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException
				| BadPaddingException e) {
			e.printStackTrace();
		}

		return mensajeDescifrado;
	}

	public String cifrarPublica(String mensaje) {
		Cipher encryptCipher;
		String encodedName = "";
		try {
			encryptCipher = Cipher.getInstance("RSA");
			encryptCipher.init(Cipher.ENCRYPT_MODE, pkServidor);

			byte[] secretMessageBytes = mensaje.getBytes(StandardCharsets.UTF_8);
			byte[] encryptedMessageBytes = encryptCipher.doFinal(secretMessageBytes);
			encodedName = Base64.getEncoder().encodeToString(encryptedMessageBytes);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException
				| BadPaddingException e) {
			e.printStackTrace();
		}

		return encodedName;
	}

	public String cifrarConLlaveSimetrica(String mensaje) {
		Cipher cipher;

		String respuesta = "";

		try {
			cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, llaveSimetrica);
			byte[] cipherText = cipher.doFinal(mensaje.getBytes());
			respuesta = Base64.getEncoder().encodeToString(cipherText);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException
				| BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return respuesta;
	}

	public String descifrarConLlaveSimetrica(String mensaje) {
		Cipher cipher;
		String respuesta = "";

		try {
			cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, llaveSimetrica);

			byte[] plainText = cipher.doFinal(Base64.getDecoder().decode(mensaje));

			respuesta = new String(plainText);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException
				| BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return respuesta;
	}

	public void generateKey(int n) throws NoSuchAlgorithmException {
		KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
		keyGenerator.init(n);
		llaveSimetrica = keyGenerator.generateKey();
	}

	public SecretKey getLlaveSimetrica() {
		return llaveSimetrica;
	}
	
	public String getReto() {
		return reto;
	}
	
	public Socket getCS() {
		return cs;
	}

	/**
	 * Inicia el socket del Cliente
	 * 
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	public void start() throws IOException, NoSuchAlgorithmException {

		System.out.println("*** Cliente ***\n");

		// protocolo 0
		leerPkServidor();

		// protocolo 1, 2
		inicioSesion();

		// protocolo 3
		enviarReto(24);

		// protocolo 4, 5
		validarRetoCifrado();

		// protocolo 6, 7, 8, 9
		enviarNombre();

		// protocolo 10, 11
		recibirEstado();

		// protocolo 11
		recibirResumen();

		// terminar conexion
		terminarConexion();
	}
}
