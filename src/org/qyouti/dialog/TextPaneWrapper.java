/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.qyouti.dialog;

import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.JTextPane;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLDocument;

/**
 *
 * @author jon
 */
public class TextPaneWrapper
        extends JTextPane
{

    public TextPaneWrapper( /*String html*/ )
    {
      super();
      setOpaque(false);
      setContentType("text/html");
      //setText(html);
    }

    public HTMLDocument getHtmlDoc()
    {
      Document doc = getDocument();
      if (!(doc instanceof HTMLDocument))
        return null;
      return (HTMLDocument) doc;
    }

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
