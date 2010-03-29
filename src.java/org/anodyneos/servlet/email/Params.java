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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
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

import org.anodyneos.servlet.multipart.MultipartFile;
import org.anodyneos.servlet.multipart.MultipartHttpServletRequest;
import org.anodyneos.servlet.util.HttpServletRequestAsMap;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Params {

    private static final Log log = LogFactory.getLog(Params.class);

    private HttpServlet servlet;
    private HttpServletRequest req;
    private javax.xml.parsers.DocumentBuilder docBuilder;
    private Map<String, Object> reqAsMap;
    private MultipartHttpServletRequest multipartReq;

    public static final char CSV_LS = '\n';

    /**
     *  <code>types</code> holds custom parameter types and values.  The key is
     *  a string for the type, the value is a java.util.Map holding all
     *  name/value pairs for the type where value is a String[].
     */
    private TreeMap<String, Map<String, List<String>>> types = new TreeMap<String, Map<String, List<String>>>();

    public static final String REQUEST_FILE = "reqFile";
    public static final String REQUEST_PARAM = "reqParam";
    public static final String REQUEST_HEADER = "reqHeader";
    public static final String REQUEST_ATTR = "reqAttr";
    public static final String COOKIE = "cookie";
    public static final String SERVLET_PARAM = "servletParam";
    public static final String CONTEXT_ATTR = "ctxAttr";
    public static final String CONTEXT_PARAM = "ctxParam";
    public static final String CGI = "CGI";

    public static final String FILE_CONTENT_TYPE = "contentType";
    public static final String FILE_SIZE = "size";
    public static final String FILE_ORIGINAL_FILENAME = "originalFilename";
    // Return a base64 encoded string representation of the file
    public static final String FILE_BASE64 = "base64";

    public static final char SEP_CHAR = ':';
    private static final String SEP_DOUBLE = "::";
    private static final String SEP_STRING = new String(new char[] {SEP_CHAR});

    public Params(HttpServlet servlet, HttpServletRequest req) {
        this.servlet = servlet;
        this.req = req;

        initType(REQUEST_PARAM);
        initType(REQUEST_HEADER);
        initType(REQUEST_ATTR);
        initType(SERVLET_PARAM);
        initType(CONTEXT_ATTR);
        initType(CONTEXT_PARAM);

        // CGI
        initType(CGI);
        reqAsMap = new HttpServletRequestAsMap(req);

        // COOKIE - store cookies into types
        initType(COOKIE);
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (int i = 0; i < cookies.length; i++) {
                addParameter(COOKIE, cookies[i].getName(), cookies[i].getValue());
            }
        }

        // REQUEST_FILE - create reference if files are possible
        initType(REQUEST_FILE);
        if (req instanceof MultipartHttpServletRequest) {
            this.multipartReq = (MultipartHttpServletRequest) req;
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
        if (REQUEST_FILE.equals(type)) {
            if (null == multipartReq) {
                return "";
            } else {
                String fileParamName = parseFileParamName(name);
                String fileParamAttr = parseFileParamAttr(name);
                MultipartFile mf = multipartReq.getFile(fileParamName);
                if (null == mf) {
                    return "";
                } else if (FILE_CONTENT_TYPE.equals(fileParamAttr)) {
                    return toStringValue(mf.getContentType());
                } else if (FILE_ORIGINAL_FILENAME.equals(fileParamAttr)) {
                    return toStringValue(mf.getOriginalFilename());
                } else if (FILE_SIZE.equals(fileParamAttr)) {
                    return Long.toString(mf.getSize());
                } else if (FILE_BASE64.equals(fileParamAttr)) {
                    try {
                        return new String(Base64.encodeBase64Chunked(mf.getBytes()));
                    } catch (IOException e) {
                        log.error("Unable to encode file <'" + fileParamName + "'> to base64: " + e.getMessage(), e);
                        return "";
                    }
                }
            }
        } else if (REQUEST_PARAM.equals(type)) {
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
            List<String> al = types.get(type).get(name);
            if (null != al) {
                return al.get(0);
            }
        }
        return "";
    }

    /**
     *  @return A String[] containing all values for the given parameter or
     *  an empty array if the parameter does not exist.
     */
    @SuppressWarnings("unchecked")
    public String[] getParameterValues(String type, String name) {
        if (REQUEST_FILE.equals(type)) {
            String val = getParameter(type, name);
            if(null == val || "".equals(val)) {
                return new String[] {};
            } else {
                return new String[] {val};
            }
        } else if (REQUEST_PARAM.equals(type)) {
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
            List<String> al = types.get(type).get(name);
            if (null != al) {
                return al.toArray(new String[al.size()]);
            }
        }
        return new String[] {};
    }

    /**
     *  @return A String[] containing all parameter names for the given type or
     *  <code>null</code> if the type does not exist.
     */
    @SuppressWarnings("unchecked")
    public String[] getParameterNames(String type) {
        if (REQUEST_FILE.equals(type)) {
            if (null == multipartReq) {
                return new String[] {};
            } else {
                List<String> list = new ArrayList<String>();
                for (Iterator<String> it = multipartReq.getFileNames(); it.hasNext();) {
                    String name = it.next();
                    list.add(name + "." + FILE_CONTENT_TYPE);
                    list.add(name + "." + FILE_ORIGINAL_FILENAME);
                    list.add(name + "." + FILE_SIZE);
                }
                return toStringArray(list);
            }
        } else if (REQUEST_PARAM.equals(type)) {
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
            Set<String> keySet = types.get(type).keySet();
            return (String[]) keySet.toArray(new String[keySet.size()]);
        }
        return new String[] {};
    }

    public MultipartFile getRequestFile(String name) {
        if (req instanceof MultipartHttpServletRequest) {
            return ((MultipartHttpServletRequest)req).getFile(name);
        } else {
            return null;
        }
    }

    /**
     *  @return A String[] containing the name of all types.
     */
    public String[] getTypeNames() {
        Set<String> keys = types.keySet();
        return keys.toArray(new String[keys.size()]);
    }

    protected String[] toStringArray(List<String> list) {
        if (list.size() == 0) {
            return new String[] {};
        } else {
            return list.toArray(new String[list.size()]);
        }
    }

    protected String[] toStringArray(Enumeration<String> e) {
        List<String> list = new ArrayList<String>();
        while (e.hasMoreElements()) {
            list.add(e.nextElement().toString());
        }
        if (0 == list.size()) {
            return new String[] {};
        } else {
            return list.toArray(new String[list.size()]);
        }
    }

    protected String[] toStringArray(String s) {
        if (null == s) {
            return new String[] {};
        } else {
            return new String[] {s};
        }
    }

    protected String[] toStringArray(Set<String> s) {
        if (null == s) {
            return new String[] {};
        } else {
            return s.toArray(new String[s.size()]);
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
        Map<String, List<String>> typeMap = new TreeMap<String, List<String>>();
        types.put(type, typeMap);
    }

    protected void addParameter(String type, String name, Object value) {
        // type: keys=paramNames, values=ArrayList of strings
        Map<String, List<String>> typeMap = (TreeMap<String, List<String>>) types.get(type);
        if (null == typeMap) {
            typeMap = new TreeMap<String, List<String>>();
            types.put(type, typeMap);
        }
        List<String> values = typeMap.get(name);
        if (null == values) {
            values = new ArrayList<String>();
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
        Set<String> args = new HashSet<String>();

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
            } else if (args.contains("htmlDouble")) {
                str = htmlDoubleEncode(str);
            } else if (args.contains("csv")) {
                str = csvEncode(str);
            }
            return str;
        }
    }

    public static String csvEncode(String str) {
        if (-1 == str.indexOf('"') &&
                -1 == str.indexOf(',') &&
                -1 == str.indexOf('\r') &&
                -1 == str.indexOf('\n')) {
                // If no quotes, cr, lf, or comma, don't escape:
                return str;
        } else {
            StringBuffer out = new StringBuffer();
            char[] cbuf = str.toCharArray();

            out.append('"');
            boolean lwnl = false;
            /*  find '\r' || '"'
                    output preceding chars
                    output '', '\n', or two '"' chars
                    reset cpOff;
            */
            int off = 0;
            int len = cbuf.length;
            int cpOff = off;
            for (int i = off; i < cbuf.length && i < off+len; i++) {
                char ch = cbuf[i];
                if (ch == '\r' || ch == '"') {
                    // output preceeding chars (if any)
                    int cpLen = i - cpOff;
                    if (cpLen > 0) {
                        out.append(cbuf, cpOff, cpLen);
                    }
                    // output for this char (if nec)
                    if (ch == '"') {
                        out.append("\"\"");
                    } else if (! lwnl) {
                        out.append(CSV_LS);
                    }
                    // don't include this char on next copy
                    cpOff = i+1;
                    lwnl = false;
                } else if (ch == '\n') {
                    lwnl = true;
                } else {
                    lwnl = false;
                }
            }
            // output rest of chars (if any)
            int cpLen = len + off - cpOff;
            if (cpLen > 0) {
                out.append(cbuf, cpOff, cpLen);
            }
            out.append('"');
            return out.toString();
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

    /**
     * This is a limited use function to convert non-ascii characters to their &#9999; equivelents, and to further
     * escape the ampersand such that the final result is "&amp;#9999;".
     *
     * @param str
     * @return
     */
    public static String htmlDoubleEncode(String str) {
        StringBuffer sb = new StringBuffer(str.length());
        for(int i=0; i < str.length(); i++) {
            char aChar = str.charAt(i);
            if ((aChar < 0x0020) || (aChar > 0x007e)) {
                sb.append('&');
                sb.append('#');
                sb.append((int)aChar);
                sb.append(';');
            } else {
                sb.append(aChar);
            }
        }
        return htmlEncode(sb.toString());
    }

    protected static String parseFileParamName(String str) {
        if (null == str) {
            return null;
        }
        int idx = str.lastIndexOf('.');
        if (-1 == idx) {
            return null;
        }
        return str.substring(0, idx);
    }

    protected static String parseFileParamAttr(String str) {
        if (null == str) {
            return null;
        }
        int idx = str.lastIndexOf('.');
        if (-1 == idx) {
            return null;
        }
        return str.substring(idx+1);
    }

}
