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


  public boolean isSupported()
  {
    return supported;
  }

  public boolean isSingleChoice()
  {
    return "single".equalsIgnoreCase( getCardinality() );
  }

  public boolean isStandardMultipleChoice()
  {
    if ( !isSupported() ) return false;
    return "single".equalsIgnoreCase( getCardinality() );
  }

  public boolean isMultipleChoice()
  {
    if ( !isSupported() ) return false;
    return true;
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

  public QTIElementResponselabel getResponseLabel( String ident )
  {
    for ( int i=0; i<renderchoice.responselabels.size(); i++ )
      if ( ident == renderchoice.responselabels.get( i ).getIdent() )
        return renderchoice.responselabels.get( i );
    return null;
  }
  
  public QTIElementResponselabel[] getResponseLabels()
  {
    QTIElementResponselabel[] a = new QTIElementResponselabel[renderchoice.responselabels.size()];
    return renderchoice.responselabels.toArray( a );
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


//  public void setCurrentValueByOffset( int offset )
//  {
//    QTIElementResponselabel responselabel = renderchoice.responselabels.get( offset );
//    if ( responselabel == null )
//      throw new IllegalArgumentException( "Attempting to set response to unavailable option." );
//    setCurrentValue( new String[] {responselabel.getIdent()} );
//  }

  private void addCurrentValueByOffset( int offset )
  {
    QTIElementResponselabel responselabel = renderchoice.responselabels.get( offset );
    if ( responselabel == null )
      throw new IllegalArgumentException( "Attempting to set response to unavailable option." );
    addCurrentValue( responselabel.getIdent() );
  }

  public void addCurrentValue( String ident )
  {
    String[] newcurrent;
    if ( current != null )
    {
      for ( int i=0; i<current.length; i++ )
        if ( current[i].equals( ident ) )
          return;  // already added to response list
      newcurrent = new String[current.length+1];
      for ( int i=0; i<current.length; i++ )
        newcurrent[i] = current[i];
      newcurrent[current.length] = ident;
      setCurrentValue( newcurrent );
      return;
    }
    setCurrentValue( new String[] {ident} );
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

  public boolean containsResponselabel( QTIElementResponselabel label )
  {
    return renderchoice.responselabels.contains( label );
  }
  
  /**
   * How many permutations does this LID have, perhaps with
   * one option fixed.
   * 
   * @param minusone Is one option in this LID fixed?
   * @return 
   */
  public int getResponsePermutations( QTIElementResponselabel label )
  {
    String card = getCardinality();
    // if the stated fixed label is in this LID then there are no
    // permutations.
    if ( "single".equals( card ) )
      return (label.lid == this)?1:renderchoice.responselabels.size();
    return 1 << (renderchoice.responselabels.size() - (label.lid == this?1:0));
  }

  public void setResponsePermutation( QTIElementResponselabel label, int perm )
  {
    String card = getCardinality();
    int i;
    int n = renderchoice.responselabels.size();
    ArrayList<Boolean> pattern = new ArrayList<Boolean>();
    reset();

    // if the stated fixed label is in this LID then there aren't multiple 
    // permutations
    if ( "single".equals( card ) && label.lid == this )
      return;

    for ( i=0; i<n; i++ )
      pattern.add( false );
    
    if ( "single".equals( card ) )
    {
      if ( perm != 0 )
        pattern.set( 1 << (perm-1), true );
    }
    else
    {
      for ( i=0; i<n; i++ )
        if ( ((perm >> i) & 1) == 1 )
          pattern.set( i, true );
    }
    
    // where is the chosen response label?
    if ( label != null )
      for ( i=0; i<n; i++ )
        if ( label == renderchoice.responselabels.get( i ) )
          pattern.add( n, false );

    for ( i=0; i<n; i++ )
      if ( pattern.get( i ) )
        addCurrentValueByOffset( i );
  }


  @Override
  public void initialize()
  {
    supported = false;
    renderchoice = null;
    
    super.initialize();

    if ( "ordered".equals( getCardinality() ) )
      return;

    Vector<QTIElementRenderchoice> choices = findElements( QTIElementRenderchoice.class, true );
    if ( choices.size() != 1 )
      return;
    renderchoice = choices.get(0);

    supported = true;
  }

  @Override
  public int getResponsePartCount()
  {
    if ( renderchoice == null )
      return 0;
    if ( renderchoice.responselabels == null )
      return 0;
    return renderchoice.responselabels.size();
  }

  @Override
  public boolean areCurrentResponseValuesAllowed()
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
