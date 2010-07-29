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
package org.milyn.delivery.sax;

import org.milyn.container.ExecutionContext;
import org.milyn.delivery.ExecutionLifecycleCleanableList;
import org.milyn.delivery.ExecutionLifecycleCleanable;

import java.util.ArrayList;
import java.util.List;

/**
 * Dynamic SAX Element Visitor list.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class DynamicSAXElementVisitorList {

    private List<SAXVisitBefore> visitBefores = new ArrayList<SAXVisitBefore>();
    private List<SAXVisitChildren> childVisitors = new ArrayList<SAXVisitChildren>();
    private List<SAXVisitAfter> visitAfters = new ArrayList<SAXVisitAfter>();
    private ExecutionLifecycleCleanableList cleanupList;

    public DynamicSAXElementVisitorList(ExecutionContext executionContext, ExecutionLifecycleCleanableList cleanupList) {
        executionContext.setAttribute(DynamicSAXElementVisitorList.class, this);
        this.cleanupList = cleanupList;
    }

    public List<SAXVisitBefore> getVisitBefores() {
        return visitBefores;
    }

    public List<SAXVisitChildren> getChildVisitors() {
        return childVisitors;
    }

    public List<SAXVisitAfter> getVisitAfters() {
        return visitAfters;
    }
    
    public static DynamicSAXElementVisitorList getList(ExecutionContext executionContext) {
        return (DynamicSAXElementVisitorList) executionContext.getAttribute(DynamicSAXElementVisitorList.class);
    }

    public static void addDynamicVisitor(SAXVisitor visitor, ExecutionContext executionContext) {
        DynamicSAXElementVisitorList list = getList(executionContext);

        if(visitor instanceof SAXVisitBefore) {
            list.visitBefores.add((SAXVisitBefore) visitor);
        }
        if(visitor instanceof SAXVisitChildren) {
            list.childVisitors.add((SAXVisitChildren) visitor);
        }
        if(visitor instanceof SAXVisitAfter) {
            list.visitAfters.add((SAXVisitAfter) visitor);
        }

        if(visitor instanceof ExecutionLifecycleCleanable) {
            list.cleanupList.add((ExecutionLifecycleCleanable) visitor);
        }
    }

    public static void removeDynamicVisitor(SAXVisitor visitor, ExecutionContext executionContext) {
        DynamicSAXElementVisitorList list = getList(executionContext);

        list.visitBefores.remove(visitor);
        list.childVisitors.remove(visitor);
        list.visitAfters.remove(visitor);
    }
}
