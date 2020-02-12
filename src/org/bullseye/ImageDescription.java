/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bullseye;

import java.awt.image.BufferedImage;

/**
 *
 * @author maber01
 */
public class ImageDescription
{
  BufferedImage image;
  double dpi;

  public ImageDescription(BufferedImage image, double dpi)
  {
    this.image = image;
    this.dpi = dpi;
  }

  public BufferedImage getImage()
  {
    return image;
  }

  public double getDpi()
  {
    return dpi;
  }
}
