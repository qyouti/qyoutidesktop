/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti.data;

import java.util.ArrayList;
import javax.swing.AbstractListModel;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.qyouti.crypto.CryptographyManager;

/**
 *
 * @author maber01
 */
public class KeyData extends AbstractListModel<String>
{
  ArrayList<KeyDatum> keylist = new ArrayList<>();
  CryptographyManager cryptoman;

  public KeyData(CryptographyManager cryptoman)
  {
    this.cryptoman = cryptoman;
  }
  
  @Override
  public int getSize()
  {
    return keylist.size();
  }

  @Override
  public String getElementAt(int index)
  {
    return keylist.get(index).displayname;
  }
  
  public long getKeyIdAt(int index)
  {
    return keylist.get(index).keyid;
  }
  
  public void addKey( PGPPublicKey key )
  {
    KeyDatum datum = new KeyDatum();
    datum.publickey = key;
    datum.keyid = key.getKeyID();
    datum.displayname = key.getUserIDs().next() + " (" + Long.toUnsignedString(datum.keyid,16) + ")";
    keylist.add(datum);
    this.fireIntervalAdded(this, keylist.size(), keylist.size());
  }
  
  public void addKey( long keyid )
  {
    PGPPublicKey key = cryptoman.findPublicKey( keyid );
    if ( key == null ) return;
    addKey( key );
  }
}
