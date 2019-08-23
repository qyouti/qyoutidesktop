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

/**
 *
 * @author jon
 */
public class QTIElementQuestestinterop
        extends QTIElement
{
  Vector<QTIElementItem> items=null;
  Hashtable<String,QTIElementItem> item_table = new Hashtable<String,QTIElementItem>();

  // Actual declared processing sections...
  Vector<QTIElementOutcomesprocessing> outcomesprocessings=null;

  // references to decvars in multiple outcomesprocessing sections
  // plus additional implicit decvars that result from individual questions
  Hashtable<String,QTIElementDecvar> decvar_table = new Hashtable<String,QTIElementDecvar>();

  HashMap<String, QTIExternalMap> external_maps = new HashMap<>();

  QTIElementAssessment assessment=null;

  QTIElementQuestestinterop override=null;
  
  @Override
  public void initialize()
  {
    item_table.clear();
    outcomesprocessings=null;
    decvar_table.clear();
    super.initialize();

    Vector<QTIElementAssessment> assessments = findElements( QTIElementAssessment.class, false );
    if ( assessments !=null && assessments.size() != 0 )
      assessment = assessments.get(0);

    items = findElements( org.qyouti.qti1.element.QTIElementItem.class, true );
    for ( int i=0; i<items.size(); i++ )
      item_table.put( items.get(i).getIdent(), items.get(i) );

    outcomesprocessings = findElements( QTIElementOutcomesprocessing.class, true );
    Vector<QTIElementDecvar> decvars;

    // get references to outcome variable declarations from the start
    // these are declared up front....
    for ( int i=0; i<outcomesprocessings.size(); i++ )
    {
      decvars = outcomesprocessings.get( i ).findElements( QTIElementDecvar.class, true );
      for ( int j=0; j<decvars.size(); j++ )
      {
        if ( decvar_table.containsKey( decvars.get( j ).getVarname() ) )
          decvars.get( j ).duplicate = true;
        else
          decvar_table.put( decvars.get( j ).getVarname(), decvars.get( j ) );
      }
    }


  }
  
  public void addExternalMap( QTIExternalMap exmap )
  {
    external_maps.put( exmap.getExternalMapName(), exmap );
  }
  
  public QTIExternalMap getExternalMap( String name )
  {
    return external_maps.get( name );
  }  
  

//  public QTIElementOutcomesprocessing getOutcomesprocessing()
//  {
//    return outcomesprocessing;
//  }

  public Vector<QTIElementItem> getItems()
  {
    return items;
  }

  public QTIElementItem getItem( String ident )
  {
    return item_table.get( ident );
  }

  public void setOverride( QTIElementQuestestinterop override )
  {
    this.override = override;
  }
  
  public QTIElementItem getOverrideItem( String ident )
  {
    if ( override == null )
      return null;
    return override.getItem( ident );
  }

  public QTIElementMaterial getAssessmentMaterial()
  {
    if ( assessment == null ) return null;
    return assessment.getMaterial();
  }

  public void processOutcomes()
  {
    int i;

    // remove item only decvars from previous processing runs
    String names[] = getOutcomeNames();
    QTIElementDecvar var;
    for ( i=0; i<names.length; i++ )
    {
    var = decvar_table.get( names[i] );
    if ( var != null && var.getItem() != null )
      decvar_table.remove( names[i] );
    }

    // get decvars from the items that we will pull up into the assessment
    // level outcomes
    Vector<QTIElementDecvar> decvars;
    QTIElementItem item;
    for ( i=0; i<items.size(); i++ )
    {
      item = items.get( i );
      // skip items that the candidate wasn't given
      if ( !item.isReferencedByCandidate() )
        continue;
      decvars = item.findElements( QTIElementDecvar.class, true );
      for ( int j=0; j<decvars.size(); j++ )
      {
        // only add item decvars if they haven't already appeared in
        // outcomesprocessing.  Also skip "SCORE" which is implicit if not
        // declared in an outcomesprocessing element
        if ( "SCORE".equals( decvars.get( j ).getVarname() ) )
          continue;
        if ( !decvar_table.containsKey( decvars.get( j ).getVarname() ) )
          decvar_table.put( decvars.get( j ).getVarname(), decvars.get( j ) );
      }
    }

    // Now SCORE can be processed
    for ( i=0; i<outcomesprocessings.size(); i++ )
      outcomesprocessings.get( i ).reset();
    
    for ( i=0; i<outcomesprocessings.size(); i++ )
      outcomesprocessings.get( i ).process();
  }

  public String[] getOutcomeNames()
  {
    String[] names = new String[decvar_table.size()];
    return decvar_table.keySet().toArray( names );
  }

  public Object getOutcomeValue( String name )
  {
    QTIElementDecvar var = decvar_table.get( name );
    if ( var == null )
      return null;
    return var.getCurrentValue();
  }
}
