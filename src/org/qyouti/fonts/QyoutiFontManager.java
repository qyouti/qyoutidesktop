package org.qyouti.fonts;


import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;
import org.apache.avalon.framework.configuration.*;
import org.apache.fop.fonts.*;
import org.apache.fop.svg.*;
import org.qyouti.*;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author jon
 */
public class QyoutiFontManager
{
  public static final String CONFIGXML = 
        "<?xml version=\"1.0\"?>\n" +
        "<fop version=\"1.0\">\n" +
        "  <use-cache>false</use-cache>\n" +
        "  <fonts>\n"          +
        "    INSERT\n"         +
        "  </fonts>\n"         +
        "</fop>\n";  

  QyoutiPreferences pref;
  FontInfo configuredfontinfo;
  FontInfo searchfontinfo;
  
  public QyoutiFontManager( QyoutiPreferences pref )
  {
    this.pref = pref;
    loadFOPFontInfo( false );
    
    Map<String,Typeface> map = configuredfontinfo.getFonts();
    System.out.println( "Font scanning results:" );
    for ( Typeface tf : map.values() )
    {
      if ( tf instanceof Base14Font || tf.getFontURI() == null )
        continue;
      System.out.println( tf.getFontName() + " = " + tf.getFontURI() );
      if ( tf.hasChar( '国' ) )
        System.out.println( "                                                              THIS FONT HAS 国 IN IT." );
    }
    System.out.println( "Font scanning complete." );    
  }

  
  
  
  private void loadFOPFontInfo( boolean search )                                          
  {                                                     
    try
    {
      PDFDocumentGraphics2D  graphics = new PDFDocumentGraphics2D( false );          
      Configuration fopconfig = getFOPConfiguration( search );
      PDFTranscoder pdft = new PDFTranscoder();
      pdft.configure( fopconfig );    
      PDFDocumentGraphics2DConfigurator configurator
              = new PDFDocumentGraphics2DConfigurator();
      boolean useComplexScriptFeatures = false; //TODO - FIX ME
      configurator.configure( graphics, fopconfig, useComplexScriptFeatures);
      if ( search )
        searchfontinfo =  graphics.getFontInfo();
      else
        configuredfontinfo =  graphics.getFontInfo();      
    }
    catch (Exception e)
    {
      e.printStackTrace();
    } 
  } 


  
  public Configuration getFOPConfiguration()
  {
    return getFOPConfiguration( false );
  }

  private Configuration getFOPConfiguration( boolean search )
  {
    DefaultConfigurationBuilder cfgBuilder = new DefaultConfigurationBuilder();
    try
    {
      return cfgBuilder.build( 
              new ByteArrayInputStream( 
                      getFOPConfigurationString( search ).getBytes( "UTF8" ) ) );
    }
    catch ( Exception ex )
    {
      Logger.getLogger(QyoutiFontManager.class.getName() ).log( Level.SEVERE, null, ex );
    }
    return null;
  }
  
  private String getFOPConfigurationString( boolean search )
  {
    String subst, name, path;
    File d = getBuiltinFontDirectory();
    File f;
    
    if ( d == null || !d.exists() ) subst = "<!-- No directory found to add here. -->";
    else if ( search ) subst = "<directory>" + d.getAbsolutePath() + "</directory>";
    else
    {
      StringBuilder str = new StringBuilder();
      int n = pref.getPropertyInt( "qyouti.print.font.count" );
      for ( int i=1; i<=n; i++ )
      {
        name = pref.getProperty( "qyouti.print.font.name."+i );
        path = pref.getProperty( "qyouti.print.font.path."+i  );
        if ( path.startsWith( "/" ) )
          f = new File( path );
        else
          f = new File( d, path );
        str.append( "        <font embed-url=\"" );
        str.append( f.toString() );
        str.append( "\"/>\n" );
      }
      subst = str.toString();
    }
    
    String c = CONFIGXML.replace( "INSERT",  subst );
    System.out.println( c );
    return c;
  }
  
  public static File getBuiltinFontDirectory()
  {
    File here;
    
    // reference to this class itself
    URL url = QyoutiFontManager.class.getResource( "QyoutiFontManager.class" );

    if ( "jar".equals( url.getProtocol() ) )
    {
      // this is the distribution version running from an executable jar
      // we need to look for folder 'fonts' in the folder which contains the
      // jar.
      try {
        JarURLConnection juc = (JarURLConnection)url.openConnection();
        // switch to the url of the jar file
        url = juc.getJarFileURL();
        here = new File( url.getPath() );
        here = here.getParentFile();
        return new File( here, "fonts" );
      }
      catch ( IOException ex ) {
        Logger.getLogger(QyoutiFontManager.class.getName() ).log( Level.SEVERE, null, ex );
      }
    }
    
    if ( "file".equals( url.getProtocol() ) )
    {
      // it is a '.class' file in the local file system
      // so this is running or debugging from within IDE
      // look for dist folder.
      
      // work up to container of build folder
      here = new File( url.getPath() );
      for ( int i=0; i<6; i++ )
        here = here.getParentFile();
      return new File( here, "dist/fonts" );
    }
    
    return null;
  }
}
