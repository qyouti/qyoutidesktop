/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bullseye;

import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Adapted from code by Martin Thoma
 * https://github.com/MartinThoma/algorithms/tree/master/crossingLineCheck/Geometry/src
 *
 * @author
 */
public class Geometry
{

  public static final double EPSILON = 0.000001;

  /**
   * Calculate the cross product of two points.
   *
   * @param a first point
   * @param b second point
   * @return the value of the cross product
   */
  public static double crossProduct( Point2D.Double a, Point2D.Double b )
  {
    return a.x * b.y - b.x * a.y;
  }

  /**
   * Check if bounding boxes do intersect. If one bounding box touches the
   * other, they do intersect.
   *
   * @param a first bounding box
   * @param b second bounding box
   * @return <code>true</code> if they intersect, <code>false</code> otherwise.
   */
  public static boolean doBoundingBoxesIntersect( Point2D.Double[] a,
                                                  Point2D.Double[] b )
  {
    return a[0].x <= b[1].x && a[1].x >= b[0].x && a[0].y <= b[1].y
            && a[1].y >= b[0].y;
  }

  /**
   * Checks if a Point2D.Double is on a line
   *
   * @param a line (interpreted as line, although given as line segment)
   * @param b point
   * @return <code>true</code> if point is on line, otherwise <code>false</code>
   */
  public static boolean isPointOnLine( LineSegment a, Point2D.Double b )
  {
    // Move the image, so that a.first is on (0|0)
    LineSegment aTmp = new LineSegment( new Point2D.Double( 0, 0 ), new Point2D.Double(
                                        a.second.x - a.first.x, a.second.y - a.first.y ) );
    Point2D.Double bTmp = new Point2D.Double( b.x - a.first.x, b.y - a.first.y );
    double r = crossProduct( aTmp.second, bTmp );
    return Math.abs( r ) < EPSILON;
  }

  /**
   * Checks if a point is right of a line. If the point is on the line, it is
   * not right of the line.
   *
   * @param a line segment interpreted as a line
   * @param b the point
   * @return <code>true</code> if the point is right of the line,
   * <code>false</code> otherwise
   */
  public static boolean isPointRightOfLine( LineSegment a, Point2D.Double b )
  {
    // Move the image, so that a.first is on (0|0)
    LineSegment aTmp = new LineSegment( new Point2D.Double( 0, 0 ), new Point2D.Double(
                                        a.second.x - a.first.x, a.second.y - a.first.y ) );
    Point2D.Double bTmp = new Point2D.Double( b.x - a.first.x, b.y - a.first.y );
    return crossProduct( aTmp.second, bTmp ) < 0;
  }

  /**
   * Check if line segment first touches or crosses the line that is defined by
   * line segment second.
   *
   * @param first line segment interpreted as line
   * @param second line segment
   * @return <code>true</code> if line segment first touches or crosses line
   * second, <code>false</code> otherwise.
   */
  public static boolean lineSegmentTouchesOrCrossesLine( LineSegment a,
                                                         LineSegment b )
  {
    return isPointOnLine( a, b.first )
            || isPointOnLine( a, b.second )
            || (isPointRightOfLine( a, b.first ) ^ isPointRightOfLine( a,
                                                                       b.second ));
  }

  /**
   * Check if line segments intersect
   *
   * @param a first line segment
   * @param b second line segment
   * @return <code>true</code> if lines do intersect, <code>false</code>
   * otherwise
   */
  public static boolean doLinesIntersect( Point2D.Double a1, Point2D.Double a2, Point2D.Double b1, Point2D.Double b2 )
  {
    LineSegment a = new LineSegment( a1, a2 );
    LineSegment b = new LineSegment( b1, b2 );
    return doLinesIntersect( a, b );
  }
  
  public static boolean doLinesIntersect( LineSegment a, LineSegment b )
  {
    Point2D.Double[] box1 = a.getBoundingBox();
    Point2D.Double[] box2 = b.getBoundingBox();
    return doBoundingBoxesIntersect( box1, box2 )
            && lineSegmentTouchesOrCrossesLine( a, b )
            && lineSegmentTouchesOrCrossesLine( b, a );
  }

  public static Point2D.Double getLineSegmentIntersection( Point2D.Double a1, Point2D.Double a2, Point2D.Double b1, Point2D.Double b2 )
  {
    if ( !doLinesIntersect( a1, a2, b1, b2 ) )
      return null;

    Point2D.Double pi = new Point2D.Double();
    double ma, mb, ta, tb;

    ma = (a1.y - a2.y) / (a1.x - a2.x);
    ta = a1.y - ma*a1.x;

    mb = (b1.y - b2.y) / (b1.x - b2.x);
    tb = b1.y - mb*b1.x;

    if ( Double.isInfinite( ma ))
    {
      pi.x = a1.x;
      pi.y = mb*pi.x+tb;
    }
    else if ( Double.isInfinite( mb ) )
    {
      pi.x = b1.x;
      pi.y = ma*pi.x+ta;
    }    
    else
    {
      pi.x = (tb-ta)/(ma-mb);
      pi.y = ma*pi.x+ta;
    }
    return pi;
  }
  
  /**
   * Check if x is right end of l
   *
   * @param x an x-coordinate of one endpoint
   * @param l a line
   * @return <code>true</code> if p is right end of l <code>false</code>
   * otherwise
   */
  private static boolean isRightEnd( double x, LineSegment l )
  {
    // TODO: Do I need EPSILON here?
    return x >= l.first.x && x >= l.second.x;
  }

  /**
   * Get all interectionLines by applying a sweep line algorithm.
   *
   * @param lines all lines you want to check, in no order
   * @return a list that contains all pairs of intersecting lines
   */
  public static Set<LineSegment[]> getAllIntersectingLines( LineSegment[] lines )
  {
    // TODO: This one is buggy! See tests
    class EventPointLine
            implements Comparable<EventPointLine>
    {

      Double sortingKey;
      LineSegment line;

      public EventPointLine( double sortingKey, LineSegment line )
      {
        this.sortingKey = sortingKey;
        this.line = line;
      }

      @Override
      public int compareTo( EventPointLine o )
      {
        return sortingKey.compareTo( o.sortingKey );
      }
    }

    class SweepLineComperator
            implements Comparator<LineSegment>
    {

      @Override
      public int compare( LineSegment o1, LineSegment o2 )
      {
        double o1FirstX = o1.first.x < o1.second.x ? o1.first.y
                : o1.second.y;
        double o2FirstX = o2.first.x < o2.second.x ? o2.first.y
                : o2.second.y;

        if ( Math.abs( o1FirstX - o2FirstX ) < EPSILON )
        {
          return 0;
        }
        else if ( o1FirstX > o2FirstX )
        {
          return 1;
        }
        else
        {
          return -1;
        }
      }
    }

    Set<LineSegment[]> intersections = new HashSet<LineSegment[]>();
    List<EventPointLine> eventPointSchedule = new ArrayList<EventPointLine>();

    for ( LineSegment line : lines )
    {
      eventPointSchedule.add( new EventPointLine( line.first.x, line ) );
      eventPointSchedule.add( new EventPointLine( line.second.x, line ) );
    }

    Collections.sort( eventPointSchedule );

    SweepLineComperator comperator = new SweepLineComperator();
    TreeSet<LineSegment> sweepLine = new TreeSet<LineSegment>( comperator );

    for ( EventPointLine p : eventPointSchedule )
    {
      // TODO: an schnittpunkten aendert sich die Reihenfolge
      // der Kanten
      if ( isRightEnd( p.sortingKey, p.line ) )
      {
        LineSegment above = sweepLine.higher( p.line );
        LineSegment below = sweepLine.lower( p.line );
        sweepLine.remove( p.line );

        if ( below != null && above != null
                && doLinesIntersect( above, below ) )
        {
          LineSegment[] tmp = new LineSegment[2];
          tmp[0] = above;
          tmp[1] = p.line;
          intersections.add( tmp );
        }
      }
      else if ( Math.abs( p.line.first.x - p.line.second.x ) < EPSILON )
      {
        // this is a vertical line

        for ( LineSegment tmpLine : sweepLine )
        {
          if ( doLinesIntersect( tmpLine, p.line ) )
          {
            LineSegment[] tmp = new LineSegment[2];
            tmp[0] = tmpLine;
            tmp[1] = p.line;
            intersections.add( tmp );
          }
        }
      }
      else
      {

        // Get identical lines
        NavigableSet<LineSegment> h = sweepLine.subSet( p.line,
                                                        true, p.line, true );

        for ( LineSegment tmpLine : h )
        {
          if ( doLinesIntersect( tmpLine, p.line ) )
          {
            LineSegment[] tmp = new LineSegment[2];
            tmp[0] = tmpLine;
            tmp[1] = p.line;
            intersections.add( tmp );
          }

        }

        sweepLine.add( p.line );

        // check if it intersects with line above or below
        LineSegment above = sweepLine.higher( p.line );
        LineSegment below = sweepLine.lower( p.line );

        if ( above != null && doLinesIntersect( above, p.line ) )
        {
          LineSegment[] tmp = new LineSegment[2];
          tmp[0] = above;
          tmp[1] = p.line;
          intersections.add( tmp );
        }

        if ( below != null && doLinesIntersect( below, p.line ) )
        {
          LineSegment[] tmp = new LineSegment[2];
          tmp[0] = below;
          tmp[1] = p.line;
          intersections.add( tmp );
        }
      }
    }

    /* Check if endpoints are equal */
    for ( int i = 0; i < eventPointSchedule.size(); i++ )
    {
      int j = i + 1;
      while ( j < eventPointSchedule.size()
              && Math.abs( eventPointSchedule.get( i ).sortingKey
                      - eventPointSchedule.get( j ).sortingKey ) < EPSILON )
      {
        j += 1;

        LineSegment[] tmp = new LineSegment[2];
        tmp[0] = eventPointSchedule.get( i ).line;
        tmp[1] = eventPointSchedule.get( j ).line;
        if ( doLinesIntersect( tmp[0], tmp[1] )
                && !intersections.contains( tmp ) )
        {
          intersections.add( tmp );
        }
      }
    }

    return intersections;
  }

  /**
   * Get all interectionLines by applying a brute force algorithm.
   *
   * @param lines all lines you want to check, in no order
   * @return a list that contains all pairs of intersecting lines
   */
  public static Set<LineSegment[]> getAllIntersectingLinesByBruteForce(
          LineSegment[] lines )
  {
    Set<LineSegment[]> intersections = new HashSet<LineSegment[]>();

    for ( int i = 0; i < lines.length; i++ )
    {
      for ( int j = i + 1; j < lines.length; j++ )
      {
        if ( doLinesIntersect( lines[i], lines[j] ) )
        {
          LineSegment[] tmp = new LineSegment[2];
          tmp[0] = lines[i];
          tmp[1] = lines[j];
          intersections.add( tmp );
        }
      }
    }

    return intersections;
  }

  public static boolean isLeftBend( Point2D.Double i, Point2D.Double j,
                                    Point2D.Double k )
  {
    Point2D.Double pi = new Point2D.Double( i.x, i.y );
    Point2D.Double pj = new Point2D.Double( j.x, j.y );
    Point2D.Double pk = new Point2D.Double( k.x, k.y );

    // Move pi to (0,0) and pj and pk with it
    pj.x -= pi.x;
    pk.x -= pi.x;
    pj.y -= pi.y;
    pk.y -= pi.y;
    LineSegment s = new LineSegment( pi, pj );

    // Move pj to (0,0) and pk with it
    pk.x -= pj.x;
    pk.y -= pj.y;

    return !(isPointRightOfLine( s, pk ) || isPointOnLine( s, pk ));
  }

  /**
   * Calculate the convex hull of points with Graham Scan
   *
   * @param points a list of points in any order
   * @return the convex hull (can be rotated)
   */
  public static List<Point2D.Double> getConvexHull( List<Point2D.Double> points )
  {
    // TODO: Doesn't work by now
    List<Point2D.Double> l = new ArrayList<Point2D.Double>();

    // find lowest point. If there is more than one lowest point
    // take the one that is left
    Point2D.Double pLow = new Point2D.Double( 0, Double.POSITIVE_INFINITY );
    for ( Point2D.Double point : points )
    {
      if ( point.y < pLow.y || (point.y == pLow.y && point.x < pLow.x) )
      {
        pLow = point;
      }
    }

    // Order all other points by angle
    class PointComparator
            implements Comparator<Point2D.Double>
    {

      Point2D.Double pLow;

      public PointComparator( Point2D.Double pLow )
      {
        this.pLow = pLow;
      }

      private double getAngle( Point2D.Double p )
      {
        // TODO: This is buggy
        double deltaX = pLow.x - p.x;
        double deltaY = pLow.y - p.y;
        if ( deltaX < EPSILON )
        {
          return 0;
        }
        else
        {
          return deltaY / deltaX;
        }
      }

      @Override
      public int compare( Point2D.Double o1, Point2D.Double o2 )
      {
        double a1 = getAngle( o1 );
        double a2 = getAngle( o2 );
        if ( Math.abs( a1 - a2 ) < EPSILON )
        {
          return 0;
        }
        else
        {
          return a1 < a2 ? -1 : 1;
        }
      }
    }

    PointComparator comparator = new PointComparator( pLow );

    Collections.sort( points, comparator );

    // go through all points
    for ( Point2D.Double tmp : points )
    {
      boolean loop = true;

      while ( loop )
      {
        if ( l.size() < 3 )
        {
          l.add( tmp );
          loop = false;
        }
        else if ( !isLeftBend( l.get( l.size() - 2 ),
                               l.get( l.size() - 1 ), tmp ) )
        {
          l.add( tmp );
          loop = false;
        }
        else
        {
          l.remove( l.size() - 1 );
        }
      }
    }

    return l;
  }

}

  class LineSegment
  {

    Point2D.Double first;
    Point2D.Double second;
    String name;

    /**
     * @param first the first point of this line
     * @param second the second point of this line
     */
    public LineSegment( Point2D.Double a, Point2D.Double b )
    {
      this.first = a;
      this.second = b;
      this.name = "LineSegment";
    }

    public LineSegment( Point2D.Double a, Point2D.Double b, String name )
    {
      this.first = a;
      this.second = b;
      this.name = name;
    }

    /**
     * Get the bounding box of this line by two points. The first point is in
     * the lower left corner, the second one at the upper right corner.
     *
     * @return the bounding box
     */
    public Point2D.Double[] getBoundingBox()
    {
      Point2D.Double[] result = new Point2D.Double[2];
      result[0] = new Point2D.Double( Math.min( first.x, second.x ), Math.min( first.y,
                                                                      second.y ) );
      result[1] = new Point2D.Double( Math.max( first.x, second.x ), Math.max( first.y,
                                                                      second.y ) );
      return result;
    }

    @Override
    public String toString()
    {
      if ( name.equals( "LineSegment" ) )
      {
        return "LineSegment [" + first + " to " + second + "]";
      }
      else
      {
        return name;
      }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((first == null) ? 0 : first.hashCode());
      result = prime * result + ((name == null) ? 0 : name.hashCode());
      result = prime * result + ((second == null) ? 0 : second.hashCode());
      return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals( Object obj )
    {
      if ( this == obj )
      {
        return true;
      }
      if ( obj == null )
      {
        return false;
      }
      if ( getClass() != obj.getClass() )
      {
        return false;
      }
      LineSegment other = (LineSegment) obj;
      if ( first == null )
      {
        if ( other.first != null )
        {
          return false;
        }
      }
      else if ( !first.equals( other.first ) )
      {
        return false;
      }
      if ( name == null )
      {
        if ( other.name != null )
        {
          return false;
        }
      }
      else if ( !name.equals( other.name ) )
      {
        return false;
      }
      if ( second == null )
      {
        if ( other.second != null )
        {
          return false;
        }
      }
      else if ( !second.equals( other.second ) )
      {
        return false;
      }
      return true;
    }
  }
