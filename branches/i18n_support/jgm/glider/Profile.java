package jgm.glider;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import javax.swing.event.*;
import javax.swing.tree.*;

public final class Profile implements Comparable<Profile>, TreeModel, Serializable {
	static Logger log = Logger.getLogger(Profile.class.getName());

	public static transient Profile root = new Profile(null, "ROOT", false);
	//public static Vector<Profile> nodes = new Vector<Profile>();
		
	//static {
	//	nodes.add(root);
	//}
	
	private Profile parent; // this's parent
	private String name;
	
	//public PriorityQueue<Profile> children = new PriorityQueue<Profile>();
	public ArrayList<Profile> children = new ArrayList<Profile>();
	
	private boolean isLeaf = false;
	
	private Profile(Profile parent, String name, boolean isLeaf) {
		this.parent = parent;
		this.name = name;
		this.isLeaf = isLeaf;
	}
	
	public boolean isLeaf() {
		return isLeaf;
	}
	
	public String getName() {
		return name;
	}
	
	public String toString() {
		return getName();
	}
	
	public String toFullString() {
		//return ((parent != null) ? parent.name : "") + ":" + ((parent != root && parent != null) ? parent.toString() + "\\" : "") + ((this != root) ? name : "");
		return ((parent != root && parent != null) ? parent.toString() + "\\" : "") + ((this != root) ? name : "");
	}
	
	public int compareTo(Profile p) {
		if (isLeaf() && !p.isLeaf())
			return 1;
		if (!isLeaf() && p.isLeaf())
			return -1;
		return getName().compareToIgnoreCase(p.getName());
	}
	
	public Profile[] getPath() {
		ArrayList<Profile> nodes = new ArrayList<Profile>();
		Profile p = this;
		
		do {
			nodes.add(p);
			p = p.parent;
		} while (p != null);
		
		Profile[] ret = new Profile[nodes.size()];
		
		for (int i = nodes.size() - 1; i >= 0; i--) {
			ret[i] = nodes.get(i);
		}
		
		return ret;
	}
	
	public Profile[] getChildren() {
		return (Profile[]) children.toArray();
	}
	
	public static Profile createNode(Profile parent, String name) {
		return createNode(parent, name, false);
	}
	
	public static Profile createLeaf(Profile parent, String name) {
		return createNode(parent, name, true);
	}
	
	private static Profile createNode(Profile parent, String name, boolean isLeaf) {
		Profile p = new Profile(parent, name, isLeaf);
		
		parent.children.add(p);
		Collections.sort(parent.children);
		//nodes.add(p);
		notifyListeners(p);
		
		return p;
	}
	
	
	///////////////////////
	// implement TreeModel
	private static transient Vector<TreeModelListener> listeners = new Vector<TreeModelListener>();
	
	public void addTreeModelListener(TreeModelListener l) {
		listeners.add(l);
	}
	
	public void removeTreeModelListener(TreeModelListener l) {
		listeners.remove(l);
	}
	
	// nodes arent changeable
	public void valueForPathChanged(TreePath path, Object newValue) {
		return;
	}
	
	public Object getRoot() {
		return root;
	}
	
	public int getChildCount(Object parent) {
		Profile p = (Profile) parent;
		return p.children.size();
	}
	
	public Object getChild(Object parent, int index) {
		Profile p = (Profile) parent;
		
		if (p.children.isEmpty()) return null;
		
		return p.children.get(index);
	}
	
	public int getIndexOfChild(Object parent, Object child) {
		Profile p = (Profile) parent;
		
		if (p.children.isEmpty()) return -1;
		
		return p.children.indexOf(child);
	}
	
	public boolean isLeaf(Object node) {
		Profile p = (Profile) node;
		return p.isLeaf();
	}
	
	public static void notifyListeners(Profile source) {
		TreeModelEvent e = new TreeModelEvent(source, source.getPath());
		
		for (TreeModelListener l : listeners) {
			l.treeNodesInserted(e);
		}
	}
	
	public static class Cache {
		public static final File profileFile = new File("profiles.dat");
		
		public static void saveProfiles() {
			try {
				ObjectOutputStream os = new ObjectOutputStream(
					new FileOutputStream(profileFile)
				);
				
				os.writeObject(root);
				os.close();
				
				log.fine("Saving profiles to " + profileFile.getName());
			} catch (IOException e) {
				log.log(Level.WARNING, "Error saving profiles", e);
			}
		}
		
		public static void loadProfiles() throws Throwable {
			try {
				ObjectInputStream is = new ObjectInputStream(
					new FileInputStream(profileFile)
				);
				
				Object o = is.readObject();
				
				if (o instanceof Profile) {
					root = (Profile) o;
					notifyListeners(root);
				}
				
				is.close();
				
				log.fine("Loading profiles from " + profileFile.getName());
			} catch (Throwable e) {
				// catch the exception here so we can print something
				// out and call reset() but throw it again so the
				// caller knows it wasn't successful
				
				log.log(Level.WARNING, "Error loading profiles", e);
				reset();
				
				throw e;
			}
		}
		
		public static void reset() {
			root = new Profile(null, "ROOT", false);;
			notifyListeners(root);
		}
	}
}
