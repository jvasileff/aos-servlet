package org.anodyneos.servlet.email;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class StringDataSource extends ADataSource {

    public static final String DEFAULT_MIME_TYPE = "text/plain";

    private String content;

    public StringDataSource(String content) {
        setCharset(DEFAULT_CHARSET);
        setMimeType(DEFAULT_MIME_TYPE);
        this.content = content;
    }

    public InputStream getInputStream() throws java.io.IOException {
        return new ByteArrayInputStream(content.getBytes(getCharset()));
    }

    public OutputStream getOutputStream() throws java.io.IOException {
        throw new java.lang.UnsupportedOperationException();
    }
}
