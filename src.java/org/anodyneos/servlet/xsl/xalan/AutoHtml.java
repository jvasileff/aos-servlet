package org.anodyneos.servlet.xsl.xalan;

import javax.xml.transform.TransformerException;

import org.apache.xalan.extensions.ExpressionContext;
import org.apache.xalan.extensions.XSLProcessorContext;
import org.apache.xalan.templates.ElemExtensionCall;
import org.apache.xpath.objects.XObject;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;

public class AutoHtml extends Extension {

    private AutoHtmlParserUrlGen autoHtmlParserUrlGen;

    public AutoHtml() {
        super();
    }

    public void init (XSLProcessorContext context, ElemExtensionCall extElem)
    throws TransformerException {
        XObject xobj;

        AutoHtmlParserUrlGen alug = null;
        xobj = getAttributeAsSelect("urlGenerator", context, extElem);
        autoHtmlParserUrlGen = (AutoHtmlParserUrlGen) xobj.object();
    }

    public DocumentFragment autoHtml( XSLProcessorContext context, ElemExtensionCall extElem)
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
        AutoHtmlParserDom ald = new AutoHtmlParserDom(new java.io.StringReader(inString), docFrag);
        if (null != autoHtmlParserUrlGen) {
            ald.setUrlGen(autoHtmlParserUrlGen);
        }
        return (DocumentFragment) ald.process();
    }

    public DocumentFragment autoHtml( ExpressionContext eContext, String inString)
    throws TransformerException, ParseException {
        Document doc = eContext.getContextNode().getOwnerDocument();
        DocumentFragment docFrag = doc.createDocumentFragment();

        AutoHtmlParserDom ald = new AutoHtmlParserDom(new java.io.StringReader(inString), docFrag);
        if (null != autoHtmlParserUrlGen) {
            ald.setUrlGen(autoHtmlParserUrlGen);
        }
        return (DocumentFragment) ald.process();

        /*
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.newDocument();
        DocumentFragment docFrag = doc.createDocumentFragment();
        */

        /*
        0158       // This no longer will work right since the DTM.
        0159       // Document myDoc = myProcessor.getContextNode().getOwnerDocument();
        0160       try
        0161       {
        0165
        0166         Text textNode = myDoc.createTextNode(textNodeValue);
        0167         DocumentFragment docFrag = myDoc.createDocumentFragment();
        0168
        0169         docFrag.appendChild(textNode);
        0170
        0171         return new NodeSet(docFrag);
        0172       }
        0173       catch(ParserConfigurationException pce)
        0174       {
        0175         throw new org.apache.xml.utils.WrappedRuntimeException(pce);
        0176       }
        0177     }
        0178   }
        */
    }
}
