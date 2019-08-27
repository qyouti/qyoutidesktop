/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti;

import java.awt.*;
import java.io.*;
import java.util.logging.*;
import javax.swing.*;
import org.qyouti.templates.*;

/**
 *
 * @author jon
 */
public class ExamCreateDialog
        extends javax.swing.JDialog
{
  File folder;
  ExamTemplate template;
  QyoutiFrame parent;

  Class<?>[] classes =
  {
    EmptyExam.class, LeedsBeckettPureMCQ.class
  };

  ExamTemplate[] instances = new ExamTemplate[classes.length];
  
  /**
   * Creates new form QuestionEditDialog
   */
  public ExamCreateDialog( QyoutiFrame parent, boolean modal )
  {
    super( parent, modal );
    this.parent = parent;
    initComponents();
    templatecombobox.removeAllItems();
    for ( int i=0; i<classes.length; i++ )
    {
      try
      {
        instances[i] = (ExamTemplate) classes[i].newInstance();
        templatecombobox.addItem( instances[i].getTemplateTitle() );
      }
      catch ( Exception ex )
      {
        Logger.getLogger( ExamCreateDialog.class.getName() ).
                log( Level.SEVERE, null, ex );
        templatecombobox.addItem( "?" );
      }
    }
    setTemplate( 0 );
  }

  public void setTemplate( int index )
  {
    setTemplate( instances[index] );
  }
  
  public void setFolder( File folder )
  {
    this.folder = folder;
  }
  
  public void setTemplate( ExamTemplate template )
  {
    this.template = template;
    centrepanel.removeAll();
    if ( template == null )
      centrepanel.add(  new JLabel( "Error loading dialog." ) );
    else
      centrepanel.add( template );
    this.pack();
    this.repaint();
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

    jPanel1 = new javax.swing.JPanel();
    templatelabel = new javax.swing.JLabel();
    templatecombobox = new javax.swing.JComboBox<>();
    centrepanel = new javax.swing.JPanel();
    bottompanel = new javax.swing.JPanel();
    createbutton = new javax.swing.JButton();
    cancelbutton = new javax.swing.JButton();

    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    setTitle("Question Edit");

    templatelabel.setText("Select Exam/Survey Template:");
    jPanel1.add(templatelabel);

    templatecombobox.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
    templatecombobox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Empty Examination", "Some other one" }));
    templatecombobox.addItemListener(new java.awt.event.ItemListener()
    {
      public void itemStateChanged(java.awt.event.ItemEvent evt)
      {
        templatecomboboxItemStateChanged(evt);
      }
    });
    jPanel1.add(templatecombobox);

    getContentPane().add(jPanel1, java.awt.BorderLayout.NORTH);

    centrepanel.setLayout(new java.awt.BorderLayout());
    getContentPane().add(centrepanel, java.awt.BorderLayout.CENTER);

    createbutton.setText("Create");
    createbutton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        createbuttonActionPerformed(evt);
      }
    });
    bottompanel.add(createbutton);

    cancelbutton.setText("Cancel");
    cancelbutton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        cancelbuttonActionPerformed(evt);
      }
    });
    bottompanel.add(cancelbutton);

    getContentPane().add(bottompanel, java.awt.BorderLayout.SOUTH);

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void cancelbuttonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cancelbuttonActionPerformed
  {//GEN-HEADEREND:event_cancelbuttonActionPerformed
    // TODO add your handling code here:
    this.setVisible( false );
    this.dispose();
  }//GEN-LAST:event_cancelbuttonActionPerformed

  private void createbuttonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_createbuttonActionPerformed
  {//GEN-HEADEREND:event_createbuttonActionPerformed
    parent.examinationBuilt( folder, template );
    this.setVisible( false );
    this.dispose();
  }//GEN-LAST:event_createbuttonActionPerformed

  private void templatecomboboxItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_templatecomboboxItemStateChanged
  {//GEN-HEADEREND:event_templatecomboboxItemStateChanged
    System.out.println( "item state change" );
    if ( evt.getStateChange() == java.awt.event.ItemEvent.SELECTED )
      setTemplate( templatecombobox.getSelectedIndex() );
  }//GEN-LAST:event_templatecomboboxItemStateChanged


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JPanel bottompanel;
  private javax.swing.JButton cancelbutton;
  private javax.swing.JPanel centrepanel;
  private javax.swing.JButton createbutton;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JComboBox<String> templatecombobox;
  private javax.swing.JLabel templatelabel;
  // End of variables declaration//GEN-END:variables

}
