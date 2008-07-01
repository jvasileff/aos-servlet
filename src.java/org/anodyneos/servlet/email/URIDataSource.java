package org.anodyneos.servlet.email;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import org.anodyneos.commons.net.URIHelper;

public class URIDataSource implements javax.activation.DataSource {

    public static final String DEFAULT_MIME_TYPE = "application/octet-stream";
    public static final String DEFAULT_CHARSET;

    static {
        DEFAULT_CHARSET = System.getProperty("file.encoding");
    }

    private URI uri;
    private URIHelper uriHelper;
    private String mimeType = DEFAULT_MIME_TYPE;
    private String charset;
    private String name;

    public URIDataSource(URI uri, URIHelper uriHelper) {
        this.uri = uri;
        this.uriHelper = uriHelper;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
    public String getMimeType() {
        return mimeType;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }
    public String getCharset() {
        return charset;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public String getContentType() {
        if (charset == null) {
            return(mimeType);
        } else {
            return(mimeType + "; charset=" + getCharset());
        }
    }

    public InputStream getInputStream() throws java.io.IOException {
        return uriHelper.openStream(uri);
    }

    public OutputStream getOutputStream() throws java.io.IOException {
        throw new java.lang.UnsupportedOperationException();
    }
}
