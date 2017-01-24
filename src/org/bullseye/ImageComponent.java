/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bullseye;

import java.awt.*;
import java.awt.image.*;
import javax.swing.*;

/**
 *
 * @author jon
 */
public class ImageComponent extends JComponent
{
  BufferedImage image = null;
  Dimension d;
  int zoomfactor = 1;

  public ImageComponent( int width, int height )
  {
    super();
    d = new Dimension( width, height );
  }

  public BufferedImage getImage()
  {
    return image;
  }

  public void setImage( BufferedImage image )
  {
    this.image = image;
    this.repaint();
  }

  public int getZoomFactor()
  {
    return zoomfactor;
  }

  public void setZoomFactor( int zoomfactor )
  {
    this.zoomfactor = zoomfactor;
    this.invalidate();
    this.repaint();
  }

  @Override
  protected void paintComponent( Graphics g )
  {
    super.paintComponent( g );
    if ( image == null ) return;
    g.drawImage( image, 0, 0, d.width, d.height, this );
  }

  @Override
  public Dimension getMinimumSize()
  {
    return d;
  }

  @Override
  public Dimension getMaximumSize()
  {
    return d;
  }

  @Override
  public Dimension getPreferredSize()
  {
    return d;
  }
  
  
  
  
}
