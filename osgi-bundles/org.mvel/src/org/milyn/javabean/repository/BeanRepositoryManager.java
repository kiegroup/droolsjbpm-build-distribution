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

import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.milyn.container.ApplicationContext;
import org.milyn.container.ExecutionContext;
import org.milyn.payload.FilterResult;
import org.milyn.payload.FilterSource;
import org.milyn.payload.JavaResult;
import org.milyn.payload.JavaSource;

/**
 * The Bean Repository Manager
 * <p/>
 * Manages the {@link BeanRepository} of the current {@link ExecutionContext} and the {@link BeanIdRegister}
 * of the current {@link ApplicationContext}. It ensures that both objects are correctly instantiated.
 *
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class BeanRepositoryManager {

	private static final String CONTEXT_KEY = BeanRepositoryManager.class.getName() + "#CONTEXT_KEY";

	private static final String BEAN_REPOSITORY_CONTEXT_KEY = BeanRepository.class.getName() + "#CONTEXT_KEY";

	private final BeanIdRegister beanIdRegister = new BeanIdRegister();

	/**
	 * Returns the instance of the {@link BeanRepositoryManager}, which is bound to the
	 * given {@link ApplicationContext}. If the {@link BeanRepositoryManager} doesn't
	 * exist yet, then one is created.
	 *
	 * @param applicationContext The {@link ApplicationContext} to which the instance is bound
	 * @return The {@link BeanRepositoryManager} instance of the given {@link ApplicationContext}
	 */
	public static BeanRepositoryManager getInstance(ApplicationContext applicationContext) {
		BeanRepositoryManager beanRepositoryManager = (BeanRepositoryManager) applicationContext.getAttribute(CONTEXT_KEY);

		if(beanRepositoryManager == null) {

			beanRepositoryManager = new BeanRepositoryManager();

			applicationContext.setAttribute(CONTEXT_KEY, beanRepositoryManager);

		}

		return beanRepositoryManager;

	}

	/**
	 * The object can only be instantiated with the {@link #getInstance(ApplicationContext)} method.
	 */
	private BeanRepositoryManager() {
	}


	/**
	 * @return the {@link BeanIdRegister} of the bound {@link ApplicationContext}
	 */
	public BeanIdRegister getBeanIdRegister() {
		return beanIdRegister;
	}

	/**
	 * @return the {@link BeanRepository} of the given {@link ExecutionContext}. If the {@link BeanRepository} does not
	 * 			exist then one is created. The {@link BeanIdRegister} which is bound to the {@link ApplicationContext}
	 * 			of the given {@link ExecutionContext} is bound to the created {@link BeanRepository}.
	 */
	public static BeanRepository getBeanRepository(ExecutionContext executionContext) {
		BeanRepository beanRepository = (BeanRepository) executionContext.getAttribute(BEAN_REPOSITORY_CONTEXT_KEY);

		if(beanRepository == null) {

			beanRepository = getInstance(executionContext.getContext()).createBeanRepository(executionContext);

			executionContext.setAttribute(BEAN_REPOSITORY_CONTEXT_KEY, beanRepository);
		}

		return beanRepository;
	}

	/**
	 * Creates the BeanRepository
	 *
	 * @param executionContext The {@link ExecutionContext} to which the {@link BeanRepository} is bound
	 * @return The new BeanRepository
	 */
	private BeanRepository createBeanRepository(ExecutionContext executionContext) {
		BeanRepository beanRepository;

		Map<String, Object> beanMap = createBeanMap(executionContext);

		beanRepository = new BeanRepository(executionContext, beanIdRegister, beanMap);

		return beanRepository;
	}


	/**
	 * Returns the BeanMap which must be used by the {@link BeanRepository}. If
	 * a JavaResult or a JavaSource is used with the {@link ExecutionContext} then
	 * those are used in the creation of the Bean map.
	 *
	 * Bean's that are already in the JavaResult or JavaSource map are given
	 * a {@link BeanId} in the {@link BeanIdRegister}.
	 *
	 * @param executionContext
	 * @return
	 */
	private Map<String, Object> createBeanMap(ExecutionContext executionContext) {
		Result result = FilterResult.getResult(executionContext, JavaResult.class);
		Source source = FilterSource.getSource(executionContext);
		Map<String, Object> beanMap = null;

		if(result != null) {
		    JavaResult javaResult = (JavaResult) result;
		    beanMap = javaResult.getResultMap();
		}

		if(source instanceof JavaSource) {
		    JavaSource javaSource = (JavaSource) source;
		    Map<String, Object> sourceBeans = javaSource.getBeans();

		    if(sourceBeans != null) {
		        if(beanMap != null) {
		            beanMap.putAll(sourceBeans);
		        } else {
		            beanMap = sourceBeans;
		        }
		    }
		}

		if(beanMap == null) {
			beanMap = new HashMap<String, Object>();
		} else {

			for(String beanId : beanMap.keySet()) {

				if(!beanIdRegister.containsBeanId(beanId)) {
					beanIdRegister.register(beanId);
				}

	        }

		}
		return beanMap;
	}

}
