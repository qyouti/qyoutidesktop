/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti.fonts;

import java.net.*;
import java.util.*;

/**
 *
 * @author jon
 */
public class QyoutiFonts
{
  private static final String[] names = 
  {
    "FreeSans.ttf",
    "FreeSansBold.ttf",
    "FreeSansBoldOblique.ttf",
    "FreeSansOblique.ttf",
    "FreeSerif.ttf",
    "FreeSerifBold.ttf",
    "FreeSerifBoldItalic.ttf",
    "FreeSerifItalic.ttf"
  };
  private static ArrayList<URL> list=null;
  
  
  public static List<URL> getFontURLList()
  {
    String path;
    URL url;
    
    if ( list == null )
    {
      list = new ArrayList<>();
      for ( String name : names )
      {
        url = QyoutiFonts.class.getResource( "/org/qyouti/fonts/resources/" + name );
        if ( url == null ) continue;
        list.add( url );
      }
    }
    return list;
  }
  
  public static void main( String[] args )
  {
    for ( URL url : getFontURLList() )
    {
      System.out.println( url.toString() );
    }
  }
}
