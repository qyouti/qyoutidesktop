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

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.qyouti.qti1.QTIResponse;
import org.qyouti.qti1.element.QTIElementItem;
import org.qyouti.qti1.element.QTIElementResponselabel;
import org.qyouti.qti1.gui.QuestionMetricBox;
import org.qyouti.util.QyoutiUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author jon
 */
public class ResponseData
{
  QuestionData question;

  public int position=-1;
  public String type;
  public String ident;
  public int index;
  private BufferedImage box_image;
  private BufferedImage filtered_image;
  public double dark_pixels=-1;
  public boolean selected=false;
  public boolean examiner_selected=false;


  public ResponseData( QuestionData question, int position, QuestionMetricBox box )
  {
    this.question = question;
    this.position = position;
    this.type = box.getType();
    this.ident = box.getIdent();
    this.index = box.getIndex();
    if ( ident == null || ident.length() == 0 )
      setIdentFromIndex();

    question.responsedatas.add( this );
    question.responsedatatable.put( ident, this );
  }

  public ResponseData( QuestionData question, Element element, int item )
  {
    this.question = question;
    position = item;
    selected          = "true".equalsIgnoreCase( element.getAttribute( "selected" ) );
    examiner_selected = "true".equalsIgnoreCase( element.getAttribute( "examiner" ) );
    ident = element.getAttribute( "ident" );
    type = element.getAttribute( "type" );
    if ( type == null || type.length() == 0 )
      type = "response_label";
    if ( "null".equalsIgnoreCase(ident)) ident = null;
    if ( ident == null || ident.length() == 0 )
      setIdentFromIndex();
    NodeList nl; // = element.getElementsByTagName( "box" );
    NodeList lines;
    BufferedImage image;
    Element box, filtered, line;
    String text;
    int x, y;


    nl = element.getElementsByTagName( "filtered" );
    if ( nl.getLength() > 0 )
    {
      filtered = (Element) nl.item( 0 );
      lines = filtered.getElementsByTagName( "line" );
      line = (Element) lines.item( 0 );
      text = line.getTextContent();
      image = new BufferedImage( lines.getLength(), text.length(), BufferedImage.TYPE_INT_RGB );
      for ( x=0; x<lines.getLength(); x++ )
      {
        line = (Element) lines.item( x );
        text = line.getTextContent();
        for ( y=0; y<text.length(); y++ )
          //image.setRGB( x, y, "#".equals( text.substring( y, y+1 ) )?0xff000000:0xffffffff );
          image.setRGB( x, y, (text.charAt(y) == '#')?0xff000000:0xffffffff );
      }
      filtered_image = image;
    }
    question.responsedatas.add( this );
    question.responsedatatable.put( ident, this );
  }


  private void setIdentFromIndex()
  {
    QTIElementItem qtiitem = question.page.exam.getAssessmentItem( question.ident );
    QTIElementResponselabel label = qtiitem.getResponselabelByOffset( index );
    if ( label != null )
      ident = label.getIdent();
    else
      this.ident = Integer.toString( position );
  }

  private File getFile( String fname )
  {
    File examfolder = question.page.exam.examfile.getParentFile();
    File scanfolder = new File( examfolder, "scans" );
    return new File( scanfolder, fname );
  }

  private BufferedImage loadImage( String fname )
  {
    BufferedImage img=null;
    try
    {
      File imgfile = getFile( fname );
      if ( !imgfile.exists() )
        return null;
      img = ImageIO.read(imgfile);
    } catch (IOException ex)
    {
      Logger.getLogger(ResponseData.class.getName()).log(Level.SEVERE, null, ex);
    }
    return img;
  }

  public BufferedImage getImage()
  {
    if ( box_image != null ) return box_image;
    return box_image=loadImage( getImageFileName() );
  }

  public BufferedImage getFilteredImage()
  {
    if ( filtered_image != null ) return filtered_image;
    return filtered_image=loadImage( getFilteredImageFileName() );
  }


  public File getImageFile()
  {
    return getFile( getImageFileName() );
  }

  public File getFilteredImageFile()
  {
    return getFile( getFilteredImageFileName() );
  }

  
  public String getImageFileName()
  {
    return question.ident + "_" + position + "_" +
                question.page.candidate_number +
                ".jpg";
  }

  public String getFilteredImageFileName()
  {
    return question.ident + "_" + position + "_" +
                question.page.candidate_number +
                "_filt.jpg";
  }



  public void emit( Writer writer )
          throws IOException
  {
    int rgb;
    writer.write( "          <response ident=\"" + ident + "\" " );
    writer.write( "type=\"" + type + "\" " );
    writer.write( "selected=\"" + (selected?"true":"false") + "\" " );
    writer.write( "examiner=\"" + (examiner_selected?"true":"false") + "\" " );
    writer.write( "imagefile=\"" + getImageFileName() + "\" ");
    writer.write( "/>\n" );

//    writer.write( "            <box>\n" );
//    for ( int i=0; i<box_image.getWidth(); i++ )
//    {
//      writer.write( "                <line>" );
//      for ( int j=0; j<box_image.getHeight(); j++ )
//      {
//        rgb = box_image.getRGB( i, j );
//        writer.write( "(" + Integer.toHexString( rgb ) + ")" );
//      }
//      writer.write( "</line>\n" );
//    }
//    writer.write( "            </box>\n" );

//    if ( filtered_image != null )
//    {
//        writer.write( "            <filtered>\n" );
//        for ( int i=0; i<filtered_image.getWidth(); i++ )
//        {
//          writer.write( "                <line>" );
//          for ( int j=0; j<filtered_image.getHeight(); j++ )
//          {
//            rgb = filtered_image.getRGB( i, j );
//            if ( (rgb & 1) == 0 )
//              writer.write( "#" );
//            else
//              writer.write( "." );
//          }
//          writer.write( "</line>\n" );
//        }
//        writer.write( "            </filtered>\n" );
//    }
//
//    writer.write( "          </response>\n" );
  }

}
