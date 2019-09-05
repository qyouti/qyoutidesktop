/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti.crypto;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.bcpg.PublicKeyPacket;
import org.bouncycastle.bcpg.RSAPublicBCPGKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPEncryptedData;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPKeyPair;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureGenerator;
import org.bouncycastle.openpgp.operator.KeyFingerPrintCalculator;
import org.bouncycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.bouncycastle.openpgp.operator.PGPDigestCalculator;
import org.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.bc.BcPBESecretKeyDecryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPDigestCalculatorProviderBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPKeyPair;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPPrivateKey;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyEncryptorBuilder;
import org.qyouti.QyoutiPreferences;
import org.qyouti.compositefile.EncryptedCompositeFile;
import org.qyouti.compositefile.EncryptedCompositeFileUser;
import org.qyouti.compositefile.demo.KeyUtil;
import org.qyouti.winselfcert.WindowsCertificateGenerator;
import static org.qyouti.winselfcert.WindowsCertificateGenerator.CRYPT_USER_PROTECTED;
import static org.qyouti.winselfcert.WindowsCertificateGenerator.MS_ENH_RSA_AES_PROV;
import static org.qyouti.winselfcert.WindowsCertificateGenerator.PROV_RSA_AES;

/**
 *
 * @author maber01
 */
public class CryptographyManager
{
  QyoutiPreferences prefs;
  String useralias;
  char[] password;
  boolean windowsavailable=false;
  boolean usewindows;
  
  File pgpseckeyfile, pgppubkeyfile, winpassfile;
  PGPPrivateKey  prikey;
  PGPPublicKey  pubkey;
  
  EncryptedCompositeFileUser user;
          
  PGPSecretKeyRingCollection secringcoll;
  PGPPublicKeyRingCollection pubringcoll;
  KeyFingerPrintCalculator fpcalc = new BcKeyFingerprintCalculator();
  BcPBESecretKeyDecryptorBuilder seckeydecbuilder = new BcPBESecretKeyDecryptorBuilder(  new BcPGPDigestCalculatorProvider() );

  
  public CryptographyManager( File base, QyoutiPreferences prefs )
  {
    this.password = null;
    this.prefs = prefs;
    
    useralias = prefs.getProperty("qyouti.crypto.useralias", null);
    usewindows = "yes".equalsIgnoreCase(prefs.getProperty("qyouti.crypto.usewindows", "no"));
    pgpseckeyfile = new File( base, "seckeyring.gpg" );
    pgppubkeyfile = new File( base, "pubkeyring.gpg" );
    winpassfile = new File( base, "seckeypassword.bin" );
    Security.addProvider(new BouncyCastleProvider());
  }
  
  public void setPassword( char[] password )
  {
    this.password = password;
  }
  
  public boolean isWindowsAvailable()
  {
    return windowsavailable;
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
  
  public void init() throws CryptographyManagerException
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
      
    try
    {
      KeyStore windowsKeyStore = KeyStore.getInstance("Windows-MY");
      windowsKeyStore.load(null, null);  // Load keystore 
      windowsavailable = true;
    }
    catch ( Exception e )
    {
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
  
  
  public void loadUserKeys() throws CryptographyManagerException
  {
    if ( useralias == null )
      return;

    if ( usewindows )
      password = loadWindowsEncryptedPassword();
      
    try
    {
      prikey = getPrivateKey(useralias, password );
      pubkey = getPublicKey(useralias);
    }
    catch (PGPException ex)
    {
      Logger.getLogger(CryptographyManager.class.getName()).log(Level.SEVERE, null, ex);
    }
    
    user = new EncryptedCompositeFileUser( useralias, prikey, pubkey );
    
  }
  
  
  public void createNewKeys( String alias, char[] password, boolean win ) throws CryptographyManagerException
  {
    if ( password == null || password.length == 0 )
    {
      try
      {
        password = EncryptedCompositeFile.generateRandomPassphrase();
      }
      catch (NoSuchAlgorithmException ex)
      {
        Logger.getLogger(CryptographyManager.class.getName()).log(Level.SEVERE, null, ex);
        throw new CryptographyManagerException( "Unable to generate password." );
      }
    }
    
    if ( win )
    {
      storeWindowsEncryptedPassword( password );
    }
    
    createNewPGPKeys( alias, password );
    prefs.setProperty( "qyouti.crypto.useralias", alias);
    prefs.setProperty( "qyouti.crypto.usewindows", win?"yes":"no" );
    prefs.save();
    useralias = alias;
    usewindows = win;
    loadUserKeys();
  }
  
    private void exportKeyPair(
          KeyPair pair,
          String alias,
          char[] passPhrase)
          throws IOException, InvalidKeyException, NoSuchProviderException, SignatureException, PGPException
  {

    PGPDigestCalculator sha1Calc = new JcaPGPDigestCalculatorProviderBuilder().build().get(HashAlgorithmTags.SHA1);
    PGPKeyPair keyPair = new JcaPGPKeyPair(PGPPublicKey.RSA_GENERAL, pair, new Date());
    PGPSecretKey secretKey = new PGPSecretKey(
            PGPSignature.DEFAULT_CERTIFICATION,
            keyPair,
            alias,
            sha1Calc,
            null,
            null,
            new JcaPGPContentSignerBuilder(
                    keyPair.getPublicKey().getAlgorithm(),
                    HashAlgorithmTags.SHA1),
            new JcePBESecretKeyEncryptorBuilder(
                    PGPEncryptedData.CAST5,
                    sha1Calc).setProvider("BC").build(passPhrase));
    PGPPublicKey key = secretKey.getPublicKey();

    ArrayList<PGPSecretKey> seckeylist = new ArrayList<>();
    seckeylist.add(secretKey);
    PGPSecretKeyRing secretKeyRing = new PGPSecretKeyRing(seckeylist);

    ArrayList<PGPPublicKey> keylist = new ArrayList<>();
    keylist.add(key);
    PGPPublicKeyRing keyring = new PGPPublicKeyRing(keylist);
    
    secringcoll = PGPSecretKeyRingCollection.addSecretKeyRing( secringcoll, secretKeyRing );
    pubringcoll = PGPPublicKeyRingCollection.addPublicKeyRing( pubringcoll, keyring );
    
    FileOutputStream fout;
    fout = new FileOutputStream( pgpseckeyfile );
    secringcoll.encode(fout);
    fout.close();
    
    fout = new FileOutputStream( pgppubkeyfile );
    pubringcoll.encode(fout);
    fout.close();
  }


  
  public void createNewPGPKeys( String alias, char[] password ) throws CryptographyManagerException
  {
    try
    {
      KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", "BC");
      kpg.initialize(2048);
      KeyPair kp = kpg.generateKeyPair();
      
      exportKeyPair( kp, alias, password );
    }
    catch (Exception ex)
    {
      Logger.getLogger(CryptographyManager.class.getName()).log(Level.SEVERE, null, ex);
      throw new CryptographyManagerException( "Unable to create key pair." );
    }
    
    
  }
  
  private char[] loadWindowsEncryptedPassword() throws CryptographyManagerException
  {
    try
    {
      KeyStore keyStore = KeyStore.getInstance("Windows-MY");
      keyStore.load(null, null);  // Load keystore 
      PrivateKey k = (PrivateKey)keyStore.getKey("My key pair for guarding passwords", null );    

      FileInputStream fin = new FileInputStream( winpassfile );
      ByteArrayOutputStream baout = new ByteArrayOutputStream();
      int b;
      while ( (b = fin.read()) >=0  )
        baout.write( b );
      fin.close();
      baout.close();

      Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
      cipher.init( Cipher.DECRYPT_MODE, k );
      byte[] decrypt = cipher.doFinal( baout.toByteArray() );
      System.out.println( "Password is: " + new String( decrypt, "UTF8" ) );
      return new String( decrypt, "UTF8" ).toCharArray();
    }
    catch ( Exception e )
    {
      e.printStackTrace();
    }
    return null;
  }
  
  private void storeWindowsEncryptedPassword( char[] password ) throws CryptographyManagerException
  {
    PublicKey pubkey;
    
    try
    {
      KeyStore keyStore = KeyStore.getInstance("Windows-MY");
      keyStore.load(null, null);  // Load keystore 
      Certificate c = keyStore.getCertificate( "My key pair for guarding passwords" );
      if ( c == null )
      {
        if ( !makeWindowsKeyPair( "My key pair for guarding passwords" ) )
          throw new CryptographyManagerException( "Unable to create Windows cryptography certificate." );
        keyStore.load(null, null);  // Load keystore 
        c = keyStore.getCertificate( "My key pair for guarding passwords" );
        if ( c == null )
          throw new CryptographyManagerException( "Unable to get Windows cryptography certificate." );
      }
      pubkey = c.getPublicKey();
    }
    catch ( Exception e )
    {
      e.printStackTrace();
      throw new CryptographyManagerException( "Technical problem trying to get Windows cryptography certificate." );
    }
    
    try
    {
      FileOutputStream fout = new FileOutputStream( winpassfile );
      Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
      cipher.init( Cipher.ENCRYPT_MODE, pubkey );
      byte[] crypt = cipher.doFinal( new String(password).getBytes() );
      FileOutputStream out = new FileOutputStream( winpassfile );
      out.write(crypt);
      out.close();
    }
    catch ( Exception ex )
    {
      Logger.getLogger(CryptographyManager.class.getName()).log(Level.SEVERE, null, ex);
      throw new CryptographyManagerException( "Technical problem trying to save encrypted password." );
    }
  }
  
  
  public static boolean makeWindowsKeyPair( String alias )
  {
    try
    {
      PublicKey pubk;
      BigInteger serial;
      WindowsCertificateGenerator wcg = new WindowsCertificateGenerator();
      
      serial = wcg.generateSelfSignedCertificate(
              "CN=" + alias,
              "qyouti-" + UUID.randomUUID().toString(),
              MS_ENH_RSA_AES_PROV,
              PROV_RSA_AES,
              true,
              2048,
              CRYPT_USER_PROTECTED
      );
      if (serial != null)
        return true;
    }
    catch ( Exception e )
    {
      e.printStackTrace();
    }
    return false;
  }
  
}
