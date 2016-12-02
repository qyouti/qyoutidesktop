/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

/**
 *
 * @author jon
 */
public class VerticalTextTableCellRenderer implements TableCellRenderer
{
  
  VerticalTextLabel label;

  public VerticalTextTableCellRenderer()
  {
    label = new VerticalTextLabel();
    label.setBorder( new BevelBorder(BevelBorder.RAISED) );
  }
  
  @Override
  public Component getTableCellRendererComponent( JTable table, Object value,
                                                  boolean isSelected,
                                                  boolean hasFocus, int row,
                                                  int column )
  {
    label.setText( value.toString() );
    return label;
  }
}
