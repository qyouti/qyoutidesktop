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

package org.qyouti.qti1.swing;

import java.util.Vector;
import javax.swing.*;
import org.qyouti.qti1.*;
import org.qyouti.qti1.element.*;

/**
 *
 * @author jon
 */
public class QTIItemPanel
        extends QTIPanel
{
    public QTIItemPanel( QTIElementItem item )
    {
        JLabel errlabel;
        if ( item.isSupported() )
        {
            QTIElementPresentation presentation = item.getPresentation();
            Vector<QTIElementFlow> flows;
            flows = presentation.findElements( QTIElementFlow.class );
            if ( flows.size()==0 )
                add( new QTIFlowPanel( presentation ) );
            else
                add( new QTIFlowPanel( flows.get(0) ) );

        }
        else
        {
            errlabel = new JLabel("Unsupported question type.");
            add(errlabel);
        }
    }
}
