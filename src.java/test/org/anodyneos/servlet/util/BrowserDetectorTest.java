package test.org.anodyneos.servlet.util;

import org.anodyneos.servlet.util.BrowserDetector;

import junit.framework.TestCase;

/**
 * @author jvas
 */
public class BrowserDetectorTest extends TestCase {

    public void testSafari() {
        tryAgent(new Object[] {
                "Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en) AppleWebKit/125.5 (KHTML, like Gecko) Safari/125.9"
                ,"Macintosh", "Safari", "125.5", new Float(125.5)});

        tryAgent(new Object[] {
                "Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en) AppleWebKit/125.5.5 (KHTML, like Gecko) Safari/125.12"
                ,"Macintosh", "Safari", "125.5.5", new Float(125.5)});

        tryAgent(new Object[] {
                "Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en) AppleWebKit/125.5.5 (KHTML, like Gecko) Safari/125.12_Adobe"
                ,"Macintosh", "Safari", "125.5.5", new Float(125.5)});

        tryAgent(new Object[] {
                "Mozilla/5.0 (Macintosh; U; PPC Mac OS X; ko-kr) AppleWebKit/103u (KHTML, like Gecko) Safari/100"
                ,"Macintosh", "Safari", "103u", new Float(103)});

        tryAgent(new Object[] {
                "Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en) AppleWebKit/74 (KHTML, like Gecko) Safari/74"
                ,"Macintosh", "Safari", "74", new Float(74)});

        tryAgent(new Object[] {
                "Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en-us) AppleWebKit/124 (KHTML, like Gecko) Safari/100.1"
                ,"Macintosh", "Safari", "124", new Float(124)});

        tryAgent(new Object[] {
                "Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en-us) AppleWebKit/60 (like Gecko) Safari/60"
                ,"Macintosh", "Safari", "60", new Float(60)});

        tryAgent(new Object[] {
                "Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en-us) AppleWebKit/85.8.2 (KHTML, like Gecko) Safari/85.8.1"
                ,"Macintosh", "Safari", "85.8.2", new Float(85.8)});


        tryAgent(new Object[] {
                "Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en-US) AppleWebKit/85 (KHTML, like Gecko) OmniWeb/v558.48"
                ,"Macintosh", "Safari", "85", new Float(85)});

        tryAgent(new Object[] {
                "Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en-us) AppleWebKit/125.5 (KHTML, like Gecko)"
                ,"Macintosh", "Safari", "125.5", new Float(125.5)});

        tryAgent(new Object[] {
                "Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en) AppleWebKit/124 (KHTML, like Gecko)"
                ,"Macintosh", "Safari", "124", new Float(124)});

        tryAgent(new Object[] {
                "Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en-US) AppleWebKit/73 (KHTML, like Gecko) OmniWeb/v483"
                ,"Macintosh", "Safari", "73", new Float(73)});

        tryAgent(new Object[] {
                "Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en) AppleWebKit/103u (KHTML, like Gecko) safari/100"
                ,"Macintosh", "Safari", "103u", new Float(103)});
    }

    public void testOpera() {
        tryAgent(new Object[] {
                "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1) Opera 7.52  [en]"
                ,BrowserDetector.WINDOWS, BrowserDetector.OPERA, "7.52", new Float(7.52)});

        tryAgent(new Object[] {
                "Mozilla/4.0 (compatible; MSIE 6.0; MSIE 5.5; Windows NT 5.1) Opera 7.03  [en]"
                ,BrowserDetector.WINDOWS, BrowserDetector.OPERA, "7.03", new Float(7.03)});

        tryAgent(new Object[] {
                "Mozilla/5.0 (Windows NT 5.1; U) Opera 7.11  [en]"
                ,BrowserDetector.WINDOWS, BrowserDetector.OPERA, "7.11", new Float(7.11)});

        tryAgent(new Object[] {
                "Mozilla/4.0 (compatible; MSIE 6.0; MSIE 5.5; Windows NT 5.1) Opera 7.02 Bork-edition  [en]"
                ,BrowserDetector.WINDOWS, BrowserDetector.OPERA, "7.02", new Float(7.02)});

        tryAgent(new Object[] {
                "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1) Opera 7.54u1  [en]"
                ,BrowserDetector.WINDOWS, BrowserDetector.OPERA, "7.54u1", new Float(7.54)});

        tryAgent(new Object[] {
                "Mozilla/4.0 (compatible; MSIE 5.0; Linux 2.4.20-64GB-SMP i686) Opera 6.03  [de]"
                ,BrowserDetector.X11, BrowserDetector.OPERA, "6.03", new Float(6.03)});

        tryAgent(new Object[] {
                "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0) Opera 7.60  [ru] (IBM EVV/3.0/EAK01AG9/LE)"
                ,BrowserDetector.WINDOWS, BrowserDetector.OPERA, "7.60", new Float(7.60)});

        tryAgent(new Object[] {
                "Mozilla/4.0 (compatible; MSIE 6.0; Mac_PowerPC) Opera 7.30  [en]"
                ,BrowserDetector.MACINTOSH, BrowserDetector.OPERA, "7.30", new Float(7.30)});

        // ALTERNATE STYLE

        tryAgent(new Object[] {
                "Opera/7.20 (Windows NT 5.1; U)  [en]"
                ,BrowserDetector.WINDOWS, BrowserDetector.OPERA, "7.20", new Float(7.20)});

        tryAgent(new Object[] {
                "Opera/7.51 (X11; Linux i686; U)  [es-LA]"
                ,BrowserDetector.X11, BrowserDetector.OPERA, "7.51", new Float(7.51)});

        tryAgent(new Object[] {
                "Opera/7.11 (Linux 2.4.2 i386; U)  [en]"
                ,BrowserDetector.X11, BrowserDetector.OPERA, "7.11", new Float(7.11)});

        // STRANGE CASES
        tryAgent(new Object[] {
                "MSIE (MSIE 6.0; Windows 2000) Opera 7.50 [en]"
                ,BrowserDetector.WINDOWS, BrowserDetector.OPERA, "7.50", new Float(7.50)});
    }

    public void testMSIE() {
        tryAgent(new Object[] {
                "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)"
                ,BrowserDetector.WINDOWS, BrowserDetector.MSIE, "6.0", new Float(6.0)});

        tryAgent(new Object[] {
                "Mozilla/4.0 (compatible; MSIE 5.0; SP1B; SunOS 5.7 sun4u; X11)"
                ,BrowserDetector.X11, BrowserDetector.MSIE, "5.0", new Float(5.0)});

        tryAgent(new Object[] {
                "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322)"
                ,BrowserDetector.WINDOWS, BrowserDetector.MSIE, "6.0", new Float(6.0)});

        tryAgent(new Object[] {
                "Mozilla/4.0 (compatible; MSIE 5.23; Mac_PowerPC)"
                ,BrowserDetector.MACINTOSH, BrowserDetector.MSIE, "5.23", new Float(5.23)});

        tryAgent(new Object[] {
                "Mozilla/4.0 (compatible; MSIE 4.01; Windows NT)"
                ,BrowserDetector.WINDOWS, BrowserDetector.MSIE, "4.01", new Float(4.01)});

        tryAgent(new Object[] {
                "Mozilla/4.0 (compatible; MSIE 5.0b1; Mac_PowerPC)"
                ,BrowserDetector.MACINTOSH, BrowserDetector.MSIE, "5.0b1", new Float(5.0)});

        tryAgent(new Object[] {
                "Mozilla/4.0 (compatible; MSIE 6.0b; Windows NT 5.1)"
                ,BrowserDetector.WINDOWS, BrowserDetector.MSIE, "6.0b", new Float(6.0)});

        tryAgent(new Object[] {
                "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0; 3M/MSIE 6.0;; .NET CLR 1.0.3705)"
                ,BrowserDetector.WINDOWS, BrowserDetector.MSIE, "6.0", new Float(6.0)});

        tryAgent(new Object[] {
                "Mozilla/4.0 WebTV/2.6 (compatible; MSIE 4.0)"
                ,BrowserDetector.UNKNOWN, BrowserDetector.MSIE, "4.0", new Float(4.0)});

        tryAgent(new Object[] {
                "Mozilla/3.0 WebTV/1.2 (compatible; MSIE 2.0)"
                ,BrowserDetector.UNKNOWN, BrowserDetector.MSIE, "2.0", new Float(2.0)});

        tryAgent(new Object[] {
                "Mozilla/4.0 (compatible; MSIE 4.01; Windows CE; PPC; 240x320)"
                ,BrowserDetector.WINDOWS, BrowserDetector.MSIE, "4.01", new Float(4.01)});

        tryAgent(new Object[] {
                "Mozilla/4.0 (compatible; MSIE 6.0b; Windows 98; Win 9x 4.90)"
                ,BrowserDetector.WINDOWS, BrowserDetector.MSIE, "6.0b", new Float(6.0)});

        tryAgent(new Object[] {
                "Mozilla/4.0 (compatible; MSIE 6.0; Win32)"
                ,BrowserDetector.UNKNOWN, BrowserDetector.MSIE, "6.0", new Float(6.0)});

        // STRANGE CASES

        tryAgent(new Object[] {
                "nogoop-HttpClient/1.1.3 Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)"
                ,BrowserDetector.WINDOWS, BrowserDetector.MSIE, "6.0", new Float(6.0)});

        tryAgent(new Object[] {
                "User-Agent: Mozilla/4.0 (compatible; MSIE 5.5; Windows NT 5.0)"
                ,BrowserDetector.WINDOWS, BrowserDetector.MSIE, "5.5", new Float(5.5)});

        tryAgent(new Object[] {
                "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; Rowan University); .NET CLR 1.1.4322)"
                ,BrowserDetector.WINDOWS, BrowserDetector.MSIE, "6.0", new Float(6.0)});

        //Robot?
        //tryAgent(new Object[] {
        //        "Mozilla/4.0 (compatible ; MSIE 6.0; Windows NT 5.1)"
        //        ,UserAgentParser.WINDOWS, UserAgentParser.MSIE, "6.0", new Float(6.0)});
    }

    public void testNetscape() {
        tryAgent(new Object[] {
                "Mozilla/4.7C-CCK-MCD {C-UDP; EBM-APPLE} (Macintosh; I; PPC)"
                ,BrowserDetector.MACINTOSH, BrowserDetector.NETSCAPE, "4.7C-CCK-MCD", new Float(4.7)});

        tryAgent(new Object[] {
                "Mozilla/4.77 [en] (Windows NT 5.0; U)"
                ,BrowserDetector.WINDOWS, BrowserDetector.NETSCAPE, "4.77", new Float(4.77)});

        tryAgent(new Object[] {
                "Mozilla/4.61 (Macintosh; I; PPC)"
                ,BrowserDetector.MACINTOSH, BrowserDetector.NETSCAPE, "4.61", new Float(4.61)});

        tryAgent(new Object[] {
                "Mozilla/4.8 [en] (Win98; U)"
                ,BrowserDetector.WINDOWS, BrowserDetector.NETSCAPE, "4.8", new Float(4.8)});

        tryAgent(new Object[] {
                "Mozilla/4.79C-CCK-MCD  [en] (X11; U; SunOS 5.8 sun4u)"
                ,BrowserDetector.X11, BrowserDetector.NETSCAPE, "4.79C-CCK-MCD", new Float(4.79)});

        // not really netscape, but close enough

        tryAgent(new Object[] {
                "Mozilla/3.01 (compatible;)"
                ,BrowserDetector.UNKNOWN, BrowserDetector.NETSCAPE, "3.01", new Float(3.01)});

        tryAgent(new Object[] {
                "Mozilla/2.0 (compatible; MS FrontPage 4.0)"
                ,BrowserDetector.UNKNOWN, BrowserDetector.NETSCAPE, "2.0", new Float(2.0)});

        tryAgent(new Object[] {
                "Mozilla/4.0 (compatible; grub-client-0.3.0; Crawl your own stuff with http://grub.org)"
                ,BrowserDetector.UNKNOWN, BrowserDetector.NETSCAPE, "4.0", new Float(4.0)});
    }

    public void testKonqueror() {
        tryAgent(new Object[] {
                "Mozilla/5.0 (compatible; Konqueror/3.2; Linux) (KHTML, like Gecko)"
                ,BrowserDetector.X11, BrowserDetector.KONQUEROR, "3.2", new Float(3.2)});

        tryAgent(new Object[] {
                "Mozilla/5.0 (compatible; Konqueror/3.1; Linux)"
                ,BrowserDetector.X11, BrowserDetector.KONQUEROR, "3.1", new Float(3.1)});

        tryAgent(new Object[] {
                "Mozilla/5.0 (compatible; Konqueror/3.1-rc6; i686 Linux; 20021021)"
                ,BrowserDetector.X11, BrowserDetector.KONQUEROR, "3.1-rc6", new Float(3.1)});

        tryAgent(new Object[] {
                "Mozilla/5.0 (compatible; Konqueror/3.0; i686 Linux; 20020911)"
                ,BrowserDetector.X11, BrowserDetector.KONQUEROR, "3.0", new Float(3.0)});
    }

    public void testMozilla() {
        tryAgent(new Object[] {
                "Mozilla/5.0 (Macintosh; U; PPC; en-US; rv:1.0.2) Gecko/20030208 Netscape/7.02"
                ,BrowserDetector.MACINTOSH, BrowserDetector.MOZILLA, "1.0.2", new Float(1.0)});

        tryAgent(new Object[] {
                "Mozilla/5.0 (Macintosh; U; PPC Mac OS X Mach-O; en-US; rv:1.4) Gecko/20030624 Netscape/7.1"
                ,BrowserDetector.MACINTOSH, BrowserDetector.MOZILLA, "1.4", new Float(1.4)});

        tryAgent(new Object[] {
                "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.7.5) Gecko/20041107 Firefox/1.0"
                ,BrowserDetector.WINDOWS, BrowserDetector.MOZILLA, "1.7.5", new Float(1.7)});

        tryAgent(new Object[] {
                "Mozilla/5.0 (Windows; U; Windows NT 5.0; en-US; rv:1.7) Gecko/20040626 Firefox/0.9.1"
                ,BrowserDetector.WINDOWS, BrowserDetector.MOZILLA, "1.7", new Float(1.7)});

        tryAgent(new Object[] {
                "Mozilla/5.0 (Macintosh; U; PPC Mac OS X Mach-O; en-US; rv:1.0.1) Gecko/20030306 Camino/0.7"
                ,BrowserDetector.MACINTOSH, BrowserDetector.MOZILLA, "1.0.1", new Float(1.0)});

        tryAgent(new Object[] {
                "Mozilla/5.0 (Macintosh; U; PPC; en-US; rv:0.9.4.1) Gecko/20020318 Netscape6/6.2.2"
                ,BrowserDetector.MACINTOSH, BrowserDetector.MOZILLA, "0.9.4.1", new Float(0.9)});

        tryAgent(new Object[] {
                "Mozilla/5.0 (Macintosh; U; PPC Mac OS X Mach-O; en-US; rv:1.8a3) Gecko/20040716 Camino/0.8+"
                ,BrowserDetector.MACINTOSH, BrowserDetector.MOZILLA, "1.8a3", new Float(1.8)});

        tryAgent(new Object[] {
                "Mozilla/5.0 (Windows; U; Win 9x 4.90; de-DE; rv:1.5) Gecko/20031007 Firebird/0.7"
                ,BrowserDetector.WINDOWS, BrowserDetector.MOZILLA, "1.5", new Float(1.5)});

        tryAgent(new Object[] {
                "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.4a) Gecko/20030323 Phoenix/0.5"
                ,BrowserDetector.X11, BrowserDetector.MOZILLA, "1.4a", new Float(1.4)});

        tryAgent(new Object[] {
                "Mozilla/5.0 (Macintosh; N; PPC; en-US; m18) Gecko/20010131 Netscape6/6.01"
                ,BrowserDetector.MACINTOSH, BrowserDetector.MOZILLA, "m18", new Float(0.18)});
    }

    private void tryAgent(Object[] array1) {
        BrowserDetector parser = new BrowserDetector();
        parser.setUserAgentString((String) array1[0]);
        Object[] result = new Object[] { parser.getUserAgentString(), parser.getBrowserPlatform(), parser.getBrowserName(),
                parser.getBrowserVersionString(), new Float(parser.getBrowserVersion()) };
        assertTrue(array1.length == result.length);
        for (int j = 0; j < array1.length; j++) {
            assertEquals(array1[j], result[j]);
        }
    }

}
