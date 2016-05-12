/*
 *
 * Copyright 2010 Leeds Metropolitan University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may 
 * not use this file except in compliance with the License. You may obtain 
 * a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 *
 *
 */



/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.qyouti.qrcode;

import com.google.zxing.LuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.ResultMetadataType;
import com.google.zxing.ResultPoint;
import java.awt.*;
import java.util.Vector;

/**
 *
 * @author jon
 */
public class QRScanResult
{
  Result wrapped_result;
  
  double blackness;
  int twist;
  int width, height;
  int image=0;               // if multiple images, index to one where qrcode was found

  ResultPoint[] points = null;


  public QRScanResult( LuminanceSource source, Result wr, double blackness, int twist )
  {
    if  ( wr==null )
      throw new IllegalArgumentException( "Null qrcode result." );
    wrapped_result = wr;
    this.width  = source.getWidth();
    this.height = source.getHeight();
    this.blackness = blackness;
    this.twist = twist;
  }

  public ResultPoint[] getResultPoints()
  {
    if ( points != null ) return points;
    ResultPoint[] original = wrapped_result.getResultPoints();
    if ( original == null ) return null;
    points = new ResultPoint[original.length];
    for ( int i=0; i<points.length; i++ )
    {
      switch ( twist )
      {
        case 1:
          points[i] = new ResultPoint( width-original[i].getY(),        original[i].getX() );
          break;
        case 2:
          points[i] = new ResultPoint( width-original[i].getX(), height-original[i].getY() );
          break;
        case 3:
          points[i] = new ResultPoint(       original[i].getY(), height-original[i].getX() );
          break;
        default:
          points[i] = original[i];
      }
    }
    return points;
  }

  /**
   * ResultPoints start in the coordinates of the search area
   * 
   */
  public void toPageCoordinates( int quarterturns, int pwidth, int pheight, Rectangle searcharea )
  {
    if ( quarterturns == 0 )
      return;
    throw new IllegalArgumentException( "Cannot handle rotations yet." );
  }
  
  public double getBlackness()
  {
    return blackness;
  }
  
  public String getText()
  {
    String str = wrapped_result.getText();
    if ( str == null ) return null;
    if ( str.length() == 0 ) return str;
    return str.substring( 1 );
  }

  public byte[] getBytes()
  {
    Vector segments = (Vector)wrapped_result.getResultMetadata().get( ResultMetadataType.BYTE_SEGMENTS );
    if ( segments == null ) return null;
    Object o = segments.get( 0 );
    if ( o==null || !(o instanceof byte[]) ) return null;
    byte[] xoutput = (byte[])o;
    byte[] output = new byte[xoutput.length-1];
    System.arraycopy( xoutput, 1, output, 0, output.length );
    return output;
  }

  public void setImageIndex( int i )
  {
    image = i;
  }
  public int getImageIndex()
  {
    return image;
  }
}
