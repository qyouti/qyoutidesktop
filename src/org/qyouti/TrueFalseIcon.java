/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti;

import java.awt.*;
import javax.swing.*;
import static org.qyouti.PinkBoxTableCellRenderer.HEIGHT;
import static org.qyouti.PinkBoxTableCellRenderer.PENCILSTROKE;
import static org.qyouti.PinkBoxTableCellRenderer.PRINTSTROKE;
import static org.qyouti.PinkBoxTableCellRenderer.WIDTH;

/**
 *
 * @author jon
 */
public class TrueFalseIcon
        implements Icon
{
  public static final BasicStroke PRINTSTROKE  = new BasicStroke( 4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND );
  public static final BasicStroke PENCILSTROKE = new BasicStroke(  2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND );

  public static final int WIDTH=32;
  public static final int HEIGHT=32;
  
  public static final Color PINK1 = new Color( 255, 220, 255 );
  public static final Color PINK2 = new Color( 255, 230, 255 );
  public static final Color GREY  = new Color( 80, 80, 80 );
  public static final Color PALEGREY  = new Color( 240, 240, 240 );
  public static final Color MIDGREY  = new Color( 200, 200, 200 );
  
  public static final Icon TRUEICON = new TrueFalseIcon( true, false );
  public static final Icon FALSEICON = new TrueFalseIcon( false, false );
  public static final Icon GRAYEDTRUEICON = new TrueFalseIcon( true, true );
  public static final Icon GRAYEDFALSEICON = new TrueFalseIcon( false, true );
  
  
  boolean b, greyed;

  public TrueFalseIcon( boolean b, boolean greyed )
  {
    this.b = b;
    this.greyed = greyed;
  }

  @Override
  public void paintIcon( Component c, Graphics g, int x, int y )
  {
    Graphics2D g2d = (Graphics2D) g;

    g2d.setColor( Color.white );
    g2d.fillRect( 0, 0, WIDTH, HEIGHT );
    g2d.setColor( greyed?PALEGREY:PINK1 );
    g2d.fillRect( 2, 2, WIDTH - 4, HEIGHT - 4 );
    g2d.setColor( greyed?PALEGREY:PINK2 );
    g2d.setStroke( PRINTSTROKE );
    g2d.drawLine( 10, 10, 22, 22 );
    g2d.drawLine( 10, 22, 22, 10 );
    g2d.setColor( greyed?MIDGREY:GREY );
    if ( b )
    {
      g2d.setStroke( PENCILSTROKE );
      g2d.drawLine( 6, 6, 26, 26 );
      g2d.drawLine( 6, 26, 26, 6 );
    }
  }

  @Override
  public int getIconWidth()
  {
    return 32;
  }

  @Override
  public int getIconHeight()
  {
    return 32;
  }
}

