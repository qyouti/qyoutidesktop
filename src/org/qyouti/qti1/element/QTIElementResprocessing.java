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
import org.qyouti.qti1.*;

/**
 *
 * @author jon
 */
public class QTIElementResprocessing
        extends QTIItemDescendant
{
  boolean supported=false;
  QTIElementOutcomes outcomes;
  Vector<QTIElementRespcondition> conditions;
  Vector<QTIItemproc> itemprocs;

  public boolean isSupported()
  {
    return supported;
  }

  public String getScoremodel()
  {
    String a = domelement.getAttribute( "scoremodel" );
    if ( a == null || a.length() == 0 )
      return "SumOfScores";
    return a;
  }

  public boolean isAllOrNothing()
  {
    return "AllorNothing".equalsIgnoreCase( getScoremodel() );
  }


  public void computeOutcomes()
  {
    int i;
    QTIElementRespcondition respcondition;
    QTIElementConditionvar conditionvar;
    QTIElementSetvar setvar;
    QTIItemproc itemproc;


    // special case for WEBCT 'all or nothing' scheme
    // ignore all the respconditions and simply award maximum score
    // if all correct and no incorrect statements are selected.
    boolean allornothing = getItem().highest_possible_score_known && isAllOrNothing();

    for ( i=0; i<conditions.size(); i++ )
    {
      respcondition = conditions.get(i);
      conditionvar = respcondition.conditionvar;
      if ( conditionvar.condition.isConditionMet() )
      {
        for ( int j=0; j<respcondition.setvars.size(); j++ )
        {
          setvar = respcondition.setvars.get(j);
          setvar.process();
        }
        if ( !respcondition.isContinue() )
          break;
      }
    }

    // after all other outcomes calculated the usual way
    // recompute the "SCORE" outcome if implementing the WebCT
    // AllOrNothing algorithm.
    if ( allornothing )
    {
      System.out.println( "Implementing WebCT all or nothing algorithm." );
      Vector<QTIElementResponselid> responselids = getItem().findElements( QTIElementResponselid.class, true );
      if ( responselids.size() == 0 )
        throw new IllegalArgumentException( "All or nothing item must have one or more response_lid elements." );
      boolean perfect = true;
      for ( i=0; i<responselids.size(); i++ )
        if ( !responselids.get( i ).isResponsePerfect() )
        {
          perfect = false;
          break;
        }
      Object score_outcome = getItem().getOutcomeValue( "SCORE" );
      if ( perfect )
      {
        System.out.println( "Perfect response." );
        getItem().setOutcome(
                "SCORE",
                (score_outcome instanceof Double)?
                  (new Double(getItem().highest_possible_score)):
                  (new Integer( (int)Math.ceil(getItem().highest_possible_score))));
      }
      else
      {
        System.out.println( "Not perfect response." );
        getItem().setOutcome(
                "SCORE",
                (score_outcome instanceof Double)?
                  (new Double(0.0)):
                  (new Integer(0))
                  );
      }
    }
    
    for ( i=0; i<itemprocs.size(); i++ )
    {
      itemproc = itemprocs.get(i);
      itemproc.computeOutcomes();
    }
  }


  @Override
  public void initialize()
  {
    supported = false;
    outcomes = null;
    conditions = null;
    itemprocs = null;
    
    super.initialize();

    Vector<QTIElementOutcomes> outcomeses = findElements( QTIElementOutcomes.class, false );
    if ( outcomeses.size() != 1 )
      return;
    outcomes = outcomeses.get( 0 );
    if ( !outcomes.isSupported() )
      return;

    conditions = findElements( QTIElementRespcondition.class, false );
    for ( int i=0; i<conditions.size(); i++ )
      if ( !conditions.get(i).isSupported() )
        return;

    itemprocs = findElements( QTIItemproc.class, false );
    for ( int i=0; i<itemprocs.size(); i++ )
      if ( !itemprocs.get(i).isSupported() )
        return;

    supported = outcomes.isSupported();
  }

}
