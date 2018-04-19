package it.ltc.bartolini.model.fnvap;

import it.ltc.bartolini.model.RigaCampiFissiBRT;

public class RigaFNVAP extends RigaCampiFissiBRT {
	
	public enum FasiORM {
		
		_000("RICEVUTO", ""),
		_050("TRASMESSO FIL. DI COMPETENZA", ""),
		_100("CONFERMATO", "S01"),
		_200("DIROTTATO", "C07"),
		_400("IN RITIRO", "S01"),
		_500("NON RITIRATO", "C06"),
		_600("RITIRATO", "S01"),
		_900("SPEDIZIONE PARTITA", "S02"),
		_910("SPEDIZIONE PARTITA", "S02"),
		_999("ANNULLATO", "C06");
		
		private final String nome;
		private final String stato;
		
		private FasiORM(String nome, String stato) {
			this.nome = nome;
			this.stato = stato;
		}
		
		@Override
		public String toString() {
			return nome;
		}

		public String getStato() {
			return stato;
		}

	}
	
	public enum CausaliRitiroNonEffettuato {
		
		_01("MANCANZA DI TEMPO"),               
		_02("MERCE NON PRONTA"),           
		_04("CLIENTE ASSENTE/CHIUSO");
		
		private final String nome;
		
		private CausaliRitiroNonEffettuato(String nome) {
			this.nome = nome;
		}
		
		@Override
		public String toString() {
			return nome;
		}

	}
	
	public enum CausaliRitiroAnnullato {
		
		_80("ANNULLAMENTO PRIMA DELL’AFFIDAMENTO"),       
		_91("RIFIUTATO DAL CLIENTE"), 
		_92("GIA' RITIRATO"),
		_94("MERCE NON IN PRODOTTO"),               
		_95("CLIENTE INESISTENTE ALL'INDIRIZZO"),
		_96("TELEFONO ERRATO E/O NON RISPONDE"),
		_97("GIA' EFFETTUATI 2 TENTATIVI");

		private final String nome;
		
		private CausaliRitiroAnnullato(String nome) {
			this.nome = nome;
		}
		
		@Override
		public String toString() {
			return nome;
		}
		
	}
	
	/**
	 * VAPIDC - Viene memorizzato il codice utente del cliente che ha inviato l’ORM (FNVAO)
	 */
	private final String identificativoCliente;
	
	/**
	 * VAPPOE - Codice filiale BRT che ha ricevuto l’ORM
	 */
	private final String filialeEmittente;
	
	/**
	 * VAPDPE - Descriz. Filiale BRT che ha ricevuto l’ORM
	 */
	private final String decodificaFilialeEmittente;
	
	/**
	 * VAPNSR - Impostato a zero
	 */
	private final String numeroSerie;
	
	/**
	 * VAPNOR - Indica il numero ritiro attribuito da BRT (non indicato nella fase 000) 
	 */
	private final String numeroRitiro;
	
	/**
	 * VAPNRV - Impostato a zero
	 */
	private final String numeroViaggio;
	
	/**
	 * VAPRFA - Riferimento indicato dal cliente (uguale al campo VAORFA)
	 */
	private final String riferimentoAlfanumerico;
	
	/**
	 * VAPPOG - Codice filiale BRT che ha eseguito la fase dell’ORM
	 */
	private final String filialeEsecuzioneFase;
	
	/**
	 * VAPDPG - Descrizione della filiale BRT che ha eseguito la fase dell’ORM
	 */
	private final String decodificaFilialeEsecuzioneFase;
	
	/**
	 * VAPDAE - Data di esecuzione della fase ORM
	 */
	private final String dataEsecuzioneFase;
	
	/**
	 * VAPORE - Ora di esecuzione fase ORM
	 */
	private final String oraVariazione;
	
	/**
	 * VAPFAR - Codice della fase eseguita (vedere elenco codici fasi ORM)
	 */
	private final String codiceFaseEseguita;
	
	/**
	 * VAPDFA - Decodifica della fase eseguita 
	 */
	private final String decodificaFase;
	
	/**
	 * VAPCAR - Causale dell’eventuale mancato ritiro (vedere elenco causali)
	 */
	private final String causaleMancatoRitiro;
	
	/**
	 * VAPDCA - Decodifica della causale di mancato ritiro
	 */
	private final String decodificaCausale;
	
	/**
	 * VAPNOT - Eventuali note inserite dal corriere
	 */
	private final String note;
	
	public RigaFNVAP(String riga) {
		super(riga);
		identificativoCliente = parseString(1, 8);
		filialeEmittente = parseString(8, 11);
		decodificaFilialeEmittente = parseString(11, 31);
		numeroSerie = parseString(31, 33);
		numeroRitiro = parseString(33, 40);
		numeroViaggio = parseString(40, 42);
		riferimentoAlfanumerico = parseString(42, 57);
		filialeEsecuzioneFase = parseString(57, 60);
		decodificaFilialeEsecuzioneFase = parseString(60, 80);
		dataEsecuzioneFase = parseString(80, 88);
		oraVariazione = parseString(88, 94);
		codiceFaseEseguita = parseString(94, 97);
		decodificaFase = parseString(97, 132);
		causaleMancatoRitiro = parseString(132, 134);
		decodificaCausale = parseString(134, 169);
		note = parseString(169, 219);
	}
	
	/**
	 * Controlla lo stato del ritiro e se è la merce è stata ritirata restituisce true
	 * @return
	 */
	public boolean isInseribile() {
		boolean inseribile;
		try {
			FasiORM fase = FasiORM.valueOf("_" + codiceFaseEseguita);
			inseribile = fase.compareTo(FasiORM._900) > 0;
		} catch (Exception e) {
			inseribile = false;
		}
		return inseribile;
	}

	public String getIdentificativoCliente() {
		return identificativoCliente;
	}

	public String getFilialeEmittente() {
		return filialeEmittente;
	}

	public String getDecodificaFilialeEmittente() {
		return decodificaFilialeEmittente;
	}

	public String getNumeroSerie() {
		return numeroSerie;
	}

	public String getNumeroRitiro() {
		return numeroRitiro;
	}

	public String getNumeroViaggio() {
		return numeroViaggio;
	}

	public String getRiferimentoAlfanumerico() {
		return riferimentoAlfanumerico;
	}

	public String getFilialeEsecuzioneFase() {
		return filialeEsecuzioneFase;
	}

	public String getDecodificaFilialeEsecuzioneFase() {
		return decodificaFilialeEsecuzioneFase;
	}

	public String getDataEsecuzioneFase() {
		return dataEsecuzioneFase;
	}

	public String getOraVariazione() {
		return oraVariazione;
	}

	public String getCodiceFaseEseguita() {
		return codiceFaseEseguita;
	}

	public String getDecodificaFase() {
		return decodificaFase;
	}

	public String getCausaleMancatoRitiro() {
		return causaleMancatoRitiro;
	}

	public String getDecodificaCausale() {
		return decodificaCausale;
	}

	public String getNote() {
		return note;
	}

}
