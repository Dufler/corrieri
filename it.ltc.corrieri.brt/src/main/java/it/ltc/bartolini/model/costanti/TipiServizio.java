package it.ltc.bartolini.model.costanti;

/**
 * Codifica sui tipi di servizio.
 * @author Damiano
 *
 */
public enum TipiServizio {					
	
	D("DISTRIBUZIONE", "DEF"),				
	C("EXPRESS", "DEF"),			
	E("PRIORITY", "O12"),
	H("Ore 10:30", "O10");

    private final String descrizione;
    private final String codificaInterna;
	
	private TipiServizio(String descrizione, String codificaInterna) {
		this.descrizione = descrizione;
		this.codificaInterna = codificaInterna;
	}

	public String getDescrizione() {
		return descrizione;
	}

	public String getCodificaInterna() {
		return codificaInterna;
	}

}
