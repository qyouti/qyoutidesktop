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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author maber01
 */
public class ExaminerCandidateData
{
  HashMap<String,ExaminerQuestionData> qmap = new HashMap<>();
  String ident;
  OutcomeData outcomes;

  public ExaminerCandidateData( String ident )
  { 
    this.ident = ident;
  }  
  
  public ExaminerCandidateData(ExaminationData exam,  Element element )
  {
    ident = element.getAttribute("ident");
    NodeList nlchildren = element.getChildNodes();
    NodeList nl;
    Element child;

    for ( int i=0; i<nlchildren.getLength(); i++ )
    {
      if ( nlchildren.item(i).getNodeType() != Node.ELEMENT_NODE )
        continue;
      child = (Element)nlchildren.item(i);


      if ( "questions".equals( child.getLocalName() ) )
      {
        nl = child.getElementsByTagName( "question" );
        ExaminerQuestionData q;

        for ( int j=0; j<nl.getLength(); j++ )
        {
          q = new ExaminerQuestionData( exam, (Element)nl.item( j ) );
          qmap.put(q.getIdent(), q);
        }
      }
      
      if ( "outcomes".equals( child.getLocalName() ) )
      {
        outcomes = new OutcomeData( exam );
        nl = child.getElementsByTagName( "outcome" );
        OutcomeDatum outcome;
        for ( int j=0; j<nl.getLength(); j++ )
        {
          outcome = new OutcomeDatum( (Element)nl.item(j) );
          outcomes.addDatum( outcome );
        }
      }
    }
  }

  public String getIdent()
  {
    return ident;
  }
  
  public void emit( Writer writer )
          throws IOException
  {
    writer.write( "  <candidate ident=\"" + ident + "\">\r\n" );
    writer.write( "    <questions>\r\n" );
    for ( ExaminerQuestionData q : qmap.values() )
      q.emit(writer);
    writer.write( "    </questions>\r\n" );
    
    writer.write( "    <outcomes>\r\n" );
    for ( int i=0; i<outcomes.getRowCount(); i++ )
      outcomes.getDatumAt( i ).emit( writer );
    writer.write( "    </outcomes>\r\n" );
    writer.write( "  </candidate>\r\n" );
  }  
  
}
