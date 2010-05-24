<?xml version="1.0" encoding="UTF-8"?>


<xsl:stylesheet 
      xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
      xmlns="http://www.w3.org/2000/svg"
      xmlns:xlink="http://www.w3.org/1999/xlink"
      xmlns:svg="http://www.w3.org/2000/svg"
      xmlns:rendersvg="xalan://org.qyouti.print.qrcode.EncodeSVGImage"
      version="1.0">
  <xsl:output method="xml"/>



  <xsl:variable name="newline">
    <xsl:text>
</xsl:text>
  </xsl:variable>




  <xsl:template match="/">
    <xsl:value-of select="$newline"/>
    <svg xmlns="http://www.w3.org/2000/svg" version="1.1" width="8.267in" height="11.693in" viewBox="0 0 826.7 1169.3">
      <desc>Optical Mark Reader Data Sheet</desc>
      <rect x="0" y="0" fill="white" stroke="none" width="826.7" height="1169.3"/>

      <xsl:call-template name="dopage">
        <xsl:with-param name="items" select="svg:svg/svg:g[@class = 'item']"/>
        <xsl:with-param name="n" select="1"/>
        <xsl:with-param name="last" select="count( svg:svg/svg:g )"/>
      </xsl:call-template>

      <xsl:for-each select="svg:svg/svg:g[@class != 'item']">
        <xsl:copy-of select="."/>
      </xsl:for-each>

    </svg>
  </xsl:template>




  <xsl:template name="dopage">
    <xsl:param name="items"/>
    <xsl:param name="n"/>
    <xsl:param name="last"/>
    <xsl:message>===================================</xsl:message>
    <xsl:message>Calculating last item on this page.</xsl:message>
    <xsl:message>===================================</xsl:message>
    <xsl:variable name="lastonpage">
      <xsl:call-template name="lastonpage">
        <xsl:with-param name="items" select="$items"/>
        <xsl:with-param name="n" select="$n"/>
        <xsl:with-param name="last" select="$last"/>
        <xsl:with-param name="voffset" select="0"/>
      </xsl:call-template>
    </xsl:variable>
    <!--
    <xsl:variable name="qrcode">
      <xsl:call-template name="qrcode">
        <xsl:with-param name="items" select="$items"/>
        <xsl:with-param name="n" select="$n"/>
        <xsl:with-param name="last" select="$lastonpage"/>
        <xsl:with-param name="voffset" select="0"/>
      </xsl:call-template>
    </xsl:variable>
    -->
    <xsl:message>dopage, lastonpage = <xsl:value-of select="$lastonpage"/></xsl:message>
    <!--
    <xsl:message>dopage, qrcode = <xsl:value-of select="$qrcode"/></xsl:message>
    -->


    <xsl:value-of select="$newline"/>
    <xsl:value-of select="$newline"/>
    <xsl:value-of select="$newline"/>
    <xsl:value-of select="$newline"/>
    <g transform="translate( 50 75 )" class="page">
      <xsl:value-of select="$newline"/>
      <!--
      <g transform="translate( 0, -100 )">
        <xsl:value-of select="$newline"/>
          <xsl:variable name="code"
            select="$qrcode"/>
          <xsl:message>QRCode = <xsl:value-of select="$code"/></xsl:message>
          <xsl:copy-of select="rendersvg:svgQRCode( $code, 60 )"/>
        <xsl:value-of select="$newline"/>
      </g>
      -->
      <xsl:value-of select="$newline"/>
      <xsl:value-of select="$newline"/>


      <xsl:call-template name="doitem">
        <xsl:with-param name="items" select="$items"/>
        <xsl:with-param name="n" select="$n"/>
        <xsl:with-param name="last" select="$lastonpage"/>
        <xsl:with-param name="voffset" select="0"/>
      </xsl:call-template>
    </g>
    <xsl:value-of select="$newline"/>
    <xsl:value-of select="$newline"/>

    <xsl:if test="$lastonpage &lt; $last">
      <xsl:call-template name="dopage">
        <xsl:with-param name="items" select="$items"/>
        <xsl:with-param name="n" select="$lastonpage + 1"/>
        <xsl:with-param name="last" select="$last"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>


  <xsl:template name="lastonpage">
    <xsl:param name="items"/>
    <xsl:param name="n"/>
    <xsl:param name="last"/>
    <xsl:param name="voffset"/>
    
    <xsl:message></xsl:message>

<!-- -->
    <xsl:choose>
      <xsl:when test="$n = $last and $voffset = 0">
        <xsl:value-of select="$n"/>
        <xsl:message>Returning last item.<xsl:value-of select="$n"/></xsl:message>
      </xsl:when>
      <!--
      <xsl:when test="$n = $last">
        <xsl:value-of select="$n - 1"/>
        <xsl:message>Returning previous item.<xsl:value-of select="$n - 1"/></xsl:message>
      </xsl:when>
      -->
      <xsl:otherwise>
      <!-- -->
        <xsl:variable name="h" select="substring-before( $items[$n]/@height, 'in' )"/>
        <xsl:variable name="total" select="$voffset + ($h * 100)"/>
        <xsl:message>Total = <xsl:value-of select="$total"/></xsl:message>
        <xsl:choose>
          <xsl:when test="$total &gt; 1050">
            <xsl:choose>
              <xsl:when test="$voffset = 0">
                <xsl:value-of select="$n"/>
                <xsl:message>Returning <xsl:value-of select="$n"/></xsl:message>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="$n - 1"/>
                <xsl:message>Returning <xsl:value-of select="$n - 1"/></xsl:message>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:when test="$n = $last">
            <xsl:value-of select="$n"/>
            <xsl:message>Returning <xsl:value-of select="$n"/></xsl:message>
          </xsl:when>
          <xsl:otherwise>
            <xsl:message>Iterate...</xsl:message>
            <xsl:call-template name="lastonpage">
              <xsl:with-param name="items" select="$items"/>
              <xsl:with-param name="n" select="$n + 1"/>
              <xsl:with-param name="last" select="$last"/>
              <xsl:with-param name="voffset" select="$total"/>
            </xsl:call-template>            
          </xsl:otherwise>
        </xsl:choose>
        <!-- -->
      </xsl:otherwise>
    </xsl:choose>
    <!-- -->
  </xsl:template>


  <xsl:template name="doitem">
    <xsl:param name="items"/>
    <xsl:param name="n"/>
    <xsl:param name="last"/>
    <xsl:param name="voffset"/>
    <xsl:if test="$n &lt;= $last">
      <xsl:value-of select="$newline"/>
      <xsl:value-of select="$newline"/>
      <g>
        <xsl:attribute name="transform">
          <xsl:value-of select="concat( 'translate( 0 ', $voffset, ')' )"/>
        </xsl:attribute>
        <xsl:apply-templates select="$items[$n]/*"/>
      </g>
      <xsl:call-template name="doitem">
        <xsl:with-param name="items" select="$items"/>
        <xsl:with-param name="n" select="$n + 1"/>
        <xsl:with-param name="last" select="$last"/>
        <xsl:with-param name="voffset" select="$voffset + 100*substring-before( $items[$n]/@height, 'in' )"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>


<!--
  <xsl:template name="qrcode">
    <xsl:param name="items"/>
    <xsl:param name="n"/>
    <xsl:param name="last"/>
    <xsl:param name="voffset"/>
    <xsl:if test="$n &lt;= $last">
      <xsl:value-of select="concat( floor($voffset) + 100, ' ' )"/>
      <xsl:call-template name="qrcode">
        <xsl:with-param name="items" select="$items"/>
        <xsl:with-param name="n" select="$n + 1"/>
        <xsl:with-param name="last" select="$last"/>
        <xsl:with-param name="voffset" select="$voffset + 100*substring-before( $items[$n]/@height, 'in' )"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>
-->


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
