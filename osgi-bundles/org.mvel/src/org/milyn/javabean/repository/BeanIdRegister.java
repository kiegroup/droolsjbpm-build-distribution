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

package org.milyn.javabean.repository;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.milyn.assertion.AssertArgument;

/**
 * Bean Id List
 * <p/>
 * Represents a map of BeanId's. Every BeanId has it own unique index. The index
 * is incremental. The index starts with zero.
 * <p/>
 * Once a BeanId is registered it can never be unregistered.
 *
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class BeanIdRegister {

	private int index = 0;

	private final Map<String, BeanId> beanIdMap = new HashMap<String, BeanId>();

	/**
	 * registers a beanId name and returns the {@link BeanId} object.
	 * If the beanId name is already registered then belonging BeanId
	 * is returned.
	 * <p>
	 * If you are sure that the BeanId is already registered
	 * then use the {@link #getBeanId(String)} method to retrieve it,
	 * because it is faster.
	 *
	 */
	public synchronized BeanId register(String beanIdName) {
		AssertArgument.isNotEmpty(beanIdName, "beanIdName");

		BeanId beanId = beanIdMap.get(beanIdName);
		if(beanId == null) {
			int id = index++;

			beanId = new BeanId(this, id, beanIdName);

			beanIdMap.put(beanIdName, beanId);
		}
		return beanId;
	}

	/**
	 * @return The BeanId or <code>null</code> if it is not registered;
	 *
	 */
	public BeanId getBeanId(String beanId) {
		return beanIdMap.get(beanId);
	}

	/**
	 * @return if the bean Id name is already registered.
	 *
	 */
	public boolean containsBeanId(String beanId) {
		return beanIdMap.containsKey(beanId);
	}

	/**
	 * @return An unmodifiable map where the key is the
	 * string based beanId and the value is the BeanId.
	 *
	 */
	public Map<String, BeanId> getBeanIdMap() {
		return Collections.unmodifiableMap(beanIdMap) ;
	}

	/**
	 * @return the current size of the map.
	 *
	 */
	public int size() {
		return index;
	}
}
