<?xml version="1.0" encoding="UTF-8"?>


<xsl:stylesheet 
      xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
      xmlns="http://www.w3.org/2000/svg"
      xmlns:xlink="http://www.w3.org/1999/xlink"
      xmlns:svg="http://www.w3.org/2000/svg"
      xmlns:rendersvg="xalan://org.qyouti.qrcode.QRCodec"
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
    <svg xmlns="http://www.w3.org/2000/svg" version="1.1" width="8.267in" height="11.693in" viewBox="0 0 826.7 1169.3">
      <xsl:value-of select="$newline"/>
      <desc>Multiple Choice Exam Paper</desc>
      <xsl:value-of select="$newline"/>



      <rect x="0" y="0" fill="white" stroke="none" width="826.7" height="1169.3"/>
      <xsl:value-of select="$newline"/>
      <rect x="0" y="0" fill="rgb( 210, 210, 240 )" stroke="none" width="150" height="1169.3"/>
      <xsl:value-of select="$newline"/>
      <line x1="85" y1="-10" x2="-10" y2="85" stroke="black" stroke-dasharray="10,10" stroke-width="2"/>
      <xsl:value-of select="$newline"/>

      <text x="30" y="30" transform="rotate( -45 30 30 )"
            text-anchor="middle" font-family="Sans"
            font-size="12" fill="black">Staple</text>

      <xsl:choose>

        <xsl:when test="$page =  0">



          <text
             fill="black"
             font-size="32"
             font-family="Sans"
             text-anchor="middle"
             y="100"
             x="413"
             id="candidate_name"
             style="-inkscape-font-specification:Sans;font-family:Sans;font-weight:normal;font-style:normal;font-stretch:normal;font-variant:normal;font-size:40.00000019px;text-anchor:middle;text-align:center;writing-mode:lr;line-height:125%"
             ><xsl:value-of select="$candidate-name"/></text>
          <text
             fill="black"
             font-size="32"
             font-family="Sans"
             text-anchor="middle"
             y="180"
             x="413"
             id="candidate_number"
             ><xsl:value-of select="$candidate-number"/></text>


          <g transform="translate( 225, 250 )">
            <xsl:apply-templates select="/svg:svg/svg:g[@class = 'intro']"/>
          </g>


          <g transform="translate( 50, 400 )">
            <text
             xml:space="preserve"
             style="font-size:22.22222328px;font-style:normal;font-variant:normal;font-weight:normal;font-stretch:normal;fill:#000000;fill-opacity:1;stroke:none;stroke-width:1px;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1;font-family:Sans;-inkscape-font-specification:Sans"
             x="172.16014"
             y="900"
             id="text3073"><tspan
               id="tspan3075"
               x="172.16014"
               y="499.33905"><tspan
                 x="172.16014"
                 y="499.33905"
                 id="tspan3077">Do not fold or crease this paper.</tspan><tspan
                 dx="0"
                 x="529.84247"
                 y="499.33905"
                 id="tspan3079" /></tspan><tspan
               id="tspan3081"
               x="172.16014"
               y="527.11676"><tspan
                 x="172.16014"
                 y="527.11676"
                 id="tspan3083">Do not make marks in the left panel.</tspan><tspan
                 dx="0"
                 x="580.10291"
                 y="527.11676"
                 id="tspan3085" /></tspan><tspan
               id="tspan3087"
               x="172.16014"
               y="554.89453"><tspan
                 x="172.16014"
                 y="554.89453"
                 id="tspan3089">Use a dark pencil to indicate your responses.</tspan><tspan
                 dx="0"
                 x="673.63586"
                 y="554.89453"
                 id="tspan3091" /></tspan><tspan
               id="tspan3093"
               x="172.16014"
               y="582.6723"><tspan
                 x="172.16014"
                 y="582.6723"
                 id="tspan3095">Put a cross in each pink box you want to select.</tspan><tspan
                 dx="0"
                 x="702.15149"
                 y="582.6723"
                 id="tspan3097" /></tspan><tspan
               id="tspan3099"
               x="172.16014"
               y="610.45007"><tspan
                 x="172.16014"
                 y="610.45007"
                 id="tspan3101">Leave other boxes blank.</tspan><tspan
                 dx="0"
                 x="451.28348"
                 y="610.45007"
                 id="tspan3103" /></tspan><tspan
               id="tspan3105"
               x="172.16014"
               y="638.22784"><tspan
                 x="172.16014"
                 y="638.22784"
                 id="tspan3107">Erase mistakes fully with a soft eraser.</tspan></tspan></text>
  </g>

          <text
             font-size="32"
             y="1125.1018"
             x="781.60413"
             id="text3111"
             style="font-size:13.33333302px;font-style:italic;font-variant:normal;font-weight:normal;font-stretch:normal;text-align:end;line-height:125%;writing-mode:lr-tb;text-anchor:end;fill:#000000;font-family:Sans;-inkscape-font-specification:Sans Italic"
             >This exam paper was created using Qyouti</text>
 
          <g transform="translate( 50, 1050 )">
            <xsl:variable name="code"
              select="concat( $candidate-name, '/', $candidate-number, '/0/0' )"/>
            <xsl:message>QRCode = <xsl:value-of select="$code"/></xsl:message>
            <xsl:copy-of select="rendersvg:encodeSVG( $code, 60 )"/>
          </g>

        </xsl:when>
        <xsl:otherwise>
          <xsl:variable name="qcount" select="count( /svg:svg/svg:g[position() = $page]/svg:g )"/>
          <xsl:message>Question on this page: <xsl:value-of select="$qcount"/></xsl:message>

          <g transform="translate( 50, 1050 )">
            <xsl:variable name="code"
              select="concat( $candidate-name, '/', $candidate-number, '/', $page, '/', $qcount )"/>
            <xsl:message>QRCode = <xsl:value-of select="$code"/></xsl:message>
            <xsl:copy-of select="rendersvg:encodeSVG( $code, 60 )"/>
          </g>

          <xsl:value-of select="$newline"/>
          <xsl:comment> Above page content. </xsl:comment>
          <xsl:value-of select="$newline"/>
          <xsl:apply-templates select="/svg:svg/svg:g[position() = $page]"/>
          <xsl:value-of select="$newline"/>
          <xsl:comment> Below page content. </xsl:comment>
          <xsl:value-of select="$newline"/>


          <text x="750" y="1120" text-anchor="end" font-family="Sans" font-size="14" fill="black">
            <xsl:value-of select="$candidate-name"/>
            <xsl:text>              </xsl:text>
            <xsl:value-of select="$candidate-number"/>
            <xsl:text>         Page </xsl:text>
            <xsl:value-of select="$page"/>
            <!--
            <xsl:text> of </xsl:text>
            <xsl:value-of select="count( /svg:svg/svg:g[$page] )-1"/>
            -->
          </text>

        </xsl:otherwise>
      </xsl:choose>



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
