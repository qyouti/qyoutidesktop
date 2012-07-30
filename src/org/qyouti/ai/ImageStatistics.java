/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.qyouti.ai;

import java.awt.image.BufferedImage;

/**
 *
 * @author jon
 */
public class ImageStatistics
{

  BufferedImage image;

  int n=0;
  double sumx=0.0, sumxsqr=0.0;

  public ImageStatistics( BufferedImage image )
  {
    this.image = image;

    int i, j, rgb;
    double v;

    for ( i=0; i < image.getWidth(); i++ )
      for ( j=0; j < image.getHeight(); j++ )
      {
        rgb = 0xffffff & image.getRGB( i, j );
        v  = rgb & 0xff;
        v += (rgb >> 8)  & 0xff;
        v += (rgb >> 16) & 0xff;
        v = v / (255.0 * 3.0);
        sumx += v;
        sumxsqr += v*v;
        n++;
      }
  }

  public double getVariance()
  {
    return (sumxsqr - (sumx*sumx/n)) / n;
  }

  public double getStandardDeviation()
  {
    return Math.sqrt( getVariance() );
  }
}
