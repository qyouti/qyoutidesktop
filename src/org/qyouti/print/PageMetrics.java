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

package org.qyouti.print;

/**
 *  All metrics in inch/100
 * @author jon
 */
public class PageMetrics
{
  public static final PageMetrics A4_COMPACT_ONE_COLUMN;

  // qrcode metrics based on centres of corner boxes, not corners.
  public int qrcode_height;
  public int qrcode_from_left;
  public int qrcode_top_from_top;
  public int qrcode_bottom_from_top;
  public int dont_mark_width;

  static
  {
    A4_COMPACT_ONE_COLUMN = new PageMetrics();
    A4_COMPACT_ONE_COLUMN.qrcode_height          =   60;
    A4_COMPACT_ONE_COLUMN.qrcode_from_left       =   50;
    A4_COMPACT_ONE_COLUMN.qrcode_top_from_top    =   75;
    A4_COMPACT_ONE_COLUMN.qrcode_bottom_from_top = 1050;
    A4_COMPACT_ONE_COLUMN.dont_mark_width        =  150;
  }
}
