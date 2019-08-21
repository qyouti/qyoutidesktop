/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti.data;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author maber01
 */
public class ExaminerData
{
  HashMap<String,ExaminerCandidateData> cmap = new HashMap<>();
  boolean unsaved = false;
  
  public ExaminerData()
  { 
  }  
  
  public ExaminerData( ExaminationData exam, Element element )
  {
    NodeList nl = element.getElementsByTagName( "candidate" );
    ExaminerCandidateData c;

    for ( int j=0; j<nl.getLength(); j++ )
    {
      c = new ExaminerCandidateData( exam, (Element)nl.item( j ) );
      cmap.put(c.getIdent(), c);
    }
  }
  
  public boolean areThereUnsavedChanges()
  {
    return unsaved;
  }
  
  public void setUnsavedChanges( boolean b )
  {
    unsaved = b;
  }
  
  public void emit( Writer writer )
          throws IOException
  {
    writer.write( "<examinerdata>\r\n" );
    for ( ExaminerCandidateData c : cmap.values() )
      c.emit(writer);
    writer.write( "</examinerdata>\r\n" );
    unsaved = false;
  }  
  
  
}
