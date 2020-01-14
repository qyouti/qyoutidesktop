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
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.*;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.UserPrincipal;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.text.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.bouncycastle.openpgp.PGPException;
import org.qyouti.crypto.CryptographyManager;
import org.quipto.compositefile.*;
import org.qyouti.QyoutiFrame;
import org.qyouti.qti1.element.QTIElementItem;
import org.qyouti.qti1.element.QTIElementMaterial;
import org.qyouti.qti1.element.QTIElementMattext;
import org.qyouti.qti1.gui.PaginationRecord;
import org.qyouti.qti1.gui.QTIRenderOptions;
import org.qyouti.qti1.gui.QuestionMetricsRecordSetCache;
import org.qyouti.templates.ItemTemplate;
import org.qyouti.util.QyoutiUtils;
import org.qyouti.xml.QyoutiDocBuilderFactory;
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
  public static final int EXAM_ROLE_ADMINISTRATOR = 0;
  public static final int EXAM_ROLE_EXAMINER = 1;
  public static final int EXAM_ROLE_OBSERVER = 2;


  static final Set<AclEntryPermission> READPERMISSIONS = EnumSet.of(
            AclEntryPermission.READ_DATA, 
            AclEntryPermission.READ_NAMED_ATTRS, 
            AclEntryPermission.READ_ATTRIBUTES,
            AclEntryPermission.READ_ACL,
            AclEntryPermission.SYNCHRONIZE
    );
  static final Set<AclEntryPermission> WRITEPERMISSIONS = EnumSet.of(
            AclEntryPermission.WRITE_DATA, 
            AclEntryPermission.APPEND_DATA, 
            AclEntryPermission.WRITE_ATTRIBUTES,
            AclEntryPermission.WRITE_NAMED_ATTRS
    );
  static final Set<AclEntryPermission> ACCESSPERMISSIONS = EnumSet.of(
            AclEntryPermission.DELETE, 
            AclEntryPermission.WRITE_ACL, 
            AclEntryPermission.WRITE_OWNER
    );
  static final Set<AclEntryPermission> READWRITEPERMISSIONS;
  static final Set<AclEntryPermission> FULLPERMISSIONS;
  static
  {
    READWRITEPERMISSIONS = EnumSet.copyOf(READPERMISSIONS);
    READWRITEPERMISSIONS.addAll(WRITEPERMISSIONS);
    FULLPERMISSIONS = EnumSet.copyOf(READWRITEPERMISSIONS);
    FULLPERMISSIONS.addAll(ACCESSPERMISSIONS);    
  }
          

  
  private CryptographyManager cryptomanager;
  private EncryptedCompositeFileUser user;
  public ExaminationCatalogue examcatalogue = null;
  LinkedList<ExaminationDataStatusListener> listeners = new LinkedList<ExaminationDataStatusListener>();

  public KeyData keysadmin;
  public KeyData keysexaminer;
  public KeyData keysobserver;
  
  public KeyData[] keysbyrole = new KeyData[3];
  
  public Hashtable<String, PersonData> persons = new Hashtable<>();
  public ArrayList<PersonData> persons_sorted = new ArrayList<>();
  public PersonListModel personlistmodel = new PersonListModel( this, persons_sorted );
  
  public Hashtable<String, CandidateData> candidates = new Hashtable<String, CandidateData>();
  public Vector<CandidateData> candidates_sorted = new Vector<CandidateData>();
  
  public QuestionDefinitions qdefs = null;
  public ArrayList<QuestionAnalysis> analyses = new ArrayList<>();
  public QuestionAnalysisTable analysistablemodel = new QuestionAnalysisTable( this, analyses );

  private File examfolder;
  
  public static final String mainarchivename = "qyouti.tar";
  public static final String mainfilename = "qyouti.xml";
  
  public static final String questionarchivename = "questions.tar";
  public static final String questionfilename = "questions.xml";
  
  public static final String scansarchivename = "scans.tar";
  public static final String scansfilename = "scans.xml";
  
  public static final String outcomearchivename = "outcomes.tar";
  public static final String outcomefilename = "outcomes.xml";
  
  public static final String examinerarchivename = "examiner.tar";
  public static final String examinerfilename = "examiner.xml";
  
  EncryptedCompositeFile mainarchive;
  EncryptedCompositeFile questionarchive;
  EncryptedCompositeFile scanarchive;
  EncryptedCompositeFile outcomearchive;
  EncryptedCompositeFile examinerarchive;

  private Vector<PrintedPageData> pages = new Vector<PrintedPageData>();
  public HashMap<String,PrintedPageData> pagemap = new HashMap<String,PrintedPageData>();
  public PageListModel pagelistmodel = new PageListModel( this, pages );

  public ExaminerData examinerdata = null;
  public OutcomeTables outcometables = null;
  
  public ScanData scans = new ScanData( this );
  
  Vector<DataTransformInstruction> datatransforminstructions = new Vector<DataTransformInstruction>();

  public Properties options = new Properties();
  public Properties default_options = new Properties();

  public QuestionMetricsRecordSetCache qmrcache;

  String lastprintid=null;
  private PaginationRecord lastpaginationrecord = null;
  
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
  
  public ExaminationData( CryptographyManager cryptomanager, ExaminationCatalogue examcatalogue,File examfolder )
          throws IOException
  {
    this.cryptomanager = cryptomanager;
    this.keysadmin    = new KeyData( cryptomanager );
    this.keysexaminer = new KeyData( cryptomanager );
    this.keysobserver = new KeyData( cryptomanager );
    keysbyrole[ExaminationData.EXAM_ROLE_ADMINISTRATOR] = keysadmin;
    keysbyrole[ExaminationData.EXAM_ROLE_EXAMINER]      = keysexaminer;
    keysbyrole[ExaminationData.EXAM_ROLE_OBSERVER]      = keysobserver;
    
    user = this.cryptomanager.getUser();
    this.examcatalogue = examcatalogue;
    this.examfolder = examfolder;
    
    mainarchive     = cryptomanager.getEncryptedCompositeFile(new File( examfolder, mainarchivename ), false);
    questionarchive = cryptomanager.getEncryptedCompositeFile(new File( examfolder, questionarchivename ), false);
    scanarchive     = cryptomanager.getEncryptedCompositeFile(new File( examfolder, scansarchivename ), false);
    outcomearchive  = cryptomanager.getEncryptedCompositeFile(new File( examfolder, outcomearchivename ), false);
    examinerarchive = cryptomanager.getEncryptedCompositeFile(new File( examfolder, examinerarchivename ), false);

    
    qmrcache = new QuestionMetricsRecordSetCache( examfolder );
    default_options.setProperty( "name_in_footer", "true" );
    default_options.setProperty( "id_in_footer", "true" );
    default_options.setProperty( "columns", "1" );
  }

  public void close() throws IOException
  {
    if ( questionarchive != null )
      questionarchive.close();
    if ( scanarchive != null )
      scanarchive.close();
    if ( outcomearchive != null )
      outcomearchive.close();
    if ( examinerarchive != null )
      examinerarchive.close();
    if ( mainarchive != null )
      mainarchive.close();
  }

  public boolean isCurrentUserInRole( int role )
  {
    KeyData data = keysbyrole[role];
    return data.contains( cryptomanager.getPreferredSecretKey().getKeyID() );
  }

  public void addAdministratorKey( long keyid )
  {
    keysadmin.addKey( keyid );
    setUnsavedChangesInMain(true);
  }
  
  public void addExaminerKey( long keyid )
  {
    keysexaminer.addKey( keyid );
    setUnsavedChangesInMain(true);
  }
  
  public void addObserverKey( long keyid )
  {
    keysobserver.addKey( keyid );
    setUnsavedChangesInMain(true);
  }
  
  public String getAdministratorKeyName( int index )
  {
    return keysadmin.getElementAt(index);
  }
  
  public String getExaminerKeyName( int index )
  {
    return keysexaminer.getElementAt(index);
  }
  
  public String getObserverKeyName( int index )
  {
    return keysobserver.getElementAt(index);
  }
  
  public void removeAdministratorKey( int index )
  {
    keysadmin.removeKeyAt(index);
    setUnsavedChangesInMain(true);
  }
  
  public void removeExaminerKey( int index )
  {
    keysexaminer.removeKeyAt(index);
    setUnsavedChangesInMain(true);
  }
  
  public void removeObserverKey( int index )
  {
    keysobserver.removeKeyAt(index);
    setUnsavedChangesInMain(true);
  }
  
  // Gather all use of "fireTable***" to methods here to make it hard to
  // forget that changes in one table may involve changes in others...
  public void processRowsInserted( ScanData model, int first, int last )
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
  public void processDataChanged()
  {
    fireTableDataChanged();
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
  public void processDataChanged( ScannedQuestionData model )
  {
    model.fireTableDataChanged();
  }
  public void processRowsUpdated( ScannedQuestionData model, int first, int last )
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
    return examfolder;
  }
  
//  public CompositeFile getScanImageArchive()
//  {
//    return scanarchive;
//  }
  
  public BufferedImage getImageFromScanArchive( String name )
  {
    if ( !scanarchive.exists(name) ) return null;
    BufferedImage img=null;
    try
    {
      InputStream in = scanarchive.getDecryptingInputStream( name );
      img = ImageIO.read(in);
      in.close();
    } catch (IOException ex)
    {
      Logger.getLogger(ScannedResponseData.class.getName()).log(Level.SEVERE, name );
      Logger.getLogger(ScannedResponseData.class.getName()).log(Level.SEVERE, null, ex);
    }
    return img;
  }
  
  public void sendImageToScanArchive( BufferedImage image, String name, String format )
  {
    try
    {
      OutputStream out = scanarchive.getEncryptingOutputStream( name, true, true );
      ImageIO.write(image, format, out );
      out.close();
    } catch (IOException ex)
    {
      Logger.getLogger(ScannedResponseData.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
  
  public OutputStream getScanArchiveOutputStream( String name )
          throws IOException
  {
    return scanarchive.getEncryptingOutputStream( name, true, true );
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
      Class<?> c = Class.forName( cn );
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

  public OutcomeCandidateData getOutcomeCandidateData( String candidateident, boolean create )
  {
    OutcomeCandidateData ocd = outcometables.cmap.get( candidateident );
    if ( ocd != null ) return ocd;
    if ( create)
    {
      ocd = new OutcomeCandidateData( this, candidateident );
      this.outcometables.cmap.put(candidateident, ocd);
    }
    return ocd;
  }
  
  public OutcomeData getOutcomeData( String candidateident, String questionident, boolean create )
  {
    OutcomeCandidateData ocd = getOutcomeCandidateData( candidateident, create );
    if ( ocd == null ) return null;
    OutcomeData oqd = ocd.getQuestionOutcomeData(questionident);
    if ( oqd != null) return oqd;
    if ( create )
    {
      oqd = new OutcomeData( this );
      ocd.addQuestionOutcomeData(questionident, oqd);
    }
    return oqd;
  }

  
  public OutcomeData getCandidateOutcomes( String candidateident )
  {
    return getOutcomeCandidateData( candidateident, true );
  }
  
  public OutcomeData getQuestionOutcomes( String candidateident, String questionident )
  {
    OutcomeData oqd = getOutcomeData( candidateident, questionident, true );
    return oqd;
  }
 
  public int getExaminerDecision( String candidateident, String questionident )
  {
    ExaminerQuestionData eqd = getExaminerQuestionData( candidateident, questionident, false );
    if ( eqd == null ) return ScannedQuestionData.EXAMINER_DECISION_NONE;
    return eqd.getExaminerdecision();
  }

  public void setExaminerDecision( String candidateident, String questionident, int n )
  {
    ExaminerQuestionData eqd = getExaminerQuestionData( candidateident, questionident, true );
    eqd.setExaminerdecision(n);
    setUnsavedChangesInExaminer(true);
  }
  
  public void setExaminerSelected( String candidateident, String questionident, String responseident, boolean b )
  {
    ExaminerResponseData erd = getExaminerResponseData( candidateident, questionident, responseident, true );
    erd.examiner_selected = b;
    setUnsavedChangesInExaminer(true);
  }
  
  public boolean isExaminerSelected( String candidateident, String questionident, String responseident )
  {
    ExaminerResponseData erd = getExaminerResponseData( candidateident, questionident, responseident, true );
    if ( erd == null ) return false;
    return erd.examiner_selected;
  }


  
  
  
  public void addToReviewList( CandidateData c, ScannedQuestionData q )
  {
    if ( reviewfilter == REVIEW_FILTER_UNREVIEWED && 
            getExaminerDecision( c.id, q.getIdent() ) != ScannedQuestionData.EXAMINER_DECISION_NONE )
      return;
    if ( reviewfilter == REVIEW_FILTER_OVERRIDDEN && 
            getExaminerDecision( c.id, q.getIdent() ) != ScannedQuestionData.EXAMINER_DECISION_OVERRIDE )
      return;
    if ( reviewfilter == REVIEW_FILTER_CONFIRMED && 
            getExaminerDecision( c.id, q.getIdent() ) != ScannedQuestionData.EXAMINER_DECISION_STAND )
      return;
    reviewlist.add( c, q );    
  }
  
  public void rebuildReviewList()
  {
    int i, j;
    CandidateData c;
    ScannedQuestionData q;
    
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

  public ScannedPageData getScannedPageData( String pageident )
  {
    return scans.getScannedPageData(pageident);
  }
  
  public void clearScans()
  {
    CandidateData candidate;
    PrintedPageData page;
    ScannedPageData spage;
    ScannedQuestionData question;
    ScannedResponseData response;

    outcometables.clearNonFixedOutcomes();
    outcometables.setUnsavedChanges( true );
    
    examinerdata.cmap.clear(); // only clearing decisions about student responses, not marking schemes
    examinerdata.setUnsavedChanges( true );

    scans.clearScanFileData();
    scans.clearScannedPageData();
    scans.setUnsavedChanges( true );
    
    pagelistmodel.fireTableDataChanged();
        
    try
    {
      scanarchive.close();
      Files.deleteIfExists( new File(scanarchive.getCanonicalPath()).toPath() );      
      scanarchive     = cryptomanager.getEncryptedCompositeFile(new File( examfolder, scansarchivename ),true);
    }
    catch ( Exception e )
    {
      e.printStackTrace();
    }
    
    setUnsavedChangesInMain( true );
    setUnsavedChangesInScans( true );
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
    setUnsavedChangesInOutcome( true );
    fireTableDataChanged();    
  }

  public void forgetPrint()
  {
    CandidateData candidate;
    
    setUnsavedChangesInMain( true );
    setPaginationRecord( null );
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
  
  public void addPage( PrintedPageData page )
  {
    pages.add( page );
    pagemap.put( page.pageid, page );
    pagelistmodel.fireTableDataChanged();
  }
  
  public PrintedPageData createPage(
                    String printid,
                    String pageid,
                    CandidateData candidate )
  {
    PrintedPageData page = new PrintedPageData( this, printid, pageid, candidate );
    addPage( page );
    linkPageToCandidate( page );
    return page;
  }
  
  public String getCandidateIdentFromPage( String pageident )
  {
    PrintedPageData ppd = lookUpPage( pageident );
    if ( ppd == null ) return null;
    return ppd.candidate_number;
  }
  
  public PrintedPageData getPage( int n )
  {
    return pages.get( n );
  }
  
  public PrintedPageData lookUpPage( String id )
  {
    return pagemap.get( id );
  }

  public void addScanImageFile( ImageFileData ifd )
  {
    scans.add( ifd );
    setUnsavedChangesInScans( true );
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
      if (candidate.getScore() != null)
        list.add( candidate.getScore().toString() );
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
    resolveMediaReferences( examfolder, basefolder, qtiexamfile.getParentFile(), nl );

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

  public CandidateData linkPageToCandidate(PrintedPageData page)
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
    outcometables.invalidateAllCandidates();
  }
  
  /** 
   * Marks scanned pages as not processed if they match the
   * criteria.
   * @param candidateident
  */
  public void invalidateOutcomes( String candidateident )
  {
    outcometables.invalidateCandidate( candidateident );
  }
  
  public void recomputeOutcomes()
  {
    invalidateAllOutcomes();
    updateOutcomes();
    setUnsavedChangesInOutcome( true );
    processDataChanged();    
  }
  
  public void updateOutcomes()
  {
    if ( outcometables == null || outcometables.cmap == null )
      return;
    for ( OutcomeCandidateData ocd : outcometables.cmap.values() )
      if ( !ocd.isValid() )
        updateOutcomes( ocd );
  }

  public void updateOutcomes( OutcomeCandidateData ocd )
  {    
    CandidateData c = this.candidates.get(ocd.getIdent());
    if ( c == null ) return;
    c.computeCandidateOutcomes();
    ocd.setValid( true );
    setUnsavedChangesInOutcome( true );
    processDataChanged();    
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
        writer = new OutputStreamWriter( examinerarchive.getEncryptingOutputStream( examinerfilename, true, true), "utf8");
        examinerdata.emit( writer );
        writer.close();
        writer = null;        
      }
      
      if ( outcometables != null && outcometables.areThereUnsavedChanges() )
      {
        writer = new OutputStreamWriter( outcomearchive.getEncryptingOutputStream( outcomefilename, true, true), "utf8");
        outcometables.emit( writer );
        writer.close();
        writer = null;                
      }
      
      if ( qdefs != null && qdefs.areThereUnsavedChanges() )
      {
        writer = new OutputStreamWriter( questionarchive.getEncryptingOutputStream(questionfilename, true, true), "utf8");
        qdefs.emit( writer );
        writer.close();
        writer = null;
      }
      
      if ( this.unsaved_changes )
      {
        writer = new OutputStreamWriter( mainarchive.getEncryptingOutputStream(mainfilename, true, true), "utf8");
        emit(writer);
        writer.close();
        writer = null;
//        for ( int i=0; i<datatransforminstructions.size(); i++ )
//        {
//          datatransforminstructions.get( i ).transform();
//        }
      }
      
      if ( this.scans.areThereUnsavedChanges() )
      {
        writer = new OutputStreamWriter( scanarchive.getEncryptingOutputStream( scansfilename,true, true), "utf8");
        writer.write("<?xml version=\"1.0\"?>\r\n<scans nextscanfileident=\"" );
        writer.write( Integer.toString(nextscanfileident) );
        writer.write("\">\r\n");
        writer.write("  <files>\r\n");
        for ( int i=0; i<scans.size(); i++ )
        {
          ImageFileData ifd = scans.get(i);
          ifd.emit(writer);
        }
        writer.write("  </files>\r\n");
        writer.write("  <pages>\r\n");
        for ( ScannedPageData page : scans.getScannedPageDataList() )
          page.emit(writer);
        writer.write("  </pages>\r\n");        
        writer.write("</scans>\r\n");
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
    if ( isCurrentUserInRole(EXAM_ROLE_ADMINISTRATOR) )
      updateAccessRights();
    return true;
  }

  public boolean areThereUnsavedChanges()
  {
    return unsaved_changes || 
            qdefs.areThereUnsavedChanges() || 
            ( examinerdata != null && examinerdata.areThereUnsavedChanges() ) || 
            (outcometables != null && outcometables.areThereUnsavedChanges())      ;
  }
  
  public void setUnsavedChangesInMain( boolean b )
  {
    if ( unsaved_changes != b )
    {
      unsaved_changes = b;
      fireStatusChange();
    }
  }

  public void setUnsavedChangesInScans( boolean b )
  {
    if ( scans.areThereUnsavedChanges() != b )
    {
      scans.setUnsavedChanges( b );
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
  
  public void setUnsavedChangesInOutcome( boolean b )
  {
    if ( outcometables.areThereUnsavedChanges() != b )
    {
      outcometables.setUnsavedChanges( b );
      fireStatusChange();
    }
  }
  
  public String getLastPrintID()
  {
    return lastprintid;
  }

  public void setPaginationRecord( PaginationRecord pr )
  {
    lastpaginationrecord = pr;
    if ( pr == null )
    {
      lastprintid = null;
      return;
    }
    
    OutputStreamWriter writer = null;
    try
    {
      lastprintid = pr.getPrintId();
      System.out.println( "Recording pagination data." );
      String name = "pagination_" + lastprintid + ".xml";
      writer = new OutputStreamWriter( mainarchive.getEncryptingOutputStream( name, true, true ), "utf8" );
      lastpaginationrecord.emit(writer);
    }
    catch (IOException ex)
    {
      Logger.getLogger(ExaminationData.class.getName()).log(Level.SEVERE, null, ex);
    }
    finally
    {
      try
      {
        if ( writer != null )
          writer.close();
      }
      catch (IOException ex)
      {
        Logger.getLogger(ExaminationData.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
    fireStatusChange();
  }

  public PaginationRecord getPaginationRecord( String id )
  {
    if ( lastprintid == null || !lastprintid.equals(id) )
      return null;
    return lastpaginationrecord;
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

  public void emitKeys( Writer writer, KeyData listmodel )
          throws IOException
  {
    for ( int i=0; i<listmodel.getSize(); i++ )
    {
      long keyid = listmodel.getKeyIdAt(i);
      writer.write( "    <key keyid=\"" );
      writer.write( Long.toUnsignedString(keyid, 16) );
      writer.write( "\"/>\r\n" );
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

    writer.write( "<keys>\r\n" );
    writer.write( "  <administrators>\r\n" );
    emitKeys( writer, keysadmin );
    writer.write( "  </administrators>\r\n" );
    writer.write( "  <examiners>\r\n" );
    emitKeys( writer, keysexaminer );
    writer.write( "  </examiners>\r\n" );
    writer.write( "  <observers>\r\n" );
    emitKeys( writer, keysobserver );
    writer.write( "  </observers>\r\n" );
    writer.write( "</keys>\r\n" );
    
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

  
  public void loadKeys( Element e, KeyData list )
          throws ParserConfigurationException, SAXException, IOException
  {
    NodeList nl = e.getChildNodes();
    Element ec;
    for ( int i=0; i<nl.getLength(); i++ )
    {
      if ( !(nl.item(i) instanceof Element) )
        continue;
      ec = (Element)nl.item(i);
      if ( !"key".equals(ec.getNodeName() ) )
        continue;
      String strkeyid = ec.getAttribute("keyid");
      long keyid;
      try
      {
        keyid = Long.parseUnsignedLong(strkeyid, 16);
      }
      catch ( NumberFormatException nfe )
      {
        continue;
      }
      list.addKey(keyid);
    }
  }
  
  public void loadKeys( Element e )
          throws ParserConfigurationException, SAXException, IOException
  {
    NodeList nl = e.getChildNodes();
    Element ec;
    KeyData activelist;
    for ( int i=0; i<nl.getLength(); i++ )
    {
      if ( !(nl.item(i) instanceof Element) )
        continue;
      ec = (Element)nl.item(i);
      
      activelist = null;
      if ( "administrators".equals(ec.getNodeName() ) )
        activelist = keysadmin;
      if ( "examiners".equals(ec.getNodeName() ) )
        activelist = keysexaminer;
      if ( "observers".equals(ec.getNodeName() ) )
        activelist = keysobserver;
      if ( activelist == null )
        continue;
      
      loadKeys( ec, activelist );
    }
  }

  public class CandidateComparator implements Comparator<CandidateData>
  {
    @Override
    public int compare(CandidateData a, CandidateData b)
    {
      return a.id.compareToIgnoreCase(b.id);
    }
  }

  public void sortCandidates()
  {
    //Collections.sort( (Vector)candidates_sorted, new CandidateComparator() );
  }

  public class PersonComparator implements Comparator<PersonData>
  {
    @Override
    public int compare(PersonData a, PersonData b)
    {
      return a.getName().compareToIgnoreCase(b.getName());
    }
  }

  public void sortPersons()
  {
    Collections.sort( persons_sorted, new PersonComparator() );
  }
  
  
 
  
  public void load()
          throws ParserConfigurationException, SAXException, IOException
  {
    if ( !mainarchive.exists(mainfilename) )
      return;
    
    loadQuestions();
    
    loadMain();
    loadPagination();
    
    if ( examinerarchive.exists( examinerfilename ) )
      loadExaminerData();
    else
      loadBlankExaminerData();
    qdefs.qti.setOverride( examinerdata.examinerqdefs.qti );

    loadScanData();
    
    loadOutcomeData();
    
    rebuildReviewList();
  }

  private void loadBlankExaminerData()
          throws ParserConfigurationException, SAXException, IOException
  {
    String strempty = "<?xml version=\"1.0\"?><examinerdata><questestinterop xmlns=\"http://www.imsglobal.org/xsd/ims_qtiasiv1p2\" xmlns:qyouti=\"http://www.qyouti.org/qtiext\"/></examinerdata>";
    ByteArrayInputStream in = new ByteArrayInputStream(strempty.getBytes("UTF-8"));
    InputSource ins = new InputSource( in );
    loadExaminerData( ins );
  }
  
  private ExaminerData loadExaminerData()
          throws ParserConfigurationException, SAXException, IOException
  {
    InputStream in = examinerarchive.getDecryptingInputStream( examinerfilename );
    InputSource source = new InputSource( in );
    return loadExaminerData( source );
  }
  
  private ExaminerData loadExaminerData( InputSource source )
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
    return examinerdata;
  }
  
  private void loadScanData()
  {
    InputStream in = null;
    try
    {
      if ( !scanarchive.exists(scansfilename) )
        return;
      in = scanarchive.getDecryptingInputStream(scansfilename);
      InputSource source = new InputSource( in );
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.parse( source );
      Element roote = document.getDocumentElement();
      
      String a = roote.getAttribute( "nextscanfileident" );
      if ( a != null && a.length() > 0 )
        nextscanfileident = new Integer( a ).intValue();
      
      NodeList cnl = roote.getElementsByTagName("file");
      for (int j = 0; j < cnl.getLength(); j++)
        scans.add( new ImageFileData(this, (Element) cnl.item(j) ) );

      NodeList pnl = roote.getElementsByTagName("page");
      for (int j = 0; j < pnl.getLength(); j++)
      {
        ScannedPageData spage = new ScannedPageData( this, (Element)pnl.item(j) );
        scans.addScannedPageData(spage);
      }
    }
    catch (IOException ex)
    {
      Logger.getLogger(ExaminationData.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (ParserConfigurationException ex)
    {
      Logger.getLogger(ExaminationData.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (SAXException ex)
    {
      Logger.getLogger(ExaminationData.class.getName()).log(Level.SEVERE, null, ex);
    }
    finally
    {
      try
      {
        if ( in != null )
          in.close();
      }
      catch (IOException ex)
      {
        Logger.getLogger(ExaminationData.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }
  
  
  private void loadOutcomeData()
          throws ParserConfigurationException, SAXException, IOException
  {
    outcometables = new OutcomeTables();
    if ( !outcomearchive.exists( outcomefilename ) )
      return;
    
    InputStream in = outcomearchive.getDecryptingInputStream(outcomefilename);
    InputSource source = new InputSource( in );
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document document = builder.parse( source );
    Element roote = document.getDocumentElement();
    if ("outcomes".equals(roote.getNodeName()))
      outcometables = new OutcomeTables( this, roote );
    in.close();
  }
  
  private void loadQuestions()
          throws ParserConfigurationException, SAXException, IOException
  {
    InputStream in = questionarchive.getDecryptingInputStream(questionfilename);
    InputSource source = new InputSource( in );
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document document = builder.parse( source );
    Element roote = document.getDocumentElement();
    qdefs=null;
    if ("questestinterop".equals(roote.getNodeName()))
      qdefs = new QuestionDefinitions(roote, personlistmodel );
    //in.close();
  }

  private void loadPagination()
          throws ParserConfigurationException, SAXException, IOException
  {
    lastpaginationrecord = null;
    if ( this.lastprintid == null )
      return;
    
    String name = "pagination_" + lastprintid + ".xml";
    if ( !mainarchive.exists(name) )
      return;

    InputStream in = mainarchive.getDecryptingInputStream(name);
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder;
    Document document;
    try
    {
      builder = factory.newDocumentBuilder();
      document = builder.parse( in );
      lastpaginationrecord = new PaginationRecord( document );
    }
    catch ( ParserConfigurationException | SAXException | IOException ex )
    {
      Logger.getLogger( ExaminationCatalogue.class.getName() ).
              log( Level.SEVERE, null, ex );
    }
    in.close();
  }
  
  private void loadMain()
          throws ParserConfigurationException, SAXException, IOException
  {
    InputStream in = mainarchive.getDecryptingInputStream(mainfilename);
    InputSource source = new InputSource( in );
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

      if ("keys".equals(e.getNodeName()))
      {
        loadKeys( e );
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
      
      if ("pages".equals(e.getNodeName()))
      {
         cnl = e.getElementsByTagName("page");
        PrintedPageData page;
        for (int j = 0; j < cnl.getLength(); j++)
        {
          page = new PrintedPageData(this, (Element) cnl.item(j) );
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
    //in.close();
    
    for ( int p=0; p<getPageCount(); p++ )
      getPage( p ).postLoad();
  
    //rebuildOutcomeNameList();
    
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
  public Class<?> getColumnClass(int columnIndex)
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
  
  private void updateAccessRights( EncryptedCompositeFile f, KeyData keys, int permissions )
  {
    try
    {
      for ( KeyDatum keydatum : keys.keylist )
      {
          f.addPublicKey( keydatum.publickey );
          f.setPermission( keydatum.publickey, permissions );
      }
    }
    catch (IOException | NoSuchProviderException | NoSuchAlgorithmException ex)
    {
      Logger.getLogger(ExaminationData.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
  
  private AclEntry addPrincipalToACEBuilder( FileSystem filesystem, AclEntry.Builder builder, String name )
  {
    UserPrincipal principal;
    try
    {
      // TODO get rid of hard-wired windows domain
      // by looking at domain part of user email address
      System.out.println( "Looking for " + name );
      principal = filesystem.getUserPrincipalLookupService().lookupPrincipalByName( name );
    }
    catch ( Exception e )
    {
      System.out.println( "Not found." );
      principal=null;
    }
    if ( principal != null )
    {
      builder.setPrincipal(principal);
      return builder.build();
    }    
    return null;
  }
  
  private void updateOperatingSystemAccessRights( Path file, List<Set<AclEntryPermission>> permsetlist ) throws IOException
  {
    AclFileAttributeView view = Files.getFileAttributeView(file, AclFileAttributeView.class);
    if ( view == null )
      throw new IOException("Unable to set file permissions on " + file.toString() + " because the file system does not support access rights." );
    List<AclEntry> oldlist = view.getAcl();
    System.out.println( "Dumping ACL for: " + file.toString() );
    for ( AclEntry entry : oldlist )
      System.out.println( "ACL Entry: " + entry.toString() );
    System.out.println( "End of ACL " );


    FileSystem filesystem = file.getFileSystem();
    ArrayList<AclEntry> aclentrylist = new ArrayList<>();
    AclEntry.Builder builder = AclEntry.newBuilder();
    Pattern pattern = Pattern.compile("\\((.*?)\\)");

    builder.setType(AclEntryType.ALLOW);
    builder.setFlags(); 
    builder.setPermissions( permsetlist.get( 0 ) );
    AclEntry ace = addPrincipalToACEBuilder( filesystem, builder, "\\OWNER RIGHTS" );
    if ( ace == null )
      throw new IOException( "Unable to find the '\\OWNER RIGHTS security principal trying to access rights on " + file.toString() );
    aclentrylist.add(ace);

    for ( int r=0; r<keysbyrole.length; r++ )
    {
      builder.setType(AclEntryType.ALLOW);
      builder.setFlags(); 
      builder.setPermissions( permsetlist.get(r) );
      for ( KeyDatum k : keysbyrole[r].keylist )
      {
        Matcher matcher =pattern.matcher(k.displayname);
        if ( !matcher.find() )
          throw new IOException( "Unable to find a computer user name in parentheses as part of the key name " + k.displayname + " Prevented setting access rights on " +file.toString() );
        String name = "LEEDSBECKETT\\" + matcher.group(1);            // TODO get rid of hard coded domain
        ace = addPrincipalToACEBuilder( filesystem, builder, name );
        if ( ace == null )
          throw new IOException( "Unable to find the given computer user name, " + name + " on the file system. Prevented setting access rights on " +file.toString() );
        aclentrylist.add(ace);
      }
    }

    System.out.println( "Dumping proposed ACL for: " + view.name() );
    for ( AclEntry entry : aclentrylist )
      System.out.println( "ACL Entry: " + entry.toString() );
    System.out.println( "End of ACL " );

    // if same entries in same order no need to save.
    if ( oldlist.size() == aclentrylist.size() )
    {
      boolean equal = true;
      for ( int i=0; i<oldlist.size(); i++ )
      {
        if ( !oldlist.get(i).equals(aclentrylist.get(i)) )
        {
          equal = false;
          break;
        }
      }
      if ( equal )
      {
        System.out.println( "Current ACL already matches. No need to save." );
        return;
      }
    }
    
    
    view.setAcl( aclentrylist );

    System.out.println( "Dumping ACL for: " + view.name() );
    for ( AclEntry entry : view.getAcl() )
      System.out.println( "ACL Entry: " + entry.toString() );
    System.out.println( "End of ACL " );

  }
  
  public void updateAccessRights()
  {
    ArrayList<EncryptedCompositeFile> allfiles = new ArrayList<>();
    ArrayList<EncryptedCompositeFile> examinerfiles = new ArrayList<>();
    allfiles.add( mainarchive );
    allfiles.add( questionarchive );
    allfiles.add( scanarchive );
    allfiles.add( examinerarchive );
    allfiles.add( outcomearchive );
    examinerfiles.add( examinerarchive );
    examinerfiles.add( outcomearchive );
    
    
    ArrayList<Set<AclEntryPermission>> permsetlist = new ArrayList<>();
    
    for ( EncryptedCompositeFile f : allfiles )
    {
      updateAccessRights( f, keysadmin, EncryptedCompositeFile.ALL_PERMISSIONS );
      if ( examinerfiles.contains(f) )
        updateAccessRights( f, keysexaminer, EncryptedCompositeFile.READ_PERMISSION | EncryptedCompositeFile.WRITE_PERMISSION );
      else
        updateAccessRights( f, keysexaminer, EncryptedCompositeFile.READ_PERMISSION );
      updateAccessRights( f, keysobserver, EncryptedCompositeFile.READ_PERMISSION );
      
      permsetlist.clear();
      permsetlist.add( FULLPERMISSIONS );
      permsetlist.add( examinerfiles.contains(f)?READWRITEPERMISSIONS:READPERMISSIONS );
      permsetlist.add( READPERMISSIONS );
      Path file = new File( f.getCanonicalPath() ).toPath();
      try
      {
        updateOperatingSystemAccessRights( file, permsetlist );
      }
      catch ( IOException ioe )
      {
        JOptionPane.showMessageDialog(null, "Data was saved but failed to set access rights on the saved files.\nReason:\n" + ioe.getMessage() );
      }
    }
  }
  
  public static void saveNewExamination( CryptographyManager cryptomanager, File examfolder, String strmain, String strquestions )
  {
    Writer writer=null;
    EncryptedCompositeFile temparchive=null;
    EncryptedCompositeFileUser user = cryptomanager.getUser();

    File teamfile = new File( examfolder.getParentFile(), "teamkeyring.tar" );
    try
    {
      cryptomanager.setTeamKeyRingFile(teamfile, !teamfile.exists() );
    }
    catch (IOException | NoSuchProviderException | NoSuchAlgorithmException | PGPException | WrongPasswordException ex)
    {
      Logger.getLogger(QyoutiFrame.class.getName()).log(Level.SEVERE, null, ex);
      return;
    }
    
    try
    {
      temparchive = cryptomanager.getEncryptedCompositeFile(new File( examfolder, mainarchivename ),true);
      writer = new OutputStreamWriter( temparchive.getEncryptingOutputStream(mainfilename, true, true), "utf8");
      writer.write( strmain );
    }
    catch ( Exception ex )
    {
      Logger.getLogger( ExaminationData.class.getName() ).
              log( Level.SEVERE, null, ex );
    }
    finally
    {
      if ( writer != null )
      {
        try
        {
          writer.close();
        }
        catch ( IOException ex )
        {
          Logger.getLogger( ExaminationData.class.getName() ).
                  log( Level.SEVERE, null, ex );
        }
      }
      if ( temparchive != null )
      {
        temparchive.close();
      }
    }
    
    writer = null;
    try
    {
      temparchive = cryptomanager.getEncryptedCompositeFile(new File( examfolder, questionarchivename ), true);
      writer = new OutputStreamWriter( temparchive.getEncryptingOutputStream( questionfilename, true, true ), "utf8");
      writer.write( strquestions );
    }
    catch ( Exception ex )
    {
      Logger.getLogger( ExaminationData.class.getName() ).
              log( Level.SEVERE, null, ex );
    }
    finally
    {
      if ( writer != null )
      {
        try
        {
          writer.close();
        }
        catch ( IOException ex )
        {
          Logger.getLogger( ExaminationData.class.getName() ).
                  log( Level.SEVERE, null, ex );
        }
      }
      if ( temparchive != null )
      {
        temparchive.close();
      }
    }    

    try
    {
      temparchive = cryptomanager.getEncryptedCompositeFile(new File( examfolder, scansarchivename ), true);
      temparchive.close();
      temparchive = cryptomanager.getEncryptedCompositeFile(new File( examfolder, outcomearchivename ), true);
      temparchive.close();
      temparchive = cryptomanager.getEncryptedCompositeFile(new File( examfolder, examinerarchivename ), true);
      temparchive.close();
    }
    catch ( Exception ex )
    {
      Logger.getLogger( ExaminationData.class.getName() ).log( Level.SEVERE, null, ex );
    }

  }
  
}

