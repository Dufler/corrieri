package it.ltc.bartolini;

import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.log4j.Logger;

import iseries.wsbeans.getlegendaeventi.GetLegendaEventiServicesProxy;
import iseries.wsbeans.getlegendaeventi.GetlegendaeventiInput;
import iseries.wsbeans.getlegendaeventi.GetlegendaeventiResult;
import iseries.wsbeans.getlegendaeventi.LegendaEVENTO;
import it.ltc.database.dao.FactoryManager;
import it.ltc.database.model.centrale.TrackingStatoCodificaCorriere;
import it.ltc.database.model.centrale.TrackingStatoCodificaCorrierePK;

public class RecuperaLegendaEventi extends Importatore {
	
	private static final int ID_BARTOLINI = 1;
	private static final String STATO_PROVVISORIO_BARTOLINI = "BRT";
	
	private static final Logger logger = Logger.getLogger(RecuperaLegendaEventi.class);
	
	private static RecuperaLegendaEventi instance;
	
	private final HashMap<String, TrackingStatoCodificaCorriere> legenda;
	private final GetLegendaEventiServicesProxy ws;
	
	private RecuperaLegendaEventi() {
		ws = new GetLegendaEventiServicesProxy();
		legenda = new HashMap<String, TrackingStatoCodificaCorriere>();
		recuperaEsitiBartolini();
	}
	
	public static RecuperaLegendaEventi getInstance() {
		if (instance == null) {
			instance = new RecuperaLegendaEventi();
		}
		return instance;
	}
	
	private void recuperaEsitiBartolini() {
		EntityManager em = FactoryManager.getInstance().getFactory(persistenceUnitName).createEntityManager();
		CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<TrackingStatoCodificaCorriere> criteria = cb.createQuery(TrackingStatoCodificaCorriere.class);
        Root<TrackingStatoCodificaCorriere> member = criteria.from(TrackingStatoCodificaCorriere.class);
        criteria.select(member).where(cb.equal(member.get("id").get("idCorriere"), ID_BARTOLINI));
        List<TrackingStatoCodificaCorriere> list = em.createQuery(criteria).getResultList();
		em.close();
		for (TrackingStatoCodificaCorriere stato : list) {
			legenda.put(stato.getId().getCodificaCorriere(), stato);
		}
	}
	
	public TrackingStatoCodificaCorriere getStato(String codifica) {
		if (!legenda.containsKey(codifica)) {
			try {
				GetlegendaeventiInput input = new GetlegendaeventiInput();
				input.setLINGUA_ISO639_ALPHA2("IT");
				input.setULTIMO_ID_RICEVUTO(codifica);
				GetlegendaeventiResult result = ws.getlegendaeventi(input);
				LegendaEVENTO[] legende = result.getLEGENDA();
				if (legende != null && legende.length > 0) //null and emptiness check
				for (LegendaEVENTO l : legende) {
					String id = l.getID();
					String descrizione = l.getDESCRIZIONE();
					TrackingStatoCodificaCorriere nuovoStato = new TrackingStatoCodificaCorriere();
					TrackingStatoCodificaCorrierePK pk = new TrackingStatoCodificaCorrierePK();
					nuovoStato.setId(pk);
					nuovoStato.setDescrizione(descrizione);
					pk.setIdCorriere(ID_BARTOLINI);
					pk.setCodificaCorriere(id);
					nuovoStato.setStato(STATO_PROVVISORIO_BARTOLINI);
					boolean inserimento = inserisciNuovoStato(nuovoStato);
					if (inserimento) {
						logger.info("Inserito il nuovo stato: " + id + ", " + descrizione);
						legenda.put(id, nuovoStato);
					} else {
						String message = "Non è stato possibile inserire lo stato: " + id + ", " + descrizione;
						logger.error(message);
						sendAlertEmail(message);
					}
				}
			} catch (Exception e) {
				logger.error(e);
				for (StackTraceElement st : e.getStackTrace())
					logger.error(st);
			}
		}
		return legenda.get(codifica);
	}
	
	private boolean inserisciNuovoStato(TrackingStatoCodificaCorriere stato) {
		boolean inserimento;
		EntityManager em = FactoryManager.getInstance().getFactory(persistenceUnitName).createEntityManager();
		EntityTransaction t = em.getTransaction();
		try {
			t.begin();
			em.persist(stato);
			t.commit();
			inserimento = true;
		} catch (Exception e) {
			inserimento = false;
			t.rollback();
		} finally {
			em.close();
		}
		return inserimento;
	}
	
}
