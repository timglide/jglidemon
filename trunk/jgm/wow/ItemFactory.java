package jgm.wow;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ItemFactory {
	private static final String SITE_URL
		= "http://wow.allakhazam.com/dev/wow/item-xml.pl?witem=";

	private ItemFactory() {}

	public static boolean factory(int id, Item item) {
		Document dom  = null;

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		try {
			// Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();
			
			// parse using builder to get DOM representation of the XML file
			dom = db.parse(SITE_URL + Integer.toString(id));
/*		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (SAXException se) {
			se.printStackTrace();
		} catch (java.io.IOException ioe) {
			ioe.printStackTrace();
		}*/
		} catch (Exception e) {
			// couldn't get xml from allakhazam, just return
			// the item with the name and no additional info
			System.err.println("Couldn't get item XML: " + e.getMessage());
			return true;
		}

		if (dom == null) return false;

		Element doc = dom.getDocumentElement();
		
		int foundId = getIntValue(doc, "id");

		if (foundId == 0 || foundId != id) return false;

		item.id       = foundId;
		item.name     = getTextValue(doc,"name1");
		item.quality  = getIntValue(doc, "quality");
		item.iconPath = getTextValue(doc, "icon");
		
		item.armor = getIntValue(doc, "armor");
		item.binds = getIntValue(doc, "binds");
		item.clazz = getIntValue(doc, "itemclass");
		item.subclass = getIntValue(doc, "itemsubclass");
		
		item.itemLevel = getIntValue(doc, "level");
		item.requiredLevel = getIntValue(doc, "minlevel");

		item.description = getTextValue(doc, "description");
		item.dmgHigh = getIntValue(doc, "dmg_high");
		item.dmgLow = getIntValue(doc, "dmg_low");
		item.speed = getIntValue(doc, "speed");
		item.stackSize = getIntValue(doc, "stacksize");
		item.unique = getIntValue(doc, "unique");
		item.merchentBuyPrice = getIntValue(doc, "buyprice");
		item.slot = getIntValue(doc, "slot");
		
		for (int i = 0; i < 5; i++) {
			item.stats[i] = getIntValue(doc, "stat" + i + "_stat");
			item.stat_values[i] = getIntValue(doc, "stat" + i + "_value");
		}
		
		return true;
	}

	private static String getTextValue(Element ele, String tagName) {
		String textVal = null;
		
		try {
			NodeList nl = ele.getElementsByTagName(tagName);
			if (nl != null && nl.getLength() > 0) {
				Element el = (Element) nl.item(0);
				textVal = el.getFirstChild().getNodeValue();
			}
		} catch (Exception e) {
			//System.out.println(e.getMessage());
		}

		return textVal;
	}

	private static int getIntValue(Element ele, String tagName) {
		try {
			return Integer.parseInt(getTextValue(ele,tagName));
		} catch (NumberFormatException e) {
			return 0;
		} catch (NullPointerException e) {
			return 0;
		}
	}

}
