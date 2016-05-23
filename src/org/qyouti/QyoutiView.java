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
 * QyoutiView.java
 */
package org.qyouti;

import org.qyouti.scan.*;
import org.qyouti.QyoutiAboutBox;
import org.qyouti.QyoutiApp;
import org.qyouti.data.*;
import java.io.*;
import java.util.logging.*;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.math.BigInteger;
import java.net.URI;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Vector;
import java.util.zip.*;
import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.print.PrintTranscoder;
import org.apache.fop.pdf.StreamCacheFactory;
import org.apache.fop.svg.PDFTranscoder;
import org.qyouti.dialog.ExamOptionsDialog;
import org.qyouti.dialog.NewExamination;
import org.qyouti.dialog.PreferencesDialog;
import org.qyouti.print.MultiPagePDFTranscoder;
import org.qyouti.print.PrintTask;
import org.qyouti.qrcode.QRCodec;
import org.qyouti.qti1.element.QTIElementItem;
import org.qyouti.qti1.gui.PaginationRecord;
import org.qyouti.qti1.gui.QTIItemRenderer;
import org.qyouti.qti1.gui.QuestionMetricsRecordSet;
import org.qyouti.util.QyoutiUtils;
import org.w3c.dom.svg.SVGDocument;

/**
 * The application's main frame.
 */
public class QyoutiView extends FrameView
{
  File appfolder = null;
  File examfolder = null;
  int selected_exam_index = 0;
  ExaminationCatalogue examcatalogue = null;
  ExaminationData exam = null;
  ScanTask scantask = null;
  PreferencesDialog prefsDialog;
  public QyoutiPreferences preferences;

  String repliesfilename;
  String reportfilename;
  String scanfolder;
  int classsize;

  public QyoutiView(
      QyoutiApp app,
      String basefoldername,
      String examname,
      String repliesfilename,
      String reportfilename,
      String scanfolder,
      int classsize,
      String command,
      boolean exit )
  {
    super(app);

    this.repliesfilename = repliesfilename;
    this.reportfilename = reportfilename;
    this.scanfolder = scanfolder;
    this.classsize = classsize;

    initComponents();

    QRCodec.setDebugImageLabel( this.debugImageLabel );

    if ( basefoldername == null )
    {
      File homefolder = new File(System.getProperty("user.home"));
      if (homefolder == null || !homefolder.exists() || !homefolder.isDirectory())
      {
        throw new IllegalArgumentException("Can't access user home folder.");
      }

      appfolder = new File(homefolder, "qyouti");
    }
    else
      appfolder = new File(basefoldername);

    if (appfolder.exists() && !appfolder.isDirectory())
    {
      throw new IllegalArgumentException("File is named after Qyouti folder.");
    }

    System.out.println( appfolder.toString() );

    if (!appfolder.exists())
    {
      if (!appfolder.mkdir())
      {
        throw new IllegalArgumentException("Couldn't create Qyouti folder.");
      }
    }

    File preferences_file = new File( appfolder, "preferences.xml" );
    preferences = new QyoutiPreferences(preferences_file);
    if ( preferences_file.exists() )
      preferences.load();
    else
      preferences.setDefaults();


    examcatalogue = new ExaminationCatalogue( appfolder );
    String[] names = examcatalogue.getNames();

    examCombo.removeAllItems();
    examCombo.addItem("Select an examination here");
    for (int i = 0; i < names.length; i++)
      examCombo.addItem( names[i] );
    examCombo.setSelectedIndex(0);



    responseTable.setDefaultRenderer(Icon.class, new IconRenderer());
    clearResponseTable();


    // status bar initialization - message timeout, idle icon and busy animation, etc
    ResourceMap resourceMap = getResourceMap();
    int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
    messageTimer = new Timer(messageTimeout, new ActionListener()
    {

      public void actionPerformed(ActionEvent e)
      {
        statusMessageLabel.setText("");
      }
    });
    messageTimer.setRepeats(false);
    int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
    for (int i = 0; i < busyIcons.length; i++)
    {
      busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
    }
    busyIconTimer = new Timer(busyAnimationRate, new ActionListener()
    {

      public void actionPerformed(ActionEvent e)
      {
        busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
        statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
      }
    });
    idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
    statusAnimationLabel.setIcon(idleIcon);
    progressBar.setVisible(false);

    // connecting action tasks to status bar via TaskMonitor
    TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
    taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener()
    {

      public void propertyChange(java.beans.PropertyChangeEvent evt)
      {
        String propertyName = evt.getPropertyName();
        if ("started".equals(propertyName))
        {
          if (!busyIconTimer.isRunning())
          {
            statusAnimationLabel.setIcon(busyIcons[0]);
            busyIconIndex = 0;
            busyIconTimer.start();
          }
          progressBar.setVisible(true);
          progressBar.setIndeterminate(true);
        } else if ("done".equals(propertyName))
        {
          busyIconTimer.stop();
          statusAnimationLabel.setIcon(idleIcon);
          progressBar.setVisible(false);
          progressBar.setValue(0);
        } else if ("message".equals(propertyName))
        {
          String text = (String) (evt.getNewValue());
          statusMessageLabel.setText((text == null) ? "" : text);
          messageTimer.restart();
        } else if ("progress".equals(propertyName))
        {
          int value = (Integer) (evt.getNewValue());
          progressBar.setVisible(true);
          progressBar.setIndeterminate(false);
          progressBar.setValue(value);
        }
      }
    });


    if ( examname != null )
    {
      loadExamName( examname );
      examCombo.setEnabled( false );
      if ( "pdf".equals( command ) )
        examtopdfButtonActionPerformed( null );
      if ( "process".equals( command ) )
         app.setExitCode( importscansCommandLine( null ) );
      if ( exit )
        app.exit();
    }
    else
    {
      if ( "preprocess".equals( command ) )
         app.setExitCode( preprocessCommandLine( null ) );
      if ( exit )
        app.exit();
      showAboutBox();
    }
  }


  private void clearResponseTable()
  {
    responseTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object[][]
            {
            },
            new String[]
            {
              "*", "Response", "Enhanced", "Interpretation", "Overide"
            })
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

      public Class getColumnClass(int columnIndex)
      {
        return types[columnIndex];
      }

      public boolean isCellEditable(int rowIndex, int columnIndex)
      {
        return canEdit[columnIndex];
      }
    });
  }



  public void setVisible( boolean v )
  {

  }


  @Action
  public void showAboutBox()
  {
    if (aboutBox == null)
    {
      JFrame mainFrame = QyoutiApp.getApplication().getMainFrame();
      aboutBox = new QyoutiAboutBox(mainFrame);
      aboutBox.setLocationRelativeTo(mainFrame);
    }
    //QyoutiApp.getApplication().show(aboutBox);
  }

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
  private void initComponents() {

    menuBar = new javax.swing.JMenuBar();
    javax.swing.JMenu fileMenu = new javax.swing.JMenu();
    saveMenuItem = new javax.swing.JMenuItem();
    prefsMenuItem = new javax.swing.JMenuItem();
    jSeparator1 = new javax.swing.JSeparator();
    javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
    javax.swing.JMenu helpMenu = new javax.swing.JMenu();
    javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
    statusPanel = new javax.swing.JPanel();
    javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
    statusMessageLabel = new javax.swing.JLabel();
    statusAnimationLabel = new javax.swing.JLabel();
    progressBar = new javax.swing.JProgressBar();
    centrePanel = new javax.swing.JPanel();
    tabbedPane = new javax.swing.JTabbedPane();
    examTabPanel = new javax.swing.JPanel();
    jPanel13 = new javax.swing.JPanel();
    jLabel2 = new javax.swing.JLabel();
    examCombo = new javax.swing.JComboBox();
    newExamButton = new javax.swing.JButton();
    jPanel17 = new javax.swing.JPanel();
    optionsButton = new javax.swing.JToggleButton();
    printexamButton = new javax.swing.JButton();
    examtopdfButton = new javax.swing.JButton();
    previewexamButton = new javax.swing.JButton();
    jPanel18 = new javax.swing.JPanel();
    debugImageLabel = new javax.swing.JLabel();
    questionTabPanel = new javax.swing.JPanel();
    jPanel15 = new javax.swing.JPanel();
    importQuestionsButton = new javax.swing.JButton();
    questionPreviewButton = new javax.swing.JButton();
    itemAnalysisButton = new javax.swing.JButton();
    jPanel16 = new javax.swing.JPanel();
    jScrollPane3 = new javax.swing.JScrollPane();
    questionTable = new javax.swing.JTable();
    scansTabPanel = new javax.swing.JPanel();
    jPanel19 = new javax.swing.JPanel();
    preprocessscansButton = new javax.swing.JButton();
    importscansButton = new javax.swing.JButton();
    clearresponsesbutton = new javax.swing.JButton();
    jPanel20 = new javax.swing.JPanel();
    jScrollPane4 = new javax.swing.JScrollPane();
    scanstable = new javax.swing.JTable();
    candidateTabPanel = new javax.swing.JPanel();
    jScrollPane1 = new javax.swing.JScrollPane();
    candidateTable = new javax.swing.JTable();
    jPanel14 = new javax.swing.JPanel();
    importCandidatesButton = new javax.swing.JButton();
    exportScoresButton = new javax.swing.JButton();
    exportRepliesButton = new javax.swing.JButton();
    exportReportButton = new javax.swing.JButton();
    responseTabPanel = new javax.swing.JPanel();
    jPanel2 = new javax.swing.JPanel();
    jPanel8 = new javax.swing.JPanel();
    jPanel5 = new javax.swing.JPanel();
    jPanel3 = new javax.swing.JPanel();
    jLabel1 = new javax.swing.JLabel();
    label_candidate_name = new javax.swing.JLabel();
    jLabel3 = new javax.swing.JLabel();
    label_candidate_number = new javax.swing.JLabel();
    jLabel5 = new javax.swing.JLabel();
    label_page_number = new javax.swing.JLabel();
    jLabel6 = new javax.swing.JLabel();
    label_page_source = new javax.swing.JLabel();
    jLabel8 = new javax.swing.JLabel();
    label_page_height = new javax.swing.JLabel();
    jPanel6 = new javax.swing.JPanel();
    previousCandidateButton = new javax.swing.JButton();
    jPanel7 = new javax.swing.JPanel();
    nextCandidateButton = new javax.swing.JButton();
    jPanel9 = new javax.swing.JPanel();
    jPanel10 = new javax.swing.JPanel();
    previousResponseButton = new javax.swing.JButton();
    jPanel11 = new javax.swing.JPanel();
    jLabel7 = new javax.swing.JLabel();
    label_question_id = new javax.swing.JLabel();
    jPanel12 = new javax.swing.JPanel();
    nextResponseButton = new javax.swing.JButton();
    jPanel4 = new javax.swing.JPanel();
    outcometable = new javax.swing.JTable();
    jPanel1 = new javax.swing.JPanel();
    jScrollPane2 = new javax.swing.JScrollPane();
    responseTable = new javax.swing.JTable();
    jPanel21 = new javax.swing.JPanel();
    examnamelabel = new javax.swing.JLabel();

    menuBar.setName("menuBar"); // NOI18N

    org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(org.qyouti.QyoutiApp.class).getContext().getResourceMap(QyoutiView.class);
    fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
    fileMenu.setName("fileMenu"); // NOI18N

    saveMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
    saveMenuItem.setMnemonic('S');
    saveMenuItem.setText(resourceMap.getString("saveMenuItem.text")); // NOI18N
    saveMenuItem.setName("saveMenuItem"); // NOI18N
    saveMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        saveMenuItemActionPerformed(evt);
      }
    });
    fileMenu.add(saveMenuItem);

    prefsMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_MASK));
    prefsMenuItem.setMnemonic('P');
    prefsMenuItem.setText(resourceMap.getString("prefsMenuItem.text")); // NOI18N
    prefsMenuItem.setName("prefsMenuItem"); // NOI18N
    prefsMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        prefsMenuItemActionPerformed(evt);
      }
    });
    fileMenu.add(prefsMenuItem);

    jSeparator1.setName("jSeparator1"); // NOI18N
    fileMenu.add(jSeparator1);

    javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(org.qyouti.QyoutiApp.class).getContext().getActionMap(QyoutiView.class, this);
    exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
    exitMenuItem.setName("exitMenuItem"); // NOI18N
    fileMenu.add(exitMenuItem);

    menuBar.add(fileMenu);

    helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
    helpMenu.setName("helpMenu"); // NOI18N

    aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
    aboutMenuItem.setName("aboutMenuItem"); // NOI18N
    helpMenu.add(aboutMenuItem);

    menuBar.add(helpMenu);

    statusPanel.setName("statusPanel"); // NOI18N

    statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

    statusMessageLabel.setName("statusMessageLabel"); // NOI18N

    statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

    progressBar.setName("progressBar"); // NOI18N

    javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
    statusPanel.setLayout(statusPanelLayout);
    statusPanelLayout.setHorizontalGroup(
      statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 724, Short.MAX_VALUE)
      .addGroup(statusPanelLayout.createSequentialGroup()
        .addContainerGap()
        .addComponent(statusMessageLabel)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 540, Short.MAX_VALUE)
        .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(statusAnimationLabel)
        .addContainerGap())
    );
    statusPanelLayout.setVerticalGroup(
      statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(statusPanelLayout.createSequentialGroup()
        .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(statusMessageLabel)
          .addComponent(statusAnimationLabel)
          .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addGap(3, 3, 3))
    );

    centrePanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
    centrePanel.setName("centrePanel"); // NOI18N
    centrePanel.setLayout(new java.awt.BorderLayout());

    tabbedPane.setName("tabbedPane"); // NOI18N

    examTabPanel.setName("examTabPanel"); // NOI18N
    examTabPanel.setLayout(new java.awt.BorderLayout(8, 8));

    jPanel13.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
    jPanel13.setName("jPanel13"); // NOI18N
    jPanel13.setLayout(new java.awt.BorderLayout(8, 8));

    jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
    jLabel2.setName("jLabel2"); // NOI18N
    jPanel13.add(jLabel2, java.awt.BorderLayout.WEST);

    examCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
    examCombo.setName("examCombo"); // NOI18N
    examCombo.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(java.awt.event.ItemEvent evt) {
        examComboItemStateChanged(evt);
      }
    });
    jPanel13.add(examCombo, java.awt.BorderLayout.CENTER);

    newExamButton.setText(resourceMap.getString("newExamButton.text")); // NOI18N
    newExamButton.setName("newExamButton"); // NOI18N
    newExamButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        newExamButtonActionPerformed(evt);
      }
    });
    jPanel13.add(newExamButton, java.awt.BorderLayout.EAST);

    examTabPanel.add(jPanel13, java.awt.BorderLayout.NORTH);

    jPanel17.setName("jPanel17"); // NOI18N
    jPanel17.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 25, 5));

    optionsButton.setText(resourceMap.getString("optionsButton.text")); // NOI18N
    optionsButton.setName("optionsButton"); // NOI18N
    optionsButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        optionsButtonActionPerformed(evt);
      }
    });
    jPanel17.add(optionsButton);

    printexamButton.setText(resourceMap.getString("printexamButton.text")); // NOI18N
    printexamButton.setName("printexamButton"); // NOI18N
    printexamButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        //printexamButtonActionPerformed(evt);
      }
    });
    jPanel17.add(printexamButton);

    examtopdfButton.setText(resourceMap.getString("examtopdfButton.text")); // NOI18N
    examtopdfButton.setName("examtopdfButton"); // NOI18N
    examtopdfButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        examtopdfButtonActionPerformed(evt);
      }
    });
    jPanel17.add(examtopdfButton);

    previewexamButton.setText(resourceMap.getString("previewexamButton.text")); // NOI18N
    previewexamButton.setName("previewexamButton"); // NOI18N
    previewexamButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        previewexamButtonActionPerformed(evt);
      }
    });
    jPanel17.add(previewexamButton);

    examTabPanel.add(jPanel17, java.awt.BorderLayout.CENTER);

    jPanel18.setName("jPanel18"); // NOI18N

    debugImageLabel.setText(resourceMap.getString("debugImageLabel.text")); // NOI18N
    debugImageLabel.setAutoscrolls(true);
    debugImageLabel.setName("debugImageLabel"); // NOI18N
    jPanel18.add(debugImageLabel);

    examTabPanel.add(jPanel18, java.awt.BorderLayout.SOUTH);

    tabbedPane.addTab(resourceMap.getString("examTabPanel.TabConstraints.tabTitle"), examTabPanel); // NOI18N

    questionTabPanel.setName("questionTabPanel"); // NOI18N
    questionTabPanel.setLayout(new java.awt.BorderLayout());

    jPanel15.setName("jPanel15"); // NOI18N

    importQuestionsButton.setText(resourceMap.getString("importQuestionsButton.text")); // NOI18N
    importQuestionsButton.setName("importQuestionsButton"); // NOI18N
    importQuestionsButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        importQuestionsButtonActionPerformed(evt);
      }
    });
    jPanel15.add(importQuestionsButton);

    questionPreviewButton.setText(resourceMap.getString("questionPreviewButton.text")); // NOI18N
    questionPreviewButton.setName("questionPreviewButton"); // NOI18N
    questionPreviewButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        questionPreviewButtonActionPerformed(evt);
      }
    });
    jPanel15.add(questionPreviewButton);

    itemAnalysisButton.setText(resourceMap.getString("itemAnalysisButton.text")); // NOI18N
    itemAnalysisButton.setName("itemAnalysisButton"); // NOI18N
    itemAnalysisButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        itemAnalysisButtonActionPerformed(evt);
      }
    });
    jPanel15.add(itemAnalysisButton);

    questionTabPanel.add(jPanel15, java.awt.BorderLayout.NORTH);

    jPanel16.setName("jPanel16"); // NOI18N
    jPanel16.setLayout(new java.awt.BorderLayout());

    jScrollPane3.setName("jScrollPane3"); // NOI18N

    questionTable.setModel(new javax.swing.table.DefaultTableModel(
      new Object [][] {

      },
      new String [] {
        "ID", "Title"
      }
    ) {
      boolean[] canEdit = new boolean [] {
        false, false
      };

      public boolean isCellEditable(int rowIndex, int columnIndex) {
        return canEdit [columnIndex];
      }
    });
    questionTable.setName("questionTable"); // NOI18N
    jScrollPane3.setViewportView(questionTable);

    jPanel16.add(jScrollPane3, java.awt.BorderLayout.CENTER);

    questionTabPanel.add(jPanel16, java.awt.BorderLayout.CENTER);

    tabbedPane.addTab(resourceMap.getString("questionTabPanel.TabConstraints.tabTitle"), questionTabPanel); // NOI18N

    scansTabPanel.setName("scansTabPanel"); // NOI18N
    scansTabPanel.setLayout(new java.awt.BorderLayout());

    jPanel19.setName("jPanel19"); // NOI18N

    preprocessscansButton.setText(resourceMap.getString("preprocessscansButton.text")); // NOI18N
    preprocessscansButton.setName("preprocessscansButton"); // NOI18N
    preprocessscansButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        preprocessscansButtonActionPerformed(evt);
      }
    });
    jPanel19.add(preprocessscansButton);

    importscansButton.setText(resourceMap.getString("importscansButton.text")); // NOI18N
    importscansButton.setName("importscansButton"); // NOI18N
    importscansButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        importscansButtonActionPerformed(evt);
      }
    });
    jPanel19.add(importscansButton);

    clearresponsesbutton.setText(resourceMap.getString("clearresponsesbutton.text")); // NOI18N
    clearresponsesbutton.setName("clearresponsesbutton"); // NOI18N
    clearresponsesbutton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        clearresponsesbuttonActionPerformed(evt);
      }
    });
    jPanel19.add(clearresponsesbutton);

    scansTabPanel.add(jPanel19, java.awt.BorderLayout.NORTH);

    jPanel20.setName("jPanel20"); // NOI18N
    jPanel20.setLayout(new java.awt.BorderLayout());

    jScrollPane4.setName("jScrollPane4"); // NOI18N

    scanstable.setModel(new javax.swing.table.DefaultTableModel(
      new Object [][] {

      },
      new String [] {
        "No.", "File", "Code", "Error"
      }
    ) {
      Class[] types = new Class [] {
        java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
      };
      boolean[] canEdit = new boolean [] {
        false, false, false, false
      };

      public Class getColumnClass(int columnIndex) {
        return types [columnIndex];
      }

      public boolean isCellEditable(int rowIndex, int columnIndex) {
        return canEdit [columnIndex];
      }
    });
    scanstable.setName("scanstable"); // NOI18N
    jScrollPane4.setViewportView(scanstable);

    jPanel20.add(jScrollPane4, java.awt.BorderLayout.CENTER);

    scansTabPanel.add(jPanel20, java.awt.BorderLayout.CENTER);

    tabbedPane.addTab(resourceMap.getString("scansTabPanel.TabConstraints.tabTitle"), scansTabPanel); // NOI18N

    candidateTabPanel.setName("candidateTabPanel"); // NOI18N
    candidateTabPanel.setLayout(new java.awt.BorderLayout());

    jScrollPane1.setName("jScrollPane1"); // NOI18N

    candidateTable.setModel(new javax.swing.table.DefaultTableModel(
      new Object [][] {

      },
      new String [] {
        "*", "Name", "ID", "Pages", "Questions", "Errors"
      }
    ) {
      Class[] types = new Class [] {
        java.lang.Object.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class
      };
      boolean[] canEdit = new boolean [] {
        false, false, false, false, false, false
      };

      public Class getColumnClass(int columnIndex) {
        return types [columnIndex];
      }

      public boolean isCellEditable(int rowIndex, int columnIndex) {
        return canEdit [columnIndex];
      }
    });
    candidateTable.setName("candidateTable"); // NOI18N
    jScrollPane1.setViewportView(candidateTable);

    candidateTabPanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);

    jPanel14.setName("jPanel14"); // NOI18N

    importCandidatesButton.setText(resourceMap.getString("importCandidatesButton.text")); // NOI18N
    importCandidatesButton.setName("importCandidatesButton"); // NOI18N
    importCandidatesButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        importCandidatesButtonActionPerformed(evt);
      }
    });
    jPanel14.add(importCandidatesButton);

    exportScoresButton.setText(resourceMap.getString("exportScoresButton.text")); // NOI18N
    exportScoresButton.setName("exportScoresButton"); // NOI18N
    exportScoresButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        exportScoresButtonActionPerformed(evt);
      }
    });
    jPanel14.add(exportScoresButton);

    exportRepliesButton.setText(resourceMap.getString("exportRepliesButton.text")); // NOI18N
    exportRepliesButton.setName("exportRepliesButton"); // NOI18N
    exportRepliesButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        exportRepliesButtonActionPerformed(evt);
      }
    });
    jPanel14.add(exportRepliesButton);

    exportReportButton.setText(resourceMap.getString("exportReportButton.text")); // NOI18N
    exportReportButton.setName("exportReportButton"); // NOI18N
    exportReportButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        exportReportButtonActionPerformed(evt);
      }
    });
    jPanel14.add(exportReportButton);

    candidateTabPanel.add(jPanel14, java.awt.BorderLayout.NORTH);

    tabbedPane.addTab(resourceMap.getString("candidateTabPanel.TabConstraints.tabTitle"), candidateTabPanel); // NOI18N

    responseTabPanel.setName("responseTabPanel"); // NOI18N
    responseTabPanel.setLayout(new java.awt.BorderLayout());

    jPanel2.setName("jPanel2"); // NOI18N
    jPanel2.setLayout(new java.awt.BorderLayout());

    jPanel8.setName("jPanel8"); // NOI18N
    jPanel8.setLayout(new java.awt.BorderLayout());

    jPanel5.setName("jPanel5"); // NOI18N
    jPanel5.setLayout(new java.awt.BorderLayout());

    jPanel3.setName("jPanel3"); // NOI18N
    jPanel3.setLayout(new java.awt.GridLayout(5, 2, 12, 0));

    jLabel1.setFont(resourceMap.getFont("jLabel1.font")); // NOI18N
    jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
    jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
    jLabel1.setName("jLabel1"); // NOI18N
    jPanel3.add(jLabel1);

    label_candidate_name.setText(resourceMap.getString("label_candidate_name.text")); // NOI18N
    label_candidate_name.setName("label_candidate_name"); // NOI18N
    jPanel3.add(label_candidate_name);

    jLabel3.setFont(resourceMap.getFont("jLabel7.font")); // NOI18N
    jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
    jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
    jLabel3.setName("jLabel3"); // NOI18N
    jPanel3.add(jLabel3);

    label_candidate_number.setText(resourceMap.getString("label_candidate_number.text")); // NOI18N
    label_candidate_number.setName("label_candidate_number"); // NOI18N
    jPanel3.add(label_candidate_number);

    jLabel5.setFont(resourceMap.getFont("jLabel5.font")); // NOI18N
    jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
    jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
    jLabel5.setName("jLabel5"); // NOI18N
    jPanel3.add(jLabel5);

    label_page_number.setText(resourceMap.getString("label_page_number.text")); // NOI18N
    label_page_number.setName("label_page_number"); // NOI18N
    jPanel3.add(label_page_number);

    jLabel6.setFont(resourceMap.getFont("jLabel6.font")); // NOI18N
    jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
    jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
    jLabel6.setName("jLabel6"); // NOI18N
    jPanel3.add(jLabel6);

    label_page_source.setName("label_page_source"); // NOI18N
    jPanel3.add(label_page_source);

    jLabel8.setFont(resourceMap.getFont("jLabel8.font")); // NOI18N
    jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
    jLabel8.setText(resourceMap.getString("jLabel8.text")); // NOI18N
    jLabel8.setName("jLabel8"); // NOI18N
    jPanel3.add(jLabel8);

    label_page_height.setName("label_page_height"); // NOI18N
    jPanel3.add(label_page_height);

    jPanel5.add(jPanel3, java.awt.BorderLayout.CENTER);

    jPanel6.setName("jPanel6"); // NOI18N

    previousCandidateButton.setText(resourceMap.getString("previousCandidateButton.text")); // NOI18N
    previousCandidateButton.setEnabled(false);
    previousCandidateButton.setName("previousCandidateButton"); // NOI18N
    previousCandidateButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        previousCandidateButtonActionPerformed(evt);
      }
    });
    jPanel6.add(previousCandidateButton);

    jPanel5.add(jPanel6, java.awt.BorderLayout.WEST);

    jPanel7.setName("jPanel7"); // NOI18N

    nextCandidateButton.setText(resourceMap.getString("nextCandidateButton.text")); // NOI18N
    nextCandidateButton.setEnabled(false);
    nextCandidateButton.setName("nextCandidateButton"); // NOI18N
    nextCandidateButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        nextCandidateButtonActionPerformed(evt);
      }
    });
    jPanel7.add(nextCandidateButton);

    jPanel5.add(jPanel7, java.awt.BorderLayout.EAST);

    jPanel8.add(jPanel5, java.awt.BorderLayout.NORTH);

    jPanel9.setName("jPanel9"); // NOI18N
    jPanel9.setLayout(new java.awt.BorderLayout());

    jPanel10.setName("jPanel10"); // NOI18N

    previousResponseButton.setText(resourceMap.getString("previousResponseButton.text")); // NOI18N
    previousResponseButton.setEnabled(false);
    previousResponseButton.setName("previousResponseButton"); // NOI18N
    previousResponseButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        previousResponseButtonActionPerformed(evt);
      }
    });
    jPanel10.add(previousResponseButton);

    jPanel9.add(jPanel10, java.awt.BorderLayout.WEST);

    jPanel11.setName("jPanel11"); // NOI18N
    jPanel11.setLayout(new java.awt.GridLayout(1, 2));

    jLabel7.setFont(resourceMap.getFont("jLabel7.font")); // NOI18N
    jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
    jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
    jLabel7.setName("jLabel7"); // NOI18N
    jPanel11.add(jLabel7);

    label_question_id.setText(resourceMap.getString("label_question_id.text")); // NOI18N
    label_question_id.setName("label_question_id"); // NOI18N
    jPanel11.add(label_question_id);

    jPanel9.add(jPanel11, java.awt.BorderLayout.CENTER);

    jPanel12.setName("jPanel12"); // NOI18N

    nextResponseButton.setText(resourceMap.getString("nextResponseButton.text")); // NOI18N
    nextResponseButton.setEnabled(false);
    nextResponseButton.setName("nextResponseButton"); // NOI18N
    nextResponseButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        nextResponseButtonActionPerformed(evt);
      }
    });
    jPanel12.add(nextResponseButton);

    jPanel9.add(jPanel12, java.awt.BorderLayout.EAST);

    jPanel8.add(jPanel9, java.awt.BorderLayout.CENTER);

    jPanel2.add(jPanel8, java.awt.BorderLayout.NORTH);

    jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, resourceMap.getString("jPanel4.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, resourceMap.getFont("jPanel4.border.titleFont"))); // NOI18N
    jPanel4.setName("jPanel4"); // NOI18N
    jPanel4.setLayout(new java.awt.BorderLayout());

    outcometable.setModel(new javax.swing.table.DefaultTableModel(
      new Object [][] {
        {null, null}
      },
      new String [] {
        "Title 1", "Title 2"
      }
    ) {
      Class[] types = new Class [] {
        java.lang.String.class, java.lang.String.class
      };

      public Class getColumnClass(int columnIndex) {
        return types [columnIndex];
      }
    });
    outcometable.setName("outcometable"); // NOI18N
    jPanel4.add(outcometable, java.awt.BorderLayout.PAGE_START);

    jPanel2.add(jPanel4, java.awt.BorderLayout.CENTER);

    responseTabPanel.add(jPanel2, java.awt.BorderLayout.NORTH);

    jPanel1.setName("jPanel1"); // NOI18N
    jPanel1.setLayout(new java.awt.BorderLayout());

    jScrollPane2.setName("jScrollPane2"); // NOI18N

    responseTable.setModel(new javax.swing.table.DefaultTableModel(
      new Object [][] {

      },
      new String [] {
        "*", "Response", "Enhanced", "Interpretation", "Overide"
      }
    ) {
      Class[] types = new Class [] {
        java.lang.String.class, java.lang.Object.class, java.lang.Object.class, java.lang.String.class, java.lang.Object.class
      };
      boolean[] canEdit = new boolean [] {
        false, false, false, false, true
      };

      public Class getColumnClass(int columnIndex) {
        return types [columnIndex];
      }

      public boolean isCellEditable(int rowIndex, int columnIndex) {
        return canEdit [columnIndex];
      }
    });
    responseTable.setName("responseTable"); // NOI18N
    responseTable.setRowHeight(58);
    responseTable.setRowMargin(4);
    jScrollPane2.setViewportView(responseTable);

    jPanel1.add(jScrollPane2, java.awt.BorderLayout.CENTER);

    responseTabPanel.add(jPanel1, java.awt.BorderLayout.CENTER);

    tabbedPane.addTab(resourceMap.getString("responseTabPanel.TabConstraints.tabTitle"), responseTabPanel); // NOI18N

    centrePanel.add(tabbedPane, java.awt.BorderLayout.CENTER);

    jPanel21.setName("jPanel21"); // NOI18N
    jPanel21.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 12, 16));

    examnamelabel.setText(resourceMap.getString("examnamelabel.text")); // NOI18N
    examnamelabel.setName("examnamelabel"); // NOI18N
    jPanel21.add(examnamelabel);

    centrePanel.add(jPanel21, java.awt.BorderLayout.NORTH);

    setComponent(centrePanel);
    setMenuBar(menuBar);
    setStatusBar(statusPanel);
  }// </editor-fold>                        

    private void previousResponseButtonActionPerformed(java.awt.event.ActionEvent evt)                                                       
    {                                                           

      TableModel model = responseTable.getModel();
      if (!(model instanceof QuestionData))
      {
        return;
      }
      QuestionData question = (QuestionData) model;
      gotoQuestion(question.previousQuestionData());
    }                                                      

    private void nextResponseButtonActionPerformed(java.awt.event.ActionEvent evt)                                                   
    {                                                       
      TableModel model = responseTable.getModel();
      if (!(model instanceof QuestionData))
      {
        return;
      }
      QuestionData question = (QuestionData) model;
      gotoQuestion(question.nextQuestionData());

    }                                                  

    private void previousCandidateButtonActionPerformed(java.awt.event.ActionEvent evt)                                                        
    {                                                            
      TableModel model = responseTable.getModel();
      if (!(model instanceof QuestionData))
      {
        return;
      }
      QuestionData question = (QuestionData) model;
      CandidateData candidate = question.page.candidate;
      CandidateData other = candidate.previousCandidateData(true);
      if (other == null)
      {
        gotoQuestion(null);
      }
      gotoQuestion(other.lastQuestionData());
    }                                                       

    private void nextCandidateButtonActionPerformed(java.awt.event.ActionEvent evt)                                                    
    {                                                        
      TableModel model = responseTable.getModel();
      if (!(model instanceof QuestionData))
      {
        return;
      }
      QuestionData question = (QuestionData) model;
      CandidateData candidate = question.page.candidate;
      CandidateData other = candidate.nextCandidateData(true);
      if (other == null)
      {
        gotoQuestion(null);
      }
      gotoQuestion(other.firstQuestionData());

    }                                                   

    private void newExamButtonActionPerformed(java.awt.event.ActionEvent evt)                                              
    {                                                  
      JFrame mainFrame = QyoutiApp.getApplication().getMainFrame();

      if (exam != null)
      {
        JOptionPane.showMessageDialog(mainFrame, "There are unsaved changes in the currently selected exam - you can't create a new one.");
        return;
      }

      if (newExamDialog == null)
      {
        newExamDialog = new NewExamination(mainFrame, true, appfolder);
        newExamDialog.setLocationRelativeTo(mainFrame);
      }
      QyoutiApp.getApplication().show(newExamDialog);
      File folder = newExamDialog.getExamFolder();
      String name = folder.getName();
      if (folder != null)
      {
        System.out.println("Creating new examination " + folder);
        examCombo.addItem(name);
        examCombo.setSelectedItem(name);
        examfolder = folder;

        exam = new ExaminationData(examcatalogue, new File(examfolder, "qyouti.xml"));
        if (!exam.save())
        {
          JOptionPane.showMessageDialog(mainFrame, "It was not possible to save data.");
        }
      }

    }                                             


    private void loadExamName( String examname )
    {

      File newexamfolder = new File(appfolder, examname);
      try
      {
        examfolder = newexamfolder;
        exam = new ExaminationData(examcatalogue, new File(examfolder, "qyouti.xml"));
        scanstable.setModel( exam.pagelistmodel );
        candidateTable.setModel(exam);
        exam.load();
        examnamelabel.setText( "Exam/Survey - " + examname );
        if (exam.qdefs != null)
        {
          questionTable.setModel(exam.qdefs);
        }
        gotoQuestion( null );
        for (int i = 0; i < exam.candidates_sorted.size(); i++)
        {
          //System.out.println( "Checking candidate " + exam.candidates_sorted.get( i ).name );
          for (int j = 0; j < exam.candidates_sorted.get(i).pages.size(); j++)
          {
            //System.out.println( "Checking page " + exam.candidates_sorted.get( i ).pages.get(j).source );
            if (exam.candidates_sorted.get(i).pages.get(j).questions.size() > 0)
            {
              //System.out.println( "Found first marked question " + exam.candidates_sorted.get(i).pages.get(j).questions.firstElement().ident );
              gotoQuestion(exam.candidates_sorted.get(i).pages.get(j).questions.firstElement());
              return;
            }
          }
        }
      } catch (Exception ex)
      {
        Logger.getLogger(QyoutiView.class.getName()).log(Level.SEVERE, null, ex);
      }

    }

    private void examComboItemStateChanged(java.awt.event.ItemEvent evt)                                           
    {                                               

      JFrame mainFrame = QyoutiApp.getApplication().getMainFrame();

      int new_selected_exam_index = examCombo.getSelectedIndex();
      if (new_selected_exam_index < 0)
      {
        return;
      }

      if (selected_exam_index == new_selected_exam_index)
      {
        return;
      }

      System.out.println("Selected exam changed. " + new_selected_exam_index);

      if (exam != null)
      {
        JOptionPane.showMessageDialog(mainFrame, "Sorry you can't select another exam. Please restart the software.");
        examCombo.setSelectedIndex(selected_exam_index);
        return;
      }

      selected_exam_index = new_selected_exam_index;
      if (new_selected_exam_index == 0)
      {
        JOptionPane.showMessageDialog(mainFrame, "Sorry you can't unselect the exam.  Please restart the software.");
        examfolder = null;
        exam = null;
//        scanstable.setModel( null );
//        candidateTable.setModel(null);
//        questionTable.setModel(null);
        return;
      }

      loadExamName( examCombo.getSelectedItem().toString() );
    }                                          

    private void importCandidatesButtonActionPerformed(java.awt.event.ActionEvent evt)                                                       
    {                                                           
      JFrame mainFrame = QyoutiApp.getApplication().getMainFrame();

      if (exam == null)
      {
        JOptionPane.showMessageDialog(mainFrame, "You need to select or create an examination before importing candidates.");
        return;
      }

      if (!exam.candidates.isEmpty())
      {
        JOptionPane.showMessageDialog(mainFrame, "You can't import candidates when there are already candidates listing in the selected exam.");
        return;
      }

      try
      {
        final JFileChooser fc = new JFileChooser();
        fc.addChoosableFileFilter(new javax.swing.filechooser.FileFilter()
        {

          @Override
          public boolean accept(File f)
          {
            if (f.isDirectory())
            {
              return true;
            }
            return f.getName().toLowerCase().endsWith(".csv");
          }

          @Override
          public String getDescription()
          {
            return "Comma Separated Value files.";
          }
        });


        int returnVal = fc.showOpenDialog(mainFrame);

        if (returnVal != JFileChooser.APPROVE_OPTION)
        {
          return;
        }
        File file = fc.getSelectedFile();
        exam.importCsvCandidates( file );
        exam.save();
      }
      catch (Exception ex)
      {
        exam.candidates.clear();
        exam.candidates_sorted.clear();
        ex.printStackTrace();
        JOptionPane.showMessageDialog(mainFrame, "Technical error importing candidate list.");
      }

}                                                      

    private void importQuestionsButtonActionPerformed(java.awt.event.ActionEvent evt)                                                      
    {                                                          

      JFrame mainFrame = QyoutiApp.getApplication().getMainFrame();

      if (exam == null)
      {
        JOptionPane.showMessageDialog(mainFrame, "You need to select or create an examination before importing questions.");
        return;
      }

      if ( exam.qdefs != null && exam.qdefs.qti != null )
      {
        JOptionPane.showMessageDialog(mainFrame, "You can't import questions when there are already questions in the selected exam.");
        return;
      }

      try
      {
        final JFileChooser fc = new JFileChooser();
        fc.addChoosableFileFilter(new javax.swing.filechooser.FileFilter()
        {

          @Override
          public boolean accept(File f)
          {
            if (f.isDirectory())
            {
              return true;
            }
            return f.getName().toLowerCase().endsWith(".xml");
          }

          @Override
          public String getDescription()
          {
            return "Questions in standalone file (XML)";
          }
        });

        fc.addChoosableFileFilter(new javax.swing.filechooser.FileFilter()
        {

          @Override
          public boolean accept(File f)
          {
            if (f.isDirectory())
            {
              return true;
            }
            return f.getName().toLowerCase().endsWith(".zip");
          }

          @Override
          public String getDescription()
          {
            return "Questions bundled in IMS content package (ZIP)";
          }
        });

        int returnVal = fc.showOpenDialog(mainFrame);

        if (returnVal != JFileChooser.APPROVE_OPTION)
        {
          return;
        }

        File file = fc.getSelectedFile();
        File importfolder = new File(examfolder, "importedquestions");
        if ( file.getName().toLowerCase().endsWith( ".txt" ) )
        {
          exam.importQuestionsFromPlainText(file,importfolder);          
        }
        else if ( file.getName().toLowerCase().endsWith( ".zip" ) )
        {
          try
          {
            QyoutiUtils.unpackZip(file, importfolder);
          } catch (Exception ze)
          {
            JOptionPane.showMessageDialog(mainFrame, "Unable to read the selected file - corrupted or not a ZIP archive.");
            return;
          }
          exam.importQuestionsFromPackage(new File(examfolder, "importedquestions/imsmanifest.xml"));
        }
        else
        {
          exam.importQuestionsFromQTI(file.getParentFile(), file);
        }
        

        exam.save();
        questionTable.setModel(exam.qdefs);
      } catch (Exception ex)
      {
        ex.printStackTrace();
        exam.qdefs = null;
        JOptionPane.showMessageDialog(mainFrame, "Technical error importing candidate list.");
      }

}                                                     

    /*
    private void printexamButtonActionPerformed(java.awt.event.ActionEvent evt)                                                
    {                                                    
      int i, j;
      JFrame mainFrame = QyoutiApp.getApplication().getMainFrame();

      if (exam == null)
      {
        JOptionPane.showMessageDialog(mainFrame, "You need to select or create an examination before printing.");
        return;
      }


      if (exam.qdefs == null || exam.qdefs.getRowCount() == 0)
      {
        JOptionPane.showMessageDialog(mainFrame, "You can't print papers - there are no questions in the exam.");
        return;
      }
      if (exam.candidates_sorted.isEmpty())
      {
        JOptionPane.showMessageDialog(mainFrame, "You can't print papers - there are no candidates in the exam.");
        return;
      }

      PrintTranscoder printtranscoder = new PrintTranscoder();
      printtranscoder.addTranscodingHint(  PrintTranscoder.KEY_SHOW_PRINTER_DIALOG, Boolean.TRUE );
      //printtranscoder.addTranscodingHint(  PrintTranscoder.KEY_SHOW_PAGE_DIALOG,    Boolean.TRUE );
      printtranscoder.addTranscodingHint(  PrintTranscoder.KEY_SCALE_TO_PAGE,       Boolean.TRUE );
      printtranscoder.addTranscodingHint(  PrintTranscoder.KEY_MARGIN_BOTTOM, new Float(0.01) );
      printtranscoder.addTranscodingHint(  PrintTranscoder.KEY_MARGIN_LEFT,   new Float(0.01) );
      printtranscoder.addTranscodingHint(  PrintTranscoder.KEY_MARGIN_RIGHT,  new Float(0.01) );
      printtranscoder.addTranscodingHint(  PrintTranscoder.KEY_MARGIN_TOP,    new Float(0.01) );
      printtranscoder.addTranscodingHint(  PrintTranscoder.KEY_MARGIN_TOP,    new Float(0.01) );

      try
      {
        URI examfolderuri;
        Vector<SVGDocument> paginated;
        TranscoderInput tinput;
        String printid = Long.toHexString( System.currentTimeMillis() );
        QuestionMetricsRecordSet qmrecset = new QuestionMetricsRecordSet(printid);
        qmrecset.setMonochromePrint( false );
        for ( j=0; j<exam.candidates_sorted.size(); j++ )
        {
          examfolderuri = exam.examfile.getParentFile().getCanonicalFile().toURI();
          paginated = QTIItemRenderer.paginateItems(
              printid,
              examfolderuri,
              exam.candidates_sorted.elementAt(j),
              exam,
              qmrecset,
              null,
              exam.getPreamble()
              );
          for ( i=0; i<paginated.size(); i++ )
          {
            tinput = new TranscoderInput( paginated.elementAt(i) );
            printtranscoder.transcode( tinput, new TranscoderOutput() );
          }
        }
        printtranscoder.print();
        exam.setOption("last_print_id", printid);
      } catch (Exception ex)
      {
        Logger.getLogger(QyoutiView.class.getName()).log(Level.SEVERE, null, ex);
      }
*/
    
      /*
      File renderfolder = new File(examfolder, "render");
      if (!renderfolder.exists())
      {
        renderfolder.mkdir();
      }
      PrintTask ptask = new PrintTask(exam.examfile, renderfolder, preferences );
      ptask.start();
      */

/*
}                                               
*/
    private void importscansButtonActionPerformed(java.awt.event.ActionEvent evt)                                                  
    {                                                      
      JFrame mainFrame = QyoutiApp.getApplication().getMainFrame();

      if (exam == null)
      {
        JOptionPane.showMessageDialog(mainFrame, "You need to select or create an examination before importing scanned pages.");
        return;
      }

      try
      {
        final JFileChooser fc = new JFileChooser();
        fc.setDialogTitle( "Select directory that contains the scanned images." );
        fc.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
        if ( scanfolder != null )
        {
          fc.setCurrentDirectory( new File(scanfolder) );
        }
        int returnVal = fc.showOpenDialog(mainFrame);

        if (returnVal != JFileChooser.APPROVE_OPTION)
        {
          return;
        }
        File file = fc.getSelectedFile();
        if ( !file.exists() || !file.isDirectory() )
          return;

        /*
        // go through directory and copy images into qyouti working directory.
        File target;
        File[] images = file.listFiles( new FileFilter()
          {
            public boolean accept(File file)
            {
              if ( !file.exists() ) return false;
              if ( !file.isFile() ) return false;
              return file.getName().endsWith( ".png" );
            }

          } );
        File scanfolder = new File( examfolder, "scans" );
        if ( !scanfolder.exists() ) scanfolder.mkdir();
        for ( int i=0; i<images.length; i++ )
        {
          target = new File( scanfolder, images[i].getName() );
          QyoutiUtils.copyFile( images[i], target );
        }
         */
        scantask = new ScanTask(this, exam, file, false, false );
        scantask.start();
      }
      catch (Exception ex)
      {
        exam.candidates.clear();
        exam.candidates_sorted.clear();
        JOptionPane.showMessageDialog(mainFrame, "Technical error importing candidate list.");
      }
    }                                                 


    private int importscansCommandLine(java.awt.event.ActionEvent evt)
    {
      JFrame mainFrame = QyoutiApp.getApplication().getMainFrame();

      if (exam == null)
      {
        System.err.println( "You need to select or create an examination before importing scanned pages.");
        return 100;
      }
      if ( scanfolder == null )
      {
        System.err.println( "No scan folder specified.");
        return 101;
      }

      File file = new File(scanfolder);
      if ( !file.exists() || !file.isDirectory() )
        return 102;

      try
      {
        scantask = new ScanTask(this, exam, file, false, true );
        scantask.run();
        if ( scantask.getExitCode() != 0 )
          return 1;
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
        return 103;
      }
      
      return 0;
    }


    private int preprocessCommandLine(java.awt.event.ActionEvent evt)
    {
      JFrame mainFrame = QyoutiApp.getApplication().getMainFrame();

      if (exam != null)
      {
        System.err.println( "You must not specify an examination before preprocessing scanned pages.");
        return 100;
      }
      if ( scanfolder == null )
      {
        System.err.println( "No scan folder specified.");
        return 101;
      }

      File file = new File(scanfolder);
      if ( !file.exists() || !file.isDirectory() )
        return 102;

      try
      {
        scantask = new ScanTask(this, new ExaminationData( examcatalogue ), file, true, true );
        scantask.run();
        if ( scantask.getExitCode() != 0 )
          return 1;
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
        return 103;
      }

      return 0;
    }



    private void prefsMenuItemActionPerformed(java.awt.event.ActionEvent evt)                                              
    {                                                  
    if (prefsDialog == null)
    {
      JFrame mainFrame = QyoutiApp.getApplication().getMainFrame();
      prefsDialog = new PreferencesDialog(mainFrame, preferences);
      prefsDialog.setLocationRelativeTo(mainFrame);
    }
    QyoutiApp.getApplication().show(prefsDialog);
    }                                             



    private void saveMenuItemActionPerformed(java.awt.event.ActionEvent evt)                                             
    {                                                 

      if (exam == null)
      {
        JFrame mainFrame = QyoutiApp.getApplication().getMainFrame();
        JOptionPane.showMessageDialog(mainFrame, "You need to select or create an examination before saving.");
        return;
      }

      exam.save();

    }                                            

    private void exportScoresButtonActionPerformed(java.awt.event.ActionEvent evt)                                                   
    {                                                       
      // TODO add your handling code here:
      JFrame mainFrame = QyoutiApp.getApplication().getMainFrame();

      if (exam == null)
      {
        JOptionPane.showMessageDialog(mainFrame, "You need to select or create an examination before exporting scores.");
        return;
      }

      if (exam.candidates.isEmpty())
      {
        JOptionPane.showMessageDialog(mainFrame, "You can't export scores when there are no candidates listed in the selected exam.");
        return;
      }

      try
      {
        final JFileChooser fc = new JFileChooser();
        fc.addChoosableFileFilter(new javax.swing.filechooser.FileFilter()
        {

          @Override
          public boolean accept(File f)
          {
            if (f.isDirectory())
            {
              return true;
            }
            return f.getName().toLowerCase().endsWith(".csv");
          }

          @Override
          public String getDescription()
          {
            return "Comma Separated Value files.";
          }
        });


        int returnVal = fc.showOpenDialog(mainFrame);

        if (returnVal != JFileChooser.APPROVE_OPTION)
        {
          return;
        }
        File file = fc.getSelectedFile();
        if ( file.exists() )
        {
          if ( !file.canWrite() )
          {
            JOptionPane.showMessageDialog(mainFrame, "The selected output file cannot be overwritten." );
            return;
          }
          if ( JOptionPane.showConfirmDialog(mainFrame, "Do you really want to overwrite the selected file?" )
                  != JOptionPane.YES_OPTION )
          {
            return;
          }
        }
        exam.exportCsvScores( file );
      }
      catch (Exception ex)
      {
        JOptionPane.showMessageDialog(mainFrame, "Technical error importing candidate list.");
      }

    }                                                  

    private void itemAnalysisButtonActionPerformed(java.awt.event.ActionEvent evt)                                                   
    {                                                       
      // TODO add your handling code here:

      
      JFrame mainFrame = QyoutiApp.getApplication().getMainFrame();

      if (exam == null)
      {
        JOptionPane.showMessageDialog(mainFrame, "You need to select or create an examination before doing item analysis.");
        return;
      }

      if (exam.candidates.isEmpty())
      {
        JOptionPane.showMessageDialog(mainFrame, "You can't do item analysis when there are no candidates listed in the selected exam.");
        return;
      }

      if ( exam.qdefs == null )
      {
        JOptionPane.showMessageDialog(mainFrame, "You can't do item analysis when there are no questions listed in the selected exam.");
        return;
      }

      exam.itemAnalysis();
      exam.save();
    }                                                  

    private void questionPreviewButtonActionPerformed(java.awt.event.ActionEvent evt)                                                      
    {                                                          
        // TODO add your handling code here:
        int row = questionTable.getSelectedRow();
        QTIElementItem item = exam.qdefs.qti.getItems().elementAt(row);

        System.out.println( "Row " + row + " IDent " + item.getIdent() );

        if ( row >= 0 )
        {
            if (questionDialog == null)
            {
              JFrame mainFrame = QyoutiApp.getApplication().getMainFrame();
              try
              {
                questionDialog = new QyoutiQuestionDialog(mainFrame, true,
                        exam.examfile.getParentFile().getCanonicalFile().toURI(),
                        exam );
              } catch (IOException ex)
              {
                Logger.getLogger(QyoutiView.class.getName()).log(Level.SEVERE, null, ex);
              }
              questionDialog.setLocationRelativeTo(mainFrame);
            }            
            questionDialog.setItem( exam.qdefs.qti.getItems().elementAt(row), row+1 );

            QyoutiApp.getApplication().show(questionDialog);
            
        }
    }                                                     

    private void previewexamButtonActionPerformed(java.awt.event.ActionEvent evt)                                                  
    {                                                      

      if (printpreviewDialog == null)
      {
        JFrame mainFrame = QyoutiApp.getApplication().getMainFrame();
        try
        {
          printpreviewDialog = new QyoutiPrintPreviewDialog(mainFrame, true,
                  exam.examfile.getParentFile().getCanonicalFile().toURI(), exam, exam.getPreamble() );
        } catch (IOException ex)
        {
          Logger.getLogger(QyoutiView.class.getName()).log(Level.SEVERE, null, ex);
        }
        printpreviewDialog.setLocationRelativeTo(mainFrame);
      }
      printpreviewDialog.setItems( exam.qdefs.qti.getItems() );

      QyoutiApp.getApplication().show(printpreviewDialog);

    }                                                 


    void displayError( String str )
    {
      JFrame mainFrame = QyoutiApp.getApplication().getMainFrame();

      if ( mainFrame == null )
        JOptionPane.showMessageDialog( mainFrame, str );
      else
        System.out.println( str );
    }


    private void examtopdfButtonActionPerformed(java.awt.event.ActionEvent evt)                                                
    {                                                    
      // TODO add your handling code here:

      
      int i, j;
      JFrame mainFrame = QyoutiApp.getApplication().getMainFrame();

      if (exam == null)
      {
        displayError( "You need to select or create an examination before printing.");
        return;
      }


      if (exam.qdefs == null || exam.qdefs.getRowCount() == 0)
      {
        displayError( "You can't print papers - there are no questions in the exam.");
        return;
      }
      if (exam.candidates_sorted.isEmpty())
      {
        displayError( "You can't print papers - there are no candidates in the exam.");
        return;
      }

      StreamCacheFactory.setDefaultCacheToFile(true);

      try
      {
        URI examfolderuri;
          examfolderuri = exam.examfile.getParentFile().getCanonicalFile().toURI();
        Vector<SVGDocument> paginated;
        TranscoderInput tinput;
        TranscoderOutput transout = new TranscoderOutput(
            new FileOutputStream(
                new File( appfolder, examfolder.getName() + ".pdf" ) ) );


        PaginationRecord paginationrecord = new PaginationRecord(examfolder.getName());
        String printid = paginationrecord.getPrintId();
        QuestionMetricsRecordSet qmrset = new QuestionMetricsRecordSet(printid);
        qmrset.setMonochromePrint( false );
        MultiPagePDFTranscoder pdftranscoder = new MultiPagePDFTranscoder();

        for ( j=0; j<exam.candidates_sorted.size(); j++ )
        {
          System.out.println( "Candidate " + (j+1) + " of " + exam.candidates_sorted.size() );
          System.out.println( "Used memory " + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() )/1000000L) );
          paginated = QTIItemRenderer.paginateItems(
              printid,
              examfolderuri,
              exam.candidates_sorted.elementAt(j),
              exam,
              qmrset,
              paginationrecord,
              exam.getPreamble() );
          for ( i=0; i<paginated.size(); i++ )
          {
            tinput = new TranscoderInput( paginated.elementAt(i) );
            pdftranscoder.transcode( tinput, transout );
            paginated.set(i, null);
          }
          paginated.clear();
        }
        pdftranscoder.complete();
        transout.getOutputStream().close();
        exam.setOption("last_print_id", printid);

//        File qmrrecfile = new File(examfolder, "printmetrics_" + printid + ".xml");
//        if ( qmrrecfile.exists() )
//          throw new IllegalArgumentException( "Unable to save print metrics." );
//        // This helps with dodgy file systems
//        try { qmrrecfile.createNewFile(); }
//        catch ( Exception ee ) {}
//        FileWriter writer = new FileWriter( qmrrecfile );
//        qmrset.emit(writer);
//        writer.close();

        FileWriter writer;
        File pagrecfile = new File(examfolder, "pagination_" + printid + ".xml");
        if ( pagrecfile.exists() )
          throw new IllegalArgumentException( "Unable to save pagination record." );
        // This helps with dodgy file systems
        try { pagrecfile.createNewFile(); }
        catch ( Exception ee ) {}
        writer = new FileWriter( pagrecfile );
        paginationrecord.emit(writer);
        writer.close();

      } catch (Exception ex)
      {
        Logger.getLogger(QyoutiView.class.getName()).log(Level.SEVERE, null, ex);
      }



    }                                               

    private void optionsButtonActionPerformed(java.awt.event.ActionEvent evt)                                              
    {                                                  
      // TODO add your handling code here:

      JFrame mainFrame = QyoutiApp.getApplication().getMainFrame();
      ExamOptionsDialog dialog;

      if (exam == null)
      {
        JOptionPane.showMessageDialog(mainFrame, "You need to select or create an examination before setting options.");
        return;
      }

      dialog = new ExamOptionsDialog( mainFrame, true, exam );
      dialog.setLocationRelativeTo(mainFrame);
      QyoutiApp.getApplication().show(dialog);
      if ( !dialog.wasCancelled() )
        exam.save();
    }                                             




    private void exportRepliesButtonActionPerformed(java.awt.event.ActionEvent evt)                                                    
    {                                                        
      // TODO add your handling code here:

      JFrame mainFrame = QyoutiApp.getApplication().getMainFrame();

      if (exam == null)
      {
        JOptionPane.showMessageDialog(mainFrame, "You need to select or create an examination before exporting replies.");
        return;
      }

      if (exam.candidates.isEmpty())
      {
        JOptionPane.showMessageDialog(mainFrame, "You can't export replies when there are no candidates listed in the selected exam.");
        return;
      }

      try
      {
        final JFileChooser fc = new JFileChooser();
        fc.addChoosableFileFilter(new javax.swing.filechooser.FileFilter()
        {

          @Override
          public boolean accept(File f)
          {
            if (f.isDirectory())
            {
              return true;
            }
            return f.getName().toLowerCase().endsWith(".xml");
          }

          @Override
          public String getDescription()
          {
            return "XML files.";
          }
        });
        fc.addChoosableFileFilter(new javax.swing.filechooser.FileFilter()
        {

          @Override
          public boolean accept(File f)
          {
            if (f.isDirectory())
            {
              return true;
            }
            return f.getName().toLowerCase().endsWith(".csv");
          }

          @Override
          public String getDescription()
          {
            return "Comma Separated Value files.";
          }
        });

        if ( repliesfilename != null )
        {
          File targetfile = new File( repliesfilename );
          fc.setSelectedFile(targetfile);
        }

        int returnVal = fc.showSaveDialog(mainFrame);

        if (returnVal != JFileChooser.APPROVE_OPTION)
        {
          return;
        }
        File file = fc.getSelectedFile();
        if ( file.exists() )
        {
          if ( !file.canWrite() )
          {
            JOptionPane.showMessageDialog(mainFrame, "The selected output file cannot be overwritten." );
            return;
          }
          if ( JOptionPane.showConfirmDialog(mainFrame, "Do you really want to overwrite the selected file?" )
                  != JOptionPane.YES_OPTION )
          {
            return;
          }
        }
        if ( file.getName().endsWith(".xml"))
          exam.exportXmlReplies(file);
        else
          exam.exportCsvReplies( file );
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(mainFrame, "Technical error exporting replies.");
      }
    


    }                                                   

    private void exportReportButtonActionPerformed(java.awt.event.ActionEvent evt)                                                   
    {                                                       
      // TODO add your handling code here:

      JFrame mainFrame = QyoutiApp.getApplication().getMainFrame();

      if (exam == null)
      {
        JOptionPane.showMessageDialog(mainFrame, "You need to select or create an examination before exporting replies.");
        return;
      }

      if (exam.candidates.isEmpty())
      {
        JOptionPane.showMessageDialog(mainFrame, "You can't export replies when there are no candidates listed in the selected exam.");
        return;
      }

      try
      {
        final JFileChooser fc = new JFileChooser();
        fc.addChoosableFileFilter(new javax.swing.filechooser.FileFilter()
        {

          @Override
          public boolean accept(File f)
          {
            if (f.isDirectory())
            {
              return true;
            }
            return f.getName().toLowerCase().endsWith(".pdf");
          }

          @Override
          public String getDescription()
          {
            return "PDF files.";
          }
        });


        if ( reportfilename != null )
        {
          File targetfile = new File( reportfilename );
          fc.setSelectedFile(targetfile);
        }


        int returnVal = fc.showOpenDialog(mainFrame);

        if (returnVal != JFileChooser.APPROVE_OPTION)
        {
          return;
        }
        File file = fc.getSelectedFile();
        if ( file.exists() )
        {
          if ( !file.canWrite() )
          {
            JOptionPane.showMessageDialog(mainFrame, "The selected output file cannot be overwritten." );
            return;
          }
          if ( JOptionPane.showConfirmDialog(mainFrame, "Do you really want to overwrite the selected file?" )
                  != JOptionPane.YES_OPTION )
          {
            return;
          }
        }
        exam.exportReport(file);
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(mainFrame, "Technical error exporting replies.");
      }

    }                                                  

    private void clearresponsesbuttonActionPerformed(java.awt.event.ActionEvent evt)                                                     
    {                                                         
      CandidateData candidate;
      PageData page;
      QuestionData question;
      ResponseData response;

      JFrame mainFrame = QyoutiApp.getApplication().getMainFrame();
      if ( JOptionPane.showConfirmDialog( mainFrame, 
              "<html><p>Clearing Responses may take some time to complete - you will have to wait for confirmation of completion.</p>" +
              "<p>Press ahead anyway?</p></html>", "Confirm",
              JOptionPane.YES_NO_OPTION ) != JOptionPane.YES_OPTION )
        return;

      for ( int i=0; i<exam.candidates_sorted.size(); i++ )
      {
        candidate = exam.candidates_sorted.get( i );
        candidate.score = 0.0;
        for ( int j=0; j<candidate.pages.size(); j++ )
        {
          page = candidate.pages.get( j );
          for ( int k=0; k<page.questions.size(); k++ )
          {
            question = page.questions.get( k );
            for ( int l=0; l<question.responsedatas.size(); l++ )
            {
              response = question.responsedatas.get( l );
              if ( response.getFilteredImageFile().exists() )
                response.getFilteredImageFile().delete();
              if ( response.getImageFile().exists() )
                response.getImageFile().delete();
            }
          }
        }
        candidate.pages.clear();
      }
      exam.pages.clear();
      exam.save();
      gotoQuestion( null );
      exam.pagelistmodel.fireTableChanged( new TableModelEvent( exam.pagelistmodel ) );
      JOptionPane.showMessageDialog( mainFrame, "Scanned responses cleared." );
    }                                                    

    private void preprocessscansButtonActionPerformed(java.awt.event.ActionEvent evt)                                                      
    {                                                          

      JFrame mainFrame = QyoutiApp.getApplication().getMainFrame();

      if (exam != null)
      {
        JOptionPane.showMessageDialog(mainFrame, "Preprocessing can only be done before you select an examination.");
        return;
      }

      try
      {
        final JFileChooser fc = new JFileChooser();
        fc.setDialogTitle( "Select directory that contains the scanned images." );
        fc.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
        if ( scanfolder != null )
        {
          fc.setCurrentDirectory( new File(scanfolder) );
        }
        int returnVal = fc.showOpenDialog(mainFrame);

        if (returnVal != JFileChooser.APPROVE_OPTION)
        {
          return;
        }
        File file = fc.getSelectedFile();
        if ( !file.exists() || !file.isDirectory() )
          return;


        scantask = new ScanTask(this, new ExaminationData( examcatalogue ), file, true, false );
        scantask.start();
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(mainFrame, "Technical error preprocessing scans.");
      }
    }                                                     



  public void gotoQuestion(QuestionData question)
  {
    int i;

    //System.out.println( "gotoQuestion " + question );

    if (question == null)
    {
      clearResponseTable();
      label_candidate_name.setText("");
      label_candidate_number.setText("");
      label_page_number.setText("");
      label_page_source.setText("");
      label_page_height.setText("");
      label_question_id.setText("");
      outcometable.setModel( new OutcomeData() );
      nextResponseButton.setEnabled(false);
      previousResponseButton.setEnabled(false);
      nextCandidateButton.setEnabled(false);
      previousCandidateButton.setEnabled(false);
      return;
    }

    label_candidate_name.setText(question.page.candidate_name);
    label_candidate_number.setText(question.page.candidate_number);
    label_page_number.setText(Integer.toString(question.page.page_number));
    label_page_source.setText(question.page.source);
    if ( question.page.height != 0.0 )
      label_page_height.setText(Double.toString(question.page.height));
    label_question_id.setText(question.ident);
    responseTable.setModel(question);

    outcometable.setModel(question.outcomes);


    QuestionData next, previous;
    next = question.nextQuestionData();
    previous = question.previousQuestionData();
    nextResponseButton.setEnabled(next != null);
    previousResponseButton.setEnabled(previous != null);

    CandidateData candidate = question.page.candidate;
    CandidateData nextc = candidate.nextCandidateData(true);
    CandidateData previousc = candidate.previousCandidateData(true);
    nextCandidateButton.setEnabled(nextc != null);
    previousCandidateButton.setEnabled(previousc != null);
  }
  // Variables declaration - do not modify                     
  private javax.swing.JPanel candidateTabPanel;
  javax.swing.JTable candidateTable;
  private javax.swing.JPanel centrePanel;
  private javax.swing.JButton clearresponsesbutton;
  private javax.swing.JLabel debugImageLabel;
  private javax.swing.JComboBox examCombo;
  private javax.swing.JPanel examTabPanel;
  private javax.swing.JLabel examnamelabel;
  private javax.swing.JButton examtopdfButton;
  private javax.swing.JButton exportRepliesButton;
  private javax.swing.JButton exportReportButton;
  private javax.swing.JButton exportScoresButton;
  private javax.swing.JButton importCandidatesButton;
  private javax.swing.JButton importQuestionsButton;
  private javax.swing.JButton importscansButton;
  private javax.swing.JButton itemAnalysisButton;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JLabel jLabel3;
  private javax.swing.JLabel jLabel5;
  private javax.swing.JLabel jLabel6;
  private javax.swing.JLabel jLabel7;
  private javax.swing.JLabel jLabel8;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JPanel jPanel10;
  private javax.swing.JPanel jPanel11;
  private javax.swing.JPanel jPanel12;
  private javax.swing.JPanel jPanel13;
  private javax.swing.JPanel jPanel14;
  private javax.swing.JPanel jPanel15;
  private javax.swing.JPanel jPanel16;
  private javax.swing.JPanel jPanel17;
  private javax.swing.JPanel jPanel18;
  private javax.swing.JPanel jPanel19;
  private javax.swing.JPanel jPanel2;
  private javax.swing.JPanel jPanel20;
  private javax.swing.JPanel jPanel21;
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
  private javax.swing.JSeparator jSeparator1;
  private javax.swing.JLabel label_candidate_name;
  private javax.swing.JLabel label_candidate_number;
  private javax.swing.JLabel label_page_height;
  private javax.swing.JLabel label_page_number;
  private javax.swing.JLabel label_page_source;
  private javax.swing.JLabel label_question_id;
  private javax.swing.JMenuBar menuBar;
  private javax.swing.JButton newExamButton;
  private javax.swing.JButton nextCandidateButton;
  private javax.swing.JButton nextResponseButton;
  private javax.swing.JToggleButton optionsButton;
  private javax.swing.JTable outcometable;
  private javax.swing.JMenuItem prefsMenuItem;
  private javax.swing.JButton preprocessscansButton;
  private javax.swing.JButton previewexamButton;
  private javax.swing.JButton previousCandidateButton;
  private javax.swing.JButton previousResponseButton;
  private javax.swing.JButton printexamButton;
  private javax.swing.JProgressBar progressBar;
  private javax.swing.JButton questionPreviewButton;
  private javax.swing.JPanel questionTabPanel;
  private javax.swing.JTable questionTable;
  private javax.swing.JPanel responseTabPanel;
  private javax.swing.JTable responseTable;
  private javax.swing.JMenuItem saveMenuItem;
  private javax.swing.JPanel scansTabPanel;
  private javax.swing.JTable scanstable;
  private javax.swing.JLabel statusAnimationLabel;
  private javax.swing.JLabel statusMessageLabel;
  private javax.swing.JPanel statusPanel;
  private javax.swing.JTabbedPane tabbedPane;
  // End of variables declaration                   
  private final Timer messageTimer;
  private final Timer busyIconTimer;
  private final Icon idleIcon;
  private final Icon[] busyIcons = new Icon[15];
  private int busyIconIndex = 0;
  private JDialog aboutBox;
  private QyoutiQuestionDialog questionDialog;
  private QyoutiPrintPreviewDialog printpreviewDialog;
  private NewExamination newExamDialog;

  static class IconRenderer extends DefaultTableCellRenderer.UIResource
  {

    public IconRenderer()
    {
      super();
      setHorizontalAlignment(JLabel.CENTER);
    }

    @Override
    public void setValue(Object value)
    {
      setIcon((value instanceof Icon) ? (Icon) value : null);
    }
  }


}
