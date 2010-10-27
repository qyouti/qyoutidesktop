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
import org.qyouti.util.QyoutiUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author jon
 */
public class ResponseData
{
  public int position=-1;
  public String ident="";
  public BufferedImage box_image;
  public BufferedImage filtered_image;
  public double dark_pixels;
  public boolean selected;
  public boolean examiner_selected;


  public ResponseData( QuestionData question )
  {
    question.responses.add( this );
  }

  public ResponseData( QuestionData question, Element element )
  {
    selected          = "true".equalsIgnoreCase( element.getAttribute( "selected" ) );
    examiner_selected = "true".equalsIgnoreCase( element.getAttribute( "examiner" ) );
    ident = element.getAttribute( "ident" );
    if ( "null".equalsIgnoreCase(ident)) ident = null;
    NodeList nl = element.getElementsByTagName( "box" );
    NodeList lines;
    BufferedImage image;
    Element box, filtered, line;
    String text;
    int x, y;
    if ( nl.getLength() > 0 )
    {
      box = (Element) nl.item( 0 );
      lines = box.getElementsByTagName( "line" );
      line = (Element) lines.item( 0 );
      text = line.getTextContent();
      image = new BufferedImage( lines.getLength(), text.length() / 10, BufferedImage.TYPE_INT_RGB );
      for ( x=0; x<lines.getLength(); x++ )
      {
        line = (Element) lines.item( x );
        text = line.getTextContent();
        for ( y=0; y<text.length(); y+=10 )
          image.setRGB( x, y/10, QyoutiUtils.parseInt( text, y+3, 6, 16 ) | 0xff000000 );
      }
      box_image = image;
    }
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
    question.responses.add( this );
  }

  public void emit( Writer writer )
          throws IOException
  {
    int rgb;
    writer.write( "          <response ident=\"" + ident + "\" " );
    writer.write( "selected=\"" + (selected?"true":"false") + "\" " );
    writer.write( "examiner=\"" + (examiner_selected?"true":"false") + "\" " );
    writer.write( ">\n" );

    writer.write( "            <box>\n" );
    for ( int i=0; i<box_image.getHeight(); i++ )
    {
      writer.write( "                <line>" );
      for ( int j=0; j<box_image.getHeight(); j++ )
      {
        rgb = box_image.getRGB( i, j );
        writer.write( "(" + Integer.toHexString( rgb ) + ")" );
      }
      writer.write( "</line>\n" );
    }
    writer.write( "            </box>\n" );
    if ( filtered_image != null )
    {
        writer.write( "            <filtered>\n" );
        for ( int i=0; i<filtered_image.getHeight(); i++ )
        {
          writer.write( "                <line>" );
          for ( int j=0; j<filtered_image.getHeight(); j++ )
          {
            rgb = filtered_image.getRGB( i, j );
            if ( (rgb & 1) == 0 )
              writer.write( "#" );
            else
              writer.write( "." );
          }
          writer.write( "</line>\n" );
        }
        writer.write( "            </filtered>\n" );
    }

    writer.write( "          </response>\n" );
  }

}
