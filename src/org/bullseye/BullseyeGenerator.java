/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bullseye;

import java.awt.*;
import java.awt.image.*;
import java.util.*;

/**
 *
 * @author jon
 */
public class BullseyeGenerator
{  
  // using prime numbers to space the rings
  public static final double RADII[] = { 29.0/29.0, 23.0/29.0, 17.0/29.0, 7.0/29.0 };
  
  public static double[] scaleRadii( double radius )
  {
    double[] radii = new double[RADII.length];
    for ( int i=0; i< radii.length; i++ )
      radii[i] = radius * RADII[i];
    return radii;
  }
  
  public static BufferedImage createBullseyeImage( double[] radii )
  {
    Color darkcolor = new Color(80,80,80);
    Color lightcolor = new Color(255,255,255);
    BufferedImage image = new BufferedImage( 400, 350, BufferedImage.TYPE_INT_RGB );
    Graphics g = image.getGraphics();
    g.setColor( lightcolor );
    g.fillRect( 0, 0, image.getWidth(), image.getHeight() );

    g.setColor( darkcolor );
    g.fillRect( 200, 200, 50, 50 );
    Font font = new Font("Serif", Font.PLAIN, 36);
    g.setFont(font);    
    g.drawString( "Some text", 200, 100 );
    g.drawString( "More text", 200, 250 );
    
    boolean dark=true;
    double localradius;
    
    if ( radii != null)
    {
      for ( int i=0; i<radii.length; i++ )
      {
        localradius = radii[i];
        g.setColor( dark?darkcolor:lightcolor );
        g.fillOval( 125 - (int)localradius, 125 - (int)localradius, (int)(localradius*2.0), (int)(localradius*2.0) );
        g.fillOval( 100 - (int)localradius, 255 - (int)localradius, (int)(localradius*2.0), (int)(localradius*2.0) );
        dark = !dark;
      }
    }
    g.setColor( lightcolor );
    g.fillRect( 150, 120, 40, 40 );
    

    ConvolveOp op = new ConvolveOp( new GaussianBlurKernel( 5 ), ConvolveOp.EDGE_NO_OP, null );
    BufferedImage blurred = op.filter( image, null );
    
    int lightness;
    Random rand = new Random();
    for ( int x=0; x<image.getWidth(); x++ )
      for ( int y=0; y<image.getHeight(); y++ )
      {
        lightness = blurred.getRGB( x, y ) & 0xff;
        lightness += rand.nextInt( 20 ) - 10;
        if ( lightness < 0   ) lightness = 0;
        if ( lightness > 255 ) lightness = 255;
        blurred.setRGB( x, y, lightness | lightness << 8 | lightness << 16 );
      }
    
    return blurred;
  }
}
