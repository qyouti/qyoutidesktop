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
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
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

  static final boolean pinkboxfullcoords = true;
  
  static final double metrics_top_qr_to_bottom_qr =  975.0;
  static final double metrics_top_qr_width        =   60.0;
  static final double metrics_qstn_qr_width       =   60.0;
  static final double metrics_qstn_qr_hzntl_box   =  113.0;

  static final Hashtable hints = new Hashtable();
  static
  {
    hints.put( DecodeHintType.TRY_HARDER, Boolean.TRUE );
  }



  public static PageData decode(ExaminationData exam, String argument )
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
    BufferedImage img_filt =  new BufferedImage( cropped.getWidth(), cropped.getHeight(),
                                                cropped.getType() );

    QRCodeColourLookupTable lookup = new QRCodeColourLookupTable( cropped.getColorModel().getNumComponents() );
    LookupOp lop = new LookupOp( lookup, null );

    for ( int n = 0; n < 80; n = (n<0) ? (-(n-5)) : (-(n+5))    )
    {
      lookup.setThreshold( 160 + n );
      img_filt = lop.filter( cropped, img_filt );
      System.out.println( "DECODING QRCode threshold = " + n );
      try
      {
        result = QRCodec.decode( img_filt );
        try {Thread.sleep(2000);} catch (InterruptedException ex){}
        return result;
      } catch (Exception ex)
      {
        //System.out.println( "No QRCode - exception thrown." );
        //Logger.getLogger(PageDecoder.class.getName()).log(Level.SEVERE, null, ex);
      }
      try {Thread.sleep(2000);} catch (InterruptedException ex){}
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

  private static QuestionCode decodeQuestion( QRScanResult scanresult )
          throws UnsupportedEncodingException
  {
    System.out.println( "QRCode contains string: " + scanresult.getText() );
    QuestionCode qcode = new QuestionCode();

    int boxes;
    if ( pinkboxfullcoords )
    {
      DataInputStream in = new DataInputStream( new ByteArrayInputStream( scanresult.getBytes() ) );
      Vector<Short> sarray = new Vector<Short>();
      try
      {
        qcode.id = in.readUTF();
        qcode.next = in.readShort();
        while ( in.available() > 0 )
          sarray.add( new Short(in.readShort()) );

      } catch (IOException ex)
      {
        Logger.getLogger(PageDecoder.class.getName()).log(Level.SEVERE, null, ex);
      }

      boxes = sarray.size() / 4;
      System.out.println( "no. boxes " + boxes );
              qcode.box_xoffset = new int[boxes];
      qcode.box_yoffset = new int[boxes];
      qcode.box_width = new int[boxes];
      qcode.box_height = new int[boxes];
      for ( int i=0; i<boxes; i++ )
      {
        qcode.box_xoffset[i] =  sarray.elementAt(i*4 + 0);
        qcode.box_yoffset[i] =  sarray.elementAt(i*4 + 1);
        qcode.box_width[i]   =  sarray.elementAt(i*4 + 2);
        qcode.box_height[i]  =  sarray.elementAt(i*4 + 3);
        System.out.println( "box " + qcode.box_xoffset[i] + ", " + qcode.box_yoffset[i] + ", " + qcode.box_width[i] + ", " + qcode.box_height[i] );
      }
    }
    else
    {
      qcode.id = decodeCString( scanresult.getBytes(), 0 );
      qcode.next = scanresult.getBytes()[ qcode.id.length()+1 ];
      boxes = (scanresult.getBytes().length - qcode.id.length() - 2) / 2;
      qcode.box_xoffset = new int[boxes];
      qcode.box_yoffset = new int[boxes];
      qcode.box_width = new int[boxes];
      qcode.box_height = new int[boxes];
      for ( int i=0; i<boxes; i++ )
      {
        qcode.box_xoffset[i] = (int)metrics_qstn_qr_hzntl_box;
        qcode.box_width[i] = 21;
        qcode.box_height[i] = 21;
        qcode.box_yoffset[i] =  ( scanresult.getBytes()[ qcode.id.length()+2+ (i*2)     ] & 0xff )
                                |
                             ( ( scanresult.getBytes()[ qcode.id.length()+2+ (i*2) + 1 ] << 8 ) & 0xff00 );
      }
    }

    return qcode;
  }


  private static PageData decode(ExaminationData exam, URI uri)
          throws IOException
  {
    BufferedImage image;
    try {
      image = ImageIO.read(uri.toURL());
    } catch (IllegalArgumentException iae) {
      throw new FileNotFoundException("Resource not found: " + uri);
    }
    if (image == null) {
      System.err.println(uri.toString() + ": Could not load image");
      return null;
    }
    return decode( exam, image, uri.toString() );
  }

  public static PageData decode(ExaminationData exam, BufferedImage image, String sourcename )
          throws IOException
  {
    int i;
    PageData page;
    QuestionData question;
    ResponseData response;
    QRScanResult result;

    Point subimage_topleft;
    Point subimage_bottomright;
    double[] triangle = new double[6];

    System.out.println( "\n\n\nDecoding a page." );
    

    try {
      double rough_dpi = (double)image.getHeight() / 11.69;
      System.out.println( "Rough DPI of image = " + rough_dpi );

      // Bottom left corner
      System.out.println( "Decoding page qr." );
      result = decodeQR( image,
              (int)(rough_dpi*0.3),
              image.getHeight()-(int)(rough_dpi*1.9),
              (int)(rough_dpi*2.0),
              image.getHeight()-(int)(rough_dpi*0.3)
              );
      if ( result == null )
        return null;
      System.out.println( "Processing decoded QR.  [" + result.getText() + "]" );
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
      //if ( question_count > 4 ) question_count = 4;

      System.out.println( "     name: " + candidate_name );
      System.out.println( "   number: " + candidate_number );
      System.out.println( "     page: " + page_number );
      System.out.println( "questions: " + question_count );

      page = new PageData( exam, candidate_name, candidate_number, page_number );
      page.source = sourcename;
      System.out.println( "Done decoding page qr." );
      if ( question_count == 0 )
        return page;

      ResultPoint[] bottom_left_points = result.getResultPoints();
      triangle[4] = bottom_left_points[1].getX()+(int)(rough_dpi*0.5);
      triangle[5] = bottom_left_points[1].getY()+image.getHeight()-(int)(rough_dpi*1.6);


      // Take top left corner only
      subimage_topleft     = new Point( (int)(rough_dpi*0.0 ), (int)(rough_dpi*0.0) );
      subimage_bottomright = new Point( (int)(rough_dpi*1.75), (int)(rough_dpi*2.0) );
      result = decodeQR( image,
              subimage_topleft.x,
              subimage_topleft.y,
              subimage_bottomright.x,
              subimage_bottomright.y  );
      if ( result == null )
      {
        System.out.println( "No top left corner QR code." );
        return null;
      }
      
      // Not really interested in data right now
      ResultPoint[] top_left_points = result.getResultPoints();
      triangle[0] = top_left_points[1].getX() + (int)subimage_topleft.getX();
      triangle[1] = top_left_points[1].getY() + (int)subimage_topleft.getY();
      triangle[2] = top_left_points[2].getX() + (int)subimage_topleft.getX();
      triangle[3] = top_left_points[2].getY() + (int)subimage_topleft.getY();

      page.height = checkpagescale( triangle );
      // are some scan lines missing?
      if ( page.height < 16 )
      {
        System.out.println( "Bailing out on this page. page.height = " + page.height );
        return null;
      }
      
      AffineTransform questiontransform = qrtransform( subimage_topleft, result, metrics_top_qr_width/100.0 , metrics_top_qr_width/100.0 );

      double voffset_hnth_inch=0.0;
      int w, h;
      QuestionCode question_code=null;

      for ( int q=0; q<question_count; q++ )
      {
        System.out.println( "==================================" );
        subimage_topleft = qrinchtopixels( questiontransform,  -0.2, voffset_hnth_inch/100.0-0.2 );
        subimage_bottomright = qrinchtopixels( questiontransform, metrics_top_qr_width/100.0+0.2, (voffset_hnth_inch+metrics_top_qr_width)/100.0+0.2 );
        System.out.println( "Look for question code here: " +
                subimage_topleft.x     + ":" + subimage_topleft.y     + ":" +
                subimage_bottomright.x + ":" + subimage_bottomright.y + ":"       );
        result = decodeQR( image,
                subimage_topleft.x,     subimage_topleft.y,
                subimage_bottomright.x, subimage_bottomright.y       );
        if ( result == null )
          break;

        questiontransform = qrtransform( subimage_topleft, result, metrics_top_qr_width/100.0, metrics_top_qr_width/100.0 );
        question = new QuestionData( page );
        question_code = decodeQuestion( result );
        System.out.println( "Question ID " + question_code.id   );
        System.out.println( "       Next " + question_code.next );
        voffset_hnth_inch = question_code.next * 10;
        question.ident = question_code.id;

        for ( int r=0; r<question_code.box_yoffset.length; r++ )
        {
          System.out.println( "-----------------------------------" );
          System.out.println( "Response " + r + " at " + question_code.box_xoffset[r] + ", " + question_code.box_yoffset[r] );
          System.out.println( "                 W, H " + question_code.box_width[r]   + ", " + question_code.box_height[r] );
          subimage_topleft     = qrinchtopixels( questiontransform, question_code.box_xoffset[r]/100.0, question_code.box_yoffset[r]/100.0 );
          subimage_bottomright = qrinchtopixels(
              questiontransform,
              (question_code.box_xoffset[r] + question_code.box_width[r])/100.0,
              (question_code.box_yoffset[r] + question_code.box_height[r])/100.0
              );
          response = new ResponseData( question );
          response.position = r;
          response.ident = null;
          w = subimage_bottomright.x - subimage_topleft.x;
          h = subimage_bottomright.y - subimage_topleft.y;
          System.out.println( "Look for box here: " + subimage_topleft.x + " : " + subimage_topleft.y + " : " + w + " : " + h );
          response.box_image = image.getSubimage(subimage_topleft.x, subimage_topleft.y, w, h );
        }
      }


      processBoxImages( page );


      return page;
    } catch (ReaderException e) {
      e.printStackTrace();
      System.err.println(sourcename + ": No barcode found");
      return null;
    }
  }


  private static void processBoxImages( PageData page )
  {
    QuestionData question;
    ResponseData response;
    ResponseBoxColourLookupTable lookup = null;
    LookupOp lop = null;
    IdentityLookupTable idlookup = null;
    LookupOp idlop = null;
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
                                                temp.getType() );
        response.filtered_image = new BufferedImage( temp.getWidth(), temp.getHeight(),
                                                      temp.getType() );

        if ( lookup == null )
        {
          lookup = new ResponseBoxColourLookupTable( temp.getColorModel().getNumComponents() );
          lop = new LookupOp( lookup, null );
          idlookup = new IdentityLookupTable( temp.getColorModel().getNumComponents() );
          idlop = new LookupOp( idlookup, null );
        }

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
        if ( darkest >= 0 && darkest_dark_pixels > 0.01 )
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
          response.selected = response.dark_pixels > 0.05;
          response.examiner_selected = response.selected;
        }
      }
    }

  }


  private static Point qrinchtopixels( AffineTransform t, double x, double y )
  {
    Point2D point = new Point2D.Double( x, y );
    Point2D tpoint = t.transform(point, null);
    return new Point( (int)tpoint.getX(), (int)tpoint.getY() );
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



  private static AffineTransform qrtransform( Point2D subimagepos, QRScanResult result, double width, double height )
  {
    double horizontal_dx, horizontal_dy;
    double vertical_dx,   vertical_dy;

    double[] scale = new double[4];  // hor dx,dy  vert dx,dy

    ResultPoint[] points = result.getResultPoints();
    // point[0] = bottom left  point[1] = top left    point[2] = top right

    // measure input vectors - pixel units
    horizontal_dx = points[2].getX() - points[1].getX();
    horizontal_dy = points[2].getY() - points[1].getY();
    vertical_dx   = points[0].getX() - points[1].getX();
    vertical_dy   = points[0].getY() - points[1].getY();

    //System.out.println( "input v " + horizontal_dx );
    //System.out.println( "input v " + horizontal_dy );
    //System.out.println( "input v " + vertical_dx );
    //System.out.println( "input v " + vertical_dy );

    // convert to hundreths of inch
    horizontal_dx = horizontal_dx / width;  
    horizontal_dy = horizontal_dy / width;
    vertical_dx   = vertical_dx   / height;
    vertical_dy   = vertical_dy   / height;

    //for ( int i=0; i<scale.length; i++ )
    //  System.out.println( "scale = " + scale[i] );

    return new AffineTransform(
        horizontal_dx,
        vertical_dx,
        horizontal_dy,
        vertical_dy,
        subimagepos.getX() + points[1].getX(),
        subimagepos.getY() + points[1].getY()
        );
  }

}


