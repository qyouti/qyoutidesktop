/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti.data;

import java.util.*;
import javax.swing.table.*;

/**
 *
 * @author jon
 */
public class ImageFileTable extends AbstractTableModel
{
  private ExaminationData exam;
  private ArrayList<ImageFileData> scans = new ArrayList<ImageFileData>();

  public ImageFileTable( ExaminationData exam )
  {
    this.exam = exam;
  }

  public void add( ImageFileData data )
  {
    scans.add( data );
    exam.processRowsInserted( this, scans.size()-1, scans.size()-1 );
  }
  
  public int size()
  {
    return scans.size();
  }
  
  public ImageFileData get( int i )
  {
    return scans.get( i );
  }
  
  public void clear()
  {
    int size = scans.size();
    scans.clear();
    exam.processRowsInserted( this, 0, size );
  }
  
  @Override
  public int getRowCount()
  {
    return scans.size();
  }

  @Override
  public int getColumnCount()
  {
    return 5;
  }

  @Override
  public Object getValueAt( int rowIndex, int columnIndex )
  {
    ImageFileData data = scans.get( rowIndex );
    switch ( columnIndex )
    {
      case 0:
        return data.getIdent();
      case 1:
        return data.getSource();
      case 2:
        return data.getImportedname();
      case 3:
        return new Boolean( data.isProcessed() );
      case 4:
        return data.getError();
    }
    return null;
  }

  @Override
  public String getColumnName( int column )
  {
    switch ( column )
    {
      case 0:
        return "Ident";
      case 1:
        return "Source";
      case 2:
        return "Name";
      case 3:
        return "Processed";
      case 4:
        return "Error";
    }
    return null;
  }
  
}
