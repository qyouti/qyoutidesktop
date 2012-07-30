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

package org.qyouti.qti1;

/**
 *
 * @author jon
 */
public abstract class QTIResponse
        extends QTIItemDescendant
{
  Object current;

  abstract public boolean isSupported();
  abstract public Object getCurrentValue();
  abstract public void setCurrentValue( Object value );

  abstract public boolean areCurrentResponseValuesAllowed();

  public int getResponsePartCount()
  {
    return 0;
  }

  public boolean isStandardMultipleChoice()
  {
    return false;
  }

  public boolean isMultipleChoice()
  {
    return false;
  }


  
  //abstract public int getResponsePermutations( String ident );
  //abstract public void setResponsePermutation( String ident, int perm );
}
