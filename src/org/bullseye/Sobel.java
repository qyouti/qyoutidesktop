/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bullseye;

import java.awt.*;
import java.awt.image.*;

/**
 *
 * @author jon
 */
public class Sobel
{

  static final int X = 0;
  static final int Y = 1;
  static final int[] SIZES =
  {
    3, 5, 7, 9
  };

  // First index is x or y
  // Second index is kernel size
  // kernel is expressed as 1D array
  static final double[][][] KERNELS = new double[2][4][];

  static final double[] SOBEL3X = KERNELS[X][0];
  static final double[] SOBEL3Y = KERNELS[Y][0];

  static final double[] SOBEL5X = KERNELS[X][1];
  static final double[] SOBEL5Y = KERNELS[Y][1];

  static final double[] SOBEL7X = KERNELS[X][2];
  static final double[] SOBEL7Y = KERNELS[Y][2];

  static final double[] SOBEL9X = KERNELS[X][3];
  static final double[] SOBEL9Y = KERNELS[Y][3];

  static
  {
    for ( int i = 0; i < SIZES.length; i++ )
    {
      KERNELS[X][i] = new double[SIZES[i] * SIZES[i]];
      KERNELS[Y][i] = new double[SIZES[i] * SIZES[i]];
      generate( SIZES[i], KERNELS[X][i], KERNELS[Y][i] );
    }
  }

  /**
   * Convert 2D coordinate into kernel 1D index.
   * @param size Size of the kernel.
   * @param x Centre of the kernel at x==0 
   * @param y Ditto y==0
   * @return
   */
  static int to1DIndex( int size, int x, int y )
  {
    int half = size / 2;
    return (size * (y + half) + (x + half));
  }

  /** 
   * Builds x and y kernels of a given size.
   * @param size Size of desired kernel
   * @param ax Destination array for X kernel
   * @param ay Destination array for Y kernel
   */
  static void generate( int size, double[] ax, double[] ay )
  {
    int x, y, n, v, start;
    int half = size / 2;

    start = -2;
    for ( y = -half; y <= 0; y++ )
    {
      v = start;
      ax[to1DIndex( size, 0, y )] = 0.0;
      ax[to1DIndex( size, 0, -y )] = 0.0;
      for ( x = -half; x < 0; x++ )
      {
        ax[to1DIndex( size, x, y )] = v;
        ax[to1DIndex( size, -x, y )] = -v;
        ax[to1DIndex( size, x, -y )] = v;
        ax[to1DIndex( size, -x, -y )] = -v;
        v++;
      }
      start--;
    }

    for ( x = 0; x < size; x++ )
    {
      for ( y = 0; y < size; y++ )
      {
        ay[x * size + y] = ax[y * size + x];
      }
    }
  }

  /**
   * Fetches a kernel of a given direction and size.
   * @param direction
   * @param size
   * @return 
   */
  static public double[] getKernel( int direction, int size )
  {
    for ( int i = 0; i < SIZES.length; i++ )
    {
      if ( SIZES[i] == size )
      {
        return KERNELS[direction][i];
      }
    }
    return null;
  }

  /**
   * Multiplies each element in array a with corresponding element
   * in array b and sums all the products.
   * @param a The A input
   * @param b The B input
   * @return The sum of products.
   */
  static double multiplyAndAccumulate( double[] a, double[] b )
  {
    if ( a == null || b == null || a.length != b.length )
    {
      throw new IllegalArgumentException( "Need arrays of equal length" );
    }

    double sum = 0.0;
    for ( int i = 0; i < a.length; i++ )
    {
      sum += a[i] * b[i];
    }
    return sum;
  }

  private static final double[][] weights =
  {
    { 1, 1, 1, 1, 1 },
    { 1, 2, 3, 2, 1 },
    { 1, 1, 4, 3, 1 },
    { 1, 2, 3, 2, 1 },
    { 1, 1, 1, 1, 1 }
  };
  
  private static double weightsum;
  
  static
  {
    weightsum=0.0;
    for ( int i=0; i<weights.length; i++ )
      for ( int j=0; j<weights[i].length; j++ )
        weightsum+=weights[i][j];
  }
  
  /**
   * Performs a Sobel edge detection transform on an image and performs
   * some filtering of the result.
   * 
   * @param input The input image.
   * @param threshold Pixels whose magnitude is below threshold will be set to zero magnitude.
   * @param usered Use the red channel of the input.
   * @param usegreen
   * @param useblue
   * @param kernelsize Size of Sobel kernel to use
   * @param results A map of results for each pixel - must be supplied by caller.
   * @return A structure with results for the whole transform.
   */
  public static SobelResult transform( BufferedImage input, 
                                      double threshold,
                                      boolean usered,
                                      boolean usegreen,
                                      boolean useblue,
                                      int kernelsize,
                                      SobelPixelResult[][] results )
  {
    if ( !usered && !usegreen && !useblue )
      throw new IllegalArgumentException( "At least one colour channel required for Sobel transform" ); 

    if ( results.length < input.getWidth() )
      throw new IllegalArgumentException( "Results array provided is too small." );
    
    int numchan=0;
    if ( usered ) numchan++;
    if ( usegreen ) numchan++;
    if ( useblue ) numchan++;
    

    int n, x, y, xa, ya, rgb;
    int pixelcount = input.getWidth() * input.getHeight();
    int borderinsetx = input.getWidth()/10;
    int borderinsety = input.getHeight()/10;
    boolean inborder;
    int bordercount = 0;
    int centrecount = 0;
    int bordertotal = 0;
    int centretotal = 0;
    
    // a little buffer of luminosity of the same size as the kernel
    // used to calculate for one pixel.
    double[] currentinput = new double[kernelsize * kernelsize];
    // For colour calculations
    SobelResult stats = new SobelResult();
    stats.width = input.getWidth();
    stats.height = input.getHeight();
    stats.results = results;
    
    SobelPixelResult result;
    double[] kernelx = getKernel( X, kernelsize );
    double[] kernely = getKernel( Y, kernelsize );

    stats.maxmag = 0.0;
    
    // Reset the results in the provided arrays.
    // The array may be bigger than needed but not too small
    for ( x = 0; x < results.length; x++ )
    {
      if ( results[x].length < input.getHeight() )
        throw new IllegalArgumentException( "Results array provided is too small." );
      for ( y = 0; y < results[x].length; y++ )
        results[x][y].clear();
    }
    
    // iterate over all the pixels of the input image
    for ( x = (kernelsize / 2); x < (input.getWidth() - (kernelsize / 2)); x++ )
    {
      for ( y = (kernelsize / 2); y < (input.getHeight() - (kernelsize / 2)); y++ )
      {
        inborder = 
                   x<=borderinsetx || 
                   x>=(input.getWidth()-borderinsetx) ||
                   y<=borderinsety || 
                   y>=(input.getWidth()-borderinsety);
        
        result = results[x][y];
        // make a little buffer with brightness of
        // eight pixels around central pixel        
        n = 0;
        for ( ya = y - (kernelsize / 2); ya <= (y + (kernelsize / 2)); ya++ )
        {
          for ( xa = x - (kernelsize / 2); xa <= (x + (kernelsize / 2)); xa++ )
          {
            rgb = input.getRGB( xa, ya );
            currentinput[n] = 0.0;
            if ( usered )
              currentinput[n] += ((rgb >> 16) & 0xff) / 255.0;
            if ( usegreen )
              currentinput[n] += ((rgb >> 8)  & 0xff) / 255.0;
            if ( useblue )
              currentinput[n] += ( rgb        & 0xff) / 255.0;
            currentinput[n] = currentinput[n] / (double)numchan;
            n++;
          }
        }

        // convolve with two sobel kernels
        result.x = multiplyAndAccumulate( currentinput, kernelx );
        result.y = multiplyAndAccumulate( currentinput, kernely );

        result.magnitude = Math.
                sqrt( result.y * result.y + result.x * result.x );

        if ( result.magnitude < threshold )
          result.magnitude = 0.0;
        
        if ( inborder ) bordertotal++;
        else centretotal++;
        if ( result.magnitude != 0.0 )
        {
          if ( inborder ) bordercount++;
          else centrecount++;
        }
        
        result.angle = Math.toDegrees( Math.atan2( result.y, result.x ) );

        if ( result.magnitude > stats.maxmag )
        {
          stats.maxmag = result.magnitude;
        }
      }
    }

    stats.percentageBorderEdgePixels = 100.0 * bordercount / bordertotal;
    stats.percentageCentreEdgePixels = 100.0 * centrecount / centretotal;
    
    double meanx, meany;
    n = 0;
    for ( x = (kernelsize / 2) + 1; x < (input.getWidth() - (kernelsize / 2) - 1); x++ )
    {
      for ( y = (kernelsize / 2) + 1; y < (input.getHeight() - (kernelsize / 2) - 1); y++ )
      {
        meanx = 0.0;
        meany = 0.0;
        for ( xa = -2; xa <= 2; xa++ )
        {
          for ( ya = -2; ya <= 2; ya++ )
          {
            result = results[x + xa][y + ya];
            if ( result.magnitude != 0.0 )
            {
              meanx += result.x * weights[xa+2][ya+2];
              meany += result.y;
              n++;
            }
          }
        }
        meanx = meanx / weightsum;
        meany = meany / weightsum;
        result = results[x][y];
        result.smooth_angle = Math.toDegrees( Math.atan2( meany, meanx ) );
      }
    }

    return stats;
  }

}
