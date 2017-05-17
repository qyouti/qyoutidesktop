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
  String inm, onm, exmapname;
  QTIElementQuestestinterop root;
  HashMap<String,String> entrymap = new HashMap<>();
  
  @Override
  public void initialize()
  {
    super.initialize();

    root = null;
    QTIElement next = this;
    while ( next.parent != null )
      next = next.parent;
    if ( next instanceof QTIElementQuestestinterop )
      root = (QTIElementQuestestinterop)next;

    inm = getAttribute( "invarname" );
    onm = getAttribute( "outvarname" );
    exmapname = getAttribute( "externalmap" );
    if ( exmapname != null )
      findExternalMap();
    
    Vector<QTIExtensionOutcomemapentry> entries;
    QTIExtensionOutcomemapentry entry;
    entries = findElements( QTIExtensionOutcomemapentry.class, false );
    for ( int i=0; i<entries.size(); i++ )
    {
      entry = entries.get( i );
      entrymap.put( entry.getInputString(), entry.getOutputString() );
    }
  }
  
  QTIExternalMap findExternalMap()
  {
    if ( root == null ) return null;
    return root.getExternalMap( exmapname );
  }
  
  public void computeOutcomes()
  {
    QTIExternalMap exmap=findExternalMap();
    String output=null;
    if ( inm == null || onm == null ) return;
    Object input = getItem().getOutcomeValue( inm );
    if ( input == null ) return;
    if ( exmap != null )
      output = exmap.getExternalMapping( input.toString() );
    if ( output == null )
      output = entrymap.get( input.toString() );
    if ( output == null ) return;
    getItem().setOutcome( onm, output);
  }
  
}
