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
import org.qyouti.svg.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGSVGElement;

/**
 *
 * @author jon
 */
public class SVGIcon
        implements Icon
{
    int width=100;
    int height=50;

    public Integer x = null;
    public Integer y = null;
    Element fragment;

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y)
    {
      this.x = new Integer( x );
      this.y = new Integer( y );
//      g.setColor(Color.red);
//      g.drawLine(x, y, x+width, y+height);
//      g.drawLine(x, y+height, x+width, y);
    }

    public void setSVG( Element fragment )
    {
        this.fragment = fragment;
    }

    public void paintSVG( Document doc )
    {
        if ( x == null || y == null )
            throw new IllegalArgumentException( "SVGIcon not ready to paint into SVG doc" );
        SVGUtils.appendFragmentToDocument( doc, fragment, 0.0, x.intValue(), y.intValue() );
    }


    @Override
    public int getIconWidth()
    {
        return width;
    }

    @Override
    public int getIconHeight()
    {
        return height;
    }

}
