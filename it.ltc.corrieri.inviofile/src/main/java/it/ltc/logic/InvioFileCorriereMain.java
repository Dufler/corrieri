package it.ltc.logic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import it.ltc.logic.corrieri.Corriere;
import it.ltc.logic.corrieri.FactoryCorrieri;
import it.ltc.logica.database.configuration.Database;
import it.ltc.logica.database.model.sqlserver.ltc.CorrieriPerCliente;
import it.ltc.logica.database.orm.EntityManager;
import it.ltc.utility.configuration.Configuration;
import it.ltc.utility.mail.Email;
import it.ltc.utility.mail.MailMan;

public class InvioFileCorriereMain {
	
	private static final Logger logger = Logger.getLogger(InvioFileCorriereMain.class);
	
	private static final String PATH_CONFIGURAZIONE_EMAIL = "/email.properties";
	private static final String PATH_CONFIGURAZIONE_DB = "/database.properties";
	
	private static EntityManager<CorrieriPerCliente> managerClienti;
	
	private static MailMan postino;
	private static List<String> destinatariErrore;

	public static void main(String[] args) throws IOException {
		logger.info("Inizio Procedura");
		//Setup email
		setupEmail();
		//Setup DB
		setupDB();
		//Recupero la lista dei clienti da gestire
		List<CorrieriPerCliente> listaClienti = managerClienti.getEntities();
		//Per ogni cliente controllo la cartella specificata alla ricerca dei file per il corriere
		for (CorrieriPerCliente cliente : listaClienti) {
			try {
				Corriere corriere = FactoryCorrieri.getInstance(cliente);
				corriere.inviaDati();
			} catch (Exception e) {
				//Loggo l'accaduto.
				e.printStackTrace();
				String oggettoErrore = "Alert: errore nell'invio del file per il corriere";
				String messaggioErrore = oggettoErrore + " per il cliente " + cliente.getCliente() + "\r\n\r\n" + e.getMessage();
				logger.error(messaggioErrore);
				Email mail = new Email(oggettoErrore, messaggioErrore);
				postino.invia(destinatariErrore, mail);
			}
		}
		logger.info("Fine Procedura");
	}
	
	private static void setupEmail() throws IOException {
		Configuration configurazioneEmail = new Configuration(PATH_CONFIGURAZIONE_EMAIL, false);
		String username = configurazioneEmail.get("email_mittente_indirizzo");
		String password = configurazioneEmail.get("email_mittente_password");
		postino = new MailMan(username, password);
		destinatariErrore = new ArrayList<String>();
		destinatariErrore.add("support@ltc-logistics.it");
	}
	
	private static void setupDB() throws IOException {
		Configuration configurazioneDB = new Configuration(PATH_CONFIGURAZIONE_DB, false);
		String nomeDB = configurazioneDB.get("db");
		Database db = Database.valueOf(nomeDB);
		managerClienti = new EntityManager<CorrieriPerCliente>(CorrieriPerCliente.class, db);
	}
	
	public static MailMan getPostino() {
		return postino;
	}

}
