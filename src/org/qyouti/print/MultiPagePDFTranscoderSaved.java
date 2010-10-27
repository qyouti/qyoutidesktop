/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti.print;

import java.awt.Color;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.svg.SVGLength;

//import org.apache.avalon.framework.configuration.Configuration;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.UnitProcessor;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.ext.awt.RenderingHintsKeyExt;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;

import org.apache.fop.Version;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.svg.*;

/**
 *
 * @author jon
 */
public class MultiPagePDFTranscoderSaved extends AbstractFOPTranscoder
{
  int pagecount = 0;
      /** Graphics2D instance that is used to paint to */
    protected PDFDocumentGraphics2D graphics = null;


  /**
   * {@inheritDoc}
   */
  @Override
    protected UserAgent createUserAgent() {
        return new AbstractFOPTranscoder.FOPTranscoderUserAgent() {
            // The PDF stuff wants everything at 72dpi
            public float getPixelUnitToMillimeter() {
                return super.getPixelUnitToMillimeter();
                //return 25.4f / 72; //72dpi = 0.352778f;
            }
        };
    }

  /**
   * Transcodes the specified Document as an image in the specified output.
   *
   * @param document the document to transcode
   * @param uri the uri of the document or null if any
   * @param output the ouput where to transcode
   * @exception TranscoderException if an error occured while transcoding
   */
  protected void transcode(Document document, String uri,
      TranscoderOutput output)
      throws TranscoderException
  {
    ++pagecount;
    System.out.println( "MultiPagePDFTranscoder processing page " + pagecount );

    if ( graphics == null)
    {
      graphics = new PDFDocumentGraphics2D(isTextStroked());
      graphics.getPDFDocument().getInfo().setProducer("Apache FOP Version "
          + Version.getVersion()
          + ": PDF Transcoder for Batik");
    }


    try
    {
      graphics.setupDefaultFontInfo();
    } catch (Exception e)
    {
      throw new TranscoderException(
          "Error while setting up PDFDocumentGraphics2D", e);
    }


    // need super super not super
    super.transcode(document, uri, output);

    if (getLogger().isTraceEnabled())
    {
      getLogger().trace("document size: " + width + " x " + height);
    }

    // prepare the image to be painted
    UnitProcessor.Context uctx = UnitProcessor.createContext(ctx,
        document.getDocumentElement());
    float widthInPt = UnitProcessor.userSpaceToSVG(width, SVGLength.SVG_LENGTHTYPE_PT,
        UnitProcessor.HORIZONTAL_LENGTH, uctx);
    int w = (int) (widthInPt + 0.5);
    float heightInPt = UnitProcessor.userSpaceToSVG(height, SVGLength.SVG_LENGTHTYPE_PT,
        UnitProcessor.HORIZONTAL_LENGTH, uctx);
    int h = (int) (heightInPt + 0.5);
    if (getLogger().isTraceEnabled())
    {
      getLogger().trace("document size: " + w + "pt x " + h + "pt");
    }

    try
    {
      if ( pagecount == 1 )
      {
        //OutputStream out = output.getOutputStream();
        //if (!(out instanceof BufferedOutputStream))
        //{
        //  out = new BufferedOutputStream(out);
        //}
        graphics.setupDocument( new BufferedOutputStream(new FileOutputStream("/home/jon/Desktop/test2.pdf")), w, h);
        graphics.setSVGDimension(width, height);

        if (hints.containsKey(ImageTranscoder.KEY_BACKGROUND_COLOR))
        {
          graphics.setBackgroundColor((Color) hints.get(ImageTranscoder.KEY_BACKGROUND_COLOR));
        }
        graphics.setGraphicContext(new org.apache.xmlgraphics.java2d.GraphicContext());

        graphics.transform(curTxf);
        graphics.setRenderingHint(RenderingHintsKeyExt.KEY_TRANSCODING,
            RenderingHintsKeyExt.VALUE_TRANSCODING_VECTOR);
      }
      
      this.root.paint(graphics);
      graphics.nextPage();
    } catch (IOException ex)
    {
      throw new TranscoderException(ex);
    }
  }





    
  public void complete() throws TranscoderException
  {
    try
    {
       graphics.finish();
    } catch (IOException ex)
    {
      throw new TranscoderException(
          "Error while setting up PDFDocumentGraphics2D", ex);
    }
  }
}
