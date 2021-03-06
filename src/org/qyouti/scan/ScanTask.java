/*
 *
 * Copyright 2010 Leeds Metropolitan University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may 
 * not use this file except in compliance with the License. You may obtain 
 * a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 *
 *
 */
 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti.scan;

//import com.sun.pdfview.PDFFile;
//import com.sun.pdfview.PDFPage;
import java.awt.Image;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import java.util.logging.*;
import javax.imageio.*;
import javax.xml.bind.*;
import org.apache.pdfbox.contentstream.*;
import org.apache.pdfbox.cos.*;
import org.apache.pdfbox.io.*;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.encryption.*;
import org.apache.pdfbox.pdmodel.graphics.image.*;
import org.bouncycastle.util.encoders.*;
import org.qyouti.*;
import org.qyouti.data.*;
import org.qyouti.scan.process.PageDecodeException;
import org.qyouti.scan.process.PageDecoder;

/**
 *
 * @author jon
 */
public class ScanTask
        extends Thread
        implements ImageObserver
{
  private static final List<String> JPEG = Arrays.asList(
            COSName.DCT_DECODE.getName(),
            COSName.DCT_DECODE_ABBREVIATION.getName());
 
  
  PageDecoder pagedecoder;
  
  QyoutiPreferences preferences;
  ScanTaskListener listener;
  ExaminationData exam;
  ArrayList<File> scanfilelist = new ArrayList<File>();
  public boolean active = false;
  //Vector<String> errorpages= new Vector<String>();
  boolean image_ready;
  int exitCode = 0;

  int imageCounter = 0;
  Path tempfolderpath;
  ArrayList<File> tempfiles=new ArrayList<File>();
  
  private final Set<COSStream> pdfimagesseen = new HashSet<COSStream>();
  MessageDigest md;
  Base64Encoder b64 = new Base64Encoder();
  
  boolean alsoimportimages=true;
  
  public ScanTask( QyoutiPreferences preferences, ExaminationData exam, File[] scanfiles )
  {
    try
    {
      md = MessageDigest.getInstance( "MD5" );
    }
    catch ( NoSuchAlgorithmException ex )
    {
      Logger.getLogger( ScanTask.class.getName() ).log( Level.SEVERE, null, ex );
      md=null;
    }

    this.preferences = preferences;
    this.exam = exam;
    if ( scanfiles != null )
    {
      this.scanfilelist = new ArrayList<File>( Arrays.asList( scanfiles ) );
      this.alsoimportimages=true;
    }
  }

  public void setScanTaskListener( ScanTaskListener listener )
  {
    this.listener = listener;
  }

  @Override
  public void run()
  {
    long start = System.currentTimeMillis();
    try
    {
      runImport();
    }
    catch ( Throwable e )
    {
      e.printStackTrace();
      exitCode = 1;
    }
    long now = System.currentTimeMillis();
    System.out.println( "Elapsed time during scanning = " + (now-start) + " ms" );
    
    if ( listener != null )
    {
      listener.scanCompleted( this );
    }
    if ( tempfolderpath != null )
    {
      try
      {
        for ( int i=0; i<tempfiles.size(); i++ )
        {
          tempfiles.get( i ).delete();
        }
        Files.delete( tempfolderpath );
      }
      catch ( IOException ioe )
      {        
      }
    }
  }

    /**
     * Writes the image to a file with the filename + an appropriate suffix, like "Image.jpg".
     * The suffix is automatically set by the
     * @param filename the filename
     * @throws IOException When something's wrong with the corresponding file.
     */
  private void importPDFImage(PDImage pdImage, ImageFileData parentpdf ) throws IOException
  {
    String suffix = "png";

    ImageFileData ifd = new ImageFileData( 
            exam, 
            parentpdf.getImportedname(),
            suffix, 
            exam.getNextScanFileIdent() );
    
    exam.addScanImageFile( ifd );

    BufferedImage image = pdImage.getImage();
    if ( image == null )
    {
      ifd.setError( "Unable to extract image from PDF." );
      return;
    }

    // no digest for file that comes from a PDF since if the user extracts
    // images from the PDF there is no knowing what kind of reenoding might
    // have taken place.  So digest works only to guard against re-importing
    // the whole PDF or separate image files.


    try
    {
      importImage( ifd, image );
    } catch (PageDecodeException ex)
    {
      Logger.getLogger(ScanTask.class.getName()).log(Level.SEVERE, null, ex);
      ifd.setError( "Unable to extract image from PDF. Because " + ex.getMessage() );
    }
  }  
  
  public String fileDigest( File file )
  {
    byte[] buffer = new byte[1024 * 128];
    
    md.reset();
    try
    (
      InputStream is = Files.newInputStream( file.toPath() );
      DigestInputStream dis = new DigestInputStream( is, md );
    )
    {
      while ( dis.read( buffer ) > 0 );
    }
    catch (Exception e)
    {
      return null;
    }
    String digest = DatatypeConverter.printBase64Binary( md.digest() );
    md.reset();
    return digest;
  }
  
  public ImageFileData importPDF( int n )
  {
    File pdffile = this.scanfilelist.get( n );
    //PDDocument document = null;
    imageCounter=0;
    ImageFileData ifd = new ImageFileData( 
            exam, 
            pdffile.getAbsolutePath(), 
            "pdf",
            exam.getNextScanFileIdent() );
    exam.addScanImageFile( ifd );

    // we have to work out the MD5 digest as a separate step
    String digest = fileDigest( pdffile);
    if ( digest == null )
    {
      ifd.setError( "Unable to read PDF file." );
      return ifd;
    }
    ifd.setDigest( digest );
    
    if ( exam.isScanImageFileImported( ifd ) )
    {
      ifd.setError( "PDF file has already been imported." );
      return ifd;
    }
    
    try ( PDDocument document = PDDocument.load( pdffile, MemoryUsageSetting.
                                  setupMixed( 1024 * 1024 * 100 ) ); )
    {      
      AccessPermission ap = document.getCurrentAccessPermission();
      if ( !ap.canExtractContent() )
        throw new IOException( "You do not have permission to extract images" );

      for ( int i = 0; i < document.getNumberOfPages(); i++ )
      {
        PDPage page = document.getPage( i );
        ImageGraphicsEngine extractor = new ImageGraphicsEngine( page, ifd );
        extractor.run();
      }
      
      
      OutputStream out = exam.getScanArchiveOutputStream( "pdfs/" + ifd.getImportedname() );
      InputStream in = new FileInputStream( pdffile );
      IOUtils.copy(in, out);
      in.close();
      out.close();
      ifd.setImported( true );
    }
    catch ( IOException ex )
    {
      ifd.setError( "Error reading the PDF file." );
      Logger.getLogger( ScanTask.class.getName() ).log( Level.SEVERE, null, ex );
    }
    
    return ifd;
  }

  
  private void importImage( ImageFileData ifd, BufferedImage image )
          throws PageDecodeException, FileNotFoundException, IOException
  {
    // Read data from page.
    PrintedPageData page = null;
    try
    {
      page = pagedecoder.decode( exam, ifd, image );
    }
    finally
    {
      if ( page!=null )
      {
        String fn = page.getPreferredFileName();
        if ( fn != null )
          ifd.setImportedname(fn);
      }
      exam.sendImageToScanArchive(image, "pages/" + ifd.getImportedname(), "png" );
      ifd.setImported( true );
    }
  }
  
  public ImageFileData importImageFile( File file )
  {
    int n = file.getName().lastIndexOf( "." );
    String suffix = (n<0)?"png":file.getName().substring( n + 1 );
    ImageFileData ifd = new ImageFileData( 
            exam, 
            file.getAbsolutePath(),
            suffix,
            exam.getNextScanFileIdent() );
    exam.addScanImageFile( ifd );

    BufferedImage image;
    md.reset();

    try
    (
      InputStream is = Files.newInputStream( file.toPath() );
      DigestInputStream dis = new DigestInputStream( is, md );
    )
    {
      image = ImageIO.read( dis );

      ifd.setDigest( DatatypeConverter.printBase64Binary( md.digest() ) );
      if ( exam.isScanImageFileImported( ifd ) )
      {
        ifd.setError( "Image file has already been imported." );
        return ifd;
      }

      importImage( ifd, image );
    }
    catch (Exception e)
    {
      e.printStackTrace();
      ifd.setError( "Error attempting the read file. Because " + e.getMessage() );
      return ifd;
    }
        
    return ifd;
  }
  
  public void runImport()
  {
    active = true;
    int i;

    int th = preferences.getPropertyInt( "qyouti.scan.threshold" );
    int inset = preferences.getPropertyInt( "qyouti.scan.inset" );
    pagedecoder = new PageDecoder( (double) th / 100.0, inset );
    try
    {
      if ( alsoimportimages )
      {
        if ( md == null ) return;
        for ( i = 0; i < scanfilelist.size(); i++ )
        {
          if ( scanfilelist.get( i ).getName().endsWith( ".png" ) ||
               scanfilelist.get( i ).getName().endsWith( ".jpg" )     )
          {
            importImageFile( scanfilelist.get( i ) );
          }

          if ( scanfilelist.get( i ).getName().endsWith( ".pdf" ) )
          {
            // pull out images from PDF to temporary files and add them to the list
            importPDF( i );
          }
        }
      }
      
      try
      {
        // work out which boxes the candidate put
        // crosses in
        pagedecoder.processBoxImages( exam );
        
        // score the items and work out other outcomes
        exam.recomputeOutcomes();
        // which candidate marks are dubious?
        exam.rebuildReviewList();
        exam.save();
        exam.processDataChanged( exam.pagelistmodel );
      }
      catch ( IOException ex )
      {
        Logger.getLogger( ScanTask.class.getName() ).log( Level.SEVERE, null, ex );
      }
      
    }
    finally
    {
      active = false;
      exam.pagelistmodel.fireTableDataChanged();
    }
  }
  
  
  @Override
  public boolean imageUpdate( Image img, int infoflags, int x, int y, int width,
                              int height )
  {
//    System.out.println( "=====================================" );
//    System.out.println( "Image flags + " + Integer.toBinaryString(infoflags) );
//    System.out.println( "=====================================" );
    image_ready = (infoflags & ImageObserver.ALLBITS) != 0;
    return !image_ready;
  }

  public int getExitCode()
  {
    return exitCode;
  }

  private class ImageGraphicsEngine
          extends PDFGraphicsStreamEngine
  {
    ImageFileData parentpdf;
    protected ImageGraphicsEngine( PDPage page, ImageFileData parentpdf )
            throws IOException
    {
      super( page );
      this.parentpdf = parentpdf;
    }

    public void run()
            throws IOException
    {
      processPage( getPage() );
    }

    @Override
    public void drawImage( PDImage pdImage )
            throws IOException
    {
      if ( pdImage instanceof PDImageXObject )
      {
        PDImageXObject xobject = (PDImageXObject) pdImage;
        // skip duplicate image (e.g. logo that appears on every page)
        if ( pdfimagesseen.contains( xobject.getCOSObject() ) )
          return;
        pdfimagesseen.add( xobject.getCOSObject() );
      }
      System.out.println( "Writing image: " + pdImage.getSuffix() );
      importPDFImage( pdImage, parentpdf );
    }

    @Override
    public void appendRectangle( Point2D p0, Point2D p1, Point2D p2, Point2D p3 )
            throws IOException
    {

    }

    @Override
    public void clip( int windingRule )
            throws IOException
    {

    }

    @Override
    public void moveTo( float x, float y )
            throws IOException
    {

    }

    @Override
    public void lineTo( float x, float y )
            throws IOException
    {

    }

    @Override
    public void curveTo( float x1, float y1, float x2, float y2, float x3,
                         float y3 )
            throws IOException
    {

    }

    @Override
    public Point2D getCurrentPoint()
            throws IOException
    {
      return new Point2D.Float( 0, 0 );
    }

    @Override
    public void closePath()
            throws IOException
    {

    }

    @Override
    public void endPath()
            throws IOException
    {

    }

    @Override
    public void strokePath()
            throws IOException
    {

    }

    @Override
    public void fillPath( int windingRule )
            throws IOException
    {

    }

    @Override
    public void fillAndStrokePath( int windingRule )
            throws IOException
    {

    }

    @Override
    public void shadingFill( COSName shadingName )
            throws IOException
    {

    }
  }

  
  public class ImageImport
  {
    BufferedImage image;
    String source;
    File imagefile;
    File pdffile;
    int pdfsequence;
  }
  
}
