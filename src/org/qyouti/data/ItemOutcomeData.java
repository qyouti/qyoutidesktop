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

import java.util.Vector;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author jon
 */
public class ItemOutcomeData
          extends AbstractTableModel
{
  public Vector<ItemOutcomeDatum> data = new Vector<ItemOutcomeDatum>();


  public int getRowCount()
  {
    return data.size();
  }

  public int getColumnCount()
  {
    return 2;
  }

  public Object getValueAt(int rowIndex, int columnIndex)
  {
    ItemOutcomeDatum datum = data.get( rowIndex );
    if ( columnIndex == 0 )
      return datum.name;
    return datum.value.toString();
  }

  public ItemOutcomeDatum getDatum( String name )
  {
    for ( int i=0; i<data.size(); i++ )
      if ( data.get(i).name.equals( name ) )
        return data.get(i);
    return null;
  }
}
