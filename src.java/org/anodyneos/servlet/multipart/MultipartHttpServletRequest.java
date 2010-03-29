/*
 * Copyright 2002-2006 the original author or authors.
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

package org.anodyneos.servlet.multipart;

import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.anodyneos.servlet.multipart.commons.CommonsMultipartFile;

/**
 * Provides additional methods for dealing with multipart content within a
 * servlet request, allowing to access uploaded files.
 * Implementations also need to override the standard
 * {@link javax.servlet.ServletRequest} methods for parameter access, making
 * multipart parameters available.
 *
 * <p>A concrete implementation is
 * {@link org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest}.
 * As an intermediate step,
 * {@link org.springframework.web.multipart.support.AbstractMultipartHttpServletRequest}
 * can be subclassed.
 *
 * @author Juergen Hoeller
 * @author Trevor D. Cook
 * @since 29.09.2003
 * @see MultipartResolver
 * @see MultipartFile
 * @see javax.servlet.http.HttpServletRequest#getParameter
 * @see javax.servlet.http.HttpServletRequest#getParameterNames
 * @see javax.servlet.http.HttpServletRequest#getParameterMap
 * @see org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest
 * @see org.springframework.web.multipart.support.AbstractMultipartHttpServletRequest
 */
public interface MultipartHttpServletRequest extends HttpServletRequest {

    /**
     * Return an {@link java.util.Iterator} of String objects containing the
     * parameter names of the multipart files contained in this request. These
     * are the field names of the form (like with normal parameters), not the
     * original file names.
     * @return the names of the files
     */
    Iterator<String> getFileNames();

    /**
     * Return the contents plus description of an uploaded file in this request,
     * or <code>null</code> if it does not exist.
     * @param name a String specifying the parameter name of the multipart file
     * @return the uploaded content in the form of a {@link org.springframework.web.multipart.MultipartFile} object
     */
    MultipartFile getFile(String name);

    /**
     * Return a {@link java.util.Map} of the multipart files contained in this request.
     * @return a map containing the parameter names as keys, and the
     * {@link org.springframework.web.multipart.MultipartFile} objects as values
     * @see MultipartFile
     */
    Map<String, CommonsMultipartFile> getFileMap();

}
