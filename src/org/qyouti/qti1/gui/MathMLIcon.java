/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.qyouti.qti1.gui;

import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;
import net.sourceforge.jeuclid.context.LayoutContextImpl;
import net.sourceforge.jeuclid.converter.Converter;
import net.sourceforge.jeuclid.converter.ConverterPlugin;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

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
            setSVG( svgdoc.getDocument().getDocumentElement() );

        } catch (Exception e)
        {
            e.printStackTrace();
        }

    }

}
