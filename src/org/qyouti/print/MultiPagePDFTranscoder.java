/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti.print;

import java.awt.Color;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import org.apache.avalon.framework.configuration.*;

import org.w3c.dom.Document;
import org.w3c.dom.svg.SVGLength;

//import org.apache.avalon.framework.configuration.Configuration;
import org.apache.batik.bridge.*;
import org.apache.batik.ext.awt.RenderingHintsKeyExt;
import org.apache.batik.transcoder.*;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.keys.*;
import org.apache.batik.util.ParsedURL;

import org.apache.fop.Version;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.pdf.StreamCacheFactory;
import org.apache.fop.svg.*;
import org.apache.xmlgraphics.image.loader.ImageContext;
import org.apache.xmlgraphics.image.loader.ImageManager;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;
import org.apache.xmlgraphics.image.loader.impl.AbstractImageSessionContext;

/**
 *
 * @author jon
 */
public class MultiPagePDFTranscoder
    extends AbstractFOPTranscoder
    implements Configurable
{

  /**
   * The key is used to specify the resolution for on-the-fly images generated
   * due to complex effects like gradients and filters. MultiPagePDFTranscoder
   */
  //public static final TranscodingHints.Key KEY_DEVICE_RESOLUTION = new FloatKey();
  /**
   * The key is used to specify whether the available fonts should be automatically
   * detected. The alternative is to configure the transcoder manually using a configuration
   * file.
   */
  //public static final TranscodingHints.Key KEY_AUTO_FONTS = new BooleanKey();
  private Configuration cfg = null;
  /** Graphics2D instance that is used to paint to */
  protected QyoutiPDFDocumentGraphics2D graphics = null;
  private ImageManager imageManager;
  private ImageSessionContext imageSessionContext;
  private int pagecount = 0;


  /**
   * Constructs a new <tt>PDFTranscoder</tt>.
   */
  public MultiPagePDFTranscoder()
  {
    super();
    // ToDo - check out how to embed fonts in the PDF
    //  http://wiki.apache.org/xmlgraphics-fop/SvgNotes/PdfTranscoderTrueTypeEmbedding
    // DefaultConfigurationBuilder dcb = new DefaultConfigurationBuilder();
    // dcb.buildFromFile( null );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected UserAgent createUserAgent()
  {
    return new AbstractFOPTranscoder.FOPTranscoderUserAgent()
    {
      // The PDF stuff wants everything at 72dpi

      @Override
      public float getPixelUnitToMillimeter()
      {
        return super.getPixelUnitToMillimeter();
        //return 25.4f / 72; //72dpi = 0.352778f;
      }
    };
  }

  /** {@inheritDoc} */
  @Override
  public void configure(Configuration cfg) throws ConfigurationException
  {
    super.configure( cfg );
  }

  /**
   * Transcodes the specified Document as an image in the specified output.
   *
   * @param document the document to transcode
   * @param uri the uri of the document or null if any
   * @param output the ouput where to transcode
   * @exception TranscoderException if an error occured while transcoding
   */
  @Override
  protected void transcode(Document document, String uri,
      TranscoderOutput output)
      throws TranscoderException
  {
    pagecount++;
    //System.out.println("transcode pagecount = " + pagecount);

    if (pagecount == 1)
    {
      System.out.println( "transcode configuration isTextStroked = " + isTextStroked() );
      graphics = new QyoutiPDFDocumentGraphics2D(isTextStroked());
      graphics.getPDFDocument().getInfo().setProducer("Apache FOP Version "
          + Version.getVersion()
          + ": PDF Transcoder for Batik");
      if (hints.containsKey(KEY_DEVICE_RESOLUTION))
      {
        graphics.setDeviceDPI(((Float) hints.get(KEY_DEVICE_RESOLUTION)).floatValue());
      }

      setupImageInfrastructure(uri);

      try
      {
        Configuration effCfg = this.cfg;
        if (effCfg == null)
        {
          System.out.println( "No config." );
          //By default, enable font auto-detection if no cfg is given
          boolean autoFonts = true;
          if (hints.containsKey(KEY_AUTO_FONTS))
          {
            autoFonts = ((Boolean) hints.get(KEY_AUTO_FONTS)).booleanValue();
          }
          System.out.println( "transcode configuration autoFonts = " + autoFonts );
          if (autoFonts)
          {
            DefaultConfiguration c = new DefaultConfiguration("pdf-transcoder");
            DefaultConfiguration fonts = new DefaultConfiguration("fonts");
            c.addChild(fonts);
            DefaultConfiguration autodetect = new DefaultConfiguration("auto-detect");
            fonts.addChild(autodetect);
            effCfg = c;
          }
        }

        if (effCfg != null)
        {
          System.out.println( "Now there is config." );
          PDFDocumentGraphics2DConfigurator configurator = new PDFDocumentGraphics2DConfigurator();
          configurator.configure(graphics, effCfg);
        } else
        {
          System.out.println( "Still no config." );
          graphics.setupDefaultFontInfo();
        }
      } catch (Exception e)
      {
        throw new TranscoderException(
            "Error while setting up PDFDocumentGraphics2D", e);
      }
    }
    else
    {
      try
      {
        graphics.startPage();
      } catch (IOException ex)
      {
        throw new TranscoderException(
            "Error while setting up PDFDocumentGraphics2D", ex);
      }
    }

    super.transcode(document, uri, output);

    if (pagecount == 1)
    {
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

      // prepare the image to be painted
      //int w = (int)(width + 0.5);
      //int h = (int)(height + 0.5);

      try
      {
        OutputStream out = output.getOutputStream();
        if (!(out instanceof BufferedOutputStream))
        {
          out = new BufferedOutputStream(out);
        }
        graphics.setupDocument( out, w, h);
        graphics.setSVGDimension(width, height);

        if (hints.containsKey(ImageTranscoder.KEY_BACKGROUND_COLOR))
        {
          graphics.setBackgroundColor((Color) hints.get(ImageTranscoder.KEY_BACKGROUND_COLOR));
        }
        graphics.setGraphicContext(new org.apache.xmlgraphics.java2d.GraphicContext());

        graphics.preparePainting();

        graphics.transform(curTxf);
        graphics.setRenderingHint(RenderingHintsKeyExt.KEY_TRANSCODING,
            RenderingHintsKeyExt.VALUE_TRANSCODING_VECTOR);

      } catch (IOException ex)
      {
        throw new TranscoderException(ex);
      }

    }

    this.root.paint(graphics);
    graphics.nextPage();
  }

  public void complete() throws TranscoderException
  {
    try
    {
      graphics.finish();
    } catch (IOException ex)
    {
      throw new TranscoderException(ex);
    }
  }



//  void setupImageInfrastructure(final String baseURI)
//  {
//    final ImageContext imageContext = new ImageContext()
//    {
//
//      public float getSourceResolution()
//      {
//        return 25.4f / userAgent.getPixelUnitToMillimeter();
//      }
//    };
//    this.imageManager = new ImageManager(imageContext);
//    this.imageSessionContext = new AbstractImageSessionContext()
//    {
//
//      @Override
//      public ImageContext getParentContext()
//      {
//        return imageContext;
//      }
//
//      @Override
//      public float getTargetResolution()
//      {
//        return graphics.getDeviceDPI();
//      }
//
//      @Override
//      public Source resolveURI(String uri)
//      {
//        System.out.println("resolve " + uri);
//        try
//        {
//          ParsedURL url = new ParsedURL(baseURI, uri);
//          InputStream in = url.openStream();
//          StreamSource source = new StreamSource(in, url.toString());
//          return source;
//        } catch (IOException ioe)
//        {
//          userAgent.displayError(ioe);
//          return null;
//        }
//      }
//    };
//  }

  /** {@inheritDoc} */
  @Override
  protected BridgeContext createBridgeContext()
  {
    //For compatibility with Batik 1.6
    return createBridgeContext("1.x");
  }

  /** {@inheritDoc} */
  @Override
  public BridgeContext createBridgeContext(String version)
  {
    FontInfo fontInfo = graphics.getFontInfo();
    if (isTextStroked())
    {
      fontInfo = null;
    }
    BridgeContext ctxx = new PDFBridgeContext(userAgent, fontInfo,
        this.imageManager, this.imageSessionContext);
    return ctxx;
  }
}
