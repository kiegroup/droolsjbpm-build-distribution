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
package org.milyn.payload;

import com.thoughtworks.xstream.XStream;
import org.milyn.assertion.AssertArgument;
import org.milyn.payload.FilterResult;

import javax.xml.transform.Result;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Java filtration/transformation result.
 * <p/>
 * Used to extract a Java "{@link Result result}" Map from the transformation.
 * Simply set an instance of this class as the {@link Result} arg in the call
 * to {@link org.milyn.Smooks#filterSource(org.milyn.container.ExecutionContext, javax.xml.transform.Source, javax.xml.transform.Result[])}.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class JavaResult extends FilterResult {
    
    private Map<String, Object> resultMap;

    /**
     * Public default constructor.
     */
    public JavaResult() {
        this(false);
    }
    
    /**
     * Public default constructor.
     */
    public JavaResult(boolean preserveOrder) {
    	if(preserveOrder) {
    		resultMap = new LinkedHashMap<String, Object>();
    	} else {
    		resultMap = new HashMap<String, Object>();
    	}
    }
    

    /**
     * Public constructor.
     * <p/>
     * See {@link #setResultMap(java.util.Map)}.
     * 
     * @param resultMap Result Map. This is the map onto which Java "result" objects will be set.
     */
    public JavaResult(Map<String, Object> resultMap) {
        AssertArgument.isNotNull(resultMap, "resultMap");
        this.resultMap = resultMap;
    }

    /**
     * Get the named bean from the Java Result Map.
     * @param name the name of the bean.
     * @return The bean Object, or null if the bean is not in the bean Result Map.
     * @see #getResultMap()
     */
    public Object getBean(String name) {
        return resultMap.get(name);
    }

    /**
     * Get the Java result map.
     * @return The Java result map.
     * @see #getBean(String)
     */
    public Map<String, Object> getResultMap() {
        return resultMap;
    }

    /**
     * Set the Java result map.
     * @param resultMap The Java result map.
     */
    public void setResultMap(Map<String, Object> resultMap) {
        this.resultMap = resultMap;
    }

    /**
     * XML Serialized form of the bean Map associate with the
     * result instance.
     * @return XML Serialized form of the bean Map associate with the
     * result instance.
     */
    public String toString() {
        StringWriter stringBuilder = new StringWriter();
        XStream xstream = new XStream();

        if(resultMap != null && !resultMap.isEmpty()) {
            Set<Map.Entry<String, Object>> entries = resultMap.entrySet();

            for (Map.Entry<String, Object> entry : entries) {
                stringBuilder.write(entry.getKey() + ":\n");
                stringBuilder.write(xstream.toXML(entry.getValue()) + "\n\n");
            }
        }

        return stringBuilder.toString();
    }
}
