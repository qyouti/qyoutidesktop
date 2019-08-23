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
import org.qyouti.data.QuestionDefinitions;
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
  QuestionDefinitions overrides = null;
  QTIElementItem item = null;
  QTIElementItem overrideitem = null;
  QTIElementDecvar overridescoredecvar = null;
  int optioncount;
  boolean changed=false;
  boolean overridechanged=false;

  String itemtitle;  // Title as it was when dialog was opened
  int itemoption;    // Option as it was... no option indicated with -1
  int overrideitemoption;    // Option as it was... no option indicated with -1
  String defaultscore;  // as it was when opened
  int override;
  JRadioButton[] buttons;  // variable number of buttons - depends on no. opts
  JRadioButton[] overridebuttons;  // variable number of buttons - depends on no. opts
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
  public void store( boolean override )
  {
    int i;
    String correctident=null;
    
    if ( override )
    {
      boolean on = overridecheckbox.isSelected();
      if ( on )
      {
        if ( overrideitem == null )
          overrideitem = overrides.copyItem( item );
      }
      else
      {
        if ( overrideitem != null )
        {
          overrides.removeItem( overrideitem );
          overrideitem = null;
        }
        return;
      }
    }
    
    if ( !override )
      item.setTitle( titlefield.getText() );
    
    JRadioButton[] currentbuttons = override?overridebuttons:buttons;
    QTIElementItem currentitem = override?overrideitem:item;
    
    int currentoption = -1;
    for ( i=0; i<optioncount; i++ )
        if ( currentbuttons[i].isSelected() )
          currentoption = i;
    
    List<QTIElementResponselabel> l = currentitem.getResponselabels();
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
    
    if ( override && overridescoredecvar != null )
    {
      overridescoredecvar.setDefaultValue( nocorrect1button.isSelected() ? "1.0" : "0.0" );
    }
    
    List<QTIElementVarequal> varequals =  currentitem.findElements( QTIElementVarequal.class, true );
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

  public void setItem( QTIElementItem item, QuestionDefinitions overrides )
  {
    this.item = item;
    this.changed = false;
    this.overridechanged = false;
    this.itemtitle = item.getTitle();
    List<QTIElementResponselabel> l = item.getResponselabels();
    this.optioncount = l.size();

    titlefield.setText( itemtitle );
    
    this.overrides = overrides;
    overrideitem = overrides.qti.getItem( item.getIdent() );
    overridepanel.setVisible( overrideitem != null );
    overridecheckbox.setSelected( overrideitem != null );
    setControlsToItem( item, false );
    if ( overrideitem != null )
      setControlsToItem( overrideitem, true );
  }
  
  void setControlsToItem( QTIElementItem item, boolean overridecontrols )
  {
    int i;

    if ( overridecontrols )
      overrideradiobuttonpanel.removeAll();
    else
      radiobuttonpanel.removeAll();
    
    List<QTIElementResponselabel> l = item.getResponselabels();
    if ( overridecontrols )
      overridebuttons = new JRadioButton[optioncount];
    else
      buttons = new JRadioButton[optioncount];

    int currentitemoption = -1;
    JRadioButton[] currentbuttons = overridecontrols?overridebuttons:buttons;
    JPanel currentpanel = overridecontrols?overrideradiobuttonpanel:radiobuttonpanel;
    ButtonGroup currentbuttongroup = overridecontrols?overridebuttongroup:buttongroup;
    for ( i=0; i<optioncount; i++ )
    {
      currentbuttons[i] = new JRadioButton();
      currentbuttons[i].setOpaque( false );
      currentbuttons[i].setText( l.get( i ).getIdent().toUpperCase() );
      currentpanel.add( currentbuttons[i] );
      currentbuttongroup.add( currentbuttons[i] );
      if ( l.get( i ).isCorrect() )
      {
        currentitemoption = i;
        currentbuttons[i].setSelected( true );
      }
    }

    if ( overridecontrols )
      this.overrideitemoption = currentitemoption;
    else
      this.itemoption = currentitemoption;


    if ( overridecontrols )
    {
      defaultscore="0.0";
      Vector<QTIElementDecvar> decvars = item.findElements( QTIElementDecvar.class, true );
      for ( i=0; i<decvars.size(); i++ )
      {
        if ( "SCORE".equals( decvars.get( i ).getVarname() ) )
          overridescoredecvar = decvars.get( i );
      }

      if ( overridescoredecvar != null )
        defaultscore = overridescoredecvar.getDefaultValue();
      if ( currentitemoption == -1 )
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
    }
    
    updateProcessingeditenabled();
  }  

  @Override
  public boolean isChanged()
  {
    evaluateChange();
    return changed;
  }

  @Override
  public boolean isOverrideChanged()
  {
    evaluateChange();
    return overridechanged;
  }


  void evaluateChange()
  {
    int currentoption = -1;
    for ( int i=0; i<optioncount; i++ )
      if ( buttons[i].isSelected() )
          currentoption = i;    
    String currenttitle = titlefield.getText();
    changed =  currentoption != itemoption || !currenttitle.equals( itemtitle );

    boolean on = overridecheckbox.isSelected();
    if ( !on )
    {
      overridechanged = overrideitem != null;
      return;
    }
    
    currentoption = -1;
    for ( int i=0; i<optioncount; i++ )
      if ( overridebuttons[i].isSelected() )
          currentoption = i;    
    int currentoverride = -1;
    if ( nocorrect0button.isSelected() )
      currentoverride = 0;
    if ( nocorrect1button.isSelected() )
      currentoverride = 1;    
    overridechanged = overrideitem == null || currentoption != overrideitemoption || currentoverride != override;
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
    overridebuttongroup = new javax.swing.ButtonGroup();
    jPanel1 = new javax.swing.JPanel();
    titlelabel = new javax.swing.JLabel();
    titlefield = new javax.swing.JTextField();
    jPanel4 = new javax.swing.JPanel();
    jPanel2 = new javax.swing.JPanel();
    correctlabel = new javax.swing.JLabel();
    radiobuttonpanel = new javax.swing.JPanel();
    overridecheckbox = new javax.swing.JCheckBox();
    overridepanel = new javax.swing.JPanel();
    pinkpanel = new javax.swing.JPanel();
    jLabel1 = new javax.swing.JLabel();
    overrideradiobuttonpanel = new javax.swing.JPanel();
    jPanel3 = new javax.swing.JPanel();
    nocorrect1button = new javax.swing.JRadioButton();
    nocorrect0button = new javax.swing.JRadioButton();

    setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 6, 6, 6));
    setLayout(new java.awt.GridBagLayout());

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

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
    add(jPanel1, gridBagConstraints);

    jPanel4.setLayout(new java.awt.GridBagLayout());

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

    overridecheckbox.setText("Override These");
    overridecheckbox.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        overridecheckboxActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    jPanel2.add(overridecheckbox, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
    jPanel4.add(jPanel2, gridBagConstraints);

    overridepanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 0), 4), "Override Response Processing"));
    overridepanel.setLayout(new java.awt.BorderLayout());

    pinkpanel.setOpaque(false);
    pinkpanel.setLayout(new java.awt.GridBagLayout());

    jLabel1.setText("Correct Answer:");
    pinkpanel.add(jLabel1, new java.awt.GridBagConstraints());

    overrideradiobuttonpanel.setOpaque(false);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    pinkpanel.add(overrideradiobuttonpanel, gridBagConstraints);

    jPanel3.setOpaque(false);

    overridebuttongroup.add(nocorrect1button);
    nocorrect1button.setText("Everyone 1 out of 1");
    nocorrect1button.setOpaque(false);
    jPanel3.add(nocorrect1button);

    overridebuttongroup.add(nocorrect0button);
    nocorrect0button.setText("Everyone 0 out of 0");
    nocorrect0button.setOpaque(false);
    jPanel3.add(nocorrect0button);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 2;
    pinkpanel.add(jPanel3, gridBagConstraints);

    overridepanel.add(pinkpanel, java.awt.BorderLayout.CENTER);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
    jPanel4.add(overridepanel, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
    add(jPanel4, gridBagConstraints);
  }// </editor-fold>//GEN-END:initComponents

  private void titlefieldActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_titlefieldActionPerformed
  {//GEN-HEADEREND:event_titlefieldActionPerformed
    
    // TODO add your handling code here:
  }//GEN-LAST:event_titlefieldActionPerformed

  
  private Window getWindow()
  {
    Component c = this;
    while ( c.getParent() != null )
    {
      c = c.getParent();
      if ( c instanceof Window )
        return (Window)c;
    }
    return null;
  }
  
  private void overridecheckboxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_overridecheckboxActionPerformed
  {//GEN-HEADEREND:event_overridecheckboxActionPerformed
    Window w = getWindow();
    
    if ( overridecheckbox.isSelected() )
    {
      // make the buttons
      if ( overrideitem != null )
        setControlsToItem( overrideitem, true );
      else
        setControlsToItem( item, true );
//        overrideitem = overrides.copyItem( item );
//        setItem( overrideitem, true );
      overridepanel.setVisible(true);
    }
    else
    {
      overridepanel.setVisible(false);
    }
    
    if ( w != null )
    {
      w.pack();
      if ( w.getHeight() > 600 )
        w.setSize( w.getWidth(), 600 );
    }
  }//GEN-LAST:event_overridecheckboxActionPerformed


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.ButtonGroup buttongroup;
  private javax.swing.JLabel correctlabel;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JPanel jPanel2;
  private javax.swing.JPanel jPanel3;
  private javax.swing.JPanel jPanel4;
  private javax.swing.JRadioButton nocorrect0button;
  private javax.swing.JRadioButton nocorrect1button;
  private javax.swing.ButtonGroup overridebuttongroup;
  private javax.swing.JCheckBox overridecheckbox;
  private javax.swing.JPanel overridepanel;
  private javax.swing.JPanel overrideradiobuttonpanel;
  private javax.swing.JPanel pinkpanel;
  private javax.swing.JPanel radiobuttonpanel;
  private javax.swing.JTextField titlefield;
  private javax.swing.JLabel titlelabel;
  // End of variables declaration//GEN-END:variables
}
