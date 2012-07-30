/*
 *
 * Copyright 2010 Leeds Metropolitan University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may 
 * not use this file except in compliance with the License. You may obtain 
 * a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 *
 *
 */



/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.qyouti;

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 *
 * @author jon
 */
public class QyoutiPreferences
        extends Properties
{
  File file;

  public QyoutiPreferences( File file )
  {
    this.file = file;
  }

  public void setDefaults()
  {
    super.setProperty( "qyouti.print.font-family", Font.SANS_SERIF );
    super.setProperty( "qyouti.qrcode.error-correction", "M" );
    super.setProperty( "qyouti.scanning.threshold", "50" );
    super.setProperty( "qyouti.scanning.inset", "50" );
  }

  public ErrorCorrectionLevel getQRCodeErrorCorrection()
  {
    String qrec = getProperty( "qyouti.qrcode.error-correction" );
    if ( "H".equalsIgnoreCase( qrec ) )
      return ErrorCorrectionLevel.H;
    if ( "M".equalsIgnoreCase( qrec ) )
      return ErrorCorrectionLevel.M;
    if ( "L".equalsIgnoreCase( qrec ) )
      return ErrorCorrectionLevel.L;
    if ( "Q".equalsIgnoreCase( qrec ) )
      return ErrorCorrectionLevel.Q;
    return ErrorCorrectionLevel.L;
  }

  public int getPropertyInt( String key )
  {
    try
    {
      return Integer.parseInt( getProperty(key) );
    }
    catch ( Exception e )
    {
      return 0;
    }
  }

  @Override
  public Object setProperty( String key, String value )
  {
    Object r = super.setProperty( key, value );
    save();
    return r;
  }


  public boolean load()
  {
    try
    {
      FileInputStream in = new FileInputStream(file);
      this.loadFromXML( in );
      in.close();
    }
    catch (Exception ex)
    {
      Logger.getLogger(QyoutiPreferences.class.getName()).log(Level.SEVERE, null, ex);
      return false;
    }
    return true;
  }

  public boolean save()
  {
    try
    {
      FileOutputStream out = new FileOutputStream(file);
      this.storeToXML(out, "Qyouti preferences file.");
      out.close();
    }
    catch ( Exception ex )
    {
      Logger.getLogger(QyoutiPreferences.class.getName()).log(Level.SEVERE, null, ex);
      return false;
    }
    return true;
  }
}
