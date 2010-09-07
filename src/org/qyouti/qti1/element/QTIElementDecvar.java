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
public class QTIElementDecvar
        extends QTIItemAncestor
{
  Object current;
  boolean supported=false;

  public Object getCurrentValue()
  {
    return current;
  }

  public void setCurrentValue( Object v )
  {
    if ( v instanceof String ||
            v instanceof Integer ||
            v instanceof Double )
      current = v;
  }

  public String getMaximumValue()
  {
    return domelement.getAttribute( "maxvalue" );
  }

  
  public String getVarname()
  {
    String a=domelement.getAttribute( "varname" );
    if ( a == null ) return "SCORE";
    return a;
  }

  public String getVartype()
  {
    String a=domelement.getAttribute( "vartype" );
    if ( a == null ) return "integer";
    return a.toLowerCase();
  }


  public void applyLimits()
  {
    String vartype = getVartype();
    if ( !"integer".equals( vartype ) && !"decimal".equals( vartype ) )
      return;

    String amin = domelement.getAttribute( "minvalue" );
    String amax = domelement.getAttribute( "maxvalue" );

    if ( "integer".equals( vartype ) )
    {
      int min = Integer.parseInt( amin );
      int max = Integer.parseInt( amax );
      int v = ((Integer)current).intValue();
      if ( v<min ) v = min;
      if ( v>max ) v = max;
      current = new Integer( v );
    }

    if ( "decimal".equals( vartype ) )
    {
      double min = Integer.parseInt( amin );
      double max = Integer.parseInt( amax );
      double v = ((Double)current).intValue();
      if ( v<min ) v = min;
      if ( v>max ) v = max;
      current = new Double( v );
    }
  }


  public boolean isSupported()
  {
    return supported;
  }


  @Override
  public void reset()
  {
    String vartype = getVartype();
    String def = domelement.getAttribute( "defaultval" );
    if ( def != null && def.length()==0 )
      def = null;
    if ( "string".equals( vartype ) )
    {
      if ( def == null )
        current = "";
      else
        current = def;
    }
    if ( "integer".equals( vartype ) )
    {
      if ( def == null )
        current = new Integer( 0 );
      else
        try { current = new Integer( def ); } catch ( NumberFormatException e ) { return; }
    }
    if ( "decimal".equals( vartype ) )
    {
      if ( def == null )
        current = new Double( 0.0 );
      else
        try { current = new Double( def ); } catch ( NumberFormatException e ) { return; }
    }

    super.reset();
  }


  @Override
  public void initialize()
  {
    super.initialize();

    supported = false;
    String vartype = getVartype();
    if ( !"string".equals( vartype ) &&
            !"integer".equals( vartype ) &&
            !"decimal".equals( vartype ) )
      return;

    reset();

    supported = true;
  }
  
}
