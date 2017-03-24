/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti.data;

import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;

/**
 *
 * @author jon
 */
public class QuestionAnalysisTable extends AbstractTableModel
{
  ExaminationData exam;
  int selection = -1;
  String ident=null;
  List<QuestionAnalysis> analyses;

  static String[] columnnames = 
  {
    "Question                        ",
    "Option",
    "True/False",
    "No. Selected",
    "No. Not Selected",
    "%Class Right",
    "Median Diff in Aptitude",
    "Lower 90% Limit",
    "Upper 90% Limit"
  };
  
  public QuestionAnalysisTable( ExaminationData exam, List<QuestionAnalysis> analyses )
  {
    this.exam = exam;
    this.analyses = analyses;
  }
  
  public void setSelectedQuestion( String ident )
  {
    this.ident = ident;
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
    exam.processDataChanged( this );
  }
  
  public String getSelectedQuestion()
  {
     return ident;
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

  /**
   * Sets properties on a JLabel for the particular data point
   * 
   * @param label
   * @param rowIndex
   * @param columnIndex 
   */
  public void setValueProperties( JLabel label, int rowIndex, int columnIndex )
  {
    if ( selection < 0 )
      return;
    QuestionAnalysis qa = analyses.get( selection );
    ResponseAnalysis ra = qa.response_analyses.get( rowIndex );
    label.setOpaque( true );
    label.setBackground( Color.WHITE );
    switch ( columnIndex )
    {
      case 2:
      case 3:
      case 4:
      case 5:
        if ( ra.correct )
          label.setBackground( Color.GREEN );
        break;
      case 6:
      case 7:
      case 8:
        if ( Double.isNaN( ra.median_difference ) )
          label.setBackground( Color.GRAY );          
        else if ( ra.median_difference_lower >= 0.0 )
          label.setBackground( Color.GREEN );
        else if ( ra.median_difference_upper <= 0.0 )
          label.setBackground( Color.RED );
        else
          label.setBackground( Color.PINK );
        break;
    }      
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
        return ra.correct?ra.right:ra.wrong;
      case 4:
        return ra.correct?ra.wrong:ra.right;
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
