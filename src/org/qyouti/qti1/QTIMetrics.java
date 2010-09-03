/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.qyouti.qti1;

import java.awt.Toolkit;

/**
 *
 * @author jon
 */
public class QTIMetrics
{
    public static final double TYPICAL_VDU_DPI = 100.0;
    public static final double SVG_UNITS_PER_PAGE_INCH = 1000.0;

    public static final double ACTUAL_VDU_DPI =
            (double)Toolkit.getDefaultToolkit().getScreenResolution();

    // QTI sizes and offsets are all expressed in VDU pixels
    // The SVG maps onto page units
    public static double qtiToSvg( double q )
    {
        return (q/TYPICAL_VDU_DPI)*SVG_UNITS_PER_PAGE_INCH;
    }

    
    public static double inchesToSvg( double i )
    {
        return i*SVG_UNITS_PER_PAGE_INCH;
    }

    public static double svgToInches( double s )
    {
        return s / SVG_UNITS_PER_PAGE_INCH;
    }

    public static double svgToPixels( double s )
    {
        return (s / SVG_UNITS_PER_PAGE_INCH)*ACTUAL_VDU_DPI;
    }

}
