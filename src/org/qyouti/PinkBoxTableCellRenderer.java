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
  
  
  int currentcolumn;
  boolean greyed=true;
  
  public PinkBoxTableCellRenderer() { super(); }

  public boolean isGreyed()
  {
    return greyed;
  }

  public void setGreyed( boolean greyed )
  {
    this.greyed = greyed;
  }
  
  
  
  @Override
  public void setValue(Object value)
  {
    if ( value==null || !(value instanceof Boolean ) )
    {
      setText("");
      setIcon( null );
    }
    
    Boolean b = (Boolean)value;
    if ( currentcolumn == 6 && greyed )
      setIcon( b.booleanValue()?TrueFalseIcon.GRAYEDTRUEICON:TrueFalseIcon.GRAYEDFALSEICON );
    else
      setIcon( b.booleanValue()?TrueFalseIcon.TRUEICON:TrueFalseIcon.FALSEICON );
    setText("");    
  }   

  @Override
  public Component getTableCellRendererComponent( JTable table, Object value,
                                                  boolean isSelected,
                                                  boolean hasFocus, int row,
                                                  int column )
  {
    currentcolumn = column;
    return super.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, column ); //To change body of generated methods, choose Tools | Templates.
  }
  
  
}
