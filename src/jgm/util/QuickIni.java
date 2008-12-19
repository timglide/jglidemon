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
/*
 * Written by Mike Wallace (mfwallace at gmail.com).  Available
 * on the web site http://mfwallace.googlepages.com/.
 * 
 * Copyright (c) 2006 Mike Wallace.
 * 
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom
 * the Software is furnished to do so, subject to the following
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package jgm.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Class to store the contents of an INI file in a hashmap.
 * This makes for quickly reading and writing an INI file
 * since the file's contents are only read once.  The
 * drawback is that comments and similar lines are discarded
 * during reading, so they're not saved when writing back
 * to the file.
 * 
 * @author Mike Wallace
 * @version 1.0
 */
public class QuickIni
{
  /**
   * Whether the input file has been read.
   */
  private boolean dataLoaded = false;
  
  /**
   * The name of the input file.
   */
  private String filename = null;
  
  /**
   * The line feed string.
   */
  private static final String LINEFEED = "\n";
  
  /**
   * The contents of the INI file.
   */
  private HashMap<String, HashMap<String, String>> contents = null;
  
  
  /**
   * Default constructor.
   */
  private QuickIni()
  {
    super();
  }
  
  
  /**
   * Constructor taking the input filename.
   * 
   * @param fileName the name of the input file
   */
  public QuickIni(final String fileName)
  {
    super();
    filename = fileName;
  }
  
  
  /**
   * Mark the data as needing to be reread from the file.
   */
  public void reload()
  {
    dataLoaded = false;
  }
  
  
  /**
   * Process a line of data.
   * 
   * @param line the line of data from the file
   * @param currSection the name of the current section
   * @return the name of the current section
   */
  private String process(final String line,
                         final String currSection)
  {
    // Check the input
    if ((line == null) || (line.length() < 3))
    {
      // It's null or empty, so return
      return currSection;
    }
    
    // Check for a comment
    if ((line.charAt(0) == ';') || (line.charAt(0) == '#'))
    {
      // It's a comment, so skip it
      return currSection;
    }
    
    // Check if the line starts with a '['.  If it
    // does, it's a section name
    if (line.charAt(0) == '[')
    {
      // See if there's a ]
      final int nCloseIndex = line.indexOf(']');
      if (nCloseIndex < 0)
      {
        // Not found.  Ignore the line.
        return currSection;
      }
      
      // Save the string between the brackets
      String section = line.substring(1, nCloseIndex);
      
      // Check if the name is non-empty
      if (section.length() < 1)
      {
        // It's an empty name, so set it to null and return
        return null;
      }
      
      // See if we already have the section name
      HashMap<String, String> props = contents.get(section);
      if (props == null)
      {
        // We don't have the name, so save it with an empty
        // (non-null) hashmap for the properties
        contents.put(section, new HashMap<String, String>(5));
      }
      
      // Return the name of the current section
      return section;
    }
    else
    {
      // Check if we're in a section
      if ((currSection == null) || (currSection.length() < 1))
      {
        // We're not, so return
        return currSection;
      }
      
      // Check if it's a property with a value
      final int nEqualIndex = line.indexOf('=');
      if (nEqualIndex <= 0)
      {
        // No equals sign, or it's the start of the line,
        // so skip the line
        return currSection;
      }
      
      // Save the length of the line
      final int nLineLen = line.length();
      if (nEqualIndex == (nLineLen - 1))
      {
        // The line ends with the equals sign,
        // so skip the line
        return currSection;
      }
      
      // Get the property and the value
      final String prop = line.substring(0, nEqualIndex).trim();
      final String value = line.substring(nEqualIndex + 1);
      
      // Check the property and value
      if ((prop.length() < 1) || (value.length() < 1))
      {
        // Something is empty, so return it
        return currSection;
      }
      
      // Get the hashset for this section
      HashMap<String, String> props = contents.get(currSection);
      if (props == null)
      {
        // This should never happen
        throw new RuntimeException("Null hashmap for a section");
      }
      
      // See if the property has already been found
      if (props.get(prop) != null)
      {
        // It's already in the map, so return
        return currSection;
      }
      
      // Add the property and value
      props.put(prop, value);
      
      // Return the name of this section
      return currSection;
    }
  }
  
  
  /**
   * Return all contents of the INI file.
   * 
   * @return all contents of the INI file
   */
  public HashMap<String, HashMap<String, String>> getContents()
  {
    // Verify the data is loaded
    checkDataLoad();
    
    // Check the data
    if (contents == null)
    {
      return null;
    }
    
    // Declare the variable that will get returned
    HashMap<String, HashMap<String, String>> outContents =
      new HashMap<String, HashMap<String, String>>(10);
    
    // Iterate over the contents
    Set<String> sects = contents.keySet();
    for (String sect : sects)
    {
      // Get the list of values for this section
      HashMap<String, String> values = contents.get(sect);
      
      // Check if there are any properties for this section
      if (values == null)
      {
        continue;
      }
      
      // Declare the hashmap to hold all values in the current section
      HashMap<String, String> outSection = new HashMap<String, String>(20);
      
      // Get the set of properties for this section
      Set<String> props = values.keySet();
      for (String prop : props)
      {
        // Get the value for this property
        String value = values.get(prop);
        
        if (value != null)
        {
          // Put the current property/value in the output section
          outSection.put(prop, value);
        }
      }
      
      // Add the output section to the return variable
      outContents.put(sect, outSection);
    }
    
    // Return the copy of the data
    return outContents;
  }
  
  
  /**
   * Check whether the data has been loaded.
   * If not, load it now.
   */
  private void checkDataLoad()
  {
    // Check if the data has already been loaded
    if (dataLoaded)
    {
      return;
    }
    
    // Mark the data as loaded
    dataLoaded = true;
    
    // This will hold the name of the current section
    String currSection = null;
    
    // Clear the content data
    contents = new HashMap<String, HashMap<String, String>>(5);
    
    // This will hold any error message that is encountered
    String errMsg = null;
    
    // Create a File object for the input file
    File file = new File(filename);
    if (!file.exists())
    {
      // The file does not exist, so return
      return;
    }
    
    // Read the file and store the data
    BufferedReader in = null;
    try
    {
      // Open a reader to the file
      in = new BufferedReader(new FileReader(file));
      
      // Read all the lines
      String str;
      while ((str = in.readLine()) != null)
      {
        currSection = process(str, currSection);
      }
      
      // Close the reader
      in.close();
      in = null;
    }
    catch (IOException ioe)
    {
      errMsg = ioe.getMessage();
      System.err.println("Exception: " + errMsg);
    }
    finally
    {
      if (in != null)
      {
        try
        {
          in.close();
        }
        catch (IOException e)
        {
          errMsg = e.getMessage();
        }
        
        in = null;
      }
    }
    
    // Uncomment this block if an exception should be
    // thrown when an error occurs reading the file
    // Check for an error
    
    if (errMsg != null)
    {
      throw new RuntimeException(errMsg);
    }
    
  }
  
  
  /**
   * Get an integer property.
   * 
   * @param sectionName the name of the section
   * @param propertyName the name of the property
   * @return the property value
   */
  public int getIntegerProperty(final String sectionName,
                                final String propertyName)
  {
    return getIntegerProperty(sectionName, propertyName, 0);
  }
  
  
  /**
   * Get an integer property.
   * 
   * @param sectionName the name of the section
   * @param propertyName the name of the property
   * @param defaultValue the default value to return
   * @return the property value
   */
  public int getIntegerProperty(final String sectionName,
                                final String propertyName,
                                final int defaultValue)
  {
    // Get the string property
    final String s = getStringProperty(sectionName, propertyName);
    
    // Check for an illegal return value
    if ((s == null || (s.length() < 1)))
    {
      return defaultValue;
    }
    
    // Declare our variable that gets returned.  Set it to
    // the default value in case an exception is thrown while
    // parsing the string.
    int val = defaultValue;
    
    // Parse the string as a number
    try
    {
      // Parse the string
      int tempValue = Integer.parseInt(s);
      
      // If we reach this point, it was a success
      val = tempValue;
    }
    catch (NumberFormatException nfe)
    {
      val = defaultValue;
    }
    
    // Return the value
    return val;
  }
  
  
  /**
   * Returns the property value for the specified section and
   * property.
   *
   * @param sectionName  the name of the section
   * @param propertyName the name of the property
   * @return the value for the specified section/property
   */
  public boolean getBooleanProperty(final String sectionName,
                                    final String propertyName)
  {
    return getBooleanProperty(sectionName, propertyName, false);
  }


  /**
   * Returns the property value for the specified section and
   * property.  If the property is not found, defaultValue
   * is returned instead.
   *
   * @param sectionName  the name of the section
   * @param propertyName the name of the property
   * @param defaultValue the default value to return, if it's not found
   * @return the value for the specified section/property
   */
  public boolean getBooleanProperty(final String sectionName,
                                    final String propertyName,
                                    final boolean defaultValue)
  {
    // Get the string from the input file
    final String str = getStringProperty(sectionName, propertyName);
    
    // Check if the returned string is null
    if ((str == null) || (str.length() < 1))
    {
      // It is, so return the default value
      return defaultValue;
    }
    
    // Declare our boolean variable that gets returned.  The default
    // value is false.
    boolean val = false;
    
    // Convert the string into a boolean
    if ((str.equals("1")) || (str.equalsIgnoreCase("true")))
    {
      // Set val to true for certain string values
      val = true;
    }
    
    // Return the boolean value
    return val;
  }
  
  
  /**
   * Returns the property value for the specified section and
   * property.
   *
   * @param sectionName  the name of the section
   * @param propertyName the name of the property
   * @return the value for the specified section/property
   */
  public long getLongProperty(final String sectionName,
                              final String propertyName)
  {
    return getLongProperty(sectionName, propertyName, 0L);
  }
  
  
  /**
   * Returns the property value for the specified section and
   * property.  If the property is not found, defaultValue
   * is returned instead.
   *
   * @param sectionName  the name of the section
   * @param propertyName the name of the property
   * @param defaultValue the default value to return, if it's not found
   * @return the value for the specified section/property
   */
  public long getLongProperty(final String sectionName,
                              final String propertyName,
                              final long defaultValue)
  {
    // Get the string from the input file
    final String str = getStringProperty(sectionName, propertyName);
    
    // Check if the returned string is null
    if (str == null)
    {
      // It is, so return the default value
      return defaultValue;
    }
    
    // Declare our variable that gets returned.  Set it to
    // the default value in case an exception is thrown while
    // parsing the string.
    long val = defaultValue;
    
    // Parse the string as a number
    try
    {
      // Parse the string
      long tempValue = Long.parseLong(str);
      
      // If we reach this point, it was a success
      val = tempValue;
    }
    catch (NumberFormatException nfe)
    {
      val = defaultValue;
    }
    
    // Return the value
    return val;
  }
  
  
  /**
   * Returns the property value for the specified section and
   * property.
   *
   * @param sectionName  the name of the section
   * @param propertyName the name of the property
   * @return the value for the specified section/property
   */
  public double getDoubleProperty(final String sectionName,
                                  final String propertyName)
  {
    return getDoubleProperty(sectionName, propertyName, 0.0);
  }
  
  
  /**
   * Returns the property value for the specified section and
   * property.  If the property is not found, defaultValue
   * is returned instead.
   *
   * @param sectionName  the name of the section
   * @param propertyName the name of the property
   * @param defaultValue the default value to return, if it's not found
   * @return the value for the specified section/property
   */
  public double getDoubleProperty(final String sectionName,
                                  final String propertyName,
                                  final double defaultValue)
  {
    // Get the string from the input file
    final String str = getStringProperty(sectionName, propertyName);
    
    // Check if the returned string is null
    if ((str == null) || (str.length() < 1))
    {
      // It is, so return the default value
      return defaultValue;
    }
    
    // Declare our variable that gets returned.  Set it to
    // the default value in case an exception is thrown while
    // parsing the string.
    double val = defaultValue;
    
    // Parse the string as a number
    try
    {
      // Parse the string
      double tempValue = Double.parseDouble(str);
      
      // If we reach this point, it was a success
      val = tempValue;
    }
    catch (NumberFormatException nfe)
    {
      val = defaultValue;
    }
    
    // Return the value
    return val;
  }
  
  
  /**
   * Returns the property value for the specified section and
   * property.  If the property is not found, defaultValue
   * is returned instead.
   *
   * @param sectionName  the name of the section
   * @param propertyName the name of the property
   * @return the value for the specified section/property
   */
  public Date getDateProperty(final String sectionName,
                              final String propertyName)
  {
    // Get the string from the input file
    final String str = getStringProperty(sectionName, propertyName);
    
    // Check if the returned string is null
    if ((str == null) || (str.length() < 1))
    {
      // It is, so return the default value
      return null;
    }
    
    // Declare our variable that gets returned.  Set it to
    // the default value in case an exception is thrown while
    // parsing the string.
    Date date = null;
    
    // Parse the string as a number
    try
    {
      // Parse the string
      long tempValue = Long.parseLong(str);
      
      // If we reach this point, the long was parsed correctly
      if (tempValue >= 0)
      {
        // Create a Date object using the temp value
        date = new Date(tempValue);
      }
    }
    catch (NumberFormatException nfe)
    {
      // Default to the current time
      date = new Date(System.currentTimeMillis());
    }
    
    // Return the value
    return date;
  }
  
  
  /**
   * Returns the value for the property in the section.
   * 
   * @param sectionName the name of the section
   * @param propertyName the name of the property
   * @return the value for propertyName in sectionName
   */
  public String getStringProperty(final String sectionName,
                                  final String propertyName)
  {
    return getStringProperty(sectionName, propertyName, null);
  }
  
  
  /**
   * Returns the value for the property in the section.
   * 
   * @param sectionName the name of the section
   * @param propertyName the name of the property
   * @param defaultValue the default value to return if not found
   * @return the value for propertyName in sectionName
   */
  public String getStringProperty(final String sectionName,
                                  final String propertyName,
                                  final String defaultValue)
  {
    // Check if the data has been loaded
    checkDataLoad();
    
    // Check if it has any data, and check the input
    if ((contents == null) || (contents.size() < 1) ||
        (sectionName == null) || (sectionName.length() < 1) ||
        (propertyName == null) || (propertyName.length() < 1))
    {
    	//if (jgm.JGlideMon.debug)
    	//	System.out.println(sectionName + "." + propertyName + " def val 1");
      return defaultValue;
    }
    
    // Get the property list for the section
    HashMap<String, String> props = contents.get(sectionName);
    if ((props == null) || (props.size() < 1))
    {
      // Return
    	//if (jgm.JGlideMon.debug)
    	//	System.out.println(sectionName + "." + propertyName + " def val 2");
      return defaultValue;
    }
    
    // Get the value
    String value = props.get(propertyName);
    if (value == null)
    {
      // It was null, so it wasn't found
    	//if (jgm.JGlideMon.debug)
    	//	System.out.println(sectionName + "." + propertyName + " def val 3");
      return defaultValue;
    }
    
    // Return the value that was found
    return value;
  }


  /**
   * Writes the propertyName=value to the specified section.
   *
   * @param sectionName  the name of the section
   * @param propertyName the name of the property
   * @param value        the property value
   * @return the success of the operation
   */
  public boolean setBooleanProperty(final String sectionName,
                                    final String propertyName,
                                    final boolean value)
  {
    // Save the value as a string
    final String sValue = Boolean.toString(value);
    
    // Pass the string to setStringProperty
    return setStringProperty(sectionName, propertyName, sValue);
  }
  
  
  /**
   * Writes the propertyName=value to the specified section.
   *
   * @param sectionName  the name of the section
   * @param propertyName the name of the property
   * @param value        the property value
   * @return the success of the operation
   */
  public boolean setIntegerProperty(final String sectionName,
                                    final String propertyName,
                                    final int value)
  {
    // Save the value as a string
    final String sValue = Integer.toString(value);
    
    // Pass the string to setStringProperty
    return setStringProperty(sectionName, propertyName, sValue);
  }
  
  
  /**
   * Writes the propertyName=value to the specified section.
   *
   * @param sectionName  the name of the section
   * @param propertyName the name of the property
   * @param value        the property value
   * @return the success of the operation
   */
  public boolean setLongProperty(final String sectionName,
                                 final String propertyName,
                                 final long value)
  {
    // Save the value as a string
    final String sValue = Long.toString(value);
    
    // Pass the string to setStringProperty
    return setStringProperty(sectionName, propertyName, sValue);
  }
  
  
  /**
   * Writes the propertyName=value to the specified section.
   *
   * @param sectionName  the name of the section
   * @param propertyName the name of the property
   * @param value        the property value
   * @return the success of the operation
   */
  public boolean setDoubleProperty(final String sectionName,
                                   final String propertyName,
                                   final double value)
  {
    // Save the value as a string
    final String sValue = Double.toString(value);
    
    // Pass the string to setStringProperty
    return setStringProperty(sectionName, propertyName, sValue);
  }
  
  
  /**
   * Writes the propertyName=value to the specified section.
   *
   * @param sectionName  the name of the section
   * @param propertyName the name of the property
   * @param value        the property value
   * @return the success of the operation
   */
  public boolean setDateProperty(final String sectionName,
                                 final String propertyName,
                                 final Date value)
  {
    // Check the date
    if (value == null)
    {
      // It's null, so pass null to setStringProperty
      return setStringProperty(sectionName, propertyName, null);
    }
    
    // Convert the date to milliseconds
    final long lDateInMS = value.getTime();
    
    // Convert the milliseconds to a string
    final String sDate = Long.toString(lDateInMS);
    
    // Pass the milliseconds string to setStringProperty
    return setStringProperty(sectionName, propertyName, sDate);
  }
  
  
  /**
   * Set the string property.
   * 
   * @param sectionName the name of the section
   * @param propertyName the name of the property
   * @param value the value to set
   * @return whether it was successful
   */
  public boolean setStringProperty(final String sectionName,
                                   final String propertyName,
                                   final String value)
  {
    // Check if the data has been loaded
    checkDataLoad();
    
    // Check the input
    if ((sectionName == null) || (sectionName.length() < 1) ||
        (propertyName == null) || (propertyName.length() < 1))
    {
      // Invalid input
      return false;
    }
    
    // See if any data is set
    if (contents == null)
    {
      if (value == null)
      {
        // The value is null, so there's nothing to do
        return true;
      }
      
      // Declare the holder for our property
      HashMap<String, String> prop = new HashMap<String, String>(5);
      prop.put(propertyName, value);
      
      // Declare the contents variable and store the data
      contents = new HashMap<String, HashMap<String, String>>(10);
      contents.put(sectionName, prop);
      
      return true;
    }
    else
    {
      // Contents are not null
      HashMap<String, String> section = contents.get(sectionName);
      if (section == null)
      {
        // Check if there's anything to store
        if (value == null)
        {
          // This value is already null
          return true;
        }
        
        // Declare a holder for the property
        section = new HashMap<String, String>(5);
        
        // Store the property and value
        section.put(propertyName, value);
        
        // Add the property to the section
        contents.put(sectionName, section);
        
        return true;
      }
      else
      {
        // The section was found.  Check the value
        if (value == null)
        {
          // See if the property exists
          String currValue = section.get(propertyName);
          if (currValue == null)
          {
            // Nothing to do
            return true;
          }
          
          // Remove the value from the property
          section.remove(propertyName);
          
          return true;
        }
        else
        {
          // Add the value
          section.put(propertyName, value);
          
          return true;
        }
      }
    }
  }
  
  
  /**
   * Save the updated data to the file.
   */
  public void updateFile()
  {
    // Check if the data has been loaded
    checkDataLoad();
    
    // This will hold any error message
    String errMsg = null;
    
    // Write the file contents
    BufferedWriter out = null;
    try
    {
      // Open the writer
      out = new BufferedWriter(new FileWriter(filename));
      
      // Check if it has any data
      if (contents != null)
      {
        // Iterate over the contents
        Set<String> sects = contents.keySet();
        for (String sect : sects)
        {
          // Write the section name
          out.write("[" + sect + "]" + LINEFEED);
          
          // Get the list of values
          HashMap<String, String> values = contents.get(sect);
          
          // Check if there are any properties for this section
          if (values == null)
          {
            out.write(LINEFEED);
            continue;
          }
          
          // Get the set of properties for this section
          Set<String> props = values.keySet();
          for (String prop : props)
          {
            // Get the value for this property
            String value = values.get(prop);
            
            // Add to the string
            out.write(prop + "=" + value + LINEFEED);
          }
          
          // Add a blank line after the section
          out.write(LINEFEED);
        }
      }
      
      // Close the writer
      out.close();
    }
    catch (IOException ioe)
    {
      errMsg = ioe.getMessage();
    }
    finally
    {
      if (out != null)
      {
        try
        {
          out.close();
        }
        catch (IOException e)
        {
          errMsg = e.getMessage();
        }
        
        out = null;
      }
    }
    
    // Check for an error
    if (errMsg != null)
    {
      throw new RuntimeException(errMsg);
    }
  }
  
  
  /**
   * Return the names of all sections.
   * 
   * @return the names of all sections
   */
  public Iterator<String> getAllSectionNames()
  {
    // Check if the data has been loaded
    checkDataLoad();
    
    // Check if it has any data
    if ((contents == null) || (contents.size() < 1))
    {
      return (new ArrayList<String>(1).iterator());
    }
    
    // Get all the section names
    List<String> names = new ArrayList<String>(contents.size());
    
    // Iterate over the contents
    Set<String> sects = contents.keySet();
    for (String sect : sects)
    {
      names.add(sect);
    }
    
    // Return the list of names
    return names.iterator();
  }
  
  
  /**
   * Return the list of all properties for this section.
   * 
   * @param sectionName the section to get the properties from
   * @return the list of properties in the section
   */
  public List<String> getAllPropertyNames(final String sectionName)
  {
    // Check if the data has been loaded
    checkDataLoad();
    
    // Check if it has any data, and check
    // the requested section name
    if ((contents == null) || (contents.size() < 1) ||
        (sectionName == null) || (sectionName.length() < 1))
    {
      // Return an empty list
      return (new ArrayList<String>(0));
    }
    
    // Get the hashmap for this section
    HashMap<String, String> properties = contents.get(sectionName);
    if ((properties == null) || (properties.size() < 1))
    {
      // Return an empty list
      return (new ArrayList<String>(0));
    }
    
    // Declare the array to return
    final int size = properties.size();
    List<String> propList = new ArrayList<String>(size);
    
    // Iterate over the properties
    Iterator<String> propSet = properties.keySet().iterator();
    while (propSet.hasNext())
    {
      // Copy the current entry
      propList.add(propSet.next());
    }
    
    // Return the list
    return propList;
  }
  
  
  /**
   * Create a string represenation of this object.
   * 
   * @return the object as a string
   */
  @Override
  public String toString()
  {
    // Check if the data's loaded
    checkDataLoad();
    
    // Declare the string builder
    StringBuilder sb = new StringBuilder(200);
    
    // Check if it has any data
    if ((contents == null) || (contents.size() < 1))
    {
      // No data, so return an empty string
      return sb.toString();
    }
    
    // Iterate over the contents
    Set<String> sects = contents.keySet();
    for (String sect : sects)
    {
      // Add to the string builder
      sb.append("[").append(sect).append("]").append(LINEFEED);
      
      // Get the list of values
      HashMap<String, String> values = contents.get(sect);
      
      // Check if there are any properties for this section
      if (values == null)
      {
        sb.append(LINEFEED);
        continue;
      }
      
      // Get the set of properties for this section
      Set<String> props = values.keySet();
      for (String prop : props)
      {
        // Get the value for this property
        String value = values.get(prop);
        
        // Add to the string
        sb.append(prop).append("=")
          .append(value).append(LINEFEED);
      }
      
      // Add a line feed
      sb.append(LINEFEED);
    }
    
    // Return the string
    return sb.toString();
  }
  
  
  /**
   * Main entry point for the application.
   * 
   * @param args arguments passed to the program
   */
  public static void main(final String[] args)
  {
    // Initialize the reader
    QuickIni reader = new QuickIni("data.ini");
    
    // Get a property value
    System.out.println(reader.getStringProperty("Mail", "CMCDLLNAME"));
    
    // Set a property value
    reader.setIntegerProperty("Harvard", "Rules", 1);
    
    // Write the data to the file
    reader.updateFile();
  }
}
