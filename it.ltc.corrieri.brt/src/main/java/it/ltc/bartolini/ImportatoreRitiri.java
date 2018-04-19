package it.ltc.bartolini;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;

import it.ltc.bartolini.model.fnvap.FileFNVAP;
import it.ltc.bartolini.model.fnvap.RigaFNVAP;

public class ImportatoreRitiri {
	
	private static final Logger logger = Logger.getLogger("Importazione Ritiri");
	
	private static ImportatoreRitiri instance;

	private ImportatoreRitiri() {
	}

	public static ImportatoreRitiri getInstance() {
		if (instance == null) {
			instance = new ImportatoreRitiri();
		}
		return instance;
	}
	
	public boolean importaFNVAP(File file) {
		boolean success = true;
		FileFNVAP fnvap = new FileFNVAP(file);
		List<RigaFNVAP> esiti = fnvap.getEsiti();
		logger.info("Sono state trovati " + esiti.size() + " ritiri nel file '" + file.getName() + "'");
		for (RigaFNVAP esito : esiti) {
			boolean importazione = importaRitiro(esito);
			if (!importazione)
				success = false;
		}
		return success;
	}

	public boolean importaRitiro(RigaFNVAP esito) {
		//TODO
		return false;
	}

}
