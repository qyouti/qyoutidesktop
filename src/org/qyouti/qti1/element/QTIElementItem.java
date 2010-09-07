/*
 *
 * Copyright 2010 Leeds Metropolitan University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may 
 * not use this file except in compliance with the License. You may obtain 
 * a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 *
 *
 */



/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.qyouti.qti1.element;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import org.qyouti.qti1.*;

/**
 *
 * @author jon
 */
public class QTIElementItem
        extends QTIElement
{
  boolean supported = false;
  QTIElementPresentation presentation;
  QTIElementResprocessing resprocessing;
  Hashtable<String,QTIResponse> response_table = new Hashtable<String,QTIResponse>();

  public boolean highest_possible_score_known=false;
  public double highest_possible_score=0.0;

  public String getTitle()
  {
    return domelement.getAttribute( "title" );
  }

  public QTIElementPresentation getPresentation()
  {
      return presentation;
  }

  /**
   * Sets response of first response_lid to value of
   * 'Nth' option in that response.  Zero based index.
   * @param offset
   */
  public void setResponseByOffset( int offset )
  {
    Vector<QTIElementResponselid> responselids = findElements( QTIElementResponselid.class, true );
    if ( responselids.size() != 1 )
      throw new IllegalArgumentException( "Item must have exactly one response_lid element." );
    responselids.get(0).setCurrentValueByOffset( offset );
  }

  public void addResponseByOffset( int offset )
  {
    Vector<QTIElementResponselid> responselids = findElements( QTIElementResponselid.class, true );
    if ( responselids.size() != 1 )
      throw new IllegalArgumentException( "Item must have exactly one response_lid element." );
    responselids.get(0).addCurrentValueByOffset( offset );
  }


  public QTIElementResponselabel getResponselabelByOffset( int offset )
  {
    Vector<QTIElementResponselid> responselids = findElements( QTIElementResponselid.class, true );
    if ( responselids.size() != 1 )
      throw new IllegalArgumentException( "Item must have exactly one response_lid element." );
    return responselids.get(0).getResponselabelByOffset( offset );
  }


  public void setResponse( String respident, Object respvalue )
  {
    QTIResponse response = response_table.get( respident );
    if ( response == null )
      throw new IllegalArgumentException( "Unknown response ident " + respident + " in assessment item " + getIdent() + "." );
    response.setCurrentValue( respvalue );
  }

  public Object getResponse( String respident )
  {
    QTIResponse response = response_table.get( respident );
    if ( response == null )
      throw new IllegalArgumentException( "Unknown response ident " + respident + " in assessment item " + getIdent() + "." );
    return response.getCurrentValue();
  }


  public String[] getOutcomeNames()
  {
    String[] names = new String[resprocessing.outcomes.decvar_vector.size()];
    for ( int i=0; i< names.length; i++ )
      names[i] = resprocessing.outcomes.decvar_vector.get( i ).getVarname();
    return names;
  }

  public Object getOutcome( String ident )
  {
    if ( !supported )
    {
      if ( "SCORE".equalsIgnoreCase( ident ) )
          return new Integer( 0 );
      throw new IllegalArgumentException( "Can't calculate outcome in unsupported assessment item " + getIdent() + "." );
    }
    QTIElementDecvar decvar = resprocessing.outcomes.decvar_table.get( ident );
    if ( decvar == null )
      throw new IllegalArgumentException( "Unknown outcome variable " + ident + " in assessment item " + getIdent() + "." );
    return decvar.getCurrentValue();
  }

  public String getOutcomeMaximum( String ident )
  {
    if ( !supported )
      throw new IllegalArgumentException( "Can't calculate outcome maximum in unsupported assessment item " + getIdent() + "." );
    QTIElementDecvar decvar = resprocessing.outcomes.decvar_table.get( ident );
    if ( decvar == null )
      throw new IllegalArgumentException( "Unknown outcome variable " + ident + " in assessment item " + getIdent() + "." );
    return decvar.getMaximumValue();
  }

  public void setOutcome( String ident, Object value )
  {
    if ( !supported )
      throw new IllegalArgumentException( "Can't calculate outcome in unsupported assessment item " + getIdent() + "." );
    QTIElementDecvar decvar = resprocessing.outcomes.decvar_table.get( ident );
    if ( decvar == null )
      throw new IllegalArgumentException( "Unknown outcome variable " + ident + " in assessment item " + getIdent() + "." );
    decvar.setCurrentValue( value );
  }

  public void computeOutcomes()
  {
    // Check that candidate responses are allowed
    Enumeration<QTIResponse> en = response_table.elements();
    QTIResponse response;
    while ( en.hasMoreElements() )
    {
      response = en.nextElement();
      // Mark as if no response was made if responses disallowed.
      if ( !response.areResponsesAllowed() )
        response.reset();
    }

    if ( !supported )
      throw new IllegalArgumentException( "Can't compute outcomes in unsupported item " + getIdent() + "." );
    resprocessing.computeOutcomes();
  }


  @Override
  public void initialize()
  {
    super.initialize();

    supported = false;

    Vector<QTIItemAncestor> desc = findElements( QTIItemAncestor.class, true );
    for ( int i=0; i<desc.size(); i++ )
      desc.get(i).setItem( this );

    Vector<QTIResponse> responses = findElements( QTIResponse.class, true );
    for ( int i=0; i<responses.size(); i++ )
    {
      if ( responses.get(i) instanceof QTIResponseUnsupported )
        return;
      response_table.put( responses.get(i).getIdent(), responses.get(i) );
    }

    
    Vector<QTIElementPresentation> presentations = findElements( QTIElementPresentation.class, true );
    if ( presentations.size() == 1 )
      presentation = presentations.get( 0 );
    else
      presentation = null;

    Vector<QTIElementResprocessing> resps = findElements( QTIElementResprocessing.class, true );
    if ( resps.size() == 1 )
      resprocessing = resps.get( 0 );
    else
      resprocessing = null;



    if ( presentation != null && resprocessing != null )
    {
      supported = presentation.isSupported() && resprocessing.isSupported();
    }

    if ( !supported )
      return;

    // now determine correct statements in multi choice.
    Vector<QTIElementResponselid> responselids = findElements( QTIElementResponselid.class, true );
    if ( responselids.size() != 1 )
      return;

    reset();
    responselids.get(0).computeCorrectResponses();
    highest_possible_score_known=true;
    highest_possible_score=responselids.get(0).highest_possible_score;
    reset();
  }


  public boolean isStandardMultipleChoice()
  {
    if ( !isSupported() ) return false;
    if ( presentation == null ) return false;
    return presentation.isStandardMultipleChoice();
  }


  public boolean isSupported()
  {
    return supported;
  }

}
