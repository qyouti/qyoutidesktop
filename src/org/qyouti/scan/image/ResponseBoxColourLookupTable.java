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

package org.qyouti.scan.image;

import java.awt.Color;
import java.awt.image.LookupTable;

/**
 *
 * @author jon
 */
public class ResponseBoxColourLookupTable 
        extends LookupTable
{
  int n_black, n_white;
  int threshold;

  double blackness, range;

  public ResponseBoxColourLookupTable( int components, double blackness, double lightness )
  {
    super( 0, components );
    this.blackness = blackness;
    this.range = lightness - blackness;
    n_black = n_white = 0;
    setThreshold( 0.85 );
  }


  public void setThreshold( double t )
  {
    threshold = (int) (blackness + (range*t));
  }

  @Override
  public int[] lookupPixel(int[] src, int[] dest)
  {
    // arrays should have three items; red, green, blue
    // source green and blue are ignored
    // output based on threshold applied to red channel

    //dest[0] = src[0] < 174 ? 0 : 255;
    // extra check - pink pixels to white
//    float[] hsv = Color.RGBtoHSB( src[0], src[1], src[2], null );
//    if ( hsv[1] > 0.2 && (hsv[0] > (290.0/360.0) ) )
//      dest[0] = 255;
//    else
      dest[0] = src[0] < threshold ? 0 : 255;
    dest[1] = dest[0];
    dest[2] = dest[0];
    if ( dest.length == 4 )
      dest[3] = 255;


    if ( dest[0] == 0 ) n_black++; else n_white++;
    return dest;
  }


  public void resetStatistics()
  {
    n_black = n_white = 0;
  }

  public int countBlackPixels()
  {
    return n_black;
  }

  public int countWhitePixels()
  {
    return n_white;
  }
}
