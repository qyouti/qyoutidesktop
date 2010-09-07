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

import org.qyouti.qti1.*;

/**
 *
 * @author jon
 */
public class QTIElementSetvar
        extends QTIItemAncestor
{

  public String getVarName()
  {
    String vn = domelement.getAttribute( "varname" );
    if ( vn != null && vn.length() > 0 ) return vn;

    // Those sons of bitches at Blackboard decided to use
    // the attribute name 'variablename' for no god damned
    // reason so this code is required:
    return domelement.getAttribute( "variablename" );
  }

  public void process()
  {
    String action = domelement.getAttribute( "action" );
    if ( action == null ) action = "Set";
    action = action.toLowerCase();
    String varname = getVarName();
    if ( varname == null ) varname = "SCORE";
    String newvalue = domelement.getTextContent();


    // Have those tossers at Blackboard thrown another spanner in the works?
    String sourcevarname;
    if ( newvalue.endsWith( ".max" ) )
    {
      sourcevarname = newvalue.substring( 0, newvalue.length() - 4 );
      newvalue = getItem().getOutcomeMaximum( sourcevarname );
    }

    if ( !"set".equals( action ) && !"add".equals( action ))
      throw new IllegalArgumentException( "Setvar actions other than set and add are not yet implemented." );

    Object currentvalue = getItem().getOutcome( varname );
    if ( currentvalue == null )
      throw new IllegalArgumentException( "Setvar can't process null outcome." );
    if ( currentvalue instanceof String )
    {
      if ( "set".equals( action ) )
        getItem().setOutcome( varname, newvalue );
      if ( "add".equals( action ) )
        getItem().setOutcome( varname, currentvalue + newvalue );
      return;
    }
    if ( currentvalue instanceof Integer )
    {
      Integer icurrentvalue = (Integer)currentvalue;
      Integer ivalue = new Integer( newvalue );
      if ( "set".equals( action ) )
        getItem().setOutcome( varname, ivalue );
      if ( "add".equals( action ) )
        getItem().setOutcome( varname, new Integer( ivalue.intValue() + icurrentvalue.intValue() ) );
      return;
    }
    if ( currentvalue instanceof Double )
    {
      Double dcurrentvalue = (Double)currentvalue;
      Double dvalue = new Double(newvalue);
      if ( "set".equals( action ) )
        getItem().setOutcome( varname, dvalue );
      if ( "add".equals( action ) )
        getItem().setOutcome( varname, new Double( dvalue.doubleValue() + dcurrentvalue.doubleValue() ) );
      return;
    }

    throw new IllegalArgumentException( "Setvar can't process outcome of type " + currentvalue.getClass() );
  }
}
