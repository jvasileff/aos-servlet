package org.anodyneos.servlet.xsl.xalan;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

public class AutoHtmlParserDom extends AutoHtmlParser {

    private static AutoHtmlParserUrlGen urlGenDefault = new AutoHtmlParserUrlGenDefault();

    private StringBuffer sb;
    private Node node;
    private Document doc;
    private AutoHtmlParserUrlGen urlGen;

    // CONSTRUCTORS
    public AutoHtmlParserDom(java.io.InputStream stream, Node node) {
        super(stream);
        this.node = node;
        this.doc = node.getOwnerDocument();
    }

    public AutoHtmlParserDom(java.io.Reader stream, Node node) {
        super(stream);
        this.node = node;
        this.doc = node.getOwnerDocument();
    }

    public void setUrlGen(AutoHtmlParserUrlGen urlGen) {
        this.urlGen = urlGen;
    }

    public Node process() throws ParseException {
        if (urlGen == null) {
            urlGen = urlGenDefault;
        }
        Input();
        flushText();
        return node;
    }

    protected void processWord(String s)   { addText(s); }
    protected void processSpace(String s)  { addSpace(s); }
    protected void processEol(String s)    { addEol(s); }
    protected void processEmail(String s)  { addHref(urlGen.emailToUrl(s), s); }
    protected void processUrl(String s)    { addHref(urlGen.urlToUrl(s), s); }
    protected void processFtp(String s)    { addHref(urlGen.ftpToUrl(s), s); }
    protected void processWww(String s)    { addHref(urlGen.wwwToUrl(s), s); }

    private void addSpace(String s) {
        // add just one space to make html look better
        // addText(s);
        addText(' ');

        /* This doesn't work well - it breaks autowrapping
        for (int i=0; i < s.length(); i++) {
            if (s.charAt(i) == ' ') {
                // add nbsp
                addText('\u00A0');
            } else {
                addText(s.charAt(i));
            }
        }
        */
    }

    private void addEol(String s) {
        // first, write out cached text to node
        flushText();

        // add br element
        Element el = doc.createElement("br");
        //Element el = doc.createElementNS("http://www.w3.org/1999/xhtml", "br");
        node.appendChild(el);
    }

    private void addText(char c) {
        // Consolidate calls to addText prior to adding text node
        if (null == sb) {
            sb = new StringBuffer();
        }
        sb.append(c);
    }

    private void addText(String s) {
        // Consolidate calls to addText prior to adding text node
        if (null == sb) {
            sb = new StringBuffer();
        }
        sb.append(s);
    }

    private void flushText() {
        // Write contents of sb to node.
        if (null != sb) {
            Text text = doc.createTextNode(sb.toString());
            node.appendChild(text);
            sb = null;
        }
    }

    private void addHref(String href, String display)
    {
        // first, write out cached text to node
        flushText();

        // add "a" element
        //Element el = doc.createElementNS("http://www.w3.org/1999/xhtml", "a");
        Element el = doc.createElement("a");
        node.appendChild(el);

        // add display text
        Text text = doc.createTextNode(display);
        el.appendChild(text);

        // add href attribute
        el.setAttribute("href", href);
    }
}
