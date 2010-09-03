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
public class PinkIcon
        extends SVGIcon
{
    static Color bordercolor = new Color( 255,   0, 255 );
    static Color   fillcolor = new Color( 255, 128, 255 );

    int borderwidth;


    public PinkIcon( int w, int h, int b )
    {
        width = w;
        height = h;
        borderwidth = b;

        DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
        String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
        Document doc = impl.createDocument(svgNS, "svg", null);
        org.w3c.dom.Element g  = (org.w3c.dom.Element) doc.createElementNS(svgNS,"g");
        org.w3c.dom.Element r1 = (org.w3c.dom.Element) doc.createElementNS(svgNS,"rect");
        org.w3c.dom.Element r2 = (org.w3c.dom.Element) doc.createElementNS(svgNS,"rect");
        r1.setAttribute("x", "0" );
        r1.setAttribute("y", "0" );
        r1.setAttribute("width", Integer.toString(width) );
        r1.setAttribute("height", Integer.toString(height) );
        r1.setAttribute("fill",  "#ff80ff" );
        r1.setAttribute("stroke", "none" );
        r2.setAttribute("x", Integer.toString(borderwidth) );
        r2.setAttribute("y", Integer.toString(borderwidth) );
        r2.setAttribute("width",  Integer.toString(width-2*borderwidth)  );
        r2.setAttribute("height", Integer.toString(height-2*borderwidth) );
        r2.setAttribute("fill",  "#ffc0ff" );
        r2.setAttribute("stroke", "none" );

        g.appendChild(r1);
        g.appendChild(r2);
        setSVG( g );
    }

}
