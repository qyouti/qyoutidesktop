/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.qyouti.print;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.fop.svg.PDFDocumentGraphics2D;

/**
 *
 * @author jon
 */
public class QyoutiPDFDocumentGraphics2D
    extends PDFDocumentGraphics2D
{
    /**
     * Create a new PDFDocumentGraphics2D.
     * This is used to create a new pdf document, the height,
     * width and output stream can be setup later.
     * For use by the transcoder which needs font information
     * for the bridge before the document size is known.
     * The resulting document is written to the stream after rendering.
     *
     * @param textAsShapes set this to true so that text will be rendered
     * using curves and not the font.
     */
    public QyoutiPDFDocumentGraphics2D(boolean textAsShapes) {
        super(textAsShapes);
    }

    /**
     * Create a new PDFDocumentGraphics2D.
     * This is used to create a new pdf document of the given height
     * and width.
     * The resulting document is written to the stream after rendering.
     *
     * @param textAsShapes set this to true so that text will be rendered
     * using curves and not the font.
     * @param stream the stream that the final document should be written to.
     * @param width the width of the document
     * @param height the height of the document
     * @throws IOException an io exception if there is a problem
     *         writing to the output stream
     */
    public QyoutiPDFDocumentGraphics2D(boolean textAsShapes, OutputStream stream,
                                 int width, int height) throws IOException {
        super(textAsShapes,stream,width,height);
    }

    /**
     * Create a new PDFDocumentGraphics2D.
     * This is used to create a new pdf document.
     * For use by the transcoder which needs font information
     * for the bridge before the document size is known.
     * The resulting document is written to the stream after rendering.
     * This constructor is Avalon-style.
     */
    public QyoutiPDFDocumentGraphics2D() {
        this(false);
    }

  @Override
  protected void preparePainting()
  {
    super.preparePainting();
  }

  @Override
  protected void startPage() throws IOException
  {
    super.startPage();
  }



}
