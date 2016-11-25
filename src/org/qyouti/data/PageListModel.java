/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.qyouti.data;

import java.util.Hashtable;
import java.util.Vector;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

/**
 *
 * @author jon
 */
public class PageListModel
        extends AbstractTableModel
{

  private static final String[] column_headings =
  {
    "No.", "File", "Code", "Error"
  };

  Vector<PageData> pages = new Vector<PageData>();
  Vector<PageData> unprocessedpages = new Vector<PageData>();
  Hashtable<String,Vector<PageData>> candidatelookup = new Hashtable<String,Vector<PageData>>();



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
        return "ident";
      case 1:
        return page.source;
      case 2:
        return page.code;
      case 3:
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
