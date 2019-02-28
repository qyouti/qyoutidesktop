/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.qyouti.data;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.w3c.dom.Element;

/**
 *
 * @author jon
 */
public class DataTransformInstruction
{
  ExaminationData exam;

  String ident;
  String title;
  String xslfilename;
  String outputfilename;

  File xslfile;
  File outputfile;

  TransformerFactory xformFactory;
  Transformer transformer;

  public DataTransformInstruction( ExaminationData exam, Element e )
  {
    this.exam = exam;
    ident = e.getAttribute( "ident" );
    title = e.getAttribute( "title" );
    xslfilename = e.getAttribute( "xslfile" );
    outputfilename = e.getAttribute( "outputfile" );

    xslfile = new File( exam.examcontainer, xslfilename );
    outputfile = new File( exam.examcontainer, outputfilename );

    xformFactory = TransformerFactory.newInstance(
            "org.apache.xalan.processor.TransformerFactoryImpl",
            getClass().getClassLoader());

    ClassLoader cl = this.getClass().getClassLoader();
    try
    {
      transformer = xformFactory.newTransformer( new StreamSource( xslfile ) );
    }
    catch ( TransformerConfigurationException ex )
    {
      transformer = null;
      Logger.getLogger( DataTransformInstruction.class.getName() ).log( Level.SEVERE, null, ex );
    }
}

  public void emit( Writer writer )
          throws IOException
  {
      writer.write( "    <transform ident=\"" );
      writer.write( ident );
      writer.write( "\" title=\"" + title );
      writer.write( "\" xslfile=\"" + xslfilename );
      writer.write( "\" outputfile=\"" + outputfilename );
      writer.write( "\"/>\n" );
  }

  public boolean transform()
  {
    try
    {
      transformer.clearParameters();
      exam.open();
      Path q = exam.getQyoutiFile();
      transformer.transform( new StreamSource( Files.newInputStream( q ) ), new StreamResult( outputfile ) );
      exam.close();
    }
    catch ( Exception ex )
    {
      Logger.getLogger( DataTransformInstruction.class.getName() ).log( Level.SEVERE, null, ex );
      return false;
    }
    
    return true;
  }
}
