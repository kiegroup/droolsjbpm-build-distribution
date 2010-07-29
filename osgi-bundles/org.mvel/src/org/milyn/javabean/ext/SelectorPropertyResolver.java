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
package org.milyn.javabean.ext;

import org.milyn.*;
import org.milyn.javabean.BeanInstancePopulator;
import org.milyn.cdr.*;
import org.milyn.cdr.extension.*;
import org.milyn.container.*;
import org.milyn.delivery.dom.*;
import org.w3c.dom.*;

/**
 * Selector Property Resolver.
 * <p/>
 * Some binding selectors can be of the form "order/customer/@customerNumber", where the
 * last token in the selector represents an attribute on the customer element (for example).  This
 * extension visitor translates this type of selector into "order/customer" plus a new property
 * on the BeanInstancePopulator config named "valueAttributeName" containing a value of
 * "customerNumber".
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class SelectorPropertyResolver implements DOMVisitBefore {

    public void visitBefore(Element element, ExecutionContext executionContext) throws SmooksException {
        ExtensionContext extensionContext = ExtensionContext.getExtensionContext(executionContext);
        SmooksResourceConfiguration populatorConfig = extensionContext.getResourceStack().peek();
        resolveSelectorTokens(populatorConfig);
    }

    public static void resolveSelectorTokens(SmooksResourceConfiguration populatorConfig) {
        String[] selectorTokens = populatorConfig.getContextualSelector();
        String valueAttributeName = SmooksResourceConfiguration.extractTargetAttribute(selectorTokens);

        if(valueAttributeName != null && !valueAttributeName.trim().equals("")) {
            populatorConfig.setSelector(getSelectorProperty(selectorTokens));
            populatorConfig.setParameter(BeanInstancePopulator.VALUE_ATTRIBUTE_NAME, valueAttributeName);
        }
    }

    public static String getSelectorProperty(String[] selectorTokens) {
        StringBuffer selectorProp = new StringBuffer();

        for (String selectorToken : selectorTokens) {
            if (!selectorToken.trim().startsWith("@")) {
                selectorProp.append(selectorToken).append(" ");
            }
        }

        return selectorProp.toString().trim();
    }

}