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
import java.util.*;
import org.apache.batik.dom.*;
import org.apache.batik.gvt.GraphicsNode;
import org.qyouti.fonts.*;
import org.w3c.dom.Document;
import org.w3c.dom.svg.SVGDocument;

/**
 *
 * @author jon
 */
public class SvgConversionResult
{
  //String svg;
  GenericDocument document;
  int w;
  int h;
  
  MissingGlyphReport missingglyphreport = new MissingGlyphReport();
  
  //GraphicsNode gvtRoot;
  public SvgConversionResult( GenericDocument d, int width, int height )
  {
    document = d; 
    w = width;
    h = height;
    /*gvtRoot = g;*/ 
  }
  
  public void addMissingGlyphReport( MissingGlyphReport mgr )
  {
    missingglyphreport.addReport( mgr );
  }
  
  public GenericDocument getDocument() { return document; }
  
  //public String getSvg() { return svg; }
  //  public int getHeight() { return (int) Math.ceil(gvtRoot.getGeometryBounds().getMaxY()); }
  //  public Rectangle2D getBounds() { return gvtRoot.getGeometryBounds(); }

  public int getWidth()
  {
    return w;
  }

  public int getHeight()
  {
    return h;
  }

  public MissingGlyphReport getMissingGlyphReport()
  {
    return missingglyphreport;
  }

}
