package leo.webcrawler.bean;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * 
 * The purpose of this class is
 * 
 * @author leoky
 *
 */
public class ExtractedData {

	private String title;
	private URL url;
	private String price;
	private String data;
	
	/**
	 * 
	 * @param title
	 * @param url
	 * @throws MalformedURLException 
	 */
	public ExtractedData(String title, String urlRepresentation) throws MalformedURLException {
		
		this.title = title;
		this.url = new URL(urlRepresentation);
	}

	/**
	 * 
	 * The purpose of this method is
	 *
	 * @return
	 */
	public String getTitle() {
	
		return title;
	}

	/**
	 * 
	 * The purpose of this method is
	 *
	 * @param title
	 */
	public void setTitle(String title) {
	
		this.title = title;
	}

	/**
	 * 
	 * The purpose of this method is
	 *
	 * @return
	 */
	public URL getUrl() {
	
		return url;
	}

	/**
	 * 
	 * The purpose of this method is
	 *
	 * @param url
	 */
	public void setUrl(URL url) {
	
		this.url = url;
	}
	
	/**
	 * 
	 * The purpose of this method is
	 *
	 * @return
	 */
	public String getPrice() {
	
		return price;
	}

	/**
	 * 
	 * The purpose of this method is
	 *
	 * @param price
	 */
	public void setPrice(String price) {
	
		this.price = price;
	}

	/**
	 * 
	 * The purpose of this method is
	 *
	 * @return
	 */
	public String getData() {
	
		return data;
	}

	/**
	 * 
	 * The purpose of this method is
	 *
	 * @param data
	 */
	public void setData(String data) {
	
		this.data = data;
	}

	@Override
	public int hashCode() {

		final int prime = 31;
		int result = 1;
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		result = prime * result + ((price == null) ? 0 : price.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {

		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExtractedData other = (ExtractedData) obj;
		if (data == null) {
			if (other.data != null)
				return false;
		}
		else if (!data.equals(other.data))
			return false;
		if (price == null) {
			if (other.price != null)
				return false;
		}
		else if (!price.equals(other.price))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		}
		else if (!title.equals(other.title))
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		}
		else if (!url.equals(other.url))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return ""+ title + " - " + price + " aparut în " + data + "\n" + url.toString() + "\n" +"";
	}
}
