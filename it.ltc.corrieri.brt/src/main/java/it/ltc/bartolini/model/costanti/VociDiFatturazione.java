package it.ltc.bartolini.model.costanti;

/**
 * Elenco delle voci di fatturazione
 * @author Damiano
 *
 */
public enum VociDiFatturazione {
	
	A("Appuntamento"),
	B("Consegna DDT"),
	C("Facchinaggio arrivo"),
	D("Spese incasso"),
	E("Riv. ass.ne resp. vettore"),
	F("Fuori misure aziendali"),
	G("Competenze assegno"),
	H("Diritto fisso"),
	I("Spese giacenza"),
	J("Isola"),
	K("Localita' disagiata"),
	L("Istat"),
	M("Interessi di mora"),
	N("Anteporto"),
	P("Consegne ai piani"),
	Q("Centri storici"),
	R("Riv. ass.ne danni merce"),
	S("Supermercati"),
	T("Anticipata"),
	U("Ritiro"),
	Z("Addizionale gestione"),
	_2("Inoltro"),
	_6("Bollo"),
	_7("T.C.P."),
	_8("IVA");

	private final String descrizione;
	
	private VociDiFatturazione(String descrizione) {
		this.descrizione = descrizione;
	}

	public String getDescrizione() {
		return descrizione;
	}
	
	@Override
	public String toString() {
		return descrizione;
	}
	
}
