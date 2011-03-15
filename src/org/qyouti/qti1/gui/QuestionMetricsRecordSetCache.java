/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.qyouti.qti1.gui;

import java.io.File;
import java.util.Hashtable;

/**
 *
 * @author jon
 */
public class QuestionMetricsRecordSetCache
{
  File basefolder;
  Hashtable<String,QuestionMetricsRecordSet> cache =
      new Hashtable<String,QuestionMetricsRecordSet>();

  public QuestionMetricsRecordSetCache(File basefolder)
  {
    this.basefolder = basefolder;
  }

  public QuestionMetricsRecordSet getSet( String name )
  {
    QuestionMetricsRecordSet set = cache.get(name);
    if ( set != null ) return set;

    File file = new File( basefolder, "printmetrics_" + name + ".xml" );
    if ( file.exists() && file.isFile() )
      return new QuestionMetricsRecordSet( file );

    File[] flist = basefolder.listFiles();
    for ( int i=0; i<flist.length; i++ )
    {
      if ( !flist[i].isDirectory() )
        continue;
      file = new File( flist[i], "printmetrics_" + name + ".xml" );
      if ( file.exists() && file.isFile() )
        return new QuestionMetricsRecordSet( file );
    }
    return null;
  }
}
