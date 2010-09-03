/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.qyouti.qti1.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

/**
 *
 * @author jon
 */
public class SVGImageIcon
        extends SVGIcon
{
    String uri;

    public SVGImageIcon( String u, int w, int h )
    {
        width = w;
        height = h;
        uri = "file:///home/jon/Desktop/aleph.png"; // u;

        DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
        String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
        Document doc = impl.createDocument(svgNS, "svg", null);
        org.w3c.dom.Element e = (org.w3c.dom.Element) doc.createElementNS(svgNS,"image");
        // put image at origin - it will be moved to right location when
        // it is wrapped in a group and translated.
        e.setAttribute("x", "0" );
        e.setAttribute("y", "0" );
        e.setAttribute("width", Integer.toString(width) );
        e.setAttribute("height", Integer.toString(height) );
        e.setAttributeNS("http://www.w3.org/1999/xlink", "xlink:href", uri );
        //e.setAttribute("xml:base", "/home/jon/Desktop");

        setSVG( e );
    }

}
