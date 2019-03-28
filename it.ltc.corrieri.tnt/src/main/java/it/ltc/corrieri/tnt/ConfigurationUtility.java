package it.ltc.corrieri.tnt;

import it.ltc.utility.configuration.ConfigurationParserWithUtils;

/**
 * Classe helper che aiuta la nella configurazione dei parametri e restituisce
 * oggetti già valorizzati in base al contenuto di "settings.properties"
 * 
 * @author Damiano
 *
 */
public class ConfigurationUtility extends ConfigurationParserWithUtils {

	public static final String configPath = "/settings.properties";

	private static ConfigurationUtility instance;

	private final boolean test;
	private final boolean verbose;

	// DB
	private final String persistenceUnit;

	// Cartelle Locali
	private final String localFolder;
	private final String localArchiveFolder;
	private final String localErrorFolder;

	// FTP
	private final String ftpHost;
	private final int ftpPort;
	private final String ftpUser;
	private final String ftpPassword;
	private final String ftpFolder;

	private ConfigurationUtility() {
		super(configPath);
		// Test
		test = Boolean.parseBoolean(configuration.get("test"));
		// Verbose
		verbose = Boolean.parseBoolean(configuration.get("verbose"));
		// FTP
		ftpHost = configuration.get("ftp_host");
		ftpPort = Integer.parseInt(configuration.get("ftp_port"));
		ftpUser = configuration.get("ftp_user");
		ftpPassword = configuration.get("ftp_password");
		ftpFolder = configuration.get("ftp_remotePath");
		// Cartelle locali
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

}
