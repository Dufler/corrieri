package it.ltc.logic.corrieri;

import java.io.File;

import org.apache.log4j.Logger;

import it.ltc.database.model.legacy.sede.CorrieriPerCliente;
import it.ltc.utility.mail.Email;

public class InvioExcel extends InvioMail {
	
	private static final String PREFIX = "LTCGLS";
	private static final String SUFFIX_EXCEL = ".xls";
	private static final String SUFFIX_DAT = ".dat";
	
	private static final Logger logger = Logger.getLogger(InvioExcel.class);
	
	private String nomeCliente;
	
	public InvioExcel(CorrieriPerCliente cliente) {
		super(cliente);
		configurazioneCliente = cliente;
		nomeCliente = configurazioneCliente.getCliente() + " " + configurazioneCliente.getCodiceCliente();
	}

	@Override
	public void inviaDati() throws Exception {
		setup();
		String[] files = cartellaFile.list();
		//Cerco il file excel da inviare
		for (String nomeFile : files) {
			if (nomeFile.startsWith(PREFIX) && (nomeFile.endsWith(SUFFIX_EXCEL) || nomeFile.endsWith(SUFFIX_DAT))) {
				logger.info(nomeCliente + " - Tento di inviare il file: " + nomeFile);
				Email mail = new Email(codiceCliente, MESSAGGIO_CORRIERE);
				File allegatoExcel = new File(pathCartellaFile + nomeFile);
				mail.setAllegato(allegatoExcel);
				boolean invio = postino.invia(destinatariCorriere, mail);
				if (invio) {
					logger.info(nomeCliente + " - File inviato correttamente.");
					//Sposto i file nella cartella storico
					File fileStorico = new File(pathCartellaStorico + nomeFile);
					allegatoExcel.renameTo(fileStorico);
					String messaggioRiepilogo = "I file delle spedizioni per il cliente " + ragioneSocialeCliente + " sono stati inviati al corriere " + corriere;
					String oggettoRiepilogo = "Riepilogo spedizioni per conto di " + ragioneSocialeCliente;
					Email mailRiepilogo = new Email(oggettoRiepilogo, messaggioRiepilogo);
					postino.invia(destinatariRiepilogo, mailRiepilogo);
				} else {
					logger.error(nomeCliente + " - Invio fallito.");
					throw new Exception("Invio della mail fallito.");
				}
			}
		}
	}

}
