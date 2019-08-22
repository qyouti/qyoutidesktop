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
public class QTIElementNot
        extends QTINestingCondition
{
  boolean supported=false;
  QTICondition nested_condition=null;

  @Override
  public boolean isConditionMet()
  {
    return !nested_condition.isConditionMet();
  }

  @Override
  public void initialize()
  {
    supported=false;
    nested_condition=null;
    
    super.initialize();

    supported=false;

    Vector<QTICondition> list = this.findElements( QTICondition.class, false );
    if ( list.size() != 1 )
      return;
    if ( list.get(0) instanceof QTIConditionUnsupported )
      return;
    nested_condition = list.get(0);
    
    supported=true;
  }

}
