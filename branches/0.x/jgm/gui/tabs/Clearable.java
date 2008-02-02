package jgm.gui.tabs;

/**
 * Indicates that a tab may be cleared of its data.
 * 
 * @author Tim
 * @since 0.13
 */
public interface Clearable {
	/**
	 * 
	 * @param clearingAll True if all tabs are being cleared
	 * or false if just the current tab.
	 */
	public void clear(boolean clearingAll);
}
