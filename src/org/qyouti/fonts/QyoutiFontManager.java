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
import org.apache.fontbox.util.autodetect.FontFileFinder;
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
        "  <use-cache>true</use-cache>\n" +
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
  
  private String toCSV( java.util.List<String> list )
  {
    StringBuffer buffer = new StringBuffer();
    boolean started = false;
    for ( String s : list )
    {
      if ( started ) buffer.append( ',' );
      buffer.append( s );
      started = true;
    }
    return buffer.toString();
  }
  public boolean reInitialise()
  {
    pref.setProperty( "qyouti.print.font-family-sans", toCSV( sansfamilynames ) );
    pref.setProperty( "qyouti.print.font-family-serif", toCSV( seriffamilynames ) );
    pref.setProperty( "qyouti.print.font-family-monospace", toCSV( monofamilynames ) );
    
    if ( this.searchfontinfo == null ) return false;

    // List of all the family names that will be used
    ArrayList<String> familys = new ArrayList<>();
    for ( ArrayList<String> list : this.familynames )
      familys.addAll( list );
    
    // All the font key names we will use keying to their font triplets
    HashMap<String,ArrayList<FontTriplet>> keysandtriplets = new HashMap<>();
    String fontkey;

    // iterate the triplets to decide which to put in allfontkeys
    Map<FontTriplet, String> tripletmap = this.searchfontinfo.getFontTriplets();
    ArrayList<FontTriplet> tripletlist;
    for ( FontTriplet triplet : tripletmap.keySet() )
    {
      if ( familys.contains( triplet.getName() ) )
      {
        fontkey = tripletmap.get( triplet );
        if ( !keysandtriplets.containsKey( fontkey ) )
          keysandtriplets.put( fontkey, new ArrayList<FontTriplet>() );
        tripletlist = keysandtriplets.get( fontkey );
        tripletlist.add( triplet );
      }
    }
    
    StringBuffer buffer = new StringBuffer();
    Typeface tf;
    CustomFont cf;
    for ( String key : keysandtriplets.keySet() )
    {
      tf = searchfontinfo.getFonts().get( key );
      tripletlist = keysandtriplets.get( key );
      if ( tf instanceof LazyFont )
        tf = ((LazyFont)tf).getRealFont();
      if ( !(tf instanceof CustomFont) )
        continue;
      cf = (CustomFont)tf;
      buffer.append( "<font embed-url=\"" );
      buffer.append( cf.getEmbedFileURI() );
      buffer.append( "\">\n" );
      for ( FontTriplet ft : tripletlist )
      {
        buffer.append( "<font-triplet name=\"" );
        buffer.append( ft.getName() );
        buffer.append( "\" style=\"" );
        buffer.append( ft.getStyle() );
        buffer.append( "\" weight=\"" );
        buffer.append( ft.getWeight() );
        buffer.append( "\"/>\n" );
      }
      buffer.append( "</font>\n" );
    }
    pref.setProperty( "qyouti.print.font.fopconfig", CONFIGXML.replace( "INSERT", buffer.toString() ) );
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
      
        ArrayList<String> ffnames = new ArrayList<>();
        Map<FontTriplet,String> tripmap = searchfontinfo.getFontTriplets();
        Typeface tf;
        CustomFont cf;
        java.awt.Font awtfont;
        String familyname;
        
        for ( FontTriplet trip : tripmap.keySet() )
        {
          // the triplet has a family name but what is the 'oficial'
          // awt family name?
          awtfont = new java.awt.Font( trip.getName(), java.awt.Font.PLAIN, 12 );
          familyname = awtfont.getFamily();
          
          if ( !ffnames.contains( familyname ) )
          {
            tf = searchfontinfo.getFonts().get( tripmap.get( trip ) );
            if ( tf instanceof LazyFont )
              tf = ((LazyFont)tf).getRealFont();
            if ( tf instanceof CustomFont )
            {
              cf = (CustomFont)tf;
              if ( cf.isEmbeddable() )
                ffnames.add( familyname );
            }
          }
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
  
  private java.util.List<File> getSystemFontDirectories()
  {
    FontFileFinder fontFileFinder = new FontFileFinder();
    java.util.List<URI> systemFontList = fontFileFinder.find();
    ArrayList<File> directories=new ArrayList<>();
    for ( URI uri : systemFontList )
    {
      System.out.println( uri.toString() );
      if ( "file".equals(uri.getScheme()) )
      {
        File file = new File( uri );
        File dir = file.getParentFile();
        if ( !directories.contains( dir ) )
          directories.add( dir );
      }
    }
    
    for ( File dir : directories )
      System.out.println( "System font directory: " + dir.toString() );
    return directories;
  }
  
  private String getFOPConfigurationString( boolean search )
  {
    if ( !search )
      return pref.getProperty( "qyouti.print.font.fopconfig" );
    
    java.util.List<File> syslist = getSystemFontDirectories();
    
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
      str.append( "    <directory>" );
      str.append( builtin.getAbsolutePath() );
      str.append( "</directory>\n" );
    }
 
    // get rid of auto detect because it wastes time searching in all
    // the jar files
    //str.append( "<auto-detect/>\n" );
    for ( File sysdir : syslist )
    {
      str.append( "    <directory>" );
      str.append( sysdir.getAbsolutePath() );
      str.append( "</directory>\n" );      
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
        URI uri = juc.getJarFileURL().toURI();
        here = new File( uri.getPath() );
        here = here.getParentFile();
        return new File( here, "fonts" );
      }
      catch ( IOException ex ) {
        Logger.getLogger(QyoutiFontManager.class.getName() ).log( Level.SEVERE, null, ex );
      }
      catch (URISyntaxException ex)
      {
        Logger.getLogger(QyoutiFontManager.class.getName()).log(Level.SEVERE, null, ex);
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
