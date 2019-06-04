package it.ltc.bartolini;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.log4j.Logger;

import it.ltc.database.dao.FactoryManager;
import it.ltc.database.model.legacy.TestaCorrLight;

public class RecuperaTestate {
	
	private static final Logger logger = Logger.getLogger(RecuperaTestate.class);
	
	private static RecuperaTestate instance;
	
	private RecuperaTestate() {}
	
	public static RecuperaTestate getInstance() {
		if (instance == null) {
			instance = new RecuperaTestate();
		}
		return instance;
	}
	
	/**
	 * Cerca nella DB identificato dal nome della risorsa (Persistence Unit Name) una spedizione con il dato riferimento.
	 * Se ne viene trovata solo una allora viene restituita, nel caso in cui invece ne vengano trovate più di una o nessuna allora tenta di trovarla utilizzando il nome del destinatario e la data.
	 * Anche qui viene restituito il dato solo se si trova un solo candidato.
	 * @param nomeRisorsa il nome della persistence unit
	 * @param riferimentoSpedizione il riferimento (alfabetico o numerico) della spedizione 
	 * @param destinatarioSpedizione la ragione sociale del destinatario della spedizione
	 * @param dataSpedizione la data della spedizione
	 * @return la testata della spedizione presente nei sistemi legacy se trovata, null alterimenti.
	 */
	public TestaCorrLight recuperaTestata(String nomeRisorsa, String riferimentoSpedizione, String destinatarioSpedizione, Date dataSpedizione) {
		TestaCorrLight testata = null;
		//Controllo che sia una persistence unit esistente, verrà cambiato/tolto in futuro.
		if (nomeRisorsa.startsWith("legacy-") && !riferimentoSpedizione.isEmpty()) {
			EntityManager em = FactoryManager.getInstance().getFactory(nomeRisorsa).createEntityManager();
			CriteriaBuilder cb = em.getCriteriaBuilder();
	        CriteriaQuery<TestaCorrLight> criteria = cb.createQuery(TestaCorrLight.class);
	        Root<TestaCorrLight> member = criteria.from(TestaCorrLight.class);
	        riferimentoSpedizione = riferimentoSpedizione.replaceAll("DDT", "");
	        riferimentoSpedizione = riferimentoSpedizione.replaceAll("/", "");
	        Predicate condizioneRiferimentoLike = cb.like(member.get("mittenteAlfa"), "%" + riferimentoSpedizione + "%");
	        //Predicate condizioneRiferimento = cb.equal(member.get("mittenteAlfa"), riferimentoSpedizione);
	        //FIXME - Per ora vado a controllare solo il riferimento, se funziona bene.
	        //criteria.select(member).where(cb.and(condizioneRiferimento, condizioneDestinatario));
	        criteria.select(member).where(condizioneRiferimentoLike);
	        List<TestaCorrLight> list = em.createQuery(criteria).setMaxResults(2).getResultList();
	        testata = list.size() == 1 ? list.get(0) : null;
	        //Se non sono riuscito a trovarla così ritento
	        if (testata == null && !destinatarioSpedizione.isEmpty()) {
	        	GregorianCalendar data = new GregorianCalendar();
	        	data.setTime(dataSpedizione);
	        	int anno = data.get(Calendar.YEAR);
	        	int giorno = data.get(Calendar.DAY_OF_MONTH);
	        	data.set(Calendar.DAY_OF_MONTH, giorno - 2);
	        	int meseGiornoInizio = (data.get(Calendar.MONTH) + 1) * 100 + data.get(Calendar.DAY_OF_MONTH);
	        	data.set(Calendar.DAY_OF_MONTH, giorno + 4);
	        	int meseGiornoFine = (data.get(Calendar.MONTH) + 1) * 100 + data.get(Calendar.DAY_OF_MONTH);
	        	Predicate condizioneData = cb.between(member.get("dataSpe"), meseGiornoInizio, meseGiornoFine);
	        	Predicate condizioneAnno = cb.equal(member.get("annoSpe"), anno);
	        	Predicate condizioneDestinatario = cb.equal(member.get("ragSocDest"), destinatarioSpedizione);
	        	criteria.select(member).where(cb.and(condizioneAnno, condizioneData, condizioneDestinatario));
		        List<TestaCorrLight> list2 = em.createQuery(criteria).setMaxResults(1).getResultList();
		        testata = list2.size() == 1 ? list2.get(0) : null;
	        }
	        em.close();
		}
		if (testata != null) {
			logger.info("Testata trovata! Pezzi: " + testata.getPezzi());
		}
		return testata;
	}

}
