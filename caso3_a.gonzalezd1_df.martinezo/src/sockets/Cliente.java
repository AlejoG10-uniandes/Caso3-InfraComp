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
	
	private PublicKey publicKeyServer;

	private SecretKey llaveSimetrica;

	private String reto;

	private String nombre;

	private String estadoPaquete;
	
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
	 * @throws IOException 
	 */
	public void leerPkServidor() throws IOException {
		


		File publicKeyFile = new File("public.key");
		byte[] publicKeyBytes = Files.readAllBytes(publicKeyFile.toPath());
		
		KeyFactory keyFactory;
		try {
			keyFactory = KeyFactory.getInstance("RSA");
			EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
			publicKeyServer = keyFactory.generatePublic(publicKeySpec);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Paso 2: El Cliente pide iniciar sesion y recibe confirmacion por el Servidor
	 * @throws IOException 
	 */
	public void peticionIniciarSesion() throws IOException {
		
		
		
		
		pw.println("INICIO");
		pw.flush();
		
		// String str = bf.readLine();
		// System.out.println("Servidor: " + str + "\n");
	}
	
	/**
	 * Paso 3: El Cliente envia el reto al Servidor (numero aleatorio de 24 digitos)
	 * @param l, longitud del reto
	 * @throws IOException
	 */
	public void enviarReto(int l) throws IOException {
		String str = bf.readLine();

		String retoMensaje = "reto -> ";
		int min = 0;
		int max = 9;
		int rnd = 0;
		for (int i=0; i < l; i++) {
			// primer digito: min ≠ 0
			min = i == 0 ? 1 : 0;
			
			// (int) (Math.random() * max) + min
			rnd = (int) (Math.random() * max) + min;
			reto+=rnd+"";
			retoMensaje += rnd;
		}
		
		pw.println(retoMensaje);
		pw.flush();
	}

	/**
	 * Paso 4: El Cliente valida si el reto encriptado es igual al generado, si es correcto se genera la llave simétrica
	 * y se envía cifrada con la llave pública al Servidor
	 * @throws NoSuchAlgorithmException
	 */
	
	public void validacionRetoEncriptado() throws IOException, NoSuchAlgorithmException {

		String str = bf.readLine();
		
		String retoDescifrado = descifrarPublica(str);

			if(!reto.equals(retoDescifrado)){

				terminarConexion();

			}
		// genera llave simetrica de 256 bits
			generateKey(256);

			Cipher encryptCipher;
				try {
					encryptCipher = Cipher.getInstance("RSA");
					encryptCipher.init(Cipher.ENCRYPT_MODE, publicKeyServer);

					byte[] encryptedKey = encryptCipher.doFinal(llaveSimetrica.getEncoded());
					String encodedKey = Base64.getEncoder().encodeToString(encryptedKey);

					// envío llave simetrica encriptada RSA
					pw.println("llave simetrica: " + encodedKey);
					pw.flush();


				} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}


		pw.flush();

	}

	/**
	 * Paso 5: Se envia el nombre y se espera confirmacion para seguir con el id de paquete
	 * @throws IOException
	 */
	public void envioNombre() throws IOException{

		String str = bf.readLine();

		
		
		if(str.equals("ACK")){

			Scanner reader = new Scanner(System.in);  

			System.out.println("Escriba su nombre: ");
			nombre = reader.nextLine();

			String encodedName = cifrarPublica(nombre);

			// envío nombre encriptado RSA
			pw.println("nombre: " + encodedName);
			reader.close();

		}
			

			str = bf.readLine();
			if(str.equals("ERROR")){
				terminarConexion();
			}
			else{
				Scanner reader = new Scanner(System.in);  

				System.out.println("Escriba el identificador de paquete: ");
				String idPaquete = reader.nextLine();

				String encodedPaquete = cifrarConLlaveSimetrica(idPaquete);

				pw.print("idPaquete: " + encodedPaquete);
				reader.close();

			}

			pw.flush();

		}

		/**
	 * Paso 6: Se recibe el estado de paquete y se envia "ACK"
	 * @throws IOException
	 */

	 public void recibirEstado() throws IOException{
		String str = bf.readLine();
		if(str.equals("ERROR")){
			terminarConexion();
		}

		estadoPaquete = str;
		pw.println("ACK");

		pw.flush();
	 }


	 /**
	 * Paso 7: Se recibe el resumen y se verifica
	 * @throws IOException
	 * @throws Exception
	 */
	public void recibirResumen() throws IOException{
		String str = bf.readLine();

		String comparacion;
		try {
			comparacion = codigoResumen(estadoPaquete);
			if(str.equals(comparacion)){
				pw.println("TERMINAR");
			}
			else{
				terminarConexion();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
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
	for(byte b: a)
		sb.append(String.format("%02x", b));
	return sb.toString();
}

	public String descifrarPublica(String mensaje){
		Cipher decryptCipher;
		String mensajeDescifrado = "";
		try {
			decryptCipher = Cipher.getInstance("RSA");
			decryptCipher.init(Cipher.DECRYPT_MODE, publicKeyServer);

			byte[] secretMessageBytes = Base64.getDecoder().decode(mensaje);

			byte[] decryptedMessageBytes = decryptCipher.doFinal(secretMessageBytes);
			mensajeDescifrado = new String(decryptedMessageBytes, StandardCharsets.UTF_8);
		}
		catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

			return mensajeDescifrado;
	}

	public String cifrarPublica(String mensaje){

		Cipher encryptCipher;
		String encodedName = "";
		try {
			encryptCipher = Cipher.getInstance("RSA");
			encryptCipher.init(Cipher.ENCRYPT_MODE, publicKeyServer);

			byte[] secretMessageBytes = mensaje.getBytes(StandardCharsets.UTF_8);
			byte[] encryptedMessageBytes = encryptCipher.doFinal(secretMessageBytes);
			encodedName = Base64.getEncoder().encodeToString(encryptedMessageBytes);


		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return encodedName;

	}

	public String cifrarConLlaveSimetrica(String mensaje){
    
    Cipher cipher;

	String respuesta="";

	try {

		cipher = Cipher.getInstance("AES/EBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, llaveSimetrica);
    	byte[] cipherText = cipher.doFinal(mensaje.getBytes());
		respuesta = Base64.getEncoder().encodeToString(cipherText);
	} 
	catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
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

	public void terminarConexion() throws IOException {
		pw.println("END");
		pw.flush();
		cs.close();
		System.out.println("Fin de la conexión.\n");
	}
	
	/**
	 * Inicia el socket del Cliente
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	public void start() throws IOException, NoSuchAlgorithmException
    {	
		reto="";
		// paso 1
		leerPkServidor();
		
		// paso 2
		peticionIniciarSesion();
		
		// paso 3
		enviarReto(24);

		// paso 4
		validacionRetoEncriptado();

		// paso 5
		envioNombre();

		// paso 6
		recibirEstado();

		// paso 7
		recibirResumen();
		
		// TODO: DEMAS PASOS
		
		// terminar conexion
		terminarConexion();
    }
}
