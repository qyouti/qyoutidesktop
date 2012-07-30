/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti.ai;

import com.jhlabs.image.GaussianFilter;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.neuroph.core.learning.SupervisedTrainingElement;
import org.neuroph.core.learning.TrainingSet;
import org.qyouti.scan.image.FastFourierTransform2D;
import org.qyouti.util.QyoutiUtils;
import org.w3c.dom.Document;
import org.w3c.dom.svg.SVGDocument;

/**
 *
 * @author jon
 */
public class TrainingSetGenerator
{

  Random random = new Random( System.currentTimeMillis() );

  TrainingVectorImage image;

  public static final int DIMENSION = 64;

  TrainingSet<SupervisedTrainingElement> trainingset = new TrainingSet<SupervisedTrainingElement>(DIMENSION*DIMENSION,DIMENSION*DIMENSION);

  public TrainingSet<SupervisedTrainingElement> getTrainingSet()
  {
    return trainingset;
  }

  GaussianFilter gausianfilter = new GaussianFilter( 3.0F );
  GaussianFilter foregroundgausianfilter = new GaussianFilter( 7.0F );

  private void addTrainingElement( boolean save )
  {
    image.neuralnetinput = new double[DIMENSION*DIMENSION];
    double[] input = image.neuralnetinput;
    double[] target = image.neuralnettarget = new double[DIMENSION*DIMENSION];
    double foreground;
    for ( int i=0; i<(DIMENSION*DIMENSION); i++ )
    {
      input[i] = Math.sqrt( image.dirtyfft[i*2]*image.dirtyfft[i*2] + image.dirtyfft[i*2 + 1]*image.dirtyfft[i*2 + 1] );
      foreground = Math.sqrt( image.foregroundfft[i*2]*image.foregroundfft[i*2] + image.foregroundfft[i*2 + 1]*image.foregroundfft[i*2 + 1] );
      target[i] = (i==0)?input[i]:foreground;
    }
    if ( save )
      trainingset.addElement( new SupervisedTrainingElement(input, target) );
  }

  public void createImage( boolean save )
  {
    try
    {
      image = new TrainingVectorImage( this, random.nextBoolean()?TrainingVectorImage.MARKED:TrainingVectorImage.BLANK );
      //image = new TrainingVectorImage( this, TrainingVectorImage.MARKED );
      image.foregroundrasterimage = toBufferedImage( image.getForegroundSVGDocument(), DIMENSION, DIMENSION );
      if ( image.type == image.MARKED )
        image.foregroundrasterimage = foregroundgausianfilter.filter( image.foregroundrasterimage, null );
      image.dirtyrasterimage = toBufferedImage( image.getSVGDocument(), DIMENSION, DIMENSION );
      image.dirtyrasterimage = dirtyUpImage( gausianfilter.filter( image.dirtyrasterimage, null ) );

      image.foregroundfft = FastFourierTransform2D.toFloatArray( image.foregroundrasterimage );
      FastFourierTransform2D.fft2d( image.foregroundfft, 2, 1 );
      image.foregroundfftimage = FastFourierTransform2D.toBufferedImage( image.foregroundfft );

      image.dirtyfft = FastFourierTransform2D.toFloatArray( image.dirtyrasterimage );
      FastFourierTransform2D.fft2d( image.dirtyfft, 2, 1 );
      image.dirtyfftimage = FastFourierTransform2D.toBufferedImage( image.dirtyfft );

      addTrainingElement( save );
    }
    catch ( TranscoderException ex )
    {
      Logger.getLogger( TrainingSetGenerator.class.getName() ).log( Level.SEVERE, null, ex );
    }
  }

  public SVGDocument getCurrentSVG()
  {
    return (SVGDocument) image.getSVGDocument();
  }

  public BufferedImage getCurrentBufferedImage()
  {
    return image.dirtyrasterimage;
  }

  public BufferedImage getCurrentForegroundBufferedImage()
  {
    return image.foregroundrasterimage;
  }

  public BufferedImage getCurrentFFTImage()
  {
    return image.dirtyfftimage;
  }

  public BufferedImage getCurrentForegroundFFTImage()
  {
    return image.foregroundfftimage;
  }

  public float[] getCurrentFFT()
  {
    return image.dirtyfft;
  }

  public float[] getCurrentForegroundFFT()
  {
    return image.foregroundfft;
  }

  public double[] getCurrentNeuralNetInput()
  {
    return image.neuralnetinput;
  }
  
  public double[] getCurrentNeuralNetTarget()
  {
    return image.neuralnettarget;
  }



  public BufferedImage dirtyUpImage( BufferedImage cleanimage )
  {
    BufferedImage dirtyimage = new BufferedImage(
            cleanimage.getWidth(),
            cleanimage.getHeight(),
            cleanimage.getType() );
    // do 0.1% of pixels
    int x, y, rgb;
    double intensity;
    int r;
    for ( x = 0; x < dirtyimage.getWidth(); x++ )
    {
      for ( y = 0; y < dirtyimage.getHeight(); y++ )
      {
        rgb = cleanimage.getRGB( x, y );
        intensity = (double)((rgb & 0xff) + ((rgb>>8) & 0xff) + ((rgb>>16) & 0xff)) / (255.0*3.0);
        intensity += nextGaussian( 0.0, 0.005, -1.0, 1.0 );
        r = (int)(intensity * 255.0);
        if ( r < 0 ) r = 0;
        if ( r > 255 ) r = 255;
        dirtyimage.setRGB( x, y, (rgb & 0xff000000) | r | (r<<8) | (r<<16) );
      }
    }

    return dirtyimage;
  }

  public double nextGaussian( double mean, double sd, double min, double max )
  {
    double r;
    do
    {
      r = mean + sd * random.nextGaussian();
    }
    while ( r < min || r > max );

    return r;
  }

  public BufferedImage toBufferedImage( Document doc, float width, float height )
          throws TranscoderException
  {
    BufferedImageTranscoder imageTranscoder = new BufferedImageTranscoder();

    imageTranscoder.addTranscodingHint( PNGTranscoder.KEY_WIDTH, width );
    imageTranscoder.addTranscodingHint( PNGTranscoder.KEY_HEIGHT, height );

    TranscoderInput input = new TranscoderInput( doc );
    imageTranscoder.transcode( input, null );

    return imageTranscoder.getBufferedImage();
  }

  class BufferedImageTranscoder
          extends ImageTranscoder
  {

    @Override
    public BufferedImage createImage( int w, int h )
    {
      System.out.println( "Creating image buffer" );
      BufferedImage bi = new BufferedImage( w, h, BufferedImage.TYPE_INT_ARGB );
      return bi;
    }

    @Override
    public void writeImage( BufferedImage img, TranscoderOutput output )
    {
      System.out.println( "Writing image buffer" );
      this.img = img;
    }

    public BufferedImage getBufferedImage()
    {
      return img;
    }
    private BufferedImage img = null;
  }
}
