/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bullseye;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

/**
 *
 * @author jon
 */
public class BullseyePageScanner
{
  BullseyeLocator locator;
  float width, height, radius; 
  Point2D.Float[] expectedpoints = new Point2D.Float[3];
  
  /**
   * 
   * @param width in hundredths of inch
   * @param height
   * @param topleft
   * @param bottomleft
   * @param bottomright
   * @param radius
   * @param subradii as proportion of radius
   */
  public BullseyePageScanner( 
          float width, float height, 
          Point2D.Float[] expectedpoints,
          float radius, double[] subradii
          )
  {
    this.width = width;
    this.height = height;
    for ( int i=0; i<3; i++ )
      this.expectedpoints[i] = expectedpoints[i];
    this.radius = radius;
    locator = new BullseyeLocator(null, radius, subradii);
  }
  
  public BullseyePage scan( BufferedImage image )
  {
    int i, pass;
    BullseyePage bpage = new BullseyePage();
    Point[] points;
        
    bpage.pagebounds = new Rectangle( 0, 0, image.getWidth(), image.getHeight() );
    bpage.roughscale = (((float)image.getWidth() / width) + ((float)image.getHeight() / height))/2.0f;
    bpage.bullseyepointsscan = new Point[expectedpoints.length];
    bpage.bullseyepointsprint = new Point[expectedpoints.length];
    for ( i=0; i<expectedpoints.length; i++ )
      bpage.bullseyepointsprint[i] = new Point( Math.round(expectedpoints[i].x), Math.round(expectedpoints[i].y) );
              
    for ( pass=0; pass<=1; pass++ )
    {
      locator.setEstimatedRadius(radius*bpage.roughscale);
      for ( i=0; i<expectedpoints.length; i++ )
      {
        if ( pass == 0 )
          bpage.searchareas[i] = new Rectangle( 
                Math.round(expectedpoints[i].x * bpage.roughscale), 
                Math.round(expectedpoints[i].y * bpage.roughscale), 
                0, 0 );
        else
          bpage.searchareas[i] = new Rectangle( 
                bpage.bullseyepointsscan[i].x, 
                bpage.bullseyepointsscan[i].y, 
                0, 0 );
        bpage.searchareas[i].grow( Math.round(radius*(pass==0?4f:1.5f)*bpage.roughscale), 
                                   Math.round(radius*(pass==0?4f:1.5f)*bpage.roughscale) );
        bpage.searchareas[i] = bpage.searchareas[i].intersection( bpage.pagebounds );

        bpage.searchimages[i] = image.getSubimage(
                        bpage.searchareas[i].x, 
                        bpage.searchareas[i].y, 
                        bpage.searchareas[i].width, 
                        bpage.searchareas[i].height );
        locator.setInputImage( bpage.searchimages[i] );

        points = locator.locateBullseye();
        bpage.votemapimages[i] = locator.getVoteMapImage();
        if ( points.length != 1 )
          return bpage;
        bpage.bullseyepointsscan[i] = new Point( points[0].x + bpage.searchareas[i].x, points[0].y + bpage.searchareas[i].y );
      }
      
      // rescale after pass 0
      // to get better bullseye centres
      // after pass 1 to establish calibration
      bpage.calibrate();      
    }
    
    bpage.failed = false;
    return bpage;
  }
}
