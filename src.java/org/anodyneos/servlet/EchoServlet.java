package org.anodyneos.servlet;

import java.util.Enumeration;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
}
