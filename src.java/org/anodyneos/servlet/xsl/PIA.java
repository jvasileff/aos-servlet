package org.anodyneos.servlet.xsl;

import java.util.Hashtable;
import java.util.StringTokenizer;

import org.w3c.dom.ProcessingInstruction;

/**
 * Parses a processing instruction's (PI) attributes for easy retrieval.
 * JVAS - code taken from xalan sources - DefaultApplyXSL.java
 */
public class PIA {

    private Hashtable<String, String> piAttributes = null;

    /**
     * Constructor.
     * @param pi The processing instruction whose attributes are to be parsed
     */
    public PIA(ProcessingInstruction pi)
    {
        piAttributes = new Hashtable<String, String>();
        StringTokenizer tokenizer = new StringTokenizer(pi.getNodeValue(), "=\"");
        while(tokenizer.hasMoreTokens())
            {
                piAttributes.put(tokenizer.nextToken().trim(), tokenizer.nextToken().trim());
            }
    }
    /**
     * Returns value of specified attribute.
     *  @param name Attribute name
     *  @return Attribute value, or null if the attribute name does not exist
     */
    public String getAttribute(String name)
    {
        return piAttributes.get(name);
    }
}
