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
import java.io.*;
import java.net.URISyntaxException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Vector;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import org.qyouti.data.*;
import org.qyouti.scan.process.PageDecoder;

/**
 *
 * @author jon
 */
public class ScanTask
        extends Thread
{
  QyoutiView view;
  ExaminationData exam;
  File scanfolder;
  public boolean active=false;
  Vector<String> errorpages= new Vector<String>();

  public ScanTask( QyoutiView view, ExaminationData exam, File scanfolder )
  {
    this.view = view;
    this.exam = exam;
    this.scanfolder = scanfolder;
  }

  @Override
  public void run()
  {
    active=true;

    try
    {
      File[] filenames;
      
      PageData page;
      filenames = scanfolder.listFiles();
      Arrays.sort(filenames);
      FileInputStream fis;
      FileChannel fc;
      MappedByteBuffer bb;
      PDFFile pdffile;
      PDFPage pdfpage;
      Image image;

      for ( int i=0; i<filenames.length; i++ )
      {
        if ( filenames[i].getName().endsWith( ".png" ) ||
             filenames[i].getName().endsWith( ".jpg" ) )
        {
          // Read data from page.
          page = PageDecoder.decode( exam, filenames[i].getCanonicalPath() );
          postProcessPage( page, filenames[i].getCanonicalPath() );
        }
        if ( filenames[i].getName().endsWith( ".pdf" ) )
        {
          // Open the file and then get a channel from the stream
          fis = new FileInputStream(filenames[i].getCanonicalPath());
          fc = fis.getChannel();

          // Get the file's size and then map it into memory
          int sz = (int)fc.size();
          bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, sz);
          pdffile = new PDFFile( bb );
          for ( int j=0; j<pdffile.getNumPages(); j++ )
          {
            //  Pages start at 1 and asking for 0 also returns 1!!!
            pdfpage = pdffile.getPage(j+1,true);
            System.out.println( "Scanning PDF file " + (pdfpage.getWidth()/72.0) + "\" by " + (pdfpage.getHeight()/72.0) + "\"" );
            image = pdfpage.getImage(
                (int)(300.0*pdfpage.getWidth()/72.0),
                (int)(300.0*pdfpage.getHeight()/72.0), null, null );
            System.out.println( "Class " + image.getClass().getCanonicalName() );
            page = PageDecoder.decode( exam, (BufferedImage)image, filenames[i].getCanonicalPath() + " page " + (j+1) );
            postProcessPage( page, filenames[i].getCanonicalPath() + " page " + (j+1) );
          }
          fc.close();
        }
      }

      for ( int n=0; n<errorpages.size(); n++ )
        System.out.println( "ERROR with " + errorpages.get(n) );

      if ( errorpages.size() == 0 )
        exam.save();

    } catch (Exception ex)
    {
      ex.printStackTrace();
    }
    

    active=false;
  }

  private void postProcessPage( PageData page, String sourcename )
  {
    if ( page == null )
    {
      errorpages.add( sourcename );
      return;
      //break;
    }
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
      view.gotoQuestion( page.questions.lastElement() );
    }
  }

}
