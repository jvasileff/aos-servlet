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

package org.anodyneos.servlet.multipart;

import javax.servlet.ServletException;

//import org.springframework.web.util.NestedServletException;

/**
 * Exception thrown on multipart resolution.
 *
 * <p>Extends ServletException for convenient throwing in any Servlet resource
 * (such as a Filter), and NestedServletException for proper root cause handling
 * (as the plain ServletException doesn't expose its root cause at all).
 *
 * @author Trevor D. Cook
 * @since 29.09.2003
 * @see MultipartResolver#resolveMultipart
 * @see org.springframework.web.multipart.support.MultipartFilter
 */
public class MultipartException extends ServletException {

    private static final long serialVersionUID = -3404016365225340017L;

    /**
     * Constructor for MultipartException.
     * @param msg message
     */
    public MultipartException(String msg) {
        super(msg);
    }

    /**
     * Constructor for MultipartException.
     * @param msg message
     * @param ex root cause from multipart parsing API in use
     */
    public MultipartException(String msg, Throwable ex) {
        super(msg, ex);
    }

}
