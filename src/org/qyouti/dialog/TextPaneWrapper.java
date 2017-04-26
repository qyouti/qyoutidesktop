/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.qyouti.dialog;

import java.awt.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.logging.*;
import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import static javax.swing.text.html.HTMLEditorKit.DEFAULT_CSS;
import javax.swing.text.html.parser.*;
import org.qyouti.fonts.*;
import org.qyouti.print.*;
import sun.awt.*;

/**
 *
 * @author jon
 */
public class TextPaneWrapper
        extends JTextPane
{
    private QyoutiStyleSheet defstylesheet=null;
  
    SvgConversionResult result=null;

    public TextPaneWrapper( /*String html*/ )
    {
      super();
      setOpaque(false);
      // making not opaque doesn't seem to work so we set the background
      // colour with zero alpha channel to supress background drawing.

      //HTMLDocument htmldoc = this.getHtmlDoc();
      //setDocument( new HTMLDocument( defstylesheet ) );
        //setText(html);
    }


    public void setQyoutiStyleSheet( QyoutiStyleSheet ss )
    {
      this.defstylesheet = ss;
    }
    
    public void setText( String text )
    {
      setBackground( new Color( 255, 255, 255, 0 ) );
      setContentType("text/html");
      
      HTMLDocument htmlDoc = new HTMLDocument( defstylesheet );
      setDocument( htmlDoc );
      super.setText( text );
    }
    
    public void setupStyle()
    {
      getHtmlDoc().getStyleSheet().addStyleSheet( defstylesheet );
    }
    
    /**
     * This method calls ComponentToSvg.convert.  There is some jiggery
     * pokery because that method must be called from within the Event
     * Dispatcher Thread regardless of whether the callee is in that
     * thread.  So, this sets up a runnable, detects the current thread
     * and either directly calls the runnable or invokes it in the event
     * dispatcher thread and blocks until done.
     * 
     * It is likely that this will be called from the event dispatcher
     * when a single question is rendered (preview) but from a specially
     * created Thread when rendering lots of questions and remaining
     * responsive to user input.
     * 
     * @param width
     * @return 
     */
    public SvgConversionResult getSVG( final int width )
    {
      final TextPaneWrapper wrapper = this;
      result = null;
      
      Runnable r = new Runnable()
      {
        @Override
        public void run()
        {
          result = ComponentToSvg.convert( wrapper, width );
        }
        
      };
      
      if ( SwingUtilities.isEventDispatchThread() )
      {
        //System.out.println( "Rendering component to SVG - ALREADY IN EVENT DISPATCH THREAD." );
        r.run();
      }
      else
      {
        //System.out.println( "Rendering component to SVG - INVOKING EVENT DISPATCH THREAD." );
        try
        {
          SwingUtilities.invokeAndWait( r );
        }
        catch ( InterruptedException ex )
        {
          Logger.getLogger( TextPaneWrapper.class.getName() ).
                  log( Level.SEVERE, null, ex );
          return null;
        }
        catch ( InvocationTargetException ex )
        {
          Logger.getLogger( TextPaneWrapper.class.getName() ).
                  log( Level.SEVERE, null, ex );
          return null;
        }
      }
      
      return result;
    }
    
    public HTMLDocument getHtmlDoc()
    {
      Document doc = getDocument();
      if (!(doc instanceof HTMLDocument))
        return null;
      return (HTMLDocument) doc;
    }

    @Override
    @Deprecated
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
    @Deprecated
    public void resize(int width, int height)
    {
        super.resize(width, height);
    }

    @Override
    @Deprecated
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
