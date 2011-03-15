/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.qyouti.statistics;

/**
 *
 * @author jon
 */
public class SimpleStatistics
{
  long n;
  double sumx;
  double sumsqx;

  public SimpleStatistics()
  {
    reset();
  }

  public void reset()
  {
    n=0;
    sumx=0.0;
    sumsqx=0.0;
  }

  public void addDatum( double x )
  {
    n += 1;
    sumx += x;
    sumsqx += x*x;
  }

  public double mean()
  {
    return sumx / (double)n;
  }

  public double sampleStandardDeviation()
  {
    return Math.sqrt((sumsqx-(sumx*sumx/(double)n))/((double)n-1));
  }

}
