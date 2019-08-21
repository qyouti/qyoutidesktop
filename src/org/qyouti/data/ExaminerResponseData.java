/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti.data;

import java.io.IOException;
import java.io.Writer;
import org.w3c.dom.Element;

/**
 *
 * @author maber01
 */
public class ExaminerResponseData
{
  ExaminerQuestionData question;
  String ident;
  Element element;
  boolean examiner_selected=false;
  
  public ExaminerResponseData( String ident )
  {
    this.ident = ident;
  }
  
  public ExaminerResponseData( Element element )
  {
    this.element = element;
    ident = element.getAttribute( "ident" );
    examiner_selected = "true".equalsIgnoreCase( element.getAttribute( "examiner"  ) );
  }
  
  public void emit( Writer writer )
          throws IOException
  {
    writer.write( "        <response ident=\"" + ident + "\" " );
    writer.write( "examiner=\"" + (examiner_selected?"true":"false") + "\"/>\r\n" );
  }  
}
