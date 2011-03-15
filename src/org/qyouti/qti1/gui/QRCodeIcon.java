/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.qyouti.qti1.gui;

import org.qyouti.qrcode.QRCodec;

/**
 *
 * @author jon
 */
public class QRCodeIcon
        extends SVGIcon
{
    String codedstring;
    double qheight;  // total height of question - v. distance to next question
    String coords;   // , delim. coords of pink boxes relative to qr ref centre
    double qrwidth;  // width in SVG units of qr code itself (not same as icon width)


    int padding;
    
    public QRCodeIcon( int w, int h, String str, double qrw )
    {
        width = w;
        height = h;
        codedstring=str;
        qrwidth = qrw;
        //padding = (int)(0.4*qrw);
        padding = 0;
        update( null );
    }

    public int getPadding()
    {
      return padding;
    }

    public void update( QuestionMetricsRecord mrec )
    {
        org.w3c.dom.Element g;
        if ( mrec == null )
          g = QRCodec.encodeSVG(codedstring, qrwidth);
        else
          g = QRCodec.encodeSVG( mrec.toByteArray(), qrwidth);

        g.setAttribute("transform", "translate(" + padding + ", " + padding + ")" );
        setSVG( g );
    }
}
