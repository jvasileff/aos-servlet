package org.anodyneos.servlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Title: EchoRawServlet
 * Description: Utility for viewing POST information sent by the browser.  All
 * POST data recieved from the browser is sent back to the browser without
 * modification.  HTTP Headers and additional debugging information is printed
 * to stdout.
 * @author John Vasileff
 * @version 1.0
 */

public class EchoRawServlet extends HttpServlet {

    public void service(HttpServletRequest req, HttpServletResponse res) throws java.io.IOException {
        // print the header and debug info to system out
        java.util.Enumeration enum = req.getHeaderNames();
        System.out.println("---------- Begin ----------");
        System.out.println("req.getCharacterEncoding(): " + req.getCharacterEncoding());
        System.out.println("---------- Headers ----------");
        while (enum.hasMoreElements()) {
            String name = (String) enum.nextElement();
            System.out.println(name + "=" + req.getHeader(name));
        }
        // simply output the input.
        res.setContentType("application/octet-stream");
        java.io.InputStream is = req.getInputStream();
        java.io.OutputStream os = res.getOutputStream();
        byte[] bytes = new byte[1024];

        res.setContentLength(req.getContentLength());

        int totalRead = 0;
        while (totalRead < req.getContentLength()) {
            int numRead = is.read(bytes);
            if (numRead == -1) {
                break;
            }
            totalRead += numRead;
            os.write(bytes, 0, numRead);
        }
        is.close();
        os.close();
    }
}
