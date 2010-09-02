/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.qyouti.dialog;

import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.JTextPane;

/**
 *
 * @author jon
 */
public class TextPaneWrapper
        extends JTextPane
{

    @Override
    public void reshape(int x, int y, int w, int h)
    {
        super.reshape(x, y, w, h);
    }

    @Override
    public void setBounds(int x, int y, int width, int height)
    {
        super.setBounds(x, y, width, height);
    }

    @Override
    public void setBounds(Rectangle r)
    {
        super.setBounds(r);
    }

    

    @Override
    public void resize(int width, int height)
    {
        super.resize(width, height);
    }

    @Override
    public void resize(Dimension d)
    {
        super.resize(d);
    }

    @Override
    public void setSize(int width, int height)
    {
        super.setSize(width, height);
    }

    @Override
    public void setSize(Dimension d)
    {
        super.setSize(d);
    }

    @Override
    public boolean getScrollableTracksViewportWidth()
    {
        return true;
    }

}
