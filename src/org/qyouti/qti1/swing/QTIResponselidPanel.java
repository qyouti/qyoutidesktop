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
public class QTIResponselidPanel
        extends QTIPanel
{
    public QTIResponselidPanel( QTIElementResponselid rlid )
    {
        JLabel errlabel;
        Vector<QTIItemAncestor> children;
        children = rlid.findElements(QTIItemAncestor.class);
        QTIItemAncestor child;
        for (int i = 0; i < children.size(); i++)
        {
            child = children.get(i);

            if (child instanceof QTIElementMaterial)
            {
                add(new QTIMaterialPanel((QTIElementMaterial) child));
                continue;
            }

            if (child instanceof QTIRender)
            {
                if (child instanceof QTIElementRenderchoice)
                {
                    add(new QTIRenderchoicePanel((QTIElementRenderchoice) child));
                    continue;
                } else
                {
                    removeAll();
                    errlabel = new JLabel("Unsupported question type.");
                    add(errlabel);
                    return;
                }
            }
        }
    }
}