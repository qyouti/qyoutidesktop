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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.qyouti.qti1.element.*;
import org.w3c.dom.*;

/**
 *
 * @author jon
 */
public class QTIBuilder
{

  static String elementNameToClassName( String element_name )
  {
    StringBuffer buffer = new StringBuffer( element_name );
    for ( int i=0; i<buffer.length(); i++ )
      if ( buffer.charAt( i ) == '_' )
        buffer.deleteCharAt( i-- );

    buffer.setCharAt( 0, Character.toUpperCase( buffer.charAt( 0 ) ) );
    buffer.insert( 0, "org.qyouti.qti1.element.QTIElement" );
    return buffer.toString();
  }

  static Class getElementClass( String element_name )
  {
    String class_name = elementNameToClassName( element_name );
    try
    {
      return Class.forName( class_name );
    }
    catch (ClassNotFoundException ex)
    {
      return null;
    }
  }


  QTIElement buildOne(Element element, QTIElement parent)
  {
    String nodename = element.getNodeName();
    QTIElement qtielement=new UnrecognisedQTIElement();
    Class c = getElementClass( nodename );
    if ( c != null )
    {
      try
      {
        qtielement = (QTIElement) c.newInstance();
      }
      catch ( Exception ex )
      {
      }
    }
    
    qtielement.domelement = element;
    qtielement.setParent(parent);
    return qtielement;
  }

  QTIElement build(Element element, QTIElement parent)
  {
    QTIElement qtielement = buildOne(element, parent);
    NodeList nl = element.getChildNodes();
    for (int i = 0; i < nl.getLength(); i++)
    {
      if (nl.item(i) instanceof Element)
      {
        qtielement.appendChild(build((Element) nl.item(i), qtielement));
      }
    }
    return qtielement;
  }

  public QTIElement build(Element element)
  {
    QTIElement root = build(element, null);
    root.initialize();
    return root;
  }


}
