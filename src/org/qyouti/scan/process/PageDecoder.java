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
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.LookupOp;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.imageio.ImageIO;
import org.bullseye.*;
import org.qyouti.barcode.ZXingCodec;
import org.qyouti.barcode.ZXingResult;
import org.qyouti.qti1.QTIResponse;
import org.qyouti.qti1.element.QTIElementItem;
import org.qyouti.qti1.element.QTIElementResponselabel;
import org.qyouti.qti1.element.QTIElementResponselid;
import org.qyouti.qti1.ext.qyouti.QTIExtensionRespextension;
import org.qyouti.qti1.gui.*;
import org.qyouti.scan.image.*;

/**
 *
 * @author jon
 */
public class PageDecoder
{

  //final Hashtable hints = new Hashtable();

  double boxthreshold;
  int boxinset;

  public PageDecoder( double threshold, int inset )
  {
    //hints.put( DecodeHintType.TRY_HARDER, Boolean.TRUE );
    boxthreshold = threshold;
    boxinset = inset;
  }

private int previous_threshold=120;
private ZXingResult decodeBarcode( BufferedImage image, Rectangle[] r )
          throws ReaderException
  {
    int threshold=0;
    ZXingResult result=null;
    
    //MonochromeBitmapSource source = new BufferedImageMonochromeBitmapSource( image, x1, y1, x2, y2 );
    BufferedImage[] cropped = new BufferedImage[r.length];
    BufferedImage[] img_filt = new BufferedImage[r.length];
    BarcodeColourLookupTable lookup;
    LookupOp lop;
    for ( int i=0; i<r.length; i++ )
    {
      cropped[i] = image.getSubimage(r[i].x, r[i].y, r[i].width, r[i].height);
      img_filt[i] =  new BufferedImage( cropped[i].getWidth(), cropped[i].getHeight(), BufferedImage.TYPE_3BYTE_BGR );
                                                //cropped[i].getType() );
    }
    lookup = new BarcodeColourLookupTable( image.getColorModel().getNumComponents() );
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
          result = ZXingCodec.decode( BarcodeFormat.CODE_128, img_filt[i] );
          if ( result != null )
          {
            if ( n != 0 )
            {
              previous_threshold = threshold;
            }
            if ( result.getText() == null ) continue;
            if ( !result.getText().startsWith("qyouti/") ) continue;
            System.out.println( "DECODED qyouti barcode at threshold = " + threshold + " in rectangle " + i );
            return result;
          }
        } catch (Exception ex)
        {
          //Logger.getLogger(PageDecoder.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
    }
    return null;
  }



/**
 * 
 * @param exam  Data about this examination
 * @param image Bitmap image of a page
 * @param sourcename Original file name of page
 * @param scanorder Position in list of scanned pages
 * @return
 * @throws IOException 
 */  
  public PageData identifyPage( ExaminationData exam, ImageFileData ifd, BufferedImage image )
          throws IOException
  {
    int i;
    PageData page=null;
    ZXingResult barcoderesult;
    Rectangle r;
    Rectangle[] barcodesearchrect = new Rectangle[4];
    int ih = image.getHeight();
    int iw = image.getWidth();
    Point[] points;
    Point bull_tl=null, bull_bl=null, bull_br=null;
    double bradius_pixels = 1;
    
    String code;
    String printid;
    String pageid;
    
    // Initialise the page record that this method builds up
    //page = new PageData( exam, ifd.getImportedname() );

    // Look for the bar code in a strip down the left side or in case
    // the scan was made with a rotation look in strips on the other
    // three edges
    try
    {
      // Bar code must be in a region near edge of page
      barcodesearchrect[0] = new Rectangle(       0,      0, iw/8, ih   );  // left
      barcodesearchrect[1] = new Rectangle(  7*iw/8,      0, iw/8, ih   );  // right
      barcodesearchrect[2] = new Rectangle(       0,      0, iw,   ih/8 );  // top
      barcodesearchrect[3] = new Rectangle(       0, 7*ih/8, iw,   ih/8 );  // bottom
      
      barcoderesult = decodeBarcode( image, barcodesearchrect );
      if ( barcoderesult == null )
      {
        ifd.setError( "No barcode found on this image." );
        return null;
      }

      System.out.println( "Barcode orientation: " + barcoderesult.getOrientation() );
      
      System.out.println( "Barcode = {" + barcoderesult.getText() + "}" );
      code = barcoderesult.getText();
      StringTokenizer ptok = new StringTokenizer( code, "/" );
      try
      {
        if ( !"qyouti".equals( ptok.nextToken() ) )
        {
          ifd.setError( "Non-qyouti barcode found '" + code + "'." );
          return null;        
        }                
        printid = ptok.nextToken();
        pageid = ptok.nextToken();
      }
      catch ( NoSuchElementException nsee )
      {
        ifd.setError( "Unable to parse text in the barcode '" + code + "'." );
        return null;        
      }
      
      if ( !printid.equals( exam.getLastPrintID() ) )
      {
        ifd.setError( "The printid in the barcode does not match this exam/survey '" + printid + "'." );
        return null;                
      }
      
      // now we can look up the PrintData object...
      page = exam.lookUpPage( pageid );
      if ( page == null )
      {
        ifd.setError( "Unable to find the page in the print record " + pageid + "'." );
        return null;                
      }      
      
      if ( page.scanned )
      {
        ifd.setError( "A scan for this page ID has already been imported " + pageid + "'." );
        return null;                
      }      
      page.scanned=true;
      
      // use printid and pageid to get information about the page...
      page.examfolder = exam.examcatalogue.getExamFolderFromPrintMetric( page.printid );
      page.paginationfile = new File( page.examfolder, "pagination_" + page.printid + ".xml" );              
      //page.source = sourcename;

      if ( !page.paginationfile.exists() && !page.paginationfile.isFile() )
      {
        ifd.setError( "Cannot find the pagination data file for this page." );
        return null;
      }
      
      PaginationRecord paginationrecord = exam.examcatalogue.getPrintMetric( page.printid );
      PaginationRecord.Candidate prcandidate = paginationrecord.getCandidate( page.pageid );
      PaginationRecord.Page prpage = paginationrecord.getPage( page.pageid );
      double[] caldim = prpage.getCalibrationDimension();
      
      if ( caldim == null )
      {
        page.error = "Cannot calibrate page.";
        ifd.setError( page.error );
        return page;
      }
      
      page.candidate = exam.candidates.get( prcandidate.getId() );
      page.candidate_number = page.candidate.id;        
      page.candidate_name = page.candidate.name;      
      page.declared_calibration_width  = caldim[0];
      page.declared_calibration_height = caldim[1];

      
      // Now is the time to flip/rotate the image.
      if ( "270".equals( barcoderesult.getOrientation() ) )
      {
        page.rotatedimage = image;
      }
      else
      {
        PageRotator rot = new PageRotator( image );
        if ( "90".equals( barcoderesult.getOrientation() ) )
          page.rotatedimage = rot.rotate180();
        else if ( "0".equals( barcoderesult.getOrientation() ) )        
          page.rotatedimage = rot.rotate90();
        else if ( "180".equals( barcoderesult.getOrientation() ) )        
          page.rotatedimage = rot.rotate270();
        else
        {
          page.error = "Unable to reorient image. (Barcode at " + barcoderesult.getOrientation() + " degrees.)";
          ifd.setError( page.error );
          return page;          
        }
//        ImageIO.write(
//                    page.rotatedimage,
//                    "jpg",
//                    new File( exam.getExamFolder(), "debug_rotation_" + page.printid + "_" + page.pageid + "_" + page.candidate_number + ".jpg" )
//              );        
      }
      ih = page.rotatedimage.getHeight();
      iw = page.rotatedimage.getWidth();
      page.scanbounds = new Rectangle( 0, 0, page.rotatedimage.getWidth(), page.rotatedimage.getHeight() );


      PaginationRecord.Bullseye[] bullseyerecord = new PaginationRecord.Bullseye[3];
      
      bullseyerecord[0] = prpage.getBullseye( PaginationRecord.Bullseye.BULLSEYE_TOP_LEFT );
      bullseyerecord[1] = prpage.getBullseye( PaginationRecord.Bullseye.BULLSEYE_BOTTOM_LEFT );
      bullseyerecord[2] = prpage.getBullseye( PaginationRecord.Bullseye.BULLSEYE_BOTTOM_RIGHT );
      if ( bullseyerecord[0] == null || bullseyerecord[1] == null || bullseyerecord[2] == null )
      {
        page.error = "Printed page lacked info about calibration bullseyes.";
        ifd.setError( page.error );
        return page;
      }

      double approxpixelspercentiinch =  page.rotatedimage.getWidth() / (double)prpage.getWidth();
      for ( i=0; i<3; i++ )
      {
        bradius_pixels = (double)bullseyerecord[i].getR() * approxpixelspercentiinch;
        Point approxcentre = new Point( 
                (int)((double)bullseyerecord[i].getX() * approxpixelspercentiinch), 
                (int)((double)bullseyerecord[i].getY() * approxpixelspercentiinch)   
              );
        
        System.out.println( "Trial bullseye radius = " + bradius_pixels + " pixels." );
        r = new Rectangle( approxcentre );
        r.grow( (int)(bradius_pixels*3.5), (int)(bradius_pixels*3.5) );
        r = r.intersection( page.scanbounds );
        BufferedImage searchimage = page.rotatedimage.getSubimage( r.x, r.y, r.width, r.height );

        
        BullseyeLocator bloc = new BullseyeLocator( searchimage, bradius_pixels, BullseyeGenerator.RADII );
        points = bloc.locateBullseye();


        if ( points.length != 1 )
        {
          page.error = "Failed to find bullseye in corner " + (i+1) + " of page.";
          ifd.setError( page.error );
          try
          {
            ImageIO.write(
                        searchimage,
                        "jpg",
                        new File( exam.getExamFolder(), "debug_bullseye_" + i + "_" + page.printid + "_" + page.pageid + "_" + page.candidate_number + ".jpg" )
                  );
            ImageIO.write(
                        bloc.getVoteMapImage(),
                        "jpg",
                        new File( exam.getExamFolder(), "debug_bullseye_votes_" + i + "_" + page.printid + "_" + page.pageid + "_" + page.candidate_number + ".jpg" )
                  );          } catch (IOException ex)
          {
            Logger.getLogger(PageDecoder.class.getName()).log(Level.SEVERE, null, ex);
            //page.error = "Technical error saving debug image.";
            //ifd.setError( page.error );
          }
          return page;
        }
        points[0].translate( r.x, r.y );
        if ( i == 0 )
          bull_tl = points[0];
        if ( i == 1 )
          bull_bl = points[0];
        if ( i == 2 )
          bull_br = points[0];
        
      }
      
      if ( false )
      {
        page.error = "Done.";
        ifd.setError( page.error );
        return page;
      }
      
      // Use a bullseye area to measure blackest and whitest pixels
      // 
        
      int nrad = Math.round( (float)bradius_pixels );
      r = new Rectangle( bull_tl );
      r.grow( nrad, nrad );
      r = r.intersection( page.scanbounds );
      BufferedImage bullseyeimage = page.rotatedimage.getSubimage( r.x, r.y, r.width, r.height );
      BufferedImageStats stats = new BufferedImageStats( bullseyeimage );
      
      //page.blackness = calibrationresult[qrlocation[1]].getBlackness();
      
      page.pagetransform = pageTransform(
              bull_tl, bull_bl, bull_br,
              page.declared_calibration_width , page.declared_calibration_height );
      
      Point origin = inchesToPixels( page.pagetransform, 0.0, 0.0 );
      Point inch = inchesToPixels( page.pagetransform, 1.0, 0.0 );
      double dx = inch.x - origin.x;
      double dy = inch.y - origin.y;
      page.dpi = Math.sqrt( dx*dx + dy*dy );
      
      System.out.println( "Scanned page DPI = " + page.dpi );
    }
    catch (ReaderException e)
    {
      e.printStackTrace();
      System.err.println( ifd.getSource() + ": No barcode found");
      page.error = "Can't read bar code.";
      ifd.setError( page.error );
    }

    return page;
  }



  public PageData decode( ExaminationData exam, ImageFileData ifd, BufferedImage image )
          throws IOException
  {
    int i;
    PageData page;
    QuestionData question;
    ResponseData response;
    Point subimage_topleft;
    Point subimage_bottomright;
    int w, h;
    boolean debug = false;
    BufferedImage debug_image=null;
    Graphics2D debug_graphics=null;
    
    System.out.println( "Decoding a page." );

    // identify the page first (which includes calibrating
    // coordinates too.) and if this fails give up
    page = identifyPage( exam, ifd, image );
    if ( ifd.getError() != null )
      return null;  
    
    if ( false )
      return page;
    
    if ( debug )
    {
      debug_image = new BufferedImage( page.rotatedimage.getWidth(), page.rotatedimage.getHeight(), page.rotatedimage.getType() );
      debug_graphics = debug_image.createGraphics();
      debug_graphics.drawImage( page.rotatedimage, 0, 0, null );
    }

    if ( debug )
    {
      // indicate the calibration with two vectors
      debug_graphics.setColor( Color.red );
      debug_graphics.drawLine( 
              (int)page.pagetransform.origin_x, 
              (int)page.pagetransform.origin_y, 
              (int)(page.pagetransform.origin_x + 8.0*page.pagetransform.vertical_dx), 
              (int)(page.pagetransform.origin_y + 8.0*page.pagetransform.vertical_dy)  );      
      debug_graphics.drawLine( 
              (int)(page.pagetransform.origin_x + 8.0*page.pagetransform.vertical_dx), 
              (int)(page.pagetransform.origin_y + 8.0*page.pagetransform.vertical_dy),  
              (int)(page.pagetransform.origin_x + 8.0*page.pagetransform.vertical_dx + 5.0*page.pagetransform.horizontal_dx ), 
              (int)(page.pagetransform.origin_y + 8.0*page.pagetransform.vertical_dy + 5.0*page.pagetransform.horizontal_dy )  );
      debug_graphics.setColor( Color.green );
      Point a = inchesToPixels( page.pagetransform, 0.0, 8.0 );
      Point b = inchesToPixels( page.pagetransform, 5.0, 8.0 );
      debug_graphics.drawLine( 
              (int)page.pagetransform.origin_x, 
              (int)page.pagetransform.origin_y, 
              a.x, a.y );
      debug_graphics.drawLine( 
              a.x, a.y, b.x, b.y );
    }

    PaginationRecord pr = exam.examcatalogue.getPrintMetric( page.printid );
    PaginationRecord.Page prpage = pr.getPage( page.pageid );
    double[] offset = prpage.getItemOffset();
    if ( offset == null )
    {
        page.error = "Calibration failed.";
        return page;
    }
    PaginationRecord.Item[] items = prpage.getItems();

    QuestionMetricsRecord questionmetrics=null;
//    Point2D measureditempos =new Point2D.Double();
//    Point2D measureditempos_inches = new Point2D.Double();
    
    for ( int q=0; q<items.length; q++ )
    {
      question = new QuestionData( page );
      question.setImagesProcessed( false );
      question.ident = items[q].getIdent();
      questionmetrics = items[q].getQuestionMetricsRecord();

      // Save an image of the whole question
      subimage_topleft     = inchesToPixels(
            page.pagetransform,
            items[q].getX()/100.0,
            items[q].getY()/100.0 );
      subimage_bottomright = inchesToPixels(
            page.pagetransform,
            (items[q].getX() + items[q].getWidth())/100.0,
            (items[q].getY() + items[q].getHeight())/100.0
            );
      w = subimage_bottomright.x - subimage_topleft.x;
      h = subimage_bottomright.y - subimage_topleft.y;
      
      try
      {
        BufferedImage qimage = page.rotatedimage.getSubimage(subimage_topleft.x, subimage_topleft.y, w, h );
        float scale = 100.0f / (float)page.dpi;  // convert to 100 dpi image
        ImageIO.write(
                      ImageResizer.resize( 
                              qimage, 
                              Math.round( qimage.getWidth()*scale ), 
                              Math.round( qimage.getHeight()*scale ) ),
                      "jpg",
                      question.getImageFile()
                );
      } catch (Exception ex)
      {
        Logger.getLogger(PageDecoder.class.getName()).log(Level.SEVERE, null, ex);
        page.error = "Technical error saving question image.";
        return page;
      }

      // pull out images of the pink boxes that were printed on this page
      QuestionMetricBox[] boxes =  questionmetrics.getBoxesAsArray();
      for ( int r=0; r<boxes.length; r++ )
      { 
        subimage_topleft     = inchesToPixels(
            page.pagetransform,
            (items[q].getX() + boxes[r].x)/100.0,
            (items[q].getY() + boxes[r].y)/100.0 );
        subimage_bottomright = inchesToPixels(
            page.pagetransform,
            (items[q].getX() + boxes[r].x + boxes[r].width)/100.0,
            (items[q].getY() + boxes[r].y + boxes[r].height)/100.0
            );
        response = new ResponseData( question, r, boxes[r] );
        w = subimage_bottomright.x - subimage_topleft.x;
        h = subimage_bottomright.y - subimage_topleft.y;
        response.setImageWidth( w );
        response.setImageHeight( h );
        
        
        //System.out.println( "Look for box here: " + subimage_topleft.x + " : " + subimage_topleft.y + " : " + w + " : " + h );
//        if ( response.getImageFile().exists() )
//        {
//          page.error = "Scanned same page twice?";
//          return page;
//        }
        try
        {
          ImageIO.write(
                        page.rotatedimage.getSubimage(subimage_topleft.x, subimage_topleft.y, w, h ),
                        "jpg",
                        response.getImageFile()
                  );
        } catch (IOException ex)
        {
          Logger.getLogger(PageDecoder.class.getName()).log(Level.SEVERE, null, ex);
          page.error = "Technical error saving box image.";
          return page;
        }
      }
    }
   
    /*
    // now ready to try and interpret the images of the pink boxes
    //page.prepareImageProcessor( qmrset.isMonochromePrint(), page.blackness, boxthreshold );
    page.prepareImageProcessor( false, page.blackness, boxthreshold );
    try
    {
      processBoxImages( page );
    } catch (IOException ex)
    {
      Logger.getLogger(PageDecoder.class.getName()).log(Level.SEVERE, null, ex);
      page.error = "Technical error processing filtered box images.";
      return page;
    }
    */
    
    if ( debug )
    {
      debug_graphics.dispose();
      try
      {
        ImageIO.write(
                      debug_image,
                      "jpg",
                      new File( exam.getExamFolder(), "debug_" + page.printid + "_" + page.pageid + "_" + page.candidate_number + ".jpg" )
                );
      } catch (IOException ex)
      {
        Logger.getLogger(PageDecoder.class.getName()).log(Level.SEVERE, null, ex);
        page.error = "Technical error saving debug image.";
        return page;
      }
    }
    return page;
  }

  public void processBoxImages( ExaminationData exam )
          throws IOException
  {
    int i, j, k;
    PageData page;
    QuestionData q;
    ResponseData r;
    int maxw=0, maxh=0;
    QTIElementItem qti_item;

    
    for ( i=0; i<exam.getPageCount(); i++ )
    {
      page = exam.getPage( i );
      if ( page.error != null )
        continue;
      for ( j=0; j<page.questions.size(); j++ )
      {
        q = page.questions.get( j );
        if ( q.areImagesProcessed() )
          continue;
        for ( k=0; k<q.responsedatas.size(); k++ )
        {
          r = q.responsedatas.get( k );
          if ( "response_label".equals( r.type ) )
          { 
            if ( r.getImageWidth() > maxw ) maxw = r.getImageWidth();
            if ( r.getImageHeight() > maxh ) maxh = r.getImageHeight();
          }
        }
      }
    }

    XLocator xlocator = new XLocator( maxw, maxh );
    xlocator.setDebugLevel( 2 );
    for ( i=0; i<exam.getPageCount(); i++ )
    {
      page = exam.getPage( i );
      if ( page.error != null )
        continue;
      processBoxImages( page, xlocator );
    }
  }
  
  
  private void processBoxImages( PageData page, XLocator xlocator ) throws IOException
  {
    QuestionData questiondata;
    QTIElementItem qti_item;
    QTIResponse[] responses;

    for ( int i=0; i<page.questions.size(); i++ )
    {
      questiondata = page.questions.get( i );
      if ( questiondata == null || questiondata.ident == null || questiondata.areImagesProcessed() )
        continue;
      questiondata.setImagesProcessed( true );
      
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
          processBoxImagesForResponselid( (QTIElementResponselid)responses[j], questiondata, xlocator );
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

  private void processBoxImagesForResponselid( 
          QTIElementResponselid responselid, 
          QuestionData questiondata, 
          XLocator xlocator )
          throws IOException
  {
    ResponseData responsedata;

    QTIElementResponselabel[] rlabels = responselid.getResponseLabels();
    File[] filelist = new File[rlabels.length];
    XLocatorReport[] reports = new XLocatorReport[rlabels.length];
    ArrayList<BufferedImage> debugimages = new ArrayList<BufferedImage>();
    
    for ( int j=0; j<rlabels.length; j++ )
    {
      responsedata = questiondata.getResponseData( rlabels[j].getIdent() );
      filelist[j] = responsedata.getImageFile();
    }

    xlocator.setImageFiles( filelist );
    XLocatorListener listener = new XLocatorListener()
      {
        @Override
        public void notifyProgress( int percentage )
        {
        }

        @Override
        public void notifyComplete( XLocatorReport report, int i )
        {
          reports[i] = report;
        }

        @Override
        public void notifyNewDebugImage( BufferedImage image, int i )
        {
          debugimages.add( image );
        }
    };
    
    xlocator.addProgressListener( listener );
    xlocator.run();
    xlocator.removeProgressListener( listener );

    if ( false )
    {
      for ( int i=0; i<debugimages.size(); i++ )
      {
        try
        {
          ImageIO.write(
                        debugimages.get( i ),
                        "jpg",
                        new File( questiondata.getImageFile().getParentFile(), questiondata.ident + "_debug_" + i + ".jpg" ) );
        } catch (IOException ex)
        {
          Logger.getLogger(PageDecoder.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
    }
    
    int count=0;
    for ( int j=0; j<rlabels.length; j++ )
    {
      responsedata = questiondata.getResponseData( rlabels[j].getIdent() );
      responsedata.debug_message = reports[j].toString();
      responsedata.candidate_selected = reports[j].hasX();
      responsedata.examiner_selected = false;
      
      if ( responsedata.candidate_selected )
      {
        count++;
        // Perhaps not a simple X
        if ( reports[j].getAdditionalPointsofInterest().size() != 0 )
        {
          responsedata.needsreview = true;
          questiondata.needsreview = true;        
        }
      }
      else
      {
        // No X but not blank either
        if ( reports[j].getPercentageCentreEdgePixels() > 5.0 )
        {
          responsedata.needsreview = true;
          questiondata.needsreview = true;
        }
      }
    }
    
    // More than one X but only one choice allowed
    if ( responselid.isSingleChoice() && count > 1 )
    {
      questiondata.needsreview = true;    
      for ( int j=0; j<rlabels.length; j++ )
      {
        responsedata = questiondata.getResponseData( rlabels[j].getIdent() );
        if ( responsedata.candidate_selected )
          responsedata.needsreview = true;
      }
    }
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
        responseimageprocessor.filter( responsedata, questiondata.page.exam.getResponseImageFolder() );
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
            responsedata.candidate_selected = true;
            //responsedata.examiner_selected = true;
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
          responsedata.candidate_selected = responsedata.dark_pixels > 0.05;
          //responsedata.examiner_selected = responsedata.candidate_selected;
        }
      }

  }

  /**
   * 
   * @param t Calibration information
   * @param x distance horizontally from top left bullseye in inches
   * @param y ditto vertically
   * @return 
   */
  private Point inchesToPixels( TransformData t, double x, double y )
  {
    double pixx, pixy;
    
    pixx = t.origin_x + x*t.horizontal_dx + y*t.vertical_dx;
    pixy = t.origin_y + x*t.horizontal_dy + y*t.vertical_dy;
            
    return new Point( Math.round( (float)pixx ), Math.round( (float)pixy ) );
  }


  /**
   * Records origin in image and scaling vectors for horizontal
   * and vertical.
   * 
   * @param resulta
   * @param resultb
   * @param resultc
   * @param width
   * @param height
   * @return 
   */
  private TransformData pageTransform(
      Point resulta,        // top left  (pixels)
      Point resultb,        // bottom left
      Point resultc,        // bottom right
      double width,         // distance in inches from br to bl
      double height         // distance in inches from tl to bl
      )
  {
    TransformData tdata = new TransformData();
    
    tdata.origin_x = resulta.x;
    tdata.origin_y = resulta.y;

    // measure input vectors - pixel units
    // Calc vectors br to bl and bl to tl
    tdata.vertical_dx   = resultb.getX() - resulta.getX();
    tdata.vertical_dy   = resultb.getY() - resulta.getY();

    tdata.horizontal_dx = resultc.getX() - resultb.getX();
    tdata.horizontal_dy = resultc.getY() - resultb.getY();

    // convert vectors to pixels per inch
    tdata.vertical_dx   = tdata.vertical_dx   / height;
    tdata.vertical_dy   = tdata.vertical_dy   / height;

    tdata.horizontal_dx = tdata.horizontal_dx / width;
    tdata.horizontal_dy = tdata.horizontal_dy / width;

    return tdata;
  }


  public class TransformData
  {
    double origin_x, origin_y;             // pixel coordinates in image
    double horizontal_dx, horizontal_dy;   // pixels per inch
    double vertical_dx,   vertical_dy;     // pixels per inch
  }
}


