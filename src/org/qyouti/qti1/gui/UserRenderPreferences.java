/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.qyouti.qti1.gui;

import java.io.IOException;
import java.io.Writer;
import org.w3c.dom.Element;

/**
 *
 * @author jon
 */
public class UserRenderPreferences
{
  boolean serif=false;
  double fontsize;

  String packed=null;
  int hash;

  public UserRenderPreferences()
  {
  }

  public UserRenderPreferences( Element element )
  {
    String s = element.getAttribute( "fontsize" );
    fontsize = Double.parseDouble(s);
    s = element.getAttribute( "serif" );
    serif = "true".equals(s);
  }

  public void emit( Writer writer )
          throws IOException
  {
    writer.write( "  <preferences fontsize=\"" );
    writer.write( Double.toString(fontsize) );
    writer.write( "\"" );
    if ( serif )
      writer.write( " serif=\"true\"" );
    writer.write( "/>\n" );
  }

  public String packedRepresentation()
  {
    if ( packed != null ) return packed;
    packed = "version1_" + Double.toString(fontsize) + (serif?"s":"");
    hash = packed.hashCode();
    return packed;
  }

  public double getFontsize()
  {
    return fontsize;
  }

  public void setFontsize(double fontsize)
  {
    this.fontsize = fontsize;
  }

  public boolean isSerif()
  {
    return serif;
  }

  public void setSerif(boolean serif)
  {
    this.serif = serif;
  }

  @Override
  public boolean equals(Object obj)
  {
    if ( obj == null ) return false;
    if ( !(obj instanceof UserRenderPreferences)) return false;
    UserRenderPreferences other = (UserRenderPreferences)obj;
    return this.serif == other.serif && this.fontsize == other.fontsize;
  }


  @Override
  public int hashCode()
  {
    packedRepresentation();
    return hash;
  }


}
