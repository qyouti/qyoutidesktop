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

package org.qyouti.util;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jon
 */
public class QyoutiUtils
{

  public static boolean copyFile( File source, File destination )
  {
    try
    {
      FileChannel sourcechan = new FileInputStream(source).getChannel();
      FileChannel destinationchan = new FileOutputStream(destination).getChannel();
      destinationchan.transferFrom( sourcechan, 0, sourcechan.size() );
      sourcechan.close();
      destinationchan.close();
    } catch (Exception ex)
    {
      Logger.getLogger(QyoutiUtils.class.getName()).log(Level.SEVERE, null, ex);
      return false;
    }
    return true;
  }
}
