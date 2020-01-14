/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bullseye;

import java.awt.Point;
import java.awt.image.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.logging.*;
import javax.imageio.*;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.apache.commons.math3.ml.clustering.DoublePoint;
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
    ClusterData[][] clusterdata = new ClusterData[angles.length][];
    byte[] red = { (byte)0xff, (byte)0,    (byte)0xff,  (byte)0xff };
    byte[] grn = { (byte)0xff, (byte)0xff, (byte)0,     (byte)0xff };
    byte[] blu = { (byte)0xff, (byte)0,    (byte)0,     (byte)0    };
    BufferedImage forandagainst = new BufferedImage( input.getWidth(), input.getHeight(), 
            BufferedImage.TYPE_BYTE_BINARY, new IndexColorModel(2,4,red,grn,blu) );
    for ( int a=0; a<angles.length; a++ )
    {
      list = this.getClusterables( sobelresult, angles[a], range );
      if ( debuglevel >= 2 )
        notifyListeners( -1, false, this.clusterablesToImage( list, input.getWidth(), input.getHeight() ), "Angle " + angles[a] );
      
      clusters = clusterers.get(a).cluster(list);
      clustcount[a] = clusters.size();
      count+=clustcount[a];
      clusterdata[a] = new ClusterData[clustcount[a]];
      int bestcluster=-1;
      double dfromcentre, bestdfromcentre=Double.MAX_VALUE;
      for ( int c=0; c<clusters.size(); c++)
      {
        clusterdata[a][c] = new ClusterData();
        clusterdata[a][c].cluster = clusters.get(c);
        clusterdata[a][c].regression = new SimpleRegression();
        clusterdata[a][c].good = false;
        clusterdata[a][c].sumx = 0.0;
        clusterdata[a][c].sumy = 0.0;
        for ( DoublePoint p : clusters.get(c).getPoints() )
        {
          clusterdata[a][c].sumx += p.getPoint()[0];
          clusterdata[a][c].sumy += p.getPoint()[1];
          clusterdata[a][c].regression.addData( p.getPoint()[0], p.getPoint()[1] );
        }
        clusterdata[a][c].meanx = clusterdata[a][c].sumx / clusters.get(c).getPoints().size();
        clusterdata[a][c].meany = clusterdata[a][c].sumy / clusters.get(c).getPoints().size();
        clusterdata[a][c].regression.regress();
        clusterdata[a][c].slope = Math.abs( clusterdata[a][c].regression.getSlope() );
        if ( clusterdata[a][c].slope > 0.3 && clusterdata[a][c].slope < 4.0 && clusterdata[a][c].regression.getSignificance() < 0.005 )
        {
          double dx = input.getWidth()/2 - clusterdata[a][c].meanx;
          double dy = input.getHeight()/2 - clusterdata[a][c].meany;
          dfromcentre = Math.sqrt( dx*dx + dy*dy );
          if ( bestcluster<0 || dfromcentre < bestdfromcentre )
          {
            bestcluster = c;
            bestdfromcentre = dfromcentre;
          }
          clusterdata[a][c].good = true;
        }
        if ( debuglevel >= 2 )
        {
          notifyListeners( -1, false, this.clusterablesToImage( clusters.get(c).getPoints(), input.getWidth(), input.getHeight() ), 
                  "Cluster " + c + (clusterdata[a][c].good?" GOOD":" BAD") );
          notifyListeners( -1, false, null,  "Slope " + clusterdata[a][c].slope );
          notifyListeners( -1, false, null,  "p " + clusterdata[a][c].regression.getSignificance() );
          notifyListeners( -1, false, null,  "r2 " + clusterdata[a][c].regression.getRSquare() );
          notifyListeners( -1, false, null,  "MeanSqErr " + clusterdata[a][c].regression.getMeanSquareError() );
        }
      }
      if ( bestcluster>=0 )
      {
        notifyListeners( -1, false, null,  "Best cluster is cluster for angle " + angles[a] + " is " + bestcluster );
        for ( DoublePoint p : clusterdata[a][bestcluster].cluster.getPoints() )
        {
          forandagainst.setRGB( (int)p.getPoint()[0], (int)p.getPoint()[1], 0x00ff00 );
          goodcount++;
        }
      }
      else
        isx = false;        
    }
    // hasX == there is at least one good cluster in each of four angles
    currentreport.hasX = isx;
  
    if ( debuglevel >= 2 )
      notifyListeners( -1, false, ImageResizer.resize(forandagainst,input.getWidth()*4,input.getHeight()*4), "Good only" );
    for ( int x=0; x<sobelresult.width; x++ )
      for ( int y=0; y<sobelresult.height; y++ )
      {
        SobelPixelResult spr = sobelresult.results[x][y];
        if ( spr.magnitude == 0.0 )
          continue;
        int rgb = forandagainst.getRGB( x, y );
        if ( (rgb & 0xffffff) != 0xffffff )
          continue;
        int adjacentcount=0;
        int otherrgb;
        for ( int dx=x-1; dx>=0 && dx<sobelresult.width && dx<=x+1; dx++ )
          for ( int dy=y-1; dy>=0 && dy<sobelresult.height && dy<=y+1; dy++ )
          {
            if ( dx==0 && dy == 0 ) continue;
            otherrgb = forandagainst.getRGB(dx, dy) & 0xffffff;
            if ( otherrgb == 0x00ff00 )
              adjacentcount++;
          }
        if ( adjacentcount >=3 )
        {
          forandagainst.setRGB(x, y, 0xffff00 );
        }
        else
        {
          forandagainst.setRGB(x, y, 0xff0000 );
          badcount++;
        }
      }
    if ( debuglevel >= 2 )
      notifyListeners( -1, false, ImageResizer.resize(forandagainst,input.getWidth()*4,input.getHeight()*4), "Good count = " + goodcount + " Bad count " + badcount );
     
    if ( currentreport.hasX )
    {
      if ( badcount > goodcount/2.0 )
        currentreport.dubious = true;
      if ( badcount > 2.0*goodcount )
        currentreport.hasX = false;
    }
    else
    {
      if ( (badcount + goodcount) > 0.02*input.getHeight()*input.getWidth() )
        currentreport.dubious = true;
    }
    
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
    Cluster<DoublePoint> cluster;
    double sumx, sumy, meanx, meany;
    SimpleRegression regression;
    double slope;
    boolean good;
  }
}
