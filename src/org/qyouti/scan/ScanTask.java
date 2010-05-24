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

import org.qyouti.QyoutiView;
import java.awt.GridLayout;
import java.io.*;
import java.net.URISyntaxException;
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
    Vector<String> errorpages= new Vector<String>();

    try
    {
      File[] filenames;
      
      /*
      filenames = folder.listFiles();
      Arrays.sort(filenames);
      for ( int i=0; i<filenames.length; i++ )
      {
        if ( filenames[i].getName().startsWith( "QUE_" ) &&
             filenames[i].getName().endsWith( ".xml" )   )
          exam.loadAssessmentItem( filenames[i] );
      }
      */

      PageData page;
      filenames = scanfolder.listFiles();
      Arrays.sort(filenames);
      for ( int i=0; i<filenames.length; i++ )
      {
        if ( filenames[i].getName().endsWith( ".png" ) )
        {
          // Read data from page.
          page = PageDecoder.decodeOneArgument( exam, filenames[i].getCanonicalPath() );
          if ( page == null )
          {
            errorpages.add( filenames[i].getName() );
            continue;
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


}
