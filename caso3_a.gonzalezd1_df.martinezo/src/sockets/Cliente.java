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
		File publicKeyFile = new File("public.key");
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
	 * Protocolo 1, 2: El Cliente pide iniciar sesion y recibe
	 * confirmacion por parte del Servidor
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
		String retoMensaje = "<";
		int min = 0;
		int max = 9;
		int rnd = 0;
		for (int i = 0; i < l; i++) {
			// primer digito: min ≠ 0
			min = i == 0 ? 1 : 0;
			int rnd = (int) (Math.random() * max) + min;
      		reto+=rnd+"";

			retoMensaje += rnd;
		}

		retoMensaje += ">";
		System.out.println("[3] Enviando el reto al Servidor: " + reto + "\n");

		pw.println(retoMensaje);
		pw.flush();
	}

	/**
	 * Protocolo 4, 5: El Cliente valida si el reto encriptado es igual
	 * al generado, si es correcto se genera la llave simétrica y se envía cifrada
	 * con la llave pública al Servidor
	 * 
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	public void validarRetoCifrado() throws IOException, NoSuchAlgorithmException {
		// Reto cifrado por el Servidor
		String str = bf.readLine();
		System.out.println("[4] Servidor: Reto cifrado -> " + str + "\n");

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

			// envío llave simetrica encriptada RSA
			System.out.println("[5] Enviando la llave simetrica al Servidor: " + encodedKey + "\n");
			pw.println("LS: " + encodedKey);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException
				| BadPaddingException e) {
			e.printStackTrace();
		}

		pw.flush();
	}

	/**
	 * Protocolo 6, 7, 8, 9: El Cliente envia su nombre. Si el nombre esta en la tabla el Cliente
	 * recibe un mensaje de confirmacion por parte del Servidor. Si el nombre no esta
	 * en la tabla el Cliente recibe un error por parte del Servidor
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

			// envío nombre encriptado RSA
			pw.println("Nombre -> " + encodedName);
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
			String idPaquete = scn.nextLine();
			
			// TODO: ERROR!
			String encodedPaquete = cifrarConLlaveSimetrica(idPaquete);

			pw.print("ID-PKT: " + encodedPaquete);
		}

		pw.flush();
	}
	
	/**
	 * Protocolo 10, 11: Se recibe el estado de paquete y se envia "ACK"
	 * 
	 * @throws IOException
	 */
	public void recibirEstado() throws IOException {
		String str = bf.readLine();
		if (str.equals("ERROR")) {
			terminarConexion();
			return;
		}

		estadoPaquete = str;
		
		System.out.println("[1] Escriba ACK para confirmar: ");
		String ack = scn.nextLine();
		
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

		String comparacion;
		try {
			comparacion = codigoResumen(estadoPaquete);
			if (str.equals(comparacion)) {
				pw.println("TERMINAR");
			} else {
				terminarConexion();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return encodedName;
	}
	
	// TODO: ERROR!
	public String cifrarConLlaveSimetrica(String mensaje) {
		Cipher cipher;

		String respuesta = "";

		try {
			cipher = Cipher.getInstance("AES/EBC/PKCS5Padding");
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

	public void generateKey(int n) throws NoSuchAlgorithmException {
		KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
		keyGenerator.init(n);
		llaveSimetrica = keyGenerator.generateKey();
	}

	/**
	 * Ultimo paso: La conexion termina
	 * 
	 * @throws IOException
	 */
	public void terminarConexion() throws IOException {
		pw.println("TERMINAR");
		pw.flush();
		scn.close();
		cs.close();
		System.out.println("Fin de la conexión.\n");
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
