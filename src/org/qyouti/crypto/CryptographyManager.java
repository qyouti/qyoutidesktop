/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti.crypto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Iterator;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import org.bouncycastle.bcpg.ArmoredInputStream;
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.bcpg.SymmetricKeyAlgorithmTags;
import org.bouncycastle.bcpg.sig.Features;
import org.bouncycastle.bcpg.sig.KeyFlags;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPEncryptedData;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPKeyPair;
import org.bouncycastle.openpgp.PGPKeyRingGenerator;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureGenerator;
import org.bouncycastle.openpgp.PGPSignatureList;
import org.bouncycastle.openpgp.PGPSignatureSubpacketGenerator;
import org.bouncycastle.openpgp.PGPSignatureSubpacketVector;
import org.bouncycastle.openpgp.operator.KeyFingerPrintCalculator;
import org.bouncycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.bouncycastle.openpgp.operator.PBESecretKeyEncryptor;
import org.bouncycastle.openpgp.operator.PGPDigestCalculator;
import org.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.bc.BcPBESecretKeyDecryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPBESecretKeyEncryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPContentVerifierBuilderProvider;
import org.bouncycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider;
import org.bouncycastle.openpgp.operator.bc.BcPGPKeyPair;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPDigestCalculatorProviderBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPKeyPair;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyEncryptorBuilder;
import org.bouncycastle.util.Arrays;
import org.qyouti.QyoutiPreferences;
import org.qyouti.compositefile.EncryptedCompositeFile;
import org.qyouti.compositefile.EncryptedCompositeFileUser;
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

  PasswordProvider pwprov;
  QyoutiPreferences prefs;
//  String useralias;
  char[] password;
  boolean windowsavailable=false;
  boolean usewindows;
  
  File pgpseckeyfile, pgppubkeyfile;
  String preferredkeyfingerprint;
  PGPSecretKey  preferredseckey;
  PGPPrivateKey  preferredprikey;
  
  EncryptedCompositeFileUser user;
          
  PGPSecretKeyRingCollection secringcoll;
  PGPPublicKeyRingCollection pubringcoll;
  KeyFingerPrintCalculator fpcalc = new BcKeyFingerprintCalculator();
  BcPBESecretKeyDecryptorBuilder seckeydecbuilder = new BcPBESecretKeyDecryptorBuilder(  new BcPGPDigestCalculatorProvider() );

  ArrayList<PGPSecretKey> secretkeys = new ArrayList<>();
  //int opensecretkey = -1;
  
  public CryptographyManager( File base, QyoutiPreferences prefs, PasswordProvider pwprov )
  {
    this.password = null;
    this.prefs = prefs;
    this.pwprov = pwprov;
    preferredkeyfingerprint = prefs.getProperty("qyouti.crypto.preferredkey");
    pgpseckeyfile = new File( base, "seckeyring.gpg" );
    pgppubkeyfile = new File( base, "pubkeyring.gpg" );
    Security.addProvider(new BouncyCastleProvider());
  }
  
  /*
  public void openSecretKey( byte[] fingerprint )
  {
    opensecretkey = -1;
    user = null;

    int i;
    PGPSecretKey seckey;
    PGPPublicKey pubkey;
    String strfingerprinta = CryptographyManager.printFingerprint(fingerprint, "");
    String strfingerprintb;
    for ( i=0; i<secretkeys.size(); i++ )
    {
      seckey = secretkeys.get(i);
      pubkey = seckey.getPublicKey();
      strfingerprintb = CryptographyManager.printFingerprint(pubkey.getFingerprint(), "");
      if ( strfingerprinta.equals( strfingerprintb ) )
      {        
        opensecretkey = i;
        break;
      }
    }
    
    if ( i < secretkeys.size() )
    {
      opensecretkey = i;
    }
  }
  
  public PGPSecretKey getOpenSecretKey()
  {
    if ( opensecretkey < 0 || opensecretkey >= secretkeys.size() )
      return null;
    return secretkeys.get(opensecretkey);
  }
  */
  
  public static Date getSecretKeyCreationDate( PGPSecretKey seckey )
  {
    PGPPublicKey pubkey = seckey.getPublicKey();
    // Get self signatures - should only be one
    Iterator<PGPSignature> it = pubkey.getSignaturesForKeyID( seckey.getKeyID() );
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

  private static String getEncryptedPasswordPreferenceKey( PGPPublicKey pubkey )
  {
    String fp = CryptographyManager.printFingerprint( pubkey.getFingerprint(), "_" );
    return "qyouti.crypto.key." + fp + ".encryptedpassword";
  }
  
  public String getEncryptedWindowsPassword( PGPSecretKey seckey )
  {
    PGPPublicKey pubkey = seckey.getPublicKey();
    String str = prefs.getProperty( getEncryptedPasswordPreferenceKey(pubkey) );
    if ( str == null || str.length() == 0 )
      return null;
    return str;
  }

  public void setEncryptedWindowsPassword( PGPSecretKey seckey, String encryptedpassword )
  {
    PGPPublicKey pubkey = seckey.getPublicKey();
    prefs.setProperty( getEncryptedPasswordPreferenceKey(pubkey), encryptedpassword );
    prefs.save();
  }
  
  public void deleteEncryptedWindowsPassword( PGPSecretKey seckey )
  {
    prefs.remove( getEncryptedPasswordPreferenceKey(seckey.getPublicKey()) );
    prefs.save();
  }
  
  public char[] getWindowsPassword( PGPSecretKey seckey )
  {
    String encpass = getEncryptedWindowsPassword( seckey );
    if ( encpass == null )
      return null;
    
    try
    {
      return decryptWindowsPassword( encpass );
    }
    catch (CryptographyManagerException ex)
    {
      Logger.getLogger(CryptographyManager.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }
  
  
  
  public EncryptedCompositeFileUser getUser()
  {
    if ( user != null ) return user;
    
    if ( preferredseckey == null )
      return null;

    if ( preferredprikey == null )
    {
      char[] pw = getWindowsPassword( preferredseckey );
      if ( pw == null )
        pw = pwprov.getUserSuppliedPassword();
      if ( pw == null ) return null;
      try
      {
        preferredprikey = getPrivateKey(preferredseckey, pw);
      }
      catch (PGPException ex)
      {
        Logger.getLogger(CryptographyManager.class.getName()).log(Level.SEVERE, null, ex);
        return null;
      }
    }
    
    PGPPublicKey pubkey = preferredseckey.getPublicKey();
    user = new EncryptedCompositeFileUser( pubkey.getUserIDs().next(), preferredprikey, pubkey );
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
      windowsavailable = false;
    }
    
    loadSecretKeys();
  }
  
  /*
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
  */
  
  private PGPPrivateKey getPrivateKey( PGPSecretKey seckey, char[] passphrase ) throws PGPException
  {
    if ( secringcoll == null ) return null;
    PBESecretKeyDecryptor dec = seckeydecbuilder.build(passphrase);
    return seckey.extractPrivateKey(dec);
  }
  
  /*
  public void unloadPrivateKey()
  {
    preferredprikey = null;
    user = new EncryptedCompositeFileUser( useralias, null, preferredseckey.getPublicKey() );
    password = null;
  }
  
  public boolean loadPrivateKey()
  {
    preferredprikey = null;
    if ( user == null || useralias == null )
      return false;
    
    try
    {
      if ( password == null )
        return false;
      
      preferredprikey = getPrivateKey(useralias, password );
      user = new EncryptedCompositeFileUser( useralias, preferredprikey, preferredseckey.getPublicKey() );
    }
    catch (Exception ex)
    {
      Logger.getLogger(CryptographyManager.class.getName()).log(Level.SEVERE, null, ex);
      preferredprikey = null;
      return false;
    }
    return true;
  }
  */
  
  
  private void loadSecretKeys()
  {
    user = null;
    preferredseckey = null;
    preferredprikey = null;
    secretkeys.clear();
    PGPPublicKey pubkey;
    String fp;
    
    Iterator<PGPSecretKeyRing> ringit = this.secringcoll.getKeyRings();
    PGPSecretKeyRing ring;
    PGPSecretKey key;
    while ( ringit.hasNext() )
    {
      ring = ringit.next();
      // only load the master key
      key = ring.getSecretKey();
      this.secretkeys.add( key );
      pubkey = key.getPublicKey();
      fp = CryptographyManager.printFingerprint( pubkey.getFingerprint(), "_" );
      if ( fp.equals( preferredkeyfingerprint) )
      {
        preferredseckey = key;
        loadUser();
      }
    }
  }

  public PGPSecretKey[] getSecretKeys()
  {
    return secretkeys.toArray( new PGPSecretKey[secretkeys.size()] );
  }
  
  public PGPSecretKey getPreferredSecretKey()
  {
    return preferredseckey;
  }
  
  public void setPreferredSecretKey( PGPSecretKey seckey )
  {
    user = null;
    preferredseckey = seckey;
    preferredprikey = null;
    
    preferredkeyfingerprint = CryptographyManager.printFingerprint( preferredseckey.getPublicKey().getFingerprint(), "_" );
    prefs.setProperty("qyouti.crypto.preferredkey", preferredkeyfingerprint);
    prefs.save();
  }
  
  public boolean loadUser()
  {
    user = null;
    try
    {
      user = new EncryptedCompositeFileUser( preferredseckey.getPublicKey().getUserIDs().next(), null, preferredseckey.getPublicKey() );
    }
    catch (Exception ex)
    {
      preferredseckey = null;
      preferredprikey = null;
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
    
    String encryptedpassword = null;
    if ( win )
    {
      encryptedpassword = encryptWindowsPassword( password );
    }
    
    PGPSecretKey seckey = createNewPGPKeys( alias, password );
    if ( seckey != null )
    {
      if ( encryptedpassword != null )
        setEncryptedWindowsPassword(seckey, encryptedpassword);
      loadSecretKeys();
    }
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
  
  public boolean deleteKeyPair( PGPSecretKey seckey )
  {
    try
    {
      PGPSecretKeyRing secretkeyring = secringcoll.getSecretKeyRing(seckey.getKeyID());
      PGPPublicKeyRing publickeyring = pubringcoll.getPublicKeyRing(seckey.getKeyID());
      secringcoll = PGPSecretKeyRingCollection.removeSecretKeyRing( secringcoll, secretkeyring );
      pubringcoll = PGPPublicKeyRingCollection.removePublicKeyRing( pubringcoll, publickeyring );
      saveKeyRingCollections();
      
      deleteEncryptedWindowsPassword(seckey);
      loadSecretKeys();
      return true;
    }
    catch (Exception ex)
    {
      Logger.getLogger(CryptographyManager.class.getName()).log(Level.SEVERE, null, ex);
      return false;
    }
  }
  
  /*
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
*/
  
  // See https://bouncycastle-pgp-cookbook.blogspot.com/
  public PGPSecretKey createNewPGPKeys(String id, char[] pass)
  {
    return createNewPGPKeys(id, pass, 0xc0);
  }

  // Note: s2kcount is a number between 0 and 0xff that controls the
  // number of times to iterate the password hash before use. More
  // iterations are useful against offline attacks, as it takes more
  // time to check each password. The actual number of iterations is
  // rather complex, and also depends on the hash function in use.
  // Refer to Section 3.7.1.3 in rfc4880.txt. Bigger numbers give
  // you more iterations.  As a rough rule of thumb, when using
  // SHA256 as the hashing function, 0x10 gives you about 64
  // iterations, 0x20 about 128, 0x30 about 256 and so on till 0xf0,
  // or about 1 million iterations. The maximum you can go to is
  // 0xff, or about 2 million iterations.  I'll use 0xc0 as a
  // default -- about 130,000 iterations.
  public PGPSecretKey createNewPGPKeys(String id, char[] pass, int s2kcount)
  {
    PGPPublicKeyRing pkr;
    PGPSecretKeyRing skr;
    
    try
    {
      // This object generates individual key-pairs.
      RSAKeyPairGenerator kpg = new RSAKeyPairGenerator();
      
      // Boilerplate RSA parameters, no need to change anything
      // except for the RSA key-size (2048). You can use whatever
      // key-size makes sense for you -- 4096, etc.
      kpg.init(new RSAKeyGenerationParameters(BigInteger.valueOf(0x10001),
              new SecureRandom(), 2048, 12));
      
      // First create the master (signing) key with the generator.
      PGPKeyPair rsakp_sign
              = new BcPGPKeyPair(PGPPublicKey.RSA_GENERAL, kpg.generateKeyPair(), new Date());
      
      // Add a self-signature on the id
      PGPSignatureSubpacketGenerator signhashgen
              = new PGPSignatureSubpacketGenerator();
      
      // Add signed metadata on the signature.
      // 1) Declare its purpose
      signhashgen.setKeyFlags(false, KeyFlags.SIGN_DATA | KeyFlags.CERTIFY_OTHER | KeyFlags.ENCRYPT_COMMS | KeyFlags.ENCRYPT_STORAGE );
      // 2) Set preferences for secondary crypto algorithms to use
      //    when sending messages to this key.
      signhashgen.setPreferredSymmetricAlgorithms(false, new int[]
      {
        SymmetricKeyAlgorithmTags.AES_256,
        SymmetricKeyAlgorithmTags.AES_192,
        SymmetricKeyAlgorithmTags.AES_128
      });
      signhashgen.setPreferredHashAlgorithms(false, new int[]
      {
        HashAlgorithmTags.SHA256,
        HashAlgorithmTags.SHA1,
        HashAlgorithmTags.SHA384,
        HashAlgorithmTags.SHA512,
        HashAlgorithmTags.SHA224,
      });
      // 3) Request senders add additional checksums to the
      //    message (useful when verifying unsigned messages.)
      signhashgen.setFeature(false, Features.FEATURE_MODIFICATION_DETECTION);
      
      // Objects used to encrypt the secret key.
      PGPDigestCalculator sha1Calc
              = new BcPGPDigestCalculatorProvider()
                      .get(HashAlgorithmTags.SHA1);
      PGPDigestCalculator sha256Calc
              = new BcPGPDigestCalculatorProvider()
                      .get(HashAlgorithmTags.SHA256);
      
      // bcpg 1.48 exposes this API that includes s2kcount. Earlier
      // versions use a default of 0x60.
      PBESecretKeyEncryptor pske
              = (new BcPBESecretKeyEncryptorBuilder(PGPEncryptedData.AES_256, sha256Calc, s2kcount))
                      .build(pass);
      
      // Finally, create the keyring itself. The constructor
      // takes parameters that allow it to generate the self
      // signature.
      PGPKeyRingGenerator keyRingGen = new PGPKeyRingGenerator(PGPSignature.POSITIVE_CERTIFICATION, rsakp_sign,
                      id, sha1Calc, signhashgen.generate(), null,
                      new BcPGPContentSignerBuilder(rsakp_sign.getPublicKey().getAlgorithm(),
                              HashAlgorithmTags.SHA1),
                      pske);
      
      pkr = keyRingGen.generatePublicKeyRing();
      skr = keyRingGen.generateSecretKeyRing();
    }
    catch (PGPException ex)
    {
      Logger.getLogger(CryptographyManager.class.getName()).log(Level.SEVERE, null, ex);
      return null;
    }

    // Add to the key ring collections...
    secringcoll = PGPSecretKeyRingCollection.addSecretKeyRing( secringcoll, skr );
    pubringcoll = PGPPublicKeyRingCollection.addPublicKeyRing( pubringcoll, pkr );
    try
    {
      saveKeyRingCollections();
    }
    catch (IOException ex)
    {
      Logger.getLogger(CryptographyManager.class.getName()).log(Level.SEVERE, null, ex);
    }

    return skr.getSecretKey();
  }



  /**
   * Create an SHA256withRSA signature
   * @param plainText The message to sign
   * @param k The private key to use.
   * @return A base 64 encoded signature
   * @throws Exception If problem occurs with JCA.
   */
  public byte[] sign(byte[] plainText)
          throws Exception
  {
    ByteArrayOutputStream baout = new ByteArrayOutputStream();
    ArmoredOutputStream aout = new ArmoredOutputStream( baout );
    PGPSignatureGenerator signatureGenerator = new PGPSignatureGenerator( 
                new BcPGPContentSignerBuilder( user.getPgppublickey().getAlgorithm(), HashAlgorithmTags.SHA256) );
    signatureGenerator.init( PGPSignature.BINARY_DOCUMENT, user.getPgpprivatekey() );
    PGPSignatureSubpacketGenerator spGen = new PGPSignatureSubpacketGenerator();
    spGen.setSignerUserID( false, user.getKeyalias() );
    signatureGenerator.setHashedSubpackets( spGen.generate() );
    signatureGenerator.update( plainText );
    PGPSignature signature = signatureGenerator.generate();
    signature.encode(aout);
    aout.close();
    return baout.toByteArray();
  }

  /**
   * Verify an SHA256withRSA signature
   * @param plainText The message that was supposedly signed
   * @param signature The signature, base64 encoded
   * @param k The public key to use.
   * @return true if the signature passed the test
   * @throws Exception If problem occurs with JCA.
   */
  public SignatureVerificationResultSet verify( byte[] plainText, byte[] signature )
          throws Exception
  {
    SignatureVerificationResultSet set = new SignatureVerificationResultSet();
    set.verified = false;
    ByteArrayInputStream bain = new ByteArrayInputStream( signature );
    ArmoredInputStream ain = new ArmoredInputStream( bain );
    PGPObjectFactory pgpF = new PGPObjectFactory( ain, null );
    
    Object o;
    PGPSignatureList siglist;
    PGPSignatureSubpacketVector subs;
    o=pgpF.nextObject();
    if ( o==null || !(o instanceof PGPSignatureList) )
      return set;
    
    siglist = (PGPSignatureList)o;
    for ( PGPSignature sig : siglist )
    {
      
      sig.getHashedSubPackets();
      subs = sig.getHashedSubPackets();
      System.out.println( "Signed by " + subs.getSignerUserID() );
      System.out.println( "Key ID " + Long.toHexString( sig.getKeyID() ) );
      SignatureVerificationResult result = new SignatureVerificationResult( subs.getSignerUserID(), sig.getKeyID() );
      set.results.add( result );

      Iterator<PGPPublicKeyRing> it = pubringcoll.getKeyRings( subs.getSignerUserID() );
      PGPPublicKeyRing keyring;
      PGPPublicKey pubkey;
      while ( it.hasNext() )
      {
        keyring = it.next();
        pubkey = keyring.getPublicKey();
        if ( pubkey != null )
        {
          System.out.println( "My key ring has a public key with the right name and with id = " + Long.toHexString( pubkey.getKeyID() ) );
          if ( pubkey.getKeyID() == sig.getKeyID() )
          {
            result.trustedkey = true;
            sig.init( new BcPGPContentVerifierBuilderProvider(), pubkey );
            sig.update(plainText);
            if ( sig.verify() )
            {
              System.out.println( "Signature Verified Authentic." );
              result.verified = true;
            }
            else
            {
              System.out.println( "Signature Verification Failed." );
            }
            
            break;
          }
        }
      }
    }

    return set;
  }  
  
  
  
  
  
  private char[] decryptWindowsPassword( String strraw ) throws CryptographyManagerException
  {
    try
    {
      KeyStore keyStore = KeyStore.getInstance("Windows-MY");
      keyStore.load(null, null);  // Load keystore 
      PrivateKey k = (PrivateKey)keyStore.getKey(WINDOWS_CERTIFICATE_ALIAS, null );    

      byte[] raw = Base64.getDecoder().decode(strraw);

      Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
      cipher.init( Cipher.DECRYPT_MODE, k );
      byte[] decrypt = cipher.doFinal( raw );
      return new String( decrypt, "UTF8" ).toCharArray();
    }
    catch ( Exception e )
    {
      e.printStackTrace();
    }
    return null;
  }
  
  private String encryptWindowsPassword( char[] password ) throws CryptographyManagerException
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
      Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
      cipher.init( Cipher.ENCRYPT_MODE, pubkey );
      byte[] crypt = cipher.doFinal( new String(password).getBytes() );
      return Base64.getEncoder().encodeToString(crypt);
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
