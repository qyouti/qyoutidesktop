/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti.qti1.ext.qyouti;

import org.qyouti.qti1.*;

/**
 * Multiple instances will appear in an outcome mapping. References one value
 * of one outcome variable and give the value of the other outcome variable.
 * @author jon
 */
public class QTIExtensionOutcomemapentry
                extends QTIItemDescendant
{
  
    @Override
  public void initialize()
  {
    super.initialize();
  }
  
  public String getInputString()
  {
    return this.getAttribute( "in" );
  }

  public String getOutputString()
  {
    return this.getAttribute( "out" );
  }

}
