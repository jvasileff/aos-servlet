package org.anodyneos.servlet.email;

import org.w3c.dom.Element;

public interface CommandFactory {

    Command getHandlerFor(Element el);

}
