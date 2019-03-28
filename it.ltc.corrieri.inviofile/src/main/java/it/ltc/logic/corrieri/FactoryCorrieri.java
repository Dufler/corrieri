package it.ltc.logic.corrieri;

import it.ltc.database.model.legacy.sede.CorrieriPerCliente;

public class FactoryCorrieri {
	
	public static final String TNT = "TNT";
	public static final String BARTOLINI = "BRT";
	public static final String BARTOLINI_ESTERO_MONO = "BRT_E_MONO";
	public static final String BARTOLINI_ESTERO_MULTI = "BRT_E_MULTI";
	public static final String GLS_TESTO = "GLS_TESTO";
	public static final String GLS_EXCEL = "GLS_EXCEL";
	
	public static Corriere getInstance(CorrieriPerCliente cliente) {
		String nome = cliente.getCorriere();
		Corriere instanza;
		switch(nome) {
			case TNT : {instanza = new InvioFileTesto(cliente); break;}
			case BARTOLINI : {instanza = new InvioFileTesto(cliente); break;}
			case BARTOLINI_ESTERO_MONO : {instanza = new BartoliniEsteroMono(cliente); break;}
			case BARTOLINI_ESTERO_MULTI : {instanza = new BartoliniEsteroMulti(cliente); break;}
			case GLS_TESTO : {instanza = new InvioFileTesto(cliente); break;}
			case GLS_EXCEL : {instanza = new InvioExcel(cliente); break;}
			default : throw new IllegalArgumentException("Indicare un corriere valido per " + cliente.getCliente() + ". E' stato specificato " + nome);
		}
		return instanza;
	}

}
