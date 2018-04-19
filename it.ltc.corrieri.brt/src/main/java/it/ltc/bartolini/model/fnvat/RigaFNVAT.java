package it.ltc.bartolini.model.fnvat;

public class RigaFNVAT {
	
	/**
	 * VATATB - flag di comodo, usualmente blank.
	 * Lunghezza campo: 1
	 */
	private final String flagAnnullamento;
	
	/**
	 * VATCCM - codice del cliente su cui verrà addebitata la spedizione.
	 * Lunghezza campo: 7
	 */
	private final String codiceClienteMittente;
	
	/**
	 * VATLNP - codice della filiale che effettua il ritiro.
	 * Lunghezza campo: 3
	 */
	private final String filialePartenza;
	
	/**
	 * VATAAS - anno nel formato yyyy
	 * Lunghezza campo: 4
	 */
	private final String annoSpedizione;
	
	/**
	 * VATNRS - numero assegnato ai clienti che segnacollano la merce.
	 * Usualmente 00
	 * Lunghezza campo: 2
	 */
	private final String numeroSerie;
	
	/**
	 * VATNSP - numero progressivo assegnato dal corriere.
	 * Lunghezza campo: 7
	 */
	private final String numeroSpedizione;
	
	/**
	 * VATTRC - comunicato dal corrirere, fisso ad E.
	 * Lunghezza campo: 1
	 */
	private final String tipoRecord;
	
	/**
	 * VATNOT - barcode che identifica in maniera univoca il collo.
	 * Lunghezza campo: 35
	 */
	private final String barcodeUnivoCollo;
	
	public RigaFNVAT(String riga) {
		flagAnnullamento = riga.substring(0, 1);
		codiceClienteMittente = riga.substring(2, 8);
		filialePartenza = riga.substring(8, 11);
		annoSpedizione = riga.substring(11, 15);
		numeroSerie = riga.substring(15, 17);
		numeroSpedizione = riga.substring(17, 24);
		tipoRecord = riga.substring(24, 25);
		barcodeUnivoCollo = riga.substring(25, 60);
	}

	public String getFlagAnnullamento() {
		return flagAnnullamento;
	}

	public String getCodiceClienteMittente() {
		return codiceClienteMittente;
	}

	public String getFilialePartenza() {
		return filialePartenza;
	}

	public String getAnnoSpedizione() {
		return annoSpedizione;
	}

	public String getNumeroSerie() {
		return numeroSerie;
	}

	public String getNumeroSpedizione() {
		return numeroSpedizione;
	}

	public String getTipoRecord() {
		return tipoRecord;
	}

	public String getBarcodeUnivoCollo() {
		return barcodeUnivoCollo;
	}

}
