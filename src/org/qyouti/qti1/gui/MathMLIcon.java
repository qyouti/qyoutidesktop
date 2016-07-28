/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.qyouti.qti1.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.Icon;
import net.sourceforge.jeuclid.context.LayoutContextImpl;
import net.sourceforge.jeuclid.converter.Converter;
import net.sourceforge.jeuclid.converter.ConverterPlugin;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.util.*;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author jon
 */
public class MathMLIcon
        extends SVGIcon
{

    public MathMLIcon( Document doc, int w, int h )
    {
        width = w;
        height = h;

        try
        {
            Converter mathmlconv = Converter.getInstance();
            ConverterPlugin.DocumentWithDimension svgdoc = mathmlconv.convert(doc, Converter.TYPE_SVG, LayoutContextImpl.getDefaultLayoutContext());
            Dimension d = svgdoc.getDimension();
            Document domdoc = svgdoc.getDocument();
            Element root = domdoc.getDocumentElement();
            Element g = (Element) domdoc.createElementNS(SVGConstants.SVG_NAMESPACE_URI,"g");
            g.setAttribute("transform", "scale( " +
                    (int)(width/d.getWidth()) + " " +
                    (int)(height/d.getHeight()) + ")"
                    );
            g.appendChild( root );
            setSVG( g );

        } catch (Exception e)
        {
            e.printStackTrace();
        }

    }

}
