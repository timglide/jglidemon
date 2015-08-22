package jgm.wow;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jgm.util.SaveURL;

public class CurrencyFactory {
	private static final Logger log = Logger.getLogger(CurrencyFactory.class.getName());
	
	private static final String BASE_URL = "http://www.wowhead.com/currency=";
	
	private static final Pattern ICON_PATTERN = Pattern.compile(
			"property=\"og.*?image\"\\s*content=\".*?;([^;]+?).jpg");
	
	public static boolean factory(Currency c) {
		if (null == c)
			throw new IllegalArgumentException("c was null");
		
		try {
			URL url = new URL(BASE_URL + c.id);
			String str = SaveURL.getURL(url);
			
			SaveURL.writeURLtoFile(url, "data/currency-" + c.id + ".html");
			
			Matcher m = ICON_PATTERN.matcher(str);
			
			if (!m.find()) {
				log.warning("Icon pattern didn't match");
				return false;
			}
			
			c.iconPath = m.group(1);
			
			return true;
		} catch (Exception e) {
			log.log(Level.WARNING, "Couldn't retrieve currency info", e);
		}
		
		return false;
	}
	
	
	private CurrencyFactory() { }
}
