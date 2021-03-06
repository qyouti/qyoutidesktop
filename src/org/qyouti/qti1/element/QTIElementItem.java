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

import java.util.*;
import org.qyouti.qti1.*;
import org.qyouti.util.*;

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
  QTIResponse[] response_list = null;

  boolean foranonymouscandidate;
  boolean foridentifiedcandidate;

  public boolean highest_possible_score_known=false;
  public double highest_possible_score=0.0;

  // Indicates that current candidate (whose outcomes are being processed)
  // was given this question so it ought to be included in processing.
  boolean referenced_by_candidate = false;

  @Override
  public void reset()
  {
    referenced_by_candidate = false;
    QTIElementItem qtiitemoverride = qti.getOverrideItem(getIdent());
    if ( qtiitemoverride != null )
      qtiitemoverride.reset();
    super.reset();
  }

  public boolean isOverriden()
  {
    QTIElementItem qtiitemoverride = qti.getOverrideItem(getIdent());
    return qtiitemoverride != null;
  }
  
  public boolean isReferencedByCandidate()
  {
    return referenced_by_candidate;
  }
  public void setReferencedByCandidate()
  {
    referenced_by_candidate = true;
    QTIElementItem qtiitemoverride = qti.getOverrideItem(getIdent());
    if ( qtiitemoverride != null )
      qtiitemoverride.setReferencedByCandidate();
  }

  public String getTitle()
  {
    return domelement.getAttribute( "title" );
  }
  
  public void setTitle( String title )
  {
    domelement.setAttribute( "title", title );
  }

  public QTIElementPresentation getPresentation()
  {
      return presentation;
  }

  public String getTemplateClassName()
  {
    return domelement.getAttribute( "qyouti:template" );
  }


  private void initCandidateType()
  {
    foranonymouscandidate = true;
    foridentifiedcandidate = true;
    String att = this.domelement.getAttributeNS( "http://www.qyouti.org/qtiext", "candidatetype" );
    if ( att == null || att.length() == 0 )
      return;
    if ( "all".equals( att ) ) return;
    if (  "anonymous".equals( att ) ) { foridentifiedcandidate = false; return; }
    if ( "identified".equals( att ) ) {  foranonymouscandidate = false; return; }
    foranonymouscandidate = false;
    foridentifiedcandidate = false;
  }
  
  public boolean isForCandidate( boolean anonymous )
  {
    return anonymous?foranonymouscandidate:foridentifiedcandidate;
  }

  public QTIResponse[] getResponses()
  {
    QTIElementItem qtiitemoverride = qti.getOverrideItem(getIdent());
    if ( qtiitemoverride != null )
      return qtiitemoverride.getResponses();
    return response_list;
  }


  /**
   * Gets value of response specified by ident
   * @param respident The ident of the response
   */
  public Object getResponseValue( String respident )
  {
    QTIElementItem qtiitemoverride = qti.getOverrideItem(getIdent());
    if ( qtiitemoverride != null )
      return qtiitemoverride.getResponseValue( respident );
    
    QTIResponse response = response_table.get( respident );
    if ( response == null )
      throw new IllegalArgumentException( "Unknown response ident " + respident + " in assessment item " + getIdent() + "." );
    return response.getCurrentValue();
  }


//  /**
//   * Sets response of first response_lid to value of
//   * 'Nth' option in that response.  Zero based index.
//   * @param offset
//   */
//  public void setResponseValueByOffset( int offset )
//  {
//    presentation.responselid.setCurrentValueByOffset( offset );
//  }

//  /**
//   * Takes current value of first response_lid and appends
//   * the 'Nth' option in that value.  Zero based index.
//   * @param offset
//   */
//  public void addResponseValueByOffset( int offset )
//  {
//    presentation.responselid.addCurrentValueByOffset( offset );
//  }

  public List<QTIElementResponselabel> getResponselabels()
  {
    QTIElementItem qtiitemoverride = qti.getOverrideItem(getIdent());
    if ( qtiitemoverride != null )
      return qtiitemoverride.getResponselabels();

    if ( presentation == null )
      return null;
    // a deep search so this works with multiple responselids
    Vector<QTIElementResponselabel> responselabels = presentation.findElements( QTIElementResponselabel.class, true );
    return responselabels;
  }

  public QTIElementResponselabel getResponselabelByOffset( int offset )
  {
    QTIElementItem qtiitemoverride = qti.getOverrideItem(getIdent());
    if ( qtiitemoverride != null )
      return qtiitemoverride.getResponselabelByOffset( offset );
    
    if ( presentation == null )
      return null;
    // a deep search so this works with multiple responselids
    Vector<QTIElementResponselabel> responselabels = presentation.findElements( QTIElementResponselabel.class, true );
    if ( offset<0 || offset >= responselabels.size() )
      return null;

    return responselabels.get(offset);
  }

  public List<QTIElementResponselid> getResponselids()
  {
    QTIElementItem qtiitemoverride = qti.getOverrideItem(getIdent());
    if ( qtiitemoverride != null )
      return qtiitemoverride.getResponselids();
    
    if ( presentation == null )
      return null;
    // a deep search so this works with multiple responselids
    Vector<QTIElementResponselid> lids = presentation.findElements( QTIElementResponselid.class, true );
    return lids;
  }


  public boolean containsOutcomeName( String name )
  {
    QTIElementItem qtiitemoverride = qti.getOverrideItem(getIdent());
    if ( qtiitemoverride != null )
      return qtiitemoverride.containsOutcomeName( name );
    
    return resprocessing.outcomes.decvar_table.containsKey( name );
  }


  public String[] getOutcomeNames()
  {
    QTIElementItem qtiitemoverride = qti.getOverrideItem(getIdent());
    if ( qtiitemoverride != null )
      return qtiitemoverride.getOutcomeNames();
    
    String[] names = new String[resprocessing.outcomes.decvar_vector.size()];
    for ( int i=0; i< names.length; i++ )
      names[i] = resprocessing.outcomes.decvar_vector.get( i ).getVarname();
    return names;
  }

  public boolean isOutcomeLikert( String ident )
  {
    QTIElementItem qtiitemoverride = qti.getOverrideItem(getIdent());
    if ( qtiitemoverride != null )
      return qtiitemoverride.isOutcomeLikert( ident );
    
    QTIElementDecvar decvar = resprocessing.outcomes.decvar_table.get( ident );
    if ( decvar == null )
      return false;
    return "true".equalsIgnoreCase(decvar.getAttribute("qyoutilikert") );
  }

  public Object getOutcomeValue( String ident )
  {
    QTIElementItem qtiitemoverride = qti.getOverrideItem(getIdent());
    if ( qtiitemoverride != null )
      return qtiitemoverride.getOutcomeValue( ident );
    
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
    QTIElementItem qtiitemoverride = qti.getOverrideItem(getIdent());
    if ( qtiitemoverride != null )
      return qtiitemoverride.getOutcomeMaximum( ident );
    
    if ( !supported )
      throw new IllegalArgumentException( "Can't calculate outcome maximum in unsupported assessment item " + getIdent() + "." );
    QTIElementDecvar decvar = resprocessing.outcomes.decvar_table.get( ident );
    if ( decvar == null )
      throw new IllegalArgumentException( "Unknown outcome variable " + ident + " in assessment item " + getIdent() + "." );
    return decvar.getMaximumValue();
  }

  public void setOutcome( String ident, Object value )
  {
    QTIElementItem qtiitemoverride = qti.getOverrideItem(getIdent());
    if ( qtiitemoverride != null )
    {
      qtiitemoverride.setOutcome( ident, value );
      return;
    }

    if ( !supported )
      throw new IllegalArgumentException( "Can't calculate outcome in unsupported assessment item " + getIdent() + "." );
    QTIElementDecvar decvar = resprocessing.outcomes.decvar_table.get( ident );
    if ( decvar == null )
      throw new IllegalArgumentException( "Unknown outcome variable " + ident + " in assessment item " + getIdent() + "." );
    decvar.setCurrentValue( value );
  }

  public void computeOutcomes()
  {
    QTIElementItem qtiitemoverride = qti.getOverrideItem(getIdent());
    if ( qtiitemoverride != null )
    {
      qtiitemoverride.computeOutcomes();
      return;
    }
    
    // Check that candidate responses are allowed
    Enumeration<QTIResponse> en = response_table.elements();
    QTIResponse response;
    while ( en.hasMoreElements() )
    {
      response = en.nextElement();
      // Mark as if no response was made if responses disallowed.
      if ( !response.areCurrentResponseValuesAllowed() )
        response.reset();
    }

    if ( !supported )
      throw new IllegalArgumentException( "Can't compute outcomes in unsupported item " + getIdent() + "." );
    resprocessing.computeOutcomes();
  }

  
  /**
   *  Long winded way to work out which (if any) are unambiguously
   * the correct and incorrect answers.  Also works out the highest
   * possible score.
   */
  public void computeCorrectResponses()
  {
    QTIElementItem qtiitemoverride = qti.getOverrideItem(getIdent());
    if ( qtiitemoverride != null )
    {
      qtiitemoverride.computeCorrectResponses();
      return;
    }
    
    int i, j, k;
    MultiBaseInteger permutations;
    int permcount;
    if ( !containsOutcomeName( "SCORE" ) )
      return;
    
    Object outcome = getOutcomeValue( "SCORE" );
    if ( outcome == null || !(outcome instanceof Number) )
      return;

    Number[] outcomes_selected;
    Number[] outcomes_unselected;
    boolean up, down, failed=false;
    boolean maxonly = false;
    String lidident, ident;
    QTIElementResponselabel label;
    QTIElementResponselid lid;
    highest_possible_score = 0.0;
    
    List<QTIElementResponselabel> responselabels = getResponselabels();
    List<QTIElementResponselid> alllids = getResponselids();
    permutations = new MultiBaseInteger(alllids.size());
    
    for ( i=0; i<responselabels.size(); i++ )
    {
      label = responselabels.get(i);
      lid = label.getResponselid();
      permutations.reset();
      // Only calculate max possible mark if correct/incorrect already
      // calculated or if attributes loaded from file.
      if ( label.isCorrect() || label.isIncorrect() )
        maxonly=true;

      up = false;
      down = false;
      lidident = lid.getIdent();
      ident = label.getIdent();
      for ( k=0; k<alllids.size(); k++ )
        permutations.setBase( k, alllids.get( k ).getResponsePermutations( label ) );
      permcount = permutations.intValueMaximum() + 1;
      
      //System.out.println( "permcount = " + permcount );
      outcomes_selected   = new Number[permcount];
      outcomes_unselected = new Number[permcount];
      for ( j=0; j<permcount; j++, permutations.increment() )
      {
        //System.out.println( "permutation = " + j );
        reset();
        for ( k=0; k<alllids.size(); k++ )
          alllids.get( k ).setResponsePermutation( label, permutations.getDigit( k ) );
        computeOutcomes();
        // What is the score with our label unselected and with the current
        // permutation of other selections in the item?
        outcomes_unselected[j] = (Number)getOutcomeValue( "SCORE" );
        // Select our label
        lid.addCurrentValue( label.getIdent() );
        // What is the score now?
        computeOutcomes();
        outcomes_selected[j] = (Number)getOutcomeValue( "SCORE" );
        //System.out.println( "score = " + outcomes_selected[j] );
        // did the score go up, go down or stay the same?
        if ( outcomes_selected[j].doubleValue() > outcomes_unselected[j].doubleValue() )
          up = true;
        if ( outcomes_selected[j].doubleValue() < outcomes_unselected[j].doubleValue() )
          down = true;
        if ( outcomes_selected[j].doubleValue() > highest_possible_score )
          highest_possible_score = outcomes_selected[j].doubleValue();
        if ( outcomes_unselected[j].doubleValue() > highest_possible_score )
          highest_possible_score = outcomes_unselected[j].doubleValue();
      }
      if ( maxonly )
        continue;
      if ( up && down )
      {
        failed = true;
        break;
      }
      if ( up )
        label.setCorrect( true );
    }

    if ( maxonly )
      return;
    
    for ( i=0; i<responselabels.size(); i++ )
    {
      label = responselabels.get(i);
      if ( failed )
      {
        label.setCorrect( false );
        label.setIncorrect( false );
      }
      else
      {
        if ( !label.isCorrect() )
          label.setIncorrect( true );
      }
    }
  }
  
  

  @Override
  public void initialize()
  {
    presentation=null;
    resprocessing=null;
    response_table.clear();
    response_list = null;
    supported = false;
    highest_possible_score_known=false;
    highest_possible_score=0.0;
    
    super.initialize();
    // Let presentation and respprocessing elements check what responses
    // are supports.
    //int rlids=0, rstrs=0;

    
    initCandidateType();
    
    Vector<QTIItemDescendant> desc = findElements( QTIItemDescendant.class, true );
    for ( int i=0; i<desc.size(); i++ )
      desc.get(i).setItem( this );

    Vector<QTIResponse> responses = findElements( QTIResponse.class, true );
    response_list = new QTIResponse[responses.size()];
    for ( int i=0; i<responses.size(); i++ )
    {
      response_table.put( responses.get(i).getIdent(), responses.get(i) );
      response_list[i] = responses.get(i);
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
    if ( presentation.responselids.size() > 0 )
    {
      reset();
      computeCorrectResponses();
      highest_possible_score_known=true;
    }
    
    reset();
  }


  public boolean isStandardMultipleChoice()
  {
    if ( !isSupported() ) return false;
    if ( presentation == null ) return false;
    if ( presentation.responselids.size() != 1) return false;
    return presentation.responselids.get( 0 ).isStandardMultipleChoice();
  }

  public boolean isMultipleChoice()
  {
    if ( !isSupported() ) return false;
    if ( presentation == null ) return false;
    return presentation.isMultipleChoice();
  }

  public int getMultipleChoiceOptionCount()
  {
    if ( !isMultipleChoice() )
      return 0;
    return presentation.getMultipleChoiceOptionCount();
  }

  public boolean isString()
  {
    if ( !isSupported() ) return false;
    if ( presentation == null ) return false;
    return presentation.isString();
  }


  public boolean isSupported()
  {
    return supported;
  }

}
