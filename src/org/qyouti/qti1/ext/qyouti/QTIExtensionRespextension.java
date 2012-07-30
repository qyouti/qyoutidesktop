/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.qyouti.qti1.ext.qyouti;

import java.util.Vector;
import org.qyouti.qti1.QTIResponse;

/**
 *
 * @author jon
 */
public class QTIExtensionRespextension
        extends QTIResponse
{
  QTIExtensionRendersketcharea rendersketcharea;
  boolean supported = false;

  String[] current;


  public QTIExtensionRendersketcharea getRendersketcharea()
  {
    return rendersketcharea;
  }

  @Override
  public boolean isSupported()
  {
    return supported;
  }

  @Override
  public Object getCurrentValue()
  {
    return current;
  }

  @Override
  public void setCurrentValue(Object value)
  {
    if ( !(value instanceof String[]) )
      throw new IllegalArgumentException( "Attempt to set response string to non-String array value." );

    String[] newcurrent = (String[])value;

    // passed all checks so go ahead and set it!
    current = newcurrent;
  }

  @Override
  public boolean areCurrentResponseValuesAllowed()
  {
    // any response is an allowed response right now but in
    // the future this might check that numerical input is numerical etc.
    return true;
  }




    @Override
  public void initialize()
  {
    super.initialize();

    supported = false;

    Vector<QTIExtensionRendersketcharea> areas = findElements( QTIExtensionRendersketcharea.class, true );
    if ( areas.size() != 1 )
      return;

    rendersketcharea = areas.get(0);

    supported = true;
  }


  @Override
  public void reset()
  {
    current = new String[0];
  }

}
