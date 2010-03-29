package org.anodyneos.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Hashtable;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class EchoAJAXServlet extends HttpServlet {

    private static final long serialVersionUID = 321560897886371028L;

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        // not supported
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        try {
            String reqEnc = "UTF-8";
            String resEnc = "UTF-8";
            String resMime = "text/xml";
            String qs = req.getQueryString();
            if (null != qs) {
                System.out.println("Processing query string: " + qs);
                Hashtable<String, String[]> params = parseQueryString(qs);
                if (params.get("reqEnc") != null) {
                    reqEnc = params.get("reqEnc")[0];
                }
                if (params.get("resEnc") != null) {
                    resEnc = params.get("resEnc")[0];
                }
                if (params.get("resMime") != null) {
                    resMime = params.get("resMime")[0];
                }
            }
            req.setCharacterEncoding(reqEnc);
            Reader in = req.getReader();
            StringBuffer sb = new StringBuffer();
            int numRead = 0;
            for (char[] buf = new char[1024]; -1 != (numRead = in.read(buf));) {
                sb.append(buf, 0, numRead);
            }
            in.close();

            System.out.println(sb.toString());

            res.setContentType(resMime);
            res.setHeader("Cache-Control", "no-cache");
            res.setCharacterEncoding(resEnc);
            Writer out = res.getWriter();
            out.write(sb.toString());
            out.close();
        } catch (Exception e) {
            PrintWriter out = new PrintWriter(res.getOutputStream());
            out.println("Exception Processing Request:");
            out.println();
            e.printStackTrace(out);
            e.printStackTrace();
            out.close();
        }
    }

    /*
     * code taken from tomcat-5 HttpUtils
     */
    static public Hashtable<String, String[]> parseQueryString(String s) {

        String valArray[] = null;

        if (s == null) {
            throw new IllegalArgumentException();
        }
        Hashtable<String, String[]> ht = new Hashtable<String, String[]>();
        StringBuffer sb = new StringBuffer();
        StringTokenizer st = new StringTokenizer(s, "&");
        while (st.hasMoreTokens()) {
            String pair = (String)st.nextToken();
            int pos = pair.indexOf('=');
            if (pos == -1) {
                // XXX
                // should give more detail about the illegal argument

                //throw new IllegalArgumentException();

                // some gx urls do not include complete name / value pairs - example
                // the output to csv url for reports includes the parameter '.csv', but no value
                pair += "=";
                pos = pair.indexOf('=');
            }
            String key = parseName(pair.substring(0, pos), sb);
            String val = parseName(pair.substring(pos+1, pair.length()), sb);
            if (ht.containsKey(key)) {
                String oldVals[] = ht.get(key);
                valArray = new String[oldVals.length + 1];
                for (int i = 0; i < oldVals.length; i++)
                    valArray[i] = oldVals[i];
                valArray[oldVals.length] = val;
            } else {
                valArray = new String[1];
                valArray[0] = val;
            }
            ht.put(key, valArray);
        }
        return ht;
    }


    /*
     * code taken from tomcat-5 HttpUtils
     */
    static private String parseName(String s, StringBuffer sb) {
        sb.setLength(0);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
            case '+':
                sb.append(' ');
                break;
            case '%':
                try {
                    sb.append((char) Integer.parseInt(s.substring(i+1, i+3), 16));
                    i += 2;
                } catch (NumberFormatException e) {
                    // XXX
                    // need to be more specific about illegal arg
                    throw new IllegalArgumentException();
                } catch (StringIndexOutOfBoundsException e) {
                    String rest  = s.substring(i);
                    sb.append(rest);
                    if (rest.length()==2)
                        i++;
                }
                break;
            default:
                sb.append(c);
                break;
            }
        }
        return sb.toString();
    }

}
