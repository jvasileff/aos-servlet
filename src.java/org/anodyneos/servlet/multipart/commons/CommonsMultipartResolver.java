/*
 * Copyright 2002-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.anodyneos.servlet.multipart.commons;

import java.io.File;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.anodyneos.servlet.multipart.MaxUploadSizeExceededException;
import org.anodyneos.servlet.multipart.MultipartException;
import org.anodyneos.servlet.multipart.MultipartHttpServletRequest;
import org.anodyneos.servlet.multipart.support.DefaultMultipartHttpServletRequest;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.servlet.ServletRequestContext;

/**
 * Servlet-based MultipartResolver implementation for
 * <a href="http://jakarta.apache.org/commons/fileupload">Jakarta Commons FileUpload</a>
 * 1.1 or higher.
 *
 * <p>Provides maxUploadSize, maxInMemorySize, and defaultEncoding settings as
 * bean properties (inherited from CommonsFileUploadSupport). See respective
 * ServletFileUpload / DiskFileItemFactory properties (sizeMax, sizeThreshold,
 * headerEncoding) for details in terms of defaults and accepted values.
 *
 * <p>Saves temporary files to the servlet container's temporary directory.
 * Needs to be initialized <i>either</i> by an application context <i>or</i>
 * via the constructor that takes a ServletContext (for standalone usage).
 *
 * <p><b>NOTE:</b> As of Spring 2.0, this multipart resolver requires
 * Commons FileUpload 1.1 or higher. The implementation does not use
 * any deprecated FileUpload 1.0 API anymore, to be compatible with future
 * Commons FileUpload releases.
 *
 * @author Trevor D. Cook
 * @author Juergen Hoeller
 * @since 29.09.2003
 * @see #CommonsMultipartResolver(ServletContext)
 * @see CommonsMultipartFile
 * @see org.springframework.web.portlet.multipart.PortletMultipartResolver
 * @see org.apache.commons.fileupload.servlet.ServletFileUpload
 * @see org.apache.commons.fileupload.disk.DiskFileItemFactory
 */
public class CommonsMultipartResolver extends CommonsFileUploadSupport {

    public static final String TEMP_DIR_CONTEXT_ATTRIBUTE = "javax.servlet.context.tempdir";

    /**
     * Constructor for use as bean. Determines the servlet container's
     * temporary directory via the ServletContext passed in as through the
     * ServletContextAware interface (typically by a WebApplicationContext).
     * @see #setServletContext
     * @see org.springframework.web.context.ServletContextAware
     * @see org.springframework.web.context.WebApplicationContext
     */
    public CommonsMultipartResolver() {
        super();
    }

    /**
     * Constructor for standalone usage. Determines the servlet container's
     * temporary directory via the given ServletContext.
     * @param servletContext the ServletContext to use
     */
    public CommonsMultipartResolver(ServletContext servletContext) {
        this();
        setServletContext(servletContext);
    }

    /**
     * Initialize the underlying <code>org.apache.commons.fileupload.servlet.ServletFileUpload</code>
     * instance. Can be overridden to use a custom subclass, e.g. for testing purposes.
     * @param fileItemFactory the Commons FileItemFactory to use
     * @return the new ServletFileUpload instance
     */
    protected FileUpload newFileUpload(FileItemFactory fileItemFactory) {
        return new ServletFileUpload(fileItemFactory);
    }

    public void setServletContext(ServletContext servletContext) {
        if (!isUploadTempDirSpecified()) {
            getFileItemFactory().setRepository(getTempDir(servletContext));
        }
    }


    public boolean isMultipart(HttpServletRequest request) {
        return ServletFileUpload.isMultipartContent(new ServletRequestContext(request));
    }

    @SuppressWarnings("unchecked")
    public MultipartHttpServletRequest resolveMultipart(HttpServletRequest request) throws MultipartException {
        String encoding = determineEncoding(request);
        FileUpload fileUpload = prepareFileUpload(encoding);
        try {
            List<FileItem> fileItems = ((ServletFileUpload) fileUpload).parseRequest(request);
            MultipartParsingResult parsingResult = parseFileItems(fileItems, encoding);
            return new DefaultMultipartHttpServletRequest(
                    request, parsingResult.getMultipartFiles(), parsingResult.getMultipartParameters());
        }
        catch (FileUploadBase.SizeLimitExceededException ex) {
            throw new MaxUploadSizeExceededException(fileUpload.getSizeMax(), ex);
        }
        catch (FileUploadException ex) {
            throw new MultipartException("Could not parse multipart servlet request", ex);
        }
    }

    /**
     * Determine the encoding for the given request.
     * Can be overridden in subclasses.
     * <p>The default implementation checks the request encoding,
     * falling back to the default encoding specified for this resolver.
     * @param request current HTTP request
     * @return the encoding for the request (never <code>null</code>)
     * @see javax.servlet.ServletRequest#getCharacterEncoding
     * @see #setDefaultEncoding
     */
    protected String determineEncoding(HttpServletRequest request) {
        String encoding = request.getCharacterEncoding();
        if (encoding == null) {
            encoding = getDefaultEncoding();
        }
        return encoding;
    }

    public void cleanupMultipart(MultipartHttpServletRequest request) {
        cleanupFileItems(request.getFileMap().values());
    }

    /**
     * Return the temporary directory for the current web application,
     * as provided by the servlet container.
     * @param servletContext the servlet context of the web application
     * @return the File representing the temporary directory
     */
    private static File getTempDir(ServletContext servletContext) {
            notNull(servletContext, "ServletContext must not be null");
            return (File) servletContext.getAttribute(TEMP_DIR_CONTEXT_ATTRIBUTE);
    }

    /**
     * Assert that an object is not <code>null</code> .
     * <pre class="code">Assert.notNull(clazz, "The class must not be null");</pre>
     * @param object the object to check
     * @param message the exception message to use if the assertion fails
     * @throws IllegalArgumentException if the object is <code>null</code>
     */
    private static void notNull(Object object, String message) {
            if (object == null) {
                    throw new IllegalArgumentException(message);
            }
    }


}
