/*
	Milyn - Copyright (C) 2006

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License (version 2.1) as published by the Free Software
	Foundation.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

	See the GNU Lesser General Public License for more details:
	http://www.gnu.org/licenses/lgpl.txt
*/
package org.milyn.delivery.sax;

import org.xml.sax.Attributes;

/**
 * SAX utility methods.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public abstract class SAXUtil {

    /**
     * Get the value of the named attribute.
     * @param attributeName The attribute name.
     * @param attributes The attribute list.
     * @return The attribute value, or an empty string if not available (as with DOM).
     */
    public static String getAttribute(String attributeName, Attributes attributes) {
        return getAttribute(attributeName, attributes, "");
    }

    /**
     * Get the value of the named attribute.
     * @param attributeName The attribute name.
     * @param attributes The attribute list.
     * @param defaultVal The default value, if the attribute is not set.
     * @return The attribute value, or an empty string if not available (as with DOM).
     */
    public static String getAttribute(String attributeName, Attributes attributes, String defaultVal) {
        return getAttribute("", attributeName, attributes, defaultVal);
    }

    /**
     * Get the value of the named attribute.
     * @param attributeNamespace The attribute namespace.
     * @param attributeName The attribute name.
     * @param attributes The attribute list.
     * @param defaultVal The default value, if the attribute is not set.
     * @return The attribute value, or an empty string if not available (as with DOM).
     */
    public static String getAttribute(String attributeNamespace, String attributeName, Attributes attributes, String defaultVal) {
        int attribCount = attributes.getLength();

        for(int i = 0; i < attribCount; i++) {
            String attribName = attributes.getLocalName(i);
            if(attribName.equalsIgnoreCase(attributeName)) {
                if(attributes.getURI(i).equals(attributeNamespace)) {
                    return attributes.getValue(i);
                }
            }
        }

        return defaultVal;
    }

    public static String getXPath(SAXElement element) {
        StringBuilder builder = new StringBuilder();

        addXPathElement(element, builder);

        return builder.toString();
    }

    private static void addXPathElement(SAXElement element, StringBuilder builder) {
        if(builder.length() > 0) {
            builder.insert(0, "/");
            builder.insert(0, element.getName().getLocalPart());
        } else {
            builder.append(element.getName().getLocalPart());
        }

        SAXElement parent = element.getParent();
        if(parent != null) {
            addXPathElement(parent, builder);
        }
    }

    public static int getDepth(SAXElement element) {
        int depth = 0;

        SAXElement parent = element.getParent();
        while(parent != null) {
            depth++;
            parent = parent.getParent();
        }

        return depth;
    }
}
