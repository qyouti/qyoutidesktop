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

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.channels.FileChannel;
import java.util.Enumeration;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.batik.dom.util.DOMUtilities;
import org.qyouti.xml.RepoEntityResolver;
import org.w3c.dom.Element;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author jon
 */
public class QyoutiUtils
{

  static Random random = new Random(System.currentTimeMillis());

  public static String randomIdent()
  {
    byte[] b = new byte[16];
    random.nextBytes(b);
    StringBuffer ident = new StringBuffer();
    for (int i = 0; i < b.length; i++)
    {
      ident.append(Integer.toHexString((int) b[i] & 0x0f));
      ident.append(Integer.toHexString(((int) b[i] & 0xf0) >> 4));
    }
    return ident.toString();
  }

  public static boolean copyFile(File source, File destination)
  {
    try
    {
      FileChannel sourcechan = new FileInputStream(source).getChannel();
      FileChannel destinationchan = new FileOutputStream(destination).getChannel();
      destinationchan.transferFrom(sourcechan, 0, sourcechan.size());
      sourcechan.close();
      destinationchan.close();
    } catch (Exception ex)
    {
      Logger.getLogger(QyoutiUtils.class.getName()).log(Level.SEVERE, null, ex);
      return false;
    }
    return true;
  }

  public static boolean unpackZip(File file, File importfolder) throws ZipException, IOException
  {
    ZipFile zipfile = new ZipFile(file);
    Enumeration<? extends ZipEntry> e = zipfile.entries();
    ZipEntry zipentry;
    File entryfile;
    int b;

    while (e.hasMoreElements())
    {
      zipentry = e.nextElement();
      System.out.println(zipentry.getName());
      entryfile = new File(importfolder, zipentry.getName());
      System.out.println(entryfile.getCanonicalPath());
      FileOutputStream fout;
      InputStream in;
      if (zipentry.isDirectory())
      {
        entryfile.mkdirs();
      } else
      {
        entryfile.getParentFile().mkdirs();
        in = zipfile.getInputStream(zipentry);
        fout = new FileOutputStream(entryfile);
        while ((b = in.read()) >= 0)
        {
          fout.write(b);
        }
        fout.close();
        in.close();
      }
    }

    return true;
  }


  public static int parseInt(String s, int start, int length, int radix)
      throws NumberFormatException
  {
    if (s == null || length < 1 || start < 0)
    {
      throw new NumberFormatException("null");
    }

    if (start >= s.length() || start + length > s.length())
    {
      throw new NumberFormatException("invalid substring");
    }

    if (radix < Character.MIN_RADIX)
    {
      throw new NumberFormatException("radix " + radix
          + " less than Character.MIN_RADIX");
    }

    if (radix > Character.MAX_RADIX)
    {
      throw new NumberFormatException("radix " + radix
          + " greater than Character.MAX_RADIX");
    }

    int result = 0;
    boolean negative = false;
    int i = start;
    int max = start + length;
    int limit;
    int multmin;
    int digit;

    if (s.charAt(i) == '-')
    {
      negative = true;
      limit = Integer.MIN_VALUE;
      i++;
    } else
    {
      limit = -Integer.MAX_VALUE;
    }

    multmin = limit / radix;
    if (i < max)
    {
      digit = Character.digit(s.charAt(i++), radix);
      if (digit < 0)
      {
        throw new NumberFormatException("Can't parse number.");
      } else
      {
        result = -digit;
      }
    }
    while (i < max)
    {
      // Accumulating negatively avoids surprises near MAX_VALUE
      digit = Character.digit(s.charAt(i++), radix);
      if (digit < 0)
      {
        throw new NumberFormatException("Can't parse number.");
      }
      if (result < multmin)
      {
        throw new NumberFormatException("Can't parse number.");
      }
      result *= radix;
      if (result < limit + digit)
      {
        throw new NumberFormatException("Can't parse number.");
      }
      result -= digit;
    }
    if (negative)
    {
      if (i > 1)
      {
        return result;
      } else
      {	/* Only got "-" */
        throw new NumberFormatException("Can't parse number.");
      }
    } else
    {
      return -result;
    }
  }



  public static void dumpXMLFile( String filename, Element content, boolean addhead )
  {
    String strcontent = DOMUtilities.getXML( content );

    FileWriter writer = null;
    try
    {
        writer = new FileWriter( filename );
        if ( addhead )
          writer.write("<?xml version=\"1.0\"?>\n");
        writer.write(strcontent);
    } catch (IOException ex)
    {
        ex.printStackTrace();
    } finally
    {
        try
        {
            writer.close();
        } catch (IOException ex)
        {
          ex.printStackTrace();
        }
    }
  }


  public static double[] convertFloatsToDoubles(float[] input)
  {
      if (input == null)
          return null;
      double[] output = new double[input.length];
      for (int i = 0; i < input.length; i++)
          output[i] = input[i];
      return output;
  }

  public static float[] convertDoublesToFloats(double[] input)
  {
      if (input == null)
          return null;
      float[] output = new float[input.length];
      for (int i = 0; i < input.length; i++)
          output[i] = (float) input[i];
      return output;
  }

    public static BufferedImage resizeImage(BufferedImage originalImage, int width, int height )
    {
      BufferedImage resizedImage = new BufferedImage(width, height, originalImage.getType());
      Graphics2D g = resizedImage.createGraphics();
      g.drawImage(originalImage, 0, 0, width, height, null);
      g.dispose();
      return resizedImage;
    }

}

