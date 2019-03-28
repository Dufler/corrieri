package it.ltc.bartolini.model.fnvac;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Classe che mappa un file FNVAC
 * 
 * Di seguito vengono riportate le spedifiche di BRT
 * 
 * FNVAC00T – CERTIFICAZIONE DELLE RESE (ESITI CONSEGNA)
 * Trasmissione dati di consegna ai clienti Mittenti
 * Viene trasmesso record per ogni spedizione che ha avuto un evento di consegna identificabile in maniera univoca dai campi: anno spedizione, filiale di partenza, numero serie spedizione, numero spedizione, tipo bolla.
 * 
 * Note Tecniche :
 * 1 - L’ultimo record ricevuto è sempre riaggiornato in tutti i suoi campi: si tratta quindi di una "foto" dalla quale è anche possibile desumere l’andamento degli eventi.
 * 2 - Data lasciato avviso (VACDLA): se il campo è pieno vuol dire che in quella data è stato lasciato l’avviso.
 * 3 - Data giacenza (VACDAG): se il campo è pieno vuol dire che in quella data è stata aperta una pratica di giacenza.
 * 4 - Data consegna (VACDCM) e codice di consegna anomala (VACCCA) seguono invece la seguente logica:
 * a) se il flag di "consegna anomala" è: C (messa in consegna) oppure A (lettera di anomalia), la data indica il giorno in cui è stata inserita la "consegna anomala".
 * b) se esiste solo il flag di “consegna anomala = 1 (dirottamento)” e la data consegna è 0, vuol dire che è successa  l' "anomalia" e la data di esito finale verrà inviata successivamente.
 * c) negli altri casi la data indica la consegna effettiva e il flag di "consegna anomala" indica cosa è successo (consegna anomala blank =consegnata nella data indicata in  VACDCM).
 * 5 - Data consegna merce (VACDCM),  Codice consegna anomala (VACCCA), Data lasciato avviso (VACDLA) e  Data apertura Giacenza (VACDAG): 
 * se i campi suddetti NON risultano valorizzati significa che sono state apportate delle modifiche di altri campi (es: modifica data consegna richiesta, giorni chiusura destinatario ecc.. )
 * 
 * @author Damiano
 *
 */
public class FileFNVAC {
	
	private static final Logger logger = Logger.getLogger(FileFNVAC.class);
	
	public static final String NOME_FILE = "FNVAC";
	public static final String CSV_SEPARATOR = ",";
	
	private final File file;
	private final List<RigaFNVAC> esiti;
	
	private final HashMap<String, Integer> mappaColonne;
	private String rigaMappaColonne;
	private final List<String> righe;
	
	public FileFNVAC(File file) {
		this.file = file;
		this.righe = new LinkedList<String>();
		this.mappaColonne = new HashMap<String, Integer>();
		this.esiti = new LinkedList<RigaFNVAC>();
		leggiFile();
		creaMappa();
		creaEsiti();
	}

	private void creaEsiti() {
		for (String riga : righe) {
			String[] campi = riga.split(CSV_SEPARATOR);
			for (String campo : campi) {
				campo = campo.trim();
			}
			RigaFNVAC esito = new RigaFNVAC(mappaColonne, campi);
			esiti.add(esito);
		}
	}

	private void creaMappa() {
		String[] nomiColonne = rigaMappaColonne.split(CSV_SEPARATOR);
		for (int index = 0; index < nomiColonne.length; index ++) {
			String nomeColonna = nomiColonne[index];
			nomeColonna = nomeColonna.replaceAll("\"", "");
			mappaColonne.put(nomeColonna, index);
		}
	}

	private void leggiFile() {
		try {
			FileReader fileReader = new FileReader(file);
			BufferedReader reader = new BufferedReader(fileReader);
			String line = reader.readLine();
			if (line != null) {
				rigaMappaColonne = line;
				line = reader.readLine();
			}
			while (line != null) {
				righe.add(line);
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public File getFile() {
		return file;
	}

	public List<RigaFNVAC> getEsiti() {
		return esiti;
	}

}
