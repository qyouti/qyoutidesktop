/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti.print;

import org.qyouti.fonts.QyoutiFontManager;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;
import org.apache.avalon.framework.configuration.*;
import org.apache.batik.dom.*;
import org.apache.batik.transcoder.*;
import org.apache.fop.svg.*;
import org.apache.pdfbox.io.*;
import org.apache.pdfbox.multipdf.*;
import org.apache.pdfbox.util.*;
import org.qyouti.*;
import org.qyouti.data.*;
import org.qyouti.fonts.*;
import org.qyouti.qti1.gui.*;
import org.qyouti.util.*;


/**
 *
 * @author jon
 */
public class PrintThread extends Thread
{
  
  public static final String CONFIGXML = 
          "<?xml version=\"1.0\"?>" +
          "<fop version=\"1.0\">" +
          "  <use-cache>false</use-cache>" +
          "  <fonts>" +
          //"  <font embed-url=\"jar:file:/home/jon/files/projects/qyoutifonts/dist/qyoutifonts.jar!/org/qyouti/fonts/resources/FreeSans.ttf\"/>" +          
          "    DIRECTORY" +
          "  </fonts>" +
          "</fop>";  
  
  ExaminationData exam;
  File examcontainer;
  QyoutiFontManager fontmanager;
  QyoutiFrame frame;
  MissingGlyphReport mgr = new MissingGlyphReport();

  public static final int TYPE_PAPERS = 0;
  public static final int TYPE_ANALYSIS = 1;
  
  int type = TYPE_PAPERS;
            
  public PrintThread( ExaminationData exam, File examcontainer, QyoutiFontManager fontmanager )
  {
    super();
    this.fontmanager = fontmanager;
    this.exam = exam;
    this.examcontainer = examcontainer;
  }

  public int getType()
  {
    return type;
  }

  public void setType( int type )
  {
    this.type = type;
  }
  
  
  
  public void setQyoutiFrame( QyoutiFrame frame )
  {
    this.frame = frame;
  }

  
  @Override
  public void run()
  {
    int p=1;
    boolean error = false;
    
    try
    {
      URI examfolderuri = exam.getExamContainer().getParentFile().toURI();
      List<GenericDocument> paginated;
      PageData page;
      TranscoderInput tinput;
      TranscoderOutput transout;
      File pagefile, svgfile;
      ArrayList<File> pagefiles = new ArrayList<>();
      File pdffile;
      if ( type == TYPE_ANALYSIS )
        pdffile = new File( examcontainer.getParentFile(), examcontainer.getName() + "_analysis.pdf" );
      else
        pdffile = new File( examcontainer.getParentFile(), examcontainer.getName() + ".pdf" );
      PDFMergerUtility pdfmerger = new PDFMergerUtility();
      pdfmerger.setDestinationFileName( pdffile.getAbsolutePath() );        
      PaginationRecord paginationrecord;
      String printid;
      if ( type == TYPE_ANALYSIS )
      {
        paginationrecord = null;
        printid = "no id";
      }
      else
      {
        paginationrecord = new PaginationRecord(examcontainer.getName());
        printid = paginationrecord.getPrintId();
      }
      
      File fontfolder = new File( examcontainer.getParent(), "fonts" );
      String strconfig = CONFIGXML.replace( "DIRECTORY",  fontfolder.getPath()  );
      //DefaultConfigurationBuilder cfgBuilder = new DefaultConfigurationBuilder();
      //Configuration fopconfig = cfgBuilder.build( new ByteArrayInputStream( strconfig.getBytes( "UTF8" ) ) );
      Configuration fopconfig;
      fopconfig = fontmanager.getFOPConfiguration();
      PDFTranscoder pdft = new PDFTranscoder();
      pdft.configure( fopconfig );

      java.util.Vector<CandidateData> candidates;
      if ( type == TYPE_ANALYSIS )
      {
        candidates = new java.util.Vector<CandidateData>();
        candidates.add( null );
      }
      else
        candidates = exam.candidates_sorted;
      
      QTIItemRenderer renderer = new QTIItemRenderer(fontmanager,type,examfolderuri,exam);
      for ( int j=0; j<candidates.size(); j++ )
      {
        System.out.println( "Candidate " + (j+1) + " of " + candidates.size() );
        System.out.println( "Used memory " + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() )/1000000L) );
        paginated = renderer.paginateItems(
            fontmanager,
            mgr,
            type,
            printid,
            examfolderuri,
            candidates.elementAt(j),
            exam,
            //qmrset,
            paginationrecord,
            exam.getPreamble() );
        System.out.println( "SVG Ready" );
        for ( int i=0; i<paginated.size(); i++ )
        {
          svgfile = new File( examcontainer.getParentFile(), examcontainer.getName() + "_" + p + ".svg" );
          QyoutiUtils.dumpXMLFile( svgfile.getAbsolutePath(), paginated.get( i ).getDocumentElement(), true );
          pagefile = new File( examcontainer.getParentFile(), examcontainer.getName() + "_" + p + ".pdf" );
          p++;
          pagefiles.add( pagefile );
          System.out.println( "Transcoding page " + (i+1) + " to " + pagefile.getAbsolutePath() );
          tinput = new TranscoderInput( paginated.get( i ) );
          transout = new TranscoderOutput( new FileOutputStream( pagefile ) );
          pdft.transcode( tinput, transout );
          transout.getOutputStream().close();
          paginated.set(i, null);
          System.out.println( "Page done." );
          //svgfile.delete();
        }
        paginated.clear();
      }

      System.out.println( "Merging all candidates, all pages...." );
      for ( int i=0; i<pagefiles.size(); i++ )
        pdfmerger.addSource( pagefiles.get( i ) );

      pdfmerger.mergeDocuments( MemoryUsageSetting.setupMixed( 100L * 1024L * 1024L ) );
      //pdfmerger.mergeDocuments();

      for ( int i=0; i<pagefiles.size(); i++ )
        pagefiles.get( i ).delete();

      if ( paginationrecord == null )
        return;
      
      //pdftranscoder.complete();
      //transout.getOutputStream().close();
      exam.setLastPrintID( printid );

      System.out.println( "Recording pagination data." );

      FileWriter writer;
      File pagrecfile = new File(examcontainer, "pagination_" + printid + ".xml");
      if ( pagrecfile.exists() )
        throw new IllegalArgumentException( "Unable to save pagination record." );
      // This helps with dodgy file systems
      try { pagrecfile.createNewFile(); }
      catch ( Exception ee ) {}
      writer = new FileWriter( pagrecfile );
      paginationrecord.emit(writer);
      writer.close();
    }
    catch (TranscoderException te )
    {
      Logger.getLogger(PrintThread.class.getName()).log(Level.SEVERE, null, te);      
      Logger.getLogger(PrintThread.class.getName()).log(Level.SEVERE, null, te.getException() );  
      error = true;
    }
    catch (Exception ex)
    {
      Logger.getLogger(PrintThread.class.getName()).log(Level.SEVERE, null, ex);
      error = true;
    }
    finally
    {
      if ( frame != null )
        frame.pdfPrintComplete( error, mgr );
    }
    System.out.println( "Printing to PDF complete." );
  }
  
}
