/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.qyouti.xml;

/**
 *
 * @author jon
 */
public class StringProcessor
{
  public static String cleanXmlString( String in )
  {
    String r;
    r = in.replaceAll( "& ", "&amp; " );
    return r;
  }
}
