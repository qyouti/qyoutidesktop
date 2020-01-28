/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bullseye;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.logging.*;
import javax.imageio.*;
import org.apache.commons.math3.geometry.euclidean.twod.Line;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.commons.math3.stat.regression.RegressionResults;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.qyouti.scan.image.ImageResizer;

/**
 * This is the outline of the algorithm...
 * 
 * Input - a greyscale (or colour?) image.

 * 1) Use Sobel transform.  Results in "edginess" and "angle" values
 *    for each pixel.
 * 2) Pick out edgy pixels in turn for each multiple of 45 degrees
 * 3) Do a cluster analysis for each angle
 * 4) If there is one cluster each for NW, NE, SE and SW it might
 *    be an X.
 * 5) Check that NW cluster is NW of SE cluster and that NE cluster is
 *    NE of SW cluster.  If not, it isn't an X after all.
 * 6) If is X but there are clusters of N E S or W then flag as
 *    dubious.
 * 7) If it is not an X but there are any clusters at all then
 *    mark as dubious.
 * 
 * @author jon
 */
public class XLocatorByCluster extends Thread implements XLocator
{
  static final int DIRECTION_NW = 0;  
  static final int DIRECTION_SE = 1;  
  static final int DIRECTION_NE = 2;  
  static final int DIRECTION_SW = 3;  
  
  int debuglevel=0;
  int maxwidth;
  int maxheight;
  SobelResult sobelresult;
  SobelPixelResult[][] sobeldata;
  XLocatorReport currentreport;
  double[] angles = { 45.0, -135.0, -45.0, 135.0 };
  double range = 60.0;  
  //BufferedImage angleimage[] = new BufferedImage[angles.length];
  ArrayList<DBSCANClusterer<DoublePoint>> clusterers;
  
  LinkedList<XLocatorListener> listeners;

  Point bestpoint = null;

  File[] filelist;
  BufferedImage[] inputlist;
  int currentinput;  
  BufferedImage input;
  
  public static final double SOBEL_THRESHOLD = 0.5; //0.45;    
    
  
  /**
   * Instantiates a locator object.  This was a one-shot deal but needs
   * recoding so it can be used repeatedly to avoid creating and destroying
   * lots of arrays.
   * @param maxwidth  The maximum width of images that will be pumped through.
   * @param maxheight Ditto for height.
   */
  public XLocatorByCluster( int maxwidth, int maxheight )
  {
    int i, j;
    this.maxwidth = maxwidth;
    this.maxheight = maxheight;
    
    sobeldata = new SobelPixelResult[maxwidth][maxheight];
    for ( i=0; i<maxwidth; i++ )
      for ( j=0; j<maxheight; j++ )
        sobeldata[i][j] = new SobelPixelResult();

    this.clusterers = new ArrayList<DBSCANClusterer<DoublePoint>>();
    for ( i=0; i<angles.length; i++ )
      this.clusterers.add( new DBSCANClusterer<>( 3.0, 12, new AngleBiasedDistanceMeasure(angles[i]) ) );
        
    listeners = new LinkedList<>();    
  }

  public void setDebugLevel( int n )
  {
    this.debuglevel = n;
  }
  
  public void setImage( BufferedImage input )
  {
    if ( input==null )
      throw new IllegalArgumentException( "Null image." );
    this.inputlist = new BufferedImage[1];
    this.inputlist[0] = input;
    this.filelist = null;
    this.currentinput = 0;
  }

  public void setImages( BufferedImage[] images )
  {
    if ( images==null || images.length == 0 )
      throw new IllegalArgumentException( "Null or empty list of images." );
    this.inputlist = Arrays.copyOf( images, images.length );
    this.filelist = null;
  }

  public void setImageFiles( File[] files )
  {
    if ( files==null || files.length == 0 )
      throw new IllegalArgumentException( "Null or empty list of images." );
    this.filelist = Arrays.copyOf( files, files.length );
    this.inputlist = new BufferedImage[files.length];
  }
  
  public Point[] getLocations()
  {
    Point[] p;
    if ( bestpoint == null )
      return new Point[0];
    
    p = new Point[1];
    p[0] = bestpoint;
    return p;
  }
  
  public BufferedImage getInputImage()
  {
    return input;
  }
    
  public void addProgressListener( XLocatorListener listener )
  {
    listeners.add( listener );
  }
  
  public void removeProgressListener( XLocatorListener listener )
  {
    listeners.remove( listener );
  }
  
  void notifyListeners( int percentage, boolean complete, BufferedImage debugimage, String message )
  {
    Iterator<XLocatorListener> iter = listeners.iterator();
    XLocatorListener listener;
    while ( iter.hasNext() )
    {
      listener = iter.next();
      if ( percentage >= 0 ) listener.notifyProgress( percentage );
      if ( debugimage != null || message != null ) listener.notifyDebugMessage( debugimage, message );
      if ( complete ) listener.notifyComplete( currentreport, currentinput );
    }
  }
  
  // private boolean trigger = false;
  
  
  ArrayList<DoublePoint> getClusterables( SobelResult sobres, double mid, double range )
  {
    double angle, upper, lower;
    int[] p;
    ArrayList<DoublePoint> col = new ArrayList<>();
    
    for ( int x=0; x<sobres.width; x++ )
      for ( int y=0; y<sobres.height; y++ )
      {
        if ( sobres.results[x][y].magnitude == 0 )
          continue;
        angle = sobres.results[x][y].angle;
        upper = mid+(range/2.0);
        lower = mid-(range/2.0);
        if ( upper >  180.0 ) angle += 360.0;
        if ( lower < -180.0 ) angle -= 360.0;
        if ( angle > lower && angle < upper )
        {
          p = new int[2];
          p[0] = x;
          p[1] = y;
          col.add( new DoublePoint( p ) );
        }
      }
    
    return col;
  }
  
  
  BufferedImage clusterablesToImage( Collection<DoublePoint> list, int width, int height )
  {
    BufferedImage image = new BufferedImage( width, height, BufferedImage.TYPE_BYTE_BINARY );
    double[] point;
    
    for ( DoublePoint c : list )
    {
      point = c.getPoint();
      image.setRGB( (int)point[0], (int)point[1], 0xffffff );
    }
    
    return image;
  }
  
  BufferedImage areaToImage( Area area, int width, int height )
  {
    BufferedImage image = new BufferedImage( width, height, BufferedImage.TYPE_BYTE_BINARY );
    double[] point;
    
    Graphics2D g = image.createGraphics();
    g.setColor( Color.white );
    g.fill( area );
    g.dispose();
    
    return image;
  }
  
  
  public void pass() // PassResult result )
  {
    int goodcount=0;
    int badcount=0;
    
    int kernelsize = 3;
    bestpoint = null;
    // Transform using the red channel - so magenta ink/toner is ignored.
    sobelresult = Sobel.transform( input, SOBEL_THRESHOLD, true, false, false, kernelsize, sobeldata );  
    //System.out.println("Maximum Sobel magnitude: " + sobelresult.maxmag );
    if ( debuglevel >= 2 )
      notifyListeners( -1, false, ImageResizer.resize(sobelresult.toImage(),input.getWidth()*4,input.getHeight()*4), "Sobel Transform" );
    
    boolean isx=true;
    
    List<Cluster<DoublePoint>> clusters;
    ArrayList<DoublePoint> list;
    int count=0;
    int[] clustcount = new int[angles.length];
    ArrayList<ClusterData> clusterdatalist = new ArrayList<>();
    byte[] red = { (byte)0xff, (byte)0,    (byte)0xff,  (byte)0xff };
    byte[] grn = { (byte)0xff, (byte)0xff, (byte)0,     (byte)0xff };
    byte[] blu = { (byte)0xff, (byte)0,    (byte)0,     (byte)0    };
    BufferedImage forandagainst = new BufferedImage( input.getWidth(), input.getHeight(), 
            BufferedImage.TYPE_BYTE_BINARY, new IndexColorModel(2,4,red,grn,blu) );

    for ( int a=0; a<angles.length; a++ )
    {
      // Get coordinates of pixels with the given angle
      list = this.getClusterables( sobelresult, angles[a], range );
      if ( debuglevel >= 2 )
        notifyListeners( -1, false, this.clusterablesToImage( list, input.getWidth(), input.getHeight() ), "Angle " + angles[a] );
      
      // Find the clusters of pixel coordinates from the list
      clusters = clusterers.get(a).cluster(list);
      clustcount[a] = clusters.size();
      count+=clustcount[a];
      for ( int c=0; c<clusters.size(); c++)
      {
        Cluster<DoublePoint> cluster = clusters.get( c );
        notifyListeners( -1, false, this.clusterablesToImage( cluster.getPoints(), input.getWidth(), input.getHeight() ), "Angle " + angles[a] + " cluster " + c );
        clusterdatalist.add( new ClusterData( a, clusters.get(c) ) );
      }
    }
    
    for ( ClusterData cd : clusterdatalist )
      cd.compute();
    
    ClusterBucketSet clusterbucketset = new ClusterBucketSet( clusterdatalist );
//    int permutationcount = clusterbucketset.getPermutationCount( clusterdatalist );
//    notifyListeners( -1, false, null, "Cluster Bucket Permutation count " + permutationcount );
//    for ( int p = 0; p<permutationcount; p++ )
//    {
//      clusterbucketset.setPermutation( p, clusterdatalist );
//    }
    
    currentreport.hasX = false;
    currentreport.dubious = true;
    
    // hasX == there is at least one good cluster in each of four angles
//    currentreport.hasX = isx;
  
//    if ( debuglevel >= 2 )
//      notifyListeners( -1, false, ImageResizer.resize(forandagainst,input.getWidth()*4,input.getHeight()*4), "Good only" );
//    for ( int x=0; x<sobelresult.width; x++ )
//      for ( int y=0; y<sobelresult.height; y++ )
//      {
//        SobelPixelResult spr = sobelresult.results[x][y];
//        if ( spr.magnitude == 0.0 )
//          continue;
//        int rgb = forandagainst.getRGB( x, y );
//        if ( (rgb & 0xffffff) != 0xffffff )
//          continue;
//        int adjacentcount=0;
//        int otherrgb;
//        for ( int dx=x-1; dx>=0 && dx<sobelresult.width && dx<=x+1; dx++ )
//          for ( int dy=y-1; dy>=0 && dy<sobelresult.height && dy<=y+1; dy++ )
//          {
//            if ( dx==0 && dy == 0 ) continue;
//            otherrgb = forandagainst.getRGB(dx, dy) & 0xffffff;
//            if ( otherrgb == 0x00ff00 )
//              adjacentcount++;
//          }
//        if ( adjacentcount >=3 )
//        {
//          forandagainst.setRGB(x, y, 0xffff00 );
//        }
//        else
//        {
//          forandagainst.setRGB(x, y, 0xff0000 );
//          badcount++;
//        }
//      }
//    if ( debuglevel >= 2 )
//      notifyListeners( -1, false, ImageResizer.resize(forandagainst,input.getWidth()*4,input.getHeight()*4), "Good count = " + goodcount + " Bad count " + badcount );
//     
//    if ( currentreport.hasX )
//    {
//      if ( badcount > goodcount/2.0 )
//        currentreport.dubious = true;
//      if ( badcount > 2.0*goodcount )
//        currentreport.hasX = false;
//    }
//    else
//    {
//      if ( (badcount + goodcount) > 0.02*input.getHeight()*input.getWidth() )
//        currentreport.dubious = true;
//    }
    
    if ( debuglevel >= 2 )
      notifyListeners( -1, false, null, "X = " + currentreport.hasX  + "     Dubious = " + currentreport.dubious );
  }
    
  
  void sleepSeconds( int sec )
  {
    try
    {
      Thread.sleep( sec*1000L );
    }
    catch ( InterruptedException ex )
    {
      Logger.getLogger(XLocatorByCluster.class.getName() ).
              log( Level.SEVERE, null, ex );
    }    
  }
   
  public Point[] locateX()
  {
    int i;
    DecimalFormat df = new DecimalFormat("###.0");
    
    pass();
    Point[] points = getLocations();
    for ( i=0; i<points.length; i++ )
      System.out.println( " centre = " + points[i].x + ", " + points[i].y );
    
    return points;
  }
    
  public void runSynchronously()
  {
    run();
  }
  
  @Override
  public void run()
  {
    notifyListeners( 0, false, null, null );
    for ( currentinput=0; currentinput<inputlist.length; currentinput++ )
    {
      if ( inputlist[currentinput] == null )
      {
        try
        {
          System.out.println( "Loading " + filelist[currentinput] );
          inputlist[currentinput] = ImageIO.read( filelist[currentinput] );
        }
        catch (Exception e)
        {
          e.printStackTrace();
          continue;
        }
        
      }
      input = inputlist[currentinput];
      if ( debuglevel >= 1 )
        notifyListeners(-1, false, input, "Input " + currentinput );
      currentreport = new XLocatorReport();
      currentreport.image = input;
      locateX();
      inputlist[currentinput] = null;
  
      notifyListeners( 100*currentinput/inputlist.length, true, null, null );
    }
    
    notifyListeners( 100, false, null, null );
  }

  class ClusterData
  {
    int direction;
    Cluster<DoublePoint> cluster;

    int n;
    double sumx, sumy;
    SimpleRegression regression;
    RegressionResults regressionresults;
//    boolean good;
    
    ClusterData( int direction, Cluster<DoublePoint> cluster )
    {
      this.direction = direction;
      this.cluster = cluster;
      regression = new SimpleRegression();
    }
    
    void compute()
    {
      regression.clear();
      sumx = 0.0;
      sumy = 0.0;
      n=0;
      for ( DoublePoint point : cluster.getPoints() )
      {
        regression.addData( point.getPoint()[0], point.getPoint()[1] );
        sumx += point.getPoint()[0];
        sumy += point.getPoint()[1];
        n++;
      }
      regressionresults = regression.regress();
    }
    
  }
  
  
  class ClusterBucketResults
  {
    int  n;
    double slope;
    double angle;
    double intercept;
    double meanx;
    double meany;
    double length;
    Point2D.Double max = new Point2D.Double();
    Point2D.Double min = new Point2D.Double();
    double significance;    
    RegressionResults regressionresults;
    boolean rejected_as_line = true;
  }
  
 
  class ClusterBucket
  {
    int direction;
    ArrayList<ClusterData> clusters=new ArrayList<>();
    int permutations;
    ArrayList<ClusterBucketResults> permutationresults = new ArrayList<>();
            
    ArrayList<ClusterData> currentclusters=new ArrayList<>();
    ClusterBucketResults currentresults;
    
    int bestlinepermutation=0;
    int bestlinecappermutation=0;
    
    ClusterBucket( int direction )
    {
      this.direction = direction;
    }
    
    void addCluster( ClusterData cd )
    {
      if ( cd.direction != direction )
        throw new IllegalArgumentException( "Can't add given cluster to bucket - wrong direction." );
      clusters.add(cd);
    }
    
    void computePermutations()
    {
      permutations = 1 << clusters.size();
      System.out.println( "\nCluster Bucket direction " + direction + " clusters " + clusters.size() + " number of permutations " + permutations );
      permutationresults.add( new ClusterBucketResults() );
      
      int bestpixelcount=0;
      bestlinepermutation=0;
      bestlinecappermutation=0;
      for ( int p=1; p<permutations; p++ )
      {
        ClusterBucketResults cbr = computePermutation( p );
        cbr.rejected_as_line = cbr.regressionresults.getMeanSquareError() >= 15.0;
        if ( !cbr.rejected_as_line && cbr.n > bestpixelcount )
        {
          bestpixelcount = cbr.n;
          bestlinepermutation = p;
        }
        permutationresults.add( cbr );
        notifyListeners( -1, false, this.toImage( input.getWidth(), input.getHeight() ), "Permutation " + p  + (cbr.rejected_as_line?" REJECTED AS LINE":"") );
      }
      if ( bestlinepermutation == 0 )
        notifyListeners( -1, false, this.toImage( input.getWidth(), input.getHeight() ), "No Line Found for this directin" );
      else
        notifyListeners( -1, false, this.toImage( input.getWidth(), input.getHeight() ), "The best line is permutation " + bestlinepermutation );
      
      ClusterBucketResults bestlineresults = permutationresults.get(bestlinepermutation);
      for ( int i=0; i<clusters.size(); i++ )
      {
        // skip over clusters that have been identified as in the best line permutation
        if ( ((1 << i) & bestlinepermutation) != 0 )
          continue;
        
      }
    }

    BufferedImage toImage( int width, int height )
    {
      BufferedImage image = new BufferedImage( width, height, BufferedImage.TYPE_BYTE_BINARY );
      double[] point;

      for ( ClusterData cd : currentclusters )
      {
        for ( DoublePoint c : cd.cluster.getPoints() )
        {
          point = c.getPoint();
          image.setRGB( (int)point[0], (int)point[1], 0xffffff );
        }
      }

      return image;
    }
    
    ClusterBucketResults computePermutation( int n )
    {
      System.out.println( "Cluster Bucket direction " + direction + " permutation " + n );
      currentclusters.clear();
      for ( int i=0; i<clusters.size(); i++ )
      {
        if ( (n & (1<<i)) != 0  )
          currentclusters.add( clusters.get(i) );
      }
      return compute();
    }
    
    int getPermutationCount()
    {
      return permutations;
    }
    
    ClusterBucketResults getPermutationResults( int p )
    {
      return permutationresults.get( p );
    }
    
    ClusterBucketResults getBestLinePermutationResults()
    {
      return permutationresults.get( bestlinepermutation );
    }
    
    void clear()
    {
      clusters.clear();
    }
    boolean isEmpty()
    {
      return clusters.size() == 0;
    }
    private ClusterBucketResults compute()
    {
      ClusterBucketResults results = new ClusterBucketResults();
      SimpleRegression regression = new SimpleRegression();
      if ( currentclusters.size() == 0 )
        return results;
      
      double sumx=0.0, sumy=0.0;
      int n=0;
      for ( ClusterData cd : currentclusters )
      {
        n += cd.n;
        sumx += cd.sumx;
        sumy += cd.sumy;
        regression.append( cd.regression );
      }

      results.n = n;
      results.meanx = sumx/n;
      results.meany = sumy/n;
      results.regressionresults = regression.regress();
      results.slope = regression.getSlope();
      results.angle = Math.atan( results.slope );
      results.intercept = regression.getIntercept();
      Point2D.Double start = null;
      Point2D.Double end = null;
      double sqrdistance=0.0;
      double sqrdistancemin=0.0;
      double sqrdistancemax=0.0;
      double dx, dy;
      for ( ClusterData cd : currentclusters )
        for ( DoublePoint point : cd.cluster.getPoints() )
        {
          dx = point.getPoint()[0] - results.meanx;
          dy = point.getPoint()[1] - results.meany;
          sqrdistance = dx*dx + dy*dy;
          if ( dy < 0.0 ) sqrdistance = -sqrdistance;
          if ( start == null || sqrdistance < sqrdistancemin )
          {
            start = new Point2D.Double( point.getPoint()[0], point.getPoint()[1] );
            sqrdistancemin = sqrdistance;
          }
          if ( end == null || sqrdistance > sqrdistancemax )
          {
            end = new Point2D.Double( point.getPoint()[0], point.getPoint()[1] );
            sqrdistancemax = sqrdistance;
          }
        }
      double distancemin = Math.sqrt( Math.abs( sqrdistancemin ) );
      double distancemax = Math.sqrt( Math.abs( sqrdistancemax ) );
      results.min.x = results.meanx + Math.cos( results.angle )*-distancemin;
      results.min.y = results.meany + Math.sin( results.angle )*-distancemin;
      results.max.x = results.meanx + Math.cos( results.angle )*distancemax;
      results.max.y = results.meany + Math.sin( results.angle )*distancemax;
      results.length = distancemax + distancemin;
      results.significance = regression.getSignificance();
      System.out.println( "Cluster bucket direction " + direction + " mean sq err = " + results.regressionresults.getMeanSquareError() );
      System.out.println( "Cluster bucket direction " + direction + " significance = " + results.significance );
      System.out.println( "Cluster bucket direction " + direction + " length = " + results.length  );
      return results;
    }
    
    /**
     * Which clusters consist of 
     * @param a
     * @param b 
     */
    void computeBestLineCapPermutation( Point2D a, Point2D b )
    {
      
    }
  }
  
  class ClusterBucketSet
  {
    ClusterBucket[] buckets;
    boolean isX=false;
    
    ClusterBucketSet( List<ClusterData> clusters )
    {
      buckets = new ClusterBucket[4];
      for ( int d=0; d<buckets.length; d++ )
      {
        buckets[d] = new ClusterBucket( d );
        for ( ClusterData cd : clusters )
          if ( cd.direction == d )
            buckets[d].addCluster(cd);
        buckets[d].computePermutations();
        if ( buckets[d].bestlinepermutation == 0 )
        {
          isX=false;
          return;
        }
      }

      
      // construct two quadrilaterals within which we expect all the
      // dark pixels to be found
      Path2D.Double[] path = new Path2D.Double[2];
      Area[] quadrilateral = new Area[2];
      ClusterBucketResults cbra;
      ClusterBucketResults cbrb;
      Point2D.Double centre;
      Vector2D[] points = new Vector2D[4];
      
      for ( int i=0; i<2; i++ )
      {
        path[i] = new Path2D.Double();
        if ( i==0 )
        {
          cbra = buckets[DIRECTION_NW].getBestLinePermutationResults();
          cbrb = buckets[DIRECTION_SE].getBestLinePermutationResults();
        }
        else
        {
          cbra = buckets[DIRECTION_NE].getBestLinePermutationResults();
          cbrb = buckets[DIRECTION_SW].getBestLinePermutationResults();
        }

        points[0] = new Vector2D( cbra.min.x, cbra.min.y );
        points[1] = new Vector2D( cbra.max.x, cbra.max.y );
        points[2] = new Vector2D( cbrb.max.x, cbrb.max.y );
        points[3] = new Vector2D( cbrb.min.x, cbrb.min.y );
        Line diagonala = new Line( points[0], points[2], 1.5 );
        Line diagonalb = new Line( points[1], points[3], 1.5 );
        Vector2D intersection = diagonala.intersection(diagonalb);
        
        path[i].moveTo( points[0].getX(), points[0].getY() );
        path[i].lineTo( points[1].getX(), points[1].getY() );
        path[i].lineTo( points[2].getX(), points[2].getY() );
        path[i].lineTo( points[3].getX(), points[3].getY() );
        path[i].closePath();
        quadrilateral[i] = new Area( path[i] );
        notifyListeners( -1, false, areaToImage( quadrilateral[i], input.getWidth(), input.getHeight() ), " quadrilateral " + i );
      }
      
      Area cross = new Area( quadrilateral[0] );
      cross.add( quadrilateral[1] );
      notifyListeners( -1, false, areaToImage( cross, input.getWidth(), input.getHeight() ), " cross" );
      
      
//      // look at line end clusters
//      int da, db;
//      boolean max;
//      Point2D a, b;
//      for ( int d=0; d<buckets.length; d++ )
//      {
//        // need to tell this bucket about where lines were found on the
//        // two orthogonal buckets
//        switch ( d )
//        {
//          case DIRECTION_NE:
//            da = DIRECTION_NW;
//            db = DIRECTION_SE;
//            max = true;
//            break;
//          case DIRECTION_NW:
//            da = DIRECTION_NE;
//            db = DIRECTION_SW;
//            max = false;
//            break;
//          case DIRECTION_SE:
//            da = DIRECTION_NE;
//            db = DIRECTION_SW;
//            max = true;
//            break;
//          case DIRECTION_SW:
//            da = DIRECTION_NW;
//            db = DIRECTION_SE;
//            max = false;
//            break;
//          default:
//            da = -1;
//            db = -1;
//            max = true;
//            break;
//        }
//        // we expect line cap pixels to lie between
//        // the ends of the two parallel lines that have been found...
//        if ( buckets[da].bestlinepermutation == 0 )
//          a = null;
//        else
//        {
//          ClusterBucketResults cbr = buckets[da].getBestLinePermutationResults();
//          a = max?cbr.max:cbr.min;
//        }
//        if ( buckets[db].bestlinepermutation == 0 )
//          b = null;
//        else
//        {
//          ClusterBucketResults cbr = buckets[db].getBestLinePermutationResults();
//          b = max?cbr.max:cbr.min;
//        }
//        
//      }
    }

    
    
    
    
//    void setPermutation( int n, List<ClusterData> list )
//    {
//      permutation = n;
//      clear();
//      if ( list.isEmpty() )
//        return;
//      int a=n, type, direction;
//      for ( ClusterData cluster : list )
//      {
//        type = a % 3;
//        a = a/3;
//        if ( type == BUCKET_TYPE_IGNORE )
//          direction = 0;
//        else
//          direction = cluster.direction;
//        buckets[type][direction].clusters.add(cluster);
//      }
//      score();
//    }
//    
//    void score()
//    {
//      significance = 1.0;
//      // worth doing the stats?
//      for ( int d=0; d<buckets[BUCKET_TYPE_LINE].length; d++ )
//      {
//        if ( buckets[BUCKET_TYPE_LINE][d].isEmpty() )
//        {
//          //notifyListeners(-1, false, null, "Non viable permutation - one or more lines absent." );
//          return;
//        }
//      }
//      
//      System.out.println( "Computing permutation " + permutation );
//      for ( int t=0; t<buckets.length; t++ )
//      {
//        if ( t == BUCKET_TYPE_IGNORE )
//          continue;
//        for ( int d=0; d<buckets[t].length; d++ )
//        {
//          if ( buckets[t][d].isEmpty() )
//          {
//            System.out.println( "Empty bucket" );
//            continue;
//          }
//          else
//          {
//            buckets[t][d].compute();
//            significance *= buckets[t][d].significance;
//          }
//        }
//      }
//      System.out.println( "Overall significance = " + significance );
//    }
  }
}
