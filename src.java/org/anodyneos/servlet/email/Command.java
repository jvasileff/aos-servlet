package org.anodyneos.servlet.email;

import org.w3c.dom.Element;

public interface Command {

    void process(EmailContext ctx, Element el) throws Exception;

}
