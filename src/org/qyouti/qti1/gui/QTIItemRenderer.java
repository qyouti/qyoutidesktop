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
import java.util.Hashtable;
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
import org.qyouti.data.CandidateData;
import org.qyouti.dialog.TextPaneWrapper;
import org.qyouti.print.ComponentToSvg;
import org.qyouti.print.SvgConversionResult;
import org.qyouti.qrcode.QRCodec;
import org.qyouti.qti1.*;
import org.qyouti.qti1.element.*;
import org.qyouti.qti1.ext.webct.QTIExtensionWebctMaterialwebeq;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGSVGElement;

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
  int qnumber;
  UserRenderPreferences prefs;

  static Hashtable<UserRenderPreferences,Hashtable<String,SvgConversionResult>> cache =
      new Hashtable<UserRenderPreferences,Hashtable<String,SvgConversionResult>>();
  

  static QTIMetrics metrics = null;


  private static SvgConversionResult getFromCache( String id, UserRenderPreferences prefs )
  {
    System.out.println( "sets in cache " + cache.size() );
    Hashtable<String,SvgConversionResult> table = cache.get(prefs);
    if ( table == null )
      return null;
    System.out.println( "items in table " + table.size() );
    return table.get(id);
  }

  private static void putIntoCache( String id, UserRenderPreferences prefs, SvgConversionResult svgr )
  {
    Hashtable<String,SvgConversionResult> table = cache.get(prefs);
    if ( table == null )
    {
      table = new Hashtable<String,SvgConversionResult>();
      cache.put(prefs, table);
    }
    table.put(id, svgr);
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
  public QTIItemRenderer(URI examfolderuri, QTIElementItem item, int qnumber, UserRenderPreferences prefs )
  {
    int i;
    this.examfolderuri = examfolderuri;
    this.qnumber = qnumber;

    if ( prefs == null )
    {
      this.prefs = new UserRenderPreferences();
      this.prefs.serif = false;
      this.prefs.fontsize = getMetrics().getPropertyInches("fontsize");
    }
    else
      this.prefs = prefs;

    svgres = getFromCache(item.getIdent(), this.prefs);

    if ( svgres == null )
    {
      renderItem( item );
      putIntoCache( item.getIdent(), this.prefs, svgres );
    }
  }

  private void renderItem( QTIElementItem item )
  {
    int i;

    QTIElementPresentation presentation = item.getPresentation();
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
    //scrollpane.getViewport().setSize( getMetrics().getPropertySvgUnitsInt("page-width"), 50);
    scrollpane.getViewport().setSize( getMetrics().getPropertySvgUnitsInt("page-width"), textPane.getSize().height );
    scrollpane.getViewport().doLayout();

    //System.out.println( "Edit component location: " + field.getX() + " " + field.getY() );
    System.out.println("textpane size: " + textPane.getSize());

    // Get the textPane to paint itself into an SVG document.
    // This will have blank rectangles whereever SVGIcon objects are.
    svgres = ComponentToSvg.convert(textPane);
    Rectangle2D bounds = svgres.getBounds();
    org.w3c.dom.Element svgroot = svgres.getDocument().getDocumentElement();
    
    svgroot.setAttribute(  "height", "" + textPane.getSize().height );
    svgroot.setAttribute(  "viewBox", "0 0 " + getMetrics().getPropertySvgUnitsInt("page-width") + " " + textPane.getSize().height );

    // Where did the QRCode go?
    QRCodeIcon qricon = (QRCodeIcon)state.qriconinsert.icon;

    // 'Paint' all the icons
    for ( i = 0; i < state.inserts.size(); i++)
    {
      insert = state.inserts.get(i);
      if (insert.icon != null && insert.icon != qricon )
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
        coords.append( (picon.x - qricon.x - qricon.getPadding())/10 );
        coords.append( " " );
        coords.append( (picon.y - qricon.y - qricon.getPadding())/10 );
        coords.append( " " );
        coords.append( picon.getIconWidth()/10 );
        coords.append( " " );
        coords.append( picon.getIconHeight()/10 );
        coords.append( " " );
      }
    }
    
    // do the qr code again last so the pink icon coordinates can be passed in.
    qricon.update( textPane.getSize().height / 10.0, coords.toString() );
    qricon.paintSVG(svgres.getDocument());
  }



  private void renderSubElements( QTIElement e, RenderState state )
  {
    Vector<QTIElement> list = e.findElements(QTIElement.class, false);
    for (int i = 0; i < list.size(); i++)
    {
      renderElement(list.elementAt(i), state);
    }
  }


  private void renderElement(QTIElement e, RenderState state)
  {
    if ( e == null )
    {
      UnrecognisedQTIElement unrec = (UnrecognisedQTIElement) e;
      state.html.append("[[Unrec element type: " + unrec.debug() + "]]");
      return;
    }
    if (e instanceof UnrecognisedQTIElement)
    {
      UnrecognisedQTIElement unrec = (UnrecognisedQTIElement) e;
      state.html.append("[[Unrec element type: " + unrec.debug() + "]]");
      return;
    }

    if (e instanceof QTIElementPresentation)
    {
      state.html.append("<html>\n<body style=\"font-size: " );
      state.html.append(getMetrics().inchesToSvg( prefs.getFontsize() ));
      state.html.append("px; font-family: " );
      state.html.append( getMetrics().getProperty( prefs.isSerif()?"fontfamily-serif":"fontfamily" ) );
      state.html.append( ";\">\n");
      state.html.append( "<table border=\"0\">" );
      state.html.append( "<tr>" );
      state.html.append( "<td width=\"" + getMetrics().getPropertySvgUnitsInt("page-margin-left") + "\"></td>\n" );
      state.html.append( "<td width=\"" + getMetrics().getPropertySvgUnitsInt("qrcode-full-width") + "\" valign=\"top\" style=\"bgcolor; green;\" >" );

      QRCodeIcon qricon = new QRCodeIcon(
              getMetrics().getPropertySvgUnitsInt("item-xoffset")-
              getMetrics().getPropertySvgUnitsInt("qrcode-xoffset"),
              getMetrics().getPropertySvgUnitsInt("qrcode-width"),
              state.item.getIdent(),
              getMetrics().getPropertySvgUnitsInt("qrcode-width")
              );

      state.inserts.add(new InteractionInsert(state.next_id, e, null, qricon));
      // this is also recorded separately because it needs to be (re)processed last
      state.qriconinsert = new InteractionInsert(state.next_id, e, null, qricon);
      state.html.append("<span id=\"qti_insert_" + (state.next_id++) + "\">*</span>\n");

      state.html.append( "</td><td  width=\"" + getMetrics().getPropertySvgUnitsInt("item-width") + "\" style=\"bgcolor; green;\">" );
      state.html.append("<div>");
      TitleIcon titicon = new TitleIcon(
              qnumber,
              getMetrics().getPropertySvgUnitsInt("item-width"),
              getMetrics().getPropertySvgUnitsInt("item-title-height"),
              getMetrics().getPropertySvgUnitsInt("item-title-font-height")
              );
      state.inserts.add(new InteractionInsert(state.next_id, e, null, titicon));
      state.html.append("<span id=\"qti_insert_" + (state.next_id++) + "\">*</span>\n");
      state.html.append("</div>");
      state.html.append("<div>");
      state.open_block = true;

      renderSubElements( e, state );

      if (state.open_block)
      {
        state.html.append("</div>\n");
        state.open_block = false;
      }
      state.html.append("\n</td>\n<td width=\"" + getMetrics().getPropertySvgUnitsInt("page-margin-right") + "\"> </td>\n</tr>\n</table>\n</body>\n<html>");
      state.open_block = true;
      return;
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
      return;
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

      state.in_choice = true;
      renderSubElements( e, state );
      state.in_choice = false;

      state.html.append("</table>\n");
      return;
    }

    if (e instanceof QTIElementRenderfib)
    {
      QTIElementRenderfib efib = (QTIElementRenderfib)e;
      if (state.open_block)
      {
        state.html.append("</div>\n");
        state.open_block = false;
      }
 
      state.inserts.add(new InteractionInsert(state.next_id, e, null,
              new PinkIcon(
                            (int) QTIMetrics.inchesToSvg(0.15 * efib.getColumns() ),
                            (int) QTIMetrics.inchesToSvg(0.2 * efib.getRows() ),
                            (int) QTIMetrics.inchesToSvg(0.02))));
      state.html.append("<div style=\"padding: 50px 25px 50px 25px;\"><span id=\"qti_insert_" + (state.next_id++) + "\">*</span></div>\n");

      return;
    }

    if (e instanceof QTIElementResponselabel)
    {
      if ( state.in_fib )
        return;
      
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

      renderSubElements( e, state );

      if (state.open_block)
      {
        state.html.append("</div>\n");
        state.open_block = false;
      }
      state.html.append("</td>\n</tr>\n");
      return;
    }


    // not an interesting element but it might have interesting sub-elements.
    renderSubElements( e, state );

  }





  public static SVGDocument decorateItemForPreview( SVGDocument itemdoc , boolean rulers )
  {
    Vector<SVGDocument> list = new Vector<SVGDocument>();
    list.add(itemdoc );
    return decorateItemForPreview( list , false, null, null );
  }

  public static Vector<SVGDocument> paginateItems( URI examfolderuri, Vector<QTIElementItem> items, CandidateData candidate  )
  {
      int i;
      Vector<Vector<SVGDocument>> pages = new Vector<Vector<SVGDocument>>();
      Vector<SVGDocument> page;
      SVGDocument svgdocs[]=null;

      page = new Vector<SVGDocument>();
      pages.add( page );

      double totalspace = getMetrics().getPropertySvgUnits("page-height")
              -getMetrics().getPropertySvgUnits("page-margin-top")
              -getMetrics().getPropertySvgUnits("page-margin-bottom");
      double spaceleft = totalspace;
      double itemheight;

      svgdocs = new SVGDocument[items.size()];
      QTIItemRenderer renderer;
      for ( i=0; i<items.size(); i++ )
      {
        renderer = new QTIItemRenderer( examfolderuri, items.elementAt(i), i+1, candidate.preferences );
        svgdocs[i] = renderer.getSVGDocument();
        itemheight = Integer.parseInt( svgdocs[i].getRootElement().getAttribute("height") );
        if ( itemheight > spaceleft )
        {
          page = new Vector<SVGDocument>();
          pages.add( page );
          spaceleft = totalspace;
        }
        page.add( svgdocs[i] );
        spaceleft -= itemheight;
      }

      Vector<SVGDocument> paginated = new Vector<SVGDocument>();
      String qrout, footer;
      for ( i=0; i<pages.size(); i++ )
      {
        qrout = candidate.name + "/" + candidate.id + "/" + i + "/" + pages.elementAt(i).size();
        footer = candidate.name + "   " + candidate.id + "   Page " + (i+1) + " of " + pages.size();
        paginated.add( QTIItemRenderer.decorateItemForPreview( pages.elementAt(i) , false,  qrout, footer ) );
        System.out.println( "Paginated: " + qrout );
      }

      return paginated;
  }

  public static SVGDocument decorateItemForPreview( Vector<SVGDocument> itemdocs , boolean rulers, String pageqr, String footer )
  {
    int i;
    String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
    DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
    SVGDocument pdoc = (SVGDocument) impl.createDocument(svgNS, "svg", null);
    double decoroffsetx = rulers?0.5:0.0, decoroffsety = rulers?0.5:0.0;

    double widthinches = getMetrics().getPropertyInches("page-width") + decoroffsetx;
    double heightinches = getMetrics().getPropertyInches("page-height") + decoroffsety;

    SVGSVGElement svg = pdoc.getRootElement();
    //        (org.w3c.dom.Element) docpreview.removeChild(docpreview.getDocumentElement());
    //org.w3c.dom.Element svg = docpreview.createElement( "svg" );
    svg.setAttribute("viewBox", "" +
            QTIMetrics.inchesToSvg(  0.0 )         + " " +
            QTIMetrics.inchesToSvg(  0.0 )         + " " +
            QTIMetrics.inchesToSvg(  widthinches ) + " " +
            QTIMetrics.inchesToSvg(  heightinches )
            );
    svg.setAttribute("width",  ""+widthinches+"in" );
    svg.setAttribute("height", ""+heightinches+"in" );


//    while ( svg.getChildNodes().getLength() != 0 )
//    {
//      itemg.appendChild( svg.getFirstChild().cloneNode(true) );
//      svg.removeChild( svg.getFirstChild() );
//    }

    org.w3c.dom.Element decorationgroup = pdoc.createElementNS( svgNS, "g");
    svg.appendChild(decorationgroup);

    // white page background
    org.w3c.dom.Element r = pdoc.createElementNS( svgNS, "rect");
    r.setAttribute( "x", "" + QTIMetrics.inchesToSvg(  decoroffsetx ) );
    r.setAttribute( "y", "" + QTIMetrics.inchesToSvg(  decoroffsety ) );
    r.setAttribute( "stroke", "rgb(0,0,0)" );
    r.setAttribute( "stroke-width", "" + QTIMetrics.inchesToSvg( 0.02 ) );
    r.setAttribute( "fill", "rgb(255,255,255)" );
    r.setAttribute( "width",  "" + getMetrics().getPropertySvgUnitsInt("page-width") );
    r.setAttribute( "height", "" + getMetrics().getPropertySvgUnitsInt("page-height") );
    decorationgroup.appendChild( r );

    // blue panel on left
    r = pdoc.createElementNS( svgNS, "rect");
    r.setAttribute( "x", "" + QTIMetrics.inchesToSvg(  decoroffsetx ) );
    r.setAttribute( "y", "" + QTIMetrics.inchesToSvg(  decoroffsety ) );
    r.setAttribute( "stroke", "none" );
    r.setAttribute( "fill", "rgb(180,180,200)" );
    r.setAttribute( "width",  "" + QTIMetrics.inchesToSvg( 1.5 ) );
    r.setAttribute( "height", "" + getMetrics().getPropertySvgUnitsInt("page-height") );
    decorationgroup.appendChild( r );

    if ( pageqr != null )
    {
      QRCodeIcon qricon = new QRCodeIcon(
              getMetrics().getPropertySvgUnitsInt("item-xoffset")-
              getMetrics().getPropertySvgUnitsInt("qrcode-xoffset"),
              getMetrics().getPropertySvgUnitsInt("qrcode-width"),
              pageqr,
              getMetrics().getPropertySvgUnitsInt("qrcode-width")
              );
      qricon.x = new Integer( getMetrics().getPropertySvgUnitsInt("qrcode-page-x") );
      qricon.y = new Integer( getMetrics().getPropertySvgUnitsInt("qrcode-page-y") );
      qricon.paintSVG(pdoc);
    }

    if ( footer != null )
    {
      org.w3c.dom.Element tfooter;
      tfooter = (org.w3c.dom.Element) pdoc.createElementNS(svgNS, "text");
      tfooter.setAttribute("text-anchor", "end" );
      tfooter.setAttribute("x", "" + (getMetrics().getPropertySvgUnitsInt("page-width") -getMetrics().getPropertySvgUnitsInt("page-margin-right")) );
      tfooter.setAttribute("y", "" + (getMetrics().getPropertySvgUnitsInt("page-height")-(getMetrics().getPropertySvgUnitsInt("page-margin-bottom")/2)) );
      tfooter.setAttribute("font-size", "" + QTIMetrics.inchesToSvg( 0.15 ) );
      tfooter.setTextContent( footer );
      decorationgroup.appendChild(tfooter);
    }

    if ( rulers )
    {
      // yellow ruler at top
      org.w3c.dom.Element rt = pdoc.createElementNS( svgNS, "rect");
      rt.setAttribute( "x", "" + QTIMetrics.inchesToSvg( decoroffsetx ) );
      rt.setAttribute( "y", "" + QTIMetrics.inchesToSvg( 0.0 ) );
      rt.setAttribute( "stroke", "rgb(0,0,0)" );
      rt.setAttribute( "stroke-width", "" + QTIMetrics.inchesToSvg( 0.02 ) );
      rt.setAttribute( "fill", "rgb(255,255,200)" );
      rt.setAttribute( "width",  "" + getMetrics().getPropertySvgUnitsInt("page-width") );
      rt.setAttribute( "height", "" + QTIMetrics.inchesToSvg(  decoroffsety ) );
      decorationgroup.appendChild( rt );


      // ticks and labels on top ruler
      org.w3c.dom.Element line;
      org.w3c.dom.Element t;
      double x;
      for ( x=0.0, i=0; x<getMetrics().getPropertySvgUnits("page-width"); x+=QTIMetrics.inchesToSvg(  0.2 ), i++ )
      {
        line = pdoc.createElementNS(svgNS, "line");
        line.setAttribute( "x1", "" + (x+QTIMetrics.inchesToSvg( 0.5 )) );
        line.setAttribute( "y1", "" + QTIMetrics.inchesToSvg( 0.5 ) );
        line.setAttribute( "x2", "" + (x+QTIMetrics.inchesToSvg( 0.5 )) );
        line.setAttribute( "y2", "" + QTIMetrics.inchesToSvg( (i%5)==0?0.4:0.45 ) );
        line.setAttribute( "stroke", "rgb(0,0,0)" );
        line.setAttribute( "stroke-width", "" + QTIMetrics.inchesToSvg( 0.02 ) );
        decorationgroup.appendChild(line);
        if ( (i%5)==0 && i>0 )
        {
          t = (org.w3c.dom.Element) pdoc.createElementNS(svgNS, "text");
          t.setAttribute("text-anchor", "middle" );
          t.setAttribute("x", "" + (x + QTIMetrics.inchesToSvg( 0.5 ) ) );
          t.setAttribute("y", "" + QTIMetrics.inchesToSvg( 0.3 ) );
          t.setAttribute("font-size", "" + QTIMetrics.inchesToSvg( 0.2 ) );
          t.setTextContent( "" + (i/5) + "\"" );
          decorationgroup.appendChild(t);
        }
      }

      // yellow ruler on left
      org.w3c.dom.Element rl = pdoc.createElementNS( svgNS, "rect");
      rl.setAttribute( "x", "" + QTIMetrics.inchesToSvg(  0.0 ) );
      rl.setAttribute( "y", "" + QTIMetrics.inchesToSvg(  0.5 ) );
      rl.setAttribute( "stroke", "rgb(0,0,0)" );
      rl.setAttribute( "stroke-width", "" + QTIMetrics.inchesToSvg( 0.02 ) );
      rl.setAttribute( "fill", "rgb(255,255,220)" );
      rl.setAttribute( "width", "" + QTIMetrics.inchesToSvg( 0.5 ) );
      rl.setAttribute( "height",  "" + getMetrics().getPropertySvgUnitsInt("page-height") );
      decorationgroup.appendChild( rl );
      double y;
      for ( y=0.0, i=0; y<getMetrics().getPropertySvgUnits("page-height"); y+=QTIMetrics.inchesToSvg(  0.2 ), i++ )
      {
        line = pdoc.createElementNS(svgNS, "line");
        line.setAttribute( "x1", "" + QTIMetrics.inchesToSvg( 0.5 ) );
        line.setAttribute( "y1", "" + (y+QTIMetrics.inchesToSvg( 0.5 )) );
        line.setAttribute( "x2", "" + QTIMetrics.inchesToSvg( (i%5)==0?0.4:0.45 ) );
        line.setAttribute( "y2", "" + (y+QTIMetrics.inchesToSvg( 0.5 )) );
        line.setAttribute( "stroke", "rgb(0,0,0)" );
        line.setAttribute( "stroke-width", "" + QTIMetrics.inchesToSvg( 0.02 ) );
        decorationgroup.appendChild(line);
        if ( (i%5)==0 && i>0 )
        {
          t = (org.w3c.dom.Element) pdoc.createElementNS(svgNS, "text");
          t.setAttribute("text-anchor", "end" );
          t.setAttribute("x", "" + QTIMetrics.inchesToSvg( 0.4 ) );
          t.setAttribute("y", "" + (y + QTIMetrics.inchesToSvg( 0.55 ) ) );
          t.setAttribute("font-size", "" + QTIMetrics.inchesToSvg( 0.2 ) );
          t.setTextContent( "" + (i/5) + "\"" );
          decorationgroup.appendChild(t);
        }
      }
    }

    // plonk given svg onto decorations
    org.w3c.dom.Element itemg;
    SVGDocument itemdoc;
    SVGSVGElement itemsvg;
    double vertical_offset = QTIMetrics.inchesToSvg( decoroffsety + 0.5 );
    double itemheight;
    for ( i=0; i<itemdocs.size(); i++ )
    {
      itemdoc = itemdocs.elementAt(i);
      itemg = pdoc.createElementNS( svgNS, "g" );
      itemg.setAttribute("transform",
          "translate( " +
          (int)QTIMetrics.inchesToSvg( decoroffsetx ) + ", "+
          (int)vertical_offset + ")");
      svg.appendChild(itemg);
      itemsvg = (SVGSVGElement) pdoc.importNode( itemdoc.getDocumentElement(), true );
      itemg.appendChild(itemsvg);
      itemheight = Double.parseDouble( itemsvg.getAttribute("height") );
      vertical_offset+=itemheight;
    }
    pdoc.normalizeDocument();
    return pdoc;
  }



  public SVGDocument getPreviewSVGDocument()
  {
    return decorateItemForPreview( (SVGDocument) svgres.getDocument().cloneNode(true), true);
  }

  public SVGDocument getSVGDocument()
  {
    return svgres.getDocument();
  }


  public static QTIMetrics getMetrics()
  {
    if ( metrics == null )
      metrics = new QTIMetrics();
    return metrics;
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

    boolean in_fib=false;
    boolean in_choice=false;
    
    // This is held separately from those in the inserts vector
    // because it needs to be rendered last. This is because it encodes
    // metrics from the pink squares.
    InteractionInsert qriconinsert = null;
    Vector<InteractionInsert> inserts = new Vector<InteractionInsert>();
  }
  
}
