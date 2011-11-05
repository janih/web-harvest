package org.webharvest;

import net.sf.saxon.om.NamespaceResolver;
import org.webharvest.utils.Stack;
import org.webharvest.utils.XmlUtil;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class XmlNamespaceUtils {

    @SuppressWarnings({"finally", "ReturnInsideFinallyBlock"})
    public static NamespaceResolver getNamespaceResolverFromBrokenXml(String xmlHeadFragment) {
        final Map<String, Stack<String>> nsMap = new HashMap<String, Stack<String>>();
        try {
            XmlUtil.getSAXParserFactory(false, true).
                    newSAXParser().
                    parse(new InputSource(new StringReader(xmlHeadFragment)), new DefaultHandler() {
                        @Override public void startPrefixMapping(String prefix, String uri) throws SAXException {
                            Stack<String> stack = nsMap.get(prefix);
                            if (stack == null) {
                                stack = new Stack<String>();
                                nsMap.put(prefix, stack);
                            }
                            stack.push(uri);
                        }

                        @Override public void endPrefixMapping(String prefix) throws SAXException {
                            final Stack<String> stack = nsMap.get(prefix);
                            if (stack.size() > 1) {
                                stack.pop();
                            } else {
                                nsMap.remove(prefix);
                            }
                        }
                    });
        } finally {
            return new NamespaceResolver() {
                @Override public String getURIForPrefix(String prefix, boolean useDefault) {
                    if (useDefault || prefix.length() > 0) {
                        final Stack<String> stack = nsMap.get(prefix);
                        return stack == null ? null : stack.peek();
                    } else {
                        return null;
                    }
                }

                @Override public Iterator iteratePrefixes() {
                    return nsMap.keySet().iterator();
                }
            };
        }
    }

}
