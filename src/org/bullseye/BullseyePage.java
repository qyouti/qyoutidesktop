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

  double vvectx, vvecty, hvectx, hvecty, vvectlen, hvectlen;
  double hl, vl;
  
  double hxunitscale, hyunitscale, vxunitscale, vyunitscale;
  double scanoriginx, scanoriginy;
          
  double[] vertexpected;
  double[] vertobserved;
  PolynomialSplineFunction verticalcalibration;
  
  void calibrate( boolean useminor )
  {
    vvectx = bullseyepointsscan[0].x - bullseyepointsscan[1].x;
    vvecty = bullseyepointsscan[0].y - bullseyepointsscan[1].y;
    vvectlen = (double) Math.sqrt( vvectx*vvectx + vvecty*vvecty );
    hvectx = bullseyepointsscan[2].x - bullseyepointsscan[0].x;
    hvecty = bullseyepointsscan[2].y - bullseyepointsscan[0].y;
    hvectlen = (double) Math.sqrt( hvectx*hvectx + hvecty*hvecty );

    hl = bullseyepointsprint[2].x - bullseyepointsprint[0].x;
    vl = bullseyepointsprint[0].y - bullseyepointsprint[1].y;

    roughscale = ((hvectlen / hl) + (vvectlen / vl))/2.0f; 
    
    hxunitscale = hvectx/hl;
    hyunitscale = hvecty/hl;
    vxunitscale = vvectx/vl;
    vyunitscale = vvecty/vl;
    
    scanoriginx = (double)bullseyepointsscan[1].x;
    scanoriginy = (double)bullseyepointsscan[1].y;

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
      if ( i==0 ) n=1;
      else if ( i < (minorcount+1) ) n = i+2;
      else n = 0;
      vertobserved[i] = bullseyepointsscan[n].y;
      this.toImageCoordinates( 
              bullseyepointsprint[n].x - bullseyepointsprint[1].x, 
              bullseyepointsprint[n].y - bullseyepointsprint[1].y, 
              temp, false );
      vertexpected[i] = temp.y;
      System.out.println( "=================  i = " + i + " Expected = " + vertexpected[i] + " Observed = " + vertobserved[i] );
    }
    System.out.println( "=================" );
    verticalcalibration = interpolator.interpolate( vertexpected, vertobserved );
    for ( double expected=vertexpected[0]; expected<vertexpected[minorcount+1]; expected += 5.0 )
      System.out.println( "================= Expected = " + expected + " Observed = " + verticalcalibration.value(expected) );
    System.out.println( "=================" );
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
