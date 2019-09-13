/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti.crypto;

import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author maber01
 */
public class SignatureVerificationResultSet
        extends AbstractTableModel
{
  boolean verified;
  ArrayList<SignatureVerificationResult> results = new ArrayList<>();

  @Override
  public int getRowCount()
  {
    return results.size();
  }

  @Override
  public int getColumnCount()
  {
    return 3;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex)
  {
    if ( rowIndex < 0 || rowIndex >=getRowCount() || columnIndex<0 || columnIndex>=getColumnCount() )
      return null;
    
    SignatureVerificationResult result = results.get(rowIndex);
    switch ( columnIndex )
    {
      case 0:
        return result.keyalias;
      case 1:
        return result.trustedkey;
      case 2:
        return result.verified;
    }
    return null;
  }

  @Override
  public Class<?> getColumnClass(int columnIndex)
  {
    switch ( columnIndex )
    {
      case 0:
        return String.class;
      case 1:
        return Boolean.class;
      case 2:
        return Boolean.class;
    }
    return null;
  }

  @Override
  public String getColumnName(int column)
  {
    switch ( column )
    {
      case 0:
        return "Signer Name";
      case 1:
        return "Trusted Key";
      case 2:
        return "Verified";
    }
    return null;
  }
  
  
}
