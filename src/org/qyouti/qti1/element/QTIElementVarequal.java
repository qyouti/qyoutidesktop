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

import org.qyouti.qti1.QTIVarcondition;

/**
 *
 * @author jon
 */
public class QTIElementVarequal
        extends QTIVarcondition
{
  
  public boolean isConditionMet()
  {
    Object varvalue = getResponse();

    if ( varvalue == null ) return false;

    if ( varvalue instanceof String )
    {
      String strvalue = (String)varvalue;
      if ( isCaseSensitive() )
        return strvalue.equals( textcontent );
      return strvalue.equalsIgnoreCase( textcontent );
    }

    if ( varvalue instanceof String[] )
    {
      String[] strarrayvalue = (String[])varvalue;
      if ( strarrayvalue.length == 0 )
        return false;

      for ( int i=0; i<strarrayvalue.length; i++)
      {
        if ( isCaseSensitive() )
        {
          if ( strarrayvalue[i].equals( textcontent ) )
            return true;
        }
        else
        {
          if ( strarrayvalue[i].equalsIgnoreCase( textcontent ) )
            return true;
        }
      }
      return false;
    }

    throw new IllegalArgumentException( "Can't handle variables of type " + varvalue.getClass() );
  }
}
