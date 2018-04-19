package it.ltc.bartolini.model;

import java.util.Date;

public class RigaCampiFissiBRT extends RigaBRT {

	protected final String riga;
	
	protected RigaCampiFissiBRT(String riga) {
		this.riga = riga;
	}
	
	protected String parseString(int start, int end) {
		String s = "";
		if (riga != null && riga.length() > end) {
			s = riga.substring(start, end).trim();
		}
		return s;
	}
	
	protected Integer parseInteger(int start, int end) {
		Integer i = parseInteger(parseString(start, end));
		return i;
	}
	
	protected Double parseDouble(int start, int end) {
		String value = parseString(start, end);
		value = value.replace(',', '.');
		Double d = parseDouble(value);
		return d;
	}
	
	protected Date parseDate(int start, int end) {
		Date d = parseDate(parseString(start, end));
		return d;
	}
	
}
