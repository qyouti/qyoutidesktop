/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti.ai;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.util.*;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class builds a single vector image for inclusion in a training set.
 * The background part of the image represents what might be preprinted on
 * a survey sheet and the foreground represents the mark that a user has
 * made on the survey sheet.
 *
 * Origin of coordinate system is at centre of image.
 * Width and height are 1.0
 *
 * @author jon
 */
public class TrainingVectorImage
{

  public static final int BLANK = 0;
  public static final int MARKED = 1;
  public static final int CANCELLED = 2;
  TrainingSetGenerator gen;
  int type;

  BufferedImage foregroundrasterimage;
  BufferedImage foregroundfftimage;
  BufferedImage dirtyrasterimage, dirtyfftimage;
  float[] foregroundfft;
  float[] dirtyfft;

  double[] neuralnetinput;
  double[] neuralnettarget;

  Document svgdoc_foreground=null, svgdoc=null;
  Element svgroot;
  Element maingroup, backgroundgroup, foregroundgroup;

  String svgNS = SVGConstants.SVG_NAMESPACE_URI;


  public TrainingVectorImage( TrainingSetGenerator gen, int type )
  {
    if ( type < BLANK || type > CANCELLED )
    {
      throw new IllegalArgumentException( "Unknown type of TrainingVectorImage." );
    }
    this.gen = gen;
    this.type = type;

    DOMImplementation impl = GenericDOMImplementation.getDOMImplementation();
    svgdoc_foreground = impl.createDocument( svgNS, "svg", null );
    svgroot = svgdoc_foreground.getDocumentElement();
    svgroot.setAttribute( "viewBox", "-0.5 -0.5 1.0 1.0");
    svgroot.setAttribute( "width", "1in" );
    svgroot.setAttribute( "height", "1in" );
    maingroup = svgdoc_foreground.createElementNS( svgNS, "g" );
    backgroundgroup = svgdoc_foreground.createElementNS( svgNS, "g" );
    foregroundgroup = svgdoc_foreground.createElementNS( svgNS, "g" );


    Element r = svgdoc_foreground.createElementNS( svgNS, "rect" );
    r.setAttribute( "x", "-1" );
    r.setAttribute( "y", "-1" );
    r.setAttribute( "width", "2" );
    r.setAttribute( "height", "2" );
    r.setAttribute( "fill-opacity", "1" );
    r.setAttribute( "fill", "rgb(255,255,255)" );

    svgroot.appendChild( r );
    svgroot.appendChild( maingroup );
    maingroup.appendChild( backgroundgroup );
    maingroup.appendChild( foregroundgroup );

    double scale = gen.nextGaussian( 1.3, 0.1, 1.2, 1.4 );
    double theta = gen.nextGaussian( 0, 1, -10, 10 );
    double dx = gen.nextGaussian( 0, 0.05, -0.4, 0.4 );
    double dy = gen.nextGaussian( 0, 0.05, -0.4, 0.4 );
    maingroup.setAttribute( "transform", "scale( " + scale + ") rotate(" + theta + ") translate( " + dx + "," + dy + ")" );
    
    Box box = new Box();
    Cross cross = null;
    if ( type == MARKED )
      cross = new Cross();

    svgdoc = (Document) svgdoc_foreground.cloneNode( true );
    
    if ( cross!=null )
      cross.draw( true );
    backgroundgroup.setAttribute( "display", "none" );
    
  }

  public Document getSVGDocument()
  {
    return svgdoc;
  }

  public Document getForegroundSVGDocument()
  {
    return svgdoc_foreground;
  }



  class Box
  {
    Box()
    {
      Element r = svgdoc_foreground.createElementNS( svgNS, "rect" );
      Element defs = svgdoc_foreground.createElementNS( svgNS, "defs" );
      Element p = svgdoc_foreground.createElementNS( svgNS, "pattern" );
      Element l = svgdoc_foreground.createElementNS( svgNS, "line" );
      Element la = svgdoc_foreground.createElementNS( svgNS, "line" );
      Element lb = svgdoc_foreground.createElementNS( svgNS, "line" );


      p.setAttribute( "id", "stripes" );
      p.setAttribute( "patternUnits", "userSpaceOnUse" );
      p.setAttribute( "x",       "0" );
      p.setAttribute( "y",       "0" );
      p.setAttribute( "width",   "0.085" );
      p.setAttribute( "height",  "0.085" );
      p.setAttribute( "viewBox", "0 0 0.2 0.2" );

      l.setAttribute( "x1", "0.05" );
      l.setAttribute( "y1", "-0.1" );
      l.setAttribute( "x2", "0.05" );
      l.setAttribute( "y2", "0.25" );
      l.setAttribute( "stroke", "rgb(220,220,220)" );
      l.setAttribute( "stroke-width", "0.03" );

      
      // target box filled with stripy pattern
      r.setAttribute( "x", "-0.4" );
      r.setAttribute( "y", "-0.4" );
      r.setAttribute( "width", "0.8" );
      r.setAttribute( "height", "0.8" );
      r.setAttribute( "stroke", "rgb(64,64,64)" );
      r.setAttribute( "stroke-width", "0.005" );
      r.setAttribute( "fill-opacity", "1" );
      r.setAttribute( "fill", " url( #stripes ) " );


      la.setAttribute( "x1", "-0.3" );
      la.setAttribute( "y1", "0.3" );
      la.setAttribute( "x2", "0.3" );
      la.setAttribute( "y2", "-0.3" );
      la.setAttribute( "stroke", "rgb(255,255,255)" );
      la.setAttribute( "stroke-width", "0.05" );
      la.setAttribute( "stroke-linecap", "round" );

      lb.setAttribute( "x1", "0.3" );
      lb.setAttribute( "y1", "0.3" );
      lb.setAttribute( "x2", "-0.3" );
      lb.setAttribute( "y2", "-0.3" );
      lb.setAttribute( "stroke", "rgb(255,255,255)" );
      lb.setAttribute( "stroke-width", "0.075" );
      lb.setAttribute( "stroke-linecap", "round" );



      defs.appendChild( p );
      p.appendChild( l );
      backgroundgroup.appendChild( defs );
      backgroundgroup.appendChild( r );
      backgroundgroup.appendChild( la );
      backgroundgroup.appendChild( lb );
    }
  }



  class Cross
  {

    static final int TOPLEFT = 0;
    static final int BOTTOMRIGHT = 1;
    static final int TOPRIGHT = 2;
    static final int BOTTOMLEFT = 3;
    Point2D[] points = new Point2D[4];
    double line_width;
    int grey, darkgrey;

    Element ln1;
    Element ln2;

    Cross()
    {
      ln1 = svgdoc_foreground.createElementNS( svgNS, "line" );
      ln2 = svgdoc_foreground.createElementNS( svgNS, "line" );
      do
      {
        init();
      }
      while ( !acceptable() );

      draw( false );
    }


    void draw( boolean dark )
    {
      int g = dark?darkgrey:grey;

      ln1.setAttribute( "x1", Double.toString( points[TOPLEFT].getX() ) );
      ln1.setAttribute( "y1", Double.toString( points[TOPLEFT].getY() ) );
      ln1.setAttribute( "x2", Double.toString( points[BOTTOMRIGHT].getX() ) );
      ln1.setAttribute( "y2", Double.toString( points[BOTTOMRIGHT].getY() ) );
      ln1.setAttribute( "stroke", "rgb(" + g + "," + g + "," + g + ")" );
      ln1.setAttribute( "stroke-linecap", "round" );
      ln1.setAttribute( "stroke-width", Double.toString( line_width ) );

      ln2.setAttribute( "x1", Double.toString( points[TOPRIGHT].getX() ) );
      ln2.setAttribute( "y1", Double.toString( points[TOPRIGHT].getY() ) );
      ln2.setAttribute( "x2", Double.toString( points[BOTTOMLEFT].getX() ) );
      ln2.setAttribute( "y2", Double.toString( points[BOTTOMLEFT].getY() ) );
      ln2.setAttribute( "stroke", "rgb(" + g + "," + g + "," + g + ")" );
      ln2.setAttribute( "stroke-linecap", "round" );
      ln2.setAttribute( "stroke-width", Double.toString( line_width ) );

      foregroundgroup.appendChild( ln1 );
      foregroundgroup.appendChild( ln2 );
    }

    boolean acceptable()
    {
      for ( int i = 0; i < points.length; i++ )
      {
        if ( Math.abs( points[i].getX() ) > 1.3 )
        {
          return false;
        }
        if ( Math.abs( points[i].getY() ) > 1.3 )
        {
          return false;
        }
      }
      return true;
    }

    void init()
    {
      int i;

      // set up the ideal cross
      points[TOPLEFT] = new Point2D.Float( -0.35F, 0.35F );
      points[BOTTOMRIGHT] = new Point2D.Float( 0.35F, -0.35F );
      points[TOPRIGHT] = new Point2D.Float( 0.35F, 0.35F );
      points[BOTTOMLEFT] = new Point2D.Float( -0.35F, -0.35F );

      // Then distort it

      // Scale and stretch
      double xscale, yscale;
      do
      {
        xscale = gen.nextGaussian( 1.0, 0.3, 0.3, 1.1 );
        yscale = gen.nextGaussian( 1.0, 0.3, 0.3, 1.1 );
      }
      while ( Math.max( xscale / yscale, yscale / xscale ) > 2 );
      AffineTransform scale = AffineTransform.getScaleInstance( xscale, yscale );
      scale.transform( points, TOPLEFT, points, TOPLEFT, 4 );


      // rotate each line separately a bit and both together by more
      double theta, thetadiff;
      AffineTransform rot;
      theta = gen.nextGaussian( 0, 0.2, -0.2, 0.2 );

      for ( i = 0; i < 4; i += 2 )
      {
        thetadiff = gen.nextGaussian( 0, 0.1, -0.2, 0.2 );
        rot = AffineTransform.getRotateInstance( theta+thetadiff, 0.0, 0.0 );
        rot.transform( points, i, points, i, 2 );
      }

      // offset each line separately a little bit
      double dx, dy;
      AffineTransform off;

      for ( i = 0; i < 4; i += 2 )
      {
        dx = gen.nextGaussian( 0, 0.05, -0.2, 0.2 );
        dy = gen.nextGaussian( 0, 0.05, -0.2, 0.2 );
        off = AffineTransform.getTranslateInstance( dx, dy );
        off.transform( points, i, points, i, 2 );
      }

      // offset both lines more
      dx = gen.nextGaussian( 0, 0.1, -0.4, 0.4 );
      dy = gen.nextGaussian( 0, 0.1, -0.4, 0.4 );
      off = AffineTransform.getTranslateInstance( dx, dy );
      off.transform( points, 0, points, 0, 4 );


      line_width = (float) gen.nextGaussian( 0.05, 0.02, 0.01, 0.25 );
      grey = Math.round( (float)gen.nextGaussian( 175.0, 30.0, 100.0, 250.0 ) );
      darkgrey = grey / 4;
    }
  }
}
