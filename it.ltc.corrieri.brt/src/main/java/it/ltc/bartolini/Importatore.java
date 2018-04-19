package it.ltc.bartolini;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.log4j.Logger;

import it.ltc.bartolini.model.costanti.Nazioni;
import it.ltc.bartolini.model.costanti.TipiServizio;
import it.ltc.database.dao.FactoryManager;
import it.ltc.database.model.centrale.Cap;
import it.ltc.database.model.centrale.Commessa;
import it.ltc.database.model.centrale.Documento;
import it.ltc.database.model.centrale.Indirizzo;
import it.ltc.database.model.centrale.JoinCommessaCorriere;
import it.ltc.database.model.centrale.Sede;
import it.ltc.database.model.centrale.Spedizione;
import it.ltc.utility.mail.Email;
import it.ltc.utility.mail.MailMan;

public abstract class Importatore {
	
	private final Logger logger = Logger.getLogger("Importatore");
	
	protected final ConfigurationUtility config;
	protected final String persistenceUnitName;
	protected final HashMap<String, JoinCommessaCorriere> codiciCliente;
	protected final HashMap<Integer, Commessa> commesse;
	
	protected Importatore() {
		config = ConfigurationUtility.getInstance();
		persistenceUnitName = config.getDb();
		codiciCliente = new HashMap<>();
		commesse = new HashMap<>();
	}
	
	/**
	 * I case ripetivi sono stati inseriti lo stesso per completezza rispecchiando le specifiche Bartolini
	 * @param nazione come trovata sul file di ritorno Bartolini
	 * @return Il codice ISO3 della nazione
	 */
	protected String getNazione(String nazione) {
		try {
			Nazioni n = Nazioni.valueOf(nazione);
			nazione = n.getCodiceISO();
		} catch(Exception e) {
			nazione = "ITA";
		}
		return nazione;
	}
	
	protected String aggiustaRiferimento(String riferimento) {
		try {
			Integer numero = Integer.parseInt(riferimento);
			riferimento = numero.toString();
		} catch (NumberFormatException e) {}
		return riferimento;
	}
	
	protected String getTipoServizio(String codificaBRT) {
		String servizio;
		try {
			TipiServizio ts = TipiServizio.valueOf(codificaBRT);
			servizio = ts.getCodificaInterna();
		} catch (Exception e) {
			servizio = "DEF";
		}
		return servizio;
	}
	
	protected JoinCommessaCorriere getCodiceCliente(String cc) {
		if (!codiciCliente.containsKey(cc)) {
			EntityManager em = FactoryManager.getInstance().getFactory(persistenceUnitName).createEntityManager();
			CriteriaBuilder cb = em.getCriteriaBuilder();
	        CriteriaQuery<JoinCommessaCorriere> criteria = cb.createQuery(JoinCommessaCorriere.class);
	        Root<JoinCommessaCorriere> member = criteria.from(JoinCommessaCorriere.class);
	        criteria.select(member).where(cb.equal(member.get("codiceCliente"), cc));
	        List<JoinCommessaCorriere> list = em.createQuery(criteria).setMaxResults(1).getResultList();
	        JoinCommessaCorriere codice = list.isEmpty() ? null : list.get(0);
	        codiciCliente.put(cc, codice);
		}
		return codiciCliente.get(cc);
	}
	
	protected Commessa getCommessa(int idCommessa) {
		if (!commesse.containsKey(idCommessa)) {
			EntityManager em = FactoryManager.getInstance().getFactory(persistenceUnitName).createEntityManager();
			Commessa commessa = em.find(Commessa.class, idCommessa);
			commesse.put(idCommessa, commessa);
			em.close();
		}
		return commesse.get(idCommessa);
	}
	
	protected List<Sede> getSedi() {
		EntityManager em = FactoryManager.getInstance().getFactory(persistenceUnitName).createEntityManager();
		CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Sede> criteria = cb.createQuery(Sede.class);
        Root<Sede> member = criteria.from(Sede.class);
        criteria.select(member);
        List<Sede> list = em.createQuery(criteria).getResultList();
		em.close();
		return list;
	}
	
	protected Cap getInfoCap(String cap, String localita) {
		EntityManager em = FactoryManager.getInstance().getFactory(persistenceUnitName).createEntityManager();
		CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Cap> criteria = cb.createQuery(Cap.class);
        Root<Cap> member = criteria.from(Cap.class);
        Predicate condizione;
        if (localita != null && !localita.isEmpty()) {
        	Predicate condizioneCAP = cb.equal(member.get("id").get("cap"), cap);
        	Predicate condizioneLocalita = cb.equal(member.get("id").get("localita"), localita);
        	condizione = cb.and(condizioneCAP, condizioneLocalita);
        } else {
        	condizione = cb.equal(member.get("id").get("cap"), cap);
        }
        criteria.select(member).where(condizione);
        List<Cap> list = em.createQuery(criteria).setMaxResults(1).getResultList();
		em.close();
		Cap c = list.isEmpty() ? null : list.get(0);
		return c;
	}
	
	protected Indirizzo recuperaIndirizzo(int idIndirizzo) {
		EntityManager em = FactoryManager.getInstance().getFactory(persistenceUnitName).createEntityManager();
		Indirizzo indirizzo = em.find(Indirizzo.class, idIndirizzo);
		em.close();
		return indirizzo;
	}
	
	protected int recuperaDocumento(JoinCommessaCorriere codice, String riferimento) {
		int idOrdine;
		//Provo a vedere se esiste già un documento che faccia riferimento a quella spedizione.
		//I criteri utilizzati sono la commessa, il riferimento e il tipo di documento (Ordine)
		EntityManager em = FactoryManager.getInstance().getFactory(persistenceUnitName).createEntityManager();
		CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Documento> criteria = cb.createQuery(Documento.class);
        Root<Documento> member = criteria.from(Documento.class);
        Predicate condizioneCommessa = cb.equal(member.get("idCommessa"), codice.getCommessa());
        Predicate condizioneRiferimento = cb.equal(member.get("riferimentoCliente"), riferimento);
        Predicate condizioneTipoDocumento = cb.equal(member.get("tipo"), Documento.TipoDocumento.ORDINE);
        criteria.select(member).where(cb.and(condizioneCommessa, condizioneRiferimento, condizioneTipoDocumento)).orderBy(cb.desc(member.get("dataCreazione")));
        List<Documento> list = em.createQuery(criteria).setMaxResults(1).getResultList();
        //Controllo se ho almeno un risultato, in quel caso lo prendo altrimenti inserisco un nuovo documento.
        if (list.isEmpty()) {
        	Documento nuovoDocumento = new Documento();
        	nuovoDocumento.setDataCreazione(new Date());
        	nuovoDocumento.setIdCommessa(codice.getCommessa());
        	nuovoDocumento.setRiferimentoCliente(riferimento);
        	nuovoDocumento.setRiferimentoInterno(riferimento);
        	nuovoDocumento.setTipo(Documento.TipoDocumento.ORDINE);
        	//Inserisco il nuovo documento nel DB
        	EntityTransaction transaction = em.getTransaction();
        	try {
        		transaction.begin();
            	em.persist(nuovoDocumento);
            	transaction.commit();
        	} catch (Exception e) {
        		transaction.rollback();
        		logger.error(e.getStackTrace());
        		throw new RuntimeException("Impossibile inserire il nuovo documento");
        	}
        	idOrdine = nuovoDocumento.getId();
        } else {
        	idOrdine = list.get(0).getId();
        }
		return idOrdine;
	}
	
	protected Spedizione recuperaSpedizione(String codiceCliente, String letteraDiVettura) {
		EntityManager em = FactoryManager.getInstance().getFactory(persistenceUnitName).createEntityManager();
		CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Spedizione> criteria = cb.createQuery(Spedizione.class);
        Root<Spedizione> member = criteria.from(Spedizione.class);
        Predicate condizioneCorriere = cb.equal(member.get("idCorriere"), 1);
        Predicate condizioneCodiceCliente = cb.equal(member.get("codiceCliente"), codiceCliente);
        Predicate condizioneLDV = cb.equal(member.get("letteraDiVettura"), letteraDiVettura);
        criteria.select(member).where(cb.and(condizioneCorriere, condizioneCodiceCliente, condizioneLDV));
        List<Spedizione> list = em.createQuery(criteria).setMaxResults(1).getResultList();		
		Spedizione trovata = list.isEmpty() ? null : list.get(0);
		return trovata;
	}
	
	protected Indirizzo recuperaIndirizzo(String ragioneSociale, String provincia, String via, String localita, String cap, String nazione) {
		Indirizzo indirizzo;
		EntityManager em = FactoryManager.getInstance().getFactory(persistenceUnitName).createEntityManager();
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
        	if (nazione == null || nazione.isEmpty())
        		nazione = "ITA";
        	indirizzo = new Indirizzo();
        	indirizzo.setCap(cap);
        	indirizzo.setIndirizzo(via);
        	indirizzo.setLocalita(localita);
        	indirizzo.setNazione(nazione);
        	indirizzo.setProvincia(provincia);
        	indirizzo.setRagioneSociale(ragioneSociale);
        	EntityTransaction transaction = em.getTransaction();
        	try {
        		transaction.begin();
            	em.persist(indirizzo);
            	transaction.commit();
        	} catch (Exception e) {
        		logger.error(e.getStackTrace());
        		if (transaction != null && transaction.isActive())
        			transaction.rollback();
        		throw new RuntimeException("Impossibile inserire il nuovo indirizzo.");
        	} finally {
        		em.close();
        	}
		} else {
			indirizzo = list.get(0);
		}
		return indirizzo;
	}
	
	protected Date getScadenza(Cap cap, Date partenza, String servizio) {
		long tempoMassimo = 1;
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(partenza);
		if (servizio.equals("O10")) {
			calendar.set(Calendar.HOUR_OF_DAY, 10);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
		} else if (servizio.equals("O12")) {
			calendar.set(Calendar.HOUR_OF_DAY, 12);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
		} else if (cap != null && cap.getBrtDisagiate()) {
			calendar.set(Calendar.HOUR_OF_DAY, 18);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
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
			} else if 
				(
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
	
	protected boolean sendAlertEmail(String message) {
		List<String> destinatari = config.getIndirizziResponsabili();
		String subject = "Alert - Errore durante l'importazione dati BRT";
		MailMan mailer = config.getMailMan();
		Email mail = new Email(subject, message);
		return mailer.invia(destinatari, mail);
	}

}
