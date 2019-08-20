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
import org.qyouti.compositefile.CompositeFile;
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



}
