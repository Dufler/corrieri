package it.ltc.corrieri.tnt;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.log4j.Logger;

import it.ltc.database.dao.FactoryManager;
import it.ltc.database.model.centrale.Commessa;
import it.ltc.database.model.centrale.Indirizzo;
import it.ltc.database.model.centrale.Spedizione;
import it.ltc.database.model.legacy.TestaCorr;

public class MainImportazionePezzi {
	
	private static final Logger logger = Logger.getLogger("Main Importazione Pezzi Legacy");

	public static final String persistenceUnitName = "produzione";
	public static final int idCommessa = 5;
	public static final boolean senzaPezzi = false;
	
	private static int aggiornamenti = 0;
	private static int aggiornamentiRiferimento = 0;
	private static int aggiornamentiNomeData = 0;
	
	
	public static void main(String[] args) {
		//RecuperatoreDatiLegacy rdl = RecuperatoreDatiLegacy.getInstance();
		//Recupero le info sulla commessa
		Commessa commessa = getCommessa();
		logger.info("Opero sulla commessa: '" + commessa.getNome() + "'");
		List<Spedizione> spedizioni;
		if (senzaPezzi) {
			//Recupero le spedizioni senza pezzi
			spedizioni = getSpedizioniSenzaPezzi(commessa);
		} else {
			//Recupero tutte quelle del mese passato.
			GregorianCalendar c = new GregorianCalendar();
			int mese = c.get(Calendar.MONTH) - 1;
			//Se era Gennaio allora imposto a Dicembre dell'anno passato
			if (mese < 0) {
				mese = 11;
				int anno = c.get(Calendar.YEAR) - 1;
				c.set(Calendar.YEAR, anno);
			}
			c.set(Calendar.MONTH, mese);
			//Imposto l'inizio del mese come campo Da
			c.set(Calendar.DAY_OF_MONTH, 1);
			c.set(Calendar.HOUR_OF_DAY, 0);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MILLISECOND, 0);
			Date da = c.getTime();
			//Imposto la fine del mese come campo A
			int daysInMonth = getDayInMonth(mese);
			c.set(Calendar.DAY_OF_MONTH, daysInMonth);
			c.set(Calendar.HOUR_OF_DAY, 23);
			c.set(Calendar.MINUTE, 59);
			c.set(Calendar.SECOND, 59);
			c.set(Calendar.MILLISECOND, 0);
			Date a = c.getTime();
			spedizioni = getSpedizioniDaA(commessa, da, a);
		}
		logger.info("Sono state trovate " + spedizioni.size() + " da aggiornare.");
		//Per ogni spedizione cerco la corrispettiva nel DB legacy
		for (Spedizione spedizione : spedizioni) {
			Indirizzo destinatario = getDestinatario(spedizione);
			String ragioneSociale = destinatario != null ? destinatario.getRagioneSociale() : "";
			TestaCorr legacy = RecuperatoreDatiLegacy.getInstance().recuperaTestata(commessa.getNomeRisorsa(), spedizione.getRiferimentoCliente(), ragioneSociale, spedizione.getDataPartenza());
			//TestaCorr legacy = recuperaTestata(commessa.getNomeRisorsa(), spedizione.getRiferimentoCliente(), ragioneSociale, spedizione.getDataPartenza());
			//Se la trovo aggiorno i pezzi e lo stato di completezza
			if (legacy != null) {
				aggiornaSpedizione(spedizione, legacy);
			} else {
				logger.warn("Non ho trovato la spedizione.");
			}
		}
		logger.info("Spedizioni aggiornate: " + aggiornamenti + " su " + spedizioni.size());
		logger.info("Di cui trovate tramite riferimento: " + aggiornamentiRiferimento);
		logger.info("Di cui trovate tramite nome e data: " + aggiornamentiNomeData);
	}
	
	private static int getDayInMonth(int month) {
		int days;
		switch (month) {
			case Calendar.FEBRUARY : days = 28; break;
			case Calendar.APRIL : days = 30; break;
			case Calendar.JUNE : days = 30; break;
			case Calendar.SEPTEMBER : days = 30; break;
			case Calendar.NOVEMBER : days = 30; break;
			default : days = 31;
		}
		return days;
	}

	private static void aggiornaSpedizione(Spedizione spedizione, TestaCorr legacy) {
		EntityManager em = FactoryManager.getInstance().getFactory(persistenceUnitName).createEntityManager();
		Spedizione s = em.find(Spedizione.class, spedizione.getId());
		s.setPezzi(legacy.getPezzi());
		s.setDatiCompleti(true);
		Double peso = s.getPeso(); 
		if ((peso == null || peso <= 0) && legacy.getPeso() < 1000)
			s.setPeso(legacy.getPeso());
		Double volume = s.getVolume();
		if ((volume == null || volume <= 0) && legacy.getVolume() < 100)
			s.setVolume(legacy.getVolume());
		EntityTransaction t = em.getTransaction();
		try {
			t.begin();
			em.merge(s);
			t.commit();
			aggiornamenti += 1;
		} catch (Exception e) {
			if (t != null && t.isActive())
				t.rollback();
		}
		em.close();
	}
	
	private static List<Spedizione> getSpedizioniDaA(Commessa commessa, Date da, Date a) {
		EntityManager em = FactoryManager.getInstance().getFactory(persistenceUnitName).createEntityManager();
		CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Spedizione> criteria = cb.createQuery(Spedizione.class);
        Root<Spedizione> member = criteria.from(Spedizione.class);
        Predicate condizioneCommessa = cb.equal(member.get("idCommessa"), commessa.getId());
        Predicate condizioneDate = cb.between(member.get("dataPartenza"), da, a);
        criteria.select(member).where(cb.and(condizioneCommessa, condizioneDate));
        List<Spedizione> list = em.createQuery(criteria).getResultList();
		em.close();
		return list;
	}

	private static List<Spedizione> getSpedizioniSenzaPezzi(Commessa commessa) {
		EntityManager em = FactoryManager.getInstance().getFactory(persistenceUnitName).createEntityManager();
		CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Spedizione> criteria = cb.createQuery(Spedizione.class);
        Root<Spedizione> member = criteria.from(Spedizione.class);
        Predicate condizioneCommessa = cb.equal(member.get("idCommessa"), commessa.getId());
        Predicate condizionePezzi = cb.equal(member.get("pezzi"), 0);
        criteria.select(member).where(cb.and(condizioneCommessa, condizionePezzi));
        List<Spedizione> list = em.createQuery(criteria).getResultList();
		em.close();
		return list;
	}
	
	private static Indirizzo getDestinatario(Spedizione spedizione) {
		EntityManager em = FactoryManager.getInstance().getFactory(persistenceUnitName).createEntityManager();
		Indirizzo destinatario = em.find(Indirizzo.class, spedizione.getIndirizzoDestinazione());
		em.close();
		return destinatario;
	}

	private static Commessa getCommessa() {
		EntityManager em = FactoryManager.getInstance().getFactory(persistenceUnitName).createEntityManager();
		Commessa commessa = em.find(Commessa.class, idCommessa);
		em.close();
		return commessa;
	}
	
//	private static TestaCorr recuperaTestata(String nomeRisorsa, String riferimentoSpedizione, String destinatarioSpedizione, Date dataSpedizione) {
//		TestaCorr testata = null;
//		//Controllo che sia una persistence unit esistente, verrà cambiato/tolto in futuro.
//		if (nomeRisorsa.startsWith("legacy-") && !riferimentoSpedizione.isEmpty()) {
//			EntityManager em = FactoryManager.getInstance().getFactory(nomeRisorsa).createEntityManager();
//			CriteriaBuilder cb = em.getCriteriaBuilder();
//	        CriteriaQuery<TestaCorr> criteria = cb.createQuery(TestaCorr.class);
//	        Root<TestaCorr> member = criteria.from(TestaCorr.class);
//	        riferimentoSpedizione = riferimentoSpedizione.replaceAll("DDT", "");
//	        riferimentoSpedizione = riferimentoSpedizione.replaceAll("/", "");
//	        Predicate condizioneRiferimentoLike = cb.like(member.get("mittenteAlfa"), "%" + riferimentoSpedizione + "%");
//	        //Predicate condizioneRiferimento = cb.equal(member.get("mittenteAlfa"), riferimentoSpedizione);
//	        //FIXME - Per ora vado a controllare solo il riferimento, se funziona bene.
//	        //criteria.select(member).where(cb.and(condizioneRiferimento, condizioneDestinatario));
//	        criteria.select(member).where(condizioneRiferimentoLike);
//	        List<TestaCorr> list = em.createQuery(criteria).setMaxResults(2).getResultList();
//	        testata = list.size() == 1 ? list.get(0) : null;
//	        if (testata != null)
//	        	aggiornamentiRiferimento += 1;
//	        //Se non sono riuscito a trovarla così ritento
//	        if (testata == null && !destinatarioSpedizione.isEmpty()) {
//	        	GregorianCalendar data = new GregorianCalendar();
//	        	data.setTime(dataSpedizione);
//	        	int anno = data.get(Calendar.YEAR);
//	        	int giorno = data.get(Calendar.DAY_OF_MONTH);
//	        	data.set(Calendar.DAY_OF_MONTH, giorno - 2);
//	        	int meseGiornoInizio = (data.get(Calendar.MONTH) + 1) * 100 + data.get(Calendar.DAY_OF_MONTH);
//	        	data.set(Calendar.DAY_OF_MONTH, giorno + 4);
//	        	int meseGiornoFine = (data.get(Calendar.MONTH) + 1) * 100 + data.get(Calendar.DAY_OF_MONTH);
//	        	Predicate condizioneData = cb.between(member.get("dataSpe"), meseGiornoInizio, meseGiornoFine);
//	        	Predicate condizioneAnno = cb.equal(member.get("annoSpe"), anno);
//	        	Predicate condizioneDestinatario = cb.equal(member.get("ragSocDest"), destinatarioSpedizione);
//	        	criteria.select(member).where(cb.and(condizioneAnno, condizioneData, condizioneDestinatario));
//		        List<TestaCorr> list2 = em.createQuery(criteria).setMaxResults(2).getResultList();
//		        testata = list2.size() == 1 ? list2.get(0) : null;
//		        if (testata != null)
//		        	aggiornamentiNomeData += 1;
//	        }
//	        em.close();
//		}
//		if (testata != null) {
//			System.out.println("Testata trovata! Pezzi: " + testata.getPezzi());
//		}
//		return testata;
//	}

}
