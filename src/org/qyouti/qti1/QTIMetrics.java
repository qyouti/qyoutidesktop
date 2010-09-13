/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.qyouti.qti1;

import java.awt.Toolkit;
import java.util.Properties;

/**
 *
 * @author jon
 */
public class QTIMetrics
        extends Properties
{
    public static final double TYPICAL_VDU_DPI = 100.0;
    public static final double SVG_UNITS_PER_PAGE_INCH = 100.0;

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


    public QTIMetrics()
    {
      try
      {
        loadFromXML(getClass().getClassLoader().getResourceAsStream("org/qyouti/qti1/gui/defaultmetrics.xml"));
      } catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }
    
    public double getPropertyInches( String name )
    {
      String s = this.getProperty(name);
      if ( s == null ) return 0.0;
      return Double.parseDouble(s);
    }
    
    public double getPropertySvgUnits( String name )
    {
      String s = this.getProperty(name);
      if ( s == null ) return 0.0;
      return inchesToSvg( Double.parseDouble(s) );
    }
    public int getPropertySvgUnitsInt( String name )
    {
      return (int)getPropertySvgUnits( name );
    }
}
