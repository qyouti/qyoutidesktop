/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti.qti1;

/**
 * Interface for supplying a qti system with tables of String data. Use case
 * is to look up a person's name for inclusion in outcomes from the person's
 * ID which is already in the outcomes.
 * @author jon
 */
public interface QTIExternalMap
{
  public String getExternalMapName();
  public String getExternalMapping( String key );
}
