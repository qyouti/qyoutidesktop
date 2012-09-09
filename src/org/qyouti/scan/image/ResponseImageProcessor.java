/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti.scan.image;

import java.awt.image.BufferedImage;
import java.awt.image.LookupOp;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.qyouti.data.ResponseData;

/**
 *
 * @author jon
 */
public class ResponseImageProcessor
{
  private boolean monochrome;
  private double blackness;
  private double threshold;
  private double lightestredmean = 0.0;
  private int colourcomponents = -1;
  public ResponseBoxColourLookupTable lookup = null;
  public LookupOp lop = null;
  //public IdentityLookupTable idlookup = null;
  //public LookupOp idlop = null;

  public String darkest_ident = null;
  public double darkest_dark_pixels = 0.0;
  

  public ResponseImageProcessor( boolean monochrome, double blackness, double threshold )
  {
    this.monochrome = monochrome;
    this.blackness = blackness;
    this.threshold = threshold;
  }

  public void calibrateResponsePink( BufferedImage temp )
  {
    if ( colourcomponents < 0 )
      colourcomponents = temp.getColorModel().getNumComponents();

    if ( monochrome )
      return;
    
    double redmean = 0.0;
    for ( int x = (temp.getWidth() / 2) - 1; x <= (temp.getWidth() / 2) + 1; x++ )
    {
      for ( int y = (temp.getHeight() / 2) - 1; y <= (temp.getHeight() / 2) + 1; y++ )
      {
        redmean += (double) ((temp.getRGB( x, y ) & 0xff0000) >> 16);
      }
    }
    redmean = redmean / 9.0;
    if ( lightestredmean < redmean )
    {
      lightestredmean = redmean;
    }
  }

  public void makeReady()
  {
    // check for pages with no pink boxes
    // if none, colourcomponents won't have been
    // set and this processor won't be used for
    // this page.
    if ( colourcomponents == -1 )
      return;
    
    lookup = new ResponseBoxColourLookupTable(
            colourcomponents,
            monochrome?0.0:blackness,
            monochrome?255.0:lightestredmean );
    lookup.setThreshold( threshold );
    lop = new LookupOp( lookup, null );
    //idlookup = new IdentityLookupTable( colourcomponents );
    //idlop = new LookupOp( idlookup, null );
  }

  public void startQuestion()
  {
      darkest_ident = null;
      darkest_dark_pixels = 0.0;
  }

  public void filter( ResponseData response, File folder ) throws IOException
  {
    // this method doesn't use methods in ResponseData to load/save images because
    // during scanning process we don't want to hold loads of images in memory.
    BufferedImage filtered_image, devertical_image, tempimage;
    BufferedImage box_image = ImageIO.read( response.getImageFile() );
    lookup.resetStatistics();
    filtered_image = new BufferedImage( box_image.getWidth(), box_image.getHeight(), box_image.getType() );
    if ( monochrome )
    {
      //FastFourierTransform2D.normalise( box_image );
      float[] data = FastFourierTransform2D.toFloatArray( box_image );
      FastFourierTransform2D.fft2d( data, 2,  1 );
      tempimage = FastFourierTransform2D.toBufferedImage( data );
      ImageIO.write( tempimage, "bmp", new File( response.getFilteredImageFile().getPath() + ".1.fft.bmp" ) );
      //FastFourierTransform2D.verticalmask( data );
      FastFourierTransform2D.saltiremask( data );
      tempimage = FastFourierTransform2D.toBufferedImage( data );
      ImageIO.write( tempimage, "bmp", new File( response.getFilteredImageFile().getPath() + ".2.fft-masked.bmp" ) );
      FastFourierTransform2D.fft2d( data, 2, -1 );
      tempimage = FastFourierTransform2D.toBufferedImage( data );
      devertical_image = tempimage.getSubimage( 0, 0, box_image.getWidth(), box_image.getHeight() );
      ImageIO.write( devertical_image, "bmp", new File( response.getFilteredImageFile().getPath() + ".3.fft-reversed.jpg" ) );
      lop.filter( devertical_image, filtered_image );
    }
    else
    {
      lop.filter( box_image, filtered_image );
    }
    response.dark_pixels = (double) lookup.countBlackPixels() / (double) (lookup.countWhitePixels() + lookup.countBlackPixels());
    if ( darkest_ident==null || response.dark_pixels > darkest_dark_pixels )
    {
      darkest_ident = response.ident;
      darkest_dark_pixels = response.dark_pixels;
    }

    ImageIO.write( filtered_image, "jpg", response.getFilteredImageFile() );

    //FastFourierTransform2D.process( box_image );
  }


//        detector = new CannyEdgeDetector();
//        detector.setLowThreshold(0.5f);
//        detector.setHighThreshold(1.0f);
//        //detector.setGaussianKernelRadius(2.2f);
//        //detector.setGaussianKernelWidth(20);
//        detector.setOnlyRedChannel(true);
//        detector.setContrastNormalized(true);
//        detector.setSourceImage( temp );
//        detector.process();
//        BufferedImage edges = detector.getEdgesImage();
//        try
//        {
//          ImageIO.write(edges, "gif",
//              new File( scanfolder,
//                question.ident + "_" + j + "_" + page.candidate_number + "_edges.gif" ));
//        } catch (IOException ex)
//        {
//          Logger.getLogger(PageDecoder.class.getName()).log(Level.SEVERE, null, ex);
//        }


}
