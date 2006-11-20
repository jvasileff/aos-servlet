<?xml version="1.0"?>

<!DOCTYPE xsl:stylesheet [
<!ENTITY copy   "&#169;">
<!ENTITY nbsp   "&#160;">
]>

<xsl:stylesheet
        version="1.0"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:xalan="http://xml.apache.org/xalan"
        xmlns:ext="ext"
        exclude-result-prefixes="ext"
>
    <xsl:output method="xml" indent="yes" xalan:indent-amount="4" encoding="UTF-8"/>

    <xsl:param name="webapp.context"/>
    <xsl:param name="webapp.reloadable"/>
    <xsl:param name="dir.build.webapp"/>

    <xsl:template match="/">
        <Context
                path="{$webapp.context}"
                docBase="{$dir.build.webapp}"
                debug="0"
                privileged="false">

            <xsl:choose>
                <xsl:when test="$webapp.reloadable = 'true'">
                    <xsl:attribute name="reloadable">true</xsl:attribute>
                    <Loader backgroundProcesssorDelay="1"
                            useSystemClassLoaderAsParent="false"
                            reloadable="true"
                            delegate="false"/>
                </xsl:when>
                <xsl:otherwise>
                    <Loader useSystemClassLoaderAsParent="false"
                            reloadable="false"
                            delegate="false"/>
                </xsl:otherwise>
            </xsl:choose>
        </Context>
    </xsl:template>

</xsl:stylesheet>
