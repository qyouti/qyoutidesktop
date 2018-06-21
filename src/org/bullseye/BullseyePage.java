/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bullseye;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 *
 * @author jon
 */
public class BullseyePage
{
  public boolean failed = true;
  
  // estimated pixels per 1/100 inch
  public float roughscale;
  
  // in pixel units
  public Rectangle pagebounds;
  public Rectangle[] searchareas = new Rectangle[3];
  public BufferedImage[] searchimages = new BufferedImage[3];
  public BufferedImage[] votemapimages = new BufferedImage[3];
  
  public Point[] bullseyepointsprint;
  public Point[] bullseyepointsscan;
  
  float vvectx, vvecty, hvectx, hvecty, vvectlen, hvectlen;
  float hl, vl;
  
  float hxunitscale, hyunitscale, vxunitscale, vyunitscale;
  float scanoriginx, scanoriginy;
          
  void calibrate()
  {
    vvectx = bullseyepointsscan[0].x - bullseyepointsscan[1].x;
    vvecty = bullseyepointsscan[0].y - bullseyepointsscan[1].y;
    vvectlen = (float) Math.sqrt( vvectx*vvectx + vvecty*vvecty );
    hvectx = bullseyepointsscan[2].x - bullseyepointsscan[0].x;
    hvecty = bullseyepointsscan[2].y - bullseyepointsscan[0].y;
    hvectlen = (float) Math.sqrt( hvectx*hvectx + hvecty*hvecty );

    hl = bullseyepointsprint[2].x - bullseyepointsprint[0].x;
    vl = bullseyepointsprint[0].y - bullseyepointsprint[1].y;

    roughscale = ((hvectlen / hl) + (vvectlen / vl))/2.0f; 
    
    hxunitscale = hvectx/hl;
    hyunitscale = hvecty/hl;
    vxunitscale = vvectx/vl;
    vyunitscale = vvecty/vl;
    
    scanoriginx = bullseyepointsscan[1].x;
    scanoriginy = bullseyepointsscan[1].y;
//    Point test = new Point( 0, -bullseyepointsprint[1].y );
//    Point p = new Point();
//    toImageCoordinates( test, p );
//    scanoriginx = p.x;
//    scanoriginy = p.y;
  }
  
  /**
   * 
   * @param printxy  Position in 100th inch relative to top left of printed page
   * @param imagexy  Position in pixels from top left of scanned image
   */
  public void toImageCoordinates( Point printxy, Point scanxy )
  {
    float x, y;
    x  = scanoriginx;
    y  = scanoriginy;
    x += (float)printxy.y * vxunitscale;
    y += (float)printxy.y * vyunitscale;
    x += (float)printxy.x * hxunitscale;
    y += (float)printxy.x * hyunitscale;
    
    scanxy.x = Math.round(x);
    scanxy.y = Math.round(y);
  }
}
