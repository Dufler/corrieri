package it.ltc.bartolini;

import java.io.File;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.log4j.Logger;

import it.ltc.bartolini.model.costanti.ChiusuraGiacenza;
import it.ltc.bartolini.model.fnvag.FileFNVAG;
import it.ltc.bartolini.model.fnvag.RigaFNVAG;
import it.ltc.database.dao.FactoryManager;
import it.ltc.database.model.centrale.JoinCommessaCorriere;
import it.ltc.database.model.centrale.Spedizione;
import it.ltc.database.model.centrale.SpedizioneGiacenza;
import it.ltc.database.model.centrale.enumcondivise.Fatturazione;

public class ImportatoreGiacenze extends Importatore {
	
	private static final Logger logger = Logger.getLogger(ImportatoreGiacenze.class);
	
	private static ImportatoreGiacenze instance;
	private final List<String> codiciMancanti;

	private ImportatoreGiacenze() {
		codiciMancanti = new LinkedList<>();
	}

	public static ImportatoreGiacenze getInstance() {
		if (null == instance) {
			instance = new ImportatoreGiacenze();
		}
		return instance;
	}
	
	public boolean importaFNVAG(File file) {
		boolean success = true;
		FileFNVAG fnvag = new FileFNVAG(file);
		List<RigaFNVAG> esiti = fnvag.getEsiti();
		logger.info("Sono state trovate " + esiti.size() + " giacenze nel file '" + file.getName() + "'");
		for (RigaFNVAG esito : esiti) {
			boolean importazione = importaGiacenza(esito);
			if (!importazione)
				success = false;
		}
		return success;
	}

	public boolean importaGiacenza(RigaFNVAG esito) {
		boolean success;
		String cc = esito.getCodiceMittenteGiacenza();
		JoinCommessaCorriere codiceCliente = getCodiceCliente(cc);
		if (codiceCliente != null) {
			String letteraDiVetturaGiacenza = getLetteraDiVetturaGiacenza(esito);
			String letteraDiVetturaOriginale = getLetteraDiVetturaOriginale(esito);
			SpedizioneGiacenza giacenza = recuperaGiacenza(letteraDiVetturaGiacenza, letteraDiVetturaOriginale);
			if (giacenza != null) {
				success = aggiornaGiacenza(giacenza, esito);
			} else {
				Spedizione spedizione = recuperaSpedizione(codiceCliente, letteraDiVetturaOriginale);
				success = inserisciGiacenza(spedizione, esito, letteraDiVetturaGiacenza, letteraDiVetturaOriginale);
			}
		} else {
			success = false;
			//manda una mail di alert a Sonia e a support indicando il codice.
			if (cc != null && !cc.isEmpty() && !codiciMancanti.contains(cc)) {
				codiciMancanti.add(cc);
				sendAlertEmail("Il codice cliente '" + cc + "' risulta mancante e va inserito!");
			}
		}
		return success;
	}

	private boolean aggiornaGiacenza(SpedizioneGiacenza giacenza, RigaFNVAG esito) {
		boolean success = true;
		if (isFatturabile(esito)) {
			EntityManager em = FactoryManager.getInstance().getFactory(persistenceUnitName).createEntityManager();
			giacenza = em.find(SpedizioneGiacenza.class, giacenza.getId());
			giacenza.setDataChiusura(esito.getDataChiusura());
			giacenza.setFatturazione(Fatturazione.FATTURABILE);
			ChiusuraGiacenza codiceChiusura = esito.getCodiceChiusura();
			//Controllo la destinazione delle merce
			if (codiceChiusura == ChiusuraGiacenza._002) {
				Integer idMittente = giacenza.getIdMittente();
				giacenza.setIdDestinatario(idMittente);
			} else if (codiceChiusura == ChiusuraGiacenza._006) {
				//TODO - trovare il modo di capire quale sia il nuovo indirizzo e aggiornarlo.
			}
			String note = giacenza.getNote() != null ? giacenza.getNote() : "";
			if (!note.isEmpty())
				note += ", ";
			note += codiceChiusura.toString();
			//Controllo che le note non siano eccessivamente lunghe.
			if (note != null && note.length() > 200)
				note = note.substring(0, 200);
			giacenza.setNote(note);
			//aggiornamento
			EntityTransaction t = em.getTransaction();
			try {
				t.begin();
				em.merge(giacenza);
				t.commit();
				success = true;
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				if (t != null && t.isActive())
					t.rollback();
				success = false;
			} finally {
				em.close();
			}
		}
		return success;
	}

	private boolean inserisciGiacenza(Spedizione spedizione, RigaFNVAG esito, String letteraDiVetturaGiacenza, String letteraDiVetturaOriginale) {
		boolean success;
		if (spedizione != null) {
			SpedizioneGiacenza giacenza = new SpedizioneGiacenza();
			Date apertura = esito.getDataApertura();
			Date chiusura = esito.getDataChiusura() != null && esito.getDataChiusura().after(apertura) ? esito.getDataChiusura() : null;
			giacenza.setDataApertura(apertura);
			giacenza.setDataChiusura(chiusura);
			Fatturazione fatturazione = isFatturabile(esito) ? Fatturazione.FATTURABILE : Fatturazione.IN_DEFINIZIONE;
			giacenza.setFatturazione(fatturazione);
			giacenza.setIdCommessa(spedizione.getIdCommessa());
			giacenza.setIdDestinatario(spedizione.getIndirizzoDestinazione());
			giacenza.setIdMittente(spedizione.getIndirizzoPartenza());
			giacenza.setIdDocumento(spedizione.getIdDocumento());
			giacenza.setIdSpedizione(spedizione.getId());
			giacenza.setLetteraDiVettura(letteraDiVetturaGiacenza);
			giacenza.setLetteraDiVetturaOriginale(letteraDiVetturaOriginale);
			String note = esito.getDescrizioneCausale() + " " + esito.getDescrizioneAggiuntivaCausale();
			giacenza.setNote(note.trim());
			//inserimento
			EntityManager em = FactoryManager.getInstance().getFactory(persistenceUnitName).createEntityManager();
			EntityTransaction t = em.getTransaction();
			try {
				t.begin();
				em.persist(giacenza);
				t.commit();
				success = true;
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				if (t != null && t.isActive())
					t.rollback();
				success = false;
			} finally {
				em.close();
			}
		} else {
			success = false;
		}
		return success;
	}
	
	/**
	 * Controlla se è stato inserito un motivo di chiusura della giacenza, se c'è diventa fatturabile.
	 */
	private boolean isFatturabile(RigaFNVAG esito) {
		return esito.getCodiceChiusura() != ChiusuraGiacenza._000;
	}

	/**
	 * Controlla se esiste già a sistema, ed eventualmente restituisce, una giacenza con queste lettere di vettura.
	 */
	private SpedizioneGiacenza recuperaGiacenza(String letteraDiVetturaGiacenza, String letteraDiVetturaOriginale) {
		EntityManager em = FactoryManager.getInstance().getFactory(persistenceUnitName).createEntityManager();
		CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<SpedizioneGiacenza> criteria = cb.createQuery(SpedizioneGiacenza.class);
        Root<SpedizioneGiacenza> member = criteria.from(SpedizioneGiacenza.class);
        Predicate condizioneLDV = cb.equal(member.get("letteraDiVettura"), letteraDiVetturaGiacenza);
        Predicate condizioneLDVOriginale = cb.equal(member.get("letteraDiVetturaOriginale"), letteraDiVetturaOriginale);
        criteria.select(member).where(cb.and(condizioneLDV, condizioneLDVOriginale));
        List<SpedizioneGiacenza> list = em.createQuery(criteria).setMaxResults(1).getResultList();
		SpedizioneGiacenza trovata = list.isEmpty() ? null : list.get(0);
		return trovata;
	}

	/**
	 * Combina il codice della filiale di partenza, il numero di serie e il numero di spedizione per ottenere la lettera di vettura secondo gli standard Bartolini.
	 * Tale lettera di vettura può essere usata nei web services per ottere il tracking della spedizione.
	 */
	private String getLetteraDiVetturaOriginale(RigaFNVAG esito) {
		String filialeDiPartenza = esito.getFilialeDiPartenza();
		String serie = esito.getNumeroSerie();
		String numeroSpedizione = esito.getNumeroSpedizione();
		String letteraDiVettura = filialeDiPartenza + serie + numeroSpedizione;
		return letteraDiVettura;
	}
	
	/**
	 * Combina il codice della filiale di partenza, il numero di serie e il numero di giacenza per ottenere la lettera di vettura secondo gli standard Bartolini.
	 * Solo la lettera di vettura originale può essere usata nei web services per ottere il tracking della spedizione, questa non va bene.
	 */
	private String getLetteraDiVetturaGiacenza(RigaFNVAG esito) {
		String filialeDiPartenza = esito.getFilialeDiPartenza();
		String serie = esito.getNumeroSerie();
		String numeroGiacenza = esito.getNumeroGiacenza();
		String letteraDiVettura = filialeDiPartenza + serie + numeroGiacenza;
		return letteraDiVettura;
	}
	
	private Spedizione recuperaSpedizione(JoinCommessaCorriere codiceCliente, String letteraDiVettura) {
		EntityManager em = FactoryManager.getInstance().getFactory(persistenceUnitName).createEntityManager();
		CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Spedizione> criteria = cb.createQuery(Spedizione.class);
        Root<Spedizione> member = criteria.from(Spedizione.class);
        Predicate condizioneCorriere = cb.equal(member.get("idCorriere"), 1);
        Predicate condizioneCodiceCliente = cb.equal(member.get("codiceCliente"), codiceCliente.getCodiceCliente());
        Predicate condizioneLDV = cb.equal(member.get("letteraDiVettura"), letteraDiVettura);
        criteria.select(member).where(cb.and(condizioneCorriere, condizioneCodiceCliente, condizioneLDV));
        List<Spedizione> list = em.createQuery(criteria).setMaxResults(1).getResultList();		
		Spedizione trovata = list.isEmpty() ? null : list.get(0);
		if (trovata != null) {
			logger.info("Spedizione presente, vado in aggiornamento settando che è andata in giacenza.");
			trovata.setGiacenza(true);
			boolean update;
			EntityTransaction t = em.getTransaction();
			try {
				t.begin();
				em.merge(trovata);
				t.commit();
				update = true;
			} catch (Exception e) {
				t.rollback();
				update = false;
			} finally {
				em.close();
			}
			if (!update) {
				String message = "Impossibile aggiornare lo stato di giacenza della spedizione: '" + letteraDiVettura + "'";
				logger.error(message);
				sendAlertEmail(message);
			}
		} else {
			em.close();
			String message = "E' stata restituita una informazione di giacenza su una spedizione non presente a sistema.";
			logger.error(message);
			//Ho tolto l'alert perchè BRT ci invia di continuo info su giacenze di codici clienti che non sono di nostra gestione (es. IMAC)
			//sendAlertEmail(message);
		}
		return trovata;
	}

}
