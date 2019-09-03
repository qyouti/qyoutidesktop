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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author maber01
 */
public class OutcomeTables
{
  HashMap<String,OutcomeCandidateData> cmap = new HashMap<>();
  
  boolean unsaved=false;

  public OutcomeTables()
  {
  }
  
  public void invalidateCandidate( String ident )
  {
    OutcomeCandidateData ocd = cmap.get(ident);
    if ( ocd != null ) ocd.setValid( false );
  }
  
  public void invalidateAllCandidates()
  {
    for ( OutcomeCandidateData ocd : cmap.values() )
      ocd.setValid( false );    
  }
  
  public OutcomeTables( ExaminationData exam, Element root )
  {
    NodeList nlchildren = root.getChildNodes();
    NodeList nl, nlq;
    Element child, grandchild;
    String ident, qident;

    if ( !"outcomes".equals(root.getLocalName()) )
      return;
    
    for ( int i=0; i<nlchildren.getLength(); i++ )
    {
      if ( nlchildren.item(i).getNodeType() != Node.ELEMENT_NODE )
        continue;
      child = (Element)nlchildren.item(i);

      if ( "candidate".equals( child.getLocalName() ) )
      {
        ident = child.getAttribute("ident");
        OutcomeCandidateData ocd = new OutcomeCandidateData( exam, ident );
        cmap.put(ident, ocd);
        nl = child.getChildNodes();
        OutcomeDatum outcome;
        for ( int j=0; j<nl.getLength(); j++ )
        {
          if ( nl.item(j).getNodeType() != Node.ELEMENT_NODE )
            continue;
          grandchild = (Element)nl.item(j);
          if ( "outcome".equals(grandchild.getLocalName()))
          {
            outcome = new OutcomeDatum( grandchild );
            ocd.addDatum( outcome );
          }
        }
        
        nl = child.getElementsByTagName( "question" );
        for ( int j=0; j<nl.getLength(); j++ )
        {
          qident = ((Element)nl.item(j)).getAttribute("ident");
          OutcomeData od = new OutcomeData( exam );
          ocd.addQuestionOutcomeData(qident, od);
          nlq = nl.item(j).getChildNodes();
          OutcomeDatum qoutcome;
          for ( int k=0; k<nlq.getLength(); k++ )
          {
            if ( nlq.item(k).getNodeType() != Node.ELEMENT_NODE )
              continue;
            qoutcome = new OutcomeDatum( (Element)nlq.item(k) );
            od.addDatum( qoutcome );
          }
        }        
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
  
  
  public void clearNonFixedOutcomes()
  {
    for ( OutcomeCandidateData ocd : cmap.values() )
      ocd.clearNonFixedOutcomes();
  }
  
  public void emit( Writer writer )
          throws IOException, TransformerException
  {
    writer.write( "<outcomes>\r\n" );
    for ( String cident : cmap.keySet() )
    {
      OutcomeCandidateData c = cmap.get(cident);
      writer.write( "  <candidate ident=\"" + cident + "\">\r\n" );
      for ( int i=0; i<c.getRowCount(); i++ )
      {
        writer.write( "    " );
        c.getDatumAt(i).emit( writer );
      }
      for ( String qident : c.getIDSet() )
      {
        OutcomeData q = c.getQuestionOutcomeData(qident);
        writer.write( "    <question ident=\"" + qident + "\">\r\n" );
        for ( int i=0; i<q.getRowCount(); i++ )
        {
          writer.write( "      " );
          q.getDatumAt(i).emit( writer );
        }
        writer.write( "    </question>\r\n" );
      }
      writer.write( "  </candidate>\r\n" );
    }
    writer.write( "</outcomes>\r\n" );
    unsaved = false;
  }  
  
}
