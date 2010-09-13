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
package org.qyouti.qrcode;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.ResultMetadataType;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.ByteMatrix;
import com.google.zxing.common.LocalBlockBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.Encoder;
import com.google.zxing.qrcode.encoder.QRCode;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.xml.parsers.*;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.w3c.dom.*;



/**
 *
 * @author jon
 */
public class QRCodec
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


  public static byte[][] encodeByteArray( String content )
  {
    return encodeByteArray( content, "UTF8" );
  }

  public static byte[][] encodeByteArray( String content, String encoding )
  {
    Result result;
    QRCode qrcode;
    ByteMatrix bmatrix;
    Hashtable hints = new Hashtable();
    hints.put( EncodeHintType.CHARACTER_SET, encoding );
    char[] extra = {'1'};

    try
    {
      do
      {
        qrcode = new QRCode();
        Encoder.encode( new String( extra ) + content, error_correction, hints, qrcode);
        bmatrix = qrcode.getMatrix();
        if ( false )
          return bmatrix.getArray();
        // but does it decode easily?
        result = null;
        try
        {
          result = decode( bmatrix.getArray() );
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

      return bmatrix.getArray();
      //System.out.println( "Encoded in " + qrcode.getMode().getName() );
    } catch (Exception e)
    {
      e.printStackTrace();
    }

    return null;
  }


  public static byte[][] encodeByteArray( byte[] content )
  {
    char[] c = new char[content.length];
    for ( int i=0; i<content.length; i++ )
    {
      c[i] = (char)((int)content[i] & 0xff);
      //System.out.println( Integer.toHexString( c[i] ) );
    }
    return encodeByteArray( new String( c ), "ISO-8859-1" );
  }



  public static BufferedImage encodeImage( byte[] content )
  {
    byte[][] matrix = encodeByteArray( content );
    if ( matrix == null ) return null;
    return makeImage( matrix );
  }

  public static BufferedImage encodeImage(String content)
  {
    byte[][] matrix = encodeByteArray( content );
    if ( matrix == null ) return null;
    return makeImage( matrix );
  }

  public static BufferedImage makeImage( byte[][] matrix )
  {
    int margin = 10;
    int pixelwidth = 3;

    BufferedImage image = new BufferedImage(
            (matrix.length + margin * 2) * pixelwidth,
            (matrix.length + margin * 2) * pixelwidth,
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
    for (int i = 0; i < matrix.length; i++)
    {
      for (int j = 0; j < matrix[i].length; j++)
      {
        if (matrix[j][i] != 0)  // make a black rectangle
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




  public static Element encodeSVG( byte[] content, double width )
  {
    byte[][] matrix = encodeByteArray( content );
    if ( matrix == null ) return null;
    return makeSVG( matrix, width );
  }

  public static Element encodeSVG( String content, double width )
  {
    byte[][] matrix = encodeByteArray( content );
    if ( matrix == null ) return null;
    return makeSVG( matrix, width );
  }



  public static Element makeSVG( byte[][] matrix, double width )
  {
    init();

    // zero point is centre of top left position mark
    // selected width measures to the centre of the top right position mark
    double mark_size = width / ((double)matrix.length - 7);  // 100th of Inch


    DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
    String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
    Document doc = impl.createDocument(svgNS, "svg", null);
    Element g = doc.createElementNS(svgNS, "g");
    g.setAttribute( "id", "qrcode_" + n++ );
    Element whiterect = doc.createElementNS(svgNS,"rect");
    whiterect.setAttribute( "x", Double.toString( -0.4 * width ) );
    whiterect.setAttribute( "y", Double.toString( -0.4 * width ) );
    whiterect.setAttribute( "stroke", "none" );
    whiterect.setAttribute( "fill", "rgb(255,255,255)" );
    whiterect.setAttribute( "width",  Double.toString( 1.8 * width ) );
    whiterect.setAttribute( "height", Double.toString( 1.8 * width ) );
    g.appendChild( whiterect );

    //Element label = doc.createElement("text");
    //label.setAttribute( "x", Double.toString( -0.3 * width ) );
    //label.setAttribute( "y", Double.toString(  1.4 * width ) );
    //label.setAttribute( "font-size", Double.toString( width / 20.0 ) );
    //label.appendChild( doc.createTextNode( new String( content ) ) );
    //g.appendChild( label );

    Element mark;

    for (int i = 0; i < matrix.length; i++)
    {
      for (int j = 0; j < matrix[i].length; j++)
      {
        if (matrix[j][i] != 0)  // make a black rectangle
        {
          mark = doc.createElementNS(svgNS, "rect" );
          mark.setAttribute( "x", Double.toString( ((double)i - 3.5) * mark_size ) );
          mark.setAttribute( "y", Double.toString( ((double)j - 3.5) * mark_size ) );
          mark.setAttribute( "width",  Double.toString( mark_size * 1.1 ) );
          mark.setAttribute( "height", Double.toString( mark_size * 1.1 ) );
          mark.setAttribute( "stroke", "none" );
          mark.setAttribute( "fill", "rgb(0,0,0)" );
          g.appendChild( mark );
        }
      }
    }

    //System.err.println( "QR - end");
    return g;
  }





  
  public static Element svgQuestionQRCode( String qid, double qheight, String qcoords, double width )
  {
    try
    {
      byte h = (byte)Math.floor( qheight / 10 );
      StringTokenizer tok = new StringTokenizer( qcoords, " " );
      byte[] coords = new byte[tok.countTokens()*2];
      short coord;
      for ( int i=0; i< (coords.length/2); i++ )
      {
        coord = Short.parseShort( tok.nextToken() );
        coords[i*2] = (byte)(coord & 0xff);
        coords[i*2+1] = (byte)((coord >> 8) & 0xff);
      }
      byte[] ba_qid = qid.getBytes("utf8");
      
      byte[] buffer = new byte[ba_qid.length+1 + 1 + coords.length];
      System.arraycopy( ba_qid, 0, buffer, 0, ba_qid.length );
      buffer[ba_qid.length] = 0; // zero terminate
      buffer[ba_qid.length+1] = h;
      System.arraycopy( coords, 0, buffer, ba_qid.length+2, coords.length );
      
      return encodeSVG(buffer, width);
    }
    catch (UnsupportedEncodingException ex)
    {
      Logger.getLogger(QRCodec.class.getName()).log(Level.SEVERE, null, ex);
    }

    return null;
  }




  public static void setErrorCorrection( ErrorCorrectionLevel ec )
  {
    error_correction = ec;
  }




  public static String decodeToString( BufferedImage image )
          throws ReaderException, UnsupportedEncodingException
  {
    QRScanResult result = decode( image );
    if ( result == null ) return null;
    return result.getText();
  }

  public static byte[] decodeToByteArray( BufferedImage image )
          throws ReaderException, UnsupportedEncodingException
  {
    QRScanResult result = decode( image );
    if ( result == null ) return null;
    return result.getBytes();
  }


  public static QRScanResult decode( BufferedImage image )
          throws ReaderException, UnsupportedEncodingException
  {
    BufferedImageLuminanceSource source = new BufferedImageLuminanceSource( image );
    if ( debugImageLabel != null )
      debugImageLabel.setIcon( new ImageIcon( image ) );
    return decode( source );
  }

  public static QRScanResult decode( BufferedImage image, int x, int y, int width, int height )
          throws ReaderException, UnsupportedEncodingException
  {
    BufferedImage cropped = new BufferedImage( width, height, BufferedImage.TYPE_INT_RGB );
    int[] rgbArray = image.getRGB( x, y, width, height, null, 0,  width );
    cropped.setRGB( 0, 0, width, height, rgbArray, 0, width );
    if ( debugImageLabel != null )
      debugImageLabel.setIcon( new ImageIcon( cropped ) );
    
    BufferedImageLuminanceSource source = new BufferedImageLuminanceSource( cropped );
    return decode( source );
  }

  public static QRScanResult decode( BufferedImageLuminanceSource source )
          throws ReaderException, UnsupportedEncodingException
  {
    Result result=null;
    Hashtable hints = new Hashtable();
    hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);

    LocalBlockBinarizer binarizer = new LocalBlockBinarizer( source );
    BinaryBitmap bitmap = new BinaryBitmap( binarizer );


    result = new QRCodeReader().decode( bitmap, hints );

    if (result == null)
    {
      System.out.println("No QR Code found.");
      return null;
    }

    return new QRScanResult( result );
  }



  /**
   * Makes an image from a freshly generated qrcode and decodes it.
   * Used to check the readability of generated pattern.
   * @param qrcode
   * @return
   * @throws com.google.zxing.ReaderException
   * @throws java.io.UnsupportedEncodingException
   */
  private static Result decode( byte[][] qrcode )
          throws ReaderException, UnsupportedEncodingException
  {
    BufferedImage image = makeImage( qrcode );

    Hashtable hints = new Hashtable();
    BufferedImageLuminanceSource source = new BufferedImageLuminanceSource( image );
    LocalBlockBinarizer binarizer = new LocalBlockBinarizer( source );
    BinaryBitmap bitmap = new BinaryBitmap( binarizer );
    Result result;

    result = new QRCodeReader().decode( bitmap, hints );

    if (result == null)
    {
      System.out.println("No QR Code found.");
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
