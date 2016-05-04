/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.qyouti.qti1.gui;

import java.io.IOException;
import java.io.Writer;
import java.util.Vector;

/**
 *
 * @author jon
 */
public class PaginationRecord
{
  String printid;
  Vector<Candidate> candidates = new Vector<Candidate>();

  public PaginationRecord( String printid )
  {
    this.printid = printid;
  }

  public void addCandidate( String id )
  {
    candidates.add( new Candidate( id ) );
  }

  public void addPage()
  {
    candidates.lastElement().pages.add( new Page() );
  }

  public void addItem( String ident )
  {
    candidates.lastElement().pages.lastElement().items.add( new Item(ident) );
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
  }

  public class Page
  {
    Vector<Item> items = new Vector<Item>();
    public void emit(Writer writer) throws IOException
    {
      writer.write("    <page>\n");
      for ( int i=0; i<items.size(); i++ )
        items.get( i ).emit( writer );
      writer.write("    </page>\n");
    }
  }

  public class Item
  {
    String ident;
    public Item( String ident )
    {
      this.ident = ident;
    }
    public void emit(Writer writer) throws IOException
    {
      writer.write("      <item id=\"");
      writer.write(ident);
      writer.write("\"/>\n");
    }
  }
}
