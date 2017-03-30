/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.qyouti.data;

import java.util.*;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author jon
 */
public class PersonListModel extends AbstractTableModel
{
  ExaminationData exam;
  
  private static final String[] column_headings =
  {
    "Name", "ID", "Excluded", "Anonymous", "Special Needs"
  };

  ArrayList<PersonData> persons = new ArrayList<>();

  public PersonListModel( ExaminationData exam, ArrayList<PersonData> data )
  {
    this.exam = exam;
    persons = data;
  }
  
  @Override
  public int getRowCount()
  {
    return persons.size();
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
    if ( columnIndex > 1 ) return Boolean.class;
    return String.class;
  }

  @Override
  public boolean isCellEditable( int rowIndex, int columnIndex )
  {
    String lastprintid = exam.getLastPrintID();    
    if ( lastprintid != null && lastprintid.length() != 0 )
      return false;
    PersonData person = persons.get( rowIndex );
    // can't set anonymous or excluded if preferences are set
    if ( person != null && person.getPreferences() != null )
      return columnIndex == 0;
    return columnIndex == 0 || columnIndex == 2 || columnIndex == 3;
  }

  @Override
  public Object getValueAt( int rowIndex, int columnIndex )
  {
    PersonData person = persons.get( rowIndex );
    switch( columnIndex )
    {
      case 0:
        return person.getName();
      case 1:
        return person.getId();
      case 2:
        return Boolean.valueOf( person.isExcluded() );
      case 3:
        return Boolean.valueOf( person.isAnonymous() );
      case 4:
        return Boolean.valueOf( person.getPreferences() != null );
    }
    return null;
  }

  @Override
  public void setValueAt( Object aValue, int rowIndex, int columnIndex )
  {
    PersonData person = persons.get( rowIndex );
    switch( columnIndex )
    {
      case 0:
        person.setName( aValue.toString() );
        break;
      case 1:
        return;
      case 2:
        person.setExcluded( ((Boolean)aValue).booleanValue() );
        break;
      case 3:
        person.setAnonymous( ((Boolean)aValue).booleanValue() );
        break;
      case 4:
        return;
    }
    fireTableCellUpdated( rowIndex, columnIndex );
  }

}
