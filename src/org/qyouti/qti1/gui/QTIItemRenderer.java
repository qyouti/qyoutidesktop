/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti.qti1.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import org.qyouti.svg.SVGUtils;
import org.qyouti.util.QyoutiUtils;
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
  TextPaneWrapper textPane;
  SvgConversionResult svgres;
  int qnumber;
  UserRenderPreferences prefs;
  QTIRenderOptions options;
  QuestionMetricsRecord mrec;



  static Hashtable<UserRenderPreferences,Hashtable<String,CacheEntry>> cache =
      new Hashtable<UserRenderPreferences,Hashtable<String,CacheEntry>>();
  

  static QTIMetrics metrics = null;


  private static CacheEntry getFromCache( String id, UserRenderPreferences prefs )
  {
    //System.out.println( "sets in cache " + cache.size() );
    Hashtable<String,CacheEntry> table = cache.get(prefs);
    if ( table == null )
      return null;
    //System.out.println( "items in table " + table.size() );
    return table.get(id);
  }

  private static void putIntoCache( String id, UserRenderPreferences prefs, CacheEntry entry )
  {
    Hashtable<String,CacheEntry> table = cache.get(prefs);
    if ( table == null )
    {
      table = new Hashtable<String,CacheEntry>();
      cache.put(prefs, table);
    }
    table.put(id, entry);
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
  public QTIItemRenderer(

      URI examfolderuri,
      QTIElementItem item,
      int qnumber,
      QTIRenderOptions options,
      UserRenderPreferences prefs
      )
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

    this.options = options;

    CacheEntry entry = getFromCache(item.getIdent(), this.prefs);

    if ( entry == null )
    {
      renderItem( item );
      putIntoCache( item.getIdent(), this.prefs, new CacheEntry( mrec, svgres ) );
    }
    else
    {
      this.svgres = entry.svgres;
      this.mrec = entry.qmr;
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
    //System.out.println("===============================================");
    //System.out.println(state.html);
    //System.out.println("===============================================");

    // Put the HTML into the Text Pane
    textPane = new TextPaneWrapper(state.html.toString());
    HTMLDocument htmldoc = textPane.getHtmlDoc();

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
          htmldoc.remove(offset, 1);
          htmldoc.insertString(offset, " ", s);
        } catch (BadLocationException ex)
        {
          Logger.getLogger(QTIItemRenderer.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
    }


    // Get the textPane to paint itself into an SVG document.
    // This will have blank rectangles whereever SVGIcon objects are.
    svgres = ComponentToSvg.convert(textPane,getMetrics().getPropertySvgUnitsInt("page-width"));
    Rectangle2D bounds = svgres.getBounds();
    org.w3c.dom.Element svgroot = svgres.getDocument().getDocumentElement();

    int qrheight;
    if ( options.getQTIRenderBooleanOption("question_metrics_qr") )
    {
        qrheight = getMetrics().getPropertySvgUnitsInt("qrcode-item-width");
        qrheight *= 1.5;
    }
    else
    {
        qrheight = getMetrics().getPropertySvgUnitsInt("qrcode-item-width-small");
        qrheight *= 2.4;
    }

    int effective_height = textPane.getSize().height;
    if ( effective_height < qrheight )
      effective_height = qrheight;
    svgroot.setAttribute(  "height", "" + effective_height );
    svgroot.removeAttribute("viewBox");

    // Where did the QRCode go?
    QRCodeIcon qricon = (QRCodeIcon)state.qriconinsert.icon;

    // 'Paint' all the icons except the qr code
    for ( i = 0; i < state.inserts.size(); i++)
    {
      insert = state.inserts.get(i);
      if (insert.icon != null && insert.icon != qricon )
      {
        insert.icon.paintSVG(svgres.getDocument());
      }
    }

    // Find out where all the pink icons ended up and codify
    Vector<Rectangle> boxes = new Vector<Rectangle>();
    Rectangle box, pinkbox;
    PinkIcon picon;
    for ( i = 0; i < state.inserts.size(); i++)
    {
      insert = state.inserts.get(i);
      if (insert.icon != null && insert.icon instanceof PinkIcon )
      {
        picon = (PinkIcon)insert.icon;
        pinkbox = picon.getPinkRectangle();
        box = new Rectangle(  (pinkbox.x - qricon.x - qricon.getPadding())/10,
                              (pinkbox.y - qricon.y - qricon.getPadding())/10,
                              pinkbox.width/10,
                              pinkbox.height/10 );
        boxes.add( box );
      }
    }
    mrec = new QuestionMetricsRecord( item.getIdent(), effective_height / 10.0, boxes );
    
    // do the qr code again last so the pink icon coordinates can be passed in.
    if ( options.getQTIRenderBooleanOption("question_metrics_qr"))
      qricon.update( mrec );
    qricon.paintSVG(svgres.getDocument());
  }

  private static SVGDocument renderSpecialPage( String name, QTIRenderOptions options )
  {
    return renderSpecialPage( name, options, null, null, null );
  }

  private static SVGDocument renderSpecialPage(
      String name,
      QTIRenderOptions options,
      String candidatename,
      String candidateid,
      String content )
  {
    int i;

    InputStream in = options.getClass().getClassLoader()
        .getResourceAsStream(name);
    BufferedReader reader = new BufferedReader( new InputStreamReader( in ) );
    StringBuffer buffer = new StringBuffer();
    String line;

    try
    {
      while ((line = reader.readLine()) != null)
      {
        buffer.append(line);
        buffer.append("\n");
      }
    } catch (IOException ex)
    {
      ex.printStackTrace();
    }
    finally
    {
      try
      {
        reader.close();
      } catch (IOException ex)
      {
      }
    }

//    System.out.println( "-------------------" );
//    System.out.println( buffer.toString()     );
//    System.out.println( "-------------------" );

    // Put the HTML into the Text Pane
    TextPaneWrapper textPane = new TextPaneWrapper(buffer.toString());
    //TextPaneWrapper textPane = new TextPaneWrapper("<p style=\"font-size: 500px;\">Rhubarb</p>");
    HTMLDocument htmldoc = textPane.getHtmlDoc();

    try
    {
      Element e = htmldoc.getElement( "candidatename" );
      if ( e != null && candidatename != null )
        htmldoc.insertAfterStart(e, candidatename);
      e = htmldoc.getElement( "candidateid" );
      if ( e != null && candidateid != null )
        htmldoc.insertAfterStart(e, candidateid);
      e = htmldoc.getElement( "content" );
      if ( e != null && content != null )
        htmldoc.insertAfterStart(e, content);

    } catch (BadLocationException ex)
    {
      Logger.getLogger(QTIItemRenderer.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IOException ex)
    {
      Logger.getLogger(QTIItemRenderer.class.getName()).log(Level.SEVERE, null, ex);
    }
    

    // Get the textPane to paint itself into an SVG document.
    // This will have blank rectangles whereever SVGIcon objects are.
    SvgConversionResult svgres = ComponentToSvg.convert(textPane,getMetrics().getPropertySvgUnitsInt("page-width"));
    Rectangle2D bounds = svgres.getBounds();
    org.w3c.dom.Element svgroot = svgres.getDocument().getDocumentElement();

    int effective_height = textPane.getSize().height;
    if ( effective_height < QTIMetrics.inchesToSvg( 1 ) )
      effective_height = (int) QTIMetrics.inchesToSvg( 1 );
    svgroot.setAttribute(  "height", "" + effective_height );
    svgroot.setAttribute(  "viewBox", "0 0 " + getMetrics().getPropertySvgUnitsInt("page-width") + " " + effective_height );


    return  svgres.getDocument();
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
      state.html.append( "<table border=\"0\" style=\"margin-bottom: " );
      state.html.append( getMetrics().inchesToSvg( 0.1 ) );
      state.html.append( ";\">" );
      state.html.append( "<tr>" );
      state.html.append( "<td width=\"" + getMetrics().getPropertySvgUnitsInt("calibration-topleft-x") + "\"></td>\n" );
      state.html.append( "<td width=\"" +
          ( getMetrics().getPropertySvgUnitsInt("item-margin-left")-
            getMetrics().getPropertySvgUnitsInt("calibration-topleft-x")      )
          + "\" valign=\"top\" style=\"bgcolor; green;\" >" );

      boolean m = options.getQTIRenderBooleanOption("question_metrics_qr");
      QRCodeIcon qricon = new QRCodeIcon(
              0,
              getMetrics().getPropertySvgUnitsInt(m?"qrcode-item-width":"qrcode-item-width-small"),
              state.item.getIdent(),
              getMetrics().getPropertySvgUnitsInt(m?"qrcode-item-width":"qrcode-item-width-small")
              );

      state.inserts.add(new InteractionInsert(state.next_id, e, null, qricon));
      // this is also recorded separately because it needs to be (re)processed last
      state.qriconinsert = new InteractionInsert(state.next_id, e, null, qricon);
      state.html.append("<span id=\"qti_insert_" + (state.next_id++) + "\">*</span>\n");

      state.html.append( "</td><td  width=\"" + getMetrics().getPropertySvgUnitsInt("item-width") + "\" style=\"bgcolor; green;\">" );

      if ( options.getQTIRenderBooleanOption("question_titles") )
      {
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
      }

      state.html.append("<div>");
//      state.open_block = true;

      renderSubElements( e, state );

//      if (state.open_block)
//      {
//        state.html.append("</div>\n");
//        state.open_block = false;
//      }
      state.html.append("\n</td>\n<td width=\"" + getMetrics().getPropertySvgUnitsInt("page-margin-right") + "\"> </td>\n</tr>\n</table>\n</body>\n<html>");
//      state.open_block = true;
      return;
    }

    if (e instanceof QTIElementFlow)
    {
      state.flow_depth++;
      renderSubElements( e, state );
      state.flow_depth--;
      return;
    }

    if (e instanceof QTIElementFlowmat)
    {
      state.html.append( "<div>" );
      renderSubElements( e, state );
      state.html.append( "</div>" );
      return;
    }

    if (e instanceof QTIElementFlowlabel)
    {
      state.html.append( "<div>" );
      renderSubElements( e, state );
      state.html.append( "</div>" );
      return;
    }


    if (e instanceof QTIMatmedia)
    {
//      if (!state.open_block)
//      {
//        state.html.append("<div>");
//        state.open_block = true;
//      }
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
          state.html.append(" &nbsp; ");
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
//          if (state.open_block)
//          {
//            state.html.append("</div>\n");
//            state.open_block = false;
//          }
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
//      if (state.open_block)
//      {
//        state.html.append("</div>\n");
//        state.open_block = false;
//      }

//      state.html.append("\n<table style=\"margin: ");
//      state.html.append(" " + (int) QTIMetrics.inchesToSvg(0.1) + "px 0px");
//      state.html.append(" " + (int) QTIMetrics.inchesToSvg(0.1) + "px ");
//      state.html.append(" 0px;\">");

      if ( state.flow_depth == 0 )
        state.html.append( "<div>" );

      state.in_choice = true;
      renderSubElements( e, state );
      state.in_choice = false;

      if ( state.flow_depth == 0 )
        state.html.append( "</div>" );

//      state.html.append("</table>\n");
      return;
    }

    if (e instanceof QTIElementRenderfib)
    {
      QTIElementRenderfib efib = (QTIElementRenderfib)e;
//      if (state.open_block)
//      {
//        state.html.append("</div>\n");
//        state.open_block = false;
//      }
 
      state.inserts.add(new InteractionInsert(state.next_id, e, null,
              new PinkIcon(
                            (int) QTIMetrics.inchesToSvg(0.15 * efib.getColumns() ),
                            (int) QTIMetrics.inchesToSvg(0.2 * efib.getRows() ),
                            (int) QTIMetrics.inchesToSvg(0.02),
                            (int) QTIMetrics.inchesToSvg(0.05))));
      state.html.append("<div style=\"padding: 50px 25px 50px 25px;\"><span id=\"qti_insert_" + (state.next_id++) + "\">*</span></div>\n");

      return;
    }

    if (e instanceof QTIElementResponselabel)
    {
      if ( state.in_fib )
        return;


      if ( state.flow_depth == 0 )
        state.html.append( "<div>" );

      state.inserts.add(new InteractionInsert(state.next_id, e, null,
              new PinkIcon(
              (int) QTIMetrics.inchesToSvg(0.27),
              (int) QTIMetrics.inchesToSvg(0.27),
              (int) QTIMetrics.inchesToSvg(0.02),
              (int) QTIMetrics.inchesToSvg(0.025))));

//      state.html.append("<tr>\n");
      // The span will always have one character in it - which will be
      // deleted and replaced with a component.
//      state.html.append("<td valign=\"top\"><div style=\"margin: 0px");
//      state.html.append(" " + (int) QTIMetrics.inchesToSvg(0.1) + "px ");
//      state.html.append(" " + (int) QTIMetrics.inchesToSvg(0.05) + "px ");
//      state.html.append(" 0px;\">");
      state.html.append("<span id=\"qti_insert_" + (state.next_id++) + "\">*</span> ");
//      state.html.append("</div></td>\n");
//      state.html.append("<td valign=\"top\"><div>");
//      state.open_block = true;

      renderSubElements( e, state );

//      if (state.open_block)
//      {
//        state.html.append("</div>\n");
//        state.open_block = false;
//      }
//      state.html.append("</td>\n</tr>\n");

      if ( state.flow_depth == 0 )
        state.html.append( "</div>" );
      return;
    }


    // not an interesting element but it might have interesting sub-elements.
    renderSubElements( e, state );

  }





  public static SVGDocument decorateItemForPreview( SVGDocument itemdoc , boolean rulers, QTIRenderOptions options )
  {
    Vector<SVGDocument> list = new Vector<SVGDocument>();
    list.add(itemdoc );
    return decorateItemForPreview( list , false, null, null, options );
  }

  
  


  public static Vector<SVGDocument> paginateItems( 
      String printid,
      URI examfolderuri,
      Vector<QTIElementItem> items,
      CandidateData candidate,
      QTIRenderOptions options,
      QuestionMetricsRecordSet metricrecordset,
      String preamble )
  {
      int i;
      Vector<Vector<SVGDocument>> pages = new Vector<Vector<SVGDocument>>();
      Vector<SVGDocument> page;
      SVGDocument svgdocs[]=null;

      page = new Vector<SVGDocument>();
      pages.add( page );

      double totalspace = getMetrics().getPropertySvgUnits("calibration-bottomright-y")
              -getMetrics().getPropertySvgUnits("calibration-topleft-y")
              -1.1*getMetrics().getPropertySvgUnits("qrcode-page-width");
      double spaceleft = totalspace;
      double itemheight;

      // how much does the qr code stick up above bottom margin?
      double qrpokeup = 0.0; getMetrics().getPropertySvgUnits("qrcode-page-encroachment");
      // how much space needed for question qr?
      double qrheight = getMetrics().getPropertySvgUnits("qrcode-full-width");

      if ( options.getQTIRenderBooleanOption("cover_sheet") )
      {
        SVGDocument coversvg = renderSpecialPage(
            "org/qyouti/qti1/gui/examcover.xhtml",
            options,
            candidate.name,
            candidate.id,
            (preamble!=null)?preamble:"" );
        page.add( coversvg );
        page = new Vector<SVGDocument>();
        pages.add( page );
        QyoutiUtils.dumpXMLFile( "/home/jon/Desktop/debug.svg", coversvg.getDocumentElement(), true );
      }

      svgdocs = new SVGDocument[items.size()];
      QTIItemRenderer renderer;
      for ( i=0; i<items.size(); i++ )
      {
        renderer = new QTIItemRenderer( examfolderuri, items.elementAt(i), i+1, 
            options, candidate.preferences );
        svgdocs[i] = renderer.getSVGDocument();
        itemheight = Integer.parseInt( svgdocs[i].getRootElement().getAttribute("height") );
        if ( itemheight > spaceleft || spaceleft < (qrpokeup + qrheight) )
        {
          page = new Vector<SVGDocument>();
          pages.add( page );
          spaceleft = totalspace;
        }
        page.add( svgdocs[i] );
        spaceleft -= itemheight;
        metricrecordset.addItem( candidate.preferences, renderer.mrec );
      }

      if ( (pages.size() & 1) == 1 && options.getQTIRenderBooleanOption("double_sided") )
      {
        SVGDocument coversvg = renderSpecialPage( "org/qyouti/qti1/gui/blank.xhtml", options );
        page.add( coversvg );
        page = new Vector<SVGDocument>();
        pages.add( page );
      }

      Vector<SVGDocument> paginated = new Vector<SVGDocument>();
      String qrout, footer;
      for ( i=0; i<pages.size(); i++ )
      {
        qrout =
            "v1/" +
            printid + "/" +
            (1 + metricrecordset.getPreferencesIndex( candidate.preferences )) + "/" +
            candidate.name + "/" +
            candidate.id + "/" +
            i + "/" +
            pages.elementAt(i).size();
        footer = "";
        if ( options.getQTIRenderBooleanOption( "name_in_footer" ) )
          footer += candidate.name + "   ";
        if ( options.getQTIRenderBooleanOption( "id_in_footer" ) )
          footer += candidate.id + "   ";
        footer += "Page " + (i+1) + " of " + pages.size();
        paginated.add( QTIItemRenderer.decorateItemForPreview( pages.elementAt(i) , false,  qrout, footer, options ) );
        //System.out.println( "Paginated: " + qrout );
      }


      return paginated;
  }

  public static SVGDocument decorateItemForPreview( Vector<SVGDocument> itemdocs , boolean rulers, String pageqr, String footer, QTIRenderOptions options )
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
    r.setAttribute( "fill", "rgb(190,190,220)" );
    r.setAttribute( "width",  "" + QTIMetrics.inchesToSvg( 1.5 ) );
    r.setAttribute( "height", "" + getMetrics().getPropertySvgUnitsInt("page-height") );
    decorationgroup.appendChild( r );

    // blue panel at bottom
    r = pdoc.createElementNS( svgNS, "rect");
    r.setAttribute( "x", "" + QTIMetrics.inchesToSvg(  decoroffsetx ) );
    r.setAttribute( "y", "" + 
        (  QTIMetrics.inchesToSvg(  decoroffsety )
           + getMetrics().getPropertySvgUnitsInt("page-height")
           - QTIMetrics.inchesToSvg( 1.3 )
        )
           );
    r.setAttribute( "stroke", "none" );
    r.setAttribute( "fill", "rgb(190,190,220)" );
    r.setAttribute( "width",  "" + getMetrics().getPropertySvgUnitsInt("page-width") );
    r.setAttribute( "height", "" + QTIMetrics.inchesToSvg( 1.3 ) );
    decorationgroup.appendChild( r );

    if ( pageqr != null )
    {
      int cw     = (int)(10.0*(getMetrics().getPropertyInches("calibration-bottomright-x") -
                               getMetrics().getPropertyInches("calibration-topleft-x")));
      int ch     = (int)(10.0*(getMetrics().getPropertyInches("calibration-bottomright-y") -
                               getMetrics().getPropertyInches("calibration-topleft-y")));
      // bottom left corner onto calibration point
      QRCodeIcon qricon = new QRCodeIcon(
              0,
              0,
              pageqr,
              getMetrics().getPropertySvgUnitsInt("qrcode-page-width")
              );
      qricon.x  = new Integer( getMetrics().getPropertySvgUnitsInt("calibration-topleft-x") );
      qricon.y  = new Integer( getMetrics().getPropertySvgUnitsInt("calibration-bottomright-y") );
      qricon.y -= new Integer( getMetrics().getPropertySvgUnitsInt("qrcode-page-width") );
      qricon.paintSVG(pdoc);

      // bottom left corner onto calibration point
      qricon = new QRCodeIcon(
              0,
              0,
              "qyouti/" + cw + "/" + ch,
              getMetrics().getPropertySvgUnitsInt("qrcode-calibration-width")
              );
      qricon.x  = new Integer( getMetrics().getPropertySvgUnitsInt("calibration-bottomright-x") );
      qricon.y  = new Integer( getMetrics().getPropertySvgUnitsInt("calibration-bottomright-y") );
      qricon.y -= new Integer( getMetrics().getPropertySvgUnitsInt("qrcode-calibration-width") );
      qricon.paintSVG(pdoc);

    }

    if ( options.getQTIRenderOption("header") != null )
    {
      org.w3c.dom.Element theader;
      theader = (org.w3c.dom.Element) pdoc.createElementNS(svgNS, "text");
      theader.setAttribute("text-anchor", getMetrics().getProperty("header-anchor") );
      theader.setAttribute("x", "" + (getMetrics().getPropertySvgUnitsInt("header-anchor-x")) );
      theader.setAttribute("y", "" + (getMetrics().getPropertySvgUnitsInt("header-anchor-y")) );
      theader.setAttribute("font-size", "" + QTIMetrics.inchesToSvg( 0.15 ) );
      theader.setTextContent( options.getQTIRenderOption("header") );
      decorationgroup.appendChild(theader);
    }

    if ( footer != null )
    {
      org.w3c.dom.Element tfooter;
      tfooter = (org.w3c.dom.Element) pdoc.createElementNS(svgNS, "text");
      tfooter.setAttribute("text-anchor", "middle" );
      tfooter.setAttribute("x", "" + (getMetrics().getPropertySvgUnitsInt("page-width")/2) );
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
    double vertical_offset = QTIMetrics.inchesToSvg( decoroffsety );
    vertical_offset += getMetrics().getPropertySvgUnits( "calibration-topleft-y" );
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
      SVGUtils.insertDocumentContents(pdoc, itemg, itemdoc);

      itemsvg = itemdoc.getRootElement();
      itemheight = Double.parseDouble( itemsvg.getAttribute("height") );
      vertical_offset+=itemheight;
    }
    //pdoc.normalizeDocument();
    return pdoc;
  }



  public SVGDocument getPreviewSVGDocument( QTIRenderOptions options )
  {
    return decorateItemForPreview( (SVGDocument) svgres.getDocument().cloneNode(true), true, options);
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
//    boolean open_block = false;
    int next_id = 1001;

    boolean in_fib=false;
    boolean in_choice=false;
    int flow_depth = 0;
    
    // This is held separately from those in the inserts vector
    // because it needs to be rendered last. This is because it encodes
    // metrics from the pink squares.
    InteractionInsert qriconinsert = null;
    Vector<InteractionInsert> inserts = new Vector<InteractionInsert>();
  }

  class CacheEntry
  {
    QuestionMetricsRecord qmr;
    SvgConversionResult svgres;
    CacheEntry( QuestionMetricsRecord qmr, SvgConversionResult svgres )
    {
      this.qmr = qmr;
      this.svgres = svgres;
    }
  }
}
