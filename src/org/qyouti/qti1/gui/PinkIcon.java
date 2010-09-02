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
        org.w3c.dom.Element e = (org.w3c.dom.Element) doc.createElementNS(svgNS,"rect");
        e.setAttribute("x", "0" );
        e.setAttribute("y", "0" );
        e.setAttribute("width", Integer.toString(width) );
        e.setAttribute("height", Integer.toString(height) );
        e.setAttribute("style", "fill:rgb(255,128,255)" );

        setSVG( e );
    }

}
