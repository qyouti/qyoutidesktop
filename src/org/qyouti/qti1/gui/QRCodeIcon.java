/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.qyouti.qti1.gui;

import org.qyouti.barcode.ZXingCodec;

/**
 *
 * @author jon
 */
public class QRCodeIcon
        extends SVGIcon
{
    String codedstring;
    double qrwidth;  // width in SVG units of qr code itself (not same as icon width)

    public QRCodeIcon( int w, int h, String str, double qrw )
    {
        width = w;
        height = h;
        codedstring=str;
        qrwidth = qrw;
        update();
    }

    public void update()
    {
        setSVG(ZXingCodec.encode2DSVG(codedstring, qrwidth) );
    }
}
