/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.qyouti.qti1.gui;

import java.awt.Component;
import java.awt.Insets;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.HTMLDocument;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.swing.JSVGCanvas;
import org.qyouti.dialog.TextPaneWrapper;
import org.qyouti.print.ComponentToSvg;
import org.qyouti.print.SvgConversionResult;
import org.qyouti.qti1.QTIElement;
import org.qyouti.qti1.QTIMatmedia;
import org.qyouti.qti1.UnrecognisedQTIElement;
import org.qyouti.qti1.element.*;
import org.qyouti.qti1.ext.webct.QTIExtensionWebctMaterialwebeq;
import org.w3c.dom.svg.SVGSVGElement;

/**
 *
 * @author jon
 */
public class QTIItemRenderer
//        extends QTIComponent
{
    JPanel comp;

    JTextPane textPane;
    SvgConversionResult svgres;

    public static void main(String args[])
    {
        new QTIItemRenderer( null );
    }



    private void renderElement( QTIElement e, RenderState state )
    {
        if ( e instanceof UnrecognisedQTIElement )
        {
            UnrecognisedQTIElement unrec = (UnrecognisedQTIElement)e;
            state.html.append( "[[Unrec element type: " + unrec.debug() + "]]" );
        }

        if ( e instanceof QTIElementPresentation )
        {
            state.html.append("<html><body><div>");
            state.open_block = true;
        }

        if ( e instanceof QTIMatmedia )
        {
            if ( !state.open_block )
            {
                state.html.append("<div>");
                state.open_block = true;
            }
            QTIMatmedia matmedia = (QTIMatmedia)e;
            if ( matmedia.isSupported() )
            {
                if ( e instanceof QTIElementMatimage )
                {
                    QTIElementMatimage matimage = (QTIElementMatimage)e;
                    String uri = matimage.getUri();
                    if ( uri == null || uri.length() == 0 )
                    {
                        state.html.append( "<strong>[UNSUPPORTED: Image with unspecified media location.]</strong>" );
                    }
                    else
                    {
                        state.html.append("<img src=\"" + uri + "\" />");
                    }
                }
                if ( e instanceof QTIElementMattext || e instanceof QTIElementMatemtext)
                {
                    if ( e instanceof QTIElementMatemtext )
                        state.html.append("<em>");
                    QTIElementMattext mattext = (QTIElementMattext)e;
                    state.html.append( mattext.getContent() );
                    if ( e instanceof QTIElementMatemtext )
                        state.html.append("</em>");
                }
                if ( e instanceof QTIExtensionWebctMaterialwebeq )
                {
                    QTIExtensionWebctMaterialwebeq webeq = (QTIExtensionWebctMaterialwebeq)e;
                    QTIExtensionWebctMaterialwebeq.Fragment[] fragments;
                    QTIExtensionWebctMaterialwebeq.MatMLEq eq;
                    fragments = webeq.getContentFragments();
                    MathMLIcon mathicon;
                    if ( state.open_block )
                    {
                        state.html.append("</div>\n");
                        state.open_block = false;
                    }
                    state.html.append("<div>\n");
                    for ( int i=0; i<fragments.length; i++ )
                    {
                        if ( fragments[i] instanceof QTIExtensionWebctMaterialwebeq.MatMLEq )
                        {
                            eq = (QTIExtensionWebctMaterialwebeq.MatMLEq)fragments[i];
                            mathicon = new MathMLIcon( eq.getMathML(), eq.getWidth(), eq.getHeight() );
                            state.inserts.add( new InteractionInsert(state.next_id, e, null, mathicon ) );
                            state.html.append( "<span id=\"qti_insert_" + (state.next_id++) + "\">*</span>\n" );
                        }
                        else
                        {
                            state.html.append( fragments[i].content );
                        }
                    }
                    state.html.append("</div>\n");
                }
            }
            else
            {
                state.html.append( "<strong>[UNSUPPORTED MEDIA FORMAT REQUESTED HERE: " + e.getClass().getCanonicalName() + "]</strong>" );
            }
        }

        if ( e instanceof QTIElementRenderchoice )
        {
            if ( state.open_block )
            {
                state.html.append("</div>\n");
                state.open_block = false;
            }
            state.html.append( "\n<table>\n" );
        }

        if ( e instanceof QTIElementResponselabel )
        {
            state.inserts.add( new InteractionInsert(state.next_id, e, null, new PinkIcon(25,25,4) ) );
            state.html.append( "<tr>\n" );
            // The span will always have one character in it - which will be
            // deleted and replaced with a component.
            state.html.append( "<td><span id=\"qti_insert_" + (state.next_id++) + "\">*</span></td>\n" );
            state.html.append( "<td><div>" );
            state.open_block = true;
        }


        // Fill in sub-elements
        Vector<QTIElement> list = e.findElements( QTIElement.class, false );
        for ( int i=0; i<list.size(); i++ )
        {
            renderElement( list.elementAt(i), state );
        }
        // Complete wrapping of sub elements...


        if ( e instanceof QTIElementResponselabel )
        {
            if ( state.open_block )
            {
                state.html.append("</div>\n");
                state.open_block = false;
            }
            state.html.append( "</td>\n</tr>\n" );
        }

        if ( e instanceof QTIElementRenderchoice )
        {
            state.html.append( "</table>\n" );
        }

        if ( e instanceof QTIElementPresentation )
        {
            if ( state.open_block )
            {
                state.html.append("</div>\n");
                state.open_block = false;
            }
            state.html.append("\n</body>\n<html>");
            state.open_block = true;
        }


    }

    
    /**
     * An HTML version of the item is built up from elements in the presentation
     * section.  Empty <span id="bla di bla"></span> elements are used as
     * place holders for stuff that must be insert later as components.
     * These components are indexed against the IDs.  Once the HTML is in the
     * document model the positions of the placeholders are found and the <span>
     * elements replaced with the corresponding components.
     * @param item
     */
    public QTIItemRenderer( QTIElementItem item )
    {
        QTIElementPresentation presentation = item.getPresentation();

        // Compose the HTML
        RenderState state = new RenderState();
        renderElement( presentation, state );
        // Iterate child elements.
        System.out.println( "==================================================" );
        System.out.println( state.html );
        System.out.println( "==================================================" );

        // Put the HTML into the Text Pane
        textPane = new TextPaneWrapper();
        textPane.setContentType("text/html");
        textPane.setText( state.html.toString() );

        Document doc = textPane.getDocument();
        if ( !(doc instanceof HTMLDocument) )
            throw new IllegalArgumentException( "Expected HTML document." );

        HTMLDocument htmldoc = (HTMLDocument)doc;
        Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
        Element docelement;
        int offset;
        //PinkIcon picon = new PinkIcon( 40, 40, 4 );
        Style s;
        InteractionInsert insert;
        for ( int i=0; i< state.inserts.size(); i++ )
        {
            insert = state.inserts.get(i);
            docelement = htmldoc.getElement( "qti_insert_" + insert.id );
            if ( docelement == null )
                continue;
            offset = docelement.getStartOffset();
            if ( insert.icon != null )
            {
                s = htmldoc.addStyle("qyouti_svg_icon_" + insert.id, def);
                StyleConstants.setIcon(s, insert.icon );
                try
                {
                    doc.remove( offset, 1 );
                    doc.insertString(offset, " ", s);
                } catch (BadLocationException ex)
                {
                    Logger.getLogger(QTIItemRenderer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        JScrollPane scrollpane = new JScrollPane();
        scrollpane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollpane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scrollpane.setViewportView( textPane );
        scrollpane.getViewport().setSize(400, 50);
        scrollpane.getViewport().doLayout();

        //System.out.println( "Edit component location: " + field.getX() + " " + field.getY() );
        System.out.println( "textpane size: " + textPane.getSize() );

        // Get the textPane to paint itself into an SVG document.
        // This will have blank rectangles whereever SCGIcon object are.
        svgres = ComponentToSvg.convert( textPane );
        for ( int i=0; i< state.inserts.size(); i++ )
        {
            insert = state.inserts.get(i);
            if ( insert.icon != null )
                insert.icon.paintSVG( svgres.getDocument() );
        }

    }



    public org.w3c.dom.Document getSVGDocument()
    {
        return svgres.getDocument();
    }


    private class InteractionInsert
    {
        int id;
        QTIElement element;
        Component component = null;
        SVGIcon icon = null;
        InteractionInsert( int id, QTIElement element, Component component, SVGIcon icon )
        {
            this.id        = id;
            this.element   = element;
            this.component = component;
            this.icon      = icon;
        }
    }
    
    private class RenderState
    {
        StringBuffer html = new StringBuffer();
        boolean open_block = false;
        int next_id = 1001;
        Vector<InteractionInsert> inserts = new Vector<InteractionInsert>();
    }
}
