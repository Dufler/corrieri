package it.ltc.logic.corrieri;

import java.io.File;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import it.ltc.logica.database.model.sqlserver.ltc.CorrieriPerCliente;
import it.ltc.utility.mail.Email;

public class BartoliniEsteroMulti extends InvioFileTesto {
	
	private static final Logger logger = Logger.getLogger(BartoliniEsteroMulti.class);
	
	public static final String MESSAGGIO_CORRIERE = "TARIFFA 100COLLO EUROEXPRESS";

	protected BartoliniEsteroMulti(CorrieriPerCliente cliente) {
		super(cliente);
		configurazioneCliente = cliente;
	}
	
	public void inviaDati() throws Exception {
		setup();
		String[] files = cartellaFile.list();
		int totaleSpedizioni = 0;
		int totaleColliSpediti = 0;
		ArrayList<String> filesFNVABDaControllare = new ArrayList<>();
		ArrayList<String> filesFNVATDaControllare = new ArrayList<>();
		for (String nomeFile : files) {
			if (nomeFile.startsWith("FNVAB")) {
				filesFNVABDaControllare.add(nomeFile);
			} else if (nomeFile.startsWith("FNVAT")) {
				filesFNVATDaControllare.add(nomeFile);
			}
		}
		int quatitàFileCorriere = filesFNVABDaControllare.size();
		if (quatitàFileCorriere > 0)
			logger.info("Comincio la verifica di " + filesFNVABDaControllare.size() + " file corriere");
		else
			logger.info("Nessun file corriere trovato.");
		// Verifico di avere entrambi i file per il corriere
		for (String fnvab : filesFNVABDaControllare) {
			String gemelloFnvat = fnvab.replaceFirst("B", "T");
			// Per ogni coppia di file FNVAB e FNVAT trovati invio una mail al
			// corriere specificato
			if (filesFNVATDaControllare.contains(gemelloFnvat)) {
				Email mail = new Email(codiceCliente, MESSAGGIO_CORRIERE);
				File fileFNVAB = new File(pathCartellaFile + fnvab);
				File fileFNVAT = new File(pathCartellaFile + gemelloFnvat);
				//Aggiusto il nome prima di inviarli come allegato
				File fileRinominatoFNVAB = new File(pathCartellaFile + NOME_FILE_FNVAB);
				fileFNVAB.renameTo(fileRinominatoFNVAB);
				File fileRinominatoFNVAT = new File(pathCartellaFile + NOME_FILE_FNVAT);
				fileFNVAT.renameTo(fileRinominatoFNVAT);
				mail.setAllegato(fileRinominatoFNVAB);
				mail.setAllegato(fileRinominatoFNVAT);
				//Invio al corriere i dati sulla spedizione
				boolean invio = postino.invia(destinatariCorriere, mail);
				if (invio) {
					//Incremento i totali delle spedizioni e colli inviati
					totaleSpedizioni += contaRighe(fileRinominatoFNVAB);
					totaleColliSpediti += contaRighe(fileRinominatoFNVAT);
					//Sposto i file nella cartella storico
					File fileStoricoFNVAB = new File(pathCartellaStorico + fnvab);
					fileRinominatoFNVAB.renameTo(fileStoricoFNVAB);
					File fileStoricoFNVAT = new File(pathCartellaStorico + gemelloFnvat);
					fileRinominatoFNVAT.renameTo(fileStoricoFNVAT);
					logger.info("File " + fnvab + " inviato al corriere e spostato nello storico.");
				} else {
					String messaggio = "Impossibile inviare il file " + fnvab + " tramite email agli indirizzi del corriere specificari";
					logger.error(messaggio);
					throw new Exception(messaggio);
				}
			} else {
				throw new RuntimeException("Ho trovato un FNVAB senza il suo FNVAT, nome del file: " + fnvab);
			}
		}
		if (quatitàFileCorriere > 0 && totaleSpedizioni > 0) {
			// Mando una mail di riepilogo al responsabile di filiale
			String messaggioRiepilogo = "I file delle spedizioni per il cliente " + ragioneSocialeCliente + " sono stati inviati al corriere " + corriere + ".\r\n\r\nTotale spedizioni: " + totaleSpedizioni + "\r\nTotale colli: " + totaleColliSpediti;
			String oggettoRiepilogo = "Riepilogo spedizioni per conto di " + ragioneSocialeCliente;
			Email mail = new Email(oggettoRiepilogo, messaggioRiepilogo);
			postino.invia(destinatariRiepilogo, mail);
		}	
	}

}
