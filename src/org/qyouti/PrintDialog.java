/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti;

import javax.swing.*;
import javax.swing.event.*;

/**
 *
 * @author jon
 */
public class PrintDialog
        extends javax.swing.JDialog
{
  private int namedcount;
  private int anoncount;
  private SpinnerNumberModel anonmodel;
  private SpinnerNumberModel extramodel;
  
  boolean confirmed = false;
  /**
   * Creates new form PrintDialog
   */
  public PrintDialog( java.awt.Frame parent, boolean modal, int namedcount, int anoncount )
  {
    super( parent, modal );
    initComponents();
    this.namedcount = namedcount;
    this.anoncount = anoncount;
    namedlabel.setText( Integer.toString( namedcount ) );
    anonlabel.setText( Integer.toString( anoncount ) );
    anonmodel = (SpinnerNumberModel)anonspinner.getModel();
    anonmodel.addChangeListener( 
            new ChangeListener()
            {
              @Override
              public void stateChanged( ChangeEvent e )
              {
                updateTotalPapers();
              }
            }
    );
    extramodel = (SpinnerNumberModel)extraspinner.getModel();
    extramodel.addChangeListener( 
            new ChangeListener()
            {
              @Override
              public void stateChanged( ChangeEvent e )
              {
                updateTotalPapers();
              }
            }
    );
    updateTotalPapers();
  }

  private void updateTotalPapers()
  {
    int total = namedcount + anoncount + anonmodel.getNumber().intValue() + extramodel.getNumber().intValue();
    totallabel.setText( Integer.toString( total ) );
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

    jPanel1 = new javax.swing.JPanel();
    jLabel1 = new javax.swing.JLabel();
    namedlabel = new javax.swing.JLabel();
    jLabel3 = new javax.swing.JLabel();
    anonlabel = new javax.swing.JLabel();
    jLabel5 = new javax.swing.JLabel();
    anonspinner = new javax.swing.JSpinner();
    jLabel8 = new javax.swing.JLabel();
    extraspinner = new javax.swing.JSpinner();
    jLabel9 = new javax.swing.JLabel();
    totallabel = new javax.swing.JLabel();
    jPanel2 = new javax.swing.JPanel();
    printbutton = new javax.swing.JButton();
    cancelbutton = new javax.swing.JButton();

    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

    jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
    jPanel1.setLayout(new java.awt.GridBagLayout());

    jLabel1.setText("Imported Named People:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 8, 4);
    jPanel1.add(jLabel1, gridBagConstraints);

    namedlabel.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
    namedlabel.setText("000");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    jPanel1.add(namedlabel, gridBagConstraints);

    jLabel3.setText("Imported Anonymous People:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 8, 4);
    jPanel1.add(jLabel3, gridBagConstraints);

    anonlabel.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
    anonlabel.setText("000");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    jPanel1.add(anonlabel, gridBagConstraints);

    jLabel5.setText("Anonymous People Not Yet Imported:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 8, 4);
    jPanel1.add(jLabel5, gridBagConstraints);

    anonspinner.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
    anonspinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    jPanel1.add(anonspinner, gridBagConstraints);

    jLabel8.setText("Spare Papers:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 8, 4);
    jPanel1.add(jLabel8, gridBagConstraints);

    extraspinner.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
    extraspinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    jPanel1.add(extraspinner, gridBagConstraints);

    jLabel9.setText("Total Papers to Print:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 8, 4);
    jPanel1.add(jLabel9, gridBagConstraints);

    totallabel.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
    totallabel.setText("000");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    jPanel1.add(totallabel, gridBagConstraints);

    getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

    printbutton.setText("Print");
    printbutton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        printbuttonActionPerformed(evt);
      }
    });
    jPanel2.add(printbutton);

    cancelbutton.setText("Cancel");
    cancelbutton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        cancelbuttonActionPerformed(evt);
      }
    });
    jPanel2.add(cancelbutton);

    getContentPane().add(jPanel2, java.awt.BorderLayout.SOUTH);

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void printbuttonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_printbuttonActionPerformed
  {//GEN-HEADEREND:event_printbuttonActionPerformed
    confirmed = true;
    dispose();
  }//GEN-LAST:event_printbuttonActionPerformed

  private void cancelbuttonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cancelbuttonActionPerformed
  {//GEN-HEADEREND:event_cancelbuttonActionPerformed
    confirmed = false;
    dispose(); 
  }//GEN-LAST:event_cancelbuttonActionPerformed

  public int getAnonPlusExtra()
  {
    return anoncount + anonmodel.getNumber().intValue() + extramodel.getNumber().intValue();
  }

  public boolean isConfirmed()
  {
    return confirmed;
  }
  
  

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JLabel anonlabel;
  private javax.swing.JSpinner anonspinner;
  private javax.swing.JButton cancelbutton;
  private javax.swing.JSpinner extraspinner;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel3;
  private javax.swing.JLabel jLabel5;
  private javax.swing.JLabel jLabel8;
  private javax.swing.JLabel jLabel9;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JPanel jPanel2;
  private javax.swing.JLabel namedlabel;
  private javax.swing.JButton printbutton;
  private javax.swing.JLabel totallabel;
  // End of variables declaration//GEN-END:variables
}
