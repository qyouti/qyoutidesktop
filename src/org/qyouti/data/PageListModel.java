/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.qyouti.data;

import java.util.Vector;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author jon
 */
public class PageListModel
        extends AbstractTableModel
{

  private static final String[] column_headings =
  {
    "Page No.", "Barcode", "Paper Name", "Paper ID", "Scanned", "Error"
  };

  Vector<PageData> pages = new Vector<PageData>();
  //Vector<PageData> unprocessedpages = new Vector<PageData>();
  //Hashtable<String,Vector<PageData>> candidatelookup = new Hashtable<String,Vector<PageData>>();



  public PageListModel( Vector<PageData> data )
  {
    this.pages = data;
  }
  
  @Override
  public int getRowCount()
  {
    return pages.size();
  }

  @Override
  public int getColumnCount()
  {
    return column_headings.length;
  }

  @Override
  public String getColumnName( int columnIndex )
  {
    return column_headings[columnIndex];
  }

  @Override
  public Class<?> getColumnClass( int columnIndex )
  {
    //if ( columnIndex == 0 ) return Integer.class;
    return String.class;
  }

  @Override
  public boolean isCellEditable( int rowIndex, int columnIndex )
  {
    return false;
  }

  @Override
  public Object getValueAt( int rowIndex, int columnIndex )
  {
    PageData page = pages.elementAt( rowIndex );
    switch( columnIndex )
    {
      case 0:
        return page.pageid;
      case 1:
        return page.code;
      case 2:
        return page.candidate_name;
      case 3:
        return page.candidate_number;
      case 4:
        return page.scanned?"yes":"no";
      case 5:
        return page.error;
    }
    return null;
  }

  @Override
  public void setValueAt( Object aValue, int rowIndex, int columnIndex )
  {
    throw new UnsupportedOperationException( "Not supported." );
  }

}
