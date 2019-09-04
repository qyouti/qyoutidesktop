/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti.crypto;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.bcpg.PublicKeyPacket;
import org.bouncycastle.bcpg.RSAPublicBCPGKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureGenerator;
import org.bouncycastle.openpgp.operator.KeyFingerPrintCalculator;
import org.bouncycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.bc.BcPBESecretKeyDecryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPPrivateKey;
import org.qyouti.QyoutiPreferences;
import org.qyouti.compositefile.EncryptedCompositeFileUser;
import org.qyouti.compositefile.demo.KeyUtil;
import org.qyouti.winselfcert.WindowsCertificateGenerator;

/**
 *
 * @author maber01
 */
public class CryptographyManager
{
  QyoutiPreferences prefs;
  String useralias;
  boolean usewindows;
  
  KeyStore windowsKeyStore;
  PrivateKey windowsPrivateKey;
  
  File pgpseckeyfile, pgppubkeyfile;
  PGPPrivateKey  prikey;
  PGPPublicKey  pubkey;
  
  EncryptedCompositeFileUser user;
          
  PGPSecretKeyRingCollection secringcoll;
  PGPPublicKeyRingCollection pubringcoll;
  KeyFingerPrintCalculator fpcalc = new BcKeyFingerprintCalculator();
  BcPBESecretKeyDecryptorBuilder seckeydecbuilder = new BcPBESecretKeyDecryptorBuilder(  new BcPGPDigestCalculatorProvider() );

  
  public CryptographyManager( File base, QyoutiPreferences prefs )
  {
    this.prefs = prefs;
    
    useralias = prefs.getProperty("qyouti.crypto.useralias", null);
    usewindows = "yes".equalsIgnoreCase(prefs.getProperty("qyouti.crypto.usewindows", "no"));
    pgpseckeyfile = new File( base, "seckeyring.gpg" );
    pgppubkeyfile = new File( base, "pubkeyring.gpg" );
    Security.addProvider(new BouncyCastleProvider());
  }
  
  public boolean isWindowsAvailable()
  {
    return windowsKeyStore != null;
  }

  public String getUserAlias()
  {
    return useralias;
  }

  public boolean isKeyStoreWindows()
  {
    return usewindows;
  }
  
  
  
  public EncryptedCompositeFileUser getUser()
  {
    return user;
  }
  
  public void init()
  {
    FileInputStream fin;

    if ( pgpseckeyfile.exists() )
    {
      try
      {
        fin = new FileInputStream( pgpseckeyfile );
        secringcoll = new PGPSecretKeyRingCollection( fin, fpcalc );
        fin.close();
      }
      catch ( Exception e )
      {
        e.printStackTrace();
        secringcoll = null;
      }
    }
    
    if ( secringcoll == null )
    {
      try
      {
        secringcoll = new PGPSecretKeyRingCollection( new ArrayList<>() );
      }
      catch ( Exception e )
      {
        e.printStackTrace();
      }
    }
      
    if ( pgppubkeyfile.exists() )
    {
      try
      {
        fin = new FileInputStream( pgppubkeyfile );
        pubringcoll = new PGPPublicKeyRingCollection( fin, fpcalc );
        fin.close();
      }
      catch ( Exception e )
      {
        e.printStackTrace();
        pubringcoll = null;
      }
    }
    
    if ( pubringcoll == null )
    {
      try
      {
        pubringcoll = new PGPPublicKeyRingCollection( new ArrayList<>() );
      }
      catch ( Exception e )
      {
        e.printStackTrace();
      }
    }
      


    if ( useralias == null || usewindows )
    {
      try
      {
        windowsKeyStore = KeyStore.getInstance("Windows-MY");
        windowsKeyStore.load(null, null);  // Load keystore 
      }
      catch ( Exception e )
      {
        // indicate that Windows key store is not available.
        windowsKeyStore = null;
      }    
    }
    loadUserKeys();
  }
  
  public PGPPublicKey getPublicKey( String name ) throws PGPException
  {
    if ( pubringcoll == null ) return null;
    Iterator<PGPPublicKeyRing> it = pubringcoll.getKeyRings(name);
    PGPPublicKeyRing keyring;
    if ( !it.hasNext() )
      return null;
    keyring = it.next();
    if ( it.hasNext() )
      return null;
    return keyring.getPublicKey();
  }
  
  public PGPPrivateKey getPrivateKey( String name, char[] passphrase ) throws PGPException
  {
    if ( secringcoll == null ) return null;
    Iterator<PGPSecretKeyRing> it = secringcoll.getKeyRings(name);
    PGPSecretKeyRing keyring;
    if ( !it.hasNext() )
      return null;
    keyring = it.next();
    if ( it.hasNext() )
      return null;
    PBESecretKeyDecryptor dec = seckeydecbuilder.build(passphrase);
    return keyring.getSecretKey().extractPrivateKey(dec);
  }
  
  
  public void loadUserKeys()
  {
    if ( useralias == null )
      return;

    if ( usewindows )
      try {
        windowsPrivateKey = (PrivateKey)windowsKeyStore.getKey( useralias, null );
    }
    catch (KeyStoreException ex) {
      Logger.getLogger(CryptographyManager.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (NoSuchAlgorithmException ex) {
      Logger.getLogger(CryptographyManager.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (UnrecoverableKeyException ex) {
      Logger.getLogger(CryptographyManager.class.getName()).log(Level.SEVERE, null, ex);
    }
    
    try
    {
      prikey = getPrivateKey(useralias, "fredfred!".toCharArray() );      
      pubkey = getPublicKey(useralias);
    }
    catch (PGPException ex)
    {
      Logger.getLogger(CryptographyManager.class.getName()).log(Level.SEVERE, null, ex);
    }
    
    if ( usewindows )
      user = new EncryptedCompositeFileUser( useralias, windowsPrivateKey, windowsKeyStore.getProvider(), pubkey );
    else
      user = new EncryptedCompositeFileUser( useralias, prikey, pubkey );
    
  }
  
  
  public void createNewKeys( String alias, boolean win ) throws CryptographyManagerException
  {
    if ( win )
      createNewWindowsKeys( alias );
    else
      createNewPGPKeys( alias );
    prefs.setProperty( "qyouti.crypto.useralias", alias);
    prefs.setProperty( "qyouti.crypto.usewindows", win?"yes":"no" );
    prefs.save();
  }
  
  public void createNewPGPKeys( String alias ) throws CryptographyManagerException
  {
  }
  
  public void createNewWindowsKeys( String alias ) throws CryptographyManagerException
  {
    WindowsCertificateGenerator wcg = new WindowsCertificateGenerator();
    BigInteger serial;
    try
    {
      
      serial = wcg.generateSelfSignedCertificate(
              "CN=" + alias,
              "qyouti-" + UUID.randomUUID().toString(),
              WindowsCertificateGenerator.MS_ENH_RSA_AES_PROV,
              WindowsCertificateGenerator.PROV_RSA_AES,
              true,
              2048,
              WindowsCertificateGenerator.CRYPT_USER_PROTECTED
      );
      if (serial == null)
      {
        System.out.println("Failed to make certificate.");
        return;
      }
      else
      {
        System.out.println("Serial number = " + serial.toString(16) );
        System.out.println("As long = " + Long.toHexString( serial.longValue() ) );        
      }


      // convert the public to PGPPublicKey 
      RSAPublicKey winpubkey = (RSAPublicKey)wcg.getPublickey();
      RSAPublicBCPGKey rsapubkey = new RSAPublicBCPGKey( winpubkey.getModulus(), winpubkey.getPublicExponent());
      PublicKeyPacket pubpacket = new PublicKeyPacket( PublicKeyPacket.RSA_GENERAL, new Date(System.currentTimeMillis()), rsapubkey );
      PGPPublicKey pgppublickey = new PGPPublicKey( pubpacket, new BcKeyFingerprintCalculator() );
      
      System.out.println(" Converted key id = " + Long.toHexString(pgppublickey.getKeyID()) );

      // wrap the JCA private key so it can be used in bouncy castle
      // There is a problem with passing in a keyid derived from the Windows CAPI
      // serial number. The public key has a 'natural' id which is calculated from
      // its fingerprint. Use that.
      JcaPGPPrivateKey prik = new JcaPGPPrivateKey( pgppublickey.getKeyID(), wcg.getPrivatekey() );
      
      
      //BcPGPKeyConverter conv = new BcPGPKeyConverter();
      //PGPPublicKey pgppublickey = conv.getPGPPublicKey(PublicKeyAlgorithmTags.RSA_GENERAL, null,  );
              
      // Add ID and sign it with own (wrapped JCA) private key.
      JcaPGPContentSignerBuilder signerbuilder = new JcaPGPContentSignerBuilder( pgppublickey.getAlgorithm(), HashAlgorithmTags.SHA1 );
      PGPSignatureGenerator siggen = new PGPSignatureGenerator( signerbuilder );
      siggen.init(PGPSignature.DEFAULT_CERTIFICATION, prik );
      PGPSignature certification = siggen.generateCertification( alias, pgppublickey );      
      PGPPublicKey signedpgppublickey = PGPPublicKey.addCertification( pgppublickey, alias, certification );
              
      // Put the signed public key in a key ring
      ArrayList<PGPPublicKey> keylist = new ArrayList<>();
      keylist.add(signedpgppublickey);
      PGPPublicKeyRing keyring = new PGPPublicKeyRing(keylist);

      pubringcoll = PGPPublicKeyRingCollection.addPublicKeyRing( pubringcoll, keyring );
      
      FileOutputStream fout = new FileOutputStream( pgppubkeyfile );
      pubringcoll.encode(fout);
      fout.close();      
    }
    catch (Exception e)
    {
      e.printStackTrace(System.out);
      throw new CryptographyManagerException( "Unable" );
    }
    
    loadUserKeys();
  }  
}
