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

import java.awt.geom.Rectangle2D;
import org.w3c.dom.Document;
import org.w3c.dom.svg.SVGDocument;

/**
 *
 * @author jon
 */
public class SvgConversionResult
{
  String svg;
  Rectangle2D bounds;       // in SVG units
  SVGDocument document;
  public SvgConversionResult( SVGDocument d, String s, Rectangle2D b ) { document = d; svg = s; bounds = b; }
  public SVGDocument getDocument() { return document; }
  public String getSvg() { return svg; }
  public int getHeight() { return (int) Math.ceil(bounds.getMaxY()); }
  public Rectangle2D getBounds() { return bounds; }
}
