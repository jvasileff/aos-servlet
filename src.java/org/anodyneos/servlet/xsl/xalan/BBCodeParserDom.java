package org.anodyneos.servlet.xsl.xalan;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

public class BBCodeParserDom extends BBCodeParser {

    private static final boolean useNamespace = true;

    private static AutoHtmlParserUrlGen urlGenDefault = new AutoHtmlParserUrlGenDefault();

    private StringBuffer sb;
    private List<Node> nodeStack = new ArrayList<Node>();
    private Node currentNode;
    private Node initialNode;
    private Document doc;
    private AutoHtmlParserUrlGen urlGen;

    // CONSTRUCTORS
    public BBCodeParserDom(java.io.InputStream stream, Node node) {
        super(stream);
        initialNode = node;
        pushNode(node);
        this.doc = node.getOwnerDocument();
    }

    public BBCodeParserDom(java.io.Reader stream, Node node) {
        super(stream);
        initialNode = node;
        pushNode(node);
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
        return initialNode;
    }

    protected void processWord(String s)   { addText(s); }
    protected void processSpace(String s)  { addSpace(s); }
    protected void processEol(String s)    { addEol(s); }
    protected void processEmail(String s)  { addHref(urlGen.emailToUrl(s), s); }
    protected void processUrl(String s)    { addHref(urlGen.urlToUrl(s), s); }
    protected void processFtp(String s)    { addHref(urlGen.ftpToUrl(s), s); }
    protected void processWww(String s)    { addHref(urlGen.wwwToUrl(s), s); }

    private void pushNode(Node node) {
        currentNode = node;
        nodeStack.add(node);
    }

    private void popNode() {
        int current = nodeStack.size() - 1;
        nodeStack.remove(current);
        currentNode = nodeStack.get(current - 1);
    }

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
        Element el = createHtmlElement("br");
        currentNode.appendChild(el);
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
            currentNode.appendChild(text);
            sb = null;
        }
    }

    private void addHref(String href, String display)
    {
        // first, write out cached text to node
        flushText();

        Element el = createHtmlElement("a");
        currentNode.appendChild(el);

        // add display text
        Text text = doc.createTextNode(display);
        el.appendChild(text);

        // add href attribute
        el.setAttribute("href", href);
    }

    protected void processCloseTag(String s) {
        flushText();
        popNode();
    }

    protected void processInvalidOpen(String s) {
        processWord(s);
    }

    protected void processOpenColorTag(String s) {
        flushText();
        Element el = createHtmlElement("span");
        currentNode.appendChild(el);
        el.setAttribute("style", "color:" + s + ";");
        pushNode(el);
    }

    protected void processOpenSimpleTag(String s) {
        flushText();

        // <#SIMPLE_TAG:   "b" | "i" | "u" | "s" | "quote" | "code" >

        if (BBCodeParser.TAG_BOLD.equals(s)) {
            Element el = createHtmlElement("span");
            currentNode.appendChild(el);
            el.setAttribute("style", "font-weight:bold;");
            pushNode(el);
        } else if (BBCodeParser.TAG_ITALICS.equals(s)) {
            Element el = createHtmlElement("span");
            currentNode.appendChild(el);
            el.setAttribute("style", "font-style:italic;");
            pushNode(el);
        } else if (BBCodeParser.TAG_UNDERLINE.equals(s)) {
            Element el = createHtmlElement("span");
            currentNode.appendChild(el);
            el.setAttribute("style", "text-decoration:underline;");
            pushNode(el);
        } else if (BBCodeParser.TAG_STRIKETHROUGH.equals(s)) {
            Element el = createHtmlElement("span");
            currentNode.appendChild(el);
            el.setAttribute("style", "text-decoration:line-through;");
            pushNode(el);
        } else if (BBCodeParser.TAG_QUOTE.equals(s)) {
            Element el = createHtmlElement("blockquote");
            currentNode.appendChild(el);
            Element el2 = createHtmlElement("p");
            el.appendChild(el2);
            pushNode(el2);
        } else if (BBCodeParser.TAG_CODE.equals(s)) {
            Element el = createHtmlElement("pre");
            currentNode.appendChild(el);
            pushNode(el);
        }
    }

    protected void processOpenSizeTag(String s) {
        flushText();
        Element el = createHtmlElement("span");
        currentNode.appendChild(el);
        el.setAttribute("style", "font-size:" + s + "%;");
        pushNode(el);
    }

    protected void processOpenUrlFtpTag(String s) { _processOpenUrlTag(urlGen.ftpToUrl(s)); }
    protected void processOpenUrlWwwTag(String s) { _processOpenUrlTag(urlGen.wwwToUrl(s)); }
    protected void processOpenUrlEmailTag(String s) { _processOpenUrlTag(urlGen.emailToUrl(s)); }
    protected void processOpenUrlTag(String s) { _processOpenUrlTag(urlGen.urlToUrl(s)); }

    protected void _processOpenUrlTag(String s) {
        flushText();
        Element el = createHtmlElement("a");
        currentNode.appendChild(el);
        el.setAttribute("href", s);
        pushNode(el);
    }

    private Element createHtmlElement(String name) {
        Element el;
        if (useNamespace) {
            el = doc.createElementNS("http://www.w3.org/1999/xhtml", name);
        } else {
            el = doc.createElement(name);
        }
        return el;
    }

}
