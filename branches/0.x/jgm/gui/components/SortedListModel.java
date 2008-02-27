package jgm.gui.components;

import javax.swing.*;
import java.util.*;

/**
 * 
 * @author Tim
 * @since 0.15
 */
public class SortedListModel extends AbstractListModel {

  // Define a SortedSet
  SortedSet<String> model;

  public SortedListModel() {
    // Create a TreeSet
    // Store it in SortedSet variable
    model = new TreeSet<String>();
  }

  // ListModel methods
  public int getSize() {
    // Return the model size
    return model.size();
  }

  public String getElementAt(int index) {
    // Return the appropriate element
    return model.toArray(new String[] {})[index];
  }

  // Other methods
  public void add(String element) {
    if (model.add(element)) {
      fireContentsChanged(this, 0, getSize());
    }
  }

  public void addAll(String ... elements) {
    Collection<String> c = Arrays.asList(elements);
    model.addAll(c);
    fireContentsChanged(this, 0, getSize());
  }

  public void clear() {
    model.clear();
    fireContentsChanged(this, 0, getSize());
  }

  public boolean contains(Object element) {
    return model.contains(element);
  }

  public Object firstElement() {
    // Return the appropriate element
    return model.first();
  }

  public String[] toArray() {
	    // Return the appropriate element
	    return model.toArray(new String[] {});
  }
  
  public Iterator iterator() {
    return model.iterator();
  }

  public Object lastElement() {
    // Return the appropriate element
    return model.last();
  }

  public boolean removeElement(Object element) {
    boolean removed = model.remove(element);
    if (removed) {
      fireContentsChanged(this, 0, getSize());
    }
    return removed;   
  }
}