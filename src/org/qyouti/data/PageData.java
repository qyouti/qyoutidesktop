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

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.LookupOp;
import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Vector;
import org.qyouti.scan.image.IdentityLookupTable;
import org.qyouti.scan.image.ResponseBoxColourLookupTable;
import org.qyouti.scan.image.ResponseImageProcessor;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author jon
 */
public class PageData
{
  public ExaminationData exam;

  public String source;
  public Integer scanorder;
  public String code;
  public String error=null;
  public boolean landscape=false;
  public int quadrant=0;
  public Rectangle scanbounds;
  public double declared_calibration_width;
  public double declared_calibration_height;

  public boolean processed=false;



  public ResponseImageProcessor responseimageprocessor = null;

  public String printid;
  public File examfolder;
  public File paginationfile;
  public CandidateData candidate;
  public double height;
  public String pageid;
  public int page_number;
  public String candidate_name;
  public String candidate_number;
  public Vector<QuestionData> questions = new Vector<QuestionData>();

  public static final DecimalFormat pagenumberformat = new DecimalFormat("000");


  public PageData( ExaminationData exam,
                    String source,
                    int scanorder )
  {
    this.exam = exam;
    this.source = source;
    this.scanorder = new Integer( scanorder );
  }


  public PageData( ExaminationData exam,
                    Element element )
  {
    this.candidate_name = element.getAttribute( "name" );
    this.candidate_number = element.getAttribute( "id" );
    this.source = element.getAttribute( "source" );
    this.code = element.getAttribute( "code" );
    this.processed = "true".equalsIgnoreCase( element.getAttribute( "processed" ) );
    try
    {
      this.page_number = Integer.parseInt( element.getAttribute( "page" ) );
    }
    catch ( NumberFormatException numberFormatException )
    {
      this.page_number = 0;
    }
    NodeList nl = element.getElementsByTagName( "error" );
    if ( nl.getLength() == 1 )
      this.error = nl.item( 0 ).getTextContent();
    
    this.exam = exam;
//    blackness = Double.parseDouble( element.getAttribute( "black" ) );
//    lightestredmean = Double.parseDouble( element.getAttribute( "pink" ) );
    nl = element.getElementsByTagName( "question" );
    QuestionData question;
    for ( int j=0; j<nl.getLength(); j++ )
      question = new QuestionData( this, (Element)nl.item( j ) );

  }

  public void postLoad()
  {
    if ( processed )
      this.candidate = exam.addPage( this );
  }

  public String getPreferredFolderName()
  {
    if ( error != null )
      return "bad_scans";
    if ( printid == null || printid.length() == 0 )
      return "bad_scans";
    if ( examfolder == null )
      return "unidentified_scans_" + printid;
    return "scans_for_" + examfolder.getName();
  }

  public String getPreferredFileExtension()
  {
    int dot = source.lastIndexOf( '.' );
    if ( dot < 0 )
      return ".jpg";
    return source.substring( dot );
  }

  public String getPreferredFileName()
  {
    if ( source == null || candidate_name==null || candidate_number== null || page_number < 0 )
      return null;
    StringBuffer b = new StringBuffer();
    b.append( "imported_" );
    b.append( candidate_name.replace( " ", "_" ).replace( "/", "_" ).replace( "'", "_" ) );
    b.append( "_" );
    b.append( candidate_number );
    b.append(  "_page_" );
    b.append( pagenumberformat.format( page_number+1L ) );
    b.append( getPreferredFileExtension() );
    return b.toString();
  }


  public void prepareImageProcessor( boolean monochrome, double blackness, double threshold )
  {
    QuestionData question;
    ResponseData response;

    responseimageprocessor = new ResponseImageProcessor( monochrome, blackness, threshold );
    // Look at the centres of all pink boxes and measure
    // brightness of the red channel
    // Find the lightest of all boxes to calibrate the
    // page.
    for ( int i=0; i<questions.size(); i++ )
    {
      question = questions.get( i );
      for ( int j=0; j<question.responsedatas.size(); j++ )
      {
        response = question.responsedatas.get( j );
        responseimageprocessor.calibrateResponsePink( response.getImage() );
      }
    }
    responseimageprocessor.makeReady();
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

    if ( candidate == null )
    {
      System.out.println( "No ref to candidate " + this.candidate_name );
      return null;
    }
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
    writer.write( "    <page " );
    if ( candidate_name != null )
      writer.write( "name=\""   + candidate_name   + "\" " );
    if ( candidate_number != null )
      writer.write( "id=\""     + candidate_number + "\" " );
    if ( page_number > 0 )
      writer.write( "page=\""   + page_number      + "\" " );
    writer.write( "source=\"" + source           + "\" " );
    if ( code != null )
      writer.write( "code=\""   + code             + "\" " );
//    writer.write( "black=\""  + blackness        + "\" " );
//    writer.write( "pink=\""   + lightestredmean  + "\" " );
    writer.write( "processed=\"" );
    writer.write( processed?"true":"false" );
    writer.write( "\" " );

    writer.write( ">\n" );

    if ( error != null )
    {
      writer.write( "      <error>" );
      writer.write( error );
      writer.write( "</error>\n" );
    }

    for ( int i=0; i<questions.size(); i++ )
      questions.get( i ).emit( writer );

    writer.write( "    </page>\n" );
  }
}
