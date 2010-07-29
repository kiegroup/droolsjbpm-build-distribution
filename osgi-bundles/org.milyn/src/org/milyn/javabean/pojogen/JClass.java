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
package org.milyn.javabean.pojogen;

import org.milyn.assertion.AssertArgument;
import org.milyn.util.FreeMarkerTemplate;
import org.milyn.io.StreamUtils;

import java.util.*;
import java.io.Writer;
import java.io.IOException;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CannotCompileException;
import javassist.NotFoundException;

/**
 * Java POJO model.
 * @author bardl
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class JClass {

    private String packageName;
    private String className;
    private Class<?> skeletonClass;
    private List<JNamedType> properties = new ArrayList<JNamedType>();
    private List<JMethod> methods = new ArrayList<JMethod>();
    private boolean fluentSetters = true;

    private static FreeMarkerTemplate template;

    static {
        try {
            template = new FreeMarkerTemplate(StreamUtils.readStreamAsString(JClass.class.getResourceAsStream("JavaClass.ftl")));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load JavaClass.ftl FreeMarker template.", e);
        }
    }

    public JClass(String packageName, String className) {
        AssertArgument.isNotNull(packageName, "packageName");
        AssertArgument.isNotNull(className, "className");
        this.packageName = packageName;
        this.className = className;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getClassName() {
        return className;
    }

    public void setFluentSetters(boolean fluentSetters) {
        this.fluentSetters = fluentSetters;
    }

    public Class<?> getSkeletonClass() {
        if(skeletonClass == null) {
            String skeletonClassName = packageName + "." + className;

            try {
                skeletonClass = Thread.currentThread().getContextClassLoader().loadClass(skeletonClassName);
            } catch (ClassNotFoundException e) {
                ClassPool pool = ClassPool.getDefault();
                CtClass cc = pool.makeClass(skeletonClassName);

                try {
                    skeletonClass = cc.toClass();
                } catch (CannotCompileException ee) {
                    throw new IllegalStateException("Unable to create runtime skeleton class for class '" + skeletonClassName + "'.", ee);
                }
            }
        }
        
        return skeletonClass;
    }

    public JClass addProperty(JNamedType property) {
        AssertArgument.isNotNull(property, "property");
        assertPropertyUndefined(property);

        properties.add(property);

        String propertyName = property.getName();
        String capitalizedPropertyName = Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);

        // Add property getter method...
        JMethod getterMethod = new JMethod(property.getType(), "get" + capitalizedPropertyName);
        getterMethod.setBody("return " + property.getName() + ";");
        methods.add(getterMethod);

        // Add property setter method...
        if(fluentSetters) {
            JMethod setterMethod = new JMethod(new JType(getSkeletonClass()), "set" + capitalizedPropertyName);
            setterMethod.addParameter(property);
            setterMethod.setBody("this." + property.getName() + " = " + property.getName() + ";  return this;");
            methods.add(setterMethod);
        } else {
            JMethod setterMethod = new JMethod("set" + capitalizedPropertyName);
            setterMethod.addParameter(property);
            setterMethod.setBody("this." + property.getName() + " = " + property.getName() + ";");
            methods.add(setterMethod);
        }

        return this;
    }

    public List<JNamedType> getProperties() {
        return properties;
    }

    public List<JMethod> getMethods() {
        return methods;
    }

    public Set<Class<?>> getImports() {
        Set<Class<?>> importSet = new LinkedHashSet<Class<?>>();

        for(JNamedType property : properties) {
            property.getType().addImports(importSet, new String[] {"java.lang", packageName});
        }

        return importSet;
    }

    public void writeClass(Writer writer) throws IOException {
        Map<String, JClass> contextObj = new HashMap<String, JClass>();

        contextObj.put("class", this);
        writer.write(template.apply(contextObj));
    }

    private void assertPropertyUndefined(JNamedType property) {
        for(JNamedType definedProperty : properties) {
            if(property.getName().equals(definedProperty.getName())) {
                throw new IllegalArgumentException("Property '" + property.getName() + "' already defined.");
            }
        }
    }
}