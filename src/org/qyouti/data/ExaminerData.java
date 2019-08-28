/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti.data;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import javax.xml.transform.TransformerException;
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
  public QuestionDefinitions examinerqdefs=null;
  
  public ExaminerData( ExaminationData exam, Element element )
  {
    NodeList nla = element.getElementsByTagNameNS("http://www.imsglobal.org/xsd/ims_qtiasiv1p2", "questestinterop" );
    if ( nla.getLength() == 1 )
      examinerqdefs = new QuestionDefinitions( (Element)nla.item(0), null );
    
    nla = element.getElementsByTagName( "candidates" );
    if ( nla.getLength() == 1 )
    {
      Element e = (Element)nla.item(0);
      NodeList nl = element.getElementsByTagName( "candidate" );
      ExaminerCandidateData c;
      for ( int j=0; j<nl.getLength(); j++ )
      {
        c = new ExaminerCandidateData( exam, (Element)nl.item( j ) );
        cmap.put(c.getIdent(), c);
      }
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
          throws IOException, TransformerException
  {
    writer.write( "<examinerdata>\r\n" );
    examinerqdefs.emit(writer);
    writer.write("\r\n\r\n<candidates>\r\n");    
    for ( ExaminerCandidateData c : cmap.values() )
      c.emit(writer);
    writer.write("</candidates>\r\n");
    writer.write( "</examinerdata>\r\n" );
    unsaved = false;
  }  
  
  
}
