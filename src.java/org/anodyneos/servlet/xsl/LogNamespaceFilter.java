package org.anodyneos.servlet.xsl;

import org.anodyneos.commons.xml.NamespaceMapping;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * Utility XMLFilter to help with debugging <code>NamespaceMapping</code>.
 *
 * @author jvas
 */
public class LogNamespaceFilter extends XMLFilterImpl {

    private NamespaceMapping mappings = new NamespaceMapping();

    public LogNamespaceFilter() {
        super();
    }

    public LogNamespaceFilter(XMLReader parent) {
        super(parent);
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        System.out.println("[START_PREFIX_MAPPING] " + prefix + ":" + uri);
        mappings.push(prefix, uri);
        super.startPrefixMapping(prefix, uri);
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        String uri = mappings.pop(prefix);
        System.out.println("[END_PREFIX_MAPPING] " + prefix + ":" + uri);
        super.endPrefixMapping(prefix);
    }

    public void startElement(String uri, String localName, String qName, Attributes attrs)
            throws SAXException {
        for (int i = 0; i < attrs.getLength(); i++) {
            System.out.println("[ATTR] " + attrs.getURI(i) + ":" + attrs.getQName(i) + "="
                    + attrs.getValue(i));
        }
        System.out.println("[START_ELEMENT] " + "URI: " + uri + "\tlocalName:" + localName
                + "\tqName:" + qName);
        super.startElement(uri, localName, qName, attrs);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        System.out.println("[END_ELEMENT] " + "URI: " + uri + "\tlocalName:" + localName
                + "\tqName:" + qName);
        super.endElement(uri, localName, qName);
    }

}
