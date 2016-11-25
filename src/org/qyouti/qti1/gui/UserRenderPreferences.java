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
  boolean bigpinkbox=false;

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
    s = element.getAttribute( "bigpinkbox" );
    bigpinkbox = "true".equals(s);
  }

  public void emit( Writer writer )
          throws IOException
  {
    writer.write( "  <preferences fontsize=\"" );
    writer.write( Double.toString(fontsize) );
    writer.write( "\"" );
    if ( serif )
      writer.write( " serif=\"true\"" );
    if ( bigpinkbox )
      writer.write( " bigpinkbox=\"true\"" );
    writer.write( "/>\n" );
  }

  public String packedRepresentation()
  {
    if ( packed != null ) return packed;
    packed = "v2_" + Double.toString(fontsize) + (serif?"_s":"") + (bigpinkbox?"_b":"");
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

  public boolean isBigpinkbox()
  {
    return bigpinkbox;
  }

  public void setBigpinkbox( boolean bigpinkbox )
  {
    this.bigpinkbox = bigpinkbox;
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
