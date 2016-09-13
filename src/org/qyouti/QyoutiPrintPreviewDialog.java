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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * QyoutiQuestionDialog.java
 *
 * Created on 08-Apr-2010, 22:03:11
 */
package org.qyouti;

import java.awt.print.PrinterException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JScrollPane;
import org.apache.batik.dom.*;
import org.apache.batik.dom.util.DOMUtilities;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.print.PrintTranscoder;
import org.qyouti.data.CandidateData;
import org.qyouti.qti1.element.QTIElementItem;
import org.qyouti.qti1.gui.*;
import org.qyouti.qti1.gui.QTIItemRenderer;
import org.qyouti.util.QyoutiUtils;
import org.w3c.dom.Document;
import org.w3c.dom.svg.SVGDocument;

/**
 *
 * @author jon
 */
public class QyoutiPrintPreviewDialog extends javax.swing.JDialog
{
    Vector<QTIElementItem> items = null;
    Vector<GenericDocument> paginated=null;

    QTIItemRenderer renderer = null;
    URI examfolderuri;
    QTIRenderOptions options;
    String preamble;
    int page=0;

    /** Creates new form QyoutiQuestionDialog
     * @param parent
     * @param modal 
     */
    public QyoutiPrintPreviewDialog(
        java.awt.Frame parent,
        boolean modal,
        URI examfolderuri,
        QTIRenderOptions options,
        String preamble )
    {
        super(parent, modal);
        this.examfolderuri = examfolderuri;
        this.options = options;
        this.preamble = preamble;
        getRootPane().setDefaultButton(closeButton);
        initComponents();
        
    }

    public void setItems( Vector<QTIElementItem> items )
    {
//examfolderuri
        QuestionMetricsRecordSet qmrecset = new QuestionMetricsRecordSet("dummyprintid");
        qmrecset.setMonochromePrint( false );
      paginated = QTIItemRenderer.paginateItems( 
          null, "dummyid", examfolderuri,
          new CandidateData( null, "A.Student", "00000000", false ),
          options, 
          //qmrecset, 
          null,
              preamble );
      //QyoutiUtils.dumpXMLFile( "/home/jon/Desktop/debug.svg", paginated.get(0).getDocumentElement(), true );

      previewcanvas.setDocument( paginated.firstElement() );
      page = 0;
      upbutton.setEnabled(false);
      downbutton.setEnabled( paginated.size() > 1 );
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    toppanel = new javax.swing.JPanel();
    centrepanel = new javax.swing.JPanel();
    jScrollPane1 = new javax.swing.JScrollPane();
    previewcanvas = new org.apache.batik.swing.JSVGCanvas();
    bottompanel = new javax.swing.JPanel();
    upbutton = new javax.swing.JButton();
    downbutton = new javax.swing.JButton();
    printButton = new javax.swing.JButton();
    closeButton = new javax.swing.JButton();

    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    setName("Form"); // NOI18N
    getContentPane().setLayout(new java.awt.BorderLayout(1, 0));

    toppanel.setName("toppanel"); // NOI18N
    toppanel.setLayout(new java.awt.BorderLayout());

    centrepanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5), javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED)));
    centrepanel.setName("centrepanel"); // NOI18N
    centrepanel.setLayout(new java.awt.BorderLayout());

    jScrollPane1.setName("jScrollPane1"); // NOI18N

    org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(org.qyouti.QyoutiApp.class).getContext().getResourceMap(QyoutiPrintPreviewDialog.class);
    previewcanvas.setBackground(resourceMap.getColor("previewcanvas.background")); // NOI18N
    previewcanvas.setEnableRotateInteractor(false);
    previewcanvas.setName("previewcanvas"); // NOI18N
    previewcanvas.setRecenterOnResize(false);
    jScrollPane1.setViewportView(previewcanvas);

    centrepanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);

    toppanel.add(centrepanel, java.awt.BorderLayout.CENTER);

    bottompanel.setName("bottompanel"); // NOI18N

    upbutton.setText(resourceMap.getString("upbutton.text")); // NOI18N
    upbutton.setName("upbutton"); // NOI18N
    upbutton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        upbuttonActionPerformed(evt);
      }
    });
    bottompanel.add(upbutton);

    downbutton.setText(resourceMap.getString("downbutton.text")); // NOI18N
    downbutton.setName("downbutton"); // NOI18N
    downbutton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        downbuttonActionPerformed(evt);
      }
    });
    bottompanel.add(downbutton);

    printButton.setText(resourceMap.getString("printButton.text")); // NOI18N
    printButton.setName("printButton"); // NOI18N
    printButton.setPreferredSize(null);
    printButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        printButtonActionPerformed(evt);
      }
    });
    bottompanel.add(printButton);

    closeButton.setText(resourceMap.getString("closeButton.text")); // NOI18N
    closeButton.setName("closeButton"); // NOI18N
    closeButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        closeButtonActionPerformed(evt);
      }
    });
    bottompanel.add(closeButton);

    toppanel.add(bottompanel, java.awt.BorderLayout.SOUTH);

    getContentPane().add(toppanel, java.awt.BorderLayout.CENTER);

    pack();
  }// </editor-fold>//GEN-END:initComponents

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_closeButtonActionPerformed
    {//GEN-HEADEREND:event_closeButtonActionPerformed
        // TODO add your handling code here:
        dispose();

    }//GEN-LAST:event_closeButtonActionPerformed

    private void printButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_printButtonActionPerformed
    {//GEN-HEADEREND:event_printButtonActionPerformed
      // TODO add your handling code here:
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
      TranscoderInput tinput;
      for ( int i=0; i<paginated.size(); i++ )
      {
        tinput = new TranscoderInput( paginated.elementAt(i) );
        printtranscoder.transcode( tinput, new TranscoderOutput() );
      }
      printtranscoder.print();
    } catch (Exception ex)
    {
      Logger.getLogger(QyoutiPrintPreviewDialog.class.getName()).log(Level.SEVERE, null, ex);
    }
    }//GEN-LAST:event_printButtonActionPerformed

    private void upbuttonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_upbuttonActionPerformed
    {//GEN-HEADEREND:event_upbuttonActionPerformed
      // TODO add your handling code here:
      page--;
      previewcanvas.setDocument( paginated.elementAt(page) );
      upbutton.setEnabled( page > 0 );
      downbutton.setEnabled( page < (paginated.size()-1) );

    }//GEN-LAST:event_upbuttonActionPerformed

    private void downbuttonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_downbuttonActionPerformed
    {//GEN-HEADEREND:event_downbuttonActionPerformed
      // TODO add your handling code here:
      page++;
      previewcanvas.setDocument( paginated.elementAt(page) );
      upbutton.setEnabled( page > 0 );
      downbutton.setEnabled( page < (paginated.size()-1) );;

    }//GEN-LAST:event_downbuttonActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JPanel bottompanel;
  private javax.swing.JPanel centrepanel;
  private javax.swing.JButton closeButton;
  private javax.swing.JButton downbutton;
  private javax.swing.JScrollPane jScrollPane1;
  private org.apache.batik.swing.JSVGCanvas previewcanvas;
  private javax.swing.JButton printButton;
  private javax.swing.JPanel toppanel;
  private javax.swing.JButton upbutton;
  // End of variables declaration//GEN-END:variables
}
