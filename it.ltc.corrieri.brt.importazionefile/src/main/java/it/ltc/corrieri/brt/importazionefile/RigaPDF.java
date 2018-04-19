package it.ltc.corrieri.brt.importazionefile;

import java.util.Date;

public class RigaPDF {
	
	private final String ldv;
	private final Date dataPartenza;
	private final String riferimento;
	private final String provincia;
	private final String ragioneSociale;
	private final String cap;
	private final int colli;
	private final double volume;
	private final double peso;
	private final double costo;
	
	public RigaPDF(String ldv, Date dataPartenza, String riferimento,
			String provincia, String ragioneSociale, String cap, int colli, double volume, double peso, double costo) {
		this.ldv = ldv;
		this.dataPartenza = dataPartenza;
		this.riferimento = riferimento;
		this.provincia = provincia;
		this.ragioneSociale = ragioneSociale;
		this.cap = cap;
		this.colli = colli;
		this.volume = volume;
		this.peso = peso;
		this.costo = costo;
	}

	public String getLdv() {
		return ldv;
	}

	public Date getDataPartenza() {
		return dataPartenza;
	}

	public String getRiferimento() {
		return riferimento;
	}

	public String getProvincia() {
		return provincia;
	}

	public String getRagioneSociale() {
		return ragioneSociale;
	}

	public String getCap() {
		return cap;
	}

	public int getColli() {
		return colli;
	}

	public double getVolume() {
		return volume;
	}

	public double getPeso() {
		return peso;
	}

	public double getCosto() {
		return costo;
	}

}
