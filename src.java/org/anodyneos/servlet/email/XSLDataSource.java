package org.anodyneos.servlet.email;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.anodyneos.commons.net.URI;
import org.anodyneos.commons.xml.xsl.TemplatesCache;

public class XSLDataSource extends ADataSource {

    public static final String DEFAULT_MIME_TYPE = "text/html";

    private TemplatesCache xslCache;
    private URI xslFile;
    private Params params;

    public XSLDataSource(TemplatesCache xslCache, URI xslFile, Params params) {
        setCharset(DEFAULT_CHARSET);
        setMimeType(DEFAULT_MIME_TYPE);
        this.xslCache = xslCache;
        this.xslFile = xslFile;
        this.params = params;
    }

    public InputStream getInputStream() throws java.io.IOException {
        ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        try {
            Transformer trans = xslCache.getTransformer(xslFile);
            if (null != getCharset()) {
                trans.setOutputProperty(OutputKeys.ENCODING, getCharset());
            }
            trans.setURIResolver(xslCache.getResolver());

            trans.transform(new DOMSource(params.getDocument()), new StreamResult(baos));
        } catch (IOException e) {
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }

        return new ByteArrayInputStream(baos.toByteArray());
    }

    public OutputStream getOutputStream() throws java.io.IOException {
        throw new java.lang.UnsupportedOperationException();
    }
}
