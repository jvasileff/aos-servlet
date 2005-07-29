package org.anodyneos.servlet.email;

import java.io.InputStream;
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
import javax.servlet.ServletException;

import org.anodyneos.commons.net.URI;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class EmailCmd implements Command {

    public static final String HOST_SMTP = "HostSMTP";
    private static EmailCmd _instance = new EmailCmd();

    private EmailCmd() {
        // singleton
    }

    public static EmailCmd getInstance() {
        if(null == _instance) {
            _instance = new EmailCmd();
        }
        return _instance;
    }

    public void process(EmailContext ctx, Element email) throws Exception {
        List to = new ArrayList();
        List cc = new ArrayList();
        List bcc = new ArrayList();
        List from = new ArrayList();

        // create some properties and get the default Session
        Properties props = new java.util.Properties();
        props.put("mail.smtp.host", ctx.getParams().getParameter(Params.SERVLET_PARAM, HOST_SMTP));
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
                    to.add(getAddress(el, ctx.getParams()));
                } else if ("cc".equals(el.getNodeName())) {
                    cc.add(getAddress(el, ctx.getParams()));
                } else if ("bcc".equals(el.getNodeName())) {
                    bcc.add(getAddress(el, ctx.getParams()));
                } else if ("from".equals(el.getNodeName())) {
                    from.add(getAddress(el, ctx.getParams()));
                } else if ("subject".equals(el.getNodeName())) {
                    message.setSubject(ctx.getParams().parse(Util.getText(el)));
                } else if ("part".equals(el.getNodeName())) {
                    processPart(ctx, message, el);
                } else if ("multipart".equals(el.getNodeName())) {
                    processMultipart(ctx, message, el);
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

    protected void processPart(EmailContext ctx, MimePart part, Element el)
    throws MessagingException, ServletException, URI.MalformedURIException {
        String mimeType = el.getAttribute("mimeType").trim();
        String fileName = el.getAttribute("fileName").trim();

        // will have one of (content, xslResultContent, fileContent)
        Element content = Util.findChildElement(el, "content");
        Element fileContent = Util.findChildElement(el, "fileContent");
        Element substResultContent = Util.findChildElement(el, "substResultContent");
        Element xslResultContent = Util.findChildElement(el, "xslResultContent");
        if (content != null) {
            String charset = content.getAttribute("charset").trim();
            StringDataSource ds = new StringDataSource(ctx.getParams().parse(Util.getText(content)));
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
                text = ctx.getParams().parse(Util.getText(substResultContent));
            } else {
                URI uri = new URI(ctx.getConfigURI(), path);
                try {
                    InputStream is = ctx.getResolver().openStream(uri);
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
                    text = ctx.getParams().parse(sw.toString());
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
            URI uri = new URI(ctx.getConfigURI(), path);
            URIDataSource ds = new URIDataSource(uri, ctx.getResolver());
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
            URI uri = new URI(ctx.getConfigURI(), path);
            XSLDataSource ds = new XSLDataSource(ctx.getTemplatesCache(), uri, ctx.getParams());
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
    protected void processMultipart(EmailContext ctx, MimePart part, Element multipartEl)
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
                    processPart(ctx, p, el);
                    multipart.addBodyPart(p);
                } else if ("multipart".equals(el.getNodeName())) {
                    MimeBodyPart p = new MimeBodyPart();
                    processMultipart(ctx, p, el);
                    multipart.addBodyPart(p);
                }
            }
        }
        part.setContent(multipart);
    }

    protected Address getAddress(Element el, Params params) throws ServletException,
            java.io.UnsupportedEncodingException{

        String address = params.parse(el.getAttribute("address"));
        /*
        if (! Util.isValidEmailFormat(address)) {
            address = "";
        }
        */
        String name = params.parse(el.getAttribute("name"));
        return new InternetAddress(address, name);
    }

}
