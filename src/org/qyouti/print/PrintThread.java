/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti.print;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;
import org.apache.batik.dom.*;
import org.apache.batik.transcoder.*;
import org.apache.fop.svg.*;
import org.apache.pdfbox.io.*;
import org.apache.pdfbox.multipdf.*;
import org.apache.pdfbox.util.*;
import org.qyouti.*;
import org.qyouti.data.*;
import org.qyouti.qti1.gui.*;

/**
 *
 * @author jon
 */
public class PrintThread extends Thread
{
  ExaminationData exam;
  File examfolder;
  QyoutiFrame frame;

  public static final int TYPE_PAPERS = 0;
  public static final int TYPE_ANALYSIS = 1;
  
  int type = TYPE_PAPERS;
          
  public PrintThread( ExaminationData exam, File examfolder )
  {
    super();
    this.exam = exam;
    this.examfolder = examfolder;
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
      URI examfolderuri = exam.getExamFolder().getCanonicalFile().toURI();
      List<GenericDocument> paginated;
      PageData page;
      TranscoderInput tinput;
      TranscoderOutput transout;
      File pagefile;
      ArrayList<File> pagefiles = new ArrayList<>();
      File pdffile;
      if ( type == TYPE_ANALYSIS )
        pdffile = new File( examfolder.getParentFile(), examfolder.getName() + "_analysis.pdf" );
      else
        pdffile = new File( examfolder.getParentFile(), examfolder.getName() + ".pdf" );
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
        paginationrecord = new PaginationRecord(examfolder.getName());
        printid = paginationrecord.getPrintId();
      }
      PDFTranscoder pdft = new PDFTranscoder();

      java.util.Vector<CandidateData> candidates;
      if ( type == TYPE_ANALYSIS )
      {
        candidates = new java.util.Vector<CandidateData>();
        candidates.add( null );
      }
      else
        candidates = exam.candidates_sorted;
      
      for ( int j=0; j<candidates.size(); j++ )
      {
        System.out.println( "Candidate " + (j+1) + " of " + candidates.size() );
        System.out.println( "Used memory " + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() )/1000000L) );
        paginated = QTIItemRenderer.paginateItems(
            this,
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
          pagefile = new File( examfolder.getParentFile(), examfolder.getName() + "_" + p++ + ".pdf" );
          pagefiles.add( pagefile );
          System.out.println( "Transcoding page " + (i+1) + " to " + pagefile.getAbsolutePath() );
          tinput = new TranscoderInput( paginated.get( i ) );
          transout = new TranscoderOutput( new FileOutputStream( pagefile ) );
          pdft.transcode( tinput, transout );
          transout.getOutputStream().close();
          paginated.set(i, null);
          System.out.println( "Page done." );
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
      File pagrecfile = new File(examfolder, "pagination_" + printid + ".xml");
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
        frame.pdfPrintComplete( error );
    }
    System.out.println( "Printing to PDF complete." );
  }
  
}
