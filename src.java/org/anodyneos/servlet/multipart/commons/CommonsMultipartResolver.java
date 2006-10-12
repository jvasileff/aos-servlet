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
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.anodyneos.servlet.multipart.MaxUploadSizeExceededException;
import org.anodyneos.servlet.multipart.MultipartException;
import org.anodyneos.servlet.multipart.support.MultipartHttpServletRequest;
import org.anodyneos.servlet.multipart.support.DefaultMultipartHttpServletRequest;

/**
 * MultipartResolver implementation for
 * <a href="http://jakarta.apache.org/commons/fileupload">Jakarta Commons FileUpload</a>.
 *
 * <p>Provides maxUploadSize, maxInMemorySize, and defaultEncoding settings as
 * bean properties; see respective DiskFileUpload properties (sizeMax, sizeThreshold,
 * headerEncoding) for details in terms of defaults and accepted values.
 *
 * <p>Saves temporary files to the servlet container's temporary directory.
 * Needs to be initialized <i>either</i> by an application context <i>or</i>
 * via the constructor that takes a ServletContext (for standalone usage).
 *
 * @author Trevor D. Cook
 * @author Juergen Hoeller
 * @since 29.09.2003
 * @see #CommonsMultipartResolver(ServletContext)
 * @see CommonsMultipartFile
 * @see org.apache.commons.fileupload.DiskFileUpload
 */
public class CommonsMultipartResolver {

    protected final Log logger = LogFactory.getLog(getClass());

    /**
     * Standard Servlet spec context attribute that specifies a temporary
     * directory for the current web application, of type java.io.File
     */
    public static final String TEMP_DIR_CONTEXT_ATTRIBUTE = "javax.servlet.context.tempdir";

    //private DiskFileUpload fileUpload;
    private DiskFileItemFactory diskFileItemFactory;

    private Long maxUploadSize; // null for default

    private String defaultEncoding = "ISO-8859-1";

    private File uploadTempDir;


    /**
     * Constructor for use as bean. Determines the servlet container's
     * temporary directory via the ServletContext passed in as through the
     * ServletContextAware interface (typically by a WebApplicationContext).
     * @see #setServletContext
     * @see org.springframework.web.context.ServletContextAware
     * @see org.springframework.web.context.WebApplicationContext
     */
    public CommonsMultipartResolver() {
        this.diskFileItemFactory = newDiskFileItemFactory();
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
     * Initialize the underlying org.apache.commons.fileupload.DiskFileUpload instance.
     * Can be overridden to use a custom subclass, e.g. for testing purposes.
     * @return the new DiskFileUpload instance
     */
    protected DiskFileItemFactory newDiskFileItemFactory() {
        return new DiskFileItemFactory();
    }

    /**
     * Return the underlying org.apache.commons.fileupload.DiskFileUpload instance.
     * There is hardly any need to access this.
     * @return the underlying DiskFileUpload instance
     */
    private DiskFileItemFactory getDiskFileItemFactory() {
        return diskFileItemFactory;
    }

    /**
     * Set the maximum allowed size (in bytes) before uploads are refused.
     * -1 indicates no limit (the default).
     * @param maxUploadSize the maximum upload size allowed
     * @see org.apache.commons.fileupload.FileUploadBase#setSizeMax
     */
    public void setMaxUploadSize(long maxUploadSize) {
        this.maxUploadSize = Long.valueOf(maxUploadSize);
    }

    /**
     * Set the maximum allowed size (in bytes) before uploads are written to disk.
     * Uploaded files will still be received past this amount, but they will not be
     * stored in memory. Default is 10240, according to Commons FileUpload.
     * @param maxInMemorySize the maximum in memory size allowed
     * @see org.apache.commons.fileupload.DiskFileUpload#setSizeThreshold
     */
    public void setMaxInMemorySize(int maxInMemorySize) {
        this.diskFileItemFactory.setSizeThreshold(maxInMemorySize);
    }

    /**
     * Set the default character encoding to use for parsing requests,
     * to be applied to headers of individual parts and to form fields.
     * Default is ISO-8859-1, according to the Servlet spec.
     * <p>If the request specifies a character encoding itself, the request
     * encoding will override this setting. This also allows for generically
     * overriding the character encoding in a filter that invokes the
     * ServletRequest.setCharacterEncoding method.
     * @param defaultEncoding the character encoding to use
     * @see #determineEncoding
     * @see javax.servlet.ServletRequest#getCharacterEncoding
     * @see javax.servlet.ServletRequest#setCharacterEncoding
     * @see WebUtils#DEFAULT_CHARACTER_ENCODING
     * @see org.apache.commons.fileupload.FileUploadBase#setHeaderEncoding
     */
    public void setDefaultEncoding(String defaultEncoding) {
        this.defaultEncoding = defaultEncoding;
    }

    /**
     * Set the temporary directory where uploaded files get stored.
     * Default is the servlet container's temporary directory for the web application.
     * @see org.springframework.web.util.WebUtils#TEMP_DIR_CONTEXT_ATTRIBUTE
     */
    public void setUploadTempDir(File uploadTempDir) throws IOException {
        if (!uploadTempDir.exists() && !uploadTempDir.mkdirs()) {
            throw new IllegalArgumentException("Given uploadTempDir [" + uploadTempDir + "] could not be created");
        }
        this.uploadTempDir = uploadTempDir;
        this.diskFileItemFactory.setRepository(uploadTempDir);
    }

    public void setServletContext(ServletContext servletContext) {
        if (this.uploadTempDir == null) {
            this.diskFileItemFactory.setRepository((File) servletContext.getAttribute(TEMP_DIR_CONTEXT_ATTRIBUTE));
        }
    }

    public boolean isMultipart(HttpServletRequest request) {
        return ServletFileUpload.isMultipartContent(request);
    }

    public MultipartHttpServletRequest resolveMultipart(HttpServletRequest request) throws MultipartException {
        ServletFileUpload fileUpload = new ServletFileUpload(diskFileItemFactory);
        String enc = determineEncoding(request);
        fileUpload.setHeaderEncoding(enc);
        if (null != maxUploadSize) {
            fileUpload.setSizeMax(maxUploadSize.longValue());
        }

        try {
            Map multipartFiles = new HashMap();
            Map multipartParams = new HashMap();

            // Extract multipart files and multipart parameters.
            List fileItems = fileUpload.parseRequest(request);
            for (Iterator it = fileItems.iterator(); it.hasNext();) {
                FileItem fileItem = (FileItem) it.next();
                if (fileItem.isFormField()) {
                    String value = null;
                    try {
                        value = fileItem.getString(enc);
                    }
                    catch (UnsupportedEncodingException ex) {
                        logger.warn("Could not decode multipart item '" + fileItem.getFieldName() +
                            "' with encoding '" + enc + "': using platform default");
                        value = fileItem.getString();
                    }
                    String[] curParam = (String[]) multipartParams.get(fileItem.getFieldName());
                    if (curParam == null) {
                        // simple form field
                        multipartParams.put(fileItem.getFieldName(), new String[] { value });
                    }
                    else {
                        // array of simple form fields
                        String[] newParam = addStringToArray(curParam, value);
                        multipartParams.put(fileItem.getFieldName(), newParam);
                    }
                }
                else {
                    // multipart file field
                    CommonsMultipartFile file = new CommonsMultipartFile(fileItem);
                    multipartFiles.put(file.getName(), file);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Found multipart file [" + file.getName() + "] of size " + file.getSize() +
                            " bytes with original filename [" + file.getOriginalFilename() + "], stored " +
                            file.getStorageDescription());
                    }
                }
            }
            return new DefaultMultipartHttpServletRequest(request, multipartFiles, multipartParams);
        } catch (FileUploadBase.SizeLimitExceededException ex) {
            throw new MaxUploadSizeExceededException(fileUpload.getSizeMax(), ex);
        } catch (FileUploadException ex) {
            throw new MultipartException("Could not parse multipart request", ex);
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
        String enc = request.getCharacterEncoding();
        if (enc == null) {
            enc = this.defaultEncoding;
        }
        return enc;
    }

    public void cleanupMultipart(MultipartHttpServletRequest request) {
        Map multipartFiles = request.getFileMap();
        for (Iterator it = multipartFiles.values().iterator(); it.hasNext();) {
            CommonsMultipartFile file = (CommonsMultipartFile) it.next();
            if (logger.isDebugEnabled()) {
                logger.debug("Cleaning up multipart file [" + file.getName() + "] with original filename [" +
                        file.getOriginalFilename() + "], stored " + file.getStorageDescription());
            }
            file.getFileItem().delete();
        }
    }

    private static String[] addStringToArray(String[] arr, String str) {
        String[] newArr = new String[arr.length + 1];
        System.arraycopy(arr, 0, newArr, 0, arr.length);
        newArr[arr.length] = str;
        return newArr;
    }

}
