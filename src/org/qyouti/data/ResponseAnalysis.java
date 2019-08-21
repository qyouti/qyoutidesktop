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

package org.qyouti.data;

import java.io.IOException;
import java.io.Writer;
import org.w3c.dom.*;

/**
 *
 * @author jon
 */
public class ResponseAnalysis
{
  int offset=0;
  String ident = null;
  boolean correct;
  int right=0;
  int wrong=0;
  double median_difference=Double.NaN;
  double median_difference_upper=Double.NaN;
  double median_difference_lower=Double.NaN;

  public ResponseAnalysis()
  {
  }
  
  public ResponseAnalysis( Element element )
  {
    String str;
    ident = element.getAttribute("ident");
    
    str = element.getAttribute("offset");
    try { offset = Integer.parseInt( str ); }
    catch ( NumberFormatException nfe ) { offset =  0; }

    str = element.getAttribute( "correct" );
    correct = str != null && str.toLowerCase().startsWith( "t" );

    str = element.getAttribute("right");
    try { right = Integer.parseInt( str ); }
    catch ( NumberFormatException nfe ) { right =  0; }
    
    str = element.getAttribute("wrong");
    try { wrong = Integer.parseInt( str ); }
    catch ( NumberFormatException nfe ) { wrong =  0; }
    
    str = element.getAttribute("median_difference");
    try { median_difference = Double.parseDouble( str ); }
    catch ( NumberFormatException nfe ) { median_difference =  Double.NaN; }
    
    str = element.getAttribute("median_difference_upper");
    try { median_difference_upper = Double.parseDouble( str ); }
    catch ( NumberFormatException nfe ) { median_difference_upper =  Double.NaN; }
    
    str = element.getAttribute("median_difference_lower");
    try { median_difference_lower = Double.parseDouble( str ); }
    catch ( NumberFormatException nfe ) { median_difference_lower =  Double.NaN; }
    
 }
  
  public void emit( Writer writer )
          throws IOException
  {
      writer.write( "      <responseanalysis ident=\"" );
      writer.write( ident );
      writer.write( "\" offset=\"" + offset + "\"" );

      writer.write( " correct=\"" + correct + "\"" );
      writer.write( " right=\"" + right + "\"" );
      writer.write( " wrong=\"" + wrong + "\"" );

      writer.write( " median_difference=\"" );
      writer.write( Double.toString(median_difference) );
      writer.write( "\"" );

      writer.write( " median_difference_lower=\"" );
      writer.write( Double.toString(median_difference_lower) );
      writer.write( "\"" );

      writer.write( " median_difference_upper=\"" );
      writer.write( Double.toString(median_difference_upper) );
      writer.write( "\"" );

      writer.write( "/>\r\n" );
  }
}
