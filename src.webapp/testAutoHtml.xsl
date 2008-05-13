<?xml version="1.0"?>

<!DOCTYPE xsl:stylesheet [
<!ENTITY copy   "&#169;">
<!ENTITY nbsp   "&#160;">
]>

<xsl:stylesheet
    version="1.0"
    xmlns="http://www.w3.org/1999/xhtml"
    xmlns:xhtml="http://www.w3.org/1999/xhtml"
    xmlns:aostest="http://www.anodyneos.org/aostest"
    xmlns:xalan="http://xml.apache.org/xalan"
    xmlns:autoHtml="org.anodyneos.servlet.xsl.xalan.AutoHtml"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    extension-element-prefixes="autoHtml">

    <xsl:output
            method="xml"
            media-type="application/xhtml+xml"
            doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN"
            doctype-system="http://www.w3.org/2002/08/xhtml/xhtml1-strict.dtd"

            indent="no"
            xalan:indent-amount="2"
            omit-xml-declaration="no"
            encoding="UTF-8"/>

    <xsl:template match="aostest:autoHtml">
        <hr/>
        <autoHtml:autoHtml select="."/>
        <hr/>
        <xsl:copy-of select="autoHtml:autoHtml(.)"/>
        <hr/>
    </xsl:template>

    <!--~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    ~~
    ~~  By default, copy source
    ~~
    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~-->

    <xsl:template match="/xhtml:html | /html">
        <html   xmlns="http://www.w3.org/1999/xhtml"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xsi:schemaLocation="http://www.w3.org/1999/xhtml http://www.w3.org/2002/08/xhtml/xhtml1-strict.xsd">
            <xsl:apply-templates select="@* | node()"/>
        </html>
    </xsl:template>

    <xsl:template match="*">
        <!--
            If we were to use xsl:copy on elements, all namespaces defined in
            the source would appear in the result (we don't want that.)
        -->
        <xsl:choose>
            <xsl:when test="namespace-uri() != ''">
                <!--
                    NOTE: xsl:element is preferred in order to cut down on
                    source document namespace declarations showing up in the
                    output, BUT, XSLTC cannot handle it well as it defines a
                    brand new prefix for every element.  See Jira bug ZP-3.

                    <xsl:element name="{local-name()}" namespace="{namespace-uri()}">
                        <xsl:apply-templates select="@* | node()"/>
                    </xsl:element>
                -->
                <xsl:copy>
                    <xsl:apply-templates select="@* | node()"/>
                </xsl:copy>
            </xsl:when>
            <xsl:otherwise>
                <xsl:element name="{local-name()}" namespace="http://www.w3.org/1999/xhtml">
                    <xsl:apply-templates select="@* | node()"/>
                </xsl:element>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- Copy all attributes and text nodes -->
    <xsl:template match="@* | text()">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>

