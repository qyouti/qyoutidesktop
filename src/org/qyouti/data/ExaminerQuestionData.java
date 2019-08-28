/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti.data;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Hashtable;
import javax.xml.soap.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author maber01
 */
public class ExaminerQuestionData
{
  private int examinerdecision = QuestionData.EXAMINER_DECISION_NONE;  
  private Element element;
  private String ident;

  public HashMap<String,ExaminerResponseData> rmap = new HashMap<>();
  
  public ExaminerQuestionData(ExaminationData exam, Element element)
  {
    this.element = element;
    ident = element.getAttribute("ident");
    String str = element.getAttribute( "decision" );
    try { examinerdecision = Integer.parseInt( str ); }
    catch ( NumberFormatException nfe ) { examinerdecision =  QuestionData.EXAMINER_DECISION_NONE; }
    NodeList nlchildren = element.getChildNodes();
    NodeList nl;
    Element child;
    
    for ( int i=0; i<nlchildren.getLength(); i++ )
    {
      if ( nlchildren.item(i).getNodeType() != Node.ELEMENT_NODE )
        continue;
      child = (Element)nlchildren.item(i);
      if ( "overrides".equals( child.getLocalName() ) )
      {
        nl = child.getElementsByTagName( "response" );
        ExaminerResponseData response;
        for ( int j=0; j<nl.getLength(); j++ )
        {
          response = new ExaminerResponseData( (Element)nl.item( j ) );
          rmap.put(response.ident, response);
        }
      }
    }
  }

  public ExaminerQuestionData( String ident )
  {
    this.ident = ident;
  }

  public String getIdent()
  {
    return ident;
  }

  
  
  public int getExaminerdecision()
  {
    return examinerdecision;
  }

  public void setExaminerdecision(int examinerdecision)
  {
    this.examinerdecision = examinerdecision;
  }    
  
  public void emit( Writer writer )
          throws IOException
  {
    writer.write( "    <question ident=\"" + ident + "\"" );
    writer.write( " decision=\"" + examinerdecision + "\"" );
    writer.write( ">\r\n" );

    if ( this.examinerdecision == QuestionData.EXAMINER_DECISION_OVERRIDE )
    {
      writer.write( "      <overrides>\r\n" );
      for ( ExaminerResponseData erd : rmap.values() )
        erd.emit(writer);
      writer.write( "      </overrides>\r\n" );
    }
    writer.write( "    </question>\r\n" );
  }
  
}
