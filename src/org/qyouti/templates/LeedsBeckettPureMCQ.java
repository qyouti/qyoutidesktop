/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti.templates;

import java.text.*;
import java.util.*;

/**
 *
 * @author jon
 */
public class LeedsBeckettPureMCQ
        extends ExamTemplate
{
  public String getTemplateTitle() {return "Leeds Beckett Pure Multiple Choice Questions. (One right answer per question.)";}

  public static final String main = "<?xml version=\"1.0\"?>\r\n" +
"<examination>\r\n" +
"\r\n" +
"\r\n" +
"<options>\r\n" +
"<option name=\"ignore_flow\">false</option>\r\n" +
"<option name=\"question_titles\">false</option>\r\n" +
"<option name=\"double_sided\">false</option>\r\n" +
"<option name=\"name_in_footer\">true</option>\r\n" +
"<option name=\"cover_sheet\">false</option>\r\n" +
"<option name=\"header\">HEADER</option>\r\n" +
"<option name=\"layout\">2</option>\r\n" +
"<option name=\"question_metrics_qr\">false</option>\r\n" +
"<option name=\"id_in_footer\">true</option>\r\n" +
"</options>\r\n" +
"\r\n" +
"<keys>\r\n" +
"  <administrators>\r\n" +
"  </administrators>\r\n" +
"  <examiners>\r\n" +
"  </examiners>\r\n" +
"  <observers>\r\n" +
"  </observers>\r\n" +
"</keys>\r\n" +
"\r\n" +
"<persons>\r\n" +
"</persons>\r\n" +
"\r\n" +
"<pages>\r\n" +
"</pages>\r\n" +
"\r\n" +
"<papers>\r\n</papers>\r\n" +
"\r\n" +
"<analysis>\r\n" +
"</analysis>\r\n" +
"\r\n" +
"<transforms>\r\n" +
"</transforms>\r\n" +
"\r\n" +
"</examination>";


  public static final String interop_top = "<?xml version=\"1.0\"?>\r\n" +
"<questestinterop xmlns=\"http://www.imsglobal.org/xsd/ims_qtiasiv1p2\" xmlns:qyouti=\"http://www.qyouti.org/qtiext\">\r\n" +
"  <assessment ident=\"ASSESSMENTIDENT\" title=\"Untitled\">\r\n" +
"    <section ident=\"section1\" title=\"Section 1\">\r\n" +
"      <outcomes_processing scoremodel=\"SumOfScores\">\r\n" +
"        <outcomes>\r\n" +
"          <decvar varname=\"SID\" vartype=\"string\"/>\r\n" +
"          <decvar varname=\"NAME\" vartype=\"string\"/>\r\n" +
"          <decvar varname=\"VOID\" vartype=\"string\"/>\r\n" +
"          <decvar varname=\"SCORE\" vartype=\"decimal\"/>\r\n" +
"        </outcomes>\r\n" +
"      </outcomes_processing>\r\n";

public static final String interop_tail = "    </section>\r\n" +
"\r\n" +
"  </assessment>\r\n" +
"</questestinterop>\r\n" +
"\r\n";
  

public static final String itemvoid = "    <item ident=\"void\" title=\"Void\">\r\n" +
"    <presentation qyouti:columns=\"1\">\r\n" +
"      <flow>\r\n" +
"       <flow_label class=\"Row\">\r\n" +
"        <material>\r\n" +
"          <mattext charset=\"US-ASCII\" texttype=\"TEXT/PLAIN\" xml:space=\"preserve\"><![CDATA[\r\n" +
"<p style=\"font-size: 110%;\"><b>TITLELINE</b></p>\r\n" +        
"<table><tr>\r\n" +        
"<td>Use a <b>dark pencil</b> to mark the pink boxes with an X. To correct a mistake erase the X with a soft\r\n" +
"pencil eraser so that it is invisible or almost invisible.  If you use a black or blue pen to indicate \r\n" +
"your answers they will still be recognised but you will not be able to correct mistakes.\r\n" +
"If you completed another sheet and this one is not to be marked put an X in the void box.</td>\r\n" +
        
"<td style=\"text-align: right;\"> ]]></mattext>\r\n" +
"        </material>\r\n" +
"        <response_lid ident=\"resp_yes\" rcardinality=\"Single\">\r\n" +
"          <render_choice shuffle=\"No\">\r\n" +
"              <response_label ident=\"yes\">\r\n" +
"                <material>\r\n" +
"                  <mattext charset=\"US-ASCII\" texttype=\"TEXT/PLAIN\" xml:space=\"preserve\"><![CDATA[Void? ]]></mattext>\r\n" +
"                </material>\r\n" +
"              </response_label>\r\n" +
"          </render_choice>\r\n" +
"        </response_lid>\r\n" +
"        <material>\r\n" +
"          <mattext charset=\"US-ASCII\" texttype=\"TEXT/PLAIN\" xml:space=\"preserve\"><![CDATA[</td></tr></table>]]></mattext>\r\n" +
"        </material>\r\n" +
"       </flow_label>\r\n" +
"      </flow>\r\n" +
"    </presentation>\r\n" +
"    <resprocessing scoremodel=\"SumofScores\">\r\n" +
"      <outcomes>\r\n" +
"        <decvar defaultval=\"no\" varname=\"VOID\" vartype=\"String\"/>\r\n" +
"      </outcomes>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"          <varequal case=\"Yes\" respident=\"resp_yes\">yes</varequal>\r\n" +
"        </conditionvar>\r\n" +
"        <setvar action=\"Set\" varname=\"VOID\">yes</setvar>\r\n" +
"      </respcondition>\r\n" +
"    </resprocessing>\r\n" +
"    </item>\r\n\r\n"; 


public static final String itemsid = "    <item ident=\"sid\" qyouti:candidatetype=\"anonymous\" title=\"Student ID\">\r\n" +
"    <presentation qyouti:columns=\"1\">\r\n" +
"        <material>\r\n" +
"          <mattext texttype=\"TEXT/HTML\" xml:space=\"preserve\"><![CDATA[\r\n" +
"           <table>" +
"            <tr><td><strong>Your&nbsp;Name&nbsp;</strong><br/>(Block&nbsp;Caps)</td><td>]]>\r\n" +
"          </mattext>\r\n" +
"        </material>\r\n" +        
"        <qyouti:render_sketcharea columns=\"20\"/>" +
"        <material>\r\n" +
"          <mattext texttype=\"TEXT/HTML\" xml:space=\"preserve\"><![CDATA[\r\n" +
"            </td></tr><tr><td><strong>Your&nbsp;Student&nbsp;ID&nbsp;</strong></td>\r\n" +
"            <td>Your student ID is the eight digit number printed on your student ID card.\r\n" +
"            (NOT your computer login which starts with a letter.)  In each block below select one\r\n" +
"            digit with a cross to enter your ID digit by digit.\r\n" +
"            </td></tr></table>\r\n" +
"            ]]></mattext>\r\n" +
"        </material>\r\n" +
"      <flow>\r\n" +
"\r\n" +
"       <flow_label class=\"Row\">\r\n" +
"        <material>\r\n" +
"<mattext charset=\"US-ASCII\" texttype=\"TEXT/PLAIN\" xml:space=\"preserve\"><![CDATA[<p><br/></p><table><tr><td><span style=\"font-size: 125%;\">1st digit</span><br/>]]></mattext>\r\n" +
"</material>\r\n" +
"        <response_lid ident=\"digit_1\" rcardinality=\"Single\">\r\n" +
"          <render_choice shuffle=\"No\">\r\n" +
"            <response_label ident=\"a1\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[1&nbsp;&nbsp;&nbsp;]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"a2\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[2&nbsp;&nbsp;&nbsp;]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"a3\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[3<br/>]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"a4\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[4&nbsp;&nbsp;&nbsp;]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"a5\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[5&nbsp;&nbsp;&nbsp;]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"a6\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[6<br/>]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"a7\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[7&nbsp;&nbsp;&nbsp;]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"a8\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[8&nbsp;&nbsp;&nbsp;]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"a9\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[9<br/>]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"a0\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[0]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"          </render_choice>\r\n" +
"        </response_lid>\r\n" +
"\r\n" +
"        <material>\r\n" +
"<mattext xml:space=\"preserve\"><![CDATA[</td><td>&nbsp;&nbsp;&nbsp;&nbsp;</td><td style=\"background: rgb(230,230,230)\"><span style=\"font-size: 125%;\">2nd digit</span><br/>]]></mattext>\r\n" +
"</material>\r\n" +
"        <response_lid ident=\"digit_2\" rcardinality=\"Single\">\r\n" +
"          <render_choice shuffle=\"No\">\r\n" +
"            <response_label ident=\"b1\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[1&nbsp;&nbsp;&nbsp;]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"b2\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[2&nbsp;&nbsp;&nbsp;]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"b3\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[3<br/>]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"b4\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[4&nbsp;&nbsp;&nbsp;]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"b5\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[5&nbsp;&nbsp;&nbsp;]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"b6\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[6<br/>]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"b7\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[7&nbsp;&nbsp;&nbsp;]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"b8\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[8&nbsp;&nbsp;&nbsp;]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"b9\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[9<br/>]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"b0\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[0]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"          </render_choice>\r\n" +
"        </response_lid>\r\n" +
"\r\n" +
"        <material>\r\n" +
"<mattext xml:space=\"preserve\"><![CDATA[</td><td>&nbsp;&nbsp;&nbsp;&nbsp;</td><td><span style=\"font-size: 125%;\">3rd digit</span><br/>]]></mattext>\r\n" +
"</material>\r\n" +
"        <response_lid ident=\"digit_3\" rcardinality=\"Single\">\r\n" +
"          <render_choice shuffle=\"No\">\r\n" +
"            <response_label ident=\"c1\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[1&nbsp;&nbsp;&nbsp;]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"c2\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[2&nbsp;&nbsp;&nbsp;]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"c3\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[3<br/>]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"c4\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[4&nbsp;&nbsp;&nbsp;]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"c5\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[5&nbsp;&nbsp;&nbsp;]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"c6\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[6<br/>]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"c7\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[7&nbsp;&nbsp;&nbsp;]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"c8\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[8&nbsp;&nbsp;&nbsp;]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"c9\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[9<br/>]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"c0\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[0]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"          </render_choice>\r\n" +
"        </response_lid>\r\n" +
"\r\n" +
"        <material>\r\n" +
"<mattext xml:space=\"preserve\"><![CDATA[</td><td>&nbsp;&nbsp;&nbsp;&nbsp;</td><td style=\"background: rgb(230,230,230)\"><span style=\"font-size: 125%;\">4th digit</span><br/>]]></mattext>\r\n" +
"</material>\r\n" +
"        <response_lid ident=\"digit_4\" rcardinality=\"Single\">\r\n" +
"          <render_choice shuffle=\"No\">\r\n" +
"            <response_label ident=\"d1\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[1&nbsp;&nbsp;&nbsp;]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"d2\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[2&nbsp;&nbsp;&nbsp;]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"d3\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[3<br/>]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"d4\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[4&nbsp;&nbsp;&nbsp;]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"d5\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[5&nbsp;&nbsp;&nbsp;]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"d6\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[6<br/>]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"d7\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[7&nbsp;&nbsp;&nbsp;]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"d8\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[8&nbsp;&nbsp;&nbsp;]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"d9\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[9<br/>]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"d0\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[0]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"          </render_choice>\r\n" +
"        </response_lid>\r\n" +
"\r\n" +
"\r\n" +
"\r\n" +
"        <material>\r\n" +
"          <mattext charset=\"US-ASCII\" texttype=\"TEXT/PLAIN\" xml:space=\"preserve\"><![CDATA[</td></tr></table><p><br/></p>]]></mattext>\r\n" +
"        </material>\r\n" +
"\r\n" +
"\r\n" +
"       </flow_label>\r\n" +
"\r\n" +
"\r\n" +
"       <flow_label class=\"Row\">\r\n" +
"        <material>\r\n" +
"<mattext charset=\"US-ASCII\" texttype=\"TEXT/PLAIN\" xml:space=\"preserve\"><![CDATA[<table><tr><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td><td style=\"background: rgb(230,230,230)\"><span style=\"font-size: 125%;\">5th digit</span><br/>]]></mattext>\r\n" +
"</material>\r\n" +
"        <response_lid ident=\"digit_5\" rcardinality=\"Single\">\r\n" +
"          <render_choice shuffle=\"No\">\r\n" +
"            <response_label ident=\"e1\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[1&nbsp;&nbsp;&nbsp;]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"e2\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[2&nbsp;&nbsp;&nbsp;]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"e3\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[3<br/>]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"e4\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[4&nbsp;&nbsp;&nbsp;]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"e5\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[5&nbsp;&nbsp;&nbsp;]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"e6\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[6<br/>]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"e7\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[7&nbsp;&nbsp;&nbsp;]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"e8\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[8&nbsp;&nbsp;&nbsp;]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"e9\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[9<br/>]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"e0\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[0]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"          </render_choice>\r\n" +
"        </response_lid>\r\n" +
"\r\n" +
"        <material>\r\n" +
"<mattext xml:space=\"preserve\"><![CDATA[</td><td>&nbsp;&nbsp;&nbsp;&nbsp;</td><td><span style=\"font-size: 125%;\">6th digit</span><br/>]]></mattext>\r\n" +
"</material>\r\n" +
"        <response_lid ident=\"digit_6\" rcardinality=\"Single\">\r\n" +
"          <render_choice shuffle=\"No\">\r\n" +
"            <response_label ident=\"f1\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[1&nbsp;&nbsp;&nbsp;]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"f2\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[2&nbsp;&nbsp;&nbsp;]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"f3\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[3<br/>]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"f4\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[4&nbsp;&nbsp;&nbsp;]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"f5\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[5&nbsp;&nbsp;&nbsp;]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"f6\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[6<br/>]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"f7\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[7&nbsp;&nbsp;&nbsp;]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"f8\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[8&nbsp;&nbsp;&nbsp;]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"f9\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[9<br/>]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"f0\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[0]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"          </render_choice>\r\n" +
"        </response_lid>\r\n" +
"\r\n" +
"        <material>\r\n" +
"<mattext xml:space=\"preserve\"><![CDATA[</td><td>&nbsp;&nbsp;&nbsp;&nbsp;</td><td style=\"background: rgb(230,230,230)\"><span style=\"font-size: 125%;\">7th digit</span><br/>]]></mattext>\r\n" +
"</material>\r\n" +
"        <response_lid ident=\"digit_7\" rcardinality=\"Single\">\r\n" +
"          <render_choice shuffle=\"No\">\r\n" +
"            <response_label ident=\"g1\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[1&nbsp;&nbsp;&nbsp;]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"g2\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[2&nbsp;&nbsp;&nbsp;]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"g3\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[3<br/>]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"g4\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[4&nbsp;&nbsp;&nbsp;]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"g5\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[5&nbsp;&nbsp;&nbsp;]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"g6\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[6<br/>]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"g7\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[7&nbsp;&nbsp;&nbsp;]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"g8\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[8&nbsp;&nbsp;&nbsp;]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"g9\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[9<br/>]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"g0\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[0]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"          </render_choice>\r\n" +
"        </response_lid>\r\n" +
"\r\n" +
"        <material>\r\n" +
"<mattext xml:space=\"preserve\"><![CDATA[</td><td>&nbsp;</td><td><span style=\"font-size: 125%;\">8th digit</span><br/>]]></mattext>\r\n" +
"</material>\r\n" +
"        <response_lid ident=\"digit_8\" rcardinality=\"Single\">\r\n" +
"          <render_choice shuffle=\"No\">\r\n" +
"            <response_label ident=\"h1\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[1&nbsp;&nbsp;&nbsp;]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"h2\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[2&nbsp;&nbsp;&nbsp;]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"h3\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[3<br/>]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"h4\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[4&nbsp;&nbsp;&nbsp;]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"h5\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[5&nbsp;&nbsp;&nbsp;]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"h6\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[6<br/>]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"h7\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[7&nbsp;&nbsp;&nbsp;]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"h8\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[8&nbsp;&nbsp;&nbsp;]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"h9\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[9<br/>]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"            <response_label ident=\"h0\">\r\n" +
"<material>\r\n" +
"<mattext><![CDATA[0]]></mattext>\r\n" +
"</material>\r\n" +
"</response_label>\r\n" +
"          </render_choice>\r\n" +
"        </response_lid>\r\n" +
"\r\n" +
"\r\n" +
"\r\n" +
"        <material>\r\n" +
"          <mattext charset=\"US-ASCII\" texttype=\"TEXT/PLAIN\" xml:space=\"preserve\"><![CDATA[</td></tr></table><p>&nbsp;</p>]]></mattext>\r\n" +
"        </material>\r\n" +
"\r\n" +
"\r\n" +
"       </flow_label>\r\n" +
"\r\n" +
"\r\n" +
"      </flow>\r\n" +
"\r\n" +
"    </presentation>\r\n" +
"    <resprocessing scoremodel=\"SumofScores\">\r\n" +
"      <outcomes>\r\n" +
"        <decvar defaultval=\"\" varname=\"SID\" vartype=\"string\"/>\r\n" +
"        <decvar defaultval=\"\" varname=\"NAME\" vartype=\"string\"/>\r\n" +
"      </outcomes>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_1\">a0</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">0</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_1\">a1</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">1</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_1\">a2</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">2</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_1\">a3</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">3</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_1\">a4</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">4</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_1\">a5</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">5</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_1\">a6</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">6</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_1\">a7</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">7</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_1\">a8</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">8</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_1\">a9</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">9</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_2\">b0</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">0</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_2\">b1</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">1</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_2\">b2</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">2</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_2\">b3</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">3</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_2\">b4</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">4</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_2\">b5</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">5</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_2\">b6</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">6</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_2\">b7</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">7</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_2\">b8</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">8</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_2\">b9</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">9</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_3\">c0</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">0</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_3\">c1</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">1</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_3\">c2</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">2</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_3\">c3</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">3</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_3\">c4</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">4</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_3\">c5</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">5</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_3\">c6</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">6</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_3\">c7</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">7</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_3\">c8</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">8</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_3\">c9</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">9</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_4\">d0</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">0</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_4\">d1</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">1</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_4\">d2</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">2</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_4\">d3</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">3</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_4\">d4</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">4</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_4\">d5</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">5</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_4\">d6</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">6</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_4\">d7</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">7</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_4\">d8</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">8</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_4\">d9</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">9</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_5\">e0</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">0</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_5\">e1</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">1</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_5\">e2</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">2</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_5\">e3</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">3</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_5\">e4</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">4</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_5\">e5</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">5</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_5\">e6</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">6</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_5\">e7</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">7</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_5\">e8</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">8</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_5\">e9</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">9</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_6\">f0</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">0</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_6\">f1</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">1</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_6\">f2</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">2</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_6\">f3</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">3</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_6\">f4</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">4</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_6\">f5</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">5</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_6\">f6</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">6</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_6\">f7</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">7</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_6\">f8</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">8</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_6\">f9</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">9</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_7\">g0</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">0</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_7\">g1</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">1</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_7\">g2</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">2</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_7\">g3</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">3</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_7\">g4</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">4</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_7\">g5</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">5</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_7\">g6</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">6</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_7\">g7</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">7</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_7\">g8</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">8</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_7\">g9</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">9</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_8\">h0</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">0</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_8\">h1</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">1</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_8\">h2</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">2</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_8\">h3</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">3</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_8\">h4</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">4</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_8\">h5</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">5</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_8\">h6</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">6</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_8\">h7</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">7</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_8\">h8</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">8</setvar>\r\n" +
"      </respcondition>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"<varequal case=\"Yes\" respident=\"digit_8\">h9</varequal>\r\n" +
"</conditionvar>\r\n" +
"<setvar action=\"Add\" varname=\"SID\">9</setvar>\r\n" +
"      </respcondition>\r\n" +
"<qyouti:itemproc_extension>\r\n" +
"  <qyouti:outcomemapping externalmap=\"persons\" invarname=\"SID\" outvarname=\"NAME\">\r\n" +
"  </qyouti:outcomemapping>\r\n" +
"</qyouti:itemproc_extension>\r\n" +
"    </resprocessing>\r\n" +
"    </item>\r\n" +
"\r\n" +
"\r\n" +
"\r\n" +
"\r\n" +
"\r\n";



public static final String itemmcqa = "    <item ident=\"QUESTIONNUMBER\" qyouti:template=\"org.qyouti.templates.PureMCQ\" title=\"Question QUESTIONNUMBER\">\r\n" +
"    <presentation qyouti:columns=\"COLUMNS\">\r\n" +
"      <material>\r\n" +
"<mattext texttype=\"text/html\" xml:space=\"preserve\">Replace this stem text.</mattext>\r\n" +
"</material>\r\n" +
"        <response_lid ident=\"resp_abcd\" rcardinality=\"Single\">\r\n" +
"          <render_choice shuffle=\"No\">\r\n";

public static final String itemmcqoption = 
"              <response_label ident=\"OPTIONIDENT\" qyouti:correct=\"TRUEFALSE\">\r\n" +
"                <material>\r\n" +
"                  <mattext texttype=\"text/html\" xml:space=\"preserve\">Replace this option text.</mattext>\r\n" +
"                </material>\r\n" +
"              </response_label>\r\n";

public static final String itemmcqb = 
"          </render_choice>\r\n" +
"        </response_lid>\r\n" +
"    </presentation>\r\n" +
"    <resprocessing scoremodel=\"SumofScores\">\r\n" +
"      <outcomes>\r\n" +
"        <decvar defaultval=\"0.0\" minvalue=\"0.0\" varname=\"SCORE\" vartype=\"Decimal\"/>\r\n" +
"      </outcomes>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"          <varequal case=\"Yes\" respident=\"resp_abcd\">a</varequal>\r\n" +
"        </conditionvar>\r\n" +
"        <setvar action=\"Add\" varname=\"SCORE\">1</setvar>\r\n" +
"      </respcondition>\r\n" +
"    </resprocessing>\r\n" +
"    </item>\r\n\r\n";


public static final String itemmcqaT = "    <item ident=\"QUESTIONNUMBER\" qyouti:template=\"org.qyouti.templates.PureMCQNoText\" title=\"Question QUESTIONNUMBER\">\r\n" +
"    <presentation qyouti:columns=\"COLUMNS\">\r\n" +
"      <material>\r\n" +
"<mattext charset=\"US-ASCII\" texttype=\"TEXT/PLAIN\" xml:space=\"preserve\"><![CDATA[<div style=\"text-align: right;\">]]></mattext>\r\n" +
"</material>\r\n" +
"      <flow>\r\n" +
"       <flow_label class=\"Row\">\r\n" +
"        <material>\r\n" +
"          <mattext charset=\"US-ASCII\" texttype=\"TEXT/PLAIN\" xml:space=\"preserve\"><![CDATA[<span style=\"font-size: 110%;\">QQUESTIONNUMBER&nbsp;&nbsp;</span>]]></mattext>\r\n" +
"        </material>\r\n" +
"        <response_lid ident=\"resp_abcd\" rcardinality=\"Single\">\r\n" +
"          <render_choice shuffle=\"No\">\r\n";

public static final String itemmcqoptionT = 
"              <response_label ident=\"OPTIONIDENT\" qyouti:correct=\"TRUEFALSE\">\r\n" +
"                <material>\r\n" +
"                  <mattext charset=\"US-ASCII\" texttype=\"TEXT/PLAIN\" xml:space=\"preserve\">OPTIONLETTER<![CDATA[&nbsp;&nbsp;]]></mattext>\r\n" +
"                </material>\r\n" +
"              </response_label>\r\n";

public static final String itemmcqbT = 
"          </render_choice>\r\n" +
"        </response_lid>\r\n" +
"       </flow_label>\r\n" +
"      </flow>\r\n" +
"      <material>\r\n" +
"<mattext charset=\"US-ASCII\" texttype=\"TEXT/PLAIN\" xml:space=\"preserve\"><![CDATA[</div>]]></mattext>\r\n" +
"</material>\r\n" +
"    </presentation>\r\n" +
"    <resprocessing scoremodel=\"SumofScores\">\r\n" +
"      <outcomes>\r\n" +
"        <decvar defaultval=\"0.0\" minvalue=\"0.0\" varname=\"SCORE\" vartype=\"Decimal\"/>\r\n" +
"      </outcomes>\r\n" +
"      <respcondition continue=\"Yes\">\r\n" +
"        <conditionvar>\r\n" +
"          <varequal case=\"Yes\" respident=\"resp_abcd\">a</varequal>\r\n" +
"        </conditionvar>\r\n" +
"        <setvar action=\"Add\" varname=\"SCORE\">1</setvar>\r\n" +
"      </respcondition>\r\n" +
"    </resprocessing>\r\n" +
"    </item>\r\n\r\n";



  
  /**
   * Creates new form MCQAnonymousNoTextExam
   */
  public LeedsBeckettPureMCQ()
  {
    initComponents();
  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents()
  {
    java.awt.GridBagConstraints gridBagConstraints;

    scrollpane1 = new javax.swing.JScrollPane();
    descriptiontextarea = new javax.swing.JTextArea();
    mainpanel = new javax.swing.JPanel();
    titlelabel = new javax.swing.JLabel();
    titlefield = new javax.swing.JTextField();
    crnlabel = new javax.swing.JLabel();
    crnfield = new javax.swing.JTextField();
    datelabel = new javax.swing.JLabel();
    datefield = new javax.swing.JTextField();
    qtestlabel = new javax.swing.JLabel();
    qtextcheckbox = new javax.swing.JCheckBox();
    optslabel = new javax.swing.JLabel();
    scrollpane2 = new javax.swing.JScrollPane();
    optslist = new javax.swing.JList<>();
    questionslabel = new javax.swing.JLabel();
    questionsfield = new javax.swing.JTextField();

    setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
    setLayout(new java.awt.BorderLayout());

    scrollpane1.setOpaque(false);
    scrollpane1.setPreferredSize(new java.awt.Dimension(243, 180));

    descriptiontextarea.setEditable(false);
    descriptiontextarea.setColumns(20);
    descriptiontextarea.setLineWrap(true);
    descriptiontextarea.setRows(8);
    descriptiontextarea.setText("This template produces an exam in which the questions are pure multiple choice. \n\nThere will be one question asking the candidate if they which to 'void' the sheet. This is used if the student chooses to start again with a fresh sheet. For 'anonymous' sheets which are handed out to students indescriminately there will be a name and student ID question. If sheets are printed with student IDs and names on them this question will be omited. The choice about named/anonymous sheets can be made later.\n\nQuestion text can be included in the response sheet or word processed separately. With this template all questions must have the same number of options.");
    descriptiontextarea.setWrapStyleWord(true);
    descriptiontextarea.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
    descriptiontextarea.setOpaque(false);
    scrollpane1.setViewportView(descriptiontextarea);

    add(scrollpane1, java.awt.BorderLayout.NORTH);

    mainpanel.setLayout(new java.awt.GridBagLayout());

    titlelabel.setText("Module Title:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
    gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
    mainpanel.add(titlelabel, gridBagConstraints);

    titlefield.setColumns(20);
    titlefield.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        titlefieldActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
    mainpanel.add(titlefield, gridBagConstraints);

    crnlabel.setText("CRN:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
    gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
    mainpanel.add(crnlabel, gridBagConstraints);

    crnfield.setColumns(10);
    crnfield.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        crnfieldActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
    mainpanel.add(crnfield, gridBagConstraints);

    datelabel.setText("Date of Exam:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
    gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
    mainpanel.add(datelabel, gridBagConstraints);

    datefield.setColumns(10);
    datefield.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        datefieldActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
    mainpanel.add(datefield, gridBagConstraints);

    qtestlabel.setText("Question text:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridy = 4;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
    gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
    mainpanel.add(qtestlabel, gridBagConstraints);

    qtextcheckbox.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
    qtextcheckbox.setText("In response paper");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridy = 4;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
    mainpanel.add(qtextcheckbox, gridBagConstraints);

    optslabel.setText("Number of Options:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridy = 5;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
    gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
    mainpanel.add(optslabel, gridBagConstraints);

    optslist.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
    optslist.setModel(new javax.swing.AbstractListModel<String>()
    {
      String[] strings = { "4", "5", "6", "7", "8", "9", "10" };
      public int getSize() { return strings.length; }
      public String getElementAt(int i) { return strings[i]; }
    });
    optslist.setVisibleRowCount(3);
    scrollpane2.setViewportView(optslist);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridy = 5;
    gridBagConstraints.ipadx = 8;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
    mainpanel.add(scrollpane2, gridBagConstraints);

    questionslabel.setText("Number of Questions:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridy = 6;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
    gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
    mainpanel.add(questionslabel, gridBagConstraints);

    questionsfield.setColumns(5);
    questionsfield.setText("20");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridy = 6;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
    mainpanel.add(questionsfield, gridBagConstraints);

    add(mainpanel, java.awt.BorderLayout.CENTER);
  }// </editor-fold>//GEN-END:initComponents

  private void titlefieldActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_titlefieldActionPerformed
  {//GEN-HEADEREND:event_titlefieldActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_titlefieldActionPerformed

  private void crnfieldActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_crnfieldActionPerformed
  {//GEN-HEADEREND:event_crnfieldActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_crnfieldActionPerformed

  private void datefieldActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_datefieldActionPerformed
  {//GEN-HEADEREND:event_datefieldActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_datefieldActionPerformed


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JTextField crnfield;
  private javax.swing.JLabel crnlabel;
  private javax.swing.JTextField datefield;
  private javax.swing.JLabel datelabel;
  private javax.swing.JTextArea descriptiontextarea;
  private javax.swing.JPanel mainpanel;
  private javax.swing.JLabel optslabel;
  private javax.swing.JList<String> optslist;
  private javax.swing.JLabel qtestlabel;
  private javax.swing.JCheckBox qtextcheckbox;
  private javax.swing.JTextField questionsfield;
  private javax.swing.JLabel questionslabel;
  private javax.swing.JScrollPane scrollpane1;
  private javax.swing.JScrollPane scrollpane2;
  private javax.swing.JTextField titlefield;
  private javax.swing.JLabel titlelabel;
  // End of variables declaration//GEN-END:variables

  @Override
  public String getMainDocumentAsString()
  { 
    return main.replaceFirst( "HEADER",  "DO NOT DISCARD UNUSED OR VOIDED SHEETS - THEY ALL MUST BE SCANNED" );
  }
  
  @Override
  public String getQuestionDocumentAsString()
  {
    int i, j;
    int nq = Integer.parseInt( questionsfield.getText() );
    int no = Integer.parseInt( optslist.getSelectedValue() );
    if ( nq < 0 )
      throw new IllegalArgumentException( "Invalid number." );
    if ( nq > 1000 )
      throw new IllegalArgumentException( "Too many." );

    String cols = "1";
    if ( no <=6 ) cols = "2";
    if ( no <=4 ) cols = "3";
    
    boolean textinpaper = qtextcheckbox.isSelected();
    String ia = textinpaper?itemmcqa:itemmcqaT;
    String io = textinpaper?itemmcqoption:itemmcqoptionT;
    String ib = textinpaper?itemmcqb:itemmcqbT;

    StringBuilder buffer = new StringBuilder(1000);
    String opt, str = interop_top;
    Random r = new Random();
    r.setSeed( System.currentTimeMillis() );

    String examtitle = titlefield.getText() + " - CRN" + crnfield.getText() + " - " + datefield.getText();
    
    buffer.append( str.replaceFirst( "ASSESSMENTIDENT", Long.toHexString( r.nextLong() ) ) );
    buffer.append( itemvoid.replaceFirst( "TITLELINE", examtitle ) );
    buffer.append( itemsid );
    String opttext;
    for ( i=0; i<nq; i++ )
    {
      str = ia.replaceAll( "QUESTIONNUMBER", Integer.toString( i+1 ) );
      str = str.replaceAll( "COLUMNS", cols );
      buffer.append( str );
      for ( j=0; j<no; j++ )
      {
        opt = io.replaceFirst( "OPTIONIDENT", Character.toString( (char)('a' + j) ) );
        opt = opt.replaceFirst( "TRUEFALSE", (j==0)?"true":"false" );
        opttext= Character.toString( (char)('A' + j) );
        buffer.append( opt.replaceFirst( "OPTIONLETTER", opttext ) );
      }
      buffer.append( ib );
    }
  
    buffer.append( interop_tail );
    return buffer.toString();
  }
}
