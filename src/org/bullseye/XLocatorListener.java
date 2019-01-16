/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bullseye;

import java.awt.image.*;

/**
 *
 * @author jon
 */
public interface XLocatorListener
{
  public void notifyProgress( int percentage );
  public void notifyComplete( XLocatorReport report, int i );
  public void notifyDebugMessage( BufferedImage image, String message );
}
