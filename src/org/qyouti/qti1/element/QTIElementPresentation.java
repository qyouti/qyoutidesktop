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

import java.util.Vector;
import org.qyouti.qti1.*;

/**
 *
 * @author jon
 */
public class QTIElementPresentation
        extends QTIItemDescendant
{
  boolean supported=false;
  QTIElementMaterial material;
  QTIElementResponselid responselid;
  QTIElementResponsestr responsestr;

  QTIElementItem item=null;


//  public boolean isStandardMultipleChoice()
//  {
//    if ( !isSupported() ) return false;
//    if ( responselid == null ) return false;
//    return responselid.isStandardMultipleChoice();
//  }
//
//  public boolean isMultipleChoice()
//  {
//    if ( !isSupported() ) return false;
//    return responselid != null;
//  }

  public boolean isString()
  {
    if ( !isSupported() ) return false;
    return responsestr != null;
  }


  public boolean isSupported()
  {
    return supported;
  }


  @Override
  public void initialize()
  {
    super.initialize();
    
    supported = false;
    Vector<QTIResponse> responses = findElements( QTIResponse.class, true );
    for ( int i=0; i<responses.size(); i++ )
    {
      if ( responses.get(i) instanceof QTIResponseUnsupported )
        return;
      if ( responses.get(i) instanceof QTIElementResponselid )
      {
        // responselid must come before responsestr
        if ( responselid != null || responsestr != null)
          return;
        responselid = (QTIElementResponselid)responses.get(i);
      }
      if ( responses.get(i) instanceof QTIElementResponsestr )
      {
        if ( responsestr != null )
          responsestr = (QTIElementResponsestr)responses.get(i);
      }
    }

    if ( responselid != null )
    {
      if ( ! responselid.isSupported() )
        return;
    }

    if ( responsestr != null )
    {
      if ( ! responsestr.isSupported() )
        return;
    }
    supported = true;
  }

  public void setItem(QTIElementItem item)
  {
    this.item=item;
  }
}
