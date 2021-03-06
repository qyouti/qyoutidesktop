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
import org.qyouti.qti1.QTIElement;

/**
 *
 * @author jon
 */
public class QTIElementAssessment
        extends QTIElement
{
  QTIElementMaterial material = null;

  @Override
  public void initialize()
  {
    material = null;
    super.initialize();

    Vector<QTIElementPresentationmaterial> pms = findElements( QTIElementPresentationmaterial.class, true );
    if ( pms.size() == 0 )
      return;
    QTIElementPresentationmaterial pm = pms.firstElement();
    Vector<QTIElementMaterial> ms = pm.findElements( QTIElementMaterial.class, true );
    if ( ms.size() > 0 )
    {
      material = ms.firstElement();
      return;
    }
  }

  public QTIElementMaterial getMaterial()
  {
    return material;
  }
}
