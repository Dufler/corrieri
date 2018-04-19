package it.ltc.bartolini.model.costanti;

/**
 * Codifica dei codici bolla.
 * @author Damiano
 *
 */
public enum CodiciBolla {					
	
	$("C/S X DIROTTAMENTO"),     
	£("C/S X COMUNIC.GIACENZE"),   
	A("C/S     FRANCO"),   
	FW("ASSEGNATO RECAPITO C/ASS"),
	H("ST. FRANCO + C/A"),
	I("ST. FRANCO + C/A"),
	J("C/S FRANCO + C/A LEGATO"),
	K("C/S FRANCO + C/A"),         
	L("C/S FRANCO + C/A"),         
	M("PREPAGATO ANNULLATO"),      
	P("ST. ASSEGN.+ C/A"),     
	Q("ST. FRANCO"),      
	X("C/S FRANCO LEGATO"),      
	Y("FRANCO +C/A ST.PROV.DEST."),
	Z("STORNO ASSEGNATO"),         
	_1("FRANCO"),                   
	_2("ASSEGNATO"),                
	_2R("ASSEGNATO RCC"),            
	_4("FRANCO    +C/ASSEGNO"),     
	_5("FRANCO    +C/ASSEGNO"),
	_6("ASSEGNATO +C/ASSEGNO");

	private final String descrizione;
	
	private CodiciBolla(String descrizione) {
		this.descrizione = descrizione;
	}

	public String getDescrizione() {
		return descrizione;
	}
	
}
