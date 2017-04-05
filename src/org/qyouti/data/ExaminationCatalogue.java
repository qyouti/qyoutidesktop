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

  Vector<ExamCatEntry> entries = new Vector<ExamCatEntry>();

  public ExaminationCatalogue( File appfolder )
  {
    int i, j;
    File[] childfiles = appfolder.listFiles();
    if ( childfiles == null ) return;
    Arrays.sort( childfiles );
    File examconfig;
    ExamCatEntry entry;
    PrintMetricEntry pmentry;
    
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
        if ( !nm.startsWith( "pagination_" ) )
        {
          continue;
        }
        if ( !nm.endsWith( ".xml" ) )
        {
          continue;
        }
        pmentry = new PrintMetricEntry();
        pmentry.id = nm.substring( 11, nm.length() - 4 );
        pmentry.file = childfiles[j];
        entry.printmetrics.add( pmentry );
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

  ExamCatEntry getEntry( String pmid )
  {
    int i, j;
    ExamCatEntry entry;

    for ( i = 0; i < entries.size(); i++ )
    {
      entry = entries.get( i );
      for ( j=0; j<entry.printmetrics.size(); j++ )
      {
        if ( entry.printmetrics.get( j ).id.equals( pmid ) )
          return entry;
      }
    }
    return null;    
  }

  PrintMetricEntry getPrintMetricEntry( String pmid )
  {
    int i, j;
    ExamCatEntry entry;

    for ( i = 0; i < entries.size(); i++ )
    {
      entry = entries.get( i );
      for ( j=0; j<entry.printmetrics.size(); j++ )
      {
        if ( entry.printmetrics.get( j ).id.equals( pmid ) )
          return entry.printmetrics.get( j );
      }
    }
    return null;    
  }
  
  
  public File getExamFolderFromPrintMetric( String pmid )
  {
    ExamCatEntry entry = getEntry( pmid );
    if ( entry != null ) return entry.folder;
    return null;
  }

  public PaginationRecord getPrintMetric( String pmid )
  {
    PrintMetricEntry pmentry = getPrintMetricEntry( pmid );
    if ( pmentry == null ) return null;
    if ( pmentry.file == null ) return null;
    if ( pmentry.paginationrecord != null ) return pmentry.paginationrecord;
    // need to load it...

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder;
    Document document;
    try
    {
      builder = factory.newDocumentBuilder();
      document = builder.parse( pmentry.file );
      pmentry.paginationrecord = new PaginationRecord( document );
    }
    catch ( ParserConfigurationException | SAXException | IOException ex )
    {
      Logger.getLogger( ExaminationCatalogue.class.getName() ).
              log( Level.SEVERE, null, ex );
    }
    
    return pmentry.paginationrecord;
  }
  
  class ExamCatEntry
  {

    String name;
    File folder;
    Vector<PrintMetricEntry> printmetrics = new Vector<PrintMetricEntry>();
  }
  
  class PrintMetricEntry
  {
    String id = null;
    File file = null;
    PaginationRecord paginationrecord = null;
  }
}
