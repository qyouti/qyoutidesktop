/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti.qti1.gui;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.*;
import java.util.Vector;

/**
 *
 * @author jon
 */
public class QuestionMetricsRecord
{

    public String id;
    public double height;
    public Vector<QuestionMetricBox> boxes;

    QuestionMetricsRecord(String id, double height, Vector<QuestionMetricBox> boxes)
    {
      this.id = id;
      this.height = height;
      this.boxes = boxes;
    }

    public QuestionMetricBox[] getBoxesAsArray()
    {
      QuestionMetricBox[] b = new QuestionMetricBox[boxes.size()];
      return boxes.toArray( b );
    }


//    byte[] toByteArray()
//    {
//      try
//      {
//        ByteArrayOutputStream baout = new ByteArrayOutputStream();
//        DataOutputStream out = new DataOutputStream(baout);
//
//
//        out.writeUTF(id);
//        short h = (short) Math.floor(height / 10); //tenths of an inch
//        out.writeShort(h);
//
//        short coord;
//        Rectangle r;
//        for (int i = 0; i<boxes.size(); i++)
//        {
//          r = boxes.get(i);
//          out.writeShort( (short)r.x );
//          out.writeShort( (short)r.y );
//          out.writeShort( (short)r.width );
//          out.writeShort( (short)r.height );
//        }
//
//
//        byte[] buffer = baout.toByteArray();
//        return buffer;
//      } catch (IOException ex)
//      {
//      }
//
//      return null;
//    }

    public void emit(Writer writer) throws IOException
    {
      writer.write("    <item item-id=\"");
      writer.write( id );
      writer.write("\" height=\"" );
      writer.write( Double.toString( height ) );
      writer.write( "\">\n");

      for ( int i=0; i<boxes.size(); i++ )
      {
        writer.write("      <box type=\"" + boxes.get(i).getType()  );
        writer.write("\" ident=\"" +        boxes.get(i).getIdent() );
        writer.write("\" x=\"" +            boxes.get(i).x          );
        writer.write("\" y=\"" +            boxes.get(i).y          );
        writer.write("\" w=\"" +            boxes.get(i).width      );
        writer.write("\" h=\"" +            boxes.get(i).height     );
        writer.write( "\" />\n");
      }

      writer.write("    </item>\n");
    }
}
