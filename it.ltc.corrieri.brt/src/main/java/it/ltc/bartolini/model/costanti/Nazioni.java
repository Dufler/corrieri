package it.ltc.bartolini.model.costanti;

/**
 * Codifica ad hoc Bartolini per le nazioni.
 * @author Damiano
 *
 */
public enum Nazioni {
	
	ITA("Italia", "ITA", true),
	A("Austria", "AUT", true),
	AND("Andorra", "AND", false),
	B("Belgio", "BEL", true),
	BG("Bulgaria", "BGR", true),
	CH("Svizzera", "CHE", false),
	CZ("Repubblica Ceca", "CZE", true),
	D("Germania", "DEU", true),
	DK("Danimarca", "DNK", true),
	E("Spagna", "ESP", true),
	F("Francia", "FRA", true),
	GB("Gran Bretagna", "GBR", false),	
	GBZ("Gibilterra", "GBZ", false),
	GR("Grecia", "GRC", true),
	HR("Croazia", "HRV", false),
	HU("Ungheria", "HUN", true),      
	IRL("Irlanda", "IRL", true),
	L("Lussemburgo", "LUX", true),
	LI("Liechtenstein", "LIE", false),   				
	MC("Principato Monaco", "MCO", true),
	N("Norvegia", "NOR", false),
	NL("Olanda", "NLD", true),
	P("Portogallo", "PRT", true),
	PL("Polonia", "POL", true),
	RO("Romania", "ROU", true),
	S("Svezia", "SWE", true),
	SF("Finlandia", "FIN", true),
    SK("Slovacchia", "SVK", true),
    SLO("Slovenia", "SVN", true),
    SM("San Marino", "SMR", true);	

	private final String nome;
	private final String codiceISO;
	private final boolean ue;
	
	private Nazioni(String nome, String codiceISO, boolean ue) {
		this.nome = nome;
		this.codiceISO = codiceISO;
		this.ue = ue;
	}
	
	public String getNome() {
		return nome;
	}

	public String getCodiceISO() {
		return codiceISO;
	}
	
	public boolean isUE() {
		return ue;
	}

}
