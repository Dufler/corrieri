package it.ltc.corrieri.tnt.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public abstract class DatiTNT {
	
	protected static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
	protected static SimpleDateFormat sdfSoloGiorno = new SimpleDateFormat("yyyyMMdd");
	
	protected final String line;
	
	public DatiTNT(String l, int lunghezzaMinima) {
		line = checkLunghezza(l, lunghezzaMinima);
	}
	
	protected String checkLunghezza(String s, int l) {
		while (s.length() < l) {
			s = s + " ";
		}
		return s;
	}
	
	protected int getIntero(String line, int s, int e) {
		int i;
		try {
			i = Integer.parseInt(line.substring(s, e));
		} catch (NumberFormatException exception) {
			i = 0;
		}
		return i;
	}
	
	/**
	 * Restituisce un double parsando la string
	 * @param line
	 * @param s il carattere iniziale
	 * @param e il carattere finale
	 * @param dp il numero di decimali
	 * @return il double parsato
	 */
	protected double getDecimale(String line, int s, int e, int dp) {
		double d;
		try {
			d = Double.parseDouble(line.substring(s, e));
			d = d / Math.pow(10, dp);
		} catch (NumberFormatException exception) {
			d = 0;
		}
		return d;
	}

	protected Date getDataSoloGiorno(String line, int s, int e) {
		Date data;
		try {
			sdfSoloGiorno.setTimeZone(TimeZone.getTimeZone("UTC"));
			data = sdfSoloGiorno.parse(line.substring(s, e));
		} catch (ParseException exception) {
			data = null;
		}
		return data;
	}
	
	protected Date getDataEOra(String line, int sd, int ed, int sh, int eh) {
		Date dataeora;
		try {
			String data = line.substring(sd, ed);
			String ora = line.substring(sh, eh).trim();
			while (ora.length() < eh - sh)
				ora = ora + "0";
			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
			dataeora = sdf.parse(data + ora);
		} catch (ParseException exception) {
			dataeora = null;
		}
		return dataeora;
	}

}
