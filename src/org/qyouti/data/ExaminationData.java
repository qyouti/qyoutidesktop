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

package org.qyouti.data;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.qyouti.qti1.element.QTIElementItem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 *
 * @author jon
 */
public class ExaminationData
        extends AbstractTableModel
{
  public Hashtable<String,CandidateData> candidates = new Hashtable<String,CandidateData>();
  public Vector<CandidateData> candidates_sorted = new Vector<CandidateData>();

  public Hashtable<String,Element> assessmentitemelements = new Hashtable<String,Element>();
  public Vector<Element> assessmentitemelementssorted = new Vector<Element>();

  public QuestionDefinitions qdefs = null;

  public Hashtable<String,QuestionAnalysis> analysistable = null;
  public Vector<QuestionAnalysis> analyses = null;

  public File examfile;


  public ExaminationData( File xmlfile )
  {
    examfile=xmlfile;
  }



  public void itemAnalysis()
  {
    if ( qdefs == null )
      return;
    if ( qdefs.qti == null )
      return;
    analyses = new Vector<QuestionAnalysis>();
    analysistable = new Hashtable<String,QuestionAnalysis>();

    qdefs.itemAnalysis( candidates_sorted, analyses );
    ResponseAnalysis ranal;
    System.out.print( ",,,\"No. Students Right\",\"No. Students Wrong\",\"% Class Right\",\"Median Aptitude Difference\",\"Lower 95% limit\",\"Upper 95% limit\",,,,,,,\n" );
    for ( int i=0; i<analyses.size(); i++ )
    {
      for ( int j=0; j<analyses.get(i).response_analyses.size(); j++ )
      {
        ranal = analyses.get(i).response_analyses.get(j);

        if ( j==0 )
          System.out.print( "\"Question " + analyses.get(i).offset + "\"");
        else
          System.out.print( "\"\"");
        System.out.print( ",\"" );
        System.out.print( (char)('a' + ranal.offset -1) );
        System.out.print( "\"," );
        System.out.print( ranal.correct?"\"T\"":"\"F\"" );
        System.out.print( "," );
        System.out.print( ranal.right );
        System.out.print( "," );
        System.out.print( ranal.wrong );
        System.out.print( "," );
        if ( (ranal.right + ranal.wrong) > 0 )
          System.out.print( (double)ranal.right /  (double)(ranal.right + ranal.wrong) );
        
        if ( ranal.right <2 || ranal.wrong <2 )
        {
          System.out.print( ",,," );
          if ( ranal.right + ranal.wrong < 10 )
            System.out.print( ",*,,,,,,\"Not enough data.\"" );
          else
          {
            if ( ranal.right > ranal.wrong )
              System.out.print( ",,*,,,,,\"Too easy. Can't calculate stats.\"" );
            else if ( ranal.right < ranal.wrong )
              System.out.print( ",,,*,,,,\"Too difficult. Can't calculate stats.\"" );
            else
              System.out.print( ",*,,,,,,\"Not enough data.\"" );
          }
        }
        else
        {
          System.out.print( "," );
          System.out.print( ranal.median_difference );
          System.out.print( "," );
          System.out.print( ranal.median_difference_lower );
          System.out.print( "," );
          System.out.print( ranal.median_difference_upper );
          if ( ranal.median_difference_lower >= 0.0 && ranal.median_difference_upper > 0.0)
          {
            System.out.print( ",,,,*,,,\"Positive discriminator.\"" );
          }
          else if ( ranal.median_difference_upper <= 0.0 && ranal.median_difference_lower < 0.0)
          {
            System.out.print( ",,,,,*,,\"NEGATIVE DISCRIMINATOR!!\"" );
          }
          else
          {
            System.out.print( ",,,,,,*,\"No significant discrimination\"" );
          }
        }
        System.out.print( "\n" );
        if ( (j+1) == analyses.get(i).response_analyses.size() )
          System.out.print( ",,,,,,,,,,,,,,\n" );
      }
    }
  }


  public void importCandidates( File xmlfile )
          throws ParserConfigurationException, SAXException, IOException
  {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document document = builder.parse( xmlfile );

    Element roote = document.getDocumentElement();
    System.out.println( roote.getNodeName() );
    NodeList nl = roote.getElementsByTagName( "candidate" );
    Element ecandidate;
    CandidateData candidate;
    for ( int i=0; i<nl.getLength(); i++ )
    {
      ecandidate = (Element)nl.item( i );
      candidate = new CandidateData( this, ecandidate.getAttribute( "name" ), ecandidate.getAttribute("id") );
      candidates.put( candidate.id, candidate );
      candidates_sorted.add( candidate );
      fireTableDataChanged();
    }
  }


  public void importCsvCandidates( File csvfile )
          throws ParserConfigurationException, SAXException, IOException
  {
    int i, j, lastnamecolumn=-1, firstnamecolumn=-1, idcolumn=-1;
    CandidateData candidate;
    CSVReader reader = new CSVReader( new FileReader( csvfile ) );
    String[] line = reader.readNext();
    for ( j=0; j<line.length; j++ )
    {
      System.out.println( line[j] );
      if ( "Last Name".equals( line[j] ) )
        lastnamecolumn = j;
      if ( "First Name".equals( line[j] ) )
        firstnamecolumn = j;
      if ( "User ID".equals( line[j] ) )
        idcolumn = j;
    }
    if ( lastnamecolumn<0 || firstnamecolumn<0 || idcolumn<0 )
      throw new IOException( "CSV file doesn't contain the necessary columns: 'Last Name', 'First Name' and 'User ID'." );
    while ( (line = reader.readNext()) != null )
    {
      for ( j=0; j<line.length; j++ ) System.out.println( line[j] );
      candidate = new CandidateData( this, line[firstnamecolumn] + " " + line[lastnamecolumn], line[idcolumn] );
      candidates.put( candidate.id, candidate );
      candidates_sorted.add( candidate );
      fireTableDataChanged();
    }
  }

  public void exportCsvScores( File csvfile )
          throws ParserConfigurationException, SAXException, IOException
  {
    int i;
    CandidateData candidate;
    CSVWriter csvwriter = new CSVWriter( new FileWriter( csvfile ) );
    String[] line = new String[3];

    for ( i=0; i < candidates_sorted.size(); i++ )
    {
      candidate = candidates_sorted.get( i );
      line[0]=candidate.id;
      line[1]=candidate.name;
      line[2]="";
      if ( candidate.score != null )
        line[2]=candidate.score.toString();
      csvwriter.writeNext( line );
    }
    csvwriter.close();
  }

  public void importQuestions( File imsmanifest )
          throws ParserConfigurationException, SAXException, IOException
  {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document manifestdoc = builder.parse( imsmanifest );

    Element roote = manifestdoc.getDocumentElement();
    System.out.println( roote.getNodeName() );
    NodeList nl = roote.getElementsByTagName( "resources" );
    if ( nl.getLength() != 1 )
      return;

    Element resources = (Element)nl.item( 0 );
    Element resource;
    Element imsresource = null;
    nl = resources.getElementsByTagName( "resource" );
    // Look for first resource which is a QTI 1.2 file
    for ( int i=0; i<nl.getLength(); i++ )
    {
      resource = (Element)nl.item( i );
      if ( !"ims_qtiasiv1p2".equals( resource.getAttribute( "type" ) ) )
        continue;
      imsresource = resource;
    }

    if ( imsresource == null )
      return;

    nl = imsresource.getElementsByTagName( "file" );
    if ( nl.getLength() != 1 )
      return;
    Element fileelement = (Element)nl.item( 0 );

    File qtiexamfile = new File( imsmanifest.getParentFile(), fileelement.getAttribute( "href" ) );
    if ( !qtiexamfile.exists() || !qtiexamfile.isFile() )
      return;

    Document qti12doc = builder.parse( qtiexamfile );
    Element qtiexamroote = qti12doc.getDocumentElement();
    System.out.println( qtiexamroote.getNodeName() );
    nl = qtiexamroote.getElementsByTagName( "assessment" );
    if ( nl.getLength() != 1 )
      return;

    Element assessment = (Element)nl.item( 0 );
    nl = assessment.getElementsByTagName( "section" );
    if ( nl.getLength() != 1 )
      return;

    Element section = (Element)nl.item( 0 );
    Element itemref;
    Vector<Element> itemrefs = new Vector<Element>();
    nl = assessment.getElementsByTagName( "itemref" );
    for ( int i=0; i<nl.getLength(); i++ )
    {
      itemref = (Element)nl.item( i );
      itemrefs.add( itemref );
    }

    // scan directory that holds the qti exam and look for questions.
    File parent = qtiexamfile.getParentFile();
    File[] files = parent.listFiles( new XMLFileFilter() );
    Document qdocument;
    Element qroote, qelement;
    String elementname;
    for ( int i=0; i< files.length; i++ )
    {
      if ( !files[i].isFile() )
        continue;
      try
      {
        qdocument = builder.parse( files[i] );
      }
      catch ( Exception ex )
      {
        continue;  // not well formed XML
      }
      qroote = qdocument.getDocumentElement();
      // well formed xml but what is in it?
      elementname = qroote.getNodeName();
      if ( !"questestinterop".equals( elementname ) )
        continue;
      //if ( !"http://www.imsglobal.org/xsd/ims_qtiasiv1p2".equals(qroote.getNamespaceURI() ) )
      //  continue;

      //This is a qti file so look for questions inside top level
      nl = qroote.getElementsByTagName( "item" );
      for ( int j=0; j<nl.getLength(); j++ )
      {
        qelement = (Element)nl.item( j );
        assessmentitemelements.put( qelement.getAttribute("ident"), qelement );
      }
    }

    // now get the questions in the order defined in the qti exam file.
    // and 'transplant' the nodes
    String ident;
    Node duplicate;
    for ( int i=0; i< itemrefs.size(); i++ )
    {
      itemref = itemrefs.get( i );
      ident = itemref.getAttribute( "linkrefid" );
      qelement = assessmentitemelements.get( ident );
      if ( qelement != null )
      {
        duplicate = qti12doc.importNode( qelement, true );
        itemref.getParentNode().appendChild( duplicate );
      }
      itemref.getParentNode().removeChild( itemref );
    }
    qti12doc.normalizeDocument();
    qdefs = new QuestionDefinitions( qtiexamroote );
    
    //  qelement = assessmentitemelements.get( refids.get( i ) );
    //  if ( qelement != null )
    //    assessmentitemelementssorted.add( qelement );
  }

  class XMLFileFilter implements FilenameFilter
  {
    public boolean accept(File dir, String name)
    {
      return name.endsWith(".xml");
    }
  }


  public CandidateData addPage( PageData page )
  {
    CandidateData candidate = candidates.get( page.candidate_number );
    if ( candidate == null )
    {
      candidate = new CandidateData( this, page.candidate_name, page.candidate_number );
      candidates.put( page.candidate_number, candidate );
      candidates_sorted.add( candidate );
    }
    candidate.pages.add( page );
    fireTableDataChanged();
    return candidate;
  }


  public QTIElementItem getAssessmentItem( String id )
  {
    return qdefs.qti.getItem( id );
  }


  public boolean save()
  {
    Writer writer = null;
    try
    {
      writer = new OutputStreamWriter(new FileOutputStream( examfile ), "utf8");
      emit(writer);
      writer.close();
    } catch (Exception ex)
    {
      Logger.getLogger(ExaminationData.class.getName()).log(Level.SEVERE, null, ex);
      return false;
    } finally
    {
      try
      {
        writer.close();
      } catch (IOException ex)
      {
        Logger.getLogger(ExaminationData.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
    return true;
  }


  public void emit( Writer writer )
          throws IOException
  {
      writer.write( "<?xml version=\"1.0\"?>\n" );
      writer.write( "<examination>\n" );

      if ( qdefs != null )
      {
        try
        {
          qdefs.emit(writer);
        } catch (TransformerConfigurationException ex)
        {
          Logger.getLogger(ExaminationData.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex)
        {
          Logger.getLogger(ExaminationData.class.getName()).log(Level.SEVERE, null, ex);
        }
      }

      writer.write( "<candidates>\n" );
      Enumeration<CandidateData> e = candidates.elements();
      for ( int i=0; i<candidates_sorted.size(); i++ )
        candidates_sorted.get( i ).emit( writer );
      writer.write( "</candidates>\n" );

      writer.write( "<analysis>\n" );
      if ( analyses != null )
      {
        for ( int i=0; i<analyses.size(); i++ )
          analyses.get(i).emit(writer);
      }
      writer.write( "</analysis>\n" );

      writer.write( "</examination>\n" );
  }


  public void load()
          throws ParserConfigurationException, SAXException, IOException
  {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document document = builder.parse( examfile );

    Element roote = document.getDocumentElement();
    System.out.println( roote.getNodeName() );
    NodeList nl = roote.getChildNodes();
    NodeList cnl;
    Element e;
    Node node;
    CandidateData candidate;

    for ( int i=0; i<nl.getLength(); i++ )
    {
      node = nl.item(i);
      if ( !(node instanceof Element) )
        continue;
      e = (Element)node;
      if ( "questestinterop".equals( e.getNodeName() ) )
      {
        qdefs = new QuestionDefinitions( e );
      }


      if ( "candidates".equals( e.getNodeName() ) )
      {
        cnl = e.getElementsByTagName( "candidate" );
        for ( int j=0; j<cnl.getLength(); j++ )
        {
          candidate = new CandidateData( this, (Element)cnl.item( j ) );
        }
      }
    }
    fireTableDataChanged();
  }



  public int getRowCount()
  {
    return this.candidates.size();
  }

  public int getColumnCount()
  {
    return 7;
  }

  @Override
  public String getColumnName(int columnIndex)
  {
    System.out.println("getColumnName");
    switch ( columnIndex )
    {
      case 0:
        return "*";
      case 1:
        return "Name";
      case 2:
        return "ID";
      case 3:
        System.out.println("SCORE COLUMN");
        return "Score";
      case 4:
        return "Pages";
      case 5:
        return "Questions";
      case 6:
        return "Errors";
    }
    return null;
  }

  @Override
  public Class getColumnClass(int columnIndex)
  {
    switch ( columnIndex )
    {
      case 0:
        return Object.class;
      case 1:
        return String.class;
      case 2:
        return String.class;
      case 3:
        return Double.class;
      case 4:
        return Integer.class;
      case 5:
        return Integer.class;
      case 6:
        return String.class;
    }
    return null;
  }

  @Override
  public boolean isCellEditable( int rowIndex, int columnIndex )
  {
    return false;
  }

  public Object getValueAt(int rowIndex, int columnIndex)
  {
    CandidateData candidate = candidates_sorted.get( rowIndex );
    switch ( columnIndex )
    {
      case 0:
        return null;
      case 1:
        return candidate.name;
      case 2:
        return candidate.id;
      case 3:
        return candidate.score;
      case 4:
        return new Integer( candidate.pages.size() );
      case 5:
        return new Integer( candidate.questionsScanned() );
      case 6:
        if ( candidate.questionsScanned() < qdefs.qti.getItems().size() )
          return "Unscanned questions.";
        if ( candidate.questionsScanned() > qdefs.qti.getItems().size() )
          return "Too many scanned questions!";
        return "";
    }
    return null;
  }
}

