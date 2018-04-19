package it.ltc.bartolini.model.fnvab;

import it.ltc.bartolini.model.RigaCampiFissiBRT;

/**
 * Tracciato standard BRT per le informazioni sulle spedizioni, viene usato sia per l'invio che per la ricezione dei dati.
 * Va in coppia al tracciato FNVAT.
 * @author Damiano
 *
 */
public class RigaFNVAB extends RigaCampiFissiBRT {
	
	/**
	 * Normalmente blank, l'utilizzo va concordato. 
	 */
	private final String flagAnnullamento;
	private final String codiceClienteMittente;
	private final String filialePartenza;
	private final String annoSpedizione;
	private final String meseGiornoSpedizione;
	private final String numeroSerie;
	private final String numeroSpedizione;
	private final String codiceBolla;
	private final String filialeArrivo;
	private final String ragioneSocialeDestinatario;
	private final String estensioneRagioneSocialeDestinatario;
	private final String indirizzoDestinatario;
	private final String capDestinatario;
	private final String localitaDestinatario;
	private final String provinciaDestinatario;
	private final String nazioneDestinatario;
	private final String primoGiornoChiusuraDestinario;
	private final String secondoGiornoChiusuraDestinatario;
	private final String codiceTariffa;
	private final String tipoServizioBolle;
	private final Double importoDaAssicurare;
	private final String divisaImportoDaAssicurare;
	private final String naturaMerceMittente;
	private final Integer numeroColli;
	private final Double peso;
	private final Double volume;
	private final Double quantitaDaFatturare;
	private final Double importoContrassegno;
	private final String divisaImportoContrassegno;
	private final String tipoIncassoContrassegno;
	private final String particolaritaContrassegno;
	private final String riferimentoMittenteNumerico;
	private final String riferimentoMittenteAlfabetico;
	private final Integer numeroSegnacolloDal;
	private final Integer numeroSegnacolloAl;
	private final String cumulativoColli;
	private final String note;
	private final String noteAggiuntive;
	private final String zonaConsegna;
	private final String codiceTrattamentoMerce;
	private final String fermoDeposito;
	private final String dataConsegnaRichiesta;
	private final String oraConsegnaRichiesta;
	private final String tipoConsegnaRichiesta;
	private final String codiceTassazione;
	private final String flagTassazione;
	private final Double valoreMerceDichiarato;
	private final String divisaValoreMerceDichiarato;
	private final String gestioneParticolaritaConsegna;
	private final String gestioneParticolaritaGiacenza;
	private final String gestioneParticolaritaVarie;
	private final String primaConsegnaParticolare;
	private final String secondaConsegnaParticolare;
	private final String codiceSociale;
	private final String anticipata;
	private final String ragioneSocialeMittenteOriginale;
	private final String capMittenteOriginale;
	private final String nazioneMittenteOriginale;
	
	public RigaFNVAB(String riga) {
		super(riga);
		flagAnnullamento = parseString(0, 1);
		codiceClienteMittente = parseString(2, 9);
		filialePartenza = parseString(10, 13);
		annoSpedizione = parseString(14, 18);
		meseGiornoSpedizione = parseString(19, 23);
		numeroSerie = parseString(24, 26);
		numeroSpedizione = parseString(27, 34);
		codiceBolla = parseString(35, 37);
		filialeArrivo = parseString(37, 40);
		ragioneSocialeDestinatario = parseString(40, 75);
		estensioneRagioneSocialeDestinatario = parseString(75, 110);
		indirizzoDestinatario = parseString(110, 145);
		capDestinatario = parseString(145, 150);
		localitaDestinatario = parseString(154, 189);
		provinciaDestinatario = parseString(189, 192);
		nazioneDestinatario = parseString(192, 195);
		primoGiornoChiusuraDestinario = parseString(195, 197);
		secondoGiornoChiusuraDestinatario = parseString(197, 199);
		codiceTariffa = parseString(199, 202);
		tipoServizioBolle = parseString(202, 203);
		importoDaAssicurare = parseDouble(204, 218);
		divisaImportoDaAssicurare = parseString(218, 221);
		naturaMerceMittente = parseString(221, 237);
		numeroColli = parseInteger(237, 242);
		peso = parseDouble(243, 251);
		volume = parseDouble(252, 258);
		quantitaDaFatturare = parseDouble(259, 273);
		importoContrassegno = parseDouble(274, 288);
		tipoIncassoContrassegno = parseString(288, 290);
		divisaImportoContrassegno = parseString(290, 293);
		particolaritaContrassegno = parseString(294, 296);
		riferimentoMittenteNumerico = parseString(296, 311);
		riferimentoMittenteAlfabetico = parseString(311, 326);
		numeroSegnacolloDal = parseInteger(327, 334);
		numeroSegnacolloAl = parseInteger(335, 342);
		cumulativoColli = parseString(342, 343);
		note = parseString(344, 379);
		noteAggiuntive = parseString(379, 414);
		zonaConsegna = parseString(414, 416);
		codiceTrattamentoMerce = parseString(416, 417);
		fermoDeposito = parseString(417, 419);
		dataConsegnaRichiesta = parseString(420, 428);
		tipoConsegnaRichiesta = parseString(428, 429);
		oraConsegnaRichiesta = parseString(430, 434);
		codiceTassazione = parseString(434, 436);
		flagTassazione = parseString(436, 437);
		valoreMerceDichiarato = parseDouble(438, 453);
		divisaValoreMerceDichiarato = parseString(453, 456);
		gestioneParticolaritaConsegna = parseString(456, 458);
		gestioneParticolaritaGiacenza = parseString(458, 460);
		gestioneParticolaritaVarie = parseString(460, 462);
		primaConsegnaParticolare = parseString(464, 465);
		secondaConsegnaParticolare = parseString(465, 466);
		codiceSociale = parseString(466, 467);
		anticipata = parseString(467, 468);
		ragioneSocialeMittenteOriginale = parseString(468, 493);
		capMittenteOriginale = parseString(493, 502);
		nazioneMittenteOriginale = parseString(502, 505);
	}

	public String getFlagAnnullamento() {
		return flagAnnullamento;
	}

	public String getCodiceClienteMittente() {
		return codiceClienteMittente;
	}

	public String getFilialePartenza() {
		return filialePartenza;
	}

	public String getAnnoSpedizione() {
		return annoSpedizione;
	}

	public String getMeseGiornoSpedizione() {
		return meseGiornoSpedizione;
	}

	public String getNumeroSerie() {
		return numeroSerie;
	}

	public String getNumeroSpedizione() {
		return numeroSpedizione;
	}

	public String getCodiceBolla() {
		return codiceBolla;
	}

	public String getFilialeArrivo() {
		return filialeArrivo;
	}

	public String getRagioneSocialeDestinatario() {
		return ragioneSocialeDestinatario;
	}

	public String getEstensioneRagioneSocialeDestinatario() {
		return estensioneRagioneSocialeDestinatario;
	}

	public String getIndirizzoDestinatario() {
		return indirizzoDestinatario;
	}

	public String getCapDestinatario() {
		return capDestinatario;
	}

	public String getLocalitaDestinatario() {
		return localitaDestinatario;
	}

	public String getProvinciaDestinatario() {
		return provinciaDestinatario;
	}

	public String getNazioneDestinatario() {
		return nazioneDestinatario;
	}

	public String getPrimoGiornoChiusuraDestinario() {
		return primoGiornoChiusuraDestinario;
	}

	public String getSecondoGiornoChiusuraDestinatario() {
		return secondoGiornoChiusuraDestinatario;
	}

	public String getCodiceTariffa() {
		return codiceTariffa;
	}

	public String getTipoServizioBolle() {
		return tipoServizioBolle;
	}

	public Double getImportoDaAsicurare() {
		return importoDaAssicurare;
	}

	public String getDivisaImportoDaAssicurare() {
		return divisaImportoDaAssicurare;
	}

	public String getNaturaMerceMittente() {
		return naturaMerceMittente;
	}

	public Integer getNumeroColli() {
		return numeroColli;
	}

	public Double getPeso() {
		return peso;
	}

	public Double getVolume() {
		return volume;
	}

	public Double getQuantitaDaFatturare() {
		return quantitaDaFatturare;
	}

	public Double getImportoContrassegno() {
		return importoContrassegno;
	}

	public String getDivisaImportoContrassegno() {
		return divisaImportoContrassegno;
	}

	public String getTipoIncassoContrassegno() {
		return tipoIncassoContrassegno;
	}

	public String getParticolaritaContrassegno() {
		return particolaritaContrassegno;
	}

	public String getRiferimentoMittenteNumerico() {
		return riferimentoMittenteNumerico;
	}

	public String getRiferimentoMittenteAlfabetico() {
		return riferimentoMittenteAlfabetico;
	}

	public Integer getNumeroSegnacolloDal() {
		return numeroSegnacolloDal;
	}

	public Integer getNumeroSegnacolloAl() {
		return numeroSegnacolloAl;
	}

	public String getCumulativoColli() {
		return cumulativoColli;
	}

	public String getNote() {
		return note;
	}

	public String getNoteAggiuntive() {
		return noteAggiuntive;
	}

	public String getZonaConsegna() {
		return zonaConsegna;
	}

	public String getCodiceTrattamentoMerce() {
		return codiceTrattamentoMerce;
	}

	public String getFermoDeposito() {
		return fermoDeposito;
	}

	public String getDataConsegnaRichiesta() {
		return dataConsegnaRichiesta;
	}

	public String getOraConsegnaRichiesta() {
		return oraConsegnaRichiesta;
	}

	public String getTipoConsegnaRichiesta() {
		return tipoConsegnaRichiesta;
	}

	public String getCodiceTassazione() {
		return codiceTassazione;
	}

	public String getFlagTassazione() {
		return flagTassazione;
	}

	public Double getValoreMerceDichiarato() {
		return valoreMerceDichiarato;
	}

	public String getDivisaValoreMerceDichiarato() {
		return divisaValoreMerceDichiarato;
	}

	public String getGestioneParticolaritaConsegna() {
		return gestioneParticolaritaConsegna;
	}

	public String getGestioneParticolaritaGiacenza() {
		return gestioneParticolaritaGiacenza;
	}

	public String getGestioneParticolaritaVarie() {
		return gestioneParticolaritaVarie;
	}

	public String getPrimaConsegnaParticolare() {
		return primaConsegnaParticolare;
	}

	public String getSecondaConsegnaParticolare() {
		return secondaConsegnaParticolare;
	}

	public String getCodiceSociale() {
		return codiceSociale;
	}

	public String getAnticipata() {
		return anticipata;
	}

	public String getRagioneSocialeMittenteOriginale() {
		return ragioneSocialeMittenteOriginale;
	}

	public String getCapMittenteOriginale() {
		return capMittenteOriginale;
	}

	public String getNazioneMittenteOriginale() {
		return nazioneMittenteOriginale;
	}

}
