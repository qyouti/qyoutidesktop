/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.qyouti.qti1.gui;

import java.awt.*;
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
  Color background=null;

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
    s = element.getAttribute( "background" );
    if ( s.startsWith( "0x" ) )
    {
      int hex = Integer.parseInt( s.substring( 2 ), 16 );
      background = new Color( hex );
    }
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
    if ( background != null )
      writer.write( " background=\"0x" + Integer.toString( background.getRGB(), 16 ) + "\"" );
    writer.write( "/>\n" );
  }

  public String packedRepresentation()
  {
    if ( packed != null ) return packed;
    packed = "v2_" + Double.toString(fontsize) + (serif?"_s":"") + (bigpinkbox?"_b":"");
    if ( background != null ) packed += "_c" + Integer.toString( background.getRGB(), 16 );
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

  public Color getBackground()
  {
    return background;
  }

  public void setBackground( Color background )
  {
    this.background = background;
  }

  
  
  @Override
  public boolean equals(Object obj)
  {
    if ( obj == null ) return false;
    if ( !(obj instanceof UserRenderPreferences)) return false;
    UserRenderPreferences other = (UserRenderPreferences)obj;
    return this.packedRepresentation().equals( other.packedRepresentation() );
  }


  @Override
  public int hashCode()
  {
    packedRepresentation();
    return hash;
  }


}
