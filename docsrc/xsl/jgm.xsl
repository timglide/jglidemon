<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  version="1.0">
  
  <!-- Try to make HTML look nicer -->
  <xsl:param name="make.valid.html" select="1"/>
  <xsl:param name="html.cleanup" select="1"/>
  
  <xsl:param name="html.stylesheet" select="'css/jgm.css'"/>
  
  <!-- For auto-numbering -->
  <xsl:param name="section.autolabel" select="1"/>
  <xsl:param name="section.label.includes.component.label" select="1"/>
  
  <!-- For nav graphics -->
  <xsl:param name="navig.graphics" select="0"/>
  
  <xsl:param name="suppress.navigation" select="0"/> 
   
  <!-- For admon graphics -->
  <xsl:param name="admon.graphics" select="1"/>
  <xsl:param name="admon.graphics.path">images/</xsl:param>
  
  <xsl:param name="use.id.as.filename" select="1"/>
  
  <xsl:param name="generate.legalnotice.link" select="0"/>

  <xsl:param name="toc.section.depth" select="4"/>
  
  <!--xsl:template name="user.header.navigation">
    <p>Documentation by ACME Data Inc. No Coyote's allowed.</p>
    <hr />
  </xsl:template>
  <xsl:template name="user.footer.navigation">
    <hr />
    <p>The Road Runner was here - All Wrongs Reserved.</p>
  </xsl:template-->
</xsl:stylesheet>
