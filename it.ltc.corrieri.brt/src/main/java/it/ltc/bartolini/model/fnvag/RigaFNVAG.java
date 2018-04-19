package it.ltc.bartolini.model.fnvag;

import java.util.Date;

import it.ltc.bartolini.model.RigaCampiFissiBRT;
import it.ltc.bartolini.model.costanti.CausaliGiacenza;
import it.ltc.bartolini.model.costanti.ChiusuraGiacenza;

/**
 * Rappresento un elemento FNAVG, una giacenza.<br>
 * <br>
 * FNVAG00T – GESTIONE GIACENZE
 * Trasmissione comunicazioni di apertura e chiusura GIACENZE ai mittenti
 * Verrà trasmesso un record per ogni giacenza, identificabile in maniera univoca dai campi:
 * - Anno apertura giacenza, filiale che ha aperto giacenza, numero giacenza
 * Le date sono nel formato anno mese giorno
 * 
 * @author Damiano
 *
 */
public class RigaFNVAG extends RigaCampiFissiBRT {

	/**
	 * VAGLNP - FILIALE  DI PARTENZA
	 * codice della filiale da cui è partita la merce.
	 */
	private final String filialeDiPartenza;
	
	/**
	 * VAGAAS - ANNO SPEDIZIONE
	 * anno della spedizione comprensivo del secolo (formato yyyy)
	 */
	private final int annoSpedizione;
	
	/**
	 * VAGNRS - NUMERO SERIE
	 * N.assegnato ai clienti che segnacollano la merce (altrimenti  0)
	 */
	private final String numeroSerie;
	
	/**
	 * VAGNSP - NUMERO SPEDIZIONE
	 * Numero progressivo assegnato dal corriere
	 */
	private final String numeroSpedizione;
	
	/**
	 * VAGLNA - FILIALE DI ARRIVO
	 * codice della filiale che effettua la consegna
	 */
	private final String filialeArrivo;
	
	/**
	 * Combina i campi:
	 * 
	 * VAGAGC - ANNO APERTURA GIACENZA
	 * Anno in cui la giacenza ha avuto inizio
	 * 
	 *  VAGMGC - MESE / GIORNO APERTURA GIACENZA
	 *  Mese e giorno in cui la giacenza ha avuto inizio
	 */
	private final Date dataApertura;
	
	/**
	 * VAGFGC - FILIALE CHE HA APERTO LA GIACENZA
	 * Cod.filiale che ha aperto la giacenza (fil. arrivo o partenza)
	 */
	private final String filialeAperturaGiacenza;
	
	/**
	 * VAGNGC - NUMERO GIACENZA
	 * Numero progressivo interno (corriere) della giacenza
	 */
	private final String numeroGiacenza;
	
	/**
	 * VAGFRG - NR. PROGR. RIAPERTURA GIACENZA
	 * Progressivo <> 0 se la spedizione dopo una prima segnalazione di giacenza è stata nuovamente respinta.
	 */
	private final int progressivoAperturaGiacenza;
	
	/**
	 * VAGGGA - GESTIONE PARTICOLARITA’ GIACENZA
	 * uso interno corriere
	 */
	private final String particolarita;
	
	/**
	 * VAGCMC - CAUSALE GIACENZA
	 * Ved.  elenco causali giacenza
	 */
	private final CausaliGiacenza causale;
	
	/**
	 * VAGDMC - DESCRIZIONE CAUSALE GIACENZA
	 * Idem
	 */
	private final String descrizioneCausale;
	
	/**
	 * VAGNOT - DESCR. ULTERIORE MOTIVO DI GIACENZA
	 * eventuale ulteriore motivazione di giacenza
	 */
	private final String descrizioneAggiuntivaCausale;
	
	/**
	 * VAGRMN - RIFERIMENTO MITTENTE NUMERICO
	 * Numero di riferimento del mittente: bolla/fattura
	 */
	private final String riferimentoMittenteNumerico;
	
	/**
	 * VAGRMA - RIFERIMENTO MITTENTE ALFABETICO
	 * Numero di riferimento alfabetico del mittente
	 */
	private final String riferimentoMittenteAlfabetico;
	
	/**
	 * VAGSCM - CODICE MITTENTE GIACENZA
	 * Codice di conto corrente improprio del cliente c/o corriere
	 */
	private final String codiceMittenteGiacenza;
	
	/**
	 * VAGDMM - DATA STAMPA MODULO MITTENTE GIACENZA
	 * Data in cui la comunicazione di giacenza è stata stampata e inoltrata al mittente
	 */
	private final Date dataStampaModulo;
	
	/**
	 * VAGVCS - VARIAZ. IMPORTO C/ASSEGNO (S/N)
	 * S= indica che l'importo del contrassegno è stato variato
	 */
	private final boolean variazioneContrassegno;
	
	/**
	 * VAGCAS - NUOVO IMPORTO C/ASSEGNO
	 * Nuovo importo del contrassegno a seguito di variazione dello stesso (0 indica che il contrassegno è stato annullato)
	 * questo campo ha valore unicamente se il flag variazione importo contrassegno è impostato uguale ad S
	 */
	private final Double nuovoImportoContrassegno;
	
	/**
	 * VAGVCA - DIVISA IMPORTO C/ASSEGNO
	 * divisa dell'importo contrassegno
	 */
	private final String divisaContrassegno;
	
	/**
	 * VAGDDM - DATA DISPOSIZIONI MITTENTE
	 * data in cui sono state immesse le disposizioni ricevute dal mittente per la risoluzione della giacenza.
	 */
	private final Date dataDisposizioniMittente;
	
	/**
	 * VAGDED - DATA ESEGUIBILITA’ DISPOSIZ. GIACENZA
	 * Se maggiore di 0 indica la data dalla quale le disposizioni giacenza saranno eseguite.
	 */
	private final Date dataEsiguibilita;
	
	/**
	 * VAGDUR - ULTIMA DATA RIAPERTURA GIACENZA
	 * data in cui è stata effettuata l'eventuale riapertura della giacenza.
	 */
	private final Date ultimaDataRiapertura;
	
	/**
	 * VAGDCG - DATA  CHIUSURA GIACENZA IN ARRIVO
	 * Data di fine giacenza.
	 */
	private final Date dataChiusura;
	
	/**
	 * VAGCFG - CODICE FINE GIACENZA
	 * Ved. tabella codice chiusura giacenza.
	 */
	private final ChiusuraGiacenza codiceChiusura;
	
	/**
	 * VAGDSD - DATA SOSTA –DAL-
	 * Data inizio sosta giacenza.
	 */
	private final Date dataSostaDal;
	
	/**
	 * VAGDSA - DATA SOSTA – AL -
	 * Data fine sosta giacenza.
	 */
	private final Date dataSostaAl;
	
	/**
	 * VAGGGS - GIORNI SOSTA
	 * totale giorni di sosta giacenza.
	 */
	private final int giorniSosta;
	
	/**
	 * VAGSGF - FRANCHIGIA GIORNI SOSTA
	 * Numero giorni per i quali non è previsto alcun addebito per sosta.
	 */
	private final int giorniFranchigia;
	
	/**
	 * VAGSGA - SPESE AGGIUNTIVE GIACENZA
	 * Ulteriori spese relative alla giacenza.
	 */
	private final String speseAggiuntive;
	
	/**
	 * VAGTFT - TIPO FATTURAZ.
	 * 0=SI (vengono addebitate le spese)
	 * 9=NO	(non vengono addebitate le spese) 
	 */
	private final boolean fatturazione;
	
	/**
	 * VAGDBG - DATA CREAZIONE BOLLA GIACENZA
	 * Data in cui è stata emessa bolla per il recupero spese giacenza.
	 */
	private final Date dataCreazioneBolla;
	
	/**
	 * VAGLPW - FILIALE  PARTENZA BOLLA GIACENZA
	 * Filiale o corriere che ha emesso bolla di recupero spese.
	 */
	private final String filialePartenzaBolla;
	
	/**
	 * VAGASW - ANNO BOLLA GIACENZA
	 * Anno di emissione bolla di recupero spese.
	 */
	private final Integer annoBollaGiacenza;
	
	/**
	 * VAGNSW - NR SERIE SPEDIZIONE BOLLA GIACENZA
	 * Sempre uguale a 0.
	 */
	private final String serieBollaGiacenza;
	
	/**
	 * VAGNBW - NR SPEDIZIONE BOLLA GIACENZA
	 * Numero bolla di recupero spese giacenza.
	 */
	private final String numeroSpedizioneBolla;
	
	/**
	 * VAGKSC - CODICE CLIENTE BOLLA ADDEBITO GIACENZA
	 * Cod.cliente a cui è stata emessa la bolla di recupero.
	 */
	private final String codiceClienteBolla;
	
	public RigaFNVAG(String riga) {
		super(riga);
		filialeDiPartenza = parseString(1, 4);
		annoSpedizione = parseInteger(5, 9);
		numeroSerie = parseString(10, 12);
		numeroSpedizione = parseString(13, 20);
		filialeArrivo = parseString(21, 24);
		String annoAperura = parseString(25, 29);
		String meseGiornoApertura = parseString(30, 34);
		dataApertura = parseDate(annoAperura + meseGiornoApertura);
		filialeAperturaGiacenza = parseString(35, 38);
		numeroGiacenza = parseString(39, 46);
		progressivoAperturaGiacenza = parseInteger(47, 48);
		particolarita = parseString(49, 50);
		causale = CausaliGiacenza.valueOf("_" + parseString(50, 53));
		descrizioneCausale = parseString(53, 103);
		descrizioneAggiuntivaCausale = parseString(103, 153);
		riferimentoMittenteNumerico = parseString(154, 169);
		riferimentoMittenteAlfabetico = parseString(169, 184);
		codiceMittenteGiacenza = parseString(185, 192);
		dataStampaModulo = parseDate(193, 201);
		String variazione = parseString(201, 202);
		variazioneContrassegno = variazione.equals("S");
		nuovoImportoContrassegno = parseDouble(203, 217);
		divisaContrassegno = parseString(218, 220);
		dataDisposizioniMittente = parseDate(221, 229);
		dataEsiguibilita = parseDate(230, 238);
		ultimaDataRiapertura = parseDate(239, 247);
		dataChiusura = parseDate(248, 256);
		String chiusura = parseString(256, 259).isEmpty() ? "000" : parseString(256, 259);
		codiceChiusura = ChiusuraGiacenza.valueOf("_" + chiusura);
		dataSostaDal = parseDate(284, 292);
		dataSostaAl = parseDate(293, 301);
		giorniSosta = parseInteger(302, 305);
		giorniFranchigia = parseInteger(306, 309);
		speseAggiuntive = parseString(310, 317);
		String tipoFatturazione = parseString(318, 319);
		fatturazione = tipoFatturazione.equals("0");
		dataCreazioneBolla = parseDate(320, 328);
		filialePartenzaBolla = parseString(329, 332);
		annoBollaGiacenza = parseInteger(333, 337);
		serieBollaGiacenza = parseString(338, 340);
		numeroSpedizioneBolla = parseString(341, 348);
		codiceClienteBolla = parseString(349, 356);
	}

	public String getFilialeDiPartenza() {
		return filialeDiPartenza;
	}

	public int getAnnoSpedizione() {
		return annoSpedizione;
	}

	public String getNumeroSerie() {
		return numeroSerie;
	}

	public String getNumeroSpedizione() {
		return numeroSpedizione;
	}

	public String getFilialeArrivo() {
		return filialeArrivo;
	}

	public Date getDataApertura() {
		return dataApertura;
	}

	public String getFilialeAperturaGiacenza() {
		return filialeAperturaGiacenza;
	}

	public String getNumeroGiacenza() {
		return numeroGiacenza;
	}

	public int getProgressivoAperturaGiacenza() {
		return progressivoAperturaGiacenza;
	}

	public String getParticolarita() {
		return particolarita;
	}

	public CausaliGiacenza getCausale() {
		return causale;
	}

	public String getDescrizioneCausale() {
		return descrizioneCausale;
	}

	public String getDescrizioneAggiuntivaCausale() {
		return descrizioneAggiuntivaCausale;
	}

	public String getRiferimentoMittenteNumerico() {
		return riferimentoMittenteNumerico;
	}

	public String getRiferimentoMittenteAlfabetico() {
		return riferimentoMittenteAlfabetico;
	}

	public String getCodiceMittenteGiacenza() {
		return codiceMittenteGiacenza;
	}

	public Date getDataStampaModulo() {
		return dataStampaModulo;
	}

	public boolean isVariazioneContrassegno() {
		return variazioneContrassegno;
	}

	public Double getNuovoImportoContrassegno() {
		return nuovoImportoContrassegno;
	}

	public String getDivisaContrassegno() {
		return divisaContrassegno;
	}

	public Date getDataDisposizioniMittente() {
		return dataDisposizioniMittente;
	}

	public Date getDataEsiguibilita() {
		return dataEsiguibilita;
	}

	public Date getUltimaDataRiapertura() {
		return ultimaDataRiapertura;
	}

	public Date getDataChiusura() {
		return dataChiusura;
	}

	public ChiusuraGiacenza getCodiceChiusura() {
		return codiceChiusura;
	}

	public Date getDataSostaDal() {
		return dataSostaDal;
	}

	public Date getDataSostaAl() {
		return dataSostaAl;
	}

	public int getGiorniSosta() {
		return giorniSosta;
	}

	public int getGiorniFranchigia() {
		return giorniFranchigia;
	}

	public String getSpeseAggiuntive() {
		return speseAggiuntive;
	}

	public boolean isFatturazione() {
		return fatturazione;
	}

	public Date getDataCreazioneBolla() {
		return dataCreazioneBolla;
	}

	public String getFilialePartenzaBolla() {
		return filialePartenzaBolla;
	}

	public Integer getAnnoBollaGiacenza() {
		return annoBollaGiacenza;
	}

	public String getSerieBollaGiacenza() {
		return serieBollaGiacenza;
	}

	public String getNumeroSpedizioneBolla() {
		return numeroSpedizioneBolla;
	}

	public String getCodiceClienteBolla() {
		return codiceClienteBolla;
	}
	
}
