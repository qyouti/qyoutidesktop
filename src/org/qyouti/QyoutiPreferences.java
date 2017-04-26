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



/**
 *
 * @author jon
 */
public class QyoutiPreferences
        extends Properties
{
  File file;
  ArrayList<String> fontnamelist = new ArrayList<>();
  HashMap<String,String> fonturltable = new HashMap<>();

  public QyoutiPreferences( File file )
  {
    this.file = file;
  }

  public void setDefaults()
  {    
    // relative URLs are relative to qyouti installation fonts folder
    super.clear();
    super.setProperty( "qyouti.print.font.count",   "12"                       );
    super.setProperty( "qyouti.print.font.path.1",  "FreeSans.ttf"             );
    super.setProperty( "qyouti.print.font.path.2",  "FreeSansBold.ttf"         );
    super.setProperty( "qyouti.print.font.path.3",  "FreeSansBoldOblique.ttf"  );
    super.setProperty( "qyouti.print.font.path.4",  "FreeSansOblique.ttf"      );
    super.setProperty( "qyouti.print.font.path.5",  "FreeSerif.ttf"            );
    super.setProperty( "qyouti.print.font.path.6",  "FreeSerifBold.ttf"        );
    super.setProperty( "qyouti.print.font.path.7",  "FreeSerifBoldItalic.ttf"  );
    super.setProperty( "qyouti.print.font.path.8",  "FreeSerifItalic.ttf"      );
    super.setProperty( "qyouti.print.font.path.9",  "FreeMono.ttf"             );
    super.setProperty( "qyouti.print.font.path.10", "FreeMonoBold.ttf"         );
    super.setProperty( "qyouti.print.font.path.11", "FreeMonoBoldOblique.ttf"  );
    super.setProperty( "qyouti.print.font.path.12", "FreeMonoOblique.ttf"      );
    
    
    super.setProperty( "qyouti.print.font-family-sans",     "FreeSans,FreeSerif"  );
    super.setProperty( "qyouti.print.font-family-serif",    "FreeSerif" );
    super.setProperty( "qyouti.print.font-family-monospace","FreeMono,FreeSerif" );
  }

  public String getFontURL( String name )
  {
    String url = fonturltable.get( name );
    if ( "notfound".equals( url ) )
      return null;
    return url;
  }
  
  public int getPropertyInt( String key )
  {
    try { return Integer.parseInt( getProperty(key) ); }
    catch ( Exception e ) { return 0; }
  }

  public void rebuild()
  {
    int n = getPropertyInt( "qyouti.print.font.count" );
    int i;
    String name, fullname;
    
    fontnamelist.clear();
    fonturltable.clear();
    for ( i=1; i<=n; i++ )
    {
      name = getProperty( "qyouti.print.font.name." + i );
      fullname = getProperty( "qyouti.print.font.url." + i );
      fontnamelist.add( name );
      fonturltable.put( name, fullname );
    }    
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
    rebuild();
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
