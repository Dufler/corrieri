package it.ltc.bartolini;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;

import it.ltc.utility.mail.Email;
import it.ltc.utility.mail.MailMan;

public class MainImportazioneBartolini {
	
	private static final String SPEDIZIONE = "FNVAB";
	private static final String GIACENZA = "FNVAG";
	
	private static final Logger logger = Logger.getLogger("Main");
	
	public static void main(String[] args) {
		logger.info("Avvio la procedura di importazione dati BRT");
		ConfigurationUtility config = ConfigurationUtility.getInstance();
		//Database db = config.getDb();
		ImportatoreGiacenze ig = ImportatoreGiacenze.getInstance();
		ImportatoreSpedizioni is = ImportatoreSpedizioni.getInstance();
		ImportatoreEsiti ie = ImportatoreEsiti.getInstance();
		//Importatore importatore = Importatore.getInstance(db);
		String pathCartellaEsiti = config.getLocalFolder();
		String pathStoricoCartellaEsiti = config.getLocalArchiveFolder();
		String pathCartellaErrori = config.getErrorFolder();
		String pathCartellaControllo = config.getCheckFolder();
		File cartellaEsiti = new File(pathCartellaEsiti);
		for (File fileEsito : cartellaEsiti.listFiles()) {	
			if (fileEsito.isFile()) {
				try {
					//Verifico se è un file di controllo, se si lo salto.
					if (fileEsito.getName().endsWith("CHK")) {
						File fileStorico = new File(pathCartellaControllo + fileEsito.getName());
						fileEsito.renameTo(fileStorico);
						logger.info("Spostato il file di controllo : " + fileEsito.getName() + " in " + pathCartellaControllo);
					} else {
						boolean importazione;
						String tipoFile = fileEsito.getName().substring(0, 5);					
						switch (tipoFile) {
							case SPEDIZIONE : importazione = is.importaFNVAB(fileEsito); break;
							case GIACENZA : importazione = ig.importaFNVAG(fileEsito); break;
							default : importazione = false;
						}
						if (importazione) {
							File fileStorico = new File(pathStoricoCartellaEsiti + fileEsito.getName());
							fileEsito.renameTo(fileStorico);
							logger.info("Spostato il file: " + fileEsito.getName() + " in " + pathStoricoCartellaEsiti);
						} else {
							File fileErrori = new File(pathCartellaErrori + fileEsito.getName());
							fileEsito.renameTo(fileErrori);
							logger.info("Spostato il file con errori: " + fileEsito.getName() + " in " + pathCartellaErrori);
						}
					}
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
					String message = "Errore durante l'elaborazione del file " + fileEsito.getName() + ", controllare il file di log.";
					sendAlertEmail(message);
				}
			} 
		}
		logger.info("Procedo con l'importazione degli esiti delle spedizioni non ancora chiuse chiamando il WS Bartolini.");
		ie.importaEsiti();
		logger.info("Fine procedura.");
	}
	
	private static void sendAlertEmail(String message) {
		ConfigurationUtility config = ConfigurationUtility.getInstance();
		List<String> destinatari = config.getIndirizziResponsabili();
		String subject = "Alert - Errore durante l'importazione dati BRT";
		MailMan mailer = config.getMailMan();
		Email mail = new Email(subject, message);
		boolean invio = mailer.invia(destinatari, mail);
		if (!invio)
			logger.error("Impossibile inviare la mail di alert!");
	}
	
}