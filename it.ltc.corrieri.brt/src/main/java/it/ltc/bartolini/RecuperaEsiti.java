package it.ltc.bartolini;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import iseries.wsbeans.brt_trackingbybrtshipmentid.BRT_TrackingByBRTshipmentIDServicesProxy;
import iseries.wsbeans.brt_trackingbybrtshipmentid.BrtTRACKINGBYBRTSHIPMENTIDInput;
import iseries.wsbeans.brt_trackingbybrtshipmentid.BrtTRACKINGBYBRTSHIPMENTIDResult;
import iseries.wsbeans.brt_trackingbybrtshipmentid.Eventi;
import iseries.wsbeans.brt_trackingbybrtshipmentid.Evento;
import iseries.wsbeans.brt_trackingbybrtshipmentid.RecapitoMITT;
import iseries.wsbeans.brt_trackingbybrtshipmentid.Spedizione;
import it.ltc.database.model.centrale.Cap;
import it.ltc.database.model.centrale.Indirizzo;
import it.ltc.database.model.centrale.Tracking;
import it.ltc.database.model.centrale.TrackingPK;
import it.ltc.database.model.centrale.TrackingStatoCodificaCorriere;

public class RecuperaEsiti extends Importatore {

	private static final Logger logger = Logger.getLogger(RecuperaEsiti.class);

	private static RecuperaEsiti instance;

	private final BRT_TrackingByBRTshipmentIDServicesProxy ws;
	private final SimpleDateFormat sdf;
	private final SimpleDateFormat yearParser;

	private RecuperaEsiti() {
		ws = new BRT_TrackingByBRTshipmentIDServicesProxy();
		sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		yearParser = new SimpleDateFormat("yyyy");
	}

	public static RecuperaEsiti getInstance() {
		if (instance == null) {
			instance = new RecuperaEsiti();
		}
		return instance;
	}
	
	private void aggiustaMittente(it.ltc.database.model.centrale.Spedizione spedizione, BrtTRACKINGBYBRTSHIPMENTIDResult result) {
		Spedizione bolla = result.getBOLLA();
		RecapitoMITT recapito = bolla.getMITTENTE();
		Integer idMittente = getMittente(recapito);
		boolean inserimentoMittente = (idMittente != -1);
		if (inserimentoMittente) {
			spedizione.setIndirizzoPartenza(idMittente);
			//Non ci dovrebbe essere bisogno di aggiornare qui in quanto verrà fatto dall'importatore.
		}
	}
	
	private Integer getMittente(RecapitoMITT mMittente) {
		Integer idMittente;
		String cap = mMittente.getCAP();
		String ragioneSociale = mMittente.getRAGIONE_SOCIALE();
		String indirizzo = mMittente.getINDIRIZZO();
		String localita = mMittente.getLOCALITA();
		if (cap != null && !cap.isEmpty() && ragioneSociale != null && !ragioneSociale.isEmpty() && indirizzo != null && !indirizzo.isEmpty()) {
			Cap c = getInfoCap(mMittente.getCAP(), null);
			String provincia = c != null ? c.getProvincia() : "XX";
			Indirizzo mittente = recuperaIndirizzo(ragioneSociale, provincia, indirizzo, localita, cap, null);
			idMittente = mittente.getId();		
		} else {
			idMittente = -1;
		}
		return idMittente;
	}

	public LinkedList<Tracking> getTracking(it.ltc.database.model.centrale.Spedizione spedizione) {
		String letteraDiVettura = spedizione.getLetteraDiVettura();
		Date dataSpedizione = spedizione.getDataPartenza();
		int idSpedizione = spedizione.getId();
		LinkedList<Tracking> tracking = new LinkedList<Tracking>();
		if (letteraDiVettura.length() == 12)
		try {
			BrtTRACKINGBYBRTSHIPMENTIDInput input = new BrtTRACKINGBYBRTSHIPMENTIDInput();
			input.setLINGUA_ISO639_ALPHA2("IT");
			input.setSPEDIZIONE_ANNO(yearParser.format(dataSpedizione));
			input.setSPEDIZIONE_BRT_ID(letteraDiVettura);
			BrtTRACKINGBYBRTSHIPMENTIDResult result = ws.brt_trackingbybrtshipmentid(input);
			if (result.getESITO() == 0) {
				aggiustaMittente(spedizione, result);
				Eventi[] eventi = result.getLISTA_EVENTI();
				if (eventi == null || eventi.length == 0) {
					System.out.println("Nessun evento disponibile!");
				} else for (Eventi e : eventi) {
					Evento evento = e.getEVENTO();
					String id = evento.getID();
					String data = evento.getDATA();
					data = data.replace('.', '-');
					String ora = evento.getORA();
					ora = ora.replace('.', ':');
					String filiale = evento.getFILIALE();
					String descrizione = evento.getDESCRIZIONE();
					TrackingStatoCodificaCorriere stato = RecuperaLegendaEventi.getInstance().getStato(id);
					if (stato != null) {
						Tracking t = new Tracking();
						TrackingPK pk = new TrackingPK();
						pk.setData(sdf.parse(data + " " + ora));
						pk.setIdSpedizione(idSpedizione);
						pk.setStato(stato.getStato());
						t.setDescrizione(filiale + ": " + descrizione);
						t.setId(pk);
						tracking.add(t);
					} else {
						System.out.println("(Errore) Stato non presente!");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
		return tracking;
	}
}
