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
public class QTIResponseUnsupported
        extends QTIResponse
{
  public boolean isSupported() { return false; }

  @Override
  public Object getCurrentValue()
  {
    throw new UnsupportedOperationException("Not supported.");
  }

  @Override
  public void setCurrentValue(Object value)
  {
    throw new UnsupportedOperationException("Unsupported response type - can't set candidate value.");
  }

  @Override
  public int getResponsePermutations( String ident )
  {
    return -1;
  }

  @Override
  public void setResponsePermutation( String ident, int perm)
  {
    throw new UnsupportedOperationException( "setResponsePermutation unsupported in this response type");
  }

  @Override
  public boolean areResponsesAllowed()
  {
    return false;
  }
}
