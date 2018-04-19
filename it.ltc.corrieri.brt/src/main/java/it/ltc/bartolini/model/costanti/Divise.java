package it.ltc.bartolini.model.costanti;

/**
 * Codifica per gli importi in denaro.
 * @author Damiano
 *
 */
public enum Divise {
	
	ATS("Scellino Austriaco"),
	BEF("Franco Belga"),
	CAD("Dollaro Canadese"),
	CHF("Franco Svizzero"),
	DEM("Marco Tedesco"),
	DKK("Corona Danese"),
	ESP("Pesetas spagnola"),
	EUR("Euro"),
	FIM("Marco Finlandese"),
	FRF("Franco Francese"),
	GBP("Sterlina Inglese"),
	GRD("Dracma Greca"),
	IEP("Sterlina Irlandese"),
	ITL("Lira Italiana"),
	JPY("Yen Giapponese"),
	NLG("Fiorino Olandese"),
	NOK("Corona Norvegese"),
	PTE("Escudo Portoghese"),
	SEK("Corona Svedese"),
	USD("Dollaro USA"),
	XEU("ECU");
	
	private final String descrizione;
	
	private Divise(String descrizione) {
		this.descrizione = descrizione;
	}

	public String getDescrizione() {
		return descrizione;
	}
}