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
import java.lang.ref.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.quipto.compositefile.CompositeFile;
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
public class ScannedResponseData
{
  private ExaminationData exam;
  public String questionident;
  public String candidateident;

  public int position=-1;
  public String type;
  public String ident;
  public int index;
  private String imagefilename;
  private int imagewidth;
  private int imageheight;
  private SoftReference<BufferedImage> box_image;
  private SoftReference<BufferedImage> filtered_image;
  public double dark_pixels=-1;
  public boolean needsreview=false;
  public boolean candidate_selected=false;

  public String debug_message=null;

  ExaminerResponseData erd = null;
  
  public ScannedResponseData( ExaminationData exam, String candidateident, String questionident, int position, QuestionMetricBox box )
  {
    this.exam = exam;
    this.candidateident = candidateident;
    this.questionident = questionident;
    this.position = position;
    this.type = box.getType();
    this.ident = box.getIdent();
    this.index = box.getIndex();
    if ( ident == null || ident.length() == 0 )
      setIdentFromIndex();
  }
  
  
  public ScannedResponseData( ExaminationData exam, String candidateident, String questionident, Element element, int item )
  {
    this.exam = exam;
    this.candidateident = candidateident;
    this.questionident = questionident;
    position = item;
    String str = element.getAttribute( "needsreview" );
    needsreview = str != null && str.toLowerCase().startsWith( "y" );
    candidate_selected          = "true".equalsIgnoreCase( element.getAttribute( "selected"  ) );
    str = element.getAttribute( "imagefile" );
    if ( str != null && str.length() > 0 )
      imagefilename = str;
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
  }

  public boolean isSelected()
  {
    if ( exam.getExaminerDecision(candidateident,questionident) == ScannedQuestionData.EXAMINER_DECISION_OVERRIDE )
      return exam.isExaminerSelected(candidateident, questionident, ident);
    return candidate_selected;
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
    QTIElementItem qtiitem = exam.getAssessmentItem( questionident );
    QTIElementResponselabel label = qtiitem.getResponselabelByOffset( index );
    if ( label != null )
      ident = label.getIdent();
    else
      this.ident = Integer.toString( position );
  }

  private BufferedImage loadImage( String fname )
  {
    return exam.getImageFromScanArchive("responses/"+fname);
  }

  public void setImage( BufferedImage image )
  {
    exam.sendImageToScanArchive( image, "responses/"+getImageFileName(), "png" );
  }

  public BufferedImage getImage()
  {
    if ( box_image == null || box_image.get() == null )
      box_image = new SoftReference<>( loadImage( getImageFileName() ) );
    return box_image.get();
  }

  public BufferedImage getFilteredImage()
  {
    if ( filtered_image == null || filtered_image.get() == null )
      filtered_image = new SoftReference<>( loadImage( getFilteredImageFileName() ) );
    return filtered_image.get();
  }

  public String getImageFileName()
  {
    if ( imagefilename != null )
      return imagefilename;    
    imagefilename = questionident + "_" + position + "_" + candidateident + ".png";
    return imagefilename;
  }

  public String getFilteredImageFileName()
  {
    return questionident + "_" + position + "_" + candidateident + "_filt.jpg";
  }



  public void emit( Writer writer )
          throws IOException
  {
    int rgb;
    writer.write( "          <response ident=\"" + ident + "\" " );
    writer.write( "type=\"" + type + "\" " );
    writer.write( "needsreview=\"" + (needsreview?"yes":"no") + "\" " );
    writer.write("selected=\"" + (candidate_selected?"true":"false") + "\" " );
    writer.write( "imagefile=\"" + imagefilename + "\" ");
    writer.write( "imagewidth=\"" + imagewidth + "\" ");
    writer.write( "imageheight=\"" + imageheight + "\" ");
    if ( debug_message != null )
    {
      writer.write( ">" );
      writer.write( debug_message );
      writer.write( "</response>\r\n" );
    }
    else
      writer.write( "/>\r\n" );

  }

}
