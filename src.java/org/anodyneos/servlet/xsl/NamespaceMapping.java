package org.anodyneos.servlet.xsl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

/**
 * Can be used to track prefix to namespace mappings when implementing a SAX
 * content handler. As a content handler recieves <code>startPrefixMapping</code>
 * and <code>endPrefixMapping</code> calls, the content handler should call
 * <code>push</code> and <code>pop</code> on an instance of this class.
 * <code>peek</code> may be called at any time to see the current namespace
 * uri that is mapped to a prefix.
 *
 * This class uses a <code>Stack</code> to keep track of multiple uri
 * mappings for a prefix. Prefixes may be any string including the empty
 * string.
 *
 * @author jvas
 */
public class NamespaceMapping {

    /**
     * Keys are prefix names, values are a stack of URIs with the top of the
     * stack representing the current mapping.
     */
    private HashMap prefixMap = new HashMap();

    public NamespaceMapping() {
        super();
    }

    /**
     * Register a new prefix mapping. Normally called by a content handler in
     * its <code>startPrefixMapping</code> method.
     *
     * @param prefix
     * @param uri
     */
    public void push(String prefix, String uri) {
        if (null == prefix) {
            prefix = "";
        }
        if (null == uri) {
            uri = "";
        }
        Stack stack = (Stack) prefixMap.get(prefix);
        if (null == stack) {
            stack = new Stack();
            prefixMap.put(prefix, stack);
        }
        stack.push(uri);
    }

    /**
     * Remove a prefix mapping. Normally called by a content handler in its
     * <code>endPrefixMapping</code> method.
     *
     * @param prefix
     * @return the namespace uri being popped.
     */
    public String pop(String prefix) {
        Stack stack = (Stack) prefixMap.get(prefix);
        if (null == stack) {
            return "";
        } else {
            String uri = (String) stack.pop();
            if (stack.isEmpty()) {
                prefixMap.remove(prefix);
            }
            return uri;
        }
    }

    /**
     * Returns the current namespace uri for the given prefix.
     *
     * @param prefix
     * @return The namespace uri
     */
    public String peek(String prefix) {
        Stack stack = (Stack) prefixMap.get(prefix);
        if (null == stack) {
            return "";
        } else {
            return (String) stack.peek();
        }
    }

    /**
     * Can be used to determin wether a prefix is currently registered with
     * this class. This method will return false if the prefix has never been
     * registered or if it was registered but all namespace uris have been
     * popped.
     *
     * @param prefix
     * @return
     */
    public boolean prefixExists(String prefix) {
        return null != prefixMap.get(prefix);
    }

    /**
     * Provides reverse lookup; namespace uri -> prefix. The first prefix found
     * will be returned. Multiple calls to this method may return different
     * prefixes if more than one prefix is mapped to the given namespace uri.
     *
     * @param uri
     *            The uri to search for.
     * @return The prefix to use for the uri or null if none exists.
     */
    public String findPrefixForURI(String uri) {
        Iterator it = prefixMap.keySet().iterator();
        while (it.hasNext()) {
            String prefix = (String) it.next();
            Stack stack = (Stack) prefixMap.get(prefix);
            if (uri.equals((String) stack.peek())) { return prefix; }
        }
        return null;
    }
}
