/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti.qti1;

/**
 *
 * @author jon
 */
public abstract class QTIItemproc
  extends QTIItemDescendant
{
  public abstract boolean isSupported();  
  public abstract void computeOutcomes();
}
