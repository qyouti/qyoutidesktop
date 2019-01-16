/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bullseye;

import java.awt.Point;
import java.util.ArrayList;

/**
 *
 * @author jon
 */
public class XLocatorReportByVote extends XLocatorReport
{
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
}
