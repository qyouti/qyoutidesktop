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
public class ScanData extends AbstractTableModel
{
  private final ExaminationData exam;
  private final ArrayList<ImageFileData> scans = new ArrayList<>();
  private final ArrayList<ScannedPageData> pages = new ArrayList<>();
  private final HashMap<String,ScannedPageData> pagesbyid = new HashMap<>();

  boolean unsaved=false;
  
  public ScanData( ExaminationData exam )
  {
    this.exam = exam;
  }
  
  public boolean areThereUnsavedChanges()
  {
    return unsaved;
  }
  
  public void setUnsavedChanges( boolean b )
  {
    unsaved = b;
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
  
  public void clearScanFileData()
  {
    int size = scans.size();
    scans.clear();
    exam.processRowsInserted( this, 0, size );
  }

  
  public void clearScannedPageData()
  {
    pages.clear();
    pagesbyid.clear();
  }

  public void markQuestionScansUnprocessed()
  {
    for ( ScannedPageData page : pages )
      for ( ScannedQuestionData question : page.getQuestions() )
      {
        for ( ScannedResponseData response : question.responsedatas )
          response.reset();
        question.imagesprocessed = false;
        question.needsreview = false;
      }
  }
  
  
  public void addScannedPageData( ScannedPageData page )
  {
    pages.add(page);
    pagesbyid.put( page.getIdent(), page );
  }
  
  public List<ScannedPageData> getScannedPageDataList()
  {
    return pages;
  }
  
  public ScannedPageData getScannedPageData( String pageident )
  {
    return pagesbyid.get( pageident );
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
