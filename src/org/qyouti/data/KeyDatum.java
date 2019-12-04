/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti.data;

import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSecretKey;

/**
 *
 * @author maber01
 */
public class KeyDatum
{
  long keyid;
  PGPSecretKey secretkey;
  PGPPublicKey publickey;
  String displayname;
}
