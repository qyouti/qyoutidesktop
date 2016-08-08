/*
 *
 * Copyright 2010 Leeds Metropolitan University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may 
 * not use this file except in compliance with the License. You may obtain 
 * a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 *
 *
 */



/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.qyouti.scan;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import org.qyouti.QyoutiView;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Vector;
import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import org.qyouti.*;
import org.qyouti.data.*;
import org.qyouti.scan.process.PageDecoder;

/**
 *
 * @author jon
 */
public class ScanTask
        extends Thread
        implements ImageObserver
{
  QyoutiPreferences preferences;
  ScanTaskListener listener;
  ExaminationData exam;
  File scanfolder;
  public boolean active=false;
  //Vector<String> errorpages= new Vector<String>();
  boolean image_ready;
  boolean preprocess=false;
  boolean commandline=false;
  int exitCode=0;

  public ScanTask( QyoutiPreferences preferences, ExaminationData exam, File scanfolder, boolean preprocess, boolean commandline )
  {
    this.preferences = preferences;
    this.exam = exam;
    this.scanfolder = scanfolder;
    this.preprocess = preprocess;
    this.commandline = commandline;
    if ( !preprocess && !exam.scanfolder.exists() )
      exam.scanfolder.mkdir();
  }

  public void setScanTaskListener( ScanTaskListener listener )
  {
    this.listener = listener;
  }
  
  @Override
  public void run()
  {
    if ( preprocess )
    {
      runPreProcess();
    }
    else
    {
      runImport();
    }
    if ( listener != null ) listener.scanCompleted();
  }



  public void runPreProcess()
  {
    int i, j, k, l;
    active=true;

    try
    {
      File[] scanfiles;

      PageData page;
      scanfiles = scanfolder.listFiles();
      Arrays.sort(scanfiles);
      FileInputStream fis;
      FileChannel fc;
      MappedByteBuffer bb;
      PDFFile pdffile;
      PDFPage pdfpage;
      Image image;
      String uri;
      String newname;
      File newfile;
      String foldername;
      File folder, destfile;
      boolean success;

      
      int th    = preferences.getPropertyInt( "qyouti.scan.threshold" );
      int inset = preferences.getPropertyInt( "qyouti.scan.inset" );
      PageDecoder pagedecoder = new PageDecoder( (double)th / 100.0, inset );
      
      for ( i=0; i<scanfiles.length; i++ )
      {
        if ( scanfiles[i].getName().endsWith( ".png" ) ||
             scanfiles[i].getName().endsWith( ".jpg" ) )
        {
          System.out.println( "\n\nProcessing " + scanfiles[i].getName() );

          // Read data from page.
          page = pagedecoder.identifyPage( exam, scanfiles[i].getCanonicalPath(), exam.getPageCount() );
          if ( page.error != null )
            System.out.println( "ERROR:  " + page.error );
          exam.addPage( page );

          foldername = page.getPreferredFolderName();
          folder = new File( scanfolder, foldername );
          if ( !folder.exists() )
            folder.mkdir();
          destfile = new File( folder, scanfiles[i].getName() );
          success = scanfiles[i].renameTo( destfile );
          if ( success ) page.source = destfile.getCanonicalPath();

          if ( !commandline )
            exam.pagelistmodel.fireTableChanged( new TableModelEvent( exam.pagelistmodel ) );
        }
      }
      if ( !commandline )
        JOptionPane.showMessageDialog( null, "Finished processing images." );

    } catch (Exception ex)
    {
      ex.printStackTrace();
      if ( !commandline )
        JOptionPane.showMessageDialog( null, "An error interupted the processing." );
    }

    active=false;
  }



  public void runImport()
  {
    int i, j, k, l;
    active=true;

    try
    {
      File[] filenames;
      
      PageData page, rescanpage;
      filenames = scanfolder.listFiles();
      Arrays.sort(filenames);
      FileInputStream fis;
      FileChannel fc;
      MappedByteBuffer bb;
      PDFFile pdffile;
      PDFPage pdfpage;
      Image image;
      String uri;
      String newname, movedname;
      File newfile;
      int processed_count=0;
      int page_limit = this.commandline?50:2000;
      
      long errorname = System.currentTimeMillis();
      

      int th    = preferences.getPropertyInt( "qyouti.scan.threshold" );
      int inset = preferences.getPropertyInt( "qyouti.scan.inset" );
      PageDecoder pagedecoder = new PageDecoder( (double)th / 100.0, inset );

      System.out.println( "Started scanning image files for import." );


      for ( i=0; i<filenames.length; i++ )
      {
        if ( filenames[i].getName().endsWith( ".png" ) ||
             filenames[i].getName().endsWith( ".jpg" ) )
        {
          // in command line mode skip any files that start with "imported_"
          // because those have been processed already
          if ( commandline && filenames[i].getName().startsWith( "imported_" ) )
            continue;

          rescanpage = null;
          // file already scanned?
          for ( j=0; j<exam.getPageCount(); j++ )
          {
            uri = filenames[i].toURI().toString();
            if ( uri.equals( exam.getPage( j ).source ) )
            {
              rescanpage = exam.getPage( j );
              break;
            }
          }

//          if ( rescanpage != null )
//            System.out.println( "RESCAN--------------------");
          // skip this file if name is already recorded and was processed ok
          if ( rescanpage != null && rescanpage.processed )
          {
//            System.out.println( "SKIPPING--------------------");
            continue;
          }

          if ( processed_count >= page_limit )
            break;
          System.out.println( "\n\nProcessing " + filenames[i].getName() );
          processed_count++;

          // Read data from page.
          page = pagedecoder.decode( exam, filenames[i].getCanonicalPath(), rescanpage != null?j:exam.getPageCount() );
          if ( rescanpage != null )
            exam.replacePage( j, page );  // put it where the earlier one was
          else
            exam.addPage( page );   // entirely new file - put it at the end


          // change file name to match candidate!!
          newname = page.getPreferredFileName();
          if ( newname == null )
            newname = "imported_unidentifiable_" + Long.toHexString( errorname++ ) + page.getPreferredFileExtension();
          if ( page.error != null )
            newname = "imported_scan_error_" + Long.toHexString( errorname++ ) + page.getPreferredFileExtension();

          System.out.println( "Proposed new name: " + newname );
          newfile = new File( scanfolder, newname );
          if ( newfile.exists() )
          {
            movedname = "imported_replaced_" +
                        Long.toHexString( errorname++ ) +
                        "_" + newfile.getName();
            newfile.renameTo( new File( scanfolder, movedname ) );
          }

          if ( filenames[i].renameTo( newfile ) )
            page.source = newfile.toURI().toString();

          exam.pagelistmodel.fireTableChanged( new TableModelEvent( exam.pagelistmodel ) );
        }
//        if ( filenames[i].getName().endsWith( ".pdf" ) )
//        {
//          // Open the file and then get a channel from the stream
//          fis = new FileInputStream(filenames[i].getCanonicalPath());
//          fc = fis.getChannel();
//
//          // Get the file's size and then map it into memory
//          int sz = (int)fc.size();
//          bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, sz);
//          pdffile = new PDFFile( bb );
//          for ( j=0; j<pdffile.getNumPages(); j++ )
//          {
//            //  Pages start at 1 and asking for 0 also returns 1!!!
//            pdfpage = pdffile.getPage(j+1,true);
//            //System.out.println( "Scanning PDF file " + (pdfpage.getWidth()/72.0) + "\" by " + (pdfpage.getHeight()/72.0) + "\"" );
//            image_ready = false;
//            image = pdfpage.getImage(
//                (int)(300.0*pdfpage.getWidth()/72.0),
//                (int)(300.0*pdfpage.getHeight()/72.0), null, this );
//            //System.out.println( "Class " + image.getClass().getCanonicalName() );
//            while ( !image_ready )
//              Thread.sleep( 100 );
//            page = PageDecoder.decode( exam, (BufferedImage)image, filenames[i].getCanonicalPath(), exam.pages.size() );
//            exam.pages.add( page );
//            exam.pagelistmodel.fireTableChanged( new TableModelEvent( exam.pagelistmodel ) );
//            image.flush();
//          }
//          fc.close();
//        }
      }

      if ( processed_count == 0 )
      {
        exitCode = 1;
        return;
      }

      PageData otherpage;
      // check for duplicate scans
      for ( i=1; i<exam.getPageCount(); i++ )
      {
        page = exam.getPage(i );
        if ( page.error != null )
          continue;
        
        for ( j=0; j<i; j++ )
        {
          otherpage = exam.getPage( j );
          if ( otherpage.error != null )
            continue;

          if ( page.code.equals( otherpage.code ) )
          {
            if ( !page.processed )
              page.error = "This is a duplicate of a page previously scanned.";
            if ( !otherpage.processed )
              otherpage.error = "This is a duplicate of a page previously scanned.";
            break;
          }

          if ( page.candidate_number == null           ||
               otherpage.candidate_number == null      ||
               page.candidate_number.length() == 0     ||
               otherpage.candidate_number.length() == 0 )
            break;

          if ( !page.candidate_number.equals( otherpage.candidate_number ) )
            break;
          // if the pages are for the same candidate do they have
          // duplicate questions?
          for ( k=0; k<page.questions.size(); k++ )
          {
            for ( l=0; l<otherpage.questions.size(); l++ )
            {
              if ( page.questions.get( k ).ident.equals( otherpage.questions.get( l ) ) )
              {
                if ( !page.processed )
                  page.error = "Page has duplicate question.";
                if ( !otherpage.processed )
                  otherpage.error = "Page has duplicate question.";
                break;
              }
            }
          }
        }
      }

      // Images are now fully processed so now it's
      // time to work out the outcomes
      for ( i=0; i<exam.getPageCount(); i++ )
      {
        page = exam.getPage( i );
        if ( page.error != null )
          continue;
        if ( page.processed )
          continue;
        processPageOutcomes( page );
      }

      if ( !this.commandline )
        JOptionPane.showMessageDialog( null, "Data will now be saved. Please do not shut down the software until complete." );
      exam.save();
      exam.pagelistmodel.fireTableChanged( new TableModelEvent( exam.pagelistmodel ) );
      if ( !this.commandline )
        JOptionPane.showMessageDialog( null, "Page scans processed and data saved." );
    } catch (Exception ex)
    {
      ex.printStackTrace();
      JOptionPane.showMessageDialog( null, "An error interupted the scan processing and no changes were saved." );
    }
    

    active=false;
  }

  private void processPageOutcomes( PageData page )
  {
    if ( page == null )
    {
      return;
    }

    page.candidate = page.exam.linkPageToCandidate(page);
    if ( page.candidate ==null )
        return;
    
    // Compute outcomes based on QTI def of question
    for ( int j=0; j<page.questions.size(); j++ )
    {
      page.questions.get( j ).processResponses();
    }
    if ( page.questions.size() > 0 )
    {
      // recalculates total score after every page
      page.candidate.processAllResponses();
      // and updates presentation of data
      //view.gotoQuestion( page.questions.lastElement() );
    }
    page.processed = true;
  }

  @Override
  public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height)
  {
//    System.out.println( "=====================================" );
//    System.out.println( "Image flags + " + Integer.toBinaryString(infoflags) );
//    System.out.println( "=====================================" );
    image_ready = (infoflags & ImageObserver.ALLBITS ) != 0;
    return !image_ready;
  }

  public int getExitCode()
  {
    return exitCode;
  }

}
