package it.ltc.bartolini.model.costanti;

/**
 * Codifica dei turni di chiusura
 * 1° byte = numero giorno della settimana (1 Lunedì, 2 Martedì, ..., 7 Domenica (non indicare), blank tutti i giorni) 
 * 2° byte = periodo del giorno (M Mattina, P Pomeriggio, blank tutto il giorno)
 * @author Damiano
 *
 */
public enum TurniDiChiusura {
	
	P("Solo pomeriggio"),
	M("Solo mattina"),
	_1("Lunedi"),
	_2("Martedi"),
    _3("Mercoledi"),
    _4("Giovedi"),
    _5("Venerdi"),
    _6("Sabato"),
    _7("Domenica (non indicare)");

	private final String descrizione;
	
	private TurniDiChiusura(String descrizione) {
		this.descrizione = descrizione;
	}

	public String getDescrizione() {
		return descrizione;
	}
}
