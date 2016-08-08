/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti.dialog;

import java.awt.*;
import org.qyouti.print.*;
import org.qyouti.qti1.gui.*;

/**
 *
 * @author jon
 */
public class QyoutiCustomAWTEvent extends AWTEvent
{
  QTIItemRenderer renderer;
  int width;
  
  public QyoutiCustomAWTEvent( Object source, int id )
  {
    super( source, id );
  }

  public void setRenderer( QTIItemRenderer renderer )
  {
    this.renderer = renderer;
  }

  public QTIItemRenderer getRenderer()
  {
    return renderer;
  }

  

  public int getWidth()
  {
    return width;
  }

  public void setWidth( int width )
  {
    this.width = width;
  }
  
  
}
