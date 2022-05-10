package sockets;

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
import java.util.ArrayList;
import java.util.Base64;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

import clases.Tabla;

/**
 * Clase que hace uso del servidor
 */
public class Servidor extends Conexion {
	
	/**
	 * ---------
	 * ATRIBUTOS
	 * ---------
	 */
	
	/**
	 * Server Sockes - Socket del Servidor
	 */
	private ServerSocket ss;
	
	/**
	 * Socket - Socket del Cliente
	 */
	private Socket cs;
	
	/**
	 * Llave privada (secret) del Servidor
	 */
	private PrivateKey privateKey;
	
	/**
	 * Llave publica (public) del Servidor
	 */
	private PublicKey publicKey;
	
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
	 * -------
	 * METODOS
	 * -------
	 */
	
	/**
	 * Constructor de la clase Servidor
	 * @throws IOException
	 */
	public Servidor() throws IOException {
		ss = new ServerSocket(PORT);
		ArrayList<String>nombres = new ArrayList<>();
		ArrayList<String>idsPkts = new ArrayList<>();
		ArrayList<Integer>estadosPkts = new ArrayList<>();

		for(int i =0; i<8 ; i++){
			nombres.add("carlos");
			nombres.add("juan");
			nombres.add("maria");
			nombres.add("laura");
		}
		
		
		int j = -1;
		int valorEntero=0;
		for(int i=0; i<32; i++){
			idsPkts.add(i+"");
			
			if(j<6){
				valorEntero = j;
				j++;
			}
			else{
				j=-1;
				valorEntero=j;
			}
			
			estadosPkts.add(valorEntero);
		}

	}
	
	/**
	 * Lee los mensajes enviados por el cliente
	 * @throws IOException 
	 */
	public void leerMensajesCliente() throws IOException {
		String str;
		while(!(str = bf.readLine()).equals("END")) {
			
			if (str == null)
				continue;
			
			
			
			// mensaje 1 - inicio de sesion
            if (str.equalsIgnoreCase("INICIO")) {
            	System.out.println("Cliente: " + str + "\n");
            	pw.println("ACK");
            }
            
            // mensaje 2 - reto
            if (str.startsWith("reto -> ")) {
            	System.out.println("Cliente: " + str + "\n");
            	reto = str.substring(8, str.length());

				// mensaje 3 - encriptar reto con llave privada
				String encodedMessage = cifrarConLlavePrivada(reto);

				// envío reto encriptado RSA
				pw.println(encodedMessage);

				}


			// mensaje 4 recibe llave simetrica encriptada y confirma con "ACK"
			if (str.startsWith("llave simetrica: ")) {

					String llaveString = str.substring(17, str.length());

					byte[] decryptedMessageBytes = descifrarConLlavePrivada(llaveString);
					
					llaveSimetrica = new SecretKeySpec(decryptedMessageBytes , 0, decryptedMessageBytes .length, "AES");

					pw.println("ACK");
			}

			String nombreDescifrado="";


			// mensaje 5: recibe nombre de cliente, descifra y verifica en tabla
			if (str.startsWith("nombre: ")) {

				String nombreencriptado = str.substring(8, str.length());

				byte[] decryptedMessageBytes = descifrarConLlavePrivada(nombreencriptado);

				nombreDescifrado = new String(decryptedMessageBytes, StandardCharsets.UTF_8);

				if(tabla.existeNombre(nombreDescifrado))
				{
					pw.println("ACK");
				}
				else
				{
					pw.println("ERROR");
				}
			}

			String idPaquete="";
			// mensaje 6: recibe idPaquete y verifica
			if (str.startsWith("idPaquete: ")) {
				String idPaqueteEncoded = str.substring(11, str.length());

				idPaquete = descifrarConLlaveSimetrica(idPaqueteEncoded);

				int indice = tabla.nombrePaquete(nombreDescifrado, idPaquete);

				if(tabla.nombrePaquete(nombreDescifrado, idPaquete)>=0)
				{
					pw.print(tabla.darTablaEstados().get(indice)+"");
				}
				else
				{
					pw.println("ERROR");
				}
			}


			if(str.startsWith("ACK")){

				try {
					pw.print(codigoResumen(idPaquete));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}


			}
				// TODO: DEMAS MENSAJES
				pw.flush();
			
		}
	}
	
//Referencia: https://gist.github.com/lesstif/655f6b295a619306405621e02634a08d
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
	  
	
	public void generarLlavesPublicaPrivada() {
		
		KeyPairGenerator generator;
		
		try {
			
			generator = KeyPairGenerator.getInstance("RSA");
			generator.initialize(1024);
			KeyPair pair = generator.generateKeyPair();
			privateKey = pair.getPrivate();
			publicKey = pair.getPublic();
			
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try (FileOutputStream fos = new FileOutputStream("public.key")) {
		    fos.write(publicKey.getEncoded());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String display(byte[] byteArray1) {
		StringBuilder stringBuilder = new StringBuilder();
		for(byte val : byteArray1) 
		{
			stringBuilder.append(String.format("%02x", val&0xff));
		}
		return stringBuilder.toString();
		}

	public byte[] descifrarConLlavePrivada(String mensaje){
		Cipher decryptCipher;
		byte[] decryptedMessageBytes = new byte[0];
				try {
					decryptCipher = Cipher.getInstance("RSA");
					decryptCipher.init(Cipher.DECRYPT_MODE, privateKey);

					byte[] secretMessageBytes = Base64.getDecoder().decode(mensaje);

					decryptedMessageBytes = decryptCipher.doFinal(secretMessageBytes);

					}

				catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
				return decryptedMessageBytes;

				
	}

	public String cifrarConLlavePrivada(String mensaje){

		Cipher encryptCipher;
		String encodedMessage = "";
				try {
					encryptCipher = Cipher.getInstance("RSA");
					encryptCipher.init(Cipher.ENCRYPT_MODE, privateKey);

					byte[] secretMessageBytes = mensaje.getBytes(StandardCharsets.UTF_8);
					byte[] encryptedMessageBytes = encryptCipher.doFinal(secretMessageBytes);
					encodedMessage = Base64.getEncoder().encodeToString(encryptedMessageBytes);


				} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

		return encodedMessage;

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
	

	public String descifrarConLlaveSimetrica(String mensaje){

		Cipher cipher;
		String respuesta = "";

		try {

			cipher = Cipher.getInstance("AES/EBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, llaveSimetrica);
		
			byte[] plainText = cipher.doFinal(Base64.getDecoder().decode(mensaje));

			respuesta = new String(plainText);

		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		return respuesta;
	}
	
	public void start() throws IOException
    {
		// Mensajes iniciales
		System.out.println("Iniciando Servidor en el puerto: " + PORT + "\n");
		System.out.println("Esperando conexión...\n");
		generarLlavesPublicaPrivada();
		
		// Socket del Cliente
		cs = ss.accept();
		System.out.println("Cliente conectado\n");
		
		// Output writer
		pw = new PrintWriter(cs.getOutputStream());
		
		// Input reader
		InputStreamReader in = new InputStreamReader(cs.getInputStream());
		bf = new BufferedReader(in);
		
		leerMensajesCliente();
		
		// Fin de la conexion
		ss.close();
		System.out.println("Fin de la conexión.\n");
    }
}
