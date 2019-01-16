/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bullseye;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.logging.*;
import javax.imageio.*;

/**
 * This is the outline of the algorithm...
 * 
 * Input - a greyscale (or colour?) image.
 * Output - 2D coordinates of the most likely location of an X mark
 * 
 * 1) Start by finding northern inside corner....
 * 2) Use Sobel transform.  Results in "edginess" and "angle" values
 *    for each pixel.
 * 3) Work with interesting pixels - those who's edginess is strong
 *    and whose angle is a) black south east of white or b) black south
 *    west of white.
 * 3) Each interesting pixel casts multiple 'votes' along a line extending
 *    either side along its measured angle.  This is a vote for the where
 *    the north corner is.  Votes will be highest where two lines intersect.
 * 
 * 4) If the lines curve  or are braided due to scribbling a single pixel 
 *    might not be found.
 * 
 *    Simple option is to gaussian 'blur' the vote table. This ought to
 *    result in a single pixel with a local maximum of votes.
 * 
 * 5) Local maximum vote pixels are found - building a list of x, y
 *    coordinates.  Maxima whose votes are considerably lower than the
 *    highest "blurred" vote are discarded.  If there are multiple local
 *    maxima this isn't a single cross.
 * 
 * 6) The same is repeated to find the other inside corners.  A check is made
 *    that they are appropriately oriented to each other. If so the cross
 *    centre is at the mean x, mean y of the four corner points.
 * 
 * 7) Return coordinates of X centre and the 
 * 
 * @author jon
 */
public class XLocatorByVote extends Thread implements XLocator
{
  int debuglevel=0;
  int maxwidth;
  int maxheight;
  SobelPixelResult[][] sobeldata;
  XLocatorReportByVote currentreport;
  
  LinkedList<XLocatorListener> listeners;

  Point bestpoint = null;

  File[] filelist;
  BufferedImage[] inputlist;
  int currentinput;
  
  BufferedImage input;
  Point[][] edge_vote_patterns_a;
  Point[][] edge_vote_patterns_b;
  int[][][] votemapa;
  int[][][] votemapb;
  int[][][] cornervotemap;
  int[][][] smoothvotemap;
  int maxvote;
  ArrayList<LocalMaximum>[] localmaxima;


  
  
  

  static final int VOTE_LENGTH=3;
  
  public static final double SOBEL_THRESHOLD = 0.45; //0.45;
    
  public static final int PIXEL_TYPE_OTHER = -1;
  
  public static final int PIXEL_ANGLES   = 180;
    
  static final int VOTE_SMOOTH_KERNEL_WIDTH = 5;
  static final float[][] VOTESMOOTHKERNEL;
  static  
  {    
    VOTESMOOTHKERNEL = GaussianBlurKernel.generateKernelData2D( VOTE_SMOOTH_KERNEL_WIDTH );
  }
  
  /**
   * Instantiates a locator object.  This was a one-shot deal but needs
   * recoding so it can be used repeatedly to avoid creating and destroying
   * lots of arrays.
   * @param maxwidth  The maximum width of images that will be pumped through.
   * @param maxheight Ditto for height.
   */
  public XLocatorByVote( int maxwidth, int maxheight )
  {
    int i, j;
    this.maxwidth = maxwidth;
    this.maxheight = maxheight;
    
    sobeldata = new SobelPixelResult[maxwidth][maxheight];
    for ( i=0; i<maxwidth; i++ )
      for ( j=0; j<maxheight; j++ )
        sobeldata[i][j] = new SobelPixelResult();
    
    this.localmaxima = new ArrayList[4];
    for ( i=0; i<4; i++ )
      this.localmaxima[i] = new ArrayList<>();
    
    createVotePatterns();

    votemapa       = new int[4][maxwidth][maxheight];
    votemapb       = new int[4][maxwidth][maxheight];
    cornervotemap  = new int[4][maxwidth][maxheight];
    smoothvotemap  = new int[4][maxwidth][maxheight];
    
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


  private void createVotePatterns()
  {
    // create vote patterns by drawing lines into an image
    // and then looking for the coordinates of all the white
    // pixels.
    BufferedImage voteworkspace;
    Graphics2D voteworkspaceg;
    double d;
    int i, x, y, xa, ya, xb, yb, rgb;
    byte[] ch = { (byte)0, (byte)255 };
    edge_vote_patterns_a = new Point[PIXEL_ANGLES][];
    edge_vote_patterns_b = new Point[PIXEL_ANGLES][];

    int w = VOTE_LENGTH*2+5;
    voteworkspace = new BufferedImage(
            w,
            w,
            BufferedImage.TYPE_BYTE_BINARY,
            new IndexColorModel( 1, 2, ch, ch, ch ) );            
    voteworkspaceg = (Graphics2D)voteworkspace.getGraphics();
    voteworkspaceg.setBackground( Color.BLACK );
    voteworkspaceg.setColor( Color.WHITE );
    voteworkspaceg.setStroke( new BasicStroke( 1.5f ) );
    
    double length = (double)VOTE_LENGTH;
    double angle;
    ArrayList<Point> pointlist = new ArrayList<>();
    for ( i=0; i<PIXEL_ANGLES; i++ )
    {
      angle = Math.PI * 2.0 * (double)i / (double)PIXEL_ANGLES;
      x = (int)Math.round( length *  Math.sin( angle ) );
      y = (int)Math.round( length *  -Math.cos( angle ) );
      xa = (w/2)-x;
      ya = (w/2)-y;
      xb = (w/2)+x;
      yb = (w/2)+y;
      voteworkspaceg.clearRect( 0, 0, w, w );      // fill with black
      voteworkspaceg.drawLine( xa, ya, w/2, w/2 ); // white line
      // now find all the white pixels and remember their coords...
      pointlist.clear();
      for ( x=0; x<w; x++ )
        for ( y=0; y<w; y++ )
        {
          rgb = voteworkspace.getRGB( x, y );
          if ( rgb == 0xffffffff )
            pointlist.add( new Point( x-(w/2), y-(w/2) ) );
        }
      edge_vote_patterns_a[i] = new Point[pointlist.size()];
      edge_vote_patterns_a[i] = pointlist.toArray( edge_vote_patterns_a[i] );
      voteworkspaceg.clearRect( 0, 0, w, w );
      voteworkspaceg.drawLine( w/2, w/2, xb, yb );
      // now find all the white pixels and remember their coords...
      pointlist.clear();
      for ( x=0; x<w; x++ )
        for ( y=0; y<w; y++ )
        {
          rgb = voteworkspace.getRGB( x, y );
          if ( rgb == 0xffffffff )
            pointlist.add( new Point( x-(w/2), y-(w/2) ) );
        }
      edge_vote_patterns_b[i] = new Point[pointlist.size()];
      edge_vote_patterns_b[i] = pointlist.toArray( edge_vote_patterns_b[i] );
  
      //notifyListeners( 0, false, false, voteworkspace );
      //sleepSeconds(1);
    }
    voteworkspaceg.dispose();
  }
  
  Point[] getVotePattern( double angle, boolean direction )
  {
    int i = (int)Math.round( PIXEL_ANGLES * angle/(Math.PI*2.0) );
    if ( i < 0 ) i += PIXEL_ANGLES;
    return direction?edge_vote_patterns_a[i]:edge_vote_patterns_b[i];
  }
  
  
  void resetVoteMap( int[][] map )
  {
    for ( int i=0; i<map.length; i++ )
      for ( int j=0; j<map[i].length; j++ )
        map[i][j] = 0;
  }
  
  void resetAllVoteMaps()
  {  
    for ( int corner=0; corner<4; corner++ )
    {
      resetVoteMap( votemapa[corner]      );
      resetVoteMap( votemapb[corner]      );
      resetVoteMap( cornervotemap[corner] );
      resetVoteMap( smoothvotemap[corner] );
    }
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
  
  public Point[] getCornerLocations( int corner )
  {
    Point[] points = new Point[localmaxima[corner].size()];
    LocalMaximum lm;
    for ( int i=0; i<points.length; i++ )
    {
      lm = localmaxima[corner].get( i );
      points[i] = new Point( lm.x, lm.y );
    }
    return points;
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
  
  double multiplyAndAccumulate( double[] a, double[] b )
  {
    if ( a==null || b==null || a.length != b.length )
      throw new IllegalArgumentException( "Need arrays of equal length" );

    double sum = 0.0;
    for ( int i=0; i<a.length; i++ )
      sum += a[i] * b[i];
    return sum;
  }
  
  void multiplyVotes( int[][] a, int[][]b, int[][]c )
  {
    int i, j;
    for ( i=0; i<a.length; i++ )
      for ( j=0; j<a[i].length; j++ )
        c[i][j] = a[i][j] * b[i][j];
  }
  
  private boolean trigger = false;
  
  
  void applyVotes( int x, int y, SobelPixelResult sobelpixel, int direction, boolean a )
  {
    // Classify pixels, finding horizontal and vertical edges
    int type = PIXEL_TYPE_OTHER;

    if ( sobelpixel.magnitude != 0.0 )
    {
      type = (int) Math.round( (sobelpixel.smooth_angle / 360.0) * (double)PIXEL_ANGLES);
      if ( type < 0 ) type += PIXEL_ANGLES;
    }

    if ( type != PIXEL_TYPE_OTHER )
      applyVotes( x, y, type, direction, a  );
  }
  
  void applyVotes( int x, int y, int type, int direction, boolean a )
  {
    int i, dx, dy, lx, ly, votes;

    
    if ( type == PIXEL_TYPE_OTHER )
      return;
  
    int segment = -1;
    switch ( direction )
    {
      case 0:
        if ( type >=    5 && type <=  40 ) break;
        return;
      case 1:
        if ( type >=   50 && type <=  85 ) break;
        return;
      case 2:
        if ( type >=   95 && type <= 130 ) break;
        return;
      case 3:
        if ( type >=  140 && type <= 175 ) break;
        return;        
    }
    
//    if ( corner == 0 && segment != 1 && segment != 2 ) return;
//    if ( corner == 1 && segment != 2 && segment != 3 ) return;
//    if ( corner == 2 && segment != 3 && segment != 4 ) return;
//    if ( corner == 3 && segment != 4 && segment != 1 ) return;

//    if ( trigger ) return;
//    trigger = true;
    
    Point p;
    Point[] pattern = a?edge_vote_patterns_a[type]:edge_vote_patterns_b[type];
    int[][] vm = a?votemapa[direction]:votemapb[direction];
    //System.out.println( "Pixel votes" );
    for ( i=0; i<pattern.length; i++ )
    {
      // point relative to x,y where vote will be cast
      p = pattern[i];
      lx = x + p.x;
      ly = y + p.y;

      // out of bounds? Skip
      if ( lx < 0 || lx >= vm.length || ly < 0 || ly >= vm[lx].length )
        continue;

      // cast vote
      vm[lx][ly]++;
      //System.out.println( "Voted at " + lx + ", " + ly );
    }
  }
  
  
  void smoothVotes( int corner )
  {
    int x, y, kx, ky, w, h, i, j;
    int koff = VOTESMOOTHKERNEL.length / 2;
    float total;
    w = input.getWidth();
    h = input.getHeight();
    
    for ( x=0; x<w; x++ )
      for ( y=0; y<h; y++ )
      {
        total = 0.0f;
        for ( i=0; i<VOTESMOOTHKERNEL.length; i++ )
          for ( j=0; j<VOTESMOOTHKERNEL[i].length; j++ )
          {
            kx = x-koff+i;
            ky = y-koff+j;
            if ( kx >= 0 && kx < w && ky >=0 && ky < h )
              total += VOTESMOOTHKERNEL[i][j] * (float)cornervotemap[corner][kx][ky];
          }
        // vote is multiplied by 1000 to avoid losing precision with conversion
        // to integer.
        smoothvotemap[corner][x][y] = (int)Math.round( (double)total*1000.0 );
      }
  }
  
  void findLocalMaximaVotes( int corner )
  {
    int x, y, w, h, i, j;
    boolean failed;
    w = input.getWidth();
    h = input.getHeight();
    int nextgroupid=1, groupid;
    LocalMaximum other;
    
    localmaxima[corner].clear();
    if ( maxvote == 0 )
      return;
    
    for ( x=0; x<w; x++ )
    {
      for ( y=0; y<h; y++ )
      {
        failed = false;
        
        // ignore votes that are a lot lower than the maximum
        if ( smoothvotemap[corner][x][y] < (2*maxvote/3) )
          failed = true;
        
        // ignore votes around the edges
        if ( x<1 || y<1 || x>(w-2) || y>(h-2) )
          failed = true;
        
        // check all nine neighbouring pixels
        for ( i=-1; i<=1; i++ )
        {
          if ( failed ) break;
          for ( j=-1; j<=1; j++ )
          {
            // don't compare to self
            if ( i==0 && j==0 )
              continue;
            // not a local maximum if a neighbour is higher
            if ( smoothvotemap[corner][x+i][y+j] > smoothvotemap[corner][x][y] )
            {
              failed = true;
              break;
            }
          }
        }
        
        if ( !failed )
        {
          // this may be a local maximum but there might have been a neighbour with
          // an equal vote - a 'plateau'.  If so, try identifying it
          groupid = nextgroupid++;
          for ( i=0; i<localmaxima[corner].size(); i++ )
          {
            other = localmaxima[corner].get( i );
            if ( x>=(other.x-1) && x<=(other.x+1) && y>=(other.y-1) && y<=(other.y+1) )
            {
              groupid = other.groupid;
              break;
            }
          }
          localmaxima[corner].add( new LocalMaximum( x, y, smoothvotemap[corner][x][y], groupid ) );
        }
      }
    }
    
    // deal with plateaus..
    int n, sumx, sumy;
    LocalMaximum first;
    for ( j=0; j<nextgroupid; j++ )
    {
      n=0; 
      sumx=0; 
      sumy=0;
      first=null;
      for ( i=0; i<localmaxima[corner].size(); i++ )
      {
        other = localmaxima[corner].get( i );        
        if ( other.groupid != j )
          continue;
        n++;
        sumx += other.x;
        sumy += other.y;
        if ( n==1 )
          first = other;
        else
        {
          // make the coordinates of the first entry in the group
          // the centroid of all the coordinates of the group
          first.x = sumx/n;
          first.y = sumy/n;
        }
      }
    }
    
    // now delete all the redundant local minima
    for ( j=0; j<nextgroupid; j++ )
    {
      n=0; 
      first=null;
      for ( i=0; i<localmaxima[corner].size(); i++ )
      {
        other = localmaxima[corner].get( i );        
        if ( other.groupid != j )
          continue;
        n++;
        if ( n==1 )
          first = other;
        else
          localmaxima[corner].remove( i-- );
      }
    }
  }
  
  void voteStats( int[][] vm )
  {
    int x, y, w, h;
    w = input.getWidth();
    h = input.getHeight();
    // some stats...
    maxvote=0;
    for ( x=0; x<w; x++ )
      for ( y=0; y<h; y++ )
        if ( vm[x][y] > maxvote )
          maxvote = vm[x][y];
  }
  
  BufferedImage votesToImage( int[][] votemap, int maxvote )
  {
    int x, y, w, h;
    
    w=input.getWidth();
    h=input.getHeight();
    BufferedImage votemapimage = new BufferedImage(
              w,
              h,
              BufferedImage.TYPE_INT_RGB );
    for ( x=0; x<input.getWidth(); x++ )
      for ( y=0; y<input.getHeight(); y++ )
        votemapimage.setRGB( x, y, 0 );

    int vote, rgb;
    for ( x=0; x<w; x++ )
      for ( y=0; y<h; y++ )
      {        
        vote = votemap[x][y];        
        if ( maxvote > 0 )
          rgb = (255 * vote / maxvote) << 16  | (255 * vote / maxvote) << 8 | (255 * vote / maxvote);
        else
          rgb = 0;
        rgb = rgb | 0xff000000;
        votemapimage.setRGB( x, y, rgb );
      }
    
    return votemapimage;
  }
  
  void processAllVotes( int corner )
  {
    int x, y, w, h, i;

    w = input.getWidth();
    h = input.getHeight();

    voteStats( cornervotemap[corner] );
    
    if ( debuglevel >= 3 )
      notifyListeners(-1, false, votesToImage( cornervotemap[corner], maxvote ), "Votes" );
    
    smoothVotes( corner );
    
    voteStats( smoothvotemap[corner] );
    findLocalMaximaVotes( corner );
    //System.out.println( "local maxima " + localmaxima[corner].size() );

    if ( debuglevel >= 3 )
    {
      BufferedImage smoothvotemapimage = votesToImage( smoothvotemap[corner], maxvote );
      LocalMaximum max;
      for ( i=0; i<localmaxima[corner].size(); i++ )
      {
        max = localmaxima[corner].get( i );
        System.out.println( max );
        smoothvotemapimage.setRGB( max.x, max.y, 0x0000ff );
      }
      notifyListeners(-1, false, smoothvotemapimage, "Smoothed Votes" );
    }    
  }
  
  
  public void processCorners()
  {
    int inorth, isouth, ieast, iwest, i;
    LocalMaximum north, south, east, west, loc;
    int[] bestperm = {-1,-1,-1,-1};
    double score, sumx, sumy, nslen, ewlen, nclen, wclen;
    Point2D.Double pn=new Point2D.Double(), ps=new Point2D.Double(), 
                   pe=new Point2D.Double(), pw=new Point2D.Double();
    Point2D.Double intercept;
    
    double xQuality = -1.0;
    currentreport.xLocation = null;
    
    for ( isouth=0; isouth<localmaxima[0].size(); isouth++ )
      for (  iwest=0;  iwest<localmaxima[1].size();  iwest++ )
        for ( inorth=0; inorth<localmaxima[2].size(); inorth++ )
          for (  ieast=0;  ieast<localmaxima[3].size();  ieast++ )
          {
            south = localmaxima[0].get( isouth );
            west  = localmaxima[1].get( iwest );
            north = localmaxima[2].get( inorth );
            east  = localmaxima[3].get( ieast );
            
            ps.setLocation( south.x, south.y );
            pw.setLocation( west.x,  west.y );
            pn.setLocation( north.x, north.y );
            pe.setLocation( east.x,  east.y );
            
            if ( ps.equals( pn ) ) continue;
            if ( pw.equals( pe ) ) continue;

            
            intercept = Geometry.getLineSegmentIntersection( ps, pn, pe, pw );
            if ( intercept == null ) continue;
            System.out.println( "Testing permutation " + isouth + " " + iwest + " " + isouth + " " + ieast );

            nslen = pn.distance( ps );
            ewlen = pw.distance( pe );
            if ( nslen < 2.0 || ewlen < 2.0 )
            {
              System.out.println( "Points of interest too close." );
              continue;
            }

            double currentangle = Math.atan2( ps.y - intercept.y, ps.x - intercept.x )- 
                    Math.atan2( pw.y - intercept.y, pw.x - intercept.x );
            while ( currentangle >  Math.PI ) currentangle -= Math.PI;
            while ( currentangle < -Math.PI ) currentangle += Math.PI;
            // don't care about direction of the angle...
            currentangle = Math.abs( currentangle );
            // want the acute angle
            if ( currentangle > Math.PI/2.0 )
              currentangle = Math.PI - currentangle;
            System.out.println( "currentangle " + currentangle );
            if ( currentangle < Math.PI/4 )
            {
              System.out.println( "angle too acute" );
              continue;
            }
            if ( north.y >= intercept.y )
            {
              System.out.println( "North point not north of intercept" );
              continue;
            }
            if ( south.y <= intercept.y )
            {
              System.out.println( "South point not south of intercept" );
              continue;
            }
            if ( west.x >= intercept.x )
            {
              System.out.println( "West point not west of intercept" );
              continue;
            }
            if ( east.x <= intercept.x )
            {
              System.out.println( "East point not east of intercept" );
              continue;
            }
            
            nclen = pn.distance( intercept );
            wclen = pw.distance( intercept );
            
            // if shortest line much shorter than longest line...
            double currentlineratio = Math.min( nslen, ewlen ) / Math.max( nslen, ewlen );
            System.out.println( "currentlineratio " + currentlineratio );
            if ( currentlineratio < 0.25 )
            {
              System.out.println( "Lines different lengths" );
              continue;
            }
            
            // if intercept too far from centre of line
            double currentnorthsouthbias = Math.abs( nclen - nslen/2 ) / (nslen/2);
            System.out.println( "currentnorthsouthbias " + currentnorthsouthbias );
            if ( currentnorthsouthbias > 0.75 )
            {
              System.out.println( "Intercept too far from middle of north-south line" );
              continue;
            }
            double currenteastwestbias = Math.abs( wclen - ewlen/2 ) / (ewlen/2);
            System.out.println( "currenteastwestbias " + currenteastwestbias );
            if ( currenteastwestbias > 0.75 )
            {
              System.out.println( "Intercept too far from middle of west-east line" );
              continue;
            }
            
            // shortest pair of lines scores highest
            score = 1.0/((nslen) + (ewlen));
            
            if ( score > xQuality )
            {
              xQuality = score;
              bestperm[0] = isouth;
              bestperm[1] = iwest;
              bestperm[2] = inorth;
              bestperm[3] = ieast;
              sumx = 0.0;
              sumy = 0.0;
              for ( i=0; i<4; i++ )
              {
                loc = localmaxima[i].get( bestperm[i] );
                sumx += loc.x;
                sumy += loc.y;
              }
              currentreport.xLocation = new Point( Math.round( (float)sumx/4.0f ), Math.round( (float)sumy/4.0f ) );
            }
          }
    currentreport.hasX = currentreport.xLocation != null;
    currentreport.additionalPointsofInterest = new ArrayList<>();
    currentreport.xPointsofInterest = new ArrayList<>();
    Point p;
    for ( int map=0; map<4; map++ )
      for ( i=0; i<localmaxima[map].size(); i++ )
      {
        loc = localmaxima[map].get( i );
        p = new Point( loc.x, loc.y );
        if ( i == bestperm[map] )
          currentreport.xPointsofInterest.add( p );
        else
          currentreport.additionalPointsofInterest.add( p );
      }
  }
  
  public void pass() // PassResult result )
  {
    int x, y, type, corner, edge;
    int kernelsize = 3;
    SobelResult sobelresult;
    SobelPixelResult sobelpixel;
    
    bestpoint = null;
    resetAllVoteMaps();

    // Transform using the red channel - so magenta ink/toner is ignored.
    sobelresult = Sobel.transform( input, SOBEL_THRESHOLD, true, false, false, kernelsize, sobeldata );
    //System.out.println("Maximum Sobel magnitude: " + sobelresult.maxmag );
    currentreport.percentageBorderEdgePixels = sobelresult.percentageBorderEdgePixels;
    currentreport.percentageCentreEdgePixels = sobelresult.percentageCentreEdgePixels;
    if ( debuglevel >= 2 )
      notifyListeners( -1, false, sobelresult.toImage(), "Sobel Transform" );

    
    // iterate over all the pixels of the input image
    // for each pixel votes in eight vote maps
    for ( x=(kernelsize/2); x<(input.getWidth()-(kernelsize/2)); x++ )
      for ( y=(kernelsize/2); y<(input.getHeight()-(kernelsize/2)); y++ )
      {        
        sobelpixel = sobelresult.results[x][y];
        for ( corner=0; corner<4; corner++ )
        {
          applyVotes( x, y, sobelpixel, corner, true  );
          applyVotes( x, y, sobelpixel, corner, false  );
        }
      }

    
    if ( debuglevel >= 3 )
    {
      for ( edge=0; edge<4; edge++ )
      {
        voteStats( votemapa[edge] );
        notifyListeners(-1, false, votesToImage( votemapa[edge], maxvote ), "Votes A " + edge );
        voteStats( votemapb[edge] );
        notifyListeners(-1, false, votesToImage( votemapb[edge], maxvote ), "Votes B " + edge );
      }
    }
    
    // find the product of four pairs of votemap to find the
    // inside diagonal corners
    for ( corner=0; corner<4; corner++ )
      multiplyVotes( votemapb[corner], votemapa[(corner+1)%4], cornervotemap[corner] );
    
    // process each of the four product vote maps
    for ( corner=0; corner<4; corner++ )
      processAllVotes( corner );
    
    // Build a report based on points of interest
    // in the four maps
    processCorners();
    if ( currentreport.hasX )
    {
      if ( currentreport.additionalPointsofInterest.size() != 0 )
        currentreport.dubious = true;
    }
    else
    {
      if ( currentreport.percentageCentreEdgePixels > 5.0 )
        currentreport.dubious = true;      
    }
  }
  
  void sleepSeconds( int sec )
  {
    try
    {
      Thread.sleep( sec*1000L );
    }
    catch ( InterruptedException ex )
    {
      Logger.getLogger(XLocatorByVote.class.getName() ).
              log( Level.SEVERE, null, ex );
    }    
  }
   
  public Point[] locateX()
  {
    int i;
    DecimalFormat df = new DecimalFormat("###.0");
    
    pass();// trial );
    Point[] points = getLocations();
    for ( i=0; i<points.length; i++ ) //trial.centre != null )
      System.out.println( " centre = " + points[i].x + ", " + points[i].y );
    
    //dumpHistogram();
    
    return points;
  }
  
  /**
   * Try to remove JPEG encoding artifacts with a low radius blur.
   */
  public void prefilter()
  {
    int x, y, xk, yk, w, h, sum, rgb;
    w = input.getWidth();
    h = input.getHeight();
    BufferedImage filtered = new BufferedImage( w, h, input.getType() );
    for ( x=0; x<w; x++ )
      for ( y=0; y<h; y++ )
      {
        if ( x==0 || y==0 || x==(w-1) || y==(h-1) )
        {
          sum = (input.getRGB( x, y ) >> 16) & 0xff;
        }
        else
        {
          sum = 0;
          for ( xk=-1; xk<=1; xk++ )
            for ( yk=-1; yk<=1; yk++ )
            {
              sum += (input.getRGB( x, y ) >> 16) & 0xff;
            }
          sum = sum / 9;
          if ( sum > 255 ) sum = 255;
        }
        rgb = sum | (sum<<8) | (sum<<16);
        filtered.setRGB( x, y, rgb );
      }
    input = filtered;
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
      prefilter();
      if ( debuglevel >= 1 )
        notifyListeners(-1, false, input, "Input" );
      currentreport = new XLocatorReportByVote();
      currentreport.image = input;
      locateX();
      inputlist[currentinput] = null;
  
      notifyListeners( 100*currentinput/inputlist.length, true, null, null );
    }
    
    notifyListeners( 100, false, null, null );
  }

  class LocalMaximum
  {
    int x;
    int y;
    int votes;
    int groupid;
    boolean filtered;
    LocalMaximum( int x, int y, int votes, int groupid )
    {
      this.x = x;
      this.y = y;
      this.votes = votes;
      this.groupid = groupid;
      this.filtered = false;
    }
    @Override
    public String toString()
    {
      return "Local Maximum x,y = " + x + "," + y + "  votes = " + votes;
    }
    void setFiltered( boolean b )
    {
      this.filtered = b;
    }
  }
  
  

}
