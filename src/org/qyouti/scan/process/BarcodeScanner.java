/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti.scan.process;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.ReaderException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.LookupOp;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.qyouti.barcode.ZXingCodec;
import org.qyouti.barcode.ZXingResult;
import org.qyouti.scan.image.BarcodeColourLookupTable;

/**
 *
 * @author jon
 */
public class BarcodeScanner
{
  
private static int previous_threshold=120;
private static ZXingResult decodeBarcode( BufferedImage image, Rectangle[] r )
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
            result.setImageIndex(i);
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
  
  
  public static BarcodeResult scan( BufferedImage image )
  {
    int ih = image.getHeight();
    int iw = image.getWidth();
    BarcodeResult result = new BarcodeResult();
    // Bar code must be in a region near edge of page
    result.barcodesearchrect[0] = new Rectangle(       0,      0, iw/8, ih   );  // left
    result.barcodesearchrect[1] = new Rectangle(  7*iw/8,      0, iw/8, ih   );  // right
    result.barcodesearchrect[2] = new Rectangle(       0,      0, iw,   ih/8 );  // top
    result.barcodesearchrect[3] = new Rectangle(       0, 7*ih/8, iw,   ih/8 );  // bottom
    try
    {
      result.barcoderesult = decodeBarcode( image, result.barcodesearchrect );
    } catch (ReaderException ex)
    {
      Logger.getLogger(BarcodeScanner.class.getName()).log(Level.SEVERE, null, ex);
      return result;
    }
    
    if ( result.barcoderesult == null )
      return null;
    
    result.start = new Point( result.barcodesearchrect[result.barcoderesult.getImageIndex()].getLocation() );
    result.start.x += Math.round(result.barcoderesult.getResultPoints()[0].getX());
    result.start.y += Math.round(result.barcoderesult.getResultPoints()[0].getY());
    
    result.end = new Point( result.barcodesearchrect[result.barcoderesult.getImageIndex()].getLocation() );
    result.end.x += Math.round(result.barcoderesult.getResultPoints()[1].getX()); 
    result.end.y += Math.round(result.barcoderesult.getResultPoints()[1].getY());
    
    String orient = result.barcoderesult.getOrientation();
    System.out.println( "Barcode orientation: " + orient );
    if ( "270".equals( orient ) )
      result.successfulrect = 0;  //left
    else if ( "90".equals( orient ) )
      result.successfulrect = 1;  //right
    else if ( "0".equals( orient ) )
      result.successfulrect = 2;  //top
    else if ( "180".equals( orient ) )
      result.successfulrect = 3;  //bottom
    
    
    System.out.println( "Barcode = {" + result.barcoderesult.getText() + "}" );
    String code = result.barcoderesult.getText();
    StringTokenizer ptok = new StringTokenizer( code, "/" );
    try
    {
      if ( !"qyouti".equals( ptok.nextToken() ) )
        return result;        

      result.printid = ptok.nextToken();
      result.pageid = ptok.nextToken();
    }
    catch ( NoSuchElementException nsee )
    {
      return result;        
    }    
    
    return result;
  }
}
