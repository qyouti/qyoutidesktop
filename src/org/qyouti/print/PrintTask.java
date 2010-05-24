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
package org.qyouti.print;

import java.awt.print.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import org.apache.batik.transcoder.*;
import org.apache.batik.transcoder.print.*;
import org.qyouti.QyoutiPreferences;
import org.qyouti.qrcode.QRCodec;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 *
 * @author jon
 */
public class PrintTask extends Thread
{

  TransformerFactory xformFactory;
  DocumentBuilder docbuilder = null;
  NodeList candidates = null;

  File qyoutifile;
  File renderfolder;
  QyoutiPreferences preferences;

  public PrintTask( File qyoutifile, File renderfolder, QyoutiPreferences preferences )
  {
    this.qyoutifile = qyoutifile;
    this.renderfolder = renderfolder;
    this.preferences = preferences;

    xformFactory = TransformerFactory.newInstance(
            "org.apache.xalan.processor.TransformerFactoryImpl",
            getClass().getClassLoader());
    try
    {
      docbuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    }
    catch (ParserConfigurationException ex)
    {
      Logger.getLogger(PrintTask.class.getName()).log(Level.SEVERE, null, ex);
    }
  }


  public void loadQyouti() throws SAXException, IOException
  {
    Document doc = docbuilder.parse( qyoutifile );
    NodeList nl = doc.getElementsByTagName( "candidates" );
    if ( nl.getLength() ==  0 )
      candidates = null;
    else
    {
      Element e = (Element)nl.item( 0 );
      candidates = e.getElementsByTagName( "candidate" );
    }
  }

  public int countCandidates()
  {
    return candidates.getLength();
  }

  public Element getCandidate( int n )
  {
    return (Element) candidates.item( n );
  }

  public void transform(String xsl, String[] param_names, String[] param_values, File input, File output) throws TransformerConfigurationException, TransformerException
  {
    int i;

    if (xsl == null || input == null || output == null)
      throw new IllegalArgumentException("Transform element needs style, input and output attributes.");

    Transformer transformer;

    ClassLoader cl = this.getClass().getClassLoader();
    transformer = xformFactory.newTransformer(
            new StreamSource(cl.getResource("org/qyouti/print/xslt/" + xsl).toString()));
    if (param_names != null)
    {
      for (i = 0; i < param_names.length; i++)
      {
        transformer.setParameter(param_names[i], param_values[i]);
        System.out.println("Setting param " + param_names[i] + " to " + param_values[i]);
      }
    }

    String src = "file:///" + input.getAbsolutePath();
    String dst = "file:///" + output.getAbsolutePath();
    System.out.println( "src = " + src );
    System.out.println( "dst = " + dst );
    transformer.transform(
            new StreamSource(  src  ),
            new StreamResult(  dst  )  );

  }

  public String transform(String xsl, String[] param_names, String[] param_values, File input) throws TransformerConfigurationException, TransformerException, IOException
  {
    File tmp = File.createTempFile( "qtirender", ".txt" );
    transform(xsl, param_names, param_values, input, tmp);
    StringBuffer buffer = new StringBuffer();
    int c;
    FileReader reader = new FileReader( tmp );
    while ( (c=reader.read()) >= 0 )
      buffer.append( (char)c );
    reader.close();
    return buffer.toString();
  }

  public void writeToFile( String content, File file ) throws IOException
  {
    FileWriter writer = new FileWriter( file );
    writer.write( content );
    writer.close();
  }

  @Override
  public void run()
  {
    File outfile =    new File( renderfolder, "qti.xml" );
    File svgfile =    new File( renderfolder, "vectored.svg" );
    File pagsvgfile = new File( renderfolder, "vectored-paginated.svg" );
    File page =       new File( renderfolder, "page.svg" );

    File imgfolder;
    String[] pnames;
    String[] pvalues;
    String strcount, postscript, command[];
    int pages;

    try
    {
      loadQyouti();
      
      //PrinterJob printerjob = PrinterJob.getPrinterJob();
      //if ( !printerjob.printDialog() ) return;

      // convert text elements in qti file into image elements
      pnames  = new String[] {
                    "item-width",
                    "response-width",
                    "font-family"
                  };
      pvalues = new String[] {
                    "550",
                    "530",
                    preferences.getProperty("qyouti.print.font-family")
                  };
      transform("convertmaterial.xsl", pnames, pvalues, qyoutifile, outfile);


      QRCodec.init();
      QRCodec.setErrorCorrection( preferences.getQRCodeErrorCorrection() );
      // Make a big SVG file with each question in its own 'g' element
      System.out.println("making SVG file");
      transform("qti2svg.xsl", null, null, outfile, svgfile);
      System.out.println("Done making SVG file");


      // Group questions into pages
      transform("paginate.xsl", null, null, svgfile, pagsvgfile );


      // How many pages?
      strcount = transform("pagecount.xsl", null, null, pagsvgfile );
      pages = Integer.parseInt( strcount );
      System.out.println( "Page count = " + pages );


      PrintTranscoder printtranscoder = new PrintTranscoder();
      printtranscoder.addTranscodingHint(  PrintTranscoder.KEY_SHOW_PRINTER_DIALOG, Boolean.TRUE );
      //printtranscoder.addTranscodingHint(  PrintTranscoder.KEY_SHOW_PAGE_DIALOG,    Boolean.TRUE );
      printtranscoder.addTranscodingHint(  PrintTranscoder.KEY_SCALE_TO_PAGE,       Boolean.TRUE );
      printtranscoder.addTranscodingHint(  PrintTranscoder.KEY_MARGIN_BOTTOM, new Float(0.01) );
      printtranscoder.addTranscodingHint(  PrintTranscoder.KEY_MARGIN_LEFT,   new Float(0.01) );
      printtranscoder.addTranscodingHint(  PrintTranscoder.KEY_MARGIN_RIGHT,  new Float(0.01) );
      printtranscoder.addTranscodingHint(  PrintTranscoder.KEY_MARGIN_TOP,    new Float(0.01) );
      //printtranscoder.addTranscodingHint(  PrintTranscoder.K,     );
      // Repeat each page for each candidate
      Element candidate;
      TranscoderInput traninput;
      for ( int i=0; i<countCandidates(); i++ )
      {
        for ( int j=0; j<=pages; j++ )
        {
          candidate = getCandidate( i );
          pnames  = new String[] { "candidate-name", "candidate-number", "page" };
          pvalues = new String[] { candidate.getAttribute("name"),
                                   candidate.getAttribute("id"),
                                   Integer.toString( j ) };
          page =    new File( renderfolder, "page_" + (i+1) + "_" + j + ".svg" );
          transform("pageprint.xsl", pnames, pvalues,
                  pagsvgfile,
                  page );

          traninput = new TranscoderInput( "file:///" + page.getCanonicalPath() );
          printtranscoder.transcode( traninput, new TranscoderOutput() );
        }
      }

      printtranscoder.print();
      //printerjob.setPrintable( printtranscoder );
      //printerjob.print();
    }
    catch (Exception ex)
    {
      Logger.getLogger(PrintTask.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

}
