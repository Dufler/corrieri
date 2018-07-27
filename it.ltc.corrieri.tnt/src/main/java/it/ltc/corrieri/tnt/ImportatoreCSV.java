package it.ltc.corrieri.tnt;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import it.ltc.database.dao.common.CodiceClienteCorriereDao;
import it.ltc.database.dao.common.CommessaDao;
import it.ltc.database.dao.common.IndirizzoDao;
import it.ltc.database.dao.common.SpedizioneContrassegnoDao;
import it.ltc.database.dao.common.SpedizioneDao;
import it.ltc.database.model.centrale.Commessa;
import it.ltc.database.model.centrale.Indirizzo;
import it.ltc.database.model.centrale.JoinCommessaCorriere;
import it.ltc.database.model.centrale.Spedizione;
import it.ltc.database.model.centrale.Spedizione.TipoSpedizione;
import it.ltc.database.model.centrale.SpedizioneContrassegno;
import it.ltc.database.model.centrale.enumcondivise.Fatturazione;
import it.ltc.database.model.legacy.TestaCorr;
import it.ltc.utility.csv.FileCSV;

public class ImportatoreCSV {

	private static final Logger logger = Logger.getLogger(ImportatoreCSV.class);
	
	private static final String persistenceUnit = "produzione"; //"test"; //
	
	private static ImportatoreCSV instance;
	
	private final SimpleDateFormat sdf;
	//private final Date ora;
	
	private final CodiceClienteCorriereDao daoCodici;
	private final CommessaDao daoCommesse;
	private final SpedizioneDao daoSpedizioni;
	private final SpedizioneContrassegnoDao daoContrassegni;
	private final IndirizzoDao daoIndirizzi;
	private final RecuperatoreDatiLegacy recuperaLegacy;
	
	private int inserimenti;
	private int aggiornamenti;
	
	public static void main(String[] args) throws Exception {
		ImportatoreCSV importatore = ImportatoreCSV.getInstance();
		importatore.importaFile("C:\\Users\\Damiano\\Downloads\\trackingGiugno.csv");
	}
	
	private ImportatoreCSV() {
		sdf = new SimpleDateFormat("dd/MM/yyyy");
		//ora = new Date();
		daoCodici = new CodiceClienteCorriereDao(persistenceUnit);
		daoCommesse = new CommessaDao(persistenceUnit);
		daoSpedizioni = new SpedizioneDao(persistenceUnit);
		daoContrassegni = new SpedizioneContrassegnoDao(persistenceUnit);
		daoIndirizzi = new IndirizzoDao(persistenceUnit);
		recuperaLegacy = RecuperatoreDatiLegacy.getInstance();
	}
	
	public static ImportatoreCSV getInstance() {
		if (instance == null) {
			instance = new ImportatoreCSV();
		}
		return instance;
	}
	
	public void importaFile(String path) throws Exception {
		File file = new File(path);
		FileCSV csv = FileCSV.leggiFile(file, true, ";", ";");
		inserimenti = 0;
		aggiornamenti = 0;
		for (String[] riga : csv.getRighe()) {
			importaSpedizione(riga);
		}
		logger.info("Spedizione inserite: " + inserimenti);
		logger.info("Spedizione aggiornate: " + aggiornamenti);
		logger.info("Spedizioni importate: " + (inserimenti + aggiornamenti) + " su " + csv.getRighe().size());
	}
	
	private TestaCorr recuperaTestaCorr(JoinCommessaCorriere codiceCliente, String[] riga) {
		Commessa commessa = daoCommesse.trovaDaID(codiceCliente.getCommessa());
		String nomeRisorsa = commessa != null ? commessa.getNomeRisorsa() : "";
		String riferimentoSpedizione = riga[2];
		String destinatarioSpedizione = riga[10];
		Date dataSpedizione;
		try {
			dataSpedizione = sdf.parse(riga[1]);
		} catch (ParseException e) {
			dataSpedizione = new Date();
		}
		TestaCorr vecchia = recuperaLegacy.recuperaTestata(nomeRisorsa, riferimentoSpedizione, destinatarioSpedizione, dataSpedizione);
		return vecchia;
	}
	
	public boolean importaSpedizione(String[] riga) {
		boolean successo;
		try {
			String cc = riga[3];
			if (cc.length() == 7)
				cc = "0" + cc;
			JoinCommessaCorriere codiceCliente = daoCodici.trovaDaCodice(cc);
			if (codiceCliente != null) {
				TestaCorr vecchia = recuperaTestaCorr(codiceCliente, riga);
				Spedizione trovata = recuperaSpedizione(riga);
				boolean spedizionePresente = (trovata != null);
				if (spedizionePresente) {
					//Aggiorno la spedizione.
					successo = aggiornaSpedizione(trovata, riga, vecchia);
					if (successo)
						aggiornamenti += 1;
				} else {
					//Inserisco la spedizione.
					int idOrdine = inserisciNuovoDocumento(codiceCliente, riga);
					successo = inserisciNuovaSpedizione(codiceCliente, idOrdine, vecchia, riga);
					if (successo)
						inserimenti += 1;
				}
			} else {
				//Sonia non ha inserito il codice cliente.
				logger.error("Non è stato inserito il codice cliente: " + cc);
				logger.error("La spedizione persa:");
				logger.error(riga);
				successo = false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Eccezione per: ");
			logger.error(riga);
			logger.error(e);
			successo = false;
		}
		return successo;
	}
	
	private double trovaPeso(String[] riga) {
		double peso;
		try {
			String p = riga[18];
			p = p.replace(',', '.');
			peso = Double.parseDouble(p);
		} catch (NumberFormatException e) { peso = 0;}
		double pesoRilevato;
		try {
			String p = riga[19];
			p = p.replace(',', '.');
			pesoRilevato = Double.parseDouble(p);
		} catch (NumberFormatException e) { pesoRilevato = 0;}
		//Prendo il maggiore fra i due
		if (pesoRilevato > peso)
			peso = pesoRilevato;
		return peso;
	}
	
	private double trovaVolume(String[] riga) {
		double volume;
		try {
			String v = riga[20];
			v = v.replace(',', '.');
			volume = Double.parseDouble(v);
		} catch (NumberFormatException e) {
			volume = 0;
		}
		return volume;
	}
	
	private boolean aggiornaSpedizione(Spedizione trovata, String[] riga, TestaCorr vecchia) {
		//Update info generali
		trovata.setServizio(getTipoServizio(riga[21]));
		//Peso
		double peso = trovaPeso(riga);
		if (peso > 0)
			trovata.setPeso(peso);
		//Volume
		double volume = trovaVolume(riga);
		if (volume > 0)
			trovata.setVolume(volume);
		if (vecchia != null && vecchia.getPezzi() > 0) {
			trovata.setPezzi(vecchia.getPezzi());
			trovata.setDatiCompleti(true);
			logger.info("Spedizione aggiornata dal sistema legacy.");
		}
		Spedizione entity = daoSpedizioni.aggiorna(trovata);
		boolean update = entity != null;
		if (!update)
			logger.error("Impossibile aggiornare la spedizione LDV: '" + trovata.getLetteraDiVettura() + "'");
		return update;
	}

	private Spedizione recuperaSpedizione(String[] riga) {
		String letteraDiVettura = riga[0];
		Spedizione trovata = daoSpedizioni.trovaDaLetteraDiVettura(letteraDiVettura);
		return trovata;
	}

	private int ottieniDestinatario(String[] riga) {
		Indirizzo destinatario = new Indirizzo();
		destinatario.setRagioneSociale(riga[10].trim());
		String indirizzo = riga[11].trim();
		if (indirizzo.charAt(0) == '.')
			indirizzo = indirizzo.substring(1);
		if (indirizzo.charAt(indirizzo.length() - 1) == '.')
			indirizzo = indirizzo.substring(0, indirizzo.length() - 1);
		indirizzo = indirizzo.trim();
		destinatario.setIndirizzo(indirizzo);
		destinatario.setLocalita(riga[13]);
		destinatario.setCap(aggiustaCAP(riga[12]));
		destinatario.setProvincia(riga[14]);
		destinatario.setNazione("ITA");
		Indirizzo entity = daoIndirizzi.inserisci(destinatario);
		int id = entity != null ? entity.getId() : -1;
		return id;
	}

	private int ottieniMittente(String[] riga) {
		Indirizzo mittente = new Indirizzo();
		mittente.setRagioneSociale(riga[4].trim());
		String indirizzo = riga[5].trim();
		if (indirizzo.charAt(0) == '.')
			indirizzo = indirizzo.substring(1);
		if (indirizzo.charAt(indirizzo.length() - 1) == '.')
			indirizzo = indirizzo.substring(0, indirizzo.length() - 1);
		indirizzo = indirizzo.trim();
		mittente.setIndirizzo(indirizzo);
		mittente.setLocalita(riga[7]);
		mittente.setCap(aggiustaCAP(riga[6]));
		mittente.setProvincia(riga[8]);
		mittente.setNazione("ITA");
		Indirizzo entity = daoIndirizzi.inserisci(mittente);
		int id = entity != null ? entity.getId() : -1;
		return id;
	}
	
	private String aggiustaCAP(String cap) {
		for (int i = cap.length(); i < 5; i++) {
			cap = "0" + cap;
		}
		return cap;
	}

	private int inserisciNuovoDocumento(JoinCommessaCorriere codice, String[] riga) {
//		Documento documento = new Documento();
//		//Inserisci informazioni ordine
//		documento.setIdCommessa(codice.getCommessa());
//		String riferimento = riga[2];
//		if (riferimento == null || riferimento.isEmpty())
//			riferimento = "Report CSV TNT";
//		documento.setRiferimentoCliente(riferimento);
//		documento.setRiferimentoInterno(riferimento);
//		documento.setDataCreazione(ora);
//		documento.setTipo(TipoDocumento.ORDINE);
		int idOrdine = 2; //FIXME
		return idOrdine;
	}
	
	private boolean inserisciNuovaSpedizione(JoinCommessaCorriere codiceCliente, int idDocumento, TestaCorr vecchia, String[] riga) throws Exception {
		Spedizione spedizione = new Spedizione();
		//Inserisci informazioni spedizione
		spedizione.setAssicurazione(false);
		spedizione.setCodiceCliente(codiceCliente.getCodiceCliente());
		spedizione.setFatturazione(Fatturazione.FATTURABILE);
		int colli = 0;
		try {
			colli = Integer.parseInt(riga[17]);
		} catch (NumberFormatException e) {}
		spedizione.setColli(colli);
		boolean contrassegno = !(riga[22].trim().isEmpty());
		spedizione.setContrassegno(contrassegno);
		spedizione.setDataPartenza(sdf.parse(riga[1]));
		spedizione.setGiacenza(false);
		spedizione.setIdCommessa(codiceCliente.getCommessa());
		spedizione.setIdCorriere(codiceCliente.getCorriere());
		int idMittente = ottieniMittente(riga);
		int idDestinatario = ottieniDestinatario(riga);
		spedizione.setIndirizzoDestinazione(idDestinatario);
		spedizione.setIndirizzoPartenza(idMittente);
		spedizione.setRagioneSocialeDestinatario(riga[10].trim());
		spedizione.setIdDocumento(idDocumento);
		String letteraDiVettura = riga[0];
		String letteraDiVetturaOriginale = riga[0];
		//In caso di giacenza prendo la lettera di vettura originale.
		if (!letteraDiVetturaOriginale.isEmpty())
			letteraDiVettura = letteraDiVetturaOriginale;
		spedizione.setLetteraDiVettura(letteraDiVettura);
		String note = null; //riga.substring(418, 578).trim();
		spedizione.setNote(note);
		spedizione.setParticolarita(false);
		//Volume e peso
		double volume = trovaVolume(riga);
		if (volume == 0 && vecchia != null)
			spedizione.setVolume(vecchia.getVolume());
		else
			spedizione.setVolume(volume);
		double peso = trovaPeso(riga);
		if (peso == 0 && vecchia != null) {
			spedizione.setPeso(vecchia.getPeso());
		} else 
			spedizione.setPeso(peso);
		//Pezzi
		if (vecchia != null && vecchia.getPezzi() > 0) {
			spedizione.setDatiCompleti(true);
			spedizione.setPezzi(vecchia.getPezzi());
		} else {
			spedizione.setDatiCompleti(false);
			spedizione.setPezzi(0);
		}
		String riferimento = riga[2];
		spedizione.setRiferimentoCliente(riferimento);
		spedizione.setRiferimentoMittente(riferimento);
		spedizione.setServizio(getTipoServizio(riga[21]));
		spedizione.setStato("IMP");
		spedizione.setTipo(TipoSpedizione.ITALIA);
		Spedizione entity = daoSpedizioni.inserisci(spedizione);
		int idSpedizione = entity != null ? entity.getId() : -1;
		boolean inserimento = (idSpedizione != -1);
		if (inserimento && contrassegno) { //Solo se ha il contrassegno.
			inserisciNuovoContrassegno(idSpedizione, riga);
		}
		if (!inserimento)
			logger.error("Errore durante l'inserimento della spedizione!");
		return inserimento;
	}
	
	private boolean inserisciNuovoContrassegno(int idSpedizione, String[] riga) {
		SpedizioneContrassegno contrassegno = new SpedizioneContrassegno();
		//Inserisci informazioni contrassegno
		contrassegno.setIdSpedizione(idSpedizione);
		contrassegno.setAnnullato(false);
		contrassegno.setTipo("NA");
		contrassegno.setValuta("EUR");
		double valore;
		try {
			String value = riga[22];
			value = value.replace(',', '.');
			valore = Double.parseDouble(value);
		} catch (NumberFormatException e) {
			valore = 0;
			e.printStackTrace();
		}
		contrassegno.setValore(valore);
		SpedizioneContrassegno entity = daoContrassegni.inserisci(contrassegno);
		boolean inserimento = entity != null;
		if (!inserimento)
			logger.error("Errore nell'inserimento del contrassegno!");
		return inserimento;
	}

	public void importaEsito(String riga) {
				
	}
	
	private String getTipoServizio(String codificaTNT) {
		String servizio;
		switch (codificaTNT) {
			case "Express" : servizio = "DEF"; break;
			case "10:00 Express" : servizio = "O10"; break;
			case "12:00 Express" : servizio = "O12"; break;
			case "Economy Express" : servizio = "SUD"; break;
			default : servizio = "DEF";
		}
		return servizio;
	}
	
}
