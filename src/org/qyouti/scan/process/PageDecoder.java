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
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.AffineTransformOp;
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
import org.qyouti.qti1.QTIResponse;
import org.qyouti.qti1.element.QTIElementItem;
import org.qyouti.qti1.element.QTIElementResponselabel;
import org.qyouti.qti1.element.QTIElementResponselid;
import org.qyouti.qti1.ext.qyouti.QTIExtensionRespextension;
import org.qyouti.qti1.gui.*;
import org.qyouti.scan.image.CannyEdgeDetector;
import org.qyouti.scan.image.PageRotator;
import org.qyouti.scan.image.QRCodeColourLookupTable;
import org.qyouti.scan.image.ResponseImageProcessor;

/**
 *
 * @author jon
 */
public class PageDecoder
{

  final boolean pinkboxfullcoords = true;
  
  //final double metrics_top_qr_to_bottom_qr =  975.0;
  final double metrics_top_qr_width        =     35.0;
  final double metrics_top_qr_margin       =     20.0;
  //final double metrics_qstn_qr_width       =   60.0;
  //final double metrics_qstn_qr_hzntl_box   =  113.0;

  final double metrics_print_qr_search_size = 1.75;  // inches


  final Hashtable hints = new Hashtable();

  double boxthreshold;
  int boxinset;

  public PageDecoder( double threshold, int inset )
  {
    hints.put( DecodeHintType.TRY_HARDER, Boolean.TRUE );
    boxthreshold = threshold;
    boxinset = inset;
  }

  public PageData decode(ExaminationData exam, String argument, int n )
          throws IOException, URISyntaxException
  {

    File inputFile = new File(argument);
    if (inputFile.exists()) {
        return decode(exam,inputFile.toURI(), n);
    } else {
      return decode(exam,new URI(argument), n );
    }
  }

  public PageData identifyPage(ExaminationData exam, String argument, int n )
          throws IOException, URISyntaxException
  {

    File inputFile = new File(argument);
    if (inputFile.exists()) {
        return identifyPage(exam,inputFile.toURI(), n);
    } else {
      return identifyPage(exam,new URI(argument), n );
    }
  }



//  private QRScanResult decodeQR( BufferedImage image, Rectangle r )
//          throws ReaderException
//  {
//    Rectangle[] rarray = new Rectangle[1];
//    rarray[0] = r;
//    return decodeQR( image, rarray );
//    //return decodeQR( image, r.x, r.y, r.x + r.width, r.y + r.height );
//  }

//  private QRScanResult decodeQR( BufferedImage image, Rectangle[] r )
//  //private QRScanResult decodeQR( BufferedImage image, int x1, int y1, int x2, int y2 )
//          throws ReaderException
//  {
//    return decodeQR( image, r, false );
//  }

  private int previous_threshold=120;
  private QRScanResult[] decodeQR( BufferedImage image, Rectangle[] r )
          throws ReaderException
  {
    int threshold=0;
    int found = 0;
    QRScanResult currentresult;
    QRScanResult[] result = new QRScanResult[3];
    
    //MonochromeBitmapSource source = new BufferedImageMonochromeBitmapSource( image, x1, y1, x2, y2 );
    BufferedImage[] cropped = new BufferedImage[r.length];
    BufferedImage[] img_filt = new BufferedImage[r.length];
    QRCodeColourLookupTable lookup;
    LookupOp lop;
    for ( int i=0; i<r.length; i++ )
    {
      cropped[i] = image.getSubimage(r[i].x, r[i].y, r[i].width, r[i].height);
      img_filt[i] =  new BufferedImage( cropped[i].getWidth(), cropped[i].getHeight(),
                                                cropped[i].getType() );
    }
    lookup = new QRCodeColourLookupTable( image.getColorModel().getNumComponents() );
    lop = new LookupOp( lookup, null );


    for ( int n = 0; n < 100; n = (n<0) ? (-(n-2)) : (-(n+2))    )
    {
      threshold = previous_threshold + n;
      lookup.setThreshold( threshold );
      for ( int i=0; i<r.length; i++ )
      {
        img_filt[i] = lop.filter( cropped[i], img_filt[i] );
        try
        {
          currentresult = QRCodec.decode( img_filt[i], cropped[i] );
          if ( currentresult != null )
          {
            //try {Thread.sleep(20);} catch (InterruptedException ex){}
            if ( n != 0 )
            {
              //System.out.println( "DECODED QRCode at threshold = " + threshold + " in rectangle " + i );
              previous_threshold = threshold;
            }
            if ( currentresult.getText() == null ) continue;
            if ( !currentresult.getText().startsWith("qyouti/") ) continue;
            result[found++] = currentresult;
          }
        } catch (Exception ex)
        {
          //Logger.getLogger(PageDecoder.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
      if ( found == 3 )
        return result;
    }
    return null;
  }



  private byte[] getQRBytes( Result result )
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




  private String decodeCString( byte[] data, int offset )
          throws UnsupportedEncodingException
  {
    int i;
    for ( i=offset; data[i] != 0; i++ )
      ;
    byte[] subarray = new byte[i-offset];
    System.arraycopy( data, offset, subarray, 0, i-offset );

    return new String( subarray, "utf8" );
  }


  private int bytesToInteger( byte[] data, int offset )
  {
      return  ( data[offset] & 0xff )
             |
              (( data[offset + 1] << 8 ) & 0xff00 );
  }

  private QuestionMetricsRecord decodeQuestion( QuestionMetricsRecordSet qmrset, int prefindex, QRScanResult scanresult )
          throws UnsupportedEncodingException
  {
    //System.out.println( "QRCode contains string: " + scanresult.getText() );
    QuestionCode qcode = new QuestionCode();

    if ( qmrset == null )
      throw new IllegalArgumentException( "Binary data encoded in the qrcode is no longer supported.");

    return qmrset.getQuestionMetricsRecord(prefindex, scanresult.getText() );

//    QuestionMetricsRecord record = qmrset.getQuestionMetricsRecord(prefindex, scanresult.getText() );
//    qcode.id = record.id;
//    qcode.next = (int)record.height;
//    qcode.box_xoffset = new int[record.boxes.size()];
//    qcode.box_yoffset = new int[record.boxes.size()];
//    qcode.box_width = new int[record.boxes.size()];
//    qcode.box_height = new int[record.boxes.size()];
//    for ( int i=0; i<record.boxes.size(); i++ )
//    {
//      qcode.box_xoffset[i] =  record.boxes.get(i).x + (boxinset/2);
//      qcode.box_yoffset[i] =  record.boxes.get(i).y + (boxinset/2);
//      qcode.box_width[i]   =  record.boxes.get(i).width - boxinset;
//      qcode.box_height[i]  =  record.boxes.get(i).height - boxinset;
//      //System.out.println( "box " + qcode.box_xoffset[i] + ", " + qcode.box_yoffset[i] + ", " + qcode.box_width[i] + ", " + qcode.box_height[i] );
//    }
//    return qcode;


//    int boxes;
//    if ( pinkboxfullcoords )
//    {
//      DataInputStream in = new DataInputStream( new ByteArrayInputStream( scanresult.getBytes() ) );
//      Vector<Short> sarray = new Vector<Short>();
//      try
//      {
//        qcode.id = in.readUTF();
//        qcode.next = in.readShort()*10;
//        while ( in.available() > 0 )
//          sarray.add( new Short(in.readShort()) );
//
//      } catch (IOException ex)
//      {
//        Logger.getLogger(PageDecoder.class.getName()).log(Level.SEVERE, null, ex);
//      }
//
//      boxes = sarray.size() / 4;
//      System.out.println( "no. boxes " + boxes );
//      qcode.box_xoffset = new int[boxes];
//      qcode.box_yoffset = new int[boxes];
//      qcode.box_width = new int[boxes];
//      qcode.box_height = new int[boxes];
//      for ( int i=0; i<boxes; i++ )
//      {
//        qcode.box_xoffset[i] =  sarray.elementAt(i*4 + 0);
//        qcode.box_yoffset[i] =  sarray.elementAt(i*4 + 1);
//        qcode.box_width[i]   =  sarray.elementAt(i*4 + 2);
//        qcode.box_height[i]  =  sarray.elementAt(i*4 + 3);
//        System.out.println( "box " + qcode.box_xoffset[i] + ", " + qcode.box_yoffset[i] + ", " + qcode.box_width[i] + ", " + qcode.box_height[i] );
//      }
//    }
//    else
//    {
//      qcode.id = decodeCString( scanresult.getBytes(), 0 );
//      qcode.next = scanresult.getBytes()[ qcode.id.length()+1 ];
//      qcode.next *= 10;
//      boxes = (scanresult.getBytes().length - qcode.id.length() - 2) / 2;
//      qcode.box_xoffset = new int[boxes];
//      qcode.box_yoffset = new int[boxes];
//      qcode.box_width = new int[boxes];
//      qcode.box_height = new int[boxes];
//      for ( int i=0; i<boxes; i++ )
//      {
//        qcode.box_xoffset[i] = (int)metrics_qstn_qr_hzntl_box;
//        qcode.box_width[i] = 21;
//        qcode.box_height[i] = 21;
//        qcode.box_yoffset[i] =  ( scanresult.getBytes()[ qcode.id.length()+2+ (i*2)     ] & 0xff )
//                                |
//                             ( ( scanresult.getBytes()[ qcode.id.length()+2+ (i*2) + 1 ] << 8 ) & 0xff00 );
//      }
//    }
//
//    return qcode;
  }


  private PageData decode( ExaminationData exam, URI uri, int i )
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
    return decode( exam, image, uri.toString(), i );
  }


  private PageData identifyPage( ExaminationData exam, URI uri, int i )
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
    return identifyPage( exam, image, uri.toString(), i );
  }


  private Rectangle getBottomLeftSearchRectangle( PageData page, Rectangle searchrect, QRScanResult calibrationresult )
  {
    // estimate centre of bottom left qr by devising two vectors from calibration points
    // and page dimensions and transforming coords of bottom left corner of qrcode.
    ResultPoint[] points = calibrationresult.getResultPoints();
    // measure bottom right qr..
    double qrh = Math.sqrt(   (double)(points[2].getX()-points[1].getX())*(points[2].getX()-points[1].getX())
                             +(double)(points[2].getY()-points[1].getY())*(points[2].getY()-points[1].getY())    );
    double qrv = Math.sqrt(   (double)(points[1].getX()-points[0].getX())*(points[1].getX()-points[0].getX())
                             +(double)(points[1].getY()-points[0].getY())*(points[1].getY()-points[0].getY())    );

    // one pixel horizontal move
    double unitvector_h_x = (double)(points[2].getX()-points[1].getX()) / qrh;
    double unitvector_h_y = (double)(points[2].getY()-points[1].getY()) / qrh;

    // one pixel vertical move
    double unitvector_v_x = (double)(points[0].getX()-points[1].getX()) / qrv;
    double unitvector_v_y = (double)(points[0].getY()-points[1].getY()) / qrv;

    // vector in inches on page
    double inchesx = (-page.declared_calibration_width + 0.3);
    double inchesy = -0.3;

    // vector in pixels on image
    double pixelsx = qrh * inchesx/0.3;
    double pixelsy = qrv * inchesy/0.3;

    double x = (double)searchrect.x;
    double y = (double)searchrect.y;

    x += (double)points[0].getX();
    y += (double)points[0].getY();

    x += unitvector_h_x * pixelsx;
    y += unitvector_h_y * pixelsx;

    x += unitvector_v_x * pixelsy;
    y += unitvector_v_y * pixelsy;

    Rectangle r = new Rectangle( (int)x, (int)y, 1, 1 );
    r.grow( (int)(qrh * 2.2), (int)(qrv * 2.2) );
    Rectangle safe_r =  r.intersection( page.scanbounds );
    return safe_r;
  }

  private Rectangle getTopLeftSearchRectangle( PageData page, Rectangle searchrect, QRScanResult calibrationresult )
  {
    // estimate centre of bottom left qr by devising two vectors from calibration points
    // and page dimensions and transforming coords of bottom left corner of qrcode.
    ResultPoint[] points = calibrationresult.getResultPoints();
    // measure bottom right qr..
    double qrh = Math.sqrt(   (double)(points[2].getX()-points[1].getX())*(points[2].getX()-points[1].getX())
                             +(double)(points[2].getY()-points[1].getY())*(points[2].getY()-points[1].getY())    );
    double qrv = Math.sqrt(   (double)(points[1].getX()-points[0].getX())*(points[1].getX()-points[0].getX())
                             +(double)(points[1].getY()-points[0].getY())*(points[1].getY()-points[0].getY())    );

    // one pixel horizontal move
    double unitvector_h_x = (double)(points[2].getX()-points[1].getX()) / qrh;
    double unitvector_h_y = (double)(points[2].getY()-points[1].getY()) / qrh;

    // one pixel vertical move
    double unitvector_v_x = (double)(points[0].getX()-points[1].getX()) / qrv;
    double unitvector_v_y = (double)(points[0].getY()-points[1].getY()) / qrv;

    // vector in inches on page
    double inchesx = 0.15;
    double inchesy = -page.declared_calibration_height + 0.15;

    // vector in pixels on image
    double pixelsx = qrh * inchesx/0.6;
    double pixelsy = qrv * inchesy/0.6;

    double x = (double)searchrect.x;
    double y = (double)searchrect.y;

    x += (double)points[0].getX();
    y += (double)points[0].getY();

    x += unitvector_h_x * pixelsx;
    y += unitvector_h_y * pixelsx;

    x += unitvector_v_x * pixelsy;
    y += unitvector_v_y * pixelsy;

    Rectangle r = new Rectangle( (int)x, (int)y, 1, 1 );
    r.grow( (int)(qrh * 1.1), (int)(qrv * 1.1) );
    return r.intersection( page.scanbounds );
  }


  private PageData identifyPage(ExaminationData exam, BufferedImage image, String sourcename, int scanorder )
          throws IOException
  {
    int i;
    PageData page;
    QRScanResult[] calibrationresult;
    QRScanResult pageresult;
    Rectangle[] calqrsearchrect = new Rectangle[4];

    //System.out.println( "Decoding a page." );

    page = new PageData( exam, sourcename, scanorder );
    page.source = sourcename;
    page.scanbounds = new Rectangle( 0, 0, image.getWidth(), image.getHeight() );

    try
    {
      // try whole quarter pages
      int ih = image.getHeight();
      int iw = image.getWidth();
      calqrsearchrect[0] = new Rectangle(       0,    0, iw/2, ih/2 );
      calqrsearchrect[1] = new Rectangle(    iw/2,    0, iw/2, ih/2 );
      calqrsearchrect[2] = new Rectangle(       0, ih/2, iw/2, ih/2 );
      calqrsearchrect[3] = new Rectangle(    iw/2, ih/2, iw/2, ih/2 );

      //System.out.println( "Decoding calibration qr." );
      calibrationresult = decodeQR( image, calqrsearchrect );
      if ( calibrationresult == null )
      {
        page.error = "Could not find the three qyouti QR codes.";
        return page;
      }

      // rotation?
      // To do...
      
      page.code = calibrationresult[1].getText();
      StringTokenizer ptok = new StringTokenizer( page.code, "/" );
      ptok.nextToken();  // skip 'qyouti'
      ptok.nextToken();  // skip 'bl'
      page.printid = ptok.nextToken();
      page.pageid = ptok.nextToken();
      
//      page.declared_calibration_width  = Double.parseDouble(ptok.nextToken()) / 10.0;
//      page.declared_calibration_height = Double.parseDouble(ptok.nextToken()) / 10.0;

      // does the source image need to be rotated?
      page.quadrant = 0;
      // TO DO...

      page.examfolder = exam.examcatalogue.getExamFolderFromPrintMetric( page.printid );
      page.paginationfile = new File( page.examfolder, "pagination_" + page.printid + ".xml" );              
      page.source = sourcename;

      if ( !page.paginationfile.exists() && !page.paginationfile.isFile() )
      {
        page.error = "Cannot find the pagination data file for this page.";
        return page;
      }

      PaginationRecord paginationrecord = exam.examcatalogue.getPrintMetric( page.printid );
      
      if ( true )
      {
        //page.error = "Stopping processing. " + paginationrecord;
        return page;
      }

      // Now we can optionally rotate the image, work out where the calibration QRCodes
      // should be, process them and set up a coordinate system.

      
//      page.candidate_name = candidate_name;
//      page.candidate_number = candidate_number;
//      page.page_number = page_number;
       
    }
    catch (ReaderException e)
    {
      e.printStackTrace();
      System.err.println(sourcename + ": No barcode found");
      page.error = "Can't read question QRCode.";
    }

    return page;
  }



  private PageData decode(ExaminationData exam, BufferedImage image, String sourcename, int scanorder )
          throws IOException
  {
    int i;
    PageData page;
    QuestionData question;
    ResponseData response;
    QRScanResult itemresult, pageresult, calibrationresult;

    Point subimage_topleft;
    Point subimage_bottomright;
    Rectangle subimage_rect;
    double[] triangle = new double[6];
    Rectangle pageqrsearchrect, itemqrsearchrect;
    Rectangle calqrsearchrect;
    AffineTransform questiontransform, pagetransform, revpagetransform;
    double pageblackness;
    
    //System.out.println( "Decoding a page." );

    page = identifyPage( exam, image, sourcename, scanorder );
    if ( page == null || page.error != null )
      return page;

    if ( true )
    {
      page.error = "Stopped.";
      return page;
    }
    
    
    try {
      // does the source image need to be rotated?
      BufferedImage rotatedimage = image;
      PageRotator rot = new PageRotator( image );
      switch ( page.quadrant )
      {
        case 1:
          rotatedimage = rot.rotate90();
          break;
        case 2:
          rotatedimage = rot.rotate180();
          break;
        case 3:
          rotatedimage = rot.rotate270();
          break;
      }
      page.scanbounds = new Rectangle( 0, 0, rotatedimage.getWidth(), rotatedimage.getHeight() );

      
      double rough_dpi = (double)rotatedimage.getHeight() / 11.69;
      //System.out.println( "Rough DPI of image = " + rough_dpi );

      int big_inset, small_inset, width;
      small_inset = (int)(rough_dpi*0.1);
      width =       (int)(rough_dpi*1.5);
      big_inset =   width+small_inset;
      
      // recalculated based on rotated image
      calqrsearchrect = new Rectangle(
          rotatedimage.getWidth()-big_inset,
          rotatedimage.getHeight()-big_inset,
          width,
          width );

      // reread in the new rotated coordinates
//      calibrationresult = decodeQR( rotatedimage, calqrsearchrect );
      if ( calibrationresult == null )
      {
        page.error = "No qyouti signiture QR code in bottom right corner.";
        return page;
      }

      StringTokenizer ptok = new StringTokenizer( calibrationresult.getText(), "/" );
      ptok.nextToken();
      page.declared_calibration_width  = Double.parseDouble(ptok.nextToken()) / 10.0;
      page.declared_calibration_height = Double.parseDouble(ptok.nextToken()) / 10.0;

      // Bottom left corner
      pageqrsearchrect = getBottomLeftSearchRectangle( page, calqrsearchrect, calibrationresult );

      pageblackness = calibrationresult.getBlackness();



      //System.out.println( "Decoding page qr." );
//      pageresult = decodeQR( rotatedimage, pageqrsearchrect );
      if ( pageresult == null )
      {
        page.error = "Cannot find bottom left QRcode.";
        return page;
      }

//      page.code = pageresult.getText();
//      page.candidate_name = candidate_name;
//      page.candidate_number = candidate_number;
//      page.page_number = page_number;
//      page.source = sourcename;
//
//      QuestionMetricsRecordSet qmrset = null;
//      if ( printid!=null && printid.length()>0 )
//      {
//        qmrset = exam.qmrcache.getSet(printid);
//        if ( qmrset == null )
//        {
//          page.error = "Doesn't belong to this exam/survey.";
//          return page;
//        }
//      }
//      QuestionMetricsRecord qmr;
//
//      //System.out.println( "Done decoding page qr." );
//      if ( question_count == 0 )
//        return page;
//
//      itemqrsearchrect = getTopLeftSearchRectangle( page, pageqrsearchrect, pageresult );
//      //System.out.println( "Decoding first item qr." );
//      itemresult = decodeQR( rotatedimage, itemqrsearchrect );
//      if ( itemresult == null )
//      {
//        // another silly work around - covers and blank pages
//        // were reporting 1 question present
//        if ( question_count == 1 )
//          return page;
//        
//        page.error = "Cannot find top left QRcode.";
//        return page;
//      }
//      //System.out.println( "Processing first item QR.  [" + itemresult.getText() + "]" );
//
//      pagetransform = pageTransform( 
//          itemqrsearchrect, itemresult,
//          pageqrsearchrect, pageresult,
//          calqrsearchrect, calibrationresult,
//          page.declared_calibration_width , page.declared_calibration_height );
//      
//      try
//      {
//        revpagetransform = pagetransform.createInverse();
//      } catch (NoninvertibleTransformException ex)
//      {
//        Logger.getLogger(PageDecoder.class.getName()).log(Level.SEVERE, null, ex);
//        page.error = "Technical error.";
//        return page;
//      }
//
//
//      double last_height=0.0;
//      int w, h;
//      QuestionMetricsRecord questionmetrics=null;
//      Point2D measureditempos =new Point2D.Double();
//      Point2D measureditempos_inches = new Point2D.Double();
//      measureditempos_inches.setLocation(0.0, 0.0);
//      for ( int q=0; q<question_count; q++ )
//      {
////        System.out.println( "==================================" );
//        subimage_topleft = qrinchtopixels( pagetransform,  
//            measureditempos_inches.getX()            -metrics_top_qr_margin/100.0,
//            measureditempos_inches.getY()+last_height-metrics_top_qr_margin/100.0 );
//        subimage_bottomright = qrinchtopixels( pagetransform, 
//            measureditempos_inches.getX()             + (metrics_top_qr_width+metrics_top_qr_margin)/100.0,
//            measureditempos_inches.getY()+last_height + (metrics_top_qr_width+metrics_top_qr_margin)/100.0 );
//        if ( subimage_topleft.x < 0 ) subimage_topleft.x = 0;
//        if ( subimage_topleft.y < 0 ) subimage_topleft.y = 0;
////        System.out.println( "Look for question code here: " +
////                subimage_topleft.x     + ":" + subimage_topleft.y     + ":" +
////                subimage_bottomright.x + ":" + subimage_bottomright.y + ":"       );
//        subimage_rect = new Rectangle( 
//                 subimage_topleft,
//                 new Dimension(subimage_bottomright.x-subimage_topleft.x,subimage_bottomright.y-subimage_topleft.y) );
//        itemresult = decodeQR( rotatedimage, subimage_rect );
//        if ( itemresult == null )
//          break;
//
//        measureditempos.setLocation(
//            itemresult.getResultPoints()[1].getX()+subimage_topleft.x,
//            itemresult.getResultPoints()[1].getY()+subimage_topleft.y );
////        questiontransform = qrtransform( subimage_topleft, itemresult, metrics_top_qr_width/100.0, metrics_top_qr_width/100.0 );
//        question = new QuestionData( page );
//        questionmetrics = decodeQuestion( qmrset, userpref, itemresult );
//        if ( questionmetrics == null )
//        {
//          page.error = "Unrecognised question ID " + itemresult.getText();
//          return page;
//        }
//        //System.out.println( "Question ID " + question_code.id   );
//        //System.out.println( "       Next " + question_code.next );
////        last_height = ((double)questionmetrics.height)/100.0;
////        question.ident = questionmetrics.id;
//
//        revpagetransform.transform( measureditempos, measureditempos_inches );
//        //System.out.println( "Location: " + measureditempos_inches.getX() + "   " +measureditempos_inches.getY() );
//
//        QuestionMetricBox[] boxes =  questionmetrics.getBoxesAsArray();
//        for ( int r=0; r<boxes.length; r++ )
//        {
//          //System.out.println( "-----------------------------------" );
//          //System.out.println( "Response " + r + " at " + question_code.box_xoffset[r] + ", " + question_code.box_yoffset[r] );
//          //System.out.println( "                 W, H " + question_code.box_width[r]   + ", " + question_code.box_height[r] );
//          subimage_topleft     = qrinchtopixels(
//              pagetransform,
//              measureditempos_inches.getX() + boxes[r].x/100.0,
//              measureditempos_inches.getY() + boxes[r].y/100.0 );
//          subimage_bottomright = qrinchtopixels(
//              pagetransform,
//              measureditempos_inches.getX() + (boxes[r].x + boxes[r].width)/100.0,
//              measureditempos_inches.getY() + (boxes[r].y + boxes[r].height)/100.0
//              );
//          response = new ResponseData( question, r, boxes[r] );
//          w = subimage_bottomright.x - subimage_topleft.x;
//          h = subimage_bottomright.y - subimage_topleft.y;
//          //System.out.println( "Look for box here: " + subimage_topleft.x + " : " + subimage_topleft.y + " : " + w + " : " + h );
//          if ( response.getImageFile().exists() )
//          {
//            page.error = "Scanned same page twice?";
//            return page;
//          }
//          try
//          {
//            ImageIO.write(
//                          rotatedimage.getSubimage(subimage_topleft.x, subimage_topleft.y, w, h ),
//                          "jpg",
//                          response.getImageFile()
//                    );
//          } catch (IOException ex)
//          {
//            Logger.getLogger(PageDecoder.class.getName()).log(Level.SEVERE, null, ex);
//            page.error = "Technical error saving box image.";
//            return page;
//          }
//        }
//      }
//
//
//      page.prepareImageProcessor( qmrset.isMonochromePrint(), pageblackness, boxthreshold );
//      try
//      {
//        processBoxImages( page );
//      } catch (IOException ex)
//      {
//        Logger.getLogger(PageDecoder.class.getName()).log(Level.SEVERE, null, ex);
//        page.error = "Technical error saving filtered box image.";
//        return page;
//      }


      return page;
    } catch (Exception e ) { //ReaderException e) {
      e.printStackTrace();
      System.err.println(sourcename + ": No barcode found");
      page.error = "Can't read question QRCode.";
      return page;
    }
  }

  private void processBoxImages( PageData page ) throws IOException
  {
    QuestionData questiondata;
    QTIElementItem qti_item;
    QTIResponse[] responses;

    for ( int i=0; i<page.questions.size(); i++ )
    {
      questiondata = page.questions.get( i );
      if ( questiondata == null || questiondata.ident == null )
        continue;

      qti_item = page.exam.qdefs.qti.getItem( questiondata.ident );

      // skip questions that aren't supported
      if ( !qti_item.isSupported() )
        continue;

      // find all the response elements
      responses = qti_item.getResponses();
      for ( int j=0; j<responses.length; j++ )
      {
        if ( !responses[j].isSupported() )
          continue;
        if ( responses[j] instanceof QTIElementResponselid )
        {
          processBoxImagesForResponselid( (QTIElementResponselid)responses[j], questiondata );
          continue;
        }
        if ( responses[j] instanceof QTIExtensionRespextension )
          processBoxImagesForSketcharea( (QTIExtensionRespextension)responses[j], questiondata );
      }
    }
  }

  private void processBoxImagesForSketcharea( QTIExtensionRespextension responseext, QuestionData questiondata ) throws IOException
  {
    // At present no processing required - the image needs no special processing in
    // sketch areas
  }

  private void processBoxImagesForResponselid( QTIElementResponselid responselid, QuestionData questiondata ) throws IOException
  {
    ResponseData responsedata;
    BufferedImage temp;
    String next_darkest_ident;
    double darkest_dark_pixels, next_darkest_dark_pixels;
    double redmean;

    CannyEdgeDetector detector;

    ResponseImageProcessor responseimageprocessor = questiondata.page.responseimageprocessor;

      // reset question stats
      responseimageprocessor.startQuestion();
      // filter the images
      // Iterate the response labels in this responselid and each time
      // process the corresponding user input image
      QTIElementResponselabel[] rlabels = responselid.getResponseLabels();
      for ( int j=0; j<rlabels.length; j++ )
      {
        responsedata = questiondata.getResponseData( rlabels[j].getIdent() );
        responseimageprocessor.filter( responsedata, questiondata.page.exam.scanfolder );
      }


      // now look at image statistics to decide what the user's intended
      // response was.
      if ( responselid.isSingleChoice() )
      {
        // now processing single available option
        // low threshold for accepting a mark
        if ( responseimageprocessor.darkest_ident != null &&
                responseimageprocessor.darkest_dark_pixels > 0.01 )
        {
          // find next darkest selection
          next_darkest_ident = null;
          next_darkest_dark_pixels = 0.0;
          for ( int j=0; j<rlabels.length; j++ )
          {
            responsedata = questiondata.getResponseData( rlabels[j].getIdent() );
            if ( responsedata.ident.equals( responseimageprocessor.darkest_ident ) )
              continue;
            if ( responsedata.dark_pixels > next_darkest_dark_pixels )
            {
              next_darkest_ident = responsedata.ident;
              next_darkest_dark_pixels = responsedata.dark_pixels;
            }
          }

          // Only a clear distinct selection if no other dark box or
          // the next darkest
          // is less than 80% of the darkest
          if ( next_darkest_ident == null || (next_darkest_dark_pixels / responseimageprocessor.darkest_dark_pixels) < 0.8 )
          {
            responsedata = questiondata.getResponseData( responseimageprocessor.darkest_ident );
            responsedata.selected = true;
            responsedata.examiner_selected = true;
          }
        }
      }
      else
      {
        // processing question which can have multiple true statements
        // use simple threshold.
        for ( int j=0; j<rlabels.length; j++ )
        {
          responsedata = questiondata.getResponseData( rlabels[j].getIdent() );
          responsedata.selected = responsedata.dark_pixels > 0.05;
          responsedata.examiner_selected = responsedata.selected;
        }
      }

  }


  private Point qrinchtopixels( AffineTransform t, double x, double y )
  {
    Point2D point = new Point2D.Double( x, y );
    Point2D tpoint = t.transform(point, null);
    return new Point( (int)tpoint.getX(), (int)tpoint.getY() );
  }

  private double checkpagescale( double[] triangle )
  {
    double adx = triangle[2]-triangle[0];
    double ady = triangle[3]-triangle[1];
    double alength = Math.sqrt( (adx*adx) + (ady*ady) );
    double bdx = triangle[4]-triangle[0];
    double bdy = triangle[5]-triangle[1];
    double blength = Math.sqrt( (bdx*bdx) + (bdy*bdy) );
    return blength / alength;
  }

  private double[] calibrate( double[] triangle, double width, double height )
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



  private AffineTransform qrtransform( Point2D subimagepos, QRScanResult result, double width, double height )
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

  private AffineTransform pageTransform(
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


