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

import java.awt.geom.NoninvertibleTransformException;
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
import java.awt.Rectangle;
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
import org.qyouti.qti1.gui.QuestionMetricsRecord;
import org.qyouti.qti1.gui.QuestionMetricsRecordSet;
import org.qyouti.scan.image.CannyEdgeDetector;
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



  private static QRScanResult decodeQR( BufferedImage image, Rectangle r )
          throws ReaderException
  {
    return decodeQR( image, r.x, r.y, r.x + r.width, r.y + r.height );
  }


  private static int previous_threshold=120;
  private static QRScanResult decodeQR( BufferedImage image, int x1, int y1, int x2, int y2 )
          throws ReaderException
  {
    int threshold=0;
    QRScanResult result;
    //MonochromeBitmapSource source = new BufferedImageMonochromeBitmapSource( image, x1, y1, x2, y2 );
    BufferedImage cropped = image.getSubimage(x1, y1, x2-x1, y2-y1);
    BufferedImage img_filt =  new BufferedImage( cropped.getWidth(), cropped.getHeight(),
                                                cropped.getType() );

    QRCodeColourLookupTable lookup = new QRCodeColourLookupTable( cropped.getColorModel().getNumComponents() );
    LookupOp lop = new LookupOp( lookup, null );

    for ( int n = 0; n < 200; n = (n<0) ? (-(n-2)) : (-(n+2))    )
    {
      threshold = previous_threshold + n;
      lookup.setThreshold( threshold );
      img_filt = lop.filter( cropped, img_filt );
      try
      {
        result = QRCodec.decode( img_filt, cropped );
        try {Thread.sleep(20);} catch (InterruptedException ex){}
        if ( n != 0 )
        {
          System.out.println( "DECODED QRCode at threshold = " + threshold );
          previous_threshold = threshold;
        }
        return result;
      } catch (Exception ex)
      {
        //System.out.println( "No QRCode - exception thrown." );
        //Logger.getLogger(PageDecoder.class.getName()).log(Level.SEVERE, null, ex);
      }
      try {Thread.sleep(20);} catch (InterruptedException ex){}
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

  private static QuestionCode decodeQuestion( QuestionMetricsRecordSet qmrset, int prefindex, QRScanResult scanresult )
          throws UnsupportedEncodingException
  {
    //System.out.println( "QRCode contains string: " + scanresult.getText() );
    QuestionCode qcode = new QuestionCode();

    //if there is a qmrset then the metrics are in that file, not the qr code
    if ( qmrset != null )
    {
      QuestionMetricsRecord record = qmrset.getQuestionMetricsRecord(prefindex, scanresult.getText() );
      qcode.id = record.id;
      qcode.next = (int)record.height;
      qcode.box_xoffset = new int[record.boxes.size()];
      qcode.box_yoffset = new int[record.boxes.size()];
      qcode.box_width = new int[record.boxes.size()];
      qcode.box_height = new int[record.boxes.size()];
      for ( int i=0; i<record.boxes.size(); i++ )
      {
        qcode.box_xoffset[i] =  record.boxes.get(i).x+4;
        qcode.box_yoffset[i] =  record.boxes.get(i).y+4;
        qcode.box_width[i]   =  record.boxes.get(i).width-8;
        qcode.box_height[i]  =  record.boxes.get(i).height-8;
        //System.out.println( "box " + qcode.box_xoffset[i] + ", " + qcode.box_yoffset[i] + ", " + qcode.box_width[i] + ", " + qcode.box_height[i] );
      }
      return qcode;
    }


    int boxes;
    if ( pinkboxfullcoords )
    {
      DataInputStream in = new DataInputStream( new ByteArrayInputStream( scanresult.getBytes() ) );
      Vector<Short> sarray = new Vector<Short>();
      try
      {
        qcode.id = in.readUTF();
        qcode.next = in.readShort()*10;
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
      qcode.next *= 10;
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
    QRScanResult itemresult, pageresult, calibrationresult;

    Point subimage_topleft;
    Point subimage_bottomright;
    double[] triangle = new double[6];
    Rectangle pageqrsearchrect, calqrsearchrect, itemqrsearchrect;
    AffineTransform questiontransform, pagetransform, revpagetransform;
    
    System.out.println( "Decoding a page." );
    

    try {
      double rough_dpi = (double)image.getHeight() / 11.69;
      //System.out.println( "Rough DPI of image = " + rough_dpi );

      // Bottom left corner
      pageqrsearchrect = new Rectangle(
          0,
          image.getHeight()-(int)(rough_dpi*1.5),
          (int)(rough_dpi*1.5),
          (int)(rough_dpi*1.5) );

      //Bottom right corner
      calqrsearchrect = new Rectangle(
          image.getWidth()-(int)(rough_dpi*1.2),
          image.getHeight()-(int)(rough_dpi*1.2),
          (int)(rough_dpi*1.1),
          (int)(rough_dpi*1.1) );

      // Top left corner
      itemqrsearchrect = new Rectangle(
          0,
          0,
          (int)(rough_dpi*1.5),
          (int)(rough_dpi*1.5) );

      //System.out.println( "Decoding calibration qr." );
      calibrationresult = decodeQR( image, calqrsearchrect );
      if ( calibrationresult == null )
        return null;
      //System.out.println( "Processing calibration QR.  [" + calibrationresult.getText() + "]" );
      if ( !calibrationresult.getText().startsWith("qyouti/") )
      {
        System.out.println( "No qyouti signiture QR code in bottom right corner." );
        return null;
      }
      StringTokenizer ptok = new StringTokenizer( calibrationresult.getText(), "/" );
      ptok.nextToken();
      double declared_calibration_width  = Double.parseDouble(ptok.nextToken()) / 10.0;
      double declared_calibration_height = Double.parseDouble(ptok.nextToken()) / 10.0;
//      System.out.println( "Calibration area " +
//          declared_calibration_width + "\" by " +
//          declared_calibration_height + "\""
//          );


      double blackness = calibrationresult.getBlackness();

      //System.out.println( "Decoding first item qr." );
      itemresult = decodeQR( image, itemqrsearchrect );
      if ( itemresult == null )
        return null;
      //System.out.println( "Processing first item QR.  [" + itemresult.getText() + "]" );


      //System.out.println( "Decoding page qr." );
      pageresult = decodeQR( image, pageqrsearchrect );
      if ( pageresult == null )
        return null;
      System.out.println( "Processing decoded QR.  [" + pageresult.getText() + "]" );
      ptok = new StringTokenizer( pageresult.getText(), "/" );
      String version = ptok.nextToken();
      String printid = ptok.nextToken();
      int userpref = 0;
      try  { userpref = Integer.parseInt(ptok.nextToken()); }
      catch ( NumberFormatException nfe ) {}
      userpref--;
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

//      System.out.println( "   version: " + version );
//      System.out.println( " print run: " + printid );
//      System.out.println( " pref. set: " + userpref );
//      System.out.println( "      name: " + candidate_name );
//      System.out.println( "    number: " + candidate_number );
//      System.out.println( "      page: " + page_number );
//      System.out.println( " questions: " + question_count );

      QuestionMetricsRecordSet qmrset = exam.qmrcache.getSet(printid);
      QuestionMetricsRecord qmr;

      page = new PageData( exam, candidate_name, candidate_number, page_number );
      page.source = sourcename;
      //System.out.println( "Done decoding page qr." );
      if ( question_count == 0 )
        return page;


      pagetransform = pageTransform( 
          itemqrsearchrect, itemresult,
          pageqrsearchrect, pageresult,
          calqrsearchrect, calibrationresult,
          declared_calibration_width , declared_calibration_height );
      
      try
      {
        revpagetransform = pagetransform.createInverse();
      } catch (NoninvertibleTransformException ex)
      {
        Logger.getLogger(PageDecoder.class.getName()).log(Level.SEVERE, null, ex);
        return null;
      }


      double last_height=0.0;
      int w, h;
      QuestionCode question_code=null;
      Point2D measureditempos =new Point2D.Double();
      Point2D measureditempos_inches = new Point2D.Double();
      measureditempos_inches.setLocation(0.0, 0.0);
      for ( int q=0; q<question_count; q++ )
      {
//        System.out.println( "==================================" );
        subimage_topleft = qrinchtopixels( pagetransform,  
            measureditempos_inches.getX()-0.2,
            measureditempos_inches.getY()+last_height-0.2 );
        subimage_bottomright = qrinchtopixels( pagetransform, 
            measureditempos_inches.getX() + metrics_top_qr_width/100.0+0.2,
            measureditempos_inches.getY()+last_height + metrics_top_qr_width/100.0+0.2 );
        if ( subimage_topleft.x < 0 ) subimage_topleft.x = 0;
        if ( subimage_topleft.y < 0 ) subimage_topleft.y = 0;
//        System.out.println( "Look for question code here: " +
//                subimage_topleft.x     + ":" + subimage_topleft.y     + ":" +
//                subimage_bottomright.x + ":" + subimage_bottomright.y + ":"       );
        itemresult = decodeQR( image,
                subimage_topleft.x,     subimage_topleft.y,
                subimage_bottomright.x, subimage_bottomright.y       );
        if ( itemresult == null )
          break;

        measureditempos.setLocation(
            itemresult.getResultPoints()[1].getX()+subimage_topleft.x,
            itemresult.getResultPoints()[1].getY()+subimage_topleft.y );
//        questiontransform = qrtransform( subimage_topleft, itemresult, metrics_top_qr_width/100.0, metrics_top_qr_width/100.0 );
        question = new QuestionData( page );
        question_code = decodeQuestion( qmrset, userpref, itemresult );
        //System.out.println( "Question ID " + question_code.id   );
        //System.out.println( "       Next " + question_code.next );
        last_height = ((double)question_code.next)/100.0;
        question.ident = question_code.id;

        revpagetransform.transform( measureditempos, measureditempos_inches );
        //System.out.println( "Location: " + measureditempos_inches.getX() + "   " +measureditempos_inches.getY() );

        for ( int r=0; r<question_code.box_yoffset.length; r++ )
        {
          //System.out.println( "-----------------------------------" );
          //System.out.println( "Response " + r + " at " + question_code.box_xoffset[r] + ", " + question_code.box_yoffset[r] );
          //System.out.println( "                 W, H " + question_code.box_width[r]   + ", " + question_code.box_height[r] );
          subimage_topleft     = qrinchtopixels(
              pagetransform,
              measureditempos_inches.getX() + question_code.box_xoffset[r]/100.0,
              measureditempos_inches.getY() + question_code.box_yoffset[r]/100.0 );
          subimage_bottomright = qrinchtopixels(
              pagetransform,
              measureditempos_inches.getX() + (question_code.box_xoffset[r] + question_code.box_width[r])/100.0,
              measureditempos_inches.getY() + (question_code.box_yoffset[r] + question_code.box_height[r])/100.0
              );
          response = new ResponseData( question );
          response.position = r;
          response.ident = null;
          w = subimage_bottomright.x - subimage_topleft.x;
          h = subimage_bottomright.y - subimage_topleft.y;
          //System.out.println( "Look for box here: " + subimage_topleft.x + " : " + subimage_topleft.y + " : " + w + " : " + h );
          response.box_image = image.getSubimage(subimage_topleft.x, subimage_topleft.y, w, h );
        }
      }


      processBoxImages( page, blackness );


      return page;
    } catch (ReaderException e) {
      e.printStackTrace();
      System.err.println(sourcename + ": No barcode found");
      return null;
    }
  }


  private static void processBoxImages( PageData page, double blackness )
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
    double redmean, lightestredmean=0.0;

    CannyEdgeDetector detector;

    File examfolder = page.exam.examfile.getParentFile();
    File scanfolder = new File( examfolder, "scans" );
    if ( !scanfolder.exists() )
      scanfolder.mkdir();


    // Look at the centres of all pink boxes and measure
    // brightness of the red channel
    // Find the lightest of all boxes to calibrate the
    // page.
    for ( int i=0; i<page.questions.size(); i++ )
    {
      question = page.questions.get( i );
      for ( int j=0; j<question.responses.size(); j++ )
      {
        response = question.responses.get( j );
        temp = response.box_image;
        redmean=0.0;
        for ( int x=(temp.getWidth()/2)-1; x<=(temp.getWidth()/2)+1; x++ )
          for ( int y=(temp.getHeight()/2)-1; y<=(temp.getHeight()/2)+1; y++ )
            redmean += (double)((temp.getRGB(x, y) & 0xff0000) >> 16);
        redmean = redmean / 9.0;
        if ( lightestredmean < redmean )
          lightestredmean = redmean;
      }
    }


    System.out.println( "Black level: " + blackness + "   Pink level: " + lightestredmean );

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

        try
        {
          ImageIO.write(response.box_image, "gif",
              new File( scanfolder,
                question.ident + "_" + j + "_" + page.candidate_number + ".gif" ));
        } catch (IOException ex)
        {
          Logger.getLogger(PageDecoder.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if ( !qti_item.isSupported() )
        {
          response.box_image = null;
          response.filtered_image = null;
          continue;
        }

        if ( !qti_item.isMultipleChoice() )
          continue;

        temp = response.box_image;

        response.box_image = new BufferedImage( temp.getWidth(), temp.getHeight(),
                                                temp.getType() );
        response.filtered_image = new BufferedImage( temp.getWidth(), temp.getHeight(),
                                                      temp.getType() );

        if ( lookup == null )
        {
          lookup = new ResponseBoxColourLookupTable(
              temp.getColorModel().getNumComponents(),
              blackness,
              lightestredmean );
          lop = new LookupOp( lookup, null );
          idlookup = new IdentityLookupTable( temp.getColorModel().getNumComponents() );
          idlop = new LookupOp( idlookup, null );
        }

//        detector = new CannyEdgeDetector();
//        detector.setLowThreshold(0.5f);
//        detector.setHighThreshold(1.0f);
//        //detector.setGaussianKernelRadius(2.2f);
//        //detector.setGaussianKernelWidth(20);
//        detector.setOnlyRedChannel(true);
//        detector.setContrastNormalized(true);
//        detector.setSourceImage( temp );
//        detector.process();
//        BufferedImage edges = detector.getEdgesImage();
//        try
//        {
//          ImageIO.write(edges, "gif",
//              new File( scanfolder,
//                question.ident + "_" + j + "_" + page.candidate_number + "_edges.gif" ));
//        } catch (IOException ex)
//        {
//          Logger.getLogger(PageDecoder.class.getName()).log(Level.SEVERE, null, ex);
//        }


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

        //response.box_image = null;
        //response.filtered_image = null;
      }

      if ( !qti_item.isSupported() )
        continue;
      if ( !qti_item.isMultipleChoice() )
        continue;

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

  private static AffineTransform pageTransform(
      Rectangle ra, QRScanResult resulta,        // top left
      Rectangle rb, QRScanResult resultb,        // bottom left
      Rectangle rc, QRScanResult resultc,        // bottom right
      double width, double height )
  {
    double horizontal_dx, horizontal_dy;
    double vertical_dx,   vertical_dy;

    double[] scale = new double[4];  // hor dx,dy  vert dx,dy

    ResultPoint[] pointsa = resulta.getResultPoints();
    ResultPoint[] pointsb = resultb.getResultPoints();
    ResultPoint[] pointsc = resultc.getResultPoints();

    ResultPoint[] pagepoints = new ResultPoint[3];
    pagepoints[0] = pointsa[1];
    pagepoints[1] = pointsb[0];
    pagepoints[2] = pointsc[0];
    // pointsa[0] = bottom left  pointsa[1] = top left    pointsa[2] = top right

    // measure input vectors - pixel units
    horizontal_dx = (rc.x + pagepoints[2].getX()) - (rb.x + pagepoints[1].getX());
    horizontal_dy = (rc.y + pagepoints[2].getY()) - (rb.y + pagepoints[1].getY());
    vertical_dx   = (rb.x + pagepoints[1].getX()) - (ra.x + pagepoints[0].getX());
    vertical_dy   = (rb.y + pagepoints[1].getY()) - (ra.y + pagepoints[0].getY());

    //System.out.println( "input v " + horizontal_dx );
    //System.out.println( "input v " + horizontal_dy );
    //System.out.println( "input v " + vertical_dx );
    //System.out.println( "input v " + vertical_dy );

    // convert to pixels per inches
    horizontal_dx = horizontal_dx / width;
    horizontal_dy = horizontal_dy / width;
    vertical_dx   = vertical_dx   / height;
    vertical_dy   = vertical_dy   / height;

    //for ( int i=0; i<scale.length; i++ )
    //  System.out.println( "scale = " + scale[i] );

    return new AffineTransform(
        horizontal_dx,
        -0.5*vertical_dx,
        -2*horizontal_dy,
        vertical_dy,
        ra.x + pagepoints[0].getX(),
        ra.y + pagepoints[0].getY()
        );
  }




  public static void main( String[] args )
  {
    double hdx, hdy;
    double vdx, vdy;
    Point2D x = new Point2D.Double( 2.5, 5.0);
    
    Point2D a = new Point2D.Double( 0.0, 0.0 );
    Point2D b = new Point2D.Double( 0.0, 10.0 );
    Point2D c = new Point2D.Double( 5.0, 10.0 );

    AffineTransform rotate = AffineTransform.getRotateInstance( -0.1 );
    AffineTransform distort = AffineTransform.getTranslateInstance( 0.5, 0.5 );
    distort.concatenate( rotate );

    distort.transform(a, a);
    distort.transform(b, b);
    distort.transform(c, c);


    hdx = (c.getX() - b.getX()) /  5.0;
    hdy = (c.getY() - b.getY()) / 10.0;
    vdx = (b.getX() - a.getX()) /  5.0;
    vdy = (b.getY() - a.getY()) / 10.0;

    AffineTransform paget = new AffineTransform(
        hdx,
        -0.5*vdx,
        -2*hdy,
        vdy,
        a.getX(),
        a.getY()
        );

    paget.transform( x, x );

  }

}


