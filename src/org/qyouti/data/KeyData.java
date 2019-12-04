/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti.data;

import java.util.ArrayList;
import javax.swing.AbstractListModel;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSecretKey;
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
  
  public boolean contains( long keyid )
  {
    for ( KeyDatum datum : keylist )
      if ( datum.keyid == keyid )
        return true;
    return false;
  }
  
  public void clear()
  {
    int sz = keylist.size();
    keylist.clear();
    fireIntervalRemoved(this, 0, sz);
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
  
  public PGPPublicKey getKeyAt( int index )
  {
    return keylist.get(index).publickey;
  }

  public boolean isSecretKey( int index )
  {
    return keylist.get(index).secretkey != null;
  }
  
  public PGPSecretKey getSecretKeyAt( int index )
  {
    return keylist.get(index).secretkey;    
  }
  
  public int getIndexOfKey( long keyid )
  {
    for ( int i=0; i<keylist.size(); i++ )
    {
      if ( keylist.get(i).keyid == keyid )
        return i;
    }
    return -1;
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
  
  public void addKey( PGPSecretKey key )
  {
    KeyDatum datum = new KeyDatum();
    datum.secretkey = key;
    datum.publickey = key.getPublicKey();
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

  public void removeKeyAt( int i )
  {
    if ( i<0 || i>=keylist.size() ) return;
    keylist.remove( i );
    this.fireIntervalRemoved( this, i, i );
  }
  
  public void removeKey( long keyid )
  {
    int i = getIndexOfKey( keyid );
    removeKeyAt( i );
  }
}
