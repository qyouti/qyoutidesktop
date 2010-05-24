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

import java.util.Hashtable;
import java.util.Vector;
import org.qyouti.qti1.QTIElement;
import org.qyouti.qti1.QTIObjectsTest;

/**
 *
 * @author jon
 */
public class QTIElementObjectscondition
        extends QTIElement
{
  boolean supported=false;
  QTIObjectsTest conditiontest;
  Hashtable<String,QTIElementObjectsparameter> objectsparameters =
          new Hashtable<String,QTIElementObjectsparameter>();
  
  public boolean isSupported()
  {
    return supported;
  }

  public void process( QTIElementDecvar decvar, QTIElementItem item )
  {
    if ( !supported )
      throw new IllegalArgumentException( "This objects condition not supported.");
    if ( conditiontest.isConditionMet(item) )
    {
      //System.out.println( "Condition met, doing some processing here." );
      Object itemoutcome = item.getOutcome( decvar.getVarname() );
      if ( !(itemoutcome instanceof Number) )
        throw new IllegalArgumentException( "Score variable must have number value.");
      Object sectionoutcome = decvar.getCurrentValue();
      if ( !(sectionoutcome instanceof Number) )
        throw new IllegalArgumentException( "Score variable must have number value.");

      double wt;
      QTIElementObjectsparameter wtparam = objectsparameters.get( "qmd_weighting" );
      if ( wtparam == null )
        wt = 1.0;
      else
        wt = Double.parseDouble( wtparam.getContent() );

      Number nitem = (Number) itemoutcome;
      Number nsect = (Number) sectionoutcome;
      double running_score = nsect.doubleValue() + nitem.doubleValue() * wt;
      decvar.setCurrentValue( running_score );
    }
    //else
    //  System.out.println( "Condition NOT met so do nowt." );
    
  }

  @Override
  public void initialize()
  {
    supported = false;
    super.initialize();

    Vector<QTIObjectsTest> conditiontests = findElements( QTIObjectsTest.class, false );
    if ( conditiontests.size() != 1 )
      return;
    conditiontest = conditiontests.get( 0 );
    if ( !conditiontest.isSupported() )
      return;

    Vector<QTIElementObjectsparameter> list = findElements( QTIElementObjectsparameter.class, false );
    for ( int i=0; i<list.size(); i++ )
      objectsparameters.put( list.get(i).getPname(), list.get(i) );

    supported = true;
  }

}
