package sockets;

import clases.Tabla;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

/**
 * Clase que hace uso del servidor
 */
public class Servidor extends Conexion {

	/**
	 * ATRIBUTOS
	 */

	/**
	 * Server Socket - Socket del Servidor
	 */
	private ServerSocket ss;

	/**
	 * Socket - Socket del Cliente
	 */
	private Socket cs;

	/**
	 * Llave privada (secret) del Servidor
	 */
	private PrivateKey sk;

	/**
	 * Llave publica (public) del Servidor
	 */
	private PublicKey pk;

	/**
	 * Reto numerico enviado por el Cliente
	 */
	private String reto;

	/**
	 * PrintWriter del Servidor - output writer (comunicacion con el Cliente)
	 */
	private PrintWriter pw;

	/**
	 * BufferedReader del Servidor - input reader (lectura de mensajes del Cliente)
	 */
	private BufferedReader bf;

	/**
	 * Llave simetrica generada por el cliente
	 */
	private SecretKey llaveSimetrica;

	/**
	 * Tabla de servidor
	 */
	private Tabla tabla;

	/**
	 * Nombre del Cliente
	 */
	private String nombre;
	
	/**
	 * Estado de paquete respuesta
	 */
	private String estadoPaquete;

	/**
	 * ID de paquete respuesta
	 */
	private String idPaquete;

	/**
	 * Tiempo asimetrico
	 */
	private double tiempoAsimetrico;
	
	/**
	 * Tiempo Simetrico
	 */
	private double tiempoSimetrico;

	/**
	 * METODOS
	 */

	/**
	 * Constructor de la clase Servidor
	 * 
	 * @throws IOException
	 */
	public Servidor() throws IOException {
		ss = new ServerSocket(PORT);
		tabla = new Tabla();
		nombre = "";
		idPaquete = "";
		estadoPaquete = "";
		tiempoAsimetrico = 0.0;
		tiempoSimetrico = 0.0;
	}

	/**
	 * Lee los mensajes enviados por el cliente
	 * 
	 * @throws IOException
	 */
	public void leerMensajesCliente() throws IOException {
		String str;
		while (!(str = bf.readLine()).equals("TERMINAR")) {
			// mensaje 1 y mensaje 2
			if (str.equalsIgnoreCase("INICIO")) {
				// mensaje 1 - inicio de sesion
				System.out.println("Cliente: " + str + "\n");
				// mensaje 2 - confirmacion de inicio de sesion
				pw.println("ACK");
			}

			// mensaje 3 y mensaje 4 - Reto
			if (str.startsWith("Reto <")) {
				// mensaje 3 - reto
				System.out.println("Cliente: " + str + "\n");
				reto = str.substring(6, str.length() - 1);

				long inicio = System.nanoTime();
				String encodedMessage = cifrarConLlavePrivada(reto);
				long fin = System.nanoTime();
				tiempoAsimetrico = (double) ((fin - inicio));
				// mensaje 4 - reto cifrado
				pw.println(encodedMessage);
			}

			// mensaje 5 - recibe LS cifrada y confirma con "ACK"
			if (str.startsWith("LS <")) {
				System.out.println("Cliente: " + str + "\n");
				String llaveString = str.substring(4, str.length()-1);
				byte[] decryptedMessageBytes = descifrarConLlavePrivada(llaveString);
				llaveSimetrica = new SecretKeySpec(decryptedMessageBytes, 0, decryptedMessageBytes.length, "AES");

				pw.println("ACK");
			}

			// mensaje 6 - recibe nombre de cliente, descifra y verifica en tabla
			if (str.startsWith("Nombre <")) {
				System.out.println("Cliente: " + str + "\n");
				String nombreencriptado = str.substring(8, str.length()-1);
				byte[] decryptedMessageBytes = descifrarConLlavePrivada(nombreencriptado);
				nombre = new String(decryptedMessageBytes, StandardCharsets.UTF_8);

				// verificacion en la tabla
				if (tabla.existeNombre(nombre))
					pw.println("ACK");
				else
					pw.println("ERROR");
			}

			// mensaje 6: recibe idPaquete y verifica
			if (str.startsWith("ID-PKT <")) {
				System.out.println("Cliente: " + str + "\n");
				String idPaqueteEncoded = str.substring(8, str.length()-1);

				idPaquete = descifrarConLlaveSimetrica(idPaqueteEncoded);

				int indice = tabla.nombrePaquete(nombre, idPaquete);
				if (tabla.nombrePaquete(nombre, idPaquete) >= 0) {

					estadoPaquete = tabla.darTablaEstados().get(indice);
					pw.println(cifrarConLlaveSimetrica(estadoPaquete + ""));

				} else {
					pw.println("DESCONOCIDO");
				}
			}

			if (str.startsWith("ACK")) {
				try {
					// Prueba de cifrado de reto con llave simetrica
					long inicio1 = System.nanoTime();
					cifrarConLlaveSimetrica(reto);
					long fin1 = System.nanoTime();
					tiempoSimetrico = (double) ((fin1 - inicio1));

					//System.out.println(tiempoSimetrico);
					//System.out.println(tiempoAsimetrico);

					pw.println("TS <" + tiempoSimetrico + ">");
					pw.println("TA <" + tiempoAsimetrico + ">");

					pw.println("DIGEST <" + codigoResumen(reto + nombre + idPaquete + estadoPaquete + tiempoSimetrico + tiempoAsimetrico) + ">");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			pw.flush();
		}
		
		if (str.equalsIgnoreCase("TERMINAR")) {
			System.out.println("Cliente: " + str + "\n");
		}
	}
	
	/**
	 * Metodos auxiliares
	 */

	// Referencia: https://gist.github.com/lesstif/655f6b295a619306405621e02634a08d
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

	public void generarLlavesPublicaPrivada() {
		KeyPairGenerator generator;

		try {
			generator = KeyPairGenerator.getInstance("RSA");
			generator.initialize(1024);
			KeyPair pair = generator.generateKeyPair();
			sk = pair.getPrivate();
			pk = pair.getPublic();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		try (FileOutputStream fos = new FileOutputStream("./keys/public.key")) {
			fos.write(pk.getEncoded());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String display(byte[] byteArray1) {
		StringBuilder stringBuilder = new StringBuilder();

		for (byte val : byteArray1) {
			stringBuilder.append(String.format("%02x", val & 0xff));
		}

		return stringBuilder.toString();
	}

	public byte[] descifrarConLlavePrivada(String mensaje) {
		Cipher decryptCipher;
		byte[] decryptedMessageBytes = new byte[0];

		try {
			decryptCipher = Cipher.getInstance("RSA");
			decryptCipher.init(Cipher.DECRYPT_MODE, sk);

			byte[] secretMessageBytes = Base64.getDecoder().decode(mensaje);

			decryptedMessageBytes = decryptCipher.doFinal(secretMessageBytes);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException
				| BadPaddingException e) {
			e.printStackTrace();
		}

		return decryptedMessageBytes;
	}

	public String cifrarConLlavePrivada(String mensaje) {
		Cipher encryptCipher;
		String encodedMessage = "";
		try {
			encryptCipher = Cipher.getInstance("RSA");
			encryptCipher.init(Cipher.ENCRYPT_MODE, sk);

			byte[] secretMessageBytes = mensaje.getBytes(StandardCharsets.UTF_8);
			byte[] encryptedMessageBytes = encryptCipher.doFinal(secretMessageBytes);
			encodedMessage = Base64.getEncoder().encodeToString(encryptedMessageBytes);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException
				| BadPaddingException e) {
			e.printStackTrace();
		}

		return encodedMessage;
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
			e.printStackTrace();
		}

		return respuesta;
	}
	
	public void setLlaveSimetrica(SecretKey LS) {
		this.llaveSimetrica = LS;
	}
	
	public ServerSocket getSS() {
		return ss;
	}
	
	public void start() throws IOException {
		// Mensajes iniciales
		System.out.println("*** Servidor ***\n");
		System.out.println("Iniciando Servidor en el puerto: " + PORT);
		System.out.println("Esperando conexion...");
		generarLlavesPublicaPrivada();
		// Socket del Cliente
		cs = ss.accept();
		System.out.println("Cliente conectado\n");

		// Output writer
		pw = new PrintWriter(cs.getOutputStream());

		// Input reader
		InputStreamReader in = new InputStreamReader(cs.getInputStream());
		bf = new BufferedReader(in);

		// mensajes recibidos por el Cliente
		leerMensajesCliente();

		// Fin de la conexion
		ss.close();
		System.out.println("Fin de la conexión.\n");
	}
}
