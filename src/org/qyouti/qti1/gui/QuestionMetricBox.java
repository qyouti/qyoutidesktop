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
public class QuestionMetricBox
        extends Rectangle
{
  String type;
  String ident;
  int index;

  public QuestionMetricBox( int x, int y, int width, int height, String type, String ident, int index )
  {
    super( x, y, width, height );
    this.type = type;
    this.ident = ident;
    this.index = index;
  }

  public int getIndex()
  {
    return index;
  }


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
