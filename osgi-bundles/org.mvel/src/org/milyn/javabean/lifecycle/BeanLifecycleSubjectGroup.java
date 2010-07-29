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

import org.milyn.assertion.AssertArgument;
import org.milyn.container.ExecutionContext;
import org.milyn.javabean.repository.BeanId;
import org.milyn.javabean.repository.BeanRepositoryManager;

public class BeanLifecycleSubjectGroup {

	private final BeanLifecycleSubject beginLifecycleNotifier;

	private final BeanLifecycleSubject changeLifecycleNotifier;

	private final BeanId beanId;

	private final ExecutionContext executionContext;

	/**
	 * @param beanId
	 */
	public BeanLifecycleSubjectGroup(ExecutionContext executionContext, BeanId beanId) {
		AssertArgument.isNotNull(executionContext, "executionContext");
    	AssertArgument.isNotNull(beanId, "beanId");

		this.executionContext = executionContext;
		this.beanId = beanId;

		beginLifecycleNotifier = new BeanLifecycleSubject(executionContext, BeanLifecycle.BEGIN, beanId);
		changeLifecycleNotifier = new BeanLifecycleSubject(executionContext, BeanLifecycle.CHANGE, beanId);
	}

	/**
	 * @param beanId
	 * @deprecated Use the constructor
	 */
	@Deprecated
	public BeanLifecycleSubjectGroup(ExecutionContext executionContext, String beanId) {
		this(executionContext, getBeanId(executionContext, beanId));
	}

	/**
	 * @deprecated Use the {@link #addObserver(BeanLifecycle, String, boolean, BeanRepositoryLifecycleObserver)}
	 */
	@Deprecated
	public void addObserver(BeanLifecycle lifecycle, String observerId, boolean notifyOnce, BeanLifecycleObserver observer) {
		getBeanObserverNotifier(lifecycle).addObserver(observerId, notifyOnce, observer);

    }

	public void addObserver(BeanLifecycle lifecycle, String observerId, boolean notifyOnce, BeanRepositoryLifecycleObserver observer) {
		getBeanObserverNotifier(lifecycle).addObserver(observerId, notifyOnce, observer);

    }

    public void removeObserver(BeanLifecycle lifecycle, String observerId) {
    	getBeanObserverNotifier(lifecycle).removeObserver(observerId);

    }

    public void notifyObservers(BeanLifecycle lifecycle, Object bean) {
    	getBeanObserverNotifier(lifecycle).notifyObservers(bean);

    }

    protected BeanLifecycleSubject getBeanObserverNotifier(BeanLifecycle lifecycle) {

    	switch (lifecycle) {
		case BEGIN:
			return beginLifecycleNotifier;
		case CHANGE:
			return changeLifecycleNotifier;

		default:
			throw new IllegalArgumentException("Unknown BeanLifecycle '" + lifecycle + "'");
		}

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
	 * @param executionContext
	 * @param beanId
	 * @return
	 */
	private static BeanId getBeanId(ExecutionContext executionContext, String beanId) {
		AssertArgument.isNotNullAndNotEmpty(beanId, "beanId");

		return BeanRepositoryManager.getInstance(executionContext.getContext()).getBeanIdRegister().getBeanId(beanId);
	}
}
