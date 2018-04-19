package it.ltc.corrieri.tnt.model;

import java.util.Date;

public class DatiFile extends DatiTNT {
	
	public static final char TIPO = '0';
	
	public static final int LUNGHEZZA_MINIMA = 22;
	
	//private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss"); 
	
	private final String codiceFile;
	private final Date dataEstrazione;
	/**
	 * Il numero del giorno in un anno giuliano.
	 * es. 1° Gennaio = 1, 31 Dicembre = 365 (366 se bisestile)
	 */
	private final int numeroGiornoEstratto;
	
	public DatiFile(String s) {
		super(s, LUNGHEZZA_MINIMA);
		codiceFile = line.substring(1, 5);
		dataEstrazione = getDataEOra(line, 5, 13, 16, 22); //getDataEstrazione(line);
		numeroGiornoEstratto = getNumeroGiornoEstratto(line);
	}

//	private Date getDataEstrazione(String line) {
//		Date dataParsata;
//		try {
//			String data = line.substring(5, 13);
//			String ora = line.substring(16, 22);
//			dataParsata = sdf.parse(data + ora);
//		} catch (ParseException e) {
//			dataParsata = null;
//		}
//		return dataParsata;
//	}
	
	private int getNumeroGiornoEstratto(String line) {
		int numeroGiorni;
		try {
			numeroGiorni = Integer.parseInt(line.substring(13, 16));
		} catch (NumberFormatException e) {
			numeroGiorni = -1;
		}
		return numeroGiorni;
	}

	public String getCodiceFile() {
		return codiceFile;
	}

	public Date getDataEstrazione() {
		return dataEstrazione;
	}

	public int getNumeroGiornoEstratto() {
		return numeroGiornoEstratto;
	}

}
