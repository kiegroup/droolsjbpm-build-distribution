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
package org.milyn.delivery;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.milyn.assertion.AssertArgument;
import org.milyn.container.ExecutionContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Cleanup list for {@link ExecutionLifecycleCleanable} implementations.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class ExecutionLifecycleCleanableList {

    private static Log logger = LogFactory.getLog(ExecutionLifecycleCleanableList.class);

    private ExecutionContext executionContext;
    private List<ExecutionLifecycleCleanable> lifecycleCleanables = new ArrayList<ExecutionLifecycleCleanable>();

    public ExecutionLifecycleCleanableList(ExecutionContext executionContext) {
        AssertArgument.isNotNull(executionContext, "executionContext");
        this.executionContext = executionContext;
    }

    public void add(ExecutionLifecycleCleanable cleanable) {
        lifecycleCleanables.remove(cleanable);
        lifecycleCleanables.add(cleanable);
    }

    public void cleanup() {
        for (ExecutionLifecycleCleanable lifecycleCleanable : lifecycleCleanables) {
            try {
                lifecycleCleanable.executeExecutionLifecycleCleanup(executionContext);
            } catch (Throwable t) {
                logger.error("Cleanup failure: " + lifecycleCleanable.getClass().getName(), t);
            }
        }
    }
}
