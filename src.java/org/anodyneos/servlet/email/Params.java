package org.anodyneos.servlet.email;

/*
                    http parameters     ServletRequest.getParameter()       request-param:KEY
                    http headers        HttpServletRequest.getHeader()      request-header:KEY
                    request attributes  ServletRequest.getAttribute()       request-attr:KEY
                    cookies             ServletRequest                      cookie:KEY
                per servlet:
                    servlet parameters  ServletConfig.getInitParameter()    servlet-param:KEY
                per webapp:
                    context attributes  ServletContext.getAttribute()       context-attr:KEY
                    context parameters  ServletContext.getInitParameter()   context-param:KEY
*/


import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Params {

    private HttpServlet servlet;
    private HttpServletRequest req;
    private javax.xml.parsers.DocumentBuilder docBuilder;
    private Map reqAsMap;

    /**
     *  <code>types</code> holds custom parameter types and values.  The key is
     *  a string for the type, the value is a java.util.Map holding all
     *  name/value pairs for the type where value is a String[].
     */
    private TreeMap types = new TreeMap();

    public static final String REQUEST_PARAM = "reqParam";
    public static final String REQUEST_HEADER = "reqHeader";
    public static final String REQUEST_ATTR = "reqAttr";
    public static final String COOKIE = "cookie";
    public static final String SERVLET_PARAM = "servletParam";
    public static final String CONTEXT_ATTR = "ctxAttr";
    public static final String CONTEXT_PARAM = "ctxParam";
    public static final String CGI = "CGI";

    public static final Set CGI_VARIABLES = new HashSet();

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


    public static final char SEP_CHAR = ':';
    private static final String SEP_DOUBLE = "::";
    private static final String SEP_STRING = new String(new char[] {SEP_CHAR});

    public Params(HttpServlet servlet, HttpServletRequest req) {
        this.servlet = servlet;
        this.req = req;

        initType(REQUEST_PARAM);
        initType(REQUEST_HEADER);
        initType(REQUEST_ATTR);
        initType(COOKIE);
        initType(SERVLET_PARAM);
        initType(CONTEXT_ATTR);
        initType(CONTEXT_PARAM);

        // CGI
        initType(CGI);
        reqAsMap = new HttpRequestAsMap(req);

        // store cookies into types
        initType(COOKIE);
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (int i = 0; i < cookies.length; i++) {
                addParameter(COOKIE, cookies[i].getName(), cookies[i].getValue());
            }
        }
    }

    /**
     *  @return The first value for the parameter or <code>""</code> if the
     *  parameter does not exist.
     *
     *  @throws ServletException if the key is not formatted correctly.
     */
    public String getParameter(String key) throws ServletException {
        Reference ref = new Reference(key);
        return ref.getValue();
    }

    public String[] getParameterValues(String key) throws ServletException {
        Reference ref = new Reference(key);
        return ref.getValues();
    }

    /**
     *  @return The first value for the parameter as a <code>String</code> or
     *  <code>""</code> if the parameter does not exist.
     */
    public String getParameter(String type, String name) {
        if (REQUEST_PARAM.equals(type)) {
            return toStringValue(req.getParameter(name));
        } else if (REQUEST_HEADER.equals(type)) {
            return toStringValue(req.getHeader(name));
        } else if (REQUEST_ATTR.equals(type)) {
            return toStringValue(req.getAttribute(name));
        } else if (SERVLET_PARAM.equals(type)) {
            return toStringValue(servlet.getInitParameter(name));
        } else if (CONTEXT_ATTR.equals(type)) {
            return toStringValue(servlet.getServletContext().getAttribute(name));
        } else if (CONTEXT_PARAM.equals(type)) {
            return toStringValue(servlet.getServletContext().getInitParameter(name));
        } else if (CGI.equals(type)) {
            return toStringValue(reqAsMap.get(name));
        } else if (types.containsKey(type)) {
            ArrayList al = (ArrayList) ((Map)types.get(type)).get(name);
            if (null != al) {
                return (String) al.get(0);
            }
        }
        return "";
    }

    /**
     *  @return A String[] containing all values for the given parameter or
     *  an empty array if the parameter does not exist.
     */
    public String[] getParameterValues(String type, String name) {
        if (REQUEST_PARAM.equals(type)) {
            return toStringArray(req.getParameterValues(name));
        } else if (REQUEST_HEADER.equals(type)) {
            return toStringArray(req.getHeaders(name));
        } else if (REQUEST_ATTR.equals(type)) {
            return toStringArray(req.getAttribute(name).toString());
        } else if (SERVLET_PARAM.equals(type)) {
            return toStringArray(servlet.getInitParameter(name));
        } else if (CONTEXT_ATTR.equals(type)) {
            return toStringArray(servlet.getServletContext().getAttribute(name));
        } else if (CONTEXT_PARAM.equals(type)) {
            return toStringArray(servlet.getServletContext().getInitParameter(name));
        } else if (CGI.equals(type)) {
            return toStringArray(reqAsMap.get(name));
        } else if (types.containsKey(type)) {
            ArrayList al = (ArrayList) ((Map)types.get(type)).get(name);
            if (null != al) {
                return (String[]) al.toArray(new String[al.size()]);
            }
        }
        return new String[] {};
    }

    /**
     *  @return A String[] containing all parameter names for the given type or
     *  <code>null</code> if the type does not exist.
     */
    public String[] getParameterNames(String type) {
        if (REQUEST_PARAM.equals(type)) {
            return toStringArray(req.getParameterNames());
        } else if (REQUEST_HEADER.equals(type)) {
            return toStringArray(req.getHeaderNames());
        } else if (REQUEST_ATTR.equals(type)) {
            return toStringArray(req.getAttributeNames());
        } else if (SERVLET_PARAM.equals(type)) {
            return toStringArray(servlet.getInitParameterNames());
        } else if (CONTEXT_ATTR.equals(type)) {
            return toStringArray(servlet.getServletContext().getAttributeNames());
        } else if (CONTEXT_PARAM.equals(type)) {
            return toStringArray(servlet.getServletContext().getInitParameterNames());
        } else if (CGI.equals(type)) {
            return toStringArray(reqAsMap.keySet());
        } else if (types.containsKey(type)) {
            Set keySet = ((Map)types.get(type)).keySet();
            return (String[]) keySet.toArray(new String[keySet.size()]);
        }
        return new String[] {};
    }

    /**
     *  @return A String[] containing the name of all types.
     */
    public String[] getTypeNames() {
        Set keys = types.keySet();
        return (String[]) keys.toArray(new String[keys.size()]);
    }


    protected String[] toStringArray(Enumeration e) {
        List list = new ArrayList();
        while (e.hasMoreElements()) {
            list.add(e.nextElement().toString());
        }
        if (0 == list.size()) {
            return new String[] {};
        } else {
            return (String[]) list.toArray(new String[list.size()]);
        }
    }

    protected String[] toStringArray(String s) {
        if (null == s) {
            return new String[] {};
        } else {
            return new String[] {s};
        }
    }

    protected String[] toStringArray(Set s) {
        if (null == s) {
            return new String[] {};
        } else {
            return (String[]) s.toArray(new String[s.size()]);
        }
    }

    protected String[] toStringArray(String[] s) {
        if (null == s) {
            return new String[] {};
        } else {
            return s;
        }
    }

    protected String[] toStringArray(Object o) {
        if (null == o) {
            return new String[] {};
        } else {
            return new String[] {o.toString()};
        }
    }

    protected String toStringValue(Object s) {
        if (null == s) {
            return "";
        } else {
            return s.toString();
        }
    }

    protected void initType(String type) {
        Map typeMap = new TreeMap();
        types.put(type, typeMap);
    }

    protected void addParameter(String type, String name, Object value) {
        // type: keys=paramNames, values=ArrayList of strings
        Map typeMap = (TreeMap) types.get(type);
        if (null == typeMap) {
            typeMap = new TreeMap();
            types.put(type, typeMap);
        }
        ArrayList values = (ArrayList) typeMap.get(name);
        if (null == values) {
            values = new ArrayList();
            typeMap.put(name, values);
        }
        values.add(value.toString());
    }

    /*
    public void addTypeElements(Element parent) throws DOMException {
        Document doc = parent.getOwnerDocument();
        String[] types = getTypeNames();
        for (int i = 0; i < types.length; i++) {
            Element el = doc.createElement(types[i]);
            parent.appendChild(el);
            addParamElements(el, types[i]);
        }
    }
    */
    public void addScopeElements(Element parent) throws DOMException {
        Document doc = parent.getOwnerDocument();
        String[] types = getTypeNames();
        for (int i = 0; i < types.length; i++) {
            Element el = doc.createElement("scope");
            el.setAttribute("name", types[i]);
            parent.appendChild(el);
            addParamElements(el, types[i]);
        }
    }

    /*
    public void addParamElements(Element parent, String type) throws DOMException {
        Document doc = parent.getOwnerDocument();
        String[] names = getParameterNames(type);
        for (int i = 0; i < names.length; i++) {
            String[] values = getParameterValues(type, names[i]);
            for (int j = 0; j < values.length; j++) {
                //Element el = doc.createElement("param");
                //el.setAttribute("name", names[i]);
                Element el = doc.createElement(names[i]);
                el.appendChild(doc.createTextNode(values[j]));
                parent.appendChild(el);
            }
        }
    }
    */
    public void addParamElements(Element parent, String type) throws DOMException {
        Document doc = parent.getOwnerDocument();
        String[] names = getParameterNames(type);
        for (int i = 0; i < names.length; i++) {
            String[] values = getParameterValues(type, names[i]);
            if (values.length == 0) {
                Element el = doc.createElement("param");
                el.setAttribute("name", names[i]);
                parent.appendChild(el);
            } else {
                for (int j = 0; j < values.length; j++) {
                    Element el = doc.createElement("param");
                    el.setAttribute("name", names[i]);
                    //Element el = doc.createElement(names[i]);
                    el.appendChild(doc.createTextNode(values[j]));
                    parent.appendChild(el);
                }
            }
        }
    }

    public Document getDocument() throws ServletException {

        if(docBuilder == null) {
            try {
                DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
                docBuilderFactory.setValidating(false);
                docBuilder = docBuilderFactory.newDocumentBuilder();
            } catch (javax.xml.parsers.ParserConfigurationException e) {
                throw new ServletException(e);
            }
        }

        Document doc = docBuilder.newDocument();
        Element params = doc.createElement("params");
        doc.appendChild(params);
        addScopeElements(params);

        return doc;
    }

    public String parse(String in) throws ServletException {
        if(in.indexOf('{') == -1) {
            return in;
        }
        StringBuffer sb = new StringBuffer();
        String ref = null;
        StringTokenizer st = new StringTokenizer(in, "{}", true);
        boolean lastWasLeftBrace = false;
        boolean inBrace = false;
        while (st.hasMoreTokens()) {
            String tok = st.nextToken();
            if (! inBrace) {
                if (tok.equals("{") && ! lastWasLeftBrace) {
                    lastWasLeftBrace = true;
                } else if (tok.equals("{")) {
                    lastWasLeftBrace = false;
                    sb.append("{");
                } else if (lastWasLeftBrace && tok.equals("}")) {
                    // ignore empty ref
                    lastWasLeftBrace = false;
                } else if (lastWasLeftBrace) {
                    lastWasLeftBrace = false;
                    inBrace = true;
                    ref = tok;
                } else {
                    sb.append(tok);
                }
            } else { // in brace
                if (tok.equals("{")) {
                    throw new ServletException("reference cannot contain '{' character.");
                } else if (tok.equals("}")) {
                    inBrace = false;
                    sb.append(getParameter(ref));
                    ref = null;
                } else {
                    throw new ServletException("Token is niether '{' nor '}'.  This should not happen.");
                }
            }
        }
        if (lastWasLeftBrace || inBrace) {
            throw new ServletException("missing '}'.");
        }
        return sb.toString();
    }

    protected class Reference {
        String scope;
        String name;
        Set args = new HashSet();

        protected Reference(String ref) {
            // TODO: handle invalid syntax
            // format is "scope:arg1:arg2:...:argn::name"
            int firstSep = ref.indexOf(SEP_CHAR);
            int firstDouble = ref.indexOf(SEP_DOUBLE);
            scope = ref.substring(0, firstSep);
            name = ref.substring(firstDouble + 2, ref.length());
            if (firstSep != firstDouble && firstSep != -1 && firstDouble != -1) {
                StringTokenizer st = new StringTokenizer(ref.substring(firstSep + 1, firstDouble), SEP_STRING);
                while (st.hasMoreTokens()) {
                    String tok = st.nextToken();
                    args.add(tok);
                }
            }
        }

        protected String getValue() {
            String val = getParameter(scope, name);
            return process(val);
        }

        protected String[] getValues() {
            String[] vals = getParameterValues(scope, name);
            for(int i = 0; i < vals.length; i++) {
                vals[i] = process(vals[i]);
            }
            return vals;
        }

        protected String process(String str) {
            if (args.contains("url")) {
                try {
                    str = java.net.URLEncoder.encode(str, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    // this should not happen
                    throw new Error(e.getMessage());
                }
            } else if (args.contains("html")) {
                str = htmlEncode(str);
            }
            return str;
        }
    }

    public static String htmlEncode(String str) {
        StringBuffer sb = new StringBuffer(str.length());
        StringTokenizer st = new StringTokenizer(str, "<>&'\"", true);
        while (st.hasMoreTokens()) {
            String tok = st.nextToken();
            if ("<".equals(tok)) {
                sb.append("&lt;");
            } else if (">".equals(tok)) {
                sb.append("&gt;");
            } else if ("&".equals(tok)) {
                sb.append("&amp;");
            } else if ("'".equals(tok)) {
                sb.append("&#039;");
            } else if ("\"".equals(tok)) {
                sb.append("&quot;");
            } else {
                sb.append(tok);
            }
        }
        return sb.toString();
    }

    public class HttpRequestAsMap implements Map {
        HttpServletRequest req;
        public HttpRequestAsMap(HttpServletRequest req) {
            this.req = req;
        }
        /* (non-Javadoc)
         * @see java.util.Map#size()
         */
        public int size() {
            return CGI_VARIABLES.size();
        }
        /* (non-Javadoc)
         * @see java.util.Map#clear()
         */
        public void clear() {
            throw new UnsupportedOperationException();
        }
        /* (non-Javadoc)
         * @see java.util.Map#isEmpty()
         */
        public boolean isEmpty() {
            return false;
        }
        /* (non-Javadoc)
         * @see java.util.Map#containsKey(java.lang.Object)
         */
        public boolean containsKey(Object key) {
            return CGI_VARIABLES.contains(key);
        }
        /* (non-Javadoc)
         * @see java.util.Map#containsValue(java.lang.Object)
         */
        public boolean containsValue(Object value) {
            throw new UnsupportedOperationException();
        }
        /* (non-Javadoc)
         * @see java.util.Map#values()
         */
        public Collection values() {
            throw new UnsupportedOperationException();
        }
        /* (non-Javadoc)
         * @see java.util.Map#putAll(java.util.Map)
         */
        public void putAll(Map t) {
            throw new UnsupportedOperationException();
        }
        /* (non-Javadoc)
         * @see java.util.Map#entrySet()
         */
        public Set entrySet() {
            throw new UnsupportedOperationException();
        }
        /* (non-Javadoc)
         * @see java.util.Map#keySet()
         */
        public Set keySet() {
            return CGI_VARIABLES;
        }
        /* (non-Javadoc)
         * @see java.util.Map#get(java.lang.Object)
         */
        public Object get(Object key) {
            if (! (key instanceof String)) {
                return null;
            }
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
            } else if (k.equals("REMOTE_HOST")) {
                return req.getRemoteHost();
            }
            return null;
        }
        /* (non-Javadoc)
         * @see java.util.Map#remove(java.lang.Object)
         */
        public Object remove(Object key) {
            throw new UnsupportedOperationException();
        }
        /* (non-Javadoc)
         * @see java.util.Map#put(java.lang.Object, java.lang.Object)
         */
        public Object put(Object key, Object value) {
            throw new UnsupportedOperationException();
        }
    }

}
