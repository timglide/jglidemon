package jgm.util;

public class Properties extends java.util.Properties {
	public Properties() {
		super();
	}
	
	public Properties(Properties defaults) {
		super(defaults);
	}
	
	public boolean has(String key) {
		return containsKey(key);
	}
	
	public int getInt(String propertyName) {
		int defaultValue = 0;
		propertyName = propertyName.toLowerCase();
		
		try {
			defaultValue = 
				Integer.parseInt(getProperty(propertyName));
		} catch (Throwable e) {}
		
		return defaultValue;
	}
	
	public boolean getBool(String propertyName) {
		boolean defaultValue = false;
		propertyName = propertyName.toLowerCase();
		
		try {
			defaultValue =
				Boolean.parseBoolean(getProperty(propertyName));
		} catch (Throwable e) {}
		
		return defaultValue;
	}
	
	public long getLong(String propertyName) {
		long defaultValue = 0L;
		propertyName = propertyName.toLowerCase();
		
		try {
			defaultValue =
				Long.parseLong(getProperty(propertyName));
		} catch (Throwable e) {}
		
		return defaultValue;
	}
	
	public double getDouble(String propertyName) {
		double defaultValue = 0.0;
		propertyName = propertyName.toLowerCase();
		
		try {
			defaultValue =
				Double.parseDouble(getProperty(propertyName));
		} catch (Throwable e) {}
		
		return defaultValue;
	}
	
	public String getString(String propertyName) {
		propertyName = propertyName.toLowerCase();
		
		return getProperty(propertyName);
	}
	
	public String get(String propertyName) {
		return getString(propertyName.toLowerCase());
	}

	public void set(String propertyName, Object value) {
		setProperty(propertyName, value.toString());
	}
}
