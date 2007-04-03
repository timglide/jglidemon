package jgm;

import jgm.locale.*;

import javax.swing.*;
import java.util.*;
import java.io.*;
import java.net.URI;
import java.util.jar.*;

public class Locale {
	public static final String BUNDLE_NAME = "locale.Main";
	public static final String BUNDLE_BASE = "jgm.locale.";
	public static final java.util.Locale LOCALE_ENGLISH = new java.util.Locale("en", "");
	public static final java.util.Locale LOCALE_DEFAULT = java.util.Locale.getDefault();
	private static java.util.Locale LOCALE_CURRENT = LOCALE_DEFAULT;
	
	private static Map<String, ResourceBundle> bundles =
		new HashMap<String, ResourceBundle>();
	
	private Locale() {}
	
	private static ResourceBundle getBundle(String name) {
		if (!bundles.containsKey(name)) {
			System.err.println("Initing bundle " + name);
			bundles.put(name, ResourceBundle.getBundle(BUNDLE_BASE + name, LOCALE_CURRENT, Locale.class.getClassLoader()));
		}
			
		return bundles.get(name);
	}
	
	public static void setLocale(String lang, String country) {
		setLocale(new java.util.Locale(lang, country));	
	}
	
	public static void setLocale(java.util.Locale l) {
		java.util.Locale.setDefault(l);
		LOCALE_CURRENT = l;
		bundles.clear();
		
		notifyLocaleChanged();
	}
	
	
	private static List<LocaleListener> listeners = new Vector<LocaleListener>();
	
	public static void addListener(LocaleListener l) {
		listeners.add(l);
	}
	
	public static void removeListener(LocaleListener l) {
		listeners.remove(l);
	}
	
	private static void notifyLocaleChanged() {
		for (LocaleListener l : listeners) {
			System.out.println("Notifying " + l.getClass().getName());
			l.localeChanged();
		}
	}
	
	
	// conveinance when retrieving several keys
	// within the same section
	
	private static Map<Thread, String> savedBases =
		new HashMap<Thread, String>();
	
	public static void setBase(String s) {
		savedBases.put(Thread.currentThread(), s);
	}
	
	public static void clearBase() {
		savedBases.remove(Thread.currentThread());
	}
	
	// conveinance methods to change components' text
	
	public static void _(JLabel comp, String key) {
		if (comp == null) return;
		comp.setText(get(key));
	}
	
	public static void _(JButton comp, String key) {
		if (comp == null) return;
		comp.setText(get(key));
	}

	public static void _(JMenu comp, String key) {
		if (comp == null) return;

		String s = get(key);
		int mnu = getMnemonic(s);
		s = stripMnemonic(s);
		comp.setMnemonic(mnu);
		
		comp.setText(s);
	}
	
	public static void _(JMenuItem comp, String key) {
		if (comp == null) return;

		String s = get(key);
		int mnu = getMnemonic(s);
		s = stripMnemonic(s);
		comp.setMnemonic(mnu);
		
		comp.setText(s);
	}
	
	public static void _(java.awt.MenuItem comp, String key) {
		if (comp == null) return;

		String s = get(key);
		//int mnu = getMnemonic(s);
		s = stripMnemonic(s);
		//comp.setMnemonic(mnu);
		
		comp.setLabel(s);
	}
	
	public static String _(String key) {
		return get(key);
	}
	
	private static String get(String key) {
		String[] parts = getKeyParts(key);
		return getBundle(parts[0]).getString(parts[0] + "." + parts[1]);
	}
	
	private static String stripMnemonic(String s) {
		return s.replaceAll("&", "");
	}
	
	private static String[] getKeyParts(String s) {
		String[] ret = new String[2];
		
		// allow overriding of bundle name when
		// setBase is used
		boolean exactKey = s.charAt(0) == '^';
		
		if (!exactKey && savedBases.containsKey(Thread.currentThread())) {
			ret[0] = savedBases.get(Thread.currentThread());
			ret[1] = s;
		} else {
			int i = s.indexOf('.');
			ret[0] = s.substring(exactKey ? 1 : 0, i);
			ret[1] = s.substring(i + 1);
		}
		
		//System.err.printf("Key=%s::%s\n", ret[0], ret[1]);
		return ret;
	}
	
	private static char getMnemonic(String s) {
		int i = s.indexOf('&');
		
		if (i < 0 || i == s.length() - 1) return '\0';
		
		return Character.toUpperCase(s.charAt(i + 1));
	}
	
	// adapted from azureus
	public static java.util.Locale[] getLocales() {
		String bundleFolder = BUNDLE_NAME.replace('.', '/');
		final String prefix = BUNDLE_NAME.substring(BUNDLE_NAME.lastIndexOf('.') + 1);
		final String extension = ".properties";

		System.out.println("bundleFolder: " + bundleFolder + "; " + bundleFolder.concat(extension));
		String urlString = jgm.JGlideMon.class.getResource(bundleFolder.concat(extension)).toExternalForm();
		System.out.println("urlString: " + urlString);

		bundleFolder = "jgm/" + bundleFolder;
		
		String[] bundles = null;

		if (urlString.startsWith("jar:file:")) {
			File jar = Util.getJarFileFromURL(urlString);

			if (jar != null) {
				try {
					System.out.println("jar: " + jar.getAbsolutePath());
					JarFile jarFile = new JarFile(jar);
					Enumeration<JarEntry> entries = jarFile.entries();
					ArrayList<String> list = new ArrayList<String>(250);
					
					while (entries.hasMoreElements()) {
						JarEntry jarEntry = entries.nextElement();
						//System.out.println("jarEntry: " + jarEntry.getName());
						
						if (jarEntry.getName().startsWith(bundleFolder)
							&& jarEntry.getName().endsWith(extension)) {
							System.out.println("jarEntry: " + jarEntry.getName());
							list.add(jarEntry.getName().substring(
							bundleFolder.length() - prefix.length()));
							// "MessagesBundle_de_DE.properties"
						}
					}
					
					bundles = list.toArray(new String[list.size()]);
				} catch (Exception e) {
					e.printStackTrace();
					//Debug.printStackTrace(e);
				}
			} else {
				System.err.println("Jar was null trying to find locales.");
			}
		} else {
			File bundleDirectory = new File(URI.create(urlString)).getParentFile();
			// System.out.println("bundleDirectory: " +
			// bundleDirectory.getAbsolutePath());

			bundles = bundleDirectory.list(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.startsWith(prefix) && name.endsWith(extension);
				}
			});
		}

		HashSet<String> bundleSet = new HashSet<String>();
		
		// Any duplicates will be ignored
		bundleSet.addAll(Arrays.asList(bundles));

		List<java.util.Locale> foundLocalesList = new ArrayList<java.util.Locale>(bundleSet.size());

		foundLocalesList.add(LOCALE_ENGLISH);

		Iterator<String> val = bundleSet.iterator();
		
		while (val.hasNext()) {
			String sBundle = val.next();

			// System.out.println("ResourceBundle: " + bundles[i]);
			if (prefix.length() + 1 < sBundle.length() - extension.length()) {
				String locale = sBundle.substring(prefix.length() + 1, sBundle.length() - extension.length());
				//System.out.println("Locale: " + locale);
				String[] sLocalesSplit = locale.split("_", 3);
				
				if (sLocalesSplit.length > 0 && sLocalesSplit[0].length() == 2) {
					if (sLocalesSplit.length == 3) {
						foundLocalesList.add( new java.util.Locale(sLocalesSplit[0], sLocalesSplit[1], sLocalesSplit[2]));
					} else if (sLocalesSplit.length == 2 && sLocalesSplit[1].length() == 2) {
						foundLocalesList.add( new java.util.Locale(sLocalesSplit[0], sLocalesSplit[1]));
					} else {
						foundLocalesList.add( new java.util.Locale(sLocalesSplit[0]));
					}
				} else {
					if (sLocalesSplit.length == 3 && 
						sLocalesSplit[0].length() == 0 && 
						sLocalesSplit[2].length() > 0) {
						foundLocalesList.add( new java.util.Locale(sLocalesSplit[0], sLocalesSplit[1], sLocalesSplit[2]));
					}
				}
			}
		}

		java.util.Locale[] foundLocales = new java.util.Locale[foundLocalesList.size()];

		foundLocalesList.toArray(foundLocales);

		try {
			Arrays.sort(foundLocales, new Comparator<java.util.Locale>() {
				public final int compare (java.util.Locale a, java.util.Locale b) {
					return a.getDisplayName(a).compareToIgnoreCase(b.getDisplayName(b));
				}
			});
		} catch (Throwable e) {
			// user has a problem whereby a null-pointer exception occurs when sorting the
			// list - I've done some fixes to the locale list construction but am
			// putting this in here just in case
			//Debug.printStackTrace( e );
			e.printStackTrace();
		}
		
		return foundLocales;
	}
}
