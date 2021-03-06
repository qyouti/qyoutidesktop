/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti;

import java.awt.*;
import org.qyouti.templates.*;

/**
 *
 * @author jon
 */
public class QuestionEditDialog
        extends javax.swing.JDialog
{
  ItemTemplate template;
  QyoutiFrame parent;
  
  /**
   * Creates new form QuestionEditDialog
   */
  public QuestionEditDialog( QyoutiFrame parent, boolean modal )
  {
    super( parent, modal );
    this.parent = parent;
    initComponents();
  }

  public void setTemplate( ItemTemplate template )
  {
    this.template = template;
    centrepanel.add( template.getComponent() );
    this.pack();
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

    centrepanel = new javax.swing.JPanel();
    bottompanel = new javax.swing.JPanel();
    savebutton = new javax.swing.JButton();
    cancelbutton = new javax.swing.JButton();
    menubar = new javax.swing.JMenuBar();
    editmenu = new javax.swing.JMenu();
    viewmenu = new javax.swing.JMenu();
    formatmenu = new javax.swing.JMenu();

    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    setTitle("Question Edit");
    getContentPane().add(centrepanel, java.awt.BorderLayout.CENTER);

    savebutton.setText("O.K.");
    savebutton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        savebuttonActionPerformed(evt);
      }
    });
    bottompanel.add(savebutton);

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

    editmenu.setText("Edit");
    menubar.add(editmenu);

    viewmenu.setText("View");
    menubar.add(viewmenu);

    formatmenu.setText("Format");
    menubar.add(formatmenu);

    setJMenuBar(menubar);

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void cancelbuttonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cancelbuttonActionPerformed
  {//GEN-HEADEREND:event_cancelbuttonActionPerformed
    // TODO add your handling code here:
    this.setVisible( false );
    this.dispose();
  }//GEN-LAST:event_cancelbuttonActionPerformed

  private void savebuttonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_savebuttonActionPerformed
  {//GEN-HEADEREND:event_savebuttonActionPerformed
    if ( template.isChanged() )
    {
      template.store( false );
      parent.questionEditStored( false );
    }
    if ( template.isOverrideChanged() )
    {
      template.store( true );
      parent.questionEditStored( true );
    }
    this.setVisible( false );
    this.dispose();
  }//GEN-LAST:event_savebuttonActionPerformed


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JPanel bottompanel;
  private javax.swing.JButton cancelbutton;
  private javax.swing.JPanel centrepanel;
  private javax.swing.JMenu editmenu;
  private javax.swing.JMenu formatmenu;
  private javax.swing.JMenuBar menubar;
  private javax.swing.JButton savebutton;
  private javax.swing.JMenu viewmenu;
  // End of variables declaration//GEN-END:variables
}
