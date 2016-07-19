package leo.webcrawler.main.controller;

import java.util.List;

import leo.webcrawler.bean.ExtractedData;
import leo.webcrawler.connect.Connector;
import leo.webcrawler.url.RentUrlBuilder;

import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * The purpose of this class is
 * 
 * @author leoky
 */
public class ApplicationController {

	private RentUrlBuilder rentUrlBuilder;
	private Connector connector;

	/**
	 * 
	 */
	public ApplicationController() {

		String _urlBase = "http://olx.ro/imobiliare/apartamente-garsoniere-de-inchiriat/";
		rentUrlBuilder = new RentUrlBuilder(_urlBase);
		connector = new Connector();
	}

	/**
	 * The purpose of this method is
	 */
	public void perform() {

		rentUrlBuilder.addRoomCriteria("1");
		rentUrlBuilder.addLocation("timisoara");
		rentUrlBuilder.addPriceFrom("50");
		rentUrlBuilder.addPriceUntil("170");
		rentUrlBuilder.addSource(RentUrlBuilder.privateType);

		String builtUrl = rentUrlBuilder.getAddressAsString();
		
		connector.extractLinks(builtUrl);
		connector.displayExtractedData();
		connector.setPreviousData(connector.getExtractedData());
		connector.getExtractedData().clear();

		Timer timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				
				List<ExtractedData> previousData = connector.getPreviousData();
				connector.extractLinks(builtUrl);
				List<ExtractedData> currentData = connector.getExtractedData();
				currentData.removeAll(previousData);

				if (!currentData.isEmpty()) {
					connector.displayExtractedData();
					JTextArea ta = new JTextArea(10, 10);
					for(ExtractedData e: currentData) {
						ta.setText(e.toString() + "\n");
					}
	                ta.setWrapStyleWord(true);
	                ta.setLineWrap(true);
	                ta.setCaretPosition(0);
	                ta.setEditable(false);
					JOptionPane.showMessageDialog(null, new JScrollPane(ta), "Alerta chirie", JOptionPane.PLAIN_MESSAGE, null);
				}
				/*else {
					JOptionPane.showMessageDialog(null, "Nici o chirie", "Alerta chirie", JOptionPane.PLAIN_MESSAGE, null);
				}*/
				
				connector.getExtractedData().clear();
			}
		}, 0, 1000 * 60 * 5);

	}
}
