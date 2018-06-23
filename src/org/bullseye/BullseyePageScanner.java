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
  int vdiv, minorcount;
  float minradius;
  Point2D.Float[] expectedpoints;
  
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
          float radius, double[] subradii,
          int vdiv, float minradius
          )
  {
    this.width = width;
    this.height = height;
    this.radius = radius;
    this.minradius = minradius;
    this.vdiv = vdiv;
    if ( this.vdiv < 1 ) this.vdiv = 1;
    this.minorcount = this.vdiv-1;
    this.expectedpoints = new Point2D.Float[3+minorcount];
    for ( int i=0; i<this.expectedpoints.length; i++ )
    {
      if ( i<3 )
        this.expectedpoints[i] = (Point2D.Float)expectedpoints[i].clone();
      else  // interpolate between points 0 and 1
      {
        this.expectedpoints[i] = new Point2D.Float( this.expectedpoints[1].x, this.expectedpoints[1].y );
        this.expectedpoints[i].y += (this.expectedpoints[0].y - this.expectedpoints[1].y)*(i-2)/this.vdiv;
      }
    }
    locator = new BullseyeLocator(null, radius, subradii);
  }
  
  public BullseyePage scan( BufferedImage image )
  {
    int i, pass;
    float passradius;
    BullseyePage bpage = new BullseyePage();
    Point[] points;
    
    bpage.minorcount = minorcount;
    bpage.bullseyepointsprint = new Point[expectedpoints.length];
    bpage.bullseyepointsscan = new Point[expectedpoints.length];
    bpage.searchareas = new Rectangle[2][expectedpoints.length];
    bpage.searchimages = new BufferedImage[2][expectedpoints.length];
    bpage.votemapimages = new BufferedImage[2][expectedpoints.length];
    
    bpage.pagebounds = new Rectangle( 0, 0, image.getWidth(), image.getHeight() );
    bpage.roughscale = (((float)image.getWidth() / width) + ((float)image.getHeight() / height))/2.0f;
    for ( i=0; i<expectedpoints.length; i++ )
    {
      bpage.bullseyepointsprint[i] = new Point( Math.round(expectedpoints[i].x), Math.round(expectedpoints[i].y) );
    }
              
    Point passonepoint = new Point();
    for ( pass=0; pass<=1; pass++ )
    {
      System.out.println( "Page Scanner Pass " + pass );
      for ( i=0; i<((pass==0)?3:expectedpoints.length); i++ )
      {
        System.out.println( "Page Scanner Point " + i );
        passradius = (i<3)?radius:minradius;
        if ( pass == 0 )
          bpage.searchareas[pass][i] = new Rectangle( 
                (int)Math.round(expectedpoints[i].x * bpage.roughscale), 
                (int)Math.round(expectedpoints[i].y * bpage.roughscale), 
                0, 0 );
        else
        {
          // Use current three reference points to refine search areas
          bpage.toImageCoordinates(expectedpoints[i].x - expectedpoints[1].x, expectedpoints[i].y - expectedpoints[1].y, passonepoint, false);
          bpage.searchareas[pass][i] = new Rectangle( passonepoint.x, passonepoint.y, 0, 0 );
        }
        bpage.searchareas[pass][i].grow( (int)Math.round(passradius*(pass==0?4f:1.5f)*bpage.roughscale), 
                                   (int)Math.round(passradius*(pass==0?4f:1.5f)*bpage.roughscale) );
        bpage.searchareas[pass][i] = bpage.searchareas[pass][i].intersection( bpage.pagebounds );

        bpage.searchimages[pass][i] = image.getSubimage(
                        bpage.searchareas[pass][i].x, 
                        bpage.searchareas[pass][i].y, 
                        bpage.searchareas[pass][i].width, 
                        bpage.searchareas[pass][i].height );
        
        locator.setEstimatedRadius(passradius*bpage.roughscale);
        locator.setInputImage( bpage.searchimages[pass][i] );

        points = locator.locateBullseye();
        bpage.votemapimages[pass][i] = locator.getVoteMapImage();
        bpage.bullseyepointsscan[i] = null;
        if ( points.length == 1 )
          bpage.bullseyepointsscan[i] = new Point( 
                                points[0].x + bpage.searchareas[pass][i].x, 
                                points[0].y + bpage.searchareas[pass][i].y );
        else if ( i<3 )
          return bpage;
      }
      
      // rescale after pass 0
      // to get better bullseye centres
      // after pass 1 to establish calibration
      bpage.calibrate( pass == 1 );      
    }
    
    bpage.failed = false;
    return bpage;
  }
}
