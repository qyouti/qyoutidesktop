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

import java.awt.image.LookupTable;

/**
 *
 * @author jon
 */
public class QRCodeColourLookupTable 
        extends LookupTable
{
  int n_black, n_white;
  int threshold = 128;

  public QRCodeColourLookupTable( int components )
  {
    super( 0, components );
    n_black = n_white = 0;
  }

  public void setThreshold( int t )
  {
    threshold = t;
  }

  @Override
  public int[] lookupPixel(int[] src, int[] dest)
  {
    // arrays should have three items; red, green, blue
    // source red and green are ignored
    // output based on threshold applied to blue channel

    dest[2] = src[2] < threshold ? 0 : 255;
    dest[0] = dest[2];
    dest[1] = dest[2];

    if ( dest.length == 4 )
      dest[3] = 255;

    if ( dest[2] == 0 ) n_black++; else n_white++;
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
