package org.anodyneos.servlet.email;

import org.w3c.dom.Element;

public class CommandFactoryImpl implements CommandFactory {

    public CommandFactoryImpl() {
        // super();
    }

    public Command getHandlerFor(Element el) {
        if (el.getNodeName().equals("email")) {
            return EmailCmd.getInstance();
        } else {
            return null;
        }
    }

}
