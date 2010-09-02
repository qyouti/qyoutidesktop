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
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.*;
import org.w3c.dom.svg.SVGDocument;

/**
 *
 * @author jon
 */
public class ComponentToSvg
{

    public ComponentToSvg()
    {
    }

    public static SvgConversionResult convert(Component component)
    {
        String svg = null;
        try
        {
            svg = convertToString(component);
            CharArrayReader reader = new CharArrayReader(svg.toCharArray());
            SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(XMLResourceDescriptor.getXMLParserClassName());
            SVGDocument svgdoc = f.createSVGDocument("?", reader);
            BridgeContext ctx = new BridgeContext(new UserAgentAdapter());
            GVTBuilder builder = new GVTBuilder();

            GraphicsNode gvtRoot = builder.build(ctx, svgdoc);
            Rectangle2D rect = gvtRoot.getSensitiveBounds();
            System.out.println("SVG bounds : " + rect.toString());

            SvgConversionResult svgresult = new SvgConversionResult(svgdoc, svg, (int) Math.ceil(rect.getMaxY()));

            /*
            Element svgelement = svgresult.getDocument().getDocumentElement();
            System.out.println("SVG Doc tag name: " + svgelement.getTagName());
            NodeList nl = svgelement.getElementsByTagName("g");
            if (nl.getLength() > 0)
            {
                System.out.println("Processing transform on svg tree.");
                Element gelement = (Element) nl.item(0);
                if (gelement == null)
                {
                    System.out.println("No 'g' element.");
                } else
                {
                    gelement.setAttribute("transform", "scale(0.1)");
                }
            }
            */
            
            return svgresult;
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public static String convertToString(Component component)
    {
        try
        {
            // Get component to render into SVG
            DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
            Document document = domImpl.createDocument("http://www.w3.org/200/svg", "svg", null);
            SVGGeneratorContext genctx = SVGGeneratorContext.createDefault(document);
            genctx.setEmbeddedFontsOn(false);
            SVGGraphics2D svgGenerator = new SVGGraphics2D(genctx, true);
            component.paint(svgGenerator);

            CharArrayWriter writer = new CharArrayWriter();
            svgGenerator.stream(writer, true);
            writer.close();

            return writer.toString();
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }
}
