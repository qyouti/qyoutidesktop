/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.qyouti.qti1.ext;

import org.qyouti.qti1.ext.blackboard.QTIExtensionMatformattedtext;
import org.qyouti.qti1.ext.qyouti.*;
import org.qyouti.qti1.ext.webct.*;

/**
 * Maps namespace+element name pairs to class names
 * @author jon
 */
public class QTIExtensionResolver
{
    public static Class<?> resolve( String namespace, String name )
    {
        //System.out.println( "Resolving " + namespace + "  " + name );

      if ( "http://www.qyouti.org/qtiext".equals( namespace ) )
        {
            if ( "resp_extension".equals(name) )
                return QTIExtensionRespextension.class;
            if ( "render_sketcharea".equals(name) )
                return QTIExtensionRendersketcharea.class;
            if ( "itemproc_extension".equals(name) )
                return QTIExtensionItemproc.class;
            if ( "outcomemapping".equals(name) )
                return QTIExtensionOutcomemapping.class;
            if ( "outcomemapentry".equals(name) )
                return QTIExtensionOutcomemapentry.class;
        }

      if ( "http://www.webct.com/vista/assessment".equals( namespace ) )
        {
            if ( "material_webeq".equals(name) )
                return QTIExtensionWebctMaterialwebeq.class;
        }

        if ( "mat_formattedtext".equals(name) )
          return QTIExtensionMatformattedtext.class;

        return null;
    }
}
