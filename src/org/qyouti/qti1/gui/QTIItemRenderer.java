/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti.qti1.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.html.HTMLDocument;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.qyouti.dialog.TextPaneWrapper;
import org.qyouti.print.ComponentToSvg;
import org.qyouti.print.SvgConversionResult;
import org.qyouti.qti1.*;
import org.qyouti.qti1.element.*;
import org.qyouti.qti1.ext.webct.QTIExtensionWebctMaterialwebeq;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.NodeList;

/**
 *
 * @author jon
 */
public class QTIItemRenderer
//        extends QTIComponent
{
  URI examfolderuri;
  JPanel comp;
  JTextPane textPane;
  SvgConversionResult svgres;
  // width of space on page for a question
  // in milli-inches

  static QTIMetrics metrics = null;

  private void renderElement(QTIElement e, RenderState state)
  {
    if (e instanceof UnrecognisedQTIElement)
    {
      UnrecognisedQTIElement unrec = (UnrecognisedQTIElement) e;
      state.html.append("[[Unrec element type: " + unrec.debug() + "]]");
    }

    if (e instanceof QTIElementPresentation)
    {
      state.html.append("<html>\n<body style=\"font-size: " +
              metrics.getPropertySvgUnitsInt("fontsize") +
              "px\">\n");
      state.html.append( "<table><tr><td valign=\"top\">" );

      QRCodeIcon qricon = new QRCodeIcon(
              metrics.getPropertySvgUnitsInt("item-xoffset")-
              metrics.getPropertySvgUnitsInt("qrcode-xoffset"),
              metrics.getPropertySvgUnitsInt("qrcode-width"),
              state.item.getIdent(),
              metrics.getPropertySvgUnitsInt("qrcode-width")
              );
      state.inserts.add(new InteractionInsert(state.next_id, e, null, qricon));
      // this is also recorded separately because it needs to be (re)processed last
      state.qriconinsert = new InteractionInsert(state.next_id, e, null, qricon);
      state.html.append("<span id=\"qti_insert_" + (state.next_id++) + "\">*</span>\n");

      state.html.append( "</td><td>" );
      state.html.append("<div>");
      TitleIcon titicon = new TitleIcon(
              metrics.getPropertySvgUnitsInt("item-width"),
              metrics.getPropertySvgUnitsInt("item-title-height"),
              metrics.getPropertySvgUnitsInt("item-title-font-height")
              );
      state.inserts.add(new InteractionInsert(state.next_id, e, null, titicon));
      state.html.append("<span id=\"qti_insert_" + (state.next_id++) + "\">*</span>\n");
      state.html.append("</div>");
      state.html.append("<div>");
      state.open_block = true;
    }

    if (e instanceof QTIMatmedia)
    {
      if (!state.open_block)
      {
        state.html.append("<div>");
        state.open_block = true;
      }
      QTIMatmedia matmedia = (QTIMatmedia) e;
      if (matmedia.isSupported())
      {
        if (e instanceof QTIElementMatimage)
        {
          QTIElementMatimage matimage = (QTIElementMatimage) e;
          SVGImageIcon imicon;
          String attr_uri = matimage.getUri();
          if (attr_uri == null || attr_uri.length() == 0)
          {
            state.html.append("<strong>[UNSUPPORTED: Image with unspecified media location.]</strong>");
          } else
          {
            URI uri = null;
            try{uri = new URI(attr_uri);} catch (URISyntaxException ex) {}

            if ( !uri.isAbsolute() )
              uri = examfolderuri.resolve(uri);

            int w, h;
            w = matimage.getWidth();
            h = matimage.getHeight();
            if (w < 1 || h < 1)
            {
              h = w = 200;
              try
              {
                File imgfile = new File( uri );
                if ( imgfile.exists() && imgfile.isFile() )
                {
                  BufferedImage image = ImageIO.read(imgfile);
                  w = image.getWidth();
                  h = image.getHeight();
                }
              }
              catch ( Exception ex )
              {
                  h = w = 200;
              }
            }
            imicon = new SVGImageIcon(uri,
                    (int) QTIMetrics.qtiToSvg(w),
                    (int) QTIMetrics.qtiToSvg(h));
            state.inserts.add(new InteractionInsert(state.next_id, e, null, imicon));
            state.html.append("<span id=\"qti_insert_" + (state.next_id++) + "\">*</span>\n");
          }
        }
        if (e instanceof QTIElementMattext || e instanceof QTIElementMatemtext)
        {
          if (e instanceof QTIElementMatemtext)
          {
            state.html.append("<em>");
          }
          QTIElementMattext mattext = (QTIElementMattext) e;
          state.html.append(mattext.getContent());
          if (e instanceof QTIElementMatemtext)
          {
            state.html.append("</em>");
          }
        }
        if (e instanceof QTIExtensionWebctMaterialwebeq)
        {
          QTIExtensionWebctMaterialwebeq webeq = (QTIExtensionWebctMaterialwebeq) e;
          QTIExtensionWebctMaterialwebeq.Fragment[] fragments;
          QTIExtensionWebctMaterialwebeq.MatMLEq eq;
          fragments = webeq.getContentFragments();
          MathMLIcon mathicon;
          if (state.open_block)
          {
            state.html.append("</div>\n");
            state.open_block = false;
          }
          state.html.append("<div>\n");
          for (int i = 0; i < fragments.length; i++)
          {
            if (fragments[i] instanceof QTIExtensionWebctMaterialwebeq.MatMLEq)
            {
              eq = (QTIExtensionWebctMaterialwebeq.MatMLEq) fragments[i];
              mathicon = new MathMLIcon(
                      eq.getMathML(),
                      (int) QTIMetrics.qtiToSvg(eq.getWidth()),
                      (int) QTIMetrics.qtiToSvg(eq.getHeight()));
              state.inserts.add(new InteractionInsert(state.next_id, e, null, mathicon));
              state.html.append("<span id=\"qti_insert_" + (state.next_id++) + "\">*</span>\n");
            } else
            {
              state.html.append(fragments[i].content);
            }
          }
          state.html.append("</div>\n");
        }
      } else
      {
        state.html.append("<strong>[UNSUPPORTED MEDIA FORMAT REQUESTED HERE: " + e.getClass().getCanonicalName() + "]</strong>");
      }
    }

    if (e instanceof QTIElementRenderchoice)
    {
      if (state.open_block)
      {
        state.html.append("</div>\n");
        state.open_block = false;
      }
      state.html.append("\n<table style=\"margin: ");
      state.html.append(" " + (int) QTIMetrics.inchesToSvg(0.1) + "px 0px");
      state.html.append(" " + (int) QTIMetrics.inchesToSvg(0.1) + "px ");
      state.html.append(" 0px;\">");
    }

    if (e instanceof QTIElementResponselabel)
    {
      state.inserts.add(new InteractionInsert(state.next_id, e, null,
              new PinkIcon((int) QTIMetrics.inchesToSvg(0.24),
              (int) QTIMetrics.inchesToSvg(0.24),
              (int) QTIMetrics.inchesToSvg(0.02))));
      state.html.append("<tr>\n");
      // The span will always have one character in it - which will be
      // deleted and replaced with a component.
      state.html.append("<td valign=\"top\"><div style=\"margin: 0px");
      state.html.append(" " + (int) QTIMetrics.inchesToSvg(0.1) + "px ");
      state.html.append(" " + (int) QTIMetrics.inchesToSvg(0.05) + "px ");
      state.html.append(" 0px;\">");
      state.html.append("<span id=\"qti_insert_" + (state.next_id++) + "\">*</span></div></td>\n");
      state.html.append("<td valign=\"top\"><div>");
      state.open_block = true;
    }


    // Fill in sub-elements
    Vector<QTIElement> list = e.findElements(QTIElement.class, false);
    for (int i = 0; i < list.size(); i++)
    {
      renderElement(list.elementAt(i), state);
    }
    // Complete wrapping of sub elements...


    if (e instanceof QTIElementResponselabel)
    {
      if (state.open_block)
      {
        state.html.append("</div>\n");
        state.open_block = false;
      }
      state.html.append("</td>\n</tr>\n");
    }

    if (e instanceof QTIElementRenderchoice)
    {
      state.html.append("</table>\n");
    }

    if (e instanceof QTIElementPresentation)
    {
      if (state.open_block)
      {
        state.html.append("</div>\n");
        state.open_block = false;
      }
      state.html.append("\n</td>\n</tr>\n</table>\n</body>\n<html>");
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
  public QTIItemRenderer(URI examfolderuri, QTIElementItem item)
  {
    int i;
    this.examfolderuri = examfolderuri;
    QTIElementPresentation presentation = item.getPresentation();

    if ( metrics == null )
      metrics = new QTIMetrics();

    // Compose the HTML
    RenderState state = new RenderState();
    state.item = item;
    renderElement(presentation, state);
    // Iterate child elements.
    System.out.println("===============================================");
    System.out.println(state.html);
    System.out.println("===============================================");

    // Put the HTML into the Text Pane
    textPane = new TextPaneWrapper();
    //textPane.setBackground(Color.WHITE);
    textPane.setOpaque(false);
    textPane.setContentType("text/html");
    textPane.setText(state.html.toString());

    Document doc = textPane.getDocument();
    if (!(doc instanceof HTMLDocument))
    {
      throw new IllegalArgumentException("Expected HTML document.");
    }

    HTMLDocument htmldoc = (HTMLDocument) doc;
    Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
    Element docelement;
    int offset;
    Style s;
    InteractionInsert insert;
    for ( i = 0; i < state.inserts.size(); i++)
    {
      insert = state.inserts.get(i);
      docelement = htmldoc.getElement("qti_insert_" + insert.id);
      if (docelement == null)
      {
        continue;
      }
      offset = docelement.getStartOffset();
      if (insert.icon != null)
      {
        s = htmldoc.addStyle("qyouti_svg_icon_" + insert.id, def);
        StyleConstants.setIcon(s, insert.icon);
        try
        {
          doc.remove(offset, 1);
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
    scrollpane.setViewportView(textPane);
    scrollpane.getViewport().setSize(
            (metrics.getPropertySvgUnitsInt("item-xoffset")-
              metrics.getPropertySvgUnitsInt("qrcode-xoffset"))
              +metrics.getPropertySvgUnitsInt("item-width"), 50);
    scrollpane.getViewport().doLayout();

    //System.out.println( "Edit component location: " + field.getX() + " " + field.getY() );
    System.out.println("textpane size: " + textPane.getSize());

    // Get the textPane to paint itself into an SVG document.
    // This will have blank rectangles whereever SVGIcon objects are.
    svgres = ComponentToSvg.convert(textPane);
    Rectangle2D bounds = svgres.getBounds();
    org.w3c.dom.Element svgroot = svgres.getDocument().getDocumentElement();
//    svgroot.setAttribute(   "width", "" + QTIMetrics.svgToInches(bounds.getWidth())  + "in");
//    svgroot.setAttribute(  "height", "" + QTIMetrics.svgToInches(bounds.getHeight()) + "in");
//    svgroot.setAttribute( "viewBox", "" +
//            (int)QTIMetrics.inchesToSvg( -0.25 ) + " " +
//            (int)QTIMetrics.inchesToSvg( -0.25 ) + " " +
//            bounds.getMaxX() + " " +
//            bounds.getMaxY());
//    svgroot.setAttribute("transform", "translate( 1000 0 )" );

    // 'Paint' all the icons bar the qrcode
    for ( i = 0; i < state.inserts.size(); i++)
    {
      insert = state.inserts.get(i);
      if (insert.icon != null)
      {
        insert.icon.paintSVG(svgres.getDocument());
      }
    }
    // Find out where all the pink icons ended up and codify
    StringBuffer coords = new StringBuffer();
    PinkIcon picon;
    for ( i = 0; i < state.inserts.size(); i++)
    {
      insert = state.inserts.get(i);
      if (insert.icon != null && insert.icon instanceof PinkIcon )
      {
        picon = (PinkIcon)insert.icon;
        coords.append( picon.x );
        coords.append( " " );
        coords.append( picon.y );
        coords.append( " " );
      }
    }
    
    // do the qr code again last so the pink icon coordinates can be passed in.
    QRCodeIcon qricon = (QRCodeIcon)state.qriconinsert.icon;
    qricon.update( bounds.getHeight(), coords.toString() );
  }


  public org.w3c.dom.Document decorateItemForPreview( org.w3c.dom.Document docpreview )
  {
    int i;
    DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
    String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
    //org.w3c.dom.Document docpreview = impl.createDocument(svgNS, "svg", null);
    double widthinches = metrics.getPropertyInches("page-width") + 0.5;
    double heightinches = metrics.getPropertyInches("page-height") + 0.5;

    org.w3c.dom.Element svg = docpreview.createElementNS( svgNS, "svg" );
    svg.setAttributeNS(svgNS, "viewBox", "" +
            QTIMetrics.inchesToSvg(  0.0 )         + " " +
            QTIMetrics.inchesToSvg(  0.0 )         + " " +
            QTIMetrics.inchesToSvg(  widthinches ) + " " +
            QTIMetrics.inchesToSvg(  heightinches )
            );
    svg.setAttributeNS(svgNS, "width",  ""+widthinches+"in" );
    svg.setAttributeNS(svgNS, "height", ""+heightinches+"in" );

    org.w3c.dom.Element g = docpreview.createElementNS(svgNS, "g");
    svg.appendChild(g);

    org.w3c.dom.Element svginner =
            (org.w3c.dom.Element) docpreview.removeChild(docpreview.getDocumentElement());
    org.w3c.dom.Element itemg = docpreview.createElementNS( svgNS, "g" );
    itemg.setAttributeNS(svgNS, "transform",
            "translate(" +
            (int)QTIMetrics.inchesToSvg( 0.5 + metrics.getPropertyInches("qrcode-xoffset") ) +
            "," +
            (int)QTIMetrics.inchesToSvg( 1.0 ) +
            ")"
            );
    //while ( svginner.getChildNodes().getLength() != 0 )
    //  itemg.appendChild(svginner.getFirstChild());
    org.w3c.dom.Element rg = docpreview.createElementNS(svgNS, "rect");
    rg.setAttribute( "x", "0" );
    rg.setAttribute( "y", "0" );
    rg.setAttribute( "stroke", "rgb(0,0,0)" );
    rg.setAttribute( "stroke-width", "" + QTIMetrics.inchesToSvg( 0.02 ) );
    rg.setAttribute( "fill", "rgb(0,255,0)" );
    rg.setAttribute( "width",  "" + QTIMetrics.inchesToSvg(  1.0 ) );
    rg.setAttribute( "height", "" + QTIMetrics.inchesToSvg(  1.0 ) );
    itemg.appendChild( rg );

    svg.appendChild(itemg);
    docpreview.appendChild(svg);




    org.w3c.dom.Element r = docpreview.createElementNS(svgNS, "rect");
    r.setAttribute( "x", "" + QTIMetrics.inchesToSvg(  0.5 ) );
    r.setAttribute( "y", "" + QTIMetrics.inchesToSvg(  0.5 ) );
    r.setAttribute( "stroke", "rgb(0,0,0)" );
    r.setAttribute( "stroke-width", "" + QTIMetrics.inchesToSvg( 0.02 ) );
    r.setAttribute( "fill", "rgb(255,255,255)" );
    r.setAttribute( "width",  "" + metrics.getPropertySvgUnitsInt("page-width") );
    r.setAttribute( "height", "" + metrics.getPropertySvgUnitsInt("page-height") );
    g.appendChild( r );

    r = docpreview.createElementNS(svgNS, "rect");
    r.setAttribute( "x", "" + QTIMetrics.inchesToSvg(  0.5 ) );
    r.setAttribute( "y", "" + QTIMetrics.inchesToSvg(  0.5 ) );
    r.setAttribute( "stroke", "none" );
    r.setAttribute( "fill", "rgb(180,180,200)" );
    r.setAttribute( "width",  "" + QTIMetrics.inchesToSvg( 1.5 ) );
    r.setAttribute( "height", "" + metrics.getPropertySvgUnitsInt("page-height") );
    g.appendChild( r );

    org.w3c.dom.Element rt = docpreview.createElementNS(svgNS, "rect");
    rt.setAttribute( "x", "" + QTIMetrics.inchesToSvg( 0.5 ) );
    rt.setAttribute( "y", "" + QTIMetrics.inchesToSvg( 0.0 ) );
    rt.setAttribute( "stroke", "rgb(0,0,0)" );
    rt.setAttribute( "stroke-width", "" + QTIMetrics.inchesToSvg( 0.02 ) );
    rt.setAttribute( "fill", "rgb(255,255,200)" );
    rt.setAttribute( "width",  "" + metrics.getPropertySvgUnitsInt("page-width") );
    rt.setAttribute( "height", "" + QTIMetrics.inchesToSvg(  0.5 ) );
    g.appendChild( rt );

    org.w3c.dom.Element line;
    org.w3c.dom.Element t;
    double x;
//    for ( x=0.0, i=0; x<metrics.getPropertySvgUnits("page-width"); x+=QTIMetrics.inchesToSvg(  0.2 ), i++ )
//    {
//      line = docpreview.createElementNS(svgNS, "line");
//      line.setAttribute( "x1", "" + (x+QTIMetrics.inchesToSvg( 0.5 )) );
//      line.setAttribute( "y1", "" + QTIMetrics.inchesToSvg( 0.5 ) );
//      line.setAttribute( "x2", "" + (x+QTIMetrics.inchesToSvg( 0.5 )) );
//      line.setAttribute( "y2", "" + QTIMetrics.inchesToSvg( (i%5)==0?0.4:0.45 ) );
//      line.setAttribute( "stroke", "rgb(0,0,0)" );
//      line.setAttribute( "stroke-width", "" + QTIMetrics.inchesToSvg( 0.02 ) );
//      g.appendChild(line);
//      if ( (i%5)==0 && i>0 )
//      {
//        t = (org.w3c.dom.Element) docpreview.createElementNS(svgNS, "text");
//        t.setAttribute("text-anchor", "middle" );
//        t.setAttribute("x", "" + (x + QTIMetrics.inchesToSvg( 0.5 ) ) );
//        t.setAttribute("y", "" + QTIMetrics.inchesToSvg( 0.3 ) );
//        t.setAttribute("font-size", "" + QTIMetrics.inchesToSvg( 0.2 ) );
//        t.setTextContent( "" + (i/5) + "\"" );
//        g.appendChild(t);
//      }
//    }

    org.w3c.dom.Element rl = docpreview.createElementNS(svgNS, "rect");
    rl.setAttribute( "x", "" + QTIMetrics.inchesToSvg(  0.0 ) );
    rl.setAttribute( "y", "" + QTIMetrics.inchesToSvg(  0.5 ) );
    rl.setAttribute( "stroke", "rgb(0,0,0)" );
    rl.setAttribute( "stroke-width", "" + QTIMetrics.inchesToSvg( 0.02 ) );
    rl.setAttribute( "fill", "rgb(255,255,220)" );
    rl.setAttribute( "width", "" + QTIMetrics.inchesToSvg( 0.5 ) );
    rl.setAttribute( "height",  "" + metrics.getPropertySvgUnitsInt("page-height") );
    g.appendChild( rl );
    double y;
//    for ( y=0.0, i=0; y<metrics.getPropertySvgUnits("page-height"); y+=QTIMetrics.inchesToSvg(  0.2 ), i++ )
//    {
//      line = docpreview.createElementNS(svgNS, "line");
//      line.setAttribute( "x1", "" + QTIMetrics.inchesToSvg( 0.5 ) );
//      line.setAttribute( "y1", "" + (y+QTIMetrics.inchesToSvg( 0.5 )) );
//      line.setAttribute( "x2", "" + QTIMetrics.inchesToSvg( (i%5)==0?0.4:0.45 ) );
//      line.setAttribute( "y2", "" + (y+QTIMetrics.inchesToSvg( 0.5 )) );
//      line.setAttribute( "stroke", "rgb(0,0,0)" );
//      line.setAttribute( "stroke-width", "" + QTIMetrics.inchesToSvg( 0.02 ) );
//      g.appendChild(line);
//      if ( (i%5)==0 && i>0 )
//      {
//        t = (org.w3c.dom.Element) docpreview.createElementNS(svgNS, "text");
//        t.setAttribute("text-anchor", "end" );
//        t.setAttribute("x", "" + QTIMetrics.inchesToSvg( 0.4 ) );
//        t.setAttribute("y", "" + (y + QTIMetrics.inchesToSvg( 0.55 ) ) );
//        t.setAttribute("font-size", "" + QTIMetrics.inchesToSvg( 0.2 ) );
//        t.setTextContent( "" + (i/5) + "\"" );
//        g.appendChild(t);
//      }
//    }

    docpreview.normalizeDocument();
    return docpreview;
  }



  public org.w3c.dom.Document getSVGDocument()
  {
    return decorateItemForPreview( svgres.getDocument() );
    //return svgres.getDocument();
  }

  private class InteractionInsert
  {

    int id;
    QTIElement element;
    Component component = null;
    SVGIcon icon = null;

    InteractionInsert(int id, QTIElement element, Component component, SVGIcon icon)
    {
      this.id = id;
      this.element = element;
      this.component = component;
      this.icon = icon;
    }
  }

  private class RenderState
  {
    QTIElementItem item;
    StringBuffer html = new StringBuffer();
    boolean open_block = false;
    int next_id = 1001;
    
    // This is held separately from those in the inserts vector
    // because it needs to be rendered last. This is because it encodes
    // metrics from the pink squares.
    InteractionInsert qriconinsert = null;
    Vector<InteractionInsert> inserts = new Vector<InteractionInsert>();
  }
  
}
