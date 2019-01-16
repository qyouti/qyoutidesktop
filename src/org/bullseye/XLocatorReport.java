/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bullseye;

import java.awt.*;
import java.awt.image.*;
import java.util.*;
import java.util.List;

/**
 *
 * @author jon
 */
public class XLocatorReport
{
  BufferedImage image;
  boolean hasX;
  boolean dubious;
  Point xLocation;


  @Override
  public String toString()
  {
    StringBuilder buffer = new StringBuilder();
    buffer.append( "{ hasX=" );
    buffer.append(  hasX );
    buffer.append( " }" );
    return buffer.toString();
  }
  
  public BufferedImage getImage()
  {
    return image;
  }
  
  public boolean hasX()
  {
    return hasX;
  }

  public void setDubious( boolean b )
  {
    dubious = b;
  }
  
  public boolean isDubious()
  {
    return dubious;
  }

  public Point getXLocation()
  {
    return xLocation;
  }
}
