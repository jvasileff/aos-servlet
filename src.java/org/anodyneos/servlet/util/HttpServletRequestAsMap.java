/*
 * Created on Aug 17, 2004
 */
package org.anodyneos.servlet.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

/**
 * @author jvas
 */
public class HttpServletRequestAsMap implements Map<String, Object> {

    public static final Set<String> CGI_VARIABLES = new TreeSet<String>();

    private HttpServletRequest req;

    static {
        CGI_VARIABLES.add("AUTH_TYPE");
        CGI_VARIABLES.add("REQUEST_METHOD");
        CGI_VARIABLES.add("PATH_INFO");
        CGI_VARIABLES.add("PATH_TRANSLATED");
        CGI_VARIABLES.add("QUERY_STRING");
        CGI_VARIABLES.add("REMOTE_USER");
        CGI_VARIABLES.add("SCRIPT_NAME");
        CGI_VARIABLES.add("CONTENT_LENGTH");
        CGI_VARIABLES.add("CONTENT_TYPE");
        CGI_VARIABLES.add("SERVER_PROTOCOL");
        CGI_VARIABLES.add("SERVER_NAME");
        CGI_VARIABLES.add("SERVER_PORT");
        CGI_VARIABLES.add("REMOTE_ADDR");
        CGI_VARIABLES.add("REMOTE_HOST");
    }

    public HttpServletRequestAsMap(HttpServletRequest req) {
        this.req = req;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Map#size()
     */
    public int size() {
        return CGI_VARIABLES.size();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Map#clear()
     */
    public void clear() {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Map#isEmpty()
     */
    public boolean isEmpty() {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    public boolean containsKey(Object key) {
        return CGI_VARIABLES.contains(key);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Map#values()
     */
    public Collection<Object> values() {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Map#putAll(java.util.Map)
     */
    public void putAll(Map<? extends String, ? extends Object> t) {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Map#entrySet()
     */
    public Set<java.util.Map.Entry<String, Object>> entrySet() {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Map#keySet()
     */
    public Set<String> keySet() {
        return CGI_VARIABLES;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Map#get(java.lang.Object)
     */
    public Object get(Object key) {
        if (!(key instanceof String)) { return null; }
        String k = (String) key;

        if (k.equals("AUTH_TYPE")) {
            return req.getAuthType();
        } else if (k.equals("REQUEST_METHOD")) {
            return req.getMethod();
        } else if (k.equals("PATH_INFO")) {
            return req.getPathInfo();
        } else if (k.equals("PATH_TRANSLATED")) {
            return req.getPathTranslated();
        } else if (k.equals("QUERY_STRING")) {
            return req.getQueryString();
        } else if (k.equals("REMOTE_USER")) {
            return req.getRemoteUser();
        } else if (k.equals("SCRIPT_NAME")) {
            return req.getServletPath();
        } else if (k.equals("CONTENT_LENGTH")) {
            return new Integer(req.getContentLength());
        } else if (k.equals("CONTENT_TYPE")) {
            return req.getContentType();
        } else if (k.equals("SERVER_PROTOCOL")) {
            return req.getProtocol();
        } else if (k.equals("SERVER_NAME")) {
            return req.getServerName();
        } else if (k.equals("SERVER_PORT")) {
            return new Integer(req.getServerPort());
        } else if (k.equals("REMOTE_ADDR")) {
            return req.getRemoteAddr();
        } else if (k.equals("REMOTE_HOST")) { return req.getRemoteHost(); }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Map#remove(java.lang.Object)
     */
    public String remove(Object key) {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    public String put(String key, Object value) {
        throw new UnsupportedOperationException();
    }
}
