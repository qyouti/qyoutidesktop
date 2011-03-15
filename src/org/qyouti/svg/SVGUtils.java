/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.qyouti.svg;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
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
  public static void insertDocumentContents( SVGDocument targetdoc, Element targetnode, SVGDocument insertdoc )
  {
    Element targetsvg = targetdoc.getDocumentElement();
    Element insertsvg = insertdoc.getDocumentElement();
    NodeList nl = insertsvg.getChildNodes();

    for ( int i=0; i<nl.getLength(); i++ )
      targetnode.appendChild( targetdoc.importNode(nl.item(i), true) );
  }

}
