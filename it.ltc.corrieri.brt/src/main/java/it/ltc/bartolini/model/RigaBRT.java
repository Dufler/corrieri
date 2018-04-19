package it.ltc.bartolini.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class RigaBRT {
	
	public static final String DEFAULT_DATE_FORMAT = "yyyyMMdd";
	
	protected final SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
	
	protected Integer parseInteger(String value) {
		Integer i = null;
		if (value != null) {
			value = value.trim();
			if (!value.isEmpty())
			try {
				i = Integer.parseInt(value);
			} catch (NumberFormatException e) {}
		}
		return i;
	}
	
	protected Double parseDouble(String value) {
		Double d = null;
		if (value != null) {
			value = value.trim();
			if (!value.isEmpty())
				try {
					d = Double.parseDouble(value);
				} catch (NumberFormatException e) {}
		}
		return d;
	}
	
	protected Date parseDate(String value) {
		return parseDate(value, DEFAULT_DATE_FORMAT);
	}
	
	protected Date parseDate(String value, String pattern) {
		Date d = null;
		if (value != null && pattern != null && value.length() == pattern.length()) {
			try {
				sdf.applyPattern(pattern);
				d = sdf.parse(value);
			} catch (ParseException e) {}
		}
		return d;
	}

}
