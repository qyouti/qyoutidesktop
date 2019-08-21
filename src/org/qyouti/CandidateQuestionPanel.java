/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.nio.file.Files;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import org.qyouti.data.*;
import org.qyouti.qti1.element.*;

/**
 *
 * @author jon
 */
public class CandidateQuestionPanel
        extends javax.swing.JPanel
        implements ExaminerOverrideListener
{
  CandidateData candidate;
  String questionident;
  QuestionData questiondata;
  TableModelListener questiondatalistener;
  QTIElementItem item;
  
  BufferedImage questionimage;
  PinkBoxTableCellRenderer pinkboxrenderer = new PinkBoxTableCellRenderer();
  /**
   * Creates new form CandidateQuestionPanel
   */
  public CandidateQuestionPanel( CandidateData cd, String ident )
  {
    candidate = cd;
    questionident = ident;
    item = candidate.exam.qdefs.qti.getItem( ident );
    questiondata = candidate.getQuestionData( ident );
    
    initComponents();
    
    if ( item == null )
    {
      titlelabel.setText( "Missing Question: " + ident );
      return;
    }
    
    if ( item.getTitle() == null || item.getTitle().length() == 0 )
      titlelabel.setText("Untitled Question" );
    else
      titlelabel.setText( item.getTitle() );
    
    if ( questiondata == null )
    {
      statuslabel.setText( "Not scanned" );
      centrepanel.setVisible( false );
    }
    else
    {
      questiondata.setExaminerOverrideListener( this );
      if ( questiondata.needsreview )
      {
        switch ( questiondata.getExaminerDecision() )
        {
          case QuestionData.EXAMINER_DECISION_NONE:
            statuslabel.setText( "Review recommended." );
            break;
          case QuestionData.EXAMINER_DECISION_OVERRIDE:
            statuslabel.setText( "Responses overridden by examiner. (Review was recommended.)" );
            break;
          case QuestionData.EXAMINER_DECISION_STAND:
            statuslabel.setText( "Examiner decided responses will stand. (Review was recommended.)" );
            break;            
        }
      }
      else
      {
        switch ( questiondata.getExaminerDecision() )
        {
          case QuestionData.EXAMINER_DECISION_NONE:
            statuslabel.setText( "Review not recommended." );
            break;
          case QuestionData.EXAMINER_DECISION_OVERRIDE:
            statuslabel.setText( "Responses overridden by examiner. (Review was not recommended.)" );
            break;
          case QuestionData.EXAMINER_DECISION_STAND:
            statuslabel.setText( "Examiner decided responses will stand.  (Review was not recommended.)" );
            break;            
        }
      }

      updateButtons();

      responsetable.setModel( questiondata );
      questiondatalistener = new TableModelListener(){
        @Override
        public void tableChanged( TableModelEvent e )
        {
          System.out.println( "CandidateQuestionPanel detected table change." );
          int h;
          for ( int i=0; i<questiondata.getRowCount(); i++ )
          {
            h = questiondata.getRowHeight( i );
            if ( h>0 )
              responsetable.setRowHeight( i, h );
          }
          // Unable to set row height here...
          // Maybe better not to add/remove columns but just change
          // content of the last column
          
        }
      };
      
      questiondata.addTableModelListener( questiondatalistener );
      for ( int i=0; i<questiondata.getRowCount(); i++ )
      {
        int h = questiondata.getRowHeight( i );
        if ( h>0 )
          responsetable.setRowHeight( i, h );
      }
      //responsetable.getColumnModel().getColumn( 4 ).setCellRenderer( new PinkBoxTableCellRenderer() );
      DefaultCellEditor dce;
      JCheckBox cb = new JCheckBox();
      cb.setSelectedIcon( TrueFalseIcon.TRUEICON );
      cb.setIcon( TrueFalseIcon.FALSEICON );
      dce = new DefaultCellEditor( cb );
      responsetable.setDefaultEditor( Boolean.class, dce);
      pinkboxrenderer.setGreyed( questiondata.getExaminerDecision() != QuestionData.EXAMINER_DECISION_OVERRIDE );
      responsetable.setDefaultRenderer( Boolean.class, pinkboxrenderer );
      outcometable.setModel( questiondata.getOutcomes() );
    }
    
    imagescrollpanel.setVisible( true );
    responsetablepanel.setVisible( true );
    loadQuestionImage();
    responsetablepanel.add( responsetable.getTableHeader(), java.awt.BorderLayout.NORTH );
    responsetable.setMinimumSize(new Dimension(200,responsetable.getRowHeight( 0 )*responsetable.getRowCount()));
  }

  @Override
  public void examinerOverrideChanged()
  {
    pinkboxrenderer.setGreyed( questiondata.getExaminerDecision() != QuestionData.EXAMINER_DECISION_OVERRIDE );
    updateButtons();
  }
  
  public final void updateButtons()
  {
    switch ( questiondata.getExaminerDecision() )
    {
      case QuestionData.EXAMINER_DECISION_NONE:
        decisionbuttona.setSelected( true );
        break;
      case QuestionData.EXAMINER_DECISION_STAND:
        decisionbuttonb.setSelected( true );
        break;            
      case QuestionData.EXAMINER_DECISION_OVERRIDE:
        decisionbuttonc.setSelected( true );
        break;
    }    
  }
  
  
  @Override
  public void removeNotify()
  {
    super.removeNotify();
    this.responsetable.setModel( new DefaultTableModel() );
    this.outcometable.setModel( new DefaultTableModel() );
    questiondata.removeTableModelListener( questiondatalistener );
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

    decisionbuttongroup = new javax.swing.ButtonGroup();
    toppanel = new javax.swing.JPanel();
    titlelabel = new javax.swing.JLabel();
    statuslabel = new javax.swing.JLabel();
    centrepanel = new javax.swing.JPanel();
    innerpanel = new javax.swing.JPanel();
    decisionpanel = new javax.swing.JPanel();
    decisionbuttona = new javax.swing.JToggleButton();
    decisionbuttonb = new javax.swing.JToggleButton();
    decisionbuttonc = new javax.swing.JToggleButton();
    outcomepanel = new javax.swing.JPanel();
    outcometable = new javax.swing.JTable();
    imagepanel = new javax.swing.JPanel();
    viewimagecheckbox = new javax.swing.JCheckBox();
    imagescrollpanel = new javax.swing.JScrollPane();
    imagelabel = new javax.swing.JLabel();
    responsepanel = new javax.swing.JPanel();
    viewresponsescheckbox = new javax.swing.JCheckBox();
    responsetablepanel = new javax.swing.JPanel();
    responsetable = new javax.swing.JTable();
    analysispanel = new javax.swing.JPanel();
    analysisbutton = new javax.swing.JButton();
    jSeparator1 = new javax.swing.JSeparator();

    setBackground(java.awt.Color.white);
    setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
    setLayout(new java.awt.BorderLayout());

    toppanel.setBackground(java.awt.Color.white);
    toppanel.setLayout(new java.awt.GridLayout(0, 1));

    titlelabel.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
    titlelabel.setText("Title of Question");
    toppanel.add(titlelabel);

    statuslabel.setFont(new java.awt.Font("Dialog", 0, 14)); // NOI18N
    statuslabel.setText("question status");
    toppanel.add(statuslabel);

    add(toppanel, java.awt.BorderLayout.NORTH);

    centrepanel.setBackground(java.awt.Color.white);
    centrepanel.setLayout(new java.awt.GridBagLayout());

    innerpanel.setBackground(java.awt.Color.white);
    innerpanel.setLayout(new java.awt.GridBagLayout());

    decisionpanel.setOpaque(false);

    decisionbuttongroup.add(decisionbuttona);
    decisionbuttona.setMnemonic('r');
    decisionbuttona.setSelected(true);
    decisionbuttona.setText("No Review");
    decisionbuttona.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        decisionbuttonaActionPerformed(evt);
      }
    });
    decisionpanel.add(decisionbuttona);

    decisionbuttongroup.add(decisionbuttonb);
    decisionbuttonb.setMnemonic('C');
    decisionbuttonb.setText("Confirm");
    decisionbuttonb.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        decisionbuttonbActionPerformed(evt);
      }
    });
    decisionpanel.add(decisionbuttonb);

    decisionbuttongroup.add(decisionbuttonc);
    decisionbuttonc.setMnemonic('o');
    decisionbuttonc.setText("Override");
    decisionbuttonc.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        decisionbuttoncActionPerformed(evt);
      }
    });
    decisionpanel.add(decisionbuttonc);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.weighty = 0.5;
    innerpanel.add(decisionpanel, gridBagConstraints);

    outcomepanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Question Outcomes"));
    outcomepanel.setOpaque(false);
    outcomepanel.setLayout(new java.awt.GridBagLayout());

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
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.weighty = 0.5;
    outcomepanel.add(outcometable, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.weighty = 0.5;
    innerpanel.add(outcomepanel, gridBagConstraints);

    imagepanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Scanned Image"));
    imagepanel.setOpaque(false);
    imagepanel.setLayout(new java.awt.BorderLayout());

    viewimagecheckbox.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
    viewimagecheckbox.setSelected(true);
    viewimagecheckbox.setText("Show");
    viewimagecheckbox.setOpaque(false);
    viewimagecheckbox.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        viewimagecheckboxActionPerformed(evt);
      }
    });
    imagepanel.add(viewimagecheckbox, java.awt.BorderLayout.NORTH);

    imagescrollpanel.setBackground(java.awt.Color.white);
    imagescrollpanel.setBorder(null);
    imagescrollpanel.setOpaque(false);

    imagelabel.setBackground(java.awt.Color.white);
    imagelabel.setText("      ");
    imagelabel.setOpaque(true);
    imagescrollpanel.setViewportView(imagelabel);

    imagepanel.add(imagescrollpanel, java.awt.BorderLayout.CENTER);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.weighty = 0.5;
    innerpanel.add(imagepanel, gridBagConstraints);

    responsepanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Responses"));
    responsepanel.setOpaque(false);
    responsepanel.setLayout(new java.awt.BorderLayout());

    viewresponsescheckbox.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
    viewresponsescheckbox.setSelected(true);
    viewresponsescheckbox.setText("Show");
    viewresponsescheckbox.setOpaque(false);
    viewresponsescheckbox.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        viewresponsescheckboxActionPerformed(evt);
      }
    });
    responsepanel.add(viewresponsescheckbox, java.awt.BorderLayout.NORTH);

    responsetablepanel.setOpaque(false);
    responsetablepanel.setLayout(new java.awt.BorderLayout());

    responsetable.setModel(new javax.swing.table.DefaultTableModel(
      new Object [][]
      {

      },
      new String []
      {
        "A", "B", "C", "D"
      }
    )
    {
      boolean[] canEdit = new boolean []
      {
        false, false, false, false
      };

      public boolean isCellEditable(int rowIndex, int columnIndex)
      {
        return canEdit [columnIndex];
      }
    });
    responsetable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_LAST_COLUMN);
    responsetablepanel.add(responsetable, java.awt.BorderLayout.CENTER);

    analysispanel.setBackground(java.awt.Color.white);

    analysisbutton.setText("Analyse");
    analysisbutton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        analysisbuttonActionPerformed(evt);
      }
    });
    analysispanel.add(analysisbutton);

    responsetablepanel.add(analysispanel, java.awt.BorderLayout.SOUTH);

    responsepanel.add(responsetablepanel, java.awt.BorderLayout.CENTER);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.weighty = 0.5;
    innerpanel.add(responsepanel, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(12, 0, 12, 0);
    innerpanel.add(jSeparator1, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.weighty = 0.5;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    centrepanel.add(innerpanel, gridBagConstraints);

    add(centrepanel, java.awt.BorderLayout.CENTER);
  }// </editor-fold>//GEN-END:initComponents

  private void loadQuestionImage()
  {
    if ( questiondata == null || questiondata.getImage() == null )
    {
      imagelabel.setText( "Not Scanned" );
    }
    else
    {
      questionimage = questiondata.getImage();
      imagelabel.setIcon( new ImageIcon( questionimage ) );        
      imagelabel.setText( "" );
    }
  }
  
  private void viewimagecheckboxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_viewimagecheckboxActionPerformed
  {//GEN-HEADEREND:event_viewimagecheckboxActionPerformed
    
    imagelabel.setIcon( null );
    imagelabel.setText( "" );
      
    if ( !viewimagecheckbox.isSelected() )
    {
      questionimage = null;
      imagescrollpanel.setVisible( false );
      revalidate();
      return;
    }

    loadQuestionImage();

    imagescrollpanel.setVisible( true );
    revalidate();    
  }//GEN-LAST:event_viewimagecheckboxActionPerformed

  private void viewresponsescheckboxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_viewresponsescheckboxActionPerformed
  {//GEN-HEADEREND:event_viewresponsescheckboxActionPerformed
      responsetablepanel.setVisible( viewresponsescheckbox.isSelected() );
      revalidate();
  }//GEN-LAST:event_viewresponsescheckboxActionPerformed

  
  public void handleDecision( int n )
  {
    if ( n != questiondata.getExaminerDecision() )
    {
      System.out.println( "examiner decision change." );
      questiondata.setExaminerDecision( n );
      candidate.exam.setUnsavedChangesInExaminer( true );
    }    
  }
  
  private void decisionbuttoncActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_decisionbuttoncActionPerformed
  {//GEN-HEADEREND:event_decisionbuttoncActionPerformed
    // TODO add your handling code here:
    handleDecision(2);
  }//GEN-LAST:event_decisionbuttoncActionPerformed

  private void decisionbuttonaActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_decisionbuttonaActionPerformed
  {//GEN-HEADEREND:event_decisionbuttonaActionPerformed
    // TODO add your handling code here:
    handleDecision(0);
  }//GEN-LAST:event_decisionbuttonaActionPerformed

  private void decisionbuttonbActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_decisionbuttonbActionPerformed
  {//GEN-HEADEREND:event_decisionbuttonbActionPerformed
    // TODO add your handling code here:
    handleDecision(1);
  }//GEN-LAST:event_decisionbuttonbActionPerformed

  private void analysisbuttonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_analysisbuttonActionPerformed
  {//GEN-HEADEREND:event_analysisbuttonActionPerformed
    
    int s = responsetable.getSelectedRow();
    if ( s < 0 )
    {
      return;
    }
    
    ResponseData rd = questiondata.responsedatas.get( s );
    if ( rd == null )
      return;
    
    
    ImageAnalysisDialog d = new ImageAnalysisDialog( QyoutiFrame.getFrames()[0], false );
    d.setTitle( "Image Analysis" );
    d.setSize( 400, 800 );
    d.setImage( rd.getImage() );
    d.setVisible( true );
    d.go();
  }//GEN-LAST:event_analysisbuttonActionPerformed


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton analysisbutton;
  private javax.swing.JPanel analysispanel;
  private javax.swing.JPanel centrepanel;
  private javax.swing.JToggleButton decisionbuttona;
  private javax.swing.JToggleButton decisionbuttonb;
  private javax.swing.JToggleButton decisionbuttonc;
  private javax.swing.ButtonGroup decisionbuttongroup;
  private javax.swing.JPanel decisionpanel;
  private javax.swing.JLabel imagelabel;
  private javax.swing.JPanel imagepanel;
  private javax.swing.JScrollPane imagescrollpanel;
  private javax.swing.JPanel innerpanel;
  private javax.swing.JSeparator jSeparator1;
  private javax.swing.JPanel outcomepanel;
  private javax.swing.JTable outcometable;
  private javax.swing.JPanel responsepanel;
  private javax.swing.JTable responsetable;
  private javax.swing.JPanel responsetablepanel;
  private javax.swing.JLabel statuslabel;
  private javax.swing.JLabel titlelabel;
  private javax.swing.JPanel toppanel;
  private javax.swing.JCheckBox viewimagecheckbox;
  private javax.swing.JCheckBox viewresponsescheckbox;
  // End of variables declaration//GEN-END:variables


}
