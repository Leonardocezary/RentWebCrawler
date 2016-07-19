package leo.webcrawler.connect;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import leo.webcrawler.bean.ExtractedData;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class Connector {

	private List<ExtractedData> previousData;
	private List<ExtractedData> extractedData;
	
	/**
	 * 
	 */
	public Connector() {
		previousData = new ArrayList<ExtractedData>();
		extractedData = new ArrayList<ExtractedData>();
	}
	
	/**
	 * 
	 * The purpose of this method is
	 *
	 * @param formedURL
	 */
	public void extractLinks(String formedURL) {

		DateFormat startDateFormat = new SimpleDateFormat("HH:mm:ss");
		Date startDate = new Date();
		
		System.out.println("Started to crawl at " + startDateFormat.format(startDate));
		
		Document doc;
		try {

			int hashCounter = 0;
			String previousLink = "";
			doc = Jsoup.connect(formedURL).get();
			Elements hrefs = doc.select("a");
			Elements strongs = doc.select("strong");
			Elements dates = doc.select("p");

			//add desired links with their titles
			for (int i = 0; i < hrefs.size(); i++) {
				String attribute = hrefs.get(i).attr("href");
				String text = new String(hrefs.get(i).text());

				if (attribute.equals("#") && hashCounter < 17) {
					hashCounter++;
					continue;
				}
				if (hashCounter >= 17) {
					if (previousLink.equals(attribute) && !attribute.equals("")) {
						extractedData.add(new ExtractedData(text, attribute));
						
						if(text.contains("Urmatoarele anunturi"))
							break;
					}
					
					previousLink = attribute;
				}
			}
			
			//add price
			int dataIndex = 0;
			for (int i = 0; i < strongs.size(); i++) {
				if (strongs.get(i).text().equals(extractedData.get(dataIndex).getTitle())) {
					i++;
					ExtractedData currentData = extractedData.get(dataIndex);
					currentData.setPrice(strongs.get(i).text());
					dataIndex++;
				}
			}
			
			//add spawn date
			dataIndex = 0;
			for (int i = 0; i < dates.size(); i++) {
				if (dates.get(i).attr("class").equals("color-9 lheight16 marginbott5 x-normal")) {
					ExtractedData currentData = extractedData.get(dataIndex);
					currentData.setData(dates.get(i).text());
					dataIndex++;
				}
			}
			
			DateFormat endDateFormat = new SimpleDateFormat("HH:mm:ss");
			Date endDate = new Date();
			
			System.out.println("Ended to crawl at " + endDateFormat.format(endDate) + "\n");
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	public void displayExtractedData(){
		
		for (ExtractedData e: extractedData) {
			System.out.println(e+"-----------------\n");
		}
	}
	
	/**
	 * 
	 * The purpose of this method is
	 *
	 * @return
	 */
	public List<ExtractedData> getExtractedData(){
		return extractedData;
	}

	
	public List<ExtractedData> getPreviousData() {
	
		return previousData;
	}

	
	public void setPreviousData(List<ExtractedData> previousData) {
	
		this.previousData = previousData;
	}
	
	
}
