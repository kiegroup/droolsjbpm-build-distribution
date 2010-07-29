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

import java.util.*;

/**
 * Collections Utilities.
 * 
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class CollectionsUtil {

    /**
     * Private constructor.
     */
    private CollectionsUtil() {
    }

    /**
     * Create an Object {@link Set} from the supplied objects.
     * @param objects The objects to be added to the set.
     * @return The {@link Set}.
     */
    public static <T> Set<T> toSet(T... objects) {
        Set<T> theSet = new HashSet<T>();
        addToCollection(theSet, objects);
        return theSet;
    }

    /**
     * Create an Object {@link List} from the supplied objects.
     * @param objects The objects to be added to the list.
     * @return The {@link List}.
     */
    public static <T> List<T> toList(T... objects) {
        List<T> theSet = new ArrayList<T>();
        addToCollection(theSet, objects);
        return theSet;
    }

    private static <T> void addToCollection(Collection<T> theCollection, T... objects) {
        for(T object : objects) {
            theCollection.add(object);
        }
    }
}
