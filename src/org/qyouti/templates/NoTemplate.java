/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti.templates;

import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import org.qyouti.qti1.element.*;

/**
 *
 * @author jon
 */
public class NoTemplate
        extends javax.swing.JPanel
        implements ItemTemplate
{
  boolean presentationeditenabled=false;
  boolean processingeditenabled=false;
  QTIElementItem item = null;
  boolean changed=false;

  String itemtitle;  // Title as it was when dialog was opened
  int itemoption;    // Option as it was...
  
  /**
   * Creates new form FourOptionMCQNoText
   */
  public NoTemplate()
  {
    initComponents();
  }

  @Override
  public Component getComponent()
  {
    return this;
  }

  @Override
  public void store()
  {
    item.setTitle( titlefield.getText() );    
  }

  
  public boolean isPresentationeditenabled()
  {
    return presentationeditenabled;
  }

  public void setPresentationeditenabled( boolean presentationeditenabled )
  {
    this.presentationeditenabled = presentationeditenabled;
    titlefield.setEnabled( presentationeditenabled );
  }

  public boolean isProcessingeditenabled()
  {
    return processingeditenabled;
  }

  public void setProcessingeditenabled( boolean processingeditenabled )
  {
    this.processingeditenabled = processingeditenabled;
  }

  public QTIElementItem getItem()
  {
    return item;
  }

  public void setItem( QTIElementItem item )
  {
    this.item = item;
    this.changed = false;
    itemtitle = item.getTitle();
    titlefield.setText( itemtitle );
  }  

  @Override
  public boolean isChanged()
  {
    evaluateChange();
    return changed;
  }

  void evaluateChange()
  {
    String currenttitle = titlefield.getText();
    changed =  !currenttitle.equals( itemtitle );
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
    titlelabel = new javax.swing.JLabel();
    titlefield = new javax.swing.JTextField();
    jPanel2 = new javax.swing.JPanel();
    jLabel1 = new javax.swing.JLabel();

    jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Presentation"));

    titlelabel.setText("Title:");
    jPanel1.add(titlelabel);

    titlefield.setColumns(30);
    titlefield.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        titlefieldActionPerformed(evt);
      }
    });
    jPanel1.add(titlefield);

    add(jPanel1);

    jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Response Processing"));

    jLabel1.setText("Not a template question \nCannot Edit");
    jPanel2.add(jLabel1);

    add(jPanel2);
  }// </editor-fold>//GEN-END:initComponents

  private void titlefieldActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_titlefieldActionPerformed
  {//GEN-HEADEREND:event_titlefieldActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_titlefieldActionPerformed


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JLabel jLabel1;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JPanel jPanel2;
  private javax.swing.JTextField titlefield;
  private javax.swing.JLabel titlelabel;
  // End of variables declaration//GEN-END:variables
}
