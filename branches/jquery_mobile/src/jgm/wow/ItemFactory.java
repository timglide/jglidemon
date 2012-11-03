/*
 * -----LICENSE START-----
 * JGlideMon - A Java based remote monitor for MMO Glider
 * Copyright (C) 2007 Tim
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * -----LICENSE END-----
 */
package jgm.wow;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.logging.*;
import java.util.regex.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
//import javax.xml.parsers.ParserConfigurationException;

import org.json.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
//import org.xml.sax.SAXException;

public class ItemFactory {
	static Logger log = Logger.getLogger(Item.class.getName());
	
	static Map<String, Pattern> PATTERN_CACHE = new HashMap<String, Pattern>();
	
	// for future i18n reference see, for example,
	// http://wow.allakhazam.com/dev/wow/item-xml.pl?witem=16898&locale=frFR
	private static final String SITE_URL
		= "http://wow.allakhazam.com/cluster/item-xml.pl?witem=";

	private static final String
		ARMORY_ITEM_URL = "http://us.battle.net/api/wow/item/";
	
	private ItemFactory() {}

	public static boolean factory(int id, Item item) {
		JSONObject json = null;
		InputStream in = null;
		
		try {
			in = new URL(ARMORY_ITEM_URL + id).openStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			
			json = new JSONObject(new JSONTokener(br));
		} catch (IOException e) {
			log.warning("Couldn't get item JSON: " + e.getMessage());
			return true;
		} catch (JSONException e) {
			log.warning("Couldn't parse item JSON: " + e.getMessage());
			return true;
		} finally {
			if (null != in) {
				try {
					in.close();
				} catch (IOException e) {}
			}
		}
		
		if (null == json) return false;
		
		try {
			int foundId = json.getInt("id");
			
			if (foundId != id) return false;
			
			item.id = foundId;
			item.name = json.optString("name");
			item.quality  = json.optInt("quality");
			item.quality_ = Quality.intToQuality(item.quality);
			item.iconPath = json.optString("icon", Item.DEFAULT_ICON);
			
			item.armor = json.optInt("armor");
			item.binds = json.optInt("itemBind");
			item.clazz = json.optInt("itemClass");
			item.subclass = json.optInt("itemSubClass");
			
			item.itemLevel = json.optInt("itemLevel");
			item.requiredLevel = json.optInt("requiredLevel");

			item.description = json.optString("description");
			
			if (json.has("weaponInfo")) {
				JSONObject weaponInfo = json.getJSONObject("weaponInfo");
				JSONObject damage = weaponInfo.getJSONObject("damage");
				item.dmgHigh = damage.optInt("max");
				item.dmgLow = damage.optInt("min");
				item.speed = (int)(weaponInfo.optDouble("weaponSpeed" , 0.0) * 1000);
			}
			
			item.stackSize = json.optInt("stackable");
			item.unique = json.optInt("maxCount");
			item.merchentBuyPrice = json.optInt("sellPrice");
			
			// no AH pricing from blizzard!
			
			item.slot = json.optInt("inventoryType");
			
			if (json.has("bonusStats")) {
				JSONArray stats = json.getJSONArray("bonusStats");
				JSONObject stat;
				
				item.bonusStats = new BonusStat[stats.length()];
				item.bonusStatValues = new int[stats.length()];
				
				for (int i = 0; i < stats.length(); i++) {
					stat = stats.getJSONObject(i);
					item.bonusStats[i] = BonusStat.getById(stat.getInt("stat"));
					item.bonusStatValues[i] = stat.getInt("amount");
				}
			}
			
			item.retrievedInfo = true;
			return true;
		} catch (JSONException e) {
			log.warning("Couldn't parse item JSON: " + e.getMessage());
			return false;
		}
	}
	
	public static boolean factoryOld(int id, Item item) {
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
			log.warning("Couldn't get item XML: " + e.getMessage());
			return true;
		}

		if (dom == null) return false;

		Element doc = dom.getDocumentElement();
		
		int foundId = getIntValue(doc, "id");

		if (foundId == 0 || foundId != id) return false;

		item.id       = foundId;
		item.name     = getTextValue(doc,"name1");
		item.quality  = getIntValue(doc, "quality");
		item.quality_ = Quality.intToQuality(item.quality);
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
		
//		// replace merchentBuyPrice with ah price if appropriate
//		String[] patterns = jgm.Config.c.getArray("loot.ahlist.");
//		
//		// use the ah price if it's phat loot or if
//		// it matches one of the patterns
//		boolean useAHPriceInstead = item.quality >= jgm.Config.c.getInt("loot.phatquality");
		
//		if (!useAHPriceInstead) {
//			for (String str : patterns) {
//				if (!PATTERN_CACHE.containsKey(str)) {
//					try {
//						PATTERN_CACHE.put(str, Pattern.compile(str, Pattern.CASE_INSENSITIVE));
//					} catch (PatternSyntaxException e) {
//						PATTERN_CACHE.put(str, null);
//					}
//				}
//				
//				Pattern p = PATTERN_CACHE.get(str);
//				
//				if (p == null) continue;
//				
//				if (p.matcher(item.name).matches()) {
//					useAHPriceInstead = true;
//					break;
//				}
//			}
//		}
		
//		if (useAHPriceInstead) {
//			item.merchentBuyPrice = getIntValue(doc, "median_auc_price");
//			log.finest("Setting AH price for " + item.name + ": " + item.merchentBuyPrice);
//		}
		
		item.slot = getIntValue(doc, "slot");
		
		for (int i = 0; i < item.stats.length; i++) {
			item.stats[i] = getIntValue(doc, "stat" + i + "_stat");
			item.stat_values[i] = getIntValue(doc, "stat" + i + "_value");
		}
		
		item.retrievedInfo = true;
		
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
