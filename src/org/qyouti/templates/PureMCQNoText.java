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
public class PureMCQNoText
        extends javax.swing.JPanel
        implements ItemTemplate
{
  boolean presentationeditenabled=false;
  boolean processingeditenabled=false;
  QTIElementItem item = null;
  QTIElementDecvar scoredecvar = null;
  int optioncount;
  boolean changed=false;

  String itemtitle;  // Title as it was when dialog was opened
  int itemoption;    // Option as it was... no option indicated with -1
  String defaultscore;  // as it was when opened
  int override;
  JRadioButton[] buttons;  // variable number of buttons - depends on no. opts
  /**
   * Creates new instance
   */
  public PureMCQNoText()
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
    int i;
    String correctident=null;
    
    item.setTitle( titlefield.getText() );
    
    int currentoption = -1;
    for ( i=0; i<optioncount; i++ )
        if ( buttons[i].isSelected() )
          currentoption = i;
    
    List<QTIElementResponselabel> l = item.getResponselabels();
    for ( i=0; i<l.size(); i++ )
    {
      if ( i==currentoption )
      {
        correctident = l.get( i ).getIdent();
        l.get( i ).setCorrect( true );
        l.get( i ).setIncorrect( false );
      }
      else
      {
        l.get( i ).setCorrect( false );
        l.get( i ).setIncorrect( true );
      }
    }
    
    if ( scoredecvar != null )
    {
      scoredecvar.setDefaultValue( nocorrect1button.isSelected() ? "1.0" : "0.0" );
    }
    
    List<QTIElementVarequal> varequals =  item.findElements( QTIElementVarequal.class, true );
    if ( varequals.size() != 1 )
      return;
    QTIElementVarequal v = varequals.get( 0 );
    v.setTextContent( correctident );
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

  void updateProcessingeditenabled()
  {
    for ( int i=0; i<optioncount; i++ )
      buttons[i].setEnabled( processingeditenabled );    
  }
  
  public void setProcessingeditenabled( boolean processingeditenabled )
  {
    this.processingeditenabled = processingeditenabled;
    updateProcessingeditenabled();
  }

  public QTIElementItem getItem()
  {
    return item;
  }

  public void setItem( QTIElementItem item )
  {
    int i;
    this.item = item;
    this.changed = false;
    itemtitle = item.getTitle();
    List<QTIElementResponselabel> l = item.getResponselabels();
    optioncount = l.size();
    buttons = new JRadioButton[optioncount];
    itemoption = -1;
    radiobuttonpanel.removeAll();
    for ( i=0; i<optioncount; i++ )
    {
      buttons[i] = new JRadioButton();
      buttons[i].setText( l.get( i ).getIdent().toUpperCase() );
      radiobuttonpanel.add( buttons[i] );
      buttongroup.add( buttons[i] );
      if ( l.get( i ).isCorrect() )
      {
        itemoption = i;
        buttons[i].setSelected( true );
      }
    }
    titlefield.setText( itemtitle );
    
    defaultscore="0.0";

    Vector<QTIElementDecvar> decvars = item.findElements( QTIElementDecvar.class, true );
    for ( i=0; i<decvars.size(); i++ )
    {
      if ( "SCORE".equals( decvars.get( i ).getVarname() ) )
        scoredecvar = decvars.get( i );
    }

    if ( scoredecvar != null )
      defaultscore = scoredecvar.getDefaultValue();
    
    if ( itemoption == -1 )
    {
      if ( "1.0".equals( defaultscore ) )
      {
        override = 1;
        nocorrect1button.setSelected( true );
      }
      else
      {
        override = 0;
        nocorrect0button.setSelected( true );
      }
    }
    else
      override = -1;
            
    updateProcessingeditenabled();
  }  

  @Override
  public boolean isChanged()
  {
    evaluateChange();
    return changed;
  }

  void evaluateChange()
  {
    int currentoption = -1;
    for ( int i=0; i<optioncount; i++ )
      if ( buttons[i].isSelected() )
          currentoption = i;
    String currenttitle = titlefield.getText();
    int currentoverride = -1;
    if ( nocorrect0button.isSelected() )
      currentoverride = 0;
    if ( nocorrect1button.isSelected() )
      currentoverride = 1;
    changed =  currentoption != itemoption || currentoverride != override || !currenttitle.equals( itemtitle );
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

    buttongroup = new javax.swing.ButtonGroup();
    jPanel1 = new javax.swing.JPanel();
    titlelabel = new javax.swing.JLabel();
    titlefield = new javax.swing.JTextField();
    jPanel2 = new javax.swing.JPanel();
    correctlabel = new javax.swing.JLabel();
    radiobuttonpanel = new javax.swing.JPanel();
    jPanel3 = new javax.swing.JPanel();
    nocorrect1button = new javax.swing.JRadioButton();
    nocorrect0button = new javax.swing.JRadioButton();

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
    jPanel2.setLayout(new java.awt.GridBagLayout());

    correctlabel.setText("Correct Answer:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    jPanel2.add(correctlabel, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    jPanel2.add(radiobuttonpanel, gridBagConstraints);

    buttongroup.add(nocorrect1button);
    nocorrect1button.setText("Everyone 1 out of 1");
    jPanel3.add(nocorrect1button);

    buttongroup.add(nocorrect0button);
    nocorrect0button.setText("Everyone 0 out of 0");
    jPanel3.add(nocorrect0button);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 2;
    jPanel2.add(jPanel3, gridBagConstraints);

    add(jPanel2);
  }// </editor-fold>//GEN-END:initComponents

  private void titlefieldActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_titlefieldActionPerformed
  {//GEN-HEADEREND:event_titlefieldActionPerformed
    
    // TODO add your handling code here:
  }//GEN-LAST:event_titlefieldActionPerformed


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.ButtonGroup buttongroup;
  private javax.swing.JLabel correctlabel;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JPanel jPanel2;
  private javax.swing.JPanel jPanel3;
  private javax.swing.JRadioButton nocorrect0button;
  private javax.swing.JRadioButton nocorrect1button;
  private javax.swing.JPanel radiobuttonpanel;
  private javax.swing.JTextField titlefield;
  private javax.swing.JLabel titlelabel;
  // End of variables declaration//GEN-END:variables
}
