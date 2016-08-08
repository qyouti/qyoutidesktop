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
  
  public PrintThread( ExaminationData exam, File examfolder )
  {
    super();
    this.exam = exam;
    this.examfolder = examfolder;
  }
  
  public void setQyoutiFrame( QyoutiFrame frame )
  {
    this.frame = frame;
  }

  
  @Override
  public void run()
  {
      try
      {
        URI examfolderuri;
          examfolderuri = exam.examfile.getParentFile().getCanonicalFile().toURI();
        Vector<GenericDocument> paginated;
        TranscoderInput tinput;
        TranscoderOutput transout = new TranscoderOutput(
            new FileOutputStream(
                new File( examfolder.getParentFile(), examfolder.getName() + ".pdf" ) ) );


        PaginationRecord paginationrecord = new PaginationRecord(examfolder.getName());
        String printid = paginationrecord.getPrintId();
        // QuestionMetricsRecordSet qmrset = new QuestionMetricsRecordSet(printid);
        // qmrset.setMonochromePrint( false );
        MultiPagePDFTranscoder pdftranscoder = new MultiPagePDFTranscoder();

        for ( int j=0; j<exam.candidates_sorted.size(); j++ )
        {
          System.out.println( "Candidate " + (j+1) + " of " + exam.candidates_sorted.size() );
          System.out.println( "Used memory " + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() )/1000000L) );
          paginated = QTIItemRenderer.paginateItems(
              this,
              printid,
              examfolderuri,
              exam.candidates_sorted.elementAt(j),
              exam,
              //qmrset,
              paginationrecord,
              exam.getPreamble() );
          for ( int i=0; i<paginated.size(); i++ )
          {
            tinput = new TranscoderInput( paginated.elementAt(i) );
            pdftranscoder.transcode( tinput, transout );
            paginated.set(i, null);
          }
          paginated.clear();
        }
        pdftranscoder.complete();
        transout.getOutputStream().close();
        exam.setLastPrintID( printid );

//        File qmrrecfile = new File(examfolder, "printmetrics_" + printid + ".xml");
//        if ( qmrrecfile.exists() )
//          throw new IllegalArgumentException( "Unable to save print metrics." );
//        // This helps with dodgy file systems
//        try { qmrrecfile.createNewFile(); }
//        catch ( Exception ee ) {}
//        FileWriter writer = new FileWriter( qmrrecfile );
//        qmrset.emit(writer);
//        writer.close();

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

      } catch (Exception ex)
      {
        Logger.getLogger(QyoutiView.class.getName()).log(Level.SEVERE, null, ex);
      }
      finally
      {
        if ( frame != null )
          frame.pdfPrintComplete();
      }
    System.out.println( "Printing to PDF complete." );
  }
  
}
