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
public class QTIElementRespcondition
        extends QTIItemAncestor
{
  boolean supported=false;
  QTIElementConditionvar conditionvar;
  Vector<QTIElementSetvar> setvars;
  Vector<QTIElementDisplayfeedback> displayfeedbacks;

  boolean conditionmet=false;



  public boolean isContinue()
  {
    String a = this.domelement.getAttribute( "continue" );
    // TODO What is default?
    return "yes".equalsIgnoreCase( a );
  }

  public boolean isSupported()
  {
    return true;
  }

  @Override
  public void initialize()
  {
    super.initialize();
    
    supported=false;
    Vector<QTIElementConditionvar> cvars = findElements( QTIElementConditionvar.class, true );
    if ( cvars.size() != 1 )
      return;
    conditionvar = cvars.get( 0 );
    if ( !conditionvar.isSupported() )
      return;

    setvars = findElements( QTIElementSetvar.class, true );
    displayfeedbacks = findElements( QTIElementDisplayfeedback.class, true );
    
    supported=true;
  }


}
