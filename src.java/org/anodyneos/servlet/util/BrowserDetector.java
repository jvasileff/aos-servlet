package org.anodyneos.servlet.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.*;

import javax.servlet.http.HttpServletRequest;

public class BrowserDetector {

    public static final String MSIE = "MSIE";
    public static final String SAFARI = "Safari";
    public static final String OPERA = "Opera";
    public static final String MOZILLA = "Mozilla";
    public static final String NETSCAPE = "Netscape";
    public static final String KONQUEROR = "Konqueror";

    public static final String WINDOWS = "Windows";
    public static final String MACINTOSH = "Macintosh";
    public static final String X11 = "X11";
    public static final String UNKNOWN = "unknown";

    private String userAgentString = "";

    private String browserName = UNKNOWN;
    private float browserVersion = (float) 0;
    private String browserVersionString = "0";
    private String platform = UNKNOWN;

    private static final Pattern cleanVersionPattern = Pattern.compile("([0-9]+(\\.[0-9]+){0,1}).*");

    public static void main(String[] args) throws IOException {
        // for testing, parse each arg if provided or each line of stdin
        if (args.length != 0) {
            for (int i = 0; i < args.length; i++) {
                String userAgent = args[i];
                BrowserDetector obj = new BrowserDetector();
                obj.setUserAgentString(userAgent);
                System.out.println(obj.platform + "; " + obj.browserName + "; " + obj.browserVersion);
            }
        } else {
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            for (String userAgent; (userAgent = in.readLine()) != null;) {
                BrowserDetector obj = new BrowserDetector();
                obj.setUserAgentString(userAgent);
                System.out.println(obj.platform + "; " + obj.browserName + "; " + obj.browserVersionString);
            }
        }
    }

    public BrowserDetector() {
    }

    public BrowserDetector(String userAgentString) {
        setUserAgentString(userAgentString);
    }

    public BrowserDetector(HttpServletRequest req) {
        setUserAgentString(req.getHeader("User-Agent"));
    }

    public void setUserAgentString(String userAgentString) {
        if (null == userAgentString) {
            userAgentString = "";
        } else {
            this.userAgentString = userAgentString;
        }

        browserName = UNKNOWN;
        browserVersion = (float) 0;
        browserVersionString = "0";
        platform = UNKNOWN;
        parse();
    }

    private void parse() {
        Pattern p;
        Matcher m;

        if (
                trySafari() ||
                tryMozilla() ||
                tryOpera1() ||
                tryOpera2() ||
                tryMSIE() ||
                tryOldNetscape() ||
                tryKonqueror() ||
                false ) {
            // browser detected
            return;
        } else {
            // browser not detected
            //return;
        }
    }

    public String getBrowserName() {
        return browserName;
    }
    public float getBrowserVersion() {
        return browserVersion;
    }
    public String getBrowserVersionString() {
        return browserVersionString;
    }
    public String getBrowserPlatform() {
        return platform;
    }
    public String getUserAgentString() {
        return userAgentString;
    }


    ////////////////////////////////////////////////////////////////////////////////
    //
    // Safari
    //
    ////////////////////////////////////////////////////////////////////////////////
    private static final Pattern safariPattern = Pattern.compile(".*AppleWebKit\\/([^\\s]+).*");

    /**
     * @see http://developer.apple.com/internet/safari/safari_faq.html
     * @return
     */
    private boolean trySafari() {
        // Safari
        //      Example: "Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en) AppleWebKit/125.5 (KHTML, like Gecko) Safari/125.9"
        //      Pattern: "Mozilla/5.0 (xxx; xxx; ... xxx) AppleWebKit/xxx (KHTML, like Gecko) Safari/xxx"
        //      Pattern: "Mozilla/5.0 (xxx; xxx; ... xxx) AppleWebKit/xxx (like Gecko) Safari/xxx"
        //Pattern p = Pattern.compile("^Mozilla/5.0 .* AppleWebKit\\/([^\\s]+) (\\(KHTML, like Gecko\\)|\\(like Gecko\\)) Safari\\/([^\\/\\s]+).*");
        Matcher m = safariPattern.matcher(userAgentString);
        if (m.matches()) {
            browserName = SAFARI;
            browserVersionString = m.group(1);
            try {
                // take ignore the second dot forward.
                Matcher m2 = cleanVersionPattern.matcher(browserVersionString);
                if (m2.matches()) {
                    browserVersion = Float.valueOf(m2.group(1)).floatValue();
                }
            } catch (NumberFormatException e) {
                // use default value;
            }
            platform = MACINTOSH;
            return true;
        } else {
            return false;
        }
    }


    ////////////////////////////////////////////////////////////////////////////////
    //
    // Opera
    //
    ////////////////////////////////////////////////////////////////////////////////
    private static final Pattern operaPattern1 = Pattern.compile(".* Opera ([^\\/\\s]+).*");

    private boolean tryOpera1() {
        /* Opera #1
                Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1) Opera 7.52  [en]
                Mozilla/4.1 (compatible; MSIE 5.0; Symbian OS; Nokia 6600;452) Opera 6.20  [en-US]
                Mozilla/5.0 (Windows NT 5.0; U) Opera 7.01  [en]
        */
        Matcher m = operaPattern1.matcher(userAgentString);
        if (m.matches()) {
            browserName = OPERA;
            browserVersionString = m.group(1);

            try {
                // take ignore the second dot forward.
                Matcher m2 = cleanVersionPattern.matcher(browserVersionString);
                if (m2.matches()) {
                    browserVersion = Float.valueOf(m2.group(1)).floatValue();
                }
            } catch (NumberFormatException e) {
                // use default value;
            }

            platform = parsePlatform(userAgentString);

            return true;
        } else {
            return false;
        }
    }

    private static final Pattern operaPattern2 = Pattern.compile("^Opera/([^\\/\\s]+).*");
    private boolean tryOpera2() {
        /* Opera #2
                Opera/7.20 (Windows NT 5.1; U)  [en]
        */
        Matcher m = operaPattern2.matcher(userAgentString);
        if (m.matches()) {
            browserName = OPERA;
            browserVersionString = m.group(1);

            try {
                // take ignore the second dot forward.
                Matcher m2 = cleanVersionPattern.matcher(browserVersionString);
                if (m2.matches()) {
                    browserVersion = Float.valueOf(m2.group(1)).floatValue();
                }
            } catch (NumberFormatException e) {
                // use default value;
            }

            platform = parsePlatform(userAgentString);
            return true;
        } else {
            return false;
        }
    }


    ////////////////////////////////////////////////////////////////////////////////
    //
    // MSIE
    //
    ////////////////////////////////////////////////////////////////////////////////
    private static final Pattern msiePattern =
        Pattern.compile(".*Mozilla/[0-9].[0-9][^\\(]* \\(compatible; MSIE ([^;\\)\\s]+).*");

    private boolean tryMSIE() {
        /* MSIE
            Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)
            Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; FunWebProducts)

            Mozilla/4.0 (compatible ; MSIE 6.0; Windows NT 5.1)
            Mozilla/4.0+(Compatible;+MSIE+6.0;+Windows+NT+5.0;+.NET+CLR+1.0.3705)
            MSIE (MSIE 5.14; Mac_PowerPC)
            UserAgent: Mozilla/4.0 (compatible; MSIE 5.5; Windows NT 4.0)
        */
        Matcher m = msiePattern.matcher(userAgentString);
        if ((userAgentString.indexOf("Opera") == -1) && m.matches()) {
            browserName = MSIE;
            browserVersionString = m.group(1);

            try {
                // take ignore the second dot forward.
                Matcher m2 = cleanVersionPattern.matcher(browserVersionString);
                if (m2.matches()) {
                    browserVersion = Float.valueOf(m2.group(1)).floatValue();
                }
            } catch (NumberFormatException e) {
                // use default value;
            }

            platform = parsePlatform(userAgentString);

            return true;
        } else {
            return false;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////
    //
    // Old Netscape
    //
    ////////////////////////////////////////////////////////////////////////////////
    private static final Pattern oldNetscapePattern = Pattern.compile("Mozilla/([0-9\\.]*[^\\s\\(]*).*");

    private boolean tryOldNetscape() {
        // Old netscape does not have: MSIE, Gecko, Opera, Safari, AppleWebKit, Mozilla version < 5 and > 1.
        if ( userAgentString.indexOf("MSIE") == -1 &&
                userAgentString.indexOf("Gecko") == -1 &&
                userAgentString.indexOf("Opera") == -1 &&
                userAgentString.indexOf("AppleWebKit") == -1 &&
                userAgentString.indexOf("Safari") == -1) {

            Matcher m = oldNetscapePattern.matcher(userAgentString);

            if (m.matches()) {
                String bvString = m.group(1);
                float bv = 5;

                try {
                    // take ignore the second dot forward.
                    Matcher m2 = cleanVersionPattern.matcher(bvString);
                    if (m2.matches()) {
                        bv = Float.valueOf(m2.group(1)).floatValue();
                    }
                } catch (NumberFormatException e) {
                    // use default value;
                }

                if (bv < 5.0 && bv > 1.0) {
                    // now we have a match

                    browserName = NETSCAPE;
                    browserVersionString = bvString;
                    browserVersion = bv;
                    platform = parsePlatform(userAgentString);

                    return true;
                }
            }
        }
        return false;
    }

    ////////////////////////////////////////////////////////////////////////////////
    //
    // Konqueror
    //
    ////////////////////////////////////////////////////////////////////////////////
    private static final Pattern konquerorPattern = Pattern.compile("^Mozilla.*Konqueror\\/([^;^\\/^\\)^\\s]+).*");

    private boolean tryKonqueror() {
        // Konqueror
        //      Mozilla/5.0 (compatible; Konqueror/3.2; Linux) (KHTML, like Gecko)
        //      Mozilla/5.0 (compatible; Konqueror/3.1; Linux)
        //      Mozilla/5.0 (compatible; Konqueror/3.1-rc6; i686 Linux; 20021021)
        //		Mozilla/5.0 (compatible; Konqueror/3.0; i686 Linux; 20020906)

        Matcher m = konquerorPattern.matcher(userAgentString);
        if (m.matches()) {
            browserName = KONQUEROR;
            browserVersionString = m.group(1);
            try {
                // take ignore the second dot forward.
                Matcher m2 = cleanVersionPattern.matcher(browserVersionString);
                if (m2.matches()) {
                    browserVersion = Float.valueOf(m2.group(1)).floatValue();
                }
            } catch (NumberFormatException e) {
                // use default value;
            }
            platform = X11;
            return true;
        } else {
            return false;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////
    //
    // Mozilla
    //
    ////////////////////////////////////////////////////////////////////////////////
    private static final Pattern mozillaPattern1 = Pattern.compile(".*rv:([^;^\\/^\\)^\\s]+).*Gecko.*");
    private static final Pattern mozillaPattern2 = Pattern.compile(".*(m[0-9][0-9]).*Gecko.*");

    /**
     * This is for all Mozilla >= 5 browsers including Mozilla, Netscape 6+, Firefox, Camino, etc.
     * The version number returned is the Mozilla v:x.y number such as 1.0.2.
     * @return
     */
    private boolean tryMozilla() {
        //      Mozilla/5.0 (Macintosh; U; PPC; en-US; rv:1.0.2) Gecko/20030208 Netscape/7.02
        //      Mozilla/5.0 (Macintosh; N; PPC; en-US; m18) Gecko/20010131 Netscape6/6.01

        // Either match "rv:x.y.z*Gecko*" or "m99*Gecko*"

        boolean matched = false;

        Matcher m = mozillaPattern1.matcher(userAgentString);
        if (m.matches()) {
            matched = true;
            browserVersionString = m.group(1);
            try {
                // take ignore the second dot forward.
                Matcher m2 = cleanVersionPattern.matcher(browserVersionString);
                if (m2.matches()) {
                    browserVersion = Float.valueOf(m2.group(1)).floatValue();
                }
            } catch (NumberFormatException e) {
                // use default value;
            }
        } else {
            m = mozillaPattern2.matcher(userAgentString);
            if (m.matches()) {
                matched = true;
                browserVersionString = m.group(1);
                try {
                    browserVersion = ( Float.valueOf(m.group(1).substring(1)).floatValue() / 100F);
                } catch (NumberFormatException e) {
                    // should not happen; use default value;
                }
            }
        }

        if (matched) {
            browserName = MOZILLA;
            platform = parsePlatform(userAgentString);
            return true;
        } else {
            return false;
        }
    }

    private static final String parsePlatform(String userAgent) {
        String ret = UNKNOWN;
        if (userAgent.indexOf("Windows") != -1) {
            ret = WINDOWS;
        } else if (userAgent.indexOf("Win98") != -1) {
            ret = WINDOWS;
        } else if (userAgent.indexOf("Mac") != -1) {
            ret = MACINTOSH;
        } else if (userAgent.indexOf("Linux") != -1) {
            ret = X11;
        } else if (userAgent.indexOf("X11") != -1) {
            ret = X11;
        }
        return ret;
    }

}
