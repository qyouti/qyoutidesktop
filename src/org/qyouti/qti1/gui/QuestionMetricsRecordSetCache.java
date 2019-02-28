/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.qyouti.qti1.gui;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Hashtable;
import org.qyouti.data.ExaminationData;

/**
 *
 * @author jon
 */
public class QuestionMetricsRecordSetCache
{
  ExaminationData exam;
  Hashtable<String,QuestionMetricsRecordSet> cache =
      new Hashtable<String,QuestionMetricsRecordSet>();

  public QuestionMetricsRecordSetCache( ExaminationData exam )
  {
    this.exam = exam;
  }

  public QuestionMetricsRecordSet getSet( String name )
  {
    QuestionMetricsRecordSet set = cache.get(name);
    if ( set != null ) return set;

//    Path file = basefolder.resolve( "printmetrics_" + name + ".xml" );
//    if ( Files.exists(file) && Files.isRegularFile(file) )
//      return new QuestionMetricsRecordSet( file );

//    File[] flist = basefolder.listFiles();
//    for ( int i=0; i<flist.length; i++ )
//    {
//      if ( !flist[i].isDirectory() )
//        continue;
//      file = new File( flist[i], "printmetrics_" + name + ".xml" );
//      if ( file.exists() && file.isFile() )
//        return new QuestionMetricsRecordSet( file );
//    }
    return null;
  }
}
