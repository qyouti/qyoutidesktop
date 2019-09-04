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
public class CryptographyManagerException
        extends Exception
{

  /**
   * Creates a new instance of <code>CryptographyManagerException</code> without detail message.
   */
  public CryptographyManagerException()
  {
  }

  /**
   * Constructs an instance of <code>CryptographyManagerException</code> with the specified detail message.
   *
   * @param msg the detail message.
   */
  public CryptographyManagerException(String msg)
  {
    super(msg);
  }
}
