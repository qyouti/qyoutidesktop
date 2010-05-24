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
        extends QTIItemAncestor
{
  boolean supported=false;
  QTIElementMaterial material;
  QTIElementResponselid responselid;

  QTIElementItem item=null;


  public boolean isStandardMultipleChoice()
  {
    if ( !isSupported() ) return false;
    if ( responselid == null ) return false;
    return responselid.isStandardMultipleChoice();
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

    Vector<QTIResponseUnsupported> urlids = findElements( QTIResponseUnsupported.class, true );
    if ( urlids.size() != 0 )
      return;

    
    Vector<QTIElementResponselid> rlids = findElements( QTIElementResponselid.class, true );
    if ( rlids.size() != 1 )
      return;
    responselid = rlids.get( 0 );

    supported = responselid.isSupported();
  }

  public void setItem(QTIElementItem item)
  {
    this.item=item;
  }
}
