package org.anodyneos.servlet.xsl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.anodyneos.commons.net.ClassLoaderURIHandler;
import org.anodyneos.commons.net.URI;
import org.anodyneos.commons.xml.UnifiedResolver;
import org.anodyneos.commons.xml.xsl.TemplatesCache;
import org.anodyneos.servlet.net.ServletContextURIHandler;
import org.anodyneos.servlet.util.BrowserDetector;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * Provieds server side xslt transformations with stylesheet caching. The
 * stylesheet (text/xsl) is discovered from the source document. Stylesheets
 * are cached based on the timestamp. Simply update the timestamp of the
 * stylesheet to force a refresh.
 *
 * TODO: Check for threading issues, pool docBuilders?
 *
 * @author John Vasileff
 * @version 1.0, 8/31/2000
 */
public class XSLTServlet extends HttpServlet {

    private static final String DEFAULT_SERVLET = "fileServlet";
    private static final String CLEAR_CACHE = "clearCache";
    private static final String CACHE_SIZE = "cacheSize";
    private static final String DISABLE_CACHE = "disableCache";
    private static final String ENABLE_CACHE = "enableCache";
    private static final String DISABLE_CLIENT_CACHE = "disableClientCache";
    private static final String ENABLE_CLIENT_CACHE = "enableClientCache";
    private static final String DISABLE_OUTPUT_VALIDATION = "disableOutputValidation";
    private static final String ENABLE_OUTPUT_VALIDATION = "enableOutputValidation";
    private static final String HELP = "help";
    private static final String PARAM_OP = "op";

    private static final String IP_RESOLVER_EXTERNAL = "resolver.externalLookups";
    private static final String IP_RESOLVER_USE_CATALOG = "resolver.useCatalog";
    private static final String IP_TEMPLATE_CACHE = "template.cache";
    private static final String IP_CLIENT_CACHE = "client.cache";
    private static final String IP_INPUT_VALIDATION = "input.validation";
    private static final String IP_OUTPUT_VALIDATION = "output.validation";
    private static final String IP_XHTML_MAGIC = "xhtml.magic";
    private static final String IP_LOG_WARNINGS = "log.warnings";
    private static final String IP_TRUE = "true";
    private static final String IP_FALSE = "false";

    private TemplatesCache templatesCache;
    private UnifiedResolver resolver;
    private DocumentBuilderFactory docBuilderFactory;
    private SAXParserFactory validatingSPF;

    private boolean outputValidation = false;
    private boolean xhtmlMagic = true;
    private boolean logWarnings = true;
    private boolean clientCache = true;

    /**
     * The attribute name in the &lt;?xml-stylesheet&gt; tag used in stylesheet
     * selection.
     */
    protected static final String STYLESHEET_ATTRIBUTE = "title";

    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);

        boolean external;
        ServletContext ctx = servletConfig.getServletContext();
        ClassLoader classLoader = this.getClass().getClassLoader();
        resolver = new UnifiedResolver();

        // Setup preferences
        if (IP_TRUE.equalsIgnoreCase(servletConfig.getInitParameter(IP_OUTPUT_VALIDATION))) {
            outputValidation = true;
        } else if (IP_FALSE.equalsIgnoreCase(servletConfig.getInitParameter(IP_OUTPUT_VALIDATION))) {
            outputValidation = false;
        }

        if (IP_TRUE.equalsIgnoreCase(servletConfig.getInitParameter(IP_XHTML_MAGIC))) {
            xhtmlMagic = true;
        } else if (IP_FALSE.equalsIgnoreCase(servletConfig.getInitParameter(IP_XHTML_MAGIC))) {
            xhtmlMagic = false;
        }

        if (IP_TRUE.equalsIgnoreCase(servletConfig.getInitParameter(IP_LOG_WARNINGS))) {
            logWarnings = true;
        } else if (IP_FALSE.equalsIgnoreCase(servletConfig.getInitParameter(IP_LOG_WARNINGS))) {
            logWarnings = false;
        }

        // Setup resolver
        if (IP_TRUE.equalsIgnoreCase(servletConfig.getInitParameter(IP_RESOLVER_EXTERNAL))) {
            resolver.setDefaultLookupEnabled(true);
        } else {
            resolver.setDefaultLookupEnabled(false);
        }

        if (IP_TRUE.equalsIgnoreCase(servletConfig.getInitParameter(IP_RESOLVER_USE_CATALOG))) {
            resolver.setXMLCatalogEnabled(true);
        } else {
            resolver.setXMLCatalogEnabled(false);
        }

        resolver.addProtocolHandler("classloader", new ClassLoaderURIHandler(this.getClass()
                .getClassLoader()));
        resolver.addProtocolHandler("webapp", new ServletContextURIHandler(servletConfig
                .getServletContext()));

        // Setup templatesCache
        templatesCache = new TemplatesCache(resolver);
        if (IP_FALSE.equals(servletConfig.getInitParameter(IP_TEMPLATE_CACHE))) {
            templatesCache.setCacheEnabled(false);
        } else {
            templatesCache.setCacheEnabled(true);
        }

        // Setup clientCache
        if (IP_FALSE.equals(servletConfig.getInitParameter(IP_CLIENT_CACHE))) {
            clientCache = false;
        } else {
            clientCache = true;
        }

        // Setup documentBuilderFactory (for xml input)
        docBuilderFactory = DocumentBuilderFactory.newInstance();
        docBuilderFactory.setNamespaceAware(true);
        if (IP_FALSE.equals(servletConfig.getInitParameter(IP_INPUT_VALIDATION))) {
            docBuilderFactory.setValidating(false);
        } else {
            docBuilderFactory.setValidating(true);
        }

        // Setup SAXParserFactory for use when validating output
        validatingSPF = SAXParserFactory.newInstance();
        validatingSPF.setValidating(true);
        validatingSPF.setNamespaceAware(true);
        try {
            validatingSPF.setFeature("http://apache.org/xml/features/validation/schema", true);
        } catch (ParserConfigurationException e) {
            getServletContext().log(
                    "non-fatal error: cannot set feature"
                            + " 'http://apache.org/xml/features/validation/schema'"
                            + " on the validating SAXParserFactory.");
        } catch (SAXNotRecognizedException e) {
            getServletContext().log(
                    "non-fatal error: cannot set feature"
                            + " 'http://apache.org/xml/features/validation/schema'"
                            + " on the validating SAXParserFactory.");
        } catch (SAXNotSupportedException e) {
            getServletContext().log(
                    "non-fatal error: cannot set feature"
                            + " 'http://apache.org/xml/features/validation/schema'"
                            + " on the validating SAXParserFactory.");
        }
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException,
            IOException {

        PrintWriter out;

        //res.setBufferSize(100 * 1024);

        if (CLEAR_CACHE.equals(req.getParameter(PARAM_OP))) {
            templatesCache.clearCache();
            //System.out.println("[XSLTServet] Cache Cleared.");
            res.setContentType("text/html");
            out = res.getWriter();
            out.println("<HTML><PRE>");
            out.println("Cache cleared.");
            out.println("</PRE></HTML>");
        } else if (DISABLE_CACHE.equals(req.getParameter(PARAM_OP))) {
            templatesCache.setCacheEnabled(false);
            //System.out.println("[XSLTServlet] Cache Disabled.");
            out = res.getWriter();
            res.setContentType("text/html");
            out.println("<HTML><PRE>");
            out.println("Cache disabled.");
            out.println("</PRE></HTML>");
        } else if (ENABLE_CACHE.equals(req.getParameter(PARAM_OP))) {
            templatesCache.setCacheEnabled(true);
            //System.out.println("[XSLTServlet] Cache Enabled.");
            out = res.getWriter();
            res.setContentType("text/html");
            out.println("<HTML><PRE>");
            out.println("Cache enabled.");
            out.println("</PRE></HTML>");
        } else if (DISABLE_CLIENT_CACHE.equals(req.getParameter(PARAM_OP))) {
            clientCache = false;
            out = res.getWriter();
            res.setContentType("text/html");
            out.println("<HTML><PRE>");
            out.println("Client Cache disabled.");
            out.println("</PRE></HTML>");
        } else if (ENABLE_CLIENT_CACHE.equals(req.getParameter(PARAM_OP))) {
            clientCache = true;
            out = res.getWriter();
            res.setContentType("text/html");
            out.println("<HTML><PRE>");
            out.println("Client Cache enabled.");
            out.println("</PRE></HTML>");
        } else if (DISABLE_OUTPUT_VALIDATION.equals(req.getParameter(PARAM_OP))) {
            outputValidation = false;
            out = res.getWriter();
            res.setContentType("text/html");
            out.println("<HTML><PRE>");
            out.println("Output Validation disabled.");
            out.println("</PRE></HTML>");
        } else if (ENABLE_OUTPUT_VALIDATION.equals(req.getParameter(PARAM_OP))) {
            outputValidation = true;
            out = res.getWriter();
            res.setContentType("text/html");
            out.println("<HTML><PRE>");
            out.println("Output Validation enabled.");
            out.println("</PRE></HTML>");
        } else if (HELP.equals(req.getParameter(PARAM_OP))) {
            int cacheSize = templatesCache.getCacheSize();
            boolean enableCache = templatesCache.getCacheEnabled();
            //System.out.println("[XSLTServlet]\t\tHelp operation.");
            out = res.getWriter();
            res.setContentType("text/html");
            out.println("<HTML><PRE>");
            out.println("Current cache size: " + cacheSize);
            if (enableCache) {
                out.println("Cache currently enabled.");
            } else {
                out.println("Cache currently disabled.");
            }
            if (clientCache) {
                out.println("Client Cache currently enabled.");
            } else {
                out.println("Client Cache currently disabled.");
            }
            if (outputValidation) {
                out.println("Output Validation currently enabled.");
            } else {
                out.println("Output Validation currently disabled.");
            }
            out.println();
            out.println("op=" + CLEAR_CACHE + " to clear the cache");
            out.println("op=" + DISABLE_CACHE + " to disable the cache");
            out.println("op=" + ENABLE_CACHE + " to enable the cache");
            out.println("op=" + DISABLE_CLIENT_CACHE + " to disable the client cache");
            out.println("op=" + ENABLE_CLIENT_CACHE + " to enable the client cache");
            out.println("op=" + DISABLE_OUTPUT_VALIDATION + " to enable the cache");
            out.println("op=" + ENABLE_OUTPUT_VALIDATION + " to enable the cache");
            out.println("op=" + HELP + " for this help screen");
            out.println("</PRE></HTML>");
        } else {
            try {
                // clientCache
                if(clientCache) {
                    // either (1)
                    URL url = getServletContext().getResource(req.getServletPath());
                    URLConnection conn = url.openConnection();
                    long lastModified = conn.getLastModified();
                    if(lastModified != 0) {
                        if(req.getDateHeader("If-Modified-Since") < lastModified) {
                            // adding 1 second since client rounds to the second.
                            res.setDateHeader("Last-Modified", lastModified + 1000);
                        } else {
                            res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                            return;
                        }
                    } else {
                        // using compressed war?, assume client has most recent file
                        if(req.getDateHeader("If-Modified-Since") > 0) {
                            res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                            return;
                        } else {
                            res.setDateHeader("Last-Modified", System.currentTimeMillis());
                        }
                    }
                }

                // get xml source
                InputStream inputStream = getServletContext().getResourceAsStream(
                        req.getServletPath());

                // 404 not found ?
                if (inputStream == null) {
                    res.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }

                DocumentBuilder docBuilder = newDocBuilder();
                Document doc = docBuilder.parse(inputStream, "webapp://" + req.getServletPath());

                // rawXML ?
                if ("true".equalsIgnoreCase(req.getParameter("rawXML"))) {
                    serveRawXML(req, res, doc);
                    return;
                }

                // locate stylesheet and get processor
                String xslPath = getXSLURLfromDoc(doc, STYLESHEET_ATTRIBUTE, "temporaryval");

                // no xsl ?
                if (xslPath == null) {
                    // serve directly
                    InputStream ins = getServletContext().getResourceAsStream(req.getServletPath());
                    serveRaw(ins, res);
                    return;
                }

                // serve translated
                serveTranslated(req, res, doc, xslPath);

            } catch (Exception e) {
                if (!res.isCommitted()) {
                    res.reset();
                    res.setContentType("text/html");
                }
                java.io.PrintWriter eout = res.getWriter();
                eout.println("<H1>Exception Thrown</H1>");
                eout.println("<p>");
                eout.println(e.toString());
                eout.println("<p>");
                eout.println("<H1>Stack Trace</H1>");
                eout.println("\n<pre>");
                e.printStackTrace(eout);
                eout.println("</pre>");
            }
        }
    }

    private void serveRawXML(HttpServletRequest req, HttpServletResponse res, Document doc)
            throws IOException, TransformerException {
        String rawXMLMimeType = "text/plain; charset=UTF-8";
        if ("xml".equals(req.getParameter("rawXMLMime"))) {
            rawXMLMimeType = "text/xml; charset=UTF-8";
        } else if ("plain".equals(req.getParameter("rawXMLMime"))) {
            rawXMLMimeType = "text/plain; charset=UTF-8";
        }
        res.setContentType(rawXMLMimeType);
        PrintWriter out = res.getWriter();
        Transformer transformer = templatesCache.getTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setURIResolver(resolver);
        transformer.transform(new DOMSource(doc, "webapp://" + req.getServletPath()),
                new StreamResult(out));
        out.close();
    }

    private void serveRaw(InputStream in, HttpServletResponse res) throws IOException {
        // Best would be to inspect XML for correct charset, but
        // platform default is easier!
        // http://www.w3.org/TR/REC-xml/#NT-XMLDecl
        Reader reader = new InputStreamReader(in);
        res.setContentType("text/xml; charset=UTF-8");
        PrintWriter out = res.getWriter();
        int n;
        char[] chars = new char[1024];
        while (-1 != (n = reader.read(chars, 0, 1024))) {
            out.write(chars, 0, n);
        }
        out.close();
    }

    private void serveTranslated(HttpServletRequest req, HttpServletResponse res, Document doc,
            String xslPath) throws URI.MalformedURIException, IOException,
            TransformerConfigurationException, TransformerException, SAXException,
            ParserConfigurationException {

        org.anodyneos.commons.net.URI xslURI;
        xslURI = new org.anodyneos.commons.net.URI(new org.anodyneos.commons.net.URI("webapp://"
                + req.getServletPath()), xslPath);
        Transformer transformer = templatesCache.getTransformer(xslURI);

        String method = transformer.getOutputProperty(OutputKeys.METHOD);
        String mediaType = transformer.getOutputProperty(OutputKeys.MEDIA_TYPE);

        boolean doXHTMLMagic = false;

        if (xhtmlMagic && "xml".equalsIgnoreCase(method)
                && "application/xhtml+xml".equalsIgnoreCase(mediaType)) {
            doXHTMLMagic = true;
            // Don't do xhtmlMagic on Mozilla
            BrowserDetector bd = getBrowserDetector(req);
            if ("Mozilla".equalsIgnoreCase(bd.getBrowserName()) && bd.getBrowserVersion() >= 5) {
                doXHTMLMagic = false;
            }
        }

        // only perform output validation if output property is xml
        boolean doOutputValidation = false;
        if (outputValidation && !doXHTMLMagic && "xml".equalsIgnoreCase(method)) {
            doOutputValidation = true;
        } else if (outputValidation) {
            // asked for, but not recieved!
        }

        if (doXHTMLMagic) {
            serveTranslatedMagic(req, res, doc, transformer);
        } else {
            serveTranslated(req, res, doc, transformer, outputValidation);
        }
    }

    private void serveTranslated(HttpServletRequest req, HttpServletResponse res, Document doc,
            Transformer transformer, boolean doOutputValidation)
            throws TransformerConfigurationException, IOException, TransformerException,
            SAXException, ParserConfigurationException {

        // process and output to client
        String contentType = null;
        if ((contentType = getContentType(transformer)) != null) {
            res.setContentType(contentType);
        }
        PrintWriter out = res.getWriter();
        transformer.setURIResolver(resolver);
        String output = transformer.getOutputProperty(OutputKeys.METHOD);

        if (!doOutputValidation || !"xml".equalsIgnoreCase(output)) {
            transformer.transform(new DOMSource(doc, "webapp://" + req.getServletPath()),
                    new StreamResult(out));
            out.close();
        } else {
            // FOR VALIDATING OUTPUT, need to parse result, very
            // inefficient
            StringWriter result = new StringWriter();
            transformer.transform(new DOMSource(doc, "webapp://" + req.getServletPath()),
                    new StreamResult(result));

            SAXParser sp = validatingSPF.newSAXParser();
            XMLReader xmlReader = sp.getXMLReader();
            xmlReader.setEntityResolver(resolver);
            xmlReader.setErrorHandler(new GenericErrorHandler());
            xmlReader.parse(new InputSource(new StringReader(result.toString())));
            // copy result to out
            char[] chars = new char[1024];
            int n;
            Reader in = new StringReader(result.toString());
            while (-1 != (n = in.read(chars, 0, 1024))) {
                out.write(chars, 0, n);
            }
            out.close();
        }
    }

    private void serveTranslatedMagic(HttpServletRequest req, HttpServletResponse res,
            Document doc, Transformer transformer) throws IOException, TransformerException {

        // first is the XSL transformer (transformer)
        // then the namespace filter
        XMLFilterImpl nsFilter = new StripNamespaceFilter();
        //// then the logging filter
        //XMLFilterImpl logFilter = new LogNamespaceFilter();
        // finally an identity transformer handler to serialize
        TransformerHandler th = templatesCache.getTransformerHandler();

        // setup identity th
        th.getTransformer().setOutputProperties(transformer.getOutputProperties());
        th.getTransformer().setOutputProperty(OutputKeys.METHOD, "html");
        th.getTransformer().setOutputProperty(OutputKeys.MEDIA_TYPE, "text/html");

        // source for transformer
        DOMSource domSource = new DOMSource(doc, "webapp://" + req.getServletPath());

        // transformer outputs to nsFilter
        SAXResult saxResult = new SAXResult(nsFilter);
        //// nsFilter outputs to logging filter
        //nsFilter.setContentHandler(logFilter);
        //// logFilter outputs to th
        //logFilter.setContentHandler(th);

        // nsFilter outputs to th
        nsFilter.setContentHandler(th);

        // th outputs to browser
        PrintWriter out = res.getWriter();
        String contentType = null;
        if ((contentType = getContentType(th.getTransformer())) != null) {
            res.setContentType(contentType);
        }
        th.setResult(new StreamResult(out));

        // do it.
        transformer.transform(domSource, saxResult);

    }

    /**
     * Returns a string URL for the stylesheet associated with the specified
     * XML document. JVAS - Code taken from xalan sources -
     * DefaultApplyXSL.java. If multiple XSL stylesheets are associated with
     * the XML document, preference will be given to the stylesheet which
     * contains an attribute name/value pair that corresponds to the specified
     * attributeName and attributeValue. For example, a stylesheet may start
     * like this:
     *
     * <pre>
     *  &lt;?xml version &quot;1.0&quot; encoding=&quot;iso-8559-1&quot;?&gt;
     *  &lt;?xml-stylesheet alternate=&quot;yes&quot;
     *  title=&quot;compact&quot;
     *  href=&quot;small-extras.css&quot;
     *  type=&quot;text/css&quot;?&gt;
     *  &lt;?xml-stylesheet alternate=&quot;yes&quot;
     *  title=&quot;big print&quot;
     *  href=&quot;bigprint.css&quot;
     *  type=&quot;text/css&quot;?&gt;
     *  &lt;?xml-stylesheet href=&quot;common.css&quot;
     *  type=&quot;text/css&quot;?&gt;
     * </pre>
     *
     * Legal psuedo attributes for the xml-stylesheet pi are
     *
     * <pre>
     *  href CDATA #REQUIRED
     *  type CDATA #REQUIRED
     *  title CDATA #IMPLIED
     *  media CDATA #IMPLIED
     *  charset CDATA #IMPLIED
     *  alternate (yes|no) &quot;no&quot;
     * </pre>
     *
     * HTML 4.01 defines legal media types to be screen, tty, tv, projection,
     * handheld, print, braille, aural, and all. The HTML 4.01 equivelant
     * syntax (not supported by this method) is as follows:
     *
     * <pre>
     *  &lt;LINK rel=&quot;alternate stylesheet&quot;
     *  title=&quot;compact&quot;
     *  href=&quot;small-extras.css&quot;
     *  type=&quot;text/css&quot;&gt;
     *  &lt;LINK rel=&quot;alternate stylesheet&quot;
     *  title=&quot;big print&quot;
     *  href=&quot;bigprint.css&quot;
     *  type=&quot;text/css&quot;&gt;
     *  &lt;LINK rel=&quot;stylesheet&quot;
     *  href=&quot;common.css&quot;
     *  type=&quot;text/css&quot;&gt;
     * </pre>
     *
     * <p>
     * xsl-stylesheet specefication: <a
     * href="http://www.w3.org/TR/xml-stylesheet/">http://www.w3.org/TR/xml-stylesheet/
     * </a> <br />
     * HTML 4.01 specefication: <a
     * href="http://www.w3.org/TR/html401/">http://www.w3.org/TR/html401/ </a>
     * </p>
     */

    private static String getXSLURLfromDoc(org.w3c.dom.Document sourceTree, String attributeName,
            String attributeValue) {
        String tempURL = null;
        String returnURL = null;
        try {
            for (Node child = sourceTree.getFirstChild(); child != null; child = child
                    .getNextSibling()) {
                if (child.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
                    ProcessingInstruction pi = (ProcessingInstruction) child;
                    if (pi.getNodeName().equals("xml-stylesheet")) {
                        PIA pia = new PIA(pi);
                        if ("text/xsl".equals(pia.getAttribute("type"))) {
                            tempURL = pia.getAttribute("href");
                            String attribute = pia.getAttribute(attributeName);
                            if ((attribute != null) && (attribute.indexOf(attributeValue) > -1))
                                    return tempURL;
                            if (!"yes".equals(pia.getAttribute("alternate"))) returnURL = tempURL;
                        }
                    }
                } else if (child.getNodeType() == Node.ELEMENT_NODE) {
                    // no more PIs after first element
                    break;
                }
            }
        } catch (Exception saxExc) {
        }
        return returnURL;
    }

    private String getContentType(Transformer transformer) throws TransformerConfigurationException {
        java.util.Properties props;
        props = transformer.getOutputProperties();
        if (props == null) {
            return null;
        } else {
            String encoding = props.getProperty("encoding");
            String media = props.getProperty("media-type");
            if (encoding != null) {
                return media + "; charset=" + encoding;
            } else {
                return media;
            }
        }
    }

    private BrowserDetector getBrowserDetector(HttpServletRequest req) {
        BrowserDetector browserDetector;
        String userAgent = req.getHeader("User-Agent");
        if (userAgent == null) {
            userAgent = "";
        }
        browserDetector = new BrowserDetector(userAgent);
        return browserDetector;
    }

    private DocumentBuilder newDocBuilder() throws ParserConfigurationException {
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        docBuilder.setErrorHandler(new GenericErrorHandler());
        docBuilder.setEntityResolver(resolver);
        return docBuilder;
    }

}
