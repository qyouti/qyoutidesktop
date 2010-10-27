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

import java.util.Vector;
import org.qyouti.qti1.QTIResponse;


/**
 *
 * @author jon
 */
public class QTIElementResponselid
        extends QTIResponse
{
  boolean supported = false;
  String[] current;

  QTIElementRenderchoice renderchoice;

  public double highest_possible_score=0.0;

  public boolean isSupported()
  {
    return supported;
  }

  public boolean isStandardMultipleChoice()
  {
    if ( !isSupported() ) return false;
    return "single".equalsIgnoreCase( getCardinality() );
  }

  public Object getCurrentValue()
  {
    return current;
  }

  @Override
  public void reset()
  {
    current = new String[0];
  }

  public String getCardinality()
  {
    String cardinality = domelement.getAttribute( "rcardinality" );
    if ( cardinality == null )
      cardinality = "single";
    return cardinality.toLowerCase();
  }


  public QTIElementResponselabel getResponselabelByOffset( int offset )
  {
    if ( offset < 0 || offset >= renderchoice.responselabels.size() )
      return null;
    return renderchoice.responselabels.get( offset );
  }


  @Override
  public void setCurrentValue(Object value)
  {
    if ( !(value instanceof String[]) )
      throw new IllegalArgumentException( "Attempt to set response LID to non-String array value." );

    String[] newcurrent = (String[])value;
    //if ( newcurrent.length>1 && "single".equals( getCardinality() ) )
    //  throw new IllegalArgumentException( "Attempt to set 'single' response LID to list of items." );

    for ( int i=0; i<newcurrent.length; i++ )
    {
      if ( !renderchoice.containsResponseIdent( newcurrent[i] ) )
        throw new IllegalArgumentException( "Attempt to set response LID to value not available in choices." );
    }

    // passed all checks so go ahead and set it!
    current = newcurrent;
  }


  public void setCurrentValueByOffset( int offset )
  {
    Vector<QTIElementResponselabel> responselabels =
            findElements( QTIElementResponselabel.class, true );
    if ( offset >= responselabels.size() )
      throw new IllegalArgumentException( "Attempting to set response to unavailable option." );

    String decodedvalue = responselabels.get( offset ).getIdent();
    setCurrentValue( new String[] {decodedvalue} );
  }

  public void addCurrentValueByOffset( int offset )
  {
    Vector<QTIElementResponselabel> responselabels =
            findElements( QTIElementResponselabel.class, true );
    if ( offset >= responselabels.size() )
      throw new IllegalArgumentException( "Attempting to set response to unavailable option." );

    String decodedvalue = responselabels.get( offset ).getIdent();
    String[] newcurrent;
    if ( current != null )
    {
      for ( int i=0; i<current.length; i++ )
        if ( current[i].equals( decodedvalue ) )
          return;  // already added to response list
      newcurrent = new String[current.length+1];
      for ( int i=0; i<current.length; i++ )
        newcurrent[i] = current[i];
      newcurrent[current.length] = decodedvalue;
      setCurrentValue( newcurrent );
      return;
    }
    setCurrentValue( new String[] {decodedvalue} );
  }


  public boolean isResponsePerfect()
  {
    Vector<QTIElementResponselabel> responselabels =
            findElements( QTIElementResponselabel.class, true );
    QTIElementResponselabel label;

    if ( current == null )
      return false;

    boolean answered;
    for ( int i=0; i<responselabels.size(); i++ )
    {
      label = renderchoice.responselabels.get( i );
      answered = false;
      for ( int j=0; j<current.length; j++ )
        if ( current[j].equals( label.getIdent() ) )
        {
          answered = true;
          break;
        }

      if ( label.isCorrect() && !answered )
        return false;
      if ( label.isIncorrect() && answered )
        return false;
    }
    
    return true;
  }


  public int getResponsePermutations( String ident )
  {
    String card = getCardinality();
    if ( "single".equals( card ) )
      return 1;
    return 1 << (renderchoice.responselabels.size() - 1);
  }

  public void setResponsePermutation( String ident, int perm )
  {
    String card = getCardinality();
    int n = renderchoice.responselabels.size();
    getItem().reset();

    if ( "single".equals( card ) )
      return;

    // where is the chosen response label?
    int offset;
    for ( offset=0; offset<renderchoice.responselabels.size(); offset++ )
      if ( ident.equals( renderchoice.responselabels.get( offset ).getIdent() ) )
        break;
    if ( offset == renderchoice.responselabels.size() )
      throw new UnsupportedOperationException( "Unknown response label ident.");

    // bit twiddle the perm to make gap for selected ident
    int twiddledperm = perm;
    int maskhi, masklo;
    if ( offset < (renderchoice.responselabels.size()-1) )
    {
      masklo = (1 << offset)-1;
      maskhi = ~masklo;
      twiddledperm = (perm & masklo) | ((perm & maskhi) << 1);
    }

    for ( offset=0; offset<renderchoice.responselabels.size(); offset++ )
      if ( ((1 << offset) & twiddledperm) != 0 )
        addCurrentValueByOffset( offset );
  }



  /**
   *  Long winded way to work out which (if any) are unambiguously
   * the correct and incorrect answers.  Also works out the highest
   * possible score.
   */
  public void computeCorrectResponses()
  {
    int i, j, permcount, perm;
    Object outcome = getItem().getOutcome( "SCORE" );
    if ( outcome == null || !(outcome instanceof Number) )
      return;

    Number[] outcomes_selected;
    Number[] outcomes_unselected;
    boolean up, down, failed=false;
    boolean maxonly = false;
    String ident;
    highest_possible_score = 0.0;
    for ( i=0; i<renderchoice.responselabels.size(); i++ )
    {
      // Only calculate max possible mark if correct/incorrect already
      // calculated or if attributes loaded from file.
      if ( renderchoice.responselabels.get(i).isCorrect() ||
           renderchoice.responselabels.get(i).isIncorrect() )
        maxonly=true;

      up = false;
      down = false;
      ident = renderchoice.responselabels.get( i ).getIdent();
      permcount = getResponsePermutations( ident );
      //System.out.println( "permcount = " + permcount );
      outcomes_selected   = new Number[permcount];
      outcomes_unselected = new Number[permcount];
      for ( j=0; j<permcount; j++ )
      {
        //System.out.println( "permutation = " + j );
        getItem().reset();
        setResponsePermutation( ident, j );
        //for ( int k=0; k<current.length; k++ )
        //{
        //  System.out.println( current[k] );
        //}
        getItem().computeOutcomes();
        outcomes_unselected[j] = (Number)getItem().getOutcome( "SCORE" );
        //System.out.println( "score = " + outcomes_unselected[j] );
        setCurrentValueByOffset( i );
        getItem().computeOutcomes();
        outcomes_selected[j] = (Number)getItem().getOutcome( "SCORE" );
        //System.out.println( "score = " + outcomes_selected[j] );
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
        renderchoice.responselabels.get( i ).setCorrect( true );
    }

    if ( maxonly )
      return;
    
    for ( i=0; i<renderchoice.responselabels.size(); i++ )
    {
      if ( failed )
      {
        renderchoice.responselabels.get( i ).setCorrect( false );
        renderchoice.responselabels.get( i ).setIncorrect( false );
      }
      else
      {
        if ( !renderchoice.responselabels.get( i ).isCorrect() )
          renderchoice.responselabels.get( i ).setIncorrect( true );
      }
    }
  }


  @Override
  public void initialize()
  {
    super.initialize();

    supported = false;
    if ( "ordered".equals( getCardinality() ) )
      return;

    Vector<QTIElementRenderchoice> choices = findElements( QTIElementRenderchoice.class, true );
    if ( choices.size() != 1 )
      return;
    renderchoice = choices.get(0);

    supported = true;
  }


  @Override
  public boolean areResponsesAllowed()
  {
    // is the response given an allowable response according to
    // the definition of the item?
    // If only one answer allowed and user has given multiple
    // answers, this is not an allowed response.
    if ( "single".equals( getCardinality() ) &&
            current != null &&
            current.length > 1 )
      return false;
    return true;
  }

  
  
}
