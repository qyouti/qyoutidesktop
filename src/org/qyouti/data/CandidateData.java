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
  public ArrayList<PrintedPageData> pages = new ArrayList<PrintedPageData>();
  public String name;
  public String id;
  public boolean anonymous;

  public UserRenderPreferences preferences = null;

  public boolean fixeditems = false;
  public Vector<String> itemidents = null;
  

  public CandidateData( ExaminationData exam, String name, String id, boolean anonymous )
  {
    this.exam = exam;
    this.name = name;
    this.id = id;
    this.anonymous = anonymous;
    this.preferences = null;
  }


  public CandidateData( ExaminationData exam, Element element )
  {
    this.exam = exam;
    name = element.getAttribute( "name" );
    id   = element.getAttribute( "id" );
    String str = element.getAttribute( "anonymous" );
    anonymous = str != null && str.toLowerCase().startsWith( "y" );
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
    PrintedPageData page;
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

    nl = element.getElementsByTagName( "preferences" );
    if ( nl.getLength() > 0 )
      preferences = new UserRenderPreferences( (Element)nl.item( 0 ) );
    else
      preferences = null;
  }

  public Double getScore()
  {
    OutcomeCandidateData ocd = exam.getOutcomeCandidateData(id, false);
    if ( ocd == null ) return null;
    OutcomeDatum od = ocd.getDatum("SCORE");
    if ( od == null ) return null;
    if ( od.value == null ) return null;
    if ( od.value instanceof Integer )
      return ((Integer)od.value).doubleValue();
    if ( !(od.value instanceof Double) ) return null;
    return (Double)od.value;
  }
  
  public int getStatus()
  {
    if ( exam.getPageCount() == 0 )
      return STATUS_WAITING;
    if ( pages == null || pages.size() == 0 )
      return STATUS_UNSCANNED;
    
    int q=0, n=0;
    ScannedQuestionData qd;
    for ( int i=0; itemidents!=null && i<itemidents.size(); i++ )
    {
      qd = getQuestionData( itemidents.get( i) );
      if ( qd == null )
        continue;
      
      q++;
      if ( qd.needsreview )
      {
        n++;
        if ( qd.getExaminerDecision() == ScannedQuestionData.EXAMINER_DECISION_NONE )
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
  
  public void addPage( PrintedPageData page )
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
  
  public ScannedResponseData getResponse( String qid, int resp_offset )
  {
    int i, j;
    PrintedPageData page;
    ScannedPageData spage;
    
    for ( i=0; i<pages.size(); i++ )
    {
      page = pages.get(i);
      spage = exam.getScannedPageData(page.pageid);
      if ( spage == null ) continue;
      for ( ScannedQuestionData question : spage.getQuestions() )
      {
        if ( qid.equals( question.getIdent() ) )
        {
          if ( resp_offset>=0 && resp_offset < question.responsedatas.size() )
            return question.responsedatas.get( resp_offset );
          return null;
        }
      }
    }

    return null;
  }

  public ScannedQuestionData getQuestionData( String qid )
  {
    int i, j;
    PrintedPageData page;
    ScannedPageData spage;
    
    for ( i=0; i<pages.size(); i++ )
    {
      page = pages.get(i);
      spage = exam.getScannedPageData(page.pageid);
      if ( spage == null ) continue;
      for ( ScannedQuestionData question : spage.getQuestions() )
      {
        if ( qid.equals( question.getIdent() ) )
          return question;
      }
    }

    return null;
  }


  public int questionsScanned()
  {
    PrintedPageData page;
    ScannedPageData spage;
    int total=0;
    for ( int i=0; i<pages.size(); i++ )
    {
      page = pages.get(i);
      spage = exam.getScannedPageData(page.pageid);
      if ( spage == null ) continue;
      total += spage.getQuestions().size();
    }
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
    if ( anonymous )
      writer.write( " anonymous=\"yes\"" );
    writer.write( ">\r\n" );
    if ( itemidents != null )
    {
      writer.write( "    <items" );
      if ( fixeditems )
        writer.write( " fixed=\"yes\"" );
      writer.write( ">\r\n" );
      for ( int i=0; i<itemidents.size(); i++ )
        writer.write( "      <itemref ident=\"" + itemidents.elementAt( i ) + "\"/>\r\n" );
      writer.write( "    </items>\r\n" );
    }
    if ( preferences != null )
    {
      writer.write( "    " );
      preferences.emit(writer);
      writer.write( "\r\n" );
    }
    for ( int i=0; i<pages.size(); i++ )
    {
      writer.write( "    <page pageid=\"" );
      writer.write( pages.get( i ).pageid );
      writer.write( "\"/>\r\n" );
    }
    writer.write( "  </candidate>\r\n" );
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


  public ScannedQuestionData firstQuestionData()
  {
    List<ScannedQuestionData> list;
    PrintedPageData page;
    ScannedPageData spage;
    for ( int i=0; i< pages.size(); i++ )
    {
      page = pages.get( i );
      spage = exam.getScannedPageData(page.pageid);
      if ( spage == null ) continue;
      list = spage.getQuestions();
      if ( !list.isEmpty() )
        return list.get(0);
    }

    return null;
  }

  public ScannedQuestionData lastQuestionData()
  {
    List<ScannedQuestionData> list;
    PrintedPageData page;
    ScannedPageData spage;
    for ( int i=pages.size()-1; i>=0; i-- )
    {
      page = pages.get( i );
      spage = exam.getScannedPageData(page.pageid);
      if ( spage == null ) continue;
      list = spage.getQuestions();
      if ( !list.isEmpty() )
        return list.get(list.size()-1);
    }

    return null;
  }


  public PrintedPageData nextPageData( PrintedPageData p, boolean not_empty )
  {
    PrintedPageData other;
    ScannedPageData spage;
    int i = pages.indexOf( p );
    if ( i < 0 )
      return null;

    if ( (i+1) < pages.size() )
    {
      other = pages.get( i+1 );
      spage = exam.getScannedPageData(other.pageid);
      if ( not_empty && (spage == null || spage.getQuestions().size() == 0) )
        return nextPageData( other, not_empty );
      return other;
    }

    return null;
  }

  public PrintedPageData previousPageData( PrintedPageData p, boolean not_empty )
  {
    PrintedPageData other;
    ScannedPageData spage;
    int i = pages.indexOf( p );
    if ( i < 0 )
      return null;

    if ( (i-1) >= 0 )
    {
      other = pages.get( i-1 );
      spage = exam.getScannedPageData(other.pageid);
      if ( not_empty && (spage == null || spage.getQuestions().size() == 0) )
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

  public OutcomeData getOutcomes()
  {
    return exam.getCandidateOutcomes(id);
  }

  public void processAllResponses()
  {
    PrintedPageData page;
    ScannedPageData spage;
    OutcomeData outcomes = getOutcomes();
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
      page = pages.get(p);
      spage = exam.getScannedPageData(page.pageid);
      if ( spage == null ) continue;
      for ( ScannedQuestionData q : spage.getQuestions() )
      {
        q.processResponses();
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
    outcomes.clearNonFixedOutcomes();
    for ( int i=0; i<outcome_names.length; i++ )
    {
      outcomedata = new OutcomeDatum();
      outcomedata.name = outcome_names[i];
      outcomedata.value = exam.qdefs.qti.getOutcomeValue( outcome_names[i] );
      //System.out.println( "Outcome " + outcomedata.name );
      //System.out.println( "Value " + outcomedata.value );
      outcomes.addDatum( outcomedata );
    }
    exam.processDataChanged( outcomes );
  }

  @Override
  public String toString()
  {
    return this.id;
  }
}
