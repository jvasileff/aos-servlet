package org.anodyneos.servlet.email;

import org.anodyneos.commons.net.URI;
import org.anodyneos.commons.xml.UnifiedResolver;
import org.anodyneos.commons.xml.xsl.TemplatesCache;
import org.w3c.dom.Document;

public class EmailContext {

    private TemplatesCache templatesCache;
    private UnifiedResolver resolver;
    private Params params;
    private URI configURI;
    private Document configDoc;

    public EmailContext(
            TemplatesCache templatesCache,
            UnifiedResolver resolver,
            Params params,
            URI configURI,
            Document configDoc) {

        this.templatesCache = templatesCache;
        this.resolver       = resolver;
        this.params         = params;
        this.configURI      = configURI;
        this.configDoc      = configDoc;
    }

    public TemplatesCache getTemplatesCache() {
        return templatesCache;
    }

    public UnifiedResolver getResolver() {
        return resolver;
    }

    public Params getParams() {
        return params;
    }

    public URI getConfigURI() {
        return configURI;
    }

    public Document getConfigDoc() {
        return configDoc;
    }

}
