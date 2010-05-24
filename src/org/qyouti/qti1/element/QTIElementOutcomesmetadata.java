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

import org.qyouti.qti1.QTIObjectsTest;

/**
 *
 * @author jon
 */
public class QTIElementOutcomesmetadata
        extends QTIObjectsTest

{

  public String getMdname()
  {
    String a = domelement.getAttribute( "mdname" );
    if ( a == null )
      return "";
    return a;
  }

  public String getMdoperator()
  {
    String a = domelement.getAttribute( "mdoperator" );
    if ( a == null || a.length() == 0 )
      return "EQ";
    return a;
  }

  public String getContent()
  {
    return domelement.getTextContent();
  }

  @Override
  public boolean isSupported()
  {
    return "ident".equals( getMdname() ) && "EQ".equals(getMdoperator());
  }

  @Override
  public boolean isConditionMet( QTIElementItem item )
  {
    if ( !isSupported() )
      throw new IllegalArgumentException( "This outcomes_metadata element not supported." );

    return item.getIdent().equals( getContent() );
  }
  
}
