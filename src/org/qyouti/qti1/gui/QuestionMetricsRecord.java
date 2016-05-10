/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti.qti1.gui;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.*;
import java.util.Vector;
import org.w3c.dom.*;

/**
 *
 * @author jon
 */
public class QuestionMetricsRecord
{
    public Vector<QuestionMetricBox> boxes;

    QuestionMetricsRecord( Vector<QuestionMetricBox> boxes)
    {
      this.boxes = boxes;
    }

    QuestionMetricsRecord( NodeList nl )
    {
      int i, x, y, w, h, idx;
      Node node;
      boxes = new Vector<QuestionMetricBox>();
      Element e_box;
      QuestionMetricBox box;
      
      idx=0;
      for ( i=0; i<nl.getLength(); i++ )
      {
        node = nl.item( i );
        if ( node.getNodeType() != Node.ELEMENT_NODE )
          continue;

        if ( "box".equals( node.getNodeName() ) )
        {
          e_box = (Element)node;
          x = Integer.parseInt( e_box.getAttribute( "x" ) );
          y = Integer.parseInt( e_box.getAttribute( "y" ) );
          w = Integer.parseInt( e_box.getAttribute( "w" ) );
          h = Integer.parseInt( e_box.getAttribute( "h" ) );
          box = new QuestionMetricBox( 
                  x, y, w, h,
                  e_box.getAttribute( "type" ),
                  e_box.getAttribute( "ident" ),
                  idx++                              );
          boxes.add( box );
        }  
      }
    }
    
    public QuestionMetricBox[] getBoxesAsArray()
    {
      QuestionMetricBox[] b = new QuestionMetricBox[boxes.size()];
      return boxes.toArray( b );
    }


    public void emit(Writer writer) throws IOException
    {
      for ( int i=0; i<boxes.size(); i++ )
      {
        writer.write("        <box type=\"" + boxes.get(i).getType()  );
        writer.write("\" ident=\"" +        boxes.get(i).getIdent() );
        writer.write("\" x=\"" +            boxes.get(i).x          );
        writer.write("\" y=\"" +            boxes.get(i).y          );
        writer.write("\" w=\"" +            boxes.get(i).width      );
        writer.write("\" h=\"" +            boxes.get(i).height     );
        writer.write( "\" />\n");
      }
    }
}
