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
public class MCQAnonymousNoTextExam
        extends ExamTemplate
{
  public String getTemplateTitle() {return "Multiple choice, separate question paper.";}

  public static final String texta = "<?xml version=\"1.0\"?>\n" +
"<examination>\n" +
"\n" +
"\n" +
"<options>\n" +
"<option name=\"ignore_flow\">false</option>\n" +
"<option name=\"question_titles\">false</option>\n" +
"<option name=\"double_sided\">false</option>\n" +
"<option name=\"name_in_footer\">true</option>\n" +
"<option name=\"cover_sheet\">false</option>\n" +
"<option name=\"header\">Example Examination Paper</option>\n" +
"<option name=\"question_metrics_qr\">false</option>\n" +
"<option name=\"id_in_footer\">true</option>\n" +
"</options>\n" +
"\n" +
"\n" +
"<questestinterop xmlns=\"http://www.imsglobal.org/xsd/ims_qtiasiv1p2\" xmlns:qyouti=\"http://www.qyouti.org/qtiext\">\n" +
"  <assessment ident=\"ASSESSMENTIDENT\" title=\"Untitled\">\n" +
"    <section ident=\"section1\" title=\"Section 1\">\n" +
"      <outcomes_processing scoremodel=\"SumOfScores\">\n" +
"        <outcomes>\n" +
"          <decvar varname=\"SCORE\" vartype=\"decimal\"/>\n" +
"        </outcomes>\n" +
"      </outcomes_processing>\n";


public static final String textb = "    </section>\n" +
"\n" +
"  </assessment>\n" +
"</questestinterop>\n" +
"\n" +
"<pages>\n" +
"</pages>\n" +
"\n" +
"<candidates>\n";

public static final String textc = "</candidates>\n" +
"\n" +
"<analysis>\n" +
"</analysis>\n" +
"\n" +
"<transforms>\n" +
"</transforms>\n" +
"\n" +
"</examination>";
  

public static final String itemvoid = "    <item ident=\"void\" title=\"Void\">\n" +
"    <presentation qyouti:columns=\"1\">\n" +
"      <flow>\n" +
"       <flow_label class=\"Row\">\n" +
"        <material>\n" +
"          <mattext charset=\"US-ASCII\" texttype=\"TEXT/PLAIN\" xml:space=\"preserve\"><![CDATA[\n" +
"<p>Use a <b>dark pencil</b> to mark the pink boxes with an X. To correct a mistake erase the X with a soft\n" +
"pencil eraser so that it is invisible or almost invisible.  If you use a black or blue pen to indicate \n" +
"your answers they will still be recognised but you will not be able to correct mistakes.\n" +
"If you completed another sheet and this one is not to be marked select:</p>\n" +
"<p style=\"text-align: right;\"> ]]></mattext>\n" +
"        </material>\n" +
"        <response_lid ident=\"resp_yes\" rcardinality=\"Single\">\n" +
"          <render_choice shuffle=\"No\">\n" +
"              <response_label ident=\"yes\">\n" +
"                <material>\n" +
"                  <mattext charset=\"US-ASCII\" texttype=\"TEXT/PLAIN\" xml:space=\"preserve\"><![CDATA[Void? ]]></mattext>\n" +
"                </material>\n" +
"              </response_label>\n" +
"          </render_choice>\n" +
"        </response_lid>\n" +
"        <material>\n" +
"          <mattext charset=\"US-ASCII\" texttype=\"TEXT/PLAIN\" xml:space=\"preserve\"><![CDATA[</p>]]></mattext>\n" +
"        </material>\n" +
"       </flow_label>\n" +
"      </flow>\n" +
"    </presentation>\n" +
"    <resprocessing scoremodel=\"SumofScores\">\n" +
"      <outcomes>\n" +
"        <decvar defaultval=\"no\" varname=\"VOID\" vartype=\"String\"/>\n" +
"      </outcomes>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"          <varequal case=\"Yes\" respident=\"resp_yes\">yes</varequal>\n" +
"        </conditionvar>\n" +
"        <setvar action=\"Set\" varname=\"VOID\">yes</setvar>\n" +
"      </respcondition>\n" +
"    </resprocessing>\n" +
"    </item>\n\n"; 


public static final String itemsid = "    <item ident=\"sid\" title=\"Student ID\">\n" +
"    <presentation qyouti:columns=\"1\">\n" +
"        <material>\n" +
"          <mattext charset=\"US-ASCII\" texttype=\"TEXT/PLAIN\" xml:space=\"preserve\"><![CDATA[\n" +
"            <p style=\"font-size: 125%\">Your Student ID</p>\n" +
"            <p>Your student ID is the eight digit number printed on your student ID card.\n" +
"            (NOT your computer login which starts with a letter.)  In each block below select one\n" +
"            digit with a cross to enter your ID digit by digit.\n" +
"            </p>\n" +
"            ]]></mattext>\n" +
"        </material>\n" +
"      <flow>\n" +
"\n" +
"       <flow_label class=\"Row\">\n" +
"        <material>\n" +
"<mattext charset=\"US-ASCII\" texttype=\"TEXT/PLAIN\" xml:space=\"preserve\"><![CDATA[<p><br/><br/></p><table><tr><td><span style=\"font-size: 125%;\">1st digit</span><br/>]]></mattext>\n" +
"</material>\n" +
"        <response_lid ident=\"digit_1\" rcardinality=\"Single\">\n" +
"          <render_choice shuffle=\"No\">\n" +
"            <response_label ident=\"a1\">\n" +
"<material>\n" +
"<mattext><![CDATA[1&nbsp;&nbsp;&nbsp;]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"a2\">\n" +
"<material>\n" +
"<mattext><![CDATA[2&nbsp;&nbsp;&nbsp;]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"a3\">\n" +
"<material>\n" +
"<mattext><![CDATA[3<br/>]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"a4\">\n" +
"<material>\n" +
"<mattext><![CDATA[4&nbsp;&nbsp;&nbsp;]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"a5\">\n" +
"<material>\n" +
"<mattext><![CDATA[5&nbsp;&nbsp;&nbsp;]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"a6\">\n" +
"<material>\n" +
"<mattext><![CDATA[6<br/>]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"a7\">\n" +
"<material>\n" +
"<mattext><![CDATA[7&nbsp;&nbsp;&nbsp;]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"a8\">\n" +
"<material>\n" +
"<mattext><![CDATA[8&nbsp;&nbsp;&nbsp;]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"a9\">\n" +
"<material>\n" +
"<mattext><![CDATA[9<br/>]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"a0\">\n" +
"<material>\n" +
"<mattext><![CDATA[0]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"          </render_choice>\n" +
"        </response_lid>\n" +
"\n" +
"        <material>\n" +
"<mattext xml:space=\"preserve\"><![CDATA[</td><td>&nbsp;&nbsp;&nbsp;&nbsp;</td><td style=\"background: rgb(230,230,230)\"><span style=\"font-size: 125%;\">2nd digit</span><br/>]]></mattext>\n" +
"</material>\n" +
"        <response_lid ident=\"digit_2\" rcardinality=\"Single\">\n" +
"          <render_choice shuffle=\"No\">\n" +
"            <response_label ident=\"b1\">\n" +
"<material>\n" +
"<mattext><![CDATA[1&nbsp;&nbsp;&nbsp;]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"b2\">\n" +
"<material>\n" +
"<mattext><![CDATA[2&nbsp;&nbsp;&nbsp;]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"b3\">\n" +
"<material>\n" +
"<mattext><![CDATA[3<br/>]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"b4\">\n" +
"<material>\n" +
"<mattext><![CDATA[4&nbsp;&nbsp;&nbsp;]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"b5\">\n" +
"<material>\n" +
"<mattext><![CDATA[5&nbsp;&nbsp;&nbsp;]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"b6\">\n" +
"<material>\n" +
"<mattext><![CDATA[6<br/>]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"b7\">\n" +
"<material>\n" +
"<mattext><![CDATA[7&nbsp;&nbsp;&nbsp;]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"b8\">\n" +
"<material>\n" +
"<mattext><![CDATA[8&nbsp;&nbsp;&nbsp;]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"b9\">\n" +
"<material>\n" +
"<mattext><![CDATA[9<br/>]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"b0\">\n" +
"<material>\n" +
"<mattext><![CDATA[0]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"          </render_choice>\n" +
"        </response_lid>\n" +
"\n" +
"        <material>\n" +
"<mattext xml:space=\"preserve\"><![CDATA[</td><td>&nbsp;&nbsp;&nbsp;&nbsp;</td><td><span style=\"font-size: 125%;\">3rd digit</span><br/>]]></mattext>\n" +
"</material>\n" +
"        <response_lid ident=\"digit_3\" rcardinality=\"Single\">\n" +
"          <render_choice shuffle=\"No\">\n" +
"            <response_label ident=\"c1\">\n" +
"<material>\n" +
"<mattext><![CDATA[1&nbsp;&nbsp;&nbsp;]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"c2\">\n" +
"<material>\n" +
"<mattext><![CDATA[2&nbsp;&nbsp;&nbsp;]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"c3\">\n" +
"<material>\n" +
"<mattext><![CDATA[3<br/>]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"c4\">\n" +
"<material>\n" +
"<mattext><![CDATA[4&nbsp;&nbsp;&nbsp;]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"c5\">\n" +
"<material>\n" +
"<mattext><![CDATA[5&nbsp;&nbsp;&nbsp;]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"c6\">\n" +
"<material>\n" +
"<mattext><![CDATA[6<br/>]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"c7\">\n" +
"<material>\n" +
"<mattext><![CDATA[7&nbsp;&nbsp;&nbsp;]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"c8\">\n" +
"<material>\n" +
"<mattext><![CDATA[8&nbsp;&nbsp;&nbsp;]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"c9\">\n" +
"<material>\n" +
"<mattext><![CDATA[9<br/>]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"c0\">\n" +
"<material>\n" +
"<mattext><![CDATA[0]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"          </render_choice>\n" +
"        </response_lid>\n" +
"\n" +
"        <material>\n" +
"<mattext xml:space=\"preserve\"><![CDATA[</td><td>&nbsp;&nbsp;&nbsp;&nbsp;</td><td style=\"background: rgb(230,230,230)\"><span style=\"font-size: 125%;\">4th digit</span><br/>]]></mattext>\n" +
"</material>\n" +
"        <response_lid ident=\"digit_4\" rcardinality=\"Single\">\n" +
"          <render_choice shuffle=\"No\">\n" +
"            <response_label ident=\"d1\">\n" +
"<material>\n" +
"<mattext><![CDATA[1&nbsp;&nbsp;&nbsp;]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"d2\">\n" +
"<material>\n" +
"<mattext><![CDATA[2&nbsp;&nbsp;&nbsp;]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"d3\">\n" +
"<material>\n" +
"<mattext><![CDATA[3<br/>]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"d4\">\n" +
"<material>\n" +
"<mattext><![CDATA[4&nbsp;&nbsp;&nbsp;]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"d5\">\n" +
"<material>\n" +
"<mattext><![CDATA[5&nbsp;&nbsp;&nbsp;]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"d6\">\n" +
"<material>\n" +
"<mattext><![CDATA[6<br/>]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"d7\">\n" +
"<material>\n" +
"<mattext><![CDATA[7&nbsp;&nbsp;&nbsp;]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"d8\">\n" +
"<material>\n" +
"<mattext><![CDATA[8&nbsp;&nbsp;&nbsp;]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"d9\">\n" +
"<material>\n" +
"<mattext><![CDATA[9<br/>]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"d0\">\n" +
"<material>\n" +
"<mattext><![CDATA[0]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"          </render_choice>\n" +
"        </response_lid>\n" +
"\n" +
"\n" +
"\n" +
"        <material>\n" +
"          <mattext charset=\"US-ASCII\" texttype=\"TEXT/PLAIN\" xml:space=\"preserve\"><![CDATA[</td></tr></table><p><br/><br/></p>]]></mattext>\n" +
"        </material>\n" +
"\n" +
"\n" +
"       </flow_label>\n" +
"\n" +
"\n" +
"       <flow_label class=\"Row\">\n" +
"        <material>\n" +
"<mattext charset=\"US-ASCII\" texttype=\"TEXT/PLAIN\" xml:space=\"preserve\"><![CDATA[<table><tr><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td><td style=\"background: rgb(230,230,230)\"><span style=\"font-size: 125%;\">5th digit</span><br/>]]></mattext>\n" +
"</material>\n" +
"        <response_lid ident=\"digit_5\" rcardinality=\"Single\">\n" +
"          <render_choice shuffle=\"No\">\n" +
"            <response_label ident=\"e1\">\n" +
"<material>\n" +
"<mattext><![CDATA[1&nbsp;&nbsp;&nbsp;]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"e2\">\n" +
"<material>\n" +
"<mattext><![CDATA[2&nbsp;&nbsp;&nbsp;]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"e3\">\n" +
"<material>\n" +
"<mattext><![CDATA[3<br/>]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"e4\">\n" +
"<material>\n" +
"<mattext><![CDATA[4&nbsp;&nbsp;&nbsp;]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"e5\">\n" +
"<material>\n" +
"<mattext><![CDATA[5&nbsp;&nbsp;&nbsp;]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"e6\">\n" +
"<material>\n" +
"<mattext><![CDATA[6<br/>]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"e7\">\n" +
"<material>\n" +
"<mattext><![CDATA[7&nbsp;&nbsp;&nbsp;]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"e8\">\n" +
"<material>\n" +
"<mattext><![CDATA[8&nbsp;&nbsp;&nbsp;]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"e9\">\n" +
"<material>\n" +
"<mattext><![CDATA[9<br/>]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"e0\">\n" +
"<material>\n" +
"<mattext><![CDATA[0]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"          </render_choice>\n" +
"        </response_lid>\n" +
"\n" +
"        <material>\n" +
"<mattext xml:space=\"preserve\"><![CDATA[</td><td>&nbsp;&nbsp;&nbsp;&nbsp;</td><td><span style=\"font-size: 125%;\">6th digit</span><br/>]]></mattext>\n" +
"</material>\n" +
"        <response_lid ident=\"digit_6\" rcardinality=\"Single\">\n" +
"          <render_choice shuffle=\"No\">\n" +
"            <response_label ident=\"f1\">\n" +
"<material>\n" +
"<mattext><![CDATA[1&nbsp;&nbsp;&nbsp;]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"f2\">\n" +
"<material>\n" +
"<mattext><![CDATA[2&nbsp;&nbsp;&nbsp;]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"f3\">\n" +
"<material>\n" +
"<mattext><![CDATA[3<br/>]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"f4\">\n" +
"<material>\n" +
"<mattext><![CDATA[4&nbsp;&nbsp;&nbsp;]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"f5\">\n" +
"<material>\n" +
"<mattext><![CDATA[5&nbsp;&nbsp;&nbsp;]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"f6\">\n" +
"<material>\n" +
"<mattext><![CDATA[6<br/>]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"f7\">\n" +
"<material>\n" +
"<mattext><![CDATA[7&nbsp;&nbsp;&nbsp;]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"f8\">\n" +
"<material>\n" +
"<mattext><![CDATA[8&nbsp;&nbsp;&nbsp;]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"f9\">\n" +
"<material>\n" +
"<mattext><![CDATA[9<br/>]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"f0\">\n" +
"<material>\n" +
"<mattext><![CDATA[0]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"          </render_choice>\n" +
"        </response_lid>\n" +
"\n" +
"        <material>\n" +
"<mattext xml:space=\"preserve\"><![CDATA[</td><td>&nbsp;&nbsp;&nbsp;&nbsp;</td><td style=\"background: rgb(230,230,230)\"><span style=\"font-size: 125%;\">7th digit</span><br/>]]></mattext>\n" +
"</material>\n" +
"        <response_lid ident=\"digit_7\" rcardinality=\"Single\">\n" +
"          <render_choice shuffle=\"No\">\n" +
"            <response_label ident=\"g1\">\n" +
"<material>\n" +
"<mattext><![CDATA[1&nbsp;&nbsp;&nbsp;]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"g2\">\n" +
"<material>\n" +
"<mattext><![CDATA[2&nbsp;&nbsp;&nbsp;]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"g3\">\n" +
"<material>\n" +
"<mattext><![CDATA[3<br/>]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"g4\">\n" +
"<material>\n" +
"<mattext><![CDATA[4&nbsp;&nbsp;&nbsp;]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"g5\">\n" +
"<material>\n" +
"<mattext><![CDATA[5&nbsp;&nbsp;&nbsp;]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"g6\">\n" +
"<material>\n" +
"<mattext><![CDATA[6<br/>]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"g7\">\n" +
"<material>\n" +
"<mattext><![CDATA[7&nbsp;&nbsp;&nbsp;]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"g8\">\n" +
"<material>\n" +
"<mattext><![CDATA[8&nbsp;&nbsp;&nbsp;]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"g9\">\n" +
"<material>\n" +
"<mattext><![CDATA[9<br/>]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"g0\">\n" +
"<material>\n" +
"<mattext><![CDATA[0]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"          </render_choice>\n" +
"        </response_lid>\n" +
"\n" +
"        <material>\n" +
"<mattext xml:space=\"preserve\"><![CDATA[</td><td>&nbsp;</td><td><span style=\"font-size: 125%;\">8th digit</span><br/>]]></mattext>\n" +
"</material>\n" +
"        <response_lid ident=\"digit_8\" rcardinality=\"Single\">\n" +
"          <render_choice shuffle=\"No\">\n" +
"            <response_label ident=\"h1\">\n" +
"<material>\n" +
"<mattext><![CDATA[1&nbsp;&nbsp;&nbsp;]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"h2\">\n" +
"<material>\n" +
"<mattext><![CDATA[2&nbsp;&nbsp;&nbsp;]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"h3\">\n" +
"<material>\n" +
"<mattext><![CDATA[3<br/>]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"h4\">\n" +
"<material>\n" +
"<mattext><![CDATA[4&nbsp;&nbsp;&nbsp;]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"h5\">\n" +
"<material>\n" +
"<mattext><![CDATA[5&nbsp;&nbsp;&nbsp;]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"h6\">\n" +
"<material>\n" +
"<mattext><![CDATA[6<br/>]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"h7\">\n" +
"<material>\n" +
"<mattext><![CDATA[7&nbsp;&nbsp;&nbsp;]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"h8\">\n" +
"<material>\n" +
"<mattext><![CDATA[8&nbsp;&nbsp;&nbsp;]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"h9\">\n" +
"<material>\n" +
"<mattext><![CDATA[9<br/>]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"            <response_label ident=\"h0\">\n" +
"<material>\n" +
"<mattext><![CDATA[0]]></mattext>\n" +
"</material>\n" +
"</response_label>\n" +
"          </render_choice>\n" +
"        </response_lid>\n" +
"\n" +
"\n" +
"\n" +
"        <material>\n" +
"          <mattext charset=\"US-ASCII\" texttype=\"TEXT/PLAIN\" xml:space=\"preserve\"><![CDATA[</td></tr></table><p>&nbsp;</p>]]></mattext>\n" +
"        </material>\n" +
"\n" +
"\n" +
"       </flow_label>\n" +
"\n" +
"\n" +
"      </flow>\n" +
"\n" +
"    </presentation>\n" +
"    <resprocessing scoremodel=\"SumofScores\">\n" +
"      <outcomes>\n" +
"        <decvar defaultval=\"\" varname=\"SID\" vartype=\"string\"/>\n" +
"      </outcomes>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_1\">a0</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">0</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_1\">a1</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">1</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_1\">a2</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">2</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_1\">a3</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">3</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_1\">a4</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">4</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_1\">a5</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">5</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_1\">a6</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">6</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_1\">a7</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">7</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_1\">a8</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">8</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_1\">a9</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">9</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_2\">b0</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">0</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_2\">b1</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">1</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_2\">b2</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">2</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_2\">b3</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">3</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_2\">b4</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">4</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_2\">b5</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">5</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_2\">b6</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">6</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_2\">b7</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">7</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_2\">b8</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">8</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_2\">b9</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">9</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_3\">c0</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">0</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_3\">c1</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">1</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_3\">c2</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">2</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_3\">c3</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">3</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_3\">c4</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">4</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_3\">c5</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">5</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_3\">c6</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">6</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_3\">c7</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">7</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_3\">c8</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">8</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_3\">c9</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">9</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_4\">d0</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">0</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_4\">d1</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">1</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_4\">d2</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">2</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_4\">d3</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">3</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_4\">d4</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">4</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_4\">d5</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">5</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_4\">d6</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">6</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_4\">d7</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">7</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_4\">d8</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">8</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_4\">d9</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">9</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_5\">e0</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">0</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_5\">e1</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">1</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_5\">e2</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">2</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_5\">e3</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">3</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_5\">e4</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">4</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_5\">e5</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">5</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_5\">e6</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">6</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_5\">e7</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">7</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_5\">e8</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">8</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_5\">e9</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">9</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_6\">f0</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">0</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_6\">f1</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">1</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_6\">f2</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">2</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_6\">f3</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">3</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_6\">f4</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">4</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_6\">f5</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">5</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_6\">f6</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">6</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_6\">f7</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">7</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_6\">f8</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">8</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_6\">f9</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">9</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_7\">g0</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">0</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_7\">g1</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">1</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_7\">g2</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">2</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_7\">g3</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">3</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_7\">g4</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">4</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_7\">g5</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">5</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_7\">g6</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">6</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_7\">g7</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">7</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_7\">g8</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">8</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_7\">g9</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">9</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_8\">h0</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">0</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_8\">h1</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">1</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_8\">h2</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">2</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_8\">h3</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">3</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_8\">h4</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">4</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_8\">h5</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">5</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_8\">h6</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">6</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_8\">h7</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">7</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_8\">h8</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">8</setvar>\n" +
"      </respcondition>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"<varequal case=\"Yes\" respident=\"digit_8\">h9</varequal>\n" +
"</conditionvar>\n" +
"<setvar action=\"Add\" varname=\"SID\">9</setvar>\n" +
"      </respcondition>\n" +
"    </resprocessing>\n" +
"    </item>\n" +
"\n" +
"\n" +
"\n" +
"\n" +
"\n";



public static final String itemmcqa = "    <item ident=\"QUESTIONNUMBER\" qyouti:template=\"org.qyouti.templates.PureMCQNoText\" title=\"Question QUESTIONNUMBER\">\n" +
"    <presentation qyouti:columns=\"COLUMNS\">\n" +
"      <material>\n" +
"<mattext charset=\"US-ASCII\" texttype=\"TEXT/PLAIN\" xml:space=\"preserve\"><![CDATA[<div style=\"text-align: right;\">]]></mattext>\n" +
"</material>\n" +
"      <flow>\n" +
"       <flow_label class=\"Row\">\n" +
"        <material>\n" +
"          <mattext charset=\"US-ASCII\" texttype=\"TEXT/PLAIN\" xml:space=\"preserve\"><![CDATA[<span style=\"font-size: 110%;\">QQUESTIONNUMBER&nbsp;&nbsp;</span>]]></mattext>\n" +
"        </material>\n" +
"        <response_lid ident=\"resp_abcd\" rcardinality=\"Single\">\n" +
"          <render_choice shuffle=\"No\">\n";

public static final String itemmcqoption = 
"              <response_label ident=\"OPTIONIDENT\" qyouti:correct=\"TRUEFALSE\">\n" +
"                <material>\n" +
"                  <mattext charset=\"US-ASCII\" texttype=\"TEXT/PLAIN\" xml:space=\"preserve\">OPTIONLETTER</mattext>\n" +
"                </material>\n" +
"              </response_label>\n";

public static final String itemmcqb = 
"          </render_choice>\n" +
"        </response_lid>\n" +
"       </flow_label>\n" +
"      </flow>\n" +
"      <material>\n" +
"<mattext charset=\"US-ASCII\" texttype=\"TEXT/PLAIN\" xml:space=\"preserve\"><![CDATA[</div>]]></mattext>\n" +
"</material>\n" +
"    </presentation>\n" +
"    <resprocessing scoremodel=\"SumofScores\">\n" +
"      <outcomes>\n" +
"        <decvar defaultval=\"0.0\" minvalue=\"0.0\" varname=\"SCORE\" vartype=\"Decimal\"/>\n" +
"      </outcomes>\n" +
"      <respcondition continue=\"Yes\">\n" +
"        <conditionvar>\n" +
"          <varequal case=\"Yes\" respident=\"resp_abcd\">a</varequal>\n" +
"        </conditionvar>\n" +
"        <setvar action=\"Add\" varname=\"SCORE\">1</setvar>\n" +
"      </respcondition>\n" +
"    </resprocessing>\n" +
"    </item>\n\n";

  
  
  /**
   * Creates new form MCQAnonymousNoTextExam
   */
  public MCQAnonymousNoTextExam()
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
    optslabel = new javax.swing.JLabel();
    scrollpane2 = new javax.swing.JScrollPane();
    optslist = new javax.swing.JList<>();
    questionslabel = new javax.swing.JLabel();
    questionsfield = new javax.swing.JTextField();
    candidateslabel = new javax.swing.JLabel();
    candidatesfield = new javax.swing.JTextField();

    setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
    setLayout(new java.awt.BorderLayout());

    scrollpane1.setOpaque(false);

    descriptiontextarea.setEditable(false);
    descriptiontextarea.setColumns(20);
    descriptiontextarea.setLineWrap(true);
    descriptiontextarea.setRows(5);
    descriptiontextarea.setText("This template produces answer sheets for a pure multiple choice exam for which the questions are printed separately. The answer sheets are not assigned to specific candidates so candidates must encode their student number.");
    descriptiontextarea.setWrapStyleWord(true);
    descriptiontextarea.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
    descriptiontextarea.setOpaque(false);
    scrollpane1.setViewportView(descriptiontextarea);

    add(scrollpane1, java.awt.BorderLayout.NORTH);

    mainpanel.setLayout(new java.awt.GridBagLayout());

    optslabel.setText("Number of Options:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
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
    gridBagConstraints.ipadx = 8;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    mainpanel.add(scrollpane2, gridBagConstraints);

    questionslabel.setText("Number of Questions:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
    gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 10);
    mainpanel.add(questionslabel, gridBagConstraints);

    questionsfield.setColumns(5);
    questionsfield.setText("20");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
    mainpanel.add(questionsfield, gridBagConstraints);

    candidateslabel.setText("Number of Candidates:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
    gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 10);
    mainpanel.add(candidateslabel, gridBagConstraints);

    candidatesfield.setColumns(5);
    candidatesfield.setText("5");
    candidatesfield.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        candidatesfieldActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridy = 3;
    gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
    mainpanel.add(candidatesfield, gridBagConstraints);

    add(mainpanel, java.awt.BorderLayout.CENTER);
  }// </editor-fold>//GEN-END:initComponents

  private void candidatesfieldActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_candidatesfieldActionPerformed
  {//GEN-HEADEREND:event_candidatesfieldActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_candidatesfieldActionPerformed


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JTextField candidatesfield;
  private javax.swing.JLabel candidateslabel;
  private javax.swing.JTextArea descriptiontextarea;
  private javax.swing.JPanel mainpanel;
  private javax.swing.JLabel optslabel;
  private javax.swing.JList<String> optslist;
  private javax.swing.JTextField questionsfield;
  private javax.swing.JLabel questionslabel;
  private javax.swing.JScrollPane scrollpane1;
  private javax.swing.JScrollPane scrollpane2;
  // End of variables declaration//GEN-END:variables

  @Override
  public String getDocumentAsString()
  {
    int i, j;
    int nq = Integer.parseInt( questionsfield.getText() );
    int nc = Integer.parseInt( candidatesfield.getText() );
    int no = Integer.parseInt( optslist.getSelectedValue() );
    if ( nq < 0 || nc < 0 )
      throw new IllegalArgumentException( "Invalid number." );
    if ( nq > 1000 || nc > 1000 )
      throw new IllegalArgumentException( "Too many." );
    
    Random r = new Random();
    r.setSeed( System.currentTimeMillis() );
    StringBuilder buffer = new StringBuilder(1000);
    String opt, str;
    buffer.append( texta.replaceFirst( "ASSESSMENTIDENT", Long.toHexString( r.nextLong() ) ) );
    buffer.append( itemvoid );
    buffer.append( itemsid );
    String spacer="&amp;nbsp;&amp;nbsp;&amp;nbsp;&amp;nbsp;&amp;nbsp;";
    String opttext;
    for ( i=0; i<nq; i++ )
    {
      str = itemmcqa.replaceAll( "QUESTIONNUMBER", Integer.toString( i+1 ) );
      str = str.replaceAll( "COLUMNS", (no>=6)?"1":"2" );
      buffer.append( str );
      for ( j=0; j<no; j++ )
      {
        opt = itemmcqoption.replaceFirst( "OPTIONIDENT", Character.toString( (char)('a' + j) ) );
        opt = opt.replaceFirst( "TRUEFALSE", (j==0)?"true":"false" );
        opttext= Character.toString( (char)('A' + j) );
        if ( j != (no-1) )
          opttext = opttext + spacer;
        buffer.append( opt.replaceFirst( "OPTIONLETTER", opttext ) );
      }
      buffer.append( itemmcqb );
    }
    buffer.append( textb );
    
    DecimalFormat df = new DecimalFormat( "000" );
    for ( i=0; i<nc; i++ )
    {
      buffer.append( "<candidate name=\"Candidate\" id=\"" );
      buffer.append( df.format( i+1L ) );
      buffer.append( "\" score=\"0.0\">\n</candidate>" );
    }
    
    buffer.append( textc );
    return buffer.toString();
  }
}
