/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti.scan.process;

import java.awt.Point;
import java.awt.Rectangle;
import org.qyouti.barcode.ZXingResult;

/**
 *
 * @author jon
 */
public class BarcodeResult
{
  Rectangle[] barcodesearchrect = new Rectangle[4];
  ZXingResult barcoderesult;
  int location;
  Point start;
  Point end;
  String printid;
  String pageid;

  public int getLocation()
  {
    return location;
  }
  
  public int getBarcodeSearchRectCount()
  {
    return barcodesearchrect.length;
  }
  
  public Rectangle getBarcodeSearchRect( int n )
  {
    return barcodesearchrect[n];
  }

  public ZXingResult getBarcodeResult()
  {
    return barcoderesult;
  }

  public String getPrintID()
  {
    return printid;
  }

  public String getPageID()
  {
    return pageid;
  }

  public Point getStartPoint()
  {
    return start;
  }

  public Point getEndPoint()
  {
    return end;
  }
  
  
}
