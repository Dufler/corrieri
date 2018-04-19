package it.ltc.bartolini;

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
	private final String db;

	//Cartelle Locali
	private final String localFolder;
	private final String localArchiveFolder;
	private final String localErrorFolder;
	private final String localCheckFolder;

	private ConfigurationUtility() {
		try {
			InputStream stream = ConfigurationUtility.class.getResourceAsStream(configPath);
			logger.info(stream);
			configuration = new Configuration(stream, false);
			//Test
			test = Boolean.parseBoolean(configuration.get("test"));
			logger.info("Testing? ");
			//Verbose
			verbose = Boolean.parseBoolean(configuration.get("verbose"));
			logger.info("Verbose? ");
			//Cartelle locali
			if (test) {
				localFolder = configuration.get("test_path_cartella");
				localArchiveFolder = configuration.get("test_path_cartella_storico");
				localErrorFolder = configuration.get("test_path_cartella_errori");
				localCheckFolder = configuration.get("test_path_cartella_check");
				db = configuration.get("test_persistence_unit_name");
			} else {
				localFolder = configuration.get("path_cartella");
				localArchiveFolder = configuration.get("path_cartella_storico");
				localErrorFolder = configuration.get("path_cartella_errori");
				localCheckFolder = configuration.get("path_cartella_check");
				db = configuration.get("persistence_unit_name");
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

	public boolean isVerbose() {
		return verbose;
	}

	public String getLocalFolder() {
		return localFolder;
	}

	public String getLocalArchiveFolder() {
		return localArchiveFolder;
	}
	
	public String getErrorFolder() {
		return localErrorFolder;
	}
	
	public String getCheckFolder() {
		return localCheckFolder;
	}


	public String getDb() {
		return db;
	}

	/**
	 * Restituisce il postino.
	 * 
	 * @return un MailMan gia' configurato.
	 */
	public MailMan getMailMan() {
		String mailUser = configuration.get("email_mittente_indirizzo");
		String mailPassword = configuration.get("email_mittente_password");
		MailMan mm = new MailMan(mailUser, mailPassword);
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
