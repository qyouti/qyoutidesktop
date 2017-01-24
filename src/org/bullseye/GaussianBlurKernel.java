/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bullseye;

import java.awt.image.*;

/**
 *
 * @author jon
 */
public class GaussianBlurKernel extends Kernel
{

  private static float[] generateKernelData( int w )
  {
    float[][] data2d = generateKernelData2D( w );
    float[] data = new float[w*w];
    for ( int i=0; i<data2d.length; i++ )
      for ( int j=0; j<data2d[i].length; j++ )
        data[i*data2d.length + j]=data2d[i][j];
    return data;
  }
  
  public static float[][] generateKernelData2D( int w )
  {
    int x, y, i, j;
    int radius = (w - 1) / 2;
    float sigma = (float)radius * 1.0f / 2.0f;
    float factor = 1.0f / (2.0f * (float)Math.PI * sigma * sigma);
    float fx, fy;
    
    float[][] data2d = new float[w][w];
    
    //System.out.println( "=====================" );
    for ( x=0; x<=radius; x++ )
    {
      for ( y=0; y<=radius; y++ )
      {
        fx = (float)x;
        fy = (float)y;
        data2d[x+radius][y+radius] = factor * (float)Math.exp( -1.0 * ( fx*fx + fy*fy ) / (2.0f*sigma*sigma) );
      }
    }

    float total = 0.0f;
    for ( x=-radius; x<=radius; x++ )
    {
      for ( y=-radius; y<=radius; y++ )
      {
        if ( x<0 || y<0 )
        {
          data2d[x+radius][y+radius] = data2d[Math.abs(x)+radius][Math.abs(y)+radius];
        }
        total+=data2d[x+radius][y+radius];
        //System.out.print( data2d[x+radius][y+radius] + "   " );
      }
      //System.out.println( "" );
    }
    
    // normalise...
    //System.out.println( "Check: " + total );
    for ( x=-radius; x<=radius; x++ )
      for ( y=-radius; y<=radius; y++ )
        data2d[x+radius][y+radius] = data2d[x+radius][y+radius] / total;
    //System.out.println( "=====================" );
    
    return data2d;
  }
  
  private GaussianBlurKernel( int width, int height, float[] data )
  {
    super( width, height, data );
  }
  
  public GaussianBlurKernel( int w )
  {
    super( w, w, GaussianBlurKernel.generateKernelData( w ) );
  }
}
