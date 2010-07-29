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
package org.milyn.util;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.milyn.assertion.AssertArgument;
import org.milyn.classpath.InstanceOfFilter;
import org.milyn.classpath.IsAnnotationPresentFilter;
import org.milyn.classpath.Scanner;

/**
 * Utility methods to aid in class/resource loading.
 *
 * @author Kevin Conner
 */
public class ClassUtil {

    private static Log logger = LogFactory.getLog(ClassUtil.class);
    private static final Map<String, Class> primitives;

    static {
        primitives = new HashMap<String, Class>();
        primitives.put("int", Integer.TYPE);
        primitives.put("long", Long.TYPE);
        primitives.put("boolean", Boolean.TYPE);
        primitives.put("float", Float.TYPE);
        primitives.put("double", Double.TYPE);
        primitives.put("char", Character.TYPE);
        primitives.put("byte", Byte.TYPE);
        primitives.put("short", Short.TYPE);
    }

    /**
	 * Load the specified class.
	 *
	 * @param className
	 *            The name of the class to load.
	 * @param caller
	 *            The class of the caller.
	 * @return The specified class.
	 * @throws ClassNotFoundException
	 *             If the class cannot be found.
	 */
	public static Class forName(final String className, final Class caller) throws ClassNotFoundException {
		final ClassLoader threadClassLoader = Thread.currentThread().getContextClassLoader();

        Class primitiveClass = primitives.get(className);
        if(primitiveClass != null) {
            return primitiveClass;
        }

        if (threadClassLoader != null) {
			try {
				return threadClassLoader.loadClass(className);
			} catch (final ClassNotFoundException cnfe) {
			} // ignore
		}

		final ClassLoader classLoader = caller.getClassLoader();
		if (classLoader != null) {
			try {
				return classLoader.loadClass(className);
			} catch (final ClassNotFoundException cnfe) {
			} // ignore
		}

		return Class.forName(className, true, ClassLoader
				.getSystemClassLoader());
	}

	/**
	 * Get the specified resource as a stream.
	 *
	 * @param resourceName
	 *            The name of the class to load.
	 * @param caller
	 *            The class of the caller.
	 * @return The input stream for the resource or null if not found.
	 */
	public static InputStream getResourceAsStream(final String resourceName, final Class caller) {
		final String resource;
		if (resourceName.startsWith("/")) {
			resource = resourceName.substring(1);
		} else {
			final Package callerPackage = caller.getPackage();
			if (callerPackage != null) {
				resource = callerPackage.getName().replace('.', '/') + '/'
						+ resourceName;
			} else {
				resource = resourceName;
			}
		}
		final ClassLoader threadClassLoader = Thread.currentThread()
				.getContextClassLoader();
		if (threadClassLoader != null) {
			final InputStream is = threadClassLoader
					.getResourceAsStream(resource);
			if (is != null) {
				return is;
			}
		}

		final ClassLoader classLoader = caller.getClassLoader();
		if (classLoader != null) {
			final InputStream is = classLoader.getResourceAsStream(resource);
			if (is != null) {
				return is;
			}
		}

		return ClassLoader.getSystemResourceAsStream(resource);
	}

    public static List<Class> findInstancesOf(final Class type, String[] igrnoreList, String[] includeList) {
        InstanceOfFilter filter = new InstanceOfFilter(type, igrnoreList, includeList);
        return findInstancesOf(type, filter);
    }

    public static List<Class> findInstancesOf(final Class type) {
        InstanceOfFilter filter = new InstanceOfFilter(type);
        return findInstancesOf(type, filter);
    }

    private static List<Class> findInstancesOf(Class type, InstanceOfFilter filter) {
        Scanner scanner = new Scanner(filter);

        try {
            long startTime = System.currentTimeMillis();
            scanner.scanClasspath(Thread.currentThread().getContextClassLoader());
            logger.debug("Scanned classpath for instances of '" + type.getName() + "'.  Found " + filter.getClasses().size() + " matches. Scan took " + (System.currentTimeMillis() - startTime) + "ms.");
        } catch (IOException e) {
            throw new RuntimeException("Failed to search classspath for instances of '" + type.getName() + "'.", e);
        }

        return filter.getClasses();
    }

    public static List<Class> findAnnotatedWith(Class<? extends Annotation> type, String[] igrnoreList, String[] includeList) {
        IsAnnotationPresentFilter filter = new IsAnnotationPresentFilter(type, igrnoreList, includeList);
        return findAnnotatedWith(type, filter);
    }

    public static List<Class> findAnnotatedWith(Class<? extends Annotation> type) {
        IsAnnotationPresentFilter filter = new IsAnnotationPresentFilter(type);
        return findAnnotatedWith(type, filter);
    }

    private static List<Class> findAnnotatedWith(Class<? extends Annotation> type, IsAnnotationPresentFilter filter) {
        Scanner scanner = new Scanner(filter);

        try {
            long startTime = System.currentTimeMillis();
            scanner.scanClasspath(Thread.currentThread().getContextClassLoader());
            logger.debug("Scanned classpath for class annotated with annotation '" + type.getName() + "'.  Found " + filter.getClasses().size() + " matches. Scan took " + (System.currentTimeMillis() - startTime) + "ms.");
        } catch (IOException e) {
            throw new RuntimeException("Failed to search classspath for class annotated with annotation '" + type.getName() + "'.", e);
        }

        return filter.getClasses();
    }

    public static Object newProxyInstance(Class[] classes, InvocationHandler handler) {
        final ClassLoader threadClassLoader = Thread.currentThread().getContextClassLoader();

        if (threadClassLoader != null) {
            return Proxy.newProxyInstance(threadClassLoader, classes, handler);
        } else {
            return Proxy.newProxyInstance(ClassUtil.class.getClassLoader(), classes, handler);
        }
    }

    /**
     * Will try to create a List of classes that are listed
     * in the passed in file.
     * The fileName is expected to be found on the classpath.
     *
     * @param fileName The name of the file containing the list of classes,
     * one class name per line.
     * @param instanceOf The instanceof filter.
     * @return List<Class<T>>	list of the classes contained in the file.
     */
    public static <T> List<Class<T>> getClasses(final String fileName, Class<T> instanceOf) {
    	AssertArgument.isNotNull( fileName, "fileName" );
        AssertArgument.isNotNull( instanceOf, "instanceOf" );

        long start = System.currentTimeMillis();
        List<Class<T>> classes = new ArrayList<Class<T>>();
        Enumeration<URL> cpURLs;
        int resCount = 0;

        try {
            cpURLs = Thread.currentThread().getContextClassLoader().getResources(fileName);
        } catch (IOException e) {
            throw new RuntimeException("Error getting resource URLs for resource : " + fileName, e);
        }

        while (cpURLs.hasMoreElements()) {
            URL url = cpURLs.nextElement();
            addClasses(url, instanceOf, classes);
            resCount++;
        }

        logger.debug("Loaded " + classes.size() + " classes from " + resCount + " URLs through class list file "
                + fileName + ".  Process took " + (System.currentTimeMillis() - start) + "ms.  Turn on debug logging for more info.");

        return classes;
    }

    private static <T>  void addClasses(URL url, Class<T> instanceOf, List<Class<T>> classes) {
        InputStream ins = null;
        BufferedReader br = null;

        try
    	{
            String className;
            int count = 0;

            // Get the input stream from the connection.  Need to set the defaultUseCaches
            URLConnection connection = url.openConnection();
            connection.setDefaultUseCaches(false);
            ins = connection.getInputStream();

            br = new BufferedReader( new InputStreamReader( ins ));
	    	while( (className = br.readLine()) != null )
	    	{
                Class clazz;

                className = className.trim();

                // Ignore blank lines and lines that start with a hash...
                if(className.equals("") || className.startsWith("#")) {
                    continue;
                }

                try {
                    clazz = forName(className, ClassUtil.class);
                } catch (ClassNotFoundException e) {
                    logger.warn("Failed to load class '" + className + "'. Class not found.");
                    continue;
                }

                if(instanceOf.isAssignableFrom(clazz)) {
                    if(!contains(clazz.getName(), classes)) {
                        classes.add(clazz);
                    }
                    logger.debug( "Adding " + className + " to list of classes");
                    count++;
                } else {
                    logger.debug("Not adding class '" + clazz.getName() + "' to list.  Class does not implement/extend '" + instanceOf.getName() + "'.");
                }
            }
            logger.debug("Loaded '" + count + "' classes listed in '" + url + "'.");
    	}
    	catch (IOException e)
		{
            throw new RuntimeException("Failed to read from file : " + url, e);
		}
    	finally
    	{
            close(br);
            close(ins);
        }
    }

    private static <T> boolean contains(String name, List<Class<T>> classes) {
        for (Class<T> aClass : classes) {
            if(aClass.getName().equals(name)) {
                logger.debug("Class '" + name + "' already found on classpath.  Not adding to list.");
                return true;
            }
        }

        return false;
    }

    private static void close( final Closeable closable ) {
    	if(  closable != null )
    	{
			try
			{
				closable.close();
			}
    		catch (IOException e)
			{
    			logger.warn( "Exception while trying to close : " + closable, e);
			}
    	}
    }

    public static String toFilePath(Package aPackage) {
        return "/" + aPackage.getName().replace('.', '/');
    }

    /**
	 * Checks if the class in the first parameter is assignable
	 * to one of the classes in the second or any later parameter.
	 *
	 * @param toFind
	 * @param classes
	 * @return
	 */
	public static boolean containsAssignableClass(final Class<?> toFind, final Class<?> ... classes) {
		return indexOffFirstAssignableClass(toFind, classes) != -1;
	}

    public static <U> void setField(Field field, U instance, Object value) throws IllegalAccessException {
        boolean isAccessible = field.isAccessible();

        if(!isAccessible) {
            field.setAccessible(true);
        }

        try {
            field.set(instance, value);
        } finally {
            field.setAccessible(isAccessible);
        }
    }

    public static <U> Object getField(Field field, U instance) throws IllegalAccessException {
        boolean isAccessible = field.isAccessible();

        if(!isAccessible) {
            field.setAccessible(true);
        }

        try {
            return field.get(instance);
        } finally {
            field.setAccessible(isAccessible);
        }
    }

    /**
	 *
	 * @param toFind
	 * @param classes
	 * @return
	 */
	public static int indexOffFirstAssignableClass(final Class<?> toFind, final Class<?> ... classes) {

		for(int i = 0; i < classes.length; i++) {
			final Class<?> cls = classes[i];

			if(cls.isAssignableFrom(toFind)) {
				return i;
			}

		}
		return -1;
	}

    public static String toSetterName(String property) {
        StringBuffer setterName = new StringBuffer();

        // Add the property string to the buffer...
        setterName.append(property);
        // Uppercase the first character...
        setterName.setCharAt(0, Character.toUpperCase(property.charAt(0)));
        // Prefix with "set"...
        setterName.insert(0, "set");

        return setterName.toString();
    }

    public static Method getSetterMethod(String setterName, Object bean, Class<?> setterParamType) {
        return getSetterMethod(setterName, bean.getClass(), setterParamType);
    }

    public static Method getSetterMethod(String setterName, Class beanclass, Class<?> setterParamType) {
        Method[] methods = beanclass.getMethods();
        Method beanSetterMethod = null;

        for(Method method : methods) {
            if(method.getName().equals(setterName)) {
                Class<?>[] params = method.getParameterTypes();
                if(params != null && params.length == 1 && params[0].isAssignableFrom(setterParamType)) {
                    beanSetterMethod = method;
                    break;
                }
            }
        }

        return beanSetterMethod;
    }
}
