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

package org.qyouti.qti1.main;

import java.io.File;
import java.io.IOException;
import java.util.Vector;
import javax.xml.parsers.*;
import org.qyouti.qti1.*;
import org.qyouti.qti1.element.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 *
 * @author jon
 */
public class TestMain
{

  public static void main( String[] args ) throws ParserConfigurationException, SAXException, IOException
  {
    File qtifile = new File("/home/jon/qyouti/human_nut/qyouti.xml");
    DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    Document doc = builder.parse( qtifile );

    Element roote = doc.getDocumentElement();
    NodeList nl = roote.getChildNodes();
    Element questestinterop=null;
    Node node;

    for ( int i=0; i<nl.getLength(); i++ )
    {
      node = nl.item(i);
      if ( !(node instanceof Element) )
        continue;
      questestinterop = (Element)node;
      if ( "questestinterop".equals( questestinterop.getNodeName() ) )
        break;
      questestinterop = null;
    }

    if ( questestinterop == null )
    {
      System.err.println( "questestinterop element not found." );
      System.exit( 1 );
    }

    QTIBuilder qtibuilder = new QTIBuilder();
    QTIElement qtielement = qtibuilder.build( questestinterop );

    if ( !(qtielement instanceof QTIElementQuestestinterop) )
    {
      System.err.println( "questestinterop element not loaded." );
      System.exit( 1 );
    }

    int i;
    QTIElementQuestestinterop qti = (QTIElementQuestestinterop)qtielement;

    System.out.println( qti.toString() );
    if ( false )
      System.exit( 1 );

    Vector<QTIElementItem> items = qti.getItems();
    System.out.println( "Item count: " + items.size() );
    QTIElementItem current_item;
    for ( i=0; i<items.size(); i++ )
    {
      current_item = items.get(i);
      System.out.println( "--------------------------------------------------" );
      System.out.print( "item" + current_item.getIdent() );
      if ( !current_item.isSupported() )
      {
        System.out.println();
        continue;
      }

      System.out.println( "        SUPPORTED ITEM" );
      //current_item.setResponse( "RESP_MC", new String[] {"MC0"} );
      current_item.setResponseValueByOffset( i % 4 );
      String[] x = (String[])current_item.getResponseValue( "RESP_MC" );
      System.out.println( x[0] );
      current_item.computeOutcomes();
      System.out.println( current_item.getOutcomeValue( "SCORE" ) );
      
    }


  }
  
}
