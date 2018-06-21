/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bullseye;

import java.awt.*;
import java.awt.image.*;
import java.text.*;
import java.util.*;
import java.util.logging.*;

/**
 * This is the outline of the algorithm...
 * 
 * Input - a greyscale (or colour?) image.
 * Output - 2D coordinates of the most likely location of a
 * bullseye pattern in the image.
 * 
 * 1) Given image, expected radius of bullseye and radii of rings.
 * 2) Prepare a 'voting' matrix - one number for each
 *    pixel that could feasibly be the centre of the bullseye. 
 * 2) Use Sobel transform to find pixels that are black north of white,
 *    black south of white, black east of white, black west of white,
 *    black north east of white etc.
 *    (So nine types of pixel including "uninteresting" pixels.)
 * 3) Each interesting pixel casts multiple 'votes' for the pixel where
 *    the bullseye centre is.  E.g. for black north of white the centre
 *    could be above or below.  How far above or below will depend on 
 *    the predicted radius and how many vote pixels above and below will 
 *    depend on the number of rings in the bullseye.
 * 
 * 4) If the predicted radius is wrong instead of getting a single pixel with
 *    the maximum votes there will be a ring of popular pixels because the
 *    votes will overshoot or undershoot the centre.
 * 
 *    Simple option is to gaussian 'blur' the vote table. This ought to
 *    result in a single pixel with a local maximum of votes.
 * 
 * 5) Local maximum vote pixels are found - building a list of x, y
 *    coordinates.  Maxima whose votes are considerably lower than the
 *    highest "blurred" vote are discarded.  If there are a large number
 *    of maxima they are all discarded.
 * 
 * 6) Each local maximum is checked to see if it is too close to another
 *    local maximum.  If soo both are filtered out.  This leaves the 
 *    coordinates of pixels with peak votes which correspond to bullseyes.
 * 
 * 7) An array of Points is returned which locate the bullseyes whose
 *    radii approximately match that requested.
 * 
 * @author jon
 */
public class BullseyeLocator extends Thread
{
  LinkedList<ProgressListener> listeners;
  
  BufferedImage input;
  BufferedImage pixeltypemap;
  BufferedImage votemapimage;
  //BufferedImage filteredvotemapimage;
  int[][] votemap;
  double[] radii;
  double estimatedradius;
  double currenttrialradius;
  double maxradius;
  Point[][] edge_vote_patterns;
  int maxvote;
  int[] votehistogram;
  ArrayList<LocalMaximum> localmaxima;


  
  
  static final int VOTE_SMOOTH_KERNEL_WIDTH = 9;
  static final float[][] VOTESMOOTHKERNEL;
  
  static final double[] SOBELX =
  { 
    -1.0, 0.0, 1.0,
    -2.0, 0.0, 2.0,
    -1.0, 0.0, 1.0
  };
  
  static final double[] SOBELY =
  { 
    -1.0, -2.0, -1.0,
     0.0,  0.0,  0.0,
     1.0,  2.0,  1.0
  };
  
  static final double SOBEL_THRESHOLD = 1.0;
    
  static IndexColorModel pixeltypecolourtable = null;
  
  public static final int PIXEL_TYPE_OTHER = 0;
  
  public static final int PIXEL_TYPE_BNW   = 1;  // black north of white
  public static final int PIXEL_TYPE_BSW   = 2;
  public static final int PIXEL_TYPE_BEW   = 3;
  public static final int PIXEL_TYPE_BWW   = 4;
  
  public static final int PIXEL_TYPE_BNEW  = 5;  // black north east of white
  public static final int PIXEL_TYPE_BNWW  = 6;  
  public static final int PIXEL_TYPE_BSEW  = 7;  
  public static final int PIXEL_TYPE_BSWW  = 8;
  
  public static final int PIXEL_TYPE_MAX   = 8;
  
  public static final int[] PIXEL_TYPE_COLOURS = 
  {
    0xffffff,
    
    0xff0000,
    0x00ff00,
    0x0000ff,
    0xffff00,
    
    0xff00ff,
    0x00ffff,
    0x880088,
    0x888888
  };

  
  static  
  {
    byte[] r = new byte[PIXEL_TYPE_COLOURS.length];
    byte[] g = new byte[PIXEL_TYPE_COLOURS.length];
    byte[] b = new byte[PIXEL_TYPE_COLOURS.length];
    for ( int i=0; i<PIXEL_TYPE_COLOURS.length; i++ )
    {
      r[i] = (byte)((PIXEL_TYPE_COLOURS[i] & 0xff0000) >> 16);
      g[i] = (byte)((PIXEL_TYPE_COLOURS[i] & 0x00ff00) >> 8);
      b[i] = (byte) (PIXEL_TYPE_COLOURS[i] & 0x0000ff);
    }
    pixeltypecolourtable = new IndexColorModel(4,PIXEL_TYPE_MAX+1,r,g,b);
    
    VOTESMOOTHKERNEL = GaussianBlurKernel.generateKernelData2D( VOTE_SMOOTH_KERNEL_WIDTH );
  }
  
  public BullseyeLocator( BufferedImage input, double estimatedradius, double[] subradii )
  {
    this.localmaxima = new ArrayList<>();
    this.estimatedradius = estimatedradius;
    this.radii = new double[subradii.length];    
    System.arraycopy(subradii, 0, this.radii, 0, subradii.length );
    
    listeners = new LinkedList<>();    
    this.input = input;    
  }

  public void setEstimatedRadius(double estimatedradius)
  {
    this.estimatedradius = estimatedradius;
  }

  
  void reset( double r )
  {
    currenttrialradius = r;
    
    double d;
    int i, x, y;
    int[] distances = new int[this.radii.length];
    int[] oblique_distances = new int[this.radii.length];
    maxradius = 0.0;
    for ( i=0; i<radii.length; i++ )
    {
      d = radii[i] * currenttrialradius;
      distances[i]         = (int)Math.round( (i&1)==0?d:-d );
      d = Math.sqrt( d*d/2.0 );
      oblique_distances[i] = (int)Math.round( (i&1)==0?d:-d );
      if ( radii[i] > maxradius ) maxradius = radii[i];
    }
    maxradius = maxradius * currenttrialradius;
            
    edge_vote_patterns = new Point[PIXEL_TYPE_MAX+1][];
    edge_vote_patterns[PIXEL_TYPE_OTHER] = new Point[0];
    for ( i=0; i<=PIXEL_TYPE_MAX; i++ )
      edge_vote_patterns[i] = new Point[distances.length];
    
    for ( i=0; i<distances.length; i++ )
    {
      edge_vote_patterns[PIXEL_TYPE_BNW][i] = new Point(             0, -distances[i] );
      edge_vote_patterns[PIXEL_TYPE_BSW][i] = new Point(             0,  distances[i] );
      edge_vote_patterns[PIXEL_TYPE_BEW][i] = new Point(  distances[i],  0            );
      edge_vote_patterns[PIXEL_TYPE_BWW][i] = new Point( -distances[i],  0            );

      edge_vote_patterns[PIXEL_TYPE_BNEW][i] = new Point(  oblique_distances[i],  -oblique_distances[i] );
      edge_vote_patterns[PIXEL_TYPE_BNWW][i] = new Point( -oblique_distances[i],  -oblique_distances[i] );
      edge_vote_patterns[PIXEL_TYPE_BSEW][i] = new Point(  oblique_distances[i],   oblique_distances[i] );
      edge_vote_patterns[PIXEL_TYPE_BSWW][i] = new Point( -oblique_distances[i],   oblique_distances[i] );
    }

    pixeltypemap = new BufferedImage(
            input.getWidth(),
            input.getHeight(),
            BufferedImage.TYPE_BYTE_INDEXED,
            pixeltypecolourtable );
  
    votemapimage = new BufferedImage(
            input.getWidth(),
            input.getHeight(),
            BufferedImage.TYPE_INT_RGB );
//    filteredvotemapimage = new BufferedImage(
//            input.getWidth(),
//            input.getHeight(),
//            BufferedImage.TYPE_INT_RGB );
    maxvote=0;

    votemap  = new int[input.getWidth()][input.getHeight()];

    for ( x=0; x<input.getWidth(); x++ )
      for ( y=0; y<input.getHeight(); y++ )
        votemapimage.setRGB( x, y, 0 );
  }
  
  
  public Point[] getLocations()
  {
    Point[] points = new Point[localmaxima.size()];
    LocalMaximum lm;
    for ( int i=0; i<points.length; i++ )
    {
      lm = localmaxima.get( i );
      points[i] = new Point( lm.x, lm.y );
    }
    return points;
  }

  public BufferedImage getInputImage()
  {
    return input;
  }
  
  public void setInputImage(BufferedImage image)
  {
    input=image;
  }
  
  public BufferedImage getPixelTypeMap()
  {
    return pixeltypemap;
  }
  
  public BufferedImage getVoteMapImage()
  {
    return votemapimage;
  }
  
//  public BufferedImage getFilteredVoteMapImage()
//  {
//    return filteredvotemapimage;
//  }
  
  
  
  
  
  public void addProgressListener( ProgressListener listener )
  {
    listeners.add( listener );
  }
  
  public void removeProgressListener( ProgressListener listener )
  {
    listeners.remove( listener );
  }
  
  public void notifyListeners( int percentage, boolean passcomplete, boolean complete )
  {
    Iterator<ProgressListener> iter = listeners.iterator();
    ProgressListener listener;
    while ( iter.hasNext() )
    {
      listener = iter.next();
      listener.notifyProgress( percentage );
      if ( passcomplete ) listener.notifyPassComplete();
      if ( complete ) listener.notifyComplete();
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
  
  void applyVotes( int x, int y, int type )
  {
    int i, dx, dy, lx, ly, votes;
    
//    if ( type != PIXEL_TYPE_BNWW && 
//         type != PIXEL_TYPE_BNW
//            ) return;
    
    Point p;
    for ( i=0; i<edge_vote_patterns[type].length; i++ )
    {
      // point relative to x,y where vote will be cast
      p = edge_vote_patterns[type][i];
      lx = x + p.x;
      ly = y + p.y;

      // out of bounds? Skip
      if ( lx < 0 || lx >= votemap.length || ly < 0 || ly >= votemap[lx].length )
        continue;

      // cast vote
      votemap[lx][ly]++;
    }
  }
  
  void dumpHistogram()
  {
    for ( int i=0; i<votehistogram.length; i++ )
      System.out.println( "Votes " + i + "   pixels " + votehistogram[i] );
  }
  
  void smoothVotes()
  {
    int x, y, w, h, i, j;
    int koff = VOTESMOOTHKERNEL.length / 2;
    float total;
    w = votemap.length;
    h = votemap[0].length;
    int[][] smoothvotemap = new int[w][h];
    
    for ( x=0; x<w; x++ )
      for ( y=0; y<h; y++ )
      {
        if ( x<koff || y<koff || x>=(w-koff) || y>=(h-koff) )
          continue;
    
        total = 0.0f;
        for ( i=0; i<VOTESMOOTHKERNEL.length; i++ )
          for ( j=0; j<VOTESMOOTHKERNEL[i].length; j++ )
            total += VOTESMOOTHKERNEL[i][j] * (float)votemap[x-koff+i][y-koff+j];
        // vote is multiplied by 1000 to avoid losing precision with conversion
        // to integer.
        smoothvotemap[x][y] = (int)Math.round( (double)total*1000.0 );
      }
    votemap = smoothvotemap;
  }
  
  void findLocalMaximaVotes()
  {
    int x, y, w, h, i, j;
    boolean failed;
    w = votemap.length;
    h = votemap[0].length;
    
    localmaxima.clear();
            
    for ( x=0; x<w; x++ )
    {
      for ( y=0; y<h; y++ )
      {
        failed = false;
        if ( votemap[x][y] < (2*maxvote/3) )
          failed = true;
        
        if ( x<1 || y<1 || x>(w-2) || y>(h-2) )
          failed = true;
        
        for ( i=-1; i<=1; i++ )
        {
          if ( failed ) break;
          for ( j=-1; j<=1; j++ )
          {
            if ( i==0 && j==0 )
              continue;
            if ( votemap[x+i][y+j] > votemap[x][y] )
            {
              failed = true;
              break;
            }
          }
        }
        
        if ( !failed )
          localmaxima.add( new LocalMaximum( x, y, votemap[x][y] ) );
        if ( localmaxima.size() > 10 )
        {
          localmaxima.clear();
          return;
        }
      }
    }
  }
  
  void voteStats()
  {
    int x, y, w, h;
    w = votemapimage.getWidth();
    h = votemapimage.getHeight();
    // some stats...
    maxvote=1;
    for ( x=0; x<w; x++ )
      for ( y=0; y<h; y++ )
        if ( votemap[x][y] > maxvote )
          maxvote = votemap[x][y];
  }
  
  void filterMaximaVotes()
  {
    int i, j;
    int rsq = (int)(2.0*currenttrialradius * 2.0*currenttrialradius);
    int maxv = (int)(0.4*currenttrialradius * 0.4*currenttrialradius);
    LocalMaximum a;
    
    System.out.println( "Filtering local maxima" );
    // find clusters of local maxima that are close to each other
    // collapse each cluster down to one point - centre of gravity.
    ArrayList<LocalMaximumCluster> clusters = new ArrayList<LocalMaximumCluster>();
    LocalMaximumCluster cluster;
    
    // look at each point and either create a new cluster with one member
    // or add to an existing cluster    
    for ( i=0; i<localmaxima.size(); i++ )
    {
      a = localmaxima.get( i );
      for ( j=0; j<clusters.size(); j++ )
      {
        cluster = clusters.get( j );
        if ( cluster.inCluster( a ) )
        {
          cluster.add( a );
          // skip other clusters we are done
          a = null;
          break;
        } 
      }
      if ( a != null )
        clusters.add( new LocalMaximumCluster( a, rsq, maxv ) );
      
//        b = localmaxima.get( j );
//        dx = a.x - b.x;
//        dy = a.y - b.y;
//        dsq = dx*dx + dy*dy;
//        if ( dsq < 4.0 ) // closer than 2 pixels
//        {
//          // only filter b
//          b.filtered = true;          
//        }
//        else if ( dsq < rsq )
//        {
//          a.filtered = true;
//          b.filtered = true;
//        }
    }

    System.out.println( "Found " + clusters.size() + " clusters." );

    localmaxima.clear();
    // tight clusters are added back...
    for ( j=0; j<clusters.size(); j++ )
    {
      cluster = clusters.get( j );
      if ( cluster.isTight() )
        localmaxima.add( cluster.getMeanLocalMaximum() );
    }
    
//    for ( i=0; i<localmaxima.size(); i++ )
//    {
//      a = localmaxima.get( i );
//      if ( a.filtered )
//        localmaxima.remove( i-- );
//    }
  }
  
  void processAllVotes()
  {
    int x, y, w, h, i;

    w = votemapimage.getWidth();
    h = votemapimage.getHeight();

    System.out.println( "Search radius " + estimatedradius );

    smoothVotes();
    voteStats();
    findLocalMaximaVotes();   
    System.out.println( "local maxima " + localmaxima.size() );
    for ( i=0; i<localmaxima.size(); i++ )
      System.out.println( localmaxima.get( i ) );
    filterMaximaVotes();
    System.out.println( "local maxima " + localmaxima.size() );
    for ( i=0; i<localmaxima.size(); i++ )
      System.out.println( localmaxima.get( i ) );
    
    // create a histogram of all the pixel votes
    votehistogram = new int[maxvote+1];
    for ( x=0; x<w; x++ )
      for ( y=0; y<h; y++ )
        votehistogram[votemap[x][y]]++;
    
    int vote, rgb;
    for ( x=0; x<w; x++ )
      for ( y=0; y<h; y++ )
      {        
        vote = votemap[x][y];        
        rgb = (255 * vote / maxvote) << 16  | (255 * vote / maxvote) << 8 | (255 * vote / maxvote);
        rgb = rgb | 0xff000000;
        votemapimage.setRGB( x, y, rgb );
      }
  }
  
  public void pass() // PassResult result )
  {
    int oldprogress = 0;
    int progress;
    notifyListeners( 0, false, false );
    
    int n, x, y, xa, ya, type;
    double[] currentinput = new double[9];
    double sblx, sbly, sblxa, sblya, sblmag, sblang;
    double maxsbl;
    Color color;
    
    maxsbl=0.0;

    reset( this.currenttrialradius );
    
    // iterate over all the pixels of the input image
    for ( x=0; x<input.getWidth(); x++ )
      for ( y=0; y<input.getHeight(); y++ )
      {        
        // No sobel stuff at the edge of the input image
        if ( x==0 || y==0 || x==(input.getWidth()-1) || y==(input.getHeight()-1) )
          continue;

        // make a little buffer with brightness of
        // eight pixels around central pixel        
        n = 0;
        for ( ya=y-1; ya<=(y+1); ya++ )
          for ( xa=x-1; xa<=(x+1); xa++ )
          {
            color = new Color( input.getRGB( xa, ya ) );
            currentinput[n++] = (color.getRed() + color.getGreen() + color.getBlue()) / (255.0 * 3.0);
          }
        
        // convolve with two sobel kernels
        sblx = multiplyAndAccumulate(currentinput, SOBELX );
        sbly = multiplyAndAccumulate(currentinput, SOBELY );
        
        sblmag = Math.sqrt( sbly*sbly + sblx*sblx );
        sblang = Math.toDegrees( Math.atan2( sbly, sblx ) );
        
        sblxa = Math.abs( sblx );
        sblya = Math.abs( sbly );
        if ( sblxa > maxsbl ) maxsbl += sblxa;
        if ( sblya > maxsbl ) maxsbl += sblya;
        // Classify pixels, finding horizontal and vertical edges
        type = PIXEL_TYPE_OTHER;

        double anglerange = 5.0;
        if ( sblmag > SOBEL_THRESHOLD )
        {
          if ( sblang <               anglerange && sblang >       -anglerange ) type = PIXEL_TYPE_BWW;
          else if ( sblang < -180.0 + anglerange || sblang > 180.0 -anglerange ) type = PIXEL_TYPE_BEW;
          else if ( sblang <   90.0 + anglerange && sblang >  90.0 -anglerange ) type = PIXEL_TYPE_BNW;
          else if ( sblang <  -90.0 + anglerange && sblang > -90.0 -anglerange ) type = PIXEL_TYPE_BSW;

          else if ( sblang <  -45.0 + anglerange && sblang >  -45.0 -anglerange ) type = PIXEL_TYPE_BSWW;
          else if ( sblang <  135.0 + anglerange && sblang >  135.0 -anglerange ) type = PIXEL_TYPE_BNEW;
          else if ( sblang <   45.0 + anglerange && sblang >   45.0 -anglerange ) type = PIXEL_TYPE_BNWW;
          else if ( sblang < -135.0 + anglerange && sblang > -135.0 -anglerange ) type = PIXEL_TYPE_BSEW;
          
        }

        if ( type != PIXEL_TYPE_OTHER )
        {
          // update an image to display edges
          pixeltypemap.setRGB(x, y, PIXEL_TYPE_COLOURS[type] );
          // this pixel votes on location of bullseye centre
          applyVotes( x, y, type );
        }
        
        progress = 100 * x / input.getWidth();
        if ( progress != oldprogress )
        {
          notifyListeners( progress, false, false );
          oldprogress = progress;
        }
      }

    processAllVotes();
    
    notifyListeners( 100, true, false );
  }
  
  void sleepSeconds( int sec )
  {
    try
    {
      Thread.sleep( sec*1000L );
    }
    catch ( InterruptedException ex )
    {
      Logger.getLogger( BullseyeLocator.class.getName() ).
              log( Level.SEVERE, null, ex );
    }    
  }
   
  public Point[] locateBullseye()
  {
    int i;
    DecimalFormat df = new DecimalFormat("###.0");
    currenttrialradius = estimatedradius;
    
    pass();// trial );
    Point[] points = getLocations();
    for ( i=0; i<points.length; i++ ) //trial.centre != null )
      System.out.println( " centre = " + points[i].x + ", " + points[i].y );
    
    //dumpHistogram();
    
    notifyListeners( 100, false, true );
    return points;
  }
  
  @Override
  public void run()
  {
    locateBullseye();
  }

  class LocalMaximum
  {
    int x;
    int y;
    int votes;
    boolean filtered;
    LocalMaximum( int x, int y, int votes )
    {
      this.x = x;
      this.y = y;
      this.votes = votes;
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

  class LocalMaximumCluster
  {
    int n;
    int sumx;
    int sumy;
    int sumsqrx;
    int sumsqry;
    int sumvotes;
    
    int sqrradius;
    int maxvariance;
    
    public LocalMaximumCluster( LocalMaximum lm, int sqr, int maxv )
    {
      n=1;
      sumx = lm.x;
      sumy = lm.y;
      sumsqrx = lm.x*lm.x;
      sumsqry = lm.y*lm.y;
      sumvotes = lm.votes;
      
      sqrradius = sqr;
      maxvariance = maxv;
    }
    
    public int getX()
    {
      return sumx/n;
    }
    
    public int getY()
    {
      return sumy/n;
    }

    public int getXVariance()
    {
      return (n*sumsqrx - sumx*sumx)/n;
    }
    
    public int getYVariance()
    {
      return (n*sumsqry - sumy*sumy)/n;
    }

    public int getVariance()
    {
      int xv = getXVariance();
      int yv = getYVariance();
      return (xv > yv)?xv:yv;
    }

    public boolean isTight()
    {
      return getVariance() < maxvariance;
    }
    
    public int getVotes()
    {
      return sumvotes/n;
    }
    
    public LocalMaximum getMeanLocalMaximum()
    {
      return new LocalMaximum( getX(), getY(), getVotes() );
    }
    
    public boolean inCluster( LocalMaximum lm )
    {
      int dx, dy, dsqr;
      dx = getX() - lm.x;
      dy = getY() - lm.y;
      dsqr = dx*dx + dy*dy;
      return dsqr < sqrradius;
    }
    
    public void add( LocalMaximum lm )
    {
      n+=1;
      sumx += lm.x;
      sumy += lm.y;
      sumsqrx += lm.x*lm.x;
      sumsqry += lm.y*lm.y;
      sumvotes += lm.votes;
    }
}
  
  public interface ProgressListener
  {
    public void notifyProgress( int percentage );
    public void notifyPassComplete();
    public void notifyComplete();
  }
}
