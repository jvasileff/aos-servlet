package org.anodyneos.servlet.email;

/*
    IDEAS:

        -   Factory to produce handlers for action elements by element name.
            So, factory will return a handler suitable for processing "email"
            elements.  This would allow pluggable handlers for expansion.

        -   Create convension for parameter passing to handlers (parameters are
            http, cookie, deployment configs, etc).  Use namespace, key, and
            value.  Multiple values for each key and ordering must be
            preserved.  This would abstract away the servlet tier.  Is this too
            much?

        -   Available Data
                per request:
                    http parameters     ServletRequest.getParameter()       request-param:KEY
                    http headers        HttpServletRequest.getHeader()      request-header:KEY
                    request attributes  ServletRequest.getAttribute()       request-attr:KEY
                    cookies             ServletRequest                      cookie:KEY
                per servlet:
                    servlet parameters  ServletConfig.getInitParameter()    servlet-param:KEY
                per webapp:
                    context attributes  ServletContext.getAttribute()       context-attr:KEY
                    context parameters  ServletContext.getInitParameter()   context-param:KEY

        -   Available Data
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePart;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.anodyneos.commons.net.ClassLoaderURIHandler;
import org.anodyneos.commons.net.URI;
import org.anodyneos.commons.xml.UnifiedResolver;
import org.anodyneos.commons.xml.xsl.TemplatesCache;
import org.anodyneos.servlet.net.ServletContextURIHandler;
import org.anodyneos.servlet.xsl.GenericErrorHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class EmailServlet extends javax.servlet.http.HttpServlet {

    private static final String CLEAR_CACHE = "clearCache";
    private static final String CACHE_SIZE = "cacheSize";
    private static final String DISABLE_CACHE = "disableCache";
    private static final String ENABLE_CACHE = "enableCache";
    private static final String HELP = "help";
    private static final String PARAM_OP = "op";

    private static final String IP_TEMPLATE_RESOLVER = "template.resolver";
    private static final String IP_TEMPLATE_EXTERNAL = "template.externalLookups";
    private static final String IP_TEMPLATE_CACHE = "template.cache";
    private static final String IP_TRANSFORMER_RESOLVER = "transformer.resolver";
    private static final String IP_TRANSFORMER_EXTERNAL = "transformer.externalLookups";
    private static final String IP_PARSER_VALIDATION = "parser.validation";

    private static final String IP_CLASS_LOADER = "ClassLoader";
    private static final String IP_SERVLET_CONTEXT = "ServletContext";
    private static final String IP_TRUE = "true";
    private static final String IP_FALSE = "false";

    private static final String HOST_SMTP = "HostSMTP";

    private TemplatesCache templatesCache;
    private UnifiedResolver resolver;
    private javax.xml.parsers.DocumentBuilder docBuilder;
    private String smtpHost;

    /**
     * The attribute name in the &lt;?xml-stylesheet&gt; tag used in
     * stylesheet selection.
     */
    protected static final String STYLESHEET_ATTRIBUTE = "title";

    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);

        boolean external;
        ServletContext ctx = servletConfig.getServletContext();
        ClassLoader classLoader = this.getClass().getClassLoader();
        resolver = new UnifiedResolver();

        // Setup smtp
        smtpHost = servletConfig.getInitParameter(HOST_SMTP);

        // Setup resolver
        resolver.setDefaultLookupEnabled(false);
        resolver.addProtocolHandler("classloader",
                new ClassLoaderURIHandler(this.getClass().getClassLoader()));
        resolver.addProtocolHandler("webapp",
                new ServletContextURIHandler(servletConfig.getServletContext()));

        // Setup templatesCache
        templatesCache = new TemplatesCache(resolver);
        if (IP_FALSE.equals(servletConfig.getInitParameter(IP_TEMPLATE_CACHE))) {
            templatesCache.setCacheEnabled(false);
        } else {
            templatesCache.setCacheEnabled(true);
        }

        // Setup documentBuilder (for xml input)
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            docBuilderFactory.setValidating(false);
            docBuilder = docBuilderFactory.newDocumentBuilder();
            docBuilder.setErrorHandler(new GenericErrorHandler());
            docBuilder.setEntityResolver(resolver);
        } catch (javax.xml.parsers.ParserConfigurationException e) {
            throw new ServletException(e);
        }
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws
            ServletException, IOException {
        doGet(req, res);
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws
            ServletException, IOException {

        Params params = new Params(this, req);

        try {

            /* read/parse XML config file */

            URI configURI = new URI("webapp:///WEB-INF/email/" + req.getPathInfo());
            Document doc = docBuilder.parse(resolver.resolveEntity("", configURI.toString()));
            String redir = processDoc(configURI, doc, params);
            if (redir != null) {
                res.sendRedirect(redir);
            } else {
                params.addScopeElements(doc.getDocumentElement());

                String rawXMLMimeType = "text/xml";
                if ( "xml".equals(req.getParameter("rawXMLMime"))) {
                    rawXMLMimeType = "text/xml";
                } else if ( "plain".equals(req.getParameter("rawXMLMime"))) {
                    rawXMLMimeType = "text/plain";
                }
                res.setContentType(rawXMLMimeType);
                PrintWriter out = res.getWriter();
                Transformer transformer = templatesCache.getTransformer();
                transformer.setURIResolver(resolver);
                transformer.transform(new DOMSource(doc, "webapp://" + req.getServletPath()),
                                new StreamResult(out));
                out.close();
            }

        } catch (Exception e) {
            if (!res.isCommitted()) {
                res.reset();
                res.setContentType("text/html");
            }
            java.io.PrintWriter eout = res.getWriter();
            eout.println("<h1>Exception Thrown</h1>");
            eout.println("<p>");
            eout.println(e.toString());
            eout.println("<p>");
            eout.println("<h1>Stack Trace</h1>");
            eout.println("\n<pre>");
            e.printStackTrace(eout);
            eout.println("</pre>");
        }

    }

    public String processDoc(URI configURI, Document doc, Params params) throws Exception {
        Element config = doc.getDocumentElement();
        NodeList nl = config.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals("email")) {
                processEmail(configURI, (Element) n, params);
            }
        }

        // find redirect
        Element successRedirect = getFirstElement(config, "successRedirect");
        if (successRedirect != null) {
            return successRedirect.getAttribute("url");
        } else {
            return null;
        }
    }

    public void processEmail(URI configURI, Element email, Params params) throws Exception {
        List to = new ArrayList();
        List cc = new ArrayList();
        List bcc = new ArrayList();
        List from = new ArrayList();

        // mail variables
        Properties props;

        // create some properties and get the default Session
        props = System.getProperties();
        props.put("mail.smtp.host", smtpHost);
        Session session = Session.getDefaultInstance(props, null);
        session.setDebug(false);

        // create a mime message
        MimeMessage message = new MimeMessage(session);
        message.setSentDate(new java.util.Date());

        NodeList nl = email.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) n;
                if ("to".equals(el.getNodeName())) {
                    to.add(getAddress(el, params));
                } else if ("cc".equals(el.getNodeName())) {
                    cc.add(getAddress(el, params));
                } else if ("bcc".equals(el.getNodeName())) {
                    bcc.add(getAddress(el, params));
                } else if ("from".equals(el.getNodeName())) {
                    from.add(getAddress(el, params));
                } else if ("subject".equals(el.getNodeName())) {
                    message.setSubject(params.parse(getText(el)));
                } else if ("part".equals(el.getNodeName())) {
                    processPart(configURI, message, el, params);
                } else if ("multipart".equals(el.getNodeName())) {
                    processMultipart(configURI, message, el, params);
                }
            }

        }
        if (to.size() > 0) {
            message.setRecipients(Message.RecipientType.TO, (InternetAddress[])to.toArray(new InternetAddress[to.size()]));
        }
        if (cc.size() > 0) {
            message.setRecipients(Message.RecipientType.CC, (InternetAddress[])cc.toArray(new InternetAddress[cc.size()]));
        }
        if (bcc.size() > 0) {
            message.setRecipients(Message.RecipientType.BCC, (InternetAddress[])bcc.toArray(new InternetAddress[bcc.size()]));
        }
        if (from.size() > 0) {
            message.addFrom((InternetAddress[])from.toArray(new InternetAddress[from.size()]));
        }
        message.saveChanges();
        //System.out.println("EMAIL MESSAGE___________________________________");
        //message.writeTo(System.out);
        Transport.send(message);
    }

    public static String getText(final Node n) {
        StringBuffer sb = new StringBuffer();
        NodeList nl = n.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n2 = nl.item(i);
            if (n2.getNodeType() == Node.TEXT_NODE || n2.getNodeType() == Node.CDATA_SECTION_NODE) {
                sb.append(n2.getNodeValue());
            } else if (n2.getNodeType() == Node.ELEMENT_NODE) {
                sb.append(getText(n2));
            }
        }
        return sb.toString();
    }

    public Address getAddress(Element el, Params params) throws ServletException,
            java.io.UnsupportedEncodingException{

        String address = params.parse(el.getAttribute("address"));
        if (! Util.isValidEmailFormat(address)) {
            address = "";
        }
        String name = params.parse(el.getAttribute("name"));
        return new InternetAddress(address, name);
    }

    public Element findChildElement(Element el, String name) {
        NodeList nl = el.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals(name)) {
                return (Element) n;
            }
        }
        return null;
    }

    public void processPart(URI configURI, MimePart part, Element el, Params params)
    throws MessagingException, ServletException, URI.MalformedURIException {
        String mimeType = el.getAttribute("mimeType").trim();
        String fileName = el.getAttribute("fileName").trim();

        // will have one of (content, xslResultContent, fileContent)
        Element content = findChildElement(el, "content");
        Element fileContent = findChildElement(el, "fileContent");
        Element substResultContent = findChildElement(el, "substResultContent");
        Element xslResultContent = findChildElement(el, "xslResultContent");
        if (content != null) {
            String charset = content.getAttribute("charset").trim();
            StringDataSource ds = new StringDataSource(params.parse(getText(content)));
            if (charset.length() != 0) {
                ds.setCharset(charset);
            }
            if (mimeType.length() != 0) {
                ds.setMimeType(mimeType);
            }
            if(fileName.length() != 0) {
                ds.setName(fileName);
                part.setFileName(fileName);
            }
            part.setDataHandler(new DataHandler(ds));
        } else if (substResultContent != null) {
            String path = substResultContent.getAttribute("path").trim();
            String charset = substResultContent.getAttribute("charset").trim();
            if (charset.length() == 0) {
                charset = null;
            }
            String text;
            // if path is null, use text content of element
            if (path.length() == 0) {
                text = params.parse(getText(substResultContent));
            } else {
                URI uri = new URI(configURI, path);
                try {
                    InputStream is = resolver.openStream(uri);
                    StringWriter sw = new StringWriter();
                    byte[] buff = new byte[1024];
                    for (int numRead = is.read(buff); numRead != -1; numRead = is.read(buff)) {
                        if (charset != null) {
                            sw.write(new String(buff, 0, numRead, charset));
                        } else {
                            sw.write(new String(buff, 0, numRead));
                        }
                    }
                    sw.flush();
                    text = params.parse(sw.toString());
                } catch (java.io.IOException e) {
                    throw new ServletException(e);
                }
            }
            StringDataSource ds = new StringDataSource(text);
            if (charset != null) {
                ds.setCharset(charset);
            }
            if (mimeType.length() != 0) {
                ds.setMimeType(mimeType);
            }
            if(fileName.length() != 0) {
                ds.setName(fileName);
                part.setFileName(fileName);
            }
            part.setDataHandler(new DataHandler(ds));
        } else if (fileContent != null) {
            // TODO: Make secure
            String charset = fileContent.getAttribute("charset").trim();
            String path = fileContent.getAttribute("path").trim();
            URI uri = new URI(configURI, path);
            URIDataSource ds = new URIDataSource(uri, resolver);
            if (charset.length() != 0) {
                ds.setCharset(charset);
            }
            if (mimeType.length() != 0) {
                ds.setMimeType(mimeType);
            }
            if(fileName.length() != 0) {
                ds.setName(fileName);
                part.setFileName(fileName);
            }
            part.setDataHandler(new DataHandler(ds));
        } else if (xslResultContent != null) {
            String charset = xslResultContent.getAttribute("charset").trim();
            String path = xslResultContent.getAttribute("path").trim();
            URI uri = new URI(configURI, path);
            XSLDataSource ds = new XSLDataSource(templatesCache, uri, params);
            if (charset.length() != 0) {
                ds.setCharset(charset);
            }
            if (mimeType.length() != 0) {
                ds.setMimeType(mimeType);
            }
            if(fileName.length() != 0) {
                ds.setName(fileName);
                part.setFileName(fileName);
            }
            part.setDataHandler(new DataHandler(ds));
        }
    }

    /**
     *  @param part The <code>MimePart</code> that will hold the
     *  <code>MimeMultipart</code> content.
     */
    public void processMultipart(URI configURI, MimePart part, Element multipartEl, Params params)
    throws MessagingException, ServletException, URI.MalformedURIException {
        MimeMultipart multipart = new MimeMultipart();
        String subType = multipartEl.getAttribute("subType").trim();
        if (subType.length() > 0) {
            multipart.setSubType(subType);
        }

        NodeList nl = multipartEl.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) n;
                if ("part".equals(el.getNodeName())) {
                    MimeBodyPart p = new MimeBodyPart();
                    processPart(configURI, p, el, params);
                    multipart.addBodyPart(p);
                } else if ("multipart".equals(el.getNodeName())) {
                    MimeBodyPart p = new MimeBodyPart();
                    processMultipart(configURI, p, el, params);
                    multipart.addBodyPart(p);
                }
            }
        }
        part.setContent(multipart);
    }

    public Element getFirstElement(Element el, String name) {
        NodeList nl = el.getElementsByTagName(name);
        if (nl.getLength() == 0) {
            return null;
        } else {
            return (Element) nl.item(0);
        }
    }
}

