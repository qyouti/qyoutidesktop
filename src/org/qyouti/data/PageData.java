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
import java.util.Vector;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author jon
 */
public class PageData
{
  public ExaminationData exam;
  public CandidateData candidate;
  public String source;
  public double height;
  public int page_number;
  public String candidate_name;
  public String candidate_number;
  public Vector<QuestionData> questions = new Vector<QuestionData>();

  public PageData( ExaminationData exam,
                    String candidate_name,
                    String candidate_number,
                    int page_number )
  {
    this.candidate_name = candidate_name;
    this.candidate_number = candidate_number;
    this.page_number = page_number;
    this.exam = exam;
    this.candidate = exam.addPage( this );
  }


  public PageData( ExaminationData exam,
                    String candidate_name,
                    String candidate_number,
                    Element element )
  {
    this.candidate_name = candidate_name;
    this.candidate_number = candidate_number;
    this.exam = exam;
    NodeList nl = element.getElementsByTagName( "question" );
    QuestionData question;
    for ( int j=0; j<nl.getLength(); j++ )
      question = new QuestionData( this, (Element)nl.item( j ) );
    this.candidate = exam.addPage( this );
  }


  public QuestionData nextQuestionData( QuestionData q )
  {
    int i = questions.indexOf( q );
    if ( i < 0 )
      return null;
    if ( (i+1) < questions.size() )
      return questions.get( i+1 );

    PageData otherpage = candidate.nextPageData( this, true );
    if ( otherpage == null )
      return null;
    if ( otherpage.questions.size() == 0 )
      return null;
    
    return otherpage.questions.get( 0 );
  }

  public QuestionData previousQuestionData( QuestionData q )
  {
    int i = questions.indexOf( q );
    if ( i < 0 )
      return null;
    if ( (i-1) >= 0 )
      return questions.get( i-1 );

    PageData otherpage = candidate.previousPageData( this, true );
    if ( otherpage == null )
      return null;
    if ( otherpage.questions.size() == 0 )
      return null;

    return otherpage.questions.lastElement();
  }
  


  public void emit( Writer writer )
          throws IOException
  {
    writer.write( "    <page name=\"" + candidate_name +
                  "\" id=\"" + candidate_number +
                  "\" page=\"" + page_number + "\">\n" );

    for ( int i=0; i<questions.size(); i++ )
      questions.get( i ).emit( writer );

    writer.write( "    </page>\n" );
  }
}
