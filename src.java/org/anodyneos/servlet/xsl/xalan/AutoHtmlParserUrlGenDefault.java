package org.anodyneos.servlet.xsl.xalan;

public class AutoHtmlParserUrlGenDefault implements AutoHtmlParserUrlGen {

    public AutoHtmlParserUrlGenDefault() {
        super();
    }

    public String emailToUrl(String s)  { return "mailto:" + s; }
    public String urlToUrl(String s)    { return s; }
    public String ftpToUrl(String s)    { return "ftp://" + s; }
    public String wwwToUrl(String s)    { return "http://" + s; }
}
