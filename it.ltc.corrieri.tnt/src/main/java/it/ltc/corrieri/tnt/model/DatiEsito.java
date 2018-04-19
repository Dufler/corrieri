package it.ltc.corrieri.tnt.model;

import java.util.Date;

public class DatiEsito extends DatiTNT {
	
	public static final char TIPO = '2';
	public static final int LUNGHEZZA_MINIMA = 279;
	
	private final Date dataEstrazione;
	private final String letteraDiVettura;
	private final String codiceCliente;
	private final Date dataVariazione;
	private final Date dataConsegna;
	private final String stato;
	private final String riferimentoMittente;
	private final String noteEvento;
	private final Date dataIncasso;
	private final Date dataGiacenza;
	private final String numeroSpedizioneTNT;
	private final String primaSpedizioneSvincoloGiacenza;
	private final String secondaSpedizioneSvincoloGiacenza;
	private final String destinatarioSvincoloGiacenza;
	private final String nomeRicevente;
	private final double peso;
	private final double volume;
	private final String numeroGiacenza;
	private final Date dataAppuntamento;
	private final String nominativoAppuntamento;
	private final String filialeTNT;
	
	public DatiEsito(String s) {
		super(s, LUNGHEZZA_MINIMA);
		dataEstrazione = getDataSoloGiorno(line, 1, 9);
		letteraDiVettura = line.substring(9, 19).trim();
		codiceCliente = line.substring(19, 30).trim();
		dataVariazione = getDataEOra(line, 30, 38, 38, 44);
		dataConsegna = getDataEOra(line, 44, 52, 208, 214);
		stato = line.substring(52, 54).trim();
		riferimentoMittente = line.substring(54, 64).trim();
		noteEvento = line.substring(64, 144).trim();
		dataIncasso = getDataSoloGiorno(line, 144, 152);
		dataGiacenza = getDataSoloGiorno(line, 152, 160);
		numeroSpedizioneTNT = line.substring(160, 169).trim();
		primaSpedizioneSvincoloGiacenza = line.substring(169, 178).trim();
		secondaSpedizioneSvincoloGiacenza = line.substring(178, 187).trim();
		destinatarioSvincoloGiacenza = line.substring(187, 188).trim();
		nomeRicevente = line.substring(188, 208).trim();
		peso = getDecimale(line, 214, 221, 3);
		volume = getDecimale(line, 221, 228, 3);
		numeroGiacenza = line.substring(228, 235).trim();
		dataAppuntamento = getDataEOra(line, 237, 245, 245, 251);
		nominativoAppuntamento = line.substring(253, 273).trim();
		filialeTNT = line.substring(273, 278).trim();
	}
	
	@Override
	public String toString() {
		return "LDV: " + letteraDiVettura + ", data: " + sdf.format(dataVariazione) + ", stato : '" + stato + "'";
	}

	public Date getDataEstrazione() {
		return dataEstrazione;
	}

	public String getLetteraDiVettura() {
		return letteraDiVettura;
	}

	public String getCodiceCliente() {
		return codiceCliente;
	}

	public Date getDataVariazione() {
		return dataVariazione;
	}

	public Date getDataConsegna() {
		return dataConsegna;
	}

	public String getStato() {
		return stato;
	}

	public String getRiferimentoMittente() {
		return riferimentoMittente;
	}

	public String getNoteEvento() {
		return noteEvento;
	}

	public Date getDataIncasso() {
		return dataIncasso;
	}

	public Date getDataGiacenza() {
		return dataGiacenza;
	}

	public String getNumeroSpedizioneTNT() {
		return numeroSpedizioneTNT;
	}

	public String getPrimaSpedizioneSvincoloGiacenza() {
		return primaSpedizioneSvincoloGiacenza;
	}

	public String getSecondaSpedizioneSvincoloGiacenza() {
		return secondaSpedizioneSvincoloGiacenza;
	}

	public String getDestinatarioSvincoloGiacenza() {
		return destinatarioSvincoloGiacenza;
	}

	public String getNomeRicevente() {
		return nomeRicevente;
	}

	public double getPeso() {
		return peso;
	}

	public double getVolume() {
		return volume;
	}

	public String getNumeroGiacenza() {
		return numeroGiacenza;
	}

	public Date getDataAppuntamento() {
		return dataAppuntamento;
	}

	public String getNominativoAppuntamento() {
		return nominativoAppuntamento;
	}

	public String getFilialeTNT() {
		return filialeTNT;
	}

}
