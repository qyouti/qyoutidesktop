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

import org.w3c.dom.Document;

/**
 *
 * @author jon
 */
public class SvgConversionResult
{
  String svg;
  int height;  // in 100th inch
  Document document;
  public SvgConversionResult( Document d, String s, int h ) { document = d; svg = s; height = h; }
  public Document getDocument() { return document; }
  public String getSvg() { return svg; }
  public int getHeight() { return height; }
}
