/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bullseye;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

/**
 *
 * @author jon
 */
public class BullseyePage
{
  private static SplineInterpolator interpolator = new SplineInterpolator();

  
  public boolean failed = true;
  
  // estimated pixels per 1/100 inch
  public double roughscale;
  
  public int minorcount;  // The number of little bullseyes
  
  // in pixel units
  public Rectangle pagebounds;
  public Rectangle[][] searchareas = null;
  public BufferedImage[][] searchimages = null;
  public BufferedImage[][] votemapimages = null;
  
  public Point[] bullseyepointsprint;
  public Point[] bullseyepointsscan;
  int topi, bottomi, lefti, righti, origini;
  
  double vvectx, vvecty, hvectx, hvecty, vvectlen, hvectlen;
  double hl, vl;
  
  double hxunitscale, hyunitscale, vxunitscale, vyunitscale;
  double scanoriginx, scanoriginy;
          
  double[] vertexpected;
  double[] vertobserved;
  PolynomialSplineFunction verticalcalibration;
  
  void calibrate( boolean useminor )
  {
    vvectx = bullseyepointsscan[bottomi].x - bullseyepointsscan[topi].x;
    vvecty = bullseyepointsscan[bottomi].y - bullseyepointsscan[topi].y;
    vvectlen = (double) Math.sqrt( vvectx*vvectx + vvecty*vvecty );
    hvectx = bullseyepointsscan[righti].x - bullseyepointsscan[lefti].x;
    hvecty = bullseyepointsscan[righti].y - bullseyepointsscan[lefti].y;
    hvectlen = (double) Math.sqrt( hvectx*hvectx + hvecty*hvecty );

    hl = bullseyepointsprint[righti].x - bullseyepointsprint[lefti].x;
    vl = bullseyepointsprint[bottomi].y - bullseyepointsprint[topi].y;

    roughscale = ((hvectlen / hl) + (vvectlen / vl))/2.0f; 
    
    hxunitscale = hvectx/hl;
    hyunitscale = hvecty/hl;
    vxunitscale = vvectx/vl;
    vyunitscale = vvecty/vl;
    
    if ( origini == BullseyePageScanner.NONE )
    {
      scanoriginx = (double)bullseyepointsscan[BullseyePageScanner.TOPRIGHT].x;
      scanoriginy = (double)bullseyepointsscan[BullseyePageScanner.TOPRIGHT].y;
      scanoriginx -= hvectx;
      scanoriginy -= hvecty;
    }
    else
    {
      scanoriginx = (double)bullseyepointsscan[origini].x;
      scanoriginy = (double)bullseyepointsscan[origini].y;
    }
    
    if ( !useminor || minorcount == 0 )
      return;
    
    // Now use PiecewiseBicubicSplineInterpolator
    // to work out a vertical page coordinate correction
    vertexpected = new double[minorcount+2];
    vertobserved = new double[minorcount+2];
    int n;
    Point temp = new Point();
    System.out.println( "=================" );
    for ( int i=0; i<(minorcount+2); i++ )
    {
      if ( i==0 ) n=topi;
      else if ( i < (minorcount+1) ) n = i+3;
      else n = bottomi;
      
      if ( bullseyepointsscan[n] == null ) continue;
      vertobserved[i] = bullseyepointsscan[n].y;
      this.toImageCoordinates(bullseyepointsprint[n].x - bullseyepointsprint[lefti].x, 
              bullseyepointsprint[n].y - bullseyepointsprint[topi].y, 
              temp, false );
      vertexpected[i] = temp.y;
      // System.out.println( "=================  i = " + i + " Expected = " + vertexpected[i] + " Observed = " + vertobserved[i] );
    }
    //System.out.println( "=================" );
    verticalcalibration = interpolator.interpolate( vertexpected, vertobserved );
    //for ( double expected=vertexpected[0]; expected<vertexpected[minorcount+1]; expected += 5.0 )
    //  System.out.println( "================= Expected = " + expected + " Observed = " + verticalcalibration.value(expected) );
    //System.out.println( "=================" );
  }
  
  /**
   * 
   * @param printxy  Position in 100th inch relative to top left of printed page
   * @param imagexy  Position in pixels from top left of scanned image
   */
  public void toImageCoordinates( Point printxy, Point scanxy )
  {
    toImageCoordinates( (double)printxy.x, (double)printxy.y, scanxy, true );
  }

  
  private Point ptemp = new Point();
  /**
   * Finds a rectangle in pixel units that surrounds all the given points
   * 
   * @param p Positions in 100th inch relative to top left of printed page
   * @param bounds Rectangle in pixel coords that bounds all points
   */
  public synchronized void toImageBounds( Point[] p, Rectangle bounds )
  {
    // synchronized because of ptemp
    bounds.setLocation(0, 0);
    bounds.setSize(0, 0);
    for ( int k=0; k<p.length; k++ )
    {
      toImageCoordinates( p[k], ptemp );
      if ( k==0 )
        bounds.setLocation(ptemp);
      else
        bounds.add(ptemp);
    }    
  }
  
  /**
   * 
   * @param printxy  Position in 100th inch relative to top left of printed page
   * @param imagexy  Position in pixels from top left of scanned image
   */
  void toImageCoordinates( Point printxy, Point scanxy, boolean useminor )
  {
    toImageCoordinates( (double)printxy.x, (double)printxy.y, scanxy, useminor );
  }
  
  /**
   * 
   * @param printxy  Position in 100th inch relative to top left of printed page
   * @param imagexy  Position in pixels from top left of scanned image
   */
  public void toImageCoordinates( double printx, double printy, Point scanxy, boolean useminor )
  {
    double x, y;
    x  = scanoriginx;
    y  = scanoriginy;
    
    x += printy * vxunitscale;
    y += printy * vyunitscale;
    
    x += printx * hxunitscale;
    y += printx * hyunitscale;
    
    if ( useminor && verticalcalibration != null && verticalcalibration.isValidPoint( y ) )
        y = verticalcalibration.value(y);
    
    scanxy.x = (int)Math.round(x);
    scanxy.y = (int)Math.round(y);
  }
  
}
