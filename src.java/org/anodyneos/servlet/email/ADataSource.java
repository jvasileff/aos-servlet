package org.anodyneos.servlet.email;


import java.io.InputStream;
import java.io.OutputStream;

public abstract class ADataSource implements javax.activation.DataSource {

    public static final String DEFAULT_CHARSET;

    static {
        DEFAULT_CHARSET = System.getProperty("file.encoding");
    }

    private String mimeType;
    private String charset;
    private String name;

    protected ADataSource() {
        // super();
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
        if (getCharset() == null) {
            return(getMimeType());
        } else {
            return(getMimeType() + "; charset=" + getCharset());
        }
    }

    public abstract InputStream getInputStream() throws java.io.IOException;

    public abstract OutputStream getOutputStream() throws java.io.IOException;
}
