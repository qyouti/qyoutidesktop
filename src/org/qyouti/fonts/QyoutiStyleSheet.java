/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti.fonts;

import java.awt.*;
import java.util.*;
import javax.swing.plaf.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import sun.font.*;

/**
 *
 * @author jon
 */
public class QyoutiStyleSheet
        extends StyleSheet
{
  QyoutiFontManager fm;
  
  // These a private in ancestor class StyleContext so we need our own
  private transient FontKey fontSearch = new FontKey( null, 0, 0 );
  private transient Hashtable<FontKey, Font> fontTable = new Hashtable<FontKey, Font>();
  private boolean readonly = false;

  
  public QyoutiStyleSheet( QyoutiFontManager fm )
  {
    this.fm = fm;
  }
    
  public void setReadOnly( boolean b )
  {
    readonly = b;
  }

  @Override
  public void addStyleSheet( StyleSheet ss )
  {
    if ( readonly ) return;
    super.addStyleSheet( ss ); //To change body of generated methods, choose Tools | Templates.
  }  
  
  /**
   * This is only overridden to provide a better break point.
   *
   * @param a The attributes of the bit of text that this font is needed for.
   * @return
   */
  //@Override
  public Font getFont( AttributeSet a )
  {
    Font f = super.getFont( a );
    return f;
  }

  /** This is copied from ancestor class StyleContext and modified to suit
   * 
   * @param family
   * @param style
   * @param size
   * @return 
   */
  public Font getFont( String family, int style, int size )
  {
    fontSearch.setValue( family, style, size );
    Font f = fontTable.get( fontSearch );
    if ( f == null )
    {
      // haven't seen this one yet.
      Style defaultStyle
              = getStyle( StyleContext.DEFAULT_STYLE );
      if ( defaultStyle != null )
      {
        final String FONT_ATTRIBUTE_KEY = "FONT_ATTRIBUTE_KEY";
        Font defaultFont
                = (Font) defaultStyle.getAttribute( FONT_ATTRIBUTE_KEY );
        if ( defaultFont != null
                && defaultFont.getFamily().equalsIgnoreCase( family ) )
        {
          f = defaultFont.deriveFont( style, size );
        }
      }
      if ( f == null )
      {
        f = new Font( family, style, size );
      }
      // See http://www.docjar.com/docs/api/sun/font/FontUtilities.html
      // This is where the font is converted to a composite one.

//            if (! FontUtilities.fontSupportsDefaultEncoding(f)) {
//                f = FontUtilities.getCompositeFontUIResource(f);
//            }
      
      // my replacement implementation
      f = fm.getCompositeFontUIResource(f);
      FontKey key = new FontKey( family, style, size );
      fontTable.put( key, f );
    }
    return f;
  }
  
  /**
   * key for a font table. This class is copied from StyleContext
   * because that copy is not accessible here.
   */
  static class FontKey
  {

    private String family;
    private int style;
    private int size;

    /**
     * Constructs a font key.
     */
    public FontKey( String family, int style, int size )
    {
      setValue( family, style, size );
    }

    public void setValue( String family, int style, int size )
    {
      this.family = (family != null) ? family.intern() : null;
      this.style = style;
      this.size = size;
    }

    /**
     * Returns a hashcode for this font.
     *
     * @return a hashcode value for this font.
     */
    public int hashCode()
    {
      int fhash = (family != null) ? family.hashCode() : 0;
      return fhash ^ style ^ size;
    }

    /**
     * Compares this object to the specified object. The result is
     * <code>true</code> if and only if the argument is not <code>null</code>
     * and is a <code>Font</code> object with the same name, style, and point
     * size as this font.
     *
     * @param obj the object to compare this font with.
     * @return    <code>true</code> if the objects are equal; <code>false</code>
     * otherwise.
     */
    public boolean equals( Object obj )
    {
      if ( obj instanceof FontKey )
      {
        FontKey font = (FontKey) obj;
        return (size == font.size) && (style == font.style) && (family == font.family);
      }
      return false;
    }

  }

}
