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
  public PageData page;
  public String ident="";
  public Vector<ResponseData> responsedatas = new Vector<ResponseData>();
  public Hashtable<String,ResponseData> responsedatatable = new Hashtable<String,ResponseData>();

  public OutcomeData outcomes = new OutcomeData();

  public QuestionData( PageData page )
  {
    this.page = page;
    page.questions.add( this );
    page.exam.fireTableDataChanged();
  }


  public QuestionData( PageData page, Element element )
  {
    this.page = page;

    ident = element.getAttribute("ident");
    NodeList nl = element.getElementsByTagName( "response" );
    ResponseData response;

    for ( int j=0; j<nl.getLength(); j++ )
      response = new ResponseData( this, (Element)nl.item( j ), j );
    // then recreate enhanced images
//    for ( int j=0; j<responses.size(); j++ )
//      responses.get( j ).filterImage( page.blackness, page.lightestredmean );

    nl = element.getElementsByTagName( "outcome" );
    OutcomeDatum outcome;
    for ( int j=0; j<nl.getLength(); j++ )
    {
      outcome = new OutcomeDatum( (Element)nl.item(j) );
      outcomes.data.add( outcome );
    }
    
    page.questions.add( this );
    page.exam.fireTableDataChanged();
  }

  public ResponseData getResponseData( String ident )
  {
    return responsedatatable.get( ident );
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
          if ( responsedata.examiner_selected )
            responselid.addCurrentValue( responsedata.ident );
            //qtiitem.addResponseValueByOffset( i );
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
    outcomes.data.clear();
    for ( int i=0; i<outcome_names.length; i++ )
    {
      outcomedata = new OutcomeDatum();
      outcomedata.name = outcome_names[i];
      outcomedata.value = qtiitem.getOutcomeValue( outcome_names[i] );
      //System.out.println( "Outcome " + outcomedata.name );
      //System.out.println( "Value " + outcomedata.value );
      outcomes.data.add( outcomedata );
    }
    outcomes.fireTableDataChanged();
  }

  public int getOutcomeCount()
  {
    if ( outcomes == null) return 0;
    return outcomes.data.size();
  }

  public String getOutcomeIdentifier( int offset )
  {
    QTIElementItem qtiitem = page.exam.getAssessmentItem( ident );
    String[] outcome_names = qtiitem.getOutcomeNames();
    return outcome_names[offset];
  }

  public String getOutcomeValueString( int n )
  {
    OutcomeDatum outcomedata = outcomes.data.get( n );
    if ( outcomedata == null || outcomedata.value == null )
      return "null";
    return outcomedata.value.toString();
  }



  public void emit( Writer writer )
          throws IOException
  {
    writer.write( "      <question ident=\"" + ident + "\">\n" );
    for ( int i=0; i<responsedatas.size(); i++ )
      responsedatas.get( i ).emit( writer );

    for ( int i=0; i<outcomes.data.size(); i++ )
      outcomes.data.get( i ).emit( writer );
    
    writer.write( "      </question>\n" );
  }




  public QuestionData nextQuestionData()
  {
    return page.nextQuestionData( this );
  }

  public QuestionData previousQuestionData()
  {
    return page.previousQuestionData( this );
  }




  public int getRowCount()
  {
    return responsedatas.size();
  }

    @Override
  public int getColumnCount()
  {
    return 6;
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
        return Boolean.class;
    }
    return null;
  }

  @Override
  public boolean isCellEditable( int rowIndex, int columnIndex )
  {
    return columnIndex == 5;
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
        return "?";
      case 2:
        if ( response.getImage() == null )
            return "?";
        return new ImageIcon( response.getImage() );
      case 3:
        if ( response.getFilteredImage() == null )
            return "?";
        return new ImageIcon( response.getFilteredImage() );
      case 4:
        //if ( qtiitem == null ) return "?";
        //if ( !qtiitem.isSupported() ) return "n/a";
        return new Boolean( response.selected );
      case 5:
        //if ( qtiitem == null ) return "?";
        //if ( !qtiitem.isSupported() ) return "n/a";
        return new Boolean( response.examiner_selected );
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
  }
}
