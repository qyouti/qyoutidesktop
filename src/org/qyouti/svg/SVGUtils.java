/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.qyouti.svg;

import java.io.*;
import org.apache.batik.dom.*;
import org.apache.batik.dom.svg.*;
import org.apache.batik.svggen.*;
import org.apache.batik.util.*;
import org.w3c.dom.*;
import org.w3c.dom.svg.SVGDocument;

/**
 *
 * @author jon
 */
public class SVGUtils
{

  /**
   * Makes the child nodes of insertdoc's root nodes children
   *
   */
  public static void insertDocumentContents( GenericDocument targetdoc, Element targetnode, GenericDocument insertdoc )
  {
    Element targetsvg = targetdoc.getDocumentElement();
    Element insertsvg = insertdoc.getDocumentElement();
    NodeList nl = insertsvg.getChildNodes();

    for ( int i=0; i<nl.getLength(); i++ )
      targetnode.appendChild( targetdoc.importNode(nl.item(i), true) );
  }
  
  public static void appendFragmentToDocument( Document doc, Element fragment, double rot, int x, int y )
  {
    appendFragmentToDocument( doc.getDocumentElement(), fragment, rot, x, y );
  }
  
  public static void appendFragmentToDocument( Element dest, Element fragment, double rot, int x, int y )
  {
      String svgNS = SVGConstants.SVG_NAMESPACE_URI;
      Document doc = dest.getOwnerDocument();
      org.w3c.dom.Element g = doc.createElementNS(svgNS,"g");
      String t = "";
      if ( x!=0 || y!=0 )
        t += "translate("+x+","+y+")";
      if ( rot != 0.0 )
        t += "rotate(" + rot + ") ";
      if ( t.length() > 0 )
        g.setAttribute("transform", t );
      doc.adoptNode(fragment);
      Node imported = doc.importNode( fragment, true);
      g.appendChild(imported);
      dest.appendChild( g );
  }
  
  public static void main( String[] params )
          throws IOException
  {
    System.out.println( "SVG font test..." );
    DOMImplementation impl =
    GenericDOMImplementation.getDOMImplementation();
    String svgNS = "http://www.w3.org/2000/svg";
    Document myFactory = impl.createDocument(svgNS, "svg", null);

    SVGGeneratorContext ctx = SVGGeneratorContext.createDefault(myFactory);
    ctx.setEmbeddedFontsOn(true);
    SVGGraphics2D g2d = new SVGGraphics2D(ctx, false);
    g2d.drawString( "Hello world", 0, 0 );
    boolean useCSS = true; // we want to use CSS style attributes
    Writer out = new OutputStreamWriter(System.out, "UTF-8");
    g2d.stream(out, useCSS);    
  }
}
