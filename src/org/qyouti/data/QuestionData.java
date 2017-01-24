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

package org.qyouti.data;

import java.io.*;
import java.util.*;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;
import org.qyouti.qti1.QTIResponse;
import org.qyouti.qti1.element.QTIElementItem;
import org.qyouti.qti1.element.QTIElementResponselabel;
import org.qyouti.qti1.element.QTIElementResponselid;
import org.qyouti.qti1.ext.qyouti.QTIExtensionRendersketcharea;
import org.qyouti.qti1.ext.qyouti.QTIExtensionRespextension;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author jon
 */
public class QuestionData
                extends AbstractTableModel
{
  public static final int EXAMINER_DECISION_NONE = 0;
  public static final int EXAMINER_DECISION_STAND = 1;
  public static final int EXAMINER_DECISION_OVERRIDE = 2;
  
  public PageData page;
  public String ident="";
  public boolean imagesprocessed = false;
  public boolean needsreview = false;
  private int examinerdecision = EXAMINER_DECISION_NONE;
  public Vector<ResponseData> responsedatas = new Vector<ResponseData>();
  public Hashtable<String,ResponseData> responsedatatable = new Hashtable<String,ResponseData>();

  public OutcomeData outcomes;

  public QuestionData( PageData page )
  {
    this.page = page;
    outcomes = new OutcomeData( page.exam );
    page.questions.add( this );
    page.exam.processDataChanged( this );
  }


  public QuestionData( PageData page, Element element )
  {
    this.page = page;

    ident = element.getAttribute("ident");
    String str = element.getAttribute( "needsreview" );
    needsreview = str != null && str.toLowerCase().startsWith( "y" );
    str = element.getAttribute( "imagesprocessed" );
    imagesprocessed = str != null && str.toLowerCase().startsWith( "t" );
    str = element.getAttribute( "decision" );
    try { examinerdecision = Integer.parseInt( str ); }
    catch ( NumberFormatException nfe ) { examinerdecision =  EXAMINER_DECISION_NONE; }
    
    NodeList nl = element.getElementsByTagName( "response" );
    ResponseData response;

    for ( int j=0; j<nl.getLength(); j++ )
      response = new ResponseData( this, (Element)nl.item( j ), j );
    // then recreate enhanced images
//    for ( int j=0; j<responses.size(); j++ )
//      responses.get( j ).filterImage( page.blackness, page.lightestredmean );

    outcomes = new OutcomeData( page.exam );
    nl = element.getElementsByTagName( "outcome" );
    OutcomeDatum outcome;
    for ( int j=0; j<nl.getLength(); j++ )
    {
      outcome = new OutcomeDatum( (Element)nl.item(j) );
      outcomes.addDatum( outcome );
    }
    
    page.questions.add( this );
    page.exam.processDataChanged( this );
  }

  private File getFile( String fname )
  {
    File scanfolder = page.exam.getResponseImageFolder();
    return new File( scanfolder, fname );
  }

  public String getImageFileName()
  {
    return ident + "_" + page.candidate_number + ".jpg";
  }
  
  public File getImageFile()
  {
    return getFile( getImageFileName() );
  }

  
  public boolean areImagesProcessed()
  {
    return imagesprocessed;
  }

  public void setImagesProcessed( boolean processed )
  {
    this.imagesprocessed = processed;
  }
  
  public int getExaminerDecision()
  {
    return examinerdecision;
  }

  public void setExaminerDecision( int examinerdecision )
  {
    if ( this.examinerdecision == examinerdecision )
      return;
    this.examinerdecision = examinerdecision;
    page.candidate.processAllResponses();
    // which row?  Don't know.
    page.exam.processRowsUpdated( this, 0, getRowCount()-1 );
  }

  
  
  public ResponseData getResponseData( String ident )
  {
    return responsedatatable.get( ident );
  }

  public QTIElementItem getItem()
  {
    if ( ident == null ) return null;
    return page.exam.getAssessmentItem( ident );
  }
  
  public void processResponses()
  {
    QTIElementItem qtiitem = page.exam.getAssessmentItem( ident );

    if ( qtiitem == null )
    {
      System.err.println( "Can't find question " + ident );
      return;
    }

    if ( !qtiitem.isSupported() )
    {
      System.err.println( "Unsupported type of question " + ident );
      return;
    }

    //System.out.println( "==================================================" );
    //System.out.println( "Question title = " + qtiitem.getTitle() );

    qtiitem.reset();
    qtiitem.setReferencedByCandidate();
    // Push user responses into the QTI elements for this item
    // so outcomes can be computed

    // find all the response elements
    QTIResponse[] responses = qtiitem.getResponses();
    QTIElementResponselabel[] rlabels;
    ResponseData responsedata;
    for ( int j=0; j<responses.length; j++ )
    {
      if ( !responses[j].isSupported() )
        continue;
      if ( responses[j] instanceof QTIElementResponselid )
      {
        QTIElementResponselid responselid = (QTIElementResponselid)responses[j];
        rlabels = responselid.getResponseLabels();
        for ( int k=0; k<rlabels.length; k++ )
        {
          responsedata = getResponseData( rlabels[k].getIdent() );
          if ( responsedata.isSelected() )
            responselid.addCurrentValue( responsedata.ident );
        }
        continue;
      }
      if ( responses[j] instanceof QTIExtensionRespextension )
      {
        QTIExtensionRespextension responseext = (QTIExtensionRespextension)responses[j];
        QTIExtensionRendersketcharea sketch = responseext.getRendersketcharea();
        if ( sketch != null )
        {
          responsedata = getResponseData( sketch.getIdent() );
          responseext.setCurrentValue( responsedata.getImageFileName() );
        }
      }
    }

    // All candidate responses are pushed into the qti structure
    // so it's time to get the qti library to computer the
    // item outcomes.
    qtiitem.computeOutcomes();

    // record item outcomes from qti elements into candidate data store
    String[] outcome_names = qtiitem.getOutcomeNames();
    OutcomeDatum outcomedata;
    outcomes.clear();
    for ( int i=0; i<outcome_names.length; i++ )
    {
      outcomedata = new OutcomeDatum();
      outcomedata.name = outcome_names[i];
      outcomedata.value = qtiitem.getOutcomeValue( outcome_names[i] );
      //System.out.println( "Outcome " + outcomedata.name );
      //System.out.println( "Value " + outcomedata.value );
      outcomes.addDatum( outcomedata );
    }
    page.exam.processDataChanged( this );
  }

  public int getOutcomeCount()
  {
    if ( outcomes == null) return 0;
    return outcomes.getRowCount();
  }

  public String getOutcomeIdentifier( int offset )
  {
    QTIElementItem qtiitem = page.exam.getAssessmentItem( ident );
    String[] outcome_names = qtiitem.getOutcomeNames();
    return outcome_names[offset];
  }

  public String getOutcomeValueString( int n )
  {
    OutcomeDatum outcomedata = outcomes.getDatumAt( n );
    if ( outcomedata == null || outcomedata.value == null )
      return "null";
    return outcomedata.value.toString();
  }



  public void emit( Writer writer )
          throws IOException
  {
    writer.write( "      <question ident=\"" + ident + "\"" );
    writer.write( " imagesprocessed=\"" + (imagesprocessed?"true":"false") + "\" " );
    writer.write( " needsreview=\"" + (needsreview?"yes":"no") + "\"" );
    writer.write( " decision=\"" + examinerdecision + "\"" );
    writer.write( ">\n" );
    for ( int i=0; i<responsedatas.size(); i++ )
      responsedatas.get( i ).emit( writer );

    for ( int i=0; i<outcomes.getRowCount(); i++ )
      outcomes.getDatumAt( i ).emit( writer );
    
    writer.write( "      </question>\n" );
  }






  public int getRowCount()
  {
    return responsedatas.size();
  }

    @Override
  public int getColumnCount()
  {
    return 7; //examinerdecision==EXAMINER_DECISION_OVERRIDE?7:6;
  }

  @Override
  public String getColumnName(int columnIndex)
  {
    switch ( columnIndex )
    {
      case 0:
        return "*";
      case 1:
        return "Correct";
      case 2:
        return "Response";
      case 3:
        return "Enhanced";
      case 4:
        return "Interpreted";
      case 5:
        return "Doubted";
      case 6:
        return "Examiner Override";
    }
    return null;
  }

  @Override
  public Class getColumnClass(int columnIndex)
  {
    switch ( columnIndex )
    {
      case 0:
        return String.class;
      case 1:
        return String.class;
      case 2:
        return Icon.class;
      case 3:
        return Icon.class;
      case 4:
        return Boolean.class;
      case 5:
        return String.class;
      case 6:
        if ( examinerdecision==EXAMINER_DECISION_OVERRIDE )
          return Boolean.class;
        return String.class;
    }
    return null;
  }

  @Override
  public boolean isCellEditable( int rowIndex, int columnIndex )
  {
    return columnIndex == 6 && examinerdecision == EXAMINER_DECISION_OVERRIDE;
  }

  public int getRowHeight( int rowIndex )
  {
    int h = -1; // indicate no preference with -1
    
    ResponseData response = responsedatas.get( rowIndex );
    if ( response.getImage() != null )
      h = response.getImageHeight();
    
    return h;
  }
  
    @Override
  public Object getValueAt(int rowIndex, int columnIndex)
  {
    ResponseData response = responsedatas.get( rowIndex );
    QTIElementItem qtiitem = page.exam.getAssessmentItem( ident );
    
    switch ( columnIndex )
    {
      case 0:
        return response.ident;
        //return String.valueOf((char)('a'+rowIndex));
      case 1:
        if ( qtiitem == null ) return "?";
        if ( !qtiitem.isSupported() ) return "?";
        if ( !qtiitem.isMultipleChoice() ) return "n/a";
        QTIElementResponselabel rl = qtiitem.getResponselabelByOffset( rowIndex );
        if ( rl == null ) return "n/a";
        if ( rl.isCorrect() ) return "yes";
        if ( rl.isIncorrect() ) return "no";
        return "n/a";
      case 2:
        if ( response.getImage() == null )
            return "?";
        return new ImageIcon( response.getImage() );
      case 3:
        if ( response.getFilteredImage() == null )
            return "?";
        return new ImageIcon( response.getFilteredImage() );
      case 4:
        return response.candidate_selected;
      case 5:
        return response.needsreview?"dubious":"";
      case 6:
        if ( examinerdecision==EXAMINER_DECISION_OVERRIDE )
          return new Boolean( response.examiner_selected );
        return "n/a";
    }
    return null;
  }

  @Override
  public void setValueAt(Object aValue, int rowIndex, int columnIndex)
  {
    if ( !isCellEditable( rowIndex, columnIndex ) )
      return;

    ResponseData response = responsedatas.get( rowIndex );

    response.examiner_selected = ((Boolean)aValue).booleanValue();

    //processResponses();
    page.candidate.processAllResponses();
    page.exam.processRowsUpdated( this, 0, getRowCount()-1 );
  }
}
