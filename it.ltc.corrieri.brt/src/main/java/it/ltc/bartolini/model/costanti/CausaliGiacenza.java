package it.ltc.bartolini.model.costanti;

public enum CausaliGiacenza {
	
	_GEN("MOTIVO GENERICO (specificato nella descrizione aggiuntiva)"),
	_001("RIFIUTA SENZA SPECIFICARE IL MOTIVO"),                 
	_002("MERCE NON ORDINATA O NON CONFORME"),                   
	_003("MERCE SPEDITA IN RITARDO"),                  
	_004("MERCE GIA' RICEVUTA CON PRECEDENTE INVIO"),            
	_005("MERCE SPEDITA CON TROPPO ANTICIPO"),                   
	_006("MERCE RESA SENZA AUTORIZZAZIONE"),                     
	_008("DESTINATARIO NON PAGA IL C/ASSEGNO"),                  
	_009("CLIENTE VUOLE CONTROLLO MERCE PRIMA DELLO SVINCOLO"),  
	_012("IL DESTINATARIO NON PAGA LE SPESE DI TRASPORTO"),      
	_016("DESTINATARIO HA RIMANDATO LO SVINCOLO"),                   
	_017("DESTINATARIO ERA ASSENTE: LASCIATO AVVISO"),               
	_019("DESTINATARIO HA CESSATO L'ATTIVITA' O SI E' TRASFERITO"),  
	_021("DESTINATARIO SCONOSCIUTO ALL'INDIRIZZO DEL DDT"),          
	_022("INDIRIZZO INDICATO SULLA B.A.M ERRATO/INESISTENTE"),       
	_023("IL DESTINATARIO E' CHIUSO"),                               
	_024("IL DESTINATARIO E' CHIUSO PER FERIE"),                     
	_026("IL DESTINATARIO CHIEDE CONSEGNA AD ALTRO INDIRIZZO"),      
	_028("ESERCIZIO NON ANCORA IN ATTIVITA'"),                       
	_032("FERMO DEPOSITO-NESSUNO SI E' PRESENTATO PER RITIRO"),   
	_034("SPEDIZIONE NON CONSEGNABILE CAUSA FORZA MAGGIORE"),     
	_035("DOCUMENTI INCOMPLETI O MANCANTI"),                      
	_037("RIFIUTA LA CONSEGNA TASSATIVA"), 
	_100("CONSEGNA RICHIESTA OLTRE 72 ORE"),                 
	_101("A SEGUITO L. AVV. CONCORDATA CONSEGNA OLTRE 72 ORE");
	
	private final String descrizione;

	private CausaliGiacenza(String descrizione) {
		this.descrizione = descrizione;
	}
	
	@Override
	public String toString() {
		return descrizione;
	}

}
