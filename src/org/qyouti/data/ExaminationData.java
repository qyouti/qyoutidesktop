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
import java.math.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.text.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.fop.apps.*;
import org.qyouti.clipboard.*;
import org.qyouti.compositefile.CompositeFile;
import org.qyouti.qti1.element.QTIElementItem;
import org.qyouti.qti1.element.QTIElementMaterial;
import org.qyouti.qti1.element.QTIElementMattext;
import org.qyouti.qti1.gui.QTIRenderOptions;
import org.qyouti.qti1.gui.QuestionMetricsRecordSetCache;
import org.qyouti.statistics.Histogram;
import org.qyouti.templates.ItemTemplate;
import org.qyouti.util.QyoutiUtils;
import org.qyouti.xml.QyoutiDocBuilderFactory;
import org.qyouti.xml.StringProcessor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.*;


/**
 *
 * @author jon
 */
public class ExaminationData
        extends AbstractTableModel implements QTIRenderOptions
{
  public ExaminationCatalogue examcatalogue = null;
  LinkedList<ExaminationDataStatusListener> listeners = new LinkedList<ExaminationDataStatusListener>();
        
  public Hashtable<String, PersonData> persons = new Hashtable<>();
  public ArrayList<PersonData> persons_sorted = new ArrayList<>();
  public PersonListModel personlistmodel = new PersonListModel( this, persons_sorted );
  
  public Hashtable<String, CandidateData> candidates = new Hashtable<String, CandidateData>();
  public Vector<CandidateData> candidates_sorted = new Vector<CandidateData>();
  
  public QuestionDefinitions qdefs = null;
  public ArrayList<QuestionAnalysis> analyses = new ArrayList<>();
  public QuestionAnalysisTable analysistablemodel = new QuestionAnalysisTable( this, analyses );
  File examfolder;
  File examfile;
  File questionfile;
  File examinerfile;
  CompositeFile scanarchive;
  CompositeFile responsearchive;
  
  private Vector<PageData> pages = new Vector<PageData>();
  public HashMap<String,PageData> pagemap = new HashMap<String,PageData>();
  public PageListModel pagelistmodel = new PageListModel( pages );

  public ExaminerData examinerdata = null;
  
  public ImageFileTable scans = new ImageFileTable( this );
  
  Vector<DataTransformInstruction> datatransforminstructions = new Vector<DataTransformInstruction>();

  public Properties options = new Properties();
  public Properties default_options = new Properties();

  public QuestionMetricsRecordSetCache qmrcache;

  String lastprintid=null;
  boolean unsaved_changes=false;

  OutcomeDataListener outcomelistener = new OutcomeDataListener();
  ArrayList<String> outcomenames = new ArrayList<String>();
 
  public QuestionReviewTable reviewlist = new QuestionReviewTable( this );
  public static final int REVIEW_ALL = 1;
  public static final int REVIEW_BY_QUESTION = 2;
  public static final int REVIEW_BY_CANDIDATE = 3;

  public static final int REVIEW_FILTER_ALL = 1;
  public static final int REVIEW_FILTER_UNREVIEWED = 2;
  public static final int REVIEW_FILTER_OVERRIDDEN = 3;
  public static final int REVIEW_FILTER_CONFIRMED = 4;
    
  int reviewtype = REVIEW_ALL;
  int reviewfilter = REVIEW_FILTER_ALL;
  String reviewcandidateident = null;
  String reviewquestionident = null;
  
  int nextscanfileident = 10000;
  
  public ExaminationData(ExaminationCatalogue examcatalogue,File examfolder)
          throws IOException
  {
    this.examcatalogue = examcatalogue;
    this.examfolder = examfolder;
    examfile        = new File( examfolder, "qyouti.xml" );
    questionfile    = new File( examfolder, "questions.xml" );
    examinerfile    = new File( examfolder, "examiner.xml" );
    scanarchive     = CompositeFile.getCompositeFile(new File( examfolder, "scans.tar" ));
    responsearchive = CompositeFile.getCompositeFile(new File( examfolder, "responses.tar" ));
    qmrcache = new QuestionMetricsRecordSetCache( examfolder );
    default_options.setProperty( "name_in_footer", "true" );
    default_options.setProperty( "id_in_footer", "true" );
    default_options.setProperty( "columns", "1" );
  }

  public void close() throws IOException
  {
    scanarchive.close();
    responsearchive.close();
  }
  
  // Gather all use of "fireTable***" to methods here to make it hard to
  // forget that changes in one table may involve changes in others...
  public void processRowsInserted( ImageFileTable model, int first, int last )
  {
    model.fireTableRowsInserted( first, last );
  }  
  public void processRowsInserted( OutcomeData model, int first, int last )
  {
    model.fireTableRowsInserted( first, last );
  }
  public void processRowsDeleted( OutcomeData model, int first, int last )
  {
    model.fireTableRowsDeleted( first, last );
  }
  public void processDataChanged( OutcomeData model )
  {
    model.fireTableDataChanged();
  }
  public void processDataChanged( PageListModel model )
  {
    model.fireTableDataChanged();
  }
  public void processDataChanged( QuestionAnalysisTable model )
  {
    model.fireTableStructureChanged();
  //model.fireTableDataChanged();
  }
  public void processDataChanged( QuestionDefinitions model )
  {
    model.fireTableDataChanged();
  }
  public void processDataChanged( QuestionData model )
  {
    model.fireTableDataChanged();
  }
  public void processRowsUpdated( QuestionData model, int first, int last )
  {
    reviewlist.fireTableRowsUpdated( 0, reviewlist.getRowCount()-1 );    
    model.fireTableRowsUpdated( first, last );
  }
  public void processRowsInserted( QuestionReviewTable model, int first, int last )
  {
    model.fireTableRowsInserted( first, last );
  }
  public void processRowsDeleted( QuestionReviewTable model, int first, int last )
  {
    model.fireTableRowsDeleted( first, last );
  }
  
  
  public String getNextScanFileIdent()
  {
    return Integer.toString( nextscanfileident++ );
  }
  
  public File getExamFolder()
  {
    return examfile.getParentFile();
  }
  
  public CompositeFile getScanImageArchive()
  {
    return scanarchive;
  }
  
  public CompositeFile getResponseImageArchive()
  {
    return responsearchive;
  }
  
  public void addExaminationDataStatusListener( ExaminationDataStatusListener listener )
  {
    listeners.add( listener );
  }
  
  
  void fireStatusChange()
  {
    Iterator<ExaminationDataStatusListener> iterator = listeners.iterator();
    while ( iterator.hasNext() )
      iterator.next().examinationDataStatusChanged( this );
  }


  public QuestionAnalysis getQuestionAnalysis( String ident )
  {
    if ( analyses == null ) return null;
    for ( int i=0; i<analyses.size(); i++ )
      if ( analyses.get( i ).ident.equals( ident ) )
        return analyses.get( i );
    return null;
  }

  public int getReviewType()
  {
    return reviewtype;
  }

  public void setReviewType( int reviewtype )
  {
    reviewcandidateident = null;
    reviewquestionident = null;
    this.reviewtype = reviewtype;
    rebuildReviewList();
  }

  public int getReviewFilter()
  {
    return reviewfilter;
  }

  public void setReviewFilter( int reviewfilter )
  {
    this.reviewfilter = reviewfilter;
    rebuildReviewList();
  }
  
  public String getReviewCandidateIdent()
  {
    return reviewcandidateident;
  }
  
  public void setReviewCandidateIdent( String reviewcandidateident )
  {
    this.reviewcandidateident = reviewcandidateident;
    // rebuild the table model that lists question data for this
    // candidate...
    rebuildReviewList();
  }

  public String getReviewQuestionIdent()
  {
    return reviewquestionident;
  }
  
  public void setReviewQuestionIdent( String reviewquestionident )
  {
    this.reviewquestionident = reviewquestionident;
    // rebuild the table model that lists question data for this
    // question...
    rebuildReviewList();
  }  


  public ItemTemplate getItemTemplate( QTIElementItem item )
  {
    String cn = item.getTemplateClassName();
    if ( cn == null || cn.length() == 0 )
      cn = "org.qyouti.templates.NoTemplate";
    
    try
    {
      Class c = Class.forName( cn );
      if ( !ItemTemplate.class.isAssignableFrom( c ) )
        return null;
      ItemTemplate it = (ItemTemplate)c.newInstance();
      it.setItem( item, examinerdata.examinerqdefs );
      return it;
    }
    catch ( Exception ex )
    {
      Logger.getLogger( QTIElementItem.class.getName() ).log( Level.SEVERE, null, ex );
    }
    return null;    
  }
    


  public ExaminerCandidateData getExaminerCandidateData( String candidateident, boolean create )
  {
    ExaminerCandidateData ecd = examinerdata.cmap.get( candidateident );
    if ( ecd != null ) return ecd;
    if ( create)
    {
      ecd = new ExaminerCandidateData( candidateident );
      this.examinerdata.cmap.put(candidateident, ecd);
    }
    return ecd;
  }
  
  public ExaminerQuestionData getExaminerQuestionData( String candidateident, String questionident, boolean create )
  {
    ExaminerCandidateData ecd = getExaminerCandidateData( candidateident, create );
    if ( ecd == null ) return null;
    ExaminerQuestionData eqd = ecd.qmap.get(questionident);
    if ( eqd != null) return eqd;
    if ( create )
    {
      eqd = new ExaminerQuestionData( questionident );
      ecd.qmap.put(questionident, eqd);
    }
    return eqd;
  }

  public ExaminerResponseData getExaminerResponseData( String candidateident, String questionident, String responseident, boolean create )
  {
    ExaminerQuestionData eqd = getExaminerQuestionData( candidateident, questionident, create );
    if ( eqd == null ) return null;
    ExaminerResponseData erd = eqd.rmap.get(responseident);
    if ( erd != null) return erd;
    if ( create )
    {
      erd = new ExaminerResponseData( responseident );
      eqd.rmap.put(responseident, erd);
    }
    return erd;
  }  
  
  public OutcomeData getCandidateOutcomes( String candidateident )
  {
    ExaminerCandidateData ecd = getExaminerCandidateData( candidateident, true );
    if ( ecd == null ) return null;
    if ( ecd.outcomes == null )
      ecd.outcomes = new OutcomeData( this );
    return ecd.outcomes;
  }
  
  public OutcomeData getQuestionOutcomes( String candidateident, String questionident )
  {
    ExaminerQuestionData eqd = getExaminerQuestionData( candidateident, questionident, true );
    if ( eqd == null ) return null;
    if ( eqd.outcomes == null )
      eqd.outcomes = new OutcomeData( this );
    return eqd.outcomes;
  }
 
  public int getExaminerDecision( String candidateident, String questionident )
  {
    ExaminerQuestionData eqd = getExaminerQuestionData( candidateident, questionident, false );
    if ( eqd == null ) return QuestionData.EXAMINER_DECISION_NONE;
    return eqd.getExaminerdecision();
  }

  public void setExaminerDecision( String candidateident, String questionident, int n )
  {
    ExaminerQuestionData eqd = getExaminerQuestionData( candidateident, questionident, true );
    eqd.setExaminerdecision(n);
    examinerdata.setUnsavedChanges(true);
  }
  
  public void setExaminerSelected( String candidateident, String questionident, String responseident, boolean b )
  {
    ExaminerResponseData erd = getExaminerResponseData( candidateident, questionident, responseident, true );
    erd.examiner_selected = b;
    examinerdata.setUnsavedChanges(true);
  }
  
  public boolean isExaminerSelected( String candidateident, String questionident, String responseident )
  {
    ExaminerResponseData erd = getExaminerResponseData( candidateident, questionident, responseident, true );
    if ( erd == null ) return false;
    return erd.examiner_selected;
  }


  
  
  
  public void addToReviewList( CandidateData c, QuestionData q )
  {
    if ( reviewfilter == REVIEW_FILTER_UNREVIEWED && 
            getExaminerDecision( c.id, q.getIdent() ) != QuestionData.EXAMINER_DECISION_NONE )
      return;
    if ( reviewfilter == REVIEW_FILTER_OVERRIDDEN && 
            getExaminerDecision( c.id, q.getIdent() ) != QuestionData.EXAMINER_DECISION_OVERRIDE )
      return;
    if ( reviewfilter == REVIEW_FILTER_CONFIRMED && 
            getExaminerDecision( c.id, q.getIdent() ) != QuestionData.EXAMINER_DECISION_STAND )
      return;
    reviewlist.add( c, q );    
  }
  
  public void rebuildReviewList()
  {
    int i, j;
    CandidateData c;
    QuestionData q;
    
    reviewlist.clear();
    
    if ( reviewtype == REVIEW_BY_CANDIDATE )
    {
      if ( reviewcandidateident == null ) return;
      c = this.candidates.get( reviewcandidateident );
      for ( j=0; j<c.itemidents.size(); j++ )
      {
        q = c.getQuestionData( c.itemidents.get( j ) );
        if ( q!=null )
          addToReviewList( c, q );
      }
    }

    if ( reviewtype == REVIEW_BY_QUESTION )
    {
      if ( reviewquestionident == null ) return;
      for ( i=0; i<this.candidates_sorted.size(); i++ )
      {
        c = this.candidates_sorted.get( i );
        for ( j=0; j<c.itemidents.size(); j++ )
        {
          q = c.getQuestionData( c.itemidents.get( j ) );
          if ( q!=null && q.getIdent().equals( reviewquestionident ) )
            addToReviewList( c, q );
        }
      }
    }

    if ( reviewtype == REVIEW_ALL )
    {
      for ( i=0; i<this.candidates_sorted.size(); i++ )
      {
        c = this.candidates_sorted.get( i );
        for ( j=0; j<c.itemidents.size(); j++ )
        {
          q = c.getQuestionData( c.itemidents.get( j ) );
          if ( q!=null && q.needsreview )
            addToReviewList( c, q );
        }
      }
    }      
  }
  
  public void addOutcomeName( String name )
  {
    if ( outcomenames.contains( name ) )
      return;
    
    outcomenames.add( name );
    // extra column in candidate table
    fireTableStructureChanged();
  }

  public void clearScans()
  {
    CandidateData candidate;
    PageData page;
    QuestionData question;
    ResponseData response;

    for ( int i = 0; i < candidates_sorted.size(); i++ )
    {
      candidate = candidates_sorted.get( i );
      candidate.score = 0.0;
      for ( int j = 0; j < candidate.pages.size(); j++ )
      {
        page = candidate.pages.get( j );
        page.questions.clear();
        page.scanned = false;
        page.processed = false;
        page.error = null;
        page.source = null;
      }
      //candidate.pages.clear();
    }
    
    pagelistmodel.fireTableDataChanged();
        
    try
    {
      scanarchive.close();
      Files.deleteIfExists( new File(scanarchive.getCanonicalPath()).toPath() );

      responsearchive.close();
      Files.deleteIfExists( new File(responsearchive.getCanonicalPath()).toPath() );
      
      scanarchive     = CompositeFile.getCompositeFile(new File( examfolder, "scans.tar" ));
      responsearchive = CompositeFile.getCompositeFile(new File( examfolder, "responses.tar" ));      
    }
    catch ( Exception e )
    {
    }
    
    scans.clear();
  }
  
  
  public void createPapers( int anoncount )
  {
    // N.B. CandidateData now represents a paper
    // This create CandidateData based on PersonData
    // One CandidateData for each named person plus anoncount
    
    int i, j;
    PersonData    person;
    CandidateData candidate;
    ArrayList<CandidateData> newcandidates = new ArrayList<>();
    
    // Named first...
    for ( i = 0; i < persons_sorted.size(); i++ )
    {
      person = persons_sorted.get( i );
      if ( person.isExcluded() || person.isAnonymous() ) continue;
      candidate = new CandidateData( this, person.getName(), person.getId(), false );
      candidate.preferences = person.getPreferences();
      candidates.put(candidate.id, candidate);
      candidates_sorted.add(candidate);
      newcandidates.add( candidate );
    }
    
    // Anon after...
    DecimalFormat df = new DecimalFormat( "000" );
    for ( i = 1; i <= anoncount; i++ )
    {
      candidate = new CandidateData( this, "Anon", df.format( i ), true );
      candidates.put(candidate.id, candidate);
      candidates_sorted.add(candidate);
      newcandidates.add( candidate );
    }
    
    // Now add items to each paper
    QTIElementItem item;
    OutcomeDatum od;
    for ( i=0; i<newcandidates.size(); i++ )
    {
      candidate = newcandidates.get( i );
      
      for ( j=0; j< qdefs.qti.getItems().size(); j++ )
      {
        item = qdefs.qti.getItems().get( j );
        if ( item.isSupported() && item.isForCandidate( candidate.anonymous ) )
          candidate.addQuestion( item.getIdent() );
      }
      
      if ( !candidate.anonymous )
      {
        od = new OutcomeDatum();
        od.name = "SID";
        od.fixed = true;
        od.value = candidate.id;
        candidate.getOutcomes().addDatum( od );
        od = new OutcomeDatum();
        od.name = "NAME";
        od.fixed = true;
        od.value = candidate.name;
        candidate.getOutcomes().addDatum( od );
      }
    }
    setUnsavedChangesInMain( true );
    fireTableDataChanged();    
  }

  public void forgetPrint()
  {
    CandidateData candidate;
    
    setLastPrintID( null );
    for ( int i = 0; i < candidates_sorted.size(); i++ )
    {
      candidate = candidates_sorted.get( i );
      candidate.pages.clear();
    }
    pages.clear();
    pagemap.clear();
    pagelistmodel.fireTableDataChanged();
    candidates.clear();
    candidates_sorted.clear();
    fireTableDataChanged();
  }
  
  public int getPageCount()
  {
    return pages.size();
  }
  
  public void addPage( PageData page )
  {
    pages.add( page );
    pagemap.put( page.pageid, page );
    pagelistmodel.fireTableDataChanged();
  }
  
  public PageData createPage(
                    String printid,
                    String pageid,
                    CandidateData candidate )
  {
    PageData page = new PageData( this, printid, pageid, candidate );
    addPage( page );
    linkPageToCandidate( page );
    return page;
  }
  
  public PageData getPage( int n )
  {
    return pages.get( n );
  }
  
  public PageData lookUpPage( String id )
  {
    return pagemap.get( id );
  }

  public void addScanImageFile( ImageFileData ifd )
  {
    scans.add( ifd );
  }
  
  public boolean isScanImageFileImported( ImageFileData ifd )
  {
    if ( ifd == null || ifd.getDigest() == null )
      return false;
    
    for ( int i=0; i<scans.size(); i++ )
    {
      if ( scans.get( i ).isImported() && 
           ifd.digest.equals( scans.get( i ).digest )
              )
        return true;
    }
    return false;
  }

  public CandidateData getFirstCandidate( boolean hasquestiondata )
  {
    if ( candidates_sorted.size() == 0 ) return null;
    
    if ( !hasquestiondata )
      return this.candidates_sorted.get( 0 );
    
    CandidateData c;
    
    for ( int i=0; i<this.candidates_sorted.size(); i++ )
    {
      c = this.candidates_sorted.get( i );
      if ( c.firstQuestionData() != null )
        return c;
    }
    
    return null;
  }
  
  @Override
  public String getQTIRenderOption( String name )
  {
    String def = default_options.getProperty(name);
    if ( def != null )
      return options.getProperty(name, def);
    return options.getProperty(name);
  }

  @Override
  public boolean getQTIRenderBooleanOption( String name )
  {
    String value = getQTIRenderOption( name );
    return "true".equalsIgnoreCase( value ) || "yes".equalsIgnoreCase( value ) || "y".equalsIgnoreCase( value );
  }

  @Override
  public int getQTIRenderIntegerOption( String name )
  {
    String value = getQTIRenderOption( name );
    try
    {
      return Integer.parseInt( getQTIRenderOption( name ) );
    }
    catch ( NumberFormatException nfe )
    {
      return -1;
    }
  }

  public void setOption( String name, String value )
  {
    options.setProperty(name, value);
  }


  public void setOption( String name, boolean value )
  {
    options.setProperty( name, value?"true":"false" );
  }



  public String getPreamble()
  {
    if ( qdefs == null ) return null;
    if ( qdefs.qti == null ) return null;
    QTIElementMaterial material = qdefs.qti.getAssessmentMaterial();
    if ( material == null ) return null;
    Vector<QTIElementMattext> mattexts = material.findElements( QTIElementMattext.class );
    if ( mattexts == null || mattexts.size() == 0 ) return null;
    return mattexts.get(0).getContent();
  }



  public String itemAnalysis()
  {
    if (qdefs == null)
    {
      return null;
    }
    if (qdefs.qti == null)
    {
      return null;
    }
    String ident = analysistablemodel.getSelectedQuestion();
    analyses.clear();
    qdefs.itemAnalysis(candidates_sorted, analyses);
    analysistablemodel.setSelectedQuestion( ident );
    setUnsavedChangesInMain( true );
    
//    ResponseAnalysis ranal;
//    StringWriter writer = new StringWriter();
//    PrintWriter out = new PrintWriter( writer );
//    out.print(",,,\"No. Students Right\",\"No. Students Wrong\",\"% Class Right\",\"Median Aptitude Difference\",\"Lower 90% limit\",\"Upper 90% limit\",,,,,,,\n");
//    for (int i = 0; i < analyses.size(); i++)
//    {
//      for (int j = 0; j < analyses.get(i).response_analyses.size(); j++)
//      {
//        ranal = analyses.get(i).response_analyses.get(j);
//
//        if (j == 0)
//        {
//          out.print("\"" + analyses.get(i).title + "\"");
//        } else
//        {
//          out.print("\"\"");
//        }
//        out.print(",\"");
//        out.print((char) ('a' + ranal.offset - 1));
//        out.print("\",");
//        out.print(ranal.correct ? "\"T\"" : "\"F\"");
//        out.print(",");
//        out.print(ranal.right);
//        out.print(",");
//        out.print(ranal.wrong);
//        out.print(",");
//        if ((ranal.right + ranal.wrong) > 0)
//        {
//          out.print((double) ranal.right / (double) (ranal.right + ranal.wrong));
//        }
//
//        if (ranal.right < 2 || ranal.wrong < 2)
//        {
//          out.print(",,,");
//          if (ranal.right + ranal.wrong < 10)
//          {
//            out.print(",*,,,,,,");
//          } else
//          {
//            if (ranal.right > ranal.wrong)
//            {
//              out.print(",,*,,,,,");
//            } else if (ranal.right < ranal.wrong)
//            {
//              out.print(",,,*,,,,\"Too difficult. Can't calculate stats.\"");
//            } else
//            {
//              out.print(",*,,,,,,");
//            }
//          }
//        } else
//        {
//          out.print(",");
//          out.print(ranal.median_difference);
//          out.print(",");
//          out.print(ranal.median_difference_lower);
//          out.print(",");
//          out.print(ranal.median_difference_upper);
//          if (ranal.median_difference_lower >= 0.0 && ranal.median_difference_upper > 0.0)
//          {
//            out.print(",,,,*,,,\"Positive discriminator.\"");
//          } else if (ranal.median_difference_upper <= 0.0 && ranal.median_difference_lower < 0.0)
//          {
//            out.print(",,,,,*,,\"NEGATIVE DISCRIMINATOR!!\"");
//          } else
//          {
//            out.print(",,,,,,*,");
//          }
//        }
//        out.print("\n");
//        if ((j + 1) == analyses.get(i).response_analyses.size())
//        {
//          out.print(",,,,,,,,,,,,,,\n");
//        }
//      }
//    }
//    out.close();
    return null; //writer.getBuffer().toString();
  }

  public void importPersons(List<PersonData> list, boolean forceanon )
  {
    int i, j;
    PersonData person;
   
    for ( i = 0; i < list.size(); i++ )
    {
      person = list.get( i );
      person.setAnonymous( forceanon );
      person.setExam( this );
      persons.put(person.getId(), person);
      persons_sorted.add(person);
    }
    sortPersons();
    personlistmodel.fireTableDataChanged();
  }
  
  
  public void importCandidates(List<CandidateData> list)
  {
    int i, j;
    CandidateData candidate;
    OutcomeDatum outcome;
    QTIElementItem item;
    Vector<QTIElementItem> allitems = qdefs.qti.getItems();
    
    for ( i = 0; i < list.size(); i++ )
    {
      candidate = list.get( i );
      for ( j=0; j<allitems.size(); j++ )
      {
        item = allitems.get( j );
        if ( item.isForCandidate( candidate.anonymous ) )
          candidate.addQuestion( item.getIdent() );
      }      
      if ( !candidate.anonymous )
      {
        outcome = new OutcomeDatum();
        outcome.fixed = true;
        outcome.name = "SID";
        outcome.value = candidate.id;
        candidate.getOutcomes().addDatum( outcome );
      }
      candidates.put(candidate.id, candidate);
      candidates_sorted.add(candidate);
    }
    sortCandidates();
    fireTableDataChanged();
  }


  
  public void importCandidates(File xmlfile)
          throws ParserConfigurationException, SAXException, IOException
  {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document document = builder.parse(xmlfile);

    Element roote = document.getDocumentElement();
    //System.out.println(roote.getNodeName());
    NodeList nl = roote.getElementsByTagName("candidate");
    Element ecandidate;
    CandidateData candidate;
    for (int i = 0; i < nl.getLength(); i++)
    {
      ecandidate = (Element) nl.item(i);
      candidate = new CandidateData(this, ecandidate.getAttribute("name"), ecandidate.getAttribute("id"), false );
      candidates.put(candidate.id, candidate);
      candidates_sorted.add(candidate);
      fireTableDataChanged();
    }
    sortCandidates();
    fireTableDataChanged();
  }

  public void importCsvCandidates(File csvfile)
          throws ParserConfigurationException, SAXException, IOException
  {
    int i, j, lastnamecolumn = -1, firstnamecolumn = -1, idcolumn = -1;
    CandidateData candidate;
    CSVReader reader = new CSVReader(new FileReader(csvfile));
    String[] line = reader.readNext();
    for (j = 0; j < line.length; j++)
    {
      System.out.println(line[j]);
      if ("Last Name".equals(line[j]))
      {
        lastnamecolumn = j;
      }
      if ("First Name".equals(line[j]))
      {
        firstnamecolumn = j;
      }
      if ("User ID".equals(line[j]))
      {
        idcolumn = j;
      }
    }
    if (lastnamecolumn < 0 || firstnamecolumn < 0 || idcolumn < 0)
    {
      throw new IOException("CSV file doesn't contain the necessary columns: 'Last Name', 'First Name' and 'User ID'.");
    }
    while ((line = reader.readNext()) != null)
    {
      for (j = 0; j < line.length; j++)
      {
        System.out.println(line[j]);
      }
      candidate = new CandidateData(this, line[firstnamecolumn] + " " + line[lastnamecolumn], line[idcolumn], false );
      candidates.put(candidate.id, candidate);
      candidates_sorted.add(candidate);
      fireTableDataChanged();
    }
    sortCandidates();
    fireTableDataChanged();
  }

  public void exportCsvScores(File csvfile)
          throws ParserConfigurationException, SAXException, IOException
  {
    int i, j;
    CandidateData candidate;
    OutcomeDatum datum;
    CSVWriter csvwriter = new CSVWriter(new FileWriter(csvfile));
    ArrayList<String> list = new ArrayList<String>();
    String[] line = new String[3];

    // Find all possible top level outcome names (other than SCORE which
    // is always included
    ArrayList<String> outcomenames = new ArrayList<String>();
    for (i = 0; i < candidates_sorted.size(); i++)
    {
      candidate = candidates_sorted.get(i);
      for ( j=0; j<candidate.getOutcomes().getRowCount(); j++ )
      {
        datum = candidate.getOutcomes().getDatumAt( j );
        if ( "SCORE".equals( datum.name ) )
          continue;
        if ( !outcomenames.contains( datum.name ) )
          outcomenames.add( datum.name );
      }
    }
    
    list.add( "ID" );
    list.add( "Name" );
    list.add( "Score" );
    for (i = 0; i < outcomenames.size(); i++)
      list.add( outcomenames.get( i) );
    line = list.toArray( line );
    csvwriter.writeNext(line);
    
    
    for (i = 0; i < candidates_sorted.size(); i++)
    {
      list.clear();
      candidate = candidates_sorted.get(i);
      list.add( candidate.id );
      list.add( candidate.name );
      if (candidate.score != null)
        list.add( candidate.score.toString() );
      else
        list.add( "" );
      for ( j=0; j<outcomenames.size(); j++ )
      {
        datum = candidate.getOutcomes().getDatum( outcomenames.get( j ) );
        if ( datum.value != null)
          list.add( datum.value.toString() );
        else
          list.add( "" );
      }
      
      line = list.toArray( line );
      csvwriter.writeNext(line);
    }
    csvwriter.close();
  }



  private static final String[] likertcolour =
  {
    "#880000",
    "#ff8888",
    "#cccccc",
    "#88ff88",
    "#008800"
  };



  public void importQuestionsFromPackage(File imsmanifest)
          throws ParserConfigurationException, SAXException, IOException
  {
    int i;
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document manifestdoc = builder.parse(imsmanifest);

    Element roote = manifestdoc.getDocumentElement();
    //System.out.println(roote.getNodeName());
    NodeList nl = roote.getElementsByTagName("resources");
    if (nl.getLength() != 1)
    {
      return;
    }

    Element resources = (Element) nl.item(0);
    Element resource;
    Element imsresource = null;
    String type;
    nl = resources.getElementsByTagName("resource");
    // Look for first resource which is a QTI 1.2 file
    for (i = 0; i < nl.getLength(); i++)
    {
      resource = (Element) nl.item(i);
      type = resource.getAttribute("type");
      if ( type == null)
      {
        continue;
      }
      if (
              !type.startsWith("ims_qtiasiv1") &&
              !type.equals( "assessment/x-bb-qti-test")  // BB specific type
         )
      {
        continue;
      }
      imsresource = resource;
    }

    if (imsresource == null)
    {
      return;
    }

    // Does the resource tag have an href to identify one of multiple files?
    Element fileelement = null;
    String href = imsresource.getAttribute("href");
    String testhref;
    nl = imsresource.getElementsByTagName("file");
    if (href != null && href.length() > 0 )
    {
      // look for the referenced file element
      for (i = 0; i < nl.getLength(); i++)
      {
        fileelement = (Element) nl.item(i);
        testhref = fileelement.getAttribute("href");
        if (!href.equals(testhref))
        {
          fileelement = null;
        } else
        {
          break;
        }
      }
    }

    if (fileelement == null && nl.getLength() == 1)
    {
      fileelement = (Element) nl.item(0);
    }

    // Could be BB specific screw up
    if ( fileelement == null )
    {
      //String prefix = imsresource.lookupPrefix( "http://www.blackboard.com/content-packaging/" );
      href = imsresource.getAttribute( "bb:file" );
      if ( href== null || href.length() == 0 )
        return;  // BB peculiarity not found
    }
    else
      href = fileelement.getAttribute("href");

    File qtiexamfile = new File(imsmanifest.getParentFile(), href);
    if (!qtiexamfile.exists() || !qtiexamfile.isFile())
    {
      return;
    }

    importQuestionsFromQTI(imsmanifest.getParentFile(),qtiexamfile);
  }

  static String top = "<?xml version=\"1.0\"?>\r\n" +
"<questestinterop xmlns=\"http://www.imsglobal.org/xsd/ims_qtiasiv1p2\">\r\n" +
"  <assessment ident=\"ass{ident}\" title=\"imported\">\r\n" +
"    <section ident=\"sec{ident}\" title=\"imported\">";
  
  static String tail = "    </section>\r\n" +
"  </assessment>\r\n" +
"</questestinterop>";
  
  static String itemtop = "    <item ident=\"{itemident}\" title=\"Question {seq}\">\r\n" +
"    <presentation>\r\n" +
"      <flow>\r\n" +
"        <material>\r\n" +
"          <mattext charset=\"US-ASCII\" texttype=\"TEXT/PLAIN\" xml:space=\"preserve\">" +
"{stem}" +
"          </mattext>\r\n" +
"          <matbreak/>\r\n" +
"        </material>\r\n" +
"        <response_lid ident=\"resp_{itemident}\" rcardinality=\"Single\">\r\n" +
"          <render_choice shuffle=\"No\">\r\n" +
"            <flow_label class=\"Row\">\r\n";
          
static String itemtail = "            </flow_label>\r\n" +
"          </render_choice>\r\n" +
"        </response_lid>\r\n" +
"      </flow>\r\n" +
"    </presentation>\r\n" +
"    <resprocessing scoremodel=\"SumofScores\">\r\n" +
"      <outcomes>\r\n" +
"        <decvar defaultval=\"0.0\" minvalue=\"0.0\" varname=\"SCORE\" vartype=\"Decimal\"/>\r\n" +
"      </outcomes>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"          <varequal case=\"Yes\" respident=\"resp_{itemident}\">{correctident}</varequal>\r\n" +
"        </conditionvar>\r\n" +
"        <setvar action=\"Add\" varname=\"SCORE\">100.0</setvar>\r\n" +
"      </respcondition>\r\n" +
"    </resprocessing>\r\n" +
"    </item>\r\n";

static String option = "              <response_label xmlns:qyouti=\"http://www.qyouti.org/qtiext\" ident=\"{labelident}\">\r\n" +
"                <material>\r\n" +
"                  <mattext charset=\"US-ASCII\" texttype=\"TEXT/PLAIN\" xml:space=\"preserve\">" +
"{option}" +
"                  </mattext>\r\n" +
"                  <matbreak/>\r\n" +
"                </material>\r\n" +
"              </response_label>\r\n";
  

  public void importQuestionsFromPlainText(File textexamfile, File importfolder)
          throws ParserConfigurationException, SAXException, IOException
  {
    BufferedReader reader = new BufferedReader( new FileReader( textexamfile ) );

    if ( !importfolder.exists() )
      importfolder.mkdir();
    File tempxml = File.createTempFile( "plaintextimport", ".xml", importfolder );
    PrintWriter writer = new PrintWriter( new FileWriter( tempxml ) );

    int seq=1;
    Random r = new Random();
    byte[] rb = new byte[12];
    BigInteger bi;
    r.nextBytes( rb );
    rb[0] &= 0x7f;
    bi = new BigInteger( rb );
    String ident = bi.toString( 16 );
    
    String s = top.replace( "{ident}", ident );
    writer.append( s );
    
    String itemident=null, labelident=null, correctident=null;
    String line = reader.readLine();
    char ca, cb;
    boolean inquestion = false;
    while ( line != null )
    {
      line = line.trim();
      if ( line.length() < 3 )
      {
        ca = 0;
        cb = 0;
      }
      else
      {
        ca = line.charAt( 0 );
        cb = line.charAt( 1 );
      }
      
      if ( Character.isDigit( ca ) )
      {
        // new question.
        if ( inquestion )
        {
          if ( correctident == null ) correctident = "placeholderident";
          // Complete old question first
          s = itemtail.replace( "{itemident}", itemident );
          s = s.replace( "{correctident}", correctident );
          writer.append( s );
          correctident = null;
        }
        r.nextBytes( rb );
        rb[0] &= 0x7f;
        bi = new BigInteger( rb );
        itemident = bi.toString( 16 );
        s = itemtop.replace( "{itemident}", itemident );
        s = s.replace( "{seq}", Integer.toString( seq++ ) );
        s = s.replace( "{stem}", line.substring( line.indexOf( '.' )+1 ).trim() );
        writer.append( s );
        inquestion = true;
      }
      
      if ( Character.isAlphabetic( ca ) && (cb == '.' || cb == ':' ) )
      {
        labelident = "" + ca;
        if ( cb == ':' )
          correctident = labelident;
        s = option.replace( "{labelident}", labelident );
        s = s.replace( "{option}", line.substring( 2 ).trim() );        
        writer.append( s );
      }
      
      line = reader.readLine();
    }

    if ( inquestion )
    {
      // Complete last question
      if ( correctident == null ) correctident = "placeholderident";
      s = itemtail.replace( "{itemident}", itemident );
      s = s.replace( "{correctident}", correctident );
      writer.append( s );
    }
    
    writer.append( tail );
    writer.flush();
    writer.close();
    
    reader.close();
    
    importQuestionsFromQTI(tempxml.getParentFile(),tempxml);
}  
  
  public void importQuestionsFromQTI(File basefolder, File qtiexamfile)
          throws ParserConfigurationException, SAXException, IOException
  {
    int i;
    DocumentBuilder builder = QyoutiDocBuilderFactory.getDocumentBuilder();
    NodeList nl;

    Document qti12doc = builder.parse(qtiexamfile);
    Element qtiexamroote = qti12doc.getDocumentElement();
    //System.out.println(qtiexamroote.getNodeName());
//    nl = qtiexamroote.getElementsByTagName("assessment");
//    if (nl.getLength() != 1)
//    {
//      return;
//    }
//
//    Element assessment = (Element) nl.item(0);
//    nl = assessment.getElementsByTagName("section");
//    if (nl.getLength() != 1)
//    {
//      return;
//    }

    Element itemref;
    Vector<Element> itemrefs = new Vector<Element>();
    nl = qtiexamroote.getElementsByTagName("itemref");
    for (i = 0; i < nl.getLength(); i++)
    {
      itemref = (Element) nl.item(i);
      itemrefs.add(itemref);
    }

    if ( itemrefs.size() > 0 )
      resolveItemReferences( qtiexamfile.getParentFile(), qti12doc, itemrefs );

    nl = qtiexamroote.getElementsByTagName("matimage");
    resolveMediaReferences( examfile.getParentFile(), basefolder, qtiexamfile.getParentFile(), nl );

    checkItemIDs( qti12doc );

    qdefs = new QuestionDefinitions(qtiexamroote,personlistmodel);
  }

  private void checkItemIDs( Document qti12doc )
  {
    NodeList nl = qti12doc.getElementsByTagName("item");
    Element item;
    String ident, title;
    for ( int i=0; i < nl.getLength(); i++ )
    {
      item = (Element)nl.item(i);
      ident = item.getAttribute( "ident" );
      if ( ident==null || ident.length()==0 )
        item.setAttribute( "ident", QyoutiUtils.randomIdent() );
      title = item.getAttribute( "title" );
      if ( title==null || title.length()==0 )
        item.setAttribute( "title", "Unnamed" );
    }
  }
  
  private void resolveItemReferences( File folder, Document qti12doc, Vector<Element> itemrefs )
          throws ParserConfigurationException
  {
    int i;
    Document qdocument;
    Element qroote, qelement;
    String elementname;
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    NodeList nl;
    Hashtable<String, Element> assessmentitemelements = new Hashtable<String, Element>();

    if (itemrefs.size() > 0)
    {
      // scan directory that holds the qti exam and look for questions.
      File[] files = folder.listFiles(new XMLFileFilter());
      for (i = 0; i < files.length; i++)
      {
        if (!files[i].isFile())
        {
          continue;
        }
        try
        {
          qdocument = builder.parse(files[i]);
        } catch (Exception ex)
        {
          continue;  // not well formed XML
          }
        qroote = qdocument.getDocumentElement();
        // well formed xml but what is in it?
        elementname = qroote.getNodeName();
        if (!"questestinterop".equals(elementname))
        {
          continue;
        }
        //if ( !"http://www.imsglobal.org/xsd/ims_qtiasiv1p2".equals(qroote.getNamespaceURI() ) )
        //  continue;

        //This is a qti file so look for questions inside top level
        nl = qroote.getElementsByTagName("item");
        for (int j = 0; j < nl.getLength(); j++)
        {
          qelement = (Element) nl.item(j);
          assessmentitemelements.put(qelement.getAttribute("ident"), qelement);
        }
      }
    }

    // now get the questions in the order defined in the qti exam file.
    // and 'transplant' the nodes
    String ident;
    Node duplicate;
    Element itemref;
    for (i = 0; i < itemrefs.size(); i++)
    {
      itemref = itemrefs.get(i);
      ident = itemref.getAttribute("linkrefid");
      qelement = assessmentitemelements.get(ident);
      if (qelement != null)
      {
        duplicate = qti12doc.importNode(qelement, true);
        itemref.getParentNode().appendChild(duplicate);
      }
      itemref.getParentNode().removeChild(itemref);
    }
    qti12doc.normalizeDocument();

    //  qelement = assessmentitemelements.get( refids.get( i ) );
    //  if ( qelement != null )
    //    assessmentitemelementssorted.add( qelement );
  }


  private void resolveMediaReferences( File examfolder, File basefolder, File qtifolder, NodeList nl )
  {
    int i;
    Element e;
    URI uri;
    String attr_uri;
    File imagefile=null;
    File[] files;
    String exampath, query, contentid;
    WebCTIDFileFilter filter;
    
    try
    {
      exampath = examfolder.getCanonicalPath();
    } catch (IOException ex)
    {
      return;
    }

    for (i = 0; i < nl.getLength(); i++)
    {
      imagefile=null;
      e = (Element) nl.item(i);
      attr_uri = e.getAttribute("uri");
      if ( attr_uri== null || attr_uri.length() == 0 )
        continue;
      try { uri = new URI(attr_uri); } catch (Exception ex) {continue;}
      if ( uri.getScheme() == null && uri.getQuery() != null )
      {
        // Special case - probably WebCT Vista
        query = uri.getQuery();
        System.out.println("Image query: " + query );
        if ( query.startsWith("contentID=") && !query.contains( "&" ) )
        {
          contentid = query.substring( "contentID=".length() );
          filter = new WebCTIDFileFilter( contentid );
          files = qtifolder.listFiles(filter);
          if ( files.length == 1 )
            imagefile = files[0];
        }
      }
      else
      {
        if ( uri.getScheme() == null && !uri.getPath().startsWith("/") )
        {
          // bog standard relative reference it seems
          // Try relative to QTI file.
          imagefile = new File( qtifolder, uri.getPath() );
          if ( !imagefile.exists() || !imagefile.isFile() )
            imagefile = new File( basefolder, uri.getPath() );
        }
        // Assume it is a network or absolute file ref and leave the
        // reference as it is.
      }


      // Found file - make relative to exam folder
      if ( imagefile!=null && imagefile.exists() && imagefile.isFile() )
      {
        String imgpath=null;
        try
        {
          imgpath = imagefile.getCanonicalPath();
        } catch (IOException ex) {}

        if ( imgpath.startsWith( exampath ) )
          e.setAttribute( "uri", imgpath.substring( exampath.length()+1 ) );
      }
      // otherwise leave the uri untouched.
    }
  }




  class WebCTIDFileFilter implements FilenameFilter
  {
    String id;
    public WebCTIDFileFilter( String id )
    {
      this.id = "." + id;
    }
    public boolean accept(File dir, String name)
    {
      return name.contains(id) && !name.endsWith(".xml");
    }
  }

  class XMLFileFilter implements FilenameFilter
  {

    public boolean accept(File dir, String name)
    {
      return name.endsWith(".xml");
    }
  }

  public CandidateData linkPageToCandidate(PageData page)
  {
    if ( page.candidate_number == null )
      return null;
    
    CandidateData candidate = candidates.get(page.candidate_number);
    if (candidate == null)
    {
      page.candidate = null;
      return null;
      
      // do not add a candidate like this - if we lost the record of this
      // candidate then que sera sera
      
//      candidate = new CandidateData(this, page.candidate_name, page.candidate_number, false);
//      candidate.outcomes.addTableModelListener( outcomelistener );
//      candidates.put(page.candidate_number, candidate);
//      candidates_sorted.add(candidate);
//      sortCandidates();
    }
    // forward link
    page.candidate = candidate;
    // back link
    page.candidate.addPage(page);
    fireTableDataChanged();
    return page.candidate;
  }

  public void invalidateAllOutcomes()
  {
    invalidateOutcomes( null );
  }
  
  public void invalidateOutcomes( String itemident )
  {
    PageData page;
    for ( int i=0; i<getPageCount(); i++ )
    {
      page = getPage( i );
      if ( page.error != null )
        continue;
      
      if ( itemident == null )
      {
        page.processed = false;
      }
      else
      {
        for ( int j=0; j<page.questions.size(); j++ )
        {
          if ( itemident.equals( page.questions.get( j ).getItem().getIdent() ) )
            page.processed = false;
        }
      }
    
    }    
  }
  
  public void updateOutcomes()
  {
    PageData page;
    for ( int i=0; i<getPageCount(); i++ )
    {
      page = getPage( i );
      if ( page.error != null )
        continue;
      if ( page.processed )
        continue;
      updateOutcomes( page );
    }
    //rebuildOutcomeNameList();    
  }

  private void updateOutcomes( PageData page )
  {
    if ( page.candidate ==null )
        return;
    // Compute outcomes based on QTI def of question
    for ( int j=0; j<page.questions.size(); j++ )
    {
      page.questions.get( j ).processResponses();
    }
    if ( page.questions.size() > 0 )
    {
      // recalculates total score after every page
      page.candidate.processAllResponses();
    }
    page.processed = true;    
  }
  
  public QTIElementItem getAssessmentItem(String id)
  {
    return qdefs.qti.getItem(id);
  }

  public boolean save()
  {
    Writer writer = null;
    try
    {
      if ( examinerdata != null && examinerdata.areThereUnsavedChanges() )
      {
        writer = new OutputStreamWriter(new FileOutputStream(examinerfile), "utf8");
        examinerdata.emit( writer );
        writer.close();
        writer = null;        
      }
      
      if ( qdefs != null && qdefs.areThereUnsavedChanges() )
      {
        writer = new OutputStreamWriter(new FileOutputStream(questionfile), "utf8");
        qdefs.emit( writer );
        writer.close();
        writer = null;
      }
      
      if ( this.unsaved_changes )
      {
        writer = new OutputStreamWriter(new FileOutputStream(examfile), "utf8");
        emit(writer);
        writer.close();
        writer = null;
        for ( int i=0; i<datatransforminstructions.size(); i++ )
        {
          datatransforminstructions.get( i ).transform();
        }
      }
      
    } catch (Exception ex)
    {
      Logger.getLogger(ExaminationData.class.getName()).log(Level.SEVERE, null, ex);
      return false;
    } finally
    {
      try
      {
        if ( writer != null )
          writer.close();
      } catch (IOException ex)
      {
        Logger.getLogger(ExaminationData.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
    unsaved_changes = false;
    fireStatusChange();
    return true;
  }

  public boolean areThereUnsavedChanges()
  {
    return unsaved_changes || qdefs.areThereUnsavedChanges() || (examinerdata != null && examinerdata.areThereUnsavedChanges());
  }
  
  public void setUnsavedChangesInMain( boolean b )
  {
    if ( unsaved_changes != b )
    {
      unsaved_changes = b;
      fireStatusChange();
    }
  }

  public void setUnsavedChangesInQuestions( boolean b )
  {
    if ( qdefs.areThereUnsavedChanges() != b )
    {
      qdefs.setUnsavedChanges( b );
      fireStatusChange();
    }
  }

  public void setUnsavedChangesInExaminer( boolean b )
  {
    if ( examinerdata.areThereUnsavedChanges() != b )
    {
      examinerdata.setUnsavedChanges( b );
      fireStatusChange();
    }
  }
  
  public String getLastPrintID()
  {
    return lastprintid;
  }

  public void setLastPrintID( String lastprintid )
  {
    this.lastprintid = lastprintid;
    fireStatusChange();
  }

  
  
  public void emitOptions(Writer writer)
          throws IOException
  {
    try {
      writer.write("\n\n");

      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.newDocument();

      Element options_e = document.createElement("options");
      Element option_e;
      document.appendChild( options_e );

      Iterator<String> names = options.stringPropertyNames().iterator();
      String name;
      while ( names.hasNext() )
      {
        option_e = document.createElement("option");
        name = names.next();
        option_e.setAttribute("name", name);
        option_e.setTextContent( options.getProperty( name ) );
        options_e.appendChild(option_e);
      }

      // Prepare the DOM document for writing Source
      DOMSource source = new DOMSource(options_e);
      // Prepare the output file
      Result result = new StreamResult(writer);
      // Write the DOM document to the file
      Transformer xformer = TransformerFactory.newInstance().newTransformer();
      xformer.setOutputProperty(OutputKeys.INDENT, "yes");
      xformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      xformer.transform(source, result);

      writer.write("\n\n");
    }
    catch (Exception e) 
    {
      e.printStackTrace();
    }
  }

  public void emit(Writer writer)
          throws IOException
  {
    writer.write("<?xml version=\"1.0\"?>\r\n");
    writer.write("<examination" );
    if ( lastprintid != null)
      writer.write( " lastprintid=\"" + lastprintid + "\"" );
    writer.write( ">\r\n");


    emitOptions( writer );

    writer.write( "<scans nextscanfileident=\"" + nextscanfileident + "\">\r\n" );
    for ( int i=0; i<scans.size(); i++ )
      scans.get( i ).emit( writer );
    writer.write( "</scans>\r\n" );

    writer.write( "<persons>\r\n" );
    if (persons != null)
    {
      for (int i = 0; i < persons.size(); i++)
        persons_sorted.get(i).emit(writer);
    }
    writer.write( "</persons>\r\n" );
    
    
    // write out ahead of candidates so that candidate data
    // can reference the pages
    writer.write( "<pages>\r\n" );
    if (pages != null)
    {
      for (int i = 0; i < pages.size(); i++)
      {
        pages.get(i).emit(writer);
      }
    }
    writer.write( "</pages>\r\n" );


    writer.write("<papers>\r\n");
    Enumeration<CandidateData> e = candidates.elements();
    for (int i = 0; i < candidates_sorted.size(); i++)
    {
      candidates_sorted.get(i).emit(writer);
    }
    writer.write("</papers>\r\n");

    writer.write("<analysis>\r\n");
    if (analyses != null)
    {
      for (int i = 0; i < analyses.size(); i++)
      {
        analyses.get(i).emit(writer);
      }
    }
    writer.write("</analysis>\r\n");

    writer.write("<transforms>\r\n");
    if (datatransforminstructions != null)
    {
      for (int i = 0; i < datatransforminstructions.size(); i++)
      {
        datatransforminstructions.get(i).emit(writer);
      }
    }
    writer.write("</transforms>\r\n");

    writer.write("</examination>\r\n");
  }

  public void loadOptions( Element e )
          throws ParserConfigurationException, SAXException, IOException
  {
    NodeList nl = e.getChildNodes();
    Element ec;
    String name, value;
    for ( int i=0; i<nl.getLength(); i++ )
    {
      if ( !(nl.item(i) instanceof Element) )
        continue;
      ec = (Element)nl.item(i);
      if ( !"option".equals(ec.getNodeName()))
        continue;

      name = ec.getAttribute("name");
      if ( name == null || name.length() == 0 )
        continue;

      value = ec.getTextContent();
      options.setProperty(name, value);
    }
  }


  public class CandidateComparator implements Comparator
  {
    @Override
    public int compare(Object o1, Object o2)
    {
      CandidateData a = (CandidateData) o1;
      CandidateData b = (CandidateData) o2;
      return a.id.compareToIgnoreCase(b.id);
    }
  }

  public void sortCandidates()
  {
    //Collections.sort( (Vector)candidates_sorted, new CandidateComparator() );
  }

  public class PersonComparator implements Comparator
  {
    @Override
    public int compare(Object o1, Object o2)
    {
      PersonData a = (PersonData) o1;
      PersonData b = (PersonData) o2;
      return a.getName().compareToIgnoreCase(b.getName());
    }
  }

  public void sortPersons()
  {
    Collections.sort( (ArrayList)persons_sorted, new PersonComparator() );
  }
  
  public void load()
          throws ParserConfigurationException, SAXException, IOException
  {
    if ( !examfile.exists() || !examfile.isFile() )
      return;
    loadQuestions( new InputSource( new FileInputStream( questionfile ) ) );
    load( new InputSource( new FileInputStream( examfile ) ) );
    try
    {
      loadExaminerData( new InputSource( new FileInputStream( examinerfile ) ) );
      qdefs.qti.setOverride( examinerdata.examinerqdefs.qti );
    }
    catch ( FileNotFoundException e )
    {
      System.out.println( "No examiner data yet" );
      examinerdata = new ExaminerData();
    }
  }

  private void loadExaminerData( InputSource source )
          throws ParserConfigurationException, SAXException, IOException
  {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document document = builder.parse( source );
    Element roote = document.getDocumentElement();
    examinerdata=null;
    if ("examinerdata".equals(roote.getNodeName()))
      examinerdata = new ExaminerData( this, roote );
    else
      examinerdata = new ExaminerData();
  }
  
  private void loadQuestions( InputSource source )
          throws ParserConfigurationException, SAXException, IOException
  {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document document = builder.parse( source );
    Element roote = document.getDocumentElement();
    qdefs=null;
    if ("questestinterop".equals(roote.getNodeName()))
      qdefs = new QuestionDefinitions(roote, personlistmodel );
  }
  
  private void load( InputSource source )
          throws ParserConfigurationException, SAXException, IOException
  {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    DocumentBuilder builder = factory.newDocumentBuilder();
  
    
    Document document = builder.parse( source );

    Element roote = document.getDocumentElement();
    lastprintid = roote.getAttribute( "lastprintid" );
    if ( lastprintid != null && lastprintid.length() == 0 )
      lastprintid = null;
    
    //System.out.println(roote.getNodeName());
    NodeList nl = roote.getChildNodes();
    NodeList cnl;
    Element e;
    Node node;
    CandidateData candidate;
    PersonData person;

    for (int i = 0; i < nl.getLength(); i++)
    {
      node = nl.item(i);
      if (!(node instanceof Element))
      {
        continue;
      }
      e = (Element) node;

      if ("options".equals(e.getNodeName()))
      {
        loadOptions( e );
      }

      if ("persons".equals(e.getNodeName()))
      {
        cnl = e.getElementsByTagName("person");
        for (int j = 0; j < cnl.getLength(); j++)
          person = new PersonData(this, (Element) cnl.item(j));
        sortPersons();
      }
      
      if ( "candidates".equals(e.getNodeName()) || "papers".equals(e.getNodeName()) )
      {
        String n;
        
//        if ( "candidates".equals(e.getNodeName()) )
          n = "candidate";
//        else
//          n = "paper";
        
        cnl = e.getElementsByTagName( n );
        for (int j = 0; j < cnl.getLength(); j++)
        {
          candidate = new CandidateData(this, (Element) cnl.item(j));
        }
        sortCandidates();
      }

      if ("scans".equals(e.getNodeName()))
      {
        String a = e.getAttribute( "nextscanfileident" );
        if ( a != null && a.length() > 0 )
          nextscanfileident = new Integer( a ).intValue();
        cnl = e.getElementsByTagName("file");
        for (int j = 0; j < cnl.getLength(); j++)
          scans.add( new ImageFileData(this, (Element) cnl.item(j) ) );
      }
      
      if ("pages".equals(e.getNodeName()))
      {
         cnl = e.getElementsByTagName("page");
        PageData page;
        for (int j = 0; j < cnl.getLength(); j++)
        {
          page = new PageData(this, (Element) cnl.item(j) );
          addPage( page );
        }

      }
      
      if ("analysis".equals(e.getNodeName()))
      {
        cnl = e.getElementsByTagName("itemanalysis");
        QuestionAnalysis qa;
        for (int j = 0; j < cnl.getLength(); j++)
        {
          qa = new QuestionAnalysis( (Element) cnl.item(j) );
          analyses.add( qa );
        }

      }
      if ("transforms".equals(e.getNodeName()))
      {
        DataTransformInstruction datatransform;
        cnl = e.getElementsByTagName("transform");
        for (int j = 0; j < cnl.getLength(); j++)
        {
          datatransform = new DataTransformInstruction( this, (Element) cnl.item(j) );
          datatransforminstructions.add( datatransform );
        }
      }
    }

      for ( int p=0; p<getPageCount(); p++ )
        getPage( p ).postLoad();
  
    //rebuildOutcomeNameList();
    rebuildReviewList();
    
    fireStatusChange();
    fireTableDataChanged();
  }

  public int getRowCount()
  {
    return this.candidates.size();
  }

  public int getColumnCount()
  {
    return 4 + outcomenames.size();
  }

  @Override
  public String getColumnName(int columnIndex)
  {
    switch (columnIndex)
    {
      case 0:
        return "Status";
      case 1:
        return "Paper Name";
      case 2:
        return "Paper ID";
      case 3:
        return "Questions";
    }
    int on = columnIndex-4;
    if ( on>=0 && on<outcomenames.size() )
      return outcomenames.get( on );
    return null;
  }

  @Override
  public Class getColumnClass(int columnIndex)
  {
    switch (columnIndex)
    {
      case 0:
        return String.class;
      case 1:
        return String.class;
      case 2:
        return String.class;
      case 3:
        return Integer.class;
    }
    int on = columnIndex-4;
    if ( on>=0 && on<outcomenames.size() )
      return String.class;
    return null;
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex)
  {
    return false;
  }

  public Object getValueAt(int rowIndex, int columnIndex)
  {
    CandidateData candidate = candidates_sorted.get(rowIndex);
    int asked = candidate.questionsAsked();
    switch (columnIndex)
    {
      case 0:
        return candidate.getStatusDescription();
      case 1:
        return candidate.name;
      case 2:
        return candidate.id;
      case 3:
        return new Integer( asked );
    }
    int on = columnIndex-4;
    if ( on>=0 && on<outcomenames.size() )
    {
      if ( candidate.getOutcomes() == null ) return null;
      OutcomeDatum d = candidate.getOutcomes().getDatum( outcomenames.get( on ) );
      if ( d == null || d.value == null ) return null;
      return d.value.toString();
    }
    return null;
  }
  
  class OutcomeDataListener implements TableModelListener
  {

    @Override
    public void tableChanged( TableModelEvent e )
    {
      int i;
      TableModel model;
      
      if ( e.getSource() instanceof TableModel &&
           e.getType() == TableModelEvent.INSERT && 
           e.getFirstRow() != TableModelEvent.HEADER_ROW )
      {
        model = (TableModel)e.getSource();
        for ( i=e.getFirstRow(); i<=e.getLastRow(); i++ )
          addOutcomeName( model.getValueAt( i, 0 ).toString() );
      }
    }
    
  }
}

