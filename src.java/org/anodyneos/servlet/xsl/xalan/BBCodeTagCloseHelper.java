package org.anodyneos.servlet.xsl.xalan;

import java.util.ArrayList;
import java.util.List;

public class BBCodeTagCloseHelper {

    private BBCodeParser parser;
    private List tagStack = new ArrayList();
    private List alreadyClosedList = new ArrayList();

    /*
    - Starting a tag is always legal (can all tags be nested?)
    - Closing a tag
        - If tag specified matches most recently opened tag, close it.
        - Elseif most recently started tag != tag specified
            - If tag specified is in the tag close ignore list, remove from tag close ignore list
            - Elseif tag specified was opened at some point, just not most recently:
                - Close all tags in the stack up to and including the specified tag
                - For all tags that were closed other than the specified tag, add to tag close ignore list
            - Elseif tag specified was never opened, execute badTag(....)
        - Consider reopening some tags?
     */

    public BBCodeTagCloseHelper(BBCodeParser parser) {
        this.parser = parser;
    }

    public void openTag(String tag) {
        tagStack.add(tag);
    }

    public void closeTag(String tag) {
        int alreadyClosedIndex = alreadyClosedList.indexOf(tag);
        if (alreadyClosedIndex != -1) {
            alreadyClosedList.remove(alreadyClosedIndex);
            // throw tag away
        } else if (tagStack.isEmpty()) {
            // throw tag away?
        } else if (tag.equals(tagStack.get(tagStack.size()-1))) {
            parser.processCloseTag(tag);
            tagStack.remove(tagStack.size()-1);
        } else if (tagStack.lastIndexOf(tag) != -1) {
            for (int currentIndex = tagStack.size() - 1; currentIndex > -1; currentIndex--) {
                String currentTag = (String) tagStack.get(currentIndex);
                parser.processCloseTag(currentTag);
                tagStack.remove(currentIndex);
                if (currentTag.equals(tag)) {
                    break;
                } else {
                    alreadyClosedList.add(currentTag);
                }
            }
        }
    }

    public void endDocument() {
        for (int currentIndex = tagStack.size() - 1; currentIndex > -1; currentIndex--) {
            String currentTag = (String) tagStack.get(currentIndex);
            parser.processCloseTag(currentTag);
            tagStack.remove(currentIndex);
        }
    }

}
