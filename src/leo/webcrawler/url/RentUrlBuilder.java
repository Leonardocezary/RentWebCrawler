package leo.webcrawler.url;

/**
 * 
 * The purpose of this class is
 * 
 * @author leoky
 *
 */
public class RentUrlBuilder {

	private String baseUrl;
	public static final String privateType = "private";
	public static final String businessType = "business";
	
	/**
	 * 
	 * @param baseUrl
	 */
	public RentUrlBuilder(String baseUrl){
		this.baseUrl = baseUrl;
	}

	/**
	 * 
	 * The purpose of this method is
	 *
	 * @param roomCount
	 */
	public void addRoomCriteria(String roomCount) {
		
		if (baseUrl.endsWith("de-inchiriat/"))
			baseUrl += roomCount + "-camera/";
	}
	
	/**
	 * 
	 * The purpose of this method is
	 *
	 * @param location
	 */
	public void addLocation(String location) {
		baseUrl += location + "/";
	}
	
	/**
	 * 
	 * The purpose of this method is
	 *
	 * @param surface
	 */
	public void addPriceFrom(String price) {
		
		if(baseUrl.endsWith("/"))
			baseUrl += "?search%5Bfilter_float_price%3Afrom%5D="+ price;
		else
			baseUrl += "&search%5Bfilter_float_price%3Afrom%5D="+ price;
	}
	
	/**
	 * 
	 * The purpose of this method is
	 *
	 * @param surface
	 */
	public void addPriceUntil(String price) {
		
		if(baseUrl.endsWith("/"))
			baseUrl += "?search%5Bfilter_float_price%3Ato%5D="+ price;
		else
			baseUrl += "&search%5Bfilter_float_price%3Ato%5D="+ price;
	}
	
	/**
	 * 
	 * The purpose of this method is
	 *
	 * @param surface
	 */
	public void addSurfaceFrom(String surface) {
		
		if(baseUrl.endsWith("/"))
			baseUrl += "?search%5Bfilter_float_price%3Afrom%5D="+ surface;
		else
			baseUrl += "&search%5Bfilter_float_price%3Afrom%5D="+ surface;
	}
	
	/**
	 * 
	 * The purpose of this method is
	 *
	 * @param surface
	 */
	public void addSurfaceUntil(String surface) {
		
		if(baseUrl.endsWith("/"))
			baseUrl += "?search%5Bfilter_float_m%3Ato%5D="+ surface;
		else
			baseUrl += "&search%5Bfilter_float_m%3Ato%5D="+ surface;
	}
	
	/**
	 * 
	 * The purpose of this method is
	 *
	 * @param source
	 */
	public void addSource(String source) {
		
		if(baseUrl.endsWith("/"))
			baseUrl += "?search%5Bprivate_business%5D="+ source;
		else
			baseUrl += "&search%5Bprivate_business%5D="+ source;
	}
	
	/**
	 * 
	 * The purpose of this method is
	 *
	 * @return
	 */
	public String getAddressAsString() {
		return baseUrl;
	}
}
