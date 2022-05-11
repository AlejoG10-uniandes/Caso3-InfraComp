package clases;

import java.util.ArrayList;

public class Tabla {

	/**
	 * CONSTANTES (ESTADOS)
	 */

	private static final int PKT_EN_OFICINA = 0;

	private static final int PKT_RECOGIDO = 1;

	private static final int PKT_EN_CLASIFICACION = 2;

	private static final int PKT_DESPACHADO = 3;

	private static final int PKT_EN_ENTREGA = 4;

	private static final int PKT_ENTREGADO = 5;

	private static final int PKT_DESCONOCIDO = 6;
	
	/**
	 * ATRIBUTOS
	 */

	/**
	 * Nombres de los clientes
	 */
	private ArrayList<String> nombres = new ArrayList<String>();

	/**
	 * Ids de los paquetes
	 */
	private ArrayList<String> idsPkts = new ArrayList<String>();

	/**
	 * Estados de los paquetes
	 */
	private ArrayList<String> estadosPkts = new ArrayList<String>();

	/**
	 * Constructor de la clase Tabla
	 * 
	 * @param nombres
	 * @param idsPkts
	 * @param estadosPkts
	 */

	public Tabla() {
		ArrayList<String> nombres = new ArrayList<>();
		ArrayList<String> idsPkts = new ArrayList<>();
		ArrayList<String> estadosPkts = new ArrayList<>();
		
		// nombres
		for (int i = 0; i < 8; i++) {
			nombres.add("carlos");
			nombres.add("juan");
			nombres.add("maria");
			nombres.add("laura");
		}

		// ids y estados
		String estado = "ERROR";
		for (int i = 0; i < 32; i++) {
			idsPkts.add("id-" + i);

			if (i%7 == PKT_EN_OFICINA)
				estado = "PKT_EN_OFICINA";
		
			if (i%7 == PKT_RECOGIDO)
				estado = "PKT_RECOGIDO";
			
			if (i%7 == PKT_EN_CLASIFICACION)
				estado = "PKT_EN_CLASIFICACON";
			
			if (i%7 == PKT_DESPACHADO)
				estado = "PKT_DESPACHADO";
			
			if (i%7 == PKT_EN_ENTREGA)
				estado = "PKT_EN_ENTREGA";
			
			if (i%7 == PKT_ENTREGADO)
				estado = "PKT_ENTREGADO";
			
			if (i%7 == PKT_DESCONOCIDO)
				estado = "PKT_DESCONOCIDO";
		
			estadosPkts.add(estado);
		}
		
		// asignacion
		this.nombres = nombres;
		this.estadosPkts = estadosPkts;
		this.idsPkts = idsPkts;
	}

	/**
	 * Valida si existe el nombre de un Cliente en la tabla
	 * 
	 * @param nombre
	 * @return existe
	 */
	public boolean existeNombre(String nombre) {
		boolean existe = false;

		for (int i = 0; i < nombres.size() && !existe; i++) {
			if (nombres.get(i).equals(nombre)) {
				existe = true;
			}
		}

		return existe;
	}

	/**
	 * Valida si existe un id de paquete relacionado a un nombre de Cliente
	 * 
	 * @param nombre
	 * @param idPaquete
	 * @return indiceRespuesta
	 */
	public int nombrePaquete(String nombre, String idPaquete) {
		boolean termine = false;
		int indiceNombre = 0;
		int indiceRespuesta = -1;

		for (int i = 0; i < nombres.size() && !termine; i++) {
			if (nombres.get(i).equals(nombre)) {
				indiceNombre = i;
				termine = true;
			}
		}

		if (idsPkts.get(indiceNombre).equals(idPaquete)) {
			indiceRespuesta = indiceNombre;
		}

		return indiceRespuesta;
	}

	/**
	 * Retorna los estados de los paquetes en la tabla
	 * 
	 * @return estadosPkts
	 */
	public ArrayList<String> darTablaEstados() {
		return estadosPkts;
	}
}
