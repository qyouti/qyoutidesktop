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
    String ident;
    double qheight;  // total height of question - v. distance to next question
    String coords;   // , delim. coords of pink boxes relative to qr ref centre
    double qrwidth;  // width in SVG units of qr code itself (not same as icon width)

    public QRCodeIcon( int w, int h, String id, double qrw )
    {
        width = w;
        height = h;
        ident=id;
        qrwidth = qrw;
        update( 0.0, "" );
    }

    public void update( double qh, String c )
    {
        qheight = qh;
        coords = c;
        org.w3c.dom.Element g  = QRCodec.svgQuestionQRCode(ident, qheight, coords, qrwidth);
        setSVG( g );
    }
}
