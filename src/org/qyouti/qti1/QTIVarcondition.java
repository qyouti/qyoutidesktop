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

import org.w3c.dom.NodeList;

/**
 *
 * @author jon
 */
public abstract class QTIVarcondition
        extends QTICondition
{
  boolean supported=false;
  protected String textcontent=null;

  public boolean isSupported()
  {
    return supported;
  }

  public String getRespident()
  {
    return domelement.getAttribute( "respident" );
  }

  public boolean isCaseSensitive()
  {
    String a = domelement.getAttribute( "case" );
    if ( a == null ) a = "Yescase";
    a = a.toLowerCase();
    return "yescase".equals( a );
  }

  public Object getResponse()
  {
    return item.getResponse( getRespident() );
  }



  @Override
  public void initialize()
  {
    super.initialize();

    supported=false;

    String index = domelement.getAttribute("index");
    if ( index!=null && index.length()>0 )
      return;

    // Blackboard has empty 'varequal' which are perhaps intended
    // for use as 'varexists' tests - something not supported by QTI.

    //NodeList nl = domelement.getChildNodes();
    //if ( nl.getLength() != 1 )
    //  throw new IllegalArgumentException( "Must be one text node in varcondition element." );
      //return;

    textcontent =  domelement.getTextContent();
    if ( textcontent == null )
      textcontent = "";

    //supported= textcontent!=null && textcontent.length()>0;
    supported = true;
  }

}
