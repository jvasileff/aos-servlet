<?xml version="1.0"?>

<!DOCTYPE xsl:stylesheet [
<!ENTITY copy   "&#169;">
<!ENTITY nbsp   "&#160;">
]>

<xsl:stylesheet
        version="1.0"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:xalan="http://xml.apache.org/xslt"
        xmlns:java="http://xml.apache.org/xslt/java"
        extension-element-prefixes="xsl xalan"
        exclude-result-prefixes="xsl xalan"
        >

    <xsl:output method="html"
        media-type="text/html"
        indent="yes"
        xalan:indent-amount="4"
        doctype-public="-//W3C//DTD HTML 4.01 Frameset//EN"
        encoding="ISO-8859-1"/>

    <xsl:key name="param" match="scope/param" use="concat(../@name, '::', @name)"/>

    <xsl:template match="/">
        <html>
            <body>
                <p>
                    <xsl:variable name="fromName" select="string(key('param', 'reqParam::fromName'))"/>
                    <xsl:choose>
                        <xsl:when test="$fromName">
                            Dear <xsl:value-of select="$fromName"/>,
                        </xsl:when>
                        <xsl:otherwise>
                            Dear Sir,
                        </xsl:otherwise>
                    </xsl:choose>
                </p>
                <p>
                    This is a sample html file attachment.
                </p>
                <hr/>
                <h1>Echo utility - shows all submitted values</h1>
                <table border="1">
                    <tr>
                        <td>Param name</td>
                        <td>Param value</td>
                    </tr>
                    <xsl:for-each select="/params/*/*">
                        <tr>
                            <td><xsl:value-of select="concat(../@name, '::', @name)"/></td>
                            <td><xsl:value-of select="."/></td>
                        </tr>
                    </xsl:for-each>
                </table>
            </body>
        </html>
    </xsl:template>

</xsl:stylesheet>
