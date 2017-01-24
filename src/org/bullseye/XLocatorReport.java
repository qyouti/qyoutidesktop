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
  
  double xQuality;
  double angle;
  double lineratio;
  double northsouthbias;
  double eastwestbias;
          
  Point xLocation;
  double percentageCentreEdgePixels;
  double percentageBorderEdgePixels;
  ArrayList<Point> xPointsofInterest;
  ArrayList<Point> additionalPointsofInterest;

  @Override
  public String toString()
  {
    StringBuilder buffer = new StringBuilder();
    buffer.append( "{ hasX=" );
    buffer.append(  hasX );
    buffer.append( ", xQuality=" );
    buffer.append( xQuality );
    buffer.append( ", angle=" );
    buffer.append( angle );
    buffer.append( ", lineratio=" );
    buffer.append( lineratio );
    buffer.append( ", northsouthbias=" );
    buffer.append( northsouthbias );
    buffer.append( ", eastwestbias=" );
    buffer.append( eastwestbias );
    buffer.append( ", centreedgepixs=" );
    buffer.append( percentageCentreEdgePixels );
    buffer.append( "%, borderedgepixs=" );
    buffer.append( percentageBorderEdgePixels );
    buffer.append( "%, xpoints=" );
    buffer.append( xPointsofInterest.size() );
    buffer.append( ", additional=" );
    buffer.append( additionalPointsofInterest.size() );
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

  public double getXQuality()
  {
    return xQuality;
  }

  public Point getXLocation()
  {
    return xLocation;
  }

  public double getPercentageCentreEdgePixels()
  {
    return percentageCentreEdgePixels;
  }

  public double getPercentageBorderEdgePixels()
  {
    return percentageBorderEdgePixels;
  }

  public List<Point> getXPointsofInterest()
  {
    return xPointsofInterest;
  }

  public List<Point> getAdditionalPointsofInterest()
  {
    return additionalPointsofInterest;
  }
  
}
