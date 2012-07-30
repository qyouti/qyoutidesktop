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
public class QTIMaterialPanel
        extends QTIPanel
{

    public QTIMaterialPanel(QTIElementMaterial material)
    {
        JLabel errlabel;
        Vector<QTIItemDescendant> children;
        children = material.findElements(QTIItemDescendant.class);
        QTIItemDescendant child;
        String tagname;
        for (int i = 0; i < children.size(); i++)
        {
            child = children.get(i);
            tagname = child.getElementName();

            if ( "qtielement".equals( tagname ) )
                continue;

            if ( "altmaterial".equals( tagname ) )
                continue;

            if (child instanceof QTIElementMattext)
            {
                QTIElementMattext mattext = (QTIElementMattext)child;
                JLabel label = new JLabel( mattext.getContent() );
                add( label );
                continue;
            }

            removeAll();
            errlabel = new JLabel( "Unsupported question material." );
            add(errlabel);
            return;
        }
    }
}
