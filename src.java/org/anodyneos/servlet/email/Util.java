package org.anodyneos.servlet.email;

import javax.servlet.ServletException;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Util {

    private Util() {
        // only static methods.
    }

    /*
    // Commenting out - why not let javax.mail tell us about invalid emails
    public static final boolean isValidEmailFormat(String addr) throws ServletException  {
        try {
            RE re = new RE("^[\\w\\-\\.]+\\@([\\w\\-]+\\.)+[A-z]{2,3}$");
            return re.match(addr.trim());
        } catch (RESyntaxException e) {
            throw new ServletException(e);
        }
    }
    */

    public static Element getFirstElement(Element el, String name) {
        NodeList nl = el.getElementsByTagName(name);
        if (nl.getLength() == 0) {
            return null;
        } else {
            return (Element) nl.item(0);
        }
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

    public static Element findChildElement(Element el, String name) {
        NodeList nl = el.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals(name)) {
                return (Element) n;
            }
        }
        return null;
    }

    public static Object instantiateObject(Class refClass, String clazzName) {
        ClassLoader cl1 = Thread.currentThread().getContextClassLoader();
        ClassLoader cl2 = refClass.getClassLoader();

        Class clazz = null;
        if (null != cl1) {
            clazz = loadClass(cl1, clazzName);
        }
        if (null == clazz) {
            clazz = loadClass(cl2, clazzName);
        }
        if (null == clazz) {
            return null;
        } else {
            try {
                Object obj = clazz.newInstance();
                return obj;
            } catch (InstantiationException e) {
                return null;
            } catch (IllegalAccessException e) {
                return null;
            }
        }
    }

    private static Class loadClass(ClassLoader cl, String name) {
        try {
            Class clazz = cl.loadClass(name);
                return clazz;
            } catch (ClassNotFoundException e) {
                return null;
        }
    }

}
