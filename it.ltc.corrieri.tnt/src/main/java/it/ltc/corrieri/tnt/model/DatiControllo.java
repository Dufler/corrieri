package it.ltc.corrieri.tnt.model;

public class DatiControllo extends DatiTNT {
	
	public static final char TIPO = '9';
	public static final int LUNGHEZZA_MINIMA = 34;
	
	private final int numeroRecordPartenze;
	private final int numeroRecordAggiornamento;
	private final int numeroRecordTotali;
	
	public DatiControllo(String s) {
		super(s, LUNGHEZZA_MINIMA);
		numeroRecordPartenze = getIntero(line, 2, 12);
		numeroRecordAggiornamento = getIntero(line, 13, 23);
		numeroRecordTotali = getIntero(line, 24, 33);
	}

	public int getNumeroRecordPartenze() {
		return numeroRecordPartenze;
	}

	public int getNumeroRecordAggiornamento() {
		return numeroRecordAggiornamento;
	}

	public int getNumeroRecordTotali() {
		return numeroRecordTotali;
	}

}
