package it.ltc.bartolini.model.fnvac;

import java.util.HashMap;

import it.ltc.bartolini.model.RigaBRT;
import it.ltc.utility.miscellanea.string.StringUtility;

/**
 * FNVAC00T – CERTIFICAZIONE DELLE RESE (ESITI CONSEGNA)
 * Trasmissione dati di consegna ai clienti Mittenti
 * 
 * Viene trasmesso record per ogni spedizione che ha avuto un evento di consegna identificabile in maniera univoca dai campi:
 * - anno spedizione
 * - filiale di partenza
 * - numero serie spedizione
 * - numero spedizione
 * - tipo bolla
 * @author Damiano
 *
 */
public class RigaFNVAC extends RigaBRT {
	
	private static final StringUtility su = new StringUtility();
	
	/**
	 * Codifica per le anomalie di consegna.
	 * Per i valori numerici è stato inserito un _ davanti
	 * @author Damiano
	 *
	 */
	public enum AnomalieConsegna {
		_1("MERCE DIROTTATA"), 				
		_2("RESO AL MITTENTE"), 
		_3("RAPINATA MERCE"),
		_4("RAPINATO INCASSO"),
		_5("CHIUSURA SPEDIZIONE CON PRATICA ANOMALIA"),
		_6("CHIUSURA SPEDIZIONE CON AVARIA RESA AL MITTENTE"),
		_7("MERCE MAI AFFIDATA"),
		_9("CAMBIO DI PORTO"),
		A("APERTURA PRATICA ANOMALIA"),
		C("MESSA IN CONSEGNA"),
		R("SPEDIZIONE RIPRISTINATA DOPO ERRATA IMPUTAZIONE DI CONSEGNA"),
		S("MERCE DISTRUTTA");
		
		private final String descrizione;
		
		private AnomalieConsegna(String descrizione) {
			this.descrizione = descrizione;
		}

		public String getDescrizione() {
			return descrizione;
		}

	}
	
	/**
	 * Codifica per le consegne particolari.
	 * @author Damiano
	 *
	 */
	public enum ConsegneParticolari {
		
		A("PER APPUNTAMENTO"),
		F("FUORI MISURA"),
		P("AI PIANI"),
		S("SUPERMERCATI"),
		X("CONSEGNA DISAGIATA");
		
		private final String descrizione;
		
		private ConsegneParticolari(String descrizione) {
			this.descrizione = descrizione;
		}

		public String getDescrizione() {
			return descrizione;
		}

	}
	
	/**
	 * VACAAS - anno della spedizione nel formato yyyy
	 */
	private final Integer annoSpedizione;
	
	/**
	 * VACLNP - codice della filiale da cui è partita la merce
	 */
	private final String filialePartenza;
	
	/**
	 * VACNRS - N.assegnato ai clienti che segnacollano la merce (altrimenti  0)
	 */
	private final String numeroSerie;
	
	/**
	 * VACNSP - Numero progressivo assegnato dal corriere
	 */
	private final String numeroSpedizione;
	
	/**
	 * VACMGS - mese/giorno della lettera di vettura corriere nel formato MMdd
	 */
	private final String meseGiornoSpedizione;
	
	/**
	 * VACCBO - Vedi tabella Codici bolla
	 */
	private final String codiceBolla;
	
	/**
	 * VACLNA - codice della filiale che effettua la consegna
	 */
	private final String filialeArrivo;
	
	/**
	 * VACRSD - Ragione sociale del destinatario della merce
	 */
	private final String ragioneSocialeDestinatario;
	
	/**
	 * VACPRD - Provincia destinazione merce
	 */
	private final String provinciaDestinatario;
	
	/**
	 * VACGC1 - Vedi tabella gg chiusura
	 */
	private final String primoGiornoChiusuraDestinatario;
	
	/**
	 * VACGC2 - Vedi tabella gg chiusura
	 */
	private final String secondooGiornoChiusuraDestinatario;
	
	/**
	 * VACCTR - codice della tariffa utilizzata per la tassazione della spedizione
	 * I valori concordati sono:
	 * - 000 express
	 * - 100 express colli
	 */
	private final String codiceTariffa;
	
	/**
	 * VACCTS - codice che identifica il luogo di destinazione ai fini della applicazione della tariffa.
	 * Usualmente blank.
	 */
	private final String codiceTassazione;
	
	/**
	 * VACFTM - S = applicazione stessa tariffa del mittente al destinatario, in caso di porto assegnato
	 */
	private final String flagTassazione;
	
	/**
	 * VACFIN - determinato in base al CAP di destino
	 * C=Citta'
	 * P=Provincia
	 * I=Isola
	 * D = loc. disagiate
	 * T ZTL=CITTA'
	 * Z= ZTL PROVINCIA
	 * 
	 */
	private final String flagInoltro;
	
	/**
	 * VACFAP - determinato in base al CAP di partenza
	 * C=Citta'
	 * P=Provincia
	 * I=Isola
	 * D = loc. disagiate 
	 */
	private final String flagAnteporto;
	
	/**
	 * VACTSP - Tipo servizio richiesto (ved.tabella tipi servizio)
	 */
	private final String tipoServizioBolle;
	
	/**
	 * VACIAS - Valore totale spedizione concordato dal Mandato assicurativo sottoscritto
	 */
	private final Double importoDaAssicurare;
	
	/**
	 * VACVAS - Divisa dell'importo da assicurare
	 */
	private final String divisaImportoDaAssicurare;
	
	/**
	 * VACNAS - Indicazione generica della natura della merce
	 */
	private final String naturaMerceMittente;
	
	/**
	 * VACNCL - Numero colli di cui è composta la spedizione
	 */
	private final Integer numeroColli;
	
	/**
	 * VACPKB - Peso lordo complessivo della spedizione.
	 * Viene misurato in KG, solitamente sono 7 cifre (0 padding) di cui l'ultima indica un decimale.
	 */
	private final Double peso;
	
	/**
	 * VACVLB - Volume della spedizione espresso in mc.
	 * Viene misurato in mc^3, solitamente sono 5 cifre (0 padding) di cui le ultime 3 indicano i decimali.
	 */
	private final Double volume;
	
	/**
	 * VACQFT - Indicato solo se previsto dalle condizioni tariffarie
	 */
	private final Double quantitaDaFatturare;
	
	/**
	 * VACTIC - Vedi tabella Tipo Incasso Contrassegno
	 */
	private final String tipoIncassoContrassegno;
	
	/**
	 * VACCAS - Eventuale importo del contrassegno
	 */
	private final Double valoreContrassegno;
	
	/**
	 * VACVCA - divisa dell'importo contrassegno
	 */
	private final String divisaValoreContrassegno;
	
	/**
	 * VACCCM - Codice di conto corrente improprio del cliente c/o corriere
	 */
	private final String codiceClienteMittente;
	
	/**
	 * VACRMN - Numero di riferimento del documento Mittente (Bolla/Fattura)
	 */
	private final Integer riferimentoMittenteNumerico;
	
	/**
	 * VACRMA - Ulteriore riferimento Mittente
	 */
	private final String riferimentoMittenteAlfabetico;
	
	/**
	 * VACRMO - Viene indicata la ragione sociale del mittente originale nel caso sia diversa dal mittente che ci affida la spedizione
	 */
	private final String ragioneSocialeMittenteOriginale;
	
	/**
	 * VACFFD - Viene indicato S se la merce doveva essere consegnata c/o il ns. Magazzino di arrivo, blank altrimenti.
	 */
	private final String fermoDeposito;
	
	/**
	 * VACTCR
	 * blank = Consegna IL
	 * P = Consegna prima DEL
	 * D = Consegna DOPO IL
	 */
	private final String tipoConsegna;
	
	/**
	 * VACDCR - Data di consegna richiesta dal mittente (nel formato AAAAMMGG)
	 */
	private final String dataConsegnaRichiesta;
	
	/**
	 * VACHCR - Ora in cui è stata richiesta la consegna al destinatario
	 */
	private final String oraConsegnaRichiesta;
	
	/**
	 * VACDCM - Data in cui è avvenuta la consegna al destinatario
	 * Se codice consegna anomala è = A, considerare questa data come data apertura pratica danno
	 */
	private final String dataConsegna;
	
	/**
	 * VACHMC - Ora in cui è avvenuta la consegna al destinatario
	 */
	private final String oraConsegna;
	
	/**
	 * VACTC1 - Vedi tabella Consegne Particolari
	 */
	private final String particolareConsegnaUno;
	
	/**
	 * VACTC2 - Vedi tabella Consegne Particolari
	 */
	private final String particolareConsegnaDue;
	
	/**
	 * VACCCA - Vedi tabella codici anomalie consegna 
	 */
	private final String codiceConsegnaAnomala;
	
	/**
	 * VACDLA - Data in cui l'incaricato alla consegna ha lasciato l’avviso al destinatario
	 */
	private final String dataLasciatoAvviso;
	
	/**
	 * VACDAG - Data in cui è stata aperta pratica di giacenza.
	 * Per possibilità di riconsegna si attendono disposizioni, in mancanza delle quali, si provvederà al reso al mittente.
	 */
	private final String dataAperturaGiacenza;
	
	/**
	 * VACLOD - Localita' del destinatario
	 */
	private final String localitaDestinatario;
	
	/**
	 * E' la concatenazione di filialeDiPartenza, numeroSerie e numeroSpedizione.
	 */
	private final String letteraDiVettura;
	
	public RigaFNVAC(HashMap<String, Integer> mappaColonne, String[] campi) {
		
		//Dati spedizione
		annoSpedizione = parseInteger(campi[mappaColonne.get("VACAAS")]);
		meseGiornoSpedizione = campi[mappaColonne.get("VACMGS")];
		numeroSpedizione = su.getFormattedString(campi[mappaColonne.get("VACNSP")], 7, "0", false);
		numeroSerie = su.getFormattedString(campi[mappaColonne.get("VACNRS")], 2, "0", false);
		riferimentoMittenteNumerico = parseInteger(campi[mappaColonne.get("VACRMN")]);
		riferimentoMittenteAlfabetico = campi[mappaColonne.get("VACRMA")];
		numeroColli = parseInteger(campi[mappaColonne.get("VACNCL")]);
		peso = parseDouble(campi[mappaColonne.get("VACPKB")]);
		volume = parseDouble(campi[mappaColonne.get("VACVLB")]);
		filialePartenza = su.getFormattedString(campi[mappaColonne.get("VACAAS")], 3, "0", false);
		filialeArrivo = su.getFormattedString(campi[mappaColonne.get("VACLNA")], 3, "0", false);
		codiceClienteMittente = su.getFormattedString(campi[mappaColonne.get("VACCCM")], 7, "0", false);
		codiceBolla = campi[mappaColonne.get("VACCBO")];
		
		//Destinatario
		ragioneSocialeDestinatario = campi[mappaColonne.get("VACRSD")];
		provinciaDestinatario = campi[mappaColonne.get("VACPRD")];
		localitaDestinatario = campi[mappaColonne.get("VACLOD")];
		
		//Mittente
		ragioneSocialeMittenteOriginale = campi[mappaColonne.get("VACRMO")];
		
		//Contrassegno
		valoreContrassegno = parseDouble(campi[mappaColonne.get("VACCAS")]);
		divisaValoreContrassegno = campi[mappaColonne.get("VACVCA")];
		tipoIncassoContrassegno = campi[mappaColonne.get("VACTIC")];
		
		//Assicurazione
		importoDaAssicurare = parseDouble(campi[mappaColonne.get("VACIAS")]);
		divisaImportoDaAssicurare = campi[mappaColonne.get("VACVAS")];
		
		//Consegna
		particolareConsegnaUno = campi[mappaColonne.get("VACTC1")];
		particolareConsegnaDue = campi[mappaColonne.get("VACTC2")];
		dataConsegnaRichiesta = campi[mappaColonne.get("VACDCR")];
		oraConsegnaRichiesta = campi[mappaColonne.get("VACHCR")];
		dataConsegna = campi[mappaColonne.get("VACDCM")];
		oraConsegna = campi[mappaColonne.get("VACHMC")];
		
		//Giacenze
		dataLasciatoAvviso = campi[mappaColonne.get("VACDLA")];
		dataAperturaGiacenza = campi[mappaColonne.get("VACDAG")];
		codiceConsegnaAnomala = campi[mappaColonne.get("VACCCA")];
		
		//Dati accessori
		fermoDeposito = campi[mappaColonne.get("VACFFD")];
		flagTassazione = campi[mappaColonne.get("VACFTM")];
		flagInoltro = campi[mappaColonne.get("VACFIN")];
		flagAnteporto = campi[mappaColonne.get("VACFAP")];
		codiceTassazione = campi[mappaColonne.get("VACCTS")];
		codiceTariffa = campi[mappaColonne.get("VACCTR")];
		naturaMerceMittente = campi[mappaColonne.get("VACNAS")];
		tipoServizioBolle = campi[mappaColonne.get("VACTSP")];
		tipoConsegna = campi[mappaColonne.get("VACTCR")];
		quantitaDaFatturare = parseDouble(campi[mappaColonne.get("VACQFT")]);
		primoGiornoChiusuraDestinatario = campi[mappaColonne.get("VACGC1")];
		secondooGiornoChiusuraDestinatario = campi[mappaColonne.get("VACGC2")];
		
		//Campo generato
		letteraDiVettura = filialePartenza + numeroSerie + numeroSpedizione;
	}

	public Integer getAnnoSpedizione() {
		return annoSpedizione;
	}

	public String getFilialePartenza() {
		return filialePartenza;
	}

	public String getNumeroSerie() {
		return numeroSerie;
	}

	public String getNumeroSpedizione() {
		return numeroSpedizione;
	}

	public String getMeseGiornoSpedizione() {
		return meseGiornoSpedizione;
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

	public String getProvinciaDestinatario() {
		return provinciaDestinatario;
	}

	public String getPrimoGiornoChiusuraDestinatario() {
		return primoGiornoChiusuraDestinatario;
	}

	public String getSecondooGiornoChiusuraDestinatario() {
		return secondooGiornoChiusuraDestinatario;
	}

	public String getCodiceTariffa() {
		return codiceTariffa;
	}

	public String getCodiceTassazione() {
		return codiceTassazione;
	}

	public String getFlagTassazione() {
		return flagTassazione;
	}

	public String getFlagInoltro() {
		return flagInoltro;
	}

	public String getFlagAnteporto() {
		return flagAnteporto;
	}

	public String getTipoServizioBolle() {
		return tipoServizioBolle;
	}

	public Double getImportoDaAssicurare() {
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

	public String getTipoIncassoContrassegno() {
		return tipoIncassoContrassegno;
	}

	public Double getValoreContrassegno() {
		return valoreContrassegno;
	}

	public String getDivisaValoreContrassegno() {
		return divisaValoreContrassegno;
	}

	public String getCodiceClienteMittente() {
		return codiceClienteMittente;
	}

	public Integer getRiferimentoMittenteNumerico() {
		return riferimentoMittenteNumerico;
	}

	public String getRiferimentoMittenteAlfabetico() {
		return riferimentoMittenteAlfabetico;
	}

	public String getRagioneSocialeMittenteOriginale() {
		return ragioneSocialeMittenteOriginale;
	}

	public String getFermoDeposito() {
		return fermoDeposito;
	}

	public String getTipoConsegna() {
		return tipoConsegna;
	}

	public String getDataConsegnaRichiesta() {
		return dataConsegnaRichiesta;
	}

	public String getOraConsegnaRichiesta() {
		return oraConsegnaRichiesta;
	}

	public String getDataConsegna() {
		return dataConsegna;
	}

	public String getOraConsegna() {
		return oraConsegna;
	}

	public String getParticolareConsegnaUno() {
		return particolareConsegnaUno;
	}

	public String getParticolareConsegnaDue() {
		return particolareConsegnaDue;
	}

	public String getCodiceConsegnaAnomala() {
		return codiceConsegnaAnomala;
	}

	public String getDataLasciatoAvviso() {
		return dataLasciatoAvviso;
	}

	public String getDataAperturaGiacenza() {
		return dataAperturaGiacenza;
	}

	public String getLocalitaDestinatario() {
		return localitaDestinatario;
	}

	public String getLetteraDiVettura() {
		return letteraDiVettura;
	}

}
