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
public class QuestionAnalysisTable extends AbstractTableModel
{
  int selection = -1;
  List<QuestionAnalysis> analyses;

  static String[] columnnames = 
  {
    "Question",
    "Option",
    "True/False",
    "No. Students Right",
    "No. Students Wrong",
    "%Class Right",
    "Median Aptitude Difference",
    "Lower 90% Limit",
    "Upper 90% Limit"
  };
  
  public QuestionAnalysisTable( List<QuestionAnalysis> analyses )
  {
    this.analyses = analyses;
  }
  
  public void setSelectedQuestion( String ident )
  {
    selection = -1;
    if ( ident != null )
    {
      for ( int i=0; i<analyses.size(); i++ )
        if ( ident.equals( analyses.get( i ).ident ) )
        {
          selection = i;
          break;
        }
    }
    this.fireTableDataChanged();
  }
  
  @Override
  public int getRowCount()
  {
    if ( selection < 0 )
      return 1;
    return analyses.get( selection ).response_analyses.size();
  }

  @Override
  public int getColumnCount()
  {
    return 9;
  }

  @Override
  public Object getValueAt( int rowIndex, int columnIndex )
  {
    if ( selection < 0 )
    {
      if ( rowIndex == 0 && columnIndex == 0 )
        return "No analysis";
      return "";
    }
    
    QuestionAnalysis qa = analyses.get( selection );
    ResponseAnalysis ra = qa.response_analyses.get( rowIndex );
    switch ( columnIndex )
    {
      case 0:
        if ( rowIndex == 0 ) return qa.ident;
        return "";
      case 1:
        return ra.ident;
      case 2:
        return ra.correct?"T":"F";
      case 3:
        return ra.right;
      case 4:
        return ra.wrong;
      case 5:
        return Integer.toString( Math.round( 100.0f * (float)ra.right / (float)(ra.right + ra.wrong) ) ) + "%";
      case 6:
        return Double.toString( ra.median_difference );
      case 7:
        return Double.toString( ra.median_difference_lower );
      case 8:
        return Double.toString( ra.median_difference_upper );
    }
    
    return ".";
  }

  @Override
  public Class<?> getColumnClass( int columnIndex )
  {
    return String.class;
  }

  @Override
  public String getColumnName( int column )
  {
    return columnnames[column];
  }
  
}
