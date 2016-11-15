/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

/**
 *
 * @author jon
 */
public class PinkBoxTableCellRenderer
        extends DefaultTableCellRenderer
{
  public static final BasicStroke PRINTSTROKE  = new BasicStroke( 10, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND );
  public static final BasicStroke PENCILSTROKE = new BasicStroke(  6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND );

  public static final int WIDTH=32;
  public static final int HEIGHT=32;
  
  
  public PinkBoxTableCellRenderer() { super(); }
  
  public void setValue(Object value)
  {
    if ( value==null || !(value instanceof Boolean ) )
    {
      setText("");
      setIcon( null );
    }
    
    Boolean b = (Boolean)value;
    setIcon( b.booleanValue()?TrueFalseIcon.TRUEICON:TrueFalseIcon.FALSEICON );
    setText("");    
  }   
}
