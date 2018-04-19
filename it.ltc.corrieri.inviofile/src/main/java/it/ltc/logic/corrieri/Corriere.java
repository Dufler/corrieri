package it.ltc.logic.corrieri;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import it.ltc.logica.database.model.sqlserver.ltc.CorrieriPerCliente;


public abstract class Corriere {
	
	public static final String MESSAGGIO_CORRIERE = "";
	
	protected CorrieriPerCliente configurazioneCliente;
	
	protected String corriere;
	protected String ragioneSocialeCliente;
	protected String codiceCliente;
	protected String pathCartellaFile;
	protected String pathCartellaStorico;
	protected File cartellaFile;
	protected File cartellaStorico;
	
	public Corriere(CorrieriPerCliente cliente) {
		configurazioneCliente = cliente;
	}
	
	public abstract void inviaDati() throws Exception;
	
	protected int contaRighe(File testo) throws IOException {
		int righe = 0;
		FileReader input = new FileReader(testo);
		BufferedReader reader = new BufferedReader(input);
		while (reader.readLine() != null) {
			righe +=1;
		}
		reader.close();
		input.close();
		return righe;
	}

}
