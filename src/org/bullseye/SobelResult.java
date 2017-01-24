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
public class SobelResult
{
  public double maxmag;
  public int width, height;
  double percentageCentreEdgePixels;
  double percentageBorderEdgePixels;
  
  /**
   * Arrays of data may be reused in another transform so has a limited
   * shelf life. SobelResultData outside the bounds of the declared width and height is
   * undefined.
   */
  public SobelPixelResult[][] results;
  
  public BufferedImage toImage()
  {
    int x, y, colour;
    SobelPixelResult sobelpixel;
    BufferedImage pixeltypemap = new BufferedImage(
            width,
            height,
            BufferedImage.TYPE_BYTE_INDEXED,
            pixeltypecolourtable );
    Graphics2D g = (Graphics2D)pixeltypemap.getGraphics();
    g.setColor( Color.WHITE );
    g.fillRect( 0, 0, pixeltypemap.getWidth(), pixeltypemap.getHeight() );
    for ( x=0; x<width; x++ )
      for ( y=0; y<height; y++ )
      {
        sobelpixel = results[x][y];
        if ( sobelpixel.magnitude == 0.0 )
          pixeltypemap.setRGB(x, y, Color.WHITE.getRGB() );
        else
        {
          colour = (int) Math.round( (sobelpixel.smooth_angle / 360.0) * (double)ANGLES);
          if ( colour < 0 ) colour += ANGLES;
          pixeltypemap.setRGB(x, y, pixeltypecolourtable.getRGB( colour ) );
        }
      }
    return pixeltypemap;
  }
  

  private static IndexColorModel pixeltypecolourtable = null;
  public static final int ANGLES   = 180;
    
  static  
  {
    byte[] r = new byte[ANGLES+2];
    byte[] g = new byte[ANGLES+2];
    byte[] b = new byte[ANGLES+2];
    int rgb;
    for ( int i=0; i<ANGLES; i++ )
    {
      rgb = Color.HSBtoRGB((float)i / (float)ANGLES, 1.0f, 0.5f );
      r[i] = (byte)((rgb & 0xff0000) >> 16);
      g[i] = (byte)((rgb & 0x00ff00) >> 8);
      b[i] = (byte) (rgb & 0x0000ff);
    }
    r[ANGLES]=g[ANGLES]=b[ANGLES]=0; // add black
    r[ANGLES+1]=g[ANGLES+1]=b[ANGLES+1]=-1; // add white
    pixeltypecolourtable = new IndexColorModel(8,ANGLES+2,r,g,b);
  }
  
}
