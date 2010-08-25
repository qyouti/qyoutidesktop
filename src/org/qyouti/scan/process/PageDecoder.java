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

package org.qyouti.scan.process;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.qyouti.scan.image.ResponseBoxColourLookupTable;
import org.qyouti.scan.image.IdentityLookupTable;
import org.qyouti.data.*;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.result.*;
import com.google.zxing.common.LocalBlockBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import java.awt.image.BufferedImage;
import java.awt.image.LookupOp;
import java.io.*;
import java.net.*;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.imageio.ImageIO;
import org.qyouti.qrcode.QRCodec;
import org.qyouti.qrcode.QRScanResult;
import org.qyouti.qti1.element.QTIElementItem;
import org.qyouti.scan.image.QRCodeColourLookupTable;

/**
 *
 * @author jon
 */
public class PageDecoder
{


  static final double metrics_top_qr_to_bottom_qr =  975.0;
  static final double metrics_top_qr_width        =   60.0;
  static final double metrics_qstn_qr_width       =   60.0;
  static final double metrics_qstn_qr_hzntl_box   =  113.0;

  static final Hashtable hints = new Hashtable();
  static
  {
    hints.put( DecodeHintType.TRY_HARDER, Boolean.TRUE );
  }



  public static PageData decodeOneArgument(ExaminationData exam, String argument )
          throws IOException, URISyntaxException
  {

    File inputFile = new File(argument);
    if (inputFile.exists()) {
        return decode(exam,inputFile.toURI());
    } else {
      return decode(exam,new URI(argument) );
    }
  }




  private static QRScanResult decodeQR( BufferedImage image, int x1, int y1, int x2, int y2 )
          throws ReaderException
  {
    QRScanResult result;
    //MonochromeBitmapSource source = new BufferedImageMonochromeBitmapSource( image, x1, y1, x2, y2 );
    BufferedImage cropped = image.getSubimage(x1, y1, x2-x1, y2-y1);
    BufferedImage img_filt = new BufferedImage( cropped.getWidth(), cropped.getHeight(),
                                                BufferedImage.TYPE_INT_RGB );

    QRCodeColourLookupTable lookup = new QRCodeColourLookupTable();
    LookupOp lop = new LookupOp( lookup, null );

    for ( int n = 0; n < 80; n = (n<0) ? (-(n-5)) : (-(n+5))    )
    {
      lookup.setThreshold( 160 + n );
      lop.filter( cropped, img_filt );
      System.out.println( "DECODING QRCode threshold = " + n );
      try
      {
        result = QRCodec.decode(img_filt);
        return result;
      } catch (Exception ex)
      {
        //System.out.println( "No QRCode - exception thrown." );
        //Logger.getLogger(PageDecoder.class.getName()).log(Level.SEVERE, null, ex);
      }
      //try {Thread.sleep(1000);} catch (InterruptedException ex){}
    }

    System.out.println( "No QRCode - exception thrown." );

    return null;
  }



  private static byte[] getQRBytes( Result result )
  {
    Vector segments = (Vector)result.getResultMetadata().get( ResultMetadataType.BYTE_SEGMENTS );
    if ( segments != null )
    {
      byte[] data;
      if ( segments.size() > 0 )
      {
        data = (byte[])segments.get( 0 );
        System.out.println( ":::::::::::::::: QR bytes" );
        for ( int j=0; j<data.length; j++ )
          System.out.print( Integer.toHexString( data[j] ) + " " + ((j%16 == 15)?"\n":"") );
        System.out.println( "\n:::::::::::::::::::::::::" );
        return data;
      }
    }
    return null;
  }




  private static String decodeCString( byte[] data, int offset )
          throws UnsupportedEncodingException
  {
    int i;
    for ( i=offset; data[i] != 0; i++ )
      ;
    byte[] subarray = new byte[i-offset];
    System.arraycopy( data, offset, subarray, 0, i-offset );

    return new String( subarray, "utf8" );
  }


  private static int bytesToInteger( byte[] data, int offset )
  {
      return  ( data[offset] & 0xff )
             |
              (( data[offset + 1] << 8 ) & 0xff00 );
  }

  private static QuestionCode decodeQuestion( byte[] data )
          throws UnsupportedEncodingException
  {
    QuestionCode qcode = new QuestionCode();
    qcode.id = decodeCString( data, 0 );
    qcode.next = data[ qcode.id.length()+1 ];
    int boxes = (data.length - qcode.id.length() - 2) / 8;
    int offset, n;
    qcode.box_xoffset = new int[boxes];
    qcode.box_yoffset = new int[boxes];
    qcode.box_width = new int[boxes];
    qcode.box_height = new int[boxes];
    for ( int i=0; i<boxes; i++ )
    {
      offset = qcode.id.length()+2+ (i*8);
      qcode.box_xoffset[i] =  bytesToInteger( data, offset );
      qcode.box_yoffset[i] =  bytesToInteger( data, offset+2 );
      qcode.box_width[i] =  bytesToInteger( data, offset+4 );
      qcode.box_height[i] =  bytesToInteger( data, offset+6 );
    }
    return qcode;
  }


  private static PageData decode(ExaminationData exam, URI uri)
          throws IOException
  {
    int i;
    PageData page;
    QuestionData question;
    ResponseData response;
    BufferedImage image;
    QRScanResult result;

    System.out.println( "\n\n\nDecoding a page." );
    
    try {
      image = ImageIO.read(uri.toURL());
    } catch (IllegalArgumentException iae) {
      throw new FileNotFoundException("Resource not found: " + uri);
    }
    if (image == null) {
      System.err.println(uri.toString() + ": Could not load image");
      return null;
    }
    try {
      double[] triangle = new double[6];

      double rough_dpi = (double)image.getHeight() / 11.69;

      System.out.println( "Rough DPI of image = " + rough_dpi );

      // Bottom left corner
      System.out.println( "Decoding page qr." );
      result = decodeQR( image,
              (int)(rough_dpi*0.5),
              image.getHeight()-(int)(rough_dpi*1.9),
              (int)(rough_dpi*2.0),
              image.getHeight()-(int)(rough_dpi*0.5)
              );
      if ( result == null )
        return null;
      System.out.println( "Processing decoded QR." );
      StringTokenizer ptok = new StringTokenizer( result.getText(), "/" );
      String candidate_name = ptok.nextToken();
      String candidate_number = ptok.nextToken();
      int page_number = 0;
      int question_count = 0;
      try  { page_number = Integer.parseInt(ptok.nextToken()); }
      catch ( NumberFormatException nfe ) {}
      try  { question_count = Integer.parseInt(ptok.nextToken()); }
      catch ( NumberFormatException nfe ) {}

      // Silly bug work around
      if ( question_count > 4 ) question_count = 4;

      System.out.println( "     name: " + candidate_name );
      System.out.println( "   number: " + candidate_number );
      System.out.println( "     page: " + page_number );
      System.out.println( "questions: " + question_count );

      page = new PageData( exam, candidate_name, candidate_number, page_number );
      page.source = uri.toString();
      System.out.println( "Done decoding page qr." );
      if ( question_count == 0 )
        return page;

      ResultPoint[] bottom_left_points = result.getResultPoints();
      triangle[4] = bottom_left_points[1].getX()+(int)(rough_dpi*0.5);
      triangle[5] = bottom_left_points[1].getY()+image.getHeight()-(int)(rough_dpi*1.6);


      // Take top left corner only
      result = decodeQR( image,
              (int)(rough_dpi*0.1 ),
              (int)(rough_dpi*0.75),
              (int)(rough_dpi*1.75 ),
              (int)(rough_dpi*2.5 )  );
      if ( result == null )
        return null;
      // Not really interested in data right now
      ResultPoint[] top_left_points = result.getResultPoints();
      triangle[0] = top_left_points[1].getX() + (int)(rough_dpi*0.1 );
      triangle[1] = top_left_points[1].getY() + (int)(rough_dpi*0.75);
      triangle[2] = top_left_points[2].getX() + (int)(rough_dpi*0.1 );
      triangle[3] = top_left_points[2].getY() + (int)(rough_dpi*0.75);

      //page.height = Math.rint( checkpagescale( triangle ) * 100.0) / 100.0;
      page.height = checkpagescale( triangle );
      // are some scan lines missing?
      if ( page.height < 16.4 )
        return null;
      
      //for ( i=0; i<6; i++ )
      //  System.out.println( "triangle[" + i + "] = " + triangle[i] );
      double[] page_scale = calibrate( triangle, metrics_top_qr_width, metrics_top_qr_to_bottom_qr );
      //for ( i=0; i<4; i++ )
      //  System.out.println( "page_scale[" + i + "] = " + page_scale[i] );

      double voffset_hnth_inch, hoffset_hnth_inch;
      double voffset_pix_x;
      double voffset_pix_y;
      double offset_x, offset_y;
      double[] question_scale;
      double[] question_triangle = new double[6];
      ResultPoint[] question_points;
      String response_offsets;
      String qvoffset;
      double qvoffset_hnth_inch, qhoffset_hnth_inch;
      double qvoffset_pix_x;
      double qvoffset_pix_y;
      double qoffset_x, qoffset_y;
      int x, y, w, h;
      byte[] question_qr_data;
      QuestionCode question_code=null;

      voffset_hnth_inch = 0.0;
      for ( int q=0; q<question_count; q++ )
      {
        question = new QuestionData( page );
        
        System.out.println( "==================================" );
        hoffset_hnth_inch = 0.0;
        offset_x = hoffset_hnth_inch*page_scale[0] + voffset_hnth_inch * page_scale[2];
        offset_y = hoffset_hnth_inch*page_scale[1] + voffset_hnth_inch * page_scale[3];
        System.out.println( "Next question at " + hoffset_hnth_inch + ":" + voffset_hnth_inch + " hnth inch" );
        System.out.println( "Next question at " + offset_x + ":" + offset_y + " scan pixels" );
       
        System.out.println( "Look for question code here: " +
                (int)(triangle[0]+offset_x-(int)(rough_dpi*0.4)) + ":" +
                (int)(triangle[1]+offset_y-(int)(rough_dpi*0.4)) + ":" +
                (int)(triangle[0]+offset_x+(int)(rough_dpi)) + ":" +
                (int)(triangle[1]+offset_y+(int)(rough_dpi)) + ":"       );
                
        result = decodeQR( image,
                (int)(triangle[0]+offset_x-(int)(rough_dpi*0.4)),
                (int)(triangle[1]+offset_y-(int)(rough_dpi*0.4)),
                (int)(triangle[0]+offset_x+(int)(rough_dpi)),
                (int)(triangle[1]+offset_y+(int)(rough_dpi))       );
        if ( result == null )
          return null;
        
        question_points = result.getResultPoints();

        question_triangle[0] = question_points[1].getX();
        question_triangle[1] = question_points[1].getY();
        question_triangle[2] = question_points[2].getX();
        question_triangle[3] = question_points[2].getY();
        question_triangle[4] = question_points[0].getX();
        question_triangle[5] = question_points[0].getY();
        //for ( i=0; i<6; i++ )
        //  System.out.println( "question_triangle[" + i + "] = " + question_triangle[i] );
        question_scale = calibrate( question_triangle, metrics_qstn_qr_width, metrics_qstn_qr_width );
        //for ( i=0; i<4; i++ )
        //  System.out.println( "question_scale[" + i + "] = " + question_scale[i] );


        question_code = decodeQuestion( result.getBytes() );
        System.out.println( "Question ID " + question_code.id   );
        System.out.println( "       Next " + question_code.next );
        voffset_hnth_inch += question_code.next * 10;
        question.ident = question_code.id;

        for ( int r=0; r<question_code.box_yoffset.length; r++ )
        {
          System.out.println( "-----------------------------------" );
          System.out.println( "Response " + r + " at " + question_code.box_xoffset[r] + ", " + question_code.box_yoffset[r] );
          System.out.println( "                 W, H " + question_code.box_width[r]   + ", " + question_code.box_height[r] );

          response = new ResponseData( question );
          response.position = r;
          response.ident = null;
          qhoffset_hnth_inch = question_code.box_xoffset[r];
          qvoffset_hnth_inch = question_code.box_yoffset[r];
          qoffset_x = qhoffset_hnth_inch*question_scale[0] + qvoffset_hnth_inch * question_scale[2];
          qoffset_y = qhoffset_hnth_inch*question_scale[1] + qvoffset_hnth_inch * question_scale[3];
          //System.out.println( "Next box at " + qhoffset_hnth_inch + " : " + qvoffset_hnth_inch + " hnth inch" );
          //System.out.println( "Next box at " + qoffset_x + " : " + qoffset_y + " scan pixels" );

          x = (int)(triangle[0]+offset_x-(int)(rough_dpi*0.4)  + question_triangle[0]+qoffset_x);
          y = (int)(triangle[1]+offset_y-(int)(rough_dpi*0.4)  + question_triangle[1]+qoffset_y);
          w = (int)((rough_dpi*(double)question_code.box_width[r])/100.0);
          h = (int)((rough_dpi*(double)question_code.box_height[r])/100.0);
          System.out.println( "Look for box here: " + x + " : " + y + " : " + w + " : " + h );
          response.box_image = image.getSubimage(x, y, w, h );
        }
      }


      processBoxImages( page );


      return page;
    } catch (ReaderException e) {
      e.printStackTrace();
      System.err.println(uri.toString() + ": No barcode found");
      return null;
    }
  }


  private static void processBoxImages( PageData page )
  {
    QuestionData question;
    ResponseData response;
    ResponseBoxColourLookupTable lookup = new ResponseBoxColourLookupTable();
    LookupOp lop = new LookupOp( lookup, null );
    IdentityLookupTable idlookup = new IdentityLookupTable();
    LookupOp idlop = new LookupOp( idlookup, null );
    BufferedImage temp;
    QTIElementItem qti_item;
    boolean one_only;
    int darkest, next_darkest;
    double darkest_dark_pixels, next_darkest_dark_pixels;

    for ( int i=0; i<page.questions.size(); i++ )
    {
      question = page.questions.get( i );
      qti_item = page.exam.qdefs.qti.getItem( question.ident );
      one_only = qti_item != null && qti_item.isStandardMultipleChoice();

      darkest = -1;
      darkest_dark_pixels = 0.0;
      for ( int j=0; j<question.responses.size(); j++ )
      {
        response = question.responses.get( j );
        response.selected= false;
        response.examiner_selected = false;
        response.filtered_image = null;
        response.dark_pixels = -1;
        if ( !qti_item.isSupported() )
            continue;
        
        temp = response.box_image;

        response.box_image = new BufferedImage( temp.getWidth(), temp.getHeight(),
                                                BufferedImage.TYPE_INT_RGB );
        response.filtered_image = new BufferedImage( temp.getWidth(), temp.getHeight(),
                                                      BufferedImage.TYPE_INT_RGB );
        idlop.filter( temp, response.box_image );
        lookup.resetStatistics();
        lop.filter( temp, response.filtered_image );
        //response.selected = ( (double)lookup.countBlackPixels() / (double)lookup.countWhitePixels() ) > 0.1;
        response.dark_pixels = (double)lookup.countBlackPixels() / (double)(lookup.countWhitePixels()+lookup.countBlackPixels());
        if ( response.dark_pixels > darkest_dark_pixels )
        {
          darkest = j;
          darkest_dark_pixels = response.dark_pixels;
        }
      }

      if ( one_only )
      {
        // now processing single available option
        // low threshold for accepting a mark
        if ( darkest >= 0 && darkest_dark_pixels > 0.02 )
        {
          // find next darkest selection
          next_darkest = -1;
          next_darkest_dark_pixels = 0.0;
          for ( int j=0; j<question.responses.size(); j++ )
          {
            if ( j == darkest )
              continue;
            response = question.responses.get( j );
            if ( response.dark_pixels > next_darkest_dark_pixels )
            {
              next_darkest = j;
              next_darkest_dark_pixels = response.dark_pixels;
            }
          }

          // Only a clear distinct selection if no other dark box or
          // the next darkest
          // is less than 80% of the darkest
          if ( next_darkest < 0 || (next_darkest_dark_pixels / darkest_dark_pixels) < 0.8 )
          {
            response = question.responses.get( darkest );
            response.selected = true;
            response.examiner_selected = true;
          }
        }
      }
      else
      {
        for ( int j=0; j<question.responses.size(); j++ )
        {
          response = question.responses.get( j );
          response.selected = ( (double)lookup.countBlackPixels() / (double)lookup.countWhitePixels() ) > 0.05;
          response.examiner_selected = response.selected;
        }
      }
    }

  }



  private static double checkpagescale( double[] triangle )
  {
    double adx = triangle[2]-triangle[0];
    double ady = triangle[3]-triangle[1];
    double alength = Math.sqrt( (adx*adx) + (ady*ady) );
    double bdx = triangle[4]-triangle[0];
    double bdy = triangle[5]-triangle[1];
    double blength = Math.sqrt( (bdx*bdx) + (bdy*bdy) );
    return blength / alength;
  }

  private static double[] calibrate( double[] triangle, double width, double height )
  {
    double horizontal_dx, horizontal_dy;
    double vertical_dx,   vertical_dy;

    double[] scale = new double[4];  // hor dx,dy  vert dx,dy

    // measure input vectors
    horizontal_dx = triangle[2] - triangle[0];
    horizontal_dy = triangle[3] - triangle[1];
    vertical_dx =   triangle[4] - triangle[0];
    vertical_dy =   triangle[5] - triangle[1];

    //System.out.println( "input v " + horizontal_dx );
    //System.out.println( "input v " + horizontal_dy );
    //System.out.println( "input v " + vertical_dx );
    //System.out.println( "input v " + vertical_dy );

    scale[0] = horizontal_dx / width;  // in dots per hundredth inch
    scale[1] = horizontal_dy / width;
    scale[2] = vertical_dx / height;
    scale[3] = vertical_dy / height;

    //for ( int i=0; i<scale.length; i++ )
    //  System.out.println( "scale = " + scale[i] );

    return scale;
  }

}


