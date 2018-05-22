/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti.scan.image;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 *
 * @author jon
 */
public class PageRotator
{

  AffineTransform t_90 = AffineTransform.getQuadrantRotateInstance( 1 );
  AffineTransform t_180 = AffineTransform.getQuadrantRotateInstance( 2 );
  AffineTransform t_270 = AffineTransform.getQuadrantRotateInstance( 3 );
  AffineTransformOp op_90 = new AffineTransformOp( t_90, AffineTransformOp.TYPE_NEAREST_NEIGHBOR );
  AffineTransformOp op_180 = new AffineTransformOp( t_180, AffineTransformOp.TYPE_NEAREST_NEIGHBOR );
  AffineTransformOp op_270 = new AffineTransformOp( t_270, AffineTransformOp.TYPE_NEAREST_NEIGHBOR );

  BufferedImage sourceimg;
  int type = BufferedImage.TYPE_INT_RGB;

  public PageRotator( BufferedImage image )
  {
    this.sourceimg = new BufferedImage( image.getWidth(), image.getHeight(), type );
    this.sourceimg.getGraphics().drawImage(image, 0, 0, null);
    t_90 = AffineTransform.getTranslateInstance( 0.0, 0.0 );
    t_90.concatenate( AffineTransform.getTranslateInstance( 0.5 * (double) image.getHeight(), 0.5 * (double) image.getWidth() ) );
    t_90.concatenate( AffineTransform.getQuadrantRotateInstance( 1 ) );
    t_90.concatenate( AffineTransform.getTranslateInstance( -0.5*(double)image.getWidth(), -0.5*(double)image.getHeight() ) );
    
    t_180 = AffineTransform.getQuadrantRotateInstance( 2, 0.5 * (double) image.getWidth(), 0.5 * (double) image.getHeight() );

    t_270 = AffineTransform.getTranslateInstance( 0.0, 0.0 );
    t_270.concatenate( AffineTransform.getTranslateInstance( 0.5 * (double) image.getHeight(), 0.5 * (double) image.getWidth() ) );
    t_270.concatenate( AffineTransform.getQuadrantRotateInstance( 3 ) );
    t_270.concatenate( AffineTransform.getTranslateInstance( -0.5*(double)image.getWidth(), -0.5*(double)image.getHeight() ) );

    op_90 = new AffineTransformOp( t_90, AffineTransformOp.TYPE_NEAREST_NEIGHBOR );
    op_180 = new AffineTransformOp( t_180, AffineTransformOp.TYPE_NEAREST_NEIGHBOR );
    op_270 = new AffineTransformOp( t_270, AffineTransformOp.TYPE_NEAREST_NEIGHBOR );

  }

  public BufferedImage rotate90()
  {
    BufferedImage destimg = new BufferedImage( sourceimg.getHeight(), sourceimg.getWidth(), type );
    op_90.filter( sourceimg, destimg );
    return destimg;
  }

  public BufferedImage rotate180()
  {
    BufferedImage destimg = new BufferedImage( sourceimg.getWidth(), sourceimg.getHeight(), type );
    op_180.filter( sourceimg, destimg );
    return destimg;
  }

  public BufferedImage rotate270()
  {
    BufferedImage destimg = new BufferedImage( sourceimg.getHeight(), sourceimg.getWidth(), type );
    op_270.filter( sourceimg, destimg );
    return destimg;
  }

  public static void main( String[] args )
          throws IOException
  {
    BufferedImage image = ImageIO.read( new File( "C:\\Users\\maber01\\qyouti\\exams\\scans\\10043.jpg" ) );
    PageRotator rot = new PageRotator( image );
    BufferedImage i90 = rot.rotate90();
    BufferedImage i180 = rot.rotate180();
    BufferedImage i270 = rot.rotate270();
    ImageIO.write( i90, "jpg", new File( "C:\\Users\\maber01\\qyouti\\exams\\scans\\i90.jpg" ) );
    ImageIO.write( i180, "jpg", new File( "C:\\Users\\maber01\\qyouti\\exams\\scans\\i180.jpg" ) );
    ImageIO.write( i270, "jpg", new File( "C:\\Users\\maber01\\qyouti\\exams\\scans\\i270.jpg" ) );
}
}
