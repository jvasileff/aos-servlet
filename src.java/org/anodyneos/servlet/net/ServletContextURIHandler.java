package org.anodyneos.servlet.net;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import javax.servlet.ServletContext;

import org.anodyneos.commons.net.AbstractURIHandler;
import org.anodyneos.commons.net.URIHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class's toURL method resolves NON-Opaque URIs of the form
 * "<i>webapp</i>:///WEB-INF/some/path/SomeFile.xml"
 * to URLs within the provided ServletContext. The scheme, authority, query, and
 * fragment portions of the URI are disregarded. the URI should not include
 * leading backslash characters.
 *
 * NOTE: The scheme "webapp" is only an example. This class is not scheme
 * specific and users of this class may use another scheme name.
 *
 * @author jvas
 */
public class ServletContextURIHandler extends AbstractURIHandler implements URIHandler {

    private static final Log log = LogFactory.getLog(ServletContextURIHandler.class);
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
        assert(! uri.isOpaque());
        String path = uri.getPath();
        if (null != path) {
            try {
                return servletContext.getResource(path);
            } catch (MalformedURLException e) {
                log.warn("Returning null; URI is invalid: " + uri.toString(), e);
                return null;
            }
        } else {
            return null;
        }
    }

}
