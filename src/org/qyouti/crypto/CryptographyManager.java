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
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
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
import org.bouncycastle.util.Arrays;
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
  private static final String WINDOWS_CERTIFICATE_ALIAS = "Qyouti Certificate for Protecting Private Keys";
  
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
    if ( usewindows )
    {
      try
      {
        password = loadWindowsEncryptedPassword();
      }
      catch (CryptographyManagerException ex)
      {
        Logger.getLogger(CryptographyManager.class.getName()).log(Level.SEVERE, null, ex);
        password = null;
      }
    }
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

  public boolean isPrivateKeyWindowsProtected()
  {
    return usewindows;
  }
  
  
  
  public EncryptedCompositeFileUser getUser()
  {
    return user;
  }
  
  public boolean isUserReady()
  {
    if ( user == null ) return false;
    return user.getPgpprivatekey() != null;
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
    
    loadUser();
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
  
  private PGPPrivateKey getPrivateKey( String name, char[] passphrase ) throws PGPException
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
  
  public void unloadPrivateKey()
  {
    prikey = null;
    user = new EncryptedCompositeFileUser( useralias, null, pubkey );
    password = null;
  }
  
  public boolean loadPrivateKey()
  {
    prikey = null;
    if ( user == null || useralias == null )
      return false;
    
    try
    {
      if ( password == null )
        return false;
      
      prikey = getPrivateKey(useralias, password );
      user = new EncryptedCompositeFileUser( useralias, prikey, pubkey );
    }
    catch (Exception ex)
    {
      Logger.getLogger(CryptographyManager.class.getName()).log(Level.SEVERE, null, ex);
      prikey = null;
      return false;
    }
    return true;
  }
  
  public boolean loadUser()
  {
    prikey = null;
    pubkey = null;
    user = null;
      
    if ( useralias == null )
      return false;

    try
    {
      pubkey = getPublicKey(useralias);
      user = new EncryptedCompositeFileUser( useralias, null, pubkey );
    }
    catch (Exception ex)
    {
      prikey = null;
      pubkey = null;
      user = null;
      Logger.getLogger(CryptographyManager.class.getName()).log(Level.SEVERE, null, ex);
      return false;
    }
    
    return true;
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
    loadUser();
  }

  private void saveKeyRingCollections() throws IOException
  {
    FileOutputStream fout;
    fout = new FileOutputStream( pgpseckeyfile );
    secringcoll.encode(fout);
    fout.close();
    
    fout = new FileOutputStream( pgppubkeyfile );
    pubringcoll.encode(fout);
    fout.close();    
  }
  
  public boolean deleteKeyPair()
  {
    try
    {
      // get list of all secret key rings with right alias
      Iterator<PGPSecretKeyRing> it = secringcoll.getKeyRings( useralias );
      PGPSecretKeyRing secretkeyring;
      if ( !it.hasNext() )
        return false;
      secretkeyring = it.next();
      if ( it.hasNext() )
        return false;
      
      Iterator<PGPPublicKeyRing> pubit = pubringcoll.getKeyRings( useralias );
      PGPPublicKeyRing publickeyring;
      if ( !pubit.hasNext() )
        return false;
      publickeyring = pubit.next();
      if ( pubit.hasNext() )
        return false;
      
      secringcoll = PGPSecretKeyRingCollection.removeSecretKeyRing( secringcoll, secretkeyring );
      pubringcoll = PGPPublicKeyRingCollection.removePublicKeyRing( pubringcoll, publickeyring );
      useralias = null;
      user = null;
      prikey = null;
      pubkey = null;
      password = null;
      saveKeyRingCollections();
      prefs.remove( "qyouti.crypto.useralias" );
      prefs.remove( "qyouti.crypto.usewindows" );
      prefs.save();
      return true;
    }
    catch (Exception ex)
    {
      Logger.getLogger(CryptographyManager.class.getName()).log(Level.SEVERE, null, ex);
      return false;
    }
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
    
    saveKeyRingCollections();
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
      PrivateKey k = (PrivateKey)keyStore.getKey(WINDOWS_CERTIFICATE_ALIAS, null );    

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
      Certificate c = keyStore.getCertificate( WINDOWS_CERTIFICATE_ALIAS );
      if ( c == null )
      {
        if ( !makeWindowsKeyPair( WINDOWS_CERTIFICATE_ALIAS ) )
          throw new CryptographyManagerException( "Unable to create Windows cryptography certificate." );
        keyStore.load(null, null);  // Load keystore 
        c = keyStore.getCertificate( WINDOWS_CERTIFICATE_ALIAS );
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
  
  public static String prettyPrintFingerprint( byte[] raw )
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
        sb.append( " : " );
      String part = Integer.toHexString( ((int)ib.get()) & 0xffff ).toUpperCase();
      for ( int j=part.length(); j<4; j++ )
        part = "0" + part;
      sb.append( part );
    }
    
    return sb.toString();
  }
    
}
