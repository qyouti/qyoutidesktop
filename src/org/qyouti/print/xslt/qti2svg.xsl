<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
      xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
      xmlns="http://www.w3.org/2000/svg"
      xmlns:xlink="http://www.w3.org/1999/xlink"
      xmlns:rendersvg="xalan://org.qyouti.qrcode.QRCodec"
      xmlns:code="xalan://org.qtirender.ItemCode"
      xmlns:svg="http://www.w3.org/2000/svg"
      xmlns:qyouti="http://www.qyouti.org/qtiext"
      version="1.0">
  <xsl:output method="xml" indent="yes"/>

  <xsl:variable name="title-height"     select="40"/>
  <xsl:variable name="response-spacing" select="20"/>
  <xsl:variable name="margin-bottom"    select="50"/>




  <xsl:variable name="newline">
    <xsl:text>
</xsl:text>
  </xsl:variable>




  <xsl:template match="/">
    <xsl:value-of select="$newline"/>
    <svg version="1.1" width="8.267in" height="55in" viewBox="0 0 826.7 5500">
      <desc>Optical Mark Reader Data Sheet</desc>
      <rect x="0" y="0" fill="white" stroke="none" width="826.7" height="5500">
      </rect>
      <xsl:for-each select=".//item">
        <!-- -->
        <xsl:variable name="qid">
          <xsl:value-of select="@ident"/>
        </xsl:variable>
        <!-- -->

        <!-- <xsl:variable name="code" select="code:itemCode( . )"/> -->

        <xsl:variable name="itemheight">
          <xsl:call-template name="calcitemheight">
            <xsl:with-param name="item" select="."/>
          </xsl:call-template>
        </xsl:variable>
        <xsl:message>Item height <xsl:value-of select="$itemheight"/></xsl:message>

        <xsl:value-of select="$newline"/>
        <xsl:value-of select="$newline"/>
        <g width="7in" class="item">
          <xsl:attribute name="transform">
            <xsl:value-of select="concat( 'translate( 50 ', position()*400, ' )' )"/>
          </xsl:attribute>
          <xsl:attribute name="height">
            <xsl:value-of select="concat($itemheight div 100, 'in')"/>
          </xsl:attribute>
          <rect x="100" y="0" fill="white" stroke="none" width="700">
            <xsl:attribute name="height">
              <xsl:value-of select="$itemheight"/>
            </xsl:attribute>
          </rect>
          <text x="110" y="15" fill="black" font-size="16" font-family="Monospace" font-weight="bold" text-anchor="start">
          Question <xsl:value-of select="position()"/>
          </text>
          <xsl:for-each select="./presentation/material/matimage[1]">
            <!-- 
            <xsl:variable name="qid">
              <xsl:value-of select="../../response_lid/@ident"/>
            </xsl:variable>
             -->
            <xsl:variable name="qcoords">
              <xsl:call-template name="response-coords">
                <xsl:with-param name="item" select="ancestor::item"/>
                <xsl:with-param name="nresp" select="1"/>
                <xsl:with-param name="last" select="count( ancestor::item/presentation/response_lid/render_choice/response_label/material/matimage )"/>
                <xsl:with-param name="letter" select="'a'"/>
                <xsl:with-param name="yoffset" select="$title-height + $response-spacing + @height"/>
              </xsl:call-template>
            </xsl:variable>
            <xsl:message>qid: <xsl:value-of select="$qid"/></xsl:message>
            <xsl:message>item height: <xsl:value-of select="$itemheight"/></xsl:message>
            <xsl:message>qcoords: <xsl:value-of select="$qcoords"/></xsl:message>
            <g>
              <xsl:attribute name="transform">
                <xsl:value-of select="concat( 'translate( 110 ', $title-height, ' )' )"/>
              </xsl:attribute>
              <xsl:copy-of select="svg:svg"/>
            </g>
            <g>
              <xsl:call-template name="response">
                <xsl:with-param name="item" select="ancestor::item"/>
                <xsl:with-param name="nresp" select="1"/>
                <xsl:with-param name="last" select="count( ancestor::item/presentation/response_lid/render_choice/response_label/material/matimage )"/>
                <xsl:with-param name="letter" select="'a'"/>
                <xsl:with-param name="yoffset" select="$title-height + $response-spacing + @height"/>
              </xsl:call-template>
            </g>
            <xsl:copy-of select="rendersvg:svgQuestionQRCode( $qid, $itemheight, $qcoords, 60 )"/>
          </xsl:for-each>
          <xsl:value-of select="$newline"/>
          <xsl:value-of select="$newline"/>
        </g>
        <xsl:value-of select="$newline"/>
        <xsl:value-of select="$newline"/>
      </xsl:for-each>

      <xsl:for-each select=".//assessment/presentation_material//matimage">
        <g class="intro">
          <xsl:copy-of select="svg:svg"/>
        </g>
      </xsl:for-each>

    </svg>
  </xsl:template>




  <xsl:template name="response">
    <xsl:param name="item"/>
    <xsl:param name="nresp"/>
    <xsl:param name="last"/>
    <xsl:param name="letter"/>
    <xsl:param name="yoffset"/>
    <xsl:variable name="current"
        select="$item/presentation/response_lid/render_choice/response_label[$nresp]/material/matimage"/>


    <g>
      <xsl:attribute name="transform">
        <xsl:value-of select="concat( 'translate( 0 ', $yoffset, ' )' )"/>
      </xsl:attribute>
      <rect x="110" y="0" fill="rgb( 255,140,255 )" stroke="none" width="24" height="24"/>
      <rect x="114" y="4" fill="rgb( 255,230,255 )" stroke="none" width="16" height="16"/>

      <text x="150" y="14" fill="black" font-size="12" font-family="Serif" font-weight="bold" text-anchor="start">
        <xsl:value-of select="concat( $letter, '.' )"/>
      </text>
      <!--
      <image x="170" y="0" >
        <xsl:attribute name="width">
          <xsl:value-of select="100.0 * $current/@width div $dpi"/>
        </xsl:attribute>
        <xsl:attribute name="height">
          <xsl:value-of select="100.0 * $current/@height div $dpi"/>
        </xsl:attribute>
        <xsl:attribute name="xlink:href">
          <xsl:value-of select="$current/@uri"/>
        </xsl:attribute>
      </image>
      -->
      <g>
        <xsl:attribute name="transform">
          <xsl:value-of select="'translate( 170 0 )'"/>
        </xsl:attribute>
        <xsl:copy-of select="$current/svg:svg"/>
      </g>
    </g>

    <xsl:if test="$nresp != $last">
        <xsl:call-template name="response">
          <xsl:with-param name="item" select="$item"/>
          <xsl:with-param name="nresp" select="$nresp + 1"/>
          <xsl:with-param name="last" select="$last"/>
          <xsl:with-param name="letter">
            <xsl:call-template name="increment-letter">
              <xsl:with-param name="l" select="$letter"/>
            </xsl:call-template>
          </xsl:with-param>
          <xsl:with-param name="yoffset" select="$yoffset + $response-spacing + $current/@height"/>
        </xsl:call-template>
    </xsl:if>

  </xsl:template>






  <xsl:template name="response-coords">
    <xsl:param name="item"/>
    <xsl:param name="nresp"/>
    <xsl:param name="last"/>
    <xsl:param name="letter"/>
    <xsl:param name="yoffset"/>
    <xsl:variable name="response_label"
        select="$item/presentation/response_lid/render_choice/response_label[$nresp]"/>
    <xsl:variable name="current"
        select="$response_label/material/matimage"/>

    <!-- <xsl:value-of select="concat( $response_label/@ident, ' ', floor($yoffset+4), ' ' )"/> -->
    <xsl:value-of select="concat( floor($yoffset+4), ' ' )"/>

    <xsl:if test="$nresp != $last">
        <xsl:call-template name="response-coords">
          <xsl:with-param name="item" select="$item"/>
          <xsl:with-param name="nresp" select="$nresp + 1"/>
          <xsl:with-param name="last" select="$last"/>
          <xsl:with-param name="letter">
            <xsl:call-template name="increment-letter">
              <xsl:with-param name="l" select="$letter"/>
            </xsl:call-template>
          </xsl:with-param>
          <xsl:with-param name="yoffset" select="$yoffset + $response-spacing + $current/@height"/>
        </xsl:call-template>
    </xsl:if>

  </xsl:template>




  <xsl:template name="calcitemheight">
   <xsl:param name="item"/>
   <xsl:variable name="stem-height">
     <xsl:value-of select="$item/presentation/material/matimage/@height"/>
   </xsl:variable>
   <xsl:variable name="resp-height">
     <xsl:call-template name="calcimageheight">
       <xsl:with-param name="list" select="$item/presentation/response_lid//matimage"/>
       <xsl:with-param name="i" select="1"/>
       <xsl:with-param name="runningtotal" select="0"/>
     </xsl:call-template>
   </xsl:variable>
   <xsl:value-of select="$title-height + $stem-height + $resp-height + $margin-bottom"/>
  </xsl:template>

  <xsl:template name="calcimageheight">
   <xsl:param name="list"/>
   <xsl:param name="i"/>
   <xsl:param name="runningtotal"/>
   <xsl:variable name="last" select="count( $list )"/>
   <xsl:choose>
     <xsl:when test="$i &gt; $last">
       <xsl:value-of select="$runningtotal"/>
     </xsl:when>
     <xsl:otherwise>
       <xsl:call-template name="calcimageheight">
         <xsl:with-param name="list" select="$list"/>
         <xsl:with-param name="i" select="$i + 1"/>
         <xsl:with-param name="runningtotal" select="$runningtotal + $response-spacing + $list[$i]/@height"/>
       </xsl:call-template>
     </xsl:otherwise>
   </xsl:choose>
  </xsl:template>




  <xsl:template name="increment-letter">
   <xsl:param name="l"/>
   <xsl:value-of select="translate( $l, 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ', 'bcdefghijklmnopqrstuvwxyz#BCDEFGHIJKLMNOPQRSTUVWXYZ#' )"/>
  </xsl:template>



</xsl:stylesheet>
