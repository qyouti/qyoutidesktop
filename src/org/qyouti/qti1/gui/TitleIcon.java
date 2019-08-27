/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti.qti1.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.util.*;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

/**
 *
 * @author jon
 */
public class TitleIcon
        extends SVGIcon
{

  String content;

  public TitleIcon( int qnumber, String str, int w, int h, int fs )
  {
    width = w;
    height = h;

    DOMImplementation impl = GenericDOMImplementation.getDOMImplementation();
    String svgNS = SVGConstants.SVG_NAMESPACE_URI;
    Document doc = impl.createDocument(svgNS, "svg", null);
    org.w3c.dom.Element g = doc.createElementNS(svgNS, "g");
    org.w3c.dom.Element t = doc.createElementNS(svgNS, "text");
    t.setAttribute( "class", "qyouti-question-title" );
    t.setAttribute("x", "0");
    t.setAttribute("y", Integer.toString(  fs + ((h-fs)/2)  )  );
    t.setAttribute("font-size", Integer.toString(fs));
    if ( str != null )
      t.setTextContent( str );
    else
      t.setTextContent( "Question " + qnumber );

    g.appendChild(t);
    setSVG(g);
  }
}
