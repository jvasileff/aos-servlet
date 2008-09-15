package org.anodyneos.servlet.xsl.xalan;


public class BBCodeParserTester extends BBCodeParser {

    private StringBuffer sb = new StringBuffer();

    // CONSTRUCTORS
    public BBCodeParserTester(java.io.InputStream stream) {
        super(stream);
    }

    public BBCodeParserTester(java.io.Reader stream) {
        super(stream);
    }

    // MAIN
    public static void main(String args[]) throws ParseException {
        BBCodeParserTester parser = new BBCodeParserTester(System.in);
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

    protected void processInvalidOpen(String s) { sb.append(s); }
    protected void processOpenSimpleTag(String s) { sb.append("<" + s + ">"); }
    protected void processCloseTag(String s) { sb.append("</" + s + ">"); }

    protected void processOpenUrlFtpTag(String s) { processOpenUrlTag("ftp://" + s); }
    protected void processOpenUrlWwwTag(String s) { processOpenUrlTag("http://" + s); }
    protected void processOpenUrlEmailTag(String s) { processOpenUrlTag("mailto:" + s); }

    protected void processOpenUrlTag(String s) { sb.append("<a href='" + s + "'>"); }
    protected void processOpenColorTag(String s) { sb.append("<span style='color:" + s + "'>"); }
    protected void processOpenSizeTag(String s) { sb.append("<span style='size:" + s + "%'>"); }

    private void addHref(String href, String display) {
        sb.append("<a href='" + href + "'>" + display + "</a>");
    }
}
