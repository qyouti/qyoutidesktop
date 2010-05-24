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

import java.util.StringTokenizer;
import org.qyouti.qti1.*;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;

/**
 *
 * @author jon
 */
public class QTIElementResponselabel
         extends QTIItemAncestor
{
  boolean supported = false;

  boolean correct = false;
  boolean incorrect = false;


  public void setCorrectAttribute()
  {
    if ( correct && !incorrect )
      domelement.setAttributeNS("http://www.qyouti.org/qtiext", "qyouti:correct", "true" );
    else if ( !correct && incorrect )
      domelement.setAttributeNS("http://www.qyouti.org/qtiext", "qyouti:correct", "false" );
    else
      domelement.setAttributeNS("http://www.qyouti.org/qtiext", "qyouti:correct", "unknown" );
  }


  public void setCorrect( boolean b )
  {
    correct = b;
    setCorrectAttribute();
  }

  public void setIncorrect( boolean b )
  {
    incorrect = b;
    setCorrectAttribute();
  }

  public boolean isCorrect()
  {
    return correct;
  }

  public boolean isIncorrect()
  {
    return incorrect;
  }

  public boolean isSupported()
  {
    return supported;
  }

  public boolean getRshuffle()
  {
    String a = domelement.getAttribute( "rshuffle" );
    if ( a == null ) a = "Yes";
    return "yes".equalsIgnoreCase(a);
  }
  
  public String[] getMatchgroup()
  {
    String a = domelement.getAttribute( "match_group" );
    if ( a == null ) return new String[0];
    StringTokenizer tok = new StringTokenizer( a, "," );
    String[] result = new String[tok.countTokens()];
    for ( int i=0; tok.hasMoreTokens(); i++ )
      result[i] = tok.nextToken();
    return result;
  }

  public int getMatchmax()
  {
    String a = domelement.getAttribute( "match_group" );
    if ( a == null ) return -1;  // indicates no limit to matches
    int n;
    try
    {
      n = Integer.parseInt(a);
      return n;
    }
    catch ( Exception e )
    {
    }
    return -1;
  }

  @Override
  public void initialize()
  {
    super.initialize();

    supported = false;

    if ( getMatchgroup().length > 0 )
      return;

    if ( getMatchmax() != -1 )
      return;

    supported = true;

    correct = false;
    incorrect = false;
    String correctattribute = domelement.getAttribute( "qyouti:correct" );
    correct = "true".equals( correctattribute );
    incorrect = "false".equals( correctattribute );
  }

  public void toString( StringBuffer buffer, int indent )
  {
    for ( int i=0; i<indent; i++ )
      buffer.append( " " );
    buffer.append( getName() );
    if ( isCorrect() ) buffer.append( "    CORRECT" );
    if ( isIncorrect() ) buffer.append( "    INCORRECT" );
    buffer.append( "\n" );
    for ( int c=0; c<children.size(); c++ )
      children.get( c ).toString( buffer, indent+4 );
  }
  
}
