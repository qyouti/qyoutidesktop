<?xml version="1.0" encoding="UTF-8"?>

<!--
    Document   : convertmaterial.xsl
    Created on : 24 July 2009, 19:17
    Author     : jon
    Description:
        Converts all the material elements in a QTI file to bitmap images.
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:render="xalan://org.qyouti.print.MattextToSvg"
                xmlns:qti="http://www.imsglobal.org/xsd/ims_qtiasiv1p2"
                xmlns:qyouti="http://www.qyouti.org/qtiext"
                version="1.0">
  <xsl:output method="xml"/>
  <xsl:param name="item-width"/>
  <xsl:param name="response-width"/>
  <xsl:param name="font-family"/>


  <xsl:template match="/">
    <xsl:text>
</xsl:text>
    <xsl:apply-templates select="examination/qti:questestinterop"/>
    <xsl:comment>Working?</xsl:comment>
  </xsl:template>

  <xsl:template match="qti:mattext[@texttype = 'text/html']">
    <xsl:variable name="width">
      <xsl:choose>
        <xsl:when test="count(ancestor::qti:render_choice) > 0">
          <xsl:value-of select="$response-width"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$item-width"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="html">
      <xsl:apply-templates select="*|text()" mode="literal"/>
    </xsl:variable>

    <xsl:copy-of select="render:htmlToSvg($html, $width, $font-family)"/>
  </xsl:template>


  <xsl:template match="qti:mattext">
    <xsl:variable name="width">
      <xsl:choose>
        <xsl:when test="count(ancestor::qti:render_choice) > 0">
          <xsl:value-of select="$response-width"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$item-width"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:copy-of select="render:textToSvg(., $width, $font-family)"/>
  </xsl:template>


  <xsl:template match="qti:flow">
    <!-- Remove flow elements but place their child elements higher in the tree. -->
    <xsl:apply-templates select="*|text()"/>
  </xsl:template>

  <xsl:template match="qti:flow_label">
    <!-- Remove flow_label elements but place their child elements higher in the tree. -->
    <xsl:apply-templates select="*|text()"/>
  </xsl:template>

  <xsl:template match="qti:matbreak">
    <!-- Omit the matbreak element entirely. -->
  </xsl:template>

  <xsl:template match="qti:itemfeedback">
    <!-- Omit the feedback section entirely. -->
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


  <xsl:template match="*" mode="literal">
    <xsl:text>&lt;</xsl:text>
    <xsl:value-of select="name()"/>

    <xsl:for-each select="@*">
      <xsl:text> </xsl:text>
      <xsl:value-of select="name()"/>
      <xsl:text>=&quot;</xsl:text>
      <xsl:value-of select="."/>
      <xsl:text>&quot;</xsl:text>
    </xsl:for-each>

    <xsl:text>&gt;</xsl:text>

    <xsl:apply-templates select="*|text()" mode="literal"/>

    <xsl:text>&lt;/</xsl:text>
    <xsl:value-of select="name()"/>
    <xsl:text>&gt;</xsl:text>

  </xsl:template>



</xsl:stylesheet>

