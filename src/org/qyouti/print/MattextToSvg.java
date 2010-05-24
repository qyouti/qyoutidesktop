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
package org.qyouti.print;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.*;
import org.w3c.dom.*;

/**
 *
 * @author jon
 */
public class MattextToSvg
{

  private static DocumentBuilder docbuilder = null;
  private static boolean failed = false;

  public static void init()
  {
    if (docbuilder != null)
    {
      return;
    }

    try
    {
      docbuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    } catch (Exception ex)
    {
      Logger.getLogger(MattextToSvg.class.getName()).log(Level.SEVERE, null, ex);
      docbuilder = null;
    }
  }

  private static org.w3c.dom.Element convert( String fullhtml, double width )
  {
    Element matimage;
    Document doc;
    
    HtmlToSvg converter = new HtmlToSvg();
    SvgConversionResult result =
            converter.convert(
              fullhtml,
              (int)width,
              (int)width*10 );

    if ( result == null )
    {
      System.out.println( "No SVG element produced." );
      return null;
    }

    doc = docbuilder.newDocument();
    matimage = doc.createElement("matimage");
    matimage.setAttribute("imagtype", "image/jpeg");
    // 100th inch
    matimage.setAttribute("width", Integer.toString( (int)width/10 ));
    // 100th inch
    matimage.setAttribute("height", Integer.toString( result.getHeight()/10 ) );
    matimage.setAttribute("uri", "inline" );

    matimage.appendChild( doc.importNode( result.getDocument().getDocumentElement(), true ) );
    // matimage.setTextContent( "\n" + jpegbase64 );
    // failed = true;
    return matimage;
  }

  public static org.w3c.dom.Element htmlToSvg( String text, double width, String font_family )
  {
    if (failed)
    {
      return null;
    }
    init();
    
    System.out.println( text );
    String fullhtml =
            "<html><body><div style=\"font-size: 100px; font-family: " +
            font_family +
            ";\">" +
            text +
            "</div></body></html>";

    return convert( fullhtml, width );
  }

  /**
   *
   * @param nl
   * @param width In 100th inch.
   * @param font_family
   * @return
   */
  public static org.w3c.dom.Element textToSvg(org.w3c.dom.NodeList nl, double width, String font_family )
  {
    int i;
    Node node;
    NodeList mattextnl;
    Element el, matimage;
    CharacterData html;


    if (failed)
    {
      return null;
    }
    init();
    if (nl == null)
    {
      return null;
    }
    if (nl.getLength() == 0)
    {
      return null;
    }

    try
    {

      for (i = 0; i < nl.getLength(); i++)
      {
        node = nl.item(i);
        if (!(node instanceof org.w3c.dom.Element))
        {
          continue;
        }

        el = (org.w3c.dom.Element) node;
        if (!"mattext".equals(el.getTagName()))
        {
          continue;
        }

        mattextnl = el.getChildNodes();
        if (mattextnl.getLength() == 0)
        {
          continue;
        }

        if (!(mattextnl.item(0) instanceof org.w3c.dom.CharacterData))
        {
          continue;
        }

        html = (CharacterData) mattextnl.item(0);
        String fullhtml =
                "<html><body><div style=\"font-size: 100px; font-family: " +
                font_family +
                ";\">" +
                html.getData() +
                "</div></body></html>";

        return convert( fullhtml, width );
      }

      System.out.println( "No mattext element." );
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      Logger.getLogger(MattextToSvg.class.getName()).log(Level.SEVERE, null, ex);
      failed = true;
      return null;
    }

    return null;
  }


}
