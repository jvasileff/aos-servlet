package org.anodyneos.servlet.xsl.xalan;

import javax.xml.transform.TransformerException;

import org.apache.xalan.extensions.XSLProcessorContext;
import org.apache.xalan.templates.ElemExtensionCall;
import org.apache.xml.utils.QName;
import org.apache.xpath.XPath;
import org.apache.xpath.objects.XObject;
public class Extension {

    public Extension() {
        super();
    }

    protected static XObject getAttributeAsSelect( String attributeName, XSLProcessorContext context, ElemExtensionCall extElem)
    throws TransformerException {
        XObject xobj = null;
        String expr = extElem.getAttribute (attributeName, context.getContextNode(), context.getTransformer());
        if(null != expr) {
            org.apache.xpath.XPathContext xctxt = context.getTransformer().getXPathContext();
            XPath myxpath = new XPath(expr, extElem, xctxt.getNamespaceContext(), XPath.SELECT);
            xobj = myxpath.execute(xctxt, context.getContextNode(), extElem);
        }
        return xobj;
    }

    protected static XObject getVariableOrParam( QName qName, XSLProcessorContext context, ElemExtensionCall extElem)
    throws TransformerException {
        org.apache.xpath.XPathContext xctxt = context.getTransformer().getXPathContext();
        xctxt.pushNamespaceContext(extElem);
        try {
            return xctxt.getExpressionContext().getVariableOrParam(qName);
        } finally {
            xctxt.popNamespaceContext();
        }

        /*
            XObject xObject;

            // Example #1
            org.apache.xpath.XPathContext xctxt = context.getTransformer().getXPathContext();
            xctxt.pushNamespaceContext(extElem);
            xObject = xctxt.getExpressionContext().getVariableOrParam(qName);
            xctxt.popNamespaceContext();

            // Example #2
            org.apache.xpath.XPathContext xctxt = context.getTransformer().getXPathContext();
            xctxt.pushNamespaceContext(extElem);
            xObject = xctxt.getVarStack().getVariableOrParam(xctxt, qName);
            xctxt.popNamespaceContext();

            // Example #3
            org.apache.xpath.operations.Variable var = new org.apache.xpath.operations.Variable();
            var.setQName(qName);
            org.apache.xpath.XPathContext xctxt = context.getTransformer().getXPathContext();
            xctxt.pushNamespaceContext(extElem);
            xctxt.pushCurrentNodeAndExpression( xctxt.getDTMHandleFromNode(context.getContextNode()),    xctxt.getDTMHandleFromNode(context.getContextNode()));
            xObject = var.execute(xctxt);
            xctxt.popCurrentNodeAndExpression();
            xctxt.popNamespaceContext();

            // Example #4
            String expr = "$param";
            org.apache.xpath.XPathContext xctxt = context.getTransformer().getXPathContext();
            XPath myxpath = new XPath(expr, extElem, xctxt.getNamespaceContext(), XPath.SELECT);
            xObject = myxpath.execute(xctxt, context.getContextNode(), extElem);
        */
    }
}
