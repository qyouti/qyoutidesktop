/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.qyouti.statistics;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Vector;

/**
 *
 * @author jon
 */
public class Histogram
{
  Hashtable<String,Counter> table = new Hashtable<String,Counter>();
  Vector<String> sorted = new Vector<String>();

  int total_count=0;

  public void add( String item )
  {
    Counter entry = table.get(item);
    if ( entry == null )
    {
      entry = new Counter();
      table.put(item, entry);
      sorted.add(item);
      Collections.sort( sorted );
    }
    entry.count++;
    total_count++;
  }

  public String[] getHeadings()
  {
    return sorted.toArray(new String[0]);
  }

  public int getTotalCount()
  {
    return total_count;
  }
  
  public int getCount( String item )
  {
    Counter entry = table.get(item);
    if ( entry == null )
      return 0;
    return entry.count;
  }

  public double getProportion( String item )
  {
    int n = getCount( item );
    return ((double)n) / ((double)total_count);
  }

  private class Counter
  {
    int count=0;
  }
}
