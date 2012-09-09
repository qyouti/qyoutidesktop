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
public class TickBoxIcon
        extends UserInputIcon
{


    int borderwidth;
    int padding;
    boolean crossed=false;


    public TickBoxIcon( int w, int h, int b, int p )
    {
        width = w;
        height = h;
        borderwidth = b;
        padding = p;

        DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
        String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
        Document doc = impl.createDocument(svgNS, "svg", null);
        org.w3c.dom.Element g  = (org.w3c.dom.Element) doc.createElementNS(svgNS,"g");
//        org.w3c.dom.Element rw = (org.w3c.dom.Element) doc.createElementNS(svgNS,"rect");
//        org.w3c.dom.Element r1 = (org.w3c.dom.Element) doc.createElementNS(svgNS,"rect");
        org.w3c.dom.Element r2 = (org.w3c.dom.Element) doc.createElementNS(svgNS,"rect");
        org.w3c.dom.Element ln1 = null;
        org.w3c.dom.Element ln2 = null;
//        rw.setAttribute("x", "0" );
//        rw.setAttribute("y", "0" );
//        rw.setAttribute("width", Integer.toString(width) );
//        rw.setAttribute("height", Integer.toString(height) );
//        rw.setAttribute("fill",  "#ffffff" );
//        r1.setAttribute("stroke", "none" );
//        r1.setAttribute("x", Integer.toString( padding ) );
//        r1.setAttribute("y", Integer.toString( padding ) );
//        r1.setAttribute("width", Integer.toString(width-2*padding) );
//        r1.setAttribute("height", Integer.toString(height-2*padding) );
//        r1.setAttribute("fill",  "#000000" );
//        r1.setAttribute("stroke", "none" );

        r2.setAttribute("x", Integer.toString( padding ) );                 //Integer.toString(borderwidth+padding) );
        r2.setAttribute("y", Integer.toString( padding ) );                 //Integer.toString(borderwidth+padding) );
        r2.setAttribute("width",  Integer.toString( width-2*padding  ) );   //Integer.toString(width-2*(borderwidth+padding))  );
        r2.setAttribute("height", Integer.toString( height-2*padding ) );   //Integer.toString(height-2*(borderwidth+padding)) );
        // assumes fill pattern was set up at top of svg.
        r2.setAttribute("fill",  "url(#stripes)" );
        r2.setAttribute("stroke", "none" );

        int inset = 2*borderwidth+padding;

        // g.appendChild(r1);
        g.appendChild(r2);


          ln1 = (org.w3c.dom.Element) doc.createElementNS(svgNS,"line");
          ln2 = (org.w3c.dom.Element) doc.createElementNS(svgNS,"line");
          ln1.setAttribute("x1", Integer.toString( inset ) );
          ln1.setAttribute("y1", Integer.toString( inset ) );
          ln1.setAttribute("x2", Integer.toString( width-inset ) );
          ln1.setAttribute("y2", Integer.toString( height-inset ) );
          ln1.setAttribute("stroke", "#ffffff" );
          ln1.setAttribute("stroke-linecap", "round" );
          ln1.setAttribute("stroke-width", Integer.toString( height / 10 ) );

          ln2.setAttribute("x1", Integer.toString( inset ) );
          ln2.setAttribute("y2", Integer.toString( inset ) );
          ln2.setAttribute("x2", Integer.toString( width-inset ) );
          ln2.setAttribute("y1", Integer.toString( height-inset ) );
          ln2.setAttribute("stroke", "#ffffff" );
          ln2.setAttribute("stroke-linecap", "round" );
          ln2.setAttribute("stroke-width", Integer.toString( height / 10 ) );

          g.appendChild( ln1 );
          g.appendChild( ln2 );

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
