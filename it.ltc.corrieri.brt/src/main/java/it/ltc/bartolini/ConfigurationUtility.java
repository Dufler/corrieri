package it.ltc.bartolini;

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
	private final String db;

	// Cartelle Locali
	private final String localFolder;
	private final String localArchiveFolder;
	private final String localErrorFolder;
	private final String localCheckFolder;

	private ConfigurationUtility() {
		super(configPath);
		// Test
		test = Boolean.parseBoolean(configuration.get("test"));
		// Verbose
		verbose = Boolean.parseBoolean(configuration.get("verbose"));
		// Cartelle locali
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

}
