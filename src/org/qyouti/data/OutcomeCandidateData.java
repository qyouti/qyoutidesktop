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
import java.util.HashMap;
import java.util.Vector;
import javax.swing.event.*;
import javax.swing.table.AbstractTableModel;
import javax.xml.transform.TransformerException;

/**
 *
 * @author jon
 */
public class OutcomeCandidateData
          extends OutcomeData
{
  HashMap<String,OutcomeData> qmap = new HashMap<>();
  String cident;
  
  public OutcomeCandidateData(ExaminationData exam, String cident)
  {
    super(exam);
    this.cident = cident;
  }
  
  public void addQuestionOutcomeData( String qident, OutcomeData o )
  {
    qmap.put(qident, o);
  }
  
  public OutcomeData getQuestionOutcomeData( String qident )
  {
    return qmap.get(qident);
  }
  
  public void clearnQuestionOutcomeData()
  {
    qmap.clear();
  }
  
}
