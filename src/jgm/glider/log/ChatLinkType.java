package jgm.glider.log;

public enum ChatLinkType {
	ITEM,
	SPELL,
	ENCHANT,
	QUEST,
	PLAYER(false),
	ACHIEVEMENT,
	CHANNEL(false)/*,
	// gives glyph id, not spell id that wowhead needs
	GLYPH(false) // |Hglyph:701|h[Glyph of Beacon of Light]|h */
	;
	
	public final boolean isWowheadable;
	
	private ChatLinkType() {
		this(true);
	}
	
	private ChatLinkType(boolean isWowheadable) {
		this.isWowheadable = isWowheadable;
	}
	
	public String getHtml5Link(
			String id,
			String enchant,
			String gem1,
			String gem2,
			String gem3,
			String gem4,
			String suffix,
			String unique,
			String linkLvl,
			String name) {
		
		String type = name().toLowerCase();
		
		switch (this) {
			case CHANNEL: // |Hchannel:channel:4|h[4. test]|h
				if ("channel".equalsIgnoreCase(id)) {
					id = enchant;
				} else {
					id = id.toLowerCase();
				}
				
				// |Hchannel:GUILD|h[Guild]|h
				break;
		
			case ENCHANT:
				type = "spell";
		}
		
		if ("0".equals(enchant)) enchant = "";
		if ("0".equals(gem1)) gem1 = "";
		if ("0".equals(gem2)) gem2 = "";
		if ("0".equals(gem3)) gem3 = "";
		if ("0".equals(gem4)) gem4 = "";
		if ("0".equals(suffix)) suffix = "";
		if ("0".equals(unique)) unique = "";
		if ("0".equals(linkLvl)) linkLvl = "";
		
		StringBuilder sb = new StringBuilder(50)
			.append("<a href=\"");
			
		if (isWowheadable) {
			sb.append("http://www.wowhead.com/")
			.append(type).append('=').append(id)
			.append("\" target=\"_blank\"");
		} else {
			sb.append("#\"");
		}
			
		sb.append(" class=\"chat-link ")
		.append(type)
		.append("\" data-link-type=\"")
		.append(type)
		.append("\" data-link-id=\"")
		.append(id)
		.append("\"");
			
		if (isWowheadable) {
			sb.append(" rel=\"")
			.append(type)
			.append('=').append(id);
		}
		
		switch (this) {
			case ITEM:
				if (!linkLvl.isEmpty()) {
					sb.append("&amp;lvl=").append(linkLvl);
				}
				
				if (!enchant.isEmpty()) {
					sb.append("&amp;ench=").append(enchant);
				}
				
				// wowhead needs the actual item ids for the gems but
				// the itemlink has the "enchantid" the gems give instead
//				if (!gem1.isEmpty()) {
//					sb.append("&amp;gems=").append(gem1);
//					
//					if (!gem2.isEmpty()) {
//						sb.append(':').append(gem2);
//					}
//					
//					if (!gem3.isEmpty()) {
//						sb.append(':').append(gem3);
//					}
//					
//					if (!gem4.isEmpty()) {
//						sb.append(':').append(gem4);
//					}
//				}
				
				if (!suffix.isEmpty()) {
					sb.append("&amp;rand=").append(suffix);
				}
				
				break;
		}

		if (isWowheadable) {
			sb.append('"');
		}
		
		sb.append('>');
		
		sb.append(name);
		
		sb.append("</a>");
		
		return sb.toString();
	}
}
