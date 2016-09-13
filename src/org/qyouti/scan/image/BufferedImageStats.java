/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti.scan.image;

import java.awt.image.*;

/**
 *
 * @author jon
 */
public class BufferedImageStats
{
  int blackest;
  int whitest;
  
  public BufferedImageStats( BufferedImage image )
  {
    int i, j, rgb, lum;
    for ( i=0; i<image.getWidth(); i++ )
    {
      for ( j=0; j<image.getHeight(); j++ )
      {
        rgb = image.getRGB( i, j );
        lum = (rgb & 0xff) + ((rgb >> 8) & 0xff) + ((rgb >> 16) & 0xff);
        if ( i==0 && j==0 )
        {
          blackest = lum;
          whitest = lum;
        }
        else
        {
          if ( lum < blackest ) blackest = lum;
          if ( lum > whitest ) whitest = lum;
        }
      }
    }
    
  }
  
}
