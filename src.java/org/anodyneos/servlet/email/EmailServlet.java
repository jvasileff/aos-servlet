package org.anodyneos.servlet.email;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.anodyneos.commons.net.ClassLoaderURIHandler;
import org.anodyneos.commons.net.URI;
import org.anodyneos.commons.xml.UnifiedResolver;
import org.anodyneos.commons.xml.xsl.TemplatesCache;
import org.anodyneos.servlet.multipart.MultipartHttpServletRequest;
import org.anodyneos.servlet.multipart.commons.CommonsMultipartResolver;
import org.anodyneos.servlet.net.ServletContextURIHandler;
import org.anodyneos.servlet.xsl.GenericErrorHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class EmailServlet extends javax.servlet.http.HttpServlet {

    protected static final Log logger = LogFactory.getLog(EmailServlet.class);

    private static final long serialVersionUID = 3544676161419817017L;

    private static final String IP_TEMPLATE_CACHE = "template.cache";
    private static final String IP_COMMAND_FACTORY = "aos.commandFactoryClass";

    /** if maxUploadSizeBytes is not set or invalid, default is 0 bytes */
    private static final String IP_MAX_UPLOAD = "maxUploadSizeBytes";

    private static final String IP_TMP_DIR = "tmpDir";

    private static final String TRUE = "true";
    private static final String FALSE = "false";

    private CommandFactory commandFactory;
    private UnifiedResolver resolver;
    private TemplatesCache templatesCache;

    private long ctxMaxUpload = 0L;

    private File tmpDir = null;

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

        // Setup commandFactory
        String clazz = servletConfig.getInitParameter(IP_COMMAND_FACTORY);
        if (clazz == null) {
            clazz = "org.anodyneos.servlet.email.CommandFactoryImpl";
        }
        commandFactory = (CommandFactory) Util.instantiateObject(EmailServlet.class, clazz);
        if (commandFactory == null) {
            throw new ServletException("Cannot instantiate command factory.");
        }

        // Setup resolver
        resolver.setDefaultLookupEnabled(false);
        resolver.addProtocolHandler("classloader",
                new ClassLoaderURIHandler(this.getClass().getClassLoader()));
        resolver.addProtocolHandler("webapp",
                new ServletContextURIHandler(servletConfig.getServletContext()));

        // Setup templatesCache
        templatesCache = new TemplatesCache(resolver);
        if (FALSE.equals(servletConfig.getInitParameter(IP_TEMPLATE_CACHE))) {
            templatesCache.setCacheEnabled(false);
        } else {
            templatesCache.setCacheEnabled(true);
        }

        // Setup ctxMaxUpload
        try {
            String maxUploadStr = servletConfig.getInitParameter(IP_MAX_UPLOAD);
            if (null != maxUploadStr) {
                ctxMaxUpload = Long.parseLong(maxUploadStr);
            }
        } catch(NumberFormatException e) {
            // do nothing, stick with 0L.
        }

        // Setup tmp directory
        String str =  servletConfig.getInitParameter(IP_TMP_DIR);
        if (null != str && str.length() > 0) {
            tmpDir = new File(str);
        }

    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws
            ServletException, IOException {
        doGet(req, res);
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws
            ServletException, IOException {

        Params params;
        URI configURI;
        Document configDoc;
        EmailContext ctx;

        CommonsMultipartResolver multipartResolver = null;

        // outer try block to make sure we cleanup uploaded files
        try {
            // Read configuration and setup context object
            try {
                //configURI = new URI("webapp:///WEB-INF/email/" + req.getPathInfo());
                configURI = new URI("webapp://" + req.getServletPath());
                configDoc = getDocumentBuilder().parse(
                        resolver.resolveEntity("", configURI.toString()));

                // allow the config file to specify request encoding prior to parsing get/post
                String forceRequestEncoding = null;
                try {
                    forceRequestEncoding =
                        configDoc.getDocumentElement().getAttribute("forceRequestCharset");
                    if(null != forceRequestEncoding && ! ("".equals(forceRequestEncoding))) {
                        req.setCharacterEncoding(forceRequestEncoding);
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            } catch (SAXException e) {
                throw new ServletException("Cannot parse config xml.", e);
            } catch (ParserConfigurationException e) {
                throw new ServletException("Cannot create DocumentBuilder.", e);
            }

            // setup multipart parser if required
            long maxUpload = 0;
            if (ctxMaxUpload > 0) {
                String maxUploadStr = configDoc.getDocumentElement().getAttribute("maxUploadSize").trim();
                long xemlMaxUpload = 0;
                try {
                    xemlMaxUpload = Long.parseLong(maxUploadStr);
                } catch(NumberFormatException e) {
                    // do nothing, stick with 0L.
                }
                maxUpload = Math.min(ctxMaxUpload, xemlMaxUpload);
            }

            try {
                multipartResolver = new CommonsMultipartResolver(getServletContext());
                if (multipartResolver.isMultipart(req)) {
                    multipartResolver.setMaxInMemorySize(10240); // 10KB
                    multipartResolver.setMaxUploadSize(maxUpload);
                    if (null != tmpDir) {
                        multipartResolver.setUploadTempDir(tmpDir);
                    }
                    req = multipartResolver.resolveMultipart(req);
                }

                params = new Params(this, req);
                ctx = new EmailContext(templatesCache, resolver, params,  configURI, configDoc);

                String redir = processDoc(ctx);
                if (redir != null) {
                    res.sendRedirect(redir);
                } else {
                    /* This stuff should be put into a debugging operation */
                    /*
                     * params.addScopeElements(configDoc.getDocumentElement());
                     *
                     * String rawXMLMimeType = "text/xml"; if (
                     * "xml".equals(req.getParameter("rawXMLMime"))) {
                     * rawXMLMimeType = "text/xml"; } else if (
                     * "plain".equals(req.getParameter("rawXMLMime"))) {
                     * rawXMLMimeType = "text/plain"; }
                     * res.setContentType(rawXMLMimeType); PrintWriter out =
                     * res.getWriter(); Transformer transformer =
                     * templatesCache.getTransformer();
                     * transformer.setURIResolver(resolver);
                     * transformer.transform(new DOMSource(configDoc, "webapp://" +
                     * req.getServletPath()), new StreamResult(out)); out.close();
                     */

                    res.setContentType("text/html");
                    PrintWriter out = res.getWriter();
                    out.println("<html><body>");
                    out.println("<center><h1>The email has been sent.</h1></center>");
                    out.println("</body></html>");
                }
            //} catch (MaxUploadSizeExceededException e) {
                //throw e;
            //} catch (MultipartException e) {
                //throw e;
            } catch (Exception e) {
                logger.error("Error sending email", e);
                String failureRedirect = null;
                try {
                    failureRedirect =
                        configDoc.getDocumentElement().getAttribute("failureRedirect");
                } catch (Throwable t) {
                    // noop
                }
                if (!res.isCommitted()) {
                    res.reset();
                    if (null != failureRedirect && failureRedirect.length() > 0) {
                        res.sendRedirect(failureRedirect);
                    } else {
                        res.setContentType("text/html");
                        printErrorMessage(res.getWriter(), e);
                    }
                }
            }
        } finally {
            if (null != multipartResolver && req instanceof MultipartHttpServletRequest) {
                multipartResolver.cleanupMultipart((MultipartHttpServletRequest) req);
            }
        }

    }

    public String processDoc(EmailContext ctx) throws Exception {
        Element config = ctx.getConfigDoc().getDocumentElement();
        NodeList nl = config.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) n;
                Command cmd = commandFactory.getHandlerFor(el);
                if (cmd != null) {
                    cmd.process(ctx, el);
                } else {
                    throw new ServletException("Cannot find command handler for element.");
                }
            }
        }

        // find redirect
        String successRedirect = config.getAttribute("successRedirect");
        if (! "".equals(successRedirect)) {
            return successRedirect;
        } else {
            return null;
        }
    }

    private DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        docBuilderFactory.setValidating(false);
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        docBuilder.setErrorHandler(new GenericErrorHandler());
        docBuilder.setEntityResolver(resolver);
        return docBuilder;
    }

    protected void printErrorMessage(PrintWriter eout, Exception e) {
        eout.println("<h1>Exception Thrown</h1>");
        eout.println("<p>");
        eout.println(e.toString());
        eout.println("<p>");
        eout.println("<h1>Stack Trace</h1>");
        eout.println("\n<pre>");
        e.printStackTrace(eout);
        eout.println("</pre>");
    }

    private static String[] addStringToArray(String[] arr, String str) {
        String[] newArr = new String[arr.length + 1];
        System.arraycopy(arr, 0, newArr, 0, arr.length);
        newArr[arr.length] = str;
        return newArr;
    }

}
