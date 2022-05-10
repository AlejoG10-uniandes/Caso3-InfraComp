package clases;

import java.util.ArrayList;

public class Tabla {
	
	/**
	 * --------------------
	 * CONSTANTES (ESTADOS)
	 * --------------------
	 */
	
	private static final int PKT_DESCONOCIDO = -1;
	
	private static final int PKT_EN_OFICINA = 0;
	
	private static final int PKT_RECOGIDO = 1;
	
	private static final int PKT_EN_CLASIFICACION = 2;
	
	private static final int PKT_DESPACHADO = 3;
	
	private static final int PKT_EN_ENTREGA = 4;
	
	private static final int PKT_ENTREGADO = 5;
	
	/**
	 * ---------
	 * ATRIBUTOS
	 * ---------
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
	private ArrayList<Integer> estadosPkts = new ArrayList<Integer>();
}
