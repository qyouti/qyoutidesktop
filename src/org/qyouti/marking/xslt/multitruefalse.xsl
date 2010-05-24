<?xml version="1.0" encoding="UTF-8"?>

<!--
    Document   : multitruefalse.xsl
    Created on : 23 September 2009, 09:01
    Author     : jon
    Description:
        Purpose of transformation follows.
-->

<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:qti="http://www.imsglobal.org/xsd/ims_qtiasiv1p2"
  xmlns:qyouti="http://www.qyouti.org/qtiext"
  version="1.0">
  <xsl:output method="xml" indent="yes"/>
  <xsl:param name="markforcorrecttrue" select="1"/>
  <xsl:param name="markforcorrectfalse" select="1"/>


  <xsl:variable name="newline">
    <xsl:text>
</xsl:text>
  </xsl:variable>



  <xsl:template match="qti:resprocessing">
    <xsl:variable name="item" select="ancestor::qti:item"/>
    <xsl:variable name="rlid" select="$item//qti:response_lid"/>
    <xsl:variable name="opts" select="count( $rlid//qti:response_label )"/>

    <xsl:element name="{name()}">
      <xsl:value-of select="$newline"/>
      <xsl:value-of select="$newline"/>
      <outcomes>
        <xsl:value-of select="$newline"/>
        <decvar defaultval="0.0" minvalue="0.0" varname="SCORE" vartype="Decimal"/>
        <xsl:value-of select="$newline"/>
      </outcomes>
      <xsl:value-of select="$newline"/>

      <xsl:for-each select="$rlid//qti:response_label">
        <xsl:choose>
          <xsl:when test="@qyouti:correct = 'true'">
            <xsl:value-of select="$newline"/>
            <respcondition continue="Yes">
              <xsl:value-of select="$newline"/>
              <conditionvar>
                <xsl:value-of select="$newline"/>
                <varequal case="Yes" respident="RESP_MC"><xsl:value-of select="@ident"/></varequal>
                <xsl:value-of select="$newline"/>
              </conditionvar>
              <xsl:value-of select="$newline"/>
              <setvar action="Add" varname="SCORE"><xsl:value-of select="$markforcorrecttrue"/></setvar>
              <xsl:value-of select="$newline"/>
            </respcondition>
            <xsl:value-of select="$newline"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$newline"/>
            <respcondition continue="Yes">
              <xsl:value-of select="$newline"/>
              <conditionvar>
                <xsl:value-of select="$newline"/>
                <not>
                  <varequal case="Yes" respident="RESP_MC"><xsl:value-of select="@ident"/></varequal>
                </not>
                <xsl:value-of select="$newline"/>
              </conditionvar>
              <xsl:value-of select="$newline"/>
              <setvar action="Add" varname="SCORE"><xsl:value-of select="$markforcorrecttrue"/></setvar>
              <xsl:value-of select="$newline"/>
            </respcondition>
            <xsl:value-of select="$newline"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>

      <xsl:value-of select="$newline"/>
      <xsl:value-of select="$newline"/>
    </xsl:element>
  </xsl:template>


  <xsl:template match="*">
    <xsl:element name="{name()}">
      <xsl:for-each select="@*">
        <xsl:attribute name="{name()}">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:for-each>
      <xsl:apply-templates select="*|text()"/>
    </xsl:element>
  </xsl:template>


  <xsl:template match="text()">
    <xsl:value-of select="."/>
  </xsl:template>



</xsl:stylesheet>
