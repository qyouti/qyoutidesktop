/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti.data;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author maber01
 */
public class ScannedPageData
{
  private ExaminationData exam;
  private String ident;
  private String candidateident;
  boolean processed;
  private double dpi=300.0;
  private ArrayList<ScannedQuestionData> questions = new ArrayList<>();

  public ScannedPageData(ExaminationData exam, String ident)
  {
    this.exam = exam;
    this.ident = ident;
    candidateident = exam.getCandidateIdentFromPage(ident);
  }
  
  public ScannedPageData(ExaminationData exam, Element element)
  {
    this.exam = exam;
    this.processed = "true".equalsIgnoreCase( element.getAttribute( "processed" ) );
    this.ident = element.getAttribute( "pageid" );
    String strdpi = element.getAttribute("dpi");
    try
    {
      this.dpi = Double.parseDouble(strdpi);
    }
    catch ( NumberFormatException nfe )
    {
      this.dpi = 300.0;
    }
    candidateident = exam.getCandidateIdentFromPage(ident);
    NodeList nl = element.getElementsByTagName( "question" );
    for ( int j=0; j<nl.getLength(); j++ )
      questions.add( new ScannedQuestionData( exam, ident, candidateident, (Element)nl.item( j ) ) );
  }  

  public double getDpi()
  {
    return dpi;
  }

  public void setDpi(double dpi)
  {
    this.dpi = dpi;
  }

  
        
  public String getIdent()
  {
    return ident;
  }

  public boolean isProcessed()
  {
    return processed;
  }

  public void setProcessed(boolean processed)
  {
    this.processed = processed;
  }
  
  
  
  public List<ScannedQuestionData> getQuestions()
  {
    return questions;
  }
  
  public void clearQuestions()
  {
    questions.clear();
  }
    
  public void addQuestion( ScannedQuestionData q )
  {
    questions.add(q);
  }
  
  public void emit( Writer writer )
          throws IOException
  {
    writer.write( "    <page " );
    writer.write( "pageid=\"" + ident           + "\" " );
    writer.write( "scanned=\"" );
    writer.write( "\" processed=\"" );
    writer.write( processed?"true":"false" );
    writer.write( "\" dpi=\"" + dpi + "\"");
    writer.write( ">\r\n" );
    
    for ( int i=0; i<questions.size(); i++ )
      questions.get( i ).emit( writer );

    writer.write( "    </page>\r\n" );
  }
  
}
