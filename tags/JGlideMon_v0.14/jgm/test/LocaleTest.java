package jgm.test;

import jgm.Locale;

public class LocaleTest {
	public static void main(String[] args) {		
		System.out.println("Availible Locales:");
		
		for (java.util.Locale l : Locale.getLocales()) {
			System.out.println("    " + l.getDisplayName());
		}
		
		//System.out.println("\nAll Phrases:");
		//for (String key : Locale.bundle.keySet()) {
		//	System.out.println("    " + key + ": " + Locale.bundle.getString(key));
		//}
		
		Locale.setLocale(java.util.Locale.FRANCE);
		
		//System.out.println("\nAll Phrases (en francais):");
		//for (String key : Locale.bundle.keySet()) {
		//	System.out.println("    " + key + ": " + Locale.bundle.getString(key));
		//}
	}
}
