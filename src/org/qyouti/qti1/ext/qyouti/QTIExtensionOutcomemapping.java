/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti.qti1.ext.qyouti;

import java.util.*;
import org.qyouti.qti1.*;
import org.qyouti.qti1.element.*;

/**
 * Custom qyouti processing. This container represents a mapping from one
 * outcome variable to another.  Will contain multiple entries.
 * @author jon
 */
public class QTIExtensionOutcomemapping
                extends QTIItemDescendant
{
  String inm, onm;
  HashMap<String,String> entrymap = new HashMap<>();
  
  @Override
  public void initialize()
  {
    super.initialize();
    
    inm = getAttribute( "invarname" );
    onm = getAttribute( "outvarname" );
    
    Vector<QTIExtensionOutcomemapentry> entries;
    QTIExtensionOutcomemapentry entry;
    entries = findElements( QTIExtensionOutcomemapentry.class, false );
    for ( int i=0; i<entries.size(); i++ )
    {
      entry = entries.get( i );
      entrymap.put( entry.getInputString(), entry.getOutputString() );
    }
  }
  
  public void computeOutcomes()
  {
    if ( inm == null || onm == null ) return;
    Object input = getItem().getOutcomeValue( inm );
    if ( input == null ) return;
    String output = entrymap.get( input.toString() );
    if ( output == null ) return;
    getItem().setOutcome( onm, output);
  }
  
}
