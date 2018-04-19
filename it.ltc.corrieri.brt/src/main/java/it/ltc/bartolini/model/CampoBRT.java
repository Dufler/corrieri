package it.ltc.bartolini.model;

/**
 * Attualmente non è utilizzata.
 * @author Damiano
 *
 */
public class CampoBRT {
	
	public enum Tipo {
		
		STRINGA,
		INTERO,
		DECIMALE;
		
	}
	
	private final int lunghezza;
	private final Tipo tipo;
	
	public CampoBRT(String valore, int lunghezza, Tipo tipo) {
		//Do something with value
		this.lunghezza = lunghezza;
		this.tipo = tipo;
	}

	public int getLunghezza() {
		return lunghezza;
	}

	public Tipo getTipo() {
		return tipo;
	}

}
