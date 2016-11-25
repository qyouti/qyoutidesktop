/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti.scan.image;

import java.awt.*;
import java.awt.image.*;

/**
 *
 * @author jon
 */
public class ImageResizer
{
  public static BufferedImage resize(BufferedImage img, int newW, int newH)
  { 
    Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
    BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_3BYTE_BGR ); //img.getType());

    Graphics2D g2d = dimg.createGraphics();
    g2d.drawImage(tmp, 0, 0, null);
    g2d.dispose();

    return dimg;
  }
}
