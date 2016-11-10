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
  public QuestionData question;

  public int position=-1;
  public String type;
  public String ident;
  public int index;
  private int imagewidth;
  private int imageheight;
  private BufferedImage box_image;
  private BufferedImage filtered_image;
  public double dark_pixels=-1;
  public boolean needsreview=false;
  public boolean selected=false;
  public boolean examiner_selected=false;

  public String debug_message=null;

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
    String str = element.getAttribute( "needsreview" );
    needsreview = str != null && str.toLowerCase().startsWith( "y" );
    selected          = "true".equalsIgnoreCase( element.getAttribute( "selected"  ) );
    examiner_selected = "true".equalsIgnoreCase( element.getAttribute( "examiner"  ) );
    ident = element.getAttribute( "ident" );
    type = element.getAttribute( "type" );
    try { imagewidth  = Integer.parseInt( element.getAttribute( "imagewidth"  ) ); }
    catch ( NumberFormatException nfe ) { imagewidth=0; }
    try { imageheight = Integer.parseInt( element.getAttribute( "imageheight" ) ); }
    catch ( NumberFormatException nfe ) { imageheight=0; }
    if ( type == null || type.length() == 0 )
      type = "response_label";
    if ( "null".equalsIgnoreCase(ident)) ident = null;
    if ( ident == null || ident.length() == 0 )
      setIdentFromIndex();

    String content = element.getTextContent();
    if ( content !=null && content.length() > 0 )
      debug_message = content;
    
    question.responsedatas.add( this );
    question.responsedatatable.put( ident, this );
  }

  public int getImageWidth()
  {
    return imagewidth;
  }

  public void setImageWidth( int imagewidth )
  {
    this.imagewidth = imagewidth;
  }

  public int getImageHeight()
  {
    return imageheight;
  }

  public void setImageHeight( int imageheight )
  {
    this.imageheight = imageheight;
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
    writer.write( "needsreview=\"" + (needsreview?"yes":"no") + "\" " );
    writer.write( "selected=\"" + (selected?"true":"false") + "\" " );
    writer.write( "examiner=\"" + (examiner_selected?"true":"false") + "\" " );
    writer.write( "imagefile=\"" + getImageFileName() + "\" ");
    writer.write( "imagewidth=\"" + imagewidth + "\" ");
    writer.write( "imageheight=\"" + imageheight + "\" ");
    if ( debug_message != null )
    {
      writer.write( ">" );
      writer.write( debug_message );
      writer.write( "</response>\n" );
    }
    else
      writer.write( "/>\n" );

  }

}
