/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.qyouti.qti1.gui;


import java.awt.Rectangle;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

/**
 *
 * @author jon
 */
public class PinkIcon
        extends SVGIcon
{
    static String bordercolor = "#ffbbff";
    static String   fillcolor = "#ffccff";

    int borderwidth;
    int padding;


    public PinkIcon( int w, int h, int b, int p )
    {
        width = w;
        height = h;
        borderwidth = b;
        padding = p;

        DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
        String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
        Document doc = impl.createDocument(svgNS, "svg", null);
        org.w3c.dom.Element g  = (org.w3c.dom.Element) doc.createElementNS(svgNS,"g");
        org.w3c.dom.Element rw = (org.w3c.dom.Element) doc.createElementNS(svgNS,"rect");
        org.w3c.dom.Element r1 = (org.w3c.dom.Element) doc.createElementNS(svgNS,"rect");
        org.w3c.dom.Element r2 = (org.w3c.dom.Element) doc.createElementNS(svgNS,"rect");
        rw.setAttribute("x", "0" );
        rw.setAttribute("y", "0" );
        rw.setAttribute("width", Integer.toString(width) );
        rw.setAttribute("height", Integer.toString(height) );
        rw.setAttribute("fill",  "#ffffff" );
        r1.setAttribute("stroke", "none" );
        r1.setAttribute("x", Integer.toString( padding ) );
        r1.setAttribute("y", Integer.toString( padding ) );
        r1.setAttribute("width", Integer.toString(width-2*padding) );
        r1.setAttribute("height", Integer.toString(height-2*padding) );
        r1.setAttribute("fill",  bordercolor );
        r1.setAttribute("stroke", "none" );
        r2.setAttribute("x", Integer.toString(borderwidth+padding) );
        r2.setAttribute("y", Integer.toString(borderwidth+padding) );
        r2.setAttribute("width",  Integer.toString(width-2*(borderwidth+padding))  );
        r2.setAttribute("height", Integer.toString(height-2*(borderwidth+padding)) );
        r2.setAttribute("fill",  fillcolor );
        r2.setAttribute("stroke", "none" );

        g.appendChild(r1);
        g.appendChild(r2);
        setSVG( g );
    }


    public Rectangle getPinkRectangle()
    {
      return new Rectangle(
          x+padding,
          y+padding,
          getIconWidth() - 2*padding,
          getIconHeight() - 2*padding );
    }
}
