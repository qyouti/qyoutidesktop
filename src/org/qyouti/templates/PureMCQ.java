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
import javax.swing.GroupLayout.ParallelGroup;
import org.qyouti.data.QuestionDefinitions;
import org.qyouti.qti1.element.*;

/**
 *
 * @author jon
 */
public class PureMCQ
        extends javax.swing.JPanel
        implements ItemTemplate
{
  boolean presentationeditenabled=false;
  boolean processingeditenabled=false;
  QTIElementItem item = null;
  int optioncount;
  boolean changed=false;

  String itemtitle;  // Title as it was when dialog was opened
  int itemoption;    // Option as it was...
  JRadioButton[] buttons;  // variable number of buttons - depends on no. opts

  MaterialEditorPanel stemfield;  
  MaterialEditorPanel[] optionfields;
  
  
  /**
   * Creates new instance
   */
  public PureMCQ()
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
    if ( override )
      return;
    
    int i;
    String correctident=null;
    
    item.setTitle( titlefield.getText() );
    
    stemfield.store();
    for ( i=0; i<optionfields.length; i++ )
      optionfields[i].store();
            
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
    stemfield.setEnabled( presentationeditenabled );
    for ( int i=0; i<optionfields.length; i++ )
      optionfields[i].setEnabled( presentationeditenabled );
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
    javax.swing.GroupLayout gl;
    GroupLayout.SequentialGroup sgh;
    GroupLayout.ParallelGroup pgh;
    GroupLayout.SequentialGroup sgv;
    GroupLayout.ParallelGroup pgv;
    int i;
    this.item = item;
    this.changed = false;
    
    // get hold of data and references from item
    itemtitle = item.getTitle();
    List<QTIElementResponselabel> l = item.getResponselabels();
    List<QTIElementMaterial> templist;
    ArrayList<QTIElementMaterial> resplabmats = new ArrayList<>();
    QTIElementMaterial stemmaterial = null;
    optioncount = l.size();
    for ( i=0; i<optioncount; i++ )
    {
      templist = l.get( i ).findElements( QTIElementMaterial.class, false );
      if ( templist.size() > 0 )
      {
        resplabmats.add( i, templist.get( 0 ) );
      }
    }
    
    templist = item.getPresentation().findElements( QTIElementMaterial.class, false );
    if ( templist.size() > 0 )
      stemmaterial = templist.get( 0 );

    buttons = new JRadioButton[optioncount];
    optionfields = new MaterialEditorPanel[optioncount];
    JLabel[] optionlabels = new JLabel[optioncount];
    itemoption = -1;
    radiobuttonpanel.removeAll();
    optionspanel.removeAll();

    stemfield = new MaterialEditorPanel( stemmaterial );
    gl = new javax.swing.GroupLayout(stempanel);
    stempanel.setLayout( gl );
    sgh = gl.createSequentialGroup();
    sgh.addComponent( stemfield );
    gl.setHorizontalGroup( sgh );
    sgv = gl.createSequentialGroup();
    sgv.addComponent( stemfield );
    gl.setVerticalGroup( sgv );
    
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
      
      optionlabels[i] = new JLabel( Character.toString( (char)('A' + i ) ) );
      optionfields[i] = new MaterialEditorPanel( resplabmats.get( i ) );
    }
    titlefield.setText( itemtitle );    

    gl = new javax.swing.GroupLayout(optionspanel);
    optionspanel.setLayout(gl);
    
    sgh = gl.createSequentialGroup();
    pgh = gl.createParallelGroup();
    for ( i=0; i<optioncount; i++ )
      pgh.addComponent( optionlabels[i] );
    sgh.addGroup( pgh );
    pgh = gl.createParallelGroup();
    for ( i=0; i<optioncount; i++ )
      pgh.addComponent( optionfields[i] );
    sgh.addGroup( pgh );    
    gl.setHorizontalGroup( sgh );
    
    sgv = gl.createSequentialGroup();
    for ( i=0; i<optioncount; i++ )
    {
      pgv = gl.createParallelGroup();
      pgv.addComponent( optionlabels[i] );
      pgv.addComponent( optionfields[i] );
      sgv.addGroup( pgv );
    }
    gl.setVerticalGroup( sgv );
    
    
    updateProcessingeditenabled();
    this.invalidate();
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
    return false;
  }

  void evaluateChange()
  {
    int i;
    int currentoption = -1;
    for ( i=0; i<optioncount; i++ )
      if ( buttons[i].isSelected() )
          currentoption = i;
    String currenttitle = titlefield.getText();
    changed =  currentoption != itemoption || !currenttitle.equals( itemtitle ) || stemfield.isChanged();
    for ( i=0; i<optionfields.length; i++ )
      changed = changed || optionfields[i].isChanged();
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

    buttongroup = new javax.swing.ButtonGroup();
    presentationpanel = new javax.swing.JPanel();
    titlelabel = new javax.swing.JLabel();
    titlefield = new javax.swing.JTextField();
    stemlabel = new javax.swing.JLabel();
    stempanel = new javax.swing.JPanel();
    optionslabel = new javax.swing.JLabel();
    optionspanel = new javax.swing.JPanel();
    responsepanel = new javax.swing.JPanel();
    correctlabel = new javax.swing.JLabel();
    radiobuttonpanel = new javax.swing.JPanel();

    java.awt.FlowLayout flowLayout1 = new java.awt.FlowLayout(java.awt.FlowLayout.LEFT);
    flowLayout1.setAlignOnBaseline(true);
    setLayout(flowLayout1);

    presentationpanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Presentation"));

    titlelabel.setText("Title:");

    titlefield.setColumns(50);
    titlefield.setMinimumSize(new java.awt.Dimension(200, 19));
    titlefield.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        titlefieldActionPerformed(evt);
      }
    });

    stemlabel.setText("Stem Text:");

    stempanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 15, 1, 1));

    javax.swing.GroupLayout stempanelLayout = new javax.swing.GroupLayout(stempanel);
    stempanel.setLayout(stempanelLayout);
    stempanelLayout.setHorizontalGroup(
      stempanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 0, Short.MAX_VALUE)
    );
    stempanelLayout.setVerticalGroup(
      stempanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 8, Short.MAX_VALUE)
    );

    optionslabel.setText("Option Text:");

    optionspanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 15, 1, 1));

    javax.swing.GroupLayout optionspanelLayout = new javax.swing.GroupLayout(optionspanel);
    optionspanel.setLayout(optionspanelLayout);
    optionspanelLayout.setHorizontalGroup(
      optionspanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 0, Short.MAX_VALUE)
    );
    optionspanelLayout.setVerticalGroup(
      optionspanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 15, Short.MAX_VALUE)
    );

    javax.swing.GroupLayout presentationpanelLayout = new javax.swing.GroupLayout(presentationpanel);
    presentationpanel.setLayout(presentationpanelLayout);
    presentationpanelLayout.setHorizontalGroup(
      presentationpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(presentationpanelLayout.createSequentialGroup()
        .addGap(20, 20, 20)
        .addGroup(presentationpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(titlelabel)
          .addComponent(stemlabel)
          .addComponent(optionslabel))
        .addGap(32, 32, 32)
        .addGroup(presentationpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
          .addComponent(titlefield, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(stempanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(optionspanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        .addGap(0, 10, Short.MAX_VALUE))
    );
    presentationpanelLayout.setVerticalGroup(
      presentationpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(presentationpanelLayout.createSequentialGroup()
        .addGap(5, 5, 5)
        .addGroup(presentationpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(titlelabel)
          .addComponent(titlefield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(presentationpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(stempanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(stemlabel))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addGroup(presentationpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(optionslabel)
          .addComponent(optionspanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addGap(0, 16, Short.MAX_VALUE))
    );

    add(presentationpanel);

    responsepanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Response Processing"));

    correctlabel.setText("Correct Answer:");
    responsepanel.add(correctlabel);
    responsepanel.add(radiobuttonpanel);

    add(responsepanel);
  }// </editor-fold>//GEN-END:initComponents

  private void titlefieldActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_titlefieldActionPerformed
  {//GEN-HEADEREND:event_titlefieldActionPerformed

    // TODO add your handling code here:
  }//GEN-LAST:event_titlefieldActionPerformed


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.ButtonGroup buttongroup;
  private javax.swing.JLabel correctlabel;
  private javax.swing.JLabel optionslabel;
  private javax.swing.JPanel optionspanel;
  private javax.swing.JPanel presentationpanel;
  private javax.swing.JPanel radiobuttonpanel;
  private javax.swing.JPanel responsepanel;
  private javax.swing.JLabel stemlabel;
  private javax.swing.JPanel stempanel;
  private javax.swing.JTextField titlefield;
  private javax.swing.JLabel titlelabel;
  // End of variables declaration//GEN-END:variables
}
