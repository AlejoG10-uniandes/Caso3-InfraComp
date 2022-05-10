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


	public Tabla(ArrayList<String>nombres, ArrayList<String>idsPkts, ArrayList<Integer>estadosPkts){
		this.nombres = nombres;
		this.estadosPkts = estadosPkts;
		this.idsPkts = idsPkts;
	}

	public boolean existeNombre(String nombre){
		boolean existe = false;

		for(int i = 0 ; i <nombres.size() && !existe ; i++){
			if(nombres.get(i).equals(nombre)){
				existe = true;
			}
		}

		return existe;

	}

	public int nombrePaquete(String nombre, String idPaquete){

		boolean termine = false;
		int indiceNombre=0;
		int indiceRespuesta=-1;


		for(int i = 0; i < nombres.size() && !termine; i++){

			if(nombres.get(i).equals(nombre)){
				indiceNombre = i;
				termine = true;
				
			}
			
		}

		if(idsPkts.get(indiceNombre).equals(idPaquete)){
			indiceRespuesta = indiceNombre;
		}



		return indiceRespuesta;

	}

	public ArrayList<Integer> darTablaEstados(){
		return estadosPkts;
	}
}
