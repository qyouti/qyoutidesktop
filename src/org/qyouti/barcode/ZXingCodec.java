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
package org.qyouti.barcode;

import com.google.zxing.*;
import com.google.zxing.aztec.*;
import com.google.zxing.aztec.encoder.AztecCode;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.*;

import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.ByteMatrix;
//import com.google.zxing.qrcode.encoder.Encoder;
import com.google.zxing.qrcode.encoder.QRCode;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.xml.parsers.*;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.util.*;
import org.w3c.dom.*;



/**
 *
 * @author jon
 */
public class ZXingCodec
{
  private static DocumentBuilder docbuilder = null;
  //private static EncodeBitArray encoder;
  private static int n = 1000;
  private static boolean failed=false;
  private static ErrorCorrectionLevel error_correction = ErrorCorrectionLevel.L;
  private static Random random = new Random();

  private static JLabel debugImageLabel=null;



  public static void setDebugImageLabel( JLabel label )
  {
    debugImageLabel = label;
  }

  public static void init()
  {
    if ( docbuilder != null )
      return;

    try
    {
      docbuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      //encoder = new EncodeBitArray();
      //encoder.qrcodeVersion = 5;
    } catch (Exception ex)
    {
      docbuilder = null;
    }
  }


  static BitMatrix byteMatrixToBitMatrix( ByteMatrix bytematrix )
  {
    BitMatrix bitmatrix = new BitMatrix( bytematrix.getWidth(), bytematrix.getHeight() );
    bitmatrix.clear();
    for ( int x=0; x<bitmatrix.getWidth(); x++ )
      for ( int y=0; y<bitmatrix.getHeight(); y++ )
      {
        if ( bytematrix.get( x, y ) != 0 )
          bitmatrix.set( x, y );
      }
    return bitmatrix;
  }
  
  static BitMatrix encode2DBitMatrix( String content, String encoding )
  {
    Result result;
    QRCode qrcode;
    AztecCode azteccode;
    ByteMatrix bytematrix;
    Hashtable<EncodeHintType,String> hints = new Hashtable<EncodeHintType,String>();
    hints.put( EncodeHintType.CHARACTER_SET, encoding );
    char[] extra = {'1'};

    try
    {
        if ( true )
        {
          byte[] data = content.getBytes( encoding );
          azteccode = com.google.zxing.aztec.encoder.Encoder.encode( data );
          return azteccode.getMatrix();
        }
        
      do
      {
        qrcode = com.google.zxing.qrcode.encoder.Encoder.encode( new String( extra ) + content, error_correction, hints);
        bytematrix = qrcode.getMatrix();
        if ( false )
          return byteMatrixToBitMatrix( bytematrix );
        // but does it decode easily?
        result = null;
        try
        {
          result = decode( byteMatrixToBitMatrix( bytematrix ) );
        }
        catch ( Exception innere )
        {
          innere.printStackTrace();
        }
        extra[0]++;
        if ( extra[0] == ':' )
          throw new IllegalArgumentException( "Unable to make easily readable QR code." );
      }
      while ( result == null );

      return byteMatrixToBitMatrix( bytematrix );
      //System.out.println( "Encoded in " + qrcode.getMode().getName() );
    } catch (Exception e)
    {
      e.printStackTrace();
    }

    return null;
  }

  static BitMatrix encode1DBitMatrix( String content, String encoding )
  {
    // encoding is ignored!!!!
    
    HashMap<EncodeHintType,String> hints = new HashMap<>();
    Code128Writer codewriter = new Code128Writer();
    try
    {
      // Set width to 1 means output width will be minimum possible width
      // Set height to 1 means we get single row of pixels
      return codewriter.encode( content, BarcodeFormat.CODE_128, 1, 1, hints );
    } catch (Exception e)
    {
      e.printStackTrace();
    }

    return null;
  }


  public static BufferedImage encode2DImage(String content)
  {
    BitMatrix bitmatrix = encode2DBitMatrix( content, "UTF8" );
    if ( bitmatrix == null ) return null;
    return makeImage( bitmatrix );
  }

  static BufferedImage makeImage( BitMatrix matrix )
  {
    int margin = 10;
    int pixelwidth = 3;

    BufferedImage image = new BufferedImage(
            (matrix.getWidth() + margin * 2) * pixelwidth,
            (matrix.getHeight() + margin * 2) * pixelwidth,
            BufferedImage.TYPE_INT_RGB);

    // Fill image with white
    for (int x = 0; x < image.getWidth(); x++)
    {
      for (int y = 0; y < image.getHeight(); y++)
      {
        image.setRGB(x, y, 0xffffffff);
      }
    }

    // Paint the black blocks
    for (int i = 0; i < matrix.getWidth(); i++)
    {
      for (int j = 0; j < matrix.getHeight(); j++)
      {
        if ( matrix.get( i, j ) )  // make a black rectangle
        {
          for (int x = (i * pixelwidth) + margin * pixelwidth; x < (i * pixelwidth) + margin * pixelwidth + pixelwidth; x++)
          {
            for (int y = (j * pixelwidth) + margin * pixelwidth; y < (j * pixelwidth) + margin * pixelwidth + pixelwidth; y++)
            {
              image.setRGB(x, y, 0xff000000);
            }
          }
        }
      }
    }

    return image;
  }

  public static Element encode2DSVG( String content, double width )
  {
    BitMatrix matrix = encode2DBitMatrix( content, "UTF8" );
    if ( matrix == null ) return null;
    return makeSVG( matrix, matrix.getWidth()/2, matrix.getHeight()/2, width, width );
  }

  public static Element encode1DSVG( String content, double width, double height )
  {
    BitMatrix matrix = encode1DBitMatrix( content, "UTF8" );
    if ( matrix == null ) return null;
    return makeSVG( matrix, matrix.getWidth()/2, matrix.getHeight()/2, width, height );
  }


  static Element makeSVG( BitMatrix matrix, int refpixx, int refpixy, double width, double height )
  {
    init();

    // for QR code zero point is centre of top left position mark
    // selected width measures to the centre of the top right position mark
    //double mark_size = width / ((double)matrix.getWidth() - 7);  // 100th of Inch

    // For aztec code and 1D barcodes zero point is the centre of the code
    double mark_w =  width / (double)matrix.getWidth();
    double mark_h = height / (double)matrix.getHeight();

    DOMImplementation impl = GenericDOMImplementation.getDOMImplementation();
    String svgNS = SVGConstants.SVG_NAMESPACE_URI;
    Document doc = impl.createDocument(svgNS, "svg", null);
    Element g = doc.createElementNS(svgNS, "g");
    g.setAttribute( "id", "qrcode_" + n++ );
    Element whiterect = doc.createElementNS(svgNS,"rect");
    whiterect.setAttribute("x", Double.toString(((double)-refpixx - 0.5) * mark_w ) );
    whiterect.setAttribute("y", Double.toString(((double)-refpixy - 0.5) * mark_h ) );
    whiterect.setAttribute( "stroke", "none" );
    whiterect.setAttribute( "fill", "rgb(255,255,255)" );
    whiterect.setAttribute("width",  Double.toString(matrix.getWidth()  * mark_w ) );
    whiterect.setAttribute("height", Double.toString(matrix.getHeight() * mark_h ) );
    g.appendChild( whiterect );
    g.appendChild( doc.createTextNode( "\n" ) );

    Element mark;

    for (int i = 0; i < matrix.getWidth(); i++)
    {
      for (int j = 0; j < matrix.getHeight(); j++)
      {
        if (matrix.get( i, j ) )  // make a black rectangle
        {
          mark = doc.createElementNS(svgNS, "rect" );
          mark.setAttribute("x", Double.toString(((double)(i - refpixx) - 0.5) * mark_w ) );
          mark.setAttribute("y", Double.toString(((double)(j - refpixy) - 0.5) * mark_h ) );
          mark.setAttribute("width",  Double.toString(mark_w * 1.1 ) );
          mark.setAttribute("height", Double.toString(mark_h * 1.1 ) );
          mark.setAttribute( "stroke", "black" );
          mark.setAttribute( "stroke-width", "0.0" );
          mark.setAttribute( "fill", "black" );
//          if ( i==refpixx && j==refpixy )
//            mark.setAttribute( "fill", "green" );
          g.appendChild( mark );
          g.appendChild( doc.createTextNode( "\n" ) );
        }
      }
    }

    //System.err.println( "QR - end");
    return g;
  }




  public static void setErrorCorrection( ErrorCorrectionLevel ec )
  {
    error_correction = ec;
  }




  public static String decodeToString( BarcodeFormat format, BufferedImage image )
          throws ReaderException, UnsupportedEncodingException
  {
    ZXingResult result = decode( format, image );
    if ( result == null ) return null;
    return result.getText();
  }


  public static ZXingResult decode( BarcodeFormat format, BufferedImage image )
          throws ReaderException, UnsupportedEncodingException
  {
    BufferedImageLuminanceSource source = new BufferedImageLuminanceSource( image );
    if ( debugImageLabel != null )
      debugImageLabel.setIcon( new ImageIcon( image ) );
    if ( format.equals( BarcodeFormat.QR_CODE ) )
      return decodeWithTwist( source );
    if ( format.equals( BarcodeFormat.CODE_128 ) )
      return decodeWithoutTwist( format, source );
    if ( format.equals( BarcodeFormat.AZTEC ) )
      return decodeWithoutTwist( format, source );
    throw new IllegalArgumentException("Unsupported barcode format.");
  }


  private static ZXingResult decodeWithTwist( BufferedImageLuminanceSource source )
          throws ReaderException, UnsupportedEncodingException
  {
    LuminanceSource lsource;
    Result result=null;
    Hashtable<DecodeHintType,Object> hints = new Hashtable<DecodeHintType,Object>();
    hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);

//    LocalBlockBinarizer binarizer = new LocalBlockBinarizer( source );
    GlobalHistogramBinarizer binarizer;
    BinaryBitmap bitmap;

    lsource = source;
    int twist;
    result = null;
    for ( twist=0; twist<4; twist++ )
    {
      binarizer = new GlobalHistogramBinarizer( lsource );
      bitmap = new BinaryBitmap( binarizer );
      try
      {
        result = new QRCodeReader().decode( bitmap, hints );
      }
      catch ( ReaderException re )
      {
      }
      if ( result != null )
        break;

      //System.out.println( "QR not found - trying 90deg rotation." );
      lsource = lsource.rotateCounterClockwise();
    }

    if ( result == null )
    {
      //System.out.println( "QR still not found." );
      return null;
    }

    return new ZXingResult( source, result, twist );
  }

  
  private static ZXingResult decodeWithoutTwist( BarcodeFormat format, BufferedImageLuminanceSource source )
          throws ReaderException, UnsupportedEncodingException
  {
    LuminanceSource lsource;
    Result result;
    HashMap<DecodeHintType,Object> hints = new HashMap<>();
    hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);

    GlobalHistogramBinarizer binarizer;
    BinaryBitmap bitmap;

    lsource = source;
    result = null;

    binarizer = new GlobalHistogramBinarizer( lsource );
    bitmap = new BinaryBitmap( binarizer );
    try
    {
      if ( format.equals( BarcodeFormat.CODE_128 ) )
        result = new Code128Reader().decode( bitmap, hints );
      else if ( format.equals( BarcodeFormat.AZTEC ) )
        result = new AztecReader().decode( bitmap, hints );
      else
        throw new IllegalArgumentException("Unsupported barcode format.");
    }
    catch ( ReaderException re )
    {
    }

    if ( result == null )
      return null;

    return new ZXingResult( source, result, 0 );
  }
  
  
  public static double calibrateBlack( BufferedImage image, ResultPoint[] points )
  {
    int i, j, k, x, y, rgb;
    double total=0.0;

    for ( i=0; i<points.length; i++ )
      for ( j=-1; j<=1; j++ )
        for ( k=-1; k<=1; k++ )
        {
          x = (int)points[i].getX() + j;
          y = (int)points[i].getY() + k;
          rgb = image.getRGB( x, y );
          total += (double)((rgb >> 16) & 0xff);
        }

    return total / (9.0 * points.length);
  }


  /**
   * Makes an image from a freshly generated qrcode and decodes it.
   * Used to check the readability of generated pattern.
   * @param qrcode
   * @return
   * @throws com.google.zxing.ReaderException
   * @throws java.io.UnsupportedEncodingException
   */
  private static Result decode( BitMatrix qrcode )
          throws ReaderException, UnsupportedEncodingException
  {
    BufferedImage image = makeImage( qrcode );

    Hashtable<DecodeHintType,Object> hints = new Hashtable<DecodeHintType,Object>();
    BufferedImageLuminanceSource source = new BufferedImageLuminanceSource( image );
//    LocalBlockBinarizer binarizer = new LocalBlockBinarizer( source );
    GlobalHistogramBinarizer binarizer = new GlobalHistogramBinarizer( source );
    BinaryBitmap bitmap = new BinaryBitmap( binarizer );
    Result result;

    result = new QRCodeReader().decode( bitmap, hints );

    if (result == null)
    {
      //System.out.println("No QR Code found.");
      return null;
    }

    return result;
  }


  public static BufferedImage dirtyUpImage( BufferedImage cleanimage )
  {
    BufferedImage dirtyimage = new BufferedImage(
              cleanimage.getWidth()*4,
              cleanimage.getHeight()*4,
              cleanimage.getType() );
    Graphics2D g = dirtyimage.createGraphics();
    AffineTransform xform = AffineTransform.getScaleInstance( 4.0, 4.0 );
    g.drawImage( cleanimage, xform, null );

    // do 0.1% of pixels
    int x, y, rgb;
    for ( int i=0; i< (dirtyimage.getWidth() * dirtyimage.getHeight() / 1000 ); i++ )
    {
      x = (random.nextInt() & 0xfffffff) % dirtyimage.getWidth();
      y = (random.nextInt() & 0xfffffff) % dirtyimage.getHeight();
      rgb = dirtyimage.getRGB( x, y );
      if ( rgb == 0xff000000 )
        dirtyimage.setRGB( x, y, rgb  ^ 0xffffff );
    }

    return dirtyimage;
  }
}
