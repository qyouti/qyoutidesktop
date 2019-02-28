/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti.data;

import java.io.*;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
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
  public static final int EXAM_TYPE_FOLDER = 0;
  public static final int EXAM_TYPE_ZIP = 1;
  
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
    PrintMetricEntry pmentry;
    
    for ( i = 0; i < childfiles.length; i++ )
    {
      if ( childfiles[i].isDirectory() )
      {
        examconfig = new File( childfiles[i], "qyouti.xml" );
        if ( examconfig.exists() && examconfig.isFile() )
        {
          entry = new ExamCatEntry();
          entry.type = EXAM_TYPE_FOLDER;
          entry.name = childfiles[i].getName();
          entry.container = childfiles[i];
          entries.add( entry );
        }
      }
      else if ( childfiles[i].isFile() && childfiles[i].getName().endsWith( ".qyouti" ) )
      {
        entry = new ExamCatEntry();
        entry.type = EXAM_TYPE_ZIP;
        entry.name = childfiles[i].getName();
        entry.container = childfiles[i];
        entries.add( entry );
      }
    }

    for ( i = 0; i < entries.size(); i++ )
    {
      entry = entries.get( i );
      FileSystem fs=null;
      try
      {
        Path base;
        if ( entry.type == EXAM_TYPE_ZIP )
        {
          Path p = entry.container.toPath();
          URI uri = URI.create("jar:" + p.toUri());
          fs = FileSystems.newFileSystem(uri, new HashMap<>() );        
          base = fs.getPath("/");
        }
        else
          base = entry.container.toPath();
        try ( DirectoryStream<Path> ds = Files.newDirectoryStream(base))
        {
          for (Path child : ds)
          {
              if (Files.isDirectory(child))
                continue;

              String nm = child.getFileName().toString();
              if ( !nm.startsWith("pagination_") )
                continue;

              if ( !nm.endsWith(".xml") )
                continue;

              pmentry = new PrintMetricEntry();
              pmentry.id = nm.substring( 11, nm.length() - 4 );
              pmentry.path = child;
              entry.printmetrics.add( pmentry );
          }
        }
      }
      catch ( IOException ioe )
      {
        
      }
      
      if ( fs != null )
        try {
          fs.close();
      } catch (IOException ex) {
        Logger.getLogger(ExaminationCatalogue.class.getName()).log(Level.SEVERE, null, ex);
      }
    
    }
  }

  public File getContainer( int n )
  {
    return entries.get(n).container;
  }
  
  public boolean isZip( int n )
  {
    return entries.get(n).type == EXAM_TYPE_ZIP;
  }
  
  public Path getNewPath( String str )
  {
    File zipfile = new File( appfolder, str );
    URI uri = URI.create("jar:" + zipfile.toURI().toString() );
    try 
    {
      HashMap<String,String> hm = new HashMap<>();
      hm.put("create", "true");
      FileSystem fs = FileSystems.newFileSystem(uri, hm );        
      return fs.getRootDirectories().iterator().next();
    }
    catch ( Exception e )
    {
      e.printStackTrace();
    }
    return null;
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
  
  
  public File getExamPathFromPrintMetric( String pmid )
  {
    ExamCatEntry entry = getEntry( pmid );
    if ( entry != null ) return entry.container;
    return null;
  }

  public PaginationRecord getPrintMetric( String pmid )
  {
    PrintMetricEntry pmentry = getPrintMetricEntry( pmid );
    if ( pmentry == null ) return null;
    if ( pmentry.path == null ) return null;
    if ( pmentry.paginationrecord != null ) return pmentry.paginationrecord;
    // need to load it...
    
    return pmentry.paginationrecord = getPrintMetric( pmentry.path );
  }
  
  public static PaginationRecord getPrintMetric( Path path )
  {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder;
    Document document;
    try
    {
      builder = factory.newDocumentBuilder();
      document = builder.parse( Files.newInputStream( path ) );
      return new PaginationRecord( document );
    }
    catch ( ParserConfigurationException | SAXException | IOException ex )
    {
      Logger.getLogger( ExaminationCatalogue.class.getName() ).
              log( Level.SEVERE, null, ex );
    }
    return null;
  }
  
  class ExamCatEntry
  {
    int type;
    String name;
    File container;
    Vector<PrintMetricEntry> printmetrics = new Vector<PrintMetricEntry>();
  }
  
  class PrintMetricEntry
  {
    String id = null;
    Path path = null;
    PaginationRecord paginationrecord = null;
  }
}
