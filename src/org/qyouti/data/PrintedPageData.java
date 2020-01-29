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
import java.io.*;
import java.text.DecimalFormat;
import org.qyouti.scan.image.ResponseImageProcessor;
import org.qyouti.scan.process.*;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author jon
 */
public class PrintedPageData implements Comparable<PrintedPageData>
{
  public ExaminationData exam;

  // fields that are saved to disk
  public String code;
  //private boolean scanned=false;
  //private boolean processed=false;
  public String printid;
  public String pageid;
//  public int page_number;
  
  // fields that are NOT saved to disk
  //public BufferedImage rotatedimage;
  public int quarterturns;
  public double blackness;
  public String error=null;
    
//  public boolean landscape=false;
//  public int quadrant=0;
  public Rectangle scanbounds;
  public double declared_calibration_width;
  public double declared_calibration_height;


  public ResponseImageProcessor responseimageprocessor = null;

  //public File examfolder;
  //public File paginationfile;
  public CandidateData candidate;
  public String candidate_number;
  public String candidate_name;
  public double height;
  
  public PageDecoder.TransformData pagetransform;
  public double dpi;
  
//  public String candidate_name;
//  public String candidate_number;

  public static final DecimalFormat pagenumberformat = new DecimalFormat("000");


//  public PageData( ExaminationData exam,
//                    String source )
//  {
//    this.exam = exam;
//    this.source = source;
//  }

  public PrintedPageData( ExaminationData exam,
                    String printid,
                    String pageid,
                    CandidateData candidate )
  {
    this.exam = exam;
    this.printid = printid;
    this.pageid = pageid;
    this.code = "qyouti/" + printid + "/" + pageid;
    this.candidate = candidate;
    this.candidate_name = candidate.name;
    this.candidate_number = candidate.id;
  }


  public PrintedPageData( ExaminationData exam,
                    Element element )
  {
    this.candidate_name = element.getAttribute( "name" );
    this.candidate_number = element.getAttribute( "id" );
    this.code = element.getAttribute( "code" );
    this.pageid = element.getAttribute( "pageid" );
    this.printid = element.getAttribute( "printid" );
    NodeList nl = element.getElementsByTagName( "error" );
    if ( nl.getLength() == 1 )
      this.error = nl.item( 0 ).getTextContent();
    
    this.exam = exam;
  }

  public void postLoad()
  {
    this.candidate = exam.linkPageToCandidate( this );
  }


  public String getPreferredFileExtension()
  {
    return ".png";
  }

  public String getPreferredFileName()
  {
    if ( candidate_name==null || candidate_number== null || pageid == null )
      return null;
    StringBuffer b = new StringBuffer();
    b.append( "imported_" );
    b.append( candidate_name.replace( " ", "_" ).replace( "/", "_" ).replace( "'", "_" ) );
    b.append( "_" );
    b.append( candidate_number );
    b.append(  "_page_" );
    b.append( this.pageid );
    b.append( getPreferredFileExtension() );
    return b.toString();
  }




  public void emit( Writer writer )
          throws IOException
  {
    writer.write( "    <page " );
    if ( candidate_name != null )
      writer.write( "name=\""   + candidate_name   + "\" " );
    if ( candidate_number != null )
      writer.write( "id=\""     + candidate_number + "\" " );
    writer.write( "printid=\"" + printid           + "\" " );
    writer.write( "pageid=\"" + pageid           + "\" " );
    if ( code != null )
      writer.write( "code=\""   + code             + "\" " );

    if ( error != null )
    {
      writer.write( ">\r\n      <error>" );
      writer.write( error );
      writer.write( "</error>\r\n</page>\r\n" );
    }
    else
      writer.write( "/>\r\n" );
  }


  @Override
  public int compareTo( PrintedPageData other )
  {
    String t, o;
    t = this.pageid;
    o = other.pageid;
    
    if ( t == null ) t = "0000";
    if ( o == null ) o = "0000";
    return t.compareTo( o );
  }
}
