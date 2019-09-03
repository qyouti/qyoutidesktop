/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti.data;

import java.io.*;
import java.util.Arrays;
import java.util.Vector;
import java.util.logging.*;
import javax.xml.parsers.*;
import org.qyouti.qti1.gui.*;
import org.w3c.dom.*;
import org.xml.sax.*;

/**
 *
 * @author jon
 */
public class ExaminationCatalogue
{
  File appfolder;
  Vector<ExamCatEntry> entries = new Vector<ExamCatEntry>();

  public ExaminationCatalogue( File appfolder )
  {
    this.appfolder = appfolder;
    scan();
  }
  
  void scan()
  {
    entries.clear();
    
    int i, j;
    File[] childfiles = appfolder.listFiles();
    if ( childfiles == null ) return;
    Arrays.sort( childfiles );
    File examconfig;
    ExamCatEntry entry;
    
    for ( i = 0; i < childfiles.length; i++ )
    {
      if ( childfiles[i].isDirectory() )
      {
        examconfig = new File( childfiles[i], ExaminationData.mainarchivename );
        if ( examconfig.exists() && examconfig.isFile() )
        {
          entry = new ExamCatEntry();
          entry.name = childfiles[i].getName();
          entry.folder = childfiles[i];
          entries.add( entry );
        }
      }
    }
  }

  public String[] getNames()
  {
    String[] names = new String[entries.size()];
    for ( int i = 0; i < entries.size(); i++ )
      names[i] = entries.get( i ).name;
    return names;
  }

  class ExamCatEntry
  {
    String name;
    File folder;
  }  
}
