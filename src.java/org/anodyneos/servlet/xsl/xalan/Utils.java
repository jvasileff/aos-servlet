package org.anodyneos.servlet.xsl.xalan;

import javax.xml.transform.TransformerException;

import org.apache.xalan.extensions.ExpressionContext;

public class Utils extends Extension {

    public Utils() {
        super();
    }

    public static String htmlNormalizeSpace( ExpressionContext eContext, String inString)
    throws TransformerException {
        boolean lastWasSpace = false;
        StringBuffer sb = new StringBuffer(inString.length());
        for (int i = 0; i < inString.length(); i++) {
            switch (inString.charAt(i))
            {
                case '\n':
                case '\r':
                case '\t':
                case ' ':
                if (! lastWasSpace) {
                    lastWasSpace = true;
                    sb.append(' ');
                }
                continue;
                default:
                    lastWasSpace = false;
                    sb.append(inString.charAt(i));
            }
        }
        return  sb.toString();
    }
}
