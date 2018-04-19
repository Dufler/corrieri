package it.ltc.corrieri.tnt.model;

import java.util.Date;

public class DatiSpedizione extends DatiTNT {
	
	public static final char TIPO = '1';
	public static final int LUNGHEZZA_MINIMA = 646;
	
	private final Date dataEstrazione;
	private final String statoAvanzamento;
	private final Date dataPartenzaSpedizione;
	private final String letteraDiVettura;
	private final String tipoDocumento;
	private final String filialeDiPartenza;
	private final String filialeDiArrivo;
	private final String ragioneSocialeDestinatario;
	private final String indirizzoDestinatario;
	private final String localitaDestinatario;
	private final String capDestinatario;
	private final String provinciaDestinatario;
	private final String ragioneSocialeMittente;
	private final String indirizzoMittente;
	private final String localitaMittente;
	private final String capMittente;
	private final String provinciaMittente;
	private final int numeroColli;
	private final double pesoDichiarato;
	private final double peso;
	private final double volume;
	private final String tipoMerceCollo;
	private final String tipoMerceBauletto;
	private final Date dataConsegna;
	private final String codiceCliente;
	private final String tipoServizio;
	private final String flagRFR;
	private final String valutaContrassegno;
	private final double valoreContrassegno;
	private final String letteraDiVetturaAssegnato;
	private final Date dataIncassoContrassegno;
	private final Date dataGiacenza;
	private final String riferimentoMittente;
	private final String riferimentoDivisioneFattura;
	private final String riferimentoAllegatoFattura;
	private final String riferimentoUnificazioneFattura;
	private final String riferimentoNumeroOfferta;
	private final String note;
	private final String fermoDeposito;
	private final String numeroSpedizioneTNT;
	private final String oraCreazioneSpedizione; //hhmmss
	private final String flagPaperless; //D o P
	private final String tipoSvincolo; //M o D
	private final String letteraDiVetturaOriginaria;
	private final String numeroPresaUnivoca;
	private final Date dataPresaRichiesta;
	private final Date dataPresaEffettuata;
	private final String flagReso; //S o N
	
	public DatiSpedizione(String s) {
		super(s, LUNGHEZZA_MINIMA);
		dataEstrazione = getDataSoloGiorno(line, 1, 9);
		statoAvanzamento = line.substring(9, 11).trim();
		dataPartenzaSpedizione = getDataSoloGiorno(line, 11, 19);
		letteraDiVettura = line.substring(19, 29).trim();
		tipoDocumento = line.substring(29, 30).trim();
		filialeDiPartenza = line.substring(30, 35).trim();
		filialeDiArrivo = line.substring(35, 40).trim();
		ragioneSocialeDestinatario = line.substring(40, 80).trim();
		indirizzoDestinatario = ripulisciIndirizzo(line.substring(80, 115).trim());
		localitaDestinatario = line.substring(115, 155).trim();
		capDestinatario = line.substring(155, 160).trim();
		provinciaDestinatario = line.substring(160, 162).trim();
		ragioneSocialeMittente = line.substring(162, 202).trim();
		indirizzoMittente = ripulisciIndirizzo(line.substring(202, 237).trim());
		localitaMittente = line.substring(237, 277).trim();
		capMittente = line.substring(277, 282).trim();
		provinciaMittente = line.substring(282, 284).trim();
		numeroColli = getIntero(line, 284, 289);
		pesoDichiarato = getDecimale(line, 289, 296, 3);
		peso = getDecimale(line, 296, 303, 3);
		volume = getDecimale(line, 303, 310, 3);
		tipoMerceCollo = line.substring(310, 311).trim();
		tipoMerceBauletto = line.substring(311, 312).trim();
		dataConsegna = getDataSoloGiorno(line, 312, 320);
		codiceCliente = line.substring(320, 331).trim();
		tipoServizio = line.substring(331, 332).trim();
		flagRFR = line.substring(332, 333).trim();
		valutaContrassegno = line.substring(333, 336).trim();
		valoreContrassegno = getDecimale(line, 336, 347, 2);
		letteraDiVetturaAssegnato = line.substring(347, 357).trim();
		dataIncassoContrassegno = getDataSoloGiorno(line, 357, 365);
		dataGiacenza = getDataSoloGiorno(line, 365, 373);
		riferimentoMittente = line.substring(373, 383).trim();
		riferimentoDivisioneFattura = line.substring(383, 388).trim();
		riferimentoAllegatoFattura = line.substring(388, 403).trim();
		riferimentoUnificazioneFattura = line.substring(403, 411).trim();
		riferimentoNumeroOfferta = line.substring(411, 418).trim();
		note = line.substring(418, 578).trim();
		fermoDeposito = line.substring(578, 579).trim();
		numeroSpedizioneTNT = line.substring(579, 588).trim();
		oraCreazioneSpedizione = line.substring(588, 594).trim();
		flagPaperless = line.substring(594, 595).trim();
		tipoSvincolo = line.substring(595, 596).trim();
		letteraDiVetturaOriginaria = line.substring(596, 606).trim();
		numeroPresaUnivoca = line.substring(606, 615).trim();
		dataPresaRichiesta = getDataSoloGiorno(line, 615, 623);
		dataPresaEffettuata = getDataEOra(line, 623, 631, 631, 637);
		flagReso = line.substring(645, 646).trim();
	}
	
	private String ripulisciIndirizzo(String indirizzo) {
		if (!indirizzo.isEmpty()) {
			if (indirizzo.charAt(0) == '.')
				indirizzo = indirizzo.substring(1);
			if (indirizzo.charAt(indirizzo.length() - 1) == '.')
				indirizzo = indirizzo.substring(0, indirizzo.length() - 1);
			indirizzo = indirizzo.trim();
		}
		return indirizzo;
	}
	
	@Override
	public String toString() {
		return "LDV: " + letteraDiVettura + ", data: " + sdfSoloGiorno.format(dataPartenzaSpedizione);
	}

	public Date getDataEstrazione() {
		return dataEstrazione;
	}

	public String getStatoAvanzamento() {
		return statoAvanzamento;
	}

	public Date getDataPartenzaSpedizione() {
		return dataPartenzaSpedizione;
	}

	public String getLetteraDiVettura() {
		return letteraDiVettura;
	}

	public String getTipoDocumento() {
		return tipoDocumento;
	}

	public String getFilialeDiPartenza() {
		return filialeDiPartenza;
	}

	public String getFilialeDiArrivo() {
		return filialeDiArrivo;
	}

	public String getRagioneSocialeDestinatario() {
		return ragioneSocialeDestinatario;
	}

	public String getIndirizzoDestinatario() {
		return indirizzoDestinatario;
	}

	public String getLocalitaDestinatario() {
		return localitaDestinatario;
	}

	public String getCapDestinatario() {
		return capDestinatario;
	}

	public String getProvinciaDestinatario() {
		return provinciaDestinatario;
	}

	public String getRagioneSocialeMittente() {
		return ragioneSocialeMittente;
	}

	public String getIndirizzoMittente() {
		return indirizzoMittente;
	}

	public String getLocalitaMittente() {
		return localitaMittente;
	}

	public String getCapMittente() {
		return capMittente;
	}

	public String getProvinciaMittente() {
		return provinciaMittente;
	}

	public int getNumeroColli() {
		return numeroColli;
	}

	public double getPesoDichiarato() {
		return pesoDichiarato;
	}

	public double getPeso() {
		return peso;
	}

	public double getVolume() {
		return volume;
	}

	public String getTipoMerceCollo() {
		return tipoMerceCollo;
	}

	public String getTipoMerceBauletto() {
		return tipoMerceBauletto;
	}

	public Date getDataConsegna() {
		return dataConsegna;
	}

	public String getCodiceCliente() {
		return codiceCliente;
	}

	public String getTipoServizio() {
		return tipoServizio;
	}

	public String getFlagRFR() {
		return flagRFR;
	}

	public String getValutaContrassegno() {
		return valutaContrassegno;
	}

	public double getValoreContrassegno() {
		return valoreContrassegno;
	}

	public String getLetteraDiVetturaAssegnato() {
		return letteraDiVetturaAssegnato;
	}

	public Date getDataIncassoContrassegno() {
		return dataIncassoContrassegno;
	}

	public Date getDataGiacenza() {
		return dataGiacenza;
	}

	public String getRiferimentoMittente() {
		return riferimentoMittente;
	}

	public String getRiferimentoDivisioneFattura() {
		return riferimentoDivisioneFattura;
	}

	public String getRiferimentoAllegatoFattura() {
		return riferimentoAllegatoFattura;
	}

	public String getRiferimentoUnificazioneFattura() {
		return riferimentoUnificazioneFattura;
	}

	public String getRiferimentoNumeroOfferta() {
		return riferimentoNumeroOfferta;
	}

	public String getNote() {
		return note;
	}

	public String getFermoDeposito() {
		return fermoDeposito;
	}

	public String getNumeroSpedizioneTNT() {
		return numeroSpedizioneTNT;
	}

	public String getOraCreazioneSpedizione() {
		return oraCreazioneSpedizione;
	}

	public String getFlagPaperless() {
		return flagPaperless;
	}

	public String getTipoSvincolo() {
		return tipoSvincolo;
	}

	public String getLetteraDiVetturaOriginaria() {
		return letteraDiVetturaOriginaria;
	}

	public String getNumeroPresaUnivoca() {
		return numeroPresaUnivoca;
	}

	public Date getDataPresaRichiesta() {
		return dataPresaRichiesta;
	}

	public Date getDataPresaEffettuata() {
		return dataPresaEffettuata;
	}

	public String getFlagReso() {
		return flagReso;
	}

}
