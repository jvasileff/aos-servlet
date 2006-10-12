package org.anodyneos.servlet.email;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.anodyneos.servlet.multipart.MultipartFile;

public class MultipartFileDataSource extends ADataSource {

    MultipartFile mf;

    public MultipartFileDataSource(MultipartFile mf) {
        this.mf = mf;
        setMimeType(mf.getContentType());
        setName(mf.getOriginalFilename());
    }

    public InputStream getInputStream() throws IOException {
        return mf.getInputStream();
    }

    public OutputStream getOutputStream() throws IOException {
        throw new java.lang.UnsupportedOperationException();
    }

}
