package org.anodyneos.servlet.net;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContext;

import org.anodyneos.commons.net.URI;
import org.anodyneos.commons.net.URIHandler;

public class ServletContextURIHandler extends URIHandler {

    private ServletContext servletContext;

    public ServletContextURIHandler(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    /**
     *  Returns a URL for the given URI or null if the URI cannot be resolved.
     *
     *  @return The URL or null.
     */
    public URL toURL(URI uri) {
        String path = uri.getPath();
        try {
            return servletContext.getResource(path);
        } catch (MalformedURLException e) {
            return null;
        }
    }

}
