/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bullseye;

import java.awt.image.BufferedImage;
import java.io.File;

/**
 *
 * @author jon
 */
public interface XLocator
{
  public void setImages( BufferedImage[] images );
  public void setImageFiles( File[] files );
  public void addProgressListener( XLocatorListener listener );
  public void removeProgressListener( XLocatorListener listener );
  public void runSynchronously();
  public void setDebugLevel( int n );  
}
