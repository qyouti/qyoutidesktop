/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.quipto.compositefile.EncryptedCompositeFileUser;
import org.quipto.passwords.WindowsPasswordHandler;
import org.qyouti.crypto.CryptographyManager;
import org.qyouti.data.KeyData;

/**
 *
 * @author maber01
 */
public class IdentityDialog
        extends javax.swing.JDialog
{
  CryptographyManager cryptoman;

  private static final DateFormat df = new SimpleDateFormat( "HH:mm dd/MMM/yyyy" );

  private KeyData secretentries;
  private KeyData publicentries;
  
  /**
   * Creates new form IdentityDialog
   * @param parent
   * @param cryptoman
   */
  public IdentityDialog(Frame parent, CryptographyManager cryptoman )
  {
    super(parent, true);
    this.setTitle("Qyouti - Manage Personal Key Store");
    this.cryptoman = cryptoman;
    secretentries = new KeyData( cryptoman );
    publicentries = new KeyData( cryptoman );
    initComponents();    
    secretkeypairlist.setModel(secretentries);
    trustedpublickeylist.setModel(publicentries);
    updateFields();
  }


//  private void sortEntries()
//  {
//    entries.sort( new Comparator<Entry>() {
//      @Override
//      public int compare(Entry o1, Entry o2)
//      {
//        long da = 0L;
//        long db = 0L;
//        if ( o1.creationdate != null )
//          da = o1.creationdate.getTime();
//        if ( o2.creationdate != null )
//          db = o2.creationdate.getTime();
//        if ( da < db ) return -1;
//        if ( da > db ) return 1;
//        return 0;
//      }
//    } );
//  }
  
 
  private final void updateButtons()
  {
    int selectedpane = tabbedpane.getSelectedIndex();
    int selecteditem;
    if ( selectedpane == 0 )
      selecteditem = secretkeypairlist.getSelectedIndex();
    else
      selecteditem = trustedpublickeylist.getSelectedIndex();
    createbutton.setEnabled( selectedpane == 0  );
    activatebutton.setEnabled( selectedpane == 0 && selecteditem >= 0 );    
    exportbutton.setEnabled( selectedpane == 0 && selecteditem >= 0 );
    importbutton.setEnabled( selectedpane == 1 );
    deletebutton.setEnabled( selecteditem >= 0 );
  }
  
  private void updateFields()
  {
    secretentries.clear();
    publicentries.clear();
    
    PGPSecretKey preferredseckey = null;
    
    keystorelabel.setText("");
    protectiontypelabel.setText("");
    String alias = cryptoman.getPersonalAlias();
    activekeylabel.setText( alias==null?"no active key selected":alias );
    if ( cryptoman.personalKeyStoreFileExists() )
    {
      if ( cryptoman.getPersonalKeyStoreUser().getPasswordHandler() instanceof WindowsPasswordHandler )
        protectiontypelabel.setText( "Windows Cryptography" );
      else
        protectiontypelabel.setText( "Password" );
      keystorelabel.setText( cryptoman.getPersonalKeyStoreFile().getAbsolutePath() );
      PGPSecretKey[] seckeys;
      seckeys = cryptoman.getSecretKeys();
      for ( PGPSecretKey k : seckeys )
        secretentries.addKey( k );
      //sortEntries();
      preferredseckey = cryptoman.getPreferredSecretKey();
      
      PGPPublicKey[] pubkeys;
      pubkeys = cryptoman.getTrustedPublicKeys();
      for ( PGPPublicKey k : pubkeys )
        publicentries.addKey( k );
    }


    updateSelectedKeyPane();
    updateSelectedTrustedKeyPane();
    updateButtons();
  }

  private void updateSelectedKeyPane()
  {
    secretsplitpane.setRightComponent(null);
    int selected = secretkeypairlist.getSelectedIndex();
    if ( selected >= 0 )
    {
      PGPSecretKey seck = secretentries.getSecretKeyAt(selected);
      SecretKeyPanel skpanel = new SecretKeyPanel( cryptoman, seck );
      secretsplitpane.setRightComponent( skpanel );
    }    
    else
    {
      JPanel blank = new JPanel();
      blank.setLayout( new FlowLayout() );
      blank.add( new JLabel( cryptoman.personalKeyStoreFileExists()?"Create or select a key pair.":"No personal key store exists." ) );
      secretsplitpane.setRightComponent( blank ); 
      return;
    }
  }

  private void updateSelectedTrustedKeyPane()
  {
    trustedsplitpane.setRightComponent(null);
    int selected = trustedpublickeylist.getSelectedIndex();
    if ( selected >= 0 )
    {
      PGPPublicKey puck = publicentries.getKeyAt(selected);
      PublicKeyPanel pukpanel = new PublicKeyPanel( cryptoman, puck );
      trustedsplitpane.setRightComponent( pukpanel );
    }    
    else
    {
      JPanel blank = new JPanel();
      blank.setLayout( new FlowLayout() );
      blank.add( new JLabel( cryptoman.personalKeyStoreFileExists()?"Create or select a key pair.":"No personal key store exists." ) );
      trustedsplitpane.setRightComponent( blank ); 
      return;
    }
  }

  
  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
   * content of this method is always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents()
  {

    jLabel2 = new javax.swing.JLabel();
    jPanel3 = new javax.swing.JPanel();
    jLabel1 = new javax.swing.JLabel();
    keystorelabel = new javax.swing.JLabel();
    jLabel4 = new javax.swing.JLabel();
    protectiontypelabel = new javax.swing.JLabel();
    jLabel6 = new javax.swing.JLabel();
    activekeylabel = new javax.swing.JLabel();
    jPanel1 = new javax.swing.JPanel();
    tabbedpane = new javax.swing.JTabbedPane();
    secretsplitpane = new javax.swing.JSplitPane();
    jScrollPane1 = new javax.swing.JScrollPane();
    secretkeypairlist = new javax.swing.JList<>();
    trustedsplitpane = new javax.swing.JSplitPane();
    jScrollPane2 = new javax.swing.JScrollPane();
    trustedpublickeylist = new javax.swing.JList<>();
    jPanel2 = new javax.swing.JPanel();
    createbutton = new javax.swing.JButton();
    activatebutton = new javax.swing.JButton();
    exportbutton = new javax.swing.JButton();
    importbutton = new javax.swing.JButton();
    deletebutton = new javax.swing.JButton();
    closebutton = new javax.swing.JButton();

    jLabel2.setText("jLabel2");

    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

    jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
    jLabel1.setText("Personal Key Store:");

    jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
    jLabel4.setText("Protection:");

    jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
    jLabel6.setText("Active Key:");

    javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
    jPanel3.setLayout(jPanel3Layout);
    jPanel3Layout.setHorizontalGroup(
      jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel3Layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
          .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE))
        .addGap(18, 18, 18)
        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(keystorelabel, javax.swing.GroupLayout.DEFAULT_SIZE, 767, Short.MAX_VALUE)
          .addComponent(protectiontypelabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(activekeylabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        .addContainerGap())
    );
    jPanel3Layout.setVerticalGroup(
      jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel3Layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel1)
          .addComponent(keystorelabel))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel4)
          .addComponent(protectiontypelabel))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel6)
          .addComponent(activekeylabel))
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    getContentPane().add(jPanel3, java.awt.BorderLayout.NORTH);

    jPanel1.setMinimumSize(new java.awt.Dimension(650, 400));
    jPanel1.setPreferredSize(new java.awt.Dimension(650, 400));
    jPanel1.setLayout(new java.awt.BorderLayout());

    tabbedpane.setMinimumSize(new java.awt.Dimension(500, 300));
    tabbedpane.setPreferredSize(new java.awt.Dimension(500, 300));
    tabbedpane.addChangeListener(new javax.swing.event.ChangeListener()
    {
      public void stateChanged(javax.swing.event.ChangeEvent evt)
      {
        tabbedpaneStateChanged(evt);
      }
    });
    tabbedpane.addPropertyChangeListener(new java.beans.PropertyChangeListener()
    {
      public void propertyChange(java.beans.PropertyChangeEvent evt)
      {
        tabbedpanePropertyChange(evt);
      }
    });

    secretsplitpane.setDividerLocation(200);
    secretsplitpane.setDividerSize(10);
    secretsplitpane.setResizeWeight(0.5);

    secretkeypairlist.setModel(new javax.swing.AbstractListModel<String>()
    {
      String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
      public int getSize() { return strings.length; }
      public String getElementAt(int i) { return strings[i]; }
    });
    secretkeypairlist.addListSelectionListener(new javax.swing.event.ListSelectionListener()
    {
      public void valueChanged(javax.swing.event.ListSelectionEvent evt)
      {
        secretkeypairlistValueChanged(evt);
      }
    });
    jScrollPane1.setViewportView(secretkeypairlist);

    secretsplitpane.setLeftComponent(jScrollPane1);

    tabbedpane.addTab("Own Key Pairs", secretsplitpane);

    trustedsplitpane.setDividerLocation(200);
    trustedsplitpane.setDividerSize(10);
    trustedsplitpane.setResizeWeight(0.5);

    trustedpublickeylist.setModel(new javax.swing.AbstractListModel<String>()
    {
      String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
      public int getSize() { return strings.length; }
      public String getElementAt(int i) { return strings[i]; }
    });
    trustedpublickeylist.addListSelectionListener(new javax.swing.event.ListSelectionListener()
    {
      public void valueChanged(javax.swing.event.ListSelectionEvent evt)
      {
        trustedpublickeylistValueChanged(evt);
      }
    });
    jScrollPane2.setViewportView(trustedpublickeylist);

    trustedsplitpane.setLeftComponent(jScrollPane2);

    tabbedpane.addTab("Trusted Keys", trustedsplitpane);

    jPanel1.add(tabbedpane, java.awt.BorderLayout.CENTER);

    getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

    createbutton.setText("Create...");
    createbutton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        createbuttonActionPerformed(evt);
      }
    });
    jPanel2.add(createbutton);

    activatebutton.setText("Make Active");
    activatebutton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        activatebuttonActionPerformed(evt);
      }
    });
    jPanel2.add(activatebutton);

    exportbutton.setText("Export");
    exportbutton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        exportbuttonActionPerformed(evt);
      }
    });
    jPanel2.add(exportbutton);

    importbutton.setText("Import");
    importbutton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        importbuttonActionPerformed(evt);
      }
    });
    jPanel2.add(importbutton);

    deletebutton.setText("Delete");
    deletebutton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        deletebuttonActionPerformed(evt);
      }
    });
    jPanel2.add(deletebutton);

    closebutton.setText("Close");
    closebutton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        closebuttonActionPerformed(evt);
      }
    });
    jPanel2.add(closebutton);

    getContentPane().add(jPanel2, java.awt.BorderLayout.SOUTH);

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void closebuttonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_closebuttonActionPerformed
  {//GEN-HEADEREND:event_closebuttonActionPerformed
    dispose();
  }//GEN-LAST:event_closebuttonActionPerformed

  private void createbuttonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_createbuttonActionPerformed
  {//GEN-HEADEREND:event_createbuttonActionPerformed
    
    CreateIdentityDialog cid = new CreateIdentityDialog( (Frame)getParent(), cryptoman );
    cid.setModal( true );
    cid.setVisible( true );
    
    System.out.println( "CreateIdentityDialog created new identity." );
    
    updateFields();
  }//GEN-LAST:event_createbuttonActionPerformed

  private void deletebuttonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_deletebuttonActionPerformed
  {//GEN-HEADEREND:event_deletebuttonActionPerformed
    
    int selected = secretkeypairlist.getSelectedIndex();
    if ( selected < 0 )
    {
      JOptionPane.showMessageDialog( rootPane, "No key pair is selected." );
      return;
    }
    
    if ( 0 != JOptionPane.showConfirmDialog( rootPane, 
            "If you delete the key pair you will permanently lose \n" +
            "access to files protected with just your public key. \n" +
            "In the case of files which others also have access to \n" +
            "you may be able to regain access with their cooperation.\n\n" +
            "Are you sure you want to delete your key pair?",
            "Delete Key Pair",
            JOptionPane.YES_NO_OPTION ) )
      return;
    
    if ( !cryptoman.deleteKeyPair( secretentries.getSecretKeyAt( selected ) ) )
      JOptionPane.showMessageDialog( rootPane, "There was a technical fault attempting to delete your key pair." );
    updateFields();
    
  }//GEN-LAST:event_deletebuttonActionPerformed

  private void activatebuttonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_activatebuttonActionPerformed
  {//GEN-HEADEREND:event_activatebuttonActionPerformed
    Component c = tabbedpane.getSelectedComponent();
    if ( c != secretsplitpane )
      return;
    
    PGPSecretKey seckey;
    int selected = secretkeypairlist.getSelectedIndex();
    if ( selected < 0 )
    {
      JOptionPane.showMessageDialog( rootPane, "No key pair is selected." );
      return;
    }
    seckey = secretentries.getSecretKeyAt( selected );
    cryptoman.setPreferredSecretKey( seckey );
    updateFields();
    JOptionPane.showMessageDialog( rootPane, "The selected key has been made active and will automatically load in the future." );    
  }//GEN-LAST:event_activatebuttonActionPerformed

  private void secretkeypairlistValueChanged(javax.swing.event.ListSelectionEvent evt)//GEN-FIRST:event_secretkeypairlistValueChanged
  {//GEN-HEADEREND:event_secretkeypairlistValueChanged
    
    if ( evt.getValueIsAdjusting() ) return;
    System.out.println( "List selection event." );
    updateSelectedKeyPane();
    updateButtons();    
  }//GEN-LAST:event_secretkeypairlistValueChanged

  private void trustedpublickeylistValueChanged(javax.swing.event.ListSelectionEvent evt)//GEN-FIRST:event_trustedpublickeylistValueChanged
  {//GEN-HEADEREND:event_trustedpublickeylistValueChanged
    if ( evt.getValueIsAdjusting() ) return;
    System.out.println( "List selection event." );
    updateSelectedTrustedKeyPane();
    updateButtons();
  }//GEN-LAST:event_trustedpublickeylistValueChanged

  private void tabbedpanePropertyChange(java.beans.PropertyChangeEvent evt)//GEN-FIRST:event_tabbedpanePropertyChange
  {//GEN-HEADEREND:event_tabbedpanePropertyChange
    // TODO add your handling code here:
  }//GEN-LAST:event_tabbedpanePropertyChange

  private void tabbedpaneStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_tabbedpaneStateChanged
  {//GEN-HEADEREND:event_tabbedpaneStateChanged
    updateButtons();
  }//GEN-LAST:event_tabbedpaneStateChanged

  private void exportbuttonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_exportbuttonActionPerformed
  {//GEN-HEADEREND:event_exportbuttonActionPerformed
    
    int selected = secretkeypairlist.getSelectedIndex();
    if ( selected < 0 )
    {
      JOptionPane.showMessageDialog( rootPane, "No key pair is selected." );
      return;
    }
    PGPSecretKey seckey = secretentries.getSecretKeyAt( selected );
    String name = secretentries.getElementAt( selected );
    if ( seckey == null )
    {
      JOptionPane.showMessageDialog( rootPane, "Unable to access selected key pair." );
      return;
    }
    PGPPublicKey pubkey = seckey.getPublicKey();
    
    ByteArrayOutputStream baout = new ByteArrayOutputStream();
    try ( ArmoredOutputStream aout = new ArmoredOutputStream( baout ) )
    {
      pubkey.encode(aout);
    }
    catch ( IOException ioex )
    {
      JOptionPane.showMessageDialog( rootPane, "Technical issue extracting public key from key pair." );
      return;      
    }
    
    String armored = new String( baout.toByteArray() );
    System.out.println( new String( armored ) );
    StringSelection strsel = new StringSelection( armored );
    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(strsel, null);

    JOptionPane.showMessageDialog( rootPane, 
            "The public key has been extracted from the key pair \n" + name + 
                    "\nand placed on the clipboard. You can now paste the text into an email " +
                    "\nand send to someone who needs to establish a trust relationship with " +
                    "\nyour key. It is safe for anyone in the world to see this public key " +
                    "\nbecause it has been separated from the private key that makes up the pair." );
    
  }//GEN-LAST:event_exportbuttonActionPerformed

  private void importbuttonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_importbuttonActionPerformed
  {//GEN-HEADEREND:event_importbuttonActionPerformed
    
    ImportKeyDialog importkeydialog = new ImportKeyDialog( (Frame)getParent(), true );
    importkeydialog.setVisible( true );
    
    PGPPublicKey importingpubkey = importkeydialog.getPublicKey();
    if ( importingpubkey == null )
      return;
    
    PGPPublicKey[] pubkeys;
    pubkeys = cryptoman.getTrustedPublicKeys();
    for ( PGPPublicKey pkey : pubkeys )
    {
      if ( pkey.getKeyID() == importingpubkey.getKeyID() )
      {
        JOptionPane.showMessageDialog( rootPane, "The public key you want to import is already in your personal store." );
        return;      
      }
    }

    if ( !cryptoman.addTrustedPublicKey(importingpubkey) )
    {
      JOptionPane.showMessageDialog( rootPane, "There was a technical problem attempting to add the key to your personal store." );
      return;      
    }

    JOptionPane.showMessageDialog( rootPane, "Success - the key was imported into your personal store." );
    updateFields();  
  }//GEN-LAST:event_importbuttonActionPerformed

  
  private char[] promptForPassword()
  {
    JPanel panel = new JPanel();
    JLabel label = new JLabel("Enter a password:");
    JPasswordField pass = new JPasswordField();
    panel.setLayout(new FlowLayout() );
    pass.setMinimumSize( new Dimension(200,30) );
    pass.setPreferredSize( pass.getMinimumSize() );
    pass.setSize( pass.getMinimumSize() );
    panel.add(label);
    panel.add(pass);
    panel.doLayout();
    String[] options = new String[]{"O.K.", "Cancel"};
    int option = JOptionPane.showOptionDialog(null, panel, "Enter Password",
                             JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE,
                             null, options, options[1]);
    if(option == 0) // pressing OK button
      return pass.getPassword();
    return null;
  }
  
 


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton activatebutton;
  private javax.swing.JLabel activekeylabel;
  private javax.swing.JButton closebutton;
  private javax.swing.JButton createbutton;
  private javax.swing.JButton deletebutton;
  private javax.swing.JButton exportbutton;
  private javax.swing.JButton importbutton;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JLabel jLabel4;
  private javax.swing.JLabel jLabel6;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JPanel jPanel2;
  private javax.swing.JPanel jPanel3;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JScrollPane jScrollPane2;
  private javax.swing.JLabel keystorelabel;
  private javax.swing.JLabel protectiontypelabel;
  private javax.swing.JList<String> secretkeypairlist;
  private javax.swing.JSplitPane secretsplitpane;
  private javax.swing.JTabbedPane tabbedpane;
  private javax.swing.JList<String> trustedpublickeylist;
  private javax.swing.JSplitPane trustedsplitpane;
  // End of variables declaration//GEN-END:variables
}
