package it.ltc.corrieri.tnt;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.log4j.Logger;

import it.ltc.corrieri.tnt.model.DatiEsito;
import it.ltc.corrieri.tnt.model.DatiSpedizione;
import it.ltc.corrieri.tnt.model.FileTNT;
import it.ltc.database.dao.FactoryManager;
import it.ltc.database.model.centrale.Cap;
import it.ltc.database.model.centrale.CapPK;
import it.ltc.database.model.centrale.Commessa;
import it.ltc.database.model.centrale.Documento;
import it.ltc.database.model.centrale.Indirizzo;
import it.ltc.database.model.centrale.JoinCommessaCorriere;
import it.ltc.database.model.centrale.Nazione;
import it.ltc.database.model.centrale.Spedizione;
import it.ltc.database.model.centrale.Spedizione.TipoSpedizione;
import it.ltc.database.model.centrale.SpedizioneContrassegno;
import it.ltc.database.model.centrale.SpedizioneGiacenza;
import it.ltc.database.model.centrale.Tracking;
import it.ltc.database.model.centrale.TrackingPK;
import it.ltc.database.model.centrale.TrackingStatoCodificaCorriere;
import it.ltc.database.model.centrale.TrackingStatoCodificaCorrierePK;
import it.ltc.database.model.centrale.enumcondivise.Fatturazione;
import it.ltc.database.model.legacy.TestaCorr;
import it.ltc.utility.mail.Email;
import it.ltc.utility.mail.MailMan;

public class Importatore {
	
	private static final Logger logger = Logger.getLogger("Importatore");
	
	private static Importatore instance;
	
	private final EntityManager em;
	private final RecuperatoreDatiLegacy rdl;
	private final Set<String> spedizioniInRitardo;

	private Importatore() {
		ConfigurationUtility config = ConfigurationUtility.getInstance();
		em = FactoryManager.getInstance().getFactory(config.getPersistenceUnit()).createEntityManager();
		rdl = RecuperatoreDatiLegacy.getInstance();
		spedizioniInRitardo = new HashSet<>();
	}

	public static Importatore getInstance() {
		if (instance == null) {
			instance = new Importatore();
		}
		return instance;
	}
	
	public Set<String> getSpedizioniInRitardo() {
		return spedizioniInRitardo;
	}
	
	public boolean importaFile(FileTNT file) {
		boolean success;
		try {
			List<DatiSpedizione> datiSpedizioni = file.getDatiSpedizioni();
			logger.info("(Importatore) Il file contiene " + datiSpedizioni.size() + " spedizioni.");
			importaSpedizioni(datiSpedizioni);
			List<DatiEsito> datiEsiti = file.getDatiEsiti();
			logger.info("(Importatore) Il file contiene " + datiEsiti.size() + " esiti.");
			importaEsiti(datiEsiti);
			success = true;
		} catch (Exception e) {
			success = false;
			logger.error("Errore durante l'importazione del file: '" + file.getFile().getName() + "'");
		}
		return success;
	}
	
	public void importaSpedizioni(List<DatiSpedizione> datiSpedizioni) {
		Set<String> codiciNonInseriti = new HashSet<String>();
		for (DatiSpedizione spedizione : datiSpedizioni) {
			//Recupero il codice cliente.
			JoinCommessaCorriere codice = trovaCodiceCliente(spedizione);
			//Controllo che il codice cliente sia stato inserito a sistema.
			if (codice == null)	{
				codiciNonInseriti.add(spedizione.getCodiceCliente());
				continue;
			}
			//Controlle che sia stata abbinata una commessa al dato codice cliente.
			Commessa commessa = trovaCommessa(codice.getCommessa());
			if (commessa == null) {
				codiciNonInseriti.add(spedizione.getCodiceCliente());
				continue;
			}
			//Controllo se è una giacenza
			boolean giacenza = isGiacenza(spedizione);
			if (giacenza)
				importaDatiGiacenza(spedizione);
			else
				importaDatiSpedizione(spedizione, commessa);
		}
		//Se non sono stati inseriti tutti i codici cliente lancio una segnalazione di errore.
		if (codiciNonInseriti.size() > 0)
			inviaSegnalazioneCodiciMancanti(codiciNonInseriti);
	}
	
	private JoinCommessaCorriere trovaCodiceCliente(DatiSpedizione spedizione) {
		JoinCommessaCorriere codice = em.find(JoinCommessaCorriere.class, spedizione.getCodiceCliente());
		return codice;
	}
	
	private Commessa trovaCommessa(int idCommessa) {
		Commessa commessa = em.find(Commessa.class, idCommessa);
		return commessa;
	}
	
	private void inviaSegnalazioneCodiciMancanti(Set<String> codiciNonInseriti) {
		String oggetto = "Errori nell'importazione degli esiti TNT";
		String messaggio = "I seguenti codici clienti sono risultati mancanti e vanno inseriti in Logica:\r\n";
		for (String s : codiciNonInseriti) {
			messaggio += "\r\n- " + s;
		}
		inviaMail(oggetto, messaggio);
	}
	
	private void inviaSegnalazioneStatiMancanti(Set<String> statiNonInseriti) {
		String oggetto = "Errori nell'importazione degli esiti TNT";
		String messaggio = "I seguenti stati per gli esiti sono risultati mancanti e vanno inseriti in Logica:\r\n";
		for (String s : statiNonInseriti) {
			messaggio += "\r\n- " + s;
		}
		inviaMail(oggetto, messaggio);
	}
	
	private void inviaMail(String oggetto, String messaggio) {
		Email mail = new Email(oggetto, messaggio);
		List<String> destinatariDaAvvisare = new ArrayList<String>();
		destinatariDaAvvisare.add("damiano.bellucci@ltc-logistics.it");
		//destinatariDaAvvisare.add("support@ltc-logistics.it");
		String emailMittente = "sysinfo@ltc-logistics.it";
		String passwordMittente = "ltc10183";
		MailMan postino = new MailMan(emailMittente, passwordMittente, true);
		boolean invio = postino.invia(destinatariDaAvvisare, mail);
		if (invio)
			logger.info("mail di segnalazione dei codici mancanti inviata con successo.");
		else
			logger.error("non è stato possibile inviare la mail di segnalazione.");
	}
	
	private void importaDatiSpedizione(DatiSpedizione spedizione, Commessa commessa) {
		String nomeRisorsa = commessa.getNomeRisorsa();
		String riferimentoSpedizione = spedizione.getRiferimentoMittente();
		String destinatarioSpedizione = spedizione.getRagioneSocialeDestinatario();
		Date dataSpedizione = spedizione.getDataPartenzaSpedizione();
		TestaCorr spedizioneLegacy = rdl.recuperaTestata(nomeRisorsa, riferimentoSpedizione, destinatarioSpedizione, dataSpedizione);
		Spedizione spedizioneTrovata = recuperaSpedizione(spedizione);
		//Se la spedizione era già presente vado in aggiornamento, altrimenti la inserisco.
		if (spedizioneTrovata != null) {
			aggiornaSpedizione(spedizioneTrovata, spedizione, spedizioneLegacy);
		} else {
			inserisciSpedizione(spedizione, spedizioneLegacy);
		}
	}

	private boolean inserisciSpedizione(DatiSpedizione spedizione, TestaCorr spedizioneLegacy) {
		JoinCommessaCorriere codice = trovaCodiceCliente(spedizione);
		int idDocumento = getDocumento(codice, spedizione);
		boolean inserimento = inserisciNuovaSpedizione(codice, idDocumento, spedizioneLegacy, spedizione);
		return inserimento;
	}
	
	private int getDocumento(JoinCommessaCorriere codice, DatiSpedizione spedizione) {
		int idOrdine;
		//Provo a vedere se esiste già un documento che faccia riferimento a quella spedizione.
		//I criteri utilizzati sono la commessa, il riferimento e il tipo di documento (Ordine)
		CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Documento> criteria = cb.createQuery(Documento.class);
        Root<Documento> member = criteria.from(Documento.class);
        Predicate condizioneCommessa = cb.equal(member.get("idCommessa"), codice.getCommessa());
        Predicate condizioneRiferimento = cb.equal(member.get("riferimentoCliente"), spedizione.getRiferimentoMittente());
        Predicate condizioneTipoDocumento = cb.equal(member.get("tipo"), Documento.TipoDocumento.ORDINE);
        criteria.select(member).where(cb.and(condizioneCommessa, condizioneRiferimento, condizioneTipoDocumento)).orderBy(cb.desc(member.get("dataCreazione")));
        List<Documento> list = em.createQuery(criteria).setMaxResults(1).getResultList();
        //Controllo se ho almeno un risultato, in quel caso lo prendo altrimenti inserisco un nuovo documento.
        if (list.isEmpty()) {
        	Documento nuovoDocumento = new Documento();
        	nuovoDocumento.setDataCreazione(new Date());
        	nuovoDocumento.setIdCommessa(codice.getCommessa());
        	nuovoDocumento.setRiferimentoCliente(spedizione.getRiferimentoMittente());
        	nuovoDocumento.setRiferimentoInterno(spedizione.getRiferimentoMittente());
        	nuovoDocumento.setTipo(Documento.TipoDocumento.ORDINE);
        	//Inserisco il nuovo documento nel DB
        	EntityTransaction transaction = em.getTransaction();
        	try {
        		transaction.begin();
            	em.persist(nuovoDocumento);
            	transaction.commit();
        	} catch (Exception e) {
        		transaction.rollback();
        		printStackTrace(e);
        		throw new RuntimeException("Impossibile inserire il nuovo documento");
        	}
        	idOrdine = nuovoDocumento.getId();
        } else {
        	idOrdine = list.get(0).getId();
        }
		return idOrdine;
	}
	
	private String getTipoServizio(String codificaTNT) {
		String servizio;
		switch (codificaTNT) {
			case "A" : servizio = "DEF"; break;
			case "D" : servizio = "O10"; break;
			case "T" : servizio = "O12"; break;
			case "N" : servizio = "SUD"; break;
			default : servizio = "DEF";
		}
		return servizio;
	}
	
	private boolean inserisciNuovaSpedizione(JoinCommessaCorriere codiceCliente, int idDocumento, TestaCorr vecchia, DatiSpedizione dati) {
		Spedizione spedizione = new Spedizione();
		//Inserisci informazioni spedizione
		spedizione.setIdDocumento(idDocumento);
		spedizione.setAssicurazione(false);
		spedizione.setCodiceCliente(codiceCliente.getCodiceCliente());
		spedizione.setColli(dati.getNumeroColli());
		boolean contrassegno = dati.getValoreContrassegno() > 0;
		spedizione.setContrassegno(contrassegno);
		spedizione.setDataPartenza(dati.getDataPartenzaSpedizione());
		spedizione.setGiacenza(false);
		spedizione.setIdCommessa(codiceCliente.getCommessa());
		spedizione.setIdCorriere(codiceCliente.getCorriere());
		Indirizzo mittente = recuperaMittente(dati);
		Indirizzo destinatario = recuperaDestinatario(dati);
		spedizione.setRagioneSocialeDestinatario(destinatario.getRagioneSociale());
		spedizione.setIndirizzoDestinazione(destinatario.getId());
		spedizione.setIndirizzoPartenza(mittente.getId());
		String letteraDiVettura = dati.getLetteraDiVettura();
		String letteraDiVetturaOriginale = dati.getLetteraDiVetturaOriginaria();
		//In caso di giacenza prendo la lettera di vettura originale.
		if (!letteraDiVetturaOriginale.isEmpty())
			letteraDiVettura = letteraDiVetturaOriginale;
		spedizione.setLetteraDiVettura(letteraDiVettura);
		spedizione.setNote(dati.getNote());
		spedizione.setParticolarita(false);
		//Peso
		double peso = dati.getPeso();
		//Fix, per TNT se il peso riscontrato è uguale a quello dichiarato allora non viene inserito.
		if (peso == 0)
			peso = dati.getPesoDichiarato();
		if (peso == 0 && vecchia != null) {
			peso = vecchia.getPeso();
		}
		//Controllo ulteriore su valori "impossibili"
		if (peso < 0 || peso > 9999)
			peso = 0;
		spedizione.setPeso(peso);
		//Pezzi
		int pezzi = vecchia != null ? vecchia.getPezzi() : 0;
		spedizione.setPezzi(pezzi);
		//Volume
		double volume = dati.getVolume();
		if (volume == 0 && vecchia != null) {
			volume = vecchia.getVolume();
		}
		//Controllo ulteriore su valori "impossibili"
		if (volume < 0 || volume > 999)
			volume = 0;
		spedizione.setVolume(volume);
		//Tutto il resto
		String riferimento = dati.getRiferimentoMittente();
		spedizione.setRiferimentoCliente(riferimento);
		spedizione.setRiferimentoMittente(riferimento);
		spedizione.setServizio(getTipoServizio(dati.getTipoServizio()));
		spedizione.setStato("IMP");
		spedizione.setFatturazione(Fatturazione.IN_DEFINIZIONE);
		TipoSpedizione tipoSpedizione = getTipoSpedizione(destinatario);
		spedizione.setTipo(tipoSpedizione);
		//Se ho trovato corrispondenza nei sistemi legacy lo indico
		if (vecchia != null)
			spedizione.setDatiCompleti(true);
		//inserisco la spedizione a sistema
		EntityTransaction transaction = em.getTransaction();
		try {
			transaction.begin();
			em.persist(spedizione);
			transaction.commit();
		} catch (Exception e) {
			printStackTrace(e);
			transaction.rollback();
			throw new RuntimeException("Impossibile inserire la spedizione: '" + spedizione.getLetteraDiVettura() + "'");
		}
		int idSpedizione = spedizione.getId();
		if (contrassegno) { //Solo se ha il contrassegno.
			inserisciContrassegno(idSpedizione, dati);
		}
		boolean inserimento = (idSpedizione != -1);
		String messaggio = "Inserimento spedizione LDV: '" + spedizione.getLetteraDiVettura() + "', ";
		if (inserimento)
			messaggio += "successo!";
		else
			messaggio += "fallito, impossibile scrivere nel DB.";
		logger.info(messaggio);
		return inserimento;
	}
	
	private TipoSpedizione getTipoSpedizione(Indirizzo destinatario) {
		TipoSpedizione tipo;
		String iso = destinatario.getNazione();
		if (iso == null || iso.isEmpty() || "ITA".equals(iso)) {
			tipo = TipoSpedizione.ITALIA;
		} else {
			Nazione n = em.find(Nazione.class, iso);
			if (n != null) {
				tipo = n.getUe() ? TipoSpedizione.UE : TipoSpedizione.EXTRA_UE;
			} else {
				tipo = TipoSpedizione.UE;
			}
		}
		return tipo;
	}

	private void inserisciContrassegno(int idSpedizione, DatiSpedizione spedizione) {
		SpedizioneContrassegno contrassegno = new SpedizioneContrassegno();
		contrassegno.setIdSpedizione(idSpedizione);
		contrassegno.setAnnullato(false);
		contrassegno.setTipo("NA");
		contrassegno.setValuta("EUR");
		double valore = spedizione.getValoreContrassegno();
		contrassegno.setValore(valore);
		EntityTransaction transaction = em.getTransaction();
		try {
			transaction.begin();
			em.persist(contrassegno);
			transaction.commit();
		} catch (Exception e) {
			printStackTrace(e);
			transaction.rollback();
			throw new RuntimeException("Impossibile inserire il contrassegno per la spedizione: '" + spedizione.getLetteraDiVettura() + "'");
		}
	}

	/**
	 * Importa i dati della giacenza a partire da quelli passati come argomento.
	 * @param spedizione i dati sulla spedizione.
	 */
	private void importaDatiGiacenza(DatiSpedizione spedizione) {
		String ldv = spedizione.getLetteraDiVettura();
		String ldvo = spedizione.getLetteraDiVetturaOriginaria();
		//Controllo se ho già la giacenza pronta
		SpedizioneGiacenza trovata = recuperaGiacenza(ldv, ldvo);
		//Se l'ho trovata sono a posto, non devo fare nulla.
		//Se non l'ho trovata provo a vedere se è stata solo inserita ma mai aggiornata
		if (trovata == null) {
			trovata = recuperaGiacenza(ldvo, ldvo);
			//Se l'ho trovata provo ad aggiornare con la giusta ldv altrimenti mi manca del tutto quindi la inserisco.
			if (trovata != null) {
				aggiornaGiacenza(trovata, spedizione);
			} else {
				inserisciGiacenza(spedizione);
			}
		}
	}

	/**
	 * Aggiorna la giacenza con la giusta lettera di vettura e indirizzi.
	 * @param trovata
	 * @param spedizione
	 */
	private void aggiornaGiacenza(SpedizioneGiacenza trovata, DatiSpedizione spedizione) {
		String ldv = spedizione.getLetteraDiVettura();
		Indirizzo d = recuperaDestinatario(spedizione);
		Indirizzo m = recuperaMittente(spedizione);
		trovata.setLetteraDiVettura(ldv);
		trovata.setIdDestinatario(d.getId());
		trovata.setIdMittente(m.getId());
		EntityTransaction transaction = em.getTransaction();
		logger.info("Giacenza '" + trovata.getLetteraDiVettura() +  "' aggiornata con la giusta ldv e indirizzi!");
		try {
			transaction.begin();
			em.merge(trovata);
			transaction.commit();
		} catch (Exception e) {
			printStackTrace(e);
			if (transaction != null && transaction.isActive())
				transaction.rollback();
			throw new RuntimeException("Impossibile aggiornare la giacenza '" + trovata.getLetteraDiVettura() + "'");
		}
		
	}
	
	/**
	 * Aggiorna la giacenza con la giusta data di apertura.
	 * @param trovata
	 * @param spedizione
	 */
	private void aggiornaGiacenza(SpedizioneGiacenza trovata, DatiEsito esito) {
		trovata.setDataApertura(esito.getDataVariazione());
		EntityTransaction transaction = em.getTransaction();
		try {
			transaction.begin();
			em.merge(trovata);
			transaction.commit();
		} catch (Exception e) {
			printStackTrace(e);
			if (transaction != null && transaction.isActive())
				transaction.rollback();
			throw new RuntimeException("Impossibile aggiornare la giacenza '" + trovata.getLetteraDiVettura() + "'");
		}
		
	}

	/**
	 * Inserisce a sistema una giacenza con le informazioni contenute nei dati della spedizione.
	 * @param spedizione
	 * @return
	 */
	private void inserisciGiacenza(DatiSpedizione spedizione) {
		Spedizione s = recuperaSpedizione(spedizione);
		if (s != null) {
			Indirizzo d = recuperaDestinatario(spedizione);
			Indirizzo m = recuperaMittente(spedizione);
			SpedizioneGiacenza giacenza = new SpedizioneGiacenza();
			giacenza.setDataApertura(spedizione.getDataPartenzaSpedizione());
			giacenza.setFatturazione(Fatturazione.IN_DEFINIZIONE);
			giacenza.setLetteraDiVettura(spedizione.getLetteraDiVettura());
			giacenza.setLetteraDiVetturaOriginale(spedizione.getLetteraDiVetturaOriginaria());
			giacenza.setIdDestinatario(d.getId());
			giacenza.setIdMittente(m.getId());
			giacenza.setIdDocumento(s.getIdDocumento());
			giacenza.setIdSpedizione(s.getId());
			giacenza.setIdCommessa(s.getIdCommessa());
			EntityTransaction transaction = em.getTransaction();
			try {
				transaction.begin();
				em.persist(giacenza);
				transaction.commit();
				logger.info("inserita nuova giacenza da spedizione, LDV: '" + spedizione.getLetteraDiVettura() + "', spedizione LDV: '" + spedizione.getLetteraDiVetturaOriginaria() + "'");
			} catch (Exception e) {
				logger.error("Impossibile inserire le informazioni di giacenza per LDV '" + spedizione.getLetteraDiVetturaOriginaria() + "'");
				printStackTrace(e);
				if (transaction != null && transaction.isActive())
					transaction.rollback();
				throw new RuntimeException("Impossibile inserire la giacenza: '" + s.getLetteraDiVettura() + "'");
			}			
		} else {
			logger.error("La spedizione LDV '" + spedizione.getLetteraDiVetturaOriginaria() + "' non è mai stata inserita a sistema.");
		}
	}
	
	/**
	 * Inserisce a sistema una giacenza con le informazioni contenute nei dati dell'esito.
	 * @param spedizione
	 * @return
	 */
	private SpedizioneGiacenza inserisciGiacenza(DatiEsito esito, Spedizione spedizione) {
		String letteraDiVettura = esito.getLetteraDiVettura();
		SpedizioneGiacenza giacenza = new SpedizioneGiacenza();
		int idDestinatario = spedizione.getIndirizzoDestinazione();
		int idMittente = spedizione.getIndirizzoPartenza();
		giacenza.setDataApertura(esito.getDataVariazione());
		giacenza.setFatturazione(Fatturazione.IN_DEFINIZIONE);
		giacenza.setLetteraDiVettura(letteraDiVettura);
		giacenza.setLetteraDiVetturaOriginale(spedizione.getLetteraDiVettura());
		giacenza.setIdDestinatario(idDestinatario);
		giacenza.setIdMittente(idMittente);
		giacenza.setIdDocumento(spedizione.getIdDocumento());
		giacenza.setIdSpedizione(spedizione.getId());
		giacenza.setIdCommessa(spedizione.getIdCommessa());
		EntityTransaction transaction = em.getTransaction();
		try {
			transaction.begin();
			em.persist(giacenza);
			transaction.commit();
			logger.info("inserita nuova giacenza da esito, LDV: '" + letteraDiVettura + "', spedizione LDV: '" + spedizione.getLetteraDiVettura() + "'");
		} catch (Exception e) {
			logger.error("Impossibile inserire la giacenza con LDV originale '" + letteraDiVettura + "'");
			printStackTrace(e);
			if (transaction != null && transaction.isActive())
				transaction.rollback();
			//throw new RuntimeException("Impossibile inserire la giacenza '" + letteraDiVettura + "'");
		}			
		return giacenza;
	}

	/**
	 * Ottiene un'oggetto indirizzo che rappresenta il mittente a partire dai dati della spedizione.
	 * @param spedizione
	 * @return
	 */
	private Indirizzo recuperaMittente(DatiSpedizione spedizione) {
		String ragioneSociale = spedizione.getRagioneSocialeMittente();
		String provincia = spedizione.getProvinciaMittente();
		String via = spedizione.getIndirizzoMittente();
		String localita = spedizione.getLocalitaMittente();
		String cap = spedizione.getCapMittente();
		Indirizzo indirizzo = recuperaIndirizzo(ragioneSociale, provincia, via, localita, cap);
		return indirizzo;
	}

	/**
	 * Ottiene un'oggetto indirizzo che rappresenta il destinatario a partire dai dati della spedizione.
	 * @param spedizione
	 * @return
	 */
	private Indirizzo recuperaDestinatario(DatiSpedizione spedizione) {
		String ragioneSociale = spedizione.getRagioneSocialeDestinatario();
		String provincia = spedizione.getProvinciaDestinatario();
		String via = spedizione.getIndirizzoDestinatario();
		String localita = spedizione.getLocalitaDestinatario();
		String cap = spedizione.getCapDestinatario();
		Indirizzo indirizzo = recuperaIndirizzo(ragioneSociale, provincia, via, localita, cap);
		return indirizzo;
	}
	
	private Indirizzo recuperaIndirizzo(String ragioneSociale, String provincia, String via, String localita, String cap) {
		Indirizzo indirizzo;
		CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Indirizzo> criteria = cb.createQuery(Indirizzo.class);
        Root<Indirizzo> member = criteria.from(Indirizzo.class);
        Predicate condizioneRagioneSociale = cb.equal(member.get("ragioneSociale"), ragioneSociale);
        Predicate condizioneProvincia = cb.equal(member.get("provincia"), provincia);
        Predicate condizioneIndirizzo = cb.equal(member.get("indirizzo"), via);
        Predicate condizioneLocalita = cb.equal(member.get("localita"), localita);
        Predicate condizioneCAP = cb.equal(member.get("cap"), cap);
        criteria.select(member).where(cb.and(condizioneRagioneSociale, condizioneProvincia, condizioneIndirizzo, condizioneLocalita, condizioneCAP));
        List<Indirizzo> list = em.createQuery(criteria).setMaxResults(1).getResultList();
		//Se l'ho trovato gli restituisco quello altrimenti lo inserisco.
        if (list.isEmpty()) {
        	indirizzo = new Indirizzo();
        	indirizzo.setCap(cap);
        	indirizzo.setIndirizzo(via);
        	indirizzo.setLocalita(localita);
        	indirizzo.setNazione("ITA");
        	indirizzo.setProvincia(provincia);
        	indirizzo.setRagioneSociale(ragioneSociale);
        	EntityTransaction transaction = em.getTransaction();
        	try {
        		transaction.begin();
            	em.persist(indirizzo);
            	transaction.commit();
        	} catch (Exception e) {
        		printStackTrace(e);
        		transaction.rollback();
        		throw new RuntimeException("Impossibile inserire il nuovo indirizzo.");
        	}
		} else {
			indirizzo = list.get(0);
		}
		return indirizzo;
	}

	/**
	 * Se la spedizione ha due lettere di vettura e quella originaria non è vuota allora si tratta di una giacenza.
	 * @param spedizione
	 * @return
	 */
	private boolean isGiacenza(DatiSpedizione spedizione) {
		String ldv = spedizione.getLetteraDiVetturaOriginaria();
		boolean giacenza = !(ldv == null || ldv.isEmpty());
		return giacenza;
	}

	/**
	 * Tenta di aggiornare lo stato di una spedizione:
	 * - se lo stato è riconducibile ad una presa in carico della merce da parte del corriere allora la rende fatturabile.
	 * Tenta di aggiornare le informazioni di una spedizione a partire dalle info legacy:
	 * - pezzi
	 * @param spedizioneTrovata
	 * @param spedizione
	 * @param spedizioneLegacy
	 * @return
	 */
	private void aggiornaSpedizione(Spedizione spedizioneTrovata, DatiSpedizione spedizione, TestaCorr spedizioneLegacy) {
		// Aggiornamento dello stato
		String messaggioStato = "Aggiornamento stato spedizione '" + spedizioneTrovata.getLetteraDiVettura() + "' ";
		String statoCorriere = spedizione.getStatoAvanzamento();
		TrackingStatoCodificaCorriere codifica = trovaCodifica(statoCorriere);
		if (codifica != null) {
			String stato = codifica.getStato();
			spedizioneTrovata.setStato(stato);
			//Se la spedizione è stata presa in carico dal corriere controllo lo stato di fatturazione.
			if (stato.equals("S01") || stato.equals("S02") || stato.equals("S03") || stato.equals("S04")) {
				//Se la spedizione è ancora segnata come non fatturabile allora la rendo fatturabile.
				if (spedizioneTrovata.getFatturazione() == Fatturazione.IN_DEFINIZIONE) {
					spedizioneTrovata.setFatturazione(Fatturazione.FATTURABILE);
				}
			}
			EntityTransaction transaction = em.getTransaction();
			try {
				transaction.begin();
				em.merge(spedizioneTrovata);
				transaction.commit();
			} catch (Exception e) {
				printStackTrace(e);
				transaction.rollback();
				throw new RuntimeException("Impossibile aggiornare la spedizione: '" + spedizioneTrovata.getLetteraDiVettura() + "'");
			}
			messaggioStato += "a '" + statoCorriere + "' (" + codifica.getStato() + ") successo!";
		} else {
			messaggioStato += "fallito, impossibile recuperare lo stato associato a: '" + statoCorriere + "'";
		}
		logger.info(messaggioStato);
		//Aggiornamento dei pezzi, se necessario.
		if (!spedizioneTrovata.getDatiCompleti()) {
			String messaggio = "Aggiornamento dati spedizione '" + spedizioneTrovata.getLetteraDiVettura() + "' : ";
			//Se li ho trovati dai sistemi legacy procedo all'aggiornamento.
			if (spedizioneLegacy != null) {
				spedizioneTrovata.setPezzi(spedizioneLegacy.getPezzi());
				spedizioneTrovata.setDatiCompleti(true);
				EntityTransaction transaction = em.getTransaction();
				try {
					transaction.begin();
					em.merge(spedizioneTrovata);
					transaction.commit();
				} catch (Exception e) {
					printStackTrace(e);
					transaction.rollback();
					throw new RuntimeException("Impossibile aggiornare la spedizione: '" + spedizioneTrovata.getLetteraDiVettura() + "'");
				}
				messaggio += "successo!";
			} else { //Altrimenti notifico il fallimento.
				messaggio += "fallito, non è stato possibile recuperarla dal sistema legacy.";
			}
			logger.info(messaggio);
		}
	}
	
	private TrackingStatoCodificaCorriere trovaCodifica(String statoCorriere) {
		TrackingStatoCodificaCorrierePK pk = new TrackingStatoCodificaCorrierePK();
		pk.setCodificaCorriere(statoCorriere);
		pk.setIdCorriere(2);
		TrackingStatoCodificaCorriere codifica = em.find(TrackingStatoCodificaCorriere.class, pk);
		return codifica;
	}
	
	private SpedizioneGiacenza recuperaGiacenza(String letteraDiVettura, String letteraDiVetturaOriginale) {	
		CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<SpedizioneGiacenza> criteria = cb.createQuery(SpedizioneGiacenza.class);
        Root<SpedizioneGiacenza> member = criteria.from(SpedizioneGiacenza.class);
        Predicate condizioneLDV = cb.equal(member.get("letteraDiVettura"), letteraDiVettura);
        if (letteraDiVetturaOriginale != null && !letteraDiVetturaOriginale.isEmpty()) {
        	Predicate condizioneLDVOriginale = cb.equal(member.get("letteraDiVetturaOriginale"), letteraDiVetturaOriginale);
        	criteria.select(member).where(cb.and(condizioneLDV, condizioneLDVOriginale));
        } else {
        	criteria.select(member).where(condizioneLDV);
        }
        List<SpedizioneGiacenza> list = em.createQuery(criteria).setMaxResults(1).getResultList();
        SpedizioneGiacenza trovata = list.isEmpty() ? null : list.get(0);
		return trovata;
	}
	
	private Spedizione recuperaSpedizione(DatiSpedizione spedizione) {
		String codiceCliente = spedizione.getCodiceCliente();
		String letteraDiVettura = spedizione.getLetteraDiVettura();
		String letteraDiVetturaOriginale = spedizione.getLetteraDiVetturaOriginaria();
		// In caso di giacenza prendo la lettera di vettura originale.
		if (!letteraDiVetturaOriginale.isEmpty())
			letteraDiVettura = letteraDiVetturaOriginale;
		Spedizione trovata = recuperaSpedizione(codiceCliente, letteraDiVettura);
		return trovata;
	}

	private Spedizione recuperaSpedizione(String codiceCliente, String letteraDiVettura) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Spedizione> criteria = cb.createQuery(Spedizione.class);
        Root<Spedizione> member = criteria.from(Spedizione.class);
        Predicate condizioneCodice = cb.equal(member.get("codiceCliente"), codiceCliente);
        Predicate condizioneCorriere = cb.equal(member.get("idCorriere"), 2);
        Predicate condizioneLDV = cb.equal(member.get("letteraDiVettura"), letteraDiVettura);
        criteria.select(member).where(cb.and(condizioneCodice, condizioneCorriere, condizioneLDV));
        List<Spedizione> list = em.createQuery(criteria).setMaxResults(1).getResultList();
        Spedizione trovata = list.isEmpty() ? null : list.get(0);
		return trovata;
	}

	public int importaEsiti(List<DatiEsito> datiEsiti) {
		Set<String> statiNonCodificati = new HashSet<String>();
		int importazioni = 0;
		for (DatiEsito esito : datiEsiti) {
			String letteraDiVettura = esito.getLetteraDiVettura();
			String codiceCliente = esito.getCodiceCliente();
			Spedizione spedizione = recuperaSpedizione(codiceCliente, letteraDiVettura);
			SpedizioneGiacenza giacenza = recuperaGiacenza(letteraDiVettura, null);
			//Se non ho trovato la spedizione collegata controllo se si tratta di una giacenza
			//Se effettivamente è una giacenza recupero la spedizione collegata
			if (spedizione == null && giacenza != null) {
				spedizione = em.find(Spedizione.class, giacenza.getIdSpedizione());
			}
			//Se sono riuscito a trovare la spedizione allora vado ad aggiornare i dati ad essa relativi
			//inserire un evento di tracking ed eventualmente aggiornare la giacenza
			if (spedizione != null) {
				Date data = esito.getDataVariazione();
				String stato = esito.getStato();
				TrackingStatoCodificaCorriere codificaStato = trovaCodifica(stato);
				//Se non sono riuscito a recuperare lo stato lo aggiungo alla lista e lo rimpiazzo con uno fittizio.
				if (codificaStato == null) {
					statiNonCodificati.add(stato);
					codificaStato = trovaCodifica("XXX");
					codificaStato.setDescrizione("Stato Evento TNT non codificato: " + stato);
				}
				//Verifico se è arrivata a destinazione in tempo
				if (codificaStato.getStato().equals("S03")) {
					Indirizzo destinazione = em.find(Indirizzo.class, spedizione.getIndirizzoDestinazione());
					Cap cap = getInfoCap(destinazione.getCap(), destinazione.getLocalita());
					Date scadenza = getScadenza(cap, spedizione.getDataPartenza(), spedizione.getServizio());
					if (scadenza.before(data)) {
						codificaStato = trovaCodifica("RIT");
						spedizione.setInRitardo(true);
						spedizioniInRitardo.add(spedizione.getLetteraDiVettura());
					}
				}
				//Inoltre se era in giacenza allora la chiudo
				if (giacenza != null && isFinale(codificaStato.getStato())) {
					giacenza.setDataChiusura(data);
					giacenza.setFatturazione(Fatturazione.FATTURABILE);
					EntityTransaction transaction = em.getTransaction();
					try {
						transaction.begin();
						em.persist(giacenza);
						transaction.commit();
					} catch (Exception e) {
						printStackTrace(e);
						transaction.rollback();
						throw new RuntimeException("Impossibile aggiornare la giacenza: '" + giacenza.getLetteraDiVettura() + "'");
					}
					logger.info("Chiusura giacenza '" + letteraDiVettura + "'");
				}
				//Appuntamento, se presente
				Date dataAppuntamento = esito.getDataAppuntamento();
				if (dataAppuntamento != null)
					spedizione.setParticolarita(true);
				//Aggiorno i valori per la spedizione, se necessario
				double peso = esito.getPeso();
				if (peso > 0)
					spedizione.setPeso(peso);
				double volume = esito.getVolume();
				if (volume > 0)
					spedizione.setVolume(volume);
				//Aggiornamento dello stato della spedizione
				spedizione.setStato(codificaStato.getStato());
				//Fatturabilità
				if (codificaStato.getStato().equals("S01") || codificaStato.getStato().equals("S02") || codificaStato.getStato().equals("S03") || codificaStato.getStato().equals("S04")) {
					if (spedizione.getFatturazione() == Fatturazione.IN_DEFINIZIONE)
						spedizione.setFatturazione(Fatturazione.FATTURABILE);
				}
				//Giacenza
				if (codificaStato.getStato().equals("C01")) {
					if (giacenza == null || !giacenza.getLetteraDiVettura().equals(esito.getLetteraDiVettura()))
						inserisciGiacenza(esito, spedizione);
					else if (giacenza.getDataChiusura() == null) //FIX: vado ad aggiornare la data di apertura solo se non è ancora chiusa, ci sono casi in cui si riapre più volte e potrebbe prenderlo in maniera errata.
						aggiornaGiacenza(giacenza, esito);
					spedizione.setGiacenza(true);
				}
				EntityTransaction transaction = em.getTransaction();
				try {
					transaction.begin();
					em.merge(spedizione);
					transaction.commit();
				} catch (Exception e) {
					printStackTrace(e);
					if (transaction != null && transaction.isActive())
						transaction.rollback();
					throw new RuntimeException("Impossibile aggiornare la spedizione: '" + spedizione.getLetteraDiVettura() + "'");
				}
				//Inserisco il tracking
				String note = esito.getNoteEvento();
				String filiale = esito.getFilialeTNT();
				String descrizione = codificaStato.getDescrizione() + " " + filiale;
				if (!note.isEmpty())
					descrizione += " - " + note;
				Tracking tracking = new Tracking();
				TrackingPK pk = new TrackingPK();
				pk.setIdSpedizione(spedizione.getId());
				pk.setStato(codificaStato.getStato());
				pk.setData(data);
				tracking.setId(pk);
				tracking.setDescrizione(descrizione);
				EntityTransaction transaction2 = em.getTransaction();
				try {
					transaction2.begin();
					em.merge(tracking); //Uso merge invece di persist perchè potrebbe già esistere, vengono duplicati spesso.
					transaction2.commit();
				} catch (Exception e) {
					printStackTrace(e);
					if (transaction2 != null && transaction2.isActive())
						transaction2.rollback();
					//throw new RuntimeException("Impossibile inserire il nuovo tracking: '" + tracking.getId() + "'");
				}
				logger.info("Tracking inserito e spedizione aggiornata.");
			} else {
				logger.error("Spedizione non trovata, LDV: '" + esito.getLetteraDiVettura() + "', codice cliente: '" + esito.getCodiceCliente() + "'");
			}
		}
		if (statiNonCodificati.size() > 0)
			inviaSegnalazioneStatiMancanti(statiNonCodificati);
		return importazioni;
	}
	
	/**
	 * Recupera un cap con il cap e localita' specificate.
	 * @param cap il valore stinga del cap
	 * @param localita il nome della localita'
	 * @return l'entity Cap trovata o null se non ci sono corrispondenze.
	 */
	private Cap getInfoCap(String cap, String localita) {
		CapPK pk = new CapPK();
		pk.setCap(cap);
		pk.setLocalita(localita);
		Cap c = em.find(Cap.class, pk);
		return c;
	}
	
	public boolean isFinale(String stato) {
		boolean consegnato = "S03".equals(stato);
		boolean consegnatoInRitardo = "S04".equals(stato);
		boolean annullato = "C06".equals(stato);
		boolean finale = consegnato || consegnatoInRitardo || annullato;
		return finale;
	}

	private Date getScadenza(Cap cap, Date partenza, String servizio) {
		long tempoMassimo = 1;
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(partenza);
		calendar.set(Calendar.HOUR_OF_DAY, 20);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		if (servizio.equals("O10")) {
			calendar.set(Calendar.HOUR_OF_DAY, 10);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
		} else if (servizio.equals("O12")) {
			calendar.set(Calendar.HOUR_OF_DAY, 12);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
		} else if (cap != null && cap.getBrtDisagiate()) {
			tempoMassimo += 1;
		}
		for (int i = 0; i < tempoMassimo; i++) {
			int giornoSettimana = calendar.get(Calendar.DAY_OF_WEEK);
			int giornoAnno = calendar.get(Calendar.DAY_OF_YEAR);
			int bisestile = 0;
			if (calendar.isLeapYear(calendar.get(Calendar.YEAR)))
				bisestile += 1;
			if (giornoSettimana == Calendar.SATURDAY || giornoSettimana == Calendar.SUNDAY) {
				tempoMassimo += 1;
			} else if (
				giornoAnno == 1 || //1 Gennaio, Capodanno
				giornoAnno == 6 || //6 Gennaio, Epifania
				giornoAnno == (115 + bisestile) || //25 Aprile, festa della liberazione
				giornoAnno == (121 + bisestile) || //1 Maggio, festa dei lavoratori
				giornoAnno == (153 + bisestile) || //2 Giugno, festa della repubblica
				giornoAnno == (227 + bisestile) || //15 Agosto, Ferragosto
				giornoAnno == (305 + bisestile) || //1 Novembre, tutti i santi
				giornoAnno == (342 + bisestile) || //8 Dicembre, Madonna
				giornoAnno == (359 + bisestile) || //25 Dicembre, Natale
				giornoAnno == (360 + bisestile) //26 Dicembre, s. Stefano
			) {
				tempoMassimo += 1;
			}
			calendar.add(Calendar.DAY_OF_YEAR, 1);
		}
		Date scadenza = calendar.getTime();
		return scadenza;
	}
	
	private void printStackTrace(Exception e) {
		logger.error(e);
		for (StackTraceElement element: e.getStackTrace())
			logger.error(element);
	}

}
