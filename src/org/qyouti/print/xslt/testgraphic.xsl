<?xml version="1.0" encoding="UTF-8"?>


<xsl:stylesheet 
      xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
      xmlns="http://www.w3.org/2000/svg"
      xmlns:xlink="http://www.w3.org/1999/xlink"
      xmlns:svg="http://www.w3.org/2000/svg"
      xmlns:rendersvg="xalan://org.qyouti.print.qrcode.EncodeSVGImage"
      version="1.0">
  <xsl:output method="xml"/>
  <xsl:param name="candidate-name"/>
  <xsl:param name="candidate-number"/>
  <xsl:param name="page"/>



  <xsl:variable name="newline">
    <xsl:text>
</xsl:text>
  </xsl:variable>




  <xsl:template match="/">
    <xsl:value-of select="$newline"/>
    <svg xmlns="http://www.w3.org/2000/svg" version="1.1" width="3in" height="3in" viewBox="0 0 300 300">
      <xsl:value-of select="$newline"/>
      <desc>Optical Mark Reader Data Sheet</desc>
      <xsl:value-of select="$newline"/>
      <rect x="0" y="0" fill="white" stroke="none" width="826.7" height="1169.3"/>
      <xsl:value-of select="$newline"/>
      <g transform="translate( 100, 100 )">
        <xsl:copy-of select="rendersvg:svgTestCode( 60 )"/>
      </g>
    </svg>
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
