package org.anodyneos.servlet.xsl.xalan;


public class AutoHtmlParserTester extends AutoHtmlParser {

    private StringBuffer sb = new StringBuffer();

    // CONSTRUCTORS
    public AutoHtmlParserTester(java.io.InputStream stream) {
        super(stream);
    }

    public AutoHtmlParserTester(java.io.Reader stream) {
        super(stream);
    }

    // MAIN
    public static void main(String args[]) throws ParseException {
        AutoHtmlParserTester parser = new AutoHtmlParserTester(System.in);
        parser.process();
    }

    private void process() throws ParseException {
        Input();
        System.out.println();
        System.out.println("=======================================");
        System.out.println(sb.toString());
        System.out.println("=======================================");
        System.out.println();
    }

    protected void processWord(String s)   { sb.append(s); }
    protected void processSpace(String s)  { sb.append(s); }
    protected void processEol(String s)    { sb.append(s); }
    protected void processEmail(String s)  { addHref("mailto:" + s, s); }
    protected void processUrl(String s)    { addHref(s, s); }
    protected void processFtp(String s)    { addHref("ftp://" + s, s); }
    protected void processWww(String s)    { addHref("http://" + s, s); }

    private void addHref(String href, String display) {
        sb.append("<a href='" + href + "'>" + display + "</a>");
    }
}
