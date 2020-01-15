package org.bullseye;


import java.awt.geom.AffineTransform;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.apache.commons.math3.util.FastMath;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author jon
 */
public class AngleBiasedDistanceMeasure implements DistanceMeasure
{
  AffineTransform trans;
  double[] src = new double[4];
  double[] dst = new double[4];
  
  double assymetryfactor = 0.75;
  
  public AngleBiasedDistanceMeasure( double angle )
  {
    trans = AffineTransform.getRotateInstance( Math.toRadians(angle) );
  }
  
  @Override
  public double compute(double[] a, double[] b )
  {
    if ( a.length != 2 )
      throw new DimensionMismatchException( a.length, 2 );
    if ( b.length != 2 )
      throw new DimensionMismatchException( b.length, 2 );
    src[0] = a[0];
    src[1] = a[1];
    src[2] = b[0];
    src[3] = b[1];
    trans.transform(src, 0, dst, 0, 2);
    
    double dx = (dst[0] - dst[2]) * assymetryfactor;
    double dy = (dst[1] - dst[3]);
    
    return FastMath.sqrt( dx*dx + dy*dy );
  }
  
}
