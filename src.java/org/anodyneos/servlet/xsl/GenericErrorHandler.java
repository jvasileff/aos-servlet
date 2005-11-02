package org.anodyneos.servlet.xsl;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * <meta name="usage" content="general"/>
 * Implement SAX error handler for default reporting.
 */
public class GenericErrorHandler implements ErrorHandler, ErrorListener {

    private static final Log log = LogFactory.getLog(GenericErrorHandler.class);

    public GenericErrorHandler() {}

    public void warning(SAXParseException exception) throws SAXException {
        log.warn(
            "[SAX Warning] " +
            getLocationString(exception) + ": " + exception.getMessage());
    }

    public void error(SAXParseException exception) throws SAXException {
        log.error(
            "[SAX Error] " +
            getLocationString(exception) + ": " + exception.getMessage());
    }

    public void fatalError(SAXParseException exception) throws SAXException {
        log.error(
            "[SAX Fatal Error] " +
            getLocationString(exception) + ": " + exception.getMessage());
        throw exception;
    }

    public void warning(TransformerException exception) throws TransformerException {
        log.warn(
            "[Transformer Warning] " +
            getLocationString(exception) + ": " + exception.getMessage());
    }

    public void error(TransformerException exception) throws TransformerException {
        log.error(
            "[Transformer Error] " +
            getLocationString(exception) + ": " + exception.getMessage());
    }

    public void fatalError(TransformerException exception) throws TransformerException {
        log.error(
            "[Transformer Fatal Error] " +
            getLocationString(exception) + ": " + exception.getMessage());
        throw exception;
    }


    private String getLocationString(SAXParseException ex) {
        StringBuffer str = new StringBuffer();

        String id = ex.getSystemId();
        if (id != null) {
            /*
            int index = systemId.lastIndexOf('/');
            if (index != -1)
                systemId = systemId.substring(index + 1);
            */
        } else {
            id = "SystemId Unknown";
        }
        return id
            + "; Line " + ex.getLineNumber()
            + "; Column " + ex.getColumnNumber();
    }

    private String getLocationString(TransformerException exception) {
        SourceLocator locator = exception.getLocator();
        String id;
        if(null != locator) {
            id = (locator.getPublicId() != locator.getPublicId())
                    ? locator.getPublicId()
                    : (null != locator.getSystemId())
                        ? locator.getSystemId() : "SystemId Unknown";
            return id
                + "; Line " + locator.getLineNumber()
                + "; Column " + locator.getColumnNumber();
        }
        return "SystemId Unknown";
    }
}
