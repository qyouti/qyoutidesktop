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

import java.awt.Component;
import java.awt.geom.Rectangle2D;
import java.io.*;
import javax.swing.*;
import org.apache.batik.bridge.*;
import org.apache.batik.dom.*;
import org.apache.batik.dom.svg.*;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGIDGenerator;
import org.apache.batik.util.*;
import org.w3c.dom.*;
import org.w3c.dom.svg.SVGDocument;

/**
 *
 * @author jon
 */
public class ComponentToSvg
{
  private static SVGIDGenerator idgen = new SVGIDGenerator();


    public static SvgConversionResult convert(Component component, int width)
    {
      JScrollPane scrollpane = new JScrollPane();
      scrollpane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
      scrollpane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
      scrollpane.setViewportView(component);
      scrollpane.getViewport().setSize( width, component.getSize().height );
      scrollpane.getViewport().doLayout();

      return convert( component );
    }

    static SvgConversionResult convert(Component component)
    {
        String svg = null;
        try
        {
            // Get component to render into SVG
            DOMImplementation impl = GenericDOMImplementation.getDOMImplementation();
            GenericDocument document = (GenericDocument) impl.createDocument(SVGConstants.SVG_NAMESPACE_URI, "svg", null);
            SVGGeneratorContext genctx = SVGGeneratorContext.createDefault(document);
            genctx.setEmbeddedFontsOn(false);
            genctx.setIDGenerator(idgen);
            SVGGraphics2D svgGenerator = new SVGGraphics2D(genctx, false);
            component.paint(svgGenerator);
            Element root = document.getDocumentElement();
            svgGenerator.getRoot(root);

//            BridgeContext ctx = new BridgeContext(new UserAgentAdapter());
//            GVTBuilder builder = new GVTBuilder();
//            GraphicsNode gvtRoot = builder.build(ctx, document);
//            Rectangle2D rect = gvtRoot.getSensitiveBounds();
            //System.out.println("SVG bounds : " + rect );

            SvgConversionResult svgresult = new SvgConversionResult(document, null ); // gvtRoot);

            return svgresult;
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

//    public static String convertToString(Component component)
//    {
//        try
//        {
//            // Get component to render into SVG
//            DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
//            Document document = domImpl.createDocument("http://www.w3.org/200/svg", "svg", null);
//            SVGGeneratorContext genctx = SVGGeneratorContext.createDefault(document);
//            genctx.setEmbeddedFontsOn(false);
//            SVGGraphics2D svgGenerator = new SVGGraphics2D(genctx, true);
//            component.paint(svgGenerator);
//
//            CharArrayWriter writer = new CharArrayWriter();
//            svgGenerator.stream(writer, true);
//            writer.close();
//
//            return writer.toString();
//        } catch (Exception e)
//        {
//            e.printStackTrace();
//        }
//
//        return null;
//    }
}
