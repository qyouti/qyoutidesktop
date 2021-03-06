/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti.qti1.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.html.*;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.dom.*;
import org.apache.batik.util.*;
import org.bullseye.*;
import org.qyouti.data.CandidateData;
import org.qyouti.dialog.TextPaneWrapper;
import org.qyouti.print.ComponentToSvg;
import org.qyouti.print.SvgConversionResult;
import org.qyouti.barcode.ZXingCodec;
import org.qyouti.data.*;
import org.qyouti.dialog.*;
import org.qyouti.fonts.*;
import org.qyouti.print.*;
import org.qyouti.qti1.*;
import org.qyouti.qti1.element.*;
import org.qyouti.qti1.ext.qyouti.QTIExtensionRendersketcharea;
import org.qyouti.qti1.ext.webct.QTIExtensionWebctMaterialwebeq;
import org.qyouti.svg.SVGUtils;
import org.qyouti.util.QyoutiUtils;
import org.w3c.dom.*;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGSVGElement;
import org.w3c.dom.traversal.*;

/**
 *
 * @author jon
 */
public class QTIItemRenderer
//        extends QTIComponent
{
  QTIElementItem item;
  URI examfolderuri;
  JPanel comp;
  static final TextPaneWrapper textPane = new TextPaneWrapper();
  GenericDocument document;
  SvgConversionResult svgres;
  int qnumber;
  UserRenderPreferences prefs;
  QTIRenderOptions options;
  QuestionMetricsRecord mrec;
  QyoutiFontManager fontmanager;

  int columnoverride = 0;
  String analysishtml = "";
  
  int type = PrintThread.TYPE_PAPERS;
  

  QTIMetrics metrics = null;



//  static Hashtable<UserRenderPreferences,Hashtable<String,CacheEntry>> cache =
//      new Hashtable<UserRenderPreferences,Hashtable<String,CacheEntry>>();
  

//  private static CacheEntry getFromCache( String id, UserRenderPreferences prefs )
//  {
//    //System.out.println( "sets in cache " + cache.size() );
//    Hashtable<String,CacheEntry> table = cache.get(prefs);
//    if ( table == null )
//      return null;
//    //System.out.println( "items in table " + table.size() );
//    return table.get(id);
//  }
//
//  private static void putIntoCache( String id, UserRenderPreferences prefs, CacheEntry entry )
//  {
//    Hashtable<String,CacheEntry> table = cache.get(prefs);
//    if ( table == null )
//    {
//      table = new Hashtable<String,CacheEntry>();
//      cache.put(prefs, table);
//    }
//    table.put(id, entry);
//  }

  /**
   * An HTML version of the item is built up from elements in the presentation
   * section.  Empty <span id="bla di bla"></span> elements are used as
   * place holders for stuff that must be insert later as components.
   * These components are indexed against the IDs.  Once the HTML is in the
   * document model the positions of the placeholders are found and the <span>
   * elements replaced with the corresponding components.
   * @param type
   * @param examfolderuri
   * @param item
   * @param qnumber
   * @param options
   * @param prefs
   */
  public QTIItemRenderer(
      QyoutiFontManager fontmanager,
      int type,
      URI examfolderuri,
      QTIRenderOptions options
      )
  {
    this.item = null;
    this.fontmanager = fontmanager;
    this.type = type;
    this.examfolderuri = examfolderuri;
    this.qnumber = 1;
    this.prefs = new UserRenderPreferences();
    this.options = options;
  }

  public void setItem( QTIElementItem item, int qnumber, UserRenderPreferences prefs )
  {
    this.item = item;
    this.qnumber = qnumber;
    if ( prefs == null )
      this.prefs = new UserRenderPreferences();
    else
      this.prefs = prefs;    
  }
  
  public String getAnalysisHTML()
  {
    return analysishtml;
  }

  public void setAnalysisHTML( String analysishtml )
  {
    this.analysishtml = analysishtml;
  }

  public int getColumnOverride()
  {
    return columnoverride;
  }

  public void setColumnOverride( int c )
  {
    this.columnoverride = c;
  }

  
  
  public void renderItem()
  {
    int i;
    //System.out.println( "Render item " + item.getIdent() );

    QTIElementPresentation presentation = item.getPresentation();
    // Compose the HTML
    RenderState state = new RenderState();
    state.item = item;
    state.ignore_flow = options.getQTIRenderBooleanOption("ignore_flow");
    //state.break_response_labels = options.getQTIRenderBooleanOption("break_response_labels");
    renderElement(presentation, state);
    System.out.println("===============================================");
    System.out.println(state.html);
    System.out.println("===============================================");

    // Put the HTML into the Text Pane
    //TextPaneWrapper textPane = new TextPaneWrapper();
    textPane.setSize( 0, 0 );
    textPane.setQyoutiStyleSheet( fontmanager.getQyoutiStyleSheet( prefs==null?false:prefs.isSerif() ) );
    textPane.setText( state.html.toString() );
    
    // Force update to view before analysis
    textPane.getPreferredSize();
    MissingGlyphReport mgr = fontmanager.analyzeDocumentView( textPane.getUI().getRootView( textPane ) );
    
    HTMLDocument htmldoc = textPane.getHtmlDoc();
    //fontmanager.analyzeDocument( htmldoc );
    
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
        s.addAttribute( CSS.Attribute.VERTICAL_ALIGN, "middle" );
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

//  couldn't get this bit to work so revert to outputting
//  an HTML table.

    // Get the textPane to paint itself into an SVG document.
    // This will have blank rectangles whereever SVGIcon objects are.
    double width = getMetrics().getPropertyInches( "page-width" );
    width -= getMetrics().getPropertyInches( "item-area-inset-left" );
    width -= getMetrics().getPropertyInches( "item-area-inset-right" );
    int columns = item.getPresentation().getColumns();
    if ( columnoverride > 0 ) columns = columnoverride;
    width -= (double)(columns-1) * getMetrics().getPropertyInches( "item-area-column-spacing" );
    width = width / (double)columns;

    // This call may switch into the Event Dispatcher Thread.
    svgres =  textPane.getSVG( (int)QTIMetrics.inchesToSvg(width) );
    svgres.addMissingGlyphReport( mgr );
    //fontmanager.analyzeDocumentView( textPane.getUI().getRootView( textPane ) );
    //fontmanager.analyzeDocument( htmldoc );
    
    org.w3c.dom.Element svgroot = svgres.getDocument().getDocumentElement();
    NodeList nl = svgroot.getElementsByTagName( "defs" );
    if ( nl.getLength() > 0 )
    {
      // add stripy pattern to defs
      org.w3c.dom.Element defs = (org.w3c.dom.Element)nl.item( 0 );
      String svgNS = SVGConstants.SVG_NAMESPACE_URI;
      org.w3c.dom.Element p = svgres.getDocument().createElementNS( svgNS, "pattern" );
      org.w3c.dom.Element l = svgres.getDocument().createElementNS( svgNS, "line" );
      defs.appendChild( p );
      p.appendChild( l );

      p.setAttribute( "id", "stripes" );
      p.setAttribute( "patternUnits", "userSpaceOnUse" );
      p.setAttribute( "x",       "0" );
      p.setAttribute( "y",       "0" );
      p.setAttribute( "width",   "24" );
      p.setAttribute( "height",  "24" );
      p.setAttribute( "viewBox", "0 0 24 24" );

      l.setAttribute( "x1", "15" );
      l.setAttribute( "y1", "-1" );
      l.setAttribute( "x2", "15" );
      l.setAttribute( "y2", "25" );
      l.setAttribute( "stroke", "#c0c0c0" );
      l.setAttribute( "stroke-width", "6" );

    }
    

    int effective_height = textPane.getSize().height;

    svgroot.setAttribute(  "height", "" + effective_height );
    svgroot.removeAttribute("viewBox");


    // 'Paint' all the icons
    for ( i = 0; i < state.inserts.size(); i++)
    {
      insert = state.inserts.get(i);
      insert.icon.paintSVG(svgres.getDocument());
    }

    // Find out where all the pink icons ended up and codify
    Vector<QuestionMetricBox> boxes = new Vector<QuestionMetricBox>();
    Rectangle pinkbox;
    QuestionMetricBox box;
    UserInputIcon picon;
    for ( i = 0; i < state.inserts.size(); i++)
    {
      insert = state.inserts.get(i);
      if ( insert.icon != null && 
           insert.icon instanceof UserInputIcon &&
           insert.icon.x != null && 
           insert.icon.y != null    )
      {
        picon = (UserInputIcon)insert.icon;
        pinkbox = picon.getPinkRectangle();
        box = new QuestionMetricBox(
                              (pinkbox.x)/10,
                              (pinkbox.y)/10,
                              pinkbox.width/10,
                              pinkbox.height/10,
                              picon.getType(),
                              picon.getIdent(),
                              0
                              );
        boxes.add( box );
      }
    }
    mrec = new QuestionMetricsRecord( boxes );
        
    // Clear the text pane for reuse
    textPane.setText( "" );
  
    try
    {
      QyoutiUtils.dumpXMLFile( File.createTempFile( "/home/jon/temp/svg/qti", ".svg" ).getPath(), svgres.getDocument().getDocumentElement(), true );
    }
    catch ( IOException ex )
    {
      Logger.getLogger( QTIItemRenderer.class.getName() ).
              log( Level.SEVERE, null, ex );
    }
  }

  private  GenericDocument renderSpecialPage( String name, QTIRenderOptions options, QyoutiStyleSheet ss )
  {
    return renderSpecialPage( name, options, null, null, null, ss );
  }

  private  GenericDocument renderSpecialPage(
      String name,
      QTIRenderOptions options,
      String candidatename,
      String candidateid,
      String content,
      QyoutiStyleSheet ss )
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
    //TextPaneWrapper textPane = new TextPaneWrapper(buffer.toString());
    textPane.setSize( 0, 0 );
    textPane.setText( buffer.toString() );
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
      state.html.append("[[null element]]");
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
      state.html.append("<html>\n<head></head>\n" );
      state.html.append("<body style=\"font-size: " );
      state.html.append(QTIMetrics.inchesToSvg( prefs.getFontsize( getMetrics().getPropertyInches("fontsize") ) ));
      state.html.append("px" );//; font-family: " );
      //state.html.append( fontmanager.getDefaultFontFamilyName( prefs==null?false:prefs.isSerif() ) );
      state.html.append( ";\">\n");
      state.html.append( "<div>" ); // <table width=\"200\"><tr><td>" );

      if ( options.getQTIRenderBooleanOption("question_titles") )
      {
        state.html.append("<div>");
        TitleIcon titicon = new TitleIcon(
                qnumber,
                state.item.getTitle(),
                getMetrics().getPropertySvgUnitsInt("item-width")/5*2,  // BODGE BODGE TODO - get rid of bodge
                getMetrics().getPropertySvgUnitsInt("item-title-height"),
                getMetrics().getPropertySvgUnitsInt("item-title-font-height")
                );
        state.inserts.add(new InteractionInsert(state.next_id, e, null, titicon));
        state.html.append("<span id=\"qti_insert_" + (state.next_id++) + "\">*</span>\n");
        state.html.append("</div>");
      }

      state.html.append("<div>");
      renderSubElements( e, state );
      state.html.append("\n</div>\n");
      
      if ( type == PrintThread.TYPE_ANALYSIS )
        state.html.append( analysishtml );
      
      state.html.append("</div>\n</body>\n</html>\n");
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
      state.html.append( "<div class=\"Flowmat\">" );
      renderSubElements( e, state );
      state.html.append( "</div>" );
      return;
    }

    if (e instanceof QTIElementFlowlabel)
    {
      state.html.append( "<div class=\"Flowlabel\">" );
      renderSubElements( e, state );
      state.html.append( "</div>\n" );
      return;
    }


    if (e instanceof QTIMatmedia)
    {
//      if (!state.open_block)
//      {
//        state.html.append("<div class=\"Matmedia\">");
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
            try{uri = new URI(attr_uri);}
            catch (URISyntaxException ex)
            {
              ex.printStackTrace();
            }

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
            imicon = new SVGImageIcon(
                    uri,
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
          //state.html.append(" &nbsp; ");
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
          state.html.append("<div class=\"WebctMaterialwebeq\">\n");
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
      state.label_index=0;
      
      if ( state.flow_depth == 0 || state.ignore_flow )
        state.html.append( "<div class=\"Renderchoice\" style=\"padding-top: 80px;\">" );

      state.in_choice = true;
      renderSubElements( e, state );
      state.in_choice = false;

      if ( state.flow_depth == 0 || state.ignore_flow )
        state.html.append( "</div>" );

//      state.html.append("</table>\n");
      return;
    }

    if (e instanceof QTIElementRenderfib || e instanceof QTIExtensionRendersketcharea )
    {
      QTIExtensionRendersketcharea sketcharea = (QTIExtensionRendersketcharea)e;
//      if (state.open_block)
//      {
//        state.html.append("</div>\n");
//        state.open_block = false;
//      }

      String type=null;
      if ( e instanceof QTIElementRenderfib )
        type="render_fib";
      else
        type="render_sketch_area";
      state.inserts.add(new InteractionInsert(state.next_id, e, null,
              new PinkIcon(
                            (int) QTIMetrics.inchesToSvg(0.2 * sketcharea.getColumns() ),
                            (int) QTIMetrics.inchesToSvg(0.25 * sketcharea.getRows() ),
                            (int) QTIMetrics.inchesToSvg(0.02),
                            (int) QTIMetrics.inchesToSvg(0.0),
                            false,
                            type,
                            e.getIdent()
                            )));
      if ( state.flow_depth == 0 || state.ignore_flow )
        state.html.append( "<div class=\"Rendersketcharea\">" );
      state.html.append("<span id=\"qti_insert_" + (state.next_id++) + "\">*</span>\n");
      if ( state.flow_depth == 0 || state.ignore_flow )
        state.html.append( "</div>" );

      return;
    }

    if (e instanceof QTIElementResponselabel)
    {
      if ( state.in_fib )
        return;


      int a = prefs.isBigpinkbox() ? 4 : 1;
      int b = prefs.isBigpinkbox() ? 3 : 1;
      
      state.inserts.add(new InteractionInsert(state.next_id, e, null,
              new PinkIcon(
              getMetrics().getPropertySvgUnitsInt("pinkbox_width") * a / b,
              getMetrics().getPropertySvgUnitsInt("pinkbox_width") * a / b,
              getMetrics().getPropertySvgUnitsInt("pinkbox_border") * a / b,
              getMetrics().getPropertySvgUnitsInt("pinkbox_padding") * a / b,
              true,
              "response_label",
              e.getIdent()
              )));

//      state.inserts.add(new InteractionInsert(state.next_id, e, null,
//              new TickBoxIcon(
//              (int) QTIMetrics.inchesToSvg(0.27),
//              (int) QTIMetrics.inchesToSvg(0.27),
//              (int) QTIMetrics.inchesToSvg(0.02),
//              (int) QTIMetrics.inchesToSvg(0.025)
//              )));


      if ( state.flow_depth == 0 || state.ignore_flow )
      {
        //state.html.append( "<div>" );
        state.html.append("<table><tr>\n");
        state.html.append("<td valign=\"top\"><div  class=\"Responselabel\" style=\"margin: 0px");
        state.html.append(" " + (int) QTIMetrics.inchesToSvg(0.1) + "px ");
        state.html.append(" " + (int) QTIMetrics.inchesToSvg(0.05) + "px ");
        state.html.append(" 0px;\">");
      }

      // The span will always have one character in it - which will be
      // deleted and replaced with a component.
      state.html.append("<span id=\"qti_insert_" + (state.next_id++) + "\">*</span> ");

      if ( state.flow_depth == 0 || state.ignore_flow )
      {
        state.html.append("</div></td>\n");
        state.html.append("<td valign=\"top\"><div  class=\"Responselabel\" style=\"margin: 0px");
        state.html.append(" " + (int) QTIMetrics.inchesToSvg(0.05) + "px ");
        state.html.append(" " + (int) QTIMetrics.inchesToSvg(0.05) + "px ");
        state.html.append(" 0px;\">");
        state.html.append( (char)('A' + state.label_index) );
        state.html.append(".</div></td>");
        state.html.append("<td valign=\"top\"><div>");
      }

      renderSubElements( e, state );

      if ( state.flow_depth == 0 || state.ignore_flow )
      {
        state.html.append("</div>\n");
        state.html.append("</td>\n</tr>\n");
        state.html.append( "</table>" );
      }
      state.label_index++;
      return;
    }


    // not an interesting element but it might have interesting sub-elements.
    renderSubElements( e, state );

  }






  public List<GenericDocument> paginateItems( 
      QyoutiFontManager fontmanager,
      MissingGlyphReport mgr,
      int type,
      String printid,
      URI examfolderuri,
      CandidateData candidate,
      ExaminationData exam,
      //QuestionMetricsRecordSet metricrecordset,
      PaginationRecord paginationrecord,
      String preamble )
  {
      int i;
      //Vector<Vector<SVGDocumentPlacement>> pages = new Vector<Vector<SVGDocumentPlacement>>();
      if ( candidate!=null && paginationrecord != null )
        paginationrecord.addCandidate( candidate.id );
      Vector<SVGDocumentPlacement> page;
      GenericDocument svgdocs[]=null;
      Vector<GenericDocument> paginated = new Vector<GenericDocument>();
      Vector<SVGHooks> svghooks = new Vector<SVGHooks>();
      Vector<QTIElementItem> items;
      if ( candidate == null )
        items = exam.qdefs.qti.getItems();
      else
        items = candidate.getItems();
      UserRenderPreferences prefs = (candidate!=null)?candidate.preferences:null;
      QyoutiStyleSheet ss = fontmanager.getQyoutiStyleSheet( (prefs!=null)?prefs.isSerif():false );
      
      int pwidth = (int)(getMetrics().getPropertyInches( "page-width" )*100);
      int pheight = (int)(getMetrics().getPropertyInches( "page-height" )*100);
      int tlx  = getMetrics().getPropertySvgUnitsInt("calibration-topleft-x");
      int tly  = getMetrics().getPropertySvgUnitsInt("calibration-topleft-y");
      int pinsetl = (int)(getMetrics().getPropertyInches( "item-area-inset-left" )*100);
      int pinsett = (int)(getMetrics().getPropertyInches( "item-area-inset-top" )*100);


      double itemareinsetleft = getMetrics().getPropertySvgUnits("item-area-inset-left");
      double itemareinsettop = getMetrics().getPropertySvgUnits("item-area-inset-top");
      
//      int columns = exam.getQTIRenderIntegerOption( "columns" );
      int columnoverride = 0;
      if ( type == PrintThread.TYPE_ANALYSIS )
         columnoverride = 1;
      if ( prefs != null && prefs.isOneColumn() )
         columnoverride = 1;
      
      int columns = -1;
      int previous_columns=-1;
      int column = 0;
      double columnoffset=0;

      
      double totalspace = 
               getMetrics().getPropertySvgUnits("page-height")
              -getMetrics().getPropertySvgUnits("item-area-inset-top")
              -getMetrics().getPropertySvgUnits("item-area-inset-bottom");
      double spaceleft = totalspace;
      double col_sec_top = 0.0;
      double xoffset = 0.0;
      double yoffset = 0.0;
      
      double itemheight, itemwidth;
      boolean has_cover = exam.getQTIRenderBooleanOption("cover_sheet");
      boolean has_blank = false;

      // get a blank page ready for questions...
      page = new Vector<SVGDocumentPlacement>();
      String pageid;
      if ( candidate!=null && paginationrecord != null )
      {
        pageid = paginationrecord.addPage( pwidth, pheight, pinsetl, pinsett );
        exam.createPage( printid, pageid, candidate );
      }
            
      if ( has_cover )
      {
        GenericDocument coversvg = renderSpecialPage("org/qyouti/qti1/gui/examcover.xhtml",
            exam,
            (candidate!=null)?candidate.name:"---",
            (candidate!=null)?candidate.id:"---",
            (preamble!=null)?preamble:"",
            ss
            );
        page.add( new SVGDocumentPlacement( coversvg, 0.0, 0.0 ) );
        svghooks.add( new SVGHooks() );
        paginated.add(placeSVGOnPage(
                page, 
                false, 
                exam, 
                fontmanager,
                prefs, 
                paginationrecord, 
                svghooks.lastElement() ) );
        
        // get a blank page ready for questions...
        page = new Vector<SVGDocumentPlacement>();
        if ( candidate != null && paginationrecord != null ) //paginationrecord.addPage( pwidth, pheight, pinsetl, pinsett );
        {
          pageid = paginationrecord.addPage( pwidth, pheight, pinsetl, pinsett );
          exam.addPage( new PrintedPageData( exam, printid, pageid, candidate ) );
        }
      }

      svgdocs = new GenericDocument[items.size()];
      for ( i=0; i<items.size(); i++ )
      {
        previous_columns = columns;
        columns = items.elementAt(i).getPresentation().getColumns();
        if ( columnoverride > 0 ) columns = columnoverride;
        if ( columns != previous_columns )
        {
          // new section with different number of columns
          columnoffset = getMetrics().getPropertyInches( "page-width" );
          columnoffset -= getMetrics().getPropertyInches( "item-area-inset-left" );
          columnoffset -= getMetrics().getPropertyInches( "item-area-inset-right" );
          columnoffset -= (double)(columns-1) * getMetrics().getPropertyInches( "item-area-column-spacing" );
          columnoffset = columnoffset / (double)columns;
          columnoffset += getMetrics().getPropertyInches( "item-area-column-spacing" );
          if ( column == 0 )
          {
            // previous section was working in first column
            // so we need to start the section in the place where
            // the previous section would have put it
            col_sec_top = yoffset;
          }
            // else
            // previous section is in 2nd or later column
            // so must have reached the bottom of the page in the 1st
            // column.
            // So, we need a new page for this section...
          column = 0;
        }
        
        // if ( i==29 ) System.out.println( "Item 29" );
        setItem( items.elementAt(i), i+1, prefs );
        if ( type == PrintThread.TYPE_ANALYSIS )
          setAnalysisHTML( exam.getQuestionAnalysis( items.elementAt(i).getIdent() ).toHTML() );
        setColumnOverride( columnoverride );
        renderItem();
        mgr.addReport( getMissingGlyphReport() );
        svgdocs[i] = getSVGDocument();
        // if ( i==29 ) QyoutiUtils.dumpXMLFile( "/home/jon/Desktop/debug.svg", svgdocs[i].getDocumentElement(), true );
        itemheight = Integer.parseInt( svgdocs[i].getDocumentElement().getAttribute("height") );
        itemwidth = getSVGResult().getWidth();
        if ( itemheight > spaceleft )
        {
//        System.out.println( "Out of space for item height" );
          // Is there another column on this page?
          if ( column < (columns-1) )
          {
            // yes - move to next column for this question
            spaceleft = totalspace - col_sec_top;
            yoffset = col_sec_top;
            column++;
          }
          else
          {
            // finish off the page
            svghooks.add( new SVGHooks() );
            paginated.add(placeSVGOnPage(
                    page, false, exam, fontmanager, prefs, paginationrecord, svghooks.lastElement() ) );
            
            // start a new page for this question
            if (  candidate != null && paginationrecord != null ) //paginationrecord.addPage( pwidth, pheight, pinsetl, pinsett );
            {
              pageid = paginationrecord.addPage( pwidth, pheight, pinsetl, pinsett );
              exam.addPage( new PrintedPageData( exam, printid, pageid, candidate ) );
            }
            
            page = new Vector<SVGDocumentPlacement>();
            
            spaceleft = totalspace;
            yoffset = 0.0;
            col_sec_top = 0.0;
            column=0;
          }
        }
        
        xoffset = QTIMetrics.inchesToSvg( columnoffset * column );
        page.add( new SVGDocumentPlacement( svgdocs[i], xoffset + itemareinsetleft, yoffset + itemareinsettop ) );
        // xoffset is relative to itemareainsetleft
        // record integer relative to top left bullseye_major in hundredths of inch
        if ( candidate != null && paginationrecord != null )
          paginationrecord.addItem( 
                  items.elementAt( i ).getIdent(), 
                  (int)((xoffset+itemareinsetleft - tlx)/10.0), 
                  (int)((yoffset+itemareinsettop - tly)/10.0),
                  (int)(itemwidth/10.0),
                  (int)(itemheight/10.0),
                  mrec );
        spaceleft -= itemheight;
        yoffset += itemheight;
        //metricrecordset.addItem( candidate.preferences, renderer.mrec );
      }

      // complete the last page of questions
      svghooks.add( new SVGHooks() );
      paginated.add(placeSVGOnPage(page, false, exam, fontmanager, prefs, paginationrecord, svghooks.lastElement() ) );

      boolean doublesided = exam.getQTIRenderBooleanOption("double_sided");
      boolean multipage = (doublesided && svghooks.size() > 2) || (!doublesided && svghooks.size() > 1);
      
      
      if ( (svghooks.size() & 1) == 1 && doublesided )
      {
        has_blank = true;
        GenericDocument blanksvg = renderSpecialPage("org/qyouti/qti1/gui/blank.xhtml", exam, ss );
        // start a page
        if (  candidate != null && paginationrecord != null ) //paginationrecord.addPage( pwidth, pheight, pinsetl, pinsett );
        {
          pageid = paginationrecord.addPage( pwidth, pheight, pinsetl, pinsett );
          exam.addPage( new PrintedPageData( exam, printid, pageid, candidate ) );
        }
        page = new Vector<SVGDocumentPlacement>();
        
        // create some SVG
        page.add( new SVGDocumentPlacement( blanksvg, 0.0, 0.0 ) );

        // complete page
        svghooks.add( new SVGHooks() );
        paginated.add(placeSVGOnPage(page, false, exam, fontmanager, prefs, paginationrecord, svghooks.lastElement() ) );
      }

      String footer;
      for ( i=0; i<svghooks.size(); i++ )
      {
        if ( i==0 && multipage && svghooks.get( i ).staplegroupelement != null )
          addStaple( svghooks.get( i ).staplegroupelement );
        footer = "";
        if ( candidate != null && exam.getQTIRenderBooleanOption( "name_in_footer" ) )
          footer += candidate.name + "   ";
        if (  candidate != null && exam.getQTIRenderBooleanOption( "id_in_footer" ) )
          footer += candidate.id + "\u00a0\u00a0\u00a0\u00a0";
        footer += "Page " + (i+1) + " of " + svghooks.size();
        svghooks.get(i).footertextelement.setTextContent( footer );
        //System.out.println( "Paginated: " + qrout );
      }


      return paginated;
  }

  static org.w3c.dom.Element staple;
  void initializeStaple()
  {
    org.w3c.dom.Document doc;
    try
    {
      InputStream in = getClass().getClassLoader().getResourceAsStream("org/qyouti/qti1/gui/staple.xml");  
      String parser = XMLResourceDescriptor.getXMLParserClassName();
      SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
      String uri = "staple.svg";
      doc = f.createSVGDocument(uri, in);
      in.close();
    }
    catch (IOException ex)
    {
      staple = null;
      return;
    }  
    NodeList nl = doc.getDocumentElement().getElementsByTagName("g");
    staple = (org.w3c.dom.Element) nl.item(0);
  }
  
//  static org.w3c.dom.Element calibration_tl;
//  static org.w3c.dom.Element calibration_bl;
//  static org.w3c.dom.Element calibration_br;
  static org.w3c.dom.Element bullseye_major, bullseye_minor;
  
  static org.w3c.dom.Element initializeCalibrationCodes( int radius )
  {
    org.w3c.dom.Element bullseye;
    
    double[] radii = BullseyeGenerator.scaleRadii( radius );
    boolean dark = true;

    DOMImplementation impl = GenericDOMImplementation.getDOMImplementation();
    String svgNS = SVGConstants.SVG_NAMESPACE_URI;
    org.w3c.dom.Document doc = impl.createDocument(svgNS, "svg", null);
    bullseye = doc.createElementNS(svgNS, "g");
    doc.getDocumentElement().appendChild( bullseye );
    org.w3c.dom.Element circle;
    
    bullseye.appendChild( doc.createTextNode( "\n" ) );
    for ( int i=0; i<radii.length; i++ )
    {
      circle = doc.createElementNS(svgNS,"circle");
      circle.setAttribute("cx", "0.0" );
      circle.setAttribute("cy", "0.0" );
      circle.setAttribute("r", Double.toString( radii[i] ) );

      circle.setAttribute( "stroke", "none" );
      circle.setAttribute( "fill", dark?"rgb(0,0,0)":"rgb(255,255,255)" );
      dark = !dark;
      bullseye.appendChild( circle );
      bullseye.appendChild( doc.createTextNode( "\n" ) );
    }
    return bullseye;
  }
  
  
  public GenericDocument placeSVGOnPage( 
          Vector<SVGDocumentPlacement> itemdocs , 
          boolean rulers, 
          QTIRenderOptions options,
          QyoutiFontManager fontmanager,
          UserRenderPreferences userprefs,
          PaginationRecord paginationrecord,
          SVGHooks hooks
        )
  {
    int i;
    String svgNS = SVGConstants.SVG_NAMESPACE_URI;
    DOMImplementation impl = GenericDOMImplementation.getDOMImplementation();
    GenericDocument pdoc = (GenericDocument) impl.createDocument(svgNS, "svg", null);
    double rulerwidth = rulers?0.5:0.0, rulerheight = rulers?0.5:0.0;

    double widthinches = getMetrics().getPropertyInches("page-width") + rulerwidth;
    double heightinches = getMetrics().getPropertyInches("page-height") + rulerheight;
    int layout = options.getQTIRenderIntegerOption("layout");
            
    String backgroundrgb = "rgb(255,255,255)";
    if ( userprefs != null && userprefs.getBackground() != null )
    {
      Color colour = userprefs.getBackground();
      backgroundrgb = "rgb(" + colour.getRed() + "," + colour.getGreen() + "," + colour.getBlue() + ")";
    } 

    AbstractElement svg = (AbstractElement) pdoc.getDocumentElement();
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
    svg.setAttribute("preserveAspectRatio", "xMinYMin meet" );

    org.w3c.dom.Element defs = pdoc.createElementNS( svgNS, "defs" );
    svg.appendChild( defs );
    org.w3c.dom.Element filter = pdoc.createElementNS( svgNS, "filter" );
    defs.appendChild( filter );
    filter.setAttribute( "id", "yellowhighlight" );
    filter.setAttribute( "x", "-0.025" );
    filter.setAttribute( "y", "-0.25" );
    filter.setAttribute( "width", "1.05" );
    filter.setAttribute( "height", "1.5" );
    
    org.w3c.dom.Element filterF = pdoc.createElementNS( svgNS, "feFlood" );
    filter.appendChild( filterF );
    filterF.setAttribute( "flood-color", "yellow" );
    filterF.setAttribute( "opacity", "1.0" );
    
//    while ( svg.getChildNodes().getLength() != 0 )
//    {
//      itemg.appendChild( svg.getFirstChild().cloneNode(true) );
//      svg.removeChild( svg.getFirstChild() );
//    }

    org.w3c.dom.Element decorationgroup = pdoc.createElementNS( svgNS, "g");
    decorationgroup.setAttribute( 
            "font-family", 
            fontmanager.getDefaultFontFamilyName( userprefs==null?false:userprefs.isSerif() ) );
    if ( rulers )
      decorationgroup.setAttribute("transform",
           "translate( " +
          (int)QTIMetrics.inchesToSvg(rulerwidth ) + ", "+
          (int)QTIMetrics.inchesToSvg(rulerheight ) + ")");
    svg.appendChild(decorationgroup);

    // white page background
    org.w3c.dom.Element r = pdoc.createElementNS( svgNS, "rect");
    r.setAttribute("x", "0" );
    r.setAttribute("y", "0" );
    r.setAttribute( "stroke", "rgb(0,0,0)" );
    r.setAttribute( "stroke-width", "" + QTIMetrics.inchesToSvg( 0.02 ) );
    r.setAttribute( "fill", backgroundrgb );
    r.setAttribute( "width",  "" + getMetrics().getPropertySvgUnitsInt("page-width") );
    r.setAttribute( "height", "" + getMetrics().getPropertySvgUnitsInt("page-height") );
    decorationgroup.appendChild( r );

    
    String pageqr = "qyouti";
    if ( paginationrecord != null )
      pageqr = pageqr + "/" + paginationrecord.getPageId();
    
      

    // Add calibration marks - using the same SVG over and over
    // for every page.
    if ( bullseye_major == null )
      bullseye_major = initializeCalibrationCodes( getMetrics().getPropertySvgUnitsInt("bullseye-radius") );
    int minordivisions = getMetrics().getPropertyInt("bullseye-minor-divisions");
    if ( minordivisions > 1 && bullseye_minor == null )
      bullseye_minor = initializeCalibrationCodes( getMetrics().getPropertySvgUnitsInt("bullseye-minor-radius") );
    
    int tlx, tly, brx, bry;
    int w = getMetrics().getPropertySvgUnitsInt("barcode-width");
    int o = getMetrics().getPropertySvgUnitsInt("barcode-offset");
    tlx  = getMetrics().getPropertySvgUnitsInt("calibration-topleft-x");
    tly  = getMetrics().getPropertySvgUnitsInt("calibration-topleft-y");
    brx  = getMetrics().getPropertySvgUnitsInt("calibration-bottomright-x");
    bry  = getMetrics().getPropertySvgUnitsInt("calibration-bottomright-y");
    
    org.w3c.dom.Element onedbarcode;
    if ( layout == 2 )
    {
      onedbarcode = ZXingCodec.encode1DSVG( pageqr, (brx-tlx)*3/4, w );
      SVGUtils.appendFragmentToDocument( decorationgroup, onedbarcode, 0.0, (tlx+brx)/2, o  );
    }
    else
    {
      onedbarcode = ZXingCodec.encode1DSVG( pageqr, (bry-tly)*3/4, w );
      SVGUtils.appendFragmentToDocument( decorationgroup, onedbarcode, 90.0, o, (tly+bry)/2 );
    }
    //QyoutiUtils.dumpXMLFile( "/home/jon/Desktop/debug.svg", onedbarcode, true );

    if ( layout==2 )
    {
      SVGUtils.appendFragmentToDocument(decorationgroup, bullseye_major, 0.0, tlx, bry );
      SVGUtils.appendFragmentToDocument(decorationgroup, bullseye_major, 0.0, brx, bry );
      SVGUtils.appendFragmentToDocument(decorationgroup, bullseye_major, 0.0, brx, tly );
      for ( i=1; i<minordivisions; i++ )
        SVGUtils.appendFragmentToDocument(decorationgroup, bullseye_minor, 0.0, brx, tly + (bry-tly)*i/minordivisions );    
    }
    else
    {
      SVGUtils.appendFragmentToDocument(decorationgroup, bullseye_major, 0.0, tlx, bry );
      SVGUtils.appendFragmentToDocument(decorationgroup, bullseye_major, 0.0, brx, bry );
      SVGUtils.appendFragmentToDocument(decorationgroup, bullseye_major, 0.0, tlx, tly );      
    }
    
    if ( paginationrecord != null )
    {
      int radius = getMetrics().getPropertySvgUnitsInt("bullseye-radius");
      if ( layout==2 )
      {
        paginationrecord.addBarcode( PaginationRecord.Barcode.BARCODE_TOP, pageqr );
        paginationrecord.addBullseye(PaginationRecord.Bullseye.BULLSEYE_BOTTOM_LEFT,  tlx/10, bry/10, radius/10 );
        paginationrecord.addBullseye(PaginationRecord.Bullseye.BULLSEYE_BOTTOM_RIGHT, brx/10, bry/10, radius/10 );
        paginationrecord.addBullseye(PaginationRecord.Bullseye.BULLSEYE_TOP_RIGHT,    brx/10, tly/10, radius/10 );
        radius = getMetrics().getPropertySvgUnitsInt("bullseye-minor-radius");
        paginationrecord.setVerticalBullseyeDivisions( minordivisions, radius/10 );
      }
      else
      {
        paginationrecord.addBarcode( PaginationRecord.Barcode.BARCODE_LEFT, pageqr );
        paginationrecord.addBullseye(PaginationRecord.Bullseye.BULLSEYE_BOTTOM_LEFT,  tlx/10, bry/10, radius/10 );
        paginationrecord.addBullseye(PaginationRecord.Bullseye.BULLSEYE_BOTTOM_RIGHT, brx/10, bry/10, radius/10 );
        paginationrecord.addBullseye(PaginationRecord.Bullseye.BULLSEYE_TOP_LEFT,     tlx/10, tly/10, radius/10 );
      }
    }
    // Completed calibration marks

    
    if ( options.getQTIRenderOption("header") != null )
    {
      org.w3c.dom.Element theader;
      
      // This just makes a yellow box the same position and size as the given text
      theader = pdoc.createElementNS(svgNS, "text" );
      theader.setAttribute("filter", "url(#yellowhighlight)" );
      theader.setAttribute("text-anchor", getMetrics().getProperty("header-anchor") );
      theader.setAttribute("x", "" + (getMetrics().getPropertySvgUnitsInt("header-anchor-x")) );
      theader.setAttribute("y", "" + (getMetrics().getPropertySvgUnitsInt("header-anchor-y")) );
      theader.setAttribute("font-size", "" + QTIMetrics.inchesToSvg( 0.15 ) );
      theader.setTextContent( options.getQTIRenderOption("header") );
      decorationgroup.appendChild(theader);
      
      // puts the text on top of the highlight box
      theader = pdoc.createElementNS(svgNS, "text");
      theader.setAttribute("text-anchor", getMetrics().getProperty("header-anchor") );
      theader.setAttribute("x", "" + (getMetrics().getPropertySvgUnitsInt("header-anchor-x")) );
      theader.setAttribute("y", "" + (getMetrics().getPropertySvgUnitsInt("header-anchor-y")) );
      theader.setAttribute("font-size", "" + QTIMetrics.inchesToSvg( 0.15 ) );
      theader.setTextContent( options.getQTIRenderOption("header") );
      decorationgroup.appendChild(theader);
    }

    org.w3c.dom.Element tfooter;
    tfooter = pdoc.createElementNS(svgNS, "text");
    tfooter.setAttribute("text-anchor", "middle" );
    tfooter.setAttribute("x", "" + (getMetrics().getPropertySvgUnitsInt("page-width")/2) );
    tfooter.setAttribute("y", "" + (getMetrics().getPropertySvgUnitsInt("page-height")-(QTIMetrics.inchesToSvg(0.6))) );
    tfooter.setAttribute("font-size", "" + QTIMetrics.inchesToSvg( 0.15 ) );
    decorationgroup.appendChild(tfooter);

    if ( hooks != null )
    {
      hooks.footertextelement = tfooter;
      
      hooks.staplegroupelement = pdoc.createElementNS( svgNS, "g");
      decorationgroup.appendChild( hooks.staplegroupelement );
      hooks.staplegroupelement.setAttribute("x", "" + QTIMetrics.inchesToSvg(rulerwidth ) );
      hooks.staplegroupelement.setAttribute("y", "" + QTIMetrics.inchesToSvg(rulerheight ) );
    }

    if ( rulers )
    {
      org.w3c.dom.Element rulergroup = pdoc.createElementNS( svgNS, "g");
      svg.appendChild(rulergroup);
      
      // yellow ruler at top
      org.w3c.dom.Element rt = pdoc.createElementNS( svgNS, "rect");
      rt.setAttribute("x", "" + QTIMetrics.inchesToSvg(rulerwidth ) );
      rt.setAttribute( "y", "" + QTIMetrics.inchesToSvg( 0.0 ) );
      rt.setAttribute( "stroke", "rgb(0,0,0)" );
      rt.setAttribute( "stroke-width", "" + QTIMetrics.inchesToSvg( 0.02 ) );
      rt.setAttribute( "fill", "rgb(255,255,200)" );
      rt.setAttribute( "width",  "" + getMetrics().getPropertySvgUnitsInt("page-width") );
      rt.setAttribute("height", "" + QTIMetrics.inchesToSvg(rulerheight ) );
      rulergroup.appendChild( rt );


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
        rulergroup.appendChild(line);
        if ( (i%5)==0 && i>0 )
        {
          t = pdoc.createElementNS(svgNS, "text");
          t.setAttribute("text-anchor", "middle" );
          t.setAttribute("x", "" + (x + QTIMetrics.inchesToSvg( 0.5 ) ) );
          t.setAttribute("y", "" + QTIMetrics.inchesToSvg( 0.3 ) );
          t.setAttribute("font-size", "" + QTIMetrics.inchesToSvg( 0.2 ) );
          t.setTextContent( "" + (i/5) + "\"" );
          rulergroup.appendChild(t);
        }
      }

      // yellow ruler on left
      org.w3c.dom.Element rl = pdoc.createElementNS( svgNS, "rect");
      rl.setAttribute( "x", "" + QTIMetrics.inchesToSvg(  0.0 ) );
      rl.setAttribute( "y", "" + QTIMetrics.inchesToSvg(  0.5 ) );
      rl.setAttribute( "stroke", "rgb(0,0,0)" );
      rl.setAttribute( "stroke-width", "" + QTIMetrics.inchesToSvg( 0.02 ) );
      rl.setAttribute( "fill", "rgb(255,255,200)" );
      rl.setAttribute( "width", "" + QTIMetrics.inchesToSvg( 0.5 ) );
      rl.setAttribute( "height",  "" + getMetrics().getPropertySvgUnitsInt("page-height") );
      rulergroup.appendChild( rl );
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
        rulergroup.appendChild(line);
        if ( (i%5)==0 && i>0 )
        {
          t = pdoc.createElementNS(svgNS, "text");
          t.setAttribute("text-anchor", "end" );
          t.setAttribute("x", "" + QTIMetrics.inchesToSvg( 0.4 ) );
          t.setAttribute("y", "" + (y + QTIMetrics.inchesToSvg( 0.55 ) ) );
          t.setAttribute("font-size", "" + QTIMetrics.inchesToSvg( 0.2 ) );
          t.setTextContent( "" + (i/5) + "\"" );
          rulergroup.appendChild(t);
        }
      }
    }

    // plonk given svg onto decorations
    org.w3c.dom.Element itemg;
    SVGDocumentPlacement itemdoc;
    for ( i=0; i<itemdocs.size(); i++ )
    {
      itemdoc = itemdocs.elementAt(i);

      itemg = pdoc.createElementNS( svgNS, "g" );
      itemg.setAttribute("transform",
          "translate( " +
          (int)itemdoc.getX() + ", "+
          (int)itemdoc.getY() + ")");
      decorationgroup.appendChild(itemg);
      SVGUtils.insertDocumentContents(pdoc, itemg, itemdoc.getDoc() );
    }
    
    //pdoc.normalizeDocument();
    // Fix font-family attributes...
    NodeIterator iter = pdoc.createNodeIterator( 
            pdoc.getDocumentElement(), 
            NodeFilter.SHOW_ELEMENT, 
            new NodeFilter()
            {
              @Override
              public short acceptNode( Node n )
              {
                if ( n.getNodeType() == Node.ELEMENT_NODE )
                {
                  org.w3c.dom.Element e = (org.w3c.dom.Element)n;
                  Attr attr = e.getAttributeNode( "font-family" );
                  return attr==null?NodeFilter.FILTER_SKIP:NodeFilter.FILTER_ACCEPT;
                }
                return NodeFilter.FILTER_REJECT;
              }
            }, 
            false );
    Node node;
    org.w3c.dom.Element e;
    Attr attr;
    String val;
    while ( true )
    {
      node = iter.nextNode();
      if ( node == null ) break;
      if ( node.getNodeType() != Node.ELEMENT_NODE ) continue;
      e = (org.w3c.dom.Element)node;
      attr = e.getAttributeNode( "font-family" );
      if ( attr == null ) continue;
      val = attr.getValue();
      if ( val.startsWith( "'" ) ) val = val.substring( 1 );
      if ( val.endsWith( "'" ) )   val = val.substring( 0, val.length()-1 );
      val = fontmanager.getSVGFontFamilyAttribute( val );

      attr.setValue( val );
    }
    
    //QyoutiUtils.dumpXMLFile( "/home/jon/Desktop/page.svg", pdoc.getDocumentElement(), true );
    return pdoc;
  }

  public void addStaple( org.w3c.dom.Element staplegroup )
  {
    if ( staple == null )
      initializeStaple();
    
    SVGUtils.appendFragmentToDocument(staplegroup, staple, 0, 0, 0);
    
    org.w3c.dom.Element line;
    line =  staplegroup.getOwnerDocument().createElementNS( SVGConstants.SVG_NAMESPACE_URI, "line");
    line.setAttribute( "x1", "" + QTIMetrics.inchesToSvg( 0.0 ) );
    line.setAttribute( "y1", "" + QTIMetrics.inchesToSvg( 1.0 ) );
    line.setAttribute( "x2", "" + QTIMetrics.inchesToSvg( 1.0 ) );
    line.setAttribute( "y2", "" + QTIMetrics.inchesToSvg( 0.0 ) );
    line.setAttribute( "stroke-width",  "" + QTIMetrics.inchesToSvg( 0.02 ) );
    line.setAttribute( "stroke-dasharray",  "" + QTIMetrics.inchesToSvg( 0.1 ) + ", " + QTIMetrics.inchesToSvg( 0.1 ) );
    line.setAttribute( "stroke", "rgb(0,0,0)" );
    staplegroup.appendChild( line );
  }

  public GenericDocument getPreviewSVGDocument( QTIRenderOptions options )
  {
    Vector<SVGDocumentPlacement> v = new Vector<SVGDocumentPlacement>();
    double itemareinsetleft = getMetrics().getPropertySvgUnits("item-area-inset-left");
    double itemareinsettop = getMetrics().getPropertySvgUnits("item-area-inset-top");

    SVGDocumentPlacement dp = new SVGDocumentPlacement((GenericDocument) svgres.getDocument().cloneNode(true), itemareinsetleft, itemareinsettop );
    v.add( dp );
    return placeSVGOnPage( v, true, options, fontmanager, null, null, null );
  }

  public SvgConversionResult getSVGResult()
  {
    return svgres;
  }

  public GenericDocument getSVGDocument()
  {
    return svgres.getDocument();
  }

  public MissingGlyphReport getMissingGlyphReport()
  {
    return svgres.getMissingGlyphReport();
  }

  public QTIMetrics getMetrics()
  {
    if ( metrics == null )
    {
      String name = "org/qyouti/qti1/gui/defaultmetrics.xml";
      if ( options != null && options.getQTIRenderIntegerOption("layout") == 2 )
        name = "org/qyouti/qti1/gui/topbarmetrics.xml";
      metrics = new QTIMetrics(name);
    }
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

    int label_index=0;
    boolean ignore_flow = false;
//    boolean break_response_labels = false;

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

class SVGDocumentPlacement
{
  GenericDocument doc;
  double x; // inches from left
  double y; // inches from top
  public SVGDocumentPlacement( GenericDocument doc, double x, double y)
  {
    this.doc = doc;
    this.x = x;
    this.y = y;
  }
  public GenericDocument getDoc()
  {
    return doc;
  }
  public double getX()
  {
    return x;
  }
  public double getY()
  {
    return y;
  }
}

  
  class SVGHooks
  {
    public org.w3c.dom.Element staplegroupelement;    
    public org.w3c.dom.Element footertextelement;    
  }

