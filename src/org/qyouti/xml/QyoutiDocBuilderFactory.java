/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.qyouti.xml;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 *
 * @author jon
 */
public class QyoutiDocBuilderFactory
{
  private static DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
  static
  {
    factory.setValidating( false );
  }

  public static DocumentBuilder getDocumentBuilder() throws ParserConfigurationException
  {
    DocumentBuilder builder = factory.newDocumentBuilder();
    builder.setEntityResolver( new RepoEntityResolver() );
    return builder;
  }
}
