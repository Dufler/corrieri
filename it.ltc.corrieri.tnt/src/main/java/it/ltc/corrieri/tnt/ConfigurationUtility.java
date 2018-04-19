package it.ltc.corrieri.tnt;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import it.ltc.utility.configuration.Configuration;
import it.ltc.utility.mail.MailMan;

/**
 * Classe helper che aiuta la nella configurazione dei parametri e restituisce
 * oggetti già valorizzati in base al contenuto di "settings.properties"
 * 
 * @author Damiano
 *
 */
public class ConfigurationUtility {
	
	private static final Logger logger = Logger.getLogger("ConfigurationUtility");
	
	public static final String configPath = "/settings.properties";

	private static ConfigurationUtility instance;

	private final Configuration configuration;
	
	private final boolean test;
	private final boolean verbose;
	
	//DB
	private final String persistenceUnit;

	//Cartelle Locali
	private final String localFolder;
	private final String localArchiveFolder;
	private final String localErrorFolder;
	
	//FTP
	private final String ftpHost;
	private final int ftpPort;
	private final String ftpUser;
	private final String ftpPassword;
	private final String ftpFolder;
	
	private ConfigurationUtility() {
		try {
			InputStream stream = ConfigurationUtility.class.getResourceAsStream(configPath);
			logger.info(stream);
			configuration = new Configuration(stream, false);
			//Test
			test = Boolean.parseBoolean(configuration.get("test"));
			logger.info("Testing? " + test);
			//Verbose
			verbose = Boolean.parseBoolean(configuration.get("verbose"));
			logger.info("Verbose? " + verbose);
			//FTP
			ftpHost = configuration.get("ftp_host");
			ftpPort = Integer.parseInt(configuration.get("ftp_port"));
			ftpUser = configuration.get("ftp_user");
			ftpPassword = configuration.get("ftp_password");
			ftpFolder = configuration.get("ftp_remotePath");
			//Cartelle locali
			if (test) {
				localFolder = configuration.get("test_path_cartella");
				localArchiveFolder = configuration.get("test_path_cartella_storico");
				localErrorFolder = configuration.get("test_path_cartella_errori");
				persistenceUnit = configuration.get("test_persistence_unit_name");
			} else {
				localFolder = configuration.get("path_cartella");
				localArchiveFolder = configuration.get("path_cartella_storico");
				localErrorFolder = configuration.get("path_cartella_errori");
				persistenceUnit = configuration.get("persistence_unit_name");
			}
		} catch (IOException e) {
			logger.error(e);
			String errorMessage = "Impossibile caricare i files di configurazione.";
			throw new RuntimeException(errorMessage);
		}
	}

	public static ConfigurationUtility getInstance() {
		if (null == instance) {
			instance = new ConfigurationUtility();
		}
		return instance;
	}
	
	public boolean isTest() {
		return test;
	}

	public boolean isVerbose() {
		return verbose;
	}

	public String getLocalFolder() {
		return localFolder;
	}

	public String getLocalArchiveFolder() {
		return localArchiveFolder;
	}
	
	public String getLocalErrorFolder() {
		return localErrorFolder;
	}

	public String getFtpHost() {
		return ftpHost;
	}

	public int getFtpPort() {
		return ftpPort;
	}

	public String getFtpUser() {
		return ftpUser;
	}

	public String getFtpPassword() {
		return ftpPassword;
	}

	public String getFtpFolder() {
		return ftpFolder;
	}

	public String getPersistenceUnit() {
		return persistenceUnit;
	}

	/**
	 * Restituisce il postino.
	 * 
	 * @return un MailMan gia' configurato.
	 */
	public MailMan getMailMan() {
		String mailUser = configuration.get("email_mittente_indirizzo");
		String mailPassword = configuration.get("email_mittente_password");
		MailMan mm = new MailMan(mailUser, mailPassword, true);
		return mm;
	}

	/**
	 * Restituisce la lista di indirizzi dei destinatari.
	 * 
	 * @return una lista di indirizzi mail a cui verranno spedite le notifiche.
	 */
	public List<String> getIndirizziDestinatari() {
		List<String> destinatari = new LinkedList<String>();
		String indirizzi = configuration.get("email_destinatari_indirizzi");
		for (String indirizzo : indirizzi.split(","))
			destinatari.add(indirizzo);
		return destinatari;
	}

	/**
	 * Restituisce la lista di indirizzi dei destinatari.
	 * 
	 * @return una lista di indirizzi mail a cui verranno spedite le notifiche.
	 */
	public List<String> getIndirizziResponsabili() {
		List<String> destinatari = new LinkedList<String>();
		String indirizzi = configuration.get("email_destinatari_responsabili_indirizzi");
		for (String indirizzo : indirizzi.split(","))
			destinatari.add(indirizzo);
		return destinatari;
	}
}
