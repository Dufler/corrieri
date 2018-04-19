package it.ltc.corrieri.brt.importazionefile;

public class MainImportazioneFileBrt {

	public static void main(String[] args) throws Exception {
		System.out.println("Avvio dell'importazione.");
		String path = "C:\\Users\\Damiano\\Documents\\LTC\\traffic\\simulazioni\\ciesse\\fatturaBRTottobre2017.txt";
		ImportatoreTestoPDF importatore = ImportatoreTestoPDF.getInstance();
		importatore.setCommessa(32); //25 = Arcadia, 32 ltc test
		importatore.setDocumento(2); //Check se esiste, 2 per ltc test
		importatore.setCodiceCliente("0637094"); //va inserito quello giusto
		importatore.setDb("produzione"); //"test" o "produzione"
		importatore.importaTestoPDF(path);
		System.out.println("Importazione terminata.");
	}

}
