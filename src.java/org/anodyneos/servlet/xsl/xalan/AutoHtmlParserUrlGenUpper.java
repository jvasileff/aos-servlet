package org.anodyneos.servlet.xsl.xalan;

public class AutoHtmlParserUrlGenUpper implements AutoHtmlParserUrlGen {

    public AutoHtmlParserUrlGenUpper() {
        super();
    }

    public String emailToUrl(String s)  { return "MAILTO:" + s; }
    public String urlToUrl(String s)    { return s; }
    public String ftpToUrl(String s)    { return "FTP://" + s; }
    public String wwwToUrl(String s)    { return "HTTP://" + s; }
}
