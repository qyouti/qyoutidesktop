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

import java.util.Vector;
import org.w3c.dom.*;


/**
 *
 * @author jon
 */
public abstract class QTIElement
{
  protected Element domelement;
  protected Vector<QTIElement> children = new Vector<QTIElement>();
  public QTIElement parent = null;

  // Only package classes can instantiate
  public QTIElement()
  {
  }

  void setParent( QTIElement parent )
  {
    this.parent = parent;
  }

  void appendChild( QTIElement child )
  {
    children.add( child );
  }

  public final String getName()
  {
    Class c = getClass();
    String cn = c.getName();
    return cn.substring( cn.lastIndexOf( ".QTIElement" )+11 );
  }

  public final String getElementName()
  {
    if ( domelement == null )
        return null;
    return domelement.getTagName();
  }


  public void initialize()
  {
    for ( int i=0; i<children.size(); i++ )
      children.get( i ).initialize();
    reset();
  }

  public void reset()
  {
    for ( int i=0; i<children.size(); i++ )
      children.get( i ).reset();
  }


  /**
   *
   * @return
   */
  public String getIdent()
  {
    return domelement.getAttribute( "ident" );
  }


  public <U> Vector<U> findElements( Class<U> type )
  {
    return findElements( type, false );
  }


  public <U> Vector<U> findElements( Class<U> type, boolean deep )
  {
    Vector<U> found = new Vector<U>();
    findElements( found, type, deep );
    return found;
  }



  void findElements( Vector found, Class type, boolean deep )
  {
    int i;
    // find in order of appearance in XML regardless of depth
    for ( i=0; i<children.size(); i++ )
    {
      //System.out.println( "child " + i + " class " + children.get(i).getClass() );
      if ( type.isInstance( children.get(i) ) )
        found.add( children.get(i) );
      if ( deep )
        children.get(i).findElements( found, type, true );
    }
  }

  public String toString()
  {
    StringBuffer buffer = new StringBuffer();
    toString( buffer, 0 );
    return buffer.toString();
  }

  public void toString( StringBuffer buffer, int indent )
  {
    for ( int i=0; i<indent; i++ )
      buffer.append( " " );
    buffer.append( getName() );
    buffer.append( "\n" );
    for ( int c=0; c<children.size(); c++ )
      children.get( c ).toString( buffer, indent+4 );
  }

    public String getAttribute( String name )
    {
        return domelement.getAttribute( name );
    }
    public int getIntAttribute( String name )
    {
        String dim = domelement.getAttribute( name );
        if ( dim == null || dim.length() == 0 )
            return -1;
        dim = dim.trim();
        if ( dim.length() == 0 )
            return -1;
        try { return Integer.parseInt(dim); }
        catch ( Exception e ) {}
        return -1;
    }
  
}
