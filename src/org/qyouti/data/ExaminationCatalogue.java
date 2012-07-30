/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti.data;

import java.io.File;
import java.util.Arrays;
import java.util.Vector;

/**
 *
 * @author jon
 */
public class ExaminationCatalogue
{

  Vector<ExamCatEntry> entries = new Vector<ExamCatEntry>();

  public ExaminationCatalogue( File appfolder )
  {
    int i, j;
    File[] childfiles = appfolder.listFiles();
    Arrays.sort( childfiles );
    File examconfig;
    ExamCatEntry entry;
    for ( i = 0; i < childfiles.length; i++ )
    {
      if ( childfiles[i].isDirectory() )
      {
        examconfig = new File( childfiles[i], "qyouti.xml" );
        if ( examconfig.exists() && examconfig.isFile() )
        {
          entry = new ExamCatEntry();
          entry.name = childfiles[i].getName();
          entry.folder = childfiles[i];
          entries.add( entry );
        }
      }
    }

    for ( i = 0; i < entries.size(); i++ )
    {
      entry = entries.get( i );
      childfiles = entry.folder.listFiles();
      String nm;
      for ( j = 0; j < childfiles.length; j++ )
      {
        if ( !childfiles[j].isFile() )
        {
          continue;
        }
        nm = childfiles[j].getName();
        if ( !nm.startsWith( "printmetrics_" ) )
        {
          continue;
        }
        if ( !nm.endsWith( ".xml" ) )
        {
          continue;
        }
        
        entry.printmetrics.add( nm.substring( 13, nm.length() - 4 ) );
      }
    }

    i=129;
  }


  public String[] getNames()
  {
    String[] names = new String[entries.size()];
    for ( int i = 0; i < entries.size(); i++ )
      names[i] = entries.get( i ).name;
    return names;
  }

  public File getExamFolderFromPrintMetric( String pmid )
  {
    int i, j;
    ExamCatEntry entry;

    for ( i = 0; i < entries.size(); i++ )
    {
      entry = entries.get( i );
      for ( j=0; j<entry.printmetrics.size(); j++ )
      {
        if ( entry.printmetrics.get( j ).equals( pmid ) )
          return entry.folder;
      }
    }
    return null;
  }


  class ExamCatEntry
  {

    String name;
    File folder;
    Vector<String> printmetrics = new Vector<String>();
  }
}
