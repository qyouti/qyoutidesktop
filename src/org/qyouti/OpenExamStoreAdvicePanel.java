/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti;

/**
 *
 * @author maber01
 */
public class OpenExamStoreAdvicePanel
        extends javax.swing.JPanel
{

  /**
   * Creates new form NewExamStoreAdvicePanel
   */
  public OpenExamStoreAdvicePanel()
  {
    initComponents();
  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
   * content of this method is always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents()
  {

    jScrollPane1 = new javax.swing.JScrollPane();
    jTextPane1 = new javax.swing.JTextPane();

    setMaximumSize(new java.awt.Dimension(250, 250));
    setPreferredSize(new java.awt.Dimension(250, 250));
    setLayout(new java.awt.BorderLayout());

    jTextPane1.setEditable(false);
    jTextPane1.setContentType("text/html"); // NOI18N
    jTextPane1.setText("<html>\r\n  <head>\r\n\r\n  </head>\r\n  <body>\r\n    <h1>Help</h1>\n    <p style=\"margin-top: 0\">\r\n      Find a folder and select the <strong>Qyouti Store</strong> configuration file contained\n      in it. It will be named qyoutistore.tar.\n    </p>\r\n  </body>\r\n</html>\r\n");
    jScrollPane1.setViewportView(jTextPane1);

    add(jScrollPane1, java.awt.BorderLayout.CENTER);
  }// </editor-fold>//GEN-END:initComponents


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JTextPane jTextPane1;
  // End of variables declaration//GEN-END:variables
}