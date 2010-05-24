<?xml version="1.0" encoding="UTF-8"?>


<xsl:stylesheet 
      xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
      xmlns:svg="http://www.w3.org/2000/svg"
      version="1.0">
  <xsl:output method="text"/>


  <xsl:template match="/">
    <xsl:value-of select="count( svg:svg/svg:g[@class = 'page'] )"/>
  </xsl:template>


</xsl:stylesheet>
