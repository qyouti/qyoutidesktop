/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti.crypto;

/**
 *
 * @author maber01
 */
public class SignatureVerificationResult
{
  boolean verified=false;
  boolean trustedkey=false;
  String keyalias;
  long keyid;

  public SignatureVerificationResult(String keyalias, long keyid)
  {
    this.keyalias = keyalias;
    this.keyid = keyid;
  }
  
  
  
}
