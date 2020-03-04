/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bullseye;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;

/**
 *
 * @author maber01
 */
public class ImageShapeFattener
{
  double radius;
  int r;
  int w;
  BufferedImage circleimage;
  
  public ImageShapeFattener( double radius )
  {
    this.radius = radius;
    byte[] red = { (byte)0xff, (byte)0    };
    byte[] grn = { (byte)0xff, (byte)0    };
    byte[] blu = { (byte)0xff, (byte)0    };
    byte[] opa = { (byte)0x00, (byte)0xff };
    r = (int)Math.ceil(radius);
    w = 1+2*r;
    circleimage = new BufferedImage( w, w, BufferedImage.TYPE_BYTE_BINARY, new IndexColorModel(1,2,red,grn,blu,opa) );
    double sqrradius = radius*radius;
    for ( int i=0; i<w; i++ )
      for ( int j=0; j<w; j++ )
      {
        double x = i-r;
        double y = j-r;
        double d = x*x + y*y;
        if ( d<sqrradius )
          circleimage.setRGB( i, j, 0xff000000 );
        else
          circleimage.setRGB( i, j, 0x00ffffff );
      }
  }
  
  public BufferedImage getCircleImage()
  {
    return circleimage;
  }
  
  public void fattenShapeImage( BufferedImage source, BufferedImage destination, int matchrgb )
  {
    if ( destination.getWidth() != source.getWidth() || destination.getHeight() != source.getHeight() )
      throw new IllegalArgumentException( "Source and destination images need to have the same dimensions." );
    
    Graphics2D g = destination.createGraphics();
    g.setColor( Color.white );
    g.fillRect( 0 , 0, destination.getWidth(), destination.getHeight() );
    for ( int i=0; i<source.getWidth(); i++ )
      for ( int j=0; j<source.getHeight(); j++ )
        if ( source.getRGB( i, j ) == matchrgb )
          g.drawImage( circleimage, null, i-r, j-r );
  }
}
