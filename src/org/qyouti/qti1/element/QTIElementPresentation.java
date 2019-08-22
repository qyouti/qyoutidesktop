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

import java.util.*;
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
  ArrayList<QTIElementResponselid> responselids;
  QTIElementResponsestr responsestr;

  QTIElementItem item=null;

  // how many columns does this question prefer when
  // layed out on the page?
  int columns = 1;
  
  public QTIElementPresentation()
  {
    responselids = new ArrayList<>();
  }

  public int getColumns()
  {
    return columns;
  }
  
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

  public boolean isMultipleChoice()
  {
    if ( !isSupported() ) return false;
    if ( responselids.size() == 0 ) return false;
    for ( int i=0; i<responselids.size(); i++ )
      if ( !responselids.get( i ).isMultipleChoice() )
        return false;
    return true;
  }
  
  public int getMultipleChoiceOptionCount()
  {
    // a deep search so this works with multiple responselids
    Vector<QTIElementResponselabel> responselabels = findElements( QTIElementResponselabel.class, true );
    return responselabels.size();
  }
    
  @Override
  public void initialize()
  {
    material = null;
    responselids.clear();
    responsestr = null;
    item=null;
  
    super.initialize();
    
    String columnsattribute = domelement.getAttribute( "qyouti:columns" );
    columns = 1;
    if ( columnsattribute!= null && columnsattribute.length() > 0 )
    {
      try { columns = Integer.parseInt( columnsattribute ); }
      catch ( NumberFormatException nfe ) { columns = 1; }
    }
    
    supported = false;
    Vector<QTIResponse> responses = findElements( QTIResponse.class, true );
    for ( int i=0; i<responses.size(); i++ )
    {
      if ( responses.get(i) instanceof QTIResponseUnsupported )
        return;
      if ( responses.get(i) instanceof QTIElementResponselid )
      {
        // no support for items with responselids and a responsestr mixed
        if ( responsestr != null)
          return;
        responselids.add( (QTIElementResponselid)responses.get(i) );
      }
      if ( responses.get(i) instanceof QTIElementResponsestr )
      {
        if ( responsestr != null )
          responsestr = (QTIElementResponsestr)responses.get(i);
      }
    }

    for ( int i=0; i<responselids.size(); i++ )
    {
      if ( ! responselids.get( i ).isSupported() )
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
