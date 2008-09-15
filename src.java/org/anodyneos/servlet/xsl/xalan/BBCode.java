package org.anodyneos.servlet.xsl.xalan;

import javax.xml.transform.TransformerException;

import org.apache.xalan.extensions.ExpressionContext;
import org.apache.xalan.extensions.XSLProcessorContext;
import org.apache.xalan.templates.ElemExtensionCall;
import org.apache.xpath.objects.XObject;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;

public class BBCode extends Extension {

    private AutoHtmlParserUrlGen autoHtmlParserUrlGen;

    public BBCode() {
        super();
    }

    public void init (XSLProcessorContext context, ElemExtensionCall extElem)
    throws TransformerException {
        XObject xobj;

        AutoHtmlParserUrlGen alug = null;
        xobj = getAttributeAsSelect("urlGenerator", context, extElem);
        autoHtmlParserUrlGen = (AutoHtmlParserUrlGen) xobj.object();
    }

    public DocumentFragment bbCode( XSLProcessorContext context, ElemExtensionCall extElem)
    throws TransformerException, ParseException {
        XObject xobj;
        String inString = null;

        xobj = getAttributeAsSelect("select", context, extElem);
        if (xobj != null) {
            inString = xobj.str();
        }
        if((null == inString) || (inString.length() == 0)) {
            inString = extElem.getAttribute ("text", context.getContextNode(), context.getTransformer());
        }

        Document doc = context.getContextNode().getOwnerDocument();
        DocumentFragment docFrag = doc.createDocumentFragment();
        BBCodeParserDom ald = new BBCodeParserDom(new java.io.StringReader(inString), docFrag);
        if (null != autoHtmlParserUrlGen) {
            ald.setUrlGen(autoHtmlParserUrlGen);
        }
        return (DocumentFragment) ald.process();
    }

    public DocumentFragment bbCode( ExpressionContext eContext, String inString)
    throws TransformerException, ParseException {
        Document doc = eContext.getContextNode().getOwnerDocument();
        DocumentFragment docFrag = doc.createDocumentFragment();

        BBCodeParserDom ald = new BBCodeParserDom(new java.io.StringReader(inString), docFrag);
        if (null != autoHtmlParserUrlGen) {
            ald.setUrlGen(autoHtmlParserUrlGen);
        }
        return (DocumentFragment) ald.process();
    }
}
