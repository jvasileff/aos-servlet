package org.anodyneos.servlet;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.anodyneos.servlet.util.HttpServletRequestAsMap;

/**
 * Title: Echo Servlet
 * Description: Simple servlet to echo parameters for get and post requests.
 * @author John Vasileff
 * @version 1.0
 */

public class EchoServlet extends HttpServlet {

    static String CONTINUE_URL = "continueURL";
    static String INFO = "helpText";

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws java.io.IOException {
        Enumeration paramNames = req.getParameterNames();
        java.io.PrintWriter out = res.getWriter();

        out.println("<html><head><title>Echo Servlet</title></head>");
        out.println("<body>");
        if (req.getParameter(INFO) != null) {
            out.println("<p>");
            out.println(req.getParameter(INFO));
            out.println("</p>");
        }

        /*
        out.println("<h1>Request Parameters</h1>");
        out.println("<table border='1'>");
        out.println("<tr><td><b>Parmameter</b></td><td><b>Value(s)</b></td></tr>");
        while (paramNames.hasMoreElements()) {
            String paramName = (String)paramNames.nextElement();
            if (paramName.equals(CONTINUE_URL) || paramName.equals(INFO)) {
                out.println("<tr><td bgcolor='gray' valign='top'>" + paramName +
                    "</td><td bgcolor='gray' valign='top'>");
            } else {
                out.println("<tr><td valign='top'>" + paramName + "</td><td valign='top'>");
            }
            String[] paramValues = (String[])req.getParameterValues(paramName);
            for (int i=0; i < paramValues.length; i++) {
                out.println(paramValues[i] + "<br>");
            }
            out.println("</td></tr>");
        }
        out.println("</table>");
        */

        // Request Parameters
        out.println("<h1>Request Parameters</h1>");
        out.println("<table border='1'>");
        out.println("<tr><td><b>Name</b></td><td><b>Value(s)</b></td></tr>");
        for (Enumeration names = req.getParameterNames(); names.hasMoreElements();) {
            String name = (String) names.nextElement();
            String[] values = req.getParameterValues(name);
            outputRows(out, name, values);
        }
        out.println("</table>");

        // Request Headers
        out.println("<h1>Request Headers</h1>");
        out.println("<table border='1'>");
        out.println("<tr><td><b>Name</b></td><td><b>Value(s)</b></td></tr>");
        for (Enumeration enum = req.getHeaderNames(); enum.hasMoreElements();) {
            String name = (String) enum.nextElement();
            String[] values = toStringArray(req.getHeaders(name));
            outputRows(out, name, values);
        }
        out.println("</table>");

        // Request Attributes
        out.println("<h1>Request Attributes</h1>");
        out.println("<table border='1'>");
        out.println("<tr><td><b>Name</b></td><td><b>Value(s)</b></td></tr>");
        for (Enumeration enum = req.getAttributeNames(); enum.hasMoreElements();) {
            String name = (String) enum.nextElement();
            String[] values = toStringArray(req.getAttribute(name));
            outputRows(out, name, values);
        }
        out.println("</table>");

        // Servlet Parameters
        out.println("<h1>Servlet Parameters</h1>");
        out.println("<table border='1'>");
        out.println("<tr><td><b>Name</b></td><td><b>Value(s)</b></td></tr>");
        for (Enumeration enum = getInitParameterNames(); enum.hasMoreElements();) {
            String name = (String) enum.nextElement();
            String[] values = toStringArray(getInitParameter(name));
            outputRows(out, name, values);
        }
        out.println("</table>");

        // Context Attributes
        out.println("<h1>Context Attributes</h1>");
        out.println("<table border='1'>");
        out.println("<tr><td><b>Name</b></td><td><b>Value(s)</b></td></tr>");
        for (Enumeration enum = getServletContext().getAttributeNames(); enum.hasMoreElements();) {
            String name = (String) enum.nextElement();
            String[] values = toStringArray(getServletContext().getAttribute(name));
            outputRows(out, name, values);
        }
        out.println("</table>");

        // Context Parameters
        out.println("<h1>Context Parameters</h1>");
        out.println("<table border='1'>");
        out.println("<tr><td><b>Name</b></td><td><b>Value(s)</b></td></tr>");
        for (Enumeration enum = getServletContext().getInitParameterNames(); enum.hasMoreElements();) {
            String name = (String) enum.nextElement();
            String[] values = toStringArray(getServletContext().getInitParameter(name));
            outputRows(out, name, values);
        }
        out.println("</table>");

        // CGI
        out.println("<h1>CGI Variables/HttpServletRequest</h1>");
        out.println("<table border='1'>");
        out.println("<tr><td><b>Name</b></td><td><b>Value(s)</b></td></tr>");
        HttpServletRequestAsMap reqAsMap = new HttpServletRequestAsMap(req);
        for (Iterator it = reqAsMap.keySet().iterator(); it.hasNext();) {
            String name = (String) it.next();
            String[] values = toStringArray(reqAsMap.get(name));
            outputRows(out, name, values);
        }
        out.println("</table>");

        if (req.getParameter(CONTINUE_URL) != null) {
            out.println("<p>");
            out.println("<a href='" + req.getParameter(CONTINUE_URL) + "'>Continue</a>");
            out.println("</p>");
        }
        out.println("</body>");
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws java.io.IOException {
        doGet(req, res);
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

    protected void outputRows(PrintWriter out, String name, String[] values) {
        out.println("<tr>");
        out.println("<td valign='top' rowspan='" + values.length + "'>" + name + "</td>");
        if(values.length > 0) {
            out.println("<td valign='top'>" + values[0] + "</td>");
        } else {
            // no-value
        }
        out.println("</tr>");
        for(int i=1; i<values.length; i++) {
            out.println("<tr><td valign='top'>" + values[i] + "</td></tr>");
        }
    }
}
