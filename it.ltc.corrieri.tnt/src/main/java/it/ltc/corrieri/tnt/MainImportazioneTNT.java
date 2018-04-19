package it.ltc.corrieri.tnt;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import it.ltc.corrieri.tnt.model.FileTNT;
import it.ltc.utility.mail.Email;
import it.ltc.utility.mail.MailMan;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPFile;

public class MainImportazioneTNT {
	
	private static final Logger logger = Logger.getLogger("Main");
	
	private static boolean test;
	private static int port;
	private static String host;
	private static String user;
	private static String password;
	private static String remotePath;
	private static String localPath;
	private static String storicPath;
	private static String errorPath;
	private static MailMan mm;
	private static List<String> destinatariTraffic;
	private static List<String> destinatariIT;

	public static void main(String[] args) {
		logger.info("Avvio procedura.");
		setup();
		downloadFiles();
		importData();
		logger.info("Termine procedura.");
	}
	
	private static void setup() {
		logger.info("Caricamento delle impostazioni.");
		ConfigurationUtility config = ConfigurationUtility.getInstance();
		test = config.isTest();
		port = config.getFtpPort();
		host = config.getFtpHost();
		user = config.getFtpUser();
		password = config.getFtpPassword();
		remotePath = config.getFtpFolder();
		localPath = config.getLocalFolder();
		storicPath = config.getLocalArchiveFolder();
		errorPath = config.getLocalErrorFolder();
		mm = config.getMailMan();
		destinatariTraffic = config.getIndirizziDestinatari();
		destinatariIT = config.getIndirizziResponsabili();
	}

	private static void importData() {
		logger.info("(Import) Recupero i file da importare.");
		Importatore importatore = Importatore.getInstance();
		File folder = new File(localPath);
		String[] fileList = folder.list();
		List<String> filesToImport = new LinkedList<>();
		//Prendo solo i file veri e propri e li ordino.
		for (String fileName : fileList) {
			File file = new File(localPath + fileName);
			if (file.isDirectory())
				continue;
			else
				filesToImport.add(fileName);
		}
		//Se c'è qualche file da importare procedo.
		if (!filesToImport.isEmpty()) {
			filesToImport.sort(null);
			logger.info("(Import) Stanno per essere importati " + filesToImport.size() + " files.");
			int imports = 0;
			List<String> errors = new LinkedList<>();
			for (String fileName : filesToImport) {
				File file = new File(localPath + fileName);
				logger.info("(Import) Importazione del file '" + fileName + "'.");
				FileTNT fileTNT = new FileTNT(file);
				boolean success = importatore.importaFile(fileTNT);
				File destination = success ? new File(storicPath + file.getName()) : new File(errorPath + file.getName());
				boolean move = file.renameTo(destination);
				if (move) {
					imports += 1;
					String message = "Spostato il file " + file.getName() + " nella cartella " + destination.getParent();
					logger.info(message);
				} else {
					errors.add(file.getName());
					String message = "Impossibile spostare il file " + file.getName(); 
					logger.error(message);
					inviaMail(destinatariIT, "Errore Importazione TNT", message);
				}
			}
			logger.info("Sono stati importati con successo " + imports + " su " + filesToImport.size() + " files");
			//Notifica degli errori, se presenti.
			if (!errors.isEmpty()) {
				String message = "Si è tentato di importare " + errors.size() + " files ma sono andati in errore.";
				logger.error(message);
				String subject = "Importazione Dati TNT";
				message += "\r\n" + "File con errori:";
				for (String file : errors) {
					message += "\r\n" + file;
				}
				inviaMail(destinatariIT, subject, message);
			}
			//Notifica delle spedizioni in ritardo, se presenti
			Set<String> spedizioniInRitardo = importatore.getSpedizioniInRitardo();
			if (!spedizioniInRitardo.isEmpty()) {
				logger.info("Notifico agli operatori la lista delle spedizioni in ritardo.");
				String subject = "Alert - Spedizioni In Ritardo";
				String message = "Le seguenti spedizioni sono in ritardo:\r\n";
				for (String ldv : spedizioniInRitardo) {
					message += "- " + ldv + "\r\n";
				}
				inviaMail(destinatariTraffic, subject, message);
			}
		} else {
			logger.info("(Import) Nessun file da importare.");
		}
		logger.info("(Import) Termine dell'importazione.");
	}
	
	private static void inviaMail(List<String> riceventi, String subject, String message) {
		Email mail = new Email(subject, message);
		boolean invio = mm.invia(riceventi, mail);
		if (!invio) {
			logger.error("Impossibile inviare la mail: ");
			logger.error(message);
		}
	}

	private static void downloadFiles() {
		if (test) {
			logger.info("(FTP) Testing: Salto la fase di download.");
		} else try {
			logger.info("(FTP) Inizializzo le variabili, per ora sono statiche.");
			logger.info("(FTP) Creo il client FTP e mi connetto.");
			FTPClient client = new FTPClient();
			client.connect(host, port);
			client.login(user, password);
			logger.info("(FTP) Scarico i files.");
			client.changeDirectory(remotePath);
			FTPFile[] list = client.list();
			for (FTPFile file : list) {
				String remoteFile = file.getName();
				File downloadedFile = new File(localPath + remoteFile);
				client.download(remoteFile, downloadedFile);
				logger.info("(FTP) Scaricato: '" + remoteFile + "'.");
				client.deleteFile(remoteFile);
			}
			logger.info("(FTP) Termino la sessione.");
			client.disconnect(true);
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}

}
