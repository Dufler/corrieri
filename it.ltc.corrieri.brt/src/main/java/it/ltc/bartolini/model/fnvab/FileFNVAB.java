package it.ltc.bartolini.model.fnvab;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

public class FileFNVAB {
	
	private static final Logger logger = Logger.getLogger(FileFNVAB.class);
	
	public static final String NOME_FILE = "FNVAB";
	
	private final File file;
	private final List<String> righe;
	private final List<RigaFNVAB> esiti;
	
	public FileFNVAB(File file) {
		this.file = file;
		this.righe = new LinkedList<String>();
		this.esiti = new LinkedList<RigaFNVAB>();
		leggiFile();
		creaEsiti();
	}

	private void creaEsiti() {
		for (String riga : righe) {
			RigaFNVAB spedizione = new RigaFNVAB(riga);
			esiti.add(spedizione);
		}
	}

	private void leggiFile() {
		try {
			FileReader fileReader = new FileReader(file);
			BufferedReader reader = new BufferedReader(fileReader);
			String line = reader.readLine();
			while (line != null) {
				if (!line.trim().isEmpty())
					righe.add(line);
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	public List<RigaFNVAB> getEsiti() {
		return esiti;
	}

}
