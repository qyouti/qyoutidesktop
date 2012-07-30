/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti.scan.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author
 */
public class FastFourierTransform2D
{

  public static int iroot( int n )
  {
    double dr = Math.sqrt( n );
    int ir = (int)Math.round( dr );
    if ( ir*ir != n )
      throw new ArithmeticException( "Not an integer square root." );
    return ir;
  }

  public static float rgbToValue( int rgb )
  {
    return (float)((rgb & 0xff) + (rgb >> 8 & 0xff) + (rgb >>16 & 0xff)) / (float)(3.0 * 255.0);
  }

  public static int valueToRGB( float value )
  {
    int v = (int) (value * 255.0);
    if ( v > 255 ) v = 255;
    return v | v<<8 | v<<16;
  }

  public static float[] toFloatArray( BufferedImage image )
  {
    int h = image.getHeight();
    int w = image.getWidth();
    int l = h>w ? h : w;

    //find the right size the hard way
    int size;
    for ( size=1; size<l; size = size << 1 );

    System.out.println( "Image " + w + " by " + h );
    System.out.println( "Enlarging to " + size + " by " + size );

    float[] data = new float[size * size * 2];
    int rgb, index;
    for ( int x=0; x<size; x++ )
      for ( int y=0; y<size; y++ )
      {
        index = 2*(y*size + x);
        data[ index     ] = (float) 1.0;
        data[ index + 1 ] = (float) 0.0;
        if ( x<w && y<h )
          data[ index ] = rgbToValue( image.getRGB( x, y ) );
      }

    return data;
  }

  public static BufferedImage toBufferedImage( float[] data )
  {
    return toBufferedImage( data, false, 0.0F );
  }

  public static BufferedImage toBufferedImage( float[] data, float threshold )
  {
    return toBufferedImage( data, true, threshold );
  }

  public static BufferedImage toBufferedImage( float[] data, boolean usethreshold, float threshold )
  {
    float sqrthreshold = threshold*threshold;
    float r, i, sqrv;
    int index;
    int size = iroot( data.length / 2 );
    BufferedImage image = new BufferedImage( size, size, BufferedImage.TYPE_INT_RGB );
    for ( int x=0; x<size; x++ )
      for ( int y=0; y<size; y++ )
      {
        index = 2*(y*size + x);
        r = data[index];
        i = data[index+1];
        sqrv = r*r + i*i;
        if ( usethreshold )
          image.setRGB( x, y, valueToRGB( sqrv < sqrthreshold ? 0.0F : 1.0F ) );
        else
          image.setRGB( x, y, valueToRGB( (float)Math.sqrt( sqrv ) ) );
      }
    return image;
  }

  public static void normalise( float[] data, float darkest, float lightest )
  {
    int index;
    int size = iroot( data.length / 2 );
    float inverseinterval = (float) (1.0 / (lightest - darkest));
    for ( int x=0; x<size; x++ )
      for ( int y=0; y<size; y++ )
      {
        index = 2*(y*size + x);
        data[index] -= darkest;
        data[index+1] -= darkest;
        data[index] *= inverseinterval;
        data[index+1] *= inverseinterval;
      }

  }

  public static void normalise( BufferedImage image  )
  {
    int x, y;
    float value;
    float darkest=(float) 1.0, lightest=(float) 0.0;

    for ( x=0; x<image.getWidth(); x++ )
      for ( y=0; y<image.getHeight(); y++ )
      {
        value = rgbToValue( image.getRGB( x, y ) );
        if ( value < darkest  )  darkest = value;
        if ( value > lightest ) lightest = value;
      }

    float inverseinterval = (float) (1.0 / (lightest - darkest));
    
    for ( x=0; x<image.getWidth(); x++ )
      for ( y=0; y<image.getHeight(); y++ )
      {
        value = rgbToValue( image.getRGB( x, y ) );
        image.setRGB( x, y, valueToRGB( (value-darkest) * inverseinterval ) );
      }
  }

  public static void saltiremask( float[] data )
  {
    int diagwidth = 4;
    int index;
    int size = iroot( data.length / 2 );
    for ( int x=0; x<size; x++ )
      for ( int y=0; y<size; y++ )
      {
        //if ( Math.abs(x-y) > 6 && Math.abs((size-x)-y) > 6 )
        if ( ( x-y >  diagwidth && (size-x)-y >  diagwidth ) ||
             ( x-y < -diagwidth && (size-x)-y < -diagwidth ) )
        {
          index = 2*(y*size + x);
          data[index] = (float) 0.0;
          data[index+1] = (float) 0.0;
          index = 2*(x*size + y);
          data[index] = (float) 0.0;
          data[index+1] = (float) 0.0;
        }
      }
  }

  public static void verticalmask( float[] data )
  {
    int index, x, y;
    int size = iroot( data.length / 2 );
    System.out.println( "verticalmask data length " + data.length + "    size " + size );

    int bandbottom=4, bandtop=10;

    for ( x=bandbottom; x<bandtop && x<size; x++ )
      for ( y=0; y<size; y++ )
      {
        index = 2*(y*size + x);
        data[index] = (float) 0.0;
        data[index+1] = (float) 0.0;
      }
    for ( x=size-bandbottom; x>(size-bandtop) && x>0; x-- )
      for ( y=0; y<size; y++ )
      {
        index = 2*(y*size + x);
        data[index] = (float) 0.0;
        data[index+1] = (float) 0.0;
      }
  }


  // http://www.ulib.org/webRoot/Books/Numerical_Recipes/bookcpdf/c12-4.pdf
  public static void fft2d( float data[], int ndim, int isign )
  {
    System.out.println("Doing FFT");
    int SIZE = iroot( data.length / 2 );
    int SIZEsqrd = SIZE*SIZE;

    int idim;
    int i1, i2, i3, i2rev, i3rev, ip1, ip2, ip3, ifp1, ifp2;
    int ibit, k1, k2, n, nprev, nrem, ntot;
    float tempi, tempr, temp;
    double theta, wi, wpi, wpr, wr, wtemp;
    ntot = SIZEsqrd;
    nprev = 1;
    for ( idim = ndim; idim >= 1; idim-- )
    {
      n = SIZE;
      nrem = ntot / (n * nprev);
      ip1 = nprev << 1;
      ip2 = ip1 * n;
      ip3 = ip2 * nrem;
      i2rev = 1;
      for ( i2 = 1; i2 <= ip2; i2 += ip1 )
      {  // Bit reversal part
        if ( i2 < i2rev )
        {
          for ( i1 = i2; i1 <= i2 + ip1 - 2; i1 += 2 )
          {
            for ( i3 = i1; i3 <= ip3; i3 += ip2 )
            {
              i3rev = i2rev + i3 - i2;
              temp = data[i3 - 1];
              data[i3 - 1] = data[i3rev - 1];
              data[i3rev - 1] = temp;
              temp = data[i3];
              data[i3] = data[i3rev];
              data[i3rev] = temp;
            }
          }
        }
        ibit = ip2 >> 1;
        while ( ibit >= ip1 && i2rev > ibit )
        {
          i2rev -= ibit;
          ibit >>= 1;
        }
        i2rev += ibit;
      }
      ifp1 = ip1;  // Here begins the real fft code.
      while ( ifp1 < ip2 )
      {
        ifp2 = ifp1 << 1;
        theta = isign * 6.28318530717959 / (ifp2 / ip1);
        wtemp = Math.sin( 0.5 * theta );
        wpr = -2.0 * wtemp * wtemp;
        wpi = Math.sin( theta );
        wr = 1.0;
        wi = 0.0;
        for ( i3 = 1; i3 <= ifp1; i3 += ip1 )
        {
          for ( i1 = i3; i1 <= i3 + ip1 - 2; i1 += 2 )
          {
            for ( i2 = i1; i2 <= ip3; i2 += ifp2 )
            {
              k1 = i2;
              k2 = k1 + ifp1;
              //System.out.println("k2="+k2);
              tempr = (float) wr * data[k2 - 1] - (float) wi * data[k2];
              tempi = (float) wr * data[k2] + (float) wi * data[k2 - 1];
              data[k2 - 1] = data[k1 - 1] - tempr;
              data[k2] = data[k1] - tempi;
              data[k1 - 1] += tempr;
              data[k1] += tempi;
            }
          }
          wr = (wtemp = wr) * wpr - wi * wpi + wr;
          wi = wi * wpr + wtemp * wpi + wi;
        }
        ifp1 = ifp2;
      }
      nprev *= n;
    }
    // Rescale data back down.
    for ( int off = 0; off < SIZEsqrd << 1; off++ )
    {
      data[off] /= (float) SIZE;
    }
  }


  static int count = 1000000;
  public static void process( BufferedImage image )
          throws IOException
  {
    String prefix = Integer.toString( count );
    BufferedImage nimage = image.getSubimage( 0, 0, image.getWidth(), image.getHeight() );
    normalise( nimage );
    ImageIO.write( image, "jpg", new File( "/home/jon/2dfft/" + prefix + "normalised.jpg" ) );
    float[] data = toFloatArray( image );
    fft2d( data, 2,  1 );
    BufferedImage fftimage = toBufferedImage( data );
    ImageIO.write( fftimage, "jpg", new File( "/home/jon/2dfft/" + prefix + "fft.jpg" ) );
    verticalmask( data );
    //saltiremask( data );
    BufferedImage ffftimage = toBufferedImage( data );
    ImageIO.write( ffftimage, "jpg", new File( "/home/jon/2dfft/" + prefix + "fftf.jpg" ) );
    fft2d( data, 2, -1 );
    BufferedImage rfftimage = toBufferedImage( data, 0.5F );
    BufferedImage cropped = rfftimage.getSubimage( 0, 0, image.getWidth(), image.getHeight() );
    ImageIO.write( cropped, "jpg", new File( "/home/jon/2dfft/" + prefix + "fftr.jpg" ) );
    count++;
  }

  public static void main( String[] args )
  {
    try
    {
      BufferedImage image;
      image = ImageIO.read( new File( "/home/jon/a_test.jpg" ) );
      process( image );
      image = ImageIO.read( new File( "/home/jon/b_test.jpg" ) );
      process( image );
    }
    catch ( Exception ex )
    {
      Logger.getLogger( FastFourierTransform2D.class.getName() ).log( Level.SEVERE, null, ex );
    }
  }


}
