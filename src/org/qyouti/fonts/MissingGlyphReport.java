/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti.fonts;

import java.util.*;

/**
 *
 * @author jon
 */
public class MissingGlyphReport
{
  private HashMap<Character,Boolean> charmap = new HashMap();
  private ArrayList<Character> charlist = new ArrayList<>();
  
  public void addCharacter( char c )
  {
    Character co = Character.valueOf( c );
    if ( charmap.containsKey( co ) )
      return;
    charmap.put( co, Boolean.TRUE );
    charlist.add( co );
  }
  
  public void addReport( MissingGlyphReport other )
  {
    for ( Character otherco : other.charmap.keySet() )
      addCharacter( otherco );
  }
  
  public boolean isEmpty()
  {
    return charlist.isEmpty();
  }
  
  void sort()
  {
    charlist.sort( 
            new Comparator()
            {  
              @Override
              public int compare( Object o1, Object o2 )
              {
                Character c1 = (Character)o1;
                Character c2 = (Character)o2;
                if ( c1.equals( c2 ) )
                  return 0;
                if ( c1.charValue() < c2.charValue() )
                  return -1;
                return 1;
              }
            }
    );    
  }
  
  public String toString()
  {
    sort();
    StringBuffer sb = new StringBuffer();
    for ( Character c : this.charlist )
    {
      sb.append( "Decimal: " );
      sb.append( (int)c.charValue() );
      sb.append( " Hex: " );
      sb.append( Integer.toHexString( (int)c.charValue() ) );
      sb.append( " Char: {" );
      sb.append( c.charValue() );
      sb.append( "}\n" );
    }
    return sb.toString();
  }
}
