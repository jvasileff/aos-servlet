package org.anodyneos.servlet.email;

import javax.servlet.ServletException;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

public class Util {

    private Util() {
        // only static methods.
    }

    public static final boolean isValidEmailFormat(String addr) throws ServletException  {
        try {
            RE re = new RE("^[\\w\\-\\.]+\\@([\\w\\-]+\\.)+[A-z]{2,3}$");
            return re.match(addr.trim());
        } catch (RESyntaxException e) {
            throw new ServletException(e);
        }
    }

}
