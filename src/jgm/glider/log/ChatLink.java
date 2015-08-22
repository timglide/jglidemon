package jgm.glider.log;

public class ChatLink {
	public transient int pixelOffset = -1;
	public transient int pixelWidth = -1;
	
	public int rawOffset;
	public int offset;
	public ChatLinkType type;
	public String rawText;
	public String[] parts;
	public String text;
	
	public ChatLink(int rawOffset, int offset, ChatLinkType type, String rawText, String[] parts, String text) {
		this.offset = offset;
		this.type = type;
		this.rawText = rawText;
		this.parts = parts;
		this.text = text;
	}

	public int rawLength() {
		return rawText.length();
	}
	
	public int length() {
		return text.length();
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder(getClass().getSimpleName())
		.append("[type=")
		.append(type)
		.append(",rawOffset=")
		.append(rawOffset)
		.append(",offset=")
		.append(offset)
		.append(",rawLength=")
		.append(rawLength())
		.append(",length=")
		.append(length())
		.append(",rawText=\"")
		.append(rawText)
		.append("\",text=\"")
		.append(text)
		.append("\"]");
		;
		
		return sb.toString();
	}
}
