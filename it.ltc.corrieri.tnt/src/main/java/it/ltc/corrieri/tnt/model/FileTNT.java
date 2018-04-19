package it.ltc.corrieri.tnt.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

public class FileTNT {

	private static final Logger logger = Logger.getLogger("FileTNT");

	private DatiFile datiFile;
	private DatiControllo datiControllo;
	private final List<DatiSpedizione> datiSpedizioni;
	private final List<DatiEsito> datiEsiti;
	private final File file;

	public FileTNT(File file) {
		this.file = file;
		datiSpedizioni = new LinkedList<>();
		datiEsiti = new LinkedList<>();
		List<String> righe = leggiFile(file);
		for (String riga : righe) {
			char identifier = riga.charAt(0);
			switch (identifier) {
			case DatiFile.TIPO:
				datiFile = new DatiFile(riga);
				break;
			case DatiControllo.TIPO:
				datiControllo = new DatiControllo(riga);
				break;
			case DatiSpedizione.TIPO:
				datiSpedizioni.add(new DatiSpedizione(riga));
				break;
			case DatiEsito.TIPO:
				datiEsiti.add(new DatiEsito(riga));
				break;
			}
		}
		check();
	}

	public FileTNT(String path) {
		this.file = new File(path);
		datiSpedizioni = new LinkedList<>();
		datiEsiti = new LinkedList<>();
		List<String> righe = leggiFile(file);
		for (String riga : righe) {
			char identifier = riga.charAt(0);
			switch (identifier) {
			case DatiFile.TIPO:
				datiFile = new DatiFile(riga);
				break;
			case DatiControllo.TIPO:
				datiControllo = new DatiControllo(riga);
				break;
			case DatiSpedizione.TIPO:
				datiSpedizioni.add(new DatiSpedizione(riga));
				break;
			case DatiEsito.TIPO:
				datiEsiti.add(new DatiEsito(riga));
				break;
			}
		}
		check();
	}

	private void check() {
		if (datiFile == null)
			logger.warn("(FileTNT) Non è stato possibile recuperare le info generiche sul file TNT.");
		if (datiControllo != null) {
			if (datiSpedizioni.size() != datiControllo.getNumeroRecordPartenze())
				logger.warn("(FileTNT) Il numero di dati sulle spedizioni è diverso da quello atteso.");
			if (datiEsiti.size() != datiControllo.getNumeroRecordAggiornamento())
				logger.warn("(FileTNT) Il numero di dati sugli esiti è diverso da quello atteso.");
		} else
			logger.warn("(FileTNT) Non è stato possibile recuperare le info di controllo sul file TNT.");
	}

//	private List<String> leggiFile(String path) {
//		LinkedList<String> righe = new LinkedList<String>();
//		try (FileReader fReader = new FileReader(path); BufferedReader bReader = new BufferedReader(fReader);) {
//			String line = bReader.readLine();
//			while (line != null) {
//				righe.add(line);
//				line = bReader.readLine();
//			}
//		} catch (IOException e) {
//			logger.error(e);
//		}
//		return righe;
//	}
	
	private List<String> leggiFile(File file) {
		LinkedList<String> righe = new LinkedList<String>();
		try (FileReader fReader = new FileReader(file); BufferedReader bReader = new BufferedReader(fReader);) {
			String line = bReader.readLine();
			while (line != null) {
				righe.add(line);
				line = bReader.readLine();
			}
		} catch (IOException e) {
			logger.error(e);
		}
		return righe;
	}

	public DatiFile getDatiFile() {
		return datiFile;
	}

	public DatiControllo getDatiControllo() {
		return datiControllo;
	}

	public List<DatiSpedizione> getDatiSpedizioni() {
		return datiSpedizioni;
	}

	public List<DatiEsito> getDatiEsiti() {
		return datiEsiti;
	}

	public File getFile() {
		return file;
	}

}
