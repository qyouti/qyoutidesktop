/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti;

import java.net.*;
import org.apache.batik.dom.*;
import org.qyouti.qti1.element.*;
import org.qyouti.qti1.gui.*;
import org.qyouti.util.*;

/**
 *
 * @author jon
 */
public class QuestionPreviewDialog
        extends javax.swing.JDialog
{
  QTIElementItem item = null;
  QTIItemRenderer renderer = null;
  URI examfolderuri;
  QTIRenderOptions options;

  /**
   * Creates new form QuestionPreviewDialog
   */
  public QuestionPreviewDialog( java.awt.Frame parent, boolean modal, URI examfolderuri, QTIRenderOptions options )
  {
    super( parent, modal );
    this.examfolderuri = examfolderuri;
    this.options = options;
    getRootPane().setDefaultButton(closebutton);    
    initComponents();
  }

  
    public void setItem( QTIElementItem item, int qnumber )
    {
        this.item = item;

        renderer = new QTIItemRenderer( 
            null, examfolderuri, item, qnumber, options, null );
        if ( renderer == null )
        {
            previewcanvas.setSVGDocument(null);
            return;
        }

        GenericDocument svg = (GenericDocument) renderer.getPreviewSVGDocument( options );
        QyoutiUtils.dumpXMLFile( "/home/jon/Desktop/debug.svg", svg.getDocumentElement(), true );

        previewcanvas.setDocument( svg );
        // how to set zoom factor so page fits to width?
        //previewcanvas.
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

    scrollpane = new javax.swing.JScrollPane();
    previewcanvas = new org.apache.batik.swing.JSVGCanvas();
    bottompanel = new javax.swing.JPanel();
    closebutton = new javax.swing.JButton();

    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    setTitle("Question Preview");

    scrollpane.setViewportView(previewcanvas);

    getContentPane().add(scrollpane, java.awt.BorderLayout.CENTER);

    closebutton.setText("Close");
    closebutton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        closebuttonActionPerformed(evt);
      }
    });
    bottompanel.add(closebutton);

    getContentPane().add(bottompanel, java.awt.BorderLayout.SOUTH);

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void closebuttonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_closebuttonActionPerformed
  {//GEN-HEADEREND:event_closebuttonActionPerformed
    setVisible( false );
    dispose();
  }//GEN-LAST:event_closebuttonActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JPanel bottompanel;
  private javax.swing.JButton closebutton;
  private org.apache.batik.swing.JSVGCanvas previewcanvas;
  private javax.swing.JScrollPane scrollpane;
  // End of variables declaration//GEN-END:variables
}
