package it.ltc.bartolini;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.apache.log4j.Logger;

import it.ltc.bartolini.model.costanti.Nazioni;
import it.ltc.bartolini.model.fnvab.FileFNVAB;
import it.ltc.bartolini.model.fnvab.RigaFNVAB;
import it.ltc.database.dao.FactoryManager;
import it.ltc.database.model.centrale.Commessa;
import it.ltc.database.model.centrale.Indirizzo;
import it.ltc.database.model.centrale.JoinCommessaCorriere;
import it.ltc.database.model.centrale.Sede;
import it.ltc.database.model.centrale.Spedizione;
import it.ltc.database.model.centrale.SpedizioneContrassegno;
import it.ltc.database.model.centrale.enumcondivise.Fatturazione;
import it.ltc.database.model.legacy.TestaCorrLight;


/**
 * Classe singleton che si occupa di importare i dati contenuti nei file FNVAB.
 * @author Damiano
 *
 */
public class ImportatoreSpedizioni extends Importatore {
	
	private static final Logger logger = Logger.getLogger("Importazione Spedizioni");
	
	private static ImportatoreSpedizioni instance;
	
	private final RecuperaTestate rt;
	private final SimpleDateFormat sdf;
	private final List<Sede> sedi;

	private ImportatoreSpedizioni() {
		sdf = new SimpleDateFormat("yyyyMMdd");
		rt = RecuperaTestate.getInstance();
		sedi = getSedi();
	}

	public static ImportatoreSpedizioni getInstance() {
		if (instance == null) {
			instance = new ImportatoreSpedizioni();
		}
		return instance;
	}
	
	public boolean importaFNVAB(File file) {
		boolean success = true;
		FileFNVAB fnvab = new FileFNVAB(file);
		List<RigaFNVAB> esiti = fnvab.getEsiti();
		logger.info("Sono state trovate " + esiti.size() + " spedizioni nel file '" + file.getName() + "'");
		for (RigaFNVAB esito : esiti) {
			boolean importazione = importaSpedizione(esito);
			if (!importazione)
				success = false;
		}
		return success;
	}

	public boolean importaSpedizione(RigaFNVAB esito) {
		boolean success;
		try {
			String cc = esito.getCodiceClienteMittente();
			JoinCommessaCorriere codiceCliente = getCodiceCliente(cc);
			if (codiceCliente != null) {
				Commessa commessa = getCommessa(codiceCliente.getCommessa());
				Date dataSpedizione = sdf.parse(esito.getAnnoSpedizione() + esito.getMeseGiornoSpedizione());
				TestaCorrLight vecchia = rt.recuperaTestata(commessa.getNomeRisorsa(), esito.getRiferimentoMittenteNumerico(), esito.getRagioneSocialeDestinatario(), dataSpedizione);
				String letteraDiVettura = getLetteraDiVettura(esito);
				Spedizione trovata = recuperaSpedizione(codiceCliente.getCodiceCliente(), letteraDiVettura);
				boolean spedizionePresente = (trovata != null);
				if (spedizionePresente) {
					//Aggiorno la spedizione.
					success = aggiornaSpedizione(trovata, esito, vecchia);
				} else {
					//Inserisco la spedizione.
					String riferimento = aggiustaRiferimento(esito.getRiferimentoMittenteNumerico());
					int idDocumento = recuperaDocumento(codiceCliente, riferimento);
					success = inserisciNuovaSpedizione(codiceCliente, idDocumento, vecchia, esito);
				}
			} else {
				success = false;
				//Sonia non ha inserito il codice cliente.
				String message = "Non è stato inserito il codice cliente: '" + cc + "', la spedizione BRT '" + esito.getNumeroSpedizione() + "' è andata persa.";
				logger.error(message);
				sendAlertEmail(message);
			}
		} catch (Exception e) {
			success = false;
			String message = "Eccezione per la spedizione: '" + esito.getNumeroSpedizione() + "', " + e.getMessage(); 
			logger.error(message, e);
			sendAlertEmail(message);
		}
		return success;
	}
	
	/**
	 * Combina il codice della filiale di partenza, il numero di serie e il numero di spedizione per ottenere la lettera di vettura secondo gli standard Bartolini.
	 * Tale lettera di vettura può essere usata nei web services per ottere il tracking della spedizione.
	 */
	private String getLetteraDiVettura(RigaFNVAB esito) {
		String filialeDiPartenza = esito.getFilialePartenza();
		String serie = esito.getNumeroSerie();
		String numeroSpedizione = esito.getNumeroSpedizione();
		String letteraDiVettura = filialeDiPartenza + serie + numeroSpedizione;
		return letteraDiVettura;
	}
	
	private boolean aggiornaSpedizione(Spedizione trovata, RigaFNVAB esito, TestaCorrLight vecchia) {
		boolean update;
		if (vecchia != null && vecchia.getPezzi() > 0) {
			trovata.setPezzi(vecchia.getPezzi());
			trovata.setDatiCompleti(true);
			Double peso = trovata.getPeso();
			if ((peso == null || peso <= 0)) {
				trovata.setPeso(vecchia.getPeso());
			}
			Double volume = trovata.getVolume();
			if ((volume == null || volume <= 0)) {
				trovata.setVolume(vecchia.getVolume());
			}
			EntityManager em = FactoryManager.getInstance().getFactory(persistenceUnitName).createEntityManager();
			EntityTransaction t = em.getTransaction();
			try {
				t.begin();
				em.merge(trovata);
				t.commit();
				update = true;
				logger.info("Spedizione aggiornata: " + trovata.getLetteraDiVettura());
			} catch (Exception e) {
				t.rollback();
				update = false;
			} finally {
				em.close();
			}
		} else {
			update = false;
			logger.info("Spedizione non aggiornata: " + trovata.getLetteraDiVettura());
		}
		return update;
	}
	
	private boolean inserisciNuovaSpedizione(JoinCommessaCorriere codiceCliente, int idDocumento, TestaCorrLight vecchia, RigaFNVAB esito) throws Exception {
		Spedizione spedizione = new Spedizione();
		//Inserisci informazioni spedizione
		spedizione.setIdDocumento(idDocumento);
		spedizione.setAssicurazione(false);
		spedizione.setCodiceCliente(codiceCliente.getCodiceCliente());
		int colli = esito.getNumeroColli() != null ? esito.getNumeroColli() : 0;
		spedizione.setColli(colli);
		Double valoreContrassegno = esito.getImportoContrassegno();
		boolean contrassegno = (valoreContrassegno != null && valoreContrassegno > 0);
		spedizione.setContrassegno(contrassegno);
		String data = esito.getAnnoSpedizione() + esito.getMeseGiornoSpedizione();
		spedizione.setDataPartenza(sdf.parse(data));
		spedizione.setGiacenza(false);
		spedizione.setIdCommessa(codiceCliente.getCommessa());
		spedizione.setIdCorriere(codiceCliente.getCorriere());
		int idMittente = ottieniMittente(codiceCliente);
		Indirizzo destinatario = ottieniDestinatario(esito, spedizione);
		spedizione.setRagioneSocialeDestinatario(destinatario.getRagioneSociale());
		spedizione.setIndirizzoDestinazione(destinatario.getId());
		spedizione.setIndirizzoPartenza(idMittente);
		spedizione.setLetteraDiVettura(getLetteraDiVettura(esito));
		String note = esito.getNote();
		spedizione.setNote(note);
		spedizione.setParticolarita(false);
		double peso = esito.getPeso() != null ? esito.getPeso() : 0;
		if (peso == 0 && vecchia != null) {
			spedizione.setPeso(vecchia.getPeso());
		} else 
			spedizione.setPeso(peso);
		if (vecchia != null && vecchia.getPezzi() > 0)
			spedizione.setPezzi(vecchia.getPezzi());
		else
			spedizione.setPezzi(0);
		String riferimento = aggiustaRiferimento(esito.getRiferimentoMittenteNumerico());
		spedizione.setRiferimentoCliente(riferimento);
		spedizione.setRiferimentoMittente(riferimento);
		spedizione.setServizio(getTipoServizio(esito.getTipoServizioBolle()));
		spedizione.setStato("IMP");
		spedizione.setFatturazione(Fatturazione.FATTURABILE);
		double volume = esito.getVolume() != null ? esito.getVolume() : 0;
		if (volume == 0 && vecchia != null)
			spedizione.setVolume(vecchia.getVolume());
		else
			spedizione.setVolume(volume);
		if (vecchia != null)
			spedizione.setDatiCompleti(true);
		boolean insert;
		EntityManager em = FactoryManager.getInstance().getFactory(persistenceUnitName).createEntityManager();
		EntityTransaction t = em.getTransaction();
		try {
			t.begin();
			em.persist(spedizione);
			t.commit();
			insert = true;
			logger.info("Spedizione inserita: " + spedizione.getId());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			insert = false;
			if (t != null && t.isActive())
				t.rollback();
		} finally {
			em.close();
		}
		if (insert && contrassegno) { //Solo se ha il contrassegno.
			inserisciNuovoContrassegno(spedizione.getId(), esito);
		}
		return insert;
	}
	
	private boolean inserisciNuovoContrassegno(int idSpedizione, RigaFNVAB esito) {
		SpedizioneContrassegno contrassegno = new SpedizioneContrassegno();
		//Inserisci informazioni contrassegno
		contrassegno.setIdSpedizione(idSpedizione);
		contrassegno.setAnnullato(false);
		String tipo = esito.getTipoIncassoContrassegno();
		if (tipo.isEmpty())
			tipo = "NA";
		contrassegno.setTipo(tipo);
		String valuta = esito.getDivisaImportoContrassegno();
		if (valuta.isEmpty())
			valuta = "EUR";
		contrassegno.setValuta(valuta);
		double valore = esito.getImportoContrassegno() != null ? esito.getImportoContrassegno() : 0;
		contrassegno.setValore(valore);
		boolean insert;
		EntityManager em = FactoryManager.getInstance().getFactory(persistenceUnitName).createEntityManager();
		EntityTransaction t = em.getTransaction();
		try {
			t.begin();
			em.persist(contrassegno);
			t.commit();
			insert = true;
			logger.info("Contrassegno inserito: " + idSpedizione);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			insert = false;
			if (t != null && t.isActive())
				t.rollback();
		} finally {
			em.close();
		}
		return insert;
	}
	
	private Indirizzo ottieniDestinatario(RigaFNVAB esito, Spedizione spedizione) {
		String ragioneSociale = esito.getRagioneSocialeDestinatario();
		String estensioneRagioneSociale = esito.getEstensioneRagioneSocialeDestinatario();
		if (!estensioneRagioneSociale.isEmpty())
			ragioneSociale += " " + estensioneRagioneSociale;
		String cap = esito.getCapDestinatario();
		String localita = esito.getLocalitaDestinatario();
		String indirizzo = esito.getIndirizzoDestinatario();
		if (indirizzo.charAt(indirizzo.length() -1) == '.')
			indirizzo = indirizzo.substring(0, indirizzo.length() -1);
		indirizzo = indirizzo.replace("/", " ").trim();
		String provincia = esito.getProvinciaDestinatario();
		if (provincia.isEmpty())
			provincia = "XX";
		String nazione = getNazione(esito.getNazioneDestinatario(), spedizione);
		Indirizzo destinatario = recuperaIndirizzo(ragioneSociale, provincia, indirizzo, localita, cap, nazione);
		return destinatario;
	}
	
	/**
	 * I case ripetivi sono stati inseriti lo stesso per completezza rispecchiando le specifiche Bartolini
	 * @param nazione come trovata sul file di ritorno Bartolini
	 * @return Il codice ISO3 della nazione
	 */
	private String getNazione(String nazione, Spedizione spedizione) {
		try {
			Nazioni n = Nazioni.valueOf(nazione);
			if (n == Nazioni.ITA) {
				spedizione.setTipo(Spedizione.TipoSpedizione.ITALIA);
			} else if (n.isUE()){
				spedizione.setTipo(Spedizione.TipoSpedizione.UE);
			} else {
				spedizione.setTipo(Spedizione.TipoSpedizione.EXTRA_UE);
			}
			nazione = n.getCodiceISO();
		} catch(Exception e) {
			nazione = "ITA";
			spedizione.setTipo(Spedizione.TipoSpedizione.ITALIA);
		}
		return nazione;
	}

	private int ottieniMittente(JoinCommessaCorriere codiceCliente) {
		int id;
		Commessa commessa = getCommessa(codiceCliente.getCommessa());
		Sede sede = null;
		for (Sede s : sedi) {
			if (s.getId() == commessa.getIdSede()) {
				sede = s;
				break;
			}
		}
		if (sede != null) {
			id = sede.getIndirizzo();
		} else {
			logger.info("Impossibile trovare la sede di appartenenza del codice cliente: '" + codiceCliente.getCodiceCliente() + ", verrà utilizzata quella di default.");
			id = 1; //Metto l'indirizzo della sede di default, Perugia.
		}
		return id;
	}

}
