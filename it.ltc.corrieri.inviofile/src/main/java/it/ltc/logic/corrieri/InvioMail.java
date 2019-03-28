package it.ltc.logic.corrieri;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import it.ltc.database.model.legacy.sede.CorrieriPerCliente;
import it.ltc.logic.InvioFileCorriereMain;
import it.ltc.utility.mail.MailMan;

public abstract class InvioMail extends Corriere {
	
	protected CorrieriPerCliente configurazioneCliente;
	
	protected MailMan postino;
	protected String indirizzoCorriere;
	protected String indirizzoResponsabili;
	protected List<String> destinatariCorriere;
	protected List<String> destinatariRiepilogo;
	
	public InvioMail(CorrieriPerCliente cliente) {
		super(cliente);
		configurazioneCliente = cliente;
	}

	@Override
	public abstract void inviaDati() throws Exception;
	
	protected void setup() throws Exception {
		ragioneSocialeCliente = configurazioneCliente.getCliente();
		corriere = configurazioneCliente.getCorriere();
		//Controllo sul codice cliente
		codiceCliente = configurazioneCliente.getCodiceCliente();
		if (codiceCliente == null || codiceCliente.isEmpty())
			throw new Exception("Non è stato specificato un codice cliente per il corriere.");
		//Controlli sugli indirizzi mail specificati
		indirizzoCorriere = configurazioneCliente.getMailCorriere();
		if (indirizzoCorriere == null || indirizzoCorriere.isEmpty()) {
			throw new Exception("Non è stato specificato l'indirizzo del corriere a cui inviare i file.");
		} else {
			destinatariCorriere = new ArrayList<String>();
			String[] indirizzi = indirizzoCorriere.split(",");
			for (String indirizzo : indirizzi) {
				if (indirizzo != null && !indirizzo.isEmpty())
				destinatariCorriere.add(indirizzo);
			}
			if (destinatariCorriere.isEmpty())
				throw new Exception("Non è stato specificato l'indirizzo del corriere a cui inviare i file.");
		}
		indirizzoResponsabili = configurazioneCliente.getMailResponsabile();
		if (indirizzoResponsabili == null || indirizzoResponsabili.isEmpty()) {
			throw new Exception("Non è stato specificato l'indirizzo del responsabile a cui inviare il riepilogo.");
		} else {
			destinatariRiepilogo = new ArrayList<String>();
			String[] indirizzi = indirizzoResponsabili.split(",");
			for (String indirizzo : indirizzi) {
				if (indirizzo != null && !indirizzo.isEmpty())
					destinatariRiepilogo.add(indirizzo);
			}
			if (destinatariRiepilogo.isEmpty())
				throw new Exception("Non è stato specificato l'indirizzo del responsabile a cui inviare il riepilogo.");
		}
		//Controlli sulle cartelle specificate
		pathCartellaFile = configurazioneCliente.getCartellaFile();
		pathCartellaStorico = configurazioneCliente.getCartellaStorico();
		cartellaFile = new File(pathCartellaFile);
		if (!cartellaFile.isDirectory())
			throw new IOException("La cartella specificata per i file non esiste: '" + cartellaFile.getAbsolutePath() + "'");
		cartellaStorico = new File(pathCartellaStorico);
		if (!cartellaStorico.isDirectory())
			throw new IOException("La cartella specificata per lo storico non esiste: " + cartellaStorico.getAbsolutePath() + "'");
		//Spedizione delle mail
		postino = InvioFileCorriereMain.getPostino();
	}

}
