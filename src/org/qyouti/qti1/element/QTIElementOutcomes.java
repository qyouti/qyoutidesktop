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
import org.qyouti.qti1.*;

/**
 *
 * @author jon
 */
public class QTIElementOutcomes
        extends QTIItemDescendant
{
  boolean supported = false;
  Vector<QTIElementDecvar> decvar_vector;
  Hashtable<String,QTIElementDecvar> decvar_table = new Hashtable<String,QTIElementDecvar>();

  // These are for outcome variables that aren't declared in the outcomes processing section
  // but may pop up in items anyway.  So, when a candidate's repsonses are processed this
  // table is emptied and may or may not fill with outcomes that were declared in the
  // items' resprocessing sections
  Hashtable<String,QTIElementDecvar> additional_decvar_table = new Hashtable<String,QTIElementDecvar>();


  public boolean isSupported()
  {
    return supported;
  }

  @Override
  public void initialize()
  {
    supported = false;
    super.initialize();

    decvar_vector = findElements( QTIElementDecvar.class, true );
    if ( decvar_vector.size() == 0 )
      return;
    
    for ( int i=0; i<decvar_vector.size(); i++ )
    {
      if ( !decvar_vector.get(i).isSupported() )
        return;
      if ( decvar_table.containsKey( decvar_vector.get(i).getVarname() ) )
        return;
      decvar_table.put( decvar_vector.get(i).getVarname(), decvar_vector.get(i) );
    }

    supported = true;
  }

}
