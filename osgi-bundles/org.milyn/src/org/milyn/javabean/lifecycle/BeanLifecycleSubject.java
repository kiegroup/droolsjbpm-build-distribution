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

package org.milyn.javabean.lifecycle;

import java.util.ArrayList;
import java.util.List;

import org.milyn.assertion.AssertArgument;
import org.milyn.container.ExecutionContext;
import org.milyn.javabean.repository.BeanId;
import org.milyn.javabean.repository.BeanRepositoryManager;

public class BeanLifecycleSubject {

    private final ArrayList<ObserverContext> observers = new ArrayList<ObserverContext>();

    private final BeanLifecycle beanLifecycle;

    private final BeanId beanId;

    private final ExecutionContext executionContext;

    public BeanLifecycleSubject(ExecutionContext executionContext, BeanLifecycle beanLifecycle, BeanId beanId) {
    	AssertArgument.isNotNull(executionContext, "executionContext");
    	AssertArgument.isNotNull(beanLifecycle, "beanLifecycle");
    	AssertArgument.isNotNull(beanId, "beanId");

    	this.beanLifecycle = beanLifecycle;
    	this.executionContext = executionContext;
		this.beanId = beanId;
	}

    /**
     *
     * @param executionContext
     * @param beanLifecycle
     * @param beanId
     * @deprecated Use the {@link #BeanLifecycleSubject(ExecutionContext, BeanLifecycle, BeanId)} constructor
     */
    @Deprecated
    public BeanLifecycleSubject(ExecutionContext executionContext, BeanLifecycle beanLifecycle, String beanId) {
    	this(executionContext, beanLifecycle, getBeanId(executionContext, beanId));
	}

    /**
     *
     * @param observerId
     * @param notifyOnce
     * @param observer
     * @deprecated Us the {@link #addObserver(String, boolean, BeanRepositoryLifecycleObserver)}
     */
    @Deprecated
    public void addObserver(String observerId, boolean notifyOnce, BeanLifecycleObserver observer) {
    	AssertArgument.isNotNullAndNotEmpty(observerId, "observerId");
    	AssertArgument.isNotNull(observer, "observer");

    	removeObserver(observerId);

    	ObserverContext observerContext = new ObserverContext();
    	observerContext.observerId = observerId;
    	observerContext.observer = observer;
    	observerContext.notifyOnce = notifyOnce;

    	observers.add(observerContext);

    }

    public void addObserver(String observerId, boolean notifyOnce, BeanRepositoryLifecycleObserver observer) {
    	AssertArgument.isNotNullAndNotEmpty(observerId, "observerId");
    	AssertArgument.isNotNull(observer, "observer");

    	removeObserver(observerId);

    	ObserverContext observerContext = new ObserverContext();
    	observerContext.observerId = observerId;
    	observerContext.repositoryBeanLifecycleObserver = observer;
    	observerContext.notifyOnce = notifyOnce;

    	observers.add(observerContext);

    }

    public void removeObserver(String observerId) {
    	AssertArgument.isNotNullAndNotEmpty(observerId, "observerId");

    	boolean found = false;
    	for (int i = 0; !found && i < observers.size(); i++) {
    		ObserverContext observerContext = observers.get(i);

    		found = observerContext.observerId.equals(observerId);
    		if(found) {
    			observers.remove(i);
    		}
		}

    }

    @SuppressWarnings({ "unchecked", "deprecation" })
	public void notifyObservers(Object bean) {
    	if(observers.size() > 0) {

			List<ObserverContext> observersClone = (List<ObserverContext>) observers.clone();
			for(int i = 0; i < observersClone.size(); i++) {
				ObserverContext observerContext = observersClone.get(i);

				if(observerContext.repositoryBeanLifecycleObserver != null) {

					BeanRepositoryLifecycleEvent beanLifecycleEvent = new BeanRepositoryLifecycleEvent(executionContext, beanLifecycle, beanId, bean);

					observerContext.repositoryBeanLifecycleObserver.onBeanLifecycleEvent(beanLifecycleEvent);

				} else {

					observerContext.observer.onBeanLifecycleEvent(executionContext, beanLifecycle, beanId.getName(), bean);

				}

				if(observerContext.notifyOnce) {
					removeObserver(observerContext.observerId);
				}

    		}
    	}

    }

	/**
	 * @return the beanLifecycle
	 */
	public BeanLifecycle getBeanLifecycle() {
		return beanLifecycle;
	}

	/**
	 * @return the beanId
	 * @deprecated Don't use anymore, not alternative given yet (tell us if you need it)
	 */
	@Deprecated
	public String getBeanId() {
		return beanId.getName();
	}

	/**
	 * @return the executionContext
	 */
	public ExecutionContext getExecutionContext() {
		return executionContext;
	}

    /**
     * The context around on observer. The enabled property indicates
     * if this observer is enabled and can be notified.
     *
     * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
     */
    private class ObserverContext {

    	String observerId;

    	boolean notifyOnce = false;

    	@Deprecated
    	BeanLifecycleObserver observer;

    	BeanRepositoryLifecycleObserver repositoryBeanLifecycleObserver;
    }

    /**
	 * @param executionContext
	 * @param beanId
	 * @return
	 */
	private static BeanId getBeanId(ExecutionContext executionContext, String beanId) {
		AssertArgument.isNotNullAndNotEmpty(beanId, "beanId");

		return BeanRepositoryManager.getInstance(executionContext.getContext()).getBeanIdRegister().getBeanId(beanId);
	}
}
