/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti.qti1.ext.qyouti;

import java.util.*;
import org.qyouti.qti1.*;

/**
 * For qyouti namespace this element contains all the qyouti specific response
 * processing that cannot be expressed in normal QTI.  This was added to 
 * help implement looking up student name outcome from student ID outcome.
 * @author jon
 */
public class QTIExtensionItemproc
    extends QTIItemproc
{
  Vector<QTIExtensionOutcomemapping> mappings;
    
  @Override
  public void initialize()
  {
    super.initialize();
    QTIExtensionOutcomemapentry entry;
    mappings = findElements( QTIExtensionOutcomemapping.class, false );    
  }
  
  public void computeOutcomes()
  {
    for ( int i=0; i<mappings.size(); i++ )
      mappings.get( i ).computeOutcomes();
  }

  @Override
  public boolean isSupported()
  {
    return true;
  }
}
