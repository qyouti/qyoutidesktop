/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti.crypto;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.tree.TreeModel;
import org.bouncycastle.bcpg.sig.KeyFlags;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureSubpacketVector;
import org.bouncycastle.util.Arrays;
import org.quipto.QuiptoStandards;
import org.qyouti.QyoutiPreferences;
import org.quipto.compositefile.EncryptedCompositeFile;
import org.quipto.compositefile.EncryptedCompositeFileUser;
import org.quipto.key.impl.CompositeFileKeyFinder;
import org.quipto.key.impl.CompositeFileKeyStore;
import org.quipto.key.impl.StandardRSAKeyBuilderSigner;
import org.quipto.passwords.WindowsPasswordHandler;
import org.quipto.trust.team.TeamTrust;

/**
 *
 * @author maber01
 */
public class CryptographyManager
{
  PasswordProvider pwprov;
  QyoutiPreferences prefs;
//  String useralias;
  char[] password;
  boolean windowsavailable=false;
  boolean usewindows;
  
  File pgpseckeyfile, pgppubkeyfile;
  File personalkeystorefile;
  String personalalias;
  CompositeFileKeyStore personalkeystore;
  CompositeFileKeyFinder personalkeyfinder;
  
  File teamkeystorefile;
  TeamTrust teamtrust;
  EncryptedCompositeFileUser eu;
  
  public CryptographyManager( File base, QyoutiPreferences prefs, PasswordProvider pwprov )
  {
    this.password = null;
    this.prefs = prefs;
    this.pwprov = pwprov;
    //preferredkeyfingerprint = prefs.getProperty("qyouti.crypto.preferredkey");
    personalalias = prefs.getProperty("qyouti.crypto.alias");
    personalkeystorefile = new File( base, "keystore.tar" );
    teamkeystorefile = null;
    Security.addProvider(new BouncyCastleProvider());
  }
  
  public TreeModel getTeamTreeModel()
  {
    return teamtrust.getTreeModel();
  }
  
  public boolean personalKeyStoreFileExists()
  {
    return personalkeystorefile.exists();
  }
  
  public File getPersonalKeyStoreFile()
  {
    return personalkeystorefile;
  }
  
  public String getPersonalAlias()
  {
    return personalalias;
  }
  
  public File getTeamKeyStoreFile()
  {
    return teamkeystorefile;
  }
  
  public EncryptedCompositeFile getEncryptedCompositeFile( File file, boolean create ) throws IOException
  {
    EncryptedCompositeFile compfile = new EncryptedCompositeFile( file, create, true, eu );
    try
    {
      compfile.initA();      
      compfile.initB();    
    }
    catch ( Exception e )
    {
      throw new IOException( "Unable to open encrypted file.", e );
    }
    return compfile;
  }
  
  
  public static Date getSecretKeyCreationDate( PGPSecretKey seckey )
  {
    PGPPublicKey pubkey = seckey.getPublicKey();
    return getPublicKeyCreationDate( pubkey );
  }
  
  public static Date getPublicKeyCreationDate( PGPPublicKey pubkey )
  {
    // Get self signatures - should only be one
    Iterator<PGPSignature> it = pubkey.getSignaturesForKeyID( pubkey.getKeyID() );
    PGPSignature sig;
    if ( it.hasNext() )
    {
      sig = it.next();
      PGPSignatureSubpacketVector subs = sig.getHashedSubPackets();
      Date d = subs.getSignatureCreationTime();
      if ( d != null )
        return d;
    }
    return null;
  }
  
  
  public void setPassword( char[] password )
  {
    if ( usewindows )
    {
//      try
//      {
//        password = loadWindowsEncryptedPassword();
//      }
//      catch (CryptographyManagerException ex)
//      {
//        Logger.getLogger(CryptographyManager.class.getName()).log(Level.SEVERE, null, ex);
//        password = null;
//      }
    }
    this.password = password;
  }
  
  public boolean isWindowsAvailable()
  {
    return windowsavailable;
  }

  public boolean hasEncryptedWindowsPassword( PGPSecretKey seckey )
  {
    return true;
  }
  
  public EncryptedCompositeFileUser getUser()
  {
    return eu;
  }
  
  public void init() throws CryptographyManagerException
  {
    
    try
    {
      try
      {
        KeyStore windowsKeyStore = KeyStore.getInstance("Windows-MY");
        windowsKeyStore.load(null, null);  // Load keystore 
        windowsavailable = true;
      }
      catch ( Exception e )
      {
        windowsavailable = false;
      }
      personalkeystore = openPersonalKeyStore();
    }
    catch (Exception ex)
    {
      Logger.getLogger(CryptographyManager.class.getName()).log(Level.SEVERE, null, ex);
      throw new CryptographyManagerException( "Unable to access key stores " );
    }
  }
  
  public void setTeamKeyRingFile( File f, boolean create ) throws IOException, NoSuchProviderException, NoSuchAlgorithmException, PGPException
  {
    if ( personalkeystore == null || personalkeyfinder == null )
      throw new IOException( "Cannot open team key store - no personal key store is loaded." );
    
    if ( teamtrust != null && teamkeystorefile != null && teamkeystorefile.getCanonicalPath().equals( f.getCanonicalPath() ) )
      return;
    if ( teamtrust != null )
      teamtrust.close();
    
    teamkeystorefile = f;
    teamtrust = new TeamTrust( personalalias, personalkeystore, personalkeyfinder, teamkeystorefile );
    eu = new EncryptedCompositeFileUser( teamtrust, teamtrust );
    if ( create )
    {
      PGPPublicKey mypublickey = teamtrust.getSecretKeyForSigning().getPublicKey();
      teamtrust.addRootPublicKeyToTeamStore( mypublickey );
    }
  }
  

  public PGPPublicKey findPublicKey(long keyid)
  {
    if ( teamtrust == null )
      return null;
    return teamtrust.findPublicKey(keyid);
  }

  public boolean addTrustedPublicKey( PGPPublicKey pubkey )
  {
    if ( personalkeystore == null )
      return false;
    
    StandardRSAKeyBuilderSigner signer = new StandardRSAKeyBuilderSigner();
    PGPSecretKeyRing seckeyring = personalkeystore.getSecretKeyRing(personalalias);
    PGPSecretKey signingseckey = personalkeyfinder.getSecretKeyForSigning();
    
    PGPPublicKey newpubkey = signer.signKey(
            personalkeyfinder.getPrivateKey(signingseckey), 
            pubkey, 
            KeyFlags.CERTIFY_OTHER | KeyFlags.ENCRYPT_STORAGE | KeyFlags.SIGN_DATA,
            StandardRSAKeyBuilderSigner.INCLUDE_SELF_SIGNATURE );
    
    ArrayList<PGPPublicKey> list = new ArrayList<>();
    list.add(newpubkey);
    PGPPublicKeyRing pubkeyring = new PGPPublicKeyRing( list );
    try
    {
      personalkeystore.setPublicKeyRing( pubkeyring );
    }
    catch (IOException ex)
    {
      Logger.getLogger(CryptographyManager.class.getName()).log(Level.SEVERE, null, ex);
      return false;
    }
    return true;
  }
  
  public boolean addPublicKeyToTeam( PGPPublicKey pubkey, boolean controller )
  {
    try
    {
      teamtrust.addPublicKeyToTeamStore(personalkeyfinder.getSecretKeyForSigning().getPublicKey(), pubkey, controller);
    }
    catch (IOException | NoSuchProviderException | NoSuchAlgorithmException ex)
    {
      Logger.getLogger(CryptographyManager.class.getName()).log(Level.SEVERE, null, ex);
      return false;
    }
    
    return true;
  }
  
  public PGPPublicKey[] getTrustedPublicKeys()
  {
    if ( personalkeystore == null )
      return new PGPPublicKey[0];
    List<PGPPublicKeyRing> list = personalkeystore.getAllPublicKeyRings();
    PGPPublicKey[] a = new PGPPublicKey[list.size()];
    for ( int i=0; i<a.length; i++ )
      a[i] = list.get(i).getPublicKey();
    return a;
  }
  
  public PGPPublicKey[] getTeamPublicKeys()
  {
    if ( teamtrust == null )
      return new PGPPublicKey[0];
    List<PGPPublicKeyRing> list = teamtrust.getAllTeamPublicKeyRings();
    PGPPublicKey[] a = new PGPPublicKey[list.size()];
    for ( int i=0; i<a.length; i++ )
      a[i] = list.get(i).getPublicKey();
    return a;
  }
  
  public PGPSecretKey[] getSecretKeys()
  {
    if ( personalkeystore == null )
      return new PGPSecretKey[0];
    List<PGPSecretKeyRing> list = personalkeystore.getAllSecretKeyRings();
    PGPSecretKey[] a = new PGPSecretKey[list.size()];
    for ( int i=0; i<a.length; i++ )
      a[i] = list.get(i).getSecretKey();
    return a;
  }
  
  public PGPSecretKey getPreferredSecretKey()
  {
    if ( personalkeyfinder == null ) return null;
    return personalkeyfinder.getSecretKeyForSigning();
  }
  
  public void setPreferredSecretKey( PGPSecretKey seckey )
  {
    personalalias = seckey.getUserIDs().next();
    prefs.setProperty("qyouti.crypto.alias", personalalias );
    prefs.save();
    try
    {
      personalkeyfinder = new CompositeFileKeyFinder( personalkeystore, personalalias, personalalias );
      personalkeyfinder.init();
    }
    catch (IOException | PGPException ex)
    {
      Logger.getLogger(CryptographyManager.class.getName()).log(Level.SEVERE, null, ex);
      personalkeyfinder = null;
    }
  }
  
  
  private CompositeFileKeyStore openPersonalKeyStore() throws IOException, PGPException, NoSuchProviderException, NoSuchAlgorithmException
  {
    EncryptedCompositeFileUser eu;
    
    try 
    {
      WindowsPasswordHandler winpasshandler = new WindowsPasswordHandler();
      eu = new EncryptedCompositeFileUser( winpasshandler );
    }
    catch ( KeyStoreException ksex )
    {
      eu = null;
    }
    if ( eu != null )
    {
      personalkeystore = new CompositeFileKeyStore( personalkeystorefile, eu );
      if ( personalalias != null )
      {
        personalkeyfinder = new CompositeFileKeyFinder( personalkeystore, personalalias, personalalias );
        personalkeyfinder.init();
      }
      return personalkeystore;
    }
    return null;
  }
  
  private void storePublicKey( boolean andexport, String alias, CompositeFileKeyStore keystore, PGPPublicKey key ) throws IOException, PGPException
  {
    if ( keystore == null )
      return;
    
    ArrayList<PGPPublicKey> keylist = new ArrayList<>();
    keylist.add(key);
    PGPPublicKeyRing keyring = new PGPPublicKeyRing(keylist);
    keystore.setPublicKeyRing(keyring);
    
    // export
    if ( andexport )
    {
      File file = new File( personalkeystorefile.getParentFile(), alias.replace(" ", "_") + "_selfsignedpublickey.gpg" );
      if ( file.exists() )
        file.delete();
      FileOutputStream fout = new FileOutputStream( file );
      key.encode( fout );
      fout.close();
    }
  }

  private void storeSecretKey( String alias, CompositeFileKeyStore keystore, PGPSecretKey key ) throws IOException, PGPException
  {
    if ( keystore == null )
      return;
    
    ArrayList<PGPSecretKey> keylist = new ArrayList<>();
    keylist.add(key);
    PGPSecretKeyRing keyring = new PGPSecretKeyRing(keylist);
    keystore.setSecretKeyRing(keyring);
    
    storePublicKey( false, alias, keystore, key.getPublicKey() );  
  }  
  
  public void createNewKeys( String alias, char[] password, boolean win ) throws CryptographyManagerException
  {
    if ( !win ) throw new CryptographyManagerException("Sorry - must use windows protection.");

    try
    {
      StandardRSAKeyBuilderSigner keybuilder = new StandardRSAKeyBuilderSigner();
      PGPSecretKey secretkey = keybuilder.buildSecretKey(alias, QuiptoStandards.SECRET_KEY_STANDARD_PASS);
      if ( personalkeystore == null )
        openPersonalKeyStore();
      if (secretkey != null)
        storeSecretKey(alias, personalkeystore, secretkey);
    }
    catch ( Exception e )
    {
      e.printStackTrace();
      throw new CryptographyManagerException( "Unable to create a new key pair." );
    }
    
  }

  public boolean deleteKeyPair( PGPSecretKey seckey )
  {
    return true;
  }
  

  
  public static String prettyPrintFingerprint( byte[] raw )
  {
    return printFingerprint( raw, " : " );
  }
  
  public static String printFingerprint( byte[] raw, String separator )
  {
    StringBuilder sb = new StringBuilder();
    int chunk = 2;
    int extra = raw.length % chunk;
    byte[] padded;
    ByteBuffer bb;
    if ( extra != 0 )
    {
      padded = new byte[raw.length + chunk-extra];
      bb = ByteBuffer.wrap( padded );
      byte[] more = new byte[chunk-extra];
      Arrays.clear(more);
      bb.put( more );
      bb.put( raw );
    }
    else
      bb = ByteBuffer.wrap( raw );
    
    bb.rewind();
    ShortBuffer ib = bb.asShortBuffer();
    
    for ( int i=0; ib.hasRemaining(); i++ )
    {
      if ( i>0 )
        sb.append( separator );
      String part = Integer.toHexString( ((int)ib.get()) & 0xffff ).toUpperCase();
      for ( int j=part.length(); j<4; j++ )
        part = "0" + part;
      sb.append( part );
    }
    
    return sb.toString();
  }
  
}
