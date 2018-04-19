package it.ltc.corrieri.tnt;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import it.ltc.database.model.centrale.Documento;
import it.ltc.database.model.centrale.Documento.TipoDocumento;
import it.ltc.database.model.centrale.Indirizzo;
import it.ltc.database.model.centrale.JoinCommessaCorriere;
import it.ltc.database.model.centrale.Spedizione;
import it.ltc.database.model.centrale.SpedizioneContrassegno;
import it.ltc.database.model.legacy.TestaCorr;

public class ImportatoreCSV {

	private static final Logger logger = Logger.getLogger(ImportatoreCSV.class);
	
	
	private static ImportatoreCSV instance;
	
	private final SimpleDateFormat sdf;
	private final Date ora;
	
	private ImportatoreCSV() {
		sdf = new SimpleDateFormat("dd/MM/yyyy");
		ora = new Date();
	}
	
	public static ImportatoreCSV getInstance() {
		if (instance == null) {
			instance = new ImportatoreCSV();
		}
		return instance;
	}
	
	public void importaSpedizione(String[] riga, TestaCorr vecchia) {
		try {
		String cc = riga[3];
		if (cc.length() == 7)
			cc = "0" + cc;
		JoinCommessaCorriere codiceCliente = recuperaCodiceCliente(cc);
		if (codiceCliente != null) {
			Spedizione trovata = recuperaSpedizione(codiceCliente, riga);
			boolean spedizionePresente = (trovata != null);
			if (spedizionePresente) {
				//Aggiorno la spedizione.
				aggiornaSpedizione(trovata, riga, vecchia);
			} else {
				//Inserisco la spedizione.
				int idOrdine = inserisciNuovoDocumento(codiceCliente, riga);
				inserisciNuovaSpedizione(codiceCliente, idOrdine, vecchia, riga);
			}
		} else {
			//Sonia non ha inserito il codice cliente.
			logger.error("Non è stato inserito il codice cliente: " + cc);
			logger.error("La spedizione persa:");
			logger.error(riga);
			if (vecchia != null && vecchia.getPezzi() > 0)
				logger.error("Numero pezzi: " + vecchia.getPezzi());
		}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Eccezione per: ");
			logger.error(riga);
			logger.error(e);
		}
		
	}
	
	private JoinCommessaCorriere recuperaCodiceCliente(String cc) {
		//Recupera codice dalla stringa cc - FIXME
		return null;
	}
	
	private void aggiornaSpedizione(Spedizione trovata, String[] riga, TestaCorr vecchia) {
		//Update info generali
		trovata.setServizio(getTipoServizio(riga[21]));
		//Peso
		double peso;
		try {
			String p = riga[19].trim();
			if (p.isEmpty())
				p = riga[18].trim();
			p = p.replace(',', '.');
			peso = Double.parseDouble(p);
		} catch (NumberFormatException e) {
			peso = 0;
		}
		if (peso > 0)
			trovata.setPeso(peso);
		//Volume
		double volume;
		try {
			String v = riga[20];
			v = v.replace(',', '.');
			volume = Double.parseDouble(v);
		} catch (NumberFormatException e) {
			volume = 0;
		}
		if (volume > 0)
			trovata.setVolume(volume);
		if (vecchia != null && vecchia.getPezzi() > 0) {
			trovata.setPezzi(vecchia.getPezzi());
			logger.info("Spedizione aggiornata dal sistema legacy.");
		} else {
			//Update spedizione - FIXME
			logger.info("Spedizione aggiornata.");
		}
		boolean update = false; //Update spedizione - FIXME
		if (!update)
			logger.error("Impossibile aggiornare la spedizione LDV: '" + trovata.getLetteraDiVettura() + "'");
	}

	private Spedizione recuperaSpedizione(JoinCommessaCorriere codiceCliente, String[] riga) {
		String letteraDiVettura = riga[0];
		String letteraDiVetturaOriginale = riga[0];
		//In caso di giacenza prendo la lettera di vettura originale.
		if (!letteraDiVetturaOriginale.isEmpty())
			letteraDiVettura = letteraDiVetturaOriginale;
		Spedizione filtro = new Spedizione();
		filtro.setIdCorriere(2);
		filtro.setCodiceCliente(codiceCliente.getCodiceCliente());
		filtro.setLetteraDiVettura(letteraDiVettura);
		Spedizione trovata = null; //FIXME
		return trovata;
	}

	private int ottieniDestinatario(String[] riga) {
		int id = 0;
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
		//Ricerca e Inserimento - FIXME
		return id;
	}

	private int ottieniMittente(String[] riga) {
		int id = 0;
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
		//Ricerca e Inserimento - FIXME
		return id;
	}
	
	private String aggiustaCAP(String cap) {
		for (int i = cap.length(); i < 5; i++) {
			cap = "0" + cap;
		}
		return cap;
	}

	private int inserisciNuovoDocumento(JoinCommessaCorriere codice, String[] riga) {
		Documento documento = new Documento();
		//Inserisci informazioni ordine
		documento.setIdCommessa(codice.getCommessa());
		String riferimento = riga[2];
		if (riferimento == null || riferimento.isEmpty())
			riferimento = "Report CSV TNT";
		documento.setRiferimentoCliente(riferimento);
		documento.setRiferimentoInterno(riferimento);
		documento.setDataCreazione(ora);
		documento.setTipo(TipoDocumento.ORDINE);
		int idOrdine = -1; //FIXME
		return idOrdine;
	}
	
	private boolean inserisciNuovaSpedizione(JoinCommessaCorriere codiceCliente, int idDocumento, TestaCorr vecchia, String[] riga) throws Exception {
		Spedizione spedizione = new Spedizione();
		//Inserisci informazioni spedizione
		spedizione.setAssicurazione(false);
		spedizione.setCodiceCliente(codiceCliente.getCodiceCliente());
		spedizione.setFatturazione(Spedizione.Fatturazione.FATTURABILE);
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
		double peso = 0;
		try {
			String p = riga[18];
			p = p.replace(',', '.');
			peso = Double.parseDouble(p);
		} catch (NumberFormatException e) {}
		if (peso == 0 && vecchia != null) {
			spedizione.setPeso(vecchia.getPeso());
		} else 
			spedizione.setPeso(peso);
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
		double volume = 0;
		try {
			String v = riga[20];
			v = v.replace(',', '.');
			volume = Double.parseDouble(v);
		} catch (NumberFormatException e) {}
		if (volume == 0 && vecchia != null)
			spedizione.setVolume(vecchia.getVolume());
		else
			spedizione.setVolume(volume);
		int idSpedizione = -1; //FIXME
		if (contrassegno) { //Solo se ha il contrassegno.
			inserisciNuovoContrassegno(idSpedizione, riga);
		}
		boolean inserimento = (idSpedizione != -1);
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
		boolean inserimento = false; //FIXME
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
