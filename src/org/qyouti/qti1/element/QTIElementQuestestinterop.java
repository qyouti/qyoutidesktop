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

import java.util.Hashtable;
import org.qyouti.qti1.QTIElement;
import java.util.Vector;

/**
 *
 * @author jon
 */
public class QTIElementQuestestinterop
        extends QTIElement
{
  Vector<QTIElementItem> items=null;
  Hashtable<String,QTIElementItem> item_table = new Hashtable<String,QTIElementItem>();

  QTIElementOutcomesprocessing outcomesprocessing=null;

  QTIElementAssessment assessment=null;

  @Override
  public void initialize()
  {
    super.initialize();
    items = findElements( org.qyouti.qti1.element.QTIElementItem.class, true );
    for ( int i=0; i<items.size(); i++ )
      item_table.put( items.get(i).getIdent(), items.get(i) );

    Vector<QTIElementOutcomesprocessing> list = findElements( QTIElementOutcomesprocessing.class, true );
    if ( list.size() == 1 )
      outcomesprocessing = list.get(0);
  }

  public QTIElementOutcomesprocessing getOutcomesprocessing()
  {
    return outcomesprocessing;
  }

  public Vector<QTIElementItem> getItems()
  {
    return items;
  }

  public QTIElementItem getItem( String ident )
  {
    return item_table.get( ident );
  }

  public QTIElementMaterial getAssessmentMaterial()
  {
    if ( assessment == null ) return null;
    return assessment.getMaterial();
  }
}
