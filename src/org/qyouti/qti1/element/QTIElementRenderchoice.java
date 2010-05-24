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
import java.util.Vector;
import org.qyouti.qti1.*;

/**
 *
 * @author jon
 */
public class QTIElementRenderchoice
        extends QTIRender
{
  Vector<QTIElementResponselabel> responselabels;
  Hashtable<String,QTIElementResponselabel> responselabel_table =
          new Hashtable<String,QTIElementResponselabel>();


  public String[] getResponseIdents()
  {
    String[] idents = new String[responselabels.size()];
    for ( int i=0; i< idents.length; i++ )
      idents[i] = responselabels.get(i).getIdent();
    return idents;
  }

  public boolean containsResponseIdent( String ident )
  {
    return responselabel_table.containsKey( ident );
  }


  @Override
  public boolean isSupported()
  {
    return true;
  }


  @Override
  public void initialize()
  {
    super.initialize();

    responselabels = findElements( QTIElementResponselabel.class, true );
    for ( int i=0; i<responselabels.size(); i++ )
    {
      responselabel_table.put( responselabels.get(i).getIdent(), responselabels.get(i) );
    }  
  }

}
