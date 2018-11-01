/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.qyouti.qti1.gui;

import java.awt.Point;
import java.awt.Rectangle;

/**
 *
 * @author jon
 */
public class QuestionMetricBox
        extends Rectangle
{
  String type;
  String ident;
  int index;

  public QuestionMetricBox( int x, int y, int width, int height, String type, String ident, int index )
  {
    super( x, y, width, height );
    this.type = type;
    this.ident = ident;
    this.index = index;
  }

  public int getIndex()
  {
    return index;
  }
  
  public Point[] getCorners( Point[] p )
  {
    return getCorners( p, 0, 0 );
  }
  
  public Point[] getCorners( Point[] p, int xoffset, int yoffset )
  {
    Point[] pp;
    if ( p != null && p.length == 4 )
      pp = p;
    else
      pp = new Point[4];

    pp[0].x = x + xoffset;
    pp[0].y = y + yoffset;
    pp[1].x = pp[0].x + width;
    pp[1].y = pp[0].y;
    pp[2].x = pp[0].x + width;
    pp[2].y = pp[0].y + height;
    pp[3].x = pp[0].x;
    pp[3].y = pp[0].y + height;   
    return pp;
  }

  public String getIdent()
  {
    return ident;
  }

  public void setIdent( String ident )
  {
    this.ident = ident;
  }

  public String getType()
  {
    return type;
  }

  public void setType( String type )
  {
    this.type = type;
  }
  
}
