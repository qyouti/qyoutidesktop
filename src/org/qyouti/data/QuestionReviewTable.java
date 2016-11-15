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
public class QuestionReviewTable
                extends AbstractTableModel
{
  class QuestionReviewItem
  {
    CandidateData candidatedata;
    QuestionData questiondata;
  }
  
  ArrayList<QuestionReviewItem> list = new ArrayList<QuestionReviewItem>();
  
  public void clear()
  {
    if ( list.size() == 0 ) return;
    
    int n = list.size()-1;
    list.clear();
    fireTableRowsDeleted( 0, n );
  }
  
  public void add( CandidateData candidatedata, QuestionData questiondata )
  {
    QuestionReviewItem item = new QuestionReviewItem();
    item.candidatedata = candidatedata;
    item.questiondata = questiondata;
    list.add( item );
    fireTableRowsInserted( list.size()-1, list.size()-1 );
  }
  
  public CandidateData getCandidateData( int i )
  {
    return list.get( i ).candidatedata;
  }
  
  public QuestionData getQuestionData( int i )
  {
    return list.get( i ).questiondata;
  }
  
  @Override
  public int getRowCount()
  {
    return list.size();
  }

  @Override
  public int getColumnCount()
  {
    return 3;
  }

  @Override
  public String getColumnName( int column )
  {
    switch ( column )
    {
      case 0:
        return "Candidate ID";
      case 1:
        return "Question ID";
      case 2:
        return "Review Status";
    }
    return null;
  }

  
  
  @Override
  public Object getValueAt( int rowIndex, int columnIndex )
  {
    QuestionReviewItem item = list.get( rowIndex );
    switch ( columnIndex )
    {
      case 0:
        return item.candidatedata.id;
      case 1:
        return item.questiondata.ident;
      case 2:
        switch ( item.questiondata.getExaminerDecision() )
        {
          case QuestionData.EXAMINER_DECISION_NONE:
            return "No review decision.";
          case QuestionData.EXAMINER_DECISION_OVERRIDE:
            return "Responses overridden by examiner";
          case QuestionData.EXAMINER_DECISION_STAND:
            return "Examiner decided responses will stand";
        }        
        return "Status no. " + item.questiondata.getExaminerDecision();
    }
    return null;
  }
  
}
