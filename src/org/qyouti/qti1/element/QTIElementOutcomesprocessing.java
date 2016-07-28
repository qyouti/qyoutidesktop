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
import org.qyouti.qti1.QTIElement;

/**
 *
 * @author jon
 */
public class QTIElementOutcomesprocessing
        extends QTIElement
{
  boolean supported=false;

  QTIElementQuestestinterop root;
  QTIElementOutcomes outcomes;
  Vector<QTIElementObjectscondition> conditions;


  public boolean isSupported()
  {
    return supported;
  }

  public String getScoremodel()
  {
    String a = domelement.getAttribute( "scoremodel" );
    if ( a == null || a.length() == 0 )
      return "WeightedSumOfScores";
    return a;
  }

  @Override
  public void reset()
  {
    if ( outcomes != null )
      outcomes.reset();
  }


  public Object getOutcomeValue( String name )
  {
    QTIElementDecvar scorevar = outcomes.decvar_table.get( name );
    if ( scorevar == null )
      return null;
    return scorevar.getCurrentValue();
  }

  
  
  public void process()
  {
    int i, j, k;
    boolean weightedsum = "WeightedSumOfScores".equals( getScoremodel() );
    if ( !supported )
      throw new IllegalArgumentException( "Unsupported outcomes processing." );

    // ONLY the SCORE outcome is processed here
    // Other processing occurs in QTIElementQuestestinterop.processOutcomes()

    Vector<QTIElementItem> items = root.getItems();
    for ( i=0; i<outcomes.decvar_vector.size(); i++ )
    {
      QTIElementDecvar var = outcomes.decvar_vector.get( i );
      // reset variable to default value
      var.reset();
      // SCORE is handled very differently to other outcomes
      if ( "SCORE".equals( var.getVarname() ) )
      {
        if ( weightedsum )
        {
          // run through all the condition elements
          for ( j=0; j<conditions.size(); j++ )
          {
            // put all the relevant question outcomes through each condition element
            for ( k=0; k<items.size(); k++ )
            {
              if ( items.get( k ).isReferencedByCandidate() )
                conditions.get( j ).process(var, items.get(k) );
            }
          }
        }
        else
        {
          Object itemoutcome;
          Number nitem;
          double sum = 0.0;
          for ( k=0; k<items.size(); k++ )
          {
            if ( items.get( k ).isReferencedByCandidate() )
            {
              if ( items.get( k ).containsOutcomeName( var.getVarname() ) )
              {
                itemoutcome = items.get( k ).getOutcomeValue( var.getVarname() );
                if ( !(itemoutcome instanceof Number) )
                  throw new IllegalArgumentException( "Item score variable must have number value.");
                nitem = (Number)itemoutcome;
                sum += nitem.doubleValue();
              }
            }
          }        
          var.setCurrentValue( sum );
        }
        //System.out.println( "Grand total score: " + scorevar.getCurrentValue() );
      }
      
      // THIS IS DONE IN QUESTTESTINTEROP class
//      else
//      {
//        for ( k=0; k<items.size(); k++ )
//        {
//          // Does this item have an outcome with the same name as this
//          // top level outcomes?
//          if ( items.get( k ).containsOutcomeName( var.getVarname() ) )
//          {
//            // Copy the outcome over
//            var.setCurrentValue( items.get( k ).getOutcomeValue( var.getVarname() ) );
//            // Stop looking if not null
//            if ( var.getCurrentValue() != null )
//              break;
//          }
//        }        
//      }
    }
  }






  @Override
  public void initialize()
  {
    supported = false;
    super.initialize();

    root = null;
    QTIElement next = this;
    while ( next.parent != null )
      next = next.parent;
    if ( next instanceof QTIElementQuestestinterop )
      root = (QTIElementQuestestinterop)next;
    if ( root == null )
      return;

    if ( !"SumOfScores".equals( getScoremodel() ) &&
         !"WeightedSumOfScores".equals( getScoremodel() ) )
      return;

    Vector<QTIElementOutcomes> outcomeses = findElements( QTIElementOutcomes.class, false );
    if ( outcomeses.size() != 1 )
      return;
    outcomes = outcomeses.get( 0 );
    if ( !outcomes.isSupported() )
      return;

    conditions = findElements( QTIElementObjectscondition.class, false );
    for ( int i=0; i<conditions.size(); i++ )
      if ( !conditions.get(i).isSupported() )
        return;


    supported = outcomes.isSupported();
  }


}
