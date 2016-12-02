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
import javax.swing.table.AbstractTableModel;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.qyouti.qti1.element.QTIElementItem;
import org.qyouti.qti1.QTIBuilder;
import org.qyouti.qti1.QTIElement;
import org.qyouti.qti1.element.QTIElementQuestestinterop;
import org.qyouti.qti1.element.QTIElementResponselabel;
import org.qyouti.statistics.HodgesLehmann;
import org.w3c.dom.*;

/**
 *
 * @author jon
 */
public class QuestionDefinitions
        extends AbstractTableModel
{
  private final Element questestinterop;
  public QTIElementQuestestinterop qti;

  public QuestionDefinitions( Element questestinterop )
  {
    this.questestinterop = questestinterop;
    if ( questestinterop == null ) return;


    QTIBuilder qtibuilder = new QTIBuilder();
    QTIElement qtielement = qtibuilder.build( questestinterop );
    //System.out.println( "========================================================================" );
    //System.out.println( qtielement.toString() );
    //System.out.println( "========================================================================" );
    if ( qtielement instanceof QTIElementQuestestinterop )
    {
      qti = (QTIElementQuestestinterop)qtielement;
//      Vector<QTIElementItem> items = qti.getItems();
      //System.out.println( "Item count: " + items.size() );
//      for ( int i=0; i<items.size(); i++ )
//      {
//        System.out.println( "item" + items.get(i).getIdent() );
//      }
    }
//    System.out.println( "========================================================================" );
  }


  public void itemAnalysis( Vector<CandidateData> candidates, ArrayList<QuestionAnalysis> analyses )
  {
    int i, j, k;
    Vector<QTIElementItem> items = qti.getItems();
    QTIElementItem item;
    QTIElementResponselabel label;
    CandidateData candidate;
    ResponseData response;
    Vector<Double> candidates_right = new Vector<Double>();
    Vector<Double> candidates_wrong = new Vector<Double>();
    HodgesLehmann hodges_lehmann;
    QuestionAnalysis qanal;
    ResponseAnalysis ranal;

    //Vector<CandidateData> candidates_right = new Vector<CandidateData>();
    //Vector<CandidateData> candidates_wrong = new Vector<CandidateData>();

    System.out.println( "Item count: " + items.size() );
    for ( i=0; i<items.size(); i++ )
    {
      item = items.get(i);
      System.out.println( "analysis of item " + item.getIdent() );
      qanal = new QuestionAnalysis();
      qanal.offset = i+1;
      qanal.ident = item.getIdent();
      qanal.title = item.getTitle();
      analyses.add( qanal );
      for ( j=0; 1 == 1; j++ )
      {
        label = item.getResponselabelByOffset(j);
        if ( label == null ) break;
        if ( !label.isCorrect() && !label.isIncorrect() )
          continue;

        ranal = new ResponseAnalysis();
        qanal.response_analyses.add( ranal );
        System.out.println( "option: " + label.getIdent() );
        ranal.ident = label.getIdent();
        ranal.offset = j+1;
        ranal.correct = label.isCorrect();
        candidates_right.clear();
        candidates_wrong.clear();
        for ( k=0; k<candidates.size(); k++ )
        {
          candidate = candidates.get( k );
          //System.out.println( "Candidate: " + candidate.id + "(" + candidate.score + ") " );
          if ( candidate.score == null )
            continue;
          response = candidate.getResponse( item.getIdent(), j );
          if ( response != null )
          {
            //System.out.println( (response.examiner_selected?"selected":"blank") );
            if ( 
                     ( response.examiner_selected && label.isCorrect() )
                  || ( !response.examiner_selected && label.isIncorrect() )
               )
            {
              candidates_right.add( candidate.score );
            }
            else
            {
              candidates_wrong.add( candidate.score );
            }
          }
        }
        System.out.println( "right: " + candidates_right.size() + "   wrong: " + candidates_wrong.size() );
        ranal.right = candidates_right.size();
        ranal.wrong = candidates_wrong.size();

        if ( candidates_right.size() > 1 && candidates_wrong.size() > 1 )
        {
          hodges_lehmann = new HodgesLehmann(
                  candidates_right.toArray( new Double[0] ),
                  candidates_wrong.toArray( new Double[0] ) );
          ranal.median_difference = hodges_lehmann.getDelta();
          ranal.median_difference_lower = hodges_lehmann.getLower90Delta();
          ranal.median_difference_upper = hodges_lehmann.getUpper90Delta();
          System.out.println( "Median difference " + hodges_lehmann.getDelta() );
          System.out.println( "lower 95% limit  " + hodges_lehmann.getLower95Delta() );
          System.out.println( "upper 95% limit  " + hodges_lehmann.getUpper95Delta() );
        }
      }
    }
  }


  public int getRowCount()
  {
    return qti.getItems().size();
  }

  public int getColumnCount()
  {
    return 3;
  }

  public Object getValueAt(int rowIndex, int columnIndex)
  {
    if ( rowIndex<0 || rowIndex >= getRowCount() || columnIndex<0 || columnIndex > 2 )
      return null;

    QTIElementItem item = qti.getItems().get( rowIndex );
    if ( columnIndex == 0 ) return item.getIdent();
    if ( columnIndex == 2 ) return item.isSupported()?"yes":"no";
    return item.getTitle();
  }


  @Override
  public String getColumnName(int columnIndex)
  {
    switch ( columnIndex )
    {
      case 0:
        return "ID";
      case 1:
        return "Title";
      case 2:
        return "Supported";
    }
    return null;
  }


  public void emit( Writer writer )
          throws IOException, TransformerConfigurationException, TransformerException
  {
    TransformerFactory transfac = TransformerFactory.newInstance();
    Transformer trans = transfac.newTransformer();
    trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    trans.setOutputProperty(OutputKeys.INDENT, "yes");
    StreamResult result = new StreamResult( writer );
    DOMSource source = new DOMSource( questestinterop );
    trans.transform(source, result);
  }


}
