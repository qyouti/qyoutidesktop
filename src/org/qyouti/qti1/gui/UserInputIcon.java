/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.qyouti.qti1.gui;

import java.awt.Rectangle;

/**
 *
 * @author jon
 */
public abstract class UserInputIcon
        extends SVGIcon
{
  String type;
  String ident;
  
  abstract public Rectangle getPinkRectangle();

  public String getIdent()
  {
    return ident;
  }

  public void setIdent( String ident )
  {
    this.ident = ident;
  }

  public String getType()
  {
    return type;
  }

  public void setType( String type )
  {
    this.type = type;
  }
  
}
