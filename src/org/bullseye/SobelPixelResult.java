/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bullseye;

/**
 *
 * @author jon
 */
public class SobelPixelResult
{
  
  public double x;
  public double y;
  public double magnitude=0.0;
  public double angle;
  public double smooth_angle;
  
  public void clear()
  {
    x=0;
    y=0;
    magnitude=0.0;
    angle=0.0;
    smooth_angle=0.0;
  }
}
