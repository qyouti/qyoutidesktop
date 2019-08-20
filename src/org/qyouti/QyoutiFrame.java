/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti;

import au.com.bytecode.opencsv.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.xml.transform.*;
import org.apache.avalon.framework.configuration.*;
import org.apache.batik.dom.*;
import org.apache.batik.swing.gvt.*;
import org.apache.batik.transcoder.*;
import org.apache.fop.fonts.*;
import org.apache.fop.svg.*;
import org.apache.fop.svg.font.*;
import org.qyouti.barcode.*;
import org.qyouti.data.*;
import org.qyouti.fonts.*;
import org.qyouti.print.*;
import org.qyouti.qti1.element.*;
import org.qyouti.qti1.gui.*;
import org.qyouti.scan.*;
import org.qyouti.templates.*;
import org.w3c.dom.*;

/**
 *
 * @author jon
 */
public class QyoutiFrame
        extends javax.swing.JFrame
        implements WindowListener, ExaminationDataStatusListener,
                   ScanTaskListener
{

  QyoutiPreferences preferences;
  QyoutiFontManager fontmanager;
  
  File examfolder = null;
  ExaminationData exam;
  ExaminationCatalogue examcatalogue;
  File homefolder;
  File basefolder;
  String examname = null;

  ExamSelectDialog selectdialog;
  BusyDialog busydialog;

  String editquestionident;
  QuestionData currentquestiondata;
  String scanfolder = null;

  boolean customisationpanelupdating=false;
  
  TableModel emptytablemodel = new javax.swing.table.DefaultTableModel( new Object [][]{},new String []{} );
  
  // Possible Look & Feels
  private static final String mac      =
          "com.apple.laf.AquaLookAndFeel";
  private static final String nimbus   =
          "javax.swing.plaf.nimbus.NimbusLookAndFeel";
  private static final String metal    =
          "javax.swing.plaf.metal.MetalLookAndFeel";
  private static final String motif    =
          "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
  private static final String windows  =
          "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
  private static final String gtk  =
          "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
  String currentLookAndFeel=metal;
  JMenu lafMenu;
  
  /**
   * Creates new form QyoutiFrame
   */
  public QyoutiFrame()
  {
    currentLookAndFeel = UIManager.getLookAndFeel().getClass().getName();

    homefolder = new File( System.getProperty( "user.home" ) );
    homefolder = new File( homefolder, "qyouti" );
    if ( !homefolder.exists() )
      homefolder.mkdir();
    String strbasefolder = System.getProperty( "qyouti.exambase" );
    if ( strbasefolder != null && strbasefolder.length() > 0 )
      basefolder = new File( strbasefolder );
    else
      basefolder = new File( homefolder, "exams" );
    
    File preferences_file = new File( homefolder, "preferences.xml" );
    preferences = new QyoutiPreferences( preferences_file );
    if ( preferences_file.exists() )
      preferences.load();
    else
    {
      preferences.setDefaults();
      preferences.save();
    }
    
    fontmanager = new QyoutiFontManager( preferences );

    busydialog = new BusyDialog( this, false );
    selectdialog = new ExamSelectDialog( this, true );
    selectdialog.setFrame( this );
    selectdialog.setBaseFolder( basefolder );
    
    initComponents();
    
    previewsvgcanvas.getInteractors().clear();
    //previewsvgcanvas.getInteractors().add( panInteractor );

    createLookAndFeelMenu();
    
    //ZXingCodec.setDebugImageLabel( debuglabel );
    
    persontable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
    persontable.getSelectionModel().addListSelectionListener( 
            new ListSelectionListener() {
              
      @Override
      public void valueChanged( ListSelectionEvent e )
      {
        personSelectionChanged( e );
      }
              
            }
    );
    
    //questiontable.getColumnModel().getColumn( 0 ).
    questiontable.getSelectionModel().addListSelectionListener( 
            new ListSelectionListener() {
              
      @Override
      public void valueChanged( ListSelectionEvent e )
      {
        questionSelectionChanged( e );
      }
              
            });
    
    
    questionreviewtable.getSelectionModel().addListSelectionListener( 
            new ListSelectionListener() {
              
      @Override
      public void valueChanged( ListSelectionEvent e )
      {
        reviewSelectionChanged( e );
      }
              
            });
    
    qrevlefttable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
    qrevlefttable.getSelectionModel().addListSelectionListener( 
            new ListSelectionListener() {
              
      @Override
      public void valueChanged( ListSelectionEvent e )
      {
        reviewLeftSelectionChanged( e );
      }
              
            });
    this.addWindowListener( this );
    this.setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE );
  }

  
    /**
     * Create menus
     */
    public void createLookAndFeelMenu()
    {
        JMenuItem mi;


        // ***** create laf switcher menu
        lafMenu = (JMenu) this.menubar.add(new JMenu("Look/Feel"));
        ButtonGroup group = new ButtonGroup();
        mi = createLafMenuItem(lafMenu, "Metal",group, metal);
        mi.setSelected(true); // this is the default l&f

        mi = createLafMenuItem(lafMenu, "Nimbus", group, nimbus);

        UIManager.LookAndFeelInfo[] lafInfo = UIManager.
                                       getInstalledLookAndFeels();

        for (int counter = 0; counter < lafInfo.length; counter++) {
            String className = lafInfo[counter].getClassName();
            if (className == motif) {
                createLafMenuItem(lafMenu, "Motif", group, motif);
            } else if (className == windows) {
                createLafMenuItem(lafMenu, "Windows", group, windows);
            } else if (className == gtk) {
                createLafMenuItem(lafMenu, "Gtk", group, gtk);
            } else if (className == mac) {
                createLafMenuItem(lafMenu, "Mac", group, mac);
            }
        }

        for (int i = 0; i < lafMenu.getItemCount(); i++) {
            JMenuItem item = lafMenu.getItem(i);
            item.setSelected(item.getText().equalsIgnoreCase( currentLookAndFeel ) );
        }
    }
  
      /**
     * Creates a JRadioButtonMenuItem for the Look and Feel menu
     */
    public JMenuItem createLafMenuItem(JMenu menu, String label, ButtonGroup group, String laf) {
        JMenuItem mi = (JRadioButtonMenuItem) menu.add(new JRadioButtonMenuItem( label ));
        group.add(mi);
        mi.addActionListener(new ChangeLookAndFeelAction(this, laf));

        mi.setEnabled(isAvailableLookAndFeel(laf));

        return mi;
    }

    /**
     * A utility function that layers on top of the LookAndFeel's
     * isSupportedLookAndFeel() method. Returns true if the LookAndFeel
     * is supported. Returns false if the LookAndFeel is not supported
     * and/or if there is any kind of error checking if the LookAndFeel
     * is supported.
     *
     * The L&F menu will use this method to detemine whether the various
     * L&F options should be active or inactive.
     *
     */
     protected boolean isAvailableLookAndFeel(String laf) {
         try {
             Class lnfClass = Class.forName(laf);
             LookAndFeel newLAF = (LookAndFeel)(lnfClass.newInstance());
             return newLAF.isSupportedLookAndFeel();
         } catch(Exception e) { // If ANYTHING weird happens, return false
             return false;
         }
     }
  
    /**
     * Stores the current L&F, and calls updateLookAndFeel, below
     */
    public void setLookAndFeel(String laf) {
        if(!currentLookAndFeel.equals(laf)) {
            currentLookAndFeel = laf;
            /* The recommended way of synchronizing state between multiple
             * controls that represent the same command is to use Actions.
             * The code below is a workaround and will be replaced in future
             * version of SwingSet2 demo.
             */
            updateLookAndFeel();
            for(int i=0;i<lafMenu.getItemCount();i++) {
                JMenuItem item = lafMenu.getItem(i);
                item.setSelected(item.getText().equalsIgnoreCase(currentLookAndFeel));
            }
        }
    }

    /**
     * Sets the current L&F on each demo module
     */
    public void updateLookAndFeel() {
        try {
            UIManager.setLookAndFeel(currentLookAndFeel);
                updateThisSwingSet();
        } catch (Exception ex) {
            System.out.println("Failed loading L&F: " + currentLookAndFeel);
            System.out.println(ex);
        }
    }

    private void updateThisSwingSet() {
      SwingUtilities.updateComponentTreeUI(this);
    }
     
  public void personTableChanged( TableModelEvent e )
  {
    updatePersonCustomisePanel();
  }
  
  public void personSelectionChanged( ListSelectionEvent e )
  {
    if ( e.getValueIsAdjusting() )
      return;
    updatePersonCustomisePanel();
  }
  
  public void updatePersonCustomisePanel()
  {
    System.out.println( "updating customise panel...." );
    
    // fields will fire off change events so make
    // sure that doesn't bounce back and update the table
    customisationpanelupdating = true;

    try
    {
      PersonData person = null;
      int r = persontable.getSelectedRow();
      if ( r >= 0 )
        person = exam.persons_sorted.get( r );

      if ( person == null )
      {
        ((CardLayout)personpanel.getLayout()).show( personpanel, "noselection" );
        return;
      }

      if ( !person.isCustomisable() )
      {
        ((CardLayout)personpanel.getLayout()).show( personpanel, "noedit" );
        return;
      }

      ((CardLayout)personpanel.getLayout()).show( personpanel, "selection" );
      customforlabel.setText( person.getName() + " (" + person.getId() + ")" );
      bigpinkcheckbox.setSelected( false );
      onecolumncheckbox.setSelected( false );
      serifcheckbox.setSelected( false );
      fontsizelist.setSelectedIndex( 0 );
      colourswatchlabel.setBackground( Color.WHITE );

      UserRenderPreferences pref = person.getPreferences();
      if ( pref == null )
        return;
      double fsz = pref.getFontsize();
      bigpinkcheckbox.setSelected(            pref.isBigpinkbox()       );
      onecolumncheckbox.setSelected(            pref.isOneColumn()       );
      serifcheckbox.setSelected(              pref.isSerif()            );

      if ( fsz < 0.10  )
        fontsizelist.setSelectedIndex( 0 );
      else if ( fsz < 0.12  )
        fontsizelist.setSelectedIndex( 1 );
      else if ( fsz < 0.16  )
        fontsizelist.setSelectedIndex( 2 );
      else
        fontsizelist.setSelectedIndex( 3 );      
      colourswatchlabel.setBackground( pref.getBackground() );
    }
    finally
    {
      customisationpanelupdating = false;

    }
  }
  
  public void questionSelectionChanged( ListSelectionEvent e )
  {
    if ( e.getValueIsAdjusting() )
      return;
    
    System.out.println( "question selection change." );
    questiontable.getSelectedRow();

    QTIElementItem item = null;
    int row = questiontable.getSelectedRow();
    if ( row < 0 )
    {
      exam.analysistablemodel.setSelectedQuestion( null );
      setPreviewItem( null, row );
    }
    else
    {
      item = exam.qdefs.qti.getItems().elementAt( row );
      setPreviewItem( item, row );
      exam.analysistablemodel.setSelectedQuestion( (item==null)?null:item.getIdent() );
//      try
//      {
//        exam.qdefs.emit( new PrintWriter( System.out ), item );
//      }
//      catch ( IOException ex )
//      {
//        Logger.getLogger( QyoutiFrame.class.getName() ).log( Level.SEVERE, null, ex );
//      }
//      catch ( TransformerException ex )
//      {
//        Logger.getLogger( QyoutiFrame.class.getName() ).log( Level.SEVERE, null, ex );
//      }
    }
    
//    if ( row >= 0 )
//    {
//      CandidateData cd;
//      for ( int j=0; j<exam.candidates_sorted.size(); j++ )
//      {
//        cd = exam.candidates_sorted.get( j );
//        
//        GridBagConstraints gbc = new GridBagConstraints();
//        gbc.gridx = 0;
//        gbc.weightx = 0.5;
//        gbc.weighty = 0.5;
//        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
//        gbc.fill = GridBagConstraints.HORIZONTAL;
//        CandidateQuestionPanel cqp;
//        cqp = new CandidateQuestionPanel( cd, item.getIdent() );
//        if ( cqp != null )
//          qreviewpanel.add( cqp, gbc );
//        sp7.revalidate();
//      }
//    }
  }
    
  boolean confirmDataLoss( String message )
  {
    if ( exam != null && exam.areThereUnsavedChanges() )
    {
      if ( JOptionPane.
              showConfirmDialog( this, "There is unsaved data.\n" + message, "Confirmation", JOptionPane.YES_NO_OPTION ) != JOptionPane.YES_OPTION )
      {
        return false;
      }
      try      
      {
        exam.close();
      }
      catch (IOException ex)
      {
        Logger.getLogger(QyoutiFrame.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
    return true;
  }

  void confirmExit()
  {
    if ( confirmDataLoss( "Are you sure you want to exit?" ) )
    {
      this.setVisible( false );
      this.dispose();
      System.exit( 0 );
    }
  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents()
  {
    java.awt.GridBagConstraints gridBagConstraints;

    reviewtypebuttongroup = new javax.swing.ButtonGroup();
    reviewincludebuttongroup = new javax.swing.ButtonGroup();
    fontprefbuttongroup = new javax.swing.ButtonGroup();
    spacerlabel = new javax.swing.JLabel();
    centralpanel = new javax.swing.JPanel();
    noexamloadedpanel = new javax.swing.JPanel();
    noexamloadedlabel = new javax.swing.JLabel();
    tabs = new javax.swing.JTabbedPane();
    qtab = new javax.swing.JPanel();
    jSplitPane3 = new javax.swing.JSplitPane();
    sp1 = new javax.swing.JScrollPane();
    questiontable = new javax.swing.JTable();
    jTabbedPane1 = new javax.swing.JTabbedPane();
    jPanel8 = new javax.swing.JPanel();
    jPanel10 = new javax.swing.JPanel();
    jButton1 = new javax.swing.JButton();
    qprevscrollpane = new javax.swing.JScrollPane();
    previewsvgcanvas = new org.apache.batik.swing.JSVGCanvas();
    jPanel2 = new javax.swing.JPanel();
    questionanalysistoppane = new javax.swing.JPanel();
    jLabel4 = new javax.swing.JLabel();
    jScrollPane4 = new javax.swing.JScrollPane();
    analysistable = new javax.swing.JTable();
    jScrollPane5 = new javax.swing.JScrollPane();
    jTextPane1 = new javax.swing.JTextPane();
    perstab = new javax.swing.JPanel();
    jSplitPane1 = new javax.swing.JSplitPane();
    jPanel12 = new javax.swing.JPanel();
    jPanel13 = new javax.swing.JPanel();
    allanonbutton = new javax.swing.JButton();
    noneanonbutton = new javax.swing.JButton();
    jScrollPane3 = new javax.swing.JScrollPane();
    persontable = new javax.swing.JTable();
    jScrollPane6 = new javax.swing.JScrollPane();
    personpanel = new javax.swing.JPanel();
    nopersonselectedpanel = new javax.swing.JPanel();
    jLabel1 = new javax.swing.JLabel();
    noeditpersonpanel = new javax.swing.JPanel();
    jLabel3 = new javax.swing.JLabel();
    selectedpersonpanel = new javax.swing.JPanel();
    jLabel5 = new javax.swing.JLabel();
    customforlabel = new javax.swing.JLabel();
    resetpreferencesbutton = new javax.swing.JButton();
    bigpinkcheckbox = new javax.swing.JCheckBox();
    serifcheckbox = new javax.swing.JCheckBox();
    onecolumncheckbox = new javax.swing.JCheckBox();
    fontdizelabel = new javax.swing.JLabel();
    jScrollPane7 = new javax.swing.JScrollPane();
    fontsizelist = new javax.swing.JList<>();
    jLabel6 = new javax.swing.JLabel();
    jPanel11 = new javax.swing.JPanel();
    colourswatchlabel = new javax.swing.JLabel();
    changecolourbutton = new javax.swing.JButton();
    ctab = new javax.swing.JPanel();
    sp2 = new javax.swing.JScrollPane();
    candidatetable = new javax.swing.JTable();
    ptab = new javax.swing.JPanel();
    sp3 = new javax.swing.JScrollPane();
    pagestable = new javax.swing.JTable();
    stab = new javax.swing.JPanel();
    sp4 = new javax.swing.JScrollPane();
    scanfiletable = new javax.swing.JTable();
    rtab = new javax.swing.JPanel();
    jSplitPane2 = new javax.swing.JSplitPane();
    jPanel4 = new javax.swing.JPanel();
    jPanel5 = new javax.swing.JPanel();
    jPanel7 = new javax.swing.JPanel();
    jPanel1 = new javax.swing.JPanel();
    reviewtype1button = new javax.swing.JRadioButton();
    reviewtype2button = new javax.swing.JRadioButton();
    reviewtype3button = new javax.swing.JRadioButton();
    jPanel3 = new javax.swing.JPanel();
    reviewincludeall = new javax.swing.JRadioButton();
    reviewincludenotreviewed = new javax.swing.JRadioButton();
    reviewincludeconfirmed = new javax.swing.JRadioButton();
    reviewincludeoverridden = new javax.swing.JRadioButton();
    jPanel6 = new javax.swing.JPanel();
    previousreviewbutton = new javax.swing.JButton();
    nextreviewbutton = new javax.swing.JButton();
    jSplitPane4 = new javax.swing.JSplitPane();
    jScrollPane1 = new javax.swing.JScrollPane();
    qrevlefttable = new javax.swing.JTable();
    jPanel9 = new javax.swing.JPanel();
    sp8 = new javax.swing.JScrollPane();
    questionreviewtable = new javax.swing.JTable();
    jScrollPane2 = new javax.swing.JScrollPane();
    qrpanelouter = new javax.swing.JPanel();
    questionreviewpanel = new javax.swing.JPanel();
    jLabel2 = new javax.swing.JLabel();
    statuspanel = new javax.swing.JPanel();
    savestatuslabel = new javax.swing.JLabel();
    printstatuslabel = new javax.swing.JLabel();
    errorlabel = new javax.swing.JLabel();
    progressbar = new javax.swing.JProgressBar();
    menubar = new javax.swing.JMenuBar();
    filemenu = new javax.swing.JMenu();
    newmenuitem = new javax.swing.JMenuItem();
    openmenuitem = new javax.swing.JMenuItem();
    savemenuitem = new javax.swing.JMenuItem();
    propsmenuitem = new javax.swing.JMenuItem();
    sep1 = new javax.swing.JPopupMenu.Separator();
    configmenuitem = new javax.swing.JMenuItem();
    aboutmenuitem = new javax.swing.JMenuItem();
    sep1b = new javax.swing.JPopupMenu.Separator();
    exitmenuitem = new javax.swing.JMenuItem();
    actionmenu = new javax.swing.JMenu();
    pdfprintmenuitem = new javax.swing.JMenuItem();
    forgetprintmenuitem = new javax.swing.JMenuItem();
    jSeparator1 = new javax.swing.JPopupMenu.Separator();
    importqmenuitem = new javax.swing.JMenuItem();
    editquestionmenuitem = new javax.swing.JMenuItem();
    editallquestionsmenuitem = new javax.swing.JMenuItem();
    itemanalysismenuitem = new javax.swing.JMenuItem();
    printitemanalysismenuitem = new javax.swing.JMenuItem();
    sep2 = new javax.swing.JPopupMenu.Separator();
    importcanmenuitem = new javax.swing.JMenuItem();
    jSeparator2 = new javax.swing.JPopupMenu.Separator();
    importimagesmenuitem = new javax.swing.JMenuItem();
    clearscanneddatamenuitem = new javax.swing.JMenuItem();
    viewscanmenuitem = new javax.swing.JMenuItem();
    recomputemenuitem = new javax.swing.JMenuItem();
    sep3 = new javax.swing.JPopupMenu.Separator();
    expscoresmenuitem = new javax.swing.JMenuItem();
    exprepliesmenuitem = new javax.swing.JMenuItem();
    reviewmenu = new javax.swing.JMenu();
    previousreviewmenuitem = new javax.swing.JMenuItem();
    nextreviewmenuitem = new javax.swing.JMenuItem();
    confirmreviewmenuitem = new javax.swing.JMenuItem();
    overridereviewmenuitem = new javax.swing.JMenuItem();

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    setTitle("Qyouti Exam/Survey Processor");

    spacerlabel.setText(" ");
    getContentPane().add(spacerlabel, java.awt.BorderLayout.PAGE_START);

    centralpanel.setLayout(new java.awt.CardLayout());

    noexamloadedlabel.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
    noexamloadedlabel.setText("No active exam/survey");
    noexamloadedpanel.add(noexamloadedlabel);

    centralpanel.add(noexamloadedpanel, "card3");

    tabs.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 0, 0, 0));

    qtab.setLayout(new java.awt.BorderLayout());

    jSplitPane3.setDividerLocation(400);

    questiontable.setModel(new javax.swing.table.DefaultTableModel(
      new Object [][]
      {

      },
      new String []
      {
        "ID", "Title"
      }
    )
    {
      Class[] types = new Class []
      {
        java.lang.String.class, java.lang.String.class
      };
      boolean[] canEdit = new boolean []
      {
        false, false
      };

      public Class getColumnClass(int columnIndex)
      {
        return types [columnIndex];
      }

      public boolean isCellEditable(int rowIndex, int columnIndex)
      {
        return canEdit [columnIndex];
      }
    });
    questiontable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
    sp1.setViewportView(questiontable);

    jSplitPane3.setLeftComponent(sp1);

    jPanel8.setLayout(new java.awt.BorderLayout());

    jButton1.setText("Fit Width");
    jButton1.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        jButton1ActionPerformed(evt);
      }
    });
    jPanel10.add(jButton1);

    jPanel8.add(jPanel10, java.awt.BorderLayout.NORTH);

    qprevscrollpane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
    qprevscrollpane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    qprevscrollpane.setViewportView(previewsvgcanvas);

    jPanel8.add(qprevscrollpane, java.awt.BorderLayout.CENTER);

    jTabbedPane1.addTab("Preview", jPanel8);

    jPanel2.setLayout(new java.awt.BorderLayout());

    jLabel4.setText("Results of Statistical Analysis for Selected Question");
    questionanalysistoppane.add(jLabel4);

    jPanel2.add(questionanalysistoppane, java.awt.BorderLayout.NORTH);

    analysistable.setModel(new javax.swing.table.DefaultTableModel(
      new Object [][]
      {
        {null, null, null, null},
        {null, null, null, null},
        {null, null, null, null},
        {null, null, null, null}
      },
      new String []
      {
        "Title 1", "Title 2", "Title 3", "Title 4"
      }
    ));
    jScrollPane4.setViewportView(analysistable);

    jPanel2.add(jScrollPane4, java.awt.BorderLayout.CENTER);

    jScrollPane5.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    jScrollPane5.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    jScrollPane5.setMaximumSize(new java.awt.Dimension(32767, 75));
    jScrollPane5.setPreferredSize(new java.awt.Dimension(2961, 75));

    jTextPane1.setText("Each student's score on the whole exam is assumed to be a good estimate of the student's aptitude.  For each statement students are grouped according to whether they selected the item or not.  A Hodges-Lehmann estimate is calculated which compares the two groups and measures the median difference in aptitude. Confidence limits are also calculated on the estimate - if one is positive and the other negative then the difference in aptitude is not significant.\n\nA signficant positive difference in aptitude is expected for statements that effectively discriminate between students based on aptitude.");
    jTextPane1.setOpaque(false);
    jScrollPane5.setViewportView(jTextPane1);

    jPanel2.add(jScrollPane5, java.awt.BorderLayout.SOUTH);

    jTabbedPane1.addTab("Analysis", jPanel2);

    jSplitPane3.setRightComponent(jTabbedPane1);

    qtab.add(jSplitPane3, java.awt.BorderLayout.CENTER);

    tabs.addTab("Questions", qtab);

    perstab.setLayout(new java.awt.BorderLayout());

    jSplitPane1.setDividerLocation(400);

    jPanel12.setLayout(new java.awt.BorderLayout());

    allanonbutton.setText("All Anonymous");
    allanonbutton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        allanonbuttonActionPerformed(evt);
      }
    });
    jPanel13.add(allanonbutton);

    noneanonbutton.setText("None Anonymous");
    noneanonbutton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        noneanonbuttonActionPerformed(evt);
      }
    });
    jPanel13.add(noneanonbutton);

    jPanel12.add(jPanel13, java.awt.BorderLayout.NORTH);

    persontable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
    jScrollPane3.setViewportView(persontable);

    jPanel12.add(jScrollPane3, java.awt.BorderLayout.CENTER);

    jSplitPane1.setLeftComponent(jPanel12);

    personpanel.setLayout(new java.awt.CardLayout());

    nopersonselectedpanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

    jLabel1.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
    jLabel1.setText("Select a Person to Set Customisation Options");
    nopersonselectedpanel.add(jLabel1);

    personpanel.add(nopersonselectedpanel, "noselection");

    noeditpersonpanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

    jLabel3.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
    jLabel3.setText("Persons with 'Excluded' or 'Anonymous' set cannot have customisations");
    noeditpersonpanel.add(jLabel3);

    personpanel.add(noeditpersonpanel, "noedit");

    selectedpersonpanel.setLayout(new java.awt.GridBagLayout());

    jLabel5.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
    jLabel5.setText("Customise Printing for:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    selectedpersonpanel.add(jLabel5, gridBagConstraints);

    customforlabel.setFont(new java.awt.Font("Dialog", 0, 14)); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    selectedpersonpanel.add(customforlabel, gridBagConstraints);

    resetpreferencesbutton.setText("Reset Customisation");
    resetpreferencesbutton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        resetpreferencesbuttonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    selectedpersonpanel.add(resetpreferencesbutton, gridBagConstraints);

    bigpinkcheckbox.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
    bigpinkcheckbox.setText("Enlarge Pink Box");
    bigpinkcheckbox.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        bigpinkcheckboxActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    selectedpersonpanel.add(bigpinkcheckbox, gridBagConstraints);

    serifcheckbox.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
    serifcheckbox.setText("Use Serif Font");
    serifcheckbox.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        serifcheckboxActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    selectedpersonpanel.add(serifcheckbox, gridBagConstraints);

    onecolumncheckbox.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
    onecolumncheckbox.setText("One Column Only");
    onecolumncheckbox.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        onecolumncheckboxActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    selectedpersonpanel.add(onecolumncheckbox, gridBagConstraints);

    fontdizelabel.setText("Font Size:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    selectedpersonpanel.add(fontdizelabel, gridBagConstraints);

    fontsizelist.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
    fontsizelist.setModel(new javax.swing.AbstractListModel<String>()
    {
      String[] strings = { "Not Custom", "125%", "150%", "200%" };
      public int getSize() { return strings.length; }
      public String getElementAt(int i) { return strings[i]; }
    });
    fontsizelist.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    fontsizelist.setVisibleRowCount(4);
    fontsizelist.addListSelectionListener(new javax.swing.event.ListSelectionListener()
    {
      public void valueChanged(javax.swing.event.ListSelectionEvent evt)
      {
        fontsizelistValueChanged(evt);
      }
    });
    jScrollPane7.setViewportView(fontsizelist);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    selectedpersonpanel.add(jScrollPane7, gridBagConstraints);

    jLabel6.setText("Background Colour:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    selectedpersonpanel.add(jLabel6, gridBagConstraints);

    colourswatchlabel.setBackground(new java.awt.Color(255, 153, 51));
    colourswatchlabel.setFont(new java.awt.Font("Dialog", 0, 18)); // NOI18N
    colourswatchlabel.setText("Colour Swatch");
    colourswatchlabel.setBorder(javax.swing.BorderFactory.createCompoundBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 10, true), javax.swing.BorderFactory.createEmptyBorder(15, 15, 15, 15)));
    colourswatchlabel.setOpaque(true);
    jPanel11.add(colourswatchlabel);

    changecolourbutton.setText("Change");
    changecolourbutton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        changecolourbuttonActionPerformed(evt);
      }
    });
    jPanel11.add(changecolourbutton);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    selectedpersonpanel.add(jPanel11, gridBagConstraints);

    personpanel.add(selectedpersonpanel, "selection");

    jScrollPane6.setViewportView(personpanel);

    jSplitPane1.setRightComponent(jScrollPane6);

    perstab.add(jSplitPane1, java.awt.BorderLayout.CENTER);

    tabs.addTab("Persons", perstab);

    ctab.setLayout(new java.awt.BorderLayout());

    candidatetable.setModel(new javax.swing.table.DefaultTableModel(
      new Object [][]
      {

      },
      new String []
      {
        "*", "Name", "ID", "Pages", "Questions", "Errors"
      }
    )
    {
      Class[] types = new Class []
      {
        java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class
      };
      boolean[] canEdit = new boolean []
      {
        false, false, false, false, false, false
      };

      public Class getColumnClass(int columnIndex)
      {
        return types [columnIndex];
      }

      public boolean isCellEditable(int rowIndex, int columnIndex)
      {
        return canEdit [columnIndex];
      }
    });
    candidatetable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
    sp2.setViewportView(candidatetable);

    ctab.add(sp2, java.awt.BorderLayout.CENTER);

    tabs.addTab("Papers", ctab);

    ptab.setLayout(new java.awt.BorderLayout());

    pagestable.setModel(new javax.swing.table.DefaultTableModel(
      new Object [][]
      {

      },
      new String []
      {
        "No.", "File", "Code", "Error"
      }
    )
    {
      Class[] types = new Class []
      {
        java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
      };
      boolean[] canEdit = new boolean []
      {
        false, false, false, false
      };

      public Class getColumnClass(int columnIndex)
      {
        return types [columnIndex];
      }

      public boolean isCellEditable(int rowIndex, int columnIndex)
      {
        return canEdit [columnIndex];
      }
    });
    pagestable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
    sp3.setViewportView(pagestable);

    ptab.add(sp3, java.awt.BorderLayout.CENTER);

    tabs.addTab("Pages", ptab);

    stab.setLayout(new java.awt.BorderLayout());

    scanfiletable.setModel(new javax.swing.table.DefaultTableModel(
      new Object [][]
      {

      },
      new String []
      {
        "Ident", "Source", "Imported Name", "Processed", "Errors"
      }
    )
    {
      Class[] types = new Class []
      {
        java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Boolean.class, java.lang.String.class
      };
      boolean[] canEdit = new boolean []
      {
        false, false, false, false, false
      };

      public Class getColumnClass(int columnIndex)
      {
        return types [columnIndex];
      }

      public boolean isCellEditable(int rowIndex, int columnIndex)
      {
        return canEdit [columnIndex];
      }
    });
    scanfiletable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
    sp4.setViewportView(scanfiletable);

    stab.add(sp4, java.awt.BorderLayout.CENTER);

    tabs.addTab("Scans", stab);

    rtab.setLayout(new java.awt.BorderLayout());

    jSplitPane2.setResizeWeight(0.5);

    jPanel4.setLayout(new java.awt.BorderLayout());

    jPanel5.setLayout(new java.awt.BorderLayout());

    jPanel7.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 10, 1, 1));
    jPanel7.setLayout(new java.awt.GridLayout(0, 2));

    jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Review Type"));
    jPanel1.setLayout(new java.awt.GridLayout(0, 1));

    reviewtypebuttongroup.add(reviewtype1button);
    reviewtype1button.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
    reviewtype1button.setSelected(true);
    reviewtype1button.setText("Review Recommended");
    reviewtype1button.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        reviewtype1buttonActionPerformed(evt);
      }
    });
    jPanel1.add(reviewtype1button);

    reviewtypebuttongroup.add(reviewtype2button);
    reviewtype2button.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
    reviewtype2button.setText("Question, candidate by candidate");
    reviewtype2button.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        reviewtype2buttonActionPerformed(evt);
      }
    });
    jPanel1.add(reviewtype2button);

    reviewtypebuttongroup.add(reviewtype3button);
    reviewtype3button.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
    reviewtype3button.setText("Candidate, question by question");
    reviewtype3button.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        reviewtype3buttonActionPerformed(evt);
      }
    });
    jPanel1.add(reviewtype3button);

    jPanel7.add(jPanel1);

    jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Filter"));
    jPanel3.setLayout(new java.awt.GridLayout(0, 1));

    reviewincludebuttongroup.add(reviewincludeall);
    reviewincludeall.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
    reviewincludeall.setSelected(true);
    reviewincludeall.setText("All review statuses");
    reviewincludeall.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        reviewincludeallActionPerformed(evt);
      }
    });
    jPanel3.add(reviewincludeall);

    reviewincludebuttongroup.add(reviewincludenotreviewed);
    reviewincludenotreviewed.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
    reviewincludenotreviewed.setText("Not reviewed only");
    reviewincludenotreviewed.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        reviewincludenotreviewedActionPerformed(evt);
      }
    });
    jPanel3.add(reviewincludenotreviewed);

    reviewincludebuttongroup.add(reviewincludeconfirmed);
    reviewincludeconfirmed.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
    reviewincludeconfirmed.setText("Confirmed only");
    reviewincludeconfirmed.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        reviewincludeconfirmedActionPerformed(evt);
      }
    });
    jPanel3.add(reviewincludeconfirmed);

    reviewincludebuttongroup.add(reviewincludeoverridden);
    reviewincludeoverridden.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
    reviewincludeoverridden.setText("Overridden only");
    reviewincludeoverridden.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        reviewincludeoverriddenActionPerformed(evt);
      }
    });
    jPanel3.add(reviewincludeoverridden);

    jPanel7.add(jPanel3);

    jPanel5.add(jPanel7, java.awt.BorderLayout.NORTH);

    jPanel6.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

    previousreviewbutton.setMnemonic('p');
    previousreviewbutton.setText("Previous");
    previousreviewbutton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        previousreviewbuttonActionPerformed(evt);
      }
    });
    jPanel6.add(previousreviewbutton);

    nextreviewbutton.setMnemonic('n');
    nextreviewbutton.setText("Next");
    nextreviewbutton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        nextreviewbuttonActionPerformed(evt);
      }
    });
    jPanel6.add(nextreviewbutton);

    jPanel5.add(jPanel6, java.awt.BorderLayout.SOUTH);

    jPanel4.add(jPanel5, java.awt.BorderLayout.NORTH);

    qrevlefttable.setModel(new javax.swing.table.DefaultTableModel(
      new Object [][]
      {

      },
      new String []
      {

      }
    ));
    jScrollPane1.setViewportView(qrevlefttable);

    jSplitPane4.setLeftComponent(jScrollPane1);

    jPanel9.setLayout(new java.awt.GridBagLayout());

    questionreviewtable.setModel(new javax.swing.table.DefaultTableModel(
      new Object [][]
      {

      },
      new String []
      {
        "Candidate ID", "Question ID", "Review Status"
      }
    ));
    questionreviewtable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_LAST_COLUMN);
    sp8.setViewportView(questionreviewtable);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 0.6;
    gridBagConstraints.weighty = 0.5;
    jPanel9.add(sp8, gridBagConstraints);

    jSplitPane4.setRightComponent(jPanel9);

    jPanel4.add(jSplitPane4, java.awt.BorderLayout.CENTER);

    jSplitPane2.setLeftComponent(jPanel4);

    qrpanelouter.setBackground(java.awt.Color.white);
    qrpanelouter.setLayout(new java.awt.GridBagLayout());

    questionreviewpanel.setBackground(java.awt.Color.white);
    questionreviewpanel.setLayout(new java.awt.GridBagLayout());

    jLabel2.setText("No questions selected");
    questionreviewpanel.add(jLabel2, new java.awt.GridBagConstraints());

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.weighty = 0.5;
    qrpanelouter.add(questionreviewpanel, gridBagConstraints);

    jScrollPane2.setViewportView(qrpanelouter);

    jSplitPane2.setRightComponent(jScrollPane2);

    rtab.add(jSplitPane2, java.awt.BorderLayout.CENTER);

    tabs.addTab("Review", rtab);

    centralpanel.add(tabs, "card2");

    getContentPane().add(centralpanel, java.awt.BorderLayout.CENTER);

    statuspanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
    statuspanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

    savestatuslabel.setText(" ");
    savestatuslabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
    statuspanel.add(savestatuslabel);

    printstatuslabel.setText(" ");
    printstatuslabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
    statuspanel.add(printstatuslabel);

    errorlabel.setText(" ");
    errorlabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
    statuspanel.add(errorlabel);
    statuspanel.add(progressbar);

    getContentPane().add(statuspanel, java.awt.BorderLayout.SOUTH);

    filemenu.setText("File");

    newmenuitem.setText("New Exam/Survey...");
    newmenuitem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        newmenuitemActionPerformed(evt);
      }
    });
    filemenu.add(newmenuitem);

    openmenuitem.setText("Open Exam/Survey...");
    openmenuitem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        openmenuitemActionPerformed(evt);
      }
    });
    filemenu.add(openmenuitem);

    savemenuitem.setText("Save Exam Data");
    savemenuitem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        savemenuitemActionPerformed(evt);
      }
    });
    filemenu.add(savemenuitem);

    propsmenuitem.setText("Exam/Survey Properties...");
    propsmenuitem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        propsmenuitemActionPerformed(evt);
      }
    });
    filemenu.add(propsmenuitem);
    filemenu.add(sep1);

    configmenuitem.setText("Configure...");
    configmenuitem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        configmenuitemActionPerformed(evt);
      }
    });
    filemenu.add(configmenuitem);

    aboutmenuitem.setText("About...");
    aboutmenuitem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        aboutmenuitemActionPerformed(evt);
      }
    });
    filemenu.add(aboutmenuitem);
    filemenu.add(sep1b);

    exitmenuitem.setText("Exit");
    exitmenuitem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        exitmenuitemActionPerformed(evt);
      }
    });
    filemenu.add(exitmenuitem);

    menubar.add(filemenu);

    actionmenu.setText("Action");

    pdfprintmenuitem.setText("Print to PDF...");
    pdfprintmenuitem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        pdfprintmenuitemActionPerformed(evt);
      }
    });
    actionmenu.add(pdfprintmenuitem);

    forgetprintmenuitem.setText("Forget Last Printout");
    forgetprintmenuitem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        forgetprintmenuitemActionPerformed(evt);
      }
    });
    actionmenu.add(forgetprintmenuitem);
    actionmenu.add(jSeparator1);

    importqmenuitem.setText("Import Questions...");
    importqmenuitem.setEnabled(false);
    importqmenuitem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        importqmenuitemActionPerformed(evt);
      }
    });
    actionmenu.add(importqmenuitem);

    editquestionmenuitem.setText("Edit Question");
    editquestionmenuitem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        editquestionmenuitemActionPerformed(evt);
      }
    });
    actionmenu.add(editquestionmenuitem);

    editallquestionsmenuitem.setText("Edit All Questions");
    editallquestionsmenuitem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        editallquestionsmenuitemActionPerformed(evt);
      }
    });
    actionmenu.add(editallquestionsmenuitem);

    itemanalysismenuitem.setText("Item Analysis");
    itemanalysismenuitem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        itemanalysismenuitemActionPerformed(evt);
      }
    });
    actionmenu.add(itemanalysismenuitem);

    printitemanalysismenuitem.setText("Print Item Analysis");
    printitemanalysismenuitem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        printitemanalysismenuitemActionPerformed(evt);
      }
    });
    actionmenu.add(printitemanalysismenuitem);
    actionmenu.add(sep2);

    importcanmenuitem.setText("Import Candidates...");
    importcanmenuitem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        importcanmenuitemActionPerformed(evt);
      }
    });
    actionmenu.add(importcanmenuitem);
    actionmenu.add(jSeparator2);

    importimagesmenuitem.setText("Import Scanned Images...");
    importimagesmenuitem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        importimagesmenuitemActionPerformed(evt);
      }
    });
    actionmenu.add(importimagesmenuitem);

    clearscanneddatamenuitem.setText("Clear Scanned Data");
    clearscanneddatamenuitem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        clearscanneddatamenuitemActionPerformed(evt);
      }
    });
    actionmenu.add(clearscanneddatamenuitem);

    viewscanmenuitem.setText("View Scan");
    viewscanmenuitem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        viewscanmenuitemActionPerformed(evt);
      }
    });
    actionmenu.add(viewscanmenuitem);

    recomputemenuitem.setText("Recompute Outcomes");
    recomputemenuitem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        recomputemenuitemActionPerformed(evt);
      }
    });
    actionmenu.add(recomputemenuitem);
    actionmenu.add(sep3);

    expscoresmenuitem.setText("Export Outcomes...");
    expscoresmenuitem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        expscoresmenuitemActionPerformed(evt);
      }
    });
    actionmenu.add(expscoresmenuitem);

    exprepliesmenuitem.setText("Export Responses...");
    exprepliesmenuitem.setEnabled(false);
    actionmenu.add(exprepliesmenuitem);

    menubar.add(actionmenu);

    reviewmenu.setText("Review");

    previousreviewmenuitem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_MASK));
    previousreviewmenuitem.setText("Previous");
    previousreviewmenuitem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        previousreviewmenuitemActionPerformed(evt);
      }
    });
    reviewmenu.add(previousreviewmenuitem);

    nextreviewmenuitem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
    nextreviewmenuitem.setText("Next");
    nextreviewmenuitem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        nextreviewmenuitemActionPerformed(evt);
      }
    });
    reviewmenu.add(nextreviewmenuitem);

    confirmreviewmenuitem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));
    confirmreviewmenuitem.setText("Confirm");
    confirmreviewmenuitem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        confirmreviewmenuitemActionPerformed(evt);
      }
    });
    reviewmenu.add(confirmreviewmenuitem);

    overridereviewmenuitem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
    overridereviewmenuitem.setText("Override");
    overridereviewmenuitem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        overridereviewmenuitemActionPerformed(evt);
      }
    });
    reviewmenu.add(overridereviewmenuitem);

    menubar.add(reviewmenu);

    setJMenuBar(menubar);

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void importqmenuitemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_importqmenuitemActionPerformed
  {//GEN-HEADEREND:event_importqmenuitemActionPerformed

    if ( exam == null )
    {
      JOptionPane.
              showMessageDialog( this, "No exam/survey open." );
      return;
    }
    // TODO add your handling code here:
  }//GEN-LAST:event_importqmenuitemActionPerformed

  private void itemanalysismenuitemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_itemanalysismenuitemActionPerformed
  {//GEN-HEADEREND:event_itemanalysismenuitemActionPerformed

    if ( exam == null )
    {
      JOptionPane.showMessageDialog( this, "No exam/survey open." );
      return;
    }
    
    if ( exam.qdefs.getColumnCount() == 0 )
    {
      JOptionPane.showMessageDialog( this, "No question to analyse." );
      return;
    }
    
    int i, n=0;
    CandidateData cd;
    for ( i=0; i<exam.candidates_sorted.size(); i++ )
    {
      cd = exam.candidates_sorted.get( i );
      if ( cd.questionsAsked() == cd.questionsScanned() )
        n++;
    }
    
    if ( n<3 )
    {
      JOptionPane.showMessageDialog( this, "There are not enough candidates with all questions marked." );
      return;
    }
    
    exam.itemAnalysis();    
  }//GEN-LAST:event_itemanalysismenuitemActionPerformed

  private void expscoresmenuitemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_expscoresmenuitemActionPerformed
  {//GEN-HEADEREND:event_expscoresmenuitemActionPerformed

    if ( exam == null )
    {
      JOptionPane.
              showMessageDialog( this, "No exam/survey open." );
      return;
    }
    
    try
    {
      final JFileChooser fc = new JFileChooser();
      fc.setDialogTitle( "Select folder and file name to save." );
      fc.setFileSelectionMode( JFileChooser.FILES_ONLY );
      fc.setMultiSelectionEnabled( false );
      fc.setFileFilter(
              new javax.swing.filechooser.FileFilter()
              {
                @Override
                public boolean accept( File pathname )
                {
                  if ( pathname.isDirectory() ) return true;
                  String n = pathname.getName().toLowerCase();
                  if ( n.endsWith( ".csv" ) )  return true;
                  if ( n.endsWith( ".txt" ) )  return true;
                  return false;
                }

                @Override
                public String getDescription()
                {
                  return "All supported image formats.";
                }
              }
      );
      int returnVal = fc.showSaveDialog( this );

      if ( returnVal != JFileChooser.APPROVE_OPTION )
      {
        return;
      }
      
      File file = fc.getSelectedFile();
      FileWriter writer = new FileWriter( file );
      CSVWriter csvwriter = new CSVWriter( writer, ',', '"', "\r\n" );
      String[] line = new String[exam.getColumnCount()];
      int i, j;
      Object value;
      for ( i=0; i<line.length; i++ )
        line[i] = exam.getColumnName( i );
      csvwriter.writeNext( line );
      for ( j=0; j<exam.getRowCount(); j++ )
      {
        for ( i=0; i<line.length; i++ )
        {
          value = exam.getValueAt( j, i );
          line[i] = (value==null)?"":value.toString();
        }
        csvwriter.writeNext( line );
      }
      csvwriter.flush();
      writer.close();
    }
    catch ( Exception ex )
    {
      ex.printStackTrace();
      JOptionPane.
              showMessageDialog( this, "Problem trying to save data." );
    }
  }//GEN-LAST:event_expscoresmenuitemActionPerformed

  private void openmenuitemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_openmenuitemActionPerformed
  {//GEN-HEADEREND:event_openmenuitemActionPerformed

    if ( !confirmDataLoss( "Are you sure you want to open a different exam/survey?" ) )
    {
      return;
    }

    selectdialog.setExamName( "" );
    selectdialog.setDialogType( ExamSelectDialog.TYPE_OPEN );
    selectdialog.setVisible( true );
  }//GEN-LAST:event_openmenuitemActionPerformed

  private void importcanmenuitemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_importcanmenuitemActionPerformed
  {//GEN-HEADEREND:event_importcanmenuitemActionPerformed
    boolean forceanon = false;
    
    if ( exam == null )
    {
      JOptionPane.
              showMessageDialog( this, "No exam/survey open." );
      return;
    }

    String lastprintid = exam.getLastPrintID();    
    if ( lastprintid != null && lastprintid.length() != 0 )
    {
      JOptionPane.showMessageDialog( this, "The exam/survey has been printed so persons you import now will all be marked as 'anonymous'." );
      forceanon = true;
    }
          
    ImportPersonDialog dialog = new ImportPersonDialog( this, true );
    dialog.setExam( exam );
    dialog.setVisible( true );
    ArrayList<PersonData> list = dialog.getPersonList();
    if ( list == null || list.size() == 0 )
      return;
    System.out.println( "Importing " + list.size() + " persons." );
    exam.importPersons( list, forceanon );
    exam.setUnsavedChangesInMain( true );
  }//GEN-LAST:event_importcanmenuitemActionPerformed

  private void newmenuitemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_newmenuitemActionPerformed
  {//GEN-HEADEREND:event_newmenuitemActionPerformed
    if ( !confirmDataLoss( "Are you sure you want to create a new exam/survey?" ) )
    {
      return;
    }

    selectdialog.setExamName( "" );

    //selectdialog.setBaseFolder( );
    selectdialog.setDialogType( ExamSelectDialog.TYPE_NEW );
    selectdialog.setVisible( true );

  }//GEN-LAST:event_newmenuitemActionPerformed

  
    public void setPreviewItem( QTIElementItem item, int qnumber )
    {
      URI uri;
      
      previewsvgcanvas.setSVGDocument(null);
      if ( item == null ) return;
      
      try
      {
        uri = exam.getExamFolder().getCanonicalFile().toURI();
      }
      catch ( IOException ioe )
      {
        return;
      }
      
          
      // TO DO - create preview in background thread
      QTIItemRenderer renderer = new QTIItemRenderer(fontmanager,PrintThread.TYPE_PAPERS,uri,exam);
      renderer.setItem(item, qnumber, null);
      
      if ( renderer == null )
          return;
      renderer.renderItem();

      GenericDocument svg = (GenericDocument) renderer.getPreviewSVGDocument( exam );
      previewsvgcanvas.setDocument( svg );
      qprevscrollpane.getViewport().setViewSize( new Dimension(1000,2000) );
      // how to set zoom factor so page fits to width?
    }
  
  private void editquestionmenuitemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_editquestionmenuitemActionPerformed
  {//GEN-HEADEREND:event_editquestionmenuitemActionPerformed

    if ( exam == null )
    {
      JOptionPane.
              showMessageDialog( this, "No exam/survey open." );
      return;
    }

    QTIElementItem item = null;
    if ( tabs.getSelectedComponent() == qtab )
    {
      int row = questiontable.getSelectedRow();
      if ( row < 0 )
      {
        JOptionPane.
                showMessageDialog( this, "Select a question in the Questions tab to edit." );
        return;
      }
      item = exam.qdefs.qti.getItems().elementAt( row );
    }
    else
    {
      JOptionPane.
              showMessageDialog( this, "Question edit is only available on the Question tab." );
      return;
    }

    editquestionident = item.getIdent();
    ItemTemplate template = item.getTemplate();
    if ( template == null )
    {
      JOptionPane.
              showMessageDialog( this, "The selected question is not a template question and cannot be edited." );
      return;
    }

    String lastprintid = exam.getLastPrintID();
    template.setPresentationeditenabled( lastprintid == null || lastprintid.
            length() == 0 );
    template.setProcessingeditenabled( true );

    QuestionEditDialog dialog = new QuestionEditDialog( this, true );
    dialog.setTemplate( template );
    dialog.setVisible( true );
  }//GEN-LAST:event_editquestionmenuitemActionPerformed

  private void viewscanmenuitemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_viewscanmenuitemActionPerformed
  {//GEN-HEADEREND:event_viewscanmenuitemActionPerformed

    if ( exam == null )
    {
      JOptionPane.
              showMessageDialog( this, "No exam/survey open." );
      return;
    }

    String filename = null;
    if ( tabs.getSelectedComponent() == stab )
    {
      int row = scanfiletable.getSelectedRow();
      if ( row < 0 )
      {
        JOptionPane.
                showMessageDialog( this, "Select a scan in the Scans tab to edit." );
        return;
      }
      filename = (String) scanfiletable.getValueAt( row, 2 );
      if ( !filename.endsWith(".jpg") )
      {
        JOptionPane.
                showMessageDialog( this, "Select an image in the Scans tab to edit." );
        return;
      }
    }
    else
    {
      JOptionPane.
              showMessageDialog( this, "Scan view is only available on the Scans tab." );
      return;
    }

    ImageViewDialog dialog = new ImageViewDialog( this, true );
    dialog.setImage( exam.getScanImageArchive(), filename );
    dialog.setVisible( true );
  }//GEN-LAST:event_viewscanmenuitemActionPerformed

  private void exitmenuitemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_exitmenuitemActionPerformed
  {//GEN-HEADEREND:event_exitmenuitemActionPerformed
    confirmExit();
  }//GEN-LAST:event_exitmenuitemActionPerformed

  private void savemenuitemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_savemenuitemActionPerformed
  {//GEN-HEADEREND:event_savemenuitemActionPerformed

    if ( exam == null )
    {
      JOptionPane.
              showMessageDialog( this, "No exam/survey open." );
      return;
    }
    
    if ( exam.save() )
      return;
    JOptionPane.showMessageDialog( this, "Technical error attempting to save exam/survey." );
    
  }//GEN-LAST:event_savemenuitemActionPerformed

  private void editallquestionsmenuitemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_editallquestionsmenuitemActionPerformed
  {//GEN-HEADEREND:event_editallquestionsmenuitemActionPerformed

    if ( exam == null )
    {
      JOptionPane.
              showMessageDialog( this, "No exam/survey open." );
      return;
    }

    QTIElementItem item = null;

    int n = exam.qdefs.getRowCount();
    if ( n < 1 )
    {
      JOptionPane.showMessageDialog( this, "No questions to edit." );
      return;
    }

    editquestionident = null;  // indicates all questions are being edited
    QuestionAllEditDialog dialog = new QuestionAllEditDialog( this, true );
    for ( int row = 0; row < n; row++ )
    {
      item = exam.qdefs.qti.getItems().elementAt( row );

      ItemTemplate template = item.getTemplate();
      if ( template == null )
      {
        continue;
        //JOptionPane.showMessageDialog( this, "The selected question is not a template question and cannot be edited." );
        //return;      
      }

      String lastprintid = exam.getLastPrintID();
      template.setPresentationeditenabled( lastprintid == null || lastprintid.
              length() == 0 );
      template.setProcessingeditenabled( true );

      dialog.addTemplate( template );
    }

    dialog.setVisible( true );
  }//GEN-LAST:event_editallquestionsmenuitemActionPerformed

  private void clearscanneddatamenuitemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_clearscanneddatamenuitemActionPerformed
  {//GEN-HEADEREND:event_clearscanneddatamenuitemActionPerformed

    if ( exam == null )
    {
      JOptionPane.
              showMessageDialog( this, "No exam/survey open." );
      return;
    }

    if ( JOptionPane.showConfirmDialog(
            this,
            "<html><p>Clearing Responses removes all response data, images of pages and candidates' outcomes.</p>"
            + "<p>Press ahead anyway?</p></html>", "Confirm",
            JOptionPane.YES_NO_OPTION ) != JOptionPane.YES_OPTION )
    {
      return;
    }

    exam.clearScans();
    exam.save();
    exam.processDataChanged( exam.pagelistmodel );
  }//GEN-LAST:event_clearscanneddatamenuitemActionPerformed

  private void forgetprintmenuitemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_forgetprintmenuitemActionPerformed
  {//GEN-HEADEREND:event_forgetprintmenuitemActionPerformed

    if ( exam == null )
    {
      JOptionPane.
              showMessageDialog( this, "No exam/survey open." );
      return;
    }

    String lpid = exam.getLastPrintID();
    if ( lpid == null || lpid.length() == 0 )
    {
      JOptionPane.
              showMessageDialog( this, "There is no recorded print run to forget." );
      return;
    }

    if ( exam.scans.size() > 0 )
    {
      JOptionPane.
              showMessageDialog( this, "Before you can forget a print run you must clear all scan data." );
      return;
    }

    if ( JOptionPane.showConfirmDialog(
            this,
            "<html><p>Forgetting a print run means that you will not be able to process scans of the printed pages.\nAre you sure you want to proceed?", "Confirm",
            JOptionPane.YES_NO_OPTION ) != JOptionPane.YES_OPTION )
    {
      return;
    }
    exam.forgetPrint();
    exam.save();
  }//GEN-LAST:event_forgetprintmenuitemActionPerformed

  private void pdfprintmenuitemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_pdfprintmenuitemActionPerformed
  {//GEN-HEADEREND:event_pdfprintmenuitemActionPerformed
    if ( exam == null )
    {
      JOptionPane.
              showMessageDialog( this, "No exam/survey open." );
      return;
    }

    String lpid = exam.getLastPrintID();
    if ( lpid != null && lpid.length() > 0 )
    {
      JOptionPane.
              showMessageDialog( this, "The exam/survey has already been printed. \nYou need to clear scan data and 'forget' the previous print run before you can reprint." );
      return;
    }

    if ( exam.qdefs == null || exam.qdefs.getRowCount() == 0 )
    {
      JOptionPane.
              showMessageDialog( this, "You can't print papers - there are no questions in the exam." );
      return;
    }
    
    PersonData person;
    int namedcount=0;
    int anoncount=0;
    for ( int i=0; i < exam.persons_sorted.size(); i++ )
    {
      person = exam.persons_sorted.get( i );
      if ( person.isExcluded() ) continue;
      if ( person.isAnonymous() ) anoncount++;
      else namedcount++;
    }
    PrintDialog pd = new PrintDialog( this, true, namedcount, anoncount );
    pd.setTitle( "Print Papers to PDF File" );
    pd.setVisible( true );
    if ( !pd.isConfirmed() )
      return;
    
    System.out.println( "Print confirmed with total anon papers = " + pd.getAnonPlusExtra() );
    exam.createPapers( pd.getAnonPlusExtra() );
    
    if ( false )
      return;
    
    if ( exam.candidates_sorted.isEmpty() )
    {
      JOptionPane.
              showMessageDialog( this, "You can't print papers - there are no candidates in the exam." );
      return;
    }

    tabs.setSelectedIndex( 0 );
    tabs.setEnabled( false );
    filemenu.setEnabled( false );
    actionmenu.setEnabled( false );
    exam.setUnsavedChangesInMain( true );
    printstatuslabel.setText( "Printing..." );
    progressbar.setIndeterminate( true );

    //busydialog.setVisible( true );
    PrintThread thread = new PrintThread( exam, examfolder, fontmanager );
    thread.setQyoutiFrame( this );
    thread.start();
  }//GEN-LAST:event_pdfprintmenuitemActionPerformed

  public void pdfPrintComplete( boolean error, MissingGlyphReport mgr )
  {
    tabs.setEnabled( true );
    filemenu.setEnabled( true );
    actionmenu.setEnabled( true );
    progressbar.setIndeterminate( false );
    //busydialog.setVisible( false );
    if ( error )
    {
      JOptionPane.showMessageDialog( this, "There was a technical error while attempting to print to PDF." );
      printstatuslabel.setText( "printing error" );
    }
    if ( mgr != null && !mgr.isEmpty() )
    {
      JOptionPane.showMessageDialog( this, 
        "There were characters in the print out which are not available in the selected fonts.\n" +
                mgr.toString() );
      printstatuslabel.setText( "printing error" );
    }    
  }

  private void propsmenuitemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_propsmenuitemActionPerformed
  {//GEN-HEADEREND:event_propsmenuitemActionPerformed

    if ( exam == null )
    {
      JOptionPane.
              showMessageDialog( this, "No exam/survey open." );
      return;
    }

    ExamPropertiesDialog dialog = new ExamPropertiesDialog( this, true );
    dialog.setExaminationData( exam );
    dialog.setVisible( true );
  }//GEN-LAST:event_propsmenuitemActionPerformed

  private void importimagesmenuitemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_importimagesmenuitemActionPerformed
  {//GEN-HEADEREND:event_importimagesmenuitemActionPerformed

    if ( exam == null )
    {
      JOptionPane.
              showMessageDialog( this, "You need to open an examination before importing scanned pages." );
      return;
    }

    String lpid = exam.getLastPrintID();
    if ( lpid == null || lpid.length() == 0 )
    {
      JOptionPane.
              showMessageDialog( this, "There is no print record for the exam/survey so scanning is not possible." );
      return;
    }

    try
    {
      final JFileChooser fc = new JFileChooser();
      fc.setDialogTitle( "Select images files to scan." );
      fc.setFileSelectionMode( JFileChooser.FILES_ONLY );
      fc.setMultiSelectionEnabled( true );
      fc.setFileFilter(
              new javax.swing.filechooser.FileFilter()
              {
                @Override
                public boolean accept( File pathname )
                {
                  if ( pathname.isDirectory() ) return true;
                  String n = pathname.getName().toLowerCase();
                  if ( n.endsWith( ".png" ) )  return true;
                  if ( n.endsWith( ".jpg" ) )  return true;
                  if ( n.endsWith( ".jpeg" ) ) return true;
                  if ( n.endsWith( ".pdf" ) )  return true;
                  return false;
                }

                @Override
                public String getDescription()
                {
                  return "All supported image formats.";
                }
              }
      );
      if ( scanfolder != null )
      {
        fc.setCurrentDirectory( new File( scanfolder ) );
      }
      int returnVal = fc.showOpenDialog( this );

      if ( returnVal != JFileChooser.APPROVE_OPTION )
      {
        return;
      }
      
      File[] files = fc.getSelectedFiles();

      tabs.setSelectedIndex( 2 );
      //tabs.setEnabled( false );
      filemenu.setEnabled( false );
      actionmenu.setEnabled( false );
      exam.setUnsavedChangesInMain( true );
      progressbar.setIndeterminate( true );
      ScanTask scantask = new ScanTask( preferences, exam, files, false, false );
      scantask.setScanTaskListener( this );
      scantask.start();
    }
    catch ( Exception ex )
    {
      exam.candidates.clear();
      exam.candidates_sorted.clear();
      JOptionPane.
              showMessageDialog( this, "Technical error importing scanned images list." );
    }


  }//GEN-LAST:event_importimagesmenuitemActionPerformed

  private void aboutmenuitemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_aboutmenuitemActionPerformed
  {//GEN-HEADEREND:event_aboutmenuitemActionPerformed
    
    AboutDialog dialog = new AboutDialog( this, true );
    dialog.setVisible( true );
    
  }//GEN-LAST:event_aboutmenuitemActionPerformed

  private void previousreviewbuttonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_previousreviewbuttonActionPerformed
  {//GEN-HEADEREND:event_previousreviewbuttonActionPerformed
    if ( questionreviewtable.getSelectedRowCount() != 1 )
    {
      questionreviewtable.clearSelection();
      questionreviewtable.addRowSelectionInterval( 0, 0 );
      return;
    }

    for ( int i=1; i<( questionreviewtable.getRowCount() - 1 ); i++ )
    {
      if ( questionreviewtable.isRowSelected( i ) )
      {
        questionreviewtable.clearSelection();
        questionreviewtable.addRowSelectionInterval( i-1, i-1 );
        return;
      }
    }
  }//GEN-LAST:event_previousreviewbuttonActionPerformed

  private void nextreviewbuttonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_nextreviewbuttonActionPerformed
  {//GEN-HEADEREND:event_nextreviewbuttonActionPerformed

    
    if ( questionreviewtable.getSelectedRowCount() != 1 )
    {
      questionreviewtable.clearSelection();
      questionreviewtable.addRowSelectionInterval( 0, 0 );
      return;
    }

    for ( int i=0; i<( questionreviewtable.getRowCount() - 1 ); i++ )
    {
      if ( questionreviewtable.isRowSelected( i ) )
      {
        questionreviewtable.clearSelection();
        questionreviewtable.addRowSelectionInterval( i+1, i+1 );
        return;
      }
    }    
  }//GEN-LAST:event_nextreviewbuttonActionPerformed

  private void updateLeftList()
  {
    if ( exam.getReviewType() == ExaminationData.REVIEW_BY_QUESTION )  // All questions
    {
      qrevlefttable.setModel( exam.qdefs );
    }
    else if ( exam.getReviewType() == ExaminationData.REVIEW_BY_CANDIDATE )  // All Candidates
    {
      qrevlefttable.setModel( exam );
    }
    else
    {
      qrevlefttable.setModel( emptytablemodel );
    }
  }
  
  private void reviewTypeChanged( int type )
  {
    if ( exam.getReviewType() == type )
      return;
    exam.setReviewType( type );
    updateLeftList();
  }

  private void reviewFilterChanged( int filter )
  {
//    if ( exam.getReviewFilter() == filter )
//      return;
    // update if the user clicks on the same radio button
    // because this will allow them to thin out the list as
    // they change the review statuses on entries.
    exam.setReviewFilter( filter );
    updateLeftList();
  }
  
  
  private void reviewtype1buttonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_reviewtype1buttonActionPerformed
  {//GEN-HEADEREND:event_reviewtype1buttonActionPerformed
    reviewTypeChanged( 1 );
  }//GEN-LAST:event_reviewtype1buttonActionPerformed

  private void reviewtype2buttonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_reviewtype2buttonActionPerformed
  {//GEN-HEADEREND:event_reviewtype2buttonActionPerformed
    reviewTypeChanged( 2 );
  }//GEN-LAST:event_reviewtype2buttonActionPerformed

  private void reviewtype3buttonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_reviewtype3buttonActionPerformed
  {//GEN-HEADEREND:event_reviewtype3buttonActionPerformed
    reviewTypeChanged( 3 );
  }//GEN-LAST:event_reviewtype3buttonActionPerformed

  private void recomputemenuitemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_recomputemenuitemActionPerformed
  {//GEN-HEADEREND:event_recomputemenuitemActionPerformed
    exam.invalidateAllOutcomes();
    exam.updateOutcomes();
    exam.setUnsavedChangesInMain( true );
    exam.processDataChanged( exam.qdefs );
  }//GEN-LAST:event_recomputemenuitemActionPerformed

  private void reviewincludeallActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_reviewincludeallActionPerformed
  {//GEN-HEADEREND:event_reviewincludeallActionPerformed
    reviewFilterChanged( 1 );
  }//GEN-LAST:event_reviewincludeallActionPerformed

  private void reviewincludenotreviewedActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_reviewincludenotreviewedActionPerformed
  {//GEN-HEADEREND:event_reviewincludenotreviewedActionPerformed
    reviewFilterChanged( 2 );
  }//GEN-LAST:event_reviewincludenotreviewedActionPerformed

  private void reviewincludeoverriddenActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_reviewincludeoverriddenActionPerformed
  {//GEN-HEADEREND:event_reviewincludeoverriddenActionPerformed
    reviewFilterChanged( 3 );
  }//GEN-LAST:event_reviewincludeoverriddenActionPerformed

  private void printitemanalysismenuitemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_printitemanalysismenuitemActionPerformed
  {//GEN-HEADEREND:event_printitemanalysismenuitemActionPerformed
    if ( exam == null )
    {
      JOptionPane.showMessageDialog( this, "No exam/survey open." );
      return;
    }
    
    if ( exam.qdefs.getColumnCount() == 0 )
    {
      JOptionPane.showMessageDialog( this, "No question to analyse." );
      return;
    }
    
    int i, n=0;
    CandidateData cd;
    for ( i=0; i<exam.candidates_sorted.size(); i++ )
    {
      cd = exam.candidates_sorted.get( i );
      if ( cd.questionsAsked() == cd.questionsScanned() )
        n++;
    }
    
    if ( n<3 )
    {
      JOptionPane.showMessageDialog( this, "There are not enough candidates with all questions marked." );
      return;
    }
    
    exam.itemAnalysis();    
    printstatuslabel.setText( "Printing..." );
    progressbar.setIndeterminate( true );

    //busydialog.setVisible( true );
    PrintThread thread = new PrintThread( exam, examfolder, fontmanager );
    thread.setQyoutiFrame( this );
    thread.setType( PrintThread.TYPE_ANALYSIS );
    thread.start();    

  }//GEN-LAST:event_printitemanalysismenuitemActionPerformed

  private void jButton1ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton1ActionPerformed
  {//GEN-HEADEREND:event_jButton1ActionPerformed
    AffineTransform atr, atp, atv, atvb;
    previewsvgcanvas.resetRenderingTransform();
    atr = previewsvgcanvas.getRenderingTransform();
    atp = previewsvgcanvas.getPaintingTransform();
    atv = previewsvgcanvas.getViewingTransform();
    atvb = previewsvgcanvas.getViewBoxTransform();
    atr.concatenate( AffineTransform.getScaleInstance( 2.0, 2.0 ) );
    previewsvgcanvas.setRenderingTransform( atr );
  }//GEN-LAST:event_jButton1ActionPerformed

  private void updatePersonCustomizationFromForm()
  {
    if ( customisationpanelupdating )
      return;
    
    PersonData person = null;
    int r = persontable.getSelectedRow();
    if ( r < 0 )
      return;
    
    System.out.println( "updatePersonCustomizationFromForm" );
    person = exam.persons_sorted.get( r );
    // get values and work out if this is a null customisation....
    boolean bigpink = bigpinkcheckbox.isSelected();
    boolean onecolumn = onecolumncheckbox.isSelected();
    boolean serif = serifcheckbox.isSelected();
    double fsz = 0.0;
    switch ( fontsizelist.getSelectedIndex() )
    {
      case 0:
        fsz = 0.0;
        break;
      case 1:
        fsz = 0.10;
        break;
      case 2:
        fsz = 0.12;
        break;
      case 3:
        fsz = 0.16;
        break;
    }

    Color c = colourswatchlabel.getBackground();
    
    boolean isnull = !bigpink && !onecolumn && !serif && fsz == 0.0 && c.equals( Color.WHITE );
    if ( isnull )
    {
      person.setPreferences( null );
    }
    else
    {
      UserRenderPreferences prefs = new UserRenderPreferences();
      prefs.setBigpinkbox( bigpink );
      prefs.setOneColumn( onecolumn );
      prefs.setSerif( serif );
      prefs.setFontsize( fsz );
      prefs.setBackground( c );
      person.setPreferences( prefs );
    }
    
    persontable.repaint();
  }
  
  private void bigpinkcheckboxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_bigpinkcheckboxActionPerformed
  {//GEN-HEADEREND:event_bigpinkcheckboxActionPerformed
    updatePersonCustomizationFromForm();
  }//GEN-LAST:event_bigpinkcheckboxActionPerformed

  private void serifcheckboxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_serifcheckboxActionPerformed
  {//GEN-HEADEREND:event_serifcheckboxActionPerformed
    updatePersonCustomizationFromForm();
  }//GEN-LAST:event_serifcheckboxActionPerformed

  private void resetpreferencesbuttonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_resetpreferencesbuttonActionPerformed
  {//GEN-HEADEREND:event_resetpreferencesbuttonActionPerformed
    PersonData person = null;
    int r = persontable.getSelectedRow();
    if ( r < 0 )
      return;
    person = exam.persons_sorted.get( r );
    person.setPreferences( null );    
    updatePersonCustomisePanel();
    persontable.repaint();
  }//GEN-LAST:event_resetpreferencesbuttonActionPerformed

  private void changecolourbuttonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_changecolourbuttonActionPerformed
  {//GEN-HEADEREND:event_changecolourbuttonActionPerformed
    Color c = Color.WHITE;
    
    PersonData person = null;
    int r = persontable.getSelectedRow();
    if ( r < 0 )
      return;

    person = exam.persons_sorted.get( r );
    UserRenderPreferences pref = person.getPreferences();
    if ( pref != null )
      c = pref.getBackground();
    
    Color co = JColorChooser.showDialog( this, "Background Colour", c );
    if ( co != null && !co.equals( c ) )
    {
      colourswatchlabel.setBackground( co );
      updatePersonCustomizationFromForm();
    }
  }//GEN-LAST:event_changecolourbuttonActionPerformed

  private void fontsizelistValueChanged(javax.swing.event.ListSelectionEvent evt)//GEN-FIRST:event_fontsizelistValueChanged
  {//GEN-HEADEREND:event_fontsizelistValueChanged
    updatePersonCustomizationFromForm();
  }//GEN-LAST:event_fontsizelistValueChanged

  private void allanonbuttonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_allanonbuttonActionPerformed
  {//GEN-HEADEREND:event_allanonbuttonActionPerformed
    String lpid = exam.getLastPrintID();
    if ( lpid != null && lpid.length() > 0 )
    {
      JOptionPane.
              showMessageDialog( this, "The exam/survey has already been printed. \nYou cannot change anonymity." );
      return;
    }

    int n;
    PersonData person;
    for ( n=0; n < exam.persons_sorted.size(); n++ )
    {
      person = exam.persons_sorted.get( n );
      if ( person.getPreferences() == null )
        person.setAnonymous( true );
    }
    persontable.repaint();
  }//GEN-LAST:event_allanonbuttonActionPerformed

  private void noneanonbuttonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_noneanonbuttonActionPerformed
  {//GEN-HEADEREND:event_noneanonbuttonActionPerformed
    String lpid = exam.getLastPrintID();
    if ( lpid != null && lpid.length() > 0 )
    {
      JOptionPane.
              showMessageDialog( this, "The exam/survey has already been printed. \nYou cannot change anonymity." );
      return;
    }

    int n;
    PersonData person;
    for ( n=0; n < exam.persons_sorted.size(); n++ )
    {
      person = exam.persons_sorted.get( n );
      if ( person.getPreferences() == null )
        person.setAnonymous( false );
    }
    persontable.repaint();
  }//GEN-LAST:event_noneanonbuttonActionPerformed

  private void configmenuitemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_configmenuitemActionPerformed
  {//GEN-HEADEREND:event_configmenuitemActionPerformed
    ConfigDialog cd = new ConfigDialog( this, this.fontmanager );
    cd.setVisible( true );
    
  }//GEN-LAST:event_configmenuitemActionPerformed

  private void reviewincludeconfirmedActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_reviewincludeconfirmedActionPerformed
  {//GEN-HEADEREND:event_reviewincludeconfirmedActionPerformed
    reviewFilterChanged( 4 );
  }//GEN-LAST:event_reviewincludeconfirmedActionPerformed

    private void onecolumncheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onecolumncheckboxActionPerformed
        updatePersonCustomizationFromForm();
    }//GEN-LAST:event_onecolumncheckboxActionPerformed

  private void nextreviewmenuitemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_nextreviewmenuitemActionPerformed
  {//GEN-HEADEREND:event_nextreviewmenuitemActionPerformed
    tabs.setSelectedComponent( rtab );
    nextreviewbuttonActionPerformed(null);
  }//GEN-LAST:event_nextreviewmenuitemActionPerformed

  private void previousreviewmenuitemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_previousreviewmenuitemActionPerformed
  {//GEN-HEADEREND:event_previousreviewmenuitemActionPerformed
    tabs.setSelectedComponent( rtab );
    previousreviewbuttonActionPerformed(null);   
  }//GEN-LAST:event_previousreviewmenuitemActionPerformed

  private void confirmreviewmenuitemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_confirmreviewmenuitemActionPerformed
  {//GEN-HEADEREND:event_confirmreviewmenuitemActionPerformed
    tabs.setSelectedComponent( rtab );
    
    CandidateQuestionPanel cqp;
    Component c = questionreviewpanel.getComponent( 0 );
    if ( c == null ) return;
    if ( !(c instanceof CandidateQuestionPanel) ) return;
    cqp = (CandidateQuestionPanel)c;
    cqp.handleDecision( 1 );
    cqp.updateButtons();    
  }//GEN-LAST:event_confirmreviewmenuitemActionPerformed

  private void overridereviewmenuitemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_overridereviewmenuitemActionPerformed
  {//GEN-HEADEREND:event_overridereviewmenuitemActionPerformed
    tabs.setSelectedComponent( rtab );
    
    CandidateQuestionPanel cqp;
    Component c = questionreviewpanel.getComponent( 0 );
    if ( c == null ) return;
    if ( !(c instanceof CandidateQuestionPanel) ) return;
    cqp = (CandidateQuestionPanel)c;
    cqp.handleDecision( 2 );
    cqp.updateButtons();
  }//GEN-LAST:event_overridereviewmenuitemActionPerformed

  /**
   * Indicates that the question edit dialog stored some changes into its item
   * object. So, the exam file needs saving to disk.
   */
  void questionEditStored()
  {
    if ( exam.pagelistmodel.getRowCount() > 0 )
    {
      exam.invalidateOutcomes( editquestionident );
      exam.updateOutcomes();
    }
    exam.setUnsavedChangesInQuestions( true );
    exam.processDataChanged( exam.qdefs );
  }

  /**
   * Indicates that the template system created a string representation
   * of a new exam. It is ready to be saved to file.
   */
  void examinationBuilt( File folder, ExamTemplate template )
  {
    File file;
    FileWriter writer;
    
    file = new File( folder, "qyouti.xml" );
    writer = null;
    try
    {
      writer = new FileWriter( file );
      writer.write( template.getMainDocumentAsString() );
    }
    catch ( Exception ex )
    {
      Logger.getLogger( QyoutiFrame.class.getName() ).
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
          Logger.getLogger( QyoutiFrame.class.getName() ).
                  log( Level.SEVERE, null, ex );
        }
      }
    }
    file = new File( folder, "questions.xml" );
    writer = null;
    try
    {
      writer = new FileWriter( file );
      writer.write( template.getQuestionDocumentAsString() );
    }
    catch ( Exception ex )
    {
      Logger.getLogger( QyoutiFrame.class.getName() ).
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
          Logger.getLogger( QyoutiFrame.class.getName() ).
                  log( Level.SEVERE, null, ex );
        }
      }
    }

    loadExam( folder );
  }

  boolean examSelectDialogDone()
  {
    basefolder = selectdialog.getBaseFolder();
    examcatalogue = selectdialog.getExaminationCatalogue();

    File fold = new File( basefolder, selectdialog.getExamName() );

    if ( selectdialog.getDialogType() == ExamSelectDialog.TYPE_NEW )
    {
      System.out.
              println( "Make new exam/survey: " + selectdialog.getExamName() );
      if ( fold.exists() )
      {
        JOptionPane.
                showMessageDialog( this, "There is already a folder with that name - choose a different name." );
        return false;
      }
      if ( !fold.mkdir() )
      {
        JOptionPane.
                showMessageDialog( this, "Unable to create a folder with that name - perhaps you lack access rights." );
        return false;
      }
      ExamCreateDialog dialog = new ExamCreateDialog( this, true );
      dialog.setFolder( fold );
      dialog.setVisible( true );
    }

    if ( selectdialog.getDialogType() == ExamSelectDialog.TYPE_OPEN )
    {
      System.out.println( "Open exam/survey: " + selectdialog.getExamName() );
      loadExam( fold );
    }

    return true;
  }

  void examOptionsSaved()
  {
    exam.setUnsavedChangesInMain( true );
  }

  private void loadExam( File examfolder )
  {
    this.examfolder = examfolder;

    noexamloadedlabel.setText( "No exam/survey loaded." );
    try
    {
      if ( exam!= null )
        exam.close();
      
      exam = new ExaminationData( examcatalogue, examfolder );
      exam.addExaminationDataStatusListener( this );
      persontable.setModel( exam.personlistmodel );
      exam.personlistmodel.addTableModelListener( 
              new TableModelListener()
              {
                @Override
                public void tableChanged( TableModelEvent e )
                {
                  personTableChanged( e );
                }
              }
      );
      pagestable.setModel( exam.pagelistmodel );
      scanfiletable.setModel( exam.scans );
      candidatetable.setModel( exam );
      questionreviewtable.setModel( exam.reviewlist );
      updateLeftList();
      exam.load();
      setTitle( "Qyouti - " + examfolder.getName() );
      if ( exam.qdefs != null )
      {
        questiontable.setModel( exam.qdefs );
      }
      analysistable.setModel(exam.analysistablemodel );
      analysistable.getTableHeader().setDefaultRenderer( new VerticalTextTableCellRenderer() );
      analysistable.setRowHeight( 48 );
      analysistable.setDefaultRenderer( String.class, new TableCellRenderer() {
        JLabel label=null;
        @Override
        public Component getTableCellRendererComponent( JTable table,
                                                        Object value,
                                                        boolean isSelected,
                                                        boolean hasFocus,
                                                        int row, int column )
        {
          Dimension d;
          if ( label == null )
          {
            label = new JLabel();
            label.setHorizontalAlignment( JLabel.CENTER );
          }
          label.setText( value.toString() );
          exam.analysistablemodel.setValueProperties( label, row, column );
          return label;
        }
      } );
      ((CardLayout)centralpanel.getLayout()).last( centralpanel );
    }
    catch ( Exception ex )
    {
      Logger.getLogger( QyoutiFrame.class.getName() ).
              log( Level.SEVERE, null, ex );
      noexamloadedlabel.setText( "Error attempting to load exam/survey." );
      ((CardLayout)centralpanel.getLayout()).first( centralpanel );
    }

  }

  /**
   * @param args the command line arguments
   */
  public static void main( String args[] )
  {
    /* Set the Nimbus look and feel */
    //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
    /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
     */
    try
    {
      for ( javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.
              getInstalledLookAndFeels() )
      {
        if ( "Nimbus".equals( info.getName() ) )
        {
          javax.swing.UIManager.setLookAndFeel( info.getClassName() );
          break;
        }
      }
    }
    catch ( ClassNotFoundException ex )
    {
      java.util.logging.Logger.getLogger( QyoutiFrame.class.getName() ).
              log( java.util.logging.Level.SEVERE, null, ex );
    }
    catch ( InstantiationException ex )
    {
      java.util.logging.Logger.getLogger( QyoutiFrame.class.getName() ).
              log( java.util.logging.Level.SEVERE, null, ex );
    }
    catch ( IllegalAccessException ex )
    {
      java.util.logging.Logger.getLogger( QyoutiFrame.class.getName() ).
              log( java.util.logging.Level.SEVERE, null, ex );
    }
    catch ( javax.swing.UnsupportedLookAndFeelException ex )
    {
      java.util.logging.Logger.getLogger( QyoutiFrame.class.getName() ).
              log( java.util.logging.Level.SEVERE, null, ex );
    }
    //</editor-fold>

    /* Create and display the form */
    java.awt.EventQueue.invokeLater( new Runnable()
    {
      public void run()
      {
        new QyoutiFrame().setVisible( true );
      }
    } );
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JMenuItem aboutmenuitem;
  private javax.swing.JMenu actionmenu;
  private javax.swing.JButton allanonbutton;
  private javax.swing.JTable analysistable;
  private javax.swing.JCheckBox bigpinkcheckbox;
  private javax.swing.JTable candidatetable;
  private javax.swing.JPanel centralpanel;
  private javax.swing.JButton changecolourbutton;
  private javax.swing.JMenuItem clearscanneddatamenuitem;
  private javax.swing.JLabel colourswatchlabel;
  private javax.swing.JMenuItem configmenuitem;
  private javax.swing.JMenuItem confirmreviewmenuitem;
  private javax.swing.JPanel ctab;
  private javax.swing.JLabel customforlabel;
  private javax.swing.JMenuItem editallquestionsmenuitem;
  private javax.swing.JMenuItem editquestionmenuitem;
  private javax.swing.JLabel errorlabel;
  private javax.swing.JMenuItem exitmenuitem;
  private javax.swing.JMenuItem exprepliesmenuitem;
  private javax.swing.JMenuItem expscoresmenuitem;
  private javax.swing.JMenu filemenu;
  private javax.swing.JLabel fontdizelabel;
  private javax.swing.ButtonGroup fontprefbuttongroup;
  private javax.swing.JList<String> fontsizelist;
  private javax.swing.JMenuItem forgetprintmenuitem;
  private javax.swing.JMenuItem importcanmenuitem;
  private javax.swing.JMenuItem importimagesmenuitem;
  private javax.swing.JMenuItem importqmenuitem;
  private javax.swing.JMenuItem itemanalysismenuitem;
  private javax.swing.JButton jButton1;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JLabel jLabel3;
  private javax.swing.JLabel jLabel4;
  private javax.swing.JLabel jLabel5;
  private javax.swing.JLabel jLabel6;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JPanel jPanel10;
  private javax.swing.JPanel jPanel11;
  private javax.swing.JPanel jPanel12;
  private javax.swing.JPanel jPanel13;
  private javax.swing.JPanel jPanel2;
  private javax.swing.JPanel jPanel3;
  private javax.swing.JPanel jPanel4;
  private javax.swing.JPanel jPanel5;
  private javax.swing.JPanel jPanel6;
  private javax.swing.JPanel jPanel7;
  private javax.swing.JPanel jPanel8;
  private javax.swing.JPanel jPanel9;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JScrollPane jScrollPane2;
  private javax.swing.JScrollPane jScrollPane3;
  private javax.swing.JScrollPane jScrollPane4;
  private javax.swing.JScrollPane jScrollPane5;
  private javax.swing.JScrollPane jScrollPane6;
  private javax.swing.JScrollPane jScrollPane7;
  private javax.swing.JPopupMenu.Separator jSeparator1;
  private javax.swing.JPopupMenu.Separator jSeparator2;
  private javax.swing.JSplitPane jSplitPane1;
  private javax.swing.JSplitPane jSplitPane2;
  private javax.swing.JSplitPane jSplitPane3;
  private javax.swing.JSplitPane jSplitPane4;
  private javax.swing.JTabbedPane jTabbedPane1;
  private javax.swing.JTextPane jTextPane1;
  private javax.swing.JMenuBar menubar;
  private javax.swing.JMenuItem newmenuitem;
  private javax.swing.JButton nextreviewbutton;
  private javax.swing.JMenuItem nextreviewmenuitem;
  private javax.swing.JPanel noeditpersonpanel;
  private javax.swing.JLabel noexamloadedlabel;
  private javax.swing.JPanel noexamloadedpanel;
  private javax.swing.JButton noneanonbutton;
  private javax.swing.JPanel nopersonselectedpanel;
  private javax.swing.JCheckBox onecolumncheckbox;
  private javax.swing.JMenuItem openmenuitem;
  private javax.swing.JMenuItem overridereviewmenuitem;
  private javax.swing.JTable pagestable;
  private javax.swing.JMenuItem pdfprintmenuitem;
  private javax.swing.JPanel personpanel;
  private javax.swing.JTable persontable;
  private javax.swing.JPanel perstab;
  private org.apache.batik.swing.JSVGCanvas previewsvgcanvas;
  private javax.swing.JButton previousreviewbutton;
  private javax.swing.JMenuItem previousreviewmenuitem;
  private javax.swing.JMenuItem printitemanalysismenuitem;
  private javax.swing.JLabel printstatuslabel;
  private javax.swing.JProgressBar progressbar;
  private javax.swing.JMenuItem propsmenuitem;
  private javax.swing.JPanel ptab;
  private javax.swing.JScrollPane qprevscrollpane;
  private javax.swing.JTable qrevlefttable;
  private javax.swing.JPanel qrpanelouter;
  private javax.swing.JPanel qtab;
  private javax.swing.JPanel questionanalysistoppane;
  private javax.swing.JPanel questionreviewpanel;
  private javax.swing.JTable questionreviewtable;
  private javax.swing.JTable questiontable;
  private javax.swing.JMenuItem recomputemenuitem;
  private javax.swing.JButton resetpreferencesbutton;
  private javax.swing.JRadioButton reviewincludeall;
  private javax.swing.ButtonGroup reviewincludebuttongroup;
  private javax.swing.JRadioButton reviewincludeconfirmed;
  private javax.swing.JRadioButton reviewincludenotreviewed;
  private javax.swing.JRadioButton reviewincludeoverridden;
  private javax.swing.JMenu reviewmenu;
  private javax.swing.JRadioButton reviewtype1button;
  private javax.swing.JRadioButton reviewtype2button;
  private javax.swing.JRadioButton reviewtype3button;
  private javax.swing.ButtonGroup reviewtypebuttongroup;
  private javax.swing.JPanel rtab;
  private javax.swing.JMenuItem savemenuitem;
  private javax.swing.JLabel savestatuslabel;
  private javax.swing.JTable scanfiletable;
  private javax.swing.JPanel selectedpersonpanel;
  private javax.swing.JPopupMenu.Separator sep1;
  private javax.swing.JPopupMenu.Separator sep1b;
  private javax.swing.JPopupMenu.Separator sep2;
  private javax.swing.JPopupMenu.Separator sep3;
  private javax.swing.JCheckBox serifcheckbox;
  private javax.swing.JScrollPane sp1;
  private javax.swing.JScrollPane sp2;
  private javax.swing.JScrollPane sp3;
  private javax.swing.JScrollPane sp4;
  private javax.swing.JScrollPane sp8;
  private javax.swing.JLabel spacerlabel;
  private javax.swing.JPanel stab;
  private javax.swing.JPanel statuspanel;
  private javax.swing.JTabbedPane tabs;
  private javax.swing.JMenuItem viewscanmenuitem;
  // End of variables declaration//GEN-END:variables

  @Override
  public void windowOpened( WindowEvent e )
  {

  }

  @Override
  public void windowClosing( WindowEvent e )
  {
    System.out.println( e );
    confirmExit();
  }

  @Override
  public void windowClosed( WindowEvent e )
  {
    System.out.println( e );
  }

  @Override
  public void windowIconified( WindowEvent e )
  {
  }

  @Override
  public void windowDeiconified( WindowEvent e )
  {
  }

  @Override
  public void windowActivated( WindowEvent e )
  {
  }

  @Override
  public void windowDeactivated( WindowEvent e )
  {
  }

  @Override
  public void examinationDataStatusChanged( ExaminationData exam )
  {
    savestatuslabel.setText( exam.areThereUnsavedChanges() ? "Unsaved Data" : " " );
    String lpid = exam.getLastPrintID();
    if ( lpid == null || lpid.length() == 0 )
    {
      printstatuslabel.setText( "Not printed" );
    }
    else
    {
      printstatuslabel.setText( "Print ID = " + lpid );
    }
  }

  @Override
  public void scanCompleted()
  {
    tabs.setEnabled( true );
    filemenu.setEnabled( true );
    actionmenu.setEnabled( true );
    progressbar.setIndeterminate( false );
  }


  public void reviewSelectionChanged( ListSelectionEvent e )
  {
    int i;
    CandidateData c;
    QuestionData q;
    CandidateQuestionPanel cqp;
    GridBagConstraints gbc = new GridBagConstraints();

    gbc.gridx = 0;
    gbc.weightx = 0.5;
    gbc.weighty = 0.5;
    gbc.anchor = GridBagConstraints.FIRST_LINE_START;
    gbc.fill = GridBagConstraints.HORIZONTAL;

    questionreviewpanel.removeAll();
    if ( e.getValueIsAdjusting() )
      return;
    
    int n=0;
    for ( i=0; i<questionreviewtable.getRowCount(); i++ )
    {
      if ( questionreviewtable.isRowSelected( i ) )
      {
        c = exam.reviewlist.getCandidateData( i );
        q = exam.reviewlist.getQuestionData( i );
        cqp = new CandidateQuestionPanel( c, q.ident );
        questionreviewpanel.add( cqp, gbc );
        n++;
      }
    }
    
    if ( n==0 )
      questionreviewpanel.add( new JLabel("No questions selected.") );
    qrpanelouter.validate();
    System.out.println( "Update question review panel... " + questionreviewpanel.getWidth() +  " x " + questionreviewpanel.getHeight() );
    System.out.println( "qrpanelouter... " + qrpanelouter.getWidth() +  " x " + qrpanelouter.getHeight() );
  } 
  
  public void reviewLeftSelectionChanged( ListSelectionEvent e )
  {
    questionreviewpanel.removeAll();
    if ( e.getValueIsAdjusting() )
      return;

    System.out.println( "Left selection changed." );
    
    int row = qrevlefttable.getSelectedRow();
    String ident;
    
    if ( exam.getReviewType() == ExaminationData.REVIEW_BY_CANDIDATE )
    {
      ident = ( row < 0 )?null:exam.getValueAt( row, 2 ).toString();
      exam.setReviewCandidateIdent( ident );
    }

    if ( exam.getReviewType() == ExaminationData.REVIEW_BY_QUESTION )
    {
      ident = ( row < 0 )?null:exam.qdefs.getValueAt( row, 0 ).toString();
      exam.setReviewQuestionIdent( ident );
    }  
  }

  
    class ChangeLookAndFeelAction extends AbstractAction {
        QyoutiFrame frame;
        String laf;
        protected ChangeLookAndFeelAction(QyoutiFrame frame, String laf) {
            super("ChangeTheme");
            this.frame = frame;
            this.laf = laf;
        }

        public void actionPerformed(ActionEvent e) {
            frame.setLookAndFeel(laf);
        }
    }

 	  protected Interactor panInteractor = new AbstractPanInteractor()
    {
 	        public boolean startInteraction(InputEvent ie) {
 	            int mods = ie.getModifiers();
 	            return
 	                ie.getID() == MouseEvent.MOUSE_PRESSED &&
 	                (mods & InputEvent.BUTTON1_MASK) != 0;
 	        }
 	  };    
  
}
