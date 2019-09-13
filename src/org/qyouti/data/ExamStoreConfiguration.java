/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti.data;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.CopyUtils;
import org.apache.pdfbox.io.IOUtils;
import org.qyouti.compositefile.EncryptedCompositeFile;
import org.qyouti.crypto.CryptographyManager;
import org.qyouti.crypto.SignatureVerificationResultSet;

/**
 *
 * @author maber01
 */
public class ExamStoreConfiguration
{
  CryptographyManager cryptoman;
  File configfile;
  
  EncryptedCompositeFile configarchive;

  String publicintro;
  SignatureVerificationResultSet publicintroverification;
  
  ArrayList<ExamStorePerson> persons;
  
  public ExamStoreConfiguration( CryptographyManager cryptoman, File configfile )
  {
    this.cryptoman     = cryptoman;
    this.configfile    = configfile;
    
  }

  public final void newConfig()
  {
    try
    {
      configarchive = EncryptedCompositeFile.getCompositeFile( configfile );
      configarchive.addPublicKey( cryptoman.getUser(), cryptoman.getUser().getPgppublickey(), cryptoman.getUser().getKeyalias() );
      setPublicIntro( "To do - edit this intro." );
      configarchive.close();
      configarchive = null;
      
      loadConfig();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }    
  }

  public final void loadConfig()
  {
    try
    {
      configarchive = EncryptedCompositeFile.getCompositeFile( configfile );
      configarchive.addPublicKey( cryptoman.getUser(), cryptoman.getUser().getPgppublickey(), cryptoman.getUser().getKeyalias() );
      loadPublicIntro();
      configarchive.close();
      configarchive = null;
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }    
  }

  private final void setPublicIntro( String intro ) throws IOException, Exception
  {
    publicintro = intro;
    byte[] signature = cryptoman.sign( intro.getBytes( "UTF8" ) );

    // NOT encrypted
    OutputStream dataout = configarchive.getOutputStream( "intro.txt", true );
    dataout.write( intro.getBytes( "UTF8" ) );
    dataout.close();
    
    OutputStream sigaout = configarchive.getOutputStream( "intro.txt.sig", true );
    sigaout.write( signature );
    sigaout.close();
    
  }

  private final void loadPublicIntro() throws IOException, Exception
  {
    // NOT encrypted
    InputStream datain = configarchive.getInputStream( "intro.txt" );
    ByteArrayOutputStream baout = new ByteArrayOutputStream();
    IOUtils.copy(datain, baout);
    datain.close();
    publicintro = baout.toString("UTF8");
    
    InputStream sigin = configarchive.getInputStream( "intro.txt.sig" );
    ByteArrayOutputStream sigbaout = new ByteArrayOutputStream();
    IOUtils.copy(sigin, sigbaout);
    sigin.close();

    publicintroverification = cryptoman.verify( baout.toByteArray(), sigbaout.toByteArray() );
  }

  
  public String getPublicIntro()
  {
    return publicintro;
  }
  
  public SignatureVerificationResultSet getPublicIntroVerification()
  {
    return publicintroverification;
  }
}
