/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.qyouti.qti1.ext.qyouti;

import org.qyouti.qti1.QTIRenderUnsupported;

/**
 *
 * @author jon
 */
public class QTIExtensionRendersketcharea
        extends QTIRenderUnsupported
{
  public int getRows()
  {
    int r = this.getIntAttribute("rows");
    if ( r < 1 ) r = 1;
    return r;
  }

  public int getColumns()
  {
    int c = this.getIntAttribute("columns");
    if ( c < 1 ) c = 10;
    return c;
  }

}
