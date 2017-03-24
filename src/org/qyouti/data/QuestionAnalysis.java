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

import java.io.*;
import java.util.Vector;
import org.w3c.dom.*;

/**
 *
 * @author jon
 */
public class QuestionAnalysis
{
  int offset;
  String ident;
  String title;
  double mean_score;
  Vector<ResponseAnalysis> response_analyses = new Vector<ResponseAnalysis>();

  public QuestionAnalysis()
  {
  }
  
  public QuestionAnalysis( Element element )
  {
    String str;
    ident = element.getAttribute("ident");
    
    str = element.getAttribute("offset");
    try { offset = Integer.parseInt( str ); }
    catch ( NumberFormatException nfe ) { offset =  0; }

    title = element.getAttribute("title");

    str = element.getAttribute("mean");
    try { mean_score = Double.parseDouble( str ); }
    catch ( NumberFormatException nfe ) { mean_score =  Double.NaN; }
    
    NodeList nl = element.getElementsByTagName( "responseanalysis" );
    for ( int j=0; j<nl.getLength(); j++ )
      response_analyses.add( new ResponseAnalysis( (Element)nl.item( j ) ) );
 }
    
  
  public void emit( Writer writer )
          throws IOException
  {
      writer.write( "    <itemanalysis ident=\"" );
      writer.write( ident );
      writer.write( "\" offset=\"" + offset );
      writer.write( "\" mean=\"" );
      writer.write( Double.toString(mean_score) );
      writer.write( "\">\n" );
      for ( int i=0; i<response_analyses.size(); i++ )
      {
        response_analyses.get(i).emit( writer );
      }
      writer.write( "    </itemanalysis>\n" );
  }
  
  public String toHTML()
  {
    ResponseAnalysis ra;
    int rows = response_analyses.size();
    if ( rows == 0 )
      return "    <div><br/><strong>No Analysis</strong><br/></div>";
    
    StringBuilder buffer = new StringBuilder();
    buffer.append( "    <div><br/><strong>Analysis</strong></div>" );
    buffer.append( "<table>" );
    buffer.append( "<tr></tr><tr><th>Opt</th><th>T/F</th><th>Sel</th><th>NotSel</th><th>%Right</th>" );    
    buffer.append( "<th>Diff</th><th>Low90</th><th>High90</th></tr>" );    

    String tdstylea, tdstyleb;
    for ( int i=0; i<rows; i++ )
    {
      ra = response_analyses.get( i );
      tdstylea = ra.correct?"<td style=\"background: green;\">":"<td style=\"background: white;\">";
      if ( Double.isNaN( ra.median_difference ) )
        tdstyleb="<td style=\"background: gray;\">";
      else if ( ra.median_difference_lower >= 0.0 )
        tdstyleb="<td style=\"background: green;\">";
      else if ( ra.median_difference_upper <= 0.0 )
        tdstyleb="<td style=\"background: red;\">";
      else
        tdstyleb="<td style=\"background: rgb(255,128,128);\">";
      
      buffer.append( "<tr>" );
      buffer.append( tdstylea );
      buffer.append( ra.ident );
      buffer.append( "</td>\n" );
      buffer.append( tdstylea );
      buffer.append( ra.correct?"T":"F" );
      buffer.append( "</td>\n" );
      buffer.append( tdstylea );
      buffer.append( ra.correct?ra.right:ra.wrong );
      buffer.append( "</td>\n" );
      buffer.append( tdstylea );
      buffer.append( ra.correct?ra.wrong:ra.right );
      buffer.append( "</td>\n" );
      buffer.append( tdstylea );
      buffer.append( Integer.toString( Math.round( 100.0f * (float)ra.right / (float)(ra.right + ra.wrong) ) ) + "%" );
      buffer.append( "</td>\n" );
      buffer.append( tdstyleb );
      buffer.append( Double.toString( ra.median_difference ) );
      buffer.append( "</td>\n" );
      buffer.append( tdstyleb );
      buffer.append( Double.toString( ra.median_difference_lower ) );
      buffer.append( "</td>\n" );
      buffer.append( tdstyleb );
      buffer.append( Double.toString( ra.median_difference_upper ) );
      buffer.append( "</td></tr>\n" );
    }
    buffer.append( "<tr></tr><tr></tr></table>\n" );
    return buffer.toString();
  }
}
