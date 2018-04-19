package it.ltc.corrieri.brt.importazionefile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import it.ltc.database.dao.FactoryManager;
import it.ltc.database.model.centrale.Indirizzo;
import it.ltc.database.model.centrale.Spedizione;
import it.ltc.database.model.centrale.Spedizione.Fatturazione;
import it.ltc.database.model.centrale.Spedizione.TipoSpedizione;

public class ImportatoreTestoPDF {
	
	private static ImportatoreTestoPDF instance;

	private final SimpleDateFormat sdf;
	
	private int arrayIndex;
	
	private int commessa;
	private int documento;
	private String codiceCliente;
	private String db;
	
	private ImportatoreTestoPDF() {
		sdf = new SimpleDateFormat("yyyyddMM");
	}

	public static ImportatoreTestoPDF getInstance() {
		if (instance == null) {
			instance = new ImportatoreTestoPDF();
		}
		return instance;
	}
	
	public int getCommessa() {
		return commessa;
	}

	public void setCommessa(int commessa) {
		this.commessa = commessa;
	}

	public int getDocumento() {
		return documento;
	}

	public void setDocumento(int documento) {
		this.documento = documento;
	}

	public String getCodiceCliente() {
		return codiceCliente;
	}

	public void setCodiceCliente(String codiceCliente) {
		this.codiceCliente = codiceCliente;
	}

	public String getDb() {
		return db;
	}

	public void setDb(String db) {
		this.db = db;
	}

	public void importaTestoPDF(String path) throws Exception {
		LinkedList<String> lines = leggiFile(path);
		LinkedList<RigaPDF> spedizioni = new LinkedList<>();
		for (String line : lines) {
			RigaPDF spedizione = estrapolaDatiSpedizione(line);
			spedizioni.add(spedizione);
		}
		salvaSpedizioni(spedizioni);
	}
	
	private RigaPDF estrapolaDatiSpedizione(String line) throws Exception {
		RigaPDF riga;
		try {
			String[] datiSpedizione = line.split(" ");
			arrayIndex = 6; 
			String ldv = getLetteraDiVettura(datiSpedizione);
			Date dataPartenza = getDataPartenza(datiSpedizione);
			String riferimento = datiSpedizione[3];
			String provincia = datiSpedizione[5];
			String ragioneSociale = getRagioneSociale(datiSpedizione);
			String cap = getCap(datiSpedizione);
			int colli = getColli(datiSpedizione);
			double volume = getVolume(datiSpedizione);
			double peso = getPeso(datiSpedizione);
			double costo = getCosto(datiSpedizione);
			riga = new RigaPDF(ldv, dataPartenza, riferimento, provincia, ragioneSociale, cap, colli, volume, peso, costo);
		} catch (Exception e) {
			riga = null;
			System.out.println(line);
			throw new RuntimeException("Errore durante la lettura della riga: '" + line + "', errore: " + e);
		}
		return riga;
	}

	private double getCosto(String[] datiSpedizione) {
		Double totale = 0.0;
		for (int index = arrayIndex; index < datiSpedizione.length; index++) {
			String costoParziale = datiSpedizione[index];
			costoParziale = costoParziale.replaceAll("[a-z]", "");
			costoParziale = costoParziale.replaceAll("[A-Z]", "");
			costoParziale = costoParziale.replace(',', '.');
			if (!costoParziale.isEmpty()) {
				Double parziale = Double.parseDouble(costoParziale);
				totale += parziale;
			}
		}
		return totale;
	}

	private double getPeso(String[] datiSpedizione) {
		Double pesoReale = Double.parseDouble(datiSpedizione[arrayIndex].replace(',', '.'));
		arrayIndex += 1;
		String prossimoValore = datiSpedizione[arrayIndex];
		if (prossimoValore.matches("^\\d+(\\,\\d+)?$"))
			arrayIndex += 1;
		return pesoReale;
	}

	private double getVolume(String[] datiSpedizione) {
		Double volume = Double.parseDouble(datiSpedizione[arrayIndex].replace(',', '.'));
		arrayIndex += 1;
		return volume;
	}

	private int getColli(String[] datiSpedizione) {
		Integer colli = Integer.parseInt(datiSpedizione[arrayIndex]);
		arrayIndex += 1;
		return colli;
	}

	private String getCap(String[] datiSpedizione) {
		String cap = datiSpedizione[arrayIndex];
		arrayIndex += 1;
		return cap;
	}

	private String getRagioneSociale(String[] datiSpedizione) {
		String ragioneSociale = datiSpedizione[arrayIndex];
		for (int index = arrayIndex + 1; index < datiSpedizione.length; index++) {
			String s = datiSpedizione[index];
			if (s.matches("\\d{5}")) {
				arrayIndex = index;
				break;
			} else {
				ragioneSociale = ragioneSociale + " " + s;
			}
		}
		return ragioneSociale;
	}

	private Date getDataPartenza(String[] datiSpedizione) throws Exception {
		String annoMeseGiorno = "2017" + datiSpedizione[2];
		Date date = sdf.parse(annoMeseGiorno);
		return date;
	}

	private String getLetteraDiVettura(String[] datiSpedizione) {
		String filialePartenza = datiSpedizione[0];
		String numeroSerie = "00";
		String numeroSpedizione = datiSpedizione[1];
		while (numeroSpedizione.length() < 7)
			numeroSpedizione = "0" + numeroSpedizione;
		String ldv = filialePartenza + numeroSerie + numeroSpedizione;
		return ldv;
	}

	private void salvaSpedizioni(LinkedList<RigaPDF> spedizioni) {
		EntityManager em = FactoryManager.getInstance().getFactory(db).createEntityManager();
		EntityTransaction transaction = em.getTransaction();
		try {
    		transaction.begin();
    		for (RigaPDF riga : spedizioni) {
    			Indirizzo destinazione = getDestinazione(riga);
    			Spedizione spedizione = getSpedizione(riga);
    			em.persist(destinazione);
    			spedizione.setIndirizzoDestinazione(destinazione.getId());
    			em.persist(spedizione);
    		}
        	transaction.commit();
    	} catch (Exception e) {
    		if (transaction != null && transaction.isActive())
    			transaction.rollback();
    		e.printStackTrace();
    		throw new RuntimeException("Impossibile inserire la spedizione.");
    	} finally {
    		em.close();
    	}	
	}

	private Indirizzo getDestinazione(RigaPDF riga) {
		Indirizzo destinazione = new Indirizzo();
		destinazione.setCap(riga.getCap());
		destinazione.setConsegnaAlPiano(false);
		destinazione.setConsegnaAppuntamento(false);
		destinazione.setConsegnaGdo(false);
		destinazione.setConsegnaPrivato(false);
		destinazione.setIndirizzo("");
		destinazione.setLocalita("");
		destinazione.setNazione("ITA");
		destinazione.setProvincia(riga.getProvincia());
		destinazione.setRagioneSociale(riga.getRagioneSociale());
		return destinazione;
	}

	private Spedizione getSpedizione(RigaPDF riga) {
		Spedizione spedizione = new Spedizione();
		spedizione.setAssicurazione(false);
		spedizione.setCodiceCliente(codiceCliente);
		spedizione.setColli(riga.getColli());
		spedizione.setContrassegno(false);
		spedizione.setCosto(riga.getCosto());
		spedizione.setDataPartenza(riga.getDataPartenza());
		spedizione.setDatiCompleti(true);
		spedizione.setFatturazione(Fatturazione.NON_FATTURABILE);
		spedizione.setGiacenza(false);
		spedizione.setIdCommessa(commessa);
		spedizione.setIdCorriere(1);
		spedizione.setIdDocumento(documento);
		spedizione.setIndirizzoPartenza(1);
		spedizione.setInRitardo(false);
		spedizione.setLetteraDiVettura(riga.getLdv());
		spedizione.setParticolarita(false);
		spedizione.setPeso(riga.getPeso());
		spedizione.setPezzi(0);
		spedizione.setRagioneSocialeDestinatario(riga.getRagioneSociale());
		spedizione.setRiferimentoCliente(riga.getRiferimento());
		spedizione.setRiferimentoMittente(riga.getRiferimento());
		spedizione.setServizio("DEF");
		spedizione.setStato("IMP");
		spedizione.setTipo(TipoSpedizione.ITALIA);
		spedizione.setVolume(riga.getVolume());
		return spedizione;
	}

	private LinkedList<String> leggiFile(String path) throws Exception {
		FileReader input = new FileReader(path);
		BufferedReader reader = new BufferedReader(input);
		LinkedList<String> lines = new LinkedList<>();
		String line = reader.readLine();
		while (line != null) {
			if (line.startsWith("f")) {
				String lastLine = lines.removeLast();
				line = lastLine + " " + line;
			}
			lines.add(line);
			line = reader.readLine();
		}
		reader.close();
		return lines;
	}
	
	public void scriviRigheSuFile(String path, List<String> lines) throws Exception {
		FileWriter output = new FileWriter(path);
		BufferedWriter writer = new BufferedWriter(output);
		for (String s : lines) {
			writer.write(s);
			writer.newLine();
		}
		writer.flush();
		writer.close();
	}

}
