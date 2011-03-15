/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.qyouti.qti1.gui;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author jon
 */
public class QuestionMetricsRecordSet
{
  boolean loaded = false;
  String printid;
  Hashtable<String,Hashtable<String,QuestionMetricsRecord>> items =
      new Hashtable<String,Hashtable<String,QuestionMetricsRecord>>();
  Vector<String> prefseq = new Vector<String>();

  public QuestionMetricsRecordSet(String printid)
  {
    this.printid = printid;
  }

  public QuestionMetricsRecordSet(File file)
  {
    loaded = true;
    try
    {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document doc = builder.parse( file );
      Element root = doc.getDocumentElement();
      printid = root.getAttribute("print-id");
      NodeList nl = root.getElementsByTagName( "preferences" );
      NodeList nli, nlb;
      Element pref;
      Element eitem, box;
      int i, j, k;
      Hashtable<String,QuestionMetricsRecord> records;
      Vector<Rectangle> boxes;
      Rectangle r;
      for ( i=0; i<nl.getLength(); i++ )
      {
        pref = (Element)nl.item(i);
        records = new Hashtable<String,QuestionMetricsRecord>();
        items.put( pref.getAttribute("key"), records );
        prefseq.add(pref.getAttribute("key"));
        nli = pref.getElementsByTagName("item");
        for ( j=0; j<nli.getLength(); j++)
        {
          eitem = (Element)nli.item(j);
          boxes = new Vector<Rectangle>();
          nlb = eitem.getElementsByTagName("box");
          for ( k=0; k<nlb.getLength(); k++ )
          {
            box = (Element)nlb.item(k);
            r = new Rectangle(
                Integer.parseInt(box.getAttribute("x")),
                Integer.parseInt(box.getAttribute("y")),
                Integer.parseInt(box.getAttribute("w")),
                Integer.parseInt(box.getAttribute("h"))
                );
            boxes.add(r);
          }
          records.put(
            eitem.getAttribute("item-id"),
            new QuestionMetricsRecord(
                eitem.getAttribute("item-id"),
                Double.parseDouble(eitem.getAttribute("height")),
                boxes ) );
        }
      }
    } catch (Exception ex)
    {
      Logger.getLogger(QuestionMetricsRecordSet.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  public void addItem( UserRenderPreferences prefs, QuestionMetricsRecord item )
  {
    String packed = (prefs==null)?"default":prefs.packedRepresentation();

    if (loaded)
    {
      throw new IllegalArgumentException("Can't add items to a record that was previously saved.");
    }
    Hashtable<String,QuestionMetricsRecord> list = items.get( packed );
    if ( list == null )
    {
      list = new Hashtable<String,QuestionMetricsRecord>();
      items.put(packed, list);
      prefseq.add(packed);
    }
    list.put(item.id, item);
  }

  public QuestionMetricsRecord getQuestionMetricsRecord( int pref, String qid )
  {
    if ( pref<0 || pref >= prefseq.size() ) return null;
    String key = prefseq.get(pref);
    if ( key == null ) return null;
    Hashtable<String,QuestionMetricsRecord> list = items.get(key);
    if ( list == null ) return null;
    return list.get(qid);
  }

  public int getPreferencesIndex( UserRenderPreferences prefs )
  {
    String packed = (prefs==null)?"default":prefs.packedRepresentation();
    return prefseq.indexOf( packed );
  }

  public void emit(Writer writer) throws IOException
  {
    Hashtable<String,QuestionMetricsRecord> list;

    if (loaded)
    {
      throw new IllegalArgumentException("Can't save a record that was previously saved.");
    }
    writer.write("<?xml version=\"1.0\"?>\n");
    writer.write("<question-metrics print-id=\"");
    writer.write(printid);
    writer.write("\">\n");

    Enumeration qids;
    for (int i = 0; i <prefseq.size(); i++)
    {
      writer.write("  <preferences key=\"");
      writer.write( prefseq.get(i) );
      writer.write("\">\n");
      list = items.get( prefseq.get(i) );
      qids = list.keys();
      while ( qids.hasMoreElements() )
        list.get(qids.nextElement()).emit(writer);
      writer.write("  </preferences>\n");
    }
    writer.write("</question-metrics>\n");
  }

}
