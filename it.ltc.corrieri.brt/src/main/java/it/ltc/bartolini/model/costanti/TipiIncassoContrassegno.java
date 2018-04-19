package it.ltc.bartolini.model.costanti;

/**
 * Codifica per i tipi d'incasso contrassegno.
 * @author Damiano
 *
 */
public enum TipiIncassoContrassegno {

	B("ACCETTA ASS.C/C CORRIERE"), 
	BA("ACCETTARE ASS.C/C MITT."),  
	BB("ACCETTA ASS.INT CORRIERE"), 
	BC("ASS.C/C CORRIERE/MITTENTE"),
	BM("ACCETTARE ASS. C/C.MITT."), 
	C("ACCETTA ASS.CIRC.CORRIERE"),
	CA("CAMBIALE INTESTATA MITT."), 
	CM("ACCETTARE ASS.CIRC.MITT."), 
	OC("ASS.CIRC.MITT.ORIGINALE"),  
	OM("ACC.ASS.MITT.ORIG.(V.DDT)"),
	SC("SOLO CONTANTI"),            
	S2("SOLO CONTANTI"),              
	TM("ACC.TIT.MITT.SCAD.DEST."),  
	TO("ASS.DATATO MITT.ORIGINALE");

	private final String descrizione;
	
	private TipiIncassoContrassegno(String descrizione) {
		this.descrizione = descrizione;
	}

	public String getDescrizione() {
		return descrizione;
	}
}
