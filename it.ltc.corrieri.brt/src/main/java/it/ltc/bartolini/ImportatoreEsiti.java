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

import it.ltc.bartolini.model.fnvac.FileFNVAC;
import it.ltc.bartolini.model.fnvac.RigaFNVAC;
import it.ltc.database.dao.FactoryManager;
import it.ltc.database.model.centrale.Cap;
import it.ltc.database.model.centrale.Indirizzo;
import it.ltc.database.model.centrale.Spedizione;
import it.ltc.database.model.centrale.Tracking;

public class ImportatoreEsiti extends Importatore {
	
	private static final Logger logger = Logger.getLogger("Importazione Esiti");
	
	private static ImportatoreEsiti instance;
	
	private final RecuperaEsiti re;

	private ImportatoreEsiti() {		
		re = RecuperaEsiti.getInstance();
	}

	public static ImportatoreEsiti getInstance() {
		if (instance == null) {
			instance = new ImportatoreEsiti();
		}
		return instance;
	}
	
	public void importaEsiti() {
		List<Spedizione> spedizioni = getSpedizioni();
		for (Spedizione spedizione : spedizioni) {
			LinkedList<Tracking> listaTracking = re.getTracking(spedizione);
			if (!listaTracking.isEmpty()) {
				listaTracking.sort(null);
				for (Tracking tracking : listaTracking) {
					inserisciTracking(tracking);					
				}
				Tracking ultimo = listaTracking.getLast();
				String stato = ultimo.getId().getStato();
				if (stato.equals("S03")) {
					Indirizzo destinazione = recuperaIndirizzo(spedizione.getIndirizzoDestinazione());
					Cap cap = getInfoCap(destinazione.getCap(), destinazione.getLocalita());
					if (cap == null)
						cap = getInfoCap(destinazione.getCap(), null);
					if (cap != null) {
						Date scadenza = getScadenza(cap, spedizione.getDataPartenza(), spedizione.getServizio());
						if (scadenza.before(ultimo.getId().getData())) {
							stato = "S04"; //Consegnata, ma in ritardo
							spedizione.setInRitardo(true);
						}
					}
				}
				aggiornaStatoSpedizione(spedizione, stato);
			}
		}
	}
	
	private boolean aggiornaStatoSpedizione(Spedizione spedizione, String stato) {
		boolean update;
		EntityManager em = FactoryManager.getInstance().getFactory(persistenceUnitName).createEntityManager();
		spedizione = em.find(Spedizione.class, spedizione.getId());
		spedizione.setStato(stato);
		EntityTransaction t = em.getTransaction();
		try {
			t.begin();
			em.persist(spedizione);
			t.commit();
			update = true;
			logger.info("Aggiornata la spedizione: " + spedizione.getLetteraDiVettura() + " a " + stato);
		} catch (Exception e) {
			logger.error(e);
			if (t != null && t.isActive())
				t.rollback();
			update = false;
		} finally {
			em.close();
		}
		return update;
	}
	
	private boolean inserisciTracking(Tracking tracking) {
		boolean insert;
		EntityManager em = FactoryManager.getInstance().getFactory(persistenceUnitName).createEntityManager();
		EntityTransaction t = em.getTransaction();
		try {
			t.begin();
			em.persist(tracking);
			t.commit();
			insert = true;
		} catch (Exception e) {
			logger.error(e);
			if (t != null && t.isActive())
				t.rollback();
			insert = false;
		} finally {
			em.close();
		}
		return insert;
	}

	private List<Spedizione> getSpedizioni() {
		//String query = "SELECT * FROM spedizione WHERE id_corriere = 1 AND lettera_di_vettura IS NOT NULL AND (stato = 'IMP' OR stato = 'L04' OR stato = 'S01' OR stato = 'S02' OR stato LIKE 'C%')";
		EntityManager em = FactoryManager.getInstance().getFactory(persistenceUnitName).createEntityManager();
		CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Spedizione> criteria = cb.createQuery(Spedizione.class);
        Root<Spedizione> member = criteria.from(Spedizione.class);
        Predicate condizioneCorriere = cb.equal(member.get("idCorriere"), 1);
        Predicate condizioneLDV = cb.isNotNull(member.get("letteraDiVettura"));
        Predicate statoIMP = cb.equal(member.get("stato"), "IMP");
        Predicate statoL04 = cb.equal(member.get("stato"), "L04");
        Predicate statoS01 = cb.equal(member.get("stato"), "S01");
        Predicate statoS02 = cb.equal(member.get("stato"), "S02");
        Predicate statoC = cb.like(member.get("stato"), "C%");
        Predicate condizioneStato = cb.or(statoIMP, statoL04, statoS01, statoS02, statoC);
        criteria.select(member).where(cb.and(condizioneCorriere, condizioneLDV, condizioneStato));
        List<Spedizione> list = em.createQuery(criteria).getResultList();
		em.close();
		return list;
	}

	public boolean importaFNVAG(File file) {
		boolean success = true;
		FileFNVAC fnvac = new FileFNVAC(file);
		List<RigaFNVAC> esiti = fnvac.getEsiti();
		logger.info("Sono state trovati " + esiti.size() + " esiti nel file '" + file.getName() + "'");
		for (RigaFNVAC esito : esiti) {
			boolean importazione = importaEsito(esito);
			if (!importazione)
				success = false;
		}
		return success;
	}

	public boolean importaEsito(RigaFNVAC esito) {
		//TODO
		return true;
	}

}
