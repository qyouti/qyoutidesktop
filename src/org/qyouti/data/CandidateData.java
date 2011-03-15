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
import org.qyouti.qti1.gui.UserRenderPreferences;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author jon
 */
public class CandidateData
{
  public ExaminationData exam;
  public Vector<PageData> pages = new Vector<PageData>();
  public String name;
  public String id;
  public Double score = null;

  public UserRenderPreferences preferences = null;

  public CandidateData( ExaminationData exam, String name, String id )
  {
    this.exam = exam;
    this.name = name;
    this.id = id;
    this.score = null;
    this.preferences = null;
  }


  public CandidateData( ExaminationData exam, Element element )
  {
    this.exam = exam;
    name = element.getAttribute( "name" );
    id   = element.getAttribute( "id" );
    try { score = Double.valueOf( element.getAttribute( "score" ) ); }
    catch ( Exception e ) { score = null; }
    //System.out.println( "Adding candidate " + id );
    exam.candidates.put( id, this );
    exam.candidates_sorted.add( this );

    NodeList nl = element.getChildNodes();
    nl = element.getElementsByTagName( "page" );
    PageData page;
    for ( int j=0; j<nl.getLength(); j++ )
    {
      page = new PageData( exam, name, id, (Element)nl.item( j ) );
    }
    nl = element.getElementsByTagName( "preferences" );
    if ( nl.getLength() > 0 )
      preferences = new UserRenderPreferences( (Element)nl.item( 0 ) );
    else
      preferences = null;
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
          if ( resp_offset>=0 && resp_offset < question.responses.size() )
            return question.responses.get( resp_offset );
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

  public void emit( Writer writer )
          throws IOException
  {
    writer.write( "  <candidate name=\"" + name + "\" id=\"" + id + "\"" );
    if ( score != null )
      writer.write( " score=\"" + score + "\"" );
    writer.write( ">\n" );
    if ( preferences != null )
      preferences.emit(writer);
    for ( int i=0; i<pages.size(); i++ )
      pages.get( i ).emit( writer );

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


  public QuestionData firstQuestion()
  {
    for ( int i=0; i< pages.size(); i++ )
    {
      if ( !pages.get( i ).questions.isEmpty() )
        return pages.get(i).questions.firstElement();
    }

    return null;
  }

  public QuestionData lastQuestion()
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

  public void processAllResponses()
  {
    // all questions need to be processed to ensure that
    // QTI structure is fully populated with this candidate's
    // responses and item outcomes.
    exam.qdefs.qti.reset();
    for ( int p=0; p<pages.size(); p++ )
    {
      for ( int q=0; q<pages.get(p).questions.size(); q++ )
      {
        pages.get(p).questions.get(q).processResponses();
      }
    }
    // add up scores etc.
    exam.qdefs.qti.getOutcomesprocessing().process();
    score = null;
    Object value = exam.qdefs.qti.getOutcomesprocessing().getOutcomeValue("SCORE");
    if ( value == null )
      return;
    if ( value instanceof Double )
    {
      score = (Double)value;
      return;
    }
    if ( value instanceof Integer )
    {
      score = new Double( ((Integer)value).doubleValue() );
      return;
    }
  }

  @Override
  public String toString()
  {
    return this.id;
  }
}
