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

package org.qyouti.data;

import java.io.IOException;
import java.io.Writer;
import org.w3c.dom.Element;

/**
 *
 * @author jon
 */
public class OutcomeDatum
{
  String name;
  Object value;
  boolean fixed;
  

  public OutcomeDatum()
  {

  }

  public OutcomeDatum( Element element )
  {
    name = element.getAttribute( "ident" );
    String type = element.getAttribute( "type" );
    String v = element.getAttribute( "value" );
    if ( "integer".equals( type ) )
      value = new Integer( v );
    else if ( "decimal".equals( type ) )
      value = new Double( v );
    else if ( "null".equals( type ) )
      value = null;
    else
      value = v;
    String str = element.getAttribute( "fixed" );
    fixed = str != null && str.toLowerCase().startsWith( "y" );
  }


  public void emit( Writer writer )
          throws IOException
  {
    writer.write( "        <outcome" );
    writer.write( " ident=\"" + name  + "\"" );
    writer.write( " value=\"" + value + "\"" );
    if ( value == null )
      writer.write( " type=\"null\"" );
    else if ( value instanceof Integer )
      writer.write( " type=\"integer\"" );
    else if ( value instanceof Double )
      writer.write( " type=\"decimal\"" );
    else
      writer.write( " type=\"string\"" );
    if ( fixed )
      writer.write( " fixed=\"yes\"" );
    writer.write( "/>\r\n" );
  }

}
