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
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.qyouti.fonts.*;



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
    // relative URLs are relative to qyouti installation fonts folder
    super.clear();
    String conf = QyoutiFontManager.CONFIGXML.replaceFirst( 
            "INSERT", 
            "<directory>" + QyoutiFontManager.getBuiltinFontDirectory().getAbsolutePath() + "</directory>\n" );
    super.setProperty( "qyouti.print.font.fopconfig", conf );
        
    super.setProperty( "qyouti.print.font-family-sans",     "FreeSans,FreeSerif"  );
    super.setProperty( "qyouti.print.font-family-serif",    "FreeSerif" );
    super.setProperty( "qyouti.print.font-family-monospace","FreeMono,FreeSerif" );
  }

  
  public int getPropertyInt( String key )
  {
    try { return Integer.parseInt( getProperty(key) ); }
    catch ( Exception e ) { return 0; }
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
