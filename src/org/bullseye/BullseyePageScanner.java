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
  public static final int TOPLEFT     = 0;
  public static final int TOPRIGHT    = 1;
  public static final int BOTTOMRIGHT = 2;
  public static final int BOTTOMLEFT  = 3;
  public static final int NONE        = -1;
  
  
  BullseyeLocator locator;
  float width, height, radius; 
  int vdiv, minorcount;
  float minradius;
  Point2D.Float[] expectedpoints;
  
  int topi, bottomi, lefti, righti, origini;
  
  Point2D.Float expectedorigin;
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
    this.expectedpoints = new Point2D.Float[4+minorcount];
    for ( int i=0; i<4; i++ )
    {
      if ( expectedpoints[i] != null )
        this.expectedpoints[i] = (Point2D.Float)expectedpoints[i].clone();
    }
    
    if ( this.expectedpoints[TOPLEFT] != null )
    {
      this.expectedorigin = this.expectedpoints[TOPLEFT];
      origini = TOPLEFT;
    }
    else
    {
      if ( this.expectedpoints[TOPRIGHT] == null || this.expectedpoints[BOTTOMLEFT] == null )
        throw new IllegalArgumentException("Unable to find origin of page.");
      this.expectedorigin = new Point.Float(this.expectedpoints[BOTTOMLEFT].x, this.expectedpoints[TOPRIGHT].y );
      origini= NONE;
    }
    
    if ( this.expectedpoints[TOPLEFT] != null && this.expectedpoints[BOTTOMLEFT] != null )
    {
      topi = TOPLEFT;
      bottomi = BOTTOMLEFT;
    }
    else if ( this.expectedpoints[TOPRIGHT] != null && this.expectedpoints[BOTTOMRIGHT] != null )
    {
      topi = TOPRIGHT;
      bottomi = BOTTOMRIGHT;
    }
    else
      throw new IllegalArgumentException("Needs a top and bottom bullseye on same side of page.");

    if ( this.expectedpoints[TOPLEFT] != null && this.expectedpoints[TOPRIGHT] != null )
    {
      lefti  = TOPLEFT;
      righti = TOPRIGHT;
    }
    else if ( this.expectedpoints[BOTTOMLEFT] != null && this.expectedpoints[BOTTOMRIGHT] != null )
    {
      lefti  = BOTTOMLEFT;
      righti = BOTTOMRIGHT;
    }
    else
      throw new IllegalArgumentException("Needs a left and right bullseye on same side of page.");
      
    float dv = this.expectedpoints[bottomi].y - this.expectedpoints[topi].y;
    for ( int i=4; i<this.expectedpoints.length; i++ )
    {
      // interpolate between points top and bottom references
        this.expectedpoints[i] = new Point2D.Float( this.expectedpoints[topi].x, this.expectedpoints[topi].y );
        this.expectedpoints[i].y += dv*(i-3)/this.vdiv;
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
    bpage.topi    = topi;
    bpage.bottomi = bottomi;
    bpage.lefti   = lefti;
    bpage.righti  = righti;
    bpage.origini = origini;
    bpage.pagebounds = new Rectangle( 0, 0, image.getWidth(), image.getHeight() );
    bpage.roughscale = (float)image.getWidth() / width;
    for ( i=0; i<expectedpoints.length; i++ )
    {
      if ( expectedpoints[i] != null )
        bpage.bullseyepointsprint[i] = new Point( Math.round(expectedpoints[i].x), Math.round(expectedpoints[i].y) );
    }
              
    Point passonepoint = new Point();
    for ( pass=0; pass<=1; pass++ )
    {
      System.out.println( "Page Scanner Pass " + pass );
      for ( i=0; i<((pass==0)?4:expectedpoints.length); i++ )
      {
        System.out.println( "Page Scanner Point " + i );
        if ( expectedpoints[i] == null ) continue;
        passradius = (i<4)?radius:minradius;
        if ( pass == 0 )
          bpage.searchareas[pass][i] = new Rectangle( 
                (int)Math.round(expectedpoints[i].x * bpage.roughscale), 
                (int)Math.round(expectedpoints[i].y * bpage.roughscale), 
                0, 0 );
        else
        {
          // Use current three reference points to refine search areas
          bpage.toImageCoordinates(expectedpoints[i].x - expectedorigin.x, expectedpoints[i].y - expectedorigin.y, passonepoint, false);
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
