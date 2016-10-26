/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import org.apache.batik.dom.*;
import org.apache.batik.transcoder.*;
import org.qyouti.data.*;
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
  File examfolder = null;
  ExaminationData exam;
  ExaminationCatalogue examcatalogue;
  File basefolder;
  String examname = null;

  ExamSelectDialog selectdialog;
  QuestionPreviewDialog questiondialog;
  BusyDialog busydialog;

  String editquestionident;
  QuestionData currentquestiondata;
  String scanfolder = null;

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

    //File preferences_file = new File( appfolder, "preferences.xml" );
    preferences = new QyoutiPreferences( null );//preferences_file);
    //if ( preferences_file.exists() )
    //  preferences.load();
    //else
    preferences.setDefaults();

    busydialog = new BusyDialog( this, false );
    selectdialog = new ExamSelectDialog( this, true );
    selectdialog.setFrame( this );
    initComponents();
    createLookAndFeelMenu();
    
    //questiontable.getColumnModel().getColumn( 0 ).
    candidatetable.getSelectionModel().addListSelectionListener( 
            new ListSelectionListener() {
              
      @Override
      public void valueChanged( ListSelectionEvent e )
      {
        candidateSelectionChanged( e );
      }
              
            });
    
    gotoQuestion( null );
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
     
     
    
  public void candidateSelectionChanged( ListSelectionEvent e )
  {
    System.out.println( "Candidate selection change." );
    candqpanel.removeAll();
    
    if ( candidatetable.getSelectedRowCount() == 1 )
    {
      System.out.println( "Show candidate " + candidatetable.getSelectedRow() );
      CandidateData cd = exam.candidates_sorted.get( candidatetable.getSelectedRow() );
      cdetailnamelabel.setText( cd.name );
      cdetailidlabel.setText( cd.id );
      cdetailoutcometable.setModel( cd.outcomes );
      cdetailerrorlabel.setText( cd.getErrorMessage() );
      
      Vector<QTIElementItem> items = cd.getItems();
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.gridx = 0;
      gbc.weightx = 0.5;
      gbc.weighty = 0.5;
      gbc.anchor = GridBagConstraints.FIRST_LINE_START;
      gbc.fill = GridBagConstraints.HORIZONTAL;
      CandidateQuestionPanel cqp;
      for ( int i=0; i<items.size(); i++ )
      {
        cqp = new CandidateQuestionPanel( cd, items.get( i ).getIdent() );
        candqpanel.add( cqp, gbc );
      }
      sp6.revalidate();
    }
    else
    {
      System.out.println( "Clear candidate panel" );
      cdetailnamelabel.setText( "" );
      cdetailidlabel.setText( "" );
      cdetailoutcometable.setModel( new OutcomeData() );
      cdetailerrorlabel.setText( "" );
    }
  }
  
  boolean confirmDataLoss( String message )
  {
    if ( exam != null && exam.areUnsavedChanges() )
    {
      if ( JOptionPane.
              showConfirmDialog( this, "There is unsaved data.\n" + message, "Confirmation", JOptionPane.YES_NO_OPTION ) != JOptionPane.YES_OPTION )
      {
        return false;
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

    spacerlabel = new javax.swing.JLabel();
    tabs = new javax.swing.JTabbedPane();
    qtab = new javax.swing.JPanel();
    sp1 = new javax.swing.JScrollPane();
    questiontable = new javax.swing.JTable();
    ctab = new javax.swing.JPanel();
    splitpane = new javax.swing.JSplitPane();
    sp2 = new javax.swing.JScrollPane();
    candidatetable = new javax.swing.JTable();
    jSplitPane1 = new javax.swing.JSplitPane();
    cdetailpanel = new javax.swing.JPanel();
    jPanel3 = new javax.swing.JPanel();
    jLabel1 = new javax.swing.JLabel();
    cdetailnamelabel = new javax.swing.JLabel();
    jLabel3 = new javax.swing.JLabel();
    cdetailidlabel = new javax.swing.JLabel();
    jPanel1 = new javax.swing.JPanel();
    cdetailoutcometable = new javax.swing.JTable();
    jLabel6 = new javax.swing.JLabel();
    cdetailerrorlabel = new javax.swing.JLabel();
    sp6 = new javax.swing.JScrollPane();
    candqpanelouter = new javax.swing.JPanel();
    candqpanel = new javax.swing.JPanel();
    stab = new javax.swing.JPanel();
    sp3 = new javax.swing.JScrollPane();
    scanstable = new javax.swing.JTable();
    rtab = new javax.swing.JPanel();
    resptoppanel = new javax.swing.JPanel();
    respheadpanel = new javax.swing.JPanel();
    respleftpanel = new javax.swing.JPanel();
    resptopleftpanel = new javax.swing.JPanel();
    previouscandidatebutton = new javax.swing.JButton();
    respbottomleftpanel = new javax.swing.JPanel();
    previousquestionbutton = new javax.swing.JButton();
    jPanel2 = new javax.swing.JPanel();
    resppropertypanel = new javax.swing.JPanel();
    l1 = new javax.swing.JLabel();
    candidatelabel = new javax.swing.JLabel();
    l2 = new javax.swing.JLabel();
    idlabel = new javax.swing.JLabel();
    l3 = new javax.swing.JLabel();
    pagelabel = new javax.swing.JLabel();
    l4 = new javax.swing.JLabel();
    sourcelabel = new javax.swing.JLabel();
    l5 = new javax.swing.JLabel();
    heightlabel = new javax.swing.JLabel();
    l6 = new javax.swing.JLabel();
    questionlabel = new javax.swing.JLabel();
    l7 = new javax.swing.JLabel();
    needsreviewlabel = new javax.swing.JLabel();
    l8 = new javax.swing.JLabel();
    reviewcombobox = new javax.swing.JComboBox<>();
    resprightpanel = new javax.swing.JPanel();
    resptoprightpanel = new javax.swing.JPanel();
    nextcandidatebutton = new javax.swing.JButton();
    respbottomrightpanel = new javax.swing.JPanel();
    nextquestionbutton = new javax.swing.JButton();
    outcomepanel = new javax.swing.JPanel();
    jPanel4 = new javax.swing.JPanel();
    jPanel6 = new javax.swing.JPanel();
    overalloutcometable = new javax.swing.JTable();
    jPanel5 = new javax.swing.JPanel();
    outcometable = new javax.swing.JTable();
    sp4 = new javax.swing.JScrollPane();
    responsetable = new javax.swing.JTable();
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
    previewqmenuitem = new javax.swing.JMenuItem();
    itemanalysismenuitem = new javax.swing.JMenuItem();
    sep2 = new javax.swing.JPopupMenu.Separator();
    preprocmenuitem = new javax.swing.JMenuItem();
    importimagesmenuitem = new javax.swing.JMenuItem();
    clearscanneddatamenuitem = new javax.swing.JMenuItem();
    viewscanmenuitem = new javax.swing.JMenuItem();
    sep3 = new javax.swing.JPopupMenu.Separator();
    importcanmenuitem = new javax.swing.JMenuItem();
    expscoresmenuitem = new javax.swing.JMenuItem();
    exprepliesmenuitem = new javax.swing.JMenuItem();
    expreportmenuitem = new javax.swing.JMenuItem();

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    setTitle("Qyouti Exam/Survey Processor");

    spacerlabel.setText(" ");
    getContentPane().add(spacerlabel, java.awt.BorderLayout.PAGE_START);

    qtab.setLayout(new java.awt.BorderLayout());

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

    qtab.add(sp1, java.awt.BorderLayout.CENTER);

    tabs.addTab("Questions", qtab);

    ctab.setLayout(new java.awt.BorderLayout());

    splitpane.setDividerLocation(150);
    splitpane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

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

    splitpane.setLeftComponent(sp2);

    jSplitPane1.setDividerLocation(200);

    cdetailpanel.setLayout(new java.awt.GridBagLayout());

    jPanel3.setLayout(new java.awt.GridBagLayout());

    jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    jLabel1.setText("Name :");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.weighty = 0.5;
    gridBagConstraints.insets = new java.awt.Insets(4, 8, 4, 8);
    jPanel3.add(jLabel1, gridBagConstraints);

    cdetailnamelabel.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.weighty = 0.5;
    gridBagConstraints.insets = new java.awt.Insets(4, 8, 4, 8);
    jPanel3.add(cdetailnamelabel, gridBagConstraints);

    jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    jLabel3.setText("ID :");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.weighty = 0.5;
    gridBagConstraints.insets = new java.awt.Insets(4, 8, 4, 8);
    jPanel3.add(jLabel3, gridBagConstraints);

    cdetailidlabel.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.weighty = 0.5;
    gridBagConstraints.insets = new java.awt.Insets(4, 8, 4, 8);
    jPanel3.add(cdetailidlabel, gridBagConstraints);

    jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Outcomes"));
    jPanel1.setMinimumSize(new java.awt.Dimension(80, 38));
    jPanel1.setLayout(new java.awt.BorderLayout());

    cdetailoutcometable.setModel(new javax.swing.table.DefaultTableModel(
      new Object [][]
      {
        {null, null}
      },
      new String []
      {
        "Title 1", "Title 2"
      }
    ));
    jPanel1.add(cdetailoutcometable, java.awt.BorderLayout.CENTER);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.weighty = 0.5;
    jPanel3.add(jPanel1, gridBagConstraints);

    jLabel6.setText("Errors:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.weighty = 0.5;
    gridBagConstraints.insets = new java.awt.Insets(4, 8, 4, 8);
    jPanel3.add(jLabel6, gridBagConstraints);

    cdetailerrorlabel.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.weighty = 0.5;
    gridBagConstraints.insets = new java.awt.Insets(4, 8, 4, 8);
    jPanel3.add(cdetailerrorlabel, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.weighty = 0.1;
    cdetailpanel.add(jPanel3, gridBagConstraints);

    jSplitPane1.setLeftComponent(cdetailpanel);

    sp6.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    sp6.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

    candqpanelouter.setBackground(java.awt.Color.white);
    candqpanelouter.setLayout(new java.awt.GridBagLayout());

    candqpanel.setBackground(java.awt.Color.white);
    candqpanel.setLayout(new java.awt.GridBagLayout());
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.weighty = 0.5;
    candqpanelouter.add(candqpanel, gridBagConstraints);

    sp6.setViewportView(candqpanelouter);

    jSplitPane1.setRightComponent(sp6);

    splitpane.setRightComponent(jSplitPane1);

    ctab.add(splitpane, java.awt.BorderLayout.CENTER);

    tabs.addTab("Candidates", ctab);

    stab.setLayout(new java.awt.BorderLayout());

    scanstable.setModel(new javax.swing.table.DefaultTableModel(
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
    scanstable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
    sp3.setViewportView(scanstable);

    stab.add(sp3, java.awt.BorderLayout.CENTER);

    tabs.addTab("Scans", stab);

    rtab.setLayout(new java.awt.BorderLayout());

    resptoppanel.setLayout(new java.awt.BorderLayout());

    respheadpanel.setLayout(new java.awt.BorderLayout());

    respleftpanel.setLayout(new java.awt.BorderLayout());

    resptopleftpanel.setLayout(new java.awt.BorderLayout());

    previouscandidatebutton.setText("Previous");
    previouscandidatebutton.setEnabled(false);
    previouscandidatebutton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        previouscandidatebuttonActionPerformed(evt);
      }
    });
    resptopleftpanel.add(previouscandidatebutton, java.awt.BorderLayout.CENTER);

    respleftpanel.add(resptopleftpanel, java.awt.BorderLayout.NORTH);

    respbottomleftpanel.setLayout(new java.awt.BorderLayout());

    previousquestionbutton.setText("Previous");
    previousquestionbutton.setEnabled(false);
    previousquestionbutton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        previousquestionbuttonActionPerformed(evt);
      }
    });
    respbottomleftpanel.add(previousquestionbutton, java.awt.BorderLayout.PAGE_START);

    respleftpanel.add(respbottomleftpanel, java.awt.BorderLayout.SOUTH);

    respheadpanel.add(respleftpanel, java.awt.BorderLayout.WEST);

    jPanel2.setLayout(new java.awt.GridBagLayout());

    resppropertypanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 10, 1, 10));
    resppropertypanel.setLayout(new java.awt.GridBagLayout());

    l1.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
    l1.setText("Candidate:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    resppropertypanel.add(l1, gridBagConstraints);

    candidatelabel.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    resppropertypanel.add(candidatelabel, gridBagConstraints);

    l2.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
    l2.setText("ID:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    resppropertypanel.add(l2, gridBagConstraints);

    idlabel.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    resppropertypanel.add(idlabel, gridBagConstraints);

    l3.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
    l3.setText("Page:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    resppropertypanel.add(l3, gridBagConstraints);

    pagelabel.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    resppropertypanel.add(pagelabel, gridBagConstraints);

    l4.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
    l4.setText("Source:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    resppropertypanel.add(l4, gridBagConstraints);

    sourcelabel.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
    sourcelabel.setMaximumSize(new java.awt.Dimension(250, 0));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    resppropertypanel.add(sourcelabel, gridBagConstraints);

    l5.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
    l5.setText("Height:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    resppropertypanel.add(l5, gridBagConstraints);

    heightlabel.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    resppropertypanel.add(heightlabel, gridBagConstraints);

    l6.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
    l6.setText("Question:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    resppropertypanel.add(l6, gridBagConstraints);

    questionlabel.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    resppropertypanel.add(questionlabel, gridBagConstraints);

    l7.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
    l7.setText("Needs Review:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    resppropertypanel.add(l7, gridBagConstraints);

    needsreviewlabel.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
    needsreviewlabel.setText("No");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    resppropertypanel.add(needsreviewlabel, gridBagConstraints);

    l8.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
    l8.setText("Examiner Decision:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 7;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    resppropertypanel.add(l8, gridBagConstraints);

    reviewcombobox.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
    reviewcombobox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Not Reviewed", "Confirm", "Override" }));
    reviewcombobox.addItemListener(new java.awt.event.ItemListener()
    {
      public void itemStateChanged(java.awt.event.ItemEvent evt)
      {
        reviewcomboboxItemStateChanged(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 7;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    resppropertypanel.add(reviewcombobox, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.weighty = 0.5;
    jPanel2.add(resppropertypanel, gridBagConstraints);

    respheadpanel.add(jPanel2, java.awt.BorderLayout.CENTER);

    resprightpanel.setLayout(new java.awt.BorderLayout());

    resptoprightpanel.setLayout(new java.awt.BorderLayout());

    nextcandidatebutton.setText("Next");
    nextcandidatebutton.setEnabled(false);
    nextcandidatebutton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        nextcandidatebuttonActionPerformed(evt);
      }
    });
    resptoprightpanel.add(nextcandidatebutton, java.awt.BorderLayout.CENTER);

    resprightpanel.add(resptoprightpanel, java.awt.BorderLayout.NORTH);

    respbottomrightpanel.setLayout(new java.awt.BorderLayout());

    nextquestionbutton.setText("Next");
    nextquestionbutton.setEnabled(false);
    nextquestionbutton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        nextquestionbuttonActionPerformed(evt);
      }
    });
    respbottomrightpanel.add(nextquestionbutton, java.awt.BorderLayout.CENTER);

    resprightpanel.add(respbottomrightpanel, java.awt.BorderLayout.SOUTH);

    respheadpanel.add(resprightpanel, java.awt.BorderLayout.EAST);

    resptoppanel.add(respheadpanel, java.awt.BorderLayout.PAGE_START);

    outcomepanel.setLayout(new java.awt.GridBagLayout());

    jPanel4.setLayout(new java.awt.GridBagLayout());

    jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("Overall Outcomes"));

    overalloutcometable.setModel(new javax.swing.table.DefaultTableModel(
      new Object [][]
      {
        {null, null}
      },
      new String []
      {
        "Title 1", "Title 2"
      }
    ));
    jPanel6.add(overalloutcometable);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
    jPanel4.add(jPanel6, gridBagConstraints);

    jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Question Outcomes"));

    outcometable.setModel(new javax.swing.table.DefaultTableModel(
      new Object [][]
      {
        {null, null}
      },
      new String []
      {
        "Title 1", "Title 2"
      }
    ));
    jPanel5.add(outcometable);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
    jPanel4.add(jPanel5, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.weighty = 0.5;
    outcomepanel.add(jPanel4, gridBagConstraints);

    resptoppanel.add(outcomepanel, java.awt.BorderLayout.CENTER);

    rtab.add(resptoppanel, java.awt.BorderLayout.NORTH);

    responsetable.setModel(new javax.swing.table.DefaultTableModel(
      new Object [][]
      {

      },
      new String []
      {
        "*", "Response", "Enhanced", "Interpreted", "Override"
      }
    )
    {
      Class[] types = new Class []
      {
        java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Boolean.class, java.lang.Boolean.class
      };
      boolean[] canEdit = new boolean []
      {
        false, false, false, false, true
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
    responsetable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
    sp4.setViewportView(responsetable);

    rtab.add(sp4, java.awt.BorderLayout.CENTER);

    tabs.addTab("Responses", rtab);

    getContentPane().add(tabs, java.awt.BorderLayout.CENTER);

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
    configmenuitem.setEnabled(false);
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

    pdfprintmenuitem.setText("Print to PDF");
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

    previewqmenuitem.setText("Preview Question");
    previewqmenuitem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        previewqmenuitemActionPerformed(evt);
      }
    });
    actionmenu.add(previewqmenuitem);

    itemanalysismenuitem.setText("Item Analysis");
    itemanalysismenuitem.setEnabled(false);
    itemanalysismenuitem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        itemanalysismenuitemActionPerformed(evt);
      }
    });
    actionmenu.add(itemanalysismenuitem);
    actionmenu.add(sep2);

    preprocmenuitem.setText("Preprocess Scanned Images...");
    preprocmenuitem.setEnabled(false);
    actionmenu.add(preprocmenuitem);

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
    actionmenu.add(sep3);

    importcanmenuitem.setText("Import Candidates...");
    importcanmenuitem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        importcanmenuitemActionPerformed(evt);
      }
    });
    actionmenu.add(importcanmenuitem);

    expscoresmenuitem.setText("Export Scores...");
    expscoresmenuitem.setEnabled(false);
    expscoresmenuitem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        expscoresmenuitemActionPerformed(evt);
      }
    });
    actionmenu.add(expscoresmenuitem);

    exprepliesmenuitem.setText("Export Replies...");
    exprepliesmenuitem.setEnabled(false);
    actionmenu.add(exprepliesmenuitem);

    expreportmenuitem.setText("Export Report...");
    expreportmenuitem.setEnabled(false);
    actionmenu.add(expreportmenuitem);

    menubar.add(actionmenu);

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
      JOptionPane.
              showMessageDialog( this, "No exam/survey open." );
      return;
    }
    // TODO add your handling code here:
  }//GEN-LAST:event_itemanalysismenuitemActionPerformed

  private void expscoresmenuitemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_expscoresmenuitemActionPerformed
  {//GEN-HEADEREND:event_expscoresmenuitemActionPerformed

    if ( exam == null )
    {
      JOptionPane.
              showMessageDialog( this, "No exam/survey open." );
      return;
    }
    // TODO add your handling code here:
  }//GEN-LAST:event_expscoresmenuitemActionPerformed

  private void openmenuitemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_openmenuitemActionPerformed
  {//GEN-HEADEREND:event_openmenuitemActionPerformed

    if ( !confirmDataLoss( "Are you sure you want to open a different exam/survey?" ) )
    {
      return;
    }

    if ( basefolder == null )
    {
      File homefolder = new File( System.getProperty( "user.home" ) );
      basefolder = new File( homefolder, "qyouti" );
      if ( !basefolder.exists() )
      {
        basefolder.mkdir();
      }
    }
    selectdialog.setBaseFolder( basefolder );
    selectdialog.updateList();
    selectdialog.setExamName( "" );

    //selectdialog.setBaseFolder( );
    selectdialog.setDialogType( ExamSelectDialog.TYPE_OPEN );
    selectdialog.setVisible( true );
  }//GEN-LAST:event_openmenuitemActionPerformed

  private void importcanmenuitemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_importcanmenuitemActionPerformed
  {//GEN-HEADEREND:event_importcanmenuitemActionPerformed

    if ( exam == null )
    {
      JOptionPane.
              showMessageDialog( this, "No exam/survey open." );
      return;
    }

    String lastprintid = exam.getLastPrintID();    
    if ( lastprintid != null && lastprintid.length() != 0 )
    {
      JOptionPane.showMessageDialog( this, "You can't add candidates after the exam/survey has been printed." );
      return;
    }
          
    ImportCandidateDialog dialog = new ImportCandidateDialog( this, true );
    dialog.setExam( exam );
    dialog.setVisible( true );
    ArrayList<CandidateData> list = dialog.getCandidateList();
    if ( list == null || list.size() == 0 )
      return;
    System.out.println( "Importing " + list.size() + " candidates." );
    exam.importCandidates( list );
    exam.save();
  }//GEN-LAST:event_importcanmenuitemActionPerformed

  private void newmenuitemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_newmenuitemActionPerformed
  {//GEN-HEADEREND:event_newmenuitemActionPerformed
    if ( !confirmDataLoss( "Are you sure you want to create a new exam/survey?" ) )
    {
      return;
    }

    if ( basefolder == null )
    {
      File homefolder = new File( System.getProperty( "user.home" ) );
      basefolder = new File( homefolder, "qyouti" );
      if ( !basefolder.exists() )
      {
        basefolder.mkdir();
      }
      selectdialog.setBaseFolder( basefolder );
    }

    selectdialog.setExamName( "" );

    //selectdialog.setBaseFolder( );
    selectdialog.setDialogType( ExamSelectDialog.TYPE_NEW );
    selectdialog.setVisible( true );

  }//GEN-LAST:event_newmenuitemActionPerformed

  private void nextcandidatebuttonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_nextcandidatebuttonActionPerformed
  {//GEN-HEADEREND:event_nextcandidatebuttonActionPerformed
    TableModel model = responsetable.getModel();
    if ( !(model instanceof QuestionData) )
    {
      return;
    }
    QuestionData question = (QuestionData) model;
    CandidateData candidate = question.page.candidate;
    CandidateData other = candidate.nextCandidateData( true );
    if ( other == null )
    {
      gotoQuestion( null );
    }
    gotoQuestion( other.firstQuestionData() );
  }//GEN-LAST:event_nextcandidatebuttonActionPerformed

  private void previouscandidatebuttonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_previouscandidatebuttonActionPerformed
  {//GEN-HEADEREND:event_previouscandidatebuttonActionPerformed
    TableModel model = responsetable.getModel();
    if ( !(model instanceof QuestionData) )
    {
      return;
    }
    QuestionData question = (QuestionData) model;
    CandidateData candidate = question.page.candidate;
    CandidateData other = candidate.previousCandidateData( true );
    if ( other == null )
    {
      gotoQuestion( null );
    }
    gotoQuestion( other.lastQuestionData() );
  }//GEN-LAST:event_previouscandidatebuttonActionPerformed

  private void nextquestionbuttonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_nextquestionbuttonActionPerformed
  {//GEN-HEADEREND:event_nextquestionbuttonActionPerformed

    TableModel model = responsetable.getModel();
    if ( !(model instanceof QuestionData) )
    {
      return;
    }
    QuestionData question = (QuestionData) model;
    gotoQuestion( question.nextQuestionData() );   // TODO add your handling code here:
  }//GEN-LAST:event_nextquestionbuttonActionPerformed

  private void previousquestionbuttonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_previousquestionbuttonActionPerformed
  {//GEN-HEADEREND:event_previousquestionbuttonActionPerformed
    TableModel model = responsetable.getModel();
    if ( !(model instanceof QuestionData) )
    {
      return;
    }
    QuestionData question = (QuestionData) model;
    gotoQuestion( question.previousQuestionData() );    // TODO add your handling code here:
  }//GEN-LAST:event_previousquestionbuttonActionPerformed

  private void previewqmenuitemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_previewqmenuitemActionPerformed
  {//GEN-HEADEREND:event_previewqmenuitemActionPerformed

    if ( exam == null )
    {
      JOptionPane.
              showMessageDialog( this, "No exam/survey open." );
      return;
    }

    QTIElementItem item = null;
    int itemnumber = 0;
    if ( tabs.getSelectedComponent() == qtab )
    {
      int row = questiontable.getSelectedRow();
      if ( row < 0 )
      {
        JOptionPane.
                showMessageDialog( this, "Select a question in the Questions tab to preview." );
        return;
      }

      item = exam.qdefs.qti.getItems().elementAt( row );
      itemnumber = row;
    }
    else if ( tabs.getSelectedComponent() == rtab )
    {
      if ( !(responsetable.getModel() instanceof QuestionData) )
      {
        JOptionPane.
                showMessageDialog( this, "There is no question to view in the responses tab." );
        return;
      }
      QuestionData qd = (QuestionData) responsetable.getModel();
      item = qd.getItem();
    }
    else
    {
      JOptionPane.
              showMessageDialog( this, "Question preview is only available on the Question and Responses tabs." );
      return;
    }

    if ( questiondialog == null )
    {
      try
      {
        questiondialog = new QuestionPreviewDialog( this, true,
                                                    exam.examfile.
                                                    getParentFile().
                                                    getCanonicalFile().toURI(),
                                                    exam );
      }
      catch ( IOException ex )
      {
        Logger.getLogger( QyoutiView.class.getName() ).
                log( Level.SEVERE, null, ex );
      }
      questiondialog.setLocationRelativeTo( this );
    }
    questiondialog.setItem( item, itemnumber );

    questiondialog.setVisible( true );
    Dimension d = questiondialog.getSize();
    // This will trigger centering and resizing to fit document to window
    questiondialog.setSize( d.width - 1, d.height - 1 );

  }//GEN-LAST:event_previewqmenuitemActionPerformed

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
    else if ( tabs.getSelectedComponent() == rtab )
    {
      if ( !(responsetable.getModel() instanceof QuestionData) )
      {
        JOptionPane.
                showMessageDialog( this, "There is no question to edit in the responses tab." );
        return;
      }
      QuestionData qd = (QuestionData) responsetable.getModel();
      item = qd.getItem();
    }
    else
    {
      JOptionPane.
              showMessageDialog( this, "Question edit is only available on the Question and Responses tabs." );
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
      int row = scanstable.getSelectedRow();
      if ( row < 0 )
      {
        JOptionPane.
                showMessageDialog( this, "Select a scan in the Scans tab to edit." );
        return;
      }
      filename = (String) scanstable.getValueAt( row, 1 );
    }
    else if ( tabs.getSelectedComponent() == rtab )
    {
      if ( !(responsetable.getModel() instanceof QuestionData) )
      {
        JOptionPane.
                showMessageDialog( this, "There is no question to edit in the responses tab." );
        return;
      }
      QuestionData qd = (QuestionData) responsetable.getModel();
      filename = qd.page.source;
    }
    else
    {
      JOptionPane.
              showMessageDialog( this, "Scan view is only available on the Scans and Responses tabs." );
      return;
    }

    ImageViewDialog dialog = new ImageViewDialog( this, true );
    dialog.setImage( filename );
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
    exam.save();

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

    exam.clearPages();
    exam.save();
    gotoQuestion( null );
    exam.pagelistmodel.
            fireTableChanged( new TableModelEvent( exam.pagelistmodel ) );
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

    if ( exam.getPageCount() > 0 )
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
    exam.setLastPrintID( null );
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
    exam.setUnsavedChanges( true );
    printstatuslabel.setText( "Printing..." );
    progressbar.setIndeterminate( true );

    //busydialog.setVisible( true );
    PrintThread thread = new PrintThread( exam, examfolder );
    thread.setQyoutiFrame( this );
    thread.start();
  }//GEN-LAST:event_pdfprintmenuitemActionPerformed

  public void pdfPrintComplete()
  {
    tabs.setEnabled( true );
    filemenu.setEnabled( true );
    actionmenu.setEnabled( true );
    progressbar.setIndeterminate( false );
    //busydialog.setVisible( false );
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
      fc.setDialogTitle( "Select directory that contains the scanned images." );
      fc.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
      if ( scanfolder != null )
      {
        fc.setCurrentDirectory( new File( scanfolder ) );
      }
      int returnVal = fc.showOpenDialog( this );

      if ( returnVal != JFileChooser.APPROVE_OPTION )
      {
        return;
      }
      File file = fc.getSelectedFile();
      if ( !file.exists() || !file.isDirectory() )
      {
        return;
      }

      tabs.setSelectedIndex( 2 );
      tabs.setEnabled( false );
      filemenu.setEnabled( false );
      actionmenu.setEnabled( false );
      exam.setUnsavedChanges( true );
      progressbar.setIndeterminate( true );
      ScanTask scantask = new ScanTask( preferences, exam, file, false, false );
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

  private void reviewcomboboxItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_reviewcomboboxItemStateChanged
  {//GEN-HEADEREND:event_reviewcomboboxItemStateChanged

    if ( evt.getStateChange() == ItemEvent.SELECTED )
    {
      int n = reviewcombobox.getSelectedIndex();
      if ( n != currentquestiondata.getExaminerDecision() )
      {
        System.out.println( "examiner decision combo box change." );
        currentquestiondata.setExaminerDecision( n );
        exam.setUnsavedChanges( true );
      }
    }

  }//GEN-LAST:event_reviewcomboboxItemStateChanged

  private void aboutmenuitemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_aboutmenuitemActionPerformed
  {//GEN-HEADEREND:event_aboutmenuitemActionPerformed
    
    AboutDialog dialog = new AboutDialog( this, true );
    dialog.setVisible( true );
    
  }//GEN-LAST:event_aboutmenuitemActionPerformed

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
    exam.setUnsavedChanges( true );
    gotoQuestion( currentquestiondata );
    exam.qdefs.fireTableDataChanged();
  }

  /**
   * Indicates that the question edit dialog stored some changes into its item
   * object. So, the exam file needs saving to disk.
   */
  void examinationBuilt( File folder, String string )
  {
    File file = new File( folder, "qyouti.xml" );
    FileWriter writer = null;
    try
    {
      writer = new FileWriter( file );
      writer.write( string );
    }
    catch ( IOException ex )
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
    exam.setUnsavedChanges( true );
  }

  private void loadExam( File examfolder )
  {
    this.examfolder = examfolder;

    try
    {
      exam = new ExaminationData( examcatalogue, new File( examfolder, "qyouti.xml" ) );
      exam.addExaminationDataStatusListener( this );
      scanstable.setModel( exam.pagelistmodel );
      candidatetable.setModel( exam );
      exam.load();
      setTitle( "Qyouti - " + examfolder.getName() );
      if ( exam.qdefs != null )
      {
        questiontable.setModel( exam.qdefs );
      }

      CandidateData c = exam.getFirstCandidate( true );
      if ( c == null )
      {
        gotoQuestion( null );
      }
      else
      {
        gotoQuestion( c.firstQuestionData() );
      }
    }
    catch ( Exception ex )
    {
      Logger.getLogger( QyoutiView.class.getName() ).
              log( Level.SEVERE, null, ex );
    }

  }

  private void clearResponseTable()
  {
    responsetable.setModel( new javax.swing.table.DefaultTableModel(
            new Object[][]
            {
            },
            new String[]
            {
              "*", "Response", "Enhanced", "Interpretation", "Overide"
            } )
    {

      Class[] types = new Class[]
      {
        java.lang.String.class,
        javax.swing.Icon.class,
        javax.swing.Icon.class,
        java.lang.Boolean.class,
        java.lang.Boolean.class
      };
      boolean[] canEdit = new boolean[]
      {
        false, false, false, false, true
      };

      public Class getColumnClass( int columnIndex )
      {
        return types[columnIndex];
      }

      public boolean isCellEditable( int rowIndex, int columnIndex )
      {
        return canEdit[columnIndex];
      }
    } );
  }

  public void gotoQuestion( QuestionData question )
  {
    int i;

    //System.out.println( "gotoQuestion " + question );
    currentquestiondata = question;
    needsreviewlabel.setOpaque( false );
    if ( question == null )
    {
      clearResponseTable();
      candidatelabel.setText( "" );
      idlabel.setText( "" );
      pagelabel.setText( "" );
      sourcelabel.setText( "" );
      heightlabel.setText( "" );
      questionlabel.setText( "" );
      needsreviewlabel.setText( "" );
      reviewcombobox.setSelectedIndex( 0 );
      reviewcombobox.setEnabled( false );
      outcometable.setModel( new OutcomeData() );
      overalloutcometable.setModel( new OutcomeData() );
      nextquestionbutton.setEnabled( false );
      previousquestionbutton.setEnabled( false );
      nextcandidatebutton.setEnabled( false );
      previouscandidatebutton.setEnabled( false );
      return;
    }

    CandidateData candidate = question.page.candidate;

    candidatelabel.setText( question.page.candidate_name );
    idlabel.setText( question.page.candidate_number );
    pagelabel.setText( Integer.toString( question.page.page_number ) );
    sourcelabel.setText( question.page.source );
    if ( question.page.height != 0.0 )
    {
      heightlabel.setText( Double.toString( question.page.height ) );
    }
    questionlabel.setText( question.ident );
    needsreviewlabel.setText( question.needsreview ? "Yes" : "No" );
    if ( question.needsreview && question.getExaminerDecision() == QuestionData.EXAMINER_DECISION_NONE )
    {
      needsreviewlabel.setOpaque( true );
    }
    reviewcombobox.setEnabled( true );
    reviewcombobox.setSelectedIndex( question.getExaminerDecision() );

    responsetable.setModel( question );
    for ( i=0; i<question.getRowCount(); i++ )
      responsetable.setRowHeight( i, question.getRowHeight( i ) );

    outcometable.setModel( question.outcomes );
    overalloutcometable.setModel( candidate.outcomes );

    QuestionData next, previous;
    next = question.nextQuestionData();
    previous = question.previousQuestionData();
    nextquestionbutton.setEnabled( next != null );
    previousquestionbutton.setEnabled( previous != null );

    CandidateData nextc = (candidate == null) ? null : candidate.
            nextCandidateData( true );
    CandidateData previousc = (candidate == null) ? null : candidate.
            previousCandidateData( true );
    nextcandidatebutton.setEnabled( nextc != null );
    previouscandidatebutton.setEnabled( previousc != null );
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
  private javax.swing.JLabel candidatelabel;
  private javax.swing.JTable candidatetable;
  private javax.swing.JPanel candqpanel;
  private javax.swing.JPanel candqpanelouter;
  private javax.swing.JLabel cdetailerrorlabel;
  private javax.swing.JLabel cdetailidlabel;
  private javax.swing.JLabel cdetailnamelabel;
  private javax.swing.JTable cdetailoutcometable;
  private javax.swing.JPanel cdetailpanel;
  private javax.swing.JMenuItem clearscanneddatamenuitem;
  private javax.swing.JMenuItem configmenuitem;
  private javax.swing.JPanel ctab;
  private javax.swing.JMenuItem editallquestionsmenuitem;
  private javax.swing.JMenuItem editquestionmenuitem;
  private javax.swing.JLabel errorlabel;
  private javax.swing.JMenuItem exitmenuitem;
  private javax.swing.JMenuItem exprepliesmenuitem;
  private javax.swing.JMenuItem expreportmenuitem;
  private javax.swing.JMenuItem expscoresmenuitem;
  private javax.swing.JMenu filemenu;
  private javax.swing.JMenuItem forgetprintmenuitem;
  private javax.swing.JLabel heightlabel;
  private javax.swing.JLabel idlabel;
  private javax.swing.JMenuItem importcanmenuitem;
  private javax.swing.JMenuItem importimagesmenuitem;
  private javax.swing.JMenuItem importqmenuitem;
  private javax.swing.JMenuItem itemanalysismenuitem;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel3;
  private javax.swing.JLabel jLabel6;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JPanel jPanel2;
  private javax.swing.JPanel jPanel3;
  private javax.swing.JPanel jPanel4;
  private javax.swing.JPanel jPanel5;
  private javax.swing.JPanel jPanel6;
  private javax.swing.JPopupMenu.Separator jSeparator1;
  private javax.swing.JSplitPane jSplitPane1;
  private javax.swing.JLabel l1;
  private javax.swing.JLabel l2;
  private javax.swing.JLabel l3;
  private javax.swing.JLabel l4;
  private javax.swing.JLabel l5;
  private javax.swing.JLabel l6;
  private javax.swing.JLabel l7;
  private javax.swing.JLabel l8;
  private javax.swing.JMenuBar menubar;
  private javax.swing.JLabel needsreviewlabel;
  private javax.swing.JMenuItem newmenuitem;
  private javax.swing.JButton nextcandidatebutton;
  private javax.swing.JButton nextquestionbutton;
  private javax.swing.JMenuItem openmenuitem;
  private javax.swing.JPanel outcomepanel;
  private javax.swing.JTable outcometable;
  private javax.swing.JTable overalloutcometable;
  private javax.swing.JLabel pagelabel;
  private javax.swing.JMenuItem pdfprintmenuitem;
  private javax.swing.JMenuItem preprocmenuitem;
  private javax.swing.JMenuItem previewqmenuitem;
  private javax.swing.JButton previouscandidatebutton;
  private javax.swing.JButton previousquestionbutton;
  private javax.swing.JLabel printstatuslabel;
  private javax.swing.JProgressBar progressbar;
  private javax.swing.JMenuItem propsmenuitem;
  private javax.swing.JPanel qtab;
  private javax.swing.JLabel questionlabel;
  private javax.swing.JTable questiontable;
  private javax.swing.JPanel respbottomleftpanel;
  private javax.swing.JPanel respbottomrightpanel;
  private javax.swing.JPanel respheadpanel;
  private javax.swing.JPanel respleftpanel;
  private javax.swing.JTable responsetable;
  private javax.swing.JPanel resppropertypanel;
  private javax.swing.JPanel resprightpanel;
  private javax.swing.JPanel resptopleftpanel;
  private javax.swing.JPanel resptoppanel;
  private javax.swing.JPanel resptoprightpanel;
  private javax.swing.JComboBox<String> reviewcombobox;
  private javax.swing.JPanel rtab;
  private javax.swing.JMenuItem savemenuitem;
  private javax.swing.JLabel savestatuslabel;
  private javax.swing.JTable scanstable;
  private javax.swing.JPopupMenu.Separator sep1;
  private javax.swing.JPopupMenu.Separator sep1b;
  private javax.swing.JPopupMenu.Separator sep2;
  private javax.swing.JPopupMenu.Separator sep3;
  private javax.swing.JLabel sourcelabel;
  private javax.swing.JScrollPane sp1;
  private javax.swing.JScrollPane sp2;
  private javax.swing.JScrollPane sp3;
  private javax.swing.JScrollPane sp4;
  private javax.swing.JScrollPane sp6;
  private javax.swing.JLabel spacerlabel;
  private javax.swing.JSplitPane splitpane;
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
    savestatuslabel.setText( exam.areUnsavedChanges() ? "Unsaved Data" : " " );
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
    CandidateData c = exam.getFirstCandidate( true );
    if ( c == null )
    {
      gotoQuestion( null );
    }
    else
    {
      gotoQuestion( c.firstQuestionData() );
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

  
}