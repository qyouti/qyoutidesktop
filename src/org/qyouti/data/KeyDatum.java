/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti.data;

import org.bouncycastle.openpgp.PGPPublicKey;

/**
 *
 * @author maber01
 */
public class KeyDatum
{
  long keyid;
  PGPPublicKey publickey;
  String displayname;
}
