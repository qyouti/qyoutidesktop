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
import javax.swing.event.*;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author jon
 */
public class OutcomeData
          extends AbstractTableModel
{
  private ExaminationData exam=null;
  private Vector<OutcomeDatum> data = new Vector<OutcomeDatum>();

  public OutcomeData( ExaminationData exam )
  {
    this.exam = exam;
    if ( exam != null && exam.outcomelistener != null )
      addTableModelListener( exam.outcomelistener );
  }
  
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
    OutcomeDatum datum = data.get( rowIndex );
    if ( columnIndex == 0 )
      return datum.name;
    return datum.value.toString();
  }

  public OutcomeDatum getDatum( String name )
  {
    for ( int i=0; i<data.size(); i++ )
      if ( data.get(i).name.equals( name ) )
        return data.get(i);
    return null;
  }
  
  /**
   * Clears non-fixed outcomes ready for (re)calculation.
   */
  public void clear()
  {
    int n = data.size()-1;
    for ( int i=0; i<data.size(); i++ )
      if ( !data.get(i).fixed )
        data.remove( i-- );
    if ( n > 0 && exam != null )    
      exam.processRowsDeleted( this, 0, n );
  }
  
  public void addDatum( OutcomeDatum datum )
  {
    data.add( datum );
    if( exam != null )
      exam.processRowsInserted( this, data.size()-1, data.size()-1 );
  }

  public OutcomeDatum getDatumAt(int rowIndex )
  {
    return data.get( rowIndex );
  }

  @Override
  public void addTableModelListener( TableModelListener l )
  {
    super.addTableModelListener( l );
    TableModelEvent e = new TableModelEvent(this, 0, data.size()-1,
                             TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT );
    // only inform this most recent listener, not all listeners.
    l.tableChanged( e );
  }
  
}
