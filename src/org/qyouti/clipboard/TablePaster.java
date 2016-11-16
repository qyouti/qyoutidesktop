package org.qyouti.clipboard;

import au.com.bytecode.opencsv.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.datatransfer.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import javax.swing.event.*;
import javax.swing.table.*;

/**
 * ExcelAdapter enables Copy-Paste Clipboard functionality on JTables. The
 * clipboard data format used by the adapter is compatible with the clipboard
 * format used by Excel. This provides for clipboard interoperability between
 * enabled JTables and Excel.
 */
public class TablePaster
        extends AbstractTableModel
{

  private String value;
  private Clipboard system;
  private StringSelection stsel;
  private String delimiter="\t";

  private int cols = 0;

  private String rawdata = "";
  private java.util.List<String[]> rowlist = new ArrayList<String[]>();
  private final ArrayList<TableModelListener> listeners = new ArrayList<>();
  private final ArrayList<String> columnnames = new ArrayList<>();

  /**
   * The Excel Adapter is constructed with a JTable on which it enables
   * Copy-Paste and acts as a Clipboard listener.
   */
  public TablePaster()
  {
    system = Toolkit.getDefaultToolkit().getSystemClipboard();
  }

  public String getDelimiter()
  {
    return delimiter;
  }

  public void setDelimiter( String delimiter )
  {
    this.delimiter = delimiter;
  }

  
  
  public void clear()
  {
    rowlist = new ArrayList<String[]>();
    columnnames.clear();
    cols=0;
    fireTableStructureChanged();
  }
  
  
  /**
   * This method is activated on the Keystrokes we are listening to in this
   * implementation. Here it listens for Copy and Paste ActionCommands.
   * Selections comprising non-adjacent cells result in invalid selection and
   * then copy action cannot be performed. Paste is done by aligning the upper
   * left corner of the selection with the 1st element in the current selection
   * of the JTable.
   */
  public void doPaste()
  {
    System.out.println( "Trying to Paste" );

    try
    {
      rawdata = (String) (system.getContents( this ).
              getTransferData( DataFlavor.stringFlavor ));
      System.out.println( "String is:" + rawdata );
      parse();
    }
    catch ( Exception ex )
    {
      ex.printStackTrace();
      return;
    }
  }

  public void parse()
  {
    int i, j;
    System.out.println( "Parsing" );

    cols=0;
    columnnames.clear();

    StringReader reader = new StringReader( rawdata );
    CSVReader csvreader = new CSVReader( reader, delimiter.charAt( 0 ) );
    try
    {
      rowlist = csvreader.readAll();
    }
    catch ( IOException ex )
    {
      Logger.getLogger( TablePaster.class.getName() ).log( Level.SEVERE, null, ex );
      rowlist = new ArrayList<String[]>();
    }
    
    for ( i=0; i<rowlist.size(); i++ )
      if ( ((String[])rowlist.get( i )).length > cols )
        cols = ((String[])rowlist.get( i )).length;
    
    for ( j=0; j<cols; j++ )
    {
      columnnames.add( "" );
      this.setColumnName( j, null );
    }
    
    fireTableStructureChanged();
  }

  @Override
  public int getRowCount()
  {
    return rowlist.size();
  }

  @Override
  public int getColumnCount()
  {
    return cols;
  }

  @Override
  public String getColumnName( int columnIndex )
  {
    return columnnames.get( columnIndex );
  }

  public void setColumnName( int columnIndex, String value )
  {
    if ( value == null )
      columnnames.set( columnIndex, Integer.toString( columnIndex + 1 ) );
    else
      columnnames.set( columnIndex, value );
  }

  @Override
  public Class<?> getColumnClass( int columnIndex )
  {
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
    String[] row;
    if ( rowIndex<0 || rowIndex>= rowlist.size() )
      return null;
    row = (String[])rowlist.get( rowIndex );
    if ( columnIndex<0 || columnIndex>=row.length )
      return null;
    return row[columnIndex];
  }

  @Override
  public void setValueAt( Object aValue, int rowIndex, int columnIndex )
  {
    return;
  }

}
