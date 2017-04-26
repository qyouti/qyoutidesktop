package org.qyouti.fonts;


import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.*;
import java.util.HashMap;
import java.util.logging.*;
import javax.swing.plaf.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import org.apache.avalon.framework.configuration.*;
import org.apache.fop.fonts.*;
import org.apache.fop.fonts.Font;
import org.apache.fop.svg.*;
import org.qyouti.*;
import org.qyouti.qti1.gui.*;
import sun.awt.*;
import sun.font.*;


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
  FontInfo configuredfontinfo=null;
  FontInfo searchfontinfo=null;

  public static final int SET_SANS   =  0;
  public static final int SET_SERIF  =  1;
  public static final int SET_MONO   =  2;
  public static final int SET_SEARCH = -1;
  
  ArrayList<String> seriffamilynames = new ArrayList<>();
  ArrayList<String> sansfamilynames  = new ArrayList<>();
  ArrayList<String> monofamilynames  = new ArrayList<>();

  ArrayList<ArrayList<String>> familynames = new ArrayList<>();

  ArrayList<String> searchfamilynames  = new ArrayList<>();

  static private boolean builtininstalled=false;
  
  public QyoutiFontManager( QyoutiPreferences pref )
  {
    this.pref = pref;
    load();
    if ( !builtininstalled )
      installBuiltins();
  }
  
  public void installBuiltins()
  {
    File d = getBuiltinFontDirectory();
    File[] ttffiles = d.listFiles( 
            new FilenameFilter()
            {
              @Override
              public boolean accept( File dir, String name )
              {
                return name.endsWith( ".ttf" );
              }
            }
    );
    
    for ( File f : ttffiles )
    {
      try
      {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        ge.registerFont( java.awt.Font.createFont( java.awt.Font.TRUETYPE_FONT, f ) );
        System.out.println( "Installed " + f  );
      }
      catch ( Exception ex )
      {
        ex.printStackTrace();
      }
    }
    
    builtininstalled = true;
  }
  
  public void reset()
  {
    pref.setDefaults();
    pref.save();
    load();
  }
  
  public void load()
  {
    seriffamilynames.clear();
    sansfamilynames.clear();
    monofamilynames.clear();
    readPreferences();
    loadFOPFontInfo( false );
    
    Map<String,Typeface> map = configuredfontinfo.getFonts();
    
    System.out.println( "Font scanning results:" );
    
    configuredfontinfo.dumpAllTripletsToSystemOut();
    
    CustomFont cf;
    for ( Typeface tf : map.values() )
    {
      if ( tf instanceof LazyFont )
        tf = ((LazyFont)tf).getRealFont();
      if ( !(tf instanceof CustomFont) )
        continue;
      cf = (CustomFont)tf;
      if ( !cf.isEmbeddable() )
        continue;
      
      System.out.println( tf.getFontName() + " = " + tf.getFontURI() );
      for ( String ffname : tf.getFamilyNames() )
        System.out.println( "Family name: " + ffname );
      if ( tf.hasChar( '国' ) )
        System.out.println( "                                                              THIS FONT HAS 国 IN IT." );
      if ( tf.hasChar( 'ツ' ) )
        System.out.println( "                                                              THIS FONT HAS ツ IN IT." );
    }
    System.out.println( "Font scanning complete." );    
  }

  
  public String getSVGFontFamilyAttribute( String topfamily )
  {
    int i, j;
    String[] familylist = new String[] { topfamily };
    StringBuffer buffer=new StringBuffer();
    buffer.append( topfamily );
    
    for ( i=0; i<familynames.size(); i++ )
    {
      if ( topfamily.equals( familynames.get( i ).get( 0 ) ) )
      {
        for ( j=1; j<familynames.get( i ).size(); j++ )
        {
          buffer.append( ',' );
          buffer.append( familynames.get( i ).get( j ) );
        }
        break;
      }
    }
    return buffer.toString();    
  }
  
  /**
   * Take a simple physical font and return a composite font with additional
   * font families that might be able to fill in glyphs missing from the
   * first font.
   * @param f
   * @return 
   */
  public java.awt.Font getCompositeFontUIResource( java.awt.Font f )
  {
    int i;
    FontUIResource fuir;
    String topfamily = f.getFamily();
    String[] familylist = new String[] { topfamily };
    
    for ( i=0; i<familynames.size(); i++ )
    {
      if ( topfamily.equals( familynames.get( i ).get( 0 ) ) )
        familylist =  familynames.get( i ).toArray( familylist );
    }

    CompositeFont cf = buildCompositeFont( familylist );
    if ( cf == null ) return f;
    
    fuir = new FontUIResource( f );
    FontAccess.getFontAccess().setFont2D( fuir, cf.handle );
    FontAccess.getFontAccess().setCreatedFont( fuir );
    return fuir;
  }
  
  private CompositeFont buildCompositeFont( String[] names )
  {
    CompositeFont cf=null;
    sun.font.FontManager fm = FontManagerFactory.getInstance();
    if ( fm instanceof SunFontManager )
    {
      cf = new CompositeFont(
            names[0],
            null,
            names,
            names.length,
            null,
            null,
            true,
            (SunFontManager)fm       
            );
    }
    return cf;
  }
  
  
  private void readPreferences()
  {
    familynames.add( readFontList( "qyouti.print.font-family-serif",    seriffamilynames ) );
    familynames.add( readFontList( "qyouti.print.font-family-sans",      sansfamilynames ) );
    familynames.add( readFontList( "qyouti.print.font-family-monospace", monofamilynames ) );
  }
  
  private ArrayList<String> readFontList( String key, ArrayList<String> array )
  {
    String str = pref.getProperty( key );
    String[] a = str.split( "," );
    for ( int i=0; i<a.length; i++ )
      array.add( a[i].trim() );
    return array;
  }
  
  public String[] getFontFamilyNames( int set )
  {
    String[] a = new String[0];
    switch ( set )
    {
      case SET_SANS:
        return this.sansfamilynames.toArray( a );
      case SET_SERIF:
        return this.seriffamilynames.toArray( a );
      case SET_MONO:
        return this.monofamilynames.toArray( a );
      case SET_SEARCH:
        if ( searchfontinfo == null )
          loadFOPFontInfo( true );
        return this.searchfamilynames.toArray( a );
    }
    
    return a;
  }  
  
  public void setFontFamilyNames( int set, String[] a )
  {
    ArrayList<String> list;
    switch ( set )
    {
      case SET_SANS:
        list = sansfamilynames;
        break;
      case SET_SERIF:
        list = seriffamilynames;
        break;
      case SET_MONO:
        list = monofamilynames;
      default:
        return;
    }
    list.clear();
    for ( String s : a )
      list.add( s );
    return;
  } 
  
  public boolean reInitialise()
  {
    // work out the list of font files needed to support
    // current fontfamily names.
    // put font lists into the preferences, update the list
    // of loaded fonts to match all those in use, reload
    // data into this font manager.
    // If fails reload the old preferences from disk.
    // otherwise save these new preferences to disk.
    if ( this.searchfontinfo == null ) return false;
    
    ArrayList<Typeface> tflist = new ArrayList<>();
    boolean duplicate;
    for ( ArrayList<String> list : this.familynames )
      for ( String ffam : list )
        // find all the fonts with this family name
        for ( Typeface tf : this.searchfontinfo.getFonts().values() )
        {
          duplicate = false;
          for ( Typeface tfother : tflist )
            if ( tf.getEmbedFontName().equals( tfother.getEmbedFontName() ) )
            {
              duplicate = true;
              break;
            }
          if ( !duplicate  && tf.getFamilyNames().contains( ffam ) )
            tflist.add( tf );
        }

    pref.setProperty( "qyouti.print.font.count", Integer.toString( tflist.size() ) );
    Typeface tf;
    CustomFont cf;
    File file;
    for ( int i=0; i<tflist.size(); i++ )
    {
      tf = tflist.get( i );
      if ( tf instanceof LazyFont )
        tf = ((LazyFont)tf).getRealFont();
      if ( !(tf instanceof CustomFont) )
        continue;
      cf = (CustomFont)tf;
      if ( !cf.isEmbeddable() )
        continue;

      file = new File( cf.getEmbedFileURI() );
      pref.setProperty( "qyouti.print.font.path."+(i+1), file.getAbsolutePath() );
    }
    pref.save();
    load();
    return true;
    // how to revert to saved version
//    pref.load();
//    return false;    
  }
  
  public String getDefaultFontFamilyName( boolean serif )
  {
    if ( serif )
      return seriffamilynames.get( 0 );
    return sansfamilynames.get( 0 );
  }
  
  public QyoutiStyleSheet getQyoutiStyleSheet( boolean serif )
  {
    StyleSheet ss = getStyleSheet( serif?seriffamilynames.get( 0 ):sansfamilynames.get( 0 ), 
                          monofamilynames.get( 0 ) );
    QyoutiStyleSheet qss = new QyoutiStyleSheet( this );
    qss.addStyleSheet( ss );
    qss.setReadOnly( true );
    return qss;
  }
  
  private String csstemplate=null;
  private HashMap<String,StyleSheet> cssmap = new HashMap<>();
  
  StyleSheet getStyleSheet( String mainfontfamily, String monofontfamily )
  {
    if ( csstemplate == null)
    {
      try
      {
        InputStream is = this.getClass().getResourceAsStream( "default.css" );
        Scanner scanner = new Scanner( is, "ISO-8859-1" ).useDelimiter( "\\A" );
        csstemplate = scanner.next();
        is.close();
      }
      catch (Throwable e)
      {
      }      
    }
    
    String key = mainfontfamily + ":::" + monofontfamily;
    StyleSheet ss = cssmap.get( key );

    if ( ss != null)
      return ss;
    
    AppContext appContext = AppContext.getAppContext();
    ss = new StyleSheet();
    String cssstr;
    
    cssstr = csstemplate.replaceAll( "MAINFONTINSERT", mainfontfamily );
    cssstr =      cssstr.replaceAll( "MONOFONTINSERT", monofontfamily );
    StringReader reader = new StringReader( cssstr );
    try
    {
      ss.loadRules( reader, null );
    }
    catch ( IOException ex )
    {
      Logger.getLogger( QyoutiFontManager.class.getName() ).
              log( Level.SEVERE, null, ex );
    }
    
    cssmap.put( key, ss );
    return ss;
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
      {
        searchfontinfo =  graphics.getFontInfo();
        searchfamilynames.clear();
        Typeface tf;
        Map<String,Typeface> map = searchfontinfo.getFonts();
        ArrayList<String> ffnames = new ArrayList<>();
        for ( String key : map.keySet() )
        {
          tf = map.get( key );
          for ( String ffname : tf.getFamilyNames() )
            if ( !ffnames.contains( ffname ) )
              ffnames.add( ffname );
        }
        
        ffnames.sort( 
                new Comparator<String>()
                {
                  @Override
                  public int compare( String o1, String o2 )
                  {
                    return o1.compareTo( o2 );
                  }
                }
        );
        
        searchfamilynames.addAll( ffnames );
      }
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
    //ArrayList<String> files = new ArrayList<>();

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
          path = pref.getProperty( "qyouti.print.font.path."+i  );
          f = new File( path );
          if ( !f.isAbsolute() )
            f = new File( builtin, path );

          // want to add system fonts as individual files but the fop
          // configuration wants font triplets for file entries
          // This means that we end up loading fonts from the
          // same folder as the configured ones whether we want them
          // or not.
          
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
//      for ( String s : files )
//      {
//        str.append( "    <file>" );
//        str.append( s );
//        str.append( "</files>\n" );
//      }
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
  
  
//  String getElementString( Element e )
//  {
//    String text;
//    boolean allwhitespace;
//    int j;
//    char c;
//    
//    try
//    {
//      text = e.getDocument().getText( e.getStartOffset(), e.getEndOffset()-e.getStartOffset() );
//    }
//    catch ( BadLocationException ex )
//    {
//      Logger.getLogger( QTIItemRenderer.class.getName() ).
//              log( Level.SEVERE, null, ex );
//      return null;
//    }
//
//    allwhitespace = true;
//    for ( j=0; j<text.length() && allwhitespace; j++ )
//    {
//      c = text.charAt( j );
//      if ( !Character.isWhitespace( c ) )
//        allwhitespace = false;
//    }
//
//    if ( allwhitespace )
//      return null;
//    
//    return text;
//  }
  
  void analyzeOneView( View view, MissingGlyphReport report )
  {
    int i, j;
    int n = view.getViewCount();
    View child;
    GlyphView gview;
    Element element;
    String text;
    java.awt.Font font;
    //Font fopfont;
    char c;
    boolean first = true;
    
    if ( view instanceof GlyphView )
    {
      gview = (GlyphView)view;
      element = gview.getElement();
      //text = getElementString( element );
      text = gview.getText( gview.getStartOffset(), gview.getEndOffset() ).toString();
      if ( text != null )
      {
        font = gview.getFont();

        for ( j=0; j<text.length(); j++ )
        {
          c = text.charAt( j );
          if ( !font.canDisplay( c ) )
          {
            report.addCharacter( c );
            if ( first )
            {
              System.out.println( text );
              System.out.println( "---------------------------------------------------------" );  
            }
            System.out.print( "No glyph for {" + c + "} from " ); 
            System.out.print( Character.UnicodeBlock.of( c ).toString() );
            System.out.println( " in font " + font.getFamily() + " or its backup fonts." );
            first = false;
          }
        }
        if ( !first )
          System.out.println( "---------------------------------------------------------" );
      }
    }
    
    for ( i=0; i<n; i++ )
    {
      child = view.getView( i );
      analyzeOneView( child, report );
    }
    
  }
  
  public MissingGlyphReport analyzeDocumentView( View rootview )
  {
    MissingGlyphReport report = new MissingGlyphReport();
    analyzeOneView( rootview, report );
    return report;
  }
  
  public void analyzeDocument( HTMLDocument htmldoc )
  {
    char c;
    Element e;
    FontTriplet triplet;
    Object attff, attwt, attst;
    String text;
    boolean allwhitespace;
    //StyleSheet ss = htmldoc.getStyleSheet();

    int i, j;
    for ( i=0; i<htmldoc.getLength(); i++ )
    {
      e = htmldoc.getCharacterElement( i );
      if ( e == null )
        continue;
      i = e.getEndOffset();

      try
      {
        text = htmldoc.getText( e.getStartOffset(), e.getEndOffset()-e.getStartOffset() );
      }
      catch ( BadLocationException ex )
      {
        Logger.getLogger( QTIItemRenderer.class.getName() ).
                log( Level.SEVERE, null, ex );
        text = "";
      }

      allwhitespace = true;
      for ( j=0; j<text.length() && allwhitespace; j++ )
      {
        c = text.charAt( j );
        if ( !Character.isWhitespace( c ) )
          allwhitespace = false;
      }
      
      if ( allwhitespace )
        continue;

      System.out.println( '{' + text + '}' );
      
      java.awt.Font font = htmldoc.getFont( e.getAttributes() );
      // but we need to find the corresponding FOP font because this is a
      // nasty composite AWT font with references to other fonts that can
      // deal with lots of other glyphs.
      Font fopfont = configuredfontinfo.getFontInstanceForAWTFont( font );
      System.out.println( font.toString() );
      
      for ( j=0; j<text.length(); j++ )
      {
        c = text.charAt( j );
        if ( !fopfont.hasChar( c ) )
        {
          System.out.println( "Font " + fopfont.getFontName() + " has no glyph for {" + c + "}" );
        }
      }
    }
  }
}
