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
import org.qyouti.qti1.QTIObjectsTest;

/**
 *
 * @author jon
 */
public class QTIElementAndobjects
        extends QTIObjectsTest
{
  boolean supported;
  QTIObjectsTest nested_a;
  QTIObjectsTest nested_b;

  @Override
  public boolean isSupported()
  {
    return supported;
  }

  @Override
  public boolean isConditionMet(QTIElementItem item)
  {
    if ( !supported )
      throw new UnsupportedOperationException("Processing not supported.");
    return nested_a.isConditionMet(item) && nested_b.isConditionMet(item);
  }

  @Override
  public void initialize()
  {
    supported = false;
    super.initialize();

    Vector<QTIObjectsTest> conditiontests = findElements( QTIObjectsTest.class, false );
    if ( conditiontests.size() != 2 )
      return;

    nested_a = conditiontests.get( 0 );
    nested_b = conditiontests.get( 1 );

    supported = nested_a.isSupported() && nested_b.isSupported();
  }

}
