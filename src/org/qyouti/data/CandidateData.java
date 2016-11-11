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
import org.qyouti.qti1.element.QTIElementItem;
import org.qyouti.qti1.gui.UserRenderPreferences;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author jon
 */
public class CandidateData
{
  public static final int STATUS_WAITING = 0;
  public static final int STATUS_UNSCANNED = 1;
  public static final int STATUS_ATTENTION = 2;
  public static final int STATUS_RESOLVED = 3;
  public static final int STATUS_OK = 4;
  
  public ExaminationData exam;
  public ArrayList<PageData> pages = new ArrayList<PageData>();
  public String name;
  public String id;
  public boolean anonymous;

  public Double score = null;
  public OutcomeData outcomes = new OutcomeData();

  public UserRenderPreferences preferences = null;

  public boolean fixeditems = false;
  public Vector<String> itemidents = null;
  

  public CandidateData( ExaminationData exam, String name, String id, boolean anonymous )
  {
    this.exam = exam;
    this.name = name;
    this.id = id;
    this.anonymous = anonymous;
    this.score = null;
    this.preferences = null;
  }


  public CandidateData( ExaminationData exam, Element element )
  {
    this.exam = exam;
    name = element.getAttribute( "name" );
    id   = element.getAttribute( "id" );
    String str = element.getAttribute( "anonymous" );
    anonymous = str != null && str.toLowerCase().startsWith( "y" );
    try { score = Double.valueOf( element.getAttribute( "score" ) ); }
    catch ( Exception e ) { score = null; }
    //System.out.println( "Adding candidate " + id );
    exam.candidates.put( id, this );
    exam.candidates_sorted.add( this );

    NodeList nl;
    itemidents = null;
    nl = element.getElementsByTagName( "items" );
    Element items, itemref;
    String ident;
    String strfixed;
    if ( nl.getLength() == 1 )
    {
      itemidents = new Vector<String>();
      items = (Element)nl.item( 0 );
      strfixed = items.getAttribute( "fixed" );
      fixeditems = strfixed!=null && strfixed.length()>0;
      
      nl = items.getElementsByTagName( "itemref" );
      for ( int j=0; j<nl.getLength(); j++ )
      {
        itemref = (Element)nl.item( j );
        ident = itemref.getAttribute( "ident" );
        if ( ident != null && ident.length() > 0 )
          itemidents.add( ident );
      }
    }

    nl = element.getElementsByTagName( "page" );
    PageData page;
    String pageid;
    Element eseq;
    for ( int j=0; j<nl.getLength(); j++ )
    {
      eseq = (Element)nl.item( j );
      pageid = eseq.getAttribute( "pageid" );
      if ( pageid != null && pageid.length() >=0 )
      {
        page = exam.lookUpPage( pageid );
        addPage( page );
      }
    }
    nl = element.getElementsByTagName( "outcome" );
    OutcomeDatum outcome;
    for ( int j=0; j<nl.getLength(); j++ )
    {
      outcome = new OutcomeDatum( (Element)nl.item(j) );
      outcomes.addDatum( outcome );
    }
    nl = element.getElementsByTagName( "preferences" );
    if ( nl.getLength() > 0 )
      preferences = new UserRenderPreferences( (Element)nl.item( 0 ) );
    else
      preferences = null;
  }

  public int getStatus()
  {
    if ( exam.getPageCount() == 0 )
      return STATUS_WAITING;
    if ( pages == null || pages.size() == 0 )
      return STATUS_UNSCANNED;
    
    int n=0;
    QuestionData qd;
    for ( int i=0; itemidents!=null && i<itemidents.size(); i++ )
    {
      qd = getQuestionData( itemidents.get( i) );
      if ( qd == null )
        return STATUS_ATTENTION;
        
      if ( qd.needsreview )
      {
        n++;
        if ( qd.getExaminerDecision() == QuestionData.EXAMINER_DECISION_NONE )
          return STATUS_ATTENTION;
      }
    }
    
    if ( n>0 )
      return STATUS_RESOLVED;
    
    return STATUS_OK;
  }
 
  public String getStatusDescription()
  {
    int status = this.getStatus();
    switch ( status )
    {
      case CandidateData.STATUS_WAITING:
        return "";  // nothing has been scanned
      case CandidateData.STATUS_UNSCANNED:
        return "No pages scanned";  // other pages scanned - none for this candidate
      case CandidateData.STATUS_ATTENTION:
        return "ATTENTION";   // Either missing questions in scans or dubious input
      case CandidateData.STATUS_RESOLVED:
        return "Resolved Issues";   // There was dubious input but these have been marked as dealt with
      case CandidateData.STATUS_OK:
        return "O.K.";   // All questions scanned and there was no dubious input
      default:
        return "Unk. Status";
    }    
  }
  
  public void addPage( PageData page )
  {
    // make a local reference to the page
    if ( pages.contains( page ) )
      return;
    pages.add( page );
    // sort list of pages by page number
    Collections.sort( pages );
  }

  public void addQuestion( String ident )
  {
    if ( itemidents == null )
      itemidents = new Vector<String>();
    itemidents.add( ident );
  }
  
  public ResponseData getResponse( String qid, int resp_offset )
  {
    int i, j;
    PageData page;
    QuestionData question;
    for ( i=0; i<pages.size(); i++ )
    {
      page = pages.get(i);
      for ( j=0; j<page.questions.size(); j++ )
      {
        question = page.questions.get(j);
        if ( qid.equals( question.ident ) )
        {
          //System.out.println( "found q " );
          if ( resp_offset>=0 && resp_offset < question.responsedatas.size() )
            return question.responsedatas.get( resp_offset );
          return null;
        }
      }
    }

    return null;
  }

  public QuestionData getQuestionData( String qid )
  {
    int i, j;
    PageData page;
    QuestionData question;
    for ( i=0; i<pages.size(); i++ )
    {
      page = pages.get(i);
      for ( j=0; j<page.questions.size(); j++ )
      {
        question = page.questions.get(j);
        if ( qid.equals( question.ident ) )
          return question;
      }
    }

    return null;
  }


  public int questionsScanned()
  {
    int total=0;
    for ( int i=0; i<pages.size(); i++ )
      total += pages.get(i).questions.size();
    return total;
  }

  public int questionsAsked()
  {
    if ( itemidents != null )
      return itemidents.size();
    return 0;
  }

  public String getErrorMessage()
  {
    int scanned = questionsScanned();
    int asked = questionsAsked();
    if ( scanned == 0 )
      return "";
    else if( scanned < asked )
      return "Unscanned questions. ";
    else if( scanned > asked )
      return "Too many scanned questions. ";
    return "O.K.";    
  }
  
  public void emit( Writer writer )
          throws IOException
  {
    writer.write( "  <candidate name=\"" + name + "\" id=\"" + id + "\"" );
    if ( score != null )
      writer.write( " score=\"" + score + "\"" );
    if ( anonymous )
      writer.write( " anonymous=\"yes\"" );
    writer.write( ">\n" );
    if ( itemidents != null )
    {
      writer.write( "    <items" );
      if ( fixeditems )
        writer.write( " fixed=\"yes\"" );
      writer.write( ">\n" );
      for ( int i=0; i<itemidents.size(); i++ )
        writer.write( "      <itemref ident=\"" + itemidents.elementAt( i ) + "\"/>\n" );
      writer.write( "    </items>\n" );
    }
    if ( preferences != null )
      preferences.emit(writer);
    for ( int i=0; i<pages.size(); i++ )
    {
      writer.write( "    <page pageid=\"" );
      writer.write( pages.get( i ).pageid );
      writer.write( "\"/>\n" );
    }
    for ( int i=0; i<outcomes.getRowCount(); i++ )
      outcomes.getDatumAt( i ).emit( writer );
    writer.write( "  </candidate>\n" );
  }

  public CandidateData nextCandidateData( boolean not_empty )
  {
    CandidateData other;
    int i = exam.candidates_sorted.indexOf( this );
    if ( i < 0 )
      return null;

    if ( (i+1) < exam.candidates_sorted.size() )
    {
      other = exam.candidates_sorted.get( i+1 );
      if ( not_empty && other.questionsScanned() == 0 )
        return other.nextCandidateData( not_empty );
      return other;
    }

    return null;
  }

  public CandidateData previousCandidateData( boolean not_empty )
  {
    CandidateData other;
    int i = exam.candidates_sorted.indexOf( this );
    if ( i < 0 )
      return null;

    if ( (i-1) >= 0 )
    {
      other = exam.candidates_sorted.get( i-1 );
      if ( not_empty && other.questionsScanned() == 0 )
        return other.previousCandidateData( not_empty );
      return other;
    }

    return null;
  }


  public QuestionData firstQuestionData()
  {
    for ( int i=0; i< pages.size(); i++ )
    {
      if ( !pages.get( i ).questions.isEmpty() )
        return pages.get(i).questions.firstElement();
    }

    return null;
  }

  public QuestionData lastQuestionData()
  {
    for ( int i=pages.size()-1; i>=0; i-- )
    {
      if ( !pages.get( i ).questions.isEmpty() )
        return pages.get(i).questions.lastElement();
    }

    return null;
  }


  public PageData nextPageData( PageData p, boolean not_empty )
  {
    PageData other;
    int i = pages.indexOf( p );
    if ( i < 0 )
      return null;

    if ( (i+1) < pages.size() )
    {
      other = pages.get( i+1 );
      if ( not_empty && other.questions.size() == 0 )
        return nextPageData( other, not_empty );
      return other;
    }

    return null;
  }

  public PageData previousPageData( PageData p, boolean not_empty )
  {
    PageData other;
    int i = pages.indexOf( p );
    if ( i < 0 )
      return null;

    if ( (i-1) >= 0 )
    {
      other = pages.get( i-1 );
      if ( not_empty && other.questions.size() == 0 )
        return previousPageData( other, not_empty );
      return other;
    }
    
    return null;
  }


  public Vector<QTIElementItem> getItems()
  {
    int i;
    Vector<QTIElementItem> v = new Vector<QTIElementItem>();
    QTIElementItem item;
    
    if ( this.itemidents == null )
    {
//      Vector<QTIElementItem> allitems = exam.qdefs.qti.getItems();
//      for ( i=0; i<allitems.size(); i++ )
//      {
//        item = allitems.get( i );
//        if ( item.isForCandidate( anonymous ) )
//          v.add( item );
//      }
      return v;
    }

    for ( i=0; i<itemidents.size(); i++ )
    {
      item = exam.qdefs.qti.getItem( itemidents.elementAt( i ) );
      if ( item != null )
        v.add( item );
    }
    
    return v;
  }


  public void processAllResponses()
  {
    // all questions need to be processed to ensure that
    // QTI structure is fully populated with this candidate's
    // responses and item outcomes.

    // reset the qti elements so it's ready to receive
    // and process this candidate's responses
    exam.qdefs.qti.reset();

    // Process item responses  and calculate item outcomes
    // for each question that we have data for
    for ( int p=0; p<pages.size(); p++ )
    {
      for ( int q=0; q<pages.get(p).questions.size(); q++ )
      {
        pages.get(p).questions.get(q).processResponses();
      }
    }

    // If there are questions set the candidate for which we
    // have no data, include them in the overall outcome
    // processing anyway so we get default values calculated
    // as if the candidate didn't answer the questions.
    Vector<QTIElementItem> items = getItems();
    for ( int i=0; i<items.size(); i++ )
    {
      if ( !items.get( i ).isReferencedByCandidate() )
      {
        items.get( i ).reset();
        items.get( i ).setReferencedByCandidate();
      }
    }

    // Process item outcomes to produce top level outcomes
    // for the candidate
    exam.qdefs.qti.processOutcomes();

    // Copy over outcomes from the qti structures into this CandidateData
    // object.
    String[] outcome_names = exam.qdefs.qti.getOutcomeNames();
    OutcomeDatum outcomedata;
    outcomes.clear();
    for ( int i=0; i<outcome_names.length; i++ )
    {
      outcomedata = new OutcomeDatum();
      outcomedata.name = outcome_names[i];
      outcomedata.value = exam.qdefs.qti.getOutcomeValue( outcome_names[i] );
      //System.out.println( "Outcome " + outcomedata.name );
      //System.out.println( "Value " + outcomedata.value );
      outcomes.addDatum( outcomedata );
    }
    outcomes.fireTableDataChanged();


    score = null;
    OutcomeDatum score_datum = outcomes.getDatum("SCORE");
    if ( score_datum == null )
      return;
    Object score_value = score_datum.value;
    if ( score_value instanceof Double )
    {
      score = (Double)score_value;
      return;
    }
    if ( score_value instanceof Integer )
    {
      score = new Double( ((Integer)score_value).doubleValue() );
      return;
    }
  }

  @Override
  public String toString()
  {
    return this.id;
  }
}
