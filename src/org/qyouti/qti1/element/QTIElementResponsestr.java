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
import org.qyouti.qti1.QTIResponseUnsupported;

/**
 *
 * @author jon
 */
public class QTIElementResponsestr
        extends QTIResponse
{
  QTIElementRenderfib renderfib;
  boolean supported = false;
  
  String[] current;


  @Override
  public boolean isSupported()
  {
    return supported;
  }

  @Override
  public Object getCurrentValue()
  {
    return current;
  }

  @Override
  public void setCurrentValue(Object value)
  {
    if ( !(value instanceof String[]) )
      throw new IllegalArgumentException( "Attempt to set response string to non-String array value." );

    String[] newcurrent = (String[])value;

    // passed all checks so go ahead and set it!
    current = newcurrent;
  }

  @Override
  public boolean areCurrentResponseValuesAllowed()
  {
    // any response is an allowed response right now but in
    // the future this might check that numerical input is numerical etc.
    return true;
  }




    @Override
  public void initialize()
  {
    super.initialize();

    supported = false;

    Vector<QTIElementRenderfib> fibs = findElements( QTIElementRenderfib.class, true );
    if ( fibs.size() != 1 )
      return;

    renderfib = fibs.get(0);

    supported = true;
  }


  @Override
  public void reset()
  {
    current = new String[0];
  }

  
}
