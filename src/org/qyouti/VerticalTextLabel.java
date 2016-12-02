/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;

/**
 *
 * @author jon
 */
public class VerticalTextLabel
        extends JLabel
{

  private boolean needsRotate=false;

  @Override
  public Dimension getSize()
  {
    if ( !needsRotate )
    {
      return super.getSize();
    }

    // reversed dimensions
    Dimension size = super.getSize();
    return new Dimension( size.height, size.width );
  }

  @Override
  public Dimension getMinimumSize()
  {
    Dimension size = super.getMinimumSize();
    return new Dimension( size.height, size.width );
  }

  @Override
  public Dimension getMaximumSize()
  {
    Dimension size = super.getMaximumSize();
    return new Dimension( size.height, size.width );
  }

  @Override
  public Dimension getPreferredSize()
  {
    Dimension size = super.getPreferredSize();
    return new Dimension( size.height, size.width );
  }

  
  @Override
  public int getHeight()
  {
    return getSize().height;
  }

  @Override
  public int getWidth()
  {
    return getSize().width;
  }


  
  
  @Override
  protected void paintComponent( Graphics g )
  {
    Graphics2D gr = (Graphics2D) g.create();

    gr.translate( 0, getSize().getHeight() );
    gr.transform( AffineTransform.getQuadrantRotateInstance( -1 ) );
    needsRotate = true;
    super.paintComponent( gr );
    needsRotate = false;
  }
}
