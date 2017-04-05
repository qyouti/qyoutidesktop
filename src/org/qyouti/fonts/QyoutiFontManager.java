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
    String path;
    File builtin = getBuiltinFontDirectory();
    File f, d;
    ArrayList<String> dirs = new ArrayList<>();
    StringBuilder str = new StringBuilder();
    
    if ( builtin == null || !builtin.exists() )
      str.append( "<!-- No directory found to add here. -->" );
    else 
    {
      dirs.add( builtin.getAbsolutePath() )
              ;
      if ( search ) 
      {
        str.append( "<auto-detect/>" );
      }
      else
      {
        int n = pref.getPropertyInt( "qyouti.print.font.count" );
        for ( int i=1; i<=n; i++ )
        {
          //name = pref.getProperty( "qyouti.print.font.name."+i );
          path = pref.getProperty( "qyouti.print.font.path."+i  );
          if ( path.startsWith( "/" ) )
            f = new File( path );
          else
            f = new File( builtin, path );
          d = f.getParentFile();
          if ( !dirs.contains( d.getAbsolutePath() ) )
            dirs.add( d.getAbsolutePath() );
        }
      }
      
      for ( String s : dirs )
      {
        str.append( "    <directory>" );
        str.append( s );
        str.append( "</directory>\n" );
      }
    }
    
    String c = CONFIGXML.replace( "INSERT",  str.toString() );
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
