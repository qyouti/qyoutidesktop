/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.qyouti.qti1.gui;

import java.io.IOException;
import java.io.Writer;
import java.math.*;
import java.security.*;
import java.text.*;
import java.util.*;
import java.util.logging.*;
import org.w3c.dom.*;

/**
 *
 * @author jon
 */
public class PaginationRecord
{
  String printid;
  Vector<Candidate> candidates = new Vector<Candidate>();
  int nextid = 1;
  HashMap<String,Page> pagesbyid = new HashMap<String,Page>();
  
  
  public static final DecimalFormat pageidformat = new DecimalFormat("0000");

  public PaginationRecord( String name )
  {
    // The print ID is a portion of an MD5 hash
    // based on the name and current time
    // Time only is not good enough because
    // qyouti may be running in multiple
    // processes building lots of surveys
    long now = System.currentTimeMillis();
    MessageDigest md;
    try
    {
      md = MessageDigest.getInstance( "MD5" );
      md.update( name.getBytes() );
      for ( int j=0; j<4; j++ )
        md.update( (byte)((now >> (j*8)) & 0xff) );
      BigInteger digest = new BigInteger( 1, md.digest() );
      printid=digest.toString( Character.MAX_RADIX );
    }
    catch ( NoSuchAlgorithmException ex )
    {
      Logger.getLogger( PaginationRecord.class.getName() ).
              log( Level.SEVERE, null, ex );
      printid = Long.toHexString( now );
    }
    printid = printid.substring( 0, 6 );
  }

  public PaginationRecord( Document doc )
  {
    Node node;
    Element e_pagination, e_candidate;
    NodeList nl;
    
    e_pagination = doc.getDocumentElement();
    printid = e_pagination.getAttribute( "print-id" );

    nl = e_pagination.getChildNodes();
    for ( int i = 0; i< nl.getLength(); i++ )
    {
      node = nl.item( i );
      if ( node.getNodeType() != Node.ELEMENT_NODE )
        continue;

      if ( !"candidate".equals( node.getNodeName() ) )
        continue;
      
      loadCandidate( (Element)node );
    }
  }
  
  public void loadCandidate( Element e_candidate )
  {
    addCandidate( e_candidate.getAttribute( "id" ) );

    Node node;
    NodeList nl;
    int p=1;

    nl = e_candidate.getChildNodes();
    for ( int i = 0; i< nl.getLength(); i++ )
    {
      node = nl.item( i );
      if ( node.getNodeType() != Node.ELEMENT_NODE )
        continue;

      if ( !"page".equals( node.getNodeName() ) )
        continue;
      
      loadPage( (Element)node );
    }
  }
  
  public void loadPage( Element e_page )
  {
    addPage( 
            e_page.getAttribute( "id" ),
            Integer.parseInt( e_page.getAttribute( "width" ) ),
            Integer.parseInt( e_page.getAttribute( "height" ) ),
            Integer.parseInt( e_page.getAttribute( "originx" ) ),
            Integer.parseInt( e_page.getAttribute( "originy" ) )
            );

    Node node;
    NodeList nl;

    nl = e_page.getChildNodes();
    for ( int i = 0; i< nl.getLength(); i++ )
    {
      node = nl.item( i );
      if ( node.getNodeType() != Node.ELEMENT_NODE )
        continue;

      if ( "bullseye".equals( node.getNodeName() ) )
        loadBullseye( (Element)node );
      if ( "item".equals( node.getNodeName() ) )
        loadItem( (Element)node );
    }
  }

  public void loadBullseye( Element e_be )
  {
    String strtype = e_be.getAttribute( "type" );
    int type;
    if ( "bottomleft".equals( strtype ) )       type = Bullseye.BULLSEYE_BOTTOM_LEFT;
    else if ( "bottomright".equals( strtype ) ) type = Bullseye.BULLSEYE_BOTTOM_RIGHT;
    else if ( "topleft".equals( strtype ) )     type = Bullseye.BULLSEYE_TOP_LEFT;
    else type = Bullseye.BULLSEYE_UNKNOWN;
    
    int x = Integer.parseInt( e_be.getAttribute( "x" ) );
    int y = Integer.parseInt( e_be.getAttribute( "y" ) );
    int r = Integer.parseInt( e_be.getAttribute( "radius" ) );
    
    addBullseye( type, x, y, r );
  }
  
  public void loadItem( Element e_item )
  {
    int x = Integer.parseInt( e_item.getAttribute( "x" ) );
    int y = Integer.parseInt( e_item.getAttribute( "y" ) );
    int w = Integer.parseInt( e_item.getAttribute( "w" ) );
    int h = Integer.parseInt( e_item.getAttribute( "h" ) );
    
    QuestionMetricsRecord qmr = new QuestionMetricsRecord( e_item.getChildNodes() );
    addItem( e_item.getAttribute( "id" ), x, y, w, h, qmr );
  }

  
  public String getPrintId()
  {
    return printid;
  }
  
  public void addCandidate( String id )
  {
    candidates.add( new Candidate( id ) );
  }

  public String addPage( int width, int height, int originx, int originy )
  {
    Page page = new Page( 
            candidates.lastElement(), 
            Integer.toString( nextid++ ), 
            //candidates.lastElement().pages.size() + 1,
            width, height, originx, originy );
    candidates.lastElement().pages.add( page );
    pagesbyid.put( page.id, page );
    return page.id;    
  }

  public void addPage( String id, int width, int height, int originx, int originy )
  {
    Page page = new Page( 
            candidates.lastElement(), 
            id, 
//            p,
            width, height, originx, originy );
    candidates.lastElement().pages.add( page );
    pagesbyid.put( page.id, page );
  }

  public String getPageId()
  {
    return printid + "/" + candidates.lastElement().pages.lastElement().id;
  }
  
  public void addItem( String ident, int x, int y, int w, int h, QuestionMetricsRecord qmr )
  {
    Page lastpage = candidates.lastElement().pages.lastElement();
    lastpage.items.add( new Item( lastpage, ident, x, y, w, h, qmr ) );
  }

  public void addBullseye( int type, int x, int y, int w )
  {
    Page lastpage = candidates.lastElement().pages.lastElement();
    lastpage.bullseyes.add( new Bullseye( lastpage, type, x, y, w ) );
    if ( type == PaginationRecord.Bullseye.BULLSEYE_BOTTOM_LEFT )
        lastpage.bl = lastpage.bullseyes.lastElement();
    if ( type == PaginationRecord.Bullseye.BULLSEYE_BOTTOM_RIGHT )
        lastpage.br = lastpage.bullseyes.lastElement();
    if ( type == PaginationRecord.Bullseye.BULLSEYE_TOP_LEFT )
        lastpage.tl = lastpage.bullseyes.lastElement();
  }

  public void emit(Writer writer) throws IOException
  {
    writer.write("<?xml version=\"1.0\"?>\n");
    writer.write("<pagination print-id=\"");
    writer.write(printid);
    writer.write("\">\n");

    for ( int i=0; i<candidates.size(); i++ )
      candidates.get( i ).emit( writer );

    writer.write("</pagination>\n");
  }


  
  public Page getPage( String pageid )
  {
    return pagesbyid.get( pageid );
  }
  
  public Candidate getCandidate( String pageid )
  {
    Page page = pagesbyid.get( pageid );
    if ( page == null ) return null;
    return page.parent;
  }
  


  public class Candidate
  {
    String id;
    Vector<Page> pages = new Vector<Page>();
    public Candidate( String id )
    {
      this.id = id;
    }

    public void emit(Writer writer) throws IOException
    {
      writer.write("  <candidate id=\"");
      writer.write(id);
      writer.write("\">\n");

      for ( int i=0; i<pages.size(); i++ )
        pages.get( i ).emit( writer );

      writer.write("  </candidate>\n");
    }

    public String getId()
    {
      return id;
    }
  }

  public class Page
  {
    Candidate parent;
    String id;
//    int pagenumber;
    int width;
    int height;
    int originx;
    int originy;
    
    Vector<Item> items = new Vector<Item>();
    Vector<Bullseye> bullseyes = new Vector<Bullseye>();
    Bullseye bl;
    Bullseye br;
    Bullseye tl;
    
    public Page( Candidate parent, String id, int width, int height, int originx, int originy )
    {
      this.parent  = parent;
      this.id      = id;
//      this.pagenumber = pagenumber;
      this.width   = width;
      this.height  = height;
      this.originx = originx;
      this.originy = originy;
    }

    public int getWidth()
    {
      return width;
    }

    public int getHeight()
    {
      return height;
    }
  
    public Bullseye getBullseye( int type )
    {
      switch ( type )
      {
        case Bullseye.BULLSEYE_TOP_LEFT:
          return tl;
        case Bullseye.BULLSEYE_BOTTOM_LEFT:
          return bl;
        case Bullseye.BULLSEYE_BOTTOM_RIGHT:
          return br;
      }
      return null;
    }
    
    public void emit(Writer writer) throws IOException
    {
      int i;
      writer.write( "    <page id=\""+ id + "\"" );
      writer.write( " width=\""+ width + "\"" );
      writer.write( " height=\""+ height + "\"" );
      writer.write( " originx=\""+ originx + "\"" );
      writer.write( " originy=\""+ originy + "\"" );
      writer.write( ">\n");
      for ( i=0; i<bullseyes.size(); i++ )
        bullseyes.get( i ).emit( writer );
      for ( i=0; i<items.size(); i++ )
        items.get( i ).emit( writer );
      writer.write("    </page>\n");
    }

//    public int getPagenumber()
//    {
//      return pagenumber;
//    }
    
    public Item[] getItems()
    {
      return items.toArray( new Item[items.size()] );
    }


    public double[] getItemOffset()
    {
      if ( tl == null )
        return null;
      double[] off = new double[2];
      off[0] = -tl.x;
      off[1] = -tl.y;
      return off;
    }
    
    /**
     * Gets horizontal and vertical spacing of bullseyes.
     * 
     * @return Array of two doubles units are in inches
     */
    public double[] getCalibrationDimension()
    {
      if ( bl == null || br == null || tl == null )
        return null;
      double[] dim = new double[2];
      dim[0] = br.x - bl.x;
      dim[1] = bl.y - tl.y;
      dim[0] = dim[0]/100.0;
      dim[1] = dim[1]/100.0;
      return dim;
    }
  }

  public class Bullseye
  {
    public static final int BULLSEYE_BOTTOM_LEFT  = 0;
    public static final int BULLSEYE_TOP_LEFT     = 1;
    public static final int BULLSEYE_BOTTOM_RIGHT = 2;
    public static final int BULLSEYE_UNKNOWN      = -1;
    
    Page parent;
    int type;
    int x;
    int y;
    int r;
    
    public Bullseye( Page parent, int type, int x, int y, int r )
    {
      this.parent = parent;
      this.type = type;
      this.x = x;
      this.y = y;
      this.r = r;
    }

    public int getX()
    {
      return x;
    }

    public int getY()
    {
      return y;
    }

    public int getR()
    {
      return r;
    }
    
    
    
    public void emit(Writer writer) throws IOException
    {
      writer.write("      <bullseye type=\"");
      switch ( type )
      {
        case BULLSEYE_BOTTOM_LEFT:
          writer.write( "bottomleft" );
          break;
        case BULLSEYE_TOP_LEFT:
          writer.write( "topleft" );
          break;
        case BULLSEYE_BOTTOM_RIGHT:
          writer.write( "bottomright" );
          break;
        default:
          writer.write( "unknown" );
          break;
      }
      writer.write("\" x=\"");
      writer.write( Integer.toString( x ) );
      writer.write("\" y=\"");
      writer.write( Integer.toString( y ) );
      writer.write("\" radius=\"");
      writer.write(Integer.toString(r ) );
      writer.write( "\"/>\n");
    }
  }
  
  public class Item
  {
    Page parent;
    String ident;
    int x;
    int y;
    int w;
    int h;
    QuestionMetricsRecord qmr;
    public Item( Page parent, String ident, int x, int y, int w, int h, QuestionMetricsRecord qmr )
    {
      this.parent = parent;
      this.ident = ident;
      this.x = x;
      this.y = y;
      this.w = w;
      this.h = h;
      this.qmr = qmr;
    }
    public void emit(Writer writer) throws IOException
    {
      writer.write("      <item id=\"");
      writer.write(ident);
      writer.write("\" x=\"");
      writer.write( Integer.toString( x ) );
      writer.write("\" y=\"");
      writer.write( Integer.toString( y ) );
      writer.write("\" w=\"");
      writer.write( Integer.toString( w ) );
      writer.write("\" h=\"");
      writer.write( Integer.toString( h ) );
      writer.write( "\">\n");
      qmr.emit( writer );
      writer.write("      </item>\n" );
    }

    public String getIdent()
    {
      return ident;
    }

    public int getX()
    {
      return x;
    }

    public int getY()
    {
      return y;
    }

    public int getWidth()
    {
      return w;
    }

    public int getHeight()
    {
      return h;
    }
    
    
    
    public QuestionMetricsRecord getQuestionMetricsRecord()
    {
      return qmr;
    }
  }
}
